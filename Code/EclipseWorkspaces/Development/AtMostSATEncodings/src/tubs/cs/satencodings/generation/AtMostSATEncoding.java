package tubs.cs.satencodings.generation;

import java.util.ArrayList;
import java.util.List;

import org.prop4j.And;
import org.prop4j.Literal;
import org.prop4j.Node;
import org.prop4j.Or;

import tubs.cs.satencodings.util.AnnotatedNode;
import tubs.cs.satencodings.util.NodeUtils;

public abstract class AtMostSATEncoding {
	public static class EncodingOptions {
		public final boolean prettyGenerateVariables;
		public final String nameSalt;
		public final boolean printDebugMessages;
		
		public EncodingOptions(boolean prettyGenerateVariables, String nameSalt, boolean printDebugMessages) {
			this.prettyGenerateVariables = prettyGenerateVariables;
			this.nameSalt = nameSalt;
			this.printDebugMessages = printDebugMessages;
		}
		
		public EncodingOptions(boolean prettyGenerateVariables, String nameSalt) {
			this(prettyGenerateVariables, nameSalt, false);
		}
		
		public EncodingOptions() {
			this(true, "");
		}
	}
	
	private VariableFactory varFactory = null;
	private String name = null;
	
	protected AtMostSATEncoding(String name) {
		this.name = name;
	}
	
	public VariableFactory getVariableFactory() {
		return varFactory;
	}
	
	public void setVariableFactory(VariableFactory varFactory) {
		this.varFactory = varFactory;
	}
	
	public String getName() {
		return name;
	}
	
	protected abstract List<AnnotatedNode> atMost(List<Literal> variables, int k, EncodingOptions options);
	
	public final List<AnnotatedNode> encodeAtLeast(List<Literal> variables, int k, EncodingOptions options) {
		final int n = variables.size();
		
		if (k == 1 || k == n) {
			// We catch this case as not all encodings support atmost with k=0.
			// This case only requires all variables to be set.
			Literal[] varArray = new Literal[n];
			for (int i = 0; i < n; ++i) {
				varArray[i] = NodeUtils.referenceLiteral(variables.get(i));
			}
			
			Node varNode = null;
			if (k == 1)
				varNode = new Or(varArray);
			else // k == n
				varNode = new And(varArray);
			
			List<AnnotatedNode> ret = new ArrayList<AnnotatedNode>();
			ret.add(new AnnotatedNode(
					varNode,
					"Simplified atLeast for k = " + k + " and n = " + n)
					);
			return ret;
		} else {
			List<Literal> negatedVariables = new ArrayList<>(variables.size());
			for (Literal v : variables) {
				negatedVariables.add(NodeUtils.negate(v));
			}
			
			List<AnnotatedNode> result = encodeAtMost(negatedVariables, variables.size() - k, options);
			
			return result;
		}
	}
	
	public final List<AnnotatedNode> encodeAtMost(List<Literal> variables, int k, EncodingOptions options) {
		if (variables.size() == k) {
			// We catch this case as not all encodings support atmost with k=n (e.g. Commander)
			// and the result is really simple. It is just: true.
			
			// We cant choose more objects, than present.
			// Hence, every configuration of variables will satisfy
			// atmost(k, n).
			
			// TODO: Find an easier way to express the constant literal TRUE in FeatureIDE.
			AnnotatedNode node = new AnnotatedNode(new Or(
					NodeUtils.referenceLiteral(variables.get(0)),
					NodeUtils.negate(variables.get(0))
					), "Simplified atmost for k = n = " + k + ". The value is always true.");
			List<AnnotatedNode> list = new ArrayList<>(1);
			list.add(node);
			return list;
		} else {
			if (k <= 0)
				throw new IllegalArgumentException("Cannot encode atmostk with k <= 0!");
			
			if (variables.size() < k)
				throw new IllegalArgumentException("Cannot encode choose more elements, than available (n < k)!");
			
			return atMost(variables, k, options);
		}
	}
}
