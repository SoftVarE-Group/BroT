package tubs.cs.satencodings.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CSVWriter {
	private static void getOrCreateParentDir(String path) {
		File file = new File(path);
		if (!file.exists()) {
			getOrCreateParentDir(file.getParent());
			file.mkdirs();
		}
	}
	public static File getOrCreate(File file) {
		if (!file.exists()) {
			getOrCreateParentDir(file.getParent());
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return file;
	}
	public static File getOrCreate(String path) {
		//System.out.println("CSVWriter.getOrCreate(" + path + ")");
		return getOrCreate(new File(path));
	}
	
	private final String separator;
	File file;
	FileWriter writer;
	
	public CSVWriter(String path, String separator, boolean append) {
		this.separator = separator;
		
		this.file = getOrCreate(path);
		
		try {
			this.writer = new FileWriter(file, append);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public CSVWriter(String path, boolean append) {
		this(path, ";", append);
	}
	
	public CSVWriter(String path) {
		this(path, ";", false);
	}
	
	public void writeLine(Object... objects) {
		String[] str = new String[objects.length];
		for (int i = 0; i < str.length; ++i)
			str[i] = objects[i].toString();
		writeLine(str);
	}
	
	public void writeLine(String...strings) {		
		for (int i = 0; i < strings.length - 1; ++i) {
			try {
				writer.append(strings[i]);
				writer.append(separator);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			writer.append(strings[strings.length - 1] + System.getProperty("line.separator"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
