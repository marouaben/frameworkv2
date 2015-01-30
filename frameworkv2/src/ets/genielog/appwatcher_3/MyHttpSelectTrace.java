package ets.genielog.appwatcher_3;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.message.BasicNameValuePair;


import com.google.gson.Gson;


public class MyHttpSelectTrace extends MyHttpClient{
	
	private static String page = "selectTrace.php";
	
	private ArrayList<Trace> traces;
	private String requestReturn = null;
	/**
	 * @param args
	 */
	public MyHttpSelectTrace(String appName, String table){
		super();
		addPair(new BasicNameValuePair("table", table));
		addPair(new BasicNameValuePair("AppName", appName));
		setAddress(getHost() + page);
	}
	
	public MyHttpSelectTrace(String appName, String table, int max){
		super();
		String maxstring = String.valueOf(max);
		addPair(new BasicNameValuePair("table", table));
		addPair(new BasicNameValuePair("AppName", appName));
		addPair(new BasicNameValuePair("max", maxstring));
		setAddress(getHost() + page);
	}

	public MyHttpSelectTrace(String appName, String table, int min, int max){
		super();
		String maxstring = String.valueOf(max);
		String minstring = String.valueOf(min);
		addPair(new BasicNameValuePair("table", table));
		addPair(new BasicNameValuePair("AppName", appName));
		addPair(new BasicNameValuePair("min", minstring));
		addPair(new BasicNameValuePair("max", maxstring));
		setAddress(getHost() + page);
	}
	
	/**
	 * Convert an inputstream to a string and sets it as requestReturn attribute.
	 */
	@Override
	public void onRequestReturn(InputStream is) {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		System.out.println("Entering onRequestReturn in MyHttpSelectTrace");
		
		String line = "";
		String res = "";
		
		Trace trace;
		Gson gson = new Gson();
		traces = new ArrayList<Trace>();
		
		try {
			while ((line = br.readLine())!= null) {
				res = res.concat(line);
				String[] test = line.split("<br />");
				for(int i = 0; i < test.length; i++){
					trace = gson.fromJson(test[i],  new Trace().getClass());
					traces.add(trace);
				}
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
	public ArrayList<Trace> getTraces(){return traces;}

}
