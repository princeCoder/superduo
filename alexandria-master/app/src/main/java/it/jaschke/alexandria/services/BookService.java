package it.jaschke.alexandria.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.IntDef;
import android.support.v4.content.CursorLoader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;

import it.jaschke.alexandria.Utility;
import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.model.Author;
import it.jaschke.alexandria.model.Book;
import it.jaschke.alexandria.model.Category;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class BookService extends IntentService {

    private final String LOG_TAG = BookService.class.getSimpleName();

    public static final String FETCH_BOOK = "it.jaschke.alexandria.services.action.FETCH_BOOK";
    public static final String DELETE_BOOK = "it.jaschke.alexandria.services.action.DELETE_BOOK";
    public static final String SAVE_BOOK = "it.jaschke.alexandria.services.action.SAVE_BOOK";

    public static final String EAN = "it.jaschke.alexandria.services.extra.EAN";

    public static final String HANDLE_MESSAGE="HANDLE_MESSAGE";
    public static final String BARCODE="BARCODE";
    public static final String MESSAGE="MESSAGE";
    public static final String BOOK="BOOK";

    public BookService() {
        super("Alexandria");
    }


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({BOOK_EXIST, NO_INTERNET,NEW_BOOK, NO_BOOK_FOUND})
    public @interface AlexandraStatus {}

    public static final int BOOK_EXIST = 0;
    public static final int NEW_BOOK = 1;
    public static final int NO_INTERNET = 2;
    public static final int NO_BOOK_FOUND = 3;

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (FETCH_BOOK.equals(action)) {
                final String ean = intent.getStringExtra(EAN);
                if(isTheBookExist(ean)){
                    Intent broadcastIntent=new Intent(HANDLE_MESSAGE);
                    broadcastIntent.putExtra(MESSAGE,BOOK_EXIST);
                    getApplicationContext().sendBroadcast(broadcastIntent);
                }
                else{
                    fetchBook(ean);
                }
            } else if (DELETE_BOOK.equals(action)) {
                final String ean = intent.getStringExtra(EAN);
                deleteBook(ean);
            }
            else if (SAVE_BOOK.equals(action)){
                Book book=(Book)intent.getSerializableExtra(BOOK);
                if(book!=null){
                    //We save the book
                    writeBooks(book);
                    //Check is the book has Categories
                    if(book.getCategories()!=null && book.getCategories().size()>0){
                        writeCategories(book.getCategories());
                    }
                    //Check if the book has authors
                    if(book.getAuthors()!=null && book.getAuthors().size()>0){
                        writeAuthors(book.getAuthors());
                    }
                }
            }
        }
    }

    /**
     * Delete a book in the database in the provided background thread with the provided
     * parameters.
     */
    private void deleteBook(String ean) {
        if(ean!=null) {
            getContentResolver().delete(AlexandriaContract.BookEntry.buildBookUri(Long.parseLong(ean)), null, null);
        }
    }


    /**
     * check if a provided book already exist
     * @param ean
     * @return
     */
    private boolean isTheBookExist(String ean){
        if(ean.length()!=13){
            return false;
        }

        Cursor bookEntry = getContentResolver().query(
                AlexandriaContract.BookEntry.buildBookUri(Long.parseLong(ean)),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        boolean found= bookEntry.getCount()>0?true:false;
        bookEntry.close();
        return found;
    }


    private void fetchBookFromDataBase(String ean){
        new CursorLoader(
                getApplicationContext(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(ean)),
                null,
                null,
                null,
                null
        );

    }

    /**
     * Handle action fetchBook in the provided background thread with the provided
     * parameters.
     *
     * Fetch the bookusing the api. If a book is found? save it in the databse
     */
    private void fetchBook(String ean) {
        // We go look online
        if(Utility.isNetworkAvailable(getApplicationContext())){
            HttpURLConnection urlConnection = null;
            String bookJsonString = null;

            try {
                final String FORECAST_BASE_URL = "https://www.googleapis.com/books/v1/volumes?";
                final String QUERY_PARAM = "q";

                final String ISBN_PARAM = "isbn:" + ean;

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, ISBN_PARAM)
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                bookJsonString=Utility.getData(urlConnection);

            } catch (Exception e) {
                Log.e(LOG_TAG, "Error ", e);
            }

            final String ITEMS = "items";

            final String VOLUME_INFO = "volumeInfo";

            final String TITLE = "title";
            final String SUBTITLE = "subtitle";
            final String AUTHORS = "authors";
            final String DESC = "description";
            final String CATEGORIES = "categories";
            final String IMG_URL_PATH = "imageLinks";
            final String IMG_URL = "thumbnail";

            try {
                JSONObject bookJson = new JSONObject(bookJsonString);
                JSONArray bookArray;
                if(bookJson.has(ITEMS)){
                    bookArray = bookJson.getJSONArray(ITEMS);
                }else{

                    //Send a broadcast saying the book doesn't exist
                    Intent messageIntent = new Intent(HANDLE_MESSAGE);
                    messageIntent.putExtra(MESSAGE,NO_BOOK_FOUND);
                    getApplicationContext().sendBroadcast(messageIntent);
                    return;
                }

                JSONObject bookInfo = ((JSONObject) bookArray.get(0)).getJSONObject(VOLUME_INFO);
                Book book=new Book();

                book.setId(ean);

                if(bookInfo.has(TITLE)){
                    book.setTitle(bookInfo.getString(TITLE)!=null?bookInfo.getString(TITLE):"");
                }


                if(bookInfo.has(SUBTITLE)) {
                    book.setSubtitle(bookInfo.getString(SUBTITLE)!=null?bookInfo.getString(SUBTITLE):"");
                }

                if(bookInfo.has(DESC)){
                    book.setDescription(bookInfo.getString(DESC)!=null?bookInfo.getString(DESC):"");
                }

                if(bookInfo.has(IMG_URL_PATH) && bookInfo.getJSONObject(IMG_URL_PATH).has(IMG_URL)) {
                    book.setImageUrl(bookInfo.getJSONObject(IMG_URL_PATH).getString(IMG_URL));
                }
                //Check if the book has an author. If yes add authors of the book
                if(bookInfo.has(AUTHORS)) {
                    book.setAuthors(getAuthorsFromJsonArray(ean, bookInfo.getJSONArray(AUTHORS)));
                }

                //Check if the book has a category. If yes add categories of the Book
                if(bookInfo.has(CATEGORIES)){
                    book.setCategories(getCategoriesFromJsonArray(ean,bookInfo.getJSONArray(CATEGORIES)));
                }

                Intent broadcastIntent=new Intent(HANDLE_MESSAGE);
                broadcastIntent.putExtra(MESSAGE,NEW_BOOK);
                broadcastIntent.putExtra(BOOK,book);
                getApplicationContext().sendBroadcast(broadcastIntent);

            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error ", e);
            }
        }
        else{
            //Todo I send a broadcast for not having a connection
            Intent broadcastIntent=new Intent(HANDLE_MESSAGE);
            broadcastIntent.putExtra(MESSAGE,NO_INTERNET);
            getApplicationContext().sendBroadcast(broadcastIntent);
        }
    }


    private ArrayList<Author> getAuthorsFromJsonArray(String ean,JSONArray jsonArray) throws JSONException{
        ArrayList<Author> authors=new ArrayList<>();
        Author author;
        for (int i = 0; i < jsonArray.length(); i++) {
            author=new Author(ean,jsonArray.getString(i));
            authors.add(author);
        }
        return  authors;
    }


    private ArrayList<Category> getCategoriesFromJsonArray(String ean, JSONArray jsonArray) throws JSONException{
        ArrayList<Category> categories=new ArrayList<>();
        Category category;
        for (int i = 0; i < jsonArray.length(); i++) {
            category=new Category(ean,jsonArray.getString(i));
            categories.add(category);
        }
        return categories;
    }

    /*
    Insert a book in the database
     */

    private void writeBooks(Book book){
        ContentValues values= new ContentValues();
        values.put(AlexandriaContract.BookEntry._ID, book.getId());
        values.put(AlexandriaContract.BookEntry.TITLE, book.getTitle());
        values.put(AlexandriaContract.BookEntry.IMAGE_URL, book.getImageUrl());
        values.put(AlexandriaContract.BookEntry.SUBTITLE, book.getSubtitle());
        values.put(AlexandriaContract.BookEntry.DESC, book.getDescription());
        getContentResolver().insert(AlexandriaContract.BookEntry.CONTENT_URI,values);
    }

    /*
    Insert authors in the database
     */

    private void writeAuthors(ArrayList<Author> authors){
        ContentValues values;
        Vector<ContentValues> vector=new Vector<>();
        for (Author author:authors) {
            values= new ContentValues();
            values.put(AlexandriaContract.AuthorEntry._ID, author.getId());
            values.put(AlexandriaContract.AuthorEntry.AUTHOR, author.getName());
            vector.add(values);
        }

        ContentValues[]valuesArray=new ContentValues[vector.size()];
        vector.toArray(valuesArray);
        getContentResolver().bulkInsert(AlexandriaContract.AuthorEntry.CONTENT_URI,valuesArray);
    }

    /*
    Insert categories in the database
     */

    private void writeCategories(ArrayList<Category> categories){
        ContentValues values;
        Vector<ContentValues> vector=new Vector<>();
        for (Category category:categories) {
            values=new ContentValues();
            values.put(AlexandriaContract.CategoryEntry._ID, category.getId());
            values.put(AlexandriaContract.CategoryEntry.CATEGORY, category.getName());
            vector.add(values);
        }
        ContentValues[]valuesArray=new ContentValues[vector.size()];
        vector.toArray(valuesArray);
        getContentResolver().bulkInsert(AlexandriaContract.CategoryEntry.CONTENT_URI,valuesArray);
    }

}