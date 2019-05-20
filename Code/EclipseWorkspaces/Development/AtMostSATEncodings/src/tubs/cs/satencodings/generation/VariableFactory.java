package tubs.cs.satencodings.generation;

import org.prop4j.Literal;
import org.prop4j.Node;

import de.ovgu.featureide.fm.core.base.IFeature;

public abstract class VariableFactory {
	public static class VariableProperties {
		public final Node parent;
		public final String description;
		public final boolean isAbstract;
		public final boolean isHidden;
		
		public VariableProperties(Node parent, String description, boolean isAbstract, boolean isHidden) {
			this.parent = parent;
			this.description = description;
			this.isAbstract = isAbstract;
			this.isHidden = isHidden;
		}
		
		public VariableProperties(Node parent, String description, boolean isAbstract) {
			this(parent, description, isAbstract, false);
		}
		
		public VariableProperties(Node parent, String description) {
			this(parent, description, false);
		}
		
		public VariableProperties(Node parent) {
			this(parent, "");
		}
	}

	private Literal root = null;
	
	public Literal getRoot() {
		return this.root;
	}
	
	public void setRoot(Literal root) {
		this.root = root;
	}
	
	public Literal createVariable(String name) {
		return createVariable(name, null);
	}

	public Literal getOrCreateVariable(String name) {
		return getOrCreateVariable(name, null);
	}
	
	public abstract Literal getVariable(String name);
	public abstract Literal createVariable(String name, VariableProperties props);
	
	public Literal getOrCreateVariable(String name, VariableProperties props) {
		Literal v = getVariable(name);
		
		if (v == null) {
			//System.out.println("[VariableFactor::getOrCreateVariable] create var " + name);
			v = createVariable(name, props);
		} else {
			//System.out.println("[VariableFactor::getOrCreateVariable] found var " + name);
		}
		
		return v;
	}
}
