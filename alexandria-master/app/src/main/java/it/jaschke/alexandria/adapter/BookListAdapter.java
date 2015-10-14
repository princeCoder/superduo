package it.jaschke.alexandria.adapter;


import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import it.jaschke.alexandria.R;
import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.model.Book;

/**
 * Created by saj on 11/01/15.
 */
public class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.BookAdapterViewHolder> {

    private Cursor mCursor;
    private Context mContext;

    //Current element selected
    private int selectedItem=-1;

    // Click handler callback
    BookAdapterOnClickHandler mCallback;


    /**
     * Cache of the children views for a forecast list item.
     */
    public class BookAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public final ImageView bookCover;
        public final TextView bookTitle;
        public final TextView bookSubTitle;


        public BookAdapterViewHolder(View view) {
            super(view);
            bookCover = (ImageView) view.findViewById(R.id.fullBookCover);
            bookTitle = (TextView) view.findViewById(R.id.listBookTitle);
            bookSubTitle = (TextView) view.findViewById(R.id.listBookSubTitle);
            view.setClickable(true);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            if(selectedItem!=-1){
                notifyItemChanged(selectedItem);
            }
            setSelectedItem(getAdapterPosition());
            mCursor.moveToPosition(getSelectedItem());

            mCallback.onClick(getCurrentBook(), this);
            notifyItemChanged(getSelectedItem());
        }
    }


    /**
     * Get the book bound to a view in the recyclerView
     * @return
     */
    private Book getCurrentBook(){
        Book book=new Book();
        // I just retreive the book id and the title because it is better to get all the book informations inside a loader
        // so if the operation takes long the UI won't be blocked
        book.setId(mCursor.getString(mCursor.getColumnIndex(AlexandriaContract.BookEntry._ID)));
        book.setTitle(mCursor.getString(mCursor.getColumnIndex(AlexandriaContract.BookEntry.TITLE)));

        return book;
    }

    /**
     * Get the selected Item Index
     * @return
     */
    public int getSelectedItem() {
        return selectedItem;
    }


    public void setSelectedItem(int selectedItem) {
        this.selectedItem = selectedItem;
    }

    public BookListAdapter(Context context, BookAdapterOnClickHandler vh) {
        mContext = context;
        mCallback = vh;
    }

    // Interface to handle the Item Click listener on the View Holder
    public interface BookAdapterOnClickHandler {
     void onClick(Book book, BookAdapterViewHolder vh);
    }




    @Override
    public BookAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if ( viewGroup instanceof RecyclerView ) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.book_list_item, viewGroup, false);
            view.setFocusable(true);
            return new BookAdapterViewHolder(view);
        } else {
            throw new RuntimeException(mContext.getString(R.string.recycler_binding_error));
        }

    }

    @Override
    public void onBindViewHolder(BookAdapterViewHolder bookAdapterViewHolder, int i) {
        mCursor.moveToPosition(i);

        String imgUrl = mCursor.getString(mCursor.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));

        Glide.with(mContext)
                .load(imgUrl)
                .error(R.mipmap.default_book)
                .crossFade()
                .into(bookAdapterViewHolder.bookCover);


        String bookTitle = mCursor.getString(mCursor.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        bookAdapterViewHolder.bookTitle.setText(bookTitle);

        String bookSubTitle = mCursor.getString(mCursor.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        bookAdapterViewHolder.bookSubTitle.setText(bookSubTitle);

        bookAdapterViewHolder.itemView.setSelected(getSelectedItem() == i);

    }

    @Override
    public int getItemCount() {
        if ( null == mCursor ) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    public Cursor getCursor() {
        return mCursor;
    }


}
