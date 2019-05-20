package tubs.cs.satencodings.generation.encodings;

import java.util.ArrayList;
import java.util.List;

import org.prop4j.And;
import org.prop4j.Node;
import org.prop4j.Literal;
import org.prop4j.Or;

import tubs.cs.satencodings.generation.AtMostSATEncoding;
import tubs.cs.satencodings.util.AnnotatedNode;
import tubs.cs.satencodings.util.Assert;
import tubs.cs.satencodings.util.MathUtils;
import tubs.cs.satencodings.util.NodeUtils;
import tubs.cs.satencodings.util.Permutation;

public class BinomialEncoding extends AtMostSATEncoding {
	public BinomialEncoding() {
		super("Binomial");
	}
	
	public Node encodeAtMost(List<Literal> variables, int k) {
		int n = variables.size();
		Assert.error(k <= n, "k <= n (" + k + " <= " + n + ")");
		
		if (k >= n) {
			// We cant choose more objects, than present.
			// Hence, every configuration of variables will satisfy
			// atmost(k,n).
			
			// TODO: Find an easier way to express the constant literal TRUE in FeatureIDE.
			return new Or(
					NodeUtils.referenceLiteral(variables.get(0)),
					NodeUtils.negate(variables.get(0))
					);
		} else { // k < n
			int subsetSize = k + 1;
			long numberOfClauses = MathUtils.over(n, subsetSize);
			
			if (numberOfClauses > Integer.MAX_VALUE)
				throw new RuntimeException("[BinomialFMGenerator.encodeAtMost] " + n + " over " + subsetSize + " is too big!");
			
			Node[] and = new Node[(int)numberOfClauses];
			Permutation permutation = new Permutation(n, subsetSize);
			
			int i = 0;
			do {
				boolean[] perm = permutation.get();
				Node[] children = new Node[subsetSize];
				
				int varIndex = -1;
				for (int j = 0; j < subsetSize; ++j) {
					while(++varIndex < perm.length && !perm[varIndex]);
					children[j] = NodeUtils.negate(variables.get(varIndex));
				}
				
				and[i++] = new Or(children);
			} while(permutation.next());
			
			return new And(and);
		}
	}
	
	@Override
	protected List<AnnotatedNode> atMost(List<Literal> variables, int k, EncodingOptions options) {
		List<AnnotatedNode> l = new ArrayList<>(1);
		l.add(new AnnotatedNode(encodeAtMost(variables, k), "Binomial AtMost(" + k + " of " + variables.size() + ")"));
		return l;
	}
}
