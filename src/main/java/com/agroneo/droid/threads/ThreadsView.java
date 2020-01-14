package com.agroneo.droid.threads;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.agroneo.droid.R;
import com.bumptech.glide.Glide;

import live.page.android.api.Json;
import live.page.android.views.ApiAdapter;

public class ThreadsView extends AppCompatActivity {


    private ApiAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.thread_view);
        if (adapter == null) {
            adapter = new ThreadAdapter();
            adapter.get("/threads/" + getIntent().getStringExtra("id"));
        }
        ((ListView) findViewById(R.id.thread)).setAdapter(adapter);
    }

    private class ThreadAdapter extends ApiAdapter {
        private Json data;


        private ThreadAdapter() {
            super(ThreadsView.this, R.layout.thread_posts);
        }

        @Override
        public View getFirst() {
            if (data == null) {
                return new View(context);
            }
            View view = getLayoutInflater().inflate(R.layout.thread_posts, null);
            return getView(view, data);
        }

        @Override
        public View getLast() {
            if (data == null) {
                return new View(context);
            }
            View view = getLayoutInflater().inflate(R.layout.thread_reply, null);
            return view;
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

        @Override
        protected Json getData(final Json data) {
            this.data = data;
            return data.getJson("posts");
        }
    }
}
