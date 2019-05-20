package tubs.cs.satencodings.generation.names;

public class AlphabeticNameGenerator implements tubs.cs.satencodings.generation.NameGenerator {
	private char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toUpperCase().toCharArray();
	
	@Override
	public String getNameAtIndex(int i) {
		String name = "";
		
		while (i >= alphabet.length) {
			name = alphabet[i % alphabet.length] + name;
			i = (i / alphabet.length) - 1; // -1 because I don't know
		}
		
		return alphabet[i] + name;
	}

}
