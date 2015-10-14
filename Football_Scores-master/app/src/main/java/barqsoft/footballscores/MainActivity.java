package barqsoft.footballscores;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import barqsoft.footballscores.sync.FootballSyncAdapter;

public class MainActivity extends ActionBarActivity
{
    public static int selected_match_id;
    public final String LOG_TAG = getClass().getSimpleName() ;
    private PagerFragment my_main;
    private String SELECTED_MATCH="SELECTED_MATCH";
    private String FRAGMENT_TAG="FRAGMENT_TAG";
    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Add the toolbar to the activity
        toolbar= (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        if (savedInstanceState == null) {
            my_main = new PagerFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, my_main, FRAGMENT_TAG)
                    .commit();
        }else{
            selected_match_id = savedInstanceState.getInt(SELECTED_MATCH);
            my_main = (PagerFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        }
        FootballSyncAdapter.initializeSyncAdapter(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about)
        {
            Intent start_about = new Intent(this,AboutActivity.class);
            startActivity(start_about);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        outState.putInt(SELECTED_MATCH, selected_match_id);
        super.onSaveInstanceState(outState);
    }

}
