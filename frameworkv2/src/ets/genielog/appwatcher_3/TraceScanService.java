package ets.genielog.appwatcher_3;


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

public class TraceScanService extends Service {

	/**
	 * Run when intent is sent to this activity. It creates a StraceCommand object to
	 * make a strace call and create an appropriate trace. 
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		System.out.println("StraceScan service started");

		Timer timer  =  new Timer();
		timer.schedule(new MyTimerTask() , 3000);
		return Service.START_NOT_STICKY;
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

		private TraceScanService context;

		public MyTimerTask(){
			super();
			this.context = context;
		}
		@Override
		public void run() {
			StraceScanCommand stracecommand = 
					new StraceScanCommand(TestCode.model, TestCode.appname, TestCode.tracelen, TestCode.sysc, context);
			stracecommand.runStrace();
		}

	}

}
