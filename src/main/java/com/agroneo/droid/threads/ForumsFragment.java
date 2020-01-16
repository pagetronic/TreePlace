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
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.agroneo.droid.R;
import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import live.page.android.api.Json;
import live.page.android.views.ApiAdapter;

public class ForumsFragment extends Fragment {


    private TabLayout tabs;
    private ViewPager pager;
    private ForumsAdapter adapter;
    private List<Json> forums = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.forums, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        pager = view.findViewById(R.id.forum);
        tabs = view.findViewById(R.id.tabs);
        forums.add(new Json("id", "ROOT").put("url", "/questions").put("title", "Question"));
        adapter = new ForumsAdapter();
        pager.setAdapter(adapter);
        tabs.setupWithViewPager(pager);
    }

    private void makeTabs(Json data) {
        forums.clear();
        forums.add(new Json("id", data.getId()).put("url", data.getString("url")).put("title", data.getString("title")));
        forums.addAll(data.getListJson("childrens"));
        adapter.notifyDataSetChanged();
    }

    private class ForumsAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return forums.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return forums.get(position).getString("title");
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            ListView list = new ListView(getContext());
            list.setLayoutParams(new ListView.LayoutParams(-1, -1));
            ThreadsAdapter threadsAdapter = new ThreadsAdapter();
            threadsAdapter.get(forums.get(position).getString("url") + "?lng=fr");
            list.setAdapter(threadsAdapter);
            container.addView(list);
            return list;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {

        }
    }

    private class ThreadsAdapter extends ApiAdapter {

        private ThreadsAdapter() {
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
           makeTabs(data);
            return data.getJson("threads");
        }
    }

}
