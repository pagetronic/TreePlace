package live.page.android.threads;

import android.content.Context;

import java.util.List;

import live.page.android.api.ApiAsync;
import live.page.android.api.ApiResult;
import live.page.android.api.Json;
import live.page.android.utils.Fx;
import live.page.android.ui.select.SelectAction;
import live.page.android.ui.select.SelectDialog;

public abstract class PostEditor {
    public static void delete(final Context ctx, String id, final PostEditor completed) {
        ApiAsync.post(ctx, "/threads", new Json("action", "remove").put("id", id), new ApiResult() {
            @Override
            public void success(Json data) {
                if (!data.containsKey("error")) {
                    completed.success();
                } else {
                    Fx.toast(ctx, data.getString("error"));
                }
            }
        });
    }

    public static void move(final Context ctx, String id, final PostEditor completed) {
        ApiAsync.get(ctx, "/threads/" + id, new ApiResult() {
            @Override
            public void success(Json data) {
                new SelectDialog(ctx, "/forums", data.getListJson("parents"), true, new SelectAction() {
                    @Override
                    public void onValues(List<String> values) {
                        Fx.log(values);
                    }
                });
            }
        }, true);

    }

    public static void edit(Context ctx, String id, final PostEditor completed) {
    }


    public static void rapid(Context ctx, String id, final PostEditor completed) {
    }


    abstract void success();
}
