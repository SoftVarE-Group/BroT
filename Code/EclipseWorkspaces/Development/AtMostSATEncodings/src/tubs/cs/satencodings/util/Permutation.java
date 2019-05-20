package tubs.cs.satencodings.util;

public class Permutation {
	private boolean[] perm;
	private final int numberOfOnes;
	
	public Permutation(int size, int numberOfOnes) {
		Assert.error(numberOfOnes <= size, "numberOfOnes <= size (" + numberOfOnes + " <= " + size + ")");
		
		perm = new boolean[size];
		this.numberOfOnes = numberOfOnes;
		
		// array has to be sorted according to @see less
		for (int i = size - 1; i > size - this.numberOfOnes - 1; --i) {
			perm[i] = true;
		}
	}
	
	public boolean[] get() {
		return perm;
	}
	
	public int size() {
		return perm.length;
	}
	
	public int getNumberOfOnes() {
		return numberOfOnes;
	}
	
	/**
	 * Implementation from https://en.cppreference.com/w/cpp/algorithm/next_permutation
	 * @return True, if a new permutation was generated. False, if the current permutation is the last one.
	 */
	public boolean next() {
		int first = 0;
		int last = perm.length;
		
		if (first == last) return false;
	    int i = last;
	    if (first == --i) return false;
	 
	    while (true) {
	        int i1, i2;
	 
	        i1 = i;
	        if (less(perm[--i], perm[i1])) {
	            i2 = last;
	            while (!less(perm[i], perm[--i2]))
	                ;
	            swap(i, i2);
	            reverse(i1, last);
	            return true;
	        }
	        if (i == first) {
	            reverse(first, last);
	            return false;
	        }
	    }
	}
	
	private boolean less(boolean a, boolean b) {
		return !a && b;
	}
	
	private void swap(int i1, int i2) {
		boolean temp = perm[i1];
    	perm[i1] = perm[i2];
    	perm[i2] = temp;
	}
	
	private void reverse(int first, int last) {
	    while ((first != last) && (first != --last)) {
	        swap(first++, last);
	    }
	}
	
	public void print() {
		for (boolean b : perm) {
			System.out.print(b ? "1" : "0");
		}
		System.out.println();
	}
}
