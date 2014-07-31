package neuman.orchidclient.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import neuman.orchidclient.content.ContentQueryMaker;


/**
 * Created by neuman on 7/15/14.
 */
public class Item {
    private String title = "No Title";
    private JSONObject json;
    protected String TAG = getClass().getSimpleName();
    protected ContentQueryMaker contentQueryMaker;
    public int color = -1;

    public Item(){
        this.json = new JSONObject();

    }

    public Item(String title){
        this.title = title;
        this.json = new JSONObject();
    }

    public Item(JSONObject j){
        try{
            this.title = j.getString("title");
        }catch(JSONException e){

        }

        this.json = j;
    }

    public Item(String i, JSONObject j){
        this.title = i;
        this.json = j;
    }

    public void put(String key, Object value){
        try {
            this.json.put(key, value);
        }
        catch (JSONException e){
            e.printStackTrace();
        }
    }

    public Object get(String key) {

        try{
            JSONObject record = this.getJSON();
            return record.get(key);

        }catch(JSONException e){
        }
        return null;
    }

    public Integer getId(){
        return (Integer)this.get("id");
    }

    public JSONArray getJSONArray(String key) {

        try{
            return this.getJSON().getJSONArray(key);

        }catch(JSONException e){
            Log.d(this.TAG, e.toString());
            e.printStackTrace();
        }
        return null;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String name) {
        this.title = name;
    }


    public JSONObject getJSON() {
        //Log.d(TAG, "item JSON: "+json);
        return json;
    }

    public void setJSON(JSONObject json) {
        this.json = json;
    }

    public ContentQueryMaker getContentQueryMaker() {
        return this.contentQueryMaker;
    }

    public void setContentQueryMaker(ContentQueryMaker contentQueryMaker) {this.contentQueryMaker = contentQueryMaker; }

}