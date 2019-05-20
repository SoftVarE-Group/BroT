package tubs.cs.satencodings.evaluation;

import org.prop4j.Literal;

import tubs.cs.satencodings.generation.DefaultVariableFactory;

public class EvaluationVariableFactory extends DefaultVariableFactory {
	private int numGeneratedVars;
	
	public EvaluationVariableFactory() {
		super();
		numGeneratedVars = 0;
	}

	@Override
	public void clear() {
		super.clear();
		numGeneratedVars = 0;
	}
	
	@Override
	public Literal createVariable(String name, VariableProperties props) {
		++numGeneratedVars;
		return super.createVariable(name, props);
	}
	
	public int getNumberOfGeneratedVariables() {
		return numGeneratedVars;
	}
}
