package ets.genielog.appwatcher_3;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;






/**
 * The default model is a Forrest-like lookahead pairs model. To create the model we
 * make a list of lookahead syscalls pairs that appears in the trace, that we consider
 * as "normal".
 * When a trace is analyzed, we determine the rate of "abnormal" sequences it contains
 * and compare it to a threshold. 
 * @author trivh
 *
 */
public class DefaultModel extends Model {

	private static final long serialVersionUID = 5377993244182911946L;
	/** Lookahead pairs, referenced by syscall name in a hashtable 
	 * Each Key corresponds to pairSize lists of Strings. These lists are
	 * HashSet to make sure each syscall is referenced only once.
	 * */
	private Hashtable<String, ArrayList<HashSet<String>>> pairs;
	/** Length of the pair. A length of 2 means we look 2 syscalls ahead.*/
	private final int pairSize;//2
	/** Count of pairs per size */
	private int[] pairCount;
	/** Max rate of abnormal syscall tolerated */
	private double threshold ;// 0.025;


	/**
	 * Calls super constructor and specifies it app name and model type ID.
	 * @param appname
	 * @param tHRESHOLD_MODEL 
	 * @param nGRAM_SIZE 
	 */
	public DefaultModel(String appname, int nGRAM_SIZE, double tHRESHOLD_MODEL) {
		super(appname, ModelManager.DEFAULT_MODEL, nGRAM_SIZE,0.1, tHRESHOLD_MODEL);
		pairSize =nGRAM_SIZE;//=2;
		this.threshold=tHRESHOLD_MODEL;
		pairCount = new int[pairSize];
		for(int i = 0; i < pairSize; i++){
			pairCount[i] = 0;
		}
	}



	/** Making a model consits in looking what pairs appears in each trace and 
	 * store them in the pairs attribute.
	 * @param traces: trace used to make the model
	 */
	@Override
	public void makeModel(ArrayList<Trace> traces){
		//hole trace
		//	public void makeModel(		ArrayList<String>  traces){

		pairs = new Hashtable<String, ArrayList<HashSet<String>>>();
		updateModel(traces);

	}




	/** 
	 * Look in the {syscall, ahead} pair of syscalls is already in the model, depth
	 * being the distance the second one is ahead of the first.
	 * @param sycall: Corresponds to the key on the hashtable to look at
	 * @param depth: Element in the ArrayList of ArrayList of String we search in.
	 * @param ahead: Element we search in the ArrayList of String.
	 * @return true if the element is found, else false.
	 */
	public boolean isInModel(String syscall, int depth, String ahead){
		// If the syscall is not in the table, the pair can't possibly be in.
		// Same thing is the corresponding item has not been properly initialized.
		if(!pairs.containsKey(syscall) && pairs.get(syscall).size()!= pairSize){
			return false;
		}
		// Look if our element is in its corresponding list, and return depending on
		// that.
		if(pairs.get(syscall).get(depth).contains(ahead)){
			return true;
		}
		return false;
	}




	@Override
	public String getModelData() {
		String str = "Lookahead Pair model \n"+
				"Number of pairs:" + pairs.size() + "\n "+
				"Per size:\n";
		int j;
		return "Lookahead"; //TODO
	}

	/**
	 * toString method returns a String containing each pairs.
	 */
	public String toString(){
		String ret = "";
		String key;
		String value;
		Enumeration<String> e = pairs.keys();
		Iterator<String> iter;
	//	private Hashtable<String, ArrayList<HashSet<String>>> pairs;

		Iterator<String> wind = pairs.keySet().iterator();  
		  while( wind.hasNext()) {
			  key = wind.next();
			  ret = ret.concat(key + ":{");
			 //  System.out.println("/********************* wind="+keywind + " ************" );
			  for(int i = 0; i < pairs.get(key).size(); i++){ //pairSize
					ret = ret.concat("{");
					iter = pairs.get(key).get(i).iterator();  
					while(iter.hasNext()){
						value = iter.next();
						ret = ret.concat(value + ", ");
					}
					ret = ret.concat("}");
				}
				ret = ret.concat("} \n");
			}

			return ret;
	}

	/**
	 * Scans a set of traces. The scan consists in checking what lookahead pairs 
	 * in the traces are present in the model. A rate of "abnormal" pairs is
	 * calculated, and compared to a threshold. 
	 * @param traces: traces comparred to model
	 * @return true if the rate exceeds the threshold, false if not.
	 */
	@Override
	public Boolean scanTraces(ArrayList<Trace> traces, int nGRAM_SIZE,
			double nGRAM_THRESHOLD, double tHRESHOLD_MODEL) {
		if(pairs == null){
			System.out.println("scanTraces error: Model not initialized");
			return null;
		}
		double[] ret = new double[traces.size()];

		String key;
		String value;
		/* Number of abnormal syscalls detected. 
		 * is incremented each time a syscall is abnormal or have one of its successor
		 * syscalls not present in its lookahead.
		 *  */
		int abnormalSysCall = 0;
		/*
		 * Number of abnormal sequences detected by sequence # of the abnormal syscall.
		 * the 0 entry is for unknown syscall, the 1st one is for syscall followed
		 * by syscall not registered on the first element of their lookahead, and so on.
		 */
		int[] abnormalSeq = new int[pairSize];
		for(int i = 0; i < pairSize ; i ++){abnormalSeq[i] = 0;}
		double rate;


		// boolean saying if the current processed syscall is considered as normal
		boolean normal = true;
		boolean found;

		//System.out.println("########## LOOKAHEAD MODEL REPORT ############");
		// Loop on number of traces
		for(int i = 0; i < traces.size(); i++){
			// Loop on traces
			abnormalSysCall = 0;
			for(int k = 0; k < pairSize ; k ++){abnormalSeq[k] = 0;}

			for(int j = 0;	j + pairSize - 1< traces.get(i).size(); j++){
				normal = true;
				key = traces.get(i).get(j); 
				// If the key is not in the hashtable, it is considered as abnormal.
				// If it is, we check if it is followed by valid syscalls.
				if(pairs.containsKey(key)){
					//Loop on depth of lookahead.
					for(int offset = 0; offset < pairSize - 1; offset++){
						value = traces.get(i).get(j + offset + 1);
						// The sequence is normal if it is normal for EACH offset.
						// This is why we use &&.
						found = isInModel(key, offset, value);
						normal = normal && found;
						if (!found){
							//Case of {syscall, syscall + offset} unkown
							abnormalSeq[offset + 1]++;
						}															
					}
				}else{
					//Case of unknown syscall.
					normal = false;
					abnormalSeq[0]++;
				}
				//If the sequence is abnormal, increment abnormal counter.
				if(!normal){
					abnormalSysCall++;
				}
			}

			rate = ((double) abnormalSysCall) / ((double) traces.get(i).size());
			/*
			System.out.print("TRACE  " + i + ":");

			System.out.println(rate);*/
			ret[i] = rate;
			if (rate> threshold) {
				return true;
			}

		}

		return false;
	}
	/**
	 * Calculates a balanced abnormal rate with abnormal sequences classified
	 * regarding to the depth of abnormality.
	 * @param abnormalSeq: count of abnormal sequences by depth
	 * @param nbCall: total syscalls count
	 * @return balanced rate
	 */
	public double balance(int[] abnormalSeq, int nbCall){
		double ret = 0;
		for (int i = 0; i < abnormalSeq.length; i++){
			ret = ret + ((double) abnormalSeq[i]) / ((double) 1 + i);
		}
		ret = ret / (double) nbCall;
		return ret;
	}

	/**
	 * Updates model by adding new pairs found in traces.
	 */
	/******************************standard: separated sequences***********************************

	public void updateModel(ArrayList<Trace> traces) {

		String key;
		String value;
		// Loop on number of traces
		for(int i = 0; i < traces.size(); i++){
			// Loop on traces
			for(int j = 0;	j + pairSize - 1 < traces.get(i).size(); j++){
				key = traces.get(i).get(j);
				// If the key is not in the hashtable, add it and initialize it.
				if(!pairs.containsKey(key)){
					pairCount[0]++;
					addKey(key);
				}
				//Loop on depth of lookahead.
				for(int offset = 0; offset < pairSize - 1; offset++){
					value = traces.get(i).get(j + offset + 1);
					//System.out.println("VALUE  " + value);
					if(pairs.get(key).get(offset).add(value)){
						pairCount[offset + 1]++;
					}

				}
			}
		}
			}
					/************************************Continuous sequences************************************/

	public void updateModel(ArrayList<Trace> traces) {

		String key=null;
		String value;
		int pos=0;
		int i = 0;
		// Loop on number of traces
			while ( i < traces.size()){
			// Loop on traces
				for(int j = 0;	j + pairSize - 1 < traces.get(i).size(); j++){
				key = traces.get(i).get(j);
				// If the key is not in the hashtable, add it and initialize it.
				if(!pairs.containsKey(key)){
					pairCount[0]++;
					addKey(key);
				}
				//Loop on depth of lookahead.
				for(int offset = 0; offset < pairSize - 1; offset++){
					value = traces.get(i).get(j + offset + 1);
					if(pairs.get(key).get(offset).add(value)){
						pairCount[offset + 1]++;
					}
				}
				pos=j;
			}
				if (i<traces.size()-1){
				for(int j = 0;	j + pos  < traces.get(i).size(); j++){
				traces.get(i+1).add(j, traces.get(i).get(j+pos));
				}	}
				/******next trace***************/
		i++;	 
	}
			int start=pos+1;
			key = traces.get(i-1).get(start);
			// If the key is not in the hashtable, add it and initialize it.
			if(!pairs.containsKey(key)){
				pairCount[0]++;
				addKey(key);
			}
			for(int offset = 0; offset< traces.get(i-1).size()-start-1; offset++){
				value = traces.get(i-1).get(start+1 + offset);
				if(pairs.get(key).get(offset).add(value)){
					pairCount[offset + 1]++;
				}
			}
			}
	
/*******************************************************************************************************/
	/** Add a key and initialize its corresponding value
	 * 
	 * @param key: key to add
	 */
	public void addKey(String key){

		// initialize the depth of the second element of the pair
		ArrayList<HashSet<String>> lists = new ArrayList<HashSet<String>>();
		for(int i = 0; i < pairSize - 1; i++){
			lists.add(new HashSet<String>());
		}
		// put the pair in the hashtable
		pairs.put(key, lists);
	}
	
	public int[] getPairNumbers(){return pairCount;}



	@Override
	public Boolean scanTracesB(ArrayList<Trace> traces, int nGRAM_SIZE,
			double nGRAM_THRESHOLD, double tHRESHOLD_MODEL) {
		// TODO Auto-generated method stub
		return null;
	}



}
