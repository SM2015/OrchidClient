package neuman.orchidclient.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import neuman.orchidclient.content.ObjectTypes;

/**
 * Created by neuman on 7/21/14.
 */
public class Indicator extends Item {

    //these are used in the scoring process
    private Integer total_records = 0;
    private Integer passing_records = 0;
    private Float percentage = new Float(100.00);

    public Indicator(JSONObject j){
        super(j);

    }
    public JSONArray getFields(){
        return getJSONArray("fields");
    }

    public ArrayList<Integer> get_boolean_field_ids() {

        ArrayList<Integer> field_ids = new ArrayList<Integer>();
        try {
            JSONArray fieldsList = getFields();
            for (int i = 0; i < fieldsList.length(); i++) {
                int colorPos = i % ObjectTypes.colors.length;
                JSONObject field = fieldsList.getJSONObject(i);
                if (field.getString("field_type").equals("CHECKBOX")){
                    field_ids.add(field.getInt("id"));
                }
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
        return field_ids;
    }

    public Float getPercentage(){
        if(this.total_records==0){
            //nothing doesn't count for anything
            return new Float(0.00);
        }
        return (passing_records * 100.0f) / total_records;
    }

    public void incrementTotal_records(){
        this.total_records++;
    }

    public void incrementPassing_records(){
        this.passing_records++;
    }

    public Float getPassing_percentage(){
        try {
            return (float) this.getJSON().getLong("passing_percentage");
        }catch(JSONException e){
            return null;
        }
    }
}
