package com.agroneo.droid.specimens;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.agroneo.droid.R;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import live.page.android.api.Json;
import live.page.android.views.ApiAdapter;

public class SpecimensFragment extends Fragment {


    private View view;
    private SpecimensAdapter specimensAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.specimens, container, false);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (specimensAdapter == null) {
            specimensAdapter = new SpecimensAdapter();
            specimensAdapter.get("/gaia/specimens");
        }
        ((ListView) view.findViewById(R.id.specimens)).setAdapter(specimensAdapter);
        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SpecimenCreator.class);
                startActivity(intent);
            }
        });
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
}