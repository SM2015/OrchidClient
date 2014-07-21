package neuman.orchidclient.models;

import org.json.JSONObject;

/**
 * Created by neuman on 7/21/14.
 */
public class Record extends Item {


    public Record(JSONObject j){
        this.setJSON(j);
        this.setTitle(getTitle());

    }


    public Integer getIndicatorID() {

        return (Integer) get("indicator");
    }


}