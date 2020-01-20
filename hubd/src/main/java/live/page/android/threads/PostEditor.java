package live.page.android.threads;

import android.content.Context;

public abstract class PostEditor {
    public static void delete(Context ctx, String id, PostEditor completed) {
    }

    public static void move(Context ctx, String id, PostEditor completed) {
    }

    public static void edit(Context ctx, String id, PostEditor completed) {
    }


    public static void rapid(Context ctx, String id, PostEditor completed) {
    }


    abstract void success();
}
