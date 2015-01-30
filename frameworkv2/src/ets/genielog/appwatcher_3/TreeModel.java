package ets.genielog.appwatcher_3;


import java.util.ArrayList;
import java.util.Iterator;





/**
 * This is a n-gram based model.
 * It builds a n-gram tree referencing the frequency for each n-gram of a size up to 
 * a fixed N. As more frequent n-grams are considered more normal, a abnormality score
 * is calculated for each n-gram from their frequency. 
 * The scan consists in finding each n-gram of a trace in the tree and summing up 
 * their abnormality score. A trace is considered abnormal it the sum of abnormality
 * scores exceeds a threshold.
 * @author trivh
 *
 */
public class TreeModel extends Model{

	public static int ANOMALIES_CALCULATORS = 1; // number of formulas for anomalies
	public static int LOG_ANOMALY = 0; // ID for first formula
	public static String[] anomaly_rates = {"Log Anomaly"}; // Names of formulas
	public static ArrayList<AnomalyRate> setters;
	private double threshold ;// 1.2;



	private static final long serialVersionUID = 7953257642769483106L;

	/**
	 * Max size of n-grams / depth of the tree
	 */
	private final int treeDepth;

	/**
	 * Root of the tree containing all n-grams.
	 */
	private TreeNode treeRoot;
	private static String rootNodeName = "rootNode";

	/**
	 * Number of ngrams used to create the model.
	 */
	private int nGramTotal;
	/**
	 * Number of disctinct ngrams in the model.
	 */
	protected int nGramDistinct;
	protected int[] nGramPerLen;
	/**
	 * Calls super constructor and specifies it app name and model type ID.
	 * @param appname
	 * @param tHRESHOLD_MODEL 
	 * @param nGRAM_SIZE 
	 */
	public TreeModel(String appname, int nGRAM_SIZE, double tHRESHOLD_MODEL) {
		super(appname, ModelManager.TREE_MODEL, nGRAM_SIZE,0.1,tHRESHOLD_MODEL);
		if (setters== null){
			setters = new ArrayList<AnomalyRate>();	
			setters.add(new LogAnomaly());
		}
		treeDepth = nGRAM_SIZE;//10
		this.threshold=tHRESHOLD_MODEL;
	}

	

	public TreeModel(String appname, int treeDepth, int nGramTotal, TreeNode root){
		super(appname, ModelManager.TREE_MODEL, treeDepth,0.1, nGramTotal);
		if (setters== null){
			setters = new ArrayList<AnomalyRate>();	
			setters.add(new LogAnomaly());
		}
		this.treeDepth = treeDepth;
		this.nGramTotal = nGramTotal;
		this.treeRoot = root;
	}

	//public void setTreeDepth(int depth){treeDepth = depth;}

	/**
	 * Steps:
	 * 	- Generate n-grams from traces and store them in a tree.
	 * Frequencies (number of apparition) are stored as well.
	 * 	- Compute abnormality rate for each n-gram and store it in the tree.
	 */
	@Override
	public void makeModel(ArrayList<Trace> traces) {
		nGramTotal = 0;
		nGramDistinct = 0;
		nGramPerLen = new int[treeDepth];
		for(int i = 0; i < treeDepth; i ++){
			nGramPerLen[i] = 0;
		}
		// Initialize n-grams tree.
		treeRoot = new TreeNode(rootNodeName);

		// Create n-grams and insert them in the tree.
		System.out.println("trace size== "+traces.size());
		for(int i = 0; i < traces.size(); i++){
			makeNgrams(traces.get(i), treeRoot, i, traces);
			// Increment total number of ngrams.
			nGramTotal = nGramTotal + traces.get(i).size() * treeDepth;
			nGramTotal = nGramTotal - (treeDepth * (treeDepth - 1)) / 2;
		}
		// Parse tree to setup abnormality rates.
		treeRoot.setupAbnormality(nGramTotal);
	}

	/**
	 * Parses a trace and inserts all n-grams contained in it in the tree.
	 * For each n-gram inserted, increments its counter. 
	 * @param traces 
	 * @param i2 
	 * @param trace: trace parsed.
	 */
	 public void makeNgrams(Trace trace, TreeNode root, int i2, ArrayList<Trace> traces){
		// Initialize n-gram. With the first sequence of the trace
		int l=0;
		ArrayList<String> ngram = new ArrayList<String>();
		for(int i = 0; i < treeDepth; i++){
			ngram.add(trace.get(i));
		}
		addInTree(ngram, root); // Add n-gram in tree
		// Loop on trace length
		for(int i = 0; i + treeDepth < trace.size(); i++){
			/* On each iteration, instead of recreating the entire n-gram, remove
			 * its first element and add at its end the next one. It acts as a
			 * sliding windows. 
			 */
			ngram.remove(0);
			ngram.add(trace.get(i + treeDepth));
			addInTree(ngram, root); // Add n-gram in tree
			l=i;
		}
	
		// Few last n-grams at the end of the trace
		for(int i = 0; i < treeDepth -1 ; i++){
			ngram.remove(0);
			addInTree(ngram, root);
		}
		/*************************************/
		if (i2+1<traces.size()){
			//erreur
			for(int j = 0;	j + l  < trace.size(); j++){
			traces.get(i2+1).add(j, trace.get(j+l));
			}	}
	}
	
	/**
	 * Parses a trace and inserts all n-grams contained in it in the tree.
	 * For each n-gram inserted, increments its counter. 
	 * This version makes sure tree is sorted by decresing frequencies
	 * @param trace: trace parsed.
	 */
	public void makeNgramsOpti(Trace trace, TreeNode root){
		// Initialize n-gram. With the first sequence of the trace
		ArrayList<String> ngram = new ArrayList<String>();
		for(int i = 0; i < treeDepth; i++){
			ngram.add(trace.get(i));
		}
		addInTree(ngram, root); // Add n-gram in tree
		// Loop on trace length
		for(int i = 0; i + treeDepth < trace.size(); i++){
			/* On each iteration, instead of recreating the entire n-gram, remove
			 * its first element and add at its end the next one. It acts as a
			 * sliding windows. 
			 */
			ngram.remove(0);
			ngram.add(trace.get(i + treeDepth));
			addInTree(ngram, root); // Add n-gram in tree
		}
		// Few last n-grams at the end of the trace
		for(int i = 0; i < treeDepth -1 ; i++){
			ngram.remove(0);
			addInTree(ngram, root);
		}
	}

	/**
	 * Adds a n-gram and all its sub n-grams in the tree.
	 * Consists in a depth -parsing of the tree to find the
	 * entire n-gram, and incrementing the frequency for
	 * each node which is part of the n-gram.
	 * @param ngram: n-gram inserted in the tree
	 */
	public void addInTree(ArrayList<String> ngram, TreeNode root){
		TreeNode parentNode = root;
		TreeNode childNode = null;
		for(int i = 0; i < ngram.size(); i++){
			childNode = find(parentNode, ngram.get(i));
			if(childNode == null){
				// If node does not exist, create it and add it in the tree.
				childNode = new TreeNode(ngram.get(i));
				parentNode.add(childNode);
				nGramDistinct ++;
				nGramPerLen[i] ++;
			}
			childNode.incFreq(); // Increment apparition counter
			parentNode = childNode; // One step deeper in the tree
		}
	}
	


	/**
	 * Searches amoung the children of a node of there is one for which its object
	 * contains the String name as syscall name.
	 * @param parent: parent node for which we search a child containing the string.
	 * @param name: name of the syscall wanted
	 * @return: The node containing the name if there is one, null if there is not.
	 */
	public TreeNode find(TreeNode parent, String name){
		if(parent == null){
			return null;
		}
		TreeNode child;
		Iterator<TreeNode> i = parent.children();
		int index = 0;
		while(i.hasNext()){
			// Loop on children nodes
			child = i.next();
			if(child.compare(name)){
				// If syscall name matches, return this child
				child.setIndex(index);
				return child;
			}
			index++;
		}
		return null;
	}


	@Override
	public String getModelData() {
		String str = "Tree model \n " + 
				"Number of N-grams:" + nGramTotal + "\n "+
				"Number of Distinct N-grams: " + nGramDistinct +  "\n "+
				"Per size:\n";
		int j;
		for (int i = 0; i < treeDepth; i++){
			j = i + 1;
			str = str.concat("  Distinct "+j+"-grams:" + nGramPerLen[i] + "\n");
		}
		return "Tree n-gram";
	}

	/**
	 * Parse tree to create a list of ngrams in the model, sorted by frequency.
	 * @return list of pairs (nGram, frequency) sorted by frequencies
	 */
	public PairList parseTreeFrequences(){

		PairList nGramList = new PairList();
		parseNodeFrequences(nGramList, treeRoot, null);
		return nGramList;
	}

	/**
	 * Updates the list of ngrams in the model with ngrams in children nodes of a node.
	 * Works recursively.
	 * @param nGramList: updated list.
	 * @param parent: parent of which children are added
	 * @param current: current n-gram (before syscall of parent is added)
	 */
	public void parseNodeFrequences(PairList nGramList, TreeNode parent, String current){

		String str = null;
		if(!parent.getSyscall().equals(rootNodeName)){ // update n-gram name
			if(current== null){
				str = parent.getSyscall();
			}else{
				str = current.concat("," + parent.getSyscall());
			}
			nGramList.add(new Pair(parent.getFrequency(), str, parent.getAbnormality(0))); // add n-gram in list
		}

		Iterator<TreeNode> i = parent.children();
		while(i.hasNext()){
			parseNodeFrequences(nGramList, i.next(), str);
		}		
	}
	public void printFrequences(){

		PairList pairs = parseTreeFrequences();
		pairs.printData();
	}
	/**
	 * Returns an array containing frequences of ngrams, by decrasing order.
	 * @return
	 */
	public double[] getFrequences(){
		PairList pairs = parseTreeFrequences();
		ArrayList<Pair> list = pairs.getList();
		double[] ret = new double[list.size()];
		for(int i = 0; i < list.size(); i++){
			ret[i] = list.get(i).freq;
		}
		return ret;
	}

	/**
	 * Class containing n-gram + frenquencies pair
	 * @author trivh
	 *
	 */
	public class Pair{
		public int freq;
		public int anomaly;
		public String nGram;
		public Pair(int freq, String nGram, int anomaly){
			this.freq = freq; 
			this.nGram = nGram;
			this.anomaly = anomaly;
		}
		public String toString(){
			return ("("+ freq + "  ,  " + anomaly + "   ,   "  + nGram + ")");
		}
	}

	/**
	 * list which sorts automatically new entered values.
	 * @author trivh
	 *
	 */
	public class PairList{
		private ArrayList<Pair> pairs;
		public PairList(){
			pairs = new ArrayList<Pair>();
		}
		/**
		 * When pair is added, it is made sure that the frequency decrasing
		 * order of the list is preserved.
		 * @param pair
		 */
		public void add(Pair pair){
			if(pairs.size() == 0){
				pairs.add(pair);
				return;
			}			
			if(pairs.get(0).freq <= pair.freq){
				pairs.add(0, pair);
				return;
			}
			if(pairs.get(pairs.size()-1).freq >= pair.freq){
				pairs.add(pair);
				return;
			}
			int i, j, l;
			i = 0;
			j = pairs.size() - 1;
			while(i < j){
				l = (i+j)/2;
				if(pair.freq == pairs.get(l).freq){
					pairs.add(l, pair);
					return;
				}
				if(pair.freq < pairs.get(l).freq){
					i = Math.max(l, i+1);
				}else{
					j = Math.min(l, j-1);
				}
			}
			if(pairs.get(i).freq > pair.freq){
				pairs.add(i+1, pair);
			}else{
				pairs.add(i, pair);
			}
		}
		public void printData(){
			System.out.println("N-gram list :");
			for(int i = 0; i < pairs.size(); i++){
				System.out.println("  " + pairs.get(i).toString());
			}		
		}
		public ArrayList<Pair> getList(){return pairs;}
	}
	/**
	 * For each trace, gets ngrams (and store them as a tree for simplicity)
	 * and count their frequency.
	 * Once this is done, sums the frequency ponderated by corresponding abnormalities,
	 * and compare to a threshold.
	 */
	@Override
	public Boolean scanTraces(ArrayList<Trace> traces, int nGRAM_SIZE,
			double nGRAM_THRESHOLD, double tHRESHOLD_MODEL) {
		double[] ret = new double[traces.size()];
		// Initialize n-grams tree.
		TreeNode traceRoot;
		int nGramNumber = 0;
		int[] anomaly = null;
		//System.out.println("########## TREE MODEL REPORT ############");
		// Create n-grams and insert them in the tree.
		for(int i = 0; i < traces.size(); i++){
			traceRoot = new TreeNode(rootNodeName);
			makeNgrams(traces.get(i), traceRoot, i, traces);
			// calculate total number of ngrams.
			nGramNumber = traces.get(i).size() * treeDepth;
			nGramNumber = nGramNumber - (treeDepth * (treeDepth - 1)) / 2;
			anomaly = anomalyRate(traceRoot);
			/*
			System.out.print("TRACE  " + i + ":");
			for(int j = 0; j < anomaly.length; j++){
				System.out.println(anomaly_rates[j] + " score: " + anomaly[j]);
			}
			 */
			ret[i] = anomaly[0] / traces.get(i).size(); // here there is only one, so we take it
		}
		double nbr=0;
		for (int i = 0; i < ret.length; i++) {
			nbr=nbr+ret[i] ;
		}
		return (nbr>threshold);
	}

	/**
	 * Returns anomaly rates for a ngram tree. An entry in the array is the anomaly
	 * rate calculated with one formula.
	 * @param traceRoot
	 * @param nGramNumber
	 * @return
	 */
	public int[] anomalyRate(TreeNode traceRoot){
		int[] ret = new int[ANOMALIES_CALCULATORS];
		for(int i = 0; i < ret.length; i++){
			ret[i] = 0;
		}
		ret = anomalyTreeParse(ret, traceRoot, treeRoot);
		return ret;
	}

	/**
	 * Returns sum anomaly for a node and its children.
	 * @param anomaly: array of anomaly rates to update
	 * @param traceNode: node of trace ngram tree to parse
	 * @param modelNode: corresponding node in the ngram tree of the model
	 * if there is one, null if there is not.
	 * @return array of anomalies incremented with anomalies of this node and its children
	 */
	public int[] anomalyTreeParse(int[] anomaly,
			TreeNode traceNode, TreeNode modelNode){
		if(modelNode == null){ // not in model: max anomaly rate
			for(int i = 0; i < anomaly.length; i++){
				//System.out.println("ajoute " + setters.get(i).defaultAnomaly(nGramTotal));
				anomaly[i] = anomaly[i] + setters.get(i).defaultAnomaly(nGramTotal);
			}
			//System.out.println(anomaly[0]);
			//			System.out.println("Syscall: "+ traceNode.getSyscall()+ "--- Not in model");
		}else{ // in model: anomaly = n-gram anomaly in model * frequency in trace
			for(int i = 0; i < anomaly.length; i++){
				anomaly[i] = anomaly[i] + (modelNode.getAbnormality(i) * traceNode.getFrequency());;
			}
			//System.out.println("Syscall: "+ traceNode.getSyscall()+ "---" + modelNode.getSyscall());
		}
		// add anomaly of children
		Iterator<TreeNode> i = traceNode.children();
		TreeNode child;
		while(i.hasNext()){
			child = i.next();
			anomaly = anomalyTreeParse(anomaly, child, find(modelNode, child.getSyscall()));
		}
		return anomaly;
	}

	/**
	 * returns array containg numbers of ngrams.
	 * ret[0] is the number of 1-gram, ret[1] of 2-gram and so on
	 * @return
	 */
	public int[] getnGramNumbers(){
		return nGramPerLen;
	}

	
	public void sortTree(){
		sortTree(treeRoot);
	}
	/**
	 * Sorts the tree of which root is given.
	 * Root children are sorted by decrasing frequence, so on recursively.
	 * @param root: root tree to sort
	 */
	public void sortTree(TreeNode root){
		ArrayList<TreeNode> newChildrenlist = new ArrayList<TreeNode>();
		Iterator<TreeNode> it = root.children();
		TreeNode child;
		int i, j, l, freq;
		while(it.hasNext()){
			child = it.next();
			freq = child.getFrequency();
			if(newChildrenlist.size() == 0){
				newChildrenlist.add(child);
			}			
			else if(newChildrenlist.get(0).getFrequency() <= freq){
				newChildrenlist.add(0, child);
			}
			else if(newChildrenlist.get(newChildrenlist.size()-1).getFrequency()
					>= freq){
				newChildrenlist.add(child);
			}
			else{
				i = 0;
				j = newChildrenlist.size() - 1;
				while(i < j){
					l = (i+j)/2;
					if(freq == newChildrenlist.get(l).getFrequency()){
						newChildrenlist.add(l, child);
						return;
					}
					if(freq < newChildrenlist.get(l).getFrequency()){
						i = Math.max(l, i+1);
					}else{
						j = Math.min(l, j-1);
					}
				}
				if(newChildrenlist.get(i).getFrequency() > freq){
					newChildrenlist.add(i+1, child);
				}else{
					newChildrenlist.add(i, child);
				}
			}
		}

		root.setChildren(newChildrenlist);
		it = root.children();
		while(it.hasNext()){
			child = it.next();
			sortTree(child);
		}
	}
	
	/*
	 * Optimized method to scan traces. Model tree must be sorted before use
	 * (see sortTree method). Instead of building a n-gram tree for traces,
	 * search them one by one in the model. Since the model tree is sorted, 
	 * this should be pretty quick. 
	 * @param traces: scanned traces
	 * @return array containing anomality score of each trace
	 */
	/* NOT USED
	public double[] scanTraceOpti(ArrayList<Trace> traces){
		double[] ret = new double[traces.size()];
		// Initialize n-grams tree.
		int nGramNumber = 0;
		int[] anomaly = new int[setters.size()];
		for(int i = 0; i < setters.size(); i++){
			anomaly[i] = 0;
		}
		int[] ngramAnomaly = new int[setters.size()];
		ArrayList<String> ngram;
		Trace trace;
		TreeNode model = treeRoot;

		// Create n-grams and insert them in the tree.
		for(int i = 0; i < traces.size(); i++){
			ngram = new ArrayList<String>();
			trace = traces.get(i);
			// calculate total number of ngrams in the trace
			nGramNumber = traces.get(i).size() * treeDepth;
			nGramNumber = nGramNumber - (treeDepth * (treeDepth - 1)) / 2;		
			
			for(int j = 0; j < treeDepth; j++){
				ngram.add(trace.get(j));
				ngramAnomaly = model.nGramAnomaly(ngram, nGramNumber);
				for(int k = 0; k < anomaly.length; k++){
					anomaly[k] = anomaly[k] + ngramAnomaly[k];
				}
				
			}
			// Loop on trace length
			for(int j = 0; j + treeDepth < trace.size(); j++){
				/* On each iteration, instead of recreating the entire n-gram, remove
				 * its first element and add at its end the next one. It acts as a
				 * sliding windows. 
				 *
				ngram.remove(0);
				ngram.add(trace.get(j + treeDepth));
				ngramAnomaly = model.nGramAnomaly(ngram, nGramNumber);
				for(int k = 0; k < anomaly.length; k++){
					anomaly[k] = anomaly[k] + ngramAnomaly[k];
				}
			}
			// Few last n-grams at the end of the trace
			for(int j = 0; j < treeDepth -1 ; j++){
				ngram.remove(0);
				ngramAnomaly = model.nGramAnomaly(ngram, nGramNumber);
				for(int k = 0; k < anomaly.length; k++){
					anomaly[k] = anomaly[k] + ngramAnomaly[k];
				}
			}

			ret[i] = anomaly[0] / traces.get(i).size(); 
		}
		return ret;
	}
	
	*/
	
	/**
	 * Node of the tree used.
	 * It allows to sort the children at will, in order to optimize
	 * tree parsing.
	 * It contains name of the syscall, frequency of apparition of
	 * the ngram represented, abnormality rate, and a list of children.
	 * Its methods allow management of these attributes as well as tree parsing.
	 */
	public class TreeNode{
		private String syscall;
		private int frequency;
		private int[] abnormality;
		private int index;

		ArrayList<TreeNode> children;
		//private TreeNode parent;

		/**
		 * Constructor, setups counter to 0 and name to given value.
		 * Abnormality is set as 0 as well and should be calculated later, as
		 * it needs a count of total number of trace as well as final frequency of
		 * this one.
		 * @param syscall
		 */
		public TreeNode(String syscall){
			this.syscall = syscall;
			children = new ArrayList<TreeNode>();
			frequency = 0;
			//parent = null;
			index = -1;
		}
		/** Increments the frequency */
		public void incFreq(){
			frequency++;
		}
		/** Setup abnormality. As we can use different methods to calculate
		 * an anomaly rate, creates an array of abnormality, one entry being
		 * the abnormality calculated with one method.
		 * @param total: total number of n-grams for all traces.
		 * */
		public void setupAbnormality(int total){
			abnormality = new int[ANOMALIES_CALCULATORS];
			if(syscall.equals(rootNodeName)){
				for(int i = 0; i < setters.size(); i++){
					abnormality[i] = 0;
					//System.out.println("Traité comme root");
				}
			}else{
				for(int i = 0; i < setters.size(); i++){
					abnormality[i] = setters.get(i).calculAnomaly(total, frequency);
					//System.out.println("Anomaly set to: "+ abnormality[i]);
				}
			}
			Iterator<TreeNode> i = children();
			TreeNode child;
			while(i.hasNext()){
				// Loop on children nodes
				child = i.next();
				child.setupAbnormality(total);
			}
		}
		/**
		 * Returns anomaly rate for a given n-gram. This function must be initially called
		 * for the root node of the tree.
		 * @param n-gram: n-gram for which anomaly rate is returned
		 * @param nGramNumber: number of ngrams in trace
		 * @return: anomaly rate of the n-gram.
		 */
		public int[] nGramAnomaly(ArrayList<String> ngramArg, int nGramNumber){
			@SuppressWarnings("unchecked")
			ArrayList<String> ngram = (ArrayList<String>) ngramArg.clone(); // we don't want to modify the actual ngram
			if(ngram.size() == 0){
				return this.getAbnormality(); // Case 1: we're at the n-gram node
			}
			Iterator<TreeNode> it = children();
			TreeNode child;
			while(it.hasNext()){
				child = it.next();
				if(child.compare(ngram.get(0))){
					ngram.remove(0);
					return child.nGramAnomaly(ngram, nGramNumber); // Case 2: next n-gram syscall is in children
				}
			}
			return defaultAnomaly(nGramNumber); //Case 3: n-gram not in model

		}

		/*
		 * Getters
		 */
		public String getSyscall(){return syscall;}
		public int getFrequency(){return frequency;}
		public int getAbnormality(int i){return abnormality[i];}
		public int[] getAbnormality(){return abnormality;}
		//public TreeNode getParent(){return parent;}
		public Iterator<TreeNode> children(){return children.iterator();}
		public int getIndex(){return index;}

		/*
		 * Setters
		 */
		//public void setParent(TreeNode parent){this.parent = parent;}
		public void setChildren(ArrayList<TreeNode> children){
			this.children = children;
		}
		public void setIndex(int index){this.index = index;}

		/**
		 * Compare a syscall name to the one of this object
		 * @param sys: syscall name compared
		 * @return true if they are the same, false otherwise.
		 */
		public boolean compare(String sys){
			return syscall.equals(sys);
		}

		/**
		 * Adds a child in children list. Also adds itself as parent
		 * of the child.
		 * @param child
		 */
		public void add(TreeNode child){
			children.add(child);
			//child.setParent(this);
		}		
		/**
		 * Returns array of default anormalies; one for each anomaly rate calculation method.
		 * @return
		 */
		public int[] defaultAnomaly(int nGramNumber){
			int[] ret = new int[setters.size()];
			for(int i = 0; i < setters.size(); i++){
				ret[i] = setters.get(i).defaultAnomaly(nGramNumber);
			}
			return ret;
		}
	}

	/**
	 * Interface implemented by classes which calculate anomaly rate.
	 * @author trivh
	 *
	 */
	public interface AnomalyRate{
		public int calculAnomaly(int total, int freq); // anomaly rate of ngram present in model
		public int defaultAnomaly(int total); // anomaly rate by default
	}
	/**
	 * Anomaly rate  = log (totalNgram / this ngram)
	 * @author trivh
	 *
	 */
	public class LogAnomaly implements AnomalyRate{
		@Override
		public int calculAnomaly(int total, int freq) {
			//System.out.println("Log Anom : "+Math.log((double)nGramTotal/(double)freq));
			//System.out.println(total + "/" + freq);
			return (int) Math.round(Math.log((double)total/(double)freq));


		}

		@Override
		public int defaultAnomaly(int total) {
			//System.out.println("default");
			return (int) Math.round(Math.log((double)total/(double)1)*10);

		}

	}


	@Override
	public Boolean scanTracesB(ArrayList<Trace> traces, int nGRAM_SIZE,
			double nGRAM_THRESHOLD, double tHRESHOLD_MODEL) {
		// TODO Auto-generated method stub
		return null;
	}



}
