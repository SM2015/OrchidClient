package neuman.orchidclient.models;

import org.json.JSONObject;

import neuman.orchidclient.content.ObjectTypes;

/**
 * Created by neuman on 7/21/14.
 */
public class Photo extends ModelItem {


    public Photo(String title){
        super(title);
    }

    public Photo(JSONObject j){
        super(j);
        //this.setJSON(j);
        //this.setTitle(getTitle());

    }



    public Integer getLocationID() {

        return (Integer) get("location_id");
    }

    public String getPath() {
        String output =  (String) get("path");
        return output;

    }

    public Location getLocation(){
        return (Location) contentQueryMaker.get_model(ObjectTypes.TYPE_LOCATION, getLocationID());

    }


}