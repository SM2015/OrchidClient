package neuman.orchidclient.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import neuman.orchidclient.authentication.AccountGeneral;
import neuman.orchidclient.content.Contract;
import neuman.orchidclient.content.ObjectTypes;

/**
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private String TAG = getClass().getSimpleName();
    private AccountManager mAccountManager;
    /**
     * URL to fetch content from during a sync.
     *
     * <p>This points to the Android Developers Blog. (Side note: We highly recommend reading the
     * Android Developer Blog to stay up to date on the latest Android platform developments!)
     */
    private static final String LOCATIONS_URL = "http://192.168.1.127:9292/location/list/";
    private static final String INDICATORS_URL = "http://192.168.1.127:9292/indicator/list/";

    /**
     * Network connection timeout, in milliseconds.
     */
    private static final int NET_CONNECT_TIMEOUT_MILLIS = 15000;  // 15 seconds

    /**
     * Network read timeout, in milliseconds.
     */
    private static final int NET_READ_TIMEOUT_MILLIS = 10000;  // 10 seconds

    /**
     * Content resolver, for performing database operations.
     */
    // Define a variable to contain a content resolver instance
    ContentResolver mContentResolver;

    /**
     * Set up the sync adapter
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();
        mAccountManager = AccountManager.get(context);
        Log.d("SYNC","SyncAdapter autoInitialize");
    }

    /**
     * Set up the sync adapter. This form of the
     * constructor maintains compatibility with Android 3.0
     * and later platform versions
     */
    public SyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();
        mAccountManager = AccountManager.get(context);
        Log.d("SYNC","SyncAdapter");

    }

    /*
 * Specify the code you want to run in the sync adapter. The entire
 * sync adapter runs in a background thread, so you don't have to set
 * up your own background processing.
 */
    @Override
    public void onPerformSync(
            Account account,
            Bundle extras,
            String authority,
            ContentProviderClient provider,
            SyncResult syncResult){
        /*
         * Put the data transfer code here.
         */

        Log.i(TAG, "Beginning network synchronization");
        drop_contentProvider();
        Log.d(TAG, "account.name: "+account.name);
        Log.d(TAG, "account.type: "+account.type);
        Log.d(TAG, "mAccountManager: "+mAccountManager.toString());
        Log.d(TAG, "AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS: "+AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS);
        String locationJSON = make_authenticated_request(account, LOCATIONS_URL);
        String indicatorJSON = make_authenticated_request(account, INDICATORS_URL);

        JSONObject locationObject = new JSONObject();
        JSONArray locationList = new JSONArray();
        try{
        locationObject = new JSONObject(locationJSON);
        locationList = locationObject.getJSONArray("locations");
        for(int i=0; i < locationList.length(); i++){
            String individual_location_json = locationList.getString(i);
            Log.d(TAG, "Attempting to insert: "+individual_location_json.toString());
            JSONObject location_json = new JSONObject(individual_location_json);
            insert_into_provider(provider,syncResult,individual_location_json, ObjectTypes.TYPE_LOCATION,location_json.getInt("id"));
        }
        }catch(JSONException e){
            Log.d(TAG, e.toString());
        }

        JSONObject indicatorObject = new JSONObject();
        JSONArray indicatorList = new JSONArray();
        try{
            indicatorObject = new JSONObject(indicatorJSON);
            indicatorList = indicatorObject.getJSONArray("locations");
            for(int i=0; i < locationList.length(); i++){
                String individual_indicator_json = indicatorList.getString(i);
                Log.d(TAG, "Attempting to insert: "+individual_indicator_json.toString());
                JSONObject indicator_json = new JSONObject(individual_indicator_json);
                insert_into_provider(provider,syncResult,individual_indicator_json, ObjectTypes.TYPE_LOCATION,indicator_json.getInt("id"));
            }
        }catch(JSONException e){
            Log.d(TAG, e.toString());
        }


        Log.i(TAG, "Network synchronization complete");



    }

    private void drop_contentProvider(){
        getContext().getContentResolver().delete(Contract.Entry.CONTENT_URI, null, null);
    }

    private void insert_into_provider(ContentProviderClient provider, SyncResult syncResult, String value, Integer objecttype, Integer model_id){
        try {
            // Defines a new Uri object that receives the result of the insertion
            Uri mNewUri;

            // Defines an object to contain the new values to insert
            ContentValues mNewValues = new ContentValues();

        /*
         * Sets the values of each column and inserts the word. The arguments to the "put"
         * method are "column name" and "value"
         */
            mNewValues.put(Contract.Entry.COLUMN_NAME_OBJECTTYPE, objecttype);
            mNewValues.put(Contract.Entry.COLUMN_NAME_JSON, value);
            mNewValues.put(Contract.Entry.COLUMN_NAME_MODEL_ID, model_id);


            mNewUri = provider.insert(
                    Contract.Entry.CONTENT_URI,   // the user dictionary content URI
                    mNewValues                          // the values to insert
            );

        } catch (RemoteException e) {
            Log.e(TAG, "Error updating database: " + e.toString());
            syncResult.databaseError = true;
            return;
        }
    }

    private String make_authenticated_request(Account account, String url){
        String responseString = "no response";
        String authtoken = mAccountManager.peekAuthToken(account, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS);
        try {

            AndroidHttpClient httpclient = AndroidHttpClient.newInstance("Android");
            HttpGet request = new HttpGet(url);
            request.setHeader("X_REQUESTED_WITH", "XMLHttpRequest");
            String cookiestring = "sessionid="+authtoken.toString();
            Log.d(TAG,"Cookiestring: "+cookiestring);
            request.addHeader("Cookie", cookiestring);
            HttpResponse response = httpclient.execute(request);
            StatusLine statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
                Log.d(TAG, responseString);
                //..more logic
            } else{
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }

        }catch(Exception e){
            Log.d("HTTP exception", e.toString());
        }
        return responseString;
    }

}