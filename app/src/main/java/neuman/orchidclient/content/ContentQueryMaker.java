package neuman.orchidclient.content;

import android.content.ContentResolver;
import android.database.Cursor;
import android.util.Log;

/**
 * Created by neuman on 7/17/14.
 */
public class ContentQueryMaker {
    private String TAG = getClass().getSimpleName();

    private ContentResolver contentResolver;

    public ContentQueryMaker(ContentResolver contentResolver){
        this.contentResolver = contentResolver;
    }

    public Cursor get_all_of_object_type(Integer object_type){
        // A "projection" defines the columns that will be returned for each row
        String[] mProjection =
                {
                        Contract.Entry._ID,    // Contract class constant for the _ID column name
                        Contract.Entry.COLUMN_NAME_OBJECTTYPE,
                        Contract.Entry.COLUMN_NAME_JSON,   // Contract class constant for the word column name
                };

        // Defines a string to contain the selection clause
        String mSelectionClause =  Contract.Entry.COLUMN_NAME_OBJECTTYPE+" = "+object_type;


        Log.d(TAG, Contract.Entry.CONTENT_URI.toString());
        Log.d(TAG,"mProjection: "+mProjection.toString());
        Cursor mCursor = contentResolver.query(
                Contract.Entry.CONTENT_URI,  // The content URI of the words table
                mProjection,                       // The columns to return for each row
                mSelectionClause,                   // Either null, or the word the user entered
                null,                    // Either empty, or the string the user entered
                Contract.Entry.COLUMN_NAME_OBJECTTYPE);                       // The sort order for the returned rows

        return mCursor;
    }

}
