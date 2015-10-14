package it.jaschke.alexandria;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import it.jaschke.alexandria.adapter.BookListAdapter;
import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.model.Book;


public class ListOfBookFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    //Recycler view
    private RecyclerView mRecyclerView;

    // SearchView used to find books
    private SearchView searchText;

    // used when we don't have books in the database.
    // We just display a message to the user. This is the empty view concept used usually by listviews
    private TextView mtextInfo;

    //Adapter
    private BookListAdapter bookListAdapter;

    //Selected position on the recyclerView
    private int mPosition= RecyclerView.NO_POSITION;

    //Selected index tag to handle his saveInstance state
    private static final String SELECTED_KEY = "selected_position";
    private final int LOADER_ID = 10;

    //Is the InstanceState saved?
    private boolean fromInstanceState;

    public ListOfBookFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        String searchString=searchText.getQuery().toString();
        if(!searchString.isEmpty())
        fetchBook(searchString);
        else
            restartLoader();
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_list_of_books, container, false);

        mtextInfo= (TextView) rootView.findViewById(R.id.textInfo);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.listOfBooks);

        // Set the layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        bookListAdapter =new BookListAdapter(getActivity(), new BookListAdapter.BookAdapterOnClickHandler() {

            @Override
            public void onClick(Book book, BookListAdapter.BookAdapterViewHolder vh) {
                ((Callback)getActivity()).onItemSelected(book);
                mPosition = vh.getAdapterPosition();
            }
        });

        if(savedInstanceState!=null){
            fromInstanceState=true;
            if(savedInstanceState.containsKey(SELECTED_KEY)){
                mPosition = savedInstanceState.getInt(SELECTED_KEY);
            }
        }

        if (mPosition != RecyclerView.NO_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mRecyclerView.smoothScrollToPosition(mPosition);
            bookListAdapter.setSelectedItem(mPosition);
        }



        mRecyclerView.setAdapter(bookListAdapter);
        mRecyclerView.setHasFixedSize(true);


        searchText = (SearchView) rootView.findViewById(R.id.searchText);
        searchText.setIconifiedByDefault(false);
        searchText.setQueryHint(getResources().getString(R.string.book_search_hint));
        searchText.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                String searchKeyword = searchText.getQuery().toString();
                if (!searchKeyword.isEmpty()) {
                    fetchBook(searchKeyword);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s.isEmpty()) {
                    if (!fromInstanceState) {
                        ((Callback) getActivity()).onItemUnselected();
                    }
                    restartLoader();

                } else{
                    if(!fromInstanceState){
                        fromInstanceState=false;
                        mPosition = RecyclerView.NO_POSITION;
                        bookListAdapter.setSelectedItem(mPosition);
                        fetchBook(s);
                    }
                    else{
                        fromInstanceState=false;
                    }

                }
                return false;
            }
        });

        // Get the searchView close button image view
        ImageView closeButton = (ImageView)searchText.findViewById(R.id.search_close_btn);

        // Set on click listener
        closeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mPosition = RecyclerView.NO_POSITION;
                bookListAdapter.setSelectedItem(mPosition);
                searchText.setQuery("", true);
                restartLoader();
                ((Callback) getActivity()).onItemUnselected();
            }
        });
        //Set the toolbar title in case the screen was rotated
        getActivity().setTitle(getString(R.string.books));

        //Remove the focus on the editText
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        return rootView;
    }


    private void restartLoader(){

        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
    }


    /*
     fetch books using his title or SubTitle
    */
    public void fetchBook(String searchString) {

            final String selection = AlexandriaContract.BookEntry.TITLE + " LIKE ? OR " + AlexandriaContract.BookEntry.SUBTITLE + " LIKE ? ";
            if (searchString.length() > 0) {
                searchString = "%" + searchString + "%";
                Cursor cursor = getActivity().getContentResolver().query(AlexandriaContract.BookEntry.CONTENT_URI, null, selection, new String[]{searchString, searchString}, null);
                if(cursor!=null && cursor.getCount()==0){
                    ((Callback)getActivity()).onItemUnselected();
                }
                bookListAdapter.swapCursor(cursor);
            }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to RecyclerView.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != RecyclerView.NO_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //update the adapter
        bookListAdapter.swapCursor(data);

        if(data!=null && data.getCount()==0){
            mtextInfo.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }else {
            mtextInfo.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        bookListAdapter.swapCursor(null);
    }


    //Callback method when the user clicks on the list
    public interface Callback {
        void onItemSelected(Book book);
        void onItemUnselected();
    }
}
