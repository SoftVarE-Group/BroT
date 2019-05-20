package tubs.cs.satencodings.generation.encodings.combined;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.prop4j.Literal;

import tubs.cs.satencodings.generation.AtMostSATEncoding;
import tubs.cs.satencodings.generation.VariableFactory;
import tubs.cs.satencodings.generation.encodings.BinaryCNFEncoding;
import tubs.cs.satencodings.generation.encodings.BinomialEncoding;
import tubs.cs.satencodings.generation.encodings.CommanderEncoding;
import tubs.cs.satencodings.generation.encodings.SequentialCounterEncoding;
import tubs.cs.satencodings.util.AnnotatedNode;

public abstract class CombinedEncoding extends AtMostSATEncoding {
	public static class AtMostSATEncodings {
		BinomialEncoding binomial;
		BinaryCNFEncoding binaryCNF;
		CommanderEncoding commander;
		SequentialCounterEncoding sequentialCounter;
		
		AtMostSATEncodings() {
			binomial          = new BinomialEncoding();
			binaryCNF         = new BinaryCNFEncoding();
			commander         = new CommanderEncoding();
			sequentialCounter = new SequentialCounterEncoding();
		}
		
		void setVariableFactory(VariableFactory varFactory) {
			binomial.setVariableFactory(varFactory);
			binaryCNF.setVariableFactory(varFactory);
			commander.setVariableFactory(varFactory);
			sequentialCounter.setVariableFactory(varFactory);
		}
	}
	private static final AtMostSATEncodings Encodings = new AtMostSATEncodings();
	private Map<String, Integer> encodingUsages;
	
	public CombinedEncoding(String name) {
		super(name);
		encodingUsages = new HashMap<>();
	}
	
	@Override
	public void setVariableFactory(VariableFactory varFactory) {
		super.setVariableFactory(varFactory);
		Encodings.setVariableFactory(varFactory);
	}

	@Override
	protected List<AnnotatedNode> atMost(List<Literal> variables, int k, EncodingOptions options) {
		AtMostSATEncoding e = selectEncoding(Encodings, variables.size(), k);
		encodingUsages.put(e.getName(), encodingUsages.getOrDefault(e.getName(), 0) + 1);
		
		if (options.printDebugMessages) {
			System.out.println("[CombinedEncoding.atMost(" + variables.size() + ", " + k + ")] chose encoding " + e.getName());
		}

		VariableFactory varFactory = getVariableFactory();
		Literal currentRoot = varFactory.getRoot();
		if (options.prettyGenerateVariables) {
			varFactory.setRoot(
					varFactory.getOrCreateVariable(
							e.getName(),
							new VariableFactory.VariableProperties(
									currentRoot,
									"Groups variables generated with encoding " + e.getName(),
									true)));
		}
		
		List<AnnotatedNode> res = e.encodeAtMost(variables, k, options);

		if (options.prettyGenerateVariables) {
			varFactory.setRoot(currentRoot);
		}
		
		return res;
	}
	
	public void resetEncodingUsages() {
		encodingUsages.clear();
	}
	
	public Map<String, Integer> getEncodingUsages() {
		return encodingUsages;
	}

	public abstract AtMostSATEncoding selectEncoding(AtMostSATEncodings encodings, int n, int k);
}