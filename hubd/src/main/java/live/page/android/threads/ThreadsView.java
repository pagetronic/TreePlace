package live.page.android.threads;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;

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
    private ListView listView;
    private List<String> history = new ArrayList<>();

    @Override
    protected int getLayout() {
        return R.layout.thread;
    }


    public void load(String thread_id) {
        getNavRight().removeAllViews();
        history.add(thread_id);
        String url = "/threads/" + thread_id;

        thread = null;
        makeHeader(false);
        adapter.clear();
        replyView.setVisibility(View.GONE);
        ApiResult onresult = new ApiResult() {
            @Override
            public void success(Json data) {
                thread = data.clone();
                thread.remove("posts");
                makeHeader(false);
                listView.removeFooterView(replyView);
                listView.addFooterView(replyView, null, true);
                if (!ThreadsNav.makeNav(ThreadsView.this, getNavRight(), thread, isEditor())) {
                    removeNavRight();
                }
                replyView.setVisibility(View.VISIBLE);
            }
        };
        adapter.get(url, onresult);
        ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawers();
        SwipeRefreshLayout swiper = findViewById(R.id.swiper);
        swiper.setOnRefreshListener(() -> adapter.get(swiper, url, onresult));
    }

    @Override
    protected void onCreate() {

        String forum_id = getIntent().getStringExtra("forum_id");
        if (forum_id != null) {
            removeNavRight();
            String url = "/forums/" + forum_id;
            CoordinatorLayout root = findViewById(R.id.root);
            root.removeAllViews();
            FrameLayout frame = new FrameLayout(getContext());
            frame.setId(R.id.host_fragment);
            root.addView(frame);
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();

            transaction.replace(R.id.host_fragment, new ForumsView(url));
            transaction.commit();
            return;
        }


        listView = findViewById(R.id.thread);
        listView.setDivider(null);
        listView.setBackgroundColor(getContext().getColor(R.color.greyLight));
        adapter = new ThreadAdapter(listView);
        listView.setAdapter(adapter);
        makeJumper(listView);

        String thread_id = getIntent().getStringExtra("id");


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


        load(thread_id);

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

    @Override
    public void onBackPressed() {
        //TODO history forum fragment
        if (history.size() <= 1) {
            super.onBackPressed();
        } else {
            load(history.get(history.size() - 2));
            history.remove(history.size() - 1);
            history.remove(history.size() - 1);

        }
    }


    private class ThreadAdapter extends ApiAdapter {

        private ThreadAdapter(ListView list) {
            super(list, R.layout.thread_post, false);
        }


        @Override
        public View getView(final View convertView, final Json post) {


            if (post.getBoolean("editable", false)) {

                return PostEditor.edit(getContext(), getLayoutInflater(), post, new PostEditor() {
                    @Override
                    void success(Json data) {
                        replace(post, data);
                    }
                });

            }

            TextView title = convertView.findViewById(R.id.title);
            ImageView avatar = convertView.findViewById(R.id.avatar);
            TextView date = convertView.findViewById(R.id.date);
            avatar.setVisibility(View.VISIBLE);
            date.setVisibility(View.VISIBLE);

            if (post.getString("title", "").equals("")) {
                title.setVisibility(View.GONE);
            } else {
                title.setText(Fx.fromHtml(post.getString("title", "")));
                title.setVisibility(View.VISIBLE);
            }
            date.setText(Since.format(context, post.parseDate("date"), 2));
            TextView text = convertView.findViewById(R.id.text);
            if (post.getString("text", "").equals("")) {
                text.setVisibility(View.GONE);
            } else {
                text.setVisibility(View.VISIBLE);
                text.setText(PostParser.parse(post.getString("text", ""), post.getListJson("docs"), post.getListJson("links")));
            }

            Glide.with(context).load(Uri.parse(post.getJson("user").getString("avatar") + "@64x64"))
                    .error(R.drawable.logo)
                    .into(avatar);


            View link_area = convertView.findViewById(R.id.link);

            if (post.containsKey("link")) {
                link_area.setVisibility(View.VISIBLE);
                Json link = post.getJson("link");
                ImageView link_image = convertView.findViewById(R.id.link_image);
                TextView link_title = convertView.findViewById(R.id.link_title);
                TextView link_text = convertView.findViewById(R.id.link_text);
                TextView link_date = convertView.findViewById(R.id.link_date);

                link_date.setText(Since.format(context, post.parseDate("date"), 2));
                link_title.setText(Fx.fromHtml(link.getString("title", "")));
                link_text.setText(Fx.fromHtml(link.getString("description", "")));

                Glide.with(context).load(Uri.parse(link.getString("image") + "@290x180"))
                        .error(R.drawable.logo)
                        .into(link_image);

                //todo possible bug itemClick on list?
                link_area.setOnClickListener(v -> Fx.browse(context, Uri.parse(link.getString("url"))));
                if (link.getString("title", "").equals(post.getString("title", ""))) {
                    title.setVisibility(View.GONE);
                    avatar.setVisibility(View.GONE);
                    date.setVisibility(View.GONE);
                }

            } else {
                link_area.setVisibility(View.GONE);
            }


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
