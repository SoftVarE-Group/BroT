package tubs.cs.studienrichtung.util;

import tubs.cs.satencodings.generation.NameGenerator;
import tubs.cs.satencodings.generation.names.NumericNameGenerator;

public class NameSalt {
	private NameGenerator generator;
	private int saltCounter;
	
	public NameSalt(String prefix, String suffix) {
		generator = new NumericNameGenerator(prefix, suffix);
		saltCounter = 0;
	}
	
	public String getSalt() {
		return generator.getNameAtIndex(saltCounter++);
	}
}
