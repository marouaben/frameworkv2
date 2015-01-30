package ets.genielog.appwatcher_3;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.message.BasicNameValuePair;


/**
 * Class used to directly insert trace into db for further and/or remote processing
 * @author trivh
 *
 */
public class MyHttpInsertTrace extends MyHttpClient{

	private static String page = "traceinsert.php";
	
	public MyHttpInsertTrace(String appName, String trace){
		super();
		addPair(new BasicNameValuePair("application", appName));
		System.out.println("insert trace for : "+appName);

		addPair(new BasicNameValuePair("trace", trace));

		setAddress(getHost() + page);
	}
	
	public void setTrace(String trace){
		removePair("trace");
		addPair(new BasicNameValuePair("trace", trace));
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
			System.out.println(" Insert Trace success");
		}else{
			System.out.println(" Insert Trace failure");
			System.out.println(res);
		}
	}

}
