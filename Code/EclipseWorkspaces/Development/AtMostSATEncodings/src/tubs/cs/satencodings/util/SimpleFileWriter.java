package tubs.cs.satencodings.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.io.manager.SimpleFileHandler;
import de.ovgu.featureide.fm.core.io.xml.XmlFeatureModelFormat;

public class SimpleFileWriter {
	public static boolean fileExists(String path) {
		return new File(path).exists();
	}
	
	public void writeFile(String path, String text, boolean overwrite) {
		assert(path != null);
		assert(text != null);
		
		BufferedWriter filewriter = null;
	
		File file = new File(path);
		
		if (file.exists()) {
			if (!overwrite)
				return;
		} else {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			filewriter = new BufferedWriter(new java.io.FileWriter(file, false));
			filewriter.write(text);
			filewriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeFile(String path, IFeatureModel model, boolean overwrite) {
		if (fileExists(path) && !overwrite)
			return;
		
		// Specify the format (this is the format for extended feature models)
		final XmlFeatureModelFormat format = new XmlFeatureModelFormat();

		SimpleFileHandler.save(Paths.get(path), model, format);
		
        //FeatureModelManager.save(model, Paths.get(path));//, FMFormatManager.getDefaultFormat());
	}
}
