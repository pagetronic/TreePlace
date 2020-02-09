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
import android.widget.Toast;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import live.page.android.R;

public abstract class ApiAdapter extends BaseAdapter {


    private final int maxItems = 500; // preserve memory, unload items
    protected List<Json> items = new ArrayList<>();
    protected Context context;
    private LayoutInflater inflater;
    private int resource;
    private ApiRequest req = null;
    private ScrollEvent scroll = null;
    private boolean autoloadPrevious = true;
    private ListView listView;

    public ApiAdapter(ListView listView) {
        this(listView, R.layout.selectable_option);
    }


    public ApiAdapter(ListView listView, int resource, boolean autoloadPrevious) {
        this(listView, resource);
        this.autoloadPrevious = autoloadPrevious;
    }

    public ApiAdapter(ListView listView, int resource) {
        this.listView = listView;
        this.context = listView.getContext();
        inflater = LayoutInflater.from(context);
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
        return post(url, data_post, 1, () -> items.remove(0), null);
    }

    private ApiAdapter post(final String url, final Json data_post, int dir, Runnable before, Runnable after) {
        if (req != null) {
            req.abort();
        }
        req = ApiAsync.post(context, url, data_post, new ApiResult() {

            @Override
            public void success(final Json rez) {
                if (before != null) {
                    before.run();
                }
                compose(rez, dir);

                if (after != null) {
                    after.run();
                }
                scroll = (String paging_str, int dir1, Runnable before, Runnable after) -> {
                    try {
                        post(url, data_post.put("paging", paging_str), dir1, before, after);
                    } catch (Exception ignore) {

                    }
                };

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
        return get(null, url, 1, () -> items.remove(0), null);
    }

    public ApiAdapter get(SwipeRefreshLayout swiper, final String url) {
        return get(swiper, url, 1, () -> items.remove(0), null);
    }

    private ApiAdapter get(final String url, int dir, Runnable before, Runnable after) {
        return get(null, url, dir, before, after);
    }

    public ApiAdapter get(SwipeRefreshLayout swiper, final String url, int dir, Runnable before, Runnable after) {

        if (req != null) {
            req.abort();
        }
        req = ApiAsync.get(context, url, new ApiResult() {

            @Override
            public void success(final Json rez) {

                if (before != null) {
                    before.run();
                }
                if (swiper != null) {
                    swiper.setRefreshing(false);
                    items.clear();
                }
                compose(rez, dir);

                if (after != null) {
                    after.run();
                }

                scroll = (String paging_str, int dir, Runnable before, Runnable after) -> {
                    try {
                        get(addPaging(url, paging_str), dir, before, after);
                    } catch (Exception ignore) {

                    }
                };
            }

            @Override
            public void error(int code, Json data) {

                if (swiper != null) {
                    swiper.setRefreshing(false);
                }
                if (data != null && data.getString("error") != null) {
                    Toast.makeText(context, data.getString("error"), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, R.string.network_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
        return this;
    }

    private void compose(Json rez, int dir) {

        Json firstItem = null;
        View firstFiew = listView.getChildAt(dir < 0 ? 1 : 0);
        int yOffset = firstFiew != null ? firstFiew.getTop() : 0;

        if (items.size() >= maxItems) {
            int firstPosition = listView.getFirstVisiblePosition();
            do {
                firstItem = firstPosition >= 0 && firstPosition < items.size() ? items.get(firstPosition) : null;
                firstPosition++;
            } while (firstItem != null && firstItem.containsKey("paging"));
        }

        Json data = getData(rez);
        List<Json> result = data.getListJson("result");

        if (items.size() + result.size() > maxItems) {


            List<Json> newItems = new ArrayList<>();

            if (dir > 0) {

                List<Json> tempsItems = new ArrayList<>();
                for (int i = items.size() - 1; i >= 0; i--) {
                    Json item = items.get(i);
                    tempsItems.add(0, item);
                    if (item.containsKey("paging")) {
                        if (newItems.size() + tempsItems.size() >= maxItems) {
                            break;
                        }
                        newItems.addAll(0, tempsItems);
                        tempsItems.clear();
                    }
                }

            } else {

                List<Json> tempsItems = new ArrayList<>();
                for (int i = 0; i < items.size(); i++) {
                    Json item = items.get(i);
                    tempsItems.add(item);
                    if (item.containsKey("paging")) {
                        if (newItems.size() + tempsItems.size() >= maxItems) {
                            break;
                        }
                        newItems.addAll(tempsItems);
                        tempsItems.clear();
                    }
                }
            }

            items.clear();
            items.addAll(newItems);
        }


        Json paging = data.getJson("paging");


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

        int lastPosition = getPosition(firstItem);

        notifyDataSetChanged();

        if (lastPosition >= 0) {
            listView.setSelectionFromTop(lastPosition, yOffset);
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

                if (position == 0) {
                    return viewAction(() -> {
                        Json jump = getJson(1);
                        int yOffset = listView.getChildAt(1).getTop();
                        scroll.doPaging(item.getString("paging"), item.getInteger("dir", 1),
                                () -> items.remove(item),
                                () -> listView.setSelectionFromTop(getPosition(jump), yOffset)
                        );
                        scroll = null;
                    });
                } else {
                    scroll.doPaging(item.getString("paging"), item.getInteger("dir", 1), () -> items.remove(item), null);
                    scroll = null;
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

    public void clear() {

        if (req != null) {
            req.abort();
        }
        items.clear();
        items.add(new Json("paging", null));
        notifyDataSetChanged();
    }

    private View viewAction(Runnable run) {
        if (autoloadPrevious) {
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

    private interface ScrollEvent {
        void doPaging(String paging_, int dir, Runnable before, Runnable after);
    }
}
