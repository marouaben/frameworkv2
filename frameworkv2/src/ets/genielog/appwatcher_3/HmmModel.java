package ets.genielog.appwatcher_3;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import static java.lang.Math.*;
/**
 * matrix of n-grams
 * we will use HMM of order 2.
 * it is a matrix of aphabets (1-grams). M[1-gramX][1-gramY]=percentage of having the 2-grams [(1-gramX)-(1-gramY)]
 * @author Maroua
 *
 */
public class HmmModel extends Model{

	/**hmm order (size of n-gram)**/
	//private final int order;
	/**hmm order 2 (size of n-gram)
	 * HashMap<Pair<Integer,Integer>, Integer> 
	 * --> 
	 * HashMap<
	 * Pair<syscallA,nextsyscallofA>, 
	 * frequenceOf(Pair<syscallA,nextsyscallofA>)
	 * >
	 * **/
	private Hashtable<String, Integer> Hmm;
	private HashMap<Integer, Double> freqF= new HashMap<Integer, Double>(); //symbole, freq in all traces 
	private double SDTotal;
	private double alphaM=1.;

	//private static final long serialVersionUID = 3204350386890670150L;
	
	
	private int sizeTraces;
	
	/**
	 * HmmRow
	 * percentage of each syscall
	 * -------------------------------------------
	 * syscall1| %  ; %  ; %  ; %  ; %  ; %  |  
	 * -------------------------------------------
	 * syscall2| %  | %  | %  | %  | %  | %  |  
	 * --------------------------------------------
	 * syscall3| %  | %  | %  | %  | %  | %  |  
	 * ---------------------------------------------
	 */
	private Hashtable<Integer, ArrayList<Double>> HmmRow=new Hashtable<Integer, ArrayList<Double>>();
	
	/**
	 * Calls super constructor and specifies it app name and model type ID.
	 * @param appname
	 */
	public HmmModel(String appname, int modelID, double nGRAM_THRESHOLD, double tHRESHOLD_MODEL) {
		super(appname, modelID, 2, nGRAM_THRESHOLD, tHRESHOLD_MODEL);
	}
	
	
	
	public HmmModel(String appname, double nGRAM_THRESHOLD, double tHRESHOLD_MODEL) {
		super(appname, 2, nGRAM_THRESHOLD, tHRESHOLD_MODEL);
	
	}


	
	@Override
	public void makeModel(ArrayList<Trace> traces) {

		int last; //last syscall
		int first; //first syscall
		Hmm=new Hashtable<String, Integer>();
		sizeTraces=0;
		for (int i = 0; i < traces.size(); i++) {
			sizeTraces=sizeTraces+traces.get(i).sizeID();
		
			for (int j = 0; j < traces.get(i).sizeID()-1; j++) {
			Hmm.put(traces.get(i).getID(j)+";"+traces.get(i).getID(j+1), 1);
			}
			last=traces.get(i).getID(traces.get(i).sizeID()-1); 
			

			if ((i+1) < traces.size()) {
				first=traces.get(i+1).getID(0);
				//Pair link =new Pair<Integer, Integer>(last, first);
				add(Hmm,last+";"+first);
			}	
		}
		//System.out.println("hmm.size(): "+hmm.size());
		System.out.println("sizeTraces: "+sizeTraces);
		setHmmRow(Hmm, traces);
		setHmmFreq(HmmRow);
					 
		 }
		
private void setHmmFreq(Hashtable<Integer, ArrayList<Double>> hmmRowN) {
		// TODO Auto-generated method stub
	double sd=0.00;
	double m=100.00/((double)hmmRowN.size());
	 for ( Integer key : hmmRowN.keySet()) {
		 ArrayList<Double> list=hmmRowN.get(key);
			double freq=0.00;
		 for (int i = 0; i < list.size(); i++) {
			 freq=freq+list.get(i); //freq of a symbole 

		}
		 sd=sd+
				 ( (freq-m)*(freq-m) );
		freqF.put(key, freq);
	 }
	 this.SDTotal=sqrt(sd/(double)(hmmRowN.size()));
	}



/*private void addSymb(int sys) {
		// TODO Auto-generated method stub
	if (symboles.containsKey(sys)) {
		int freq=symboles.get(sys);
		symboles.put(sys, freq+1);	
		
	}else {
		symboles.put(sys, 1);
	}		
	}*/


//	insert(hmm);

	private void setHmmRow(Hashtable<String, Integer> hmm2, ArrayList<Trace> traces) {
		// TODO Auto-generated method stub

		ArrayList<Double> value=new ArrayList<Double>();
		 
		 for ( String key : hmm2.keySet()) {
			 String[] keys = key.split(";");
			 int first=Integer.parseInt(keys[0]);
			 double rf=((hmm2.get(key))*100)/((double)hmm2.size());
			 double rate=rf/(double)(sizeTraces-1);
			 System.out.println("rate: "+ rate);
			 if (rate!=0.00) {			
				// SD=SD+((rate-Mean)*(rate-Mean));
				// System.out.println("SD1  "+ SD);
				// System.out.println("first"+ first);
				 
			 if (HmmRow.containsKey(first)) {	
				 //System.out.println("true"+ first);

				 value=HmmRow.get(first);
				 value.add(rate);
				 HmmRow.remove(first);
				 HmmRow.put(first, value);
				// System.out.println("HmmRow.contains(key.getFirst(): "+first+"  size: "+ value.size()+" are: "+value);

			}else {
				value=new ArrayList<Double>();
				value.add(rate);
				HmmRow.put(first, value);
			}
			 
			}
	}
		// SD=Math.sqrt(SD/(double)(hmm.size()));
		// System.out.println("SD Final "+ SD);

		 
	}

	



	private void add(Hashtable<String, Integer> hmm2,String p) {	
		if (hmm2.containsKey(p)) {
			int freq=hmm2.get(p);
			hmm2.put(p, freq+1);	
			
		}else {
			hmm2.put(p.toString(), 1);
		}		
	}





	@Override
	public String getModelData() {
		return "Hidden Markov Model";
	}
	

	@Override
	public Boolean scanTraces(ArrayList<Trace> traces, int nGRAM_SIZE,
			double nGRAM_THRESHOLD, double tHRESHOLD_MODEL) {
		/*ArrayList<Trace> tracesi=new ArrayList<Trace>();
		for (int i = 0; i < traces.size(); i++) {
			tracesi.add(traces.get(i));
			scanningDR(tracesi);
		}*/
		
		Double threshold=0.6;
		/**
		 * nbr of new n-grams 
		 */
		
		/*** we create a HashMap of tested syscalls to be compared with the model */
		HashMap<Pair<Integer,Integer>, Integer> test=new HashMap<Pair<Integer,Integer>, Integer>();

		String last; //last syscall
		String first; //first syscall
		
		Deviation D=null;
		/**
		 * LD
		 * list of deviations
		 */
		ArrayList<Deviation> LD=new ArrayList<Deviation>();
		HashMap<Pair<Integer,Integer>, Integer> LDI=new HashMap<Pair<Integer,Integer>, Integer>();
		
		/**
		 * testModel
		 * Model of testing traces with its Mean and SD
		 */
		HmmModel testModel=new HmmModel(getAppname(), 2,0); 
		testModel.makeModel(traces);
		//System.out.println("testModel.HmmRow.size: "+testModel.HmmRow.size()+"  testModel.SD: "+testModel.SD+"  testModel.Mean: "+testModel.Mean);
		int nbr=0;
		
		for (int i = 0; i < traces.size(); i++) {
			for (int j = 0; j < traces.get(i).sizeID()-1; j++) {
				Pair p =new Pair<Integer, Integer>(traces.get(i).getID(j), traces.get(i).getID(j+1));
				//System.out.println("pair: "+p.toString());


				if ((!Hmm.containsKey(p.toString()))&&(!LDI.containsKey(p)&&(HmmRow.containsKey(p.getFirst()))&&(testModel.HmmRow.containsKey(p.getSecond())&&(testModel.freqF.containsKey(p.getSecond()))))) {
				//	System.out.println("/***********foreign*************/ ");

					D=new Deviation(HmmRow,freqF,SDTotal, p, testModel.HmmRow, testModel.freqF, testModel.SDTotal);
					LD.add(D);
					LDI.put(p,1);
					nbr++;
				}
				addT(p,test);
				//search(p, test, NewG);			
			}
			last=traces.get(i).get(traces.get(i).size()-1); 
			if ((i+1) < traces.size()) {
				first=traces.get(i+1).get(0);
				Pair link =new Pair<String, String>(last, first);
				//search(link, test, NewG);
				addT(link,test);
			}
			//fill(TestRow,test);
			//ZLD(test,TestRow, LD);
			System.out.println("nbr: "+nbr);
			System.out.println("lD.size(): "+LD.size());

			insertScan(LD, HmmRow, testModel);
		}
		
		
		return true;
		}
	
	private void scanningDR(ArrayList<Trace> traces) {
		// TODO Auto-generated method stub
		Double threshold=0.5;
		/**
		 * nbr of new n-grams 
		 */
		
		/*** we create a HashMap of tested syscalls to be compared with the model */
		HashMap<Pair<Integer,Integer>, Integer> test=new HashMap<Pair<Integer,Integer>, Integer>();

		String last; //last syscall
		String first; //first syscall
		
		Deviation D=null;
		/**
		 * LD
		 * list of deviations
		 */
		ArrayList<Deviation> LD=new ArrayList<Deviation>();
		HashMap<Pair<Integer,Integer>, Integer> LDI=new HashMap<Pair<Integer,Integer>, Integer>();
		
		/**
		 * testModel
		 * Model of testing traces with its Mean and SD
		 */
		HmmModel testModel=new HmmModel(getAppname(), 2,0); 
		testModel.makeModel(traces);
		//System.out.println("testModel.HmmRow.size: "+testModel.HmmRow.size()+"  testModel.SD: "+testModel.SD+"  testModel.Mean: "+testModel.Mean);
		int nbr=0;
		int size=0;
		
		for (int i = 0; i < traces.size(); i++) {
			size=size+traces.get(i).size();
			for (int j = 0; j < traces.get(i).size()-1; j++) {
				Pair p =new Pair<Integer, Integer>(traces.get(i).getID(j), traces.get(i).getID(j+1));
				//System.out.println("pair: "+p.toString());
				

				if ((!Hmm.containsKey(p.toString()))) {
				//	System.out.println("/***********foreign*************/ ");
					nbr++;
					//D=new Deviation(HmmRow,freqF,SDTotal, p, testModel.HmmRow, testModel.freqF, testModel.SDTotal);
					///LD.add(D);
					//LDI.put(p,1);
					
				}
				addT(p,test);
				//search(p, test, NewG);			
			}
			last=traces.get(i).get(traces.get(i).size()-1); 
			if ((i+1) < traces.size()) {
				first=traces.get(i+1).get(0);
				Pair link =new Pair<String, String>(last, first);
				//search(link, test, NewG);
				addT(link,test);
			}
			//fill(TestRow,test);
			//ZLD(test,TestRow, LD);
			System.out.println("nbr: "+nbr);
			System.out.println("lDI.size(): "+LDI.size());
			write("size: "+traces.size()+";"+" rate:"+(nbr/size)*100+" nbr:"+nbr+"\n");

			//insertScan(LD, HmmRow, testModel);
		}
	}
	public void write(String texte)
	{
		//on va chercher le chemin et le nom du fichier et on me tout ca dans un String
		String adressedufichier = "/mnt/sdcard/Traces/StreamToTrace/DR_Candy_hmm.txt";
	
		//on met try si jamais il y a une exception
		try
		{
			/**
			 * BufferedWriter a besoin d un FileWriter, 
			 * les 2 vont ensemble, on donne comme argument le nom du fichier
			 * true signifie qu on ajoute dans le fichier (append), on ne marque pas par dessus 
			 
			 */
			FileWriter fw = new FileWriter(adressedufichier, true);
			
			// le BufferedWriter output auquel on donne comme argument le FileWriter fw cree juste au dessus
			BufferedWriter output = new BufferedWriter(fw);
			
			//on marque dans le fichier ou plutot dans le BufferedWriter qui sert comme un tampon(stream)
			output.write(texte);
			//on peut utiliser plusieurs fois methode write
			
			output.flush();
			//ensuite flush envoie dans le fichier, ne pas oublier cette methode pour le BufferedWriter
			
			output.close();
			//et on le ferme
			System.out.println("fichier créé");
		}
		catch(IOException ioe){
			System.out.print("Erreur : ");
			ioe.printStackTrace();
			}

	}


	private void insertScan(List<Deviation> lD, Hashtable<Integer, ArrayList<Double>> hmmRow2, HmmModel testModel )
	{	
		 double mN=100.00/((double)hmmRow2.size());
		 double mT=100.00/((double)testModel.HmmRow.size());
		try
		{ 
		    FileWriter fw = new FileWriter("/mnt/sdcard/Traces/StreamToTrace/Scan_hmm.csv");
		    PrintWriter writer = new PrintWriter(fw);
		writer.print("Type of Deviation: 1 (Foreign symbol) / 2(Foreign Pair)"); writer.print(";");
		writer.print("Pair"); writer.print(";");
		writer.print("Branches of P.first in Normal traces"); writer.print(";");
		writer.print("SD of P.first in Normal traces"); writer.print(";");//sd --> table of p.first
		writer.print("Z of P.first in Normal traces"); writer.print(";");//% of the frequencies of all symbols in the trace  (total mean and total sd)
		writer.print("Z of P.second in Abnormal traces"); writer.print(";");//% of the frequencies of all symbols in the trace  (total mean and total sd)
		writer.print("Z of P.second in Normal model"); writer.print(";");//% of the frequencies of all symbols in the trace  (total mean and total sd)
		writer.print("P(anomaly)"); writer.println(";");

		for (int i = 0; i < lD.size(); i++) {
				writer.print(lD.get(i).Type); writer.print(";");
				writer.print(" "+lD.get(i).F.getFirst()+"-"+lD.get(i).F.getSecond()); writer.print(";");
				writer.print(" "+lD.get(i).Nbranches); writer.print(";");
				writer.print(" "+lD.get(i).SD); writer.print(";");
				writer.print(" "+lD.get(i).ZN); writer.print(";");
				writer.print(" "+lD.get(i).ZAb1); writer.print(";");
				if (lD.get(i).Type==1) {
					writer.print("--"); writer.print(";");
				}else {
					writer.print(" "+lD.get(i).ZAb2); writer.print(";");
				}
				writer.print(" "+lD.get(i).Prob); writer.println(";");
		}
		writer.println(";");
		writer.print("SD of all symbols in Normal traces"); writer.print(";"); writer.print("Mean of all symbols in Normal traces"); writer.print(";");
		writer.print("SD of all symbols in Abnormal traces"); writer.print(";"); writer.print("Mean of all symbols in Abnormal traces"); writer.println(";");
		writer.print(this.SDTotal);writer.print(";");writer.print(mN);writer.print(";");
		writer.print(testModel.SDTotal);writer.print(";");writer.print(mT);writer.print(";");
		
		writer.println(";");
		writer.print("all symbols in Normal traces"); writer.print(";"); writer.print("all symbols in Ab traces"); writer.println(";");
		writer.print(HmmRow.size());writer.print(";");writer.print(testModel.HmmRow.size());writer.println(";");
		
		writer.print("all Normal traces"); writer.print(";"); writer.print("all Ab traces"); writer.println(";");
		writer.print(sizeTraces);writer.print(";");writer.print(testModel.sizeTraces);writer.print(";");

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
		    

	/**private void ZLD(HashMap<Pair<Integer, Integer>, Integer> test, Hashtable<Integer, List<Double>> testRow2,
			List<Deviation> lD) {

		HmmModel testModel=new HmmModel(this.getAppName());
		// TODO Auto-generated method stub
		for (int i = 0; i < lD.size(); i++) {
			if ((!test.containsKey(lD.get(i).F))&& (testRow2.contains((lD.get(i).F).getSecond()))) {//getSecond()
				lD.get(i).setZAb(testRow2, lD.get(i).F, SD, Mean);
			}
			
		}
		
	}/**


	/**private void fill(Hashtable<Integer, List<Double>> TestRow2, HashMap<Pair<Integer, Integer>, Integer> test) {
		// TODO Auto-generated method stub
		 List<Double> value;
		 
		 for (Pair<Integer, Integer> key : test.keySet()) {
			 double rate=((test.get(key))*100)/49999;
			 if (rate!=0.0) {			
			
			 if (TestRow2.contains(key.getFirst())) {
				 value=TestRow2.get(key.getFirst());
				 value.add(rate);
				 TestRow2.put(key.getFirst(), value);
			}else {
				value=new ArrayList<Double>();
				 value.add(rate);
				TestRow2.put(key.getFirst(), value);
			}
			 }	 
		 }
		 
	}**/


	private void addT(Pair p, HashMap<Pair<Integer, Integer>, Integer> test) {
		// TODO Auto-generated method stub
			if (test.containsKey(p)) {	
				int fr=test.get(p)+1;
				test.put(p,fr );	//freq			
			}else {
				test.put(p,1 );
			}
		
	}

	
	
	private void insert(HashMap<String, Integer> hmm4) {
		// TODO Auto-generated method stub
		try
		{ 
		    FileWriter fw = new FileWriter("/mnt/sdcard/Traces/StreamToTrace/Trace_hmm.csv");
		    PrintWriter writer = new PrintWriter(fw);
				 
				 
		//line 4		
				 // writer.println("------------------key: + key +  value:  + hmm.get(key)------------------------------");

				   Integer[][] tab =new Integer[300][300];
				   for (int i = 0; i < tab.length; i++) {
					   for (int j = 0; j < tab.length; j++) {					
					tab[i][j]=0;
					   }
				}
				 for (String key : Hmm.keySet()) {
						String[] keys = key.split(";");

					 tab[Integer.parseInt(keys[0])][Integer.parseInt(keys[1])]=Hmm.get(key);
					//writer.println(key.getSecond().toString() + ";" + hmm.get(key));
					}
				 
				 writer.print("2-grams");writer.print(";");
				 for (int j = 0; j < tab.length; j++) {
					 writer.print(j);writer.print(";");					
				}
				 writer.print("\n");
				 for (int i = 0; i < tab.length; i++) {
					 writer.print(i);writer.print(";"); //just under 2-grams
				 for (int j = 0; j < tab.length; j++) {
					 writer.print(tab[i][j]);writer.print(";");					
				}
				 writer.print("\n");
				 }
				 
 	
			    	System.out.println("------------------fin------------------------------");

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

	
	/****************************************************************************/
	private static void insertHoleTracecsv( Trace holeTrace, String name)
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


	class  Deviation{
		/**
		 * 2 typoes of deviations:
		 * Type 1 = for foreign symboles
		 * Type 2 = for seen symboles but not 2-gram (not the same order)
		 */
		int Type;
		
		/**
		 * 		if Type==1: 
		 *	F: Foreign symbol: not seen in the HMM. 
		 *	Pair<Integer, Integer> F --> F.getsecond() is foreign
		 **
		 **
		 *		if Type==2
		 *	F: Foreign 2-gram: not seen in the HMM.  
		 * 	Pair<Integer, Integer> F --> F is foreign
		 * *
		 */
		Pair<Integer, Integer>  F; 
		
		/**
		 *Nbranches:  number of branches 200 makes: simply count number of non-zero percentages in the row associated to 200. That means, in the normal model, how many other sys calls you see after 200.
		 * 
		 * nbr of other sys calls that come after F.get(first)>
		 */
		int Nbranches;
		int NbranchesT;
		
		/**
		 *Mean of branches %:
		 * a-  for each branche we store % value in the HMM (the % of each sys calls you see after  F.get(first))  --> Bi
		 * b-  we sum Bi of theses branches --> Sum(Bi)
		 * c- Mean= (Sum(Bi))/Nbranches
		 */	
		double MeanBiN;
		double MeanBiT;
		
		/**
		 *Mean of all the appeared sys calls in NM %:
		 *
		 */	
	//	double MeanBi;
		
		/**
		 * SD: standard deviation of the percentages of the branches of F.get(first):
		 *  
		 * a-  for each branche we store % value in the HMM (the % of each sys calls you see after  F.get(first))  --> Bi
		 * b-  we sum Bi of theses branches --> Sum(Bi)
		 * c-  we calculate the Mean -->double Mean = (S(Bi))/Nbranches)
		 * d-  for each Bi: subtract the Mean and square the result --> Sqi=(Bi-Mean)*(Bi-Mean)
		 * c- Then work out the mean of those squared differences and Take the square root of that --> double SD = sqrt((Sum(Sqi))/Nbranches)
		 *
		 **/		
		double SD;
		double SDT;
		
		/**
		 * Z: standard deviation of the percentages of the branches of F.get(first):
		 * Z score is the number of standard deviations the variable P(F.get(first)) is away from the mean. where P is the appearance of sys call (F.get(first)) in the normal training model
		 *  
		 * Z=(P(F.get(first)) -mean)/SD in normal traces
		 *
		 **/	
		double ZN;
	
		
		/**
		 * ZAb: standard deviation of the percentages of the branches of F.get(first):
		 *  
		 * ZAb=(P(F.get(first)) -mean)/SD in Abnormaltraces.  where P is the appearance of sys call (F.get(first)) in the normal training model
		 *
		 **/		
	//	double ZAb;
		/**
		 * ZAb1: ZAb of F.get(second)
		 * Type =1
		 * Foreign SYMBOLE
		 */
		double ZAb1;
		
		/**
		 * ZAb2: Z normal of F.get(second)
		 * Type =2
		 * Foreign PAIR --> ZAb1 +ZAb2
		 */
		double ZAb2;

		double SDTotal;
		double MeanTotal;
		
		double SDTotalTest;
		double MeanTotalTest;
		
		Hashtable<Integer, Double> freqF=new Hashtable<Integer, Double>();
		
		
		/**Hashtable<Integer, ArrayList<Double>> hmmRow;
		Hashtable<Integer, ArrayList<Double>> hmmRowTest;**/
		
		double Prob;
		

		
		public Deviation(Hashtable<Integer, ArrayList<Double>> hmmRowN, HashMap<Integer, Double> freqF2, double sDTotal2, Pair<Integer, Integer> F,
				Hashtable<Integer, ArrayList<Double>> hmmRowT, HashMap<Integer, Double> freqF3, double sDTotal3){	
			int symbole;
			
			/**this.hmmRow=hmmRow2;
			this.hmmRowTest=hmmRow3;
			this.F=F;
			this.MeanTotal=mean;
			this.SDTotal=sD2;
			this.SDTotalTest=sDtest;
			this.MeanTotalTest=meantest;**/

			
			this.MeanTotal=(100.00/(double)hmmRowN.size()); //mean total of all frequencies of all symboles 
			this.MeanTotalTest=(100.00/(double)hmmRowT.size());
			
			this.SDTotal=sDTotal2;
			this.SDTotalTest=sDTotal3;

			
			this.F=F;	
			this.Nbranches=(hmmRowN.get(F.getFirst())).size();
			this.NbranchesT=(hmmRowT.get(F.getFirst())).size();
			
			//this.MeanBiN=meanBi(hmmRowN,F.getFirst());
			//this.MeanBiT=meanBi(hmmRowT,F.getSecond());

			this.SD=SD(Nbranches,hmmRowN.get(F.getFirst()),MeanBiN);
			this.SDT=SD(NbranchesT,hmmRowT.get(F.getSecond()),MeanBiT);
			
			//this.ZN=Z(MeanBiN, SD, hmmRowN.get(F.getFirst()));
			//this.ZAb1=Z(MeanBiT, SDT, hmmRowT.get(F.getSecond()));//foreign
			
			System.out.println("F.first: "+F.getFirst());
			System.out.println("F.second: "+F.getSecond());
			System.out.println("MeanTotalTest: "+MeanTotalTest);
			System.out.println("SDTotalTest: "+SDTotalTest);
			System.out.println("SDTotal: "+SDTotal);
			
			this.ZN=Z2(MeanTotal, SDTotal,freqF2.get(F.getFirst()));
			this.ZAb1=Z2(MeanTotalTest, SDTotalTest, freqF3.get(F.getSecond()));//foreign
			
			if (!hmmRowN.containsKey(F.getSecond())){	//foreign
				this.Type=1;
				this.ZAb2=100.0;//Double.MAX_VALUE;//f.getsecond is foreign so it dosent exist in normal model


			}else {
				this.Type=2;
				//double meansecond=meanBi(hmmRowN,F.getSecond());
				//int NbranchesSecond=(hmmRowN.get(F.getSecond())).size();
				//double SDsecond=SD(NbranchesSecond,hmmRowN.get(F.getSecond()),meansecond);
				//this.ZAb2=Z(meansecond, SDsecond, hmmRowN.get(F.getSecond()));// f.getsecond exists in normal model 
				this.ZAb2=Z2(MeanTotal, SDTotal, freqF3.get(F.getFirst()));// f.getsecond exists in normal model 
			}
			
			if (this.ZN <=-2) {
				this.Prob=0;
			}
			else if ((this.ZAb1<=-2)||(this.ZAb2<=-2)) {
				this.Prob=1;
			}else
			this.Prob=prob(SD,ZN,Nbranches,ZAb1, ZAb2);
			
			
			
		}

	

		private double prob(double SDpfirstN, double ZpfirstN, int NbranchesPfirst,
				double ZpSecondAb, double ZpSecondN) {
			// TODO Auto-generated method stub
			double a;
			double b;
			double c;
			double d;
			double e;
			double f=0.00;
			
			if (Type==1) {
				f=1;
			}else if (Type==2) {
				f=ZpSecondN+3;
			}
			
			a=1+SDpfirstN;
			b=((double)alphaM*(double)ZpfirstN)+(double)3;
			c=ZpSecondAb+3;
			d=NbranchesPfirst*c*f;
			e=a*b;
			return (e/d);
		}

		private double meanBi(Hashtable<Integer, ArrayList<Double>> parts,
				Integer first) {
			double sum=0.000;
			for (int i = 0; i < parts.get(first).size(); i++) {
				sum=sum+ parts.get(first).get(i);      			

			}
			return (sum/(parts.get(first).size()));
		}
		

	


		/**public void setZAb(Hashtable<Integer, List<Double>> testRow2,
				Pair<Integer, Integer> f2, double sD2, double mean) {
			// TODO Auto-generated method stub
			Deviation D2=new Deviation(testRow2, f2, sD2, mean);
			this.ZAb=D2.Z;
		}
		 * @param f2 **/

		private double Z(double meanTotal2, double sDTotal2, ArrayList<Double> parts) {
			// TODO Auto-generated method stub
			double freq=0.000;
			for (int i = 0; i <parts.size(); i++) {
				freq=freq+ parts.get(i);      			
			}
			return ((freq-meanTotal2)/sDTotal2);
		
		}
		private double Z2(double meanTotal2, double sDTotal2, double freq) {
			// TODO Auto-generated method stub
			
			return ((freq-meanTotal2)/sDTotal2);
		
		}

		private double  SD(int nbranches2, ArrayList<Double> parts, double meanTotal) {
			
			// TODO Auto-generated method stub
			double sum=0.000;

			for (int i = 0; i <parts.size(); i++) {
				sum=sum+           
						(
								((parts.get(i)) - meanTotal)*((parts.get(i)) - meanTotal)
								);
			}
			System.out.println("sum od SD: "+sum);
			//System.out.println("nbr of branches of SD: "+nbranches2);
			//System.out.println("SD*SD: "+(sum/(double)nbranches2));
			double SD2=(sum/(double)nbranches2);
			System.out.println("SD: "+sqrt(SD2));
			return (sqrt(SD2));
		}



		 
	}	





	@Override
	public Boolean scanTracesB(ArrayList<Trace> traces, int nGRAM_SIZE,
			double nGRAM_THRESHOLD, double tHRESHOLD_MODEL) {
		// TODO Auto-generated method stub
		return null;
	}

}




