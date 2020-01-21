package live.page.android.sys;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

import live.page.android.R;

public class Settings {
    public static String getLng(Context ctx) {

        SharedPreferences settings = ctx.getSharedPreferences("settings", Context.MODE_PRIVATE);
        String lng = settings.getString("lng", null);
        if (lng != null) {
            return lng;
        }
        String[] domains = ctx.getResources().getStringArray(R.array.domains);
        lng = Locale.getDefault().getLanguage();
        for (String domain : domains) {
            if (domain.startsWith(lng + "@")) {
                return lng;
            }
        }
        lng = lng.split("_")[0];
        for (String domain : domains) {
            if (domain.startsWith(lng + "@")) {
                return lng;
            }
        }
        return domains[0].split("@")[0];
    }
}
