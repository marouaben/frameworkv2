package ets.genielog.appwatcher_3;


import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;




 

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;



/**
 * Class used to run strace in background as a service.
 * Each time it receives an intent it runs the corresponding strace and starts
 * processing sequence into valid trace. Once enough traces have been stored, it sends
 * back the trace vector to the TraceManager object and stops the strace command.
 * @author trivh
 *
 */

public class TraceService extends Service {

	/**
	 * Run when intent is sent to this activity. It creates a StraceCommand object to
	 * make a strace call and create an appropriate trace. 
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		System.out.println("Strace service started");

		Timer timer  =  new Timer();
		timer.schedule(new MyTimerTask(this) , 3000);
		return Service.START_NOT_STICKY;
	}

	/** Method called once trace has been created.
	 * Sends back traces to the traceManager.
	 * @param traces: list of traces sent back.
	 */
	public void traceDone(ArrayList<Trace> traces){
	if (traces!=null){	
		System.out.println("Setting traceList of traceManager: " + traces.size() + "traces to add.");
		ActivityModel.appwatcher.setTraceList(traces);
	}
		ActivityModel.appwatcher.notifyTraceListener(ActionsID.NEW_TRACE);
	}


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * Timer task class used to delay service execution to let it have time
	 * to initialize.
	 * @author trivh
	 *
	 */
	class MyTimerTask extends TimerTask{

		private TraceService context;
		public MyTimerTask(TraceService context){
			super();
			this.context = context;
		}
		@Override
		public void run() {
			StraceCommand stracecommand = 
					new StraceCommand(ActivityModel.appwatcher, context);
			stracecommand.runStrace();
		}

	}

}
