package tubs.cs.satencodings.generation.encodings;

import java.util.ArrayList;
import java.util.List;

import org.prop4j.And;
import org.prop4j.Literal;
import org.prop4j.Node;
import org.prop4j.Or;

import tubs.cs.satencodings.generation.AtMostSATEncoding;
import tubs.cs.satencodings.generation.VariableFactory.VariableProperties;
import tubs.cs.satencodings.util.AnnotatedNode;
import tubs.cs.satencodings.util.NodeUtils;

public class SequentialCounterEncoding extends AtMostSATEncoding {
	
	public SequentialCounterEncoding() {
		super("Sequential Counter");
	}
	
	@Override
	protected List<AnnotatedNode> atMost(List<Literal> variables, int k, EncodingOptions options) {
		// We need to create variables
		assert(getVariableFactory() != null);
		
		int n = variables.size();
		List<AnnotatedNode> constraints = new ArrayList<>();
		
		/// create registers
		Literal[][] R = new Literal[n - 1][k];
		{
			Literal registers = null;
			if (options.prettyGenerateVariables) {
				registers = getVariableFactory().getOrCreateVariable(options.nameSalt + "SQRegisters", new VariableProperties(null, "node to group register variables", true));
			}
			
			for (int i = 0; i < R.length; ++i) {
				for (int j = 0; j < R[i].length; ++j) {
					R[i][j] = getVariableFactory().getOrCreateVariable(options.nameSalt + "R_" + i + "_" + j, new VariableProperties(registers, "", true));
				}
			}
		}
		
		// Be careful with indices here. In the paper the variables are indexed, starting by 1, although (our) arrays start at 0.
		
		/// (1) X_i => first bit of register i is true
		{
			int from = indexFrom(1);
			int to   = indexTo(n-1);
			
			Node[] and = new Node[to - from];
			
			for (int i = from; i < and.length + from; ++i) {
				and[i - from] = new Or(NodeUtils.negate(variables.get(i)), R[i][0]);
			}
			
			constraints.add(new AnnotatedNode(new And(and), "Step (1)"));
		}
		
		/// (2) !R1j
		{
			int from = indexFrom(2);
			int to   = indexTo(k);
			
			Node[] and = new Node[to - from];
			
			for (int j = from; j < and.length + from; ++j) {
				and[j - from] = NodeUtils.negate(R[0][j]);
			}
			
			constraints.add(new AnnotatedNode(new And(and), "Step (2)"));
		}
		
		/// (3)
		{
			int from = indexFrom(2);
			int to   = indexTo(n-1);
			
			Node[] and = new Node[to - from];
			
			for (int i = from; i < and.length + from; ++i) {
				int innerFrom = indexFrom(1);
				int innerTo   = indexTo(k);
				
				Node[] innerAnd = new Node[innerTo - innerFrom];
				
				for (int j = innerFrom; j < innerAnd.length + innerFrom; ++j) {
					innerAnd[j - innerFrom] = new Or(NodeUtils.negate(R[i - 1][j]), R[i][j]);
				}
				
				and[i - from] = new And(innerAnd);
			}
			
			And step3 = new And(and);
			NodeUtils.flatten(step3);
			constraints.add(new AnnotatedNode(step3, "Step (3)"));
		}
		
		/// (4)
		{
			int from = indexFrom(2);
			int to   = indexTo(n-1);
			
			Node[] and = new Node[to - from];
			
			for (int i = from; i < and.length + from; ++i) {
				int innerFrom = indexFrom(2);
				int innerTo   = indexTo(k);
				
				Node[] innerAnd = new Node[innerTo - innerFrom];
				for (int j = innerFrom; j < innerAnd.length + innerFrom; ++j) {
					innerAnd[j - innerFrom] = new Or(
							NodeUtils.negate(variables.get(i)),
							NodeUtils.negate(R[i - 1][j - 1]),
							R[i][j]);
				}
				
				and[i - from] = new And(innerAnd);
			}
			
			And step4 = new And(and);
			NodeUtils.flatten(step4);
			constraints.add(new AnnotatedNode(step4, "Step (4)"));
		}
		
		/// (5)
		{
			int from = indexFrom(2); // This is the number in the original paper. It is wrong in the summarising paper.
			int to   = indexTo(n);
			
			Node[] and = new Node[to - from];
			
			for (int i = from; i < and.length + from; ++i) {
				and[i - from] = new Or(
						NodeUtils.negate(variables.get(i)),
						NodeUtils.negate(R[i - 1][k - 1])
						);
			}
			
			constraints.add(new AnnotatedNode(new And(and), "Step (5)"));
		}
		
		return constraints;
	}

	private static int indexFrom(int i) {
		return i - 1;
	}
	
	private static int indexTo(int i) {
		return i - 1 + 1;
	}
}
