package tubs.cs.satencodings;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import tubs.cs.satencodings.generation.*;
import tubs.cs.satencodings.generation.encodings.BinaryCNFEncoding;
import tubs.cs.satencodings.generation.encodings.BinaryEncoding;
import tubs.cs.satencodings.generation.encodings.BinomialEncoding;
import tubs.cs.satencodings.generation.encodings.CommanderEncoding;
import tubs.cs.satencodings.generation.encodings.SequentialCounterEncoding;
import tubs.cs.satencodings.generation.encodings.combined.SelectiveEncoding;
import tubs.cs.satencodings.generation.featuremodel.AtMostFMGenerator;
import tubs.cs.satencodings.util.Input;
import tubs.cs.satencodings.util.Output;
import tubs.cs.satencodings.util.SimpleFileWriter;

public class Main {
	private static final String ProjectFile = "model.xml";
	
	private static class ModelFileGenerator {
		private boolean overwrite = true;
		
		private String path;
		private AtMostFMGenerator generator;
		private AtMostSATEncoding encoding;
		private AtMostSATEncoding.EncodingOptions options;
		private IFeatureModel atmostModel = null, atleastModel = null;
		
		public ModelFileGenerator(String path, AtMostFMGenerator generator, AtMostSATEncoding.EncodingOptions options, AtMostSATEncoding encoding) {
			this.path = path;
			this.generator = generator;
			this.encoding = encoding;
			this.options = options;
		}
		
		public void generateModelFile(SimpleFileWriter writer, int n, int k) {
			Output.println("Generating atmost_" + path);
			Output.incIndent();
			atmostModel  = generator.generateAtMost(n, k, encoding, options);
			Output.decIndent();
			Output.println("Generating atleast_" + path);
			Output.incIndent();
			atleastModel = generator.generateAtLeast(n, k, encoding, options);			
			Output.decIndent();
			Output.println("done");
			
			writer.writeFile("generated/atmost_"  + path, atmostModel, overwrite);
			writer.writeFile("generated/atleast_" + path, atleastModel, overwrite);
		}
	}
	
	public static void main(String[] args) {
		Input.initialize();

		String nameSalt = 
				/*
				"Salt_"
				/*/
				""
				//*/
				;
		
		/*
		int n = 2;
		int k = 2;
		boolean prettyPrint = true;
		/*/
		int n = Input.readNumber("Enter   number  of   variables: ");
		int k = Input.readNumber("Enter number to select at most: ");
		boolean prettyPrint = Input.readBoolean("Pretty print generated variables");
		//*/
		
		if (k > n) {
			Output.println("Invalid input: Number of variables to select can not be greater than number of all variables.");
			Output.println("abort");
			return;
		}
		
		SimpleFileWriter writer = new SimpleFileWriter();
		AtMostFMGenerator fmGenerator = new AtMostFMGenerator();
		
		AtMostSATEncoding.EncodingOptions options = new AtMostSATEncoding.EncodingOptions(prettyPrint, nameSalt);
		
		ModelFileGenerator[] generatorsToRun;
		ModelFileGenerator exportModelGenerator;
		{
			// Create available generators
			ModelFileGenerator binomial   = new ModelFileGenerator("binomial.xml",   fmGenerator, options, new BinomialEncoding());
			ModelFileGenerator binary     = new ModelFileGenerator("binary.xml",     fmGenerator, options, new BinaryEncoding());
			ModelFileGenerator binaryCNF  = new ModelFileGenerator("binaryCNF.xml",  fmGenerator, options, new BinaryCNFEncoding());
			ModelFileGenerator sequential = new ModelFileGenerator("sequential.xml", fmGenerator, options, new SequentialCounterEncoding());
			ModelFileGenerator commander  = new ModelFileGenerator("commander.xml",  fmGenerator, options, new CommanderEncoding());
			ModelFileGenerator selective  = new ModelFileGenerator("combined.xml",   fmGenerator, options, new SelectiveEncoding());
			//ModelFileGenerator product    = new ModelFileGenerator("product.xml",    fmGenerator, options, new ProductEncoding());

			exportModelGenerator = commander;
			
			// Choose generators to run
			generatorsToRun = new ModelFileGenerator[] {
					binomial,
					binary,
					binaryCNF,
					sequential,
					commander,
					selective
					//,product
			};
		}
		
		for (int i = 0; i < generatorsToRun.length; ++i) {
			generatorsToRun[i].generateModelFile(writer, n, k);
			generatorsToRun[i] = null; // decref for garbage collector
		}
		
		if (exportModelGenerator != null) {
			IFeatureModel exportModel = exportModelGenerator.atleastModel;
			
			if (exportModel != null) {
				System.out.println("Saving " + exportModelGenerator.path + " as model.xml");
				writer.writeFile(ProjectFile, exportModel, true);
			}
		}
		
		System.out.println("EndProgram");
	}
}
