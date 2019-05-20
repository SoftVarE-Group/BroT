package tubs.cs.studienrichtung.featuremodel;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.configuration.Configuration;
import tubs.cs.studienrichtung.util.Output;

public class ConfigurationTimeEvaluator {
	public static final int Timeout = -1;
	public static final int ThreadCrashed = -2;
	private final long timeoutNanos;;
	
	private volatile double  elapsedTime = Timeout;
	private volatile boolean isThreadFinished = false;
	private volatile boolean isThreadCrashed  = false;
	
	private Output output;
	
	private static double toSeconds(long nanoTime) {
		return ((double)nanoTime) / 1_000_000_000.0;
	}
	
	public ConfigurationTimeEvaluator(double timeoutSeconds, Output output) {
		this.timeoutNanos = (long)(timeoutSeconds * 1_000_000_000);
		this.output = output;
		init();
	}
	
	private void init() {
		elapsedTime = Timeout;
		isThreadFinished = false;
		isThreadCrashed  = false;
	}
	
	@SuppressWarnings("deprecation")
	public double getTimeForLoad(IFeatureModel fm) {
		init();
		
		Thread configurationThread = new Thread() {
		    public void run() {
		    	try {
		        	long start = System.nanoTime();
					new Configuration(fm, Configuration.PARAM_PROPAGATE);
					elapsedTime = toSeconds(System.nanoTime() - start);
					isThreadFinished = true;
		    	} 
		    	/*
		    	catch (InterruptedException e) {
		    		// This would be intentionally
					isThreadFinished = elapsedTime != Timeout;
		    		return;
		    	}//*/
		    	catch (Exception e) {
		    		isThreadCrashed = true;
		    		return;
		    	}
		    }  
		};

    	final long timeBegin = System.nanoTime();
    	configurationThread.start();    	
    	
    	if (timeoutNanos < 0) {
    		try {
				configurationThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	} else {
    		long deltaTime = System.nanoTime() - timeBegin;
        	while (!isThreadCrashed && !isThreadFinished && timeoutNanos > deltaTime) {
        		try {
        			// Wait for 1 second
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					
				}
        		
        		deltaTime = System.nanoTime() - timeBegin;
				//output.println("[ConfigurationTimeEvaluator.getTimeForLoad] Waited for " + deltaTime);
        	}
        	
        	if (isThreadCrashed) {
        		//output.println("[ConfigurationTimeEvaluator.getTimeForLoad] CRASHED");
        		return ThreadCrashed;
        	}
        	
        	if (!isThreadFinished) {
        		configurationThread.interrupt();
        		//configurationThread.stop();
        		//output.println("[ConfigurationTimeEvaluator.getTimeForLoad] KILLED");
        		return Timeout;
        	}
    	}

		//output.println("[ConfigurationTimeEvaluator.getTimeForLoad] SUCCESS");
		return elapsedTime;
	}
	
	public static boolean IsTimeValid(double t) {
		return t != Timeout && t != ThreadCrashed;
	}
}
