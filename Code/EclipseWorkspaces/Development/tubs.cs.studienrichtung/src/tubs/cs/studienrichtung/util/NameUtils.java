package tubs.cs.studienrichtung.util;

public class NameUtils {
	private static final String Delimiter = "\"";
	
	public static String unpack(String name) {
		if (name.startsWith(Delimiter) && name.endsWith(Delimiter)) {
			return name.substring(1, name.length() - 1);
		}
		
		return name;
	}
}
