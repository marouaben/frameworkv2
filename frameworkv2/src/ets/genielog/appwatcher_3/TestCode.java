package ets.genielog.appwatcher_3;


import java.util.ArrayList;



public class TestCode {
	  
	public static Model model;
	public static int tracelen = 1200;
	public static String appname = "com.rovio.angrybirds";
	public static final int MODELS_DEPTH = 9; 
	public static SysCallFilter sysc = new SysCallFilter(new ArrayList<String>());
 
	/**
	 * @param args
	 */
	public static void runCode(MainActivity context) {

		/**
		 * @param args
		 */

		float time;

		ArrayList<Trace> traces;


		ArrayList<String> ignoredCalls = new ArrayList<String>();
		ignoredCalls.add("clock_gettime");

		SQL_Client sql  = new SQL_Client();

		traces = sql.selectReq(appname);
		/*
		ArrayList<Trace> scannedTraces;
		scannedTraces = sql.selectScanReq(appname, "TraceMal_2");
		scannedTraces = Trace.reSize(scannedTraces, 100);
		*/




		//traces = Trace.reSize(traces, 1200, syscf);
		// TREE MODEL 
		/*
		TreeModel model = new TreeModel("", MODELS_DEPTH);
		time = chronoModel(model, traces);
		System.out.println("Tree model creation time: "+ time + "s");
		scanReturn = chronoScan(model, scannedTraces);
		System.out.println("Tree model scan time: "+ scanReturn.getSecond() + "s");
		 

		//SORTED TREE MODEL
		/*
		TreeModelSorted tsModel = new TreeModelSorted("", MODELS_DEPTH);
		time = chronoModel(tsModel,traces);
		System.out.println("Sorted Tree model creation time: "+ time + "s");
		scanReturn = chronoScan(tsModel, scannedTraces);
		System.out.println("Sorted Tree model scan time: "+ scanReturn.getSecond() + "s");

	 */
	
		 
		model = new TreeModelSorted("", MODELS_DEPTH);
		time = chronoModel(model, traces);
		System.out.println("Tree model creation time: "+ time + "s");
		
		context.runScanService();
		/*
		scanReturn = chronoScan(lpModel, scannedTraces);
		System.out.println("LP model scan time: "+ scanReturn.getSecond() + "s");
		*/
		
		

		 


	}

	public static void plotInt(ArrayList<Pair<int[], String>> pairs, String frameTitle){
		ArrayList<Pair<double[], String>> doublePairs = 
				new ArrayList<Pair<double[], String>>();
		double[] array;
		for(int i = 0; i < pairs.size(); i++){
			array = new double[pairs.get(i).getFirst().length];
			for(int j = 0; j < pairs.get(i).getFirst().length; j++){
				array[j] = (double) pairs.get(i).getFirst()[j];
			}
			doublePairs.add(new Pair<double[], String>(array, pairs.get(i).getSecond()));
		}
		plot(doublePairs, frameTitle);
	}

	public static void plot(ArrayList<Pair<double[], String>> pairs, String frameTitle){
		/*
		ArrayList<double[]> plots  = new ArrayList<double[]>();
		ArrayList<String> legend = new ArrayList<String>();
		for(int i = 0; i < pairs.size(); i++){
			plots.add(pairs.get(i).getFirst());
			legend.add(pairs.get(i).getSecond());
		}


		Plot2DPanel plotData = new Plot2DPanel();
		plotData.addLegend("SOUTH");
		double[] x = new double[plots.get(0).length];
		for(int i = 0; i < x.length; i++){ // x axis array and get max values
			x[i] = i; 
		}

		for(int i = 0; i < plots.size(); i++){
			plotData.addLinePlot(legend.get(i), x, plots.get(i));
		}

		// put the PlotPanel in a JFrame like a JPanel
		JFrame frameData = new JFrame(frameTitle);
		frameData.setSize(600, 600);
		frameData.setContentPane(plotData);
		frameData.setVisible(true);
		 */
	}


	public static void plotNormalized(ArrayList<Pair<double[], String>> pairs, String frameTitle){
		ArrayList<double[]> plots  = new ArrayList<double[]>();
		ArrayList<String> legend = new ArrayList<String>();
		for(int i = 0; i < pairs.size(); i++){
			plots.add(pairs.get(i).getFirst());
			legend.add(pairs.get(i).getSecond());
		}

		double[] ymax = new double[plots.get(0).length];
		double[] x = new double[plots.get(0).length];
		for(int i = 0; i < x.length; i++){ // x axis array and get max values
			x[i] = i; 
			for(int j = 0; j < plots.size(); j++){
				ymax[j] = Math.max(ymax[j], plots.get(j)[i]);
			}
		}
		for(int i = 0; i < x.length; i++){ // normalize
			for(int j = 0; j < plots.size(); j++){
				plots.get(j)[i] = plots.get(j)[i] / ymax[j];
			}
		}
		/*
		Plot2DPanel plotData = new Plot2DPanel();
		plotData.addLegend("SOUTH");
		/*
		for(int i = 0; i < plots.size(); i++){
			plotData.addLinePlot(legend.get(i), x, plots.get(i));
		}

		// put the PlotPanel in a JFrame like a JPanel
		JFrame frameData = new JFrame(frameTitle);
		frameData.setSize(600, 600);
		frameData.setContentPane(plotData);
		frameData.setVisible(true);
		 */
	}

	/**
	 * Creates model and return approximate execution time
	 */
	public static float chronoModel(Model model, ArrayList<Trace> traces){
		long begin = System.nanoTime();
		model.makeModel(traces);
		long end = System.nanoTime();
		float time = ((float) (end-begin)) / 1000000000f;
		return time;
	}

	/**
	 * Creates model and return approximate execution time
	 */


}
