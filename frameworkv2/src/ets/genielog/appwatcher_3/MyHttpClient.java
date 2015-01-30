package ets.genielog.appwatcher_3;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

/**
 * Class extended to access php pages used to make DB requests.
 * Each class extended this one is dedicated to a type of SQL request,
 * each corresponding to a php page.
 * @author trivh
 *
 */
public abstract class MyHttpClient implements Runnable{
	
	// PHP host.
	//private static String host = "http://10.192.168.13/appWatcher/";
	private static String host ="http://10.180.121.16appWatcher/";
	//private static String host ="http://208.92.17.88/appWatcher/";
	//private static String host = "localhost";
	// Full address including host + page.
	private String address;//="http://10.180.121.11/appWatcher/traceinsert.php";
	// Pairs parameters of HTTP request.  
	private ArrayList<NameValuePair> nameValuePairs;

	/**
	 * Constructor, initializes pair list.
	 */
	public MyHttpClient(){
		nameValuePairs = new ArrayList<NameValuePair>();
	}
	/**
	 * Runs the request with attributes entered previously.
	 * Those attributes should be set in extended constructors.
	 */
	@Override
	public void run() {
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(address);
		try {
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = client.execute(post);
			onRequestReturn(response.getEntity().getContent());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Method called once the request result returns. Depends on the request type.
	 */
	public abstract void onRequestReturn(InputStream is);
	
	
	/**
	 * Getter for host
	 * @return
	 */
	public static String getHost(){
		return host;
	}
	/**
	 * Adds a pair in list of PHP parameters.
	 * @param pair
	 */
	public void addPair(BasicNameValuePair pair){
		nameValuePairs.add(pair);
	}
	
	/**
	 * Remove pair which name is given
	 * @param name
	 */
	public void removePair(String name){
		for(int i = 0; i < nameValuePairs.size();i++){
			if(nameValuePairs.get(i).getName().equals(name)){
				nameValuePairs.remove(i);
				return;
			}
		}
	}
	/**
	 * page setter
	 * @param p
	 */
	public void setAddress(String p){
		address = p;
	}
	
	
} 