package com.agroneo.droid.specimens;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.agroneo.droid.R;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import live.page.android.api.Json;
import live.page.android.sys.PageFragment;
import live.page.android.api.ApiAdapter;

public class SpecimensFragment extends PageFragment {


    private SpecimensAdapter specimensAdapter;


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (specimensAdapter == null) {
            specimensAdapter = new SpecimensAdapter();
            specimensAdapter.get("/gaia/specimens");
        }
        ((ListView) view.findViewById(R.id.specimens)).setAdapter(specimensAdapter);


        final SwipeRefreshLayout swiper = view.findViewById(live.page.android.R.id.swiper);
        swiper.setOnRefreshListener(() -> {
            swiper.setRefreshing(false);
            specimensAdapter.clear();
            specimensAdapter.get("/gaia/specimens");
        });


        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(view1 -> startActivity(new Intent(getActivity(), SpecimenCreator.class)));
    }

    @Override
    protected int layout() {
        return R.layout.specimens;
    }


    private class SpecimensAdapter extends ApiAdapter {

        private SpecimensAdapter() {
            super(getContext(), R.layout.specimens_view);
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
                Display display = getActivity().getWindowManager().getDefaultDisplay();
                display.getSize(size);
                int width = size.x;
                layout.height = width * 200 / 462;
                layout.width = width;

                Glide.with(context).load(Uri.parse(image.getString("url") + "@462x200.jpg"))
                        .placeholder(new ImageProgress(context))
                        .error(R.drawable.logo)
                        .into(imageView);
            }

            convertView.setOnClickListener(view -> {
                Intent intent = new Intent(context, SpecimenView.class);
                intent.putExtra("id", specimen.getId());
                context.startActivity(intent);
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
}