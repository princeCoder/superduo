package it.jaschke.alexandria;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;


public class BookDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EAN_KEY = "EAN";
    private final int LOADER_ID = 10;
    private View rootView;
    private String ean;
    private String bookTitle;
    private ShareActionProvider shareActionProvider;
    private TextView fullBookTitle;
    private TextView fullBookSubTitle;
    private ImageView fullBookCover;
    private TextView fullBookDescription;
    private TextView categories;
    private TextView authors;
    private Button deleteButton;
    public MenuItem menuItem;



    public BookDetailFragment(){
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Intent intent=getActivity().getIntent();

        if (intent != null && intent.hasExtra(BookDetailFragment.EAN_KEY)) { // We are in single pane mode
            ean= intent.getStringExtra(BookDetailFragment.EAN_KEY);
        }
        else {
            Bundle arguments = getArguments();
            if (arguments != null) {
                ean = arguments.getString(BookDetailFragment.EAN_KEY);
            }
        }

        rootView = inflater.inflate(R.layout.fragment_full_book, container, false);
        fullBookTitle=(TextView)rootView.findViewById(R.id.fullBookTitle);
        fullBookSubTitle=(TextView)rootView.findViewById(R.id.fullBookSubTitle);
        categories=(TextView)rootView.findViewById(R.id.categories);
        authors=(TextView)rootView.findViewById(R.id.authors);
        fullBookDescription=(TextView)rootView.findViewById(R.id.fullBookDesc);
        fullBookCover=(ImageView)rootView.findViewById(R.id.fullBookCover);
        deleteButton=(Button)rootView.findViewById(R.id.delete_button);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean);
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);

                if (isPhone()) {
                    getActivity().finish();

                } else {
                    //Remove the Detail fragment from the container
                    BookDetailFragment fragment = (BookDetailFragment) getFragmentManager().findFragmentByTag("Detail_fragment");
                    if (fragment != null) {
                        getActivity().getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                    }
                }
            }
        });
        return rootView;
    }

    private boolean isPhone(){
        return getActivity().findViewById(R.id.detailContainer)!=null;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.book_detail, menu);

        menuItem = menu.findItem(R.id.action_share);
        //menuItem.setVisible(true);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // restart the loader so that the shareActionProvider will get populated
        restartLoader();
    }


    //Restary the Loader
    private void restartLoader(){
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(ean)),
                null,
                null,
                null,
                null
        );
    }

    // Create the Intent used by the ShareActionProvider
    private Intent createShareIntent(String data){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + data);
        return shareIntent;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));

        if(bookTitle!=null){
            fullBookTitle.setText(bookTitle);
        }

        if(shareActionProvider!=null && bookTitle!=null){
            shareActionProvider.setShareIntent(createShareIntent(bookTitle));
        }


        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        fullBookSubTitle.setText(bookSubTitle);

        String desc = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.DESC));
        fullBookDescription.setText(desc);

        String author = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        if(author!=null){
            String[] authorsArr = author.split(",");
            authors.setLines(authorsArr.length);
            authors.setText(author.replace(",", "\n"));
        }

        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        Glide.with(getActivity())
                .load(imgUrl)
                .error(R.mipmap.default_book)
                .crossFade()
                .into(fullBookCover);
            fullBookCover.setVisibility(View.VISIBLE);

        categories.setText(data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY)));
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}