package tubs.cs.satencodings.generation.featuremodel;

import java.util.ArrayList;
import java.util.List;

import org.prop4j.Literal;
import de.ovgu.featureide.fm.core.base.FeatureUtils;
import de.ovgu.featureide.fm.core.base.IConstraint;
import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.base.IFeatureModelFactory;
import de.ovgu.featureide.fm.core.base.impl.ExtendedFeatureModelFactory;
import tubs.cs.satencodings.generation.AtMostSATEncoding;
import tubs.cs.satencodings.generation.NameGenerator;
import tubs.cs.satencodings.generation.VariableFactory;
import tubs.cs.satencodings.generation.names.AlphabeticNameGenerator;
import tubs.cs.satencodings.util.AnnotatedNode;

public class AtMostFMGenerator {
	private static interface Encode {
		List<AnnotatedNode> run(List<Literal> variables, int k, AtMostSATEncoding encoding, AtMostSATEncoding.EncodingOptions options);
	}
	
	private IFeatureModel generate(int n, int k, Encode encoder, String modelName, AtMostSATEncoding encoding, AtMostSATEncoding.EncodingOptions options) {
		/// Create Feature Model and root feature
		IFeatureModelFactory factory = new ExtendedFeatureModelFactory();
		FMGenerationHandle handle = new FMGenerationHandle(factory);
	
		IFeature rootFeature = factory.createFeature(handle.model, encoding.getName() + "Encoding_" + modelName + "_n" + n + "_k" + k);
		FeatureUtils.setRoot(handle.model, rootFeature);
		
		/// Generate variables
		NameGenerator variablesNameGenerator = new AlphabeticNameGenerator();
		List<IFeature> variables = FMGenerationUtils.generateFeatures(factory, handle.model, variablesNameGenerator, n);
		IFeature variablesRoot = FMGenerationUtils.createSubTree(handle.model, factory, "Variables", variables);
		FeatureUtils.addChild(rootFeature, variablesRoot);
		FeatureUtils.setAbstract(variablesRoot, true);
		
		List<Literal> variableNodes = new ArrayList<>(variables.size());
		for (IFeature v : variables) {
			variableNodes.add(new Literal(v));
		}
		
		/// Create encoding
		VariableFactory fmVarFactory = new FMVariableFactory(handle);
		encoding.setVariableFactory(fmVarFactory);
		List<AnnotatedNode> commanderConstraintNodes = encoder.run(variableNodes, k, encoding, options);
		encoding.setVariableFactory(null);
		
		/// Create constraints
		for (AnnotatedNode constraintNode : commanderConstraintNodes) {
			//Assert.error(constraintNode.node.isConjunctiveNormalForm(), "");
			//constraintNode.node.simplify();
			
			// Discard the constraint, if it is empty.
			if (!(constraintNode.node instanceof Literal) && constraintNode.node.getChildren().length == 0)
				continue;
			
			IConstraint constraint = factory.createConstraint(handle.model, constraintNode.node);
			constraint.setDescription(
					constraintNode.annotation
					+ (constraintNode.annotation.isEmpty() ? "" : "\n\n")
					+  "Is CNF: " + constraintNode.node.isConjunctiveNormalForm());
			handle.model.addConstraint(constraint);
		}
		
        return handle.model;
	}
	
	public IFeatureModel generateAtMost(int n, int k, AtMostSATEncoding encoding, AtMostSATEncoding.EncodingOptions options) {
		return generate(n, k,
				(List<Literal> variables, int _k, AtMostSATEncoding _encoding, AtMostSATEncoding.EncodingOptions _options) -> {
					return _encoding.encodeAtMost(variables, _k, _options);
				},
				"atMost",
				encoding, options);
	}
	
	public IFeatureModel generateAtLeast(int n, int k, AtMostSATEncoding encoding, AtMostSATEncoding.EncodingOptions options) {
		return generate(n, k,
				(List<Literal> variables, int _k, AtMostSATEncoding _encoding, AtMostSATEncoding.EncodingOptions _options) -> {
					return _encoding.encodeAtLeast(variables, _k, _options);
				},
				"atLeast",
				encoding, options);
	}
}
