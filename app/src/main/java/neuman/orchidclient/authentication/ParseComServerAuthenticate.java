package neuman.orchidclient.authentication;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.http.AndroidHttpClient;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the comminication with Parse.com
 *
 * User: udinic
 * Date: 3/27/13
 * Time: 3:30 AM
 */
public class ParseComServerAuthenticate implements neuman.orchidclient.authentication.ServerAuthenticate {

    private String TAG = "Parse";
    private Context context;
    private SharedPreferences prefs;

    public ParseComServerAuthenticate(Context context_in){

        context = context_in;
    }

    @Override
    public String userSignUp(String name, String email, String pass, String authType) throws Exception {
        //Dummy to fulfil the requirement of the interface
        //this app does not allow signup in the android client
        return null;
    }

    @Override
    public String userSignIn(String user, String pass, String authType) throws Exception {
        Log.d("Parse", "userSignIn");

        DefaultHttpClient httpClient = new DefaultHttpClient();
        String url = "/user/login/";
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String hostname = settings.getString("example_text", "NO HOSTNAME");

        Log.i(TAG, "Beginning network sign in");
        String responseString = "no response";
        String authtoken = null;
        try {

            AndroidHttpClient httpclient = AndroidHttpClient.newInstance("Android");
            Log.d(TAG, "POSTING REQUEST TO: "+hostname+url);
            HttpPost request = new HttpPost(hostname+url);
            request.setHeader("X_REQUESTED_WITH", "XMLHttpRequest");
            HttpParams params = new BasicHttpParams();
            //params.setParameter("email", user);
            //params.setParameter("password", pass);
            //request.setParams(params);

            List <NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("email", user));
            nameValuePairs.add(new BasicNameValuePair("password", pass));
            request.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            HttpResponse response = httpclient.execute(request);
            StatusLine statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){

                //..more logic
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
                Log.d(TAG, responseString);
                JSONObject meJSON = new JSONObject(responseString);
                authtoken = meJSON.get("sessionid").toString();
            } else{
                //Closes the connection.
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
                Log.d(TAG, responseString);
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }




        }catch(Exception e){
            Log.d("HTTP exception", e.toString());
        }
        httpClient = null;



//        httpGet.getParams().setParameter("username", user).setParameter("password", pass);


        Log.i(TAG, "finishing network sign in");
        Log.d("Parse", "got authtoken: "+authtoken);
        return authtoken;
    }


    private class ParseComError implements Serializable {
        int code;
        String error;
    }
    private class User implements Serializable {

        private String firstName;
        private String lastName;
        private String username;
        private String phone;
        private String objectId;
        public String sessionToken;
        private String gravatarId;
        private String avatarUrl;


        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getObjectId() {
            return objectId;
        }

        public void setObjectId(String objectId) {
            this.objectId = objectId;
        }

        public String getSessionToken() {
            return sessionToken;
        }

        public void setSessionToken(String sessionToken) {
            this.sessionToken = sessionToken;
        }

        public String getGravatarId() {
            return gravatarId;
        }

        public void setGravatarId(String gravatarId) {
            this.gravatarId = gravatarId;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }
    }
}
