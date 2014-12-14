package cz.hofmanladislav.paaplayer;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FileSystemFragment extends ListFragment {

    private FileSystemAdapter myListAdapter2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myListAdapter2 = new FileSystemAdapter(getActivity(), R.layout.row_song, ((MainActivity)getActivity()).filesList);
        setListAdapter(myListAdapter2);
    }

    public void notifyAdapter(){
        myListAdapter2.notifyDataSetChanged();
    }



    ///##### TEST UKLADANI
    /*@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            //Restore the fragment's state here
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save the fragment's state here
    }*/
}
