package it.jaschke.alexandria;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.jaschke.alexandria.data.BookContract;
import it.jaschke.alexandria.services.BookService;


public class BookDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EAN_KEY = "EAN";
    private static final int LOADER_ID = 10;
    private String ean;
    private String bookTitle;

    @Bind(R.id.fullBookCover)
    ImageView bookCoverView;
    @Bind(R.id.fullBookTitle)
    TextView bookTitleView;
    @Bind(R.id.fullBookSubTitle)
    TextView bookSubTitleView;
    @Bind(R.id.fullBookDesc)
    TextView bookDescView;
    @Bind(R.id.authors)
    TextView authorsView;
    @Bind(R.id.categories)
    TextView categoriesView;
    @Bind(R.id.delete_button)
    View deleteButton;
    @Nullable
    @Bind(R.id.item_detail_container)
    View rightContainer;

    public BookDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_detail, container, false);
        ButterKnife.bind(this, view);

        Bundle arguments = getArguments();
        if (arguments != null) {
            ean = arguments.getString(BookDetailFragment.EAN_KEY);
            getLoaderManager().restartLoader(LOADER_ID, null, this);
        }

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean);
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        View fab = getActivity().findViewById(R.id.fab);
        fab.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.book_detail, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem shareItem = menu.findItem(R.id.action_share);
        ShareActionProvider shareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        shareActionProvider.setShareIntent(getShareIntent());
    }

    protected Intent getShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + bookTitle);
        return shareIntent;
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                BookContract.BookEntry.buildFullBookUri(Long.parseLong(ean)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        bookTitle = data.getString(data.getColumnIndex(BookContract.BookEntry.TITLE));
        bookTitleView.setText(bookTitle);

        String subTitle = data.getString(data.getColumnIndex(BookContract.BookEntry.SUBTITLE));
        bookSubTitleView.setText(subTitle);

        String desc = data.getString(data.getColumnIndex(BookContract.BookEntry.DESC));
        bookDescView.setText(desc);

        String authors = data.getString(data.getColumnIndex(BookContract.AuthorEntry.AUTHOR));
        String[] authorsArr = authors.split(",");
        authorsView.setLines(authorsArr.length);
        authorsView.setText(authors.replace(",", "\n"));

        String imgUrl = data.getString(data.getColumnIndex(BookContract.BookEntry.IMAGE_URL));
        Picasso.with(getContext()).load(imgUrl).placeholder(R.drawable.ic_action_search).error(R.drawable.ic_action_search).into(bookCoverView);

        String categories = data.getString(data.getColumnIndex(BookContract.CategoryEntry.CATEGORY));
        categoriesView.setText(categories);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    @Override
    public void onPause() {
        super.onDestroyView();
        if (MainActivity.IS_TABLET && rightContainer == null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }
}