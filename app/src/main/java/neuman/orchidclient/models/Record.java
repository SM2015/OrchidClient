package neuman.orchidclient.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import neuman.orchidclient.content.ObjectTypes;

/**
 * Created by neuman on 7/21/14.
 */
public class Record extends Item {


    public Record(JSONObject j){
        super(j);
        //this.setJSON(j);
        //this.setTitle(getTitle());

    }

    public Integer getIndicatorID() {

        return (Integer) get("indicator_id");
    }

    public Integer getLocationID() {

        return (Integer) get("location_id");
    }

    public Integer get_row_id() {

        return (Integer) get("row_id");
    }

    public Boolean is_scored(){
        Boolean scored = (Boolean) this.get("scored");
        if(scored!=null){
            return scored;
        }
        return false;
    }

    public JSONArray getValues(){
        Log.d(TAG, "VALUES JSON: "+this.getJSON().toString());
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

    public Boolean is_passing(ArrayList<Integer> checkbox_field_ids){
        JSONArray values = getValues();
        try{
            //look through all values checking to see if any checkboxes are false
            for (int i = 0 ; i < values.length(); i++) {
                JSONObject obj = values.getJSONObject(i);
                if (checkbox_field_ids.contains(obj.getInt("field_id"))){
                    if(obj.get("value").equals(false)){
                        return false;
                    }
                }
            }
        }catch(JSONException e){
            Log.d(this.TAG, e.toString());
            e.printStackTrace();
        }
        return true;
    }

    public Indicator getIndicator(){
        return (Indicator) contentQueryMaker.get_model(ObjectTypes.TYPE_INDICATOR, getIndicatorID());

    }

    public Location getLocation(){
        return (Location) contentQueryMaker.get_model(ObjectTypes.TYPE_LOCATION, getLocationID());

    }


}