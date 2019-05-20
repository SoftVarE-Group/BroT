package tubs.cs.studienrichtung.util.localization;

import java.util.HashMap;
import java.util.Map;

public abstract class Localization {
	@SuppressWarnings("rawtypes")
	protected Map<Class, String> names;
	
	public Localization() {
		names = new HashMap<>();
		initialize();
	}
	
	protected abstract void initialize();
	
	public <T> String getNameOfModelClass(Class<T> type) {
		return names.get(type);
	}
}
