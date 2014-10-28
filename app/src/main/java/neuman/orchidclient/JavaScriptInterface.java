package neuman.orchidclient;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import neuman.orchidclient.content.ContentQueryMaker;
import neuman.orchidclient.content.ObjectTypes;

/**
 * Created by neuman on 10/27/14.
 */
public class JavaScriptInterface {
    Context mContext;
    Integer mLocation_id;
    ContentQueryMaker contentQueryMaker;
    String vis_data;
    private String TAG = getClass().getSimpleName();

    /** Instantiate the interface and set the context */
    JavaScriptInterface(Context c, Integer location_id) {

        mContext = c;
        mLocation_id = location_id;
        contentQueryMaker = new ContentQueryMaker(mContext.getContentResolver());
        //Visualization mVis = (Visualization) contentQueryMaker.get_model(ObjectTypes.TYPE_VISUALIZATION, mLocation_id);
        //vis_data = mVis.getJSON().toString();
        Cursor mCursor = contentQueryMaker.get_all_of_object_type_cursor(ObjectTypes.TYPE_VISUALIZATION);

        // Some providers return null if an error occurs, others throw an exception
        if (null == mCursor) {
    /*
     * Insert code here to handle the error. Be sure not to use the cursor! You may want to
     * call android.util.Log.e() to log this error.
     *
     */
            // If the Cursor is empty, the provider found no matches
            Log.d(TAG, "Cursor Error");
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
                    JSONObject location_data = new JSONObject(jsonString);
                    if(location_data.getInt("location_id")==location_id){
                        vis_data = location_data.toString();
                    }
                }catch(JSONException e){
                    Log.d(TAG, e.toString());
                    e.printStackTrace();
                }
            }
        }
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public String get_series_data(){
        return vis_data;
    }
}