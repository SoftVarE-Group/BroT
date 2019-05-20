package tubs.cs.satencodings.generation;

import java.util.HashMap;
import java.util.Map;

import org.prop4j.Literal;

public class DefaultVariableFactory extends VariableFactory {
	private Map<String, Literal> vars;
	
	public DefaultVariableFactory() {
		super();
		vars = new HashMap<>();
	}
	
	public void clear() {
		vars.clear();
	}
	
	@Override
	public Literal getVariable(String name) {
		return vars.get(name);
	}

	@Override
	public Literal createVariable(String name, VariableProperties props) {
		Literal l = new Literal(name);
		vars.put(name, l);
		return l;
	}
}
