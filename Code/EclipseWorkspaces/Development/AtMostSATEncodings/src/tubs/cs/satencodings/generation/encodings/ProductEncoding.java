package tubs.cs.satencodings.generation.encodings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.prop4j.And;
import org.prop4j.Literal;
import org.prop4j.Node;
import org.prop4j.Or;

import tubs.cs.satencodings.generation.AtMostSATEncoding;
import tubs.cs.satencodings.generation.VariableFactory;
import tubs.cs.satencodings.generation.VariableFactory.VariableProperties;
import tubs.cs.satencodings.util.AnnotatedNode;
import tubs.cs.satencodings.util.NodeUtils;
import tubs.cs.satencodings.util.Output;
import tubs.cs.satencodings.util.Pair;

public class ProductEncoding extends AtMostSATEncoding {
	private static class Tuple {
		public List<Integer> value;
		
		public Tuple(List<Integer> value) {
			this.value = value;
		}
		
		public Tuple(int length) {
			this(new ArrayList<Integer>(length));
		}
		
		public Tuple removeAt(int index) {
			Tuple slash = new Tuple(new ArrayList<Integer>(value));
			slash.value.remove(index);
			return slash;
		}
		
		@Override
		public boolean equals(Object other) {
			if (other instanceof Tuple) {
				return ((Tuple) other).value.equals(this.value);
			}
			
			return false;
		}
	}
	
	public ProductEncoding() {
		super("Product");
	}

	@Override
	protected List<AnnotatedNode> atMost(List<Literal> variables, int k, EncodingOptions options) {
		// We need to create variables
		assert(getVariableFactory() != null);
		return product(variables, k, 0, 0, options);
	}
	
	private List<AnnotatedNode> product(List<Literal> variables, int k, int recursion, int recursionIndex, EncodingOptions options) {
		VariableFactory varFactory = getVariableFactory();
		
		List<AnnotatedNode> constraints = new ArrayList<>();
		int n = variables.size();
		
		/// Step 1
		int[] p = chooseNaturalNumbers(n, k);
		
		/// Step 2
		Tuple[] X = new Tuple[n];
		for (int i = 0; i < n; ++i) {
			X[i] = createUniqueTuple(i, p);
		}
		
		/// Step 3 is just a definition. Don't ask me why they called it a step. It is implemented in @see Tuple.removeAt.
		
		/// Step 4
		Map<Pair<Integer, Tuple>, Literal> A = new HashMap<>();
		Literal recursionNode = varFactory.getOrCreateVariable(
				options.nameSalt + "R" + recursion + "_D" + recursionIndex,
				new VariableProperties(
						varFactory.getOrCreateVariable(options.nameSalt + "Ady", new VariableProperties(null, "Groups generated variables", true)),
						"",
						true));
		
		for (int d = 0; d < k + 1; ++d) {
			Set<Tuple> Y = new HashSet<>();
			for (Tuple x : X) {
				Y.add(x.removeAt(d));
			}
			
			int yIndex = 0;
			for (Tuple y : Y) {
				A.put(new Pair<>(d, y),
						varFactory.getOrCreateVariable(
								options.nameSalt + "A" + d + "_y" + yIndex + "_R" + recursion + "_D" + recursionIndex,
								new VariableProperties(recursionNode)));
			}
		}
		
		Node[] localAnd = new Node[k + 1];
		
		for (int d = 0; d < localAnd.length; ++d) {
			Node[] setAnd = new Node[X.length];
			
			for (int i = 0; i < X.length; ++i) {
				Tuple x = X[i];
				setAnd[i] = new Or(
						NodeUtils.negate(variables.get(i)),
						A.get(new Pair<>(d, x.removeAt(d)))
						);
			}
			
			// TODO: Flatten And
			localAnd[d] = new And(setAnd);
		}
		constraints.add(new AnnotatedNode(new And(localAnd), "Recursion = " + recursion + "\nIteration (d) = " + recursionIndex));
		
		// Recursion
		for (int d = 0; d < localAnd.length; ++d) {
			List<Literal> vars = new ArrayList<>();
			
			for (Tuple x : X) {
				vars.add(A.get(new Pair<>(d, x.removeAt(d))));
			}
			
			List<AnnotatedNode> result = product(vars, k, recursion + 1, d, options);
			constraints.addAll(result);
		}
		
		return constraints;
	}

	static int[] chooseNaturalNumbers(int n, int k) {
		int[] numbers = new int[k+1];
		
		// Find numbers that are >= n, when multiplied together.
		// I don't know which values fit best here.
		
		// Current idea for n = 6, k = 4
		// 6 5 4 3 2
		
		for (int i = 0; i < numbers.length; ++i) {
			numbers[i] = n - i;
		}
		
		return numbers;
	}
	
	static Tuple createUniqueTuple(int varIndex, int[] p) {
		Output.prompt("ProductEncoding.createUniqueTuple", "for index " + varIndex);
		Tuple xtuple = new Tuple(p.length);
		List<Integer> x = xtuple.value;
		
		// initialize everything to zero
		for (int i = 0; i < p.length; ++i) {
			x.add(Integer.valueOf(0));
		}

		// Constraint: 0 <= xi < pi
		while (varIndex > 0) {
			int incrementIndex = varIndex % p.length;
			
			x.set(incrementIndex, x.get(incrementIndex) + 1);
			
			varIndex = (varIndex / p.length) - 1;
		}
		
		for (Integer i : x) {
			Output.print(i + " ");
		}
		Output.println("");
		
		return xtuple;
	}
}
