package neuman.orchidclient.models;

import org.json.JSONObject;

/**
 * Created by neuman on 7/21/14.
 */
public class Location extends Item {

    public Location(JSONObject j){
        this.setJSON(j);
        this.setTitle(getTitle());

    }
}
