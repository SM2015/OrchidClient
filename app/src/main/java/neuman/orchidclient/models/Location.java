package neuman.orchidclient.models;

import android.location.LocationManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by neuman on 7/21/14.
 */
public class Location extends Item {

    public Location(JSONObject j){
        this.setJSON(j);
        this.setTitle(getTitle());

    }

    public android.location.Location get_gps_location(){
        android.location.Location orchid_location = new android.location.Location(LocationManager.GPS_PROVIDER);
        try {
            orchid_location.setLatitude(getJSON().getDouble("lattitude"));
            orchid_location.setLongitude(getJSON().getDouble("longitude"));
        }catch(JSONException e){
            Log.d(TAG, e.toString());
        }

        return orchid_location;
    }

    public float get_distance_to(android.location.Location dest){
        return dest.distanceTo(get_gps_location());
    }

}
