package ets.genielog.appwatcher_3;

import java.util.ArrayList;


/**
 * Saves models in Hashtables. There is one hashtable for each model type.
 * In hashables, the key is the app name and the value is the model.
 * @author trivh
 *
 */
public class ModelListManager extends ModelManager{
	
	/** Lists containing models */
	private ArrayList<ModelList> modelLists;

	/**
	 * Constructor. Initializes model lists.
	 */
	public ModelListManager(){
		modelLists = new ArrayList<ModelList>();
		for (int i = 0; i < modelTypes.length; i++){
			modelLists.add(new ModelList(i));
		}
	}
	/**
	 * Adds a model in the list corresponding to its type. 
	 * @param model: model to add
	 * @return: true if an existing model is replaced
	 */
	@Override
	public boolean saveModel(Model model) {
		int type = model.getModelId();
		return modelLists.get(type).addModel(model);
	}

	/**
	 * Retrives a model from a list for an application and a model type
	 * @param apk: nampe of the application
	 * @param modelType: id of model type
	 * @return: found model. null if there is no model.
	 */

	@Override
	public Model getModel(String apk, int modelType, int sizengram,
			double nGRAM_THRESHOLD, double tHRESHOLD_MODEL) {
		return modelLists.get(modelType).getModel(apk);
		// TODO Auto-generated method stub
	}



}
