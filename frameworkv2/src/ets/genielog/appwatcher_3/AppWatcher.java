package ets.genielog.appwatcher_3;


import java.util.ArrayList;




import android.content.pm.PackageInfo;

/**
 * This class is the link betwteen different activities and services. It holds shared
 * objects and parameters.
 * @author trivh
 *
 */

public class AppWatcher {


	/** Selected application being processed */
	private PackageInfo selectedApk;
	/** Selected model type used */
	//public int usedModel = ModelManager.NEW_MODEL;
	//public int usedModel = ModelManager.DEFAULT_MODEL;
	//public int usedModel = ModelManager.TREE_MODEL;
	public int usedModel;// = ModelManager.HMM_MODEL;


	//DEFAULT_MODEL;
	//TREE_MODEL;
	//NewModel;
	
	/** Listeners notified when there is a change in selected Apk */
	private ArrayList<SelectedApkListener> selApkListener;
	/** Listeners notified when there created trace changes */
	private ArrayList<TraceListener> traceListener;

	/** Model manager used to create, save, and access to models */
	private final ModelManager modelManager;
	
	/** Trace manager used */
	private final TraceManager traceManager;

	/** Parameters that should be configurable via interface later.	 */
	public  int TRACE_LENGTH = 1200; // size of a trace (originally 1200)
	public  int TRACE_Number = 100; 
	public  int NGRAM_SIZE;// = 2; 
	public  double NGRAM_THRESHOLD;// = 0.6; 
	public  double THRESHOLD_MODEL;// = 0.2;
	
	//Filter with list of syscall names to ignore
	ArrayList<String> lfilter = new ArrayList<String>();
	public SysCallFilter filter ;
	 

	/**
	 * Constructor initializes needed objects.
	 */
	public AppWatcher(){
		//ActivityModel.appwatcher.setTraceList(traces);
		//ArrayList<Trace> traces
		 lfilter.add("gettimeofday");
		 lfilter.add("");
		 filter = new SysCallFilter(lfilter);
		selApkListener = new ArrayList<SelectedApkListener>();
		traceListener = new ArrayList<TraceListener>();
		traceManager = new TraceManager(TRACE_LENGTH, null, filter );
		System.out.println("AppWatc 49: traceManager = new TraceManager(TRACE_LENGTH, null);");


		ModelManager.init();
		modelManager = new ModelDBManager();
		System.out.println("AppWatc 54: modelManager = new ModelDBManager()");
		// Model lists initialisation
	}




	/**
	 * Creates a new model with current data and inserts it in the list.
	 * If modelised application is not in database yet, creates an entry for it.
	 * @param model
	 */
	public void newModel(){
	//	System.out.println("Creating new model with "+traceManager.traceList.size() + "traces");
		System.out.println("AppWatcher 66: selectedApk.packageName== "+selectedApk.packageName);
		if (traceManager==null){System.out.println("AppWatcher 67:traceManager==null");	}
		else { if (traceManager.traceList==null){System.out.println("traceManager.traceList=null");}
		else {		System.out.println("AppWatcher 69: size=="+traceManager.traceList.size());
}
		}

//System.out.println("AppWatcher 73: traces== "+traceManager.traceList);

		Model model = modelManager.makeModel(selectedApk.packageName, traceManager.traceList, usedModel, NGRAM_SIZE,NGRAM_THRESHOLD, THRESHOLD_MODEL );
		modelManager.saveModel(model);
		notifyApkListener();
	}


	/**
	 * Update the model  for selected app with current traces and save it.
	 */
	public void updateModel(){
		Model updatedModel = modelManager.updateModel(traceManager.traceList,
				selectedApk.packageName , usedModel, NGRAM_SIZE,NGRAM_THRESHOLD, THRESHOLD_MODEL );
		modelManager.saveModel(updatedModel);
	}
	
	/**
	 * Retrieves the model for selected apk.
	 * @return previously savec model for this apk.
	 */
	public Model getModel(){
		return modelManager.getModel(selectedApk.packageName, usedModel,  NGRAM_SIZE,NGRAM_THRESHOLD, THRESHOLD_MODEL);
	}

	/**
	 * Returns yes if a model is in the list for selected apk.
	 * @return
	 */
	public boolean isModel(){
		if(selectedApk == null){return false;}
		return getModel() != null; 
		//return false; //TODO
	}
	
	/**
	 * Scans selected application. For this, uses saved model for it 
	 * and just made trace.
	 * @return: true if application has an abnormal behaviour
	 */
	public boolean scan(){
		System.out.println("scan");
		Model model = getModel();
		if(usedModel==2){
		return model.scanTracesB(traceManager.traceList, NGRAM_SIZE,NGRAM_THRESHOLD, THRESHOLD_MODEL );
		}else {
			return model.scanTraces(traceManager.traceList, NGRAM_SIZE,NGRAM_THRESHOLD, THRESHOLD_MODEL );
		}
		//return  false; // TODO
	}
	
	/**
	 * Starts trace making
	 * @param context: context running and listening the tracing
	 * @param appName: name of traced app
	 */
	public void startTracing(ActivityModel context, PackageInfo app){
		traceManager.context = context;
		traceManager.callback = (TraceListener)context;
		traceManager.createTrace(app);
	}
	
	/**
	 * Stops tracing
	 * @param appName: app for which tracing i stopped
	 */
	public void stopTracing(String appName){
		traceManager.stopTracing(appName);
		
	}
	

	//##########################################################################################
	//########################### APK SELECTED LISTENER METHODS ################################
	//##########################################################################################
	/**
	 * Add a listener for selected apk to the list.
	 */
	public void addSelApkListener(SelectedApkListener listener){
		selApkListener.add(listener);
		listener.notifyApkChange(selectedApk, isModel());
	}
	/**
	 * Removes a listener for selected apk from the list.
	 */
	public void removeSelApkListener(SelectedApkListener listener){
		selApkListener.remove(listener);
	}
	
	public Boolean isSelApp(SelectedApkListener listener){
		return selApkListener.contains(listener);
		
	}
	
	/**
	 * Notify listeners of a change of selected apk.
	 * */
	private void notifyApkListener(){
		for(int i = 0; i < selApkListener.size(); i++){
			selApkListener.get(i).notifyApkChange(selectedApk, isModel());
		}
	}
	/**
	 * Change selected apk.
	 */
	public void setSelectedApk(PackageInfo apk){
		selectedApk = apk;
		notifyApkListener();
	}
	/** Getter**/
	public String getSelectedApk(){return selectedApk.packageName;}
	
	//##########################################################################################
	//########################### APK TRACE LISTENER METHODS    ################################
	//##########################################################################################
	/**
	 * Add a listener for trace making to the list.
	 */
	public void addTraceListener(TraceListener listener){
		traceListener.add(listener);
	}
	/**
	 * Removes a listener for trace making from the list.
	 */
	public void removeTraceListener(TraceListener listener){
		traceListener.remove(listener);
	}
	
	/**
	 * Notify listeners of a change of trace making.
	 * */
	public void notifyTraceListener(int what){
		for(int i = 0; i < traceListener.size(); i++){
			traceListener.get(i).updateTrace(what);
		}
	}
	
	
	public ArrayList<Trace> getTraceList(){
		if(traceManager == null){System.out.println("AppWatcher194: tracemanager =null"); return null;}
		return traceManager.traceList;
	}
	
	public void setTraceList(ArrayList<Trace> traces){
		System.out.println("Setting trace list : "+ traces.size() + "traces now in list");
		traceManager.traceList = traces;
	}
}
