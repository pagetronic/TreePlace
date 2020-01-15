package com.agroneo.droid.threads;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.agroneo.droid.R;
import com.bumptech.glide.Glide;

import live.page.android.api.Json;
import live.page.android.sys.PageActivity;
import live.page.android.views.ApiAdapter;

public class ThreadsView extends PageActivity {


    private View firstView = null;
    private View lastView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setView(R.layout.thread_view);
        final ThreadAdapter adapter = new ThreadAdapter();
        ((ListView) findViewById(R.id.thread)).setAdapter(adapter);
        adapter.get("/threads/" + getIntent().getStringExtra("id"));
        final LayoutInflater inflater = getLayoutInflater();
        firstView = inflater.inflate(R.layout.thread_posts, new LinearLayout(this));
        lastView = inflater.inflate(R.layout.thread_reply, new LinearLayout(this));

    }


    private class ThreadAdapter extends ApiAdapter {
        private Json data = null;

        private ThreadAdapter() {
            super(ThreadsView.this, R.layout.thread_posts);
        }

        @Override
        public View getFirst() {
            if (data == null) {
                return new View(context);
            }
            return getView(firstView, data);
        }

        @Override
        public View getLast() {
            if (data == null) {
                return new View(context);
            }
            return lastView;
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

            getSupportActionBar().setTitle(data.getString("title"));
            this.data = data;
            return data.getJson("posts");
        }
    }
}
