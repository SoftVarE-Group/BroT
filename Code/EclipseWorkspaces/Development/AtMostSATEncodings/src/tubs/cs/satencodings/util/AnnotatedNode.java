package tubs.cs.satencodings.util;

import java.util.List;

import org.prop4j.Node;

public class AnnotatedNode {
	public final Node node;
	public final String annotation;
	
	public AnnotatedNode(Node node, String annotation) {
		this.node = node;
		this.annotation = annotation;
	}
	
	public AnnotatedNode(Node node) {
		this(node, "");
	}
	
	public static <T extends Node> String toString(List<T> nodes) {
		String ret = "";
		
		if (nodes.size() > 1) {
			ret = nodes.get(0).toString();
			
			for (int i = 1; i < nodes.size(); ++i) {
				ret += ", " + nodes.get(i);
			}
		}
		
		return ret;
	}
}
