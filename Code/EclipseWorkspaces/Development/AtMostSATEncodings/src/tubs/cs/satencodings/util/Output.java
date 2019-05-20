package tubs.cs.satencodings.util;

import java.io.PrintStream;
import java.nio.CharBuffer;

public class Output {
	private final static String SINGLE_INDENT = "    ";
	
	private static int GUESSED_MAX_PROMPT_LENGTH = 35;
	private static PrintStream out = System.out;
	private static String indent = "";
	
	private static String spaces(int spaces) {
		return CharBuffer.allocate(spaces).toString().replace( '\0', ' ' );
	}
	
	public static void incIndent() {
		indent += SINGLE_INDENT;
	}
	
	public static void decIndent() {
		if (indent.length() >= SINGLE_INDENT.length())
			indent = indent.substring(0, indent.length() - SINGLE_INDENT.length());
	}
	
	public static void prompt(String prompt, String message) {
		String p = "[" + prompt + "]";
		
		if (p.length() > GUESSED_MAX_PROMPT_LENGTH)
			GUESSED_MAX_PROMPT_LENGTH = p.length();		
		
		println(p + " " + spaces(GUESSED_MAX_PROMPT_LENGTH - p.length()) + message);
	}
	
	public static void print(String message) {
		out.print(indent + message);
	}
	
	public static void println(String message) {
		out.println(indent + message);
	}
}
