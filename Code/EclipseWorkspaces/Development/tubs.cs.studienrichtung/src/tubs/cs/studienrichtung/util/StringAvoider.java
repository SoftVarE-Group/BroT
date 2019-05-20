package tubs.cs.studienrichtung.util;

import java.util.HashMap;
import java.util.Map;

/*
 * If T is String this class is essentially useless.
 */
public class StringAvoider<T> {
	public static interface UniqueValueGenerator<U> {
		public U generateUniqueWord();
	}
	
	public static class UniqueIntGenerator implements UniqueValueGenerator<Integer> {
		private static int id = 0;
		@Override
		public Integer generateUniqueWord() {
			return id++;
		}
	}
	
	private Map<String, T> translation;
	private UniqueValueGenerator<? extends T> generator;
	
	public StringAvoider(UniqueValueGenerator<? extends T> generator) {
		this.translation = new HashMap<>();
		this.generator = generator;
	}
	
	public T translate(String string) {
		T value = translation.get(string);
		
		if (value == null) {
			value = generator.generateUniqueWord();
			translation.put(string, value);
		}
		
		return value;
	}
	
	public void clear() {
		this.translation.clear();
	}
}
