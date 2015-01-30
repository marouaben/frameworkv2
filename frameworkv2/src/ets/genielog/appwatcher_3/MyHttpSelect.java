package ets.genielog.appwatcher_3;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import org.apache.http.message.BasicNameValuePair;


/**
 * Class allowing to do a SELECT request in the DB.
 * request:
 * 	SELECT ModelData FROM Models WHERE ModelType = 'modeltype' 
 * 			AND ApplicationName = 'applicationname'
 * modeltype and applicationname are the parameters of the constructor.
 * @author trivh
 *
 */
public class MyHttpSelect extends MyHttpClient{

	private static String page = "select.php";
	
	private String requestReturn = null;
	
	/**
	 * Constructor: adds PHP request parameters
	 * @param appName: application name
	 * @param className: model type name
	 */
	public MyHttpSelect(String appName, String className){
		super();
		addPair(new BasicNameValuePair("AppName", appName));
		System.out.println("AppName: "+appName);

		//String[] separation = className.split("\\.");
		addPair(new BasicNameValuePair("ModelType", className));
		System.out.println("ModelType: "+ className);
		setAddress(getHost() + page);
		
	}

	/**
	 * Convert an inputstream to a string and sets it as requestReturn attribute.
	 */
	@Override
	public void onRequestReturn(InputStream is) {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		System.out.println("Entering onRequestReturn in MyHttpSelect");
		String line = "";
		String res = "";
		try {
			while ((line = br.readLine())!= null) {
				res = res.concat(line);
			}
			br.close();
			if(res.equals("")){
				requestReturn = null;
			}else{
				requestReturn = res;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	/** Getter*/
	public String getRequestReturn(){
		return requestReturn;
	}

}


