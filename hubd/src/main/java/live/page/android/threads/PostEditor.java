package live.page.android.threads;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.TextView;

import java.util.List;

import live.page.android.R;
import live.page.android.api.ApiAsync;
import live.page.android.api.ApiResult;
import live.page.android.api.Json;
import live.page.android.ui.select.SelectAction;
import live.page.android.ui.select.SelectDialog;
import live.page.android.utils.Fx;

public abstract class PostEditor {
    public static void delete(final Context ctx, String id, final PostEditor completed) {
        ApiAsync.post(ctx, "/threads", new Json("action", "remove").put("id", id), new ApiResult() {
            @Override
            public void success(Json data) {
                if (!data.containsKey("error")) {
                    completed.success(data);
                } else {
                    Fx.toast(ctx, data.getString("error"));
                }
            }
        }, true);
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
        ApiAsync.post(ctx, "/threads", new Json("action", "get").put("id", id), new ApiResult() {
            @Override
            public void success(Json data) {
                if (!data.containsKey("error")) {
                    AlertDialog post = postBox(ctx, completed);
                    TextView text = post.findViewById(R.id.text);
                    text.setText(data.getString("text", ""));

                    TextView title = post.findViewById(R.id.title);
                    if (data.getString("title") != null) {
                        title.setText(data.getString("title", ""));
                        title.setVisibility(TextView.VISIBLE);
                    } else {
                        title.setVisibility(TextView.GONE);
                    }

                    post.findViewById(R.id.cancel).setOnClickListener(v -> post.cancel());

                    post.findViewById(R.id.save).setOnClickListener(v -> {
                        Json data_post = new Json("action", "send").put("id", id);
                        data_post.put("text", text.getText().toString());
                        if (title.getVisibility() == TextView.VISIBLE) {
                            data_post.put("title", title.getText().toString());
                        }
                        ApiAsync.post(ctx, "/threads", data_post, new ApiResult() {
                                    @Override
                                    public void success(Json data) {
                                        if (data.getBoolean("ok", false)) {
                                            post.cancel();
                                            completed.success(data.getJson("post"));

                                        }
                                    }
                                }
                                , true);
                    });


                } else {
                    Fx.toast(ctx, data.getString("error"));
                }
            }
        }, true);
    }

    public static AlertDialog postBox(Context ctx, final PostEditor completed) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setView(R.layout.thread_reply);
        builder.setCancelable(true);
        return builder.show();
    }

    public static void rapid(Context ctx, String id, final PostEditor completed) {
    }


    abstract void success(Json data);
}
