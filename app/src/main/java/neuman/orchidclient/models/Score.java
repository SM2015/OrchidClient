package neuman.orchidclient.models;

import org.json.JSONObject;

import neuman.orchidclient.content.ObjectTypes;

/**
 * Created by neuman on 7/21/14.
 */
public class Score extends Item {


    public Score(JSONObject j){
        this.setJSON(j);
        this.setTitle(getTitle());

    }

    public Integer getIndicatorID() {

        return (Integer) get("indicator_id");
    }

    public Indicator getIndicator(){
        return (Indicator) contentQueryMaker.get_model(ObjectTypes.TYPE_INDICATOR, getIndicatorID());

    }



}