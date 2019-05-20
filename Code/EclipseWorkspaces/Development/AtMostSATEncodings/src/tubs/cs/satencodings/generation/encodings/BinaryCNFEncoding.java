package tubs.cs.satencodings.generation.encodings;

import java.util.ArrayList;
import java.util.List;

import org.prop4j.And;
import org.prop4j.Literal;
import org.prop4j.Node;
import org.prop4j.Or;

import tubs.cs.satencodings.generation.AtMostSATEncoding;
import tubs.cs.satencodings.generation.VariableFactory;
import tubs.cs.satencodings.generation.VariableFactory.VariableProperties;
import tubs.cs.satencodings.util.AnnotatedNode;
import tubs.cs.satencodings.util.Assert;
import tubs.cs.satencodings.util.NodeUtils;

public class BinaryCNFEncoding extends AtMostSATEncoding {

	public BinaryCNFEncoding() {
		super("Binary");
	}

	@Override
	protected List<AnnotatedNode> atMost(List<Literal> variables, int k, EncodingOptions options) {
		List<AnnotatedNode> constraints = new ArrayList<>();
		
		int n = variables.size();
		int log2n = (int) Math.ceil(Math.log(n) / Math.log(2.0));
		
		Literal[][] T = generateTs(n, k, getVariableFactory(), options);
		Literal[][] B = BinaryEncoding.generateBitVars(n, k, getVariableFactory(), options);
		long[] bits   = BinaryEncoding.generateBits(n);
		
		for (int i = 0; i < n; ++i) {
			Node left, right;

			// We have to add 1 to i because our i starts at 0 (because we are using it as an array index),
			// but the papers i starts at 1. That has to be consistent with the other used variables.
			int gFrom = Math.max(1, k-n+i+1);
			int gTo   = Math.min(i+1, k) ;
					
			{ // left
				Node[] leftOr = new Node[gTo - gFrom + 1 + 1];
				
				for (int g = 0; g < leftOr.length - 1; ++g) {
					Node t = T[gArrayIndexToFormulaIndex(g, gFrom)][i];
					leftOr[g] = t;
				}
				
				leftOr[leftOr.length - 1] = NodeUtils.negate(variables.get(i));
				
				left = new Or(leftOr);
			}
			
			
			{ // right
				Node[] rightAnd = new Node[gTo - gFrom + 1];
				
				for (int g = 0; g < rightAnd.length; ++g) {
					Node[] rightInnerAnd = new Node[log2n];
					
					for (int j = 0; j < rightInnerAnd.length; ++j) {
						rightInnerAnd[j] = new Or(
								NodeUtils.negate(T[gArrayIndexToFormulaIndex(g, gFrom)][i]),
								BinaryEncoding.phi(i, gArrayIndexToFormulaIndex(g, gFrom), j, B, bits)
								);
					}
					
					rightAnd[g] = new And(rightInnerAnd);
				}
				
				right = new And(rightAnd);
			}
			
			And constraint = new And(left, right);
			NodeUtils.flatten(constraint);
			constraints.add(new AnnotatedNode(constraint, "Constraint for variable " + i));
		}
		
		return constraints;
	}

	static Literal[][] generateTs(int n, int k, VariableFactory varFactory, EncodingOptions options) {
		Assert.error(varFactory != null);
		
		Literal[][] T = new Literal[k][n];
		
		Literal tparent = null;
		if (options.prettyGenerateVariables)
			tparent = varFactory.createVariable(options.nameSalt + "T", new VariableProperties(null, "Groups generated T variables of this encoding.", true));
		
		for (int g = 0; g < k; ++g) {
			for (int i = 0; i < n; ++i) {
				T[g][i] = varFactory.createVariable(options.nameSalt + "T_" + g + "_" + i, new VariableProperties(tparent, "", true));
			}
		}
		
		return T;
	}
	
	private static int gArrayIndexToFormulaIndex(int g, int gFrom) {
		return g + gFrom - 1;
	}
}
