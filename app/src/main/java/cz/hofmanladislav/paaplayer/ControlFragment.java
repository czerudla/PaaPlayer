package cz.hofmanladislav.paaplayer;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class ControlFragment extends Fragment {

    private View view;
    private ImageButton btnPrev;
    //private Button btnBack;
    private ImageButton btnPlay;
    private ImageButton btnMute;
    private ImageButton btnStop;
    //private Button btnForw;
    private ImageButton btnNext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        view = inflater.inflate(R.layout.fragment_control, container, false);

        btnPrev = (ImageButton)view.findViewById(R.id.btnPrev);
        //btnBack = (Button)view.findViewById(R.id.btnBack);
        btnPlay = (ImageButton)view.findViewById(R.id.btnPlay);
        btnMute = (ImageButton)view.findViewById(R.id.btnMute);
        btnStop = (ImageButton)view.findViewById(R.id.btnStop);
        //btnForw = (Button)view.findViewById(R.id.btnForward);
        btnNext = (ImageButton)view.findViewById(R.id.btnNext);

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controlsListener.onBtnPrevClick();
            }
        });

        /*btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controlsListener.onBtnBackClick();
            }
        });*/

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controlsListener.onBtnPlayClick();
            }
        });

        btnMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controlsListener.onBtnMuteClick();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controlsListener.onBtnStopClick();
            }
        });

        /*btnForw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controlsListener.onBtnForwardClick();
            }
        });*/

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controlsListener.onBtnNextClick();
            }
        });

        return view;
    }

    public void setBtnPlayIcon(int resID){
        btnPlay.setBackgroundResource(resID);
    }

    public interface IControl {
        public void onBtnPrevClick();
        //public void onBtnBackClick();
        public void onBtnPlayClick();
        public void onBtnMuteClick();
        public void onBtnStopClick();
        //public void onBtnForwardClick();
        public void onBtnNextClick();
    }

    IControl controlsListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            controlsListener = (IControl) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement controlsListener");
        }
    }
}