package ets.genielog.appwatcher_3;

import android.app.Activity;
import android.os.Handler;
import android.widget.Toast;
import ets.genielog.appwatcher_3.profiling.core.State;

/**
 * Class containing common methods, routines and attributes of all our
 * activities. All activities of this program extends it.
 * 
 * @author trivh
 * 
 */
public abstract class ActivityModel extends Activity {

	/** AppWatcher central Object, shared between activities */
	public static AppWatcher appwatcher;

	/********** PROFILING ***********/
	public static State profiler;
	/********************************/

	/** Used to allow callback from seperate threads */
	protected Handler handler;

	/**
	 * Notify user by writing a message in a toast.
	 * 
	 * @param msg
	 *            : message to write.
	 */
	public void notif(String msg) {
		Toast toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
		toast.show();
	}

	/**
	 * Displays a Dialog prompting user to do something.
	 * 
	 * @param id
	 *            : id of the Dialog to display.
	 */
	public void prompt(int id) {
		PromptDialogFragment newFragment = new PromptDialogFragment();
		newFragment.setPromptId(id);
		newFragment.show(getFragmentManager(), "Prompt" + String.valueOf(id));
	}

	/**
	 * Displays an error message
	 * 
	 * @param id
	 *            : id of the Dialog to display.
	 */
	public void error(int id) {
		ErrorDialogFragment newFragment = new ErrorDialogFragment();
		newFragment.setErrorId(id);
		newFragment.show(getFragmentManager(), "Error" + String.valueOf(id));
	}
}
