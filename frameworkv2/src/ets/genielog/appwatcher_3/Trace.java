package ets.genielog.appwatcher_3;


import java.util.ArrayList;
import java.util.Hashtable;


/** This class reprents a trace off an app . A trace consists on a sequence of 
 * syscalls. 
 *
 */

public class Trace {
	
	/** Sequence of syscalls of which the trace is made 
	 * In further versions, should extend this class to have more intelligent
	 * ways of coding a trace.*/
	public ArrayList<String> sequence;
	public ArrayList<Integer> sequenceID;

	/** keyword for bottom and begin entries in sequeneces. */
	public static final String nullCall = "null";
	Hashtable <String, Integer> syscall= new  Hashtable <String, Integer>();//syscall string , freq
	public Hashtable<Integer, Integer> syscallID= new  Hashtable <Integer, Integer>();// id syscall, freq
	public Hashtable<String, Integer> syscallID2=new  Hashtable <String, Integer>();
	public Hashtable<String, Integer> syscallID3=new  Hashtable <String, Integer>();
	public Hashtable<String, Integer> syscallID4=new  Hashtable <String, Integer>();
	public Hashtable<String, Integer> syscallID5=new  Hashtable <String, Integer>();
	public Hashtable<String, Integer> syscallID6=new  Hashtable <String, Integer>();

	
	/** Constructor initialises the array */
	public Trace(){
		sequence = new ArrayList<String>();		
		sequenceID = new ArrayList<Integer>();		
	}
	
	
	/**
	 * Input traces of any size, rewrite them into traces of size n,
	 * keeping the same initial sequence. Cuts off the last "trace" if it is not filled.
	 * @param input: traces rewritten
	 * @param n: output traces size.
	 * @return
	 */
	public static ArrayList<Trace> reSize(ArrayList<Trace> input, int n){
		ArrayList<Trace> output = new ArrayList<Trace>();
		Trace t;
		Trace t1 = new Trace();
		for(int i = 0; i < input.size(); i++){
			t = input.get(i);
			for(int j = 0; j < t.size(); j++){
				t1.add(t.get(j));
				if(t1.size() == n){
					output.add(t1);
					t1 = new Trace();
				}
				
			}
		}
		return output;
	}
	
	/**
	 * Input traces of any size, rewrite them into traces of size n,
	 * keeping the same initial sequence. Cuts off the last "trace" if it is not filled.
	 * Also removes traces specified by the filter
	 * @param input: traces rewritten
	 * @param n: output traces size.
	 * @param filt: filter object with syscall to ignore
	 * @return
	 */
	public static ArrayList<Trace> reSize(ArrayList<Trace> input, int n, SysCallFilter filt){
		ArrayList<Trace> output = new ArrayList<Trace>();
		Trace t;
		Trace t1 = new Trace();
		for(int i = 0; i < input.size(); i++){
			t = input.get(i);
			for(int j = 0; j < t.size(); j++){
				if(filt.isMonitored((String) t.get(j))){
					t1.add(t.get(j));
				}
				if(t1.size() == n){
					output.add(t1);
					t1 = new Trace();
				}
			}
		}
		return output;
	}
	
	/**
	 * Adds "null" strings at the beginning and the end of a trace.
	 */
	public static Trace tracePadding(Trace trace, int depth){
		// Add nullcall entries at the beginning
		for(int i = 0; i < depth; i++){
			trace.add(0, nullCall);
		}
		// nulcall entries at the end
		for(int i = 0; i < depth; i++){
			trace.add(nullCall);
		}

		return trace;
	}
	
	/* ArrayList methods */
	public void add(String elem){sequence.add(elem);}
	public void addID(Integer elem){sequenceID.add(elem);}

	public void add(int index, String elem){sequence.add(index, elem);}
	public void addID(int index, Integer elem){sequenceID.add(index, elem);}
	
	public String get(int i){return  sequence.get(i);}
	public Integer getID(int i){return  sequenceID.get(i);}
	
	public int getFreq(String sys){return syscall.get(sys);}
	public int getFreqID(int sys){return syscallID.get(sys);}

	public int getsizeAlp(){return syscall.size();}
	public int size(){return sequence.size();}
	public int sizeID(){return sequenceID.size();}


	
	@Override
	public String toString(){
		//String 	ret = "Trace:[";
		String 	ret = "";
		for (int i = 0; i < sequence.size(); i++){
			ret = ret.concat(sequence.get(i) + ";");
			//	ret = ret.concat(sequence.get(i) + ",");
		}
		//ret = ret.concat("]");
		return ret;
	}

	public void setSyscall(Hashtable<String, Integer> syscall2) {
		// TODO Auto-generated method stub
		this.syscall=syscall2;
	}


	public void setSyscallID(Hashtable<Integer, Integer> syscallID) {
		// TODO Auto-generated method stub
		this.syscallID=syscallID;

	}

	public void removeID(int i) {
		// TODO Auto-generated method stub
		this.sequenceID.remove(i);
		
	}


	public void setSyscallID2(Hashtable<String, Integer> syscallID2) {
		// TODO Auto-generated method stub
		this.syscallID2=syscallID2;
	}
	public void setSyscallID3(Hashtable<String, Integer> syscallID2) {
		// TODO Auto-generated method stub
		this.syscallID3=syscallID2;
	}
	public void setSyscallID4(Hashtable<String, Integer> syscallID2) {
		// TODO Auto-generated method stub
		this.syscallID4=syscallID2;
	}
	public void setSyscallID5(Hashtable<String, Integer> syscallID2) {
		// TODO Auto-generated method stub
		this.syscallID5=syscallID2;
	}
	public void setSyscallID6(Hashtable<String, Integer> syscallID2) {
		// TODO Auto-generated method stub
		this.syscallID6=syscallID2;
	}
}
