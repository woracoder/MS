package edu.buffalo.cse;

import android.content.DialogInterface;

import android.app.AlertDialog;
import android.os.Bundle;
import android.app.Dialog;
import android.app.DialogFragment;

public class DownloadCompletedDialog extends DialogFragment{

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(Registration.getMainActivity());
        builder.setMessage("Downloading Of File Completed Successfully")
               .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // FIRE ZE MISSILES!
                   }
               })
               .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
