package it.jaschke.alexandria;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.Map;
import java.util.Set;

import it.jaschke.alexandria.data.BookContract;
import it.jaschke.alexandria.data.BookDbHelper;

/**
 * Created by saj on 23/12/14.
 */
public class TestDb extends AndroidTestCase {
    public static final String LOG_TAG = TestDb.class.getSimpleName();

    public final static long ean = 9780137903955L;
    public final static String title = "Artificial Intelligence";
    public final static String subtitle = "A Modern Approach";
    public final static String imgUrl = "http://books.google.com/books/content?id=KI2WQgAACAAJ&printsec=frontcover&img=1&zoom=1";
    public final static String desc = "Presents a guide to artificial intelligence, covering such topics as intelligent agents, problem-solving, logical agents, planning, uncertainty, learning, and robotics.";
    public final static String author = "Stuart Jonathan Russell";
    public final static String category = "Computers";

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(BookDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new BookDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertReadDb() {

        BookDbHelper bookDbHelper = new BookDbHelper(mContext);
        SQLiteDatabase db = bookDbHelper.getWritableDatabase();

        ContentValues values = getBookValues();

        long retEan = db.insert(BookContract.BookEntry.TABLE_NAME, null, values);
        assertEquals(ean, retEan);

        String[] columns = {
                BookContract.BookEntry._ID,
                BookContract.BookEntry.TITLE,
                BookContract.BookEntry.IMAGE_URL,
                BookContract.BookEntry.SUBTITLE,
                BookContract.BookEntry.DESC
        };

        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                BookContract.BookEntry.TABLE_NAME,  // Table to Query
                columns,
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        validateCursor(cursor, values);

        values = getAuthorValues();


        retEan = db.insert(BookContract.AuthorEntry.TABLE_NAME, null, values);

        columns = new String[]{
                BookContract.AuthorEntry._ID,
                BookContract.AuthorEntry.AUTHOR
        };

        cursor = db.query(
                BookContract.AuthorEntry.TABLE_NAME,  // Table to Query
                columns,
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        validateCursor(cursor, values);
        // test category table

        values = getCategoryValues();
        retEan = db.insert(BookContract.CategoryEntry.TABLE_NAME, null, values);

        columns = new String[]{
                BookContract.CategoryEntry._ID,
                BookContract.CategoryEntry.CATEGORY
        };

        cursor = db.query(
                BookContract.CategoryEntry.TABLE_NAME,  // Table to Query
                columns,
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        validateCursor(cursor, values);

        bookDbHelper.close();

    }

    static void validateCursor(Cursor valueCursor, ContentValues expectedValues) {

        assertTrue(valueCursor.moveToFirst());

        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(columnName,idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
        valueCursor.close();
    }

    public static ContentValues getBookValues() {

        final ContentValues values = new ContentValues();
        values.put(BookContract.BookEntry._ID, ean);
        values.put(BookContract.BookEntry.TITLE, title);
        values.put(BookContract.BookEntry.IMAGE_URL, imgUrl);
        values.put(BookContract.BookEntry.SUBTITLE, subtitle);
        values.put(BookContract.BookEntry.DESC, desc);

        return values;
    }

    public static ContentValues getAuthorValues() {

        final ContentValues values= new ContentValues();
        values.put(BookContract.AuthorEntry._ID, ean);
        values.put(BookContract.AuthorEntry.AUTHOR, author);

        return values;
    }

    public static ContentValues getCategoryValues() {

        final ContentValues values= new ContentValues();
        values.put(BookContract.CategoryEntry._ID, ean);
        values.put(BookContract.CategoryEntry.CATEGORY, category);

        return values;
    }

    public static ContentValues getFullDetailValues() {

        final ContentValues values= new ContentValues();
        values.put(BookContract.BookEntry.TITLE, title);
        values.put(BookContract.BookEntry.IMAGE_URL, imgUrl);
        values.put(BookContract.BookEntry.SUBTITLE, subtitle);
        values.put(BookContract.BookEntry.DESC, desc);
        values.put(BookContract.AuthorEntry.AUTHOR, author);
        values.put(BookContract.CategoryEntry.CATEGORY, category);
        return values;
    }

    public static ContentValues getFullListValues() {

        final ContentValues values= new ContentValues();
        values.put(BookContract.BookEntry.TITLE, title);
        values.put(BookContract.BookEntry.IMAGE_URL, imgUrl);
        values.put(BookContract.AuthorEntry.AUTHOR, author);
        values.put(BookContract.CategoryEntry.CATEGORY, category);
        return values;
    }
}