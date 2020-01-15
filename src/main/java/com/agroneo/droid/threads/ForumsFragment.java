package com.agroneo.droid.threads;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.agroneo.droid.R;
import com.bumptech.glide.Glide;

import live.page.android.api.Json;
import live.page.android.views.ApiAdapter;

public class ForumsFragment extends Fragment {


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.threads_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        ForumAdapter forumAdapter = new ForumAdapter();
        forumAdapter.get("/questions?lng=fr");
        ((ListView) view.findViewById(R.id.threads)).setAdapter(forumAdapter);
        forumAdapter.notifyDataSetChanged();
    }

    private class ForumAdapter extends ApiAdapter {

        private ForumAdapter() {
            super(getContext(), R.layout.threads_view);
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
                    startActivity(intent);
                }
            });
            return convertView;
        }

        protected Json getData(final Json data) {
            return data.getJson("threads");
        }
    }
}
