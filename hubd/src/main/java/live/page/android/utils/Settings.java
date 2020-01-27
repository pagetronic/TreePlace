package live.page.android.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

import live.page.android.R;

public class Settings {
    public static String getLng(Context ctx) {

        SharedPreferences settings = ctx.getSharedPreferences("settings", Context.MODE_PRIVATE);
        String lng = settings.getString("lng", null);
        if (Fx.availableLng(ctx, lng)) {
            return lng;
        }
        lng = Locale.getDefault().getLanguage();
        if (Fx.availableLng(ctx, lng)) {
            return lng;
        }
        lng = lng.split("_")[0];
        if (Fx.availableLng(ctx, lng)) {
            return lng;
        }
        String[] domains = ctx.getResources().getStringArray(R.array.domains);
        return domains[0].split("@")[0];
    }
}
