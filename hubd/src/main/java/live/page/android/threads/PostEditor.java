package live.page.android.threads;

import android.content.Context;

import java.util.List;

import live.page.android.api.ApiAsync;
import live.page.android.api.ApiResult;
import live.page.android.api.Json;
import live.page.android.sys.Fx;
import live.page.android.views.Selectable;

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
                Selectable.selectable(ctx, "/forums", data.getListJson("parents"), true, new Selectable.Select() {
                    @Override
                    public void onChoice(List<Json> choices) {

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
