package ets.genielog.appwatcher_3;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

/**
 * Class used to create dialog boxes prompting user to do something.
 * @author trivh
 *
 */
public class PromptDialogFragment extends DialogFragment{
	
	/** Possible dialogs ID */
	public final static int RUN_APP_ID = 0;
	
	/** Current dialog ID */
	public int prompt_id;
	
	/** Set id of displayed message */
	public void setPromptId(int id){
		prompt_id = id;
	}
	
	/**
	 * Returns a dialog corresponding to the id attribute of the class.
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		switch(prompt_id){
		case RUN_APP_ID:
			builder.setMessage(R.string.dialog_run_app);
	
			break;
		default:
			System.out.println("Error invalid prompt dialog ID");
		}
		
		/*
		builder.setMessage(R.string.dialog_fire_missiles)
		.setPositiveButton(R.string.fire, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// FIRE ZE MISSILES!
			}
		})
		.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// User cancelled the dialog
			}
		});*/
		// Create the AlertDialog object and return it
		return builder.create();

	}
}
