package tubs.cs.satencodings.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.prop4j.And;
import org.prop4j.Literal;
import org.prop4j.Node;

import de.ovgu.featureide.fm.core.base.IFeature;

public class NodeUtils {
	public static Literal negate(Literal lit) {
		if (lit == null || lit.var == null) {
			throw new NullPointerException();
		}
		return new Literal(lit.var, !lit.positive);
	}
	
	public static Literal referenceLiteral(Literal lit) {
		if (lit == null || lit.var == null) {
			throw new NullPointerException();
		}
		return new Literal(lit.var, lit.positive);
	}
	
	public static Literal reference(Object object) {
		if (object instanceof Literal)
			return referenceLiteral((Literal) object);
		if (object instanceof IFeature)
			return reference(((IFeature) object).getName());
		
		return new Literal(object);
	}
	
	/**
	 * This method inlines all recursive Ands into the top level And, passed as argument.
	 */
	public static void flatten(And and) {
		List<And> redundantChildren = new ArrayList<>();
		
		do {
			List<Node> andsChildren = new ArrayList<>(Arrays.asList(and.getChildren()));
			for (And redundantChild : redundantChildren) {
				andsChildren.remove(redundantChild);
				
				for (Node grandchild : redundantChild.getChildren())
					andsChildren.add(grandchild);
			}
			redundantChildren.clear();
			and.setChildren(andsChildren.toArray());
			
			for (Node child : and.getChildren()) {
				if (child instanceof And) {
					redundantChildren.add((And)child);
				}
			}
		} while (!redundantChildren.isEmpty());
	}
	
	public static List<Node> unpack(List<AnnotatedNode> nodes) {
		List<Node> nodeList = new ArrayList<Node>(nodes.size());
		
		for (int i = 0; i < nodes.size(); ++i)
			nodeList.add(nodes.get(i).node);
		
		return nodeList;
	}
}
