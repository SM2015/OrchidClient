package neuman.orchidclient.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import neuman.orchidclient.content.ObjectTypes;

/**
 * Created by neuman on 7/21/14.
 */
public class Record extends Item {


    public Record(JSONObject j){
        this.setJSON(j);
        this.setTitle(getTitle());

    }

    public Integer getIndicatorID() {

        return (Integer) get("indicator_id");
    }

    public Integer getLocationID() {

        return (Integer) get("location_id");
    }

    public JSONArray getValues(){
        return getJSONArray("values");
    }

    public String getFieldStringValue(String title){
        JSONArray values = getValues();
        try{
            for (int i = 0 ; i < values.length(); i++) {
                JSONObject obj = values.getJSONObject(i);
                if (obj.getString("name").equals(title)){
                    return obj.getString("value");
                }
            }
        }catch(JSONException e){
            Log.d(this.TAG, e.toString());
            e.printStackTrace();
        }
        Log.d(TAG, "Couldn't getFieldStringValue("+title+")");
        return null;

    }

    public Indicator getIndicator(){
        return (Indicator) contentQueryMaker.get_model(ObjectTypes.TYPE_INDICATOR, getIndicatorID());

    }

    public Location getLocation(){
        return (Location) contentQueryMaker.get_model(ObjectTypes.TYPE_LOCATION, getLocationID());

    }


}