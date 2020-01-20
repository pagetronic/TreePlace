package live.page.android.threads;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.SparseArray;
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

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import live.page.android.R;
import live.page.android.api.Json;
import live.page.android.sys.Command;
import live.page.android.sys.Fx;
import live.page.android.views.ApiAdapter;

public class ForumsFragment extends Fragment {


    private final List<Json> forums = new ArrayList<>();
    private final ForumsAdapter adapter = new ForumsAdapter();
    private TabLayout tabs;
    private View view;

    public ForumsFragment(String base) {
        forums.add(new Json("url", base));
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.forums, container, false);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        if (tabs == null) {
            ViewPager pager = view.findViewById(R.id.forum);
            tabs = view.findViewById(R.id.tabs);
            pager.setAdapter(adapter);
            tabs.setupWithViewPager(pager);
        }
    }

    private void makeTabs(Json data) {
        forums.clear();
        List<Json> childrens = data.getListJson("childrens");
        if (childrens != null) {
            tabs.setVisibility(View.VISIBLE);
            forums.add(new Json("id", data.getId()).put("url", data.getString("url")).put("title", data.getString("title")));
            forums.addAll(childrens);
        } else {
            tabs.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();
    }

    private class ForumsAdapter extends PagerAdapter {

        private final SparseArray<ListView> lists = new SparseArray<>();

        @Override
        public int getCount() {
            return forums.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return forums.get(position).getString("title");
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            ListView list = lists.get(position);
            if (list == null) {
                list = new ListView(getContext());
                list.setLayoutParams(new ListView.LayoutParams(-1, -1));
                list.setAdapter(new ThreadsAdapter().get(forums.get(position).getString("url") + "?lng=fr"));
                container.addView(list);
                lists.append(position, list);
            }

            return list;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {

        }
    }

    private class ThreadsAdapter extends ApiAdapter implements View.OnLongClickListener {

        private ThreadsAdapter() {
            super(getContext(), R.layout.threads_view);
        }

        @Override
        public boolean onLongClick(View view) {
            final String id = view.getTag().toString();
            Command.make(getContext(), new Command("Test") {
                @Override
                public void onClick() {
                    Fx.log(id);
                }
            });
            return false;
        }

        @Override
        public View getView(View convertView, final Json thread) {


            ((TextView) convertView.findViewById(R.id.title)).setText(Html.fromHtml(thread.getString("title", ""), Html.FROM_HTML_MODE_LEGACY));
            ((TextView) convertView.findViewById(R.id.text)).setText(Html.fromHtml(thread.getString("text", ""), Html.FROM_HTML_MODE_LEGACY));
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

            convertView.setTag(thread.getId());
            convertView.setOnLongClickListener(ThreadsAdapter.this);
            convertView.setLongClickable(true);
            return convertView;
        }

        protected Json getData(final Json data) {
            if (forums.size() == 1) {
                makeTabs(data);
            }
            Json threads = data.getJson("threads");
            if (threads == null) {
                return data;
            }
            return threads;
        }

    }

}
