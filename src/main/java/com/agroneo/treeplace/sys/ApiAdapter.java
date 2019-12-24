package com.agroneo.treeplace.sys;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
    private ScrollEvent scroll = null;
    private int resource;
    private ApiRequest req = null;

    public ApiAdapter(Activity activity, int resource) {
        this.activity = activity;
        this.resource = resource;
    }

    public void post(final String url, final Json data) {
        scroll = null;
        if (req != null) {
            req.abort();
        }
        req = ApiAsync.post(activity, url, data, new ApiResult() {

            @Override
            public void success(final Json rez) {
                items.addAll(rez.getListJson("result"));
                final String next = rez.getJson("paging").getString("next");
                scroll = new ScrollEvent() {
                    @Override
                    public void doNext() {
                        post(url, data.put("paging", next));
                    }
                };
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
                items.addAll(data.getListJson("result"));
                final String next = data.getJson("paging").getString("next");
                scroll = new ScrollEvent() {
                    @Override
                    public void doNext() {
                        try {
                            get(addPaging(url, next));
                        } catch (Exception ignore) {

                        }
                    }
                };
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

        if (convertView == null) {
            convertView = activity.getLayoutInflater().inflate(resource, null);
        }
        if (scroll != null && position == getCount() - 1) {
            scroll.doNext();
        }
        return getView(convertView, (Json) getItem(position));
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
