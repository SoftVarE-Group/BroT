package tubs.cs.satencodings.generation.featuremodel;

import java.util.ArrayList;
import java.util.List;

import de.ovgu.featureide.fm.core.base.FeatureUtils;
import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.base.IFeatureModelFactory;
import tubs.cs.satencodings.generation.NameGenerator;

public class FMGenerationUtils {
	public static IFeature createSubTree(IFeatureModel fm, IFeatureModelFactory factory, String rootName, List<IFeature> children) {
		IFeature root = factory.createFeature(fm, rootName);
		
		fm.addFeature(root);
		
		for (IFeature f : children) {
			fm.addFeature(f);
			FeatureUtils.addChild(root, f);
		}

		return root;
	}
	
	public static List<IFeature> generateFeatures(IFeatureModelFactory factory, IFeatureModel fm, NameGenerator nameGenerator, int count) {
		List<IFeature> features = new ArrayList<>(count);
		
		for (int i = 0; i < count; ++i) {
			IFeature fi = factory.createFeature(fm, nameGenerator.getNameAtIndex(i));
			features.add(fi);
		}
		
		return features;
	}
}
