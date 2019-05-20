package tubs.cs.satencodings.generation.encodings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.prop4j.And;
import org.prop4j.Node;
import org.prop4j.Literal;
import org.prop4j.Or;

import tubs.cs.satencodings.generation.AtMostSATEncoding;
import tubs.cs.satencodings.generation.NameGenerator;
import tubs.cs.satencodings.generation.VariableFactory.VariableProperties;
import tubs.cs.satencodings.generation.names.NumericNameGenerator;
import tubs.cs.satencodings.util.AnnotatedNode;
import tubs.cs.satencodings.util.Assert;
import tubs.cs.satencodings.util.NodeUtils;
import tubs.cs.satencodings.util.Output;
import tubs.cs.satencodings.util.Pair;

public class CommanderEncoding extends AtMostSATEncoding {
	private static class GroupDimensions {
		// group count, group size (names chosen accordingly to paper)
		public int g, s;
		
		public GroupDimensions(int g, int s) {
			this.g = g;
			this.s = s;
		}
		
		boolean isValid(int n, int k) {
			return g <= n && k < g;
		}

		public void resize(int n, int k) {
			while (g * k >= n) {
				--g;
			}

			s = (int) Math.ceil((double)n / (double)g);
		}
		
		@Override
		public String toString() {
			return "(g = " + g + ", s = " + s + ")";
		}

		public static GroupDimensions estimate(int n, int k, int desiredS) {
			int s = desiredS;
			int g = (int) Math.ceil((double)n / (double)s);
			
			// We have to ensure that g > k.
			if (g <= k) {
				g = k + 1;
				s = (int) Math.ceil((double)n / (double)g);
			}
			
			return new GroupDimensions(g, s);
		}
		
		public static GroupDimensions estimate(int n, int k, GroupDimensions dims) {
			return estimate(n, k, dims.s);
		}
	}
	
	public CommanderEncoding() {
		super("Commander");
	}
	
	@Override
	protected List<AnnotatedNode> atMost(List<Literal> variables, int k, EncodingOptions options) {
		// We need to create variables
		assert(getVariableFactory() != null);
		
		int n = variables.size();
		
		//if (k*k >= n)
		//	Output.prompt("CommanderEncoding.encodeAtMost", "WARNING: encoding is only efficient for k^2 < n");
		
		GroupDimensions groupDims = GroupDimensions.estimate(n, k, k + 2 /* This number is recommended in the paper */);
		
		Output.incIndent();
		//Output.prompt("CommanderEncoding.encodeAtMost", groupDims.toString());
		
		/// run actual encoding
		List<AnnotatedNode> comm = commander(variables, k, groupDims, options);
		Output.decIndent();
		
		return comm;
	}

	public List<AnnotatedNode> commander(List<Literal> variables, int k, GroupDimensions groupDims, EncodingOptions options) {
		int n = variables.size();
		
		Assert.error(groupDims.isValid(n, k), "GroupDimensions " + groupDims + " are invalid!");
		
		Literal commandersRoot = null;
		if (options.prettyGenerateVariables)
			commandersRoot = getVariableFactory().getOrCreateVariable(options.nameSalt + "Commanders", new VariableProperties(null, "Groups generated Variables of commander encoding", true));
		
		return commanderIteration(variables, k, groupDims, commandersRoot, 0, 0, new HashMap<Pair<Integer, Integer>, Literal>(), options);
	}
	
	private List<AnnotatedNode> commanderIteration(List<Literal> variables, int k, GroupDimensions groupDims, Node commandersRoot, int recursion, int groupIDCounter, Map<Pair<Integer, Integer>, Literal> commandersByIndex, EncodingOptions options) {
		//Output.prompt("CommanderFMGenerator.commander", "iteration " + recursion + "; (" + variables.size() + " / " + k + ")");
		
		GroupDimensions currentGroupDimensions = GroupDimensions.estimate(variables.size(), k, groupDims);
		int numberOfGroups = currentGroupDimensions.g;
		int groupSize = currentGroupDimensions.s;
		
		List<AnnotatedNode> constraints = new ArrayList<>();
		BinomialEncoding binomial = new BinomialEncoding();
		
		/// 0. Generate necessary commanders
		NameGenerator commandersNameGenerator;
		List<Literal> commanders = new ArrayList<>();
		int groupLowerIndex = groupIDCounter;
		int groupUpperIndex = groupLowerIndex + numberOfGroups;
		
		Node recursionNode = null;
		if (options.prettyGenerateVariables)
			recursionNode = getVariableFactory().createVariable(
				options.nameSalt + "It" + recursion,
				new VariableProperties(
						commandersRoot,
						"Iteration: " + recursion + "\nn = " + variables.size() + "\nk = " + k + "\ns = " + groupSize + "\ng = " + numberOfGroups,
						true
						)
				);
		
		for (; groupIDCounter < groupUpperIndex; ++groupIDCounter) {
			commandersNameGenerator = new NumericNameGenerator(options.nameSalt + "c_" + recursion + "_" + (groupIDCounter-groupLowerIndex) + "_");
			
			Node group = null;
			if (options.prettyGenerateVariables) {
				group = getVariableFactory().createVariable(groupIndexToName(recursion, groupIDCounter, groupLowerIndex, options.nameSalt), new VariableProperties(recursionNode, "", true));
			}
			
			List<Literal> commandersForGroup = generateCommanders(group, commandersByIndex, commandersNameGenerator, k, groupIDCounter);
			Assert.error(k == commandersForGroup.size());
			commanders.addAll(commandersForGroup);
		}
		
		if (commanders.size() >= variables.size()) {
			//Output.prompt("CommanderFMGenerator.commander", "generation is inefficient! Number of variables is not decreasing! n = " + variables.size() + ", number of commanders = " + commanders.size());
		}

		/// 1. Encode "atmost_k(X) && atleast_k(X)" with X=(Gi union {!cij | j \in {1, ..., k}}) for each group Gi.
		for (int g = 0; g < numberOfGroups; ++g) {			
			List<Literal> group = new ArrayList<>();

			// add variables
			for (int j = g*groupSize; j < (g+1)*groupSize && j < variables.size(); ++j) {
				group.add(variables.get(j));
			}
			
			// add commanders
			for (int j = 0; j < k; ++j) {
				group.add(NodeUtils.negate(commandersByIndex.get(new Pair<>(g + groupLowerIndex, j))));
			}

			constraints.add(
					new AnnotatedNode(
							// assume, that there is exactly one entry for binomial encoding
							binomial.encodeAtLeast(group, k, options).get(0).node,
							"Step 1: Binomial AtLeast_" + k + "(" + AnnotatedNode.toString(group) + ") for group " + groupIndexToName(recursion, g, groupLowerIndex, options.nameSalt)));
			
			List<AnnotatedNode> binomialAtMost = binomial.encodeAtMost(group, k, options);
			assert(binomialAtMost.size() == 1);
			AnnotatedNode binomialAtMostNode = binomialAtMost.get(0);
			constraints.add(
					new AnnotatedNode(
							binomialAtMostNode.node,
							"Step 1: Binomial  AtMost_" + k + "(" + AnnotatedNode.toString(group) + ") for group " + groupIndexToName(recursion, g, groupLowerIndex, options.nameSalt)
							+ ".\n" + binomialAtMostNode.annotation
							)
					);
		}

		/// 2. Remove symmetrical solutions when less than k variables in a group are true by ordering the commander variables:
		if (k > 1) {
			// k - 1 because k is number of commanders per group. We dont want to iterate over the last one so ignore it (-1)
			final int implicationsPerGroup = k - 1;
			
			// foreach group i
			for (int i = 0; i < numberOfGroups; ++i) {
				int currentGroupIndex = groupLowerIndex + i;
				// create implications
				Node[] and = new Node[implicationsPerGroup];
				
				for (int j = 0; j < implicationsPerGroup; ++j) { 
					and[j] = new Or(
							NodeUtils.negate(commandersByIndex.get(Pair.of(currentGroupIndex, j))),
							NodeUtils.referenceLiteral(commandersByIndex.get(Pair.of(currentGroupIndex, j + 1)))
							);
				}
				
				constraints.add(new AnnotatedNode(new And(and), "Step 2: Symmetry breaking for group " + groupIndexToName(recursion, i, groupLowerIndex, options.nameSalt)));
			}
			
		}
		
		/// 3. Encode atmost_k on the commander variables, e.g. with recursive application of this algorithm.
		int n_recursive = commanders.size();
		if (n_recursive <= k) {
			if (n_recursive < k) {
			    //Output.prompt("CommanderFMGenerator.commander", "WARNING: More variables than available have to chosen on recursive step!");
			}
			    
			//Output.prompt("CommanderFMGenerator.commander", "Recursion ends with OR.");
			constraints.add(new AnnotatedNode(
					new Or(commanders),
					"Step 3: AtMost_" + k + " on commanders.\n"
					+ "k >= n = " + n_recursive + ", wherefore AtMost_k is just Or."));
		} else {
			currentGroupDimensions.resize(n_recursive, k);
			
			if (k < currentGroupDimensions.g) {
				constraints.addAll(commanderIteration(commanders, k, currentGroupDimensions, commandersRoot, recursion + 1, groupIDCounter, commandersByIndex, options));
			} else {
				//Output.prompt("CommanderFMGenerator.commander", "recursion ends with " + binomial.getName() + " encoding.");
				List<AnnotatedNode> binomialAtMost = binomial.encodeAtMost(commanders, k, options);
				assert(binomialAtMost.size() == 1);
				AnnotatedNode binomialAtMostNode = binomialAtMost.get(0);
				constraints.add(new AnnotatedNode(
						binomialAtMostNode.node,
						"Step 3: Binomial AtMost_" + k + "(" + AnnotatedNode.toString(commanders) + ") on commanders,\n"
								+ "since no proper number of groups for recursion could be found.\n" + binomialAtMostNode.annotation));
			}
		}
		
		return constraints;
	}
	
	private List<Literal> generateCommanders(Node parent, Map<Pair<Integer, Integer>, Literal> commandersByIndex, NameGenerator nameGenerator, int count, int group) {
		List<Literal> features = new ArrayList<>(count);
		
		for (int i = 0; i < count; ++i) {
			Literal commander = getVariableFactory().createVariable(
					nameGenerator.getNameAtIndex(i),
					new VariableProperties(parent, "", true));
			features.add(commander);
			commandersByIndex.put(new Pair<>(group, i), commander);
		}
		
		return features;
	}
	
	private String groupIndexToName(int recursion, int groupID, int lowerIndex, String salt) {
		return salt + "G_"+ recursion + "_" + (groupID-lowerIndex);
	}
}
