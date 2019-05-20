package tubs.cs.studienrichtung.featuremodel;

import de.ovgu.featureide.fm.core.base.FeatureUtils;
import de.ovgu.featureide.fm.core.base.IConstraint;
import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.editing.AdvancedNodeCreator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.prop4j.And;
import org.prop4j.Equals;
import org.prop4j.Implies;
import org.prop4j.Literal;
import org.prop4j.Node;
import org.prop4j.Or;
import org.prop4j.explain.solvers.SatSolver;
import org.prop4j.explain.solvers.SatSolverFactory;
import tubs.cs.satencodings.generation.AtMostSATEncoding;
import tubs.cs.satencodings.generation.VariableFactory;
import tubs.cs.satencodings.generation.featuremodel.FMGenerationHandle;
import tubs.cs.satencodings.util.NodeUtils;
import tubs.cs.satencodings.util.Pair;
import tubs.cs.satencodings.util.Permutation;
import tubs.cs.studienrichtung.Branch;
import tubs.cs.studienrichtung.Category;
import tubs.cs.studienrichtung.Constraint;
import tubs.cs.studienrichtung.Mandatory;
import tubs.cs.studienrichtung.Optional;
import tubs.cs.studienrichtung.Subject;
import tubs.cs.studienrichtung.featuremodel.EvaluationData.CompulsoryElectiveConstraint;
import tubs.cs.studienrichtung.util.ArrayUtils;
import tubs.cs.studienrichtung.util.NameUtils;
import tubs.cs.studienrichtung.util.StringAvoider;

public class BranchVisitor {
	private FMGenerator generator;
	private Encoder encoder;
	
	private StringAvoider<Integer> varNamesForSpecialCardinalitiesSubstituiton;
	
	// Variables that will be set during visiting.
	private FMGenerationHandle fm;
	
	public BranchVisitor(FMGenerator generator) {
		this.generator = generator;
		this.encoder = new Encoder();
		this.varNamesForSpecialCardinalitiesSubstituiton = new StringAvoider<>(new StringAvoider.UniqueIntGenerator());
	}
	
	void initialize(FMGenerationHandle fm, IFeature varGenerationRoot, AtMostSATEncoding encoding) {
		this.fm = fm;
		this.varNamesForSpecialCardinalitiesSubstituiton.clear();
		this.encoder.initialize(fm, varGenerationRoot, encoding);
	}
	
	Encoder getEncoder() {
		return encoder;
	}
	
	void visit(Branch branch, IFeature root) {
		String name = NameUtils.unpack(branch.getName());
		
		// 1.) Create own feature
		IFeature branchFeature = generator.createAndRegisterFeature(fm, name);
		FeatureUtils.addChild(root, branchFeature);
		FeatureUtils.setAbstract(branchFeature, false); // not abstract so that the branch can be chosen
		
		// 2.) Create constraints
		//generator.out().println("[BranchVisitor.visit] creating constraints for " + name);
		
		List<IConstraint> constraints = new ArrayList<>();
		
		for (Constraint constraint : branch.getConstraints()) {
			IConstraint fmConstraint = null;
			if (constraint instanceof Mandatory) {
				fmConstraint = visitMandatoryConstraint(branchFeature, (Mandatory) constraint);
			} else if (constraint instanceof Optional) {
				fmConstraint = visitOptionalConstraint(branchFeature, (Optional) constraint);
			}
			
			if (fmConstraint != null)
				constraints.add(fmConstraint);
		}
		
		for (IConstraint c : constraints)
			fm.model.addConstraint(c);
		
		generator.getEvaluationData().encodingUsages = getEncoder().getEncodingUsageRecord();
	}

	private IConstraint visitMandatoryConstraint(IFeature branch, Mandatory mandatory) {
		Literal[] subjects = new Literal[mandatory.getSubjects().size()];
		
		int i = 0;
		for (Subject mandatorySubject : mandatory.getSubjects()) {
			subjects[i] = NodeUtils.reference(generator.getFeatureByName(mandatorySubject.getName()));
			++i;
		}
		
		IConstraint constraint = fm.factory.createConstraint(fm.model, new Implies(NodeUtils.reference(branch), new And(subjects)));
		// TODO: Localization
		constraint.setDescription("Pflichtkurse der " + branch.getName());
		return constraint;
	}

	private IConstraint visitOptionalConstraint(IFeature branch, Optional optional) {
		generator.getEvaluationData().pushNewConstraint();
		
		// TODO: Create a better solution for CPs, that are not multiples of 5 and near to 5 (like 6 here):
		List<Subject> optionals = new ArrayList<>(optional.getSubjects());
		
		/*
		for (Subject s : optionals) {
			// FIXME: VERY DIRTY
			if (s.getCreditPoints() == 6)
				s.setCreditPoints(5);
		}*/
		
		Node node = CEC(branch, optionals, optional.getCreditsToAchieve());
		node.simplify();
		Node finalOptionalConstraint = new Implies(NodeUtils.reference(branch), node);
		
		{
			CompulsoryElectiveConstraint constraintRecord = generator.getEvaluationData().currentConstraint;
			constraintRecord.constraint = finalOptionalConstraint;
			constraintRecord.constraintWithoutImplication = node;
		}
		
		//node = node.toCNF();
		
		IConstraint constraint = fm.factory.createConstraint(fm.model, finalOptionalConstraint);
		// TODO: Localization
		constraint.setDescription("Wahlpflichtkurse der " + branch.getName());
		return constraint;
	}
	
	/**
	 * 
	 * @param branch
	 * @param S Subjects to choose from.
	 * @param c credit points to achieve.
	 * @return
	 */
	private Node CEC(IFeature branch, final List<Subject> S, int c) {
		if (c <= 0) {
			// We are done. Return true
			return NodeUtils.reference(Boolean.TRUE);
		}
		
		int totalCredits = 0;
		for (Subject s : S) {
			totalCredits += s.getCreditPoints();
		}
		
		if (c > totalCredits) {
			// We return null instead of "false" because this enables us to directly
			// omit this unsatisfiable clause.
			return null;
		}
		
		// Find the first n features with the same amount of credits
		List<Subject> H = new ArrayList<>();
		List<Subject> L = new ArrayList<>(S);
		L.sort((subject1, subject2) -> {
			// swap subjects to sort descendingly
			return Integer.valueOf(subject2.getCreditPoints()).compareTo(subject1.getCreditPoints());
		});

		int cpPerSubject = L.get(0).getCreditPoints();
		while (!L.isEmpty() && L.get(0).getCreditPoints() == cpPerSubject) {
			H.add(L.remove(0));
		}

		/**
		 * It is not always necessary to split the constraint in two halves to
		 * resolve different amounts of CP. If all our courses grant more CP, than we
		 * need, it is completely irrelevant how much CP they grant.
		 */
		if (!L.isEmpty()) {
			Subject subjectWithLeastCP = L.get(L.size() - 1);
			if (subjectWithLeastCP.getCreditPoints() >= c) {
				// Smash everything together and choose anything.
				H.addAll(L);;
				return atLeast(branch, H, c);
			}
		}
		
		if (L.isEmpty() /*i.e. H == S */) {
			return atLeast(branch, H, c);
		} else {
			return split(branch, H, L, c);
		}
	}
	
	private Node atLeast(IFeature branch, List<Subject> subjects, int creditsToAchieve) {
		assert(!subjects.isEmpty());
		/* Assume, that
		 *    - all subjects have the same amount of credit points.
		 *    or
		 *    - all subjects have more CP than creditsToAchieve.
		 */
		int cpPerSubject = subjects.get(0).getCreditPoints();
		
		int numberOfSubjects = subjects.size();
		int numberOfSubjectsToSelect = (int) Math.ceil((double)creditsToAchieve / (double)cpPerSubject);
		
		if (numberOfSubjectsToSelect <= 0) {
			// A negative number of subjects to select can occur, when
			// we already have more CP than required.
			return null;
		}
		
		if (numberOfSubjectsToSelect > numberOfSubjects) {
			// This can happen due to ill-formed branch descriptions, for example
			// when a subject is listed as optional, but it is actually mandatory
			// because without it, not enough credit points could be achieved.
			// The result is an invalid partial configuration, that can never be
			// satisfied. Hence, just drop this case.
			generator.out().warn("   Ill-formed generation request: cannot generate atLeast with k = " + numberOfSubjectsToSelect + " and n = " + numberOfSubjects
					+ ". This may be caused by an optional subject, that is actually mandatory."
			);
			return null;
		}
		
		/*
		generator.out().println(
				String.format(
						"    Choose %s out of %s subjects (%s CP) for %s CP total.",
						numberOfSubjectsToSelect, numberOfSubjects, cpPerSubject, creditsToAchieve));
						//*/
		return encoder.encode(Encoder.Mode.AtLeast, subjects, numberOfSubjectsToSelect, generator, branch);
	}
	
	private Node split(IFeature branch, List<Subject> H, List<Subject> L, int creditsToAchieve) {
		List<Node> nodes = new ArrayList<>();
		
		for (int k = 0; k <= H.size(); ++k) {
			Node exactlyFormula = encoder.encode(Encoder.Mode.Exactly, H, k, generator, branch);
			int remainingCredits = creditsToAchieve - (k * H.get(0).getCreditPoints());
			
			if (exactlyFormula != null) {
				//generator.out().println("Current permutation = exactly " + numberOfVariablesToBeTrue);
				if (isSatisfiable(exactlyFormula)) {
					//generator.out().println("is satisfiable => accepted");
					// - generate a formula f_s := And(s) and CEC(subjects, C)
					Node recursiveCEC = CEC(branch, L, remainingCredits);
					
					if (recursiveCEC != null) { // null is returned by CEC, if its clause is unsatisfiable
						if (recursiveCEC instanceof Literal && ((Literal)recursiveCEC).var == Boolean.TRUE) {
							nodes.add(exactlyFormula);
						} else {
							nodes.add(new And(exactlyFormula, recursiveCEC));
						}
					}
				}
			}
		}
		
		return new Or(nodes.toArray());
	}
	
	private boolean isSatisfiable(Node node) {		
		final SatSolver solver = SatSolverFactory.getDefault().getSatSolver();
		solver.addFormulas(node, AdvancedNodeCreator.createNodes(this.fm.model));		
		return solver.isSatisfiable();
	}
}
