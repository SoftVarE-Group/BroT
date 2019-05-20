package tubs.cs.studienrichtung.featuremodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.prop4j.And;
import org.prop4j.Literal;
import org.prop4j.Node;

import de.ovgu.featureide.fm.core.base.IFeature;
import tubs.cs.satencodings.generation.AtMostSATEncoding;
import tubs.cs.satencodings.generation.VariableFactory;
import tubs.cs.satencodings.generation.encodings.*;
import tubs.cs.satencodings.generation.encodings.combined.CombinedEncoding;
import tubs.cs.satencodings.generation.featuremodel.FMGenerationHandle;
import tubs.cs.satencodings.generation.featuremodel.FMVariableFactory;
import tubs.cs.satencodings.util.AnnotatedNode;
import tubs.cs.satencodings.util.NodeUtils;
import tubs.cs.studienrichtung.Subject;
import tubs.cs.studienrichtung.featuremodel.EvaluationData.EncodingRequest;
import tubs.cs.studienrichtung.util.NameSalt;

public class Encoder {
	public static enum Mode {
		AtLeast, AtMost, Exactly;
		
		public String toString() {
			return name();
		}
	}
	
	private AtMostSATEncoding encoding;
	private FMVariableFactory varFactory;
	private IFeature varGenerationRoot;
	private NameSalt salt;
	
	private int numberOfEncodingCalls = 0;
	
	public void initialize(FMGenerationHandle fm, IFeature varGenerationRoot, AtMostSATEncoding encoding) {
		varFactory = new FMVariableFactory(fm);
		this.varGenerationRoot = varGenerationRoot;
		salt = new NameSalt("S", "_");
		
		this.encoding = encoding;
		numberOfEncodingCalls = 0;
	}
	
	private AtMostSATEncoding.EncodingOptions initEncoding(AtMostSATEncoding encoding) {
		final boolean prettyPrint = false;
		final boolean printDebugMessages = false;
		
		AtMostSATEncoding.EncodingOptions options = new AtMostSATEncoding.EncodingOptions(prettyPrint, salt.getSalt(), printDebugMessages);
		varFactory.setRoot(
				varFactory.getOrCreateVariable(
						encoding.getName(),
						new VariableFactory.VariableProperties(
								varFactory.toLiteral(varGenerationRoot),
								"Groups variables generated with encoding " + encoding.getName(),
								true)));
		encoding.setVariableFactory(varFactory);
		
		return options;
	}
	
	public Node encode(Mode mode, List<Subject> subjects, int k, FMGenerator generator, IFeature branch) {
		List<EncodingRequest> encodingRequests = generator.getEvaluationData().currentConstraint.encodingRequests;
		encodingRequests.add(new EncodingRequest(mode, subjects.size(), k));
		
		// Convert subjects to literals
		List<Literal> subjectLiterals = new ArrayList<>(subjects.size());
		for (Subject sub : subjects) {
			subjectLiterals.add(NodeUtils.reference(generator.getFeatureByName(sub.getName())));
		}

		//generator.out().println("        Encoding " + mode.name() + "(n = " + subjectLiterals.size() + ", k = " + k + ")");
		List<Node> nodes = new ArrayList<>();
		if (mode == Mode.Exactly && k == 0) {
			for (Literal l : subjectLiterals) {
				nodes.add(NodeUtils.negate(l));
			}
		} else if (mode == Mode.Exactly && k == subjects.size()) {
			for (Literal l : subjectLiterals) {
				nodes.add(NodeUtils.reference(l));
			}
		} else {
			List<AnnotatedNode> constraint = new ArrayList<>();
			
			if (mode == Mode.AtLeast || mode == Mode.Exactly) {
				AtMostSATEncoding.EncodingOptions options = initEncoding(encoding);
				//generator.out().println("            atLeast: " + encoding.getName());
				constraint.addAll(encoding.encodeAtLeast(subjectLiterals, k, options));
				++numberOfEncodingCalls;
			}
			
			if (mode == Mode.AtMost || mode == Mode.Exactly) {
				AtMostSATEncoding.EncodingOptions options = initEncoding(encoding);
				//generator.out().println("            atMost: " + encoding.getName());
				constraint.addAll(encoding.encodeAtMost(subjectLiterals, k, options));
				++numberOfEncodingCalls;
			}
			
			for (AnnotatedNode annotatedNode : constraint)
				nodes.add(annotatedNode.node);
		}
		
		return new And(nodes.toArray());
	}
	
	public VariableFactory getVariableFactory() {
		return varFactory;
	}

	public Map<String, Integer> getEncodingUsageRecord() {
		if (encoding instanceof CombinedEncoding) {
			return ((CombinedEncoding) encoding).getEncodingUsages();
		}
		
		Map<String, Integer> record = new HashMap<>();
		record.put(encoding.getName(), numberOfEncodingCalls);
		return record;
	}
}
