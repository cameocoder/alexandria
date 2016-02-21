package it.jaschke.alexandria;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.jaschke.alexandria.api.BookListAdapter;
import it.jaschke.alexandria.api.Callback;
import it.jaschke.alexandria.data.BookContract;


public class BookListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final int BOOK_LOADER = 10;

    private BookListAdapter bookListAdapter;
    private int position = ListView.INVALID_POSITION;

    @Bind(R.id.listOfBooks)
    ListView bookList;
    @Bind(R.id.searchText)
    TextView searchText;
    @Bind(R.id.searchButton)
    View searchButton;

    public BookListFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(BOOK_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_of_books, container, false);
        ButterKnife.bind(this, view);

        bookListAdapter = new BookListAdapter(getActivity(), null);
        bookList.setAdapter(bookListAdapter);

        searchButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BookListFragment.this.restartLoader();
                    }
                }
        );


        bookList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = bookListAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    ((Callback) getActivity())
                            .onItemSelected(cursor.getString(cursor.getColumnIndex(BookContract.BookEntry._ID)));
                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        View fab = getActivity().findViewById(R.id.fab);
        fab.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }


    private void restartLoader() {
        getLoaderManager().restartLoader(BOOK_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id) {
            case BOOK_LOADER: {
                final String selection = BookContract.BookEntry.TITLE + " LIKE ? OR " + BookContract.BookEntry.SUBTITLE + " LIKE ? ";
                String searchString = searchText.getText().toString();

                if (searchString.length() > 0) {
                    searchString = "%" + searchString + "%";
                    return new CursorLoader(
                            getActivity(),
                            BookContract.BookEntry.CONTENT_URI,
                            null,
                            selection,
                            new String[]{searchString, searchString},
                            null
                    );
                }

                return new CursorLoader(
                        getActivity(),
                        BookContract.BookEntry.CONTENT_URI,
                        null,
                        null,
                        null,
                        null
                );
            }
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (bookListAdapter == null) {
            bookListAdapter = new BookListAdapter(getActivity(), data);
        } else {
            bookListAdapter.swapCursor(data);
        }
        if (position != ListView.INVALID_POSITION) {
            bookList.smoothScrollToPosition(position);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        bookListAdapter.swapCursor(null);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.books);
    }
}
