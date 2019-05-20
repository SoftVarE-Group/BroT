package tubs.cs.satencodings.util;

public class MathUtils {
	public static long factorial(long n) {
		if (n == 2) {
			return 2;
		} else if (n > 2) {
			return n * factorial(n - 1);
		} else {
			return 1;
		}
	}
	
	public static long over(long n, long k) {
		//return factorial(n) / (factorial(k) * factorial(n - k));
		Assert.error(n >= k);
		
		if (n == k)
			return 1;		
		if(k == 0)
		    return 1;
		if(k > n/2)
		    return over(n, n - k);
		
		return n * over(n - 1, k - 1) / k;
	}
	
	public static int findSmallestDivisor(int n) {
		int d = 2;
		while (d < n) {
			if (n % d == 0)
				break;
			++d;
		}
		
		return d;
	}
	
	public static int findGreatestDivisor(int n) {
		return n / findSmallestDivisor(n);
	}
	
	public static boolean isMultipleOf(int value, int div) {
		return value % div == 0;
	}
}
