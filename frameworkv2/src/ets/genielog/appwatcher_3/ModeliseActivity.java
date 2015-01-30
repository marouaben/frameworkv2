package ets.genielog.appwatcher_3;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;

import ets.genielog.appwatcher_3.R;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import ets.genielog.appwatcher_3.profiling.core.Processes;
import ets.genielog.appwatcher_3.profiling.core.State;
import ets.genielog.appwatcher_3.profiling.util.Config;
/**
 * Activity used to modelise an application. This class is only the Android Activity,
 * all the routine of modelisation is in AppWatcher class.
 * @author trivh
 *
 */
public class ModeliseActivity extends ActivityModel 
				implements TraceListener, SelectedApkListener {

	// variable to remeber if we want to edit or erase an existing model.
	private int modelMode;
	private final int NEW_MODEL_MODE = 0;
	private final int UPDATE_MODEL_MODE = 1;
	private final int MODEL_COMPLETE = 2;

	private PackageInfo selectedApk;

	/**
	 * On creation, takes common objects from main activity.
	 * Initialises buttons and runs the modelise routine.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_modelise);

		// Setup handler.
		handler = new Handler(new Handler.Callback(){
			public boolean handleMessage(Message msg) {
				// take action depending on message what entry
				switch(msg.what){
				case ActionsID.NEW_TRACE :
					traceDone();
					return true;
				case ActionsID.SELECTED_APK_CHANGE:
					updateUI();
					return true;
				}
				return false;
				
			}
		});
		
		// Setup buttons and their listeners.
		setupButtons();
		initText();
		
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
		Button doneButton = (Button) findViewById(R.id.button_activity_modelise_done);
		Button traceButton = (Button) findViewById(R.id.button_activity_modelise_trace);
		Button newButton = (Button) findViewById(R.id.button_activity_modelise_new);
		Button updateButton = (Button) findViewById(R.id.button_activity_modelise_update);
		EditText TraceSize = (EditText) findViewById(R.id.TraceSize);
		EditText NbrTraces = (EditText) findViewById(R.id.NbrTraces);	
	 
		traceButton.setEnabled((!TraceSize.getText().toString().equalsIgnoreCase("Size of trace"))&& (!NbrTraces.getText().toString().equalsIgnoreCase("Number of traces")));

			
			traceButton.setOnClickListener(new TraceButtonListener());

		
		
		/* Set listener for tracing button. When clicked, launch
		 * trace service and changes into stop tracing button, which stops tracing
		 * activity on click.
		 */		
		
		// Set Listener for Go button
			
		newButton.setOnClickListener(new OnClickListener(){
			
			/* When the scan button is clicked run modelise routine.*/
			@Override
			public void onClick(View arg0) {
				Log.d(Config.TAG_APP, "ModeliseActivity - new model");

				checkModelInput(appwatcher);		
				modelMode = NEW_MODEL_MODE;
				updateUI();
				profiler.requestProfileStateForProcess(State.processAction, android.os.Process.myPid());
				appwatcher.newModel();
				profiler.stopProfiler();
				//stop
				

				/************Profiling*******************
				
				Runnable r1 = new Runnable() {
					  public void run() {
						    try {
						      while (true) {
									appwatcher.newModel();
						        Thread.sleep(1000L);
						      }
						    } catch (InterruptedException iex) {}
						  }
						};
						Runnable cpu = new Runnable() {
						  public void run() {
						    try {
						      while (true) {
						    	  record(profiler, "CPU");
						        Thread.sleep(2000L);
						      }
						    } catch (InterruptedException iex) {}
						  }
						};
						
						Runnable RAM = new Runnable() {
							  public void run() {
							    try {
							      while (true) {
							    	  record(profiler, "RAM");
							        Thread.sleep(2000L);
							      }
							    } catch (InterruptedException iex) {}
							  }
							};
						
						Thread thr1 = new Thread(r1);
						Thread thr2 = new Thread(cpu);
						Thread thr3 = new Thread(RAM);
						thr1.start();
						thr2.start();
						thr3.start();
				*****************************************/
			}
		});

		//Set listener for update button
		updateButton.setOnClickListener(new OnClickListener(){
			
			/* When the scan button is clicked run modelise routine.*/
			@Override
			public void onClick(View arg0) {
				Log.d(Config.TAG_APP, "ModeliseActivity - update model");
				checkModelInput(appwatcher);		
				modelMode = UPDATE_MODEL_MODE;
				updateUI();
				appwatcher.updateModel();
			}
		});
	
	
		// Set Listener for Done button
		doneButton.setOnClickListener(new OnClickListener(){
			/* When the scan button is clicked on finishes the activity.*/
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		EditText ThresholdTotal = (EditText) findViewById(R.id.ThresholdTotal);

		// Update button: if no model, don't display it.
		newButton.setEnabled(((!ThresholdTotal.getText().toString().equalsIgnoreCase("Enter a Threshold"))));
		updateButton.setEnabled(appwatcher.isModel()&&((!ThresholdTotal.getText().toString().equalsIgnoreCase("Enter a Threshold"))));
		traceButton.setText(R.string.button_activity_modelise_starttrace);

	}
	/*private void record(State st, String nameP) {
		// TODO Auto-generated method stub
		/*********************************************************************
		if(!new File("/mnt/sdcard/Traces/profiling").exists())
        {
            // Créer le dossier avec tous ses parents
			new File("/mnt/sdcard/Traces/profiling").mkdirs();
 
        }
		**********************************************************

		File file = new File("/mnt/sdcard/Traces/profiling/"+nameP+"_"+appwatcher.getSelectedApk()+"_SizeN"+appwatcher.NGRAM_SIZE+"_ThresN"+appwatcher.NGRAM_THRESHOLD+"_ThresMod"+appwatcher.THRESHOLD_MODEL+"txt");
		ObjectOutputStream oos;
		 
		if (! file.exists())
		{
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		/**********************************************************************/
	//}
	private void checkModelInput(AppWatcher appwatcher) {
		// TODO Auto-generated method stub
		EditText ThresholdTotal = (EditText) findViewById(R.id.ThresholdTotal);
		Spinner TypeM= (Spinner)findViewById(R.id.ModelType);
		Spinner NgramL= (Spinner)findViewById(R.id.Ngram);
		Spinner ThresNgram= (Spinner)findViewById(R.id.ThresholdNgram);
	 
	if ((!ThresholdTotal.getText().toString().equalsIgnoreCase("Enter a Threshold"))&&(!ThresholdTotal.getText().toString().equalsIgnoreCase(""))) {
		double ThresholdT=Double.valueOf(ThresholdTotal.getText().toString());
		 
		String model=String.valueOf(TypeM.getSelectedItem());
		int NgramS=Integer.parseInt(String.valueOf(NgramL.getSelectedItem()).toString());
		double ThresNgramModel=Double.parseDouble(String.valueOf(ThresNgram.getSelectedItem()).toString());
		
		int usedModel ;
		updateUI();
		if (model.equalsIgnoreCase("Lookahead")) {
			usedModel=ModelManager.DEFAULT_MODEL;
		}else if (model.equalsIgnoreCase("Tree")) {
			usedModel=ModelManager.TREE_MODEL;
		}else if (model.equalsIgnoreCase("V.L.N-grams")) {
			usedModel=ModelManager.NEW_MODEL;
		}else {
			usedModel=ModelManager.HMM_MODEL;
		}
		if (model.equalsIgnoreCase("Lookahead")) {
			usedModel=ModelManager.DEFAULT_MODEL;
		}else if (model.equalsIgnoreCase("Tree")) {
			usedModel=ModelManager.TREE_MODEL;
		}else if (model.equalsIgnoreCase("V.L.N-grams")) {
			usedModel=ModelManager.NEW_MODEL;
		}else {
			usedModel=ModelManager.HMM_MODEL;
		}
						
		appwatcher.usedModel=usedModel;
		appwatcher.THRESHOLD_MODEL=ThresholdT;
		appwatcher.NGRAM_THRESHOLD=ThresNgramModel;
		appwatcher.NGRAM_SIZE=NgramS;
		
	}
	}
	/**
	 * Listener for trace button.
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
	 * Sets text when this activity is called.
	 */
	public void initText(){
		TextView traceText = (TextView) findViewById(R.id.trace_state);
		TextView modelText = (TextView) findViewById(R.id.model_state);

		traceText.setText(R.string.modelise_activity_trace_proc);

		if (appwatcher.isModel()){
			modelText.setText(R.string.modelise_activity_model_already);
		}else{
			modelText.setText(R.string.modelise_activity_model_nomodel);
		}

	}
	/**
	 * Checks if changes occured; updates text and buttons consequently.
	 */
	private void updateUI(){

		EditText TraceSize = (EditText) findViewById(R.id.TraceSize);
		EditText NbrTraces = (EditText) findViewById(R.id.NbrTraces);
		Spinner TypeM= (Spinner)findViewById(R.id.ModelType);
		Spinner NgramL= (Spinner)findViewById(R.id.Ngram);
		Spinner ThresNgram= (Spinner)findViewById(R.id.ThresholdNgram);
		EditText ThresholdTotal = (EditText) findViewById(R.id.ThresholdTotal);
		// If there is a model, the update button becomes visible.
		Button updateButton = (Button) findViewById(R.id.button_activity_modelise_update);
		// Update button: if model, display it.
		updateButton.setEnabled(appwatcher.isModel()
				&&((!ThresholdTotal.getText().toString().equalsIgnoreCase("Enter a Threshold")))
				&&(!ThresholdTotal.getText().toString().equalsIgnoreCase(""))
				&&(!ThresholdTotal.getText().toString().isEmpty()));

		//updateButton.setEnabled(appwatcher.isModel());

		
		TextView modelText = (TextView) findViewById(R.id.model_state);
		switch(modelMode){
		case NEW_MODEL_MODE:
			modelText.setText(R.string.modelise_activity_model_replace);
			break;
		case UPDATE_MODEL_MODE:
			modelText.setText(R.string.modelise_activity_model_update);
			break;
		case MODEL_COMPLETE:
			modelText.setText(R.string.modelise_activity_model_ok);
			break;
		}
		
		TextView traceText = (TextView) findViewById(R.id.trace_state);
		if(appwatcher.getTraceList() != null){
			traceText.setText(R.string.modelise_activity_trace_ok);
		}else{traceText.setText(R.string.modelise_activity_trace_proc);}	
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
	 * Method use to start tracing an application.
	 * @param nbrT 
	 * @param sizeT 
	 */
	public void startTrace(){
		Log.d(Config.TAG_APP, "ModeliseActivity - startTrace ");
		EditText TraceSize = (EditText) findViewById(R.id.TraceSize);
		EditText NbrTraces = (EditText) findViewById(R.id.NbrTraces);
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
	public void stopTrace(){
		Log.d(Config.TAG_APP, "ModeliseActivity - stopTrace ");
		appwatcher.stopTracing(selectedApk.packageName);
	}

	/**
	 * Method called once trace is created.
	 */
	public void traceDone(){
		appwatcher.removeTraceListener(this);
		updateUI();
	}

	/**
	 * Callback method when a trace is updated.
	 */
	@Override
	public void updateTrace(int what){
		handler.sendEmptyMessage(what);	
	}

	/**
	 * Callback method when selected apk is updated.
	 */
	@Override
	public void notifyApkChange(PackageInfo apk, boolean ismodel) {
		selectedApk = apk;
		handler.sendEmptyMessage(ActionsID.SELECTED_APK_CHANGE);
	}


}
