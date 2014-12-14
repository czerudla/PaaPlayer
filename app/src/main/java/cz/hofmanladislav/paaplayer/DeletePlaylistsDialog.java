package cz.hofmanladislav.paaplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

import java.util.ArrayList;

public class DeletePlaylistsDialog extends DialogFragment {

    private ArrayList<PlaylistModel> playlists;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Odstranit vybrané playlisty");
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        String[] arrayOfPlaylistNames = new String[playlists.size()];
        for (int i = 0; i < playlists.size(); i++) {
            arrayOfPlaylistNames[i] = playlists.get(i).getName();
        }
        final ArrayList selectedItems = new ArrayList();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setMultiChoiceItems(arrayOfPlaylistNames, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {
                if (isChecked) {
                    selectedItems.add(which);
                } else if (selectedItems.contains(which)) {
                    selectedItems.remove(Integer.valueOf(which));
                }
            }
        });
        builder.setPositiveButton("Odstranit vybrané", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deletePlaylistsDialogListener.onDialogDeletePlaylistsClick(selectedItems);
            }
        });
        builder.setNegativeButton("Storno", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                DeletePlaylistsDialog.this.getDialog().cancel();
            }
        });

        return builder.create();
    }

    public void setPlaylists(ArrayList<PlaylistModel> playlists) {
        this.playlists = playlists;
    }

    public interface IDeletePlaylistsDialogListener {
        public void onDialogDeletePlaylistsClick(ArrayList selectedItems);
    }

    IDeletePlaylistsDialogListener deletePlaylistsDialogListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            deletePlaylistsDialogListener = (IDeletePlaylistsDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement deletePlaylistDialogListener");
        }
    }
}
