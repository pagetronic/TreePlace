package live.page.android.threads;

import android.content.Context;

import live.page.android.api.ApiAsync;
import live.page.android.api.ApiResult;
import live.page.android.api.Json;

public abstract class PostEditor {
    public static void delete(Context ctx, String id, final PostEditor completed) {
        ApiAsync.post(ctx, "/threads", new Json("action", "remove").put("id", id), new ApiResult() {
            @Override
            public void success(Json data) {
                if (!data.containsKey("error")) {
                    completed.success();
                }
            }
        });
    }

    public static void move(Context ctx, String id, final PostEditor completed) {
    }

    public static void edit(Context ctx, String id, final PostEditor completed) {
    }


    public static void rapid(Context ctx, String id, final PostEditor completed) {
    }


    abstract void success();
}
