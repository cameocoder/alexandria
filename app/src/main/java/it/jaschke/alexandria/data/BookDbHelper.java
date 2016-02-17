package it.jaschke.alexandria.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by saj on 22/12/14.
 */
public class BookDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "alexandria.db";

    public BookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_BOOK_TABLE = "CREATE TABLE " + BookContract.BookEntry.TABLE_NAME + " (" +
                BookContract.BookEntry._ID + " INTEGER PRIMARY KEY," +
                BookContract.BookEntry.TITLE + " TEXT NOT NULL," +
                BookContract.BookEntry.SUBTITLE + " TEXT ," +
                BookContract.BookEntry.DESC + " TEXT ," +
                BookContract.BookEntry.IMAGE_URL + " TEXT, " +
                "UNIQUE (" + BookContract.BookEntry._ID + ") ON CONFLICT IGNORE)";

        final String SQL_CREATE_AUTHOR_TABLE = "CREATE TABLE " + BookContract.AuthorEntry.TABLE_NAME + " (" +
                BookContract.AuthorEntry._ID + " INTEGER," +
                BookContract.AuthorEntry.AUTHOR + " TEXT," +
                " FOREIGN KEY (" + BookContract.AuthorEntry._ID + ") REFERENCES " +
                BookContract.BookEntry.TABLE_NAME + " (" + BookContract.BookEntry._ID + "))";

        final String SQL_CREATE_CATEGORY_TABLE = "CREATE TABLE " + BookContract.CategoryEntry.TABLE_NAME + " (" +
                BookContract.CategoryEntry._ID + " INTEGER," +
                BookContract.CategoryEntry.CATEGORY + " TEXT," +
                " FOREIGN KEY (" + BookContract.CategoryEntry._ID + ") REFERENCES " +
                BookContract.BookEntry.TABLE_NAME + " (" + BookContract.BookEntry._ID + "))";


        Log.d("sql-statments", SQL_CREATE_BOOK_TABLE);
        Log.d("sql-statments", SQL_CREATE_AUTHOR_TABLE);
        Log.d("sql-statments", SQL_CREATE_CATEGORY_TABLE);

        db.execSQL(SQL_CREATE_BOOK_TABLE);
        db.execSQL(SQL_CREATE_AUTHOR_TABLE);
        db.execSQL(SQL_CREATE_CATEGORY_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
