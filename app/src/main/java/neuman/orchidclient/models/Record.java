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

    public Object getFieldValue(String field_id){
        JSONArray values = getValues();
        try{
            for (int i = 0 ; i < values.length(); i++) {
                JSONObject obj = values.getJSONObject(i);
                if (obj.getString("field_id").equals(field_id)){
                    return obj.get("value");
                }
            }
        }catch(JSONException e){
            Log.d(this.TAG, e.toString());
            e.printStackTrace();
        }
        Log.d(TAG, "Couldn't getFieldValue("+field_id+")");
        return null;

    }

    public Indicator getIndicator(){
        return (Indicator) contentQueryMaker.get_model(ObjectTypes.TYPE_INDICATOR, getIndicatorID());

    }

    public Location getLocation(){
        return (Location) contentQueryMaker.get_model(ObjectTypes.TYPE_LOCATION, getLocationID());

    }


}