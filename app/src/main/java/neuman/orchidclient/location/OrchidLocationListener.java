package neuman.orchidclient.location;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;


public class OrchidLocationListener implements LocationListener {
    GPSData gpsData = new GPSData();


    public void onLocationChanged(Location location) {
        Criteria c = new Criteria();
        //c.setAccuracy(location.getAccuracy(4.0));
        gpsData.setLongitude(location.getLongitude());
        gpsData.setLatitude(location.getLatitude());
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }
}   