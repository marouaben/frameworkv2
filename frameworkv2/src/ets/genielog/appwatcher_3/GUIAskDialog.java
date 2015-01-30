package ets.genielog.appwatcher_3;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class GUIAskDialog extends DialogFragment {
	
	private String question;
	private String[] choices;
	
	
	public void setQuestion (String question){this.question = question;}
	public void setChoices (String[] choices){this.choices = choices;}
	
	public Dialog onCreateDialog(Bundle savedInstanceState) {
       
		// 1. Instantiate an AlertDialog.Builder with its constructor
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// 2. Chain together various setter methods to set the dialog characteristics
		builder.setTitle(question)
		 .setSingleChoiceItems(choices, -1,
                      new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							
						}
               
                  
           });
		
        return builder.create();
    }

}
