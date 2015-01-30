package ets.genielog.appwatcher_3;


import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity used when user scans an application.
 * 
 * @author trivh
 *
 */
public class ScanActivity extends ActivityModel 
			implements TraceListener, SelectedApkListener{
	
	private PackageInfo selectedApk;
	/**
	 * On creation, takes common objects from main activity.
	 * Initialises buttons and runs the modelise routine.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan);
		handler = new Handler(new Handler.Callback(){
			public boolean handleMessage(Message msg) {
				// take action depending on message what entry
				switch(msg.what){
				case ActionsID.NEW_TRACE :
					traceDone();
					return true;
				}
				return false;
				
			}
		});
		
		// Setup buttons and their listeners.
		setupButtons();
		resetText();
		appwatcher.addSelApkListener(this);


	}

	@Override
	protected void onDestroy(){
		super.onDestroy();
		appwatcher.removeSelApkListener(this);
		appwatcher.removeTraceListener(this);
	}

	/**
	 * Initialises Activity button.
	 */
	public void setupButtons(){
		Button doneButton = (Button) findViewById(R.id.button_activity_scan_done);
		Button traceButton = (Button) findViewById(R.id.button_activity_scan_trace);
		EditText TraceSize = (EditText) findViewById(R.id.TraceSize_scan_trace);
		EditText NbrTraces = (EditText) findViewById(R.id.NbrTraces_scan_trace);
		traceButton.setEnabled((!TraceSize.getText().toString().equalsIgnoreCase("Size of trace"))&& (!NbrTraces.getText().toString().equalsIgnoreCase("Number of traces")));

			// Set listener for trace button
		traceButton.setOnClickListener(new TraceButtonListener());

		// Set Listener for Done button
		doneButton.setOnClickListener(new OnClickListener(){
			/* When the scan button is clicked on finishes the activity.*/
			@Override
			public void onClick(View arg0) {
				finish();
				
			/*	Runnable r1 = new Runnable() {
					  public void run() {
					    while (true) {
					    	  stopTraces(selectedApk.packageName);
					      //  Thread.sleep(1000L);
					      }
					  }
					};
					Runnable r2 = new Runnable() {
					  public void run() {
					    while (true) {
					    	  stopTrace();
					      //  Thread.sleep(2000L);
					      }
					  }
					};
					Thread thr1 = new Thread(r1);
					Thread thr2 = new Thread(r2);
					thr1.start();
					thr2.start();
					*/
				
			}
		});

	}
	/**
	 * Listener for trace button
	 * @author trivh
	 *
	 */
	public class TraceButtonListener implements OnClickListener{
		@Override
		
		public void onClick(View v) {
			Button button = (Button) v;
			button.setText(R.string.button_activity_modelise_stoptrace);
			button.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					Button button = (Button) arg0;
					button.setOnClickListener(new TraceButtonListener());
					button.setText(R.string.button_activity_modelise_starttrace);
					stopTrace();
				}			
			});
			startTrace();
			
		}
	}

	/**
	 * Reset text view to display that apk scan is being processed.
	 */
	public void resetText(){
		TextView traceText = (TextView) findViewById(R.id.scan_activity_trace_state);
		TextView modelText = (TextView) findViewById(R.id.scan_activity_model_state);

		traceText.setText(R.string.modelise_activity_trace_proc);

		if (appwatcher.isModel()){
			modelText.setText(R.string.modelise_activity_model_already);
		}else{
			modelText.setText(R.string.modelise_activity_model_nomodel);
		}
	}

	public void setTraceOk(){
		System.out.println("Access setTraceOk");
		TextView traceText = (TextView) findViewById(R.id.scan_activity_trace_state);		
		traceText.setText(R.string.modelise_activity_trace_ok);
	}

	/**
	 * Sends intent to TraceService to make it run strace for current selected apk.
	 */
	@Override
	public void sendStraceIntent(){
		Intent intent = new Intent(getApplicationContext(), TraceService.class);
		getApplicationContext().startService(intent);
	}


	/**
	 * Method use to create a trace of an apk.
	 */
	public void startTrace(){
		EditText TraceSize = (EditText) findViewById(R.id.TraceSize_scan_trace);
		EditText NbrTraces = (EditText) findViewById(R.id.NbrTraces_scan_trace);
		int SizeT = Integer.valueOf((TraceSize.getText()).toString());
		 int NbrT = Integer.valueOf((NbrTraces.getText()).toString());
		appwatcher.TRACE_LENGTH=SizeT;
		appwatcher.TRACE_Number=NbrT;
		appwatcher.addTraceListener(this);
		// Creates trace.
		appwatcher.startTracing(this, selectedApk);
	}
	/**
	 * Method used to stop tracing an application.
	 */
	public void stopTraces(String sf){
		appwatcher.stopTracing(sf);
	}
	public void stopTrace(){
		appwatcher.stopTracing(selectedApk.packageName);
	}
	/**
	 * Scan routine
	 */
	public void traceDone(){
		/**
		 * 
		if (appwatcher.isSelApp(this)) {
			laisser les boutons on stop 
		}
		 */
		appwatcher.removeTraceListener(this);
		// Get existing model for current application
		System.out.println("number of TRACE Scanned : " + 
				appwatcher.getTraceList().size());
		Model model = appwatcher.getModel();
		System.out.println("MODEL Ued: " + model.getModelData());

		// Compare traces to model
		boolean abnormal =appwatcher.scan();
		if(abnormal){
			System.out.println("App scanned: ABNORMAL behaviour");
			Toast toast = Toast.makeText(this, model.getAppName()+" : ABNORMAL behaviour", Toast.LENGTH_SHORT);
			toast.show();
		}else{
			System.out.println("App scanned: normal behaviour");
			Toast toast = Toast.makeText(this, model.getAppName()+" : normal behaviour", Toast.LENGTH_SHORT);
			toast.show();
		}

	}
	
	/**
	 * Callback method when a trace is updated.
	 */
	@Override
	public void updateTrace(int what){
		handler.sendEmptyMessage(what);	
	}


	@Override
	public void notifyApkChange(PackageInfo apk, boolean ismodel) {
		selectedApk = apk;
	}


}
