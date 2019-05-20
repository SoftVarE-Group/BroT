package tubs.cs.satencodings.util;

public class Assert {
	public static void error(boolean statement, String message) {
		if (!statement) {
			throw new RuntimeException(message);
		}
	}
	
	public static void error(boolean statement) {
		error(statement, "Assertion failed!");
	}
}
