package tubs.cs.satencodings.generation.encodings.combined;

import tubs.cs.satencodings.generation.AtMostSATEncoding;

public class MinVarCombinedEncoding extends CombinedEncoding {

	public MinVarCombinedEncoding() {
		super("CombinedMinVar");
	}

	@Override
	public AtMostSATEncoding selectEncoding(AtMostSATEncodings encodings, int n, int k) {
		if ((n < 8 && k <= n - 3) || (n >= 8 && k <= 5))
			return encodings.commander;
		else if (k <= 4 || (k == 6 && n >= 17))
			return encodings.sequentialCounter;
		else
			return encodings.binaryCNF;
	}

}
