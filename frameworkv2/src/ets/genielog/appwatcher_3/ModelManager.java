package ets.genielog.appwatcher_3;

import java.util.ArrayList;




/**
 * Class used to interact with saved models.
 * It is extended to connect to a database or manage a list.
 * @author trivh
 *
 */
public abstract class ModelManager {


	/** Existing models ID */
	public static final int DEFAULT_MODEL = 0;
	public static final int TREE_MODEL = 1;
	public static final int NEW_MODEL = 2;
	public static final int HMM_MODEL = 3;

	/** Array of existing model types */
	public static Model[] modelTypes;


	/**
	 * Initializes model types array.
	 */
	public static void init(){
		//Possible model types list making
		modelTypes = new Model[4];
		modelTypes[DEFAULT_MODEL] = new DefaultModel("",2,0.1);
		modelTypes[TREE_MODEL] = new TreeModel("",2,0.1);
		modelTypes[NEW_MODEL] = new NewModel("",0.6,0.1);
		modelTypes[HMM_MODEL] = new HmmModel("",0.6,0.1);
		
	}
	

	/** Creates a new model with the data of a trace list.
	 * @param tHRESHOLD_MODEL 
	 * @param nGRAM_THRESHOLD 
	 * @param nGRAM_SIZE 
	 * 
	 * @param apk: package for which the model is made.
	 * @param traces: traces used to make the model
	 * @param modelType: id of model type used
	 * @return model created
	 */
	public Model makeModel(String apk, ArrayList<Trace> traces, int modelType, int nGRAM_SIZE, double nGRAM_THRESHOLD, double tHRESHOLD_MODEL){

		Model newModel;
		switch(modelType){
		case DEFAULT_MODEL:
			newModel = new DefaultModel(apk, nGRAM_SIZE,  tHRESHOLD_MODEL);
			break;
		case NEW_MODEL:
			newModel = new NewModel(apk, nGRAM_THRESHOLD,  tHRESHOLD_MODEL);
			break;
		case TREE_MODEL:
			newModel = new TreeModel(apk, nGRAM_SIZE,  tHRESHOLD_MODEL);
			break;
		case HMM_MODEL:
			newModel = new HmmModel(apk,  nGRAM_THRESHOLD, tHRESHOLD_MODEL);
			break;
		default: return null;
		}
		newModel.makeModel(traces);
		return newModel;
	}

	/** Saves a model 
	 * @param: saved model
	 * @return: true if a previously existing model is replaced*/
	public abstract boolean saveModel(Model model);
	
	/** Retrieves a model from stored models. 
	 * @param tHRESHOLD_MODEL 
	 * @param nGRAM_THRESHOLD 
	 * @param usedModel */
	public abstract Model getModel(String apk, int modelType, int usedModel, double nGRAM_THRESHOLD, double tHRESHOLD_MODEL);

	/** Updates a model with a list of new traces.
	 * Note: THIS DOES NOT SAVE it, simply returns an updated version of the model.
	 * @param tHRESHOLD_MODEL 
	 * @param nGRAM_THRESHOLD 
	 * @param usedModel 
	 * @param traces: new traces
	 * @param app: modelised application name
	 * @param type: type of model
	 * @return: updates model
	 */
	public Model updateModel(ArrayList<Trace> traces, String appname, int modelType, int NGRAM_SIZE, double nGRAM_THRESHOLD, double tHRESHOLD_MODEL) {
		Model model = getModel(appname, modelType, NGRAM_SIZE,nGRAM_THRESHOLD, tHRESHOLD_MODEL);
		if(model == null){return null;}
		/*//model.updateModel(traces);
		model.updatemodel(traces);
		//System.out.println("Warning: model update not implemented");
		return model;*/
		Model newModel;
		switch(modelType){
		case DEFAULT_MODEL:
			newModel = new DefaultModel(appname, NGRAM_SIZE, tHRESHOLD_MODEL);
			break;
		case NEW_MODEL:
			newModel = new NewModel(appname, nGRAM_THRESHOLD, tHRESHOLD_MODEL);
			break;
		case TREE_MODEL:
			newModel = new TreeModel(appname, NGRAM_SIZE, tHRESHOLD_MODEL);
			break;
		case HMM_MODEL:
			newModel = new HmmModel(appname, nGRAM_THRESHOLD, tHRESHOLD_MODEL);
			break;
		default: return null;
		}
		newModel.makeModel(traces);
		return newModel;
	}


		
	

}
