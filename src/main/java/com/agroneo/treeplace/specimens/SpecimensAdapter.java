package com.agroneo.treeplace.specimens;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
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

    public SpecimensAdapter(Activity activity) {
        this.activity = activity;
        this.inflater = activity.getLayoutInflater();
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
        return getItem(position).hashCode();
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
        ((TextView) convertView.findViewById(R.id.title)).setText(specimen.getString("title"));
        ((TextView) convertView.findViewById(R.id.text)).setText(specimen.getString("text"));
        LinearLayout images = convertView.findViewById(R.id.images);
        images.removeAllViews();
        for (Json image : specimen.getListJson("images")) {
            ImageView imageView = new ImageView(activity);
            images.addView(imageView);
            imageView.requestLayout();
            ViewGroup.LayoutParams layout = imageView.getLayoutParams();
            layout.height = 200;
            layout.width = 280;

            Glide.with(activity).load(Uri.parse(image.getString("url") + "@280x200.jpg"))
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
