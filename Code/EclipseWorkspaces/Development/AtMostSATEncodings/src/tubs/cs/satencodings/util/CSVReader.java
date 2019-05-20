package tubs.cs.satencodings.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CSVReader {
	public List<String[]> read(String path, String separator) {
		Scanner scanner = null;
		try {
			scanner = new Scanner(new File(path));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
		
        scanner.useDelimiter("\n");
        List<String[]> rows = new ArrayList<>();
        
        while(scanner.hasNext()) {
        	String line = scanner.next();
        	String[] entries = line.split(separator);
        	
        	for (int i = 0; i < entries.length; ++i) {
        		entries[i] = entries[i].trim();
        	}
        	
            rows.add(entries);
        }
        scanner.close();
        
        return rows;
	}
}
