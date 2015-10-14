package barqsoft.footballscores.util;

import android.content.Context;
import android.net.Uri;
import android.text.format.Time;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;

import barqsoft.footballscores.BuildConfig;
import barqsoft.footballscores.R;

/**
 * Created by yehya khaled on 3/3/2015.
 */
public class Utilies
{
    public static final int BUNDESLIGA_1 = 394;
    public static final int BUNDESLIGA_2 = 395;
    public static final int LiGUE_1 = 396;
    public static final int LIGUE_2 = 397;
    public static final int PREMIER_LEAGUE = 398;


    public static final int PRIMERA_DIVISION = 399;
    public static final int SEGUNDA_DIVISION = 400;
    public static final int SERIE_A = 401;
    public static final int PRIMEIRA_LIGA = 402;
    public static final int BUNDESLIGA_3 = 403;
    public static final int EREDIVISIE = 404;
    public static final int CHAMPIONS_LEAGUE = 405;


    private final static String LOG_TAG=Utilies.class.getSimpleName();


    public static String getLeague(String league_num, Context context)
    {
        switch (league_num)
        {
            case "BL1" : return context.getString(R.string.bl1);
            case "BL2" : return context.getString(R.string.bl2);
            case "BL3" : return context.getString(R.string.bl3);
            case "PL" : return context.getString(R.string.pl);
            case "EL1" : return context.getString(R.string.el1);
            case "SA" : return context.getString(R.string.sa);
            case "SB" : return context.getString(R.string.sb);
            case "PD" : return context.getString(R.string.pd);
            case "SD" : return context.getString(R.string.sd);
            case "FL1" : return context.getString(R.string.fl1);
            case "FL2" : return context.getString(R.string.fl2);
            case "DED" : return context.getString(R.string.ded);
            case "PPL" : return context.getString(R.string.ppl);
            case "GSL" : return context.getString(R.string.gsl);
            case "CL" : return context.getString(R.string.cl);
            case "EL" : return context.getString(R.string.el);
            case "EC" : return context.getString(R.string.ec);
            case "WC" : return context.getString(R.string.wc);
            default: return null;
        }
    }




    public static String getLeagueName(int league_num, Context context)
    {
        switch (league_num)
        {
            case BUNDESLIGA_1 : return context.getString(R.string.bl1);
            case BUNDESLIGA_2 : return context.getString(R.string.bl2);
            case BUNDESLIGA_3 : return context.getString(R.string.bl3);
            case LiGUE_1 : return context.getString(R.string.fl1);
            case PRIMEIRA_LIGA : return context.getString(R.string.ppl);
            case LIGUE_2 : return context.getString(R.string.fl2);
            case PREMIER_LEAGUE : return context.getString(R.string.pl);
            case PRIMERA_DIVISION : return context.getString(R.string.pd);
            case SEGUNDA_DIVISION : return context.getString(R.string.sd);
            case SERIE_A : return context.getString(R.string.sa);
            case EREDIVISIE : return context.getString(R.string.ded);
            case CHAMPIONS_LEAGUE : return context.getString(R.string.cl);
            default: return null;
        }
    }



    public static String getMatchDay(String match_days,int league_num)
    {
        if(league_num == CHAMPIONS_LEAGUE)
        {
            int match_day=Integer.parseInt(match_days);
            if (match_day <= 6)
            {
                return "Group Stages, Matchday : 6";
            }
            else if(match_day == 7 || match_day == 8)
            {
                return "First Knockout round";
            }
            else if(match_day == 9 || match_day == 10)
            {
                return "QuarterFinal";
            }
            else if(match_day == 11 || match_day == 12)
            {
                return "SemiFinal";
            }
            else
            {
                return "Final";
            }
        }
        else
        {
            return "Matchday : " + match_days;
        }
    }

    public static String getScores(int home_goals,int awaygoals)
    {
        if(home_goals < 0 || awaygoals < 0)
        {
            return " - ";
        }
        else
        {
            return String.valueOf(home_goals) + " - " + String.valueOf(awaygoals);
        }
    }

//    public static int getTeamCrestByTeamName (String teamname)
//    {
//        if (teamname==null){return R.drawable.no_icon;}
//        switch (teamname)
//        { //This is the set of icons that are currently in the app. Feel free to find and add more
//            //as you go.
//            case "Arsenal London FC" : return R.drawable.arsenal;
//            case "Manchester United FC" : return R.drawable.manchester_united;
//            case "Swansea City" : return R.drawable.swansea_city_afc;
//            case "Leicester City" : return R.drawable.leicester_city_fc_hd_logo;
//            case "Everton FC" : return R.drawable.everton_fc_logo1;
//            case "West Ham United FC" : return R.drawable.west_ham;
//            case "Tottenham Hotspur FC" : return R.drawable.tottenham_hotspur;
//            case "West Bromwich Albion" : return R.drawable.west_bromwich_albion_hd_logo;
//            case "Sunderland AFC" : return R.drawable.sunderland;
//            case "Stoke City FC" : return R.drawable.stoke_city;
//            default: return R.drawable.no_icon;
//        }
//    }


//    public static String getTeamCrestById (Context c,String id)
//    {
//        final String baseUrl="http://api.football-data.org/alpha/teams/";
//        String url=null;
//
//
//        Uri fetch_build = Uri.parse(baseUrl).buildUpon().appendPath("id").build();
//        HttpURLConnection m_connection = null;
//        BufferedReader reader = null;
//        String JSON_data = null;
//
//        //Opening Connection
//        try {
//            URL fetch = new URL(fetch_build.toString());
//            m_connection = (HttpURLConnection) fetch.openConnection();
//            m_connection.setRequestMethod("GET");
//            m_connection.addRequestProperty("X-Auth-Token",c.getResources().getString(R.string.api_key));
//            m_connection.connect();
//
//            // Read the input stream into a String
//            InputStream inputStream = m_connection.getInputStream();
//            StringBuffer buffer = new StringBuffer();
//            if (inputStream == null) {
//                // Nothing to do.
//                return null;
//            }
//            reader = new BufferedReader(new InputStreamReader(inputStream));
//
//            String line;
//            while ((line = reader.readLine()) != null) {
//                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
//                // But it does make debugging a *lot* easier if you print out the completed
//                // buffer for debugging.
//                buffer.append(line + "\n");
//            }
//            if (buffer.length() == 0) {
//                // Stream was empty.  No point in parsing.
//                return null;
//            }
//            JSON_data = buffer.toString();
//            JSONObject team=new JSONObject(JSON_data);
//            if(team.has("crestUrl")){
//                url=team.getString("crestUrl");
//            }
//        }
//        catch (Exception e)
//        {
//            Log.e(LOG_TAG, "Exception here" + e.getMessage());
//        }
//        finally {
//            if(m_connection != null)
//            {
//                m_connection.disconnect();
//            }
//            if (reader != null)
//            {
//                try {
//                    reader.close();
//                }
//                catch (IOException e)
//                {
//                    Log.e(LOG_TAG,"Error Closing Stream");
//                }
//            }
//        }
//
//
//
//        return url;
//    }

    public static String getDayName(Context context, long dateInMillis) {
        // If the date is today, return the localized version of "Today" instead of the actual
        // day name.

        Time t = new Time();
        t.setToNow();
        int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
        int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
        if (julianDay == currentJulianDay) {
            return context.getString(R.string.today);
        } else if ( julianDay == currentJulianDay +1 ) {
            return context.getString(R.string.tomorrow);
        }
        else if ( julianDay == currentJulianDay -1)
        {
            return context.getString(R.string.yesterday);
        }
        else
        {
            Time time = new Time();
            time.setToNow();
            // Otherwise, the format is just the day of the week (e.g "Wednesday".
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
            return dayFormat.format(dateInMillis);
        }
    }


    public static String getData(Context context, Uri fetch_build) {

        BufferedReader reader = null;
        HttpURLConnection m_connection = null;
        URL fetch = null;
        try {
            fetch = new URL(fetch_build.toString());
            StringBuilder sb = new StringBuilder();

            m_connection = (HttpURLConnection) fetch.openConnection();
            m_connection.setRequestMethod(context.getString(R.string.http_method));
            m_connection.addRequestProperty("X-Auth-Token", context.getResources().getString(R.string.api_key));
            m_connection.addRequestProperty(context.getString(R.string.api_token), BuildConfig.FOOTBALL_SCORE_API_KEY);
            reader = new BufferedReader(new InputStreamReader(m_connection.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            return sb.toString();
        } catch (MalformedURLException e) {
            Log.d(LOG_TAG, "------------ -  Url malformated  - --------------");
        }
        catch (Exception e) {
            Log.d(LOG_TAG,"------------ -  Problem with the server  -- ----------");
            return null;
        } finally {
            if (m_connection != null) {
                m_connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
        return null;
    }


}
