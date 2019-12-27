package com.agroneo.treeplace.sys;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.agroneo.treeplace.R;
import com.agroneo.treeplace.api.ApiAsync;
import com.agroneo.treeplace.api.ApiRequest;
import com.agroneo.treeplace.api.ApiResult;
import com.agroneo.treeplace.api.Json;

import java.util.ArrayList;
import java.util.List;

public abstract class ApiAdapter extends BaseAdapter {
    protected Activity activity;
    private List<Json> items = new ArrayList<>();
    private Json progress = new Json("progress", true);
    private ScrollEvent scroll = null;
    private int resource;
    private ApiRequest req = null;


    public ApiAdapter(Activity activity) {
        this(activity, R.layout.selectable_option);
    }

    public ApiAdapter(Activity activity, int resource) {
        this.activity = activity;
        this.resource = resource;
        items.add(progress);
    }

    public void post(final String url, final Json data) {
        scroll = null;
        if (req != null) {
            req.abort();
        }
        req = ApiAsync.post(activity, url, data, new ApiResult() {

            @Override
            public void success(final Json rez) {
                items.remove(progress);
                items.addAll(rez.getListJson("result"));
                final String next = rez.getJson("paging").getString("next");
                if (next != null) {
                    items.add(progress);
                    scroll = new ScrollEvent() {
                        @Override
                        public void doNext() {
                            try {
                                post(url, data.put("paging", next));
                            } catch (Exception ignore) {

                            }
                        }
                    };
                }
                notifyDataSetChanged();
            }

            @Override
            public void error(int code, Json data) {
                Toast.makeText(activity, R.string.network_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void get(final String url) {

        scroll = null;
        if (req != null) {
            req.abort();
        }
        req = ApiAsync.get(activity, url, new ApiResult() {

            @Override
            public void success(final Json data) {
                items.remove(progress);
                items.addAll(data.getListJson("result"));
                final String next = data.getJson("paging").getString("next");

                if (next != null) {
                    items.add(progress);
                    scroll = new ScrollEvent() {
                        @Override
                        public void doNext() {
                            try {
                                get(addPaging(url, next));
                            } catch (Exception ignore) {

                            }
                        }
                    };
                }
                notifyDataSetChanged();
            }

            @Override
            public void error(int code, Json data) {
                Toast.makeText(activity, R.string.network_error, Toast.LENGTH_SHORT).show();
            }
        });
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
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).hashCode();
    }

    public abstract View getView(View view, Json item);

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Json item = (Json) getItem(position);

        if (item.getBoolean("progress", false)) {
            ProgressBar progress = new ProgressBar(activity);
            progress.setPadding(5, 25, 5, 25);
            return progress;
        }

        if (convertView == null || convertView instanceof ProgressBar) {
            convertView = activity.getLayoutInflater().inflate(resource, null);
        }

        if (scroll != null && position >= Math.max(1, getCount() - 3)) {
            scroll.doNext();
        }

        return getView(convertView, item);
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void clear() {

        if (req != null) {
            req.abort();
        }
        items.clear();
        notifyDataSetChanged();
    }

    private interface ScrollEvent {
        void doNext();
    }
}
