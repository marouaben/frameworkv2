package ets.genielog.appwatcher_3;


import java.util.ArrayList;
import java.util.Hashtable;

public class Extract {
	ArrayList<Trace> traces;
	ArrayList<Pair<Integer, ArrayList<Pair<Integer, String>>>> model;
	Hashtable<String, Integer> One;
	Hashtable<Integer, Hashtable<String, Integer>> VLG;

	double alfa;

	ArrayList<Pair<String, String>> UniquePair = new ArrayList<Pair<String, String>>();

	Pair<String, String> Pair;
	
public Extract (ArrayList<Trace> traces2, double alfa){
	this.traces=traces2;
	this.alfa=alfa;
	One= new Hashtable<String, Integer>();;
	VLG= new 	Hashtable<Integer, Hashtable<String, Integer>>();
	
	int sizeTraces=0;	
	for (int i = 0; i < traces.size(); i++) {// (int i = 0; i < 70; i++) {
		sizeTraces=sizeTraces+traces.get(i).sizeID();	
		for (int j = 0;  j < traces.get(i).sizeID() ; j++) {		
			add(Integer.toString(traces.get(i).getID(j)));		}	}
}


private void add(String p) {	
	if (One.containsKey(p)) {
		int freq=One.get(p);
		One.put(p, freq+1);	
		
	}else {
		One.put(p.toString(), 1);
	}	
}

private void addG(Hashtable<String, Integer> newg, String p) {	
	if (newg.containsKey(p)) {
		int freq=newg.get(p);
		newg.put(p, freq+1);	
		
	}else {
		newg.put(p.toString(), 1);
	}	
}
public Hashtable<Integer, Hashtable<String, Integer>> processing() {
	// TODO Auto-generated method stub
	VLG= new Hashtable<Integer, Hashtable<String,Integer>>();
	int k=1;
	Hashtable<String, Integer> current =One;
	Hashtable<String, Integer> newG = getG(k+1);

	while((current.size()!=0)){
		Hashtable<String, Integer> tmp = new Hashtable<String, Integer>();

		VLG.put(k, current);
			for ( String i : current.keySet()) {
				for ( String j : current.keySet()) {

					if (!j.equalsIgnoreCase(i)) {

						String newk =check(i,j);
						//System.out.println("newk "+newk);
						if (newG.containsKey(newk)) {
						//	System.out.println("current.get(i) "+current.get(i)+" current.get(j) "+current.get(j)+" newG.get(newk) "+newG.get(newk)+
							//		"  compare "+compare(newG.get(newk),current.get(i), current.get(j)));
							if (!compare(newG.get(newk),current.get(i), current.get(j))) {
								
								tmp.put(newk, newG.get(newk));
							}
						}
					}
				}
			}
		k++;
		current=tmp;
		newG=getG(k+1);
	}
	
	Display(VLG);
	return VLG;
}


private String check(String i, String j) {
	// TODO Auto-generated method stub
	String gram="";
	if (last(i).equalsIgnoreCase(first(j))){
		gram=i+";"+j.substring(j.length()-1);
		
	}
	return gram;
}

private String first(String i) {
	// TODO Auto-generated method stub
	String f="";
	if (!i.contains(";")) {
		f="&";
	}else {
		String[] n=i.split(";");
		int end=n[n.length-1].length();
		f=i.substring(0,i.length()-end-1);
	}
	//System.out.println("j "+i+"\t first "+f);
	return f;
}

private String last(String i) {
	// TODO Auto-generated method stub
	String l="";
	if (!i.contains(";")) {
		l="&";
	}else {
		int bg=i.split(";")[0].length()+1;
		l=i.substring(bg);
	}
	//System.out.println("i "+i+"\t last "+l);
	return l;
}


private Hashtable<String, Integer> getG(int s2) {

	// System.out.println("-----------------------s: "+s2+"--------");
		Hashtable<String, Integer> freq= new Hashtable<String, Integer>();
		for(int i=0; i<traces.size(); i++){
		//	 System.out.println("*****i: "+i+"*******");
			if ((i+1) < traces.size()) {
				int c=concatT( i, s2);
				// System.out.println("traces.get(i):  "+traces.get(i).toString());
				proc(i, s2,freq, c );
				int si=traces.get(i).sizeID();
				for (int u = 0; u < c; u++) {
					traces.get(i).removeID(si-u-1);
				}
			}
			else {
				proc(i, s2,freq, s2-1);
			}
				// System.out.println("traces.get(i):  "+traces.get(i).toString());
		}

	return freq;

}

private void proc(int i, int s2, Hashtable<String, Integer> freq, int c) {
	// TODO Auto-generated method stub
	for(int j=0; j<traces.get(i).sizeID()-s2+1; j++){
		String p=Integer.toString(traces.get(i).getID(j));
		for (int j2 = j+1; j2 < j+c+1; j2++) {
			p=p+";"+traces.get(i).getID(j2);
		}
		
		// System.out.println("pos: "+j+"\t p: "+p);
		addG(freq, p);
		}
}


private int concatT( int i, int s2) {
	// TODO Auto-generated method stub
	//System.out.println("size "+traces.get(i+1).sizeID());
int c =0;
	for (int u = 0; u < s2-1; u++) {
		if (u<traces.get(i+1).sizeID()) {
				int a =traces.get(i+1).getID(u);
				traces.get(i).addID(a);
				c++;
		}
	}
	 return c;
}




private boolean compare(Integer newp, Integer first, Integer second) {
	// TODO Auto-generated method stub
		Boolean ok=false;
		double min;
		if ((newp!=null)&&(first!=null)&&(second!=null)) {
			/**System.out.println("first\t"+first);
			System.out.println("second\t"+second);
			System.out.println("new\t"+newp);**/

		
			if(first>second){
				min=(double)second;
			}else {
				min=(double)first;
			}
			double res=alfa * min;
			
	/*	if (((double)newp) > res){
			ok=true;
		}*/
		int retval = Double.compare((double)newp, res);
	    
	     if(retval > 0) {
	       // System.out.println("newp is greater than res");
	    	ok=true;
	     }
	     else if(retval < 0) {
	     //   System.out.println("newp is less than res");
	     }
	     else {
	     //   System.out.println("newp is equal to res");
	     }
		}
		//System.out.println("ok\t"+ok);
		return !ok;
}
	public static void Display(Hashtable<Integer, Hashtable<String, Integer>> total) {

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

	}

}