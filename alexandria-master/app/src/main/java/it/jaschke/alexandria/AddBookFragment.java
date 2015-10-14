package it.jaschke.alexandria;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import it.jaschke.alexandria.barcode.ScannerActivity;
import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.model.Author;
import it.jaschke.alexandria.model.Book;
import it.jaschke.alexandria.model.Category;
import it.jaschke.alexandria.services.BookService;


public class AddBookFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private EditText ean;
    private final int LOADER_ID = 1;
    private View rootView;
    private final String EAN_CONTENT="eanContent";
    private final String BOOK="book";
    public static final String SCAN_FORMAT = "scanFormat";
    public static final String SCAN_CONTENTS = "scanContents";

    private String mScanFormat = "Format:";
    private String mScanContents = "Contents:";

//    private Toolbar toolbar;
    private TextView txtInfo;
    private TextView bookTitle;
    private TextView bookSubTitle;
    private TextView authors;
    private TextView categories;
    private ScrollView scrollView;
    private ImageView bookCover;
    private Button saveButton;
    private Button cancelButton;
    private Button scanButton;
    private BroadcastReceiver messageReceiver;
    private BroadcastReceiver barcodeReceiver;
    private Book mBook=new Book();


    public AddBookFragment(){
    }

    /***
     *  // Register a broadcast receiver
     */
    private void registerMessageBroadcast(){
        messageReceiver=new AlexandraMessageReceiver();
        Intent intent = getActivity().registerReceiver(messageReceiver,
                new IntentFilter(BookService.HANDLE_MESSAGE));
        if (intent != null) {
            messageReceiver.onReceive(getActivity(), intent);
        }
    }


    private void registerBarcodeBroadcast(){
        barcodeReceiver=new AlexandraBarcodeReceiver();
        Intent intent = getActivity().registerReceiver(barcodeReceiver,
                new IntentFilter(BookService.BARCODE));
        if (intent != null) {
            barcodeReceiver.onReceive(getActivity(), intent);
        }
    }


    /**
     * Update the UI with data
     * @param book
     */
    private void updateUI(Book book){
        bookTitle.setText(book.getTitle());
        bookSubTitle.setText(book.getSubtitle());
        if(book.getAuthors()!=null && book.getAuthors().size()>0){
            StringBuffer ch=new StringBuffer("");
            for(Author author:book.getAuthors()){
                ch.append(author.getName()).append("\n");
            }
            authors.setText(ch.toString());
        }
        if(book.getCategories()!=null && book.getCategories().size()>0){
            StringBuffer ch=new StringBuffer("");
            for(Category category:book.getCategories()){
                ch.append(category.getName()).append("\n");
            }
            categories.setText(ch.toString());
        }
        Glide.with(getActivity())
                .load(book.getImageUrl())
                .error(R.mipmap.default_book)
                .crossFade()
                .into(bookCover);


        bookCover.setVisibility(View.VISIBLE);
        saveButton.setVisibility(View.VISIBLE);
        cancelButton.setVisibility(View.VISIBLE);

        scanButton.setVisibility(View.GONE);


    }


    /**
     * Send a message to the Service to start fetching the book referenced by the ISBN entered by the user
     * @param ean: the ISBN
     */
    private void fetchBook(String ean) {

//        if(ean.equals(mBook.getId())){
//            scrollView.setVisibility(View.VISIBLE);
//            txtInfo.setVisibility(View.GONE);
//            updateUI(mBook);
//        }
//        else{
            Intent bookIntent = new Intent(getActivity(), BookService.class);
            bookIntent.putExtra(BookService.EAN, ean);
            bookIntent.setAction(BookService.FETCH_BOOK);
            getActivity().startService(bookIntent);
//        }
    }

    /**
     * Hide everything on the UI
     */
    private void clearFields(){
        bookTitle.setText("");
        bookSubTitle.setText("");
        authors.setText("");
        categories.setText("");
        bookCover.setVisibility(View.INVISIBLE);
        saveButton.setVisibility(View.INVISIBLE);
        cancelButton.setVisibility(View.INVISIBLE);
        scanButton.setVisibility(View.VISIBLE);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // register broadcast receivers
        registerMessageBroadcast();
        registerBarcodeBroadcast();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_add_book, container, false);

        setHasOptionsMenu(false);

        //Action bar
        final ActionBar ab = getActivity().getActionBar();
        if(ab!=null){
            ab.setHomeAsUpIndicator(R.drawable.ic_drawer);
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeButtonEnabled(true);
        }

        scrollView=(ScrollView)rootView.findViewById(R.id.scrollView);
        txtInfo=(TextView) rootView.findViewById(R.id.textInfo);
        bookTitle=(TextView) rootView.findViewById(R.id.bookTitle);
        bookSubTitle=(TextView) rootView.findViewById(R.id.bookSubTitle);
        authors=(TextView) rootView.findViewById(R.id.authors);
        bookCover= (ImageView)rootView.findViewById(R.id.bookCover);
        categories=(TextView) rootView.findViewById(R.id.categories);

        ean = (EditText) rootView.findViewById(R.id.ean);
        ean.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //no need
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //no need
            }

            @Override
            public void afterTextChanged(Editable s) {
                String ean = s.toString();

                //catch isbn10 numbers
                if (ean.length() == 10 && !ean.startsWith("978")) {
                    ean = "978" + ean;
                    fetchBook(ean);
                }
                if (ean.length() < 13) {

                    if(ean.isEmpty()){
                        txtInfo.setText("Please enter the ISBN of the book or use the scanner by pressing the scan button !!!");
                    }

                    txtInfo.setVisibility(View.VISIBLE);
                    scrollView.setVisibility(View.GONE);
                    clearFields();
                    return;
                }
                //Once we have an ISBN, start a book intent
                fetchBook(ean);
            }
        });

        scanButton=(Button)rootView.findViewById(R.id.scan_button);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    Intent intent=new Intent(getActivity(), ScannerActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        saveButton=(Button)rootView.findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.BOOK, mBook);
                bookIntent.setAction(BookService.SAVE_BOOK);
                mBook=new Book();
                getActivity().startService(bookIntent);
                ean.setText("");
            }
        });

        cancelButton=(Button)rootView.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ean.setText("");
            }
        });

        if(savedInstanceState!=null) {
            if (savedInstanceState.containsKey(BOOK)) {
                mBook=(Book)savedInstanceState.getSerializable(BOOK);
            }
            if (savedInstanceState.containsKey(EAN_CONTENT)) {
                ean.setText(savedInstanceState.getString(EAN_CONTENT));
            }
            if(mBook.getId()!=null){
                scrollView.setVisibility(View.VISIBLE);
                txtInfo.setVisibility(View.GONE);
            }
            else{
                scrollView.setVisibility(View.GONE);
                txtInfo.setVisibility(View.VISIBLE);
            }

        }

        //Set the toolbar title in case the screen was rotated
        getActivity().setTitle(getString(R.string.scan));

        //Remove the focus on the editText
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.book_detail, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);
        menuItem.setVisible(false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(ean!=null && !ean.getText().toString().isEmpty()) {
            outState.putString(EAN_CONTENT, ean.getText().toString());
        }
        if(mBook.getId()!=null){ // If we have a book we save that instance
            outState.putSerializable(BOOK, mBook);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //Unregister broadcast receivers
        getActivity().unregisterReceiver(messageReceiver);
        getActivity().unregisterReceiver(barcodeReceiver);
    }

    private void restartLoader(){
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(ean.getText().toString())),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        bookTitle.setText(data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE)));
        bookSubTitle.setText(data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE)));

        String auth = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        if(auth!=null){
            String[] authorsArr = auth.split(",");
            authors.setLines(authorsArr.length);
            authors.setText(auth.replace(",", "\n"));
        }

        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if(Patterns.WEB_URL.matcher(imgUrl).matches()){
            Glide.with(getActivity())
                    .load(imgUrl)
                    .error(R.drawable.ic_launcher)
                    .crossFade()
                    .into(bookCover);
            bookCover.setVisibility(View.VISIBLE);
        }

        categories.setText(data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY)));
        cancelButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    /**
     * Broadcast receiver class handleling Service messages
     */

    private class AlexandraMessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent!=null) {
                int i = intent.getIntExtra(BookService.MESSAGE, -1);
                if (i == BookService.NO_INTERNET) {
                    scrollView.setVisibility(View.GONE);
                    txtInfo.setVisibility(View.VISIBLE);
                    txtInfo.setText("No internet. Please check your connection");
                    //Toast.makeText(getActivity(), "No Internet", Toast.LENGTH_SHORT).show();
                } else if (i == BookService.BOOK_EXIST) {
                    scrollView.setVisibility(View.VISIBLE);
                    txtInfo.setVisibility(View.GONE);
                    Toast.makeText(getActivity(), "Book already exist", Toast.LENGTH_SHORT).show();
                    restartLoader();
                }
                else if(i == BookService.NEW_BOOK){
                    Book book=(Book)intent.getSerializableExtra(BookService.BOOK);
                    scrollView.setVisibility(View.VISIBLE);
                    txtInfo.setVisibility(View.GONE);
                    if(book!=null){ //We diplay information on the screen
                        mBook=book;
                        updateUI(book);
                    }
                }

                if(i == BookService.NO_BOOK_FOUND){
                    scrollView.setVisibility(View.GONE);
                    txtInfo.setText(getResources().getString(R.string.not_found));
                    txtInfo.setVisibility(View.VISIBLE);
                    scanButton.setVisibility(View.VISIBLE);

//                    Toast.makeText(getActivity(), getResources().getString(R.string.not_found), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * Broadcast receiver class to handle barcode data
     */
    private class AlexandraBarcodeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message="";
            if(intent!=null) {
                mScanContents= intent.getStringExtra(SCAN_CONTENTS);
                mScanFormat=intent.getStringExtra(SCAN_FORMAT);
                if (mScanContents !=null && mScanFormat.equals("EAN_13")) {
                    ean.setText(mScanContents);
                }
                else{
                    message="Wrong format. Please Try again";
                }
            }
            else{
                message="Unable to process your request. Scan again";
            }
            if(!message.isEmpty()){
                Toast.makeText(getActivity(),message,Toast.LENGTH_SHORT).show();
            }
        }
    }
}
