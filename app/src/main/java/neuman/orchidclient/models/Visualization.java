package neuman.orchidclient.models;

import org.json.JSONObject;

import neuman.orchidclient.content.ObjectTypes;

/**
 * Created by neuman on 7/21/14.
 */
public class Visualization extends Item {


    public Visualization(JSONObject j){
        this.setJSON(j);
        this.setTitle(getTitle());

    }

    public Integer getLocationID() {

        return (Integer) get("location_id");
    }

    public Location getLocation(){
        return (Location) contentQueryMaker.get_model(ObjectTypes.TYPE_LOCATION, getLocationID());

    }



}