package dk.kingu.shooting;

import java.util.Collection;

public interface ResultWatchSink {
	
	/**
	 * Notify watcher that there are updated results.  
	 */
	public void update();
}
