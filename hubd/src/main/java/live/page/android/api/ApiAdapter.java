package live.page.android.api;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
    private ScrollEvent scroll = null;
    private int resource;
    private Json progress = new Json("progress", true);
    private ApiRequest req = null;

    public ApiAdapter(Context context) {
        this(context, R.layout.selectable_option);
    }

    public ApiAdapter(Context context, int resource) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.resource = resource;
        items.add(progress);
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
        if(index>=0) {
            items.set(index, data);
            notifyDataSetChanged();
            return true;
        } else {
            return false;
        }
    }

    public ApiAdapter post(final String url, final Json data) {
        scroll = null;
        if (req != null) {
            req.abort();
        }
        req = ApiAsync.post(context, url, data, new ApiResult() {

            @Override
            public void success(final Json rez) {
                items.remove(progress);
                items.addAll(rez.getListJson("result"));
                final String next = rez.getJson("paging").getString("next");
                if (next != null) {
                    items.add(progress);
                    scroll = () -> {
                        try {
                            post(url, data.put("paging", next));
                        } catch (Exception ignore) {

                        }
                    };
                }
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

    protected Json getData(final Json data) {
        return data;
    }

    public ApiAdapter get(final String url) {
        return get(null, url);
    }

    public ApiAdapter get(SwipeRefreshLayout swiper, final String url) {


        scroll = null;
        if (req != null) {
            req.abort();
        }
        req = ApiAsync.get(context, url, new ApiResult() {

            @Override
            public void success(final Json data_) {
                if (swiper != null) {
                    swiper.setRefreshing(false);
                    items.clear();
                }
                Json data = getData(data_);
                items.remove(progress);
                items.addAll(data.getListJson("result"));
                final String next = data.getJson("paging") != null ? data.getJson("paging").getString("next") : null;
                if (next != null) {
                    items.add(progress);
                    scroll = () -> {
                        try {
                            get(addPaging(url, next));
                        } catch (Exception ignore) {

                        }
                    };
                }
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

    private String addPaging(String url, String paging) {
        if (!url.contains("?")) {
            return url + "?paging=" + paging;
        }
        if (url.contains("paging=")) {
            return url.replaceFirst("paging=([^&]+)", "paging=" + paging);
        }
        return url + "&paging=" + paging;
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

        Json item = (Json) getItem(position);

        if (item != null && item.getBoolean("progress", false)) {
            return progressView();
        }


        if (item == null) {
            return new View(context);
        }

        if (convertView == null || convertView.getId() != R.id.original) {
            convertView = inflater.inflate(resource, null);
            convertView.setId(R.id.original);
        }

        if (scroll != null && position >= Math.max(1, getCount() - 3)) {
            scroll.doNext();
        }

        return getView(convertView, item);
    }

    public void clear() {

        if (req != null) {
            req.abort();
        }
        items.clear();
        items.add(progress);
        notifyDataSetChanged();
    }

    protected View progressView() {

        ProgressBar progress = new ProgressBar(context);
        progress.setPadding(5, 25, 5, 25);
        return progress;
    }


    public interface ScrollEvent {
        void doNext();
    }

}
