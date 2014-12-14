package cz.hofmanladislav.paaplayer;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.HashMap;

public class NowPlayingFragment extends Fragment {

    private View view;
    private ImageButton btnOpenFolder;
    private ImageButton btnAddToQueue;
    private TextView songNameLabel;
    private TextView songPathLabel;
    private HashMap<String, String> actualSong;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        view = inflater.inflate(R.layout.fragment_nowplaying, container, false);

        btnOpenFolder = (ImageButton)view.findViewById(R.id.btnOpenFolder);
        btnAddToQueue = (ImageButton)view.findViewById(R.id.btnAddToQueue);
        songNameLabel = (TextView)view.findViewById(R.id.songNameLabelNP);
        songPathLabel = (TextView)view.findViewById(R.id.songPathLabelNP);

        btnOpenFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nowPlayingListener.onBtnOpenFolderClick(getSongPath(), view);
            }
        });

        btnAddToQueue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nowPlayingListener.onBtnAddToQueueClick(getSong());
            }
        });

        return view;
    }

    public void setSongNameLabel(String text){
        songNameLabel.setText(text);
    }

    public void setSongPathLabel(String text){
        songPathLabel.setText(text);
    }

    public void setSong(HashMap<String, String> song){
        setSongNameLabel(song.get("songTitle"));
        setSongPathLabel(song.get("songPath"));
        actualSong = song;
    }

    public HashMap<String, String> getSong() {
        return this.actualSong;
    }

    public String getSongPath(){
        if (actualSong != null)
            return actualSong.get("songPath");
        return "/";
    }

    public interface INowPlaying {
        public void onBtnOpenFolderClick(String path, View view);
        public void onBtnAddToQueueClick(HashMap<String, String> song);
    }

    INowPlaying nowPlayingListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            nowPlayingListener = (INowPlaying) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement nowPlayingListener");
        }
    }
}
