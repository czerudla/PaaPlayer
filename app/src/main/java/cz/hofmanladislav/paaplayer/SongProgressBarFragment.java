package cz.hofmanladislav.paaplayer;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

public class SongProgressBarFragment extends Fragment {

    private View view;

    private SeekBar progressBar;
    private TextView current;
    private TextView total;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        view = inflater.inflate(R.layout.fragment_songprogressbar, container, false);

        current = (TextView)view.findViewById(R.id.songCurrentDurationLabel);
        total = (TextView)view.findViewById(R.id.songTotalDurationLabel);
        progressBar = (SeekBar)view.findViewById(R.id.songProgressBar);

        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {progressListener.onProgressChangedPB(progressBar.getProgress());}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {progressListener.onStartTrackingTouchPB();}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {progressListener.onStopTrackingTouchPB(progressBar.getProgress());}
        });

        return view;
    }

    public void enableProgressBar(boolean enabled) {
        progressBar.setEnabled(enabled);
    }


    public void setCurrentTime(String time){
        current.setText(time);
    }

    public void setTotalTime(String time){
        total.setText(time);
    }

    public void setProgressBarPercentage(int percentage){
        progressBar.setProgress(percentage);
    }

    public interface ISongProgressBar {
        public void onProgressChangedPB(int progress);
        public void onStartTrackingTouchPB();
        public void onStopTrackingTouchPB(int progress);
    }

    ISongProgressBar progressListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            progressListener = (ISongProgressBar) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement progressListener");
        }
    }
}