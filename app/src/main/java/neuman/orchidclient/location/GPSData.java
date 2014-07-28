package neuman.orchidclient.location;

/**
 * Created by neuman on 7/27/14.
 */
public class GPSData {
    private static double latitude;
    /**
     * @return the latitude
     */
    public static double getLatitude() {
        return latitude;
    }

    /**
     * @param l the latitude the latitude to set
     */
    public static void setLatitude(double l) {
        latitude = l;
    }

    private static double longitude;
    /**
     * @return the longitude
     */
    public static double getLongitude() {
        return longitude;
    }

    /**
     * @param d the longitude to set
     */
    public static void setLongitude(double d) {
        longitude = d;
    }
}