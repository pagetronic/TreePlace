package com.agroneo.treeplace.specimens;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.agroneo.treeplace.R;
import com.agroneo.treeplace.api.ApiAsync;
import com.agroneo.treeplace.api.ApiResult;
import com.agroneo.treeplace.api.Json;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class SpecimensAdapter extends BaseAdapter {

    private Activity activity;
    private LayoutInflater inflater;
    private List<Json> specimens = new ArrayList<>();
    private String paging = null;
    private int limit = 0;
    private boolean isLocked = false;


    public void setActivity(Activity activity) {
        this.activity = activity;
        this.inflater = activity.getLayoutInflater();
    }

    public void init() {
        loadData(null);
    }


    public void loadData(String next) {

        if (isLocked) {
            return;
        }
        isLocked = true;
        ApiAsync.get(activity, "/gaia/specimens" + (next != null ? "?paging=" + next : ""),
                new ApiResult() {
                    @Override
                    public void success(Json data) {
                        specimens.addAll(data.getListJson("result"));
                        notifyDataSetChanged();

                        paging = data.getJson("paging").getString("next");
                        limit = data.getJson("paging").getInteger("limit");
                        if (paging != null) {
                            isLocked = false;
                        }
                    }

                    @Override
                    public void error(int code, Json data) {
                        super.error(code, data);
                        isLocked = false;
                    }
                }
        );


    }

    @Override
    public int getCount() {
        return specimens.size();
    }

    @Override
    public Object getItem(int position) {
        return specimens.get(position);
    }

    @Override
    public long getItemId(int position) {
        try {
            return getItem(position).hashCode();
        } catch (Exception e) {
            return Long.MIN_VALUE;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (position > getCount() - Math.round(limit / 2)) {
            loadData(paging);
        }

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.specimen_item, null);
        }
        Json specimen = specimens.get(position);

        convertView.findViewById(R.id.scroll).scrollTo(0, 0);
        ((TextView) convertView.findViewById(R.id.title)).setText(specimen.getString("title"));
        ((TextView) convertView.findViewById(R.id.text)).setText(specimen.getString("text"));
        LinearLayout images = convertView.findViewById(R.id.images);
        images.removeAllViews();

        for (Json image : specimen.getListJson("images")) {
            ImageView imageView = new ImageView(activity);
            images.addView(imageView);
            imageView.requestLayout();
            ViewGroup.LayoutParams layout = imageView.getLayoutParams();

            Point size = new Point();
            Display display = activity.getWindowManager().getDefaultDisplay();
            display.getSize(size);
            int width = size.x;
            layout.height = width * 200 / 462;
            layout.width = width;

            Glide.with(activity).load(Uri.parse(image.getString("url") + "@462x200.jpg"))
                    .placeholder(new ImageProgress(activity))
                    .error(R.drawable.logo)
                    .into(imageView);
        }

        return convertView;
    }

    private class ImageProgress extends CircularProgressDrawable {

        private ImageProgress(Context context) {
            super(context);
            setStrokeWidth(5f);
            setCenterRadius(30f);
            start();
        }
    }
}
