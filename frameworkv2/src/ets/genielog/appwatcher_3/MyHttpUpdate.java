package ets.genielog.appwatcher_3;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.message.BasicNameValuePair;


/**
 * Class used to update models in DB.
 * request:
 * UPDATE Models SET ModelData='data'
 * 			 WHERE ApplicationName ='appName'
 * 				AND ModelType = 'modelType';
 * Values are set in constructor.
 * @author trivh
 *
 */
public class MyHttpUpdate extends MyHttpClient{


	private static String page = "update.php";
	/**
	 * Constructor setups values of th request
	 * @param appName: name of application modelized.
	 * @param modelType: Model type
	 * @param modelData: serialized Model object.
	 */
	public MyHttpUpdate(String appName, String modelType, String modelData){
		super();
		addPair(new BasicNameValuePair("AppName", appName));
		System.out.println("AppName: "+appName);
		addPair(new BasicNameValuePair("ModelType", modelType));
		System.out.println("ModelType: "+modelType);
		addPair(new BasicNameValuePair("ModelData", modelData));
		System.out.println("ModelData: "+modelData.charAt(3));
		setAddress(getHost() + page);
	}
	/**
	 * Prints a success or error message depending on the success of the request.
	 */
	@Override
	public void onRequestReturn(InputStream is) {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line = "";
		String res = "";
		try {
			while ((line = br.readLine())!= null) {
				res = res.concat(line);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (res.equals("1")){
			System.out.println("Model Insert success");
		}else{
			System.out.println("Model Insert failure");
		}
	}

}
