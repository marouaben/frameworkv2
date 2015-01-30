package ets.genielog.appwatcher_3;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.lang.reflect.Type;










//import Profiling.State;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;








import com.google.gson.reflect.TypeToken;

import ets.genielog.appwatcher_3.profiling.core.State;

/**
 * Class used to run a strace command and 
 * Can run a ps or a strace command only.
 *
 */
public class StraceCommand {

	Boolean scanning=false;
	/** Straced process id and name*/
	private int pid = -1;
	private String name = "";
	private int nbrtrace;
	/** Trace length */
	private int lentrace;
	/** boolean to set to false to stop strace */
	private boolean keepGoing;
	/** Filter with list of syscalls to ignore */
	private SysCallFilter sysCallFilter;

	/** Output and error stream threads. */
	public StreamToTrace fluxSortie;
	public StreamDisplay fluxErreur;
	/** Process */
	private java.lang.Process process;
	private int stracePid=-1; // PID of strace process
	/** Android context service */
	private TraceService context;

	/** Constructor setups the name of the process to strace, and trace making
	 * parmeters.
	 * @param name: process name
	 * @param nbtrace: number of traces to make
	 * @param lentrace: length of traces to make
	 *  */
	public StraceCommand(AppWatcher appwatcher, TraceService context){
		this.name = appwatcher.getSelectedApk();
		this.lentrace = appwatcher.TRACE_LENGTH;
		this.nbrtrace = appwatcher.TRACE_Number;
		this.sysCallFilter = appwatcher.filter;
		this.context = context;
		keepGoing = true;
	}

	/** Used to run strace command. Will run a ps to get the pid from application name.
	 * Then runs strace with the pid found. Error stream is printed on console while
	 * Output stream is filtered and converted into a valid trace. Once enough traces
	 * have been created, the process is stopped.
	 */
	public void runStrace(){
System.out.println("name "+name);
//name com.rovio.angrybirdsspace.ads
	try {
		while ((pid <= 0)&&(keepGoing))
		{ pid =getPid (name);
	 Thread.sleep(3000);
		}

		} catch (ArrayIndexOutOfBoundsException e){
			System.out.println("Stace Command error: can't retreive process PID.");
			System.out.println("Array Index out of bounds: application may " +
					"be not running");
			e.printStackTrace();
			return;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (pid > 0)
		{ //System.out.println("pid > 0 : "+ pid);
			strace(pid);
			// Get Pid of process we just ran to be able to kill it later.
			//stracePid = getPid("strace"); //TODO
			
		System.out.println("Sequences extracted; traces are being processed. Please wait.");
		context.traceDone(fluxSortie.traces);}
		// Send extracted traces back to context
		
 
	} 

	/** 
	 * Gets the PID of process from its name. Do so by running ps and grep commands.
	 * If ps command fails, returns -1.
	 * @param name
	 * @return
	 */
	
	private int getPid (String name) {
		String	comm="ps | /data/busybox/grep "+name+" | /data/busybox/cut -c10-15";
			
			  int read;
		      byte[] buffer = new byte[4096];
		      String erreur = new String();
		      String line="";
	   	   String Getline="";
		    
		      int res = -1;
		    try {
		            Process p = Runtime.getRuntime().exec( "su" );
		            InputStream es = p.getErrorStream();
		            DataOutputStream os = new DataOutputStream(p.getOutputStream());
		        	DataInputStream osRes = new DataInputStream(p.getInputStream());
		            BufferedReader d = new BufferedReader(new InputStreamReader(osRes));

		            os.writeBytes(comm + "\n");
		            os.writeBytes("exit\n");
		            os.flush();
		            os.close();

		        	 
		        	   while ( (line=d.readLine()) != null ) {
		        	  
		        		   Getline = line;
		        		   System.out.println("Getline= "+Getline);
			         
		        	   }
		       	            
		            while ((read = es.read(buffer)) > 0) {
		           	 erreur += new String(buffer, 0, read);
		            }
		           
		            p.waitFor();
		            Log.e("log_tag", erreur.trim() + " (" + p.exitValue() + ")");
		            
		    } catch (IOException e) {
		   	 Log.e("log_tag", e.getMessage());

		   	 
		    } catch (InterruptedException e) {
		   	 Log.e("log_tag", e.getMessage());

		    }
		   
		    
				 if (((erreur.trim().isEmpty())) || (erreur==null)){   
					 if (!(Getline.isEmpty()) || (Getline!=null)){   
							String[] spl = Getline.trim().split("\\s+"); //by space
							 //System.out.println("spl[0].trim():\n"+spl[0].trim());
							if((spl[0].trim())!="")
							{res=Integer.valueOf(spl[0].trim());}
							
							 System.out.println("res:\n"+res);
							   					
					     }
		       	 
		      }
				 else{
					 System.out.println("err:\n"+erreur);
					  }
				 
				 return res;
				
	    }
		

	/**
	 * Executes a strace command as root user. Attach error and output streams as class 
	 * attribute for further interaction.
	 * @param command
	 */
	private void strace (int pid) {
		
		try {
			TraceManager.addStrace(name, this);
			System.out.println("Running strace on process " + pid);
			//run strace
			ProcessBuilder pb = new ProcessBuilder(new String[]{"su", "-c","strace -qfF -p "+ pid});

			/********** PROFILING ***********/
			State.PID = pid;
			/*******************************/
			
			process = pb.start();


			//create output and error streams
			fluxErreur = new StreamDisplay(process.getInputStream());
			fluxSortie = new StreamToTrace(process.getErrorStream());

			// run streams on separate threads
			new Thread(fluxSortie).start();
			new Thread(fluxErreur).start();
			process.waitFor();


		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		TraceManager.rmStrace(name);

	}
	
	/**
	 * Stops an ongoing strace
	 */
	public void stop(){
		
		// Once done, stop process
		stracePid=getPid("strace");
		System.out.println("stracePid : "+stracePid);
		if (stracePid > 0) { 	
			System.out.println("Stopping strace... make sure the loop actually ended");
			killSuProc(stracePid); //TODO 
		}
		System.out.println("Sequences extracted; traces are being processed. Please wait.");
		keepGoing=false;
		if (getPid(name) > 0){context.traceDone(fluxSortie.traces);
		}
			
	}

	/**
	 * Kills a root process. Necessary to kill strace we run.
	 * @param Pid: pid of strace to kill
	 */
	private void killSuProc(int Pid){
			String	comm=" kill -9 "+Pid;
				
				  int read;
			      byte[] buffer = new byte[4096];
			      String erreur = new String();
			      String line="";
			     try {
			            Process p = Runtime.getRuntime().exec( "su" );
			            InputStream es = p.getErrorStream();
			            DataOutputStream os = new DataOutputStream(p.getOutputStream());
			        	DataInputStream osRes = new DataInputStream(p.getInputStream());
			            BufferedReader d = new BufferedReader(new InputStreamReader(osRes));

			            os.writeBytes(comm + "\n");
			            os.writeBytes("exit\n");
			            os.flush();
			            os.close();

			       	            
			            while ((read = es.read(buffer)) > 0) {
			           	 erreur += new String(buffer, 0, read);
			            }
			           
			            p.waitFor();
			            Log.e("log_tag", erreur.trim() + " (" + p.exitValue() + ")");
			            
			    } catch (IOException e) {
			   	 Log.e("log_tag", e.getMessage());

			   	 
			    } catch (InterruptedException e) {
			   	 Log.e("log_tag", e.getMessage());

			    }
			   
			    
					 if (((erreur.trim().isEmpty())) || (erreur==null)){   
								 System.out.println("killing: "+Pid);
									   					
						     }
			       	 else{
						 System.out.println("err:\n"+erreur);
						  }
	   
	}


	/**
	 * Class used to read a stream. Implements runnable to be able to run error
	 * and output stream in different threads, as needed if executed program outputs
	 * on both streams. This class displays the stream in console.
	 *
	 */
	class StreamDisplay implements Runnable {

		/** Stream to display */
		protected final InputStream inputStream;
		/** Constructor */
		public StreamDisplay(InputStream inputStream) {
			this.inputStream = inputStream;
		}
		/** Getter */
		protected BufferedReader getBufferedReader(InputStream is) {
			return new BufferedReader(new InputStreamReader(is));
		}

		/** When run, uses a buffer to read the stream and display it in console
		 * line per line. */
		@Override
		public void run() {
			BufferedReader br = getBufferedReader(inputStream);
			String ligne = "";
			try {
				while ((ligne = br.readLine()) != null) {
					//System.out.println("StreamDisplay :" +ligne);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Class used to convert a stream into a string.
	 *
	 */
	class StreamStringConverter implements Runnable{

		// Stream converted to string.
		public String output = "";

		/** Stream to convert to string */
		protected final InputStream inputStream;
		/** Constructor */
		public StreamStringConverter(InputStream inputStream) {
			this.inputStream = inputStream;
		}
		/** Getter */
		protected BufferedReader getBufferedReader(InputStream is) {
			return new BufferedReader(new InputStreamReader(is));
		}

		/**
		 * When run, converts stream into a string using a buffer.
		 */
		@Override
		public void run() {
			BufferedReader br = getBufferedReader(inputStream);
			String ligne = "";
			try {
				while ((ligne = br.readLine()) != null) {
					//System.out.println("plop :"+ ligne); //TODO
					output = output.concat(ligne);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/** This class converts received stream into traces. The number of traces to
	 * create and their length depends on the attributes of SysCallFilter. Same
	 * thing for possibly ignored syscalls. Once enough traces have been created,
	 * the process is stopped. 
	 *
	 */
	class StreamToTrace implements Runnable{
		/** List of created traces */
		public ArrayList<Trace> traces;
		State can=new State(); //to send traces to server
		/** Stream to convert to trace */
		protected final InputStream inputStream;
		/** Constructor */
		public StreamToTrace(InputStream inputStream) {
			this.inputStream = inputStream;
			traces = new ArrayList<Trace>();
		}

		/** Getter */
		protected BufferedReader getBufferedReader(InputStream is) {
			return new BufferedReader(new InputStreamReader(is));
		}
		/**
		 * Runs thread as described in class comment.
		 */
		@Override
		public void run(){
			/**************************input: strace*******************************
			BufferedReader br = getBufferedReader(inputStream);
			**************************************************************/
			/**************************input: file********************************/
			int nbr=1;
			//nbr =0 model of 200 traces
			String fichierV=null;
			String fichierT;
			String fichierA;
			String fichierN;
			if (MainActivity.toscan) {
				scanning=true;
				System.out.println("/***********************scanning****************************/");
			
				/*fichierA ="/mnt/sdcard/Traces/"+ name+"TestAbnormal"+nbrtrace+".data"; // A vs T
				traces=getTraces(fichierA);*/
				
				fichierN ="/mnt/sdcard/Traces/"+ name+"TestNormal"+nbrtrace+".data"; // v vs T
				traces=getTraces(fichierN);
				
				//scan("/mnt/sdcard/Traces/"+name+".txt");

				/***********************************************************************/
				/*fichier ="/mnt/sdcard/Traces/"+name+"Inf.txt";
				fichierV ="/mnt/sdcard/Traces/StreamToTrace/"+ name+"V"+nbr+ ".txt";*/
					
			/*	fichierA ="/mnt/sdcard/Traces/"+ name+".txt";// V vs T
				scanningtraces(fichierA, fichierA, nbr);*/
				//fichierA ="/mnt/sdcard/Traces/"+name+"Inf.txt";
			//traces=getTraces(fichierA,"Normal", nbrtrace);
			//traces=getTraces(fichierV, "Validation", nbr);
			
			
			//scanningtracesDR(fichierA);
			}else {
				scanning=false;
				System.out.println("/***********************Modeling****************************/");
				//fichier ="/mnt/sdcard/Traces/"+name+".txt";
				fichierT ="/mnt/sdcard/Traces/StreamToTrace/"+ name+"T"+lentrace+".txt";
				//fichierT ="/mnt/sdcard/Traces/"+ name+ "Inf.txt";
			//	traces=getTraces(fichierA,"Anomaly", 1);
				//traces=getTraces(fichierT);
				scanningtraces(fichierT, fichierT, nbr);
				//modelingtraces(fichierT, nbr);

			}
			System.out.println("Trace count: " + traces.size());
//WriteTraces(traces, nbrtrace);
		
}
		private void scan(String fichierA) {
			/*********************************************************************/
			String line;
			MyHttpInsertTrace http = new MyHttpInsertTrace(name, "");
			int ntraces=0;
			/*********************************************************************/
			
			Syscall syscalls; 
			/**fichierSys contains ID number for each syscall , it is updating every scan/model creating**/
			File fichierSys = new File("/mnt/sdcard/Models/"+"Syscalls.data");
			if (! fichierSys.exists())
			{
				try {
					fichierSys.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				 syscalls =new Syscall(true);
				insertSyscalls(syscalls);
			}else {
				syscalls =getSyscalls();
			}
			/**syscall (syscall, freq)**/
			Hashtable <String, Integer> syscall= new  Hashtable <String, Integer>();//Alphabet
			Hashtable <String, Hashtable<Integer, Integer>> process= new  Hashtable <String, Hashtable<Integer, Integer>>();//process
			/**syscallID (ID, freq)**/
			Hashtable <Integer, Integer> syscallID= new  Hashtable <Integer, Integer>();//Alphabet ID
			
			/****************************************************************************************************/
		
			 line = "";
			 int n=1;
			try {
				InputStream ips = new FileInputStream(fichierA);
	            InputStreamReader ipsr = new InputStreamReader(ips);
	            BufferedReader br = new BufferedReader(ipsr);
				while ((line = br.readLine()) != null && keepGoing) {// &&ntraces>0  max 899
				String[] split1 = line.split("\\s+");//by space
				String p=split1[0];
				p.trim();
				
				
						
					line = extractSysCall(line);
					if(sysCallFilter.isMonitored(line)){
						System.out.println("Adding trace line: " + line);
						
						if (syscall.containsKey(line)){
							int freq= syscall.get(line);
							syscallID.put(syscalls.GetSyscalls(line), freq+1);
							syscall.put(line, freq+1);
						}
						else{
							syscall.put(line,1);
							syscallID.put(syscalls.GetSyscalls(line), 1);
						}
						/*************************/
						if (process.containsKey(p)){
							Hashtable<Integer, Integer> ss= process.get(p);
							if (ss.containsKey(syscalls.GetSyscalls(line))){
								int fr=ss.get(syscalls.GetSyscalls(line));
								ss.put(syscalls.GetSyscalls(line), fr++);
							}
							else{
								ss.put(syscalls.GetSyscalls(line),1);							
								}
							process.put(p,ss);

					}
						else{
							Hashtable<Integer, Integer> ss2 =new Hashtable<Integer, Integer>();
							ss2.put(syscalls.GetSyscalls(line), 1);
							process.put(p,ss2);
						}
						/*******************************/
						ntraces++;
					
						
						System.out.println("currentTrace size: " + ntraces);
						if (ntraces==100000) {
							Hashtable <Integer, Integer> syID= new  Hashtable <Integer, Integer>();
							Hashtable <String, Hashtable<Integer, Integer>>process2= new  Hashtable <String, Hashtable<Integer, Integer>>();//process
						syID=syscallID;
						process2=process;
							saveH(process2,100, "process");							
							saveID(syID,100, "syscallID");
						}
						if (ntraces==500000) {
							Hashtable <String, Hashtable<Integer, Integer>> process5= new Hashtable <String, Hashtable<Integer, Integer>>();//process
						process5=process;
						Hashtable <Integer, Integer> syID5= new  Hashtable <Integer, Integer>();
						syID5=syscallID;

							saveH(process5,500, "process");	
							saveID(syID5,500, "syscallID");
						}
					}

				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			saveH(process,process.size(), "processT");
			save(syscall,ntraces, "syscall");
			saveID(syscallID,ntraces, "syscallID");
			String fichierN = "/mnt/sdcard/Traces/"+ name+"TestNormal"+nbrtrace+".data"; // v vs T
			traces=getTraces(fichierN);
		}
						private void saveH(
				Hashtable<String, Hashtable<Integer, Integer>> traces,
				int nbr, String type) {
			// TODO Auto-generated method stub
							try
							{
							    FileWriter fw = new FileWriter("/mnt/sdcard/Traces/StreamToTrace/Trace_"+name+"_"+type+nbr+".csv");
							    PrintWriter writer = new PrintWriter(fw);

						 //line 1
								 for ( String key : traces.keySet()) {
								 writer.print(key); writer.print(";"); writer.println(traces.get(key).size());
								 }
								 writer.print(type);  writer.print(";");  writer.println(nbr+"-traces"); 

						
							    writer.flush();
							        
							    //Close the Print Writer
							    writer.close();
							        
							    //Close the File Writer
							    fw.close();     
							}
							catch(IOException e)
							{
							     e.printStackTrace();
							} 
					}
		

						/********************************************************/
		public void saveID(Hashtable <Integer, Integer> traces, int nbr, String type) { 
			try
				{
				    FileWriter fw = new FileWriter("/mnt/sdcard/Traces/StreamToTrace/Trace_"+name+"_"+type+nbr+".csv");
				    PrintWriter writer = new PrintWriter(fw);

			 //line 1
					 for ( Integer key : traces.keySet()) {
					 writer.print(key); writer.print(";"); writer.println(traces.get(key));
					 }
					 writer.print(type);  writer.print(";");  writer.println(nbr+"-traces"); 

			
				    writer.flush();
				        
				    //Close the Print Writer
				    writer.close();
				        
				    //Close the File Writer
				    fw.close();     
				}
				catch(IOException e)
				{
				     e.printStackTrace();
				} 
		}
		public void save(Hashtable <String, Integer> traces, int nbr, String type) { 
			try
				{
				    FileWriter fw = new FileWriter("/mnt/sdcard/Traces/StreamToTrace/Trace_"+name+"_"+type+nbr+".csv");
				    PrintWriter writer = new PrintWriter(fw);

			 //line 1
					 for ( String key : traces.keySet()) {
					 writer.print(key); writer.print(";"); writer.println(traces.get(key));
					 }
					 writer.print(type);  writer.print(";");  writer.println(nbr+"-traces"); 

			
				    writer.flush();
				        
				    //Close the Print Writer
				    writer.close();
				        
				    //Close the File Writer
				    fw.close();     
				}
				catch(IOException e)
				{
				     e.printStackTrace();
				} 
		}

		private void scanningtracesDR(String fichierA) {
			// TODO Auto-generated method stub
			//String fichier ="/mnt/sdcard/Traces/"+name+".txt";
			/*********************************************************************/
			String line;
			MyHttpInsertTrace http = new MyHttpInsertTrace(name, "");
			Trace currentTrace = new Trace();
			int ntraces=nbrtrace;
			/*********************************************************************/
			
			//File fichierst=create ("/mnt/sdcard/Traces/StreamToTrace/", name+".csv");
			Syscall syscalls; 
			/**fichierSys contains ID number for each syscall , it is updating every scan/model creating**/
			File fichierSys = new File("/mnt/sdcard/Models/"+"Syscalls.data");
			if (! fichierSys.exists())
			{
				try {
					fichierSys.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				 syscalls =new Syscall(true);
				insertSyscalls(syscalls);
			}else {
				syscalls =getSyscalls();
			}
			/**syscall (syscall, freq)**/
			Hashtable <String, Integer> syscall= new  Hashtable <String, Integer>();//Alphabet
			/**syscallID (ID, freq)**/
			Hashtable <Integer, Integer> syscallID= new  Hashtable <Integer, Integer>();//Alphabet ID
			
			/****************************************************************************************************/
			/********************************Anomaly**************************************************************/
				/*******************************************************************************************************/
			 line = "";
			try {
				InputStream ips = new FileInputStream(fichierA);
	            InputStreamReader ipsr = new InputStreamReader(ips);
	            BufferedReader br = new BufferedReader(ipsr);
				while ((line = br.readLine()) != null && keepGoing &&ntraces>0) {//ntrace=100
					line = extractSysCall(line);
				
					if(sysCallFilter.isMonitored(line)){
						System.out.println("Adding trace line: " + line);

						if (syscall.containsKey(line)){
							int freq= syscall.get(line);
							syscallID.put(syscalls.GetSyscalls(line), freq+1);
							syscall.put(line, freq+1);
						}
						else{
							syscall.put(line,1);
							syscallID.put(syscalls.GetSyscalls(line), 1);
						}
					
						currentTrace.addID(syscalls.GetSyscalls(line));
						currentTrace.add(line);
						System.out.println("currentTrace size: " + currentTrace.size());
						
						
						if(currentTrace.size() >= lentrace){//1250
							System.out.println("Adding trace to list: " + currentTrace);
							currentTrace.setSyscall(syscall);
							currentTrace.setSyscallID(syscallID);
							traces.add(currentTrace);
							System.out.println("traces size: " + currentTrace.size());
							
							syscall= new  Hashtable <String, Integer>();
							syscallID= new  Hashtable <Integer, Integer>();
							currentTrace = new Trace();
							System.out.println("Trace count: " + traces.size());
							ntraces--;
						}
					}

				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			//saveTraces(traces,"Anomaly",nbr );

		}

		private ArrayList<Trace> getTraces(String fichierA){//, String vTm,	int nbr) {
			String reqReturn =null ;
			//= http.getRequestReturn();
			/*********************************************************************/
			if(!new File("/mnt/sdcard/Models").exists())
	        {
	            return null;
	        }
			/**********************************************************/
			//File fichier = new File("/mnt/sdcard/Traces/StreamToTrace/"+ name+nbrtrace+"T"+nbr+".txt");//"/mnt/sdcard/Traces/", name	+ "Test"+vTm+nbr+".data");
			File fichier=new File(fichierA);
			ObjectInputStream ois;
			
			if (! fichier.exists())
				
			{		System.out.println("fichier1 "+fichier.getName());
				return null;
			}else {
				System.out.println("fichier "+fichier.getName());
			}
			/***********************************************************************/
			try {
				ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(fichier)));
				 reqReturn = (String) ois.readObject();
				ois.close();

				/*******************************/
			} //*/
	 catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Type listoftraces = new TypeToken<ArrayList<Trace>>() {}.getType();
			//Type listOfTestObject = new TypeToken<List<TestObject>>(){}.getType();
			ArrayList<Trace> tracesS = null;
			if(reqReturn != null){
				Gson gson = new Gson();
				try {
					tracesS = (ArrayList<Trace>) gson.fromJson(reqReturn, listoftraces);
				} catch (JsonSyntaxException e) {
					e.printStackTrace();
				}
			}else{
				tracesS = null;
				System.out.println("null");
			}
			
			
			return tracesS;	
		}

		private void modelingtraces(String fichier, int nbr) {
			String data="";
			String line = "";
			MyHttpInsertTrace http = new MyHttpInsertTrace(name, "");
			Trace currentTrace = new Trace();
			int ntraces=nbrtrace;
			Syscall syscalls; 
			File fichierSys = new File("/mnt/sdcard/Models/"+"Syscalls.data");
			if (! fichierSys.exists())
			{
				try {
					fichierSys.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}

				 syscalls =new Syscall(true);
				insertSyscalls(syscalls);
			}else {
				syscalls =getSyscalls();
			}
			
			Hashtable <String, Integer> syscall= new  Hashtable <String, Integer>();//Alphabet
		
			Hashtable <Integer, Integer> syscallID= new  Hashtable <Integer, Integer>();//Alphabet ID
			
			try {
				InputStream ips = new FileInputStream(fichier);
	            InputStreamReader ipsr = new InputStreamReader(ips);
	            BufferedReader br = new BufferedReader(ipsr);
				while ((line = br.readLine()) != null && keepGoing) {// &&ntraces>0
						System.out.println("Adding trace line: " + line);

						if (syscall.containsKey(line)){
							int freq= syscall.get(line);
							syscallID.put(syscalls.GetSyscalls(line), freq+1);
							syscall.put(line, freq+1);
						}
						else{
							syscall.put(line,1);
							syscallID.put(syscalls.GetSyscalls(line), 1);
						}
					
						currentTrace.addID(syscalls.GetSyscalls(line));
						currentTrace.add(line);
						System.out.println("currentTrace size: " + currentTrace.size());
						
						if(currentTrace.size() >= 1000){
							System.out.println("Adding trace to list: " + currentTrace);
							currentTrace.setSyscall(syscall);
							currentTrace.setSyscallID(syscallID);
							traces.add(currentTrace);
							System.out.println("traces size: " + currentTrace.size());
							
							syscall= new  Hashtable <String, Integer>();
							syscallID= new  Hashtable <Integer, Integer>();
							currentTrace = new Trace();
							System.out.println("Trace count: " + traces.size());
							ntraces--;
						}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			//saveTraces(traces,"Modeling",nbr );
		}

		private void scanningtraces(String fichierV, String fichierA, int nbr) {
			//String fichier ="/mnt/sdcard/Traces/"+name+".txt";
			/*********************************************************************/
			String line;
			MyHttpInsertTrace http = new MyHttpInsertTrace(name, "");
			Trace currentTrace = new Trace();
			int ntraces=500;//nbrtrace;
			/*********************************************************************/
			
			//File fichierst=create ("/mnt/sdcard/Traces/StreamToTrace/", name+".csv");
			Syscall syscalls; 
			/**fichierSys contains ID number for each syscall , it is updating every scan/model creating**/
			File fichierSys = new File("/mnt/sdcard/Models/"+"Syscalls.data");
			if (! fichierSys.exists())
			{
				try {
					fichierSys.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				 syscalls =new Syscall(true);
				insertSyscalls(syscalls);
			}else {
				syscalls =getSyscalls();
			}
			/**syscall (syscall, freq)**/
			Hashtable <String, Integer> syscall= new  Hashtable <String, Integer>();//Alphabet
			/**syscallID (ID, freq)**/
			Hashtable <Integer, Integer> syscallID= new  Hashtable <Integer, Integer>();//Alphabet ID
			
			/****************************************************************************************************/
			/********************************Anomaly**************************************************************/
				/*******************************************************************************************************
			 line = "";
			 int n=1;
			try {
				InputStream ips = new FileInputStream(fichierA);
	            InputStreamReader ipsr = new InputStreamReader(ips);
	            BufferedReader br = new BufferedReader(ipsr);
				while ((line = br.readLine()) != null && keepGoing&&ntraces>0) {// &&ntraces>0  max 899
					line = extractSysCall(line);
				
					if(sysCallFilter.isMonitored(line)){
						System.out.println("Adding trace line: " + line);

						if (syscall.containsKey(line)){
							int freq= syscall.get(line);
							syscallID.put(syscalls.GetSyscalls(line), freq+1);
							syscall.put(line, freq+1);
						}
						else{
							syscall.put(line,1);
							syscallID.put(syscalls.GetSyscalls(line), 1);
						}
					
						currentTrace.addID(syscalls.GetSyscalls(line));
						currentTrace.add(line);
						System.out.println("currentTrace size: " + currentTrace.size());
						
						
						if(currentTrace.size() >= 1000){
							System.out.println("Adding trace to list: " + currentTrace);
							currentTrace.setSyscall(syscall);
							currentTrace.setSyscallID(syscallID);
							traces.add(currentTrace);
							System.out.println("traces size: " + currentTrace.size());
							
							syscall= new  Hashtable <String, Integer>();
							syscallID= new  Hashtable <Integer, Integer>();
							currentTrace = new Trace();
							System.out.println("Trace count: " + traces.size());
							ntraces--;
						}/*
					/*	if (traces.size()==100) {
							ArrayList<Trace> loc = traces;
							saveTraces(loc,"Abnormal",100);	
						}*/
					/*	if (traces.size()==200) {
							ArrayList<Trace> loc = traces;
							saveTraces(loc,"Abnormal",200);	
						}
						if (traces.size()==300) {
							ArrayList<Trace> loc3 = traces;

							saveTraces(loc3,"Abnormal",300);	
						}
						if (traces.size()==400) {
							ArrayList<Trace> loc4 = traces;

							saveTraces(loc4,"Abnormal",400);	
						*/
						/*if (traces.size()==500) {//493 for candy. 330 ninja
							ArrayList<Trace> loc5 = traces;
							saveTraces(loc5,"Abnormal",500);	
						}*/
					/*}

				}
				ArrayList<Trace> locf = traces;
				saveTraces(locf,"Nnormal",locf.size());


			} catch (IOException e) {
				e.printStackTrace();
			}*/
			
			/****************************************************************************************************/
		/********************************Validation**************************************************************/
			/******************************************************************************************************/
			
			 line = "";
			
			try {
				InputStream ips = new FileInputStream(fichierV);
	            InputStreamReader ipsr = new InputStreamReader(ips);
	            BufferedReader br = new BufferedReader(ipsr);
				while ((line = br.readLine()) != null) {

						System.out.println("Adding trace line: " + line);

						if (syscall.containsKey(line)){
							int freq= syscall.get(line);
							syscallID.put(syscalls.GetSyscalls(line), freq+1);
							syscall.put(line, freq+1);
						}
						else{
							syscall.put(line,1);
							syscallID.put(syscalls.GetSyscalls(line), 1);
						}
					
						currentTrace.addID(syscalls.GetSyscalls(line));
						currentTrace.add(line);
						System.out.println("currentTrace size: " + currentTrace.size());
						
						if(currentTrace.size() >= 1000){
							System.out.println("Adding trace to list: " + currentTrace);
							currentTrace.setSyscall(syscall);
							currentTrace.setSyscallID(syscallID);
							traces.add(currentTrace);
							System.out.println("traces size: " + currentTrace.size());
							
							syscall= new  Hashtable <String, Integer>();
							syscallID= new  Hashtable <Integer, Integer>();
							currentTrace = new Trace();
							System.out.println("Trace count: " + traces.size());
							ntraces--;
						}
						/*if (traces.size()==100) {
							ArrayList<Trace> loc = traces;
							saveTraces(loc,"Normal",100);	
						}
						if (traces.size()==500) {
							ArrayList<Trace> loc5 = traces;
							saveTraces(loc5,"Normal",500);	
						}*/
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			//saveTraces(traces, "Validation", nbr);
				/********************************************************/
		}


		private void saveTraces(ArrayList<Trace> traces, String vT, int nbr) {
			// TODO Auto-generated method stub
			Gson gson = new Gson();
			String json = gson.toJson(traces);
			ObjectOutputStream oos;
			File fichierst = create("/mnt/sdcard/Traces/", name	+ "Test"+vT+nbr+".data");
			 
			if (fichierst.exists())
			{
				try {
					FileWriter writer = new FileWriter(fichierst,false); 
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else 	{
				try {
					fichierst.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			try {
				oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(fichierst)));
				oos.writeObject(json);
				oos.close();
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void WriteTraces(ArrayList<Trace> traces2, int sz) {// traces2 are normals, sz --> size normal (200,300,400,500)
			// TODO Auto-generated method stub
		
			Double sptP=((double)(7.0*(double)sz)*10.0);//7%
			Double trP=((double)(30.0*(double)sz)*10.0);//30%
			System.out.println("trP "+trP);
			Double S=0.0;
			Double E=trP;
			int n=1;
			Trace temp=new Trace();
			for (int i = 0; i < traces2.size(); i++) {
				for (int j = 0; j < traces2.get(i).size(); j++) {
					temp.add(traces2.get(i).get(j));					
				}				
				}
			
			
				while (E.intValue()< temp.size()) {		
					System.out.println("E "+E.intValue()+ "\t temp.size()  "+temp.size());


					FileWriter writerst = null;	
					File fichierst = create("/mnt/sdcard/Traces/StreamToTrace/", name+sz+"V"+n+ ".txt");
					Gson gson = new Gson();
					/********************/
					FileWriter writerst2 = null;	
					File fichierst2 = create("/mnt/sdcard/Traces/StreamToTrace/", name+sz+"T"+n+ ".txt");
					Gson gson2 = new Gson();
					/*******************/
					try {
						writerst = new FileWriter(fichierst, true);
						writerst2 = new FileWriter(fichierst2, true);
						/***************test 70%*****************/
						for (int j = 0; j <S.intValue(); j++) {
							writerst2.write(gson2.toJson(temp.get(j))); // traces treated 
							}
						/****************validation 30%**************************/
					for (int j = S.intValue(); j <E.intValue(); j++) {
					
					writerst.write(gson.toJson(temp.get(j))); // traces treated 
					}
					/***************test 70%*****************/
					for (int j = E.intValue()-1; j <temp.size(); j++) {
						writerst2.write(gson2.toJson(temp.get(j))); // traces treated 
						}					
					/********************************************************/
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}finally{

						
						  if(writerst != null){
							     try {
									writerst.close();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								
							  }
						}
				}
				n++;
				S=E-sptP; //E-sepP
				E=S+trP;
					
					}
				////////end while/////////////
			}	
		
	}

	

		private void SendToServer(MyHttpInsertTrace http, String line, String inter) {
			// TODO Auto-generated method stub
			SetActive(inter); //interface d4envoie
			String json;
			Gson gson = new Gson();
			json = gson.toJson(line);
			http.setTrace(json);
			http.run();
		}

		private void SetActive(String inter) {
			// TODO Auto-generated method stub
			
		}

		private File create(String path, String name) {
			// TODO Auto-generated method stub
			/*********************************************************************/
			if(!new File(path).exists())
	        {
	            // Créer le dossier avec tous ses parents
	            new File(path).mkdirs();
	        }
			/**********************************************************/
			
			 File fichier = new File(path+name);
			//ObjectOutputStream oos;
			
			if (! fichier.exists())
			{
				try {
					fichier.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return fichier;
		}

		/**
		 * Extracts the syscall name from a line of strace return.
		 * @param line: line to be processed
		 * @return: extracted syscall.
		 */
		public String extractSysCall(String line){
			String call="";
		//System.out.println("line : "+line);
			if(line.contains("]")){
			String[] split1 = line.split("]");
			call=split1[1];
		//	System.out.println("split1 : "+call);
			}else {
				String[] split1 = line.split("\\s+");//by space
				call=split1[1];
			}
			
			if(call.contains("(")){
			String[] split2 = call.split("\\(");
			call=split2[0];
			
		//	System.out.println("split2 : "+split2[0].trim());
			}
			if (call.trim().startsWith("<")){
				call="";}
			if (call.contains("NULL")){
				call="";}
			if ((call.trim().contains("-"))||(call.trim().contains("+"))||(call.trim().contains(","))|| (call.trim().contains("?"))||(call.trim().contains("="))|| (call.trim().contains(")"))|| (call.trim().contains("0"))){
				call="";}
		
			return call.trim();

		}

	
	public void insertSyscalls(Syscall sys){
		
		Gson gson = new Gson();
		String json = gson.toJson(sys);		
		/*********************************************************************/
		if(!new File("/mnt/sdcard/Models").exists())
        {
            // Créer le dossier avec tous ses parents
            new File("/mnt/sdcard/Models").mkdirs();
 
        }
		/**********************************************************/

		File getSys = new File("/mnt/sdcard/Models/"+"Syscalls.data");
		ObjectOutputStream oos;
		
		try {
			/*******************************/
			oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(getSys)));
			oos.writeObject(json);
			oos.close();
			/*******************************/
		} //*/
 catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}




	public void init(Trace HoleTrace, Hashtable<Integer, Integer> syscallID) {
		// TODO Auto-generated method stub
		//traces.add(HoleTrace);
		//HoleTrace.setSyscall(syscall);//single syscalls as strings with their frequencies
		HoleTrace.setSyscallID(syscallID);//single syscalls as integers with their frequencies

		Hashtable <String, Integer> syscallID2= new  Hashtable <String, Integer>();//2-grams
		Hashtable <String, Integer> syscallID3= new  Hashtable <String, Integer>();
		Hashtable <String, Integer> syscallID4= new  Hashtable <String, Integer>();
		Hashtable <String, Integer> syscallID5= new  Hashtable <String, Integer>();
		Hashtable <String, Integer> syscallID6= new  Hashtable <String, Integer>();//6-grams
		
		for (int n=0; n<HoleTrace.sequenceID.size()-1; n++ ){//sequence of ID from hole trace (as it was generated)
			
			/***************************2-grams*******************************************/
			String Ngram2=HoleTrace.sequenceID.get(n)+"-"+HoleTrace.sequenceID.get(n+1);
			if (syscallID2.containsKey(Ngram2)){//2-grams
				int freq= syscallID2.get(Ngram2);
				syscallID2.put(Ngram2, freq+1);//syscalls (2-grams), freq
			}
			else{
				syscallID2.put(Ngram2, 1);
			}
			
			/***************************3-grams*******************************************/
			if(n<HoleTrace.sequenceID.size()-2){		
			String Ngram3=HoleTrace.sequenceID.get(n)+"-"+HoleTrace.sequenceID.get(n+1)+"-"+HoleTrace.sequenceID.get(n+2);
			if (syscallID3.containsKey(Ngram3)){//3-grams
				int freq= syscallID3.get(Ngram3);
				syscallID3.put(Ngram3, freq+1);//syscalls (3-grams), freq
			}
			else{
				syscallID3.put(Ngram3, 1);
			}}
			
			/***************************4-grams*******************************************/
			if(n<HoleTrace.sequenceID.size()-3){		
			String Ngram4=HoleTrace.sequenceID.get(n)+"-"+HoleTrace.sequenceID.get(n+1)+"-"+HoleTrace.sequenceID.get(n+2)+"-"+HoleTrace.sequenceID.get(n+3);
			if (syscallID4.containsKey(Ngram4)){//3-grams
				int freq= syscallID4.get(Ngram4);
				syscallID4.put(Ngram4, freq+1);//syscalls (3-grams), freq
			}
			else{
				syscallID4.put(Ngram4, 1);
			}}
			
			/***************************5-grams*******************************************/
			if(n<HoleTrace.sequenceID.size()-4){		
			String Ngram5=HoleTrace.sequenceID.get(n)+"-"+HoleTrace.sequenceID.get(n+1)+"-"+HoleTrace.sequenceID.get(n+2)+"-"+HoleTrace.sequenceID.get(n+3)+"-"+HoleTrace.sequenceID.get(n+4);
			if (syscallID5.containsKey(Ngram5)){//3-grams
				int freq= syscallID5.get(Ngram5);
				syscallID5.put(Ngram5, freq+1);//syscalls (3-grams), freq
			}
			else{
				syscallID5.put(Ngram5, 1);
			}}
			
			/***************************6-grams*******************************************/
			if(n<HoleTrace.sequenceID.size()-5){		
			String Ngram6=HoleTrace.sequenceID.get(n)+"-"+HoleTrace.sequenceID.get(n+1)+"-"+HoleTrace.sequenceID.get(n+2)+"-"+HoleTrace.sequenceID.get(n+3)+"-"+HoleTrace.sequenceID.get(n+4)+"-"+HoleTrace.sequenceID.get(n+5);
			if (syscallID6.containsKey(Ngram6)){//3-grams
				int freq= syscallID6.get(Ngram6);
				syscallID6.put(Ngram6, freq+1);//syscalls (3-grams), freq
			}
			else{
				syscallID6.put(Ngram6, 1);
			}}
			
			/***************************end******************************************/
		}
		HoleTrace.setSyscallID2(syscallID2);
		HoleTrace.setSyscallID3(syscallID3);
		HoleTrace.setSyscallID4(syscallID4);
		HoleTrace.setSyscallID5(syscallID5);
		HoleTrace.setSyscallID6(syscallID6);
		
	}

	public Syscall getSyscalls() {
		Syscall syscalls=new Syscall();
		File getSys = new File("/mnt/sdcard/Models/"+"Syscalls.data");
		ObjectInputStream ois;
		/***********************************************************************/
		String reqReturn=null;
		try {
			//thread.join();
			/*******************************/
			ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(getSys)));
		//	reqReturn = (Livre)ois.read;
			 reqReturn = (String) ois.readObject();
			ois.close();

			/*******************************/
		} //*/
 catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		syscalls = null;
		if(reqReturn != null){
			Gson gson = new Gson();
			try {
				syscalls = (Syscall) gson.fromJson(reqReturn, Syscall.class);
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
			} 
		}else{
			syscalls = null;
		}
		return syscalls;
	}
	
	public void insertcsv(Hashtable<?, ?>[][] tab2, String name2, int ngramLenght) {
		// TODO Auto-generated method stub
				try
				{
				    FileWriter fw = new FileWriter("/mnt/sdcard/Traces/StreamToTrace/Trace_"+name+"_"+ngramLenght+".csv");
				    PrintWriter writer = new PrintWriter(fw);

			 //line 1
				    writer.print(name);  writer.print(";");  writer.println(ngramLenght+"-grams"); 
				  
			//line 2
				    writer.print("Total");
				    writer.print(";"); writer.print(Integer.toString(tab2[ngramLenght-1][0].size()));//known
				    writer.print(";"); writer.print(" ");
				    writer.print(";"); writer.println(Integer.toString(tab2[ngramLenght-1][1].size()));//unkown
				    

				    
				    
			// line 3
				    writer.print(" ");  writer.print(";");  writer.print("N-grams"); writer.print(";");  writer.println("frequency"); 

			//line 4	    	    

				   writer.println("Known N-grams");
				   writer.print(tab2[ngramLenght-1][0].toString().replace("}", "").replace("{", "").replace(",", "\n").replace("=", ";"));
				    	
				   writer.print("\n");writer.print("\n");writer.print("\n");
				   
				   writer.println("New n-grams");
				   writer.print(tab2[ngramLenght-1][1].toString().replace("}", "").replace("{", "").replace(",", "\n").replace("=", ";"));
				  
				    writer.flush();
				        
				    //Close the Print Writer
				    writer.close();
				        
				    //Close the File Writer
				    fw.close();     
				}
				catch(IOException e)
				{
				     e.printStackTrace();
				} 
	}
	private void insertHoleTrace(Trace holeTrace, String name) {
		// TODO Auto-generated method stub
		
		Gson gson = new Gson();
		String json = gson.toJson(holeTrace);		
		if(!new File("/mnt/sdcard/Traces/StreamToTrace/").exists())
        {
            // Créer le dossier avec tous ses parents
            new File("/mnt/sdcard/Traces/StreamToTrace/").mkdirs();
 
        }

		File traces = new File("/mnt/sdcard/Traces/StreamToTrace/", name+".data");
		ObjectOutputStream oos;
		
		try {
			oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(traces)));
			oos.writeObject(json);
			oos.close();
		} 
 catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Trace getHoleTrace(String name) {
		Trace holeTrace=new Trace();
		File traces = new File("/mnt/sdcard/Traces/StreamToTrace/", name+".data");
		ObjectInputStream ois;
		/***********************************************************************/
		String reqReturn=null;
		try {
			//thread.join();
			/*******************************/
			ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(traces)));
		//	reqReturn = (Livre)ois.read;
			 reqReturn = (String) ois.readObject();
			ois.close();

			/*******************************/
		} //*/
 catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		holeTrace = null;
		if(reqReturn != null){
			Gson gson = new Gson();
			try {
				holeTrace = (Trace) gson.fromJson(reqReturn, Trace.class);
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
			} 
		}else{
			holeTrace = null;
		}
		return holeTrace;
	}
	
	private  void insertHoleTracecsv( Trace holeTrace, String name)
	   {
		try
		{
		    FileWriter fw = new FileWriter("/mnt/sdcard/Traces/StreamToTrace/Trace_"+name+".csv");
		    PrintWriter writer = new PrintWriter(fw);

	 //line 1
		    writer.print(name); writer.print(";"); writer.print("Trace size ;"); writer.println(Integer.toString(holeTrace.size()));//Trace size
		  
	//line 2
		    writer.print("Total"); writer.print(";"); writer.print(Integer.toString(holeTrace.syscallID.size()));//Alphabet size (1-gram)
		    writer.print(";"); writer.print(" ");
		    writer.print(";");writer.print(" ");
		    writer.print(";");writer.print(Integer.toString(holeTrace.syscallID2.size()));//2-grams
		    
		    writer.print(";"); writer.print(" ");writer.print(";");writer.print("");writer.print(";");writer.print(Integer.toString(holeTrace.syscallID3.size()));//3-grams
		    writer.print(";"); writer.print(" ");writer.print(";");writer.print("");writer.print(";");writer.print(Integer.toString(holeTrace.syscallID4.size()));
		    writer.print(";"); writer.print(" ");writer.print(";");writer.print("");writer.print(";");writer.print(Integer.toString(holeTrace.syscallID5.size()));
		    writer.print(";"); writer.print(" ");writer.print(";");writer.print("");writer.print(";");writer.println(Integer.toString(holeTrace.syscallID6.size()));
		    
		    
	// line 3
		    writer.print(name); writer.print(";");  writer.print("Alphabets"); writer.print(";");  writer.print("frequency");    writer.print(";"); //cl 1+2+3	    	    
		    writer.print(";");  writer.print("2-grams"); writer.print(";");  writer.print("frequency"); writer.print(";");//cl 4+5+6
		    writer.print(";");  writer.print("3-grams");writer.print(";");  writer.print("frequency"); writer.print(";");//cl 7+8+9
		    writer.print(";");  writer.print("4-grams");writer.print(";");  writer.print("frequency"); writer.print(";");//cl 10+11+12
		    writer.print(";");  writer.print("5-grams");writer.print(";");  writer.print("frequency"); writer.print(";");//cl 13+14+15
		    writer.print(";");  writer.print("6-grams");writer.print(";");  writer.println("frequency"); //cl 16+17+18

	//line 4	    	    
		    
		    Enumeration e = holeTrace.syscallID.keys();
		    Enumeration e2 = holeTrace.syscallID2.keys();
		    Enumeration e3 = holeTrace.syscallID3.keys();
		    Enumeration e4 = holeTrace.syscallID4.keys();
		    Enumeration e5 = holeTrace.syscallID5.keys();
		    Enumeration e6 = holeTrace.syscallID6.keys();
int sys=0;
		    while (sys<holeTrace.sequenceID.size()) {
		    	writer.print(" "); writer.print(";");

		    	 if (e.hasMoreElements()) {	
		      int key =  (Integer) e.nextElement();
		      writer.print(key + " ; " + holeTrace.syscallID.get(key));//1-gram
		      writer.print(";"); writer.print(" ");writer.print(";");
		    	 }else {
		    		 writer.print(" "); writer.print(";");writer.print(" "); writer.print(";"); writer.print(" ");writer.print(";");
					}
		      
		      /*******************2-grams*************************/
		     if (e2.hasMoreElements()) {	
		      String key2=(String) e2.nextElement();
		      writer.print(" "+key2 );//pour ignorer la date
		      writer.print(";");
		      writer.print(holeTrace.syscallID2.get(key2));
		      writer.print(";");
		      writer.print(" ");
		      writer.print(";");
		    }else {
			      writer.print(" "); writer.print(";");writer.print(" "); writer.print(";"); writer.print(" ");writer.print(";");
			}
		     
		     /*******************3-grams*************************/
		     if (e3.hasMoreElements()) {	
		      String key3=(String) e3.nextElement();
		      writer.print(" "+key3 + " ; " + holeTrace.syscallID3.get(key3));
		      writer.print(";");
		      writer.print(" ");
		      writer.print(";");
		    }else {
			      writer.print(" "); writer.print(";");writer.print(" "); writer.print(";"); writer.print(" ");writer.print(";");
			}
		     
		     /******************4-grams*************************/
		     if (e4.hasMoreElements()) {	
		      String key4=(String) e4.nextElement();
		      writer.print(key4 + " ; " + holeTrace.syscallID4.get(key4));
		      writer.print(";");
		      writer.print(" ");
		      writer.print(";");
		    }else {
			      writer.print(" "); writer.print(";");writer.print(" "); writer.print(";"); writer.print(" ");writer.print(";");
			}
		     
		     /*******************5-grams*************************/
		     if (e5.hasMoreElements()) {	
		      String key5=(String) e5.nextElement();
		      writer.print(key5 + " ; " + holeTrace.syscallID5.get(key5));
		      writer.print(";");
		      writer.print(" ");
		      writer.print(";");
		    }else {
			      writer.print(" "); writer.print(";");writer.print(" "); writer.print(";"); writer.print(" ");writer.print(";");
			}
		     
		     /*******************6-grams*************************/
		     if (e6.hasMoreElements()) {	
		      String key6=(String) e6.nextElement();
		      writer.print(key6 + " ; ");
		      writer.println(holeTrace.syscallID6.get(key6));
		    }else {
			      writer.print(" "); writer.print(";");writer.print(" "); writer.print(";"); writer.print(" ");writer.println(";");
			}
		     
		     /*******************end*************************/
		     sys++;
		    }
		    
		
		    

		 
		    //writer.print(";"); writer.println(Integer.toString(holeTrace.getsizeAlp()));//Alphabet size
		   /** for (int i=0; i< holeTrace.syscallID.size()-1; i++)	{	
		    	 writer.print(holeTrace.syscallID.get(i));
		    }
		    writer.print(holeTrace.syscallID.toString().replace(":", ";").replace(",", "\n ;"));//holeTrace.syscallID.toString()--> alpha:freq, alph:freq ...
		    writer.print("\n"); writer.print("total"); writer.print(";"); writer.println(Integer.toString(holeTrace.getsizeAlp()));//Alphabet size**/
		    
	//line holeTrace.syscallID.size +1
	/**	    for (int i=0; i< holeTrace.sequence.size()-1; i++)	{	    	
		    writer.print(holeTrace.sequence.get(i));//trace sequence
		    writer.print(";");
			}
		    writer.println(holeTrace.sequence.get(holeTrace.sequence.size()-1));
		    
		    //third line 
		    
		    for (int i=0; i< holeTrace.sequenceID.size()-1; i++)	{	    	 
		    writer.print(Integer.toString(holeTrace.sequenceID.get(i)));//ID sequence
		    writer.print(";");
		    }
		    writer.println(Integer.toString(holeTrace.sequenceID.get(holeTrace.sequence.size()-1)));//ID sequence 
		    writer.print(holeTrace.syscallID.toString().replace(",", ";"));//ID sequence
		    **/
		  
			
		   //  writerst.write(gson2.toJson(HoleTrace.sequenceID)+"\n");
		    // writerst.write(gson2.toJson(HoleTrace.syscallID)+"\n");
		  //Flush the output to the file
		    writer.flush();
		        
		    //Close the Print Writer
		    writer.close();
		        
		    //Close the File Writer
		    fw.close();     
		}
		catch(IOException e)
		{
		     e.printStackTrace();
		} 
	    }
	public   Hashtable<String, String>[][] ReadCSV(String name){
		Trace currentTrace=new Trace();
		// @SuppressWarnings("unchecked")
		   
		 Hashtable<String, String>[][]  tab =  (Hashtable<String, String>[][]) new Hashtable<?,?>[6][2];
		 
		    
	 Hashtable<String, String> NTrace=new Hashtable<String, String>();
	 Hashtable<String, String> AbTrace=new  Hashtable<String, String>();
	 
	 Hashtable<String, String> NTrace2=new  Hashtable<String, String>();
	 Hashtable<String, String> AbTrace2=new  Hashtable<String, String>();
	 
	 Hashtable<String, String> NTrace3=new  Hashtable<String, String>();
	 Hashtable<String, String> AbTrace3=new  Hashtable<String, String>();
	 
	 Hashtable<String, String> NTrace4=new  Hashtable<String, String>();
	 Hashtable<String, String> AbTrace4=new  Hashtable<String, String>();
	 
	 Hashtable<String, String> NTrace5=new  Hashtable<String, String>();
	 Hashtable<String, String> AbTrace5=new Hashtable<String, String>();
	 
	 Hashtable<String, String> NTrace6=new Hashtable<String, String>();
	 Hashtable<String, String> AbTrace6=new  Hashtable<String, String>();
	 
		String csvFile = "/mnt/sdcard/Traces/StreamToTrace/Trace_"+name+".csv";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ";";
	 
		try {
	 
			br = new BufferedReader(new FileReader(csvFile));
			int i=0;
			
			while ((line = br.readLine()) != null) {
				i++;
				if (i>=4){
	 
			        // use comma as separator
				String[] syscall = line.split(cvsSplitBy);
				
				if (!syscall[1].isEmpty()&& !syscall[1].equals(" ")){	 
					System.out.println("!syscall[1].isEmpty()=="+syscall[1]);
					NTrace.put(syscall[1], (syscall[2]));
				}
				
					if (!syscall[3].isEmpty()&& !syscall[3].equals(" ")){	
						System.out.println("!syscall[3].isEmpty()=="+syscall[3]);
					AbTrace.put(syscall[3], (syscall[4]));
				}
				
				if (!syscall[6].isEmpty()&& !syscall[6].equals(" ")){	 					 
					System.out.println("!syscall[6].isEmpty()=="+syscall[6]);
					NTrace2.put(syscall[6],(syscall[7]));

				}
					
					if (!syscall[8].isEmpty()&& !syscall[8].equals(" ")){	
					System.out.println("!syscall[8].isEmpty()=="+syscall[8]);
					AbTrace2.put(syscall[8], (syscall[9]));

				}
				
			
				
				if (!syscall[11].isEmpty()&& !syscall[11].equals(" ")){	 	 
					System.out.println("!syscall[11].isEmpty()=="+syscall[11]);
					NTrace3.put(syscall[11], (syscall[12]));
				}
				if (!syscall[13].isEmpty()&& !syscall[13].equals(" ")){	 	 
					System.out.println("!syscall[13].isEmpty()=="+syscall[13]);
					AbTrace3.put(syscall[13], (syscall[14]));
				}
				
				if (!syscall[16].isEmpty()&& !syscall[16].equals(" ")){	 
					 
					System.out.println("!syscall[16].isEmpty()=="+syscall[16]);
					NTrace4.put(syscall[16], (syscall[17]));
				}
				if (syscall.length>19) {

				if (!syscall[18].isEmpty()&& !syscall[18].equals(" ")){	 
					System.out.println("!syscall[18].isEmpty()=="+syscall[18]);
					AbTrace4.put(syscall[18],(syscall[19]));
				}
				}
				if (syscall.length>22) {

				if (!syscall[21].isEmpty()&& !syscall[21].equals(" ")){	 
					System.out.println("!syscall[21].isEmpty()=="+syscall[21]);
					NTrace5.put(syscall[21], (syscall[22]));
				}}
				if (syscall.length>24) {

				if (!syscall[23].isEmpty()&& !syscall[23].equals(" ")){	 
					System.out.println("!syscall[23].isEmpty()=="+syscall[23]);
					AbTrace5.put(syscall[23], (syscall[24]));
				}}
				
				if (syscall.length>27) {

				if (!syscall[26].isEmpty()&& !syscall[26].equals(" ")){	
					System.out.println("!syscall[26].isEmpty()=="+syscall[26]);
					NTrace6.put(syscall[26],(syscall[27]));
				}
				}
				if (syscall.length>29) {
				if (!syscall[28].isEmpty()&& !syscall[28].equals(" ")){	
					System.out.println("!syscall[28].isEmpty()=="+syscall[28]);
					AbTrace6.put(syscall[28],(syscall[29]));
				}
				}
				
	 
			}
				}
	 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		tab[0][0] = NTrace;
		tab[0][1] = AbTrace;
		tab[1][0] = NTrace2;
		tab[1][1] = AbTrace2;
		tab[2][0] = NTrace3;
		tab[2][1] = AbTrace3;
		tab[3][0] = NTrace4;
		tab[3][1] = AbTrace4;
		tab[4][0] = NTrace5;
		tab[4][1] = AbTrace5;
		tab[5][0] = NTrace6;
		tab[5][1] = AbTrace6;
		 
	 
		System.out.println("Done");
		return tab;
	  }
	 
	
	public  Hashtable<String, String>[][] NewSeq(Hashtable<String, String>[][] tab) {
		  Hashtable<String, String>[][]  tab2 =  ( Hashtable<String, String>[][]) new Hashtable<?,?>[6][2];//kown + freq in abnormql , unkown 
		    
		 Hashtable<String, String> NTrace=new   Hashtable<String, String>();
		 Hashtable<String, String> AbTrace=new  Hashtable<String, String>();
	 
		 Hashtable<String, String> NTrace2=new  Hashtable<String, String>();
		 Hashtable<String, String> AbTrace2=new  Hashtable<String, String>();
	 
		 Hashtable<String, String> NTrace3=new  Hashtable<String, String>();
		 Hashtable<String, String> AbTrace3=new  Hashtable<String, String>();
	 
		 Hashtable<String, String> NTrace4=new  Hashtable<String, String>();
		 Hashtable<String, String> AbTrace4=new  Hashtable<String, String>();
	 
		 Hashtable<String, String> NTrace5=new  Hashtable<String, String>();
		 Hashtable<String, String> AbTrace5=new  Hashtable<String, String>();
	 
		 Hashtable<String, String>NTrace6=new  Hashtable<String, String>();
		 Hashtable<String, String> AbTrace6=new  Hashtable<String, String>();

	 
			    Enumeration e = tab[0][1].keys();
			    while (e.hasMoreElements()) {
			      String key = (String) e.nextElement();
			      
			      if (tab[0][0].containsKey(key)) {
			    	  NTrace.put(key,  tab[0][1].get(key));
				}else {
					AbTrace.put(key, tab[0][1].get(key));
			    }
			      
				}
				    Enumeration e2 = tab[1][1].keys();
				    while (e2.hasMoreElements()) {
				      String key2 = (String) e2.nextElement();
				      
				      if (tab[1][0].containsKey(key2)) {
				    	  NTrace2.put(key2,  tab[1][1].get(key2));
					}else {
						AbTrace2.put(key2,  tab[1][1].get(key2));
				    }
				      
					}
				      
				      
					    Enumeration e3 = tab[2][1].keys();
					    while (e3.hasMoreElements()) {
					      String key3 = (String) e3.nextElement();
					      
					      if (tab[2][0].containsKey(key3)) {
					    	  NTrace3.put(key3, tab[2][1].get(key3));
						}else {
							AbTrace3.put(key3, tab[2][1].get(key3));
					    }
					      
						}
					      
					      
						    Enumeration e4 = tab[3][1].keys();
						    while (e4.hasMoreElements()) {
						      String key4 = (String) e4.nextElement();
						      
						      if (tab[3][0].containsKey(key4)) {
						    	  NTrace4.put(key4,tab[3][1].get(key4));
							}else {
								AbTrace4.put(key4, tab[3][1].get(key4));
						    }
						      
						      
							}
						      
							    Enumeration e5 = tab[4][1].keys();
							    while (e5.hasMoreElements()) {
							      String key5 = (String) e5.nextElement();
							      
							      if (tab[4][0].containsKey(key5)) {
							    	  NTrace5.put(key5, tab[4][1].get(key5));
								}else {
									AbTrace5.put(key5, tab[4][1].get(key5));
							    }
							      
							      
								}
							      
								    Enumeration e6 = tab[5][1].keys();
								    while (e6.hasMoreElements()) {
								      String key6 = (String) e6.nextElement();
								      
								      if (tab[5][0].containsKey(key6)) {
								    	  NTrace6.put(key6,  tab[5][1].get(key6));
									}else {
										AbTrace6.put(key6,  tab[5][1].get(key6));
								    }
		}
									 
									tab2[0][0] = NTrace;
									tab2[0][1] = AbTrace;
									tab2[1][0] = NTrace2;
									tab2[1][1] = AbTrace2;
									tab2[2][0] = NTrace3;
									tab2[2][1] = AbTrace3;
									tab2[3][0] = NTrace4;
									tab2[3][1] = AbTrace4;
									tab2[4][0] = NTrace5;
									tab2[4][1] = AbTrace5;
									tab2[5][0] = NTrace6;
									tab2[5][1] = AbTrace6;
		 
		return tab2;
	}
	
}


