package com.agroneo.droid.threads;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.agroneo.droid.R;
import com.bumptech.glide.Glide;

import live.page.android.api.Json;
import live.page.android.views.ApiAdapter;

public class ForumAdapter extends ApiAdapter {

    private Activity activity;

    public ForumAdapter(Activity activity, int resource) {
        super(activity, resource);
        this.activity = activity;
    }

    @Override
    public View getView(View convertView, final Json thread) {


        ((TextView) convertView.findViewById(R.id.title)).setText(thread.getString("title"));
        ((TextView) convertView.findViewById(R.id.text)).setText(thread.getString("text"));
        Glide.with(context).load(Uri.parse(thread.getJson("user").getString("avatar") + "@64x64"))
                .error(R.drawable.logo)
                .into((ImageView) convertView.findViewById(R.id.avatar));


        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ThreadsView.class);
                intent.putExtra("id", thread.getId());
                context.startActivity(intent);
            }
        });
        return convertView;
    }

    protected Json getData(final Json data) {
        return data.getJson("threads");
    }
}
