package tubs.cs.studienrichtung.featuremodel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.prop4j.Node;

import de.ovgu.featureide.fm.core.base.FeatureUtils;
import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.IFeatureModelFactory;
import de.ovgu.featureide.fm.core.base.impl.ExtendedFeatureModelFactory;
import tubs.cs.satencodings.generation.AtMostSATEncoding;
import tubs.cs.satencodings.generation.encodings.BinaryCNFEncoding;
import tubs.cs.satencodings.generation.encodings.BinomialEncoding;
import tubs.cs.satencodings.generation.encodings.SequentialCounterEncoding;
import tubs.cs.satencodings.generation.encodings.combined.SelectiveEncoding;
import tubs.cs.satencodings.generation.featuremodel.FMGenerationHandle;
import tubs.cs.studienrichtung.Branch;
import tubs.cs.studienrichtung.Category;
import tubs.cs.studienrichtung.StudyArea;
import tubs.cs.studienrichtung.Subject;
import tubs.cs.studienrichtung.featuremodel.EvaluationData.CompulsoryElectiveConstraint;
import tubs.cs.studienrichtung.util.localization.Localization;
import tubs.cs.studienrichtung.util.NameUtils;
import tubs.cs.studienrichtung.util.Output;
import tubs.cs.satencodings.util.Assert;
import tubs.cs.satencodings.util.CSVWriter;
import tubs.cs.satencodings.util.SimpleFileWriter;

public class FMGenerator {
	private Localization language;
	private Output output;
	private File workingDirectory;
	private Map<String, IFeature> featureByName;
	
	private EvaluationData currentEvaluationData;
	
	public void initialize(Localization localization, Output output, File workingDirectory) {
		this.language = localization;
		this.output = output;
		this.workingDirectory = workingDirectory;
		this.featureByName = new HashMap<>();
		resetEvaluationData();
	}
	
	private void resetEvaluationData() {
		this.currentEvaluationData = new EvaluationData();
	}
	
	public void parse(StudyArea studyArea) {
		/// 1.) Create Feature Model
		FMGenerationHandle fm;
		{
			IFeatureModelFactory factory = new ExtendedFeatureModelFactory();
			fm = new FMGenerationHandle(factory);
		}
		
		// 2.) Generate tree
		visitStudyArea(studyArea, fm, new SelectiveEncoding());
		
		// 3.) Export
		SimpleFileWriter writer = new SimpleFileWriter();
		File file = new File(workingDirectory, NameUtils.unpack(studyArea.getName()) + ".model.xml");
		writer.writeFile(file.toString(), fm.model, true);
		
		output.println("[FMGenerator.parse] done");
	}
	
	public void evaluateEncodingsOn(StudyArea studyArea) {
		final int ConfigTimeoutSeconds = 600;
		final int ConfigAverageTrials  = 50;
		
		boolean withConfigTime = false;
		
		final String outputDirectory = "C:/Users/Bittner/Documents/ISF/Projektarbeit/git/Evaluation/Branches/";
		//"D:/Bittner/ISF/Projektarbeit/git/Evaluation/Branches";
		
		List<AtMostSATEncoding> encodingsToEvaluate = new ArrayList<>();
		encodingsToEvaluate.add(new BinomialEncoding());
		encodingsToEvaluate.add(new BinaryCNFEncoding());
		encodingsToEvaluate.add(new SequentialCounterEncoding());
		encodingsToEvaluate.add(new SelectiveEncoding());
		//encodingsToEvaluate.add(new TheoreticCombinedEncoding());
		//encodingsToEvaluate.add(new CommanderEncoding());
		//encodingsToEvaluate.add(new MinVarCombinedEncoding());

		File directory = new File(outputDirectory, workingDirectory.getAbsoluteFile().getName());
		CSVWriter allEncodingsCSV = new CSVWriter((new File(directory, NameUtils.unpack(studyArea.getName()) + ".csv")).getAbsolutePath());
		allEncodingsCSV.writeLine("Timeout (s)", ConfigTimeoutSeconds, "ConfigTrialsMax", ConfigAverageTrials);
		allEncodingsCSV.writeLine("Encoding", "NumVars", "NumLiterals", "ConfigTime", "ConfigTrials");

		output.println("[FMGenerator.evaluateEncodingsOn] Operating in Directory " + directory);
		for (AtMostSATEncoding encoding : encodingsToEvaluate) {
			resetEvaluationData();
			output.println("[FMGenerator.evaluateEncodingsOn] Evaluate encoding " + encoding.getName());
			
			/// 1.) Create Feature Model
			FMGenerationHandle fm;
			{
				IFeatureModelFactory factory = new ExtendedFeatureModelFactory();
				fm = new FMGenerationHandle(factory);
			}
			
			// 2.) Generate tree
			boolean success = false;
			String errorMessage = "";
			try {
				StudyArea copy = EcoreUtil.copy(studyArea);
				visitStudyArea(copy, fm, encoding);
				success = true;
			} catch (RuntimeException | OutOfMemoryError e) {
				errorMessage = e.toString();
			}

			String fileName = NameUtils.unpack(studyArea.getName()) + "_" + encoding.getName();
			
			if (success) {
				// 3.) Export Feature model
				SimpleFileWriter writer = new SimpleFileWriter();
				
				File file = new File(directory, fileName + ".model.xml");
				//output.println("[FMGenerator.evaluateEncodingsOn] Writing Model to " + file);
				CSVWriter.getOrCreate(file);
				writer.writeFile(file.toString(), fm.model, true);
				
				// 3.1) Create configuration and measure elapsed time.
				if (withConfigTime) {
					ConfigurationTimeEvaluator configEv = new ConfigurationTimeEvaluator(ConfigTimeoutSeconds, output);
					double configTime = configEv.getTimeForLoad(fm.model);
					int configTrials = 1;
					
					// If creating a configuration didn't fail, we want to measure the average time it took to create one.
					if (ConfigurationTimeEvaluator.IsTimeValid(configTime)) {
						double sumConfigTime = configTime;
						int trials = 1;
						
						for (int avgTrials = trials; avgTrials < ConfigAverageTrials; ++avgTrials) {
							double t = configEv.getTimeForLoad(fm.model);
	
							// If creating a configuration fails somewhere in between, we ignore this trial.
							if (!ConfigurationTimeEvaluator.IsTimeValid(t)) {
								continue;
							}
							
							sumConfigTime += t;
							++trials;
							//output.println("[FMGenerator.evaluateEncodingsOn] Finished Trial " + avgTrials);
						}
						
						configTime = sumConfigTime / trials;
						configTrials = trials;
					}
					
					getEvaluationData().configTime = configTime;
					getEvaluationData().configTrials = configTrials;
				} else {
					getEvaluationData().configTime = -1;
					getEvaluationData().configTrials = 0;
				}
			}
			
			// 4.) Export CSV
			CSVWriter writer = new CSVWriter((new File(directory, fileName + ".csv")).getAbsolutePath());
			if (success) {
				// Evaluate the formulas.
				EvaluationData evData = getEvaluationData();
				List<CompulsoryElectiveConstraint> constraints = evData.compulsoryElectiveConstraints;
				writer.writeLine("NumVars", "NumLiterals", "ConfigTime", "EncodingRequests");
				int sumNumLits = 0;
				int sumNumVars = 0;
				for (CompulsoryElectiveConstraint constraint : constraints) {
					Node formula = constraint.constraintWithoutImplication;
					int numLits = formula.getLiterals().size();
					int numVars = formula.getUniqueVariables().size();
					writer.writeLine(numVars, numLits, "", constraint.getEncodingRequestsAsString());
					
					sumNumLits += numLits;
					sumNumVars += numVars;
				}
				writer.writeLine("", "", "", "");
				writer.writeLine(sumNumVars, sumNumLits, evData.configTime, "");
				allEncodingsCSV.writeLine(encoding.getName(), sumNumVars, sumNumLits, evData.configTime, evData.configTrials);
			} else {
				writer.writeLine("ERROR", errorMessage);
			}
			
			writer.close();
		}
		
		allEncodingsCSV.close();
		output.println("[FMGenerator.evaluateEncodingsOn] done");
	}
	
	public IFeature createAndRegisterFeature(FMGenerationHandle fm, String name) {
		String unpackedName = NameUtils.unpack(name);
		IFeature f = fm.factory.createFeature(fm.model, unpackedName);
		fm.model.addFeature(f);
		featureByName.put(unpackedName, f);
		return f;
	}
	
	public IFeature getFeatureByName(String name) {
		return featureByName.get(NameUtils.unpack(name));
	}

	public Localization getLanguage() {
		return language;
	}
	
	public Output out() {
		return output;
	}
	
	private void visitStudyArea(StudyArea studyArea, FMGenerationHandle fm, AtMostSATEncoding encoding) {
		IFeature rootFeature = createAndRegisterFeature(fm, language.getNameOfModelClass(StudyArea.class) + " " + NameUtils.unpack(studyArea.getName()));
		FeatureUtils.setRoot(fm.model, rootFeature);
		FeatureUtils.setAbstract(rootFeature, true);

		//output.println("[FMGenerator.visitSudyArea] generating subjects of " + NameUtils.unpack(studyArea.getName()));
		for (Category cat : studyArea.getCategories())
			visitCategory(cat, fm, rootFeature);
		
		//output.println("[FMGenerator.visitSudyArea] generating branches");
		IFeature branchesFeature = createAndRegisterFeature(fm, language.getNameOfModelClass(Branch.class));
		FeatureUtils.addChild(rootFeature, branchesFeature);
		FeatureUtils.setMandatory(branchesFeature, false);
		FeatureUtils.setAlternative(branchesFeature); // Only one branch can be selected.
		FeatureUtils.setAbstract(branchesFeature, true);
		
		IFeature generatedVarsFeature = createAndRegisterFeature(fm, "GeneratedVariables");
		FeatureUtils.addChild(rootFeature, generatedVarsFeature);
		FeatureUtils.setAbstract(generatedVarsFeature, true);
		
		BranchVisitor branchVisitor = new BranchVisitor(this);
		branchVisitor.initialize(fm, generatedVarsFeature, encoding);
		for (Branch branch : studyArea.getBranches())
			branchVisitor.visit(branch, branchesFeature);
	}
	
	private void visitCategory(Category category, FMGenerationHandle fm, IFeature root) {
		IFeature node = createAndRegisterFeature(fm, category.getName());
		FeatureUtils.addChild(root, node);
		FeatureUtils.setAbstract(node, true);
		
		int min = category.getCardinalityMin();
		int max = category.getCardinalityMax();
		
		Assert.error(min >= 0, "Ill-formed cardinality: min >= 0 required, but was " + min);
		Assert.error(min <= max || max < 0, "Ill-formed cardinality: min <= max required (" + min + " <= " + max + ")");
		
		if (max == 0) { // => min <= 0
			// no child can be chosen
			// In this case the feature would be dead.
			// We cannot express this case only by cutting the tree here.
			return;
		}
		
		FeatureUtils.setAnd(node);
		FeatureUtils.setMandatory(node, min > 0);	

		if (max == 1) {
			FeatureUtils.setAlternative(node);
		} else if (min == 1) {
			// If exactly at least one child has to be chosen.
			FeatureUtils.setOr(node);
		}
		
		for (Category child : category.getCategories()) {
			visitCategory(child, fm, node);
		}
			
		for (Subject subject : category.getSubjects()) {
			visitSubject(subject, fm, node);
		}
	}
	
	private void visitSubject(Subject subject, FMGenerationHandle fm, IFeature root) {
		IFeature node = createAndRegisterFeature(fm, subject.getName());
		FeatureUtils.addChild(root, node);
	}
	
	public Category getCategoryOf(Subject subject) {
		EObject container = subject.eContainer();
		if (container instanceof Category)
			return (Category) container;
		else {
			out().warn("[FMGenerator.getCategoryOf(" + subject.getName() + ")] eContainer is not an instance of Category!");
			return null;
		}
	}

	public EvaluationData getEvaluationData() {
		return currentEvaluationData;
	}
}
