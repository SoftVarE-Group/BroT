package tubs.cs.studienrichtung.util;

public class ArrayUtils {
	public static void setAllEntriesTo(boolean[] array, boolean value) {
		for (int i = 0; i < array.length; ++i)
			array[i] = value;
	}
}
