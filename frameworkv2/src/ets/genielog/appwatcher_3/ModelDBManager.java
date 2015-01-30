package ets.genielog.appwatcher_3;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;


/**
 * Saves models in a database. 
 * @author trivh
 *
 */
public class ModelDBManager extends ModelManager {

	int sizengram;
	double nGRAM_THRESHOLD;
	double tHRESHOLD_MODEL;

	@Override
	public boolean saveModel(Model model) {
		// is previous model?
		if(getModel(model.getAppName(), model.getClass().getName(), model.sizengram, model.nGRAM_THRESHOLD, model.tHRESHOLD_MODEL) != null){
			// yes : request update + return true	
			replaceModel(model);
			return true;
		}else{

			// no :create an entry for the model
			insertNewModel(model);
			return false;
		}


	}

	/**
	 * Retreives a specified model from DB.
	 */
	@Override
	public Model getModel(String apk, int modelType, int sizengram,
			double nGRAM_THRESHOLD, double tHRESHOLD_MODEL) {
		// TODO Auto-generated method stub
		Model model = modelTypes[modelType];
		return getModel(apk, model.getClass().getName(), sizengram,nGRAM_THRESHOLD,tHRESHOLD_MODEL);	}

	/**
	 * Serializes the model sent as parameter, then updates the DB
	 * replacing its entry data.
	 * @param model
	 */
	public void replaceModel(Model model){
		// Setup request parameters
		String apkName = model.getAppName();
		String modelType = model.getClass().getName();
		
		Gson gson = new Gson();
		String json = gson.toJson(model);

		// Create request object
	//	MyHttpUpdate http = new MyHttpUpdate(apkName, modelType, json);
		// Run it on separate thread
		//Thread thread = new Thread(http);
		//thread.start();
		/*********************************************************************/
		if(!new File("/mnt/sdcard/Models").exists())
        {
            // Créer le dossier avec tous ses parents
            new File("/mnt/sdcard/Models").mkdirs();
 
        }
		/**********************************************************/

		File fichier = new File("/mnt/sdcard/Models/"+apkName+"_"+modelType+"_SizeN"+model.sizengram+"_ThresN"+model.nGRAM_THRESHOLD+"_ThresMod"+model.tHRESHOLD_MODEL+".data");
		ObjectOutputStream oos;
		 
		if (! fichier.exists())
		{
			try {
				fichier.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		/***********************************************************************/
		try {
			//thread.join();
			/*******************************/
			oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(fichier)));
			oos.writeObject("\n");
			oos.writeObject(json);
			oos.close();
			/*******************************/
		} //*/
 catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Serializes the model parameter, then insert it in DB via a
	 * INSERT request via PHP page.
	 * @param model: model uploaded
	 */
	public void insertNewModel(Model model){
	
		// Setup request parameters
		String apkName = model.getAppName();
		String modelType = model.getClass().getName();

		Gson gson = new Gson();
		String json = gson.toJson(model);
		//String data=model.getModelData();
		
		/*********************************************************************/
		if(!new File("/mnt/sdcard/Models").exists())
        {
            // Créer le dossier avec tous ses parents
            new File("/mnt/sdcard/Models").mkdirs();
 
        }
		/**********************************************************/

		File fichier = new File("/mnt/sdcard/Models/"+apkName+"_"+modelType+"_SizeN"+model.sizengram+"_ThresN"+model.nGRAM_THRESHOLD+"_ThresMod"+model.tHRESHOLD_MODEL+".data");
		ObjectOutputStream oos;
		 
		if (fichier.exists())
		{
			try {
				FileWriter writer = new FileWriter(fichier,false); 
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else 	{
			try {
				fichier.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		/***********************************************************************/
		// Create request object
		//MyHttpInsert http = new MyHttpInsert(apkName, modelType, json);
		// Run it on separate thread
		//Thread thread = new Thread(http);
		//thread.start();
		try {
			//thread.join();
			/*******************************/
			oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(fichier)));
			oos.writeObject(json);
			//oos.writeObject(data);
			oos.close();
			/*******************************/
		} //*/
 catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
 
	/**
	 * Sends a SELECT request to DB via php on a separate thread.
	 * Wait for it to finish and retreives the received model.
	 * @param apk: name modelised application.
	 * @param modelType: type of model
	 * @return model in DB. null if there is not
	 */
	public Model getModel(String apk, String modelType, int sizengram, double nGRAM_THRESHOLD,double tHRESHOLD_MODEL){
		//MyHttpSelect http = new MyHttpSelect(apk, modelType);
		
		//Thread thread = new Thread(http);
		//thread.start();
		/*try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}*/
		String reqReturn =null ;
		//= http.getRequestReturn();
		/*********************************************************************/
		if(!new File("/mnt/sdcard/Models").exists())
        {
            return null;
        }
		/**********************************************************/

		File fichier = new File("/mnt/sdcard/Models/"+apk+"_"+modelType+"_SizeN"+sizengram+"_ThresN"+nGRAM_THRESHOLD+"_ThresMod"+tHRESHOLD_MODEL+".data");
		ObjectInputStream ois;
		
		if (! fichier.exists())
			
		{		System.out.println("fichier1 "+fichier.getName());
			return null;
		}else {
			System.out.println("fichier "+fichier.getName());
		}
		/***********************************************************************/
		try {
			//thread.join();
			/*******************************/
			ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(fichier)));
		//	reqReturn = (Livre)ois.read;
			 reqReturn = (String) ois.readObject();
			ois.close();

			/*******************************/
		} //*/
 catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		Model model = null;
		if(reqReturn != null){
			Gson gson = new Gson();
			try {
				model = (Model) gson.fromJson(reqReturn, Class.forName(modelType));
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}else{
			model = null;
			System.out.println("null");
		}
		
		
		return model;
	}







}
