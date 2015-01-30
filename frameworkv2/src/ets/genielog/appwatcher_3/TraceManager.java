package ets.genielog.appwatcher_3;


import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.PackageInfo;

/** 
 * Class used to create a trace of an apk.
 * Interacts with user to make sure the apk to scan is running and runs a background service which
 * handle trace making.
 * 
 *
 */
public class TraceManager {
	/**
	 * Parameters of trace making
	 */
	protected int trace_len;
	/** List of traces created */
	public ArrayList<Trace> traceList;
	/**
	 * Android Context.
	 */
	protected ActivityModel context;
	/**
	 * Class called back once trace finished
	 */
	protected TraceListener callback;
	SysCallFilter filter ;
	
	/**
	 * List of strace commands running, classified by traced application names
	 */
	private static Hashtable<String, StraceCommand> straces;

	/**
	 * Simple constructor using default trace filter
	 * @param filter 
	 * @param trace_len: length of trace
	 * @param filter: filter used to select only some calls in trace
	 * @param interf: user interface used
	 */
	public TraceManager(int trace_len,  ActivityModel context, SysCallFilter filter){
		this.trace_len = trace_len;
		this.filter=filter;
		this.context = context;
		this.callback = (TraceListener) context;
		if(straces == null){		
			System.out.println("TraceManager 53: straces == null");

			straces = new Hashtable<String, StraceCommand>();
		}
	}

	/**
	 * Creates a list of traces of an apk, depending on the paremeter of the class.
	 * To do so, prompts user to run the apk of it is not currently running, and strace it.
	 * Then, normalizes the trace.
	 * @param apk: apk whose traces are made.
	 * @return: list of traces
	 */
	public void createTrace(PackageInfo apk){

		// Check if app is running

		if(!isRunning(apk)){
			// If not, prompt user to run it.
			System.out.println("apk not running, please run it.");
			context.prompt(PromptDialogFragment.RUN_APP_ID);
			
		}
		System.out.println("apk running");
		if(straces.get(apk.packageName)== null){
			callback.sendStraceIntent();
			System.out.println(" TraceManager77:sendStraceIntent (activer traceservice) "+straces.get(apk.packageName)+"== null");
		}
		
		// Wait until service completed trace retreiving.
	}
	
	/**
	 * Stops the strace command for an app.
	 * @param appName
	 */
	public void stopTracing(String appName){
		StraceCommand strace = straces.get(appName);
		strace.stop();
	}

	/**
	 *  Checks if a tracable process currenlty exist for an apk.
	 * @param apk: apk which process is checked
	 * @return true if there is a process.
	 */
	public boolean isRunning(PackageInfo apk){
		//Retrive app name
		String appName = apk.packageName;
		System.out.println(" TraceManager100: appName=="+appName);

		//Retrive process list
		ActivityManager manager = 
				(ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> processes = manager.getRunningAppProcesses();

		for (RunningAppProcessInfo process : processes)
		{			
			if(process.processName.equals(appName)){
				return true;
			}
		}
		return false;
	}
	/**
	 * Adds a strace to straces list.
	 */
	public static void addStrace(String appName, StraceCommand strace){
		straces.put(appName, strace);
	}
	/**
	 * Removes a strace from straces list
	 */
	public static void rmStrace(String appName){
		straces.remove(appName);
	}
}
