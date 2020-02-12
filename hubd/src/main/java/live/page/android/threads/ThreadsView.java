package live.page.android.threads;

import android.content.Context;
import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

import live.page.android.R;
import live.page.android.api.ApiAdapter;
import live.page.android.api.ApiAsync;
import live.page.android.api.ApiRequest;
import live.page.android.api.ApiResult;
import live.page.android.api.Json;
import live.page.android.auto.PageActivity;
import live.page.android.utils.Command;
import live.page.android.utils.Fx;
import live.page.android.utils.Since;

public class ThreadsView extends PageActivity {


    private LinearLayout headerView = null;
    private View replyView = null;
    private View threadPost = null;
    private ApiRequest postReq;
    private Json thread;
    private ThreadAdapter adapter;

    @Override
    protected int getLayout() {
        return R.layout.thread;
    }

    @Override
    protected void onCreate() {


        ListView listView = findViewById(R.id.thread);
        listView.setDivider(null);
        listView.setBackgroundColor(getContext().getColor(R.color.greyLight));
        adapter = new ThreadAdapter(listView);
        listView.setAdapter(adapter);
        makeJumper(listView);

        final SwipeRefreshLayout swiper = findViewById(R.id.swiper);
        String thread_id = getIntent().getStringExtra("id");
        final String url = "/threads/" + thread_id;

        headerView = new LinearLayout(getContext());
        headerView.setOrientation(LinearLayout.VERTICAL);
        listView.addHeaderView(headerView);

        replyView = getLayoutInflater().inflate(R.layout.thread_form, new LinearLayout(this));
        threadPost = getLayoutInflater().inflate(R.layout.thread_post, new LinearLayout(this));

        replyView.findViewById(R.id.cancel).setVisibility(View.GONE);
        replyView.findViewById(R.id.title).setVisibility(View.GONE);
        replyView.findViewById(R.id.save).setOnClickListener((v) -> {
            if (postReq != null) {
                postReq.abort();
            }
            EditText editText = replyView.findViewById(R.id.text);
            postReq = ApiAsync.post(getContext(), "/threads", new Json("action", "send").put("parent", "Posts(" + thread_id + ")").put("text", editText.getText().toString()), new ApiResult() {
                @Override
                public void success(Json data) {
                    if (data.getBoolean("ok", false)) {
                        adapter.addItem(data.getJson("post"));
                        editText.setText("");
                    }
                }

                @Override
                public void error(int code, Json data) {
                    Fx.log(data);
                }
            }, true);
        });

        listView.setLongClickable(true);
        listView.setOnItemLongClickListener((parent, view, pos, id) -> {


            Json post = pos == 0 ? thread : adapter.getJson(pos - 1);

            if (post != null && post.getId() != null) {
                if (post.getBoolean("editable", false)) {
                    return false;
                }
                List<Command> options = new ArrayList<>();
                if (user != null) {

                    if (user.getId().equals(post.getJson("user").getId()) || user.getBoolean("editor", false)) {
                        options.add(new Command(R.string.edit, R.drawable.edit) {
                            @Override
                            public void onClick() {
                                if (pos == 0) {
                                    makeHeader(true);
                                } else {
                                    adapter.replace(post, post.clone().put("editable", true));
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        });
                        options.add(new Command(R.string.delete, R.drawable.delete) {
                            @Override
                            public void onClick() {
                                Fx.confirm(getContext(), () -> PostEditor.delete(getContext(), post.getId(), new PostEditor() {
                                    @Override
                                    void success(Json data) {

                                    }
                                }));
                            }
                        });
                    }


                    options.add(new Command(R.string.rapid_comment, R.drawable.reply) {
                        @Override
                        public void onClick() {
                            PostEditor.rapid(getContext(), post.getId(), new PostEditor() {
                                @Override
                                void success(Json data) {

                                }
                            });
                        }
                    });
                }
                if (options.size() > 0) {
                    Command.make(getContext(), options);
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        });

        ApiResult onresult = new ApiResult() {
            @Override
            public void success(Json data) {
                thread = data.clone();
                thread.remove("posts");
                makeHeader(false);
                listView.removeFooterView(replyView);
                listView.addFooterView(replyView, null, true);
                makeNav();
            }
        };


        adapter.get(url, onresult);
        swiper.setOnRefreshListener(() -> adapter.get(swiper, url, onresult));

    }

    private void makeHeader(boolean editable) {

        headerView.removeAllViews();

        if (thread == null) {
            return;
        }
        getSupportActionBar().setTitle(thread.getString("title"));
        if (editable) {
            thread.put("editable", true);
            headerView.addView(PostEditor.edit(getContext(), getLayoutInflater(), thread, new PostEditor() {
                @Override
                void success(Json data) {
                    thread = data.clone().remove("posts");
                    makeHeader(false);
                }
            }));

        } else {
            headerView.addView(adapter.getView(threadPost, thread));
        }

    }


    private void makeJumper(ListView list) {

        ImageButton jumper = findViewById(R.id.reply);
        ViewPropertyAnimator animator = jumper.animate();
        animator.setInterpolator(new AccelerateInterpolator());
        final boolean[] isClicked = {false};


        list.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == 0 && isClicked[0]) {
                    isClicked[0] = false;
                    replyView.findViewById(R.id.text).requestFocus();
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                            .toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if (isClicked[0]) {
                    return;
                }

                boolean visible = visibleItemCount > 0 && totalItemCount > visibleItemCount && firstVisibleItem + visibleItemCount < totalItemCount;
                if (visible && jumper.getVisibility() != View.VISIBLE) {
                    animator.cancel();
                    jumper.setVisibility(View.VISIBLE);
                    animator.setDuration(300);
                    jumper.setAlpha(0F);
                    animator.alpha(1F);
                    animator.withEndAction(null);
                    animator.start();

                }

                if (!visible && jumper.getVisibility() != View.GONE) {
                    animator.cancel();
                    animator.setDuration(200);
                    jumper.setAlpha(1F);
                    animator.alpha(0F);
                    animator.withEndAction(() -> jumper.setVisibility(View.GONE));
                    animator.start();
                }
            }
        });

        jumper.setOnClickListener(v -> {
            isClicked[0] = true;
            jumper.setVisibility(View.GONE);
            list.smoothScrollToPosition(list.getCount() - 1);
        });
    }

    private void makeNav() {

        NavigationView nav = getNavRight();
        nav.removeAllViews();
        ListView thread_nav = new ListView(getContext());
        nav.addView(thread_nav);
        List<Json> items = new ArrayList<>();
        List<Json> pages = thread.getListJson("pages");
        if (pages.size() > 0) {
            items.add(new Json("separator", getString(R.string.related_pages)));
            for (Json page : pages) {
                items.add(page.put("icon", R.drawable.page));
            }
        }
        List<Json> forums = thread.getListJson("forums");
        if (forums.size() > 0) {
            items.add(new Json("separator", getString(R.string.related_forums)));
            for (Json forum : forums) {
                items.add(forum.put("icon", R.drawable.forum));
            }
        }
        if (thread.getJson("posts") != null) {
            items.add(new Json("separator", getString(R.string.same_posts)));
            List<Json> posts = thread.getJson("posts").getListJson("result");
            if (posts.size() > 0) {
                for (Json post : posts) {
                    items.add(post.put("icon", R.drawable.post));
                }
            }
        }
        if (items.size() == 0) {
            removeNavRight();
            return;
        }

        BaseAdapter adapterNav = new BaseAdapter() {

            @Override
            public int getCount() {
                return items.size();
            }

            @Override
            public Object getItem(int position) {
                return items.get(position);
            }

            @Override
            public long getItemId(int position) {
                return items.get(position).toString().hashCode();
            }

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.thread_nav, new LinearLayout(getContext()));
                }
                Json item = (Json) getItem(position);
                TextView separator = convertView.findViewById(R.id.separator);
                TextView title = convertView.findViewById(R.id.title);
                TextView intro = convertView.findViewById(R.id.intro);
                ImageView icon = convertView.findViewById(R.id.icon);

                if (item.containsKey("separator")) {

                    separator.setVisibility(View.VISIBLE);
                    separator.setText(item.getString("separator"));
                    title.setVisibility(View.GONE);
                    intro.setVisibility(View.GONE);
                    icon.setVisibility(View.GONE);

                } else {

                    separator.setVisibility(View.GONE);
                    title.setVisibility(View.VISIBLE);
                    icon.setVisibility(View.VISIBLE);

                    separator.setVisibility(View.GONE);
                    title.setText(item.getString("title", ""));

                    if (item.containsKey("intro")) {
                        intro.setVisibility(View.VISIBLE);
                        intro.setText(item.getString("intro"));
                    } else {
                        intro.setVisibility(View.GONE);
                    }
                    icon.setImageResource(item.getInteger("icon"));

                }
                return convertView;
            }

            public void addAll(List<Json> items) {
                items.addAll(items);
                notifyDataSetChanged();
            }

            public void add(Json item) {
                items.add(item);
                notifyDataSetChanged();
            }
        };
        thread_nav.setAdapter(adapterNav);
        adapterNav.notifyDataSetChanged();

    }

    private class ThreadAdapter extends ApiAdapter {

        private ThreadAdapter(ListView list) {
            super(list, R.layout.thread_post, false);
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

            TextView title = convertView.findViewById(R.id.title);
            if (thread.getString("title", "").equals("")) {
                title.setVisibility(View.GONE);
            } else {
                title.setText(Html.fromHtml(thread.getString("title", ""), Html.FROM_HTML_MODE_LEGACY));
                title.setVisibility(View.VISIBLE);
            }
            ((TextView) convertView.findViewById(R.id.date)).setText(Since.format(context, thread.parseDate("date"), 2));
            ((TextView) convertView.findViewById(R.id.text)).setText(PostParser.parse(thread.getString("text", ""), thread.getListJson("docs"), thread.getListJson("links")));

            Glide.with(context).load(Uri.parse(thread.getJson("user").getString("avatar") + "@64x64"))
                    .error(R.drawable.logo)
                    .into((ImageView) convertView.findViewById(R.id.avatar));


            return convertView;
        }

        @Override
        public int getCount() {
            return super.getCount();
        }


        @Override
        protected Json getData(final Json data) {

            return data.getJson("posts");
        }

    }
}
