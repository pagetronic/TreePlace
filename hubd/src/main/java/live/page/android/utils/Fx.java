package live.page.android.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import live.page.android.R;

public class Fx {

    public static void log(Object msg) {
        Log.e("TreePlace", msg == null ? "null" : msg.toString());
    }

    public static void toast(Context ctx, String error) {
        Toast.makeText(ctx, error == null ? ctx.getString(R.string.error) : error, Toast.LENGTH_LONG).show();
    }

    public static AlertDialog loading(Context ctx, final Action cancel) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setCancelable(true);
        ProgressBar progress = new ProgressBar(ctx);
        progress.getIndeterminateDrawable().setColorFilter(0xFFFFFFFF, android.graphics.PorterDuff.Mode.MULTIPLY);
        builder.setView(progress);
        final AlertDialog dialog = builder.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setOnCancelListener(dialog1 -> cancel.doIt());
        dialog.show();
        return dialog;
    }

    public static boolean availableLng(Context ctx, String lng) {
        if (lng != null) {
            String[] domains = ctx.getResources().getStringArray(R.array.domains);
            for (String domain : domains) {
                if (domain.startsWith(lng + "@")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static abstract class Action {
        public abstract void doIt();
    }
}
