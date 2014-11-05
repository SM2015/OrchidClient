package neuman.orchidclient.content;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    public Cursor get_all_of_model_type_cursor(Integer object_type){
        // A "projection" defines the columns that will be returned for each row
        String[] mProjection =
                {
                        Contract.Entry._ID,    // Contract class constant for the _ID column name
                        Contract.Entry.COLUMN_NAME_OBJECTTYPE,
                        Contract.Entry.COLUMN_NAME_JSON,   // Contract class constant for the word column name
                };

        // Defines a string to contain the selection clause
        String mSelectionClause =  Contract.Entry.COLUMN_NAME_OBJECTTYPE+" = "+object_type;


        //Log.d(TAG, Contract.Entry.CONTENT_URI.toString());
        //Log.d(TAG,"mProjection: "+mProjection.toString());
        Cursor mCursor = contentResolver.query(
                Contract.Entry.CONTENT_URI,  // The content URI of the words table
                mProjection,                       // The columns to return for each row
                mSelectionClause,                   // Either null, or the word the user entered
                null,                    // Either empty, or the string the user entered
                Contract.Entry.COLUMN_NAME_OBJECTTYPE);                       // The sort order for the returned rows

        return mCursor;
    }

    public ArrayList get_all_of_object_type(Integer object_type){
        ArrayList<JSONObject> output = new ArrayList<JSONObject>();
        Cursor mCursor = this.get_all_of_model_type_cursor(object_type);
        if (null == mCursor) {
            // If the Cursor is empty, the provider found no matches
            //Log.d(TAG,"Cursor Error");
        } else if (mCursor.getCount() < 1) {
            //Log.d(TAG,"No results");
        } else {
            while (mCursor.moveToNext()) {
                //Log.d(TAG,"*****CURSOR MOVED*****");
                String jsonString = mCursor.getString(2);
                try{
                    JSONObject mJSON = new JSONObject(jsonString);
                    output.add(mJSON);
                }catch(JSONException e){
                    Log.d(TAG, e.toString());
                    e.printStackTrace();
                }
            }
        }
        return output;
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
        drop_contentProvider_model(ObjectTypes.TYPE_SCORE);
    }

    public void drop_row(Integer row){
        //Log.d(TAG, "Dropping row: "+row);
        // Defines a string to contain the selection clause
        String mSelectionClause =  Contract.Entry._ID+" ="+row+"";
        contentResolver.delete(Contract.Entry.CONTENT_URI, mSelectionClause, null);
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

        //Log.d(TAG, Contract.Entry.CONTENT_URI.toString());
        //Log.d(TAG,"mProjection: "+mProjection.toString());
        Cursor mCursor = contentResolver.query(
                Contract.Entry.CONTENT_URI,  // The content URI of the words table
                mProjection,                       // The columns to return for each row
                selectionClause,                   // Either null, or the word the user entered
                null,                    // Either empty, or the string the user entered
                Contract.Entry.COLUMN_NAME_OBJECTTYPE);                       // The sort order for the returned rows
        return mCursor;
    }

    public void insert_message(String message, String breakpoint_tag){
        // Defines an object to contain the new values to insert
        ContentValues mNewValues = new ContentValues();
        JSONObject json = new JSONObject();
        try {

            json.put("title", message);
            json.put("breakpoint", breakpoint_tag);
        }catch(JSONException e){
            Log.d(TAG, e.toString());
            e.printStackTrace();
        }

        /*
         * Sets the values of each column and inserts the word. The arguments to the "put"
         * method are "column name" and "value"
         */
        mNewValues.put(Contract.Entry.COLUMN_NAME_OBJECTTYPE, ObjectTypes.TYPE_USERMESSAGE);
        mNewValues.put(Contract.Entry.COLUMN_NAME_JSON, json.toString());
        mNewValues.put(Contract.Entry.COLUMN_NAME_MODEL_ID, -1);

        contentResolver.insert(Contract.Entry.CONTENT_URI, mNewValues);
    }

    public Object get_model(Integer model_type, Integer model_id){

        // Defines a string to contain the selection clause
        String mSelectionClause =
                Contract.Entry.COLUMN_NAME_OBJECTTYPE+" = "+model_type
                        +" AND "+Contract.Entry.COLUMN_NAME_MODEL_ID+" = "+model_id;

        Cursor mCursor = make_query(mSelectionClause);
        if (null == mCursor) {
            // If the Cursor is empty, the provider found no matches
            Log.d(TAG,"Cursor Error");
        } else if (mCursor.getCount() < 1) {
            Log.d(TAG,"No results");

        } else {
            // Insert code here to do something with the results
            while (mCursor.moveToNext()) {
                //Log.d(TAG,"*****CURSOR MOVED*****");
                //Log.d(TAG, mCursor.getColumnName(0)+": "+mCursor.getString(0));
                //Log.d(TAG, mCursor.getColumnName(1)+": "+mCursor.getString(1));
                String jsonString = mCursor.getString(2);
                //Log.d(TAG, mCursor.getColumnName(2)+": "+jsonString);
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

    public JSONObject get_matching_object(Integer model_type, String key, Object value){

        ArrayList<JSONObject> jsons = get_all_of_object_type(model_type);
        for(JSONObject j :jsons){
            try{
                if(j.getInt(key)==value){
                    return j;
                }
            }catch(JSONException e){
                e.printStackTrace();
            }
        }

        return null;
    }

    public Integer get_model_count(Integer model_type){
        // Defines a string to contain the selection clause
        String mSelectionClause = Contract.Entry.COLUMN_NAME_OBJECTTYPE+" = "+model_type;

        Cursor mCursor = make_query(mSelectionClause);
        if (null == mCursor) {
    /*
     * Insert code here to handle the error. Be sure not to use the cursor! You may want to
     * call android.util.Log.e() to log this error.
     *
     */
            // If the Cursor is empty, the provider found no matches
            Log.d(TAG,"Cursor Error");
        }else{
            return mCursor.getCount();
        }

        return null;

    }

    public static Date getCurrentTime(){
        return new Date();
    }

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("hh:mm MM/dd/yy");
        Date now = getCurrentTime();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    public void update_row_json(String json_string, Integer row_id){
            String mSelectionClause = null;
            // Defines a new Uri object that receives the result of the insertion
            Uri mNewUri;
            // Defines an object to contain the new values to insert
            ContentValues mNewValues = new ContentValues();
            try {
                JSONObject json = new JSONObject(json_string);
                //Log.d(TAG, "update row with json: "+json_string);
                row_id = json.getInt("row_id");
                mSelectionClause = "_ID"+" = "+row_id;
                //Log.d(TAG, "Trying to save to :"+mSelectionClause);
            }catch(JSONException e){
                e.printStackTrace();
            }
        /*
         * Sets the values of each column and inserts the json. The arguments to the "put"
         * method are "column name" and "value"
         */
            mNewValues.put(Contract.Entry.COLUMN_NAME_JSON, json_string);
            this.contentResolver.update( Contract.Entry.CONTENT_URI, mNewValues, mSelectionClause, null );

    }

    public void save_to_provider(String json_string, Integer object_type, Integer model_id){
            Integer row_id = null;
            String mSelectionClause = null;
            // Defines a new Uri object that receives the result of the insertion
            Uri mNewUri;

            // Defines an object to contain the new values to insert
            ContentValues mNewValues = new ContentValues();


            try {
                JSONObject json = new JSONObject(json_string);
                //Log.d(TAG, "save_to_provider: "+json_string);
                mSelectionClause =
                        Contract.Entry.COLUMN_NAME_OBJECTTYPE+" = "+object_type
                                +" AND "+"_ID"+" = "+row_id;
                //Log.d(TAG, "Trying to save to :"+mSelectionClause);
            }catch(JSONException e){
                e.printStackTrace();
            }



        /*
         * Sets the values of each column and inserts the word. The arguments to the "put"
         * method are "column name" and "value"
         */
            mNewValues.put(Contract.Entry.COLUMN_NAME_OBJECTTYPE, object_type);
            mNewValues.put(Contract.Entry.COLUMN_NAME_JSON, json_string);
            if(model_id !=null) {
                mNewValues.put(Contract.Entry.COLUMN_NAME_MODEL_ID, model_id);
            }


            if(row_id != null){
                this.contentResolver.update( Contract.Entry.CONTENT_URI, mNewValues, mSelectionClause, null );
            }
            else{
                this.contentResolver.insert(Contract.Entry.CONTENT_URI, mNewValues);
            }

    }

}
