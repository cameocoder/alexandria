package it.jaschke.alexandria;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.jaschke.alexandria.data.BookContract;
import it.jaschke.alexandria.services.BookService;


public class AddBookFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "INTENT_TO_SCAN_ACTIVITY";
    private static final String SCAN_FORMAT = "scanFormat";
    private static final String SCAN_CONTENTS = "scanContents";
    public static final int SCAN_RESULT = 10;

    private static final int LOADER_ID = 1;
    private final String EAN_CONTENT = "eanContent";

    private String mScanFormat = "Format:";
    private String mScanContents = "Contents:";

    @Bind(R.id.ean)
    TextView ean;
    @Bind(R.id.scan_button)
    View scanButton;
    @Bind(R.id.save_button)
    View saveButton;
    @Bind(R.id.delete_button)
    View deleteButton;
    @Bind(R.id.bookCover)
    ImageView bookCover;
    @Bind(R.id.bookTitle)
    TextView bookTitle;
    @Bind(R.id.bookSubTitle)
    TextView bookSubTitle;
    @Bind(R.id.categories)
    TextView categories;
    @Bind(R.id.authors)
    TextView authors;

    public AddBookFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_book, container, false);
        ButterKnife.bind(this, view);

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
                }
                if (ean.length() < 13) {
                    clearFields();
                    return;
                }
                //Once we have an ISBN, start a book intent
                addBookIntent(ean);
                AddBookFragment.this.restartLoader();
            }
        });

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(getActivity());
                integrator.setPrompt("Scan Barcode");
                integrator.initiateScan();
                IntentIntegrator.forSupportFragment(AddBookFragment.this).initiateScan();
            }
        });

        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        if (preferences.getBoolean(getContext().getString(R.string.camera_access), false)) {
            scanButton.setVisibility(View.GONE);
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ean.setText("");
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean.getText().toString());
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                ean.setText("");
            }
        });

        if (savedInstanceState != null) {
            ean.setText(savedInstanceState.getString(EAN_CONTENT));
            ean.setHint("");
        }

        return view;
    }

    private void addBookIntent(String ean) {
        Intent bookIntent = new Intent(getActivity(), BookService.class);
        bookIntent.putExtra(BookService.EAN, ean);
        bookIntent.setAction(BookService.FETCH_BOOK);
        getActivity().startService(bookIntent);
    }

    @Override
    public void onResume() {
        super.onResume();
        android.support.v7.app.ActionBar actionbar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionbar != null) {
            actionbar.setTitle(R.string.scan);
        }
        View fab = getActivity().findViewById(R.id.fab);
        fab.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (ean != null) {
            outState.putString(EAN_CONTENT, ean.getText().toString());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_add_book, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_populate) {
            String[] books = {"1338099132", "9781338045758", "1408803011", "9781408803028"};
            for(String book: books) {
                addBookIntent(book);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult != null) {
            String thirteenDigitIsbn = scanningResult.getContents();
            ean.setText(thirteenDigitIsbn);
        } else {
            Intent messageIntent = new Intent(MainActivity.MESSAGE_EVENT);
            messageIntent.putExtra(MainActivity.MESSAGE_KEY, getResources().getString(R.string.not_found));
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(messageIntent);
        }
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (ean.getText().length() == 0) {
            return null;
        }
        String eanStr = ean.getText().toString();
        if (eanStr.length() == 10 && !eanStr.startsWith("978")) {
            eanStr = "978" + eanStr;
        }
        return new CursorLoader(
                getActivity(),
                BookContract.BookEntry.buildFullBookUri(Long.parseLong(eanStr)),
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

        String title = data.getString(data.getColumnIndex(BookContract.BookEntry.TITLE));
        bookTitle.setText(title);

        String subTitle = data.getString(data.getColumnIndex(BookContract.BookEntry.SUBTITLE));
        bookSubTitle.setText(subTitle);

        String author = data.getString(data.getColumnIndex(BookContract.AuthorEntry.AUTHOR));
        if (author != null) {
            String[] authorsArr = author.split(",");
            authors.setLines(authorsArr.length);
            authors.setText(author.replace(",", "\n"));
        }

        String imgUrl = data.getString(data.getColumnIndex(BookContract.BookEntry.IMAGE_URL));
        Picasso.with(getContext()).load(imgUrl).placeholder(R.drawable.ic_action_search).error(R.drawable.ic_action_search).into(bookCover);
        bookCover.setVisibility(View.VISIBLE);

        String category = data.getString(data.getColumnIndex(BookContract.CategoryEntry.CATEGORY));
        categories.setText(category);

        saveButton.setVisibility(View.VISIBLE);
        deleteButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    private void clearFields() {
        bookTitle.setText("");
        bookSubTitle.setText("");
        authors.setText("");
        categories.setText("");
        bookCover.setVisibility(View.INVISIBLE);
        saveButton.setVisibility(View.INVISIBLE);
        deleteButton.setVisibility(View.INVISIBLE);
    }
}
