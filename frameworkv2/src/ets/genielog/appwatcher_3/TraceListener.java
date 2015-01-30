package ets.genielog.appwatcher_3;



/** Interface implemented by activities that need to create a trace of
 * an application. Its method allows the callback after traceService
 * finished to create traces.
 * @author trivh
 *
 */
public interface TraceListener {
	
	/**
	 * Method to call once trace is created.
	 */
	public void updateTrace(int what);
	
	/**
	 * Method to call to send an intent to TraceService.
	 */
	public void sendStraceIntent();

}
