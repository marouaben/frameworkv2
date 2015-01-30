package ets.genielog.appwatcher_3;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;


/**
 * This is a n-gram based model.
 * The idea is to extract only frequent patterns (n-gram) and to use a threshold (from 0 to 1) to control the generalization ability of the model, allowing various lengths of n-grams.
 * 
 *  Normal behavioral profile contains sets of varied-length n-grams generating by checking each time if f(x_k+1)>alpha*min(f(x_k^i),f(x_k^j)), 
 * while alpha is a fixed threshold (between 0 and 1) and f(x_k^i) , f(x_k^j) are the frequencies of 2 n-grams of size k that constitute the n-gram x_{k+1} of size k+1.
 * 
 * In the detection phase, we extract new sets of n-grams and we compare them to the model created in learning phase.
 * 
 * @author Maroua
 *
 */
	public class NewModel extends Model {
		
		private Double threshold;//0.6;
		/** ArrayList of Pairs, 
		 ** each Pair contains a window size x and an arrayList of generating n-grams of length x.*/
		public Hashtable<Integer, Hashtable<String, Integer>> res;
		
		/**
		 * Calls super constructor and specifies it app name and model type ID.
		 * @param appname
		 * @param tHRESHOLD_MODEL 
		 * @param nGRAM_THRESHOLD 
		 */
		public NewModel(String appname, double nGRAM_THRESHOLD, double tHRESHOLD_MODEL) {			
			super(appname, ModelManager.NEW_MODEL, 2,nGRAM_THRESHOLD,tHRESHOLD_MODEL);	
			this.threshold=nGRAM_THRESHOLD;
		}
		

		
		/** Making a model consits in extracting varied-length n-grams from hole traces 
		 * using a fixed generation threshold, 
		 * and store them in the res attribute.
		 * @param traces: traces used to make the model
		 */
		@Override
		public void makeModel(ArrayList<Trace> traces){
			Extract model;
			/*ArrayList<String> trace=new ArrayList<String> ();
			for (int i=0; i<traces.size(); i++){
				String n=traces.get(i).toString().substring(0, traces.get(i).toString().length()-1);
			trace.add(n);
			}*/
			model=new Extract(traces,threshold);
			res=model.processing();		
		}
		

		/** Returns generating n-grams for each window size
		 */
		@Override
		public String getModelData() {
			String str = "New MOdel \n";		
			ArrayList<String> out =new ArrayList<String>();
			int size=0;
			//System.out.println("Max length of varied n-grams= "+ res.size());
			
			for ( Integer w : res.keySet()) {

				out.add("\n ************************* "+w+"-gram ********************\n");//window
				//System.out.println("\n ************************* "+w+"-gram ********************\n");
			
				for ( String key2 : res.get(w).keySet()) {
					size++;
				//	System.out.println(key2+" ("+res.get(w).get(key2)+")");
					out.add(key2+ "\n");//n-grams
				}}
			//System.out.println("Number of varied n-grams= "+ size);
			
		
	return "varied-length n-grams"; 
}
		public static void Display(Hashtable<Integer, Hashtable<String, Integer>> total, Hashtable<String, Integer> nbr) {

			// TODO Auto-generated method stub
			ArrayList<String> out =new ArrayList<String>();
			int size=0;
			
			System.out.println("Max length of varied n-grams= "+ total.size());
			
			for ( Integer w : total.keySet()) {

				out.add("\n ************************* "+w+"-gram ********************\n");//window
				System.out.println("\n ************************* "+w+"-gram ********************\n");
			
				for ( String key2 : total.get(w).keySet()) {
					size++;
					System.out.println(key2+" ("+total.get(w).get(key2)+")");
					out.add(key2+ "\n");//n-grams
				}}
			System.out.println("Number of varied n-grams= "+ size);

		//	System.out.print(out);
		}
	



		@Override
		public Boolean scanTraces(ArrayList<Trace> traces, int nGRAM_SIZE,
				double nGRAM_THRESHOLD, double tHRESHOLD_MODEL) {
			// TODO Auto-generated method stub
			return null;
		}

		/**
		 * Scans a set of traces. The scan consists in creating new model 
		 * and comparing it with the normal one 
		 * @param traces: traces used to make the model
		 * @return true if the rate exceeds the threshold, false if not.
		 */

		@Override
		public Boolean scanTracesB(ArrayList<Trace> traces, int nGRAM_SIZE,
				double nGRAM_THRESHOLD, double tHRESHOLD_MODEL) {
			
			/**resScan: new model**/
			 Hashtable<Integer, Hashtable<String, Integer>> resScan;
			
			 /** checking if the normal model exists and non null**/
			if(res == null){
				System.out.println("scanTraces error: Model not initialized");
				return null;
			}
			
			 
			 /**create model with nw trace**/
				Extract det=new Extract(traces,threshold);
				resScan=det.processing();
			
			/**abnormalSeq: nbr of abnormal n-grams for each set (each size)**/
				int[] abnormalSeq = new int[resScan.size()];
				
			/**ABNthreshold: threshold of abnormal n-grams for each set (each size)**/
				double ABNthreshold;
				
			/**Scan: comparing normal model "out" with new model "resScan".
			 * if resScan contains n-gram that exists in out, then it is normal one, we remove it from resScan and continue.
			 * else, if it not exist, we incremente abnormalSeq[size of this n-gram].
			 * we compare till we examine all n-grams in new model (resScan.get(i).getSecond().size())==0). 
			 * we calculate "sum": how many times the rate of abnormal n-grams within a set exceeds ABNthreshold 
			 * A trace is considered as abnormal if sum>(resScan.size()/2)*/
				
				double sum=0;
				List<Deviat> LD =new ArrayList<Deviat>();
				//ArrayList<Integer> LN =new ArrayList<Integer>();
				Hashtable<Integer, Integer>LN =new Hashtable<Integer, Integer>();
				 
				for ( int i =1; i<=resScan.size(); i++) {	//each window
				Hashtable<String, Integer> lDw=new Hashtable<String,Integer>();
					
					abnormalSeq[i-1]=0; //init
					//ABNthreshold=(1. / (i+2)); //window size increase --> threshold decrease
					//System.out.println("ABNthreshold "+ABNthreshold);
					int s=resScan.get(i).size(); //nbr of n-grams of each set
					
					int k=0; 
					while (((resScan.get(i).size())!=0)&&(k<s)){
						Iterator it = resScan.get(i).entrySet().iterator();
						while (it.hasNext())
						{
							Entry item = (Entry) it.next();
						if (res.size()>=i) {
							 if(res.get(i).containsKey(item.getKey())){//n-gram exists	
								 //resScan.get(i).remove(key);
								 System.out.println("contains "+item.getKey());
								 it.remove();
						}else{	
							lDw.put(((String)item.getKey()).replace(";", "-"), (Integer)item.getValue());
							
							System.out.println("newh: "+((String)item.getKey()).replace(";", "-")+" : "+(Integer)item.getValue() );
							abnormalSeq[i-1]++;  //abnormal n-gram	
							if (LN.contains(k)) {
								int freq=LN.get(k);
								LN.put(k, freq+1);
							}else {
								LN.put(k,1);

							}
						}
					}	
					 k++;
				 }
					 }
					 System.out.println ("abnormalSeq[i] "+abnormalSeq[i-1]);
					 System.out.println ("from total "+s);
					
				/** rate of abnormal n-grams within a set **/
				double rate= ((double)(abnormalSeq[i-1])) / (double)(s);
				//Deviat Deviat = new Deviat(i,abnormalSeq[i-1], s,rate, ABNthreshold, lDw);
			//	LD.add(Deviat);

				
				System.out.println ("rate  "+rate);
				// System.out.println ("ABNthreshold  "+ABNthreshold);

				/*if (rate>ABNthreshold) {//abnormal set
					sum++;
					
				}*/
		
			/*if (sum>(resScan.size()/2)) {//nbr of abnormal set > nbr of sets/2 
				return true;//true --> abnormal trace
			}*/
		}
			//	 insertScan(LD);
				insert(LN);
				return false;

				}
		private void insert(Hashtable<Integer, Integer> lN) {
			// TODO Auto-generated method stub

			try
			{ 
			    FileWriter fw = new FileWriter("/mnt/sdcard/Traces/StreamToTrace/Scan_VLNG.csv");
			    PrintWriter writer = new PrintWriter(fw);
			writer.print("key"); writer.print(";");writer.print("Number of new k-grams"); writer.println(";");
			
			for ( Integer key : lN.keySet()) {		
				writer.print(key); writer.print(";");writer.print(lN.get(key)); writer.println(";");
				}
			
	
					System.out.println("------------------fin------------------------------");

					  writer.flush();
				        
					    writer.close();
					        
					    fw.close();     
					}
					catch(IOException e)
					{
					     e.printStackTrace();
					} 
			 

		}



		private void insertScan(List<Deviat> lD)
		{	
			try
			{ 
			    FileWriter fw = new FileWriter("/mnt/sdcard/Traces/StreamToTrace/Scan_VLNG.csv");
			    PrintWriter writer = new PrintWriter(fw);
			writer.print("Window/set"); writer.print(";");
			writer.print("Number of anomalies by set"); writer.print(";");
			writer.print("Size of set"); writer.print(";");//sd --> table of p.first
			writer.print("Rate of abn by set"); writer.print(";");//% of the frequencies of all symbols in the trace  (total mean and total sd)
			writer.print("Threshold by set"); writer.println(";");//% of the frequencies of all symbols in the trace  (total mean and total sd)
			//% of the frequencies of all symbols in the trace  (total mean and total sd)

			for (int i = 0; i < lD.size(); i++) {
					writer.print(lD.get(i).window); writer.print(";");
					writer.print(" "+lD.get(i).nbrAbnor); writer.print(";");
					writer.print(" "+lD.get(i).sizeofSet); writer.print(";");
					writer.print(" "+lD.get(i).rate); writer.print(";");
					writer.print(" "+lD.get(i).threshold); writer.println(";");					
			}
			
			writer.println(";");
			for (int i = 0; i < lD.size(); i++) {
				
				writer.print("Set :"+lD.get(i).window); writer.print(";");	writer.print("freq");writer.println(";");	
				 for ( String key : lD.get(i).deviation.keySet()) {
					 writer.print(key); writer.print(";"); writer.print(lD.get(i).deviation.get(key)); writer.println(";");
			}
				 writer.println(";");
				 writer.println(";");
			}
			
	
					System.out.println("------------------fin------------------------------");

					  writer.flush();
				        
					    writer.close();
					        
					    fw.close();     
					}
					catch(IOException e)
					{
					     e.printStackTrace();
					} 
			 

				}

private class Deviat {
	private int window;
	private int nbrAbnor;
	private int sizeofSet;
	private Double rate;
	private Double threshold;
	private  Hashtable<String, Integer> deviation;	
	
	public Deviat(int w,int abn, int s, double rate, double thres,   Hashtable<String, Integer> lDw ){
		this.deviation=lDw;
		this.nbrAbnor=abn;
		this.rate=rate;
		this.sizeofSet=s;
		this.threshold=thres;
		this.window=w;
	}
}


		
	}
