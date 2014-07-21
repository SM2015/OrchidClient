package neuman.orchidclient.models;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import neuman.orchidclient.content.ContentQueryMaker;


/**
 * Created by neuman on 7/15/14.
 */
public class Item {
    private String title;
    private JSONObject json;
    protected String TAG = getClass().getSimpleName();
    protected ContentQueryMaker contentQueryMaker;

    public Item(){

    }

    public Item(JSONObject j){
        this.title = getTitle();
        this.json = j;
        Log.d(TAG, "%%%%%%%%%%%% item created %%%%%%%%%%");
    }

    public Item(String i, JSONObject j){
        this.title = i;
        this.json = j;
    }

    public Object get(String key) {

        try{
            JSONObject record = this.getJSON();
            return record.get(key);

        }catch(JSONException e){
            Log.d(this.TAG, e.toString());
            e.printStackTrace();
        }
        return null;
    }

    public String getTitle() {
        this.setTitle((String) get("title"));
        if(this.title == null)
        {
            return "No Title";
        }
        return title;
    }

    public void setTitle(String name) {
        this.title = name;
    }

    public JSONObject getJSON() {
        return this.json;
    }

    public void setJSON(JSONObject json) {
        this.json = json;
    }

    public ContentQueryMaker getContentQueryMaker() {
        return this.contentQueryMaker;
    }

    public void setContentQueryMaker(ContentQueryMaker contentQueryMaker) {this.contentQueryMaker = contentQueryMaker; }

}