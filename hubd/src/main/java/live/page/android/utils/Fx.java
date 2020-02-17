package live.page.android.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import live.page.android.R;
import live.page.android.api.ApiAsync;
import live.page.android.api.ApiResult;
import live.page.android.api.Json;

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

    public static void setTimeout(Runnable runnable, int delay) {
        new android.os.Handler().postDelayed(runnable, delay);
    }

    public static void awaitNetwork(Context context, Runnable runnable, int delay) {

        ApiAsync.get(context, "/", new ApiResult() {
            @Override
            public void success(Json data) {
                runnable.run();
            }

            @Override
            public void error(int code, Json data) {
                if (code == -1) {
                    setTimeout(() -> awaitNetwork(context, runnable, delay), delay);
                }
            }
        });
    }

    public static void toastNetworkError(Context context, int code, Json data) {
        if (code == -1) {
            Toast.makeText(context, R.string.network_error, Toast.LENGTH_SHORT).show();
        } else if (data != null && data.getString("error") != null) {
            Toast.makeText(context, data.getString("error", context.getString(R.string.unknown_error)), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.unknown_error, Toast.LENGTH_SHORT).show();
        }
    }

    public static void confirm(Context context, Runnable runnable) {

        new AlertDialog.Builder(context)
                .setMessage(R.string.confirmation)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, (dialog, id) -> runnable.run())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    public static String truncate(String str, int length) {
        if (str == null) {
            return null;
        }
        if (str.length() <= length) {
            return str;
        }

        int end = str.lastIndexOf(' ', length - 3);

        if (end == -1) {
            return str.substring(0, length - 3) + "…";
        }
        int newEnd = end;
        do {
            end = newEnd;
            newEnd = str.indexOf(' ', end + 1);

            if (newEnd == -1) {
                newEnd = str.length();
            }

        } while ((str.substring(0, newEnd) + "…").length() < length);

        return str.substring(0, end) + "…";
    }


    public static abstract class Action {
        public abstract void doIt();
    }
}
