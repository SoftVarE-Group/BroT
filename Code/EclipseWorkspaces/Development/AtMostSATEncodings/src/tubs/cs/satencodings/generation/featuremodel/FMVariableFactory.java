package tubs.cs.satencodings.generation.featuremodel;

import org.prop4j.Literal;
import org.prop4j.Node;

import de.ovgu.featureide.fm.core.base.FeatureUtils;
import de.ovgu.featureide.fm.core.base.IFeature;
import tubs.cs.satencodings.generation.VariableFactory;
import tubs.cs.satencodings.util.NodeUtils;

public class FMVariableFactory extends VariableFactory {
	private FMGenerationHandle fmgen = null;
	
	public FMVariableFactory(FMGenerationHandle fmGenerationHandle) {
		this.fmgen = fmGenerationHandle;
		setRoot(fmgen.model.getStructure().getRoot().getFeature());
	}
	
	public IFeature getRootAsFeature() {
		//return asFeature(getRoot());
		return fmgen.model.getStructure().getRoot().getFeature();
	}
	
	public void setRoot(IFeature root) {
		super.setRoot(toLiteral(root));
	}

	@Override
	public Literal getVariable(String name) {
		IFeature feature = fmgen.model.getFeature(name);
		
		if (feature == null)
			return null;
		
		return toLiteral(feature);
	}

	@Override
	public Literal createVariable(String name, VariableProperties props) {		
		IFeature f = fmgen.factory.createFeature(fmgen.model, name);

		IFeature parent = getRootAsFeature();

		if (props != null) {
			if (isFeature(props.parent)) {
				parent = asFeature(props.parent);
				if (parent == null) {
					throw new RuntimeException("Bug: Given parent " + props.parent + " is not a feature!");
				}
			}
			
			if (!props.description.isEmpty()) {
				f.getProperty().setDescription(props.description);
			}

			FeatureUtils.setAbstract(f, props.isAbstract);
			FeatureUtils.setHidden(f, props.isHidden);
		}
		
		// Do this to ensure, that we add the feature always to the Structure of the FM.
		// Otherwise it is not a part of the FM's tree and whole subtrees can get lost.
		FeatureUtils.addChild(parent, f);
		fmgen.model.addFeature(f);
		
		return toLiteral(f);
	}
	
	private boolean isFeature(Node node) {
		
		//return node != null && node instanceof Literal && ((Literal) node).var instanceof IFeature;
		return node != null
				&& node instanceof Literal
				&& ((Literal) node).var instanceof String
				&& fmgen.model.getFeature(((String) ((Literal) node).var)) != null;
	}
	
	public IFeature asFeature(Node node) {
		if (isFeature(node))
			return fmgen.model.getFeature(((String) ((Literal) node).var));//(IFeature) ((Literal)node).var;
		return null;
	}
	
	public Literal toLiteral(IFeature feature) {
		return NodeUtils.reference(feature);
	}
}
