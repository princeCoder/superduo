package barqsoft.footballscores.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.GenericRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.StreamEncoder;
import com.bumptech.glide.load.resource.file.FileToStreamDecoder;
import com.caverock.androidsvg.SVG;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.InputStream;

import barqsoft.footballscores.R;
import svgandroid.SvgDecoder;
import svgandroid.SvgDrawableTranscoder;
import svgandroid.SvgSoftwareLayerSetter;
import barqsoft.footballscores.data.DatabaseContract;
import barqsoft.footballscores.util.Utilies;
import barqsoft.footballscores.util.ViewHolder;
import svgandroid.SVGParser;

;

/**
 * Created by yehya khaled on 2/26/2015.
 */
public class FootballScoresAdapter extends CursorAdapter
{
    public double detail_match_id = 0;
    private String FOOTBALL_SCORES_HASHTAG = "#Football_Scores";
    private GenericRequestBuilder<Uri, InputStream, SVG, PictureDrawable> requestBuilder;
    private String LOG_TAG=getClass().getSimpleName();

    public FootballScoresAdapter(Context context, Cursor cursor, int flags)
    {
        super(context,cursor,flags);
        requestBuilder = Glide.with(context)
                .using(Glide.buildStreamModelLoader(Uri.class, context), InputStream.class)
                .from(Uri.class)
                .as(SVG.class)
                .transcode(new SvgDrawableTranscoder(), PictureDrawable.class)
                .sourceEncoder(new StreamEncoder())
                .cacheDecoder(new FileToStreamDecoder<SVG>(new SvgDecoder()))
                .decoder(new SvgDecoder())
                .placeholder(R.drawable.ic_launcher)
                .error(R.drawable.ic_launcher)
                .animate(android.R.anim.fade_in)
                .listener(new SvgSoftwareLayerSetter<Uri>());
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        View mItem = LayoutInflater.from(context).inflate(R.layout.scores_list_item, parent, false);
        ViewHolder mHolder = new ViewHolder(mItem);
        mItem.setTag(mHolder);
        return mItem;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor)
    {
        final ViewHolder mHolder = (ViewHolder) view.getTag();
        mHolder.home_name.setText(cursor.getString(cursor.getColumnIndex(DatabaseContract.scores_table.HOME_COL)));
        mHolder.away_name.setText(cursor.getString(cursor.getColumnIndex(DatabaseContract.scores_table.AWAY_COL)));
        String time=cursor.getString(cursor.getColumnIndex(DatabaseContract.scores_table.TIME_COL));
        mHolder.date.setText(time);
        mHolder.score.setText(Utilies.getScores(cursor.getInt(cursor.getColumnIndex(DatabaseContract.scores_table.HOME_GOALS_COL)), cursor.getInt(cursor.getColumnIndex(DatabaseContract.scores_table.AWAY_GOALS_COL))));
        mHolder.match_id = cursor.getDouble(cursor.getColumnIndex(DatabaseContract.scores_table.MATCH_ID));

        String homecrest=cursor.getString(cursor.getColumnIndex(DatabaseContract.scores_table.HOME_CREST_COL));
//        Glide.with(mContext)
//                .load(homecrest)
//                .error(R.drawable.ic_launcher)
//                .crossFade()
//                .into(mHolder.home_crest);
        loadNet(homecrest, mHolder.home_crest);
//        new HttpImageRequestTask(mHolder.home_crest).execute(homecrest);


        String awaycrest=cursor.getString(cursor.getColumnIndex(DatabaseContract.scores_table.AWAY_CREST_COL));
//        Glide.with(mContext)
//                .load(awaycrest)
//                .error(R.drawable.ic_launcher)
//                .crossFade()
//                .into(mHolder.away_crest);
        loadNet(awaycrest, mHolder.away_crest);
//        new HttpImageRequestTask(mHolder.away_crest).execute(awaycrest);



        LayoutInflater vi = (LayoutInflater) context.getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.detail_fragment, null);
        final ViewGroup container = (ViewGroup) view.findViewById(R.id.details_fragment_container);

        if(mHolder.match_id == detail_match_id)
        {
            container.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                    , ViewGroup.LayoutParams.MATCH_PARENT));
            TextView match_day = (TextView) v.findViewById(R.id.matchday_textview);
            String date=cursor.getString(cursor.getColumnIndex(DatabaseContract.scores_table.DATE_COL));
            match_day.setText(Utilies.getMatchDay(cursor.getString(cursor.getColumnIndex(DatabaseContract.scores_table.DATE_COL)),cursor.getInt(cursor.getColumnIndex(DatabaseContract.scores_table.LEAGUE_COL))));
            TextView league = (TextView) v.findViewById(R.id.league_textview);
            league.setText(cursor.getString(cursor.getColumnIndex(DatabaseContract.scores_table.LEAGUE_COL)));
            Button share_button = (Button) v.findViewById(R.id.share_button);
            share_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    //add Share Action
                    context.startActivity(createShareIntent(mHolder.home_name.getText()+" "
                    +mHolder.score.getText()+" "+mHolder.away_name.getText() + " "));
                }
            });
        }
        else
        {
            container.removeAllViews();
        }

    }
    public Intent createShareIntent(String ShareText) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, ShareText + mContext.getString(R.string.football_score_hashtag));
        return shareIntent;
    }

    private void loadNet(String url, ImageView imageViewNet) {
        Uri uri = Uri.parse(url);
        requestBuilder
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        // SVG cannot be serialized so it's not worth to cache it
                .load(uri)
                .into(imageViewNet);
    }


    private class HttpImageRequestTask extends AsyncTask<String, Void, Drawable> {

        private ImageView mImageView;

        public HttpImageRequestTask(ImageView img){
            mImageView=img;
        }
        @Override
        protected Drawable doInBackground(String... params) {
            try {

                //use instance of HttpClient because SAXParserFactory is not compatible with HttpConnection
                // create HttpClient
                HttpClient httpclient = new DefaultHttpClient();

                // make GET request to the given URL
                HttpResponse httpResponse = httpclient.execute(new HttpGet(params[0]));

                // receive response as inputStream
                InputStream inputStream  = httpResponse.getEntity().getContent();

                // convert inputstream to string
                if(inputStream != null){
                    svgandroid.SVG svg = SVGParser. getSVGFromInputStream(inputStream);
                    Drawable drawable = svg.createPictureDrawable();
                    return drawable;
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            // Update the view
            updateImageView(drawable);
        }


        @SuppressLint("NewApi")
        private void updateImageView(Drawable drawable){
            if(drawable != null){

                // Try using your library and adding this layer type before switching your SVG parsing
                mImageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                mImageView.setImageDrawable(drawable);
            }
        }
    }

}
