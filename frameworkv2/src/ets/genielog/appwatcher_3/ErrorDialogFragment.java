package ets.genielog.appwatcher_3;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

/**
 * Class used to create dialog boxes displaying error messages.
 * @author trivh
 *
 */
public class ErrorDialogFragment extends DialogFragment{
	
	/** Possible dialogs ID */
	public final static int NO_MODEL_ID = 0;
	
	/** Current dialog ID */
	public int error_id;
	
	/** Set id of displayed message */
	public void setErrorId(int id){
		error_id = id;
	}
	
	/**
	 * Returns a dialog corresponding to the id attribute of the class.
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		switch(error_id){
		case NO_MODEL_ID:
			builder.setMessage(R.string.dialog_no_model);
	
			break;
		default:
			System.out.println("Error invalid error dialog ID");
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
