package cz.hofmanladislav.paaplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class FileSystemAdapter extends ArrayAdapter<HashMap<String, String>> /*implements View.OnClickListener*/ {

    Context myContext;
    FileSystemUtils fsUtils = new FileSystemUtils();
    ArrayList<HashMap<String, String>> songsList;

    public FileSystemAdapter(Context context, int textViewResourceId, ArrayList<HashMap<String, String>> songsList) {
        super(context, textViewResourceId, songsList);
        this.myContext = context;
        this.songsList = songsList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater)myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.row_song, parent, false);

        row.setId(-1*(position+1));//k naslednemu ziskani pozice v listu - DULEZITE

        TextView songNameLabel = (TextView)row.findViewById(R.id.songNameLabel);
        songNameLabel.setText(songsList.get(position).get("songTitle"));
        TextView songPathLabel = (TextView)row.findViewById(R.id.songPathLabel);
        songPathLabel.setText(fsUtils.getParentFolder(songsList.get(position).get("songPath")));

        ImageButton btnAddRemoveSong = (ImageButton)row.findViewById(R.id.btnAddRemoveSong);
        btnAddRemoveSong.setBackgroundResource(android.R.drawable.ic_menu_add);

        Button btnDragDrop = (Button)row.findViewById(R.id.btnDragDrop);
        if (fsUtils.isDirectory(songsList.get(position).get("songPath"))) {
            btnDragDrop.setBackgroundResource(R.drawable.ic_menu_archive);
            btnAddRemoveSong.setVisibility(View.GONE);
        } else
            btnDragDrop.setBackgroundResource(R.drawable.ic_media_fullscreen);

        return row;
    }
}