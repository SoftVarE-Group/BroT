package tubs.cs.satencodings.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Input {
	private static BufferedReader console;
	
	public static int readNumber(String prompt) {
		Output.print(prompt);
		
		String text = "-1";
		
		try {
			text = console.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return Integer.parseInt(text);
	}

	public static boolean readBoolean(String prompt) {
		Output.print(prompt + " (yes/no): ");
		String text = "no";
		
		try {
			text = console.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return text.toLowerCase().equals("yes");
	}
	
	public static void initialize() {
		console = new BufferedReader(new InputStreamReader(System.in));
	}
}
