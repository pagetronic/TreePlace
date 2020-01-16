package live.page.android.sys;

import android.location.Location;

public class LocationUtils {
    public static String convertDMS(Location location) {
        double longitude_loc = location.getLongitude();
        double latitude_loc = location.getLatitude();

        double absolute = Math.abs(longitude_loc);
        double degrees = Math.floor(absolute);
        double minutesNotTruncated = (absolute - degrees) * 60;
        double minutes = Math.floor(minutesNotTruncated);
        double seconds = Math.floor((minutesNotTruncated - minutes) * 60);
        String longitude = degrees + "° " + minutes + "’ " + seconds + "” " + (Math.signum(longitude_loc) >= 0 ? "N" : "S");

        absolute = Math.abs(latitude_loc);
        degrees = Math.floor(absolute);
        minutesNotTruncated = (absolute - degrees) * 60;
        minutes = Math.floor(minutesNotTruncated);
        seconds = Math.floor((minutesNotTruncated - minutes) * 60);
        String latitude = degrees + "° " + minutes + "’ " + seconds + "” " + (Math.signum(latitude_loc) >= 0 ? "E" : "W");

        return longitude + " " + latitude;
    }
}
