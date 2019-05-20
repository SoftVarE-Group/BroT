package tubs.cs.satencodings.evaluation;
import java.util.ArrayList;
import java.util.List;
import org.prop4j.And;
import org.prop4j.Literal;
import org.prop4j.Node;
import tubs.cs.satencodings.generation.AtMostSATEncoding;
import tubs.cs.satencodings.generation.AtMostSATEncoding.EncodingOptions;
import tubs.cs.satencodings.generation.NameGenerator;
import tubs.cs.satencodings.generation.encodings.BinaryCNFEncoding;
import tubs.cs.satencodings.generation.encodings.BinomialEncoding;
import tubs.cs.satencodings.generation.encodings.CommanderEncoding;
import tubs.cs.satencodings.generation.encodings.SequentialCounterEncoding;
import tubs.cs.satencodings.generation.encodings.combined.SelectiveEncoding;
import tubs.cs.satencodings.generation.names.AlphabeticNameGenerator;
import tubs.cs.satencodings.util.AnnotatedNode;
import tubs.cs.satencodings.util.CSVReader;
import tubs.cs.satencodings.util.CSVWriter;
import tubs.cs.satencodings.util.NodeUtils;
import tubs.cs.satencodings.util.Pair;

public class Evaluation {	
	private static String exportPath = "C:/Users/Bittner/Documents/ISF/Projektarbeit/git/Evaluation/Naive/";
	private static final String csvSeparator = ";";
	
	private static final int RETURNCODE_SUCCESS = 1;
	private static final int RETURNCODE_MEMOVERFLOW   = 2;
	
	private static class ResultTable {
		private static class Entry {
			int n, k, numVars, numLiterals;
			
			public Entry(int n, int k, int numVars, int numLiterals) {
				this.n = n;
				this.k = k;
				this.numVars = numVars;
				this.numLiterals = numLiterals;
			}
		}
		
		List<Entry> entries;
		
		public ResultTable() {
			entries = new ArrayList<>();
		}
		
		public void pushEntry(int n, int k, int numVars, int numLiterals) {
			entries.add(new Entry(n, k, numVars, numLiterals));
		}
		
		public void exportToCSV(String path) {
			CSVWriter writer = new CSVWriter(path);
			writer.writeLine("n", "k", "#Vars", "#Literals");
			
			for (Entry e : entries) {
				writer.writeLine(e.n, e.k, e.numVars, e.numLiterals);
			}
			
			writer.close();
		}
	}
	
	private static class EncodingRecord {
		//"D:/Bittner/ISF/Projektarbeit/git/Evaluation/Naive/";
		public ResultTable table;
		AtMostSATEncoding encoding;
		boolean atMost;
		
		List<Pair<Integer, Integer>> crashesForInput;
		
		EncodingRecord(AtMostSATEncoding encoding, boolean atMost) {
			this.table = new ResultTable();
			this.atMost = atMost;
			this.encoding = encoding;
			this.crashesForInput = new ArrayList<>();
		}
		
		void exportToCSV() {
			table.exportToCSV(exportPath + (atMost ? "AtMost/" : "AtLeast/") + encoding.getName() + (atMost ? "_atmost" : "_atleast") + ".csv");
		}
		
		boolean doesNotCrashFor(int n, int k) {
			return !crashesForInput.contains(new Pair<>(n, k));
		}

		public List<AnnotatedNode> encode(List<Literal> variables, int k, EncodingOptions options) {
			if (atMost) {
				//System.out.println("      " + encoding.getName() + " atmost ");
				return encoding.encodeAtMost(variables, k, options);
			} else {
				//System.out.println("      " + encoding.getName() + " atleast");
				return encoding.encodeAtLeast(variables, k, options);
			}
		}

		public void crashesOn(int n, int k) {
			crashesForInput.add(new Pair<>(n, k));
		}
	}
	
	private static Node toNode(List<AnnotatedNode> annotatedNodes) {
		List<Node> nodes = NodeUtils.unpack(annotatedNodes);
		And and = new And(nodes.toArray());
		and.simplify();
		return and;
	}
	
	private static List<EncodingRecord> initializeEncodings(
			final boolean withBinomial,
			final boolean withBinary,
			final boolean withSQ,
			final boolean withCommander,
			final boolean withHeuristic,
			final boolean withAtLeast
			)
	{
		List<EncodingRecord> encodings = new ArrayList<>();
		
		if (withBinomial) {
			EncodingRecord binomialAtMost = new EncodingRecord(new BinomialEncoding(), true);
			encodings.add(binomialAtMost);
			
			if (withAtLeast) {
				EncodingRecord binomialAtLeast = new EncodingRecord(new BinomialEncoding(), false);
				encodings.add(binomialAtLeast);
			}
		}
		
		if (withBinary) {
			EncodingRecord binaryAtMost = new EncodingRecord(new BinaryCNFEncoding(), true);
			encodings.add(binaryAtMost);
			
			if (withAtLeast) {
				EncodingRecord binaryAtLeast = new EncodingRecord(new BinaryCNFEncoding(), false);
				encodings.add(binaryAtLeast);
			}
		}
		
		if (withSQ) {
			EncodingRecord sequentialCounterAtMost = new EncodingRecord(new SequentialCounterEncoding(), true);
			encodings.add(sequentialCounterAtMost);
			
			if (withAtLeast) {
				EncodingRecord sequentialCounterAtLeast = new EncodingRecord(new SequentialCounterEncoding(), false);
				encodings.add(sequentialCounterAtLeast);
			}
		}
		
		if (withHeuristic) {
			EncodingRecord minlitAtMost = new EncodingRecord(new SelectiveEncoding(), true);
			encodings.add(minlitAtMost);

			if (withAtLeast) {
				EncodingRecord minlitAtLeast = new EncodingRecord(new SelectiveEncoding(), false);
				encodings.add(minlitAtLeast);
			}
		}
		
		if (withCommander) {
			EncodingRecord commanderAtMost = new EncodingRecord(new CommanderEncoding(), true);
			encodings.add(commanderAtMost);
			
			if (withAtLeast) {
				EncodingRecord commanderAtLeast = new EncodingRecord(new CommanderEncoding(), false);
				encodings.add(commanderAtLeast);
			}
		}
		
		CSVReader crashReader = new CSVReader();
		List<String[]> crashes = crashReader.read(exportPath + "crashes.csv", csvSeparator);
		
		for (String[] row : crashes) {
			String encodingName = row[0];
			boolean atMost      = Boolean.parseBoolean(row[1]);
			int n               = Integer.parseInt(row[2]);
			int k               = Integer.parseInt(row[3]);
			
			for (EncodingRecord e : encodings) {
				if (e.encoding.getName().equals(encodingName) && e.atMost == atMost) {
					e.crashesOn(n, k);
				}
			}
		}
		
		return encodings;
	}


	public static void main(String[] args) {
		boolean complete = false;
		
		int MIN_N = 2;//2;
		int MAX_N = 100;
		
		int MIN_K = 1;//1;
		
		for (int i = 0; i < args.length; ++i) {
			String arg = args[i];
			
			if ("--complete".equals(arg)) {
				complete = true;
				System.out.println("Invoked with --complete");
			}
			
			if ("--path".equals(arg)) {
				exportPath = args[++i];
				System.out.println("Invoked for directory: " + exportPath);
			}
			
			if ("--mink".equals(arg)) {
				MIN_K = Integer.parseInt(args[++i]);
				System.out.println("Setting MIN_K to: " + MIN_K);
			}
			
			if ("--minn".equals(arg)) {
				MIN_N = Integer.parseInt(args[++i]);
				System.out.println("Setting MIN_N to: " + MIN_N);
			}
			
			if ("--maxn".equals(arg)) {
				MAX_N = Integer.parseInt(args[++i]);
				System.out.println("Setting MAX_N to: " + MAX_N);
			}
		}

		final boolean withBinomial  = true;
		final boolean withBinary    = true;
		final boolean withSQ        = true;
		final boolean withCommander = true;
		final boolean withHeuristic = !true;
		final boolean withAtLeast   = !true;

		int last_k = MIN_K;
		int last_n = MIN_N;
		
		if (!complete) {
			CSVReader lastPosReader = new CSVReader();
			List<String[]> lastPos = lastPosReader.read(exportPath + "lastpos.csv", csvSeparator);
			if (!lastPos.isEmpty()) {
				//last_Encoding = lastPos.get(0)[0];
				last_n = Integer.parseInt(lastPos.get(0)[1]);
				last_k = Integer.parseInt(lastPos.get(0)[2]);
			}
		}
		
		AtMostSATEncoding.EncodingOptions options;
		{
			boolean prettyPrint = false;
			String salt = "";
			options = new AtMostSATEncoding.EncodingOptions(prettyPrint, salt);
		}
		
		List<EncodingRecord> encodings = initializeEncodings(withBinomial, withBinary, withSQ, withCommander, withHeuristic, withAtLeast);
		
		NameGenerator nameGen = new AlphabeticNameGenerator();
		EvaluationVariableFactory varFactory = new EvaluationVariableFactory();
		for (EncodingRecord encodingRecord : encodings) {
			encodingRecord.encoding.setVariableFactory(varFactory);
		}
		
		int entries = 0;
		for (int n = last_n; n <= MAX_N; ++n) {
			System.out.println("  n = " + n);
			
			// Generate n variables
			List<Literal> variables = new ArrayList<>(n);
			for (int i = 0; i < n; ++i) {
				variables.add(NodeUtils.reference(nameGen.getNameAtIndex(i)));
			}
			
			for (int k = last_k; k < n; ++k) {
				System.out.println("    k = " + k);
				
				for (EncodingRecord encodingRecord : encodings) {
					if (encodingRecord.doesNotCrashFor(n, k)) {
						try {
							Node formula = toNode(encodingRecord.encode(variables, k, options));
							encodingRecord.table.pushEntry(n, k, formula.getUniqueVariables().size(), formula.getLiterals().size());
						} catch(RuntimeException e) {
							// Always fill table to make later evaluation easier.
							encodingRecord.table.pushEntry(n, k, Integer.MAX_VALUE, Integer.MAX_VALUE);
						} catch (OutOfMemoryError e) {
							// register crash case
							CSVWriter crashWriter = new CSVWriter(exportPath + "crashes.csv", true /*append*/);
							crashWriter.writeLine(encodingRecord.encoding.getName(), encodingRecord.atMost, n, k);
							crashWriter.close();
							
							// remember where we stopped
							CSVWriter lastPosWriter = new CSVWriter(exportPath + "lastpos.csv", false /*append*/);
							lastPosWriter.writeLine(encodingRecord.encoding.getName(), n, k);
							lastPosWriter.close();
							
							System.out.println("Memory Overflow for " + (encodingRecord.atMost ? "atMost" : "atLeast") + " in " + encodingRecord.encoding.getName());
							
							//restartProgram();
							
							System.exit(RETURNCODE_MEMOVERFLOW);
							return;
						}
					} else {
						// Always fill table to make later evaluation easier.
						encodingRecord.table.pushEntry(n, k, Integer.MAX_VALUE, Integer.MAX_VALUE);
					}
				}
				
				++entries;
			}
			
			last_k = MIN_K;
		}
		
		for (EncodingRecord encodingRecord : encodings) {
			encodingRecord.exportToCSV();
		}
		
		// Find optimum
		CSVWriter atMostOptimum  = new CSVWriter(exportPath + "atmost_eval.csv");
		atMostOptimum.writeLine("n", "k", "minNumVars", "minNumLiterals", "encodingMinNumVars", "encodingMinNumLiterals");
		CSVWriter atLeastOptimum = null;
		if (withAtLeast) {
			atLeastOptimum = new CSVWriter(exportPath + "atleast_eval.csv");
			atLeastOptimum.writeLine("n", "k", "minNumVars", "minNumLiterals", "encodingMinNumVars", "encodingMinNumLiterals");
		}
		boolean[] allBoolVals;
		
		if (withAtLeast) {
			allBoolVals = new boolean[2];
			allBoolVals[1] = false;
		} else {
			allBoolVals = new boolean[1];
		}
		allBoolVals[0] = true;
		
		for (int i = 0; i < entries; ++i) {
			for (boolean atMost : allBoolVals) {
				int n = -1, k = -1;
				
				int minNumVars = Integer.MAX_VALUE;
				int minNumVarsLits = Integer.MAX_VALUE;
				
				int minNumLiterals = Integer.MAX_VALUE;
				int minNumLiteralsVars = Integer.MAX_VALUE;
				
				AtMostSATEncoding encodingMinNumVars     = null;
				AtMostSATEncoding encodingMinNumLiterals = null;
				
				for (EncodingRecord encodingRecord : encodings) {
					if (atMost == encodingRecord.atMost) {
						ResultTable.Entry t = encodingRecord.table.entries.get(i);
						n = t.n;
						k = t.k;
						
						if (t.numVars < minNumVars || (t.numVars == minNumVars && t.numLiterals < minNumVarsLits)) {
							if (encodingRecord.encoding.getName() != "Binomial") {								
								encodingMinNumVars = encodingRecord.encoding;
								minNumVars = t.numVars;
								minNumVarsLits = t.numLiterals;
							}
						}
						
						if (t.numLiterals < minNumLiterals || (t.numLiterals == minNumLiterals && t.numVars < minNumLiteralsVars)) {
							encodingMinNumLiterals = encodingRecord.encoding;
							minNumLiterals = t.numLiterals;
							minNumLiteralsVars = t.numVars;
						}
					}
				}
				
				//if (encodingMinNumLiterals.getName() != "Combined" || encodingMinNumVars.getName() != "Combined") {
					String minNumVarName =     encodingMinNumVars == null ? "None" : encodingMinNumVars.getName();
					String minNumLitName = encodingMinNumLiterals == null ? "None" : encodingMinNumLiterals.getName();
					if (atMost) {
						atMostOptimum.writeLine(n, k, minNumVars, minNumLiterals, minNumVarName, minNumLitName);
					} else if (withAtLeast) {
						atLeastOptimum.writeLine(n, k, minNumVars, minNumLiterals, minNumVarName, minNumLitName);
					}
				//}
			}

		}
		
		atMostOptimum.close();
		
		if (withAtLeast) {
			atLeastOptimum.close();
		}
		
		System.out.println("done");
		System.exit(RETURNCODE_SUCCESS);
	}
}
