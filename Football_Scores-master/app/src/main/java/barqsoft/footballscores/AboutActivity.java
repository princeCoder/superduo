package barqsoft.footballscores;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class AboutActivity extends ActionBarActivity {

    private Toolbar toolbar;

    private String ABOUT_FRAGMENT_TAG="ABOUT_FRAGMENT_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        //Add the toolbar to the activity
        toolbar= (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        //Action bar
        final ActionBar ab = getSupportActionBar();
        if(ab!=null){
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeButtonEnabled(true);
        }



        if (savedInstanceState == null) {
            Fragment aboutFragment = new PlaceholderFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, aboutFragment,ABOUT_FRAGMENT_TAG )
                    .commit();
        }
        else {
            PlaceholderFragment aboutFragment=(PlaceholderFragment)getSupportFragmentManager().findFragmentByTag(ABOUT_FRAGMENT_TAG);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, aboutFragment,ABOUT_FRAGMENT_TAG )
                    .commit();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_about, container, false);
        }
    }
}
