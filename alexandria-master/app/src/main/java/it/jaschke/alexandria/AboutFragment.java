package it.jaschke.alexandria;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class AboutFragment extends Fragment {

    public AboutFragment(){

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);
        //Set the toolbar title in case the screen was rotated
        getActivity().setTitle(getString(R.string.about));

        setHasOptionsMenu(true);

        return rootView;
    }



    @Override
    public void onResume() {
        super.onResume();
        BookDetailFragment fragment=(BookDetailFragment)getFragmentManager().findFragmentByTag("Detail_fragment");
        if(fragment!=null)
            fragment.menuItem.setVisible(false);
    }
}
