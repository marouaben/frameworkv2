package ets.genielog.appwatcher_3;

import java.io.Serializable;
import java.util.ArrayList;




/**
 * Parent class for model types.
 * @author trivh
 *
 */
public abstract class Model implements Serializable {

	private static final long serialVersionUID = -1527158782915046217L;

	// Name of modelised appication.
	private String appname;
	
	// ID of model type
	private int modelId;
	int sizengram;
	double nGRAM_THRESHOLD;
	double tHRESHOLD_MODEL;
	
	/** Constructor initilalises attribute. ModelID should be the same for each 
	 * models of the same subclass.
	 * @param appname
	 * @param modelID
	 */
	public Model(String appname, int modelID, int sizengram,
			double nGRAM_THRESHOLD, double tHRESHOLD_MODEL) {
		this.setAppname(appname);
		this.modelId = modelID;
		this.nGRAM_THRESHOLD=nGRAM_THRESHOLD;
		this.tHRESHOLD_MODEL=tHRESHOLD_MODEL;
		this.sizengram=sizengram;
	}


	public Model(String appname2, int sizengram,
			double nGRAM_THRESHOLD, double tHRESHOLD_MODEL) {
		// TODO Auto-generated constructor stub
		this.setAppname(appname2);
		this.nGRAM_THRESHOLD=nGRAM_THRESHOLD;
		this.tHRESHOLD_MODEL=tHRESHOLD_MODEL;
		this.sizengram=sizengram;

	}

	/**
	 * Creates a model from a trace list
	 * @param traces
	 */
	public abstract void makeModel(ArrayList<Trace> traces);
	
	/**
	 * Return a string containing data about the model
	 * @return
	 */
	public abstract String getModelData();
	
	/**
	 * Performs the scan of a trace list. 
	 * @param tHRESHOLD_MODEL 
	 * @param nGRAM_THRESHOLD 
	 * @param nGRAM_SIZE 
	 * @param traces: traces of scanned application
	 * @return an array containing anomaly score for each trace
	 */
	public abstract Boolean scanTraces(ArrayList<Trace> traces, int nGRAM_SIZE, double nGRAM_THRESHOLD, double tHRESHOLD_MODEL);
	
	public abstract Boolean scanTracesB(ArrayList<Trace> traces, int nGRAM_SIZE, double nGRAM_THRESHOLD, double tHRESHOLD_MODEL);

	public String getAppName(){return getAppname();}
	public int getModelId(){return modelId;}


	public String getAppname() {
		return appname;
	}


	public void setAppname(String appname) {
		this.appname = appname;
	}

}
