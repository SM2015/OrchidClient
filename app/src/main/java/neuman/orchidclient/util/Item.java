package neuman.orchidclient.util;

import org.json.JSONObject;

/**
 * Created by neuman on 7/15/14.
 */
public class Item {
    private String title;
    private JSONObject json;

    public Item(){

    }

    public Item(String i, JSONObject j){
        this.title = i;
        this.json = j;
    }

    public String getTitle() {
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

}