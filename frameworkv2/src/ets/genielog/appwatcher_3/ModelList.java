package ets.genielog.appwatcher_3;

import java.util.ArrayList;
import java.util.Hashtable;


/**
 * Class containing created model.
 * This is a temporary class that allow to store model during one runtime only.
 * Further version should use database to store models.
 * @author trivh
 */
public class ModelList {

	/** Id of model type contained in this list */
	private int modelTypeId;
	
	/** Each model is associated to the name of the application it modelises */
	private Hashtable<String, Model> list;
	
	/** Constructor initializes the list and sets the ID of stored models. */
	public ModelList(int modelType){
		modelTypeId = modelType;
		list = new Hashtable<String, Model>();
	}
	
	/** Add a model in the list.
	 * The model is the value in the hashtable, while the key is the name of
	 * the application modelised.
	 * Displays a notification if a previous model is replaced.
	 * @param model
	 * @return: true if an existing model is replaced
	 */
	public boolean addModel(Model model){
		// Check model ID.
		if(model.getModelId() != modelTypeId){
			System.out.println("ModelList error: can't add model. Wrong model type.");
			System.out.println("App name: " + model.getAppName());
			System.out.println("Added Model ID detected: " + model.getModelId());
			System.out.println("Model ID requiered: " + modelTypeId);
			return false;
		}
		
		Model previousModel = list.put(model.getAppName(), model);
		
		// Notification if previous model has been replaced
		if(previousModel != null){
			System.out.println("ModelList notif: model replaced:");
			System.out.println(previousModel.toString());
			return true;
		}	
		return false;
	}
	
	/**
	 * Returns the model for the given application.
	 * @param apk: application name
	 * @return model for this application, or null if there is not
	 */
	public Model getModel(String apk){
		return list.get(apk);
	}
	
	/**
	 * Retrives the model for corresponding application from the list,
	 * update it with data from argument trace, and replace it in the list.
	 * @param traces: list of traces
	 */
	public void updateModel(String apkname, ArrayList<Trace> traceList){
		Model currentModel =list.get(apkname);
		System.out.println("Waring: model updating not implemented");
		addModel(currentModel);
		
	}
	
	/**
	 * Returns true is there is a model entry for this apk name
	 * @param apkname
	 * @return
	 */
	public boolean isModel(String apkname){
		if(getModel(apkname) == null){
			return false;
		}
		return true;
	}
	
	
}
