package tubs.cs.satencodings.generation.names;

public class NumericNameGenerator implements tubs.cs.satencodings.generation.NameGenerator {
	private String prefix, suffix;
	
	public NumericNameGenerator(String prefix, String suffix) {
		this.prefix = prefix;
		this.suffix = suffix;
	}
	
	public NumericNameGenerator(String prefix) {
		this(prefix, "");
	}

	@Override
	public String getNameAtIndex(int i) {
		return prefix + i + suffix;
	}
}
