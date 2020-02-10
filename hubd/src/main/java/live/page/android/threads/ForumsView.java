package live.page.android.threads;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import live.page.android.R;
import live.page.android.api.ApiAdapter;
import live.page.android.api.Json;
import live.page.android.auto.PageFragment;
import live.page.android.ui.Animations;
import live.page.android.utils.Command;
import live.page.android.utils.Settings;
import live.page.android.utils.Since;

public class ForumsView extends PageFragment {


    private final List<Json> forums = new ArrayList<>();
    private final ForumsAdapter adapter = new ForumsAdapter();
    private TabLayout tabs;


    public ForumsView() {

    }

    public ForumsView(String base) {
        forums.add(new Json("url", base));
    }

    @Override
    protected int layout() {
        return R.layout.forums;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        if (tabs == null) {
            ViewPager pager = view.findViewById(R.id.forum);
            tabs = view.findViewById(R.id.tabs);
            pager.setAdapter(adapter);
            tabs.setupWithViewPager(pager);
        }
        view.findViewById(R.id.new_thread).setOnClickListener(v -> newThread());
    }

    private void newThread() {
        //TODO new thread
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

        private final SparseArray<SwipeRefreshLayout> swipers = new SparseArray<>();

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
        public Object instantiateItem(@NonNull ViewGroup container, final int position) {

            if (swipers.get(position) != null) {
                return swipers.get(position);
            }

            ListView listView = new ListView(getContext());
            listView.setDivider(null);
            listView.setBackgroundColor(getContext().getColor(R.color.grey_light));

            listView.setLayoutParams(new ListView.LayoutParams(-1, -1));
            final ThreadsAdapter threadAdapter = new ThreadsAdapter(listView);
            listView.setAdapter(threadAdapter);

            final SwipeRefreshLayout swiper = new SwipeRefreshLayout(getContext());
            swiper.setLayoutParams(new SwipeRefreshLayout.LayoutParams(-1, -1));
            final String url = forums.get(position).getString("url") + "?lng=" + Settings.getLng(getContext());
            swiper.setOnRefreshListener(() -> threadAdapter.get(swiper, url));
            swiper.addView(listView);

            container.addView(swiper);

            threadAdapter.get(url);

            swipers.append(position, swiper);

            listView.setLongClickable(true);
            listView.setOnItemLongClickListener((parent, view, pos, id) -> {

                Json thread = threadAdapter.getJson(pos);
                if (thread != null && thread.getId() != null) {
                    List<Command> options = new ArrayList<>();
                    if (user != null) {
                        if (user.getId().equals(thread.getJson("user").getId()) || user.getBoolean("editor", false)) {

                            options.add(new Command(getString(R.string.delete), R.drawable.delete) {
                                @Override
                                public void onClick() {
                                    PostEditor.delete(getContext(), thread.getId(), new PostEditor() {
                                        @Override
                                        void success(Json data) {
                                            Animations.moveOut(view, new Animations.Events() {
                                                @Override
                                                public void finished() {
                                                    threadAdapter.removeItem(thread.getId());
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                            options.add(new Command(getString(R.string.edit), R.drawable.edit) {
                                @Override
                                public void onClick() {
                                    threadAdapter.replace(thread, thread.clone().put("editable", true));
                                }
                            });
                            options.add(new Command(getString(R.string.move), R.drawable.move) {
                                @Override
                                public void onClick() {
                                    PostEditor.move(getContext(), thread.getId(), new PostEditor() {
                                        @Override
                                        void success(Json data) {
                                            notifyDataSetChanged();
                                        }
                                    });
                                }
                            });
                        }

                    }
                    if (options.size() > 0) {
                        Command.make(getContext(), options);
                        return false;
                    } else {
                        return true;
                    }
                }
                return false;
            });

            listView.setClickable(true);
            listView.setOnItemClickListener((parent, view, pos, id) -> {
                Json thread = threadAdapter.getJson(pos);
                if (thread != null && thread.getId() != null) {
                    Intent intent = new Intent(getContext(), ThreadsView.class);
                    intent.putExtra("id", thread.getId());
                    startActivity(intent);
                }
            });

            return swiper;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {

        }
    }

    private class ThreadsAdapter extends ApiAdapter {

        private ThreadsAdapter(ListView list) {
            super(list, R.layout.threads_view);
        }


        @Override
        public View getView(final View convertView, final Json thread) {

            if (thread.getBoolean("editable", false)) {
                return PostEditor.edit(getContext(), getLayoutInflater(), thread, new PostEditor() {
                    @Override
                    void success(Json data) {
                        replace(thread, data);
                    }
                });
            }

            ((TextView) convertView.findViewById(R.id.title)).setText(Html.fromHtml(thread.getString("title", ""), Html.FROM_HTML_MODE_LEGACY));
            ((TextView) convertView.findViewById(R.id.text)).setText(Html.fromHtml(thread.getString("text", ""), Html.FROM_HTML_MODE_LEGACY));
            ((TextView) convertView.findViewById(R.id.date)).setText(Since.format(context, thread.parseDate("date"), 2));


            Glide.with(context).load(Uri.parse(thread.getJson("user").getString("avatar") + "@64x64"))
                    .error(R.drawable.logo)
                    .into((ImageView) convertView.findViewById(R.id.avatar));

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
