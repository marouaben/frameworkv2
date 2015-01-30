package ets.genielog.appwatcher_3;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;




/**
 *	Classe client de la base de donnée
 * */

public class SQL_Client {

	private MyHttpSelectTrace http;



	/**
	 * Constructeur. Initialise le driver.
	 */
	public SQL_Client(){

	}
 
	public ArrayList<Trace> selectReq(String appname){
		ArrayList<Trace> traces = null;
		http = new MyHttpSelectTrace(appname, "TraceGalaxyBenin");
		//Création d'un objet Statement
		Thread thread = new Thread(http);
		thread.start();
		try { 
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
		//String ret = http.getRequestReturn();

		//L'objet ResultSet contient le résultat de la requête SQL
		/*
		traces = new ArrayList<Trace>();
		Gson gson = new Gson();
		traces = gson.fromJson(ret, new ArrayList<Trace>().getClass());*/
		traces = http.getTraces();

		return traces;

	}

	public ArrayList<Trace> selectScanReq(String appName, String table){
		ArrayList<Trace> traces = null;
		http = new MyHttpSelectTrace(appName, table);
		//Création d'un objet Statement
		Thread thread = new Thread(http);
		thread.start();
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
		traces = http.getTraces();

		return traces;
	}

	public ArrayList<Trace> selectScanReq(String appName, String table, int limSup){
		ArrayList<Trace> traces = null;
		http = new MyHttpSelectTrace(appName, table, limSup);
		//Création d'un objet Statement
		Thread thread = new Thread(http);
		thread.start();
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
		traces = http.getTraces();

		return traces;
	}

	public ArrayList<Trace> selectScanReq(String appName, String table, int limInf, int limSup){
		ArrayList<Trace> traces = null;
		http = new MyHttpSelectTrace(appName, table, limInf,  limSup);
		//Création d'un objet Statement
		Thread thread = new Thread(http);
		thread.start();
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
		traces = http.getTraces();

		return traces;
	}

}
