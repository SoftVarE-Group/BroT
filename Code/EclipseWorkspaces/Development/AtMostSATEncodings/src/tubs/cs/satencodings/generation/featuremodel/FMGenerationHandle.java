package tubs.cs.satencodings.generation.featuremodel;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.base.IFeatureModelFactory;
import tubs.cs.satencodings.util.Assert;

public class FMGenerationHandle {
	public final IFeatureModel model;
	public final IFeatureModelFactory factory;
	
	public FMGenerationHandle(IFeatureModelFactory factory) {
		Assert.error(factory != null);
		
		this.factory = factory;
		this.model = factory.createFeatureModel();
	}
}
