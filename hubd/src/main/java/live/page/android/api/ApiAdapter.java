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
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import live.page.android.R;
import live.page.android.utils.Fx;

public abstract class ApiAdapter extends BaseAdapter {


    protected List<Json> items = new ArrayList<>();
    protected Context context;
    private int maxItems = 500; // preserve the memory, unload the elements if the maximum is reached, unload at best respecting the pagination
    private LayoutInflater inflater;
    private int resource;
    private ApiRequest req = null;
    private ScrollEvent scroll = null;
    private boolean autoLoadPrevious = true;
    private ListView listView;

    public ApiAdapter(ListView listView) {
        this(listView, R.layout.selectable_option);
    }


    public ApiAdapter(ListView listView, int resource, boolean autoloadPrevious) {
        this(listView, resource);
        this.autoLoadPrevious = autoloadPrevious;
    }

    public ApiAdapter(ListView listView, int resource) {
        this.listView = listView;
        this.context = listView.getContext();
        inflater = LayoutInflater.from(context);
        this.resource = resource;
        items.add(new Json("paging", null));
    }

    public void replace(Json before, Json after) {
        int index = items.indexOf(before);
        if (index >= 0) {
            items.set(index, after);
            notifyDataSetChanged();
        }
    }

    public ApiAdapter post(final String url, final Json data_post) {
        return post(null, url, data_post, 1, null);
    }

    public ApiAdapter post(SwipeRefreshLayout swiper, final String url, final Json data_post) {
        return post(swiper, url, data_post, 1, null);
    }

    public ApiAdapter post(final String url, final Json data_post, ApiResult onresult) {
        return post(null, url, data_post, 1, onresult);
    }

    private ApiAdapter post(final String url, final Json data_post, int dir) {
        return post(null, url, data_post, dir, null);
    }

    private ApiAdapter post(SwipeRefreshLayout swiper, final String url, final Json data_post, int dir, ApiResult onresult) {
        if (req != null) {
            req.abort();
        }
        req = ApiAsync.post(context, url, data_post, new ApiResult() {

            @Override
            public void success(final Json rez) {
                if (onresult != null) {
                    onresult.success(rez);
                }
                if (swiper != null) {
                    swiper.setRefreshing(false);
                    items.clear();
                }

                compose(rez, dir, (String paging_str, int dir1, Runnable after) -> {
                    try {
                        post(url, data_post.put("paging", paging_str), dir1);
                    } catch (Exception ignore) {

                    }
                });

            }

            @Override
            public void error(int code, Json data) {
                if (onresult != null) {
                    onresult.error(code, data);
                }
                Fx.toastNetworkError(context, code, data);

                notifyDataSetChanged();
                Fx.setTimeout(() -> post(swiper, url, data_post, dir, onresult), 1500);
            }
        });
        return this;
    }

    public ApiAdapter get(final String url) {
        return get(null, url, 1, null);
    }

    public ApiAdapter get(SwipeRefreshLayout swiper, final String url) {
        return get(swiper, url, 1, null);
    }

    public ApiAdapter get(SwipeRefreshLayout swiper, final String url, ApiResult onresult) {
        return get(swiper, url, 1, onresult);
    }

    public ApiAdapter get(final String url, ApiResult onresult) {
        return get(null, url, 1, onresult);
    }

    private ApiAdapter get(final String url, int dir) {
        return get(null, url, dir, null);
    }

    private ApiAdapter get(SwipeRefreshLayout swiper, final String url, int dir, ApiResult onresult) {

        if (req != null) {
            req.abort();
        }
        req = ApiAsync.get(context, url, new ApiResult() {

            @Override
            public void success(final Json rez) {
                if (onresult != null) {
                    onresult.success(rez);
                }
                if (swiper != null) {
                    swiper.setRefreshing(false);
                    items.clear();
                }


                compose(rez, dir, (String paging_str, int dir, Runnable after) -> {
                    try {
                        get(addPaging(url, paging_str), dir);
                    } catch (Exception ignore) {

                    }
                });

            }

            @Override
            public void error(int code, Json data) {
                if (onresult != null) {
                    onresult.error(code, data);
                }
                Fx.toastNetworkError(context, code, data);
                notifyDataSetChanged();
                Fx.setTimeout(() -> get(swiper, url, dir, onresult), 1500);
            }
        });
        return this;
    }

    private void compose(Json rez, int dir, ScrollEvent onScroll) {

        if (items.size() == 1) {
            items.clear();
        }
        Json jump;
        View firstView = null;
        if (dir > 0) {
            jump = getPreviousJson(listView.getLastVisiblePosition());
            for (int i = listView.getChildCount() - 1; i >= 0; i--) {
                firstView = listView.getChildAt(i);
                if (firstView.getId() == R.id.original) {
                    break;
                }
            }
        } else {
            jump = getFirstJson(listView.getFirstVisiblePosition());
            for (int i = 0; i < listView.getChildCount(); i++) {
                firstView = listView.getChildAt(i);
                if (firstView.getId() == R.id.original) {
                    break;
                }
            }
        }

        int yOffset = firstView == null ? 0 : firstView.getTop();


        Json data = getData(rez);
        List<Json> result = data.getListJson("result");

        if (items.size() + result.size() > maxItems) {

            List<Json> newItems = new ArrayList<>();

            if (dir > 0) {

                List<Json> tempsItems = new ArrayList<>();
                for (int i = items.size() - 1; i >= 0; i--) {
                    Json item = items.get(i);
                    tempsItems.add(0, item);
                    if (i == 0 || item.containsKey("paging")) {
                        newItems.addAll(0, tempsItems);
                        tempsItems.clear();
                        if (newItems.size() + result.size() > maxItems) {
                            break;
                        }
                    }
                }

            } else {

                List<Json> tempsItems = new ArrayList<>();
                for (int i = 0; i < items.size(); i++) {
                    Json item = items.get(i);
                    tempsItems.add(item);
                    if (i == items.size() - 1 || item.containsKey("paging")) {
                        newItems.addAll(tempsItems);
                        tempsItems.clear();
                        if (newItems.size() + result.size() > maxItems) {
                            break;
                        }
                    }
                }
            }

            items.clear();
            items.addAll(newItems);
        }


        Json paging = data.getJson("paging");

        if (paging.containsKey("prev")) {
            result.add(0, new Json("paging", paging.getString("prev")).put("dir", -1));
        }

        if (paging.containsKey("next")) {
            result.add(new Json("paging", paging.getString("next")).put("dir", 1));
        }

        if (dir > 0) {
            items.addAll(result);
        } else {
            items.addAll(0, result);
        }


        int position = getPosition(jump);
        scroll = onScroll;

        notifyDataSetChanged();

        if (position >= 0) {
            listView.setSelectionFromTop(position, yOffset);
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

    public Json getJson(int position) {
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
            } else if (scroll != null && (position == 0 || position == items.size() - 1)) {
                ProgressBar progress = viewProgress();
                Runnable runner = () -> {
                    scroll.doPaging(item.getString("paging"), item.getInteger("dir", 1), null);
                    scroll = null;

                };
                if (position == 0) {
                    return viewAction(runner);
                } else {
                    Fx.setTimeout(runner, 500);
                    return progress;
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


    private int getPosition(Json item) {
        if (item == null) {
            return -1;
        }
        for (int i = 0; i < items.size(); i++) {
            Json cur_item = items.get(i);
            if (cur_item.getId() != null && cur_item.getId().equals(item.getId())) {
                return i;
            }
        }
        return -1;
    }

    private Json getFirstJson(int start) {
        Json first = null;
        for (int i = start; i < items.size(); i++) {
            first = getJson(i);
            if (first == null || !first.containsKey("paging")) {
                return first;
            }
        }
        return first;
    }

    private Json getPreviousJson(int start) {
        Json last = null;
        for (int i = start; i >= 0; i--) {
            last = getJson(i);
            if (last == null || !last.containsKey("paging")) {
                return last;
            }
        }
        return last;
    }

    public void clear() {

        if (req != null) {
            req.abort();
        }
        items.clear();
        items.add(new Json("paging", null));
        notifyDataSetChanged();
    }

    private View viewAction(Runnable run) {
        if (autoLoadPrevious) {
            run.run();
            return viewProgress();
        }
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


    public void removeItem(String id) {
        for (Json item : items) {
            if (id.equals(item.getId())) {
                items.remove(item);
                notifyDataSetChanged();
                break;
            }
        }
    }

    public void addItem(Json post) {
        int position = getPosition(post);
        if (position >= 0) {
            items.set(position, post);
        } else {
            items.add(post);
        }
        notifyDataSetChanged();
    }


    private interface ScrollEvent {
        void doPaging(String paging_, int dir, Runnable after);
    }
}
