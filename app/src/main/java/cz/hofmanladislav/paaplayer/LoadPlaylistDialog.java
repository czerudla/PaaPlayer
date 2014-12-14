package cz.hofmanladislav.paaplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

import java.util.ArrayList;

public class LoadPlaylistDialog extends DialogFragment {

    private ArrayList<PlaylistModel> playlists;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Načíst playlist");
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        String[] arrayOfPlaylistNames = new String[playlists.size()];
        for (int i = 0; i < playlists.size(); i++) {
            arrayOfPlaylistNames[i] = playlists.get(i).getName();
        }
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setItems(arrayOfPlaylistNames, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                loadPlaylistDialogListener.onDialogLoadPlaylistClick(i);
            }
        });

        builder.setNegativeButton("Storno", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                LoadPlaylistDialog.this.getDialog().cancel();
            }
        });

        return builder.create();
    }

    public void setPlaylists(ArrayList<PlaylistModel> playlists) {
        this.playlists = playlists;
    }

    public interface ILoadPlaylistDialogListener {
        public void onDialogLoadPlaylistClick(int position);
    }

    ILoadPlaylistDialogListener loadPlaylistDialogListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            loadPlaylistDialogListener = (ILoadPlaylistDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement loadPlaylistDialogListener");
        }
    }
}
