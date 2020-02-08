package live.page.android.api;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import live.page.android.R;

public abstract class ApiAdapter extends BaseAdapter {

    //TODO : manage "paging prev"

    protected Context context;
    protected List<Json> items = new ArrayList<>();
    private LayoutInflater inflater;
    private int resource;
    private ApiRequest req = null;
    private ScrollEvent scroll = null;

    public ApiAdapter(Context context) {
        this(context, R.layout.selectable_option);
    }

    public ApiAdapter(Context context, int resource) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.resource = resource;
        items.add(new Json("paging", null));
    }

    public void removeItem(String id) {
        for (Json item : items) {
            if (id.equals(item.getId())) {
                items.remove(item);
                notifyDataSetChanged();
                break;
            }
        }
    }

    protected boolean replace(Json thread, Json data) {
        int index = items.indexOf(thread);
        if (index >= 0) {
            items.set(index, data);
            notifyDataSetChanged();
            return true;
        } else {
            return false;
        }
    }

    public ApiAdapter post(final String url, final Json data_post) {
        return post(url, data_post, 1, () -> items.remove(0));
    }

    private ApiAdapter post(final String url, final Json data_post, int dir, Runnable run) {
        if (req != null) {
            req.abort();
        }
        req = ApiAsync.post(context, url, data_post, new ApiResult() {

            @Override
            public void success(final Json rez) {
                if (run != null) {
                    run.run();
                }
                compose(rez, dir);
                scroll = (String paging_str, int dir, Runnable run) -> {
                    try {
                        post(url, data_post.put("paging", paging_str), dir, run);
                    } catch (Exception ignore) {

                    }
                };
                notifyDataSetChanged();
            }

            @Override
            public void error(int code, Json data) {
                if (data.getString("error") != null) {
                    Toast.makeText(context, data.getString("error"), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, R.string.network_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
        return this;
    }

    public ApiAdapter get(final String url) {
        return get(null, url, 1, () -> items.remove(0));
    }

    public ApiAdapter get(SwipeRefreshLayout swiper, final String url) {
        return get(swiper, url, 1, () -> items.remove(0));
    }

    private ApiAdapter get(final String url, int dir, Runnable run) {
        return get(null, url, dir, run);
    }

    public ApiAdapter get(SwipeRefreshLayout swiper, final String url, int dir, Runnable run) {

        if (req != null) {
            req.abort();
        }
        req = ApiAsync.get(context, url, new ApiResult() {

            @Override
            public void success(final Json rez) {
                if (run != null) {
                    run.run();
                }
                if (swiper != null) {
                    swiper.setRefreshing(false);
                    items.clear();
                }
                compose(rez, dir);

                scroll = (String paging_str, int dir, Runnable run) -> {
                    try {
                        get(addPaging(url, paging_str), dir, run);
                    } catch (Exception ignore) {

                    }
                };
                notifyDataSetChanged();
            }

            @Override
            public void error(int code, Json data) {

                if (swiper != null) {
                    swiper.setRefreshing(false);
                }
                if (data.getString("error") != null) {
                    Toast.makeText(context, data.getString("error"), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, R.string.network_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
        return this;
    }

    private void compose(Json rez, int dir) {
        Json data = getData(rez);

        Json paging = data.getJson("paging");

        List<Json> result = data.getListJson("result");

        if (paging.containsKey("next")) {
            result.add(new Json("paging", paging.getString("next")).put("dir", 1));
        }
        if (paging.containsKey("prev")) {
            result.add(0, new Json("paging", paging.getString("prev")).put("dir", -1));
        }

        if (dir > 0) {
            items.addAll(result);
        } else {
            items.addAll(0, result);
        }
    }

    private String addPaging(String url, String paging) {
        if (!url.contains("?")) {
            return url + "?paging=" + paging;
        }
        if (url.contains("paging=")) {
            return url.replaceFirst("paging=([^&]+)", "paging=" + paging);
        }
        return url + "&paging=" + paging;
    }

    protected Json getData(final Json data) {
        return data;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        if (items != null && position >= 0 && items.size() > position) {
            return items.get(position);
        }
        return null;
    }

    protected Json getJson(int position) {
        return (Json) getItem(position);
    }

    @Override
    public long getItemId(int position) {
        Object item = getItem(position);
        if (item == null) {
            return -1;
        }
        return item.hashCode();
    }

    public abstract View getView(View view, Json item);

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Json item = getJson(position);

        if (item == null) {
            return viewGone();
        }

        if (item.containsKey("paging")) {
            if (item.getString("paging") == null) {
                return viewProgress();
            } else if (scroll != null && (position == 0 || position == getCount() - 1)) {
                Runnable run = () -> {
                    scroll.doPaging(item.getString("paging"), item.getInteger("dir", 1), () -> items.remove(item));
                    scroll = null;
                };
                if (position == 0) {
                    return viewAction(run);
                } else {
                    run.run();
                    return viewProgress();
                }
            } else {
                return viewGone();
            }
        }


        if (convertView == null || convertView.getId() != R.id.original) {
            convertView = inflater.inflate(resource, null);
            convertView.setId(R.id.original);
        }


        return getView(convertView, item);
    }

    public void clear() {

        if (req != null) {
            req.abort();
        }
        items.clear();
        items.add(new Json("paging", null));
        notifyDataSetChanged();
    }

    private LinearLayout viewAction(Runnable run) {
        LinearLayout layout = new LinearLayout(context);
        layout.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        layout.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        Button button = new Button(context);
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setText(context.getString(R.string.load_previous));
        button.setOnClickListener((v) -> {
            layout.removeAllViews();
            layout.addView(viewProgress());
            run.run();
        });
        layout.addView(button);
        return layout;
    }

    private View viewGone() {
        View view = new View(context);
        view.setVisibility(View.GONE);
        return view;
    }

    private ProgressBar viewProgress() {
        ProgressBar progress = new ProgressBar(context);
        progress.setPadding(5, 25, 5, 25);
        return progress;
    }

    private interface ScrollEvent {
        void doPaging(String paging_, int dir, Runnable run);
    }
}
