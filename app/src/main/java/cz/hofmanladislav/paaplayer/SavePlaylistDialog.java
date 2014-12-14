package cz.hofmanladislav.paaplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;

public class SavePlaylistDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Uložit playlist");
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_save_playlist, null))
                .setPositiveButton("Uložit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Dialog f = (Dialog) dialog;
                        EditText edit = (EditText)f.findViewById(R.id.playlistName);
                        savePlaylistDialogListener.onDialogSavePlaylistClick(edit.getText().toString());
                    }
                })
                .setNegativeButton("Storno", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SavePlaylistDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    public interface ISavePlaylistDialogListener {
        public void onDialogSavePlaylistClick(String playlistName);
    }

    ISavePlaylistDialogListener savePlaylistDialogListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            savePlaylistDialogListener = (ISavePlaylistDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement savePlaylistDialogListener");
        }
    }
}
