package com.agroneo.treeplace.specimens;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.agroneo.treeplace.R;
import com.bumptech.glide.Glide;

import live.page.android.api.Json;
import live.page.android.views.ApiAdapter;

public class SpecimensAdapter extends ApiAdapter {

    private Activity activity;

    public SpecimensAdapter(Activity activity, int resource) {
        super(activity, resource);
        this.activity = activity;
    }

    @Override
    public View getView(View convertView, final Json specimen) {


        convertView.findViewById(R.id.scroll).scrollTo(0, 0);
        ((TextView) convertView.findViewById(R.id.title)).setText(specimen.getString("title"));
        ((TextView) convertView.findViewById(R.id.text)).setText(specimen.getString("text"));
        LinearLayout images = convertView.findViewById(R.id.images);
        images.removeAllViews();

        for (Json image : specimen.getListJson("images")) {
            ImageView imageView = new ImageView(context);
            images.addView(imageView);
            imageView.requestLayout();
            ViewGroup.LayoutParams layout = imageView.getLayoutParams();

            Point size = new Point();
            Display display = activity.getWindowManager().getDefaultDisplay();
            display.getSize(size);
            int width = size.x;
            layout.height = width * 200 / 462;
            layout.width = width;

            Glide.with(context).load(Uri.parse(image.getString("url") + "@462x200.jpg"))
                    .placeholder(new ImageProgress(context))
                    .error(R.drawable.logo)
                    .into(imageView);
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, SpecimenView.class);
                intent.putExtra("id", specimen.getId());
                context.startActivity(intent);
            }
        });
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
