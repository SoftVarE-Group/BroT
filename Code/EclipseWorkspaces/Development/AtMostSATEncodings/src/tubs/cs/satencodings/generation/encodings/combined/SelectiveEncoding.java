package tubs.cs.satencodings.generation.encodings.combined;

import tubs.cs.satencodings.generation.AtMostSATEncoding;

public class SelectiveEncoding extends CombinedEncoding {
	public SelectiveEncoding() {
		super("Selective");
	}
	
	static int k_binom(int n) {
		if (n < 6)
			return 1;
		else if (6 <= n && n < 40)
			return n - 2;
		else
			return n - 1;
	}
	
	static int k_split(int n) {
		double l = Math.ceil(Math.log10(n) / Math.log10(2));
		double a = 1 + 2*l;
		double b = 2*(l*(n + 1) - 2*n + 5);
		return (int) Math.floor(
				(b + Math.sqrt(b*b - 4*a))
				/
				(2*a)
				);
	}

	@Override
	public AtMostSATEncoding selectEncoding(AtMostSATEncodings encodings, int n, int k) {
		final int k_binom = k_binom(n);
		
		if (k_binom <= k)
		{
			return encodings.binomial;
		}
		else if (k_split(n) < k && k < k_binom)
		{
			return encodings.binaryCNF;
		}
		else
		{
			return encodings.sequentialCounter;
		}
	}

}
