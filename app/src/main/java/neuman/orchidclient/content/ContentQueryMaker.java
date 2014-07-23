package neuman.orchidclient.content;

import android.content.ContentResolver;
import android.database.Cursor;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import neuman.orchidclient.models.Indicator;
import neuman.orchidclient.models.Location;
import neuman.orchidclient.models.Record;

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

    public void drop_contentProvider_model(Integer objectType){
        // Defines a string to contain the selection clause
        String mSelectionClause =  Contract.Entry.COLUMN_NAME_OBJECTTYPE+" ="+ objectType;
        contentResolver.delete(Contract.Entry.CONTENT_URI, mSelectionClause, null);
    }

    public void drop_all_tables(){
        drop_contentProvider_model(ObjectTypes.TYPE_RECORD);
        drop_contentProvider_model(ObjectTypes.TYPE_INDICATOR);
        drop_contentProvider_model(ObjectTypes.TYPE_LOCATION);
    }

    public Cursor make_query(String selectionClause){
        // A "projection" defines the columns that will be returned for each row
        String[] mProjection =
                {
                        Contract.Entry._ID,    // Contract class constant for the _ID column name
                        Contract.Entry.COLUMN_NAME_OBJECTTYPE,
                        Contract.Entry.COLUMN_NAME_JSON,
                        Contract.Entry.COLUMN_NAME_MODEL_ID,
                };

        Log.d(TAG, Contract.Entry.CONTENT_URI.toString());
        Log.d(TAG,"mProjection: "+mProjection.toString());
        Cursor mCursor = contentResolver.query(
                Contract.Entry.CONTENT_URI,  // The content URI of the words table
                mProjection,                       // The columns to return for each row
                selectionClause,                   // Either null, or the word the user entered
                null,                    // Either empty, or the string the user entered
                Contract.Entry.COLUMN_NAME_OBJECTTYPE);                       // The sort order for the returned rows
        return mCursor;
    }

    public Object get_model(Integer model_type, Integer model_id){

        // Defines a string to contain the selection clause
        String mSelectionClause =
                Contract.Entry.COLUMN_NAME_OBJECTTYPE+" = "+model_type
                        +" AND "+Contract.Entry.COLUMN_NAME_MODEL_ID+" = "+model_id;

        Cursor mCursor = make_query(mSelectionClause);
        if (null == mCursor) {
    /*
     * Insert code here to handle the error. Be sure not to use the cursor! You may want to
     * call android.util.Log.e() to log this error.
     *
     */
            // If the Cursor is empty, the provider found no matches
            Log.d(TAG,"Cursor Error");
        } else if (mCursor.getCount() < 1) {

    /*
     * Insert code here to notify the user that the search was unsuccessful. This isn't necessarily
     * an error. You may want to offer the user the option to insert a new row, or re-type the
     * search term.
     */
            Log.d(TAG,"No results");

        } else {
            // Insert code here to do something with the results
            while (mCursor.moveToNext()) {
                Log.d(TAG,"*****CURSOR MOVED*****");
                Log.d(TAG, mCursor.getColumnName(0)+": "+mCursor.getString(0));
                Log.d(TAG, mCursor.getColumnName(1)+": "+mCursor.getString(1));
                String jsonString = mCursor.getString(2);
                Log.d(TAG, mCursor.getColumnName(2)+": "+jsonString);
                try{
                    JSONObject mJSON = new JSONObject(jsonString);
                    if( model_type == ObjectTypes.TYPE_RECORD){
                            Record r = new Record(mJSON);
                            return r;
                    }
                    else if( model_type == ObjectTypes.TYPE_INDICATOR){
                        Indicator r = new Indicator(mJSON);
                        return r;
                    }
                    else if( model_type == ObjectTypes.TYPE_LOCATION){
                        Location r = new Location(mJSON);
                        return r;
                    }

                }catch(JSONException e){
                    Log.d(TAG, e.toString());
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("hh:mm MM/dd/yy");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

}
