package tubs.cs.satencodings.generation.encodings;

import java.util.ArrayList;
import java.util.List;

import org.prop4j.And;
import org.prop4j.Node;
import org.prop4j.Literal;
import org.prop4j.Or;

import tubs.cs.satencodings.generation.AtMostSATEncoding;
import tubs.cs.satencodings.generation.VariableFactory;
import tubs.cs.satencodings.generation.VariableFactory.VariableProperties;
import tubs.cs.satencodings.util.AnnotatedNode;
import tubs.cs.satencodings.util.Assert;
import tubs.cs.satencodings.util.NodeUtils;

public class BinaryEncoding extends AtMostSATEncoding {

	public BinaryEncoding() {
		super("Binary_noncnf");
	}

	@Override
	protected List<AnnotatedNode> atMost(List<Literal> variables, int k, EncodingOptions options) {
		List<AnnotatedNode> constraints = new ArrayList<>();
		
		int n = variables.size();
		int log2n = (int) Math.ceil(Math.log(n) / Math.log(2.0));
		
		Literal[][] B = generateBitVars(n, k, getVariableFactory(), options);
		long[] bits = generateBits(n);
		
		for (int i = 0; i < n; ++i) {
			Node[] or = new Node[k];
			
			for (int g = 0; g < k; ++g) {
				Node[] and = new Node[log2n];
			
				for (int j = 0; j < log2n; ++j) {
					and[j] = new Or(
							NodeUtils.negate(variables.get(i)),
							phi(i, g, j, B, bits));
				}
				
				or[g] = new And(and);
			}
			
			// Optimisation: If there is only one child, omit the Or and use the child directly.
			Node constraint = null;
			if (or.length > 1)
				constraint = new Or(or);
			else if (or.length > 0)
				constraint = or[0];
			
			constraints.add(new AnnotatedNode(constraint));
		}
		
		return constraints;
	}
	
	static long[] generateBits(int n) {
		long[] bits = new long[n];
		
		for (int bit = 0; bit < n; ++bit) {
			bits[bit] = bit;
		}
		
		return bits;
	}
	
	static Literal[][] generateBitVars(int n, int k, VariableFactory varFactory, EncodingOptions options) {
		Assert.error(varFactory != null);
		
		int log2n = (int) Math.ceil(Math.log(n) / Math.log(2.0));
		Literal[][] B = new Literal[k][log2n];

		Literal generatedVars = null;
		if (options.prettyGenerateVariables)
			generatedVars = varFactory.createVariable(options.nameSalt + "Bits", new VariableProperties(null, "Groups generated bit variables of this encoding.", true));
		
		for (int i = 0; i < k; ++i) {
			for (int g = 0; g < log2n; ++g) {
				B[i][g] = varFactory.createVariable(options.nameSalt + "B_" + i + "_" + g, new VariableProperties(generatedVars, "", true));
			}
		}
		
		return B;
	}

	protected static Literal phi(int i, int g, int j, Literal[][] B, long[] bits) {
		// if (the jth bit of var i is 1)
		if ((bits[i] & (1 << j)) > 0) {
			return B[g][j];
		} else {
			return NodeUtils.negate(B[g][j]);
		}
	}
}
