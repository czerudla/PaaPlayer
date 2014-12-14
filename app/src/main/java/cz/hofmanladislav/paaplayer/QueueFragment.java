package cz.hofmanladislav.paaplayer;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class QueueFragment extends ListFragment {

    private QueueAdapter myListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myListAdapter = new QueueAdapter(getActivity(), R.layout.row_song, ((MainActivity)getActivity()).songsList);
        setListAdapter(myListAdapter);
    }

    public void notifyAdapter(){
        myListAdapter.notifyDataSetChanged();
    }
}
