package barqsoft.footballscores.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

import barqsoft.footballscores.R;
import barqsoft.footballscores.data.DatabaseContract;
import barqsoft.footballscores.util.Utilies;
import barqsoft.footballscores.widget.WidgetProvider;

/**
 * Created by Prinzly Ngotoum on 9/28/15.
 */
public class FootballSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = FootballSyncAdapter.class.getSimpleName();

    //Base Url
    private static final String BASE_URL="http://api.football-data.org/alpha/"; // Fixtures
    private final String MATCH_LINK = "http://api.football-data.org/alpha/fixtures/";
    private final String SOCCER_SEASON_LINK = "http://api.football-data.org/alpha/soccerseasons/";
    private final String FIXTURES = "fixtures";
    private final String LINKS = "_links";
    private final String SOCCER_SEASON = "soccerseason";
    private final String SELF = "self";
    private final String HREF = "href";
    private final String MATCH_DATE = "date";
    private final String HOME_TEAM = "homeTeam";
    private final String AWAY_TEAM = "awayTeam";
    private final String HOME_TEAM_NAME = "homeTeamName";
    private final String AWAY_TEAM_NAME = "awayTeamName";
    private final String RESULT = "result";
    private final String HOME_GOALS = "goalsHomeTeam";
    private final String AWAY_GOALS = "goalsAwayTeam";
    private final String MATCH_DAY = "matchday";
    private int fixtures;


    // Interval at which to sync with the weather, in milliseconds.
// 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 2;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;


    public FootballSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    private void updateWidgets() {
        Context context = getContext();
        //Sending a broadcast
        Intent dataUpdatedIntent = new Intent(WidgetProvider.ACTION_DATA_UPDATED);
        context.sendBroadcast(dataUpdatedIntent);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        getDataWithTimeFrame("n5");
        getDataWithTimeFrame("p5");

        //Update the widget
        updateWidgets();

    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }


    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        FootballSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }


    /**
     * Get data with a time frame
     * @param timeFrame
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void getDataWithTimeFrame (String timeFrame)
    {
        //Creating fetch URL
        final String QUERY_TIME_FRAME = getContext().getString(R.string.timeframe); //Time Frame parameter to determine days

        Uri fetch_build = Uri.parse(BASE_URL).buildUpon().appendPath(FIXTURES).
                appendQueryParameter(QUERY_TIME_FRAME, timeFrame).build();

        String JSON_data = Utilies.getData(getContext(), fetch_build);
        if(JSON_data!=null){ // Server returned data
            try {
                fixtures = Integer.parseInt(new JSONObject(JSON_data).getString("count"));
                if (fixtures == 0) { // We have no data
                    return;
                }
                //We parse data and save them in the database so the loader will get them and populate the listview
                processJSONdata(JSON_data, getContext());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else { //We didn't get any data from the server
            //Could not Connect
            Log.d(LOG_TAG, "Problem with the server.");
        }
    }


    /**
     *
     * @param JSONdata Json data in string format to be parsed
     * @param mContext Context of the application
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void processJSONdata (String JSONdata,Context mContext) {
        //Match data
        String League = null;
        String mDate = null;
        String mTime = null;
        String Home = null;
        String homeCrest = null;
        String awayCrest = null;
        String Away = null;
        String Home_goals = null;
        String Away_goals = null;
        String match_id = null;
        String match_day = null;

        try {
            JSONArray matches = new JSONObject(JSONdata).getJSONArray(FIXTURES);
            //ContentValues to be inserted
            Vector<ContentValues> values = new Vector <ContentValues> (matches.length());
            for(int i = 0;i < fixtures;i++)
            {

                JSONObject match_data = matches.getJSONObject(i);
                //League Name
                League=match_data.getJSONObject(LINKS).getJSONObject(SOCCER_SEASON).
                        getString(HREF);
                if(League!=null){
                    League=League.replace(SOCCER_SEASON_LINK, "");
                    League=Utilies.getLeagueName(Integer.parseInt(League),getContext());
                }

                //Home crest url
                homeCrest=getTeamCrest(match_data.getJSONObject(LINKS).getJSONObject(HOME_TEAM).
                        getString(HREF));

                // Away crest url
                awayCrest=getTeamCrest(match_data.getJSONObject(LINKS).getJSONObject(AWAY_TEAM).
                        getString(HREF));

                //Match id
                match_id = match_data.getJSONObject(LINKS).getJSONObject(SELF).
                        getString(HREF);
                match_id = match_id.replace(MATCH_LINK, "");

                mDate = match_data.getString(MATCH_DATE);
                mTime = mDate.substring(mDate.indexOf("T") + 1, mDate.indexOf("Z")-3);
                mDate = mDate.substring(0, mDate.indexOf("T"));

                Home = match_data.getString(HOME_TEAM_NAME);
                Away = match_data.getString(AWAY_TEAM_NAME);
                Home_goals = match_data.getJSONObject(RESULT).getString(HOME_GOALS);
                Away_goals = match_data.getJSONObject(RESULT).getString(AWAY_GOALS);
                match_day = match_data.getString(MATCH_DAY);


                // Content value to be inserted in the database
                ContentValues match_values = new ContentValues();
                match_values.put(DatabaseContract.scores_table.MATCH_ID,match_id);
                match_values.put(DatabaseContract.scores_table.DATE_COL,mDate);
                match_values.put(DatabaseContract.scores_table.TIME_COL,mTime);
                match_values.put(DatabaseContract.scores_table.HOME_COL,Home);
                match_values.put(DatabaseContract.scores_table.AWAY_COL,Away);

                match_values.put(DatabaseContract.scores_table.HOME_CREST_COL,homeCrest!=null?homeCrest:"");
                match_values.put(DatabaseContract.scores_table.AWAY_CREST_COL,awayCrest!=null?awayCrest:"");

                match_values.put(DatabaseContract.scores_table.HOME_GOALS_COL,Home_goals);
                match_values.put(DatabaseContract.scores_table.AWAY_GOALS_COL,Away_goals);
                match_values.put(DatabaseContract.scores_table.LEAGUE_COL, League != null ? League : mContext.getString(R.string.Unknown_League));
                match_values.put(DatabaseContract.scores_table.MATCH_DAY,match_day);

                values.add(match_values);
                //Log.v(LOG_TAG, "Succesfully added : ");
            }

            int inserted_data = 0;
            ContentValues[] insert_data = new ContentValues[values.size()];
            values.toArray(insert_data);
            inserted_data = mContext.getContentResolver().bulkInsert(
                    DatabaseContract.BASE_CONTENT_URI,insert_data);

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // delete old data so we don't build up an endless history
            getContext().getContentResolver().delete(DatabaseContract.scores_table.buildScoreWithDate(),
                    DatabaseContract.scores_table.DATE_COL + " <= ?",
                    new String[]{Long.toString(dayTime.setJulianDay(julianStartDay-5))});

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    // Return the league name
//    private String getLeagueName(String url){
//        Uri fetch_build = Uri.parse(url).buildUpon().build();
//        String JSON_data = Utilies.getData(getContext(), fetch_build);
//        if(JSON_data!=null)
//            return processLeagueData(JSON_data);
//        return null;
//    }


    //Get a League name using a string representing a JSON
//    private String processLeagueData(String data){
//        final String LEAGUE="league";
//
//        String league=null;
//        try {
//            JSONObject dataObj=new JSONObject(data);
//            if(dataObj.has(LEAGUE)){
//                league= Utilies.getLeague(dataObj.getString(LEAGUE),getContext());
//            }
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return league;
//    }


    // Return Team crest
    private String getTeamCrest(String url){
        Uri fetch_build = Uri.parse(url).buildUpon().build();
        String JSON_data = Utilies.getData(getContext(), fetch_build);
        if(JSON_data!=null)
            return processCrestData(JSON_data);
        return null;
    }


    //Get a team crest using a String representing a JSON
    private String processCrestData(String data){
        final String CRESTURL="crestUrl";
        String url=null;
        try {
            JSONObject dataObj=new JSONObject(data);
            if(dataObj.has(CRESTURL)){
                url= dataObj.getString(CRESTURL);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return url;
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

}
