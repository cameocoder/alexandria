package it.jaschke.alexandria.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by saj on 24/12/14.
 */
public class BookProvider extends ContentProvider {

    private static final int BOOK_ID = 100;
    private static final int BOOK = 101;

    private static final int AUTHOR_ID = 200;
    private static final int AUTHOR = 201;

    private static final int CATEGORY_ID = 300;
    private static final int CATEGORY = 301;

    private static final int BOOK_FULL = 500;
    private static final int BOOK_FULLDETAIL = 501;

    private static final UriMatcher uriMatcher = buildUriMatcher();

    private BookDbHelper bookDbHelper;

    private static final SQLiteQueryBuilder bookFull;

    static {
        bookFull = new SQLiteQueryBuilder();
        bookFull.setTables(
                BookContract.BookEntry.TABLE_NAME + " LEFT OUTER JOIN " +
                        BookContract.AuthorEntry.TABLE_NAME + " USING (" + BookContract.BookEntry._ID + ")" +
                        " LEFT OUTER JOIN " + BookContract.CategoryEntry.TABLE_NAME + " USING (" + BookContract.BookEntry._ID + ")");
    }


    private static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = BookContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, BookContract.PATH_BOOKS + "/#", BOOK_ID);
        matcher.addURI(authority, BookContract.PATH_AUTHORS + "/#", AUTHOR_ID);
        matcher.addURI(authority, BookContract.PATH_CATEGORIES + "/#", CATEGORY_ID);

        matcher.addURI(authority, BookContract.PATH_BOOKS, BOOK);
        matcher.addURI(authority, BookContract.PATH_AUTHORS, AUTHOR);
        matcher.addURI(authority, BookContract.PATH_CATEGORIES, CATEGORY);

        matcher.addURI(authority, BookContract.PATH_FULLBOOK + "/#", BOOK_FULLDETAIL);
        matcher.addURI(authority, BookContract.PATH_FULLBOOK, BOOK_FULL);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        bookDbHelper = new BookDbHelper(getContext());
        return true;

    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (uriMatcher.match(uri)) {
            case BOOK:
                retCursor = bookDbHelper.getReadableDatabase().query(
                        BookContract.BookEntry.TABLE_NAME,
                        projection,
                        selection,
                        selection == null ? null : selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case AUTHOR:
                retCursor = bookDbHelper.getReadableDatabase().query(
                        BookContract.AuthorEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case CATEGORY:
                retCursor = bookDbHelper.getReadableDatabase().query(
                        BookContract.CategoryEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case BOOK_ID:
                retCursor = bookDbHelper.getReadableDatabase().query(
                        BookContract.BookEntry.TABLE_NAME,
                        projection,
                        BookContract.BookEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case AUTHOR_ID:
                retCursor = bookDbHelper.getReadableDatabase().query(
                        BookContract.AuthorEntry.TABLE_NAME,
                        projection,
                        BookContract.AuthorEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case CATEGORY_ID:
                retCursor = bookDbHelper.getReadableDatabase().query(
                        BookContract.CategoryEntry.TABLE_NAME,
                        projection,
                        BookContract.CategoryEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case BOOK_FULLDETAIL:
                String[] bfd_projection = {
                        BookContract.BookEntry.TABLE_NAME + "." + BookContract.BookEntry.TITLE,
                        BookContract.BookEntry.TABLE_NAME + "." + BookContract.BookEntry.SUBTITLE,
                        BookContract.BookEntry.TABLE_NAME + "." + BookContract.BookEntry.IMAGE_URL,
                        BookContract.BookEntry.TABLE_NAME + "." + BookContract.BookEntry.DESC,
                        "group_concat(DISTINCT " + BookContract.AuthorEntry.TABLE_NAME + "." + BookContract.AuthorEntry.AUTHOR + ") as " + BookContract.AuthorEntry.AUTHOR,
                        "group_concat(DISTINCT " + BookContract.CategoryEntry.TABLE_NAME + "." + BookContract.CategoryEntry.CATEGORY + ") as " + BookContract.CategoryEntry.CATEGORY
                };
                retCursor = bookFull.query(bookDbHelper.getReadableDatabase(),
                        bfd_projection,
                        BookContract.BookEntry.TABLE_NAME + "." + BookContract.BookEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        selectionArgs,
                        BookContract.BookEntry.TABLE_NAME + "." + BookContract.BookEntry._ID,
                        null,
                        sortOrder);
                break;
            case BOOK_FULL:
                String[] bf_projection = {
                        BookContract.BookEntry.TABLE_NAME + "." + BookContract.BookEntry.TITLE,
                        BookContract.BookEntry.TABLE_NAME + "." + BookContract.BookEntry.IMAGE_URL,
                        "group_concat(DISTINCT " + BookContract.AuthorEntry.TABLE_NAME + "." + BookContract.AuthorEntry.AUTHOR + ") as " + BookContract.AuthorEntry.AUTHOR,
                        "group_concat(DISTINCT " + BookContract.CategoryEntry.TABLE_NAME + "." + BookContract.CategoryEntry.CATEGORY + ") as " + BookContract.CategoryEntry.CATEGORY
                };
                retCursor = bookFull.query(bookDbHelper.getReadableDatabase(),
                        bf_projection,
                        null,
                        selectionArgs,
                        BookContract.BookEntry.TABLE_NAME + "." + BookContract.BookEntry._ID,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }


    @Override
    public String getType(Uri uri) {
        final int match = uriMatcher.match(uri);

        switch (match) {
            case BOOK_FULLDETAIL:
                return BookContract.BookEntry.CONTENT_ITEM_TYPE;
            case BOOK_ID:
                return BookContract.BookEntry.CONTENT_ITEM_TYPE;
            case AUTHOR_ID:
                return BookContract.AuthorEntry.CONTENT_ITEM_TYPE;
            case CATEGORY_ID:
                return BookContract.CategoryEntry.CONTENT_ITEM_TYPE;
            case BOOK:
                return BookContract.BookEntry.CONTENT_TYPE;
            case AUTHOR:
                return BookContract.AuthorEntry.CONTENT_TYPE;
            case CATEGORY:
                return BookContract.CategoryEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = bookDbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        Uri returnUri;
        switch (match) {
            case BOOK: {
                long _id = db.insert(BookContract.BookEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = BookContract.BookEntry.buildBookUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                getContext().getContentResolver().notifyChange(BookContract.BookEntry.buildFullBookUri(_id), null);
                break;
            }
            case AUTHOR: {
                long _id = db.insert(BookContract.AuthorEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = BookContract.AuthorEntry.buildAuthorUri(values.getAsLong("_id"));
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case CATEGORY: {
                long _id = db.insert(BookContract.CategoryEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = BookContract.CategoryEntry.buildCategoryUri(values.getAsLong("_id"));
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = bookDbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case BOOK:
                rowsDeleted = db.delete(
                        BookContract.BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case AUTHOR:
                rowsDeleted = db.delete(
                        BookContract.AuthorEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CATEGORY:
                rowsDeleted = db.delete(
                        BookContract.CategoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOK_ID:
                rowsDeleted = db.delete(
                        BookContract.BookEntry.TABLE_NAME,
                        BookContract.BookEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (selection == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = bookDbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int rowsUpdated;
        switch (match) {
            case BOOK:
                rowsUpdated = db.update(BookContract.BookEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case AUTHOR:
                rowsUpdated = db.update(BookContract.AuthorEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case CATEGORY:
                rowsUpdated = db.update(BookContract.CategoryEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}