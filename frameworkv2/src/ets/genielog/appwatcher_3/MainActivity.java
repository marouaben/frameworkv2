package ets.genielog.appwatcher_3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import ets.genielog.appwatcher_3.profiling.core.Memory;
import ets.genielog.appwatcher_3.profiling.core.State;
import ets.genielog.appwatcher_3.profiling.util.Config;
import android.widget.Spinner;

/** Main activity launched with application. Displays the installed apk list and 
 * two buttons. User must choose an apk and click one of these buttons, which 
 * will run the corresponding activity. There is a button to scan an apk and another
 * to modelise it.
 * @author trivh
 *
 */
public class MainActivity extends ActivityModel implements SelectedApkListener, OnClickListener{

	public static boolean toscan;

	/** Installed packages list */
	private List<PackageInfo> packageList;
	
	/** Selected apk and boolean saying whether there is a model for it. */
	private PackageInfo selectedApk;
	private boolean isModel;

	
	/**
	 * Launched at activity creation. 
	 * Creates buttons and their listeners.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Android initialization stuff.
		super.onCreate(savedInstanceState);
		
		// Setup handler.
		handler = new Handler(new Handler.Callback(){
			public boolean handleMessage(Message msg) {
				switch(msg.what){
				case ActionsID.SELECTED_APK_CHANGE :
					updateButtons();
					return true;
				}
				return false;
			}
		});
		
		setContentView(R.layout.activity_main);
		// Initializes appwatcher class
		appwatcher = new AppWatcher();
		appwatcher.addSelApkListener(this);
		ActivityModel.appwatcher.setTraceList(setTemporarytraces());

		// Setup buttons and their listeners.
		setupButtons();
		/********** PROFILING ***********/
		Runtime rt = Runtime.getRuntime();
		long maxMemory = rt.maxMemory();
		Log.d(Config.TAG_APP,
				"MainActivity - maxMemory:"
						+ Memory.convertToSize(maxMemory, true));
		
		Log.d(Config.TAG_APP,
				"new profiler");
		profiler = new State(this);
		Log.d(Config.TAG_APP,
				"init profiler");
		profiler.initProfiler();
		/********************************/
			
	}

	private static ArrayList<Trace> setTemporarytraces() {
		// TODO Auto-generated method stub
		File fichier = new File("/mnt/sdcard/Traces/StreamToTrace/data");
		ObjectInputStream ois;
		ArrayList<String> tr;
		
		String reqReturn="";
		
		//lecture du fichier texte	
		try{
			InputStream ips=new FileInputStream(fichier); 
			InputStreamReader ipsr=new InputStreamReader(ips);
			BufferedReader br=new BufferedReader(ipsr);
			String ligne;
			while ((ligne=br.readLine())!=null){
				//System.out.println(ligne);
				reqReturn+=ligne;
			}
			br.close(); 
		}		
		catch (Exception e){
			System.out.println(e.toString());
		}
		
		Trace t= new Trace();
		String [] sp =reqReturn.split(";");
		for (int i=0; i<sp.length; i++){
			t.add(sp[i]);
		}
		ArrayList<Trace> arr =new ArrayList<Trace>();
		arr.add(t);
		
		return arr;
	}


	/**
	 * Update apk list when user resumes the app, in case user installed a new app when
	 * this was minimised.
	 */
	@Override
	protected void onResume(){
		super.onResume();
		// Get apk list
		PackageManager packageManager = getPackageManager();
		packageList = packageManager
				.getInstalledPackages(PackageManager.GET_PERMISSIONS);
		// Seteup app list view and its listener.
		setupListView();
	}
	
	/**
	 * If this activity is destroyed, remove it from listener list.
	 */
	@Override
	protected void onDestroy(){
		super.onDestroy();
		appwatcher.removeSelApkListener(this);
	}

	/**
	 * Initializes the two main buttons of this activity: Scan app and Modelise app.
	 * Initialization consists on creating OnClickListeners for them that launches
	 * corresponding activities.
	 */
	public void setupButtons(){
		// Find buttons created in activity_main.xml
		Button scanbutton = (Button) findViewById(R.id.button_scan);
		Button modelbutton = (Button) findViewById(R.id.button_modelise);
		Button benchbutton = (Button) findViewById(R.id.button_bench);
		Spinner TypeM= (Spinner)findViewById(R.id.ModelTypeM);
		Spinner NgramL= (Spinner)findViewById(R.id.NgramM);
		Spinner ThresNgram= (Spinner)findViewById(R.id.ThresholdNgramM);
		EditText ThresholdTotal = (EditText) findViewById(R.id.ThresholdTotalM);
		/**
		 * 		updateButton.setEnabled(appwatcher.isModel()
				&&((!ThresholdTotal.getText().toString().equalsIgnoreCase("Enter a Threshold")))
				&&(!ThresholdTotal.getText().toString().equalsIgnoreCase(""))
				&&(!ThresholdTotal.getText().toString().isEmpty()));
		 */
		//		traceButton.setEnabled((!TraceSize.getText().toString().equalsIgnoreCase("Size of trace"))&& (!NbrTraces.getText().toString().equalsIgnoreCase("Number of traces")));


		// Set Listeners
		scanbutton.setOnClickListener(new OnClickListener(){
			/* Runs the scan activity*/
			@Override
			public void onClick(View arg0) {
				toscan=true;
				Intent intent = new Intent(getApplicationContext(), ScanActivity.class);
				startActivity(intent);
			}

		});
		modelbutton.setOnClickListener(new OnClickListener(){
			/* Runs the modelise activity.*/
			@Override
			public void onClick(View v) {
				toscan=false;
				Intent intent = new Intent(getApplicationContext(), ModeliseActivity.class);
				startActivity(intent);		
			}
		});
		
		benchbutton.setOnClickListener(this);
		
		benchbutton.setEnabled(true);
		// Buttons are faded as long as no apk is selected
		scanbutton.setEnabled(false);
		modelbutton.setEnabled(false);
	}


	/**
	 * Retrives apk names from apk list and setups the view that displays them.
	 * Involves attaching ArrayAdapter to the View and setting the OnItemClickListener.
	 */
	public void setupListView(){
		// Get apk name list
		List<String> packageName = new ArrayList<String>();
		for(int i = 0; i < packageList.size(); i++){
			packageName.add(i, packageList.get(i).packageName);
		}

		//Display app list
		ListView apkListView = (ListView) findViewById(R.id.applist);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_list_item_1, packageName);
		apkListView.setAdapter(adapter);

		//When an apk is selected, send it to appwatcher.
		apkListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> adapter, View arg1, int position,
					long id) {
				appwatcher.setSelectedApk(packageList.get(position));
			}

		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

 
	/**
	 * Called when selected activity is changed.
	 * @param apk: new selected apk
	 * @param ismodel: true if there is a model for this apk.
	 */
	@Override
	public void notifyApkChange(PackageInfo apk, boolean ismodel) {
		selectedApk = apk;
		this.isModel = ismodel;
		
		// Send message to handler to make it update interface from the good
		// thread.
		handler.sendEmptyMessage(ActionsID.SELECTED_APK_CHANGE);
	}
	
	/** Enable or disable buttons depending on what can be done with
	 * selected apk.*/
	private void updateButtons(){
		EditText ThresholdTotal = (EditText) findViewById(R.id.ThresholdTotalM);
		Button scanbutton = (Button) findViewById(R.id.button_scan);
		Button modelbutton = (Button) findViewById(R.id.button_modelise);
		if(selectedApk == null){
			scanbutton.setEnabled(false);
			modelbutton.setEnabled(false);
		}else{
			modelbutton.setEnabled(true);
			checkModelInput(appwatcher); //to retreive parametres of the model
			if(isModel && (!ThresholdTotal.getText().toString().equalsIgnoreCase("Enter a Threshold"))&&(!ThresholdTotal.getText().toString().equalsIgnoreCase(""))){	
				scanbutton.setEnabled(true);
			}else{scanbutton.setEnabled(false);}
		}
	}
	
	public void runScanService(){
		Intent intent = new Intent(getApplicationContext(), TraceScanService.class);
		getApplicationContext().startService(intent);
	}


	@Override
	public void onClick(View v) {
		TestCode.runCode(this);
		
	}
	
	private void checkModelInput(AppWatcher appwatcher) {
		// TODO Auto-generated method stub
		EditText ThresholdTotal = (EditText) findViewById(R.id.ThresholdTotalM);
		Spinner TypeM= (Spinner)findViewById(R.id.ModelTypeM);
		Spinner NgramL= (Spinner)findViewById(R.id.NgramM);
		Spinner ThresNgram= (Spinner)findViewById(R.id.ThresholdNgramM);
	 
	if ((!ThresholdTotal.getText().toString().equalsIgnoreCase("Enter a Threshold"))&&(!ThresholdTotal.getText().toString().equalsIgnoreCase(""))) {
		double ThresholdT=Double.valueOf(ThresholdTotal.getText().toString());
		 
		String model=String.valueOf(TypeM.getSelectedItem());
		int NgramS=Integer.parseInt(String.valueOf(NgramL.getSelectedItem()).toString());
		double ThresNgramModel=Double.parseDouble(String.valueOf(ThresNgram.getSelectedItem()).toString());
		
		int usedModel ;
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

}
