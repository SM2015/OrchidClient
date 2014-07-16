package neuman.orchidclient.content;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

/*
 * Define an implementation of ContentProvider that stubs out
 * all methods
 */
public class StubProvider extends ContentProvider {
    Database mDatabaseHelper;
    /**
     * Content authority for this provider.
     */
    private static final String AUTHORITY = Contract.CONTENT_AUTHORITY;
    /**
     * URI ID for route: /entries
     */
    public static final int ROUTE_ENTRIES = 1;

    /**
     * URI ID for route: /entries/{ID}
     */
    public static final int ROUTE_ENTRIES_ID = 2;
    /*
     * Always return true, indicating that the
     * provider loaded correctly.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(AUTHORITY, "entries", ROUTE_ENTRIES);
        sUriMatcher.addURI(AUTHORITY, "entries/*", ROUTE_ENTRIES_ID);
    }
    @Override
    public boolean onCreate() {
        Log.d("SYNC", "StubProvider onCreate");
        mDatabaseHelper = new Database(getContext());
        return true;
    }

    /**
     * Perform a database query by URI.
     *
     * <p>Currently supports returning all entries (/entries) and individual entries by ID
     * (/entries/{ID}).
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Log.d("SYNC", "StubProvider query");
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        neuman.orchidclient.util.SelectionBuilder builder = new neuman.orchidclient.util.SelectionBuilder();
        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch) {
            case ROUTE_ENTRIES_ID:
                // Return a single entry, by ID.
                String id = uri.getLastPathSegment();
                builder.where(Contract.Entry._ID + "=?", id);
            case ROUTE_ENTRIES:
                // Return all known entries.
                builder.table(Contract.Entry.TABLE_NAME)
                        .where(selection, selectionArgs);
                Cursor c = builder.query(db, projection, sortOrder);
                // Note: Notification URI must be manually set here for loaders to correctly
                // register ContentObservers.
                Context ctx = getContext();
                assert ctx != null;
                c.setNotificationUri(ctx.getContentResolver(), uri);
                return c;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * Insert a new entry into the database.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d("SYNC", "StubProvider insert");
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        assert db != null;
        final int match = sUriMatcher.match(uri);
        Uri result;
        switch (match) {
            case ROUTE_ENTRIES:
                long id = db.insertOrThrow(Contract.Entry.TABLE_NAME, null, values);
                result = Uri.parse(Contract.Entry.CONTENT_URI + "/" + id);
                break;
            case ROUTE_ENTRIES_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return result;
    }

    /**
     * Delete an entry by database by URI.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        neuman.orchidclient.util.SelectionBuilder builder = new neuman.orchidclient.util.SelectionBuilder();
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        switch (match) {
            case ROUTE_ENTRIES:
                count = builder.table(Contract.Entry.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_ENTRIES_ID:
                String id = uri.getLastPathSegment();
                count = builder.table(Contract.Entry.TABLE_NAME)
                        .where(Contract.Entry._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    /**
     * Update an etry in the database by URI.
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.d("SYNC", "StubProvider update");
        neuman.orchidclient.util.SelectionBuilder builder = new neuman.orchidclient.util.SelectionBuilder();
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        switch (match) {
            case ROUTE_ENTRIES:
                count = builder.table(Contract.Entry.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_ENTRIES_ID:
                String id = uri.getLastPathSegment();
                count = builder.table(Contract.Entry.TABLE_NAME)
                        .where(Contract.Entry._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        Log.d("SYNC", "StubProvider getType");
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ROUTE_ENTRIES:
                return Contract.Entry.CONTENT_TYPE;
            case ROUTE_ENTRIES_ID:
                return Contract.Entry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

    }

    static class Database extends SQLiteOpenHelper {
        /** Schema version. */
        public static final int DATABASE_VERSION = 1;
        /** Filename for SQLite file. */
        public static final String DATABASE_NAME = "orchid.db";

        private static final String TYPE_TEXT = " TEXT";
        private static final String TYPE_INTEGER = " INTEGER";
        private static final String COMMA_SEP = ",";
        /** SQL statement to create "entry" table. */
        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + Contract.Entry.TABLE_NAME + " (" +
                        Contract.Entry._ID + " INTEGER PRIMARY KEY," +
                        Contract.Entry.COLUMN_NAME_OBJECTTYPE + TYPE_TEXT + COMMA_SEP +
                        Contract.Entry.COLUMN_NAME_MODEL_ID + TYPE_INTEGER + COMMA_SEP +
                        Contract.Entry.COLUMN_NAME_JSON    + TYPE_TEXT + ")";

        /** SQL statement to drop "entry" table. */
        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + Contract.Entry.TABLE_NAME;

        public Database(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }
    }

}