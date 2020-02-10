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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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


    private View threadView = null;
    private View replyView = null;
    private ApiRequest postReq;

    @Override
    protected int getLayout() {
        return R.layout.thread;
    }

    @Override
    protected void onCreate() {

        ListView listView = findViewById(R.id.thread);
        listView.setDivider(null);
        listView.setBackgroundColor(getContext().getColor(R.color.grey_light));
        final ThreadAdapter adapter = new ThreadAdapter(listView);
        listView.setAdapter(adapter);
        makeJumper(listView);

        final SwipeRefreshLayout swiper = findViewById(R.id.swiper);
        String thread_id = getIntent().getStringExtra("id");
        final String url = "/threads/" + thread_id;
        adapter.get(url);
        swiper.setOnRefreshListener(() -> adapter.get(swiper, url));


        final LayoutInflater inflater = getLayoutInflater();
        threadView = inflater.inflate(R.layout.thread_post, new LinearLayout(this));

        replyView = inflater.inflate(R.layout.thread_reply, new LinearLayout(this));
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

            Json post = (pos == 0) ? adapter.getBase() : adapter.getJson(pos - 1);

            if (post != null && post.getId() != null) {
                List<Command> options = new ArrayList<>();
                if (user != null) {

                    options.add(new Command(getString(R.string.rapid_comment)) {
                        @Override
                        public void onClick() {
                            PostEditor.rapid(getContext(), post.getId(), new PostEditor() {
                                @Override
                                void success(Json data) {

                                }
                            });
                        }
                    });

                    if (user.getId().equals(post.getJson("user").getId()) || user.getBoolean("editor", false)) {
                        options.add(new Command(getString(R.string.edit), R.drawable.edit) {
                            @Override
                            public void onClick() {
                                if (pos == 0) {
                                    adapter.setBase(post.clone().put("editable", true));
                                } else {
                                    adapter.replace(post, post.clone().put("editable", true));
                                }
                                adapter.notifyDataSetChanged();
                            }
                        });
                        options.add(new Command(getString(R.string.delete), R.drawable.delete) {
                            @Override
                            public void onClick() {
                                PostEditor.delete(getContext(), post.getId(), new PostEditor() {
                                    @Override
                                    void success(Json data) {

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
            return true;
        });

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

    private class ThreadAdapter extends ApiAdapter {
        private Json base = null;

        private ThreadAdapter(ListView list) {
            super(list, R.layout.thread_post, false);
        }

        private View getHeadPost() {
            if (base == null) {
                return new View(context);
            }
            return getView(threadView, base);
        }

        private View getFormReply() {
            if (base == null) {
                return new View(context);
            }
            return replyView;
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
            return super.getCount() + 2;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (position == 0) {
                return getHeadPost();
            }

            if (position == getCount() - 1) {
                return getFormReply();
            }

            return super.getView(position - 1, convertView, parent);
        }

        @Override
        protected Json getData(final Json data) {

            getSupportActionBar().setTitle(data.getString("title"));
            setBase(data);
            return data.getJson("posts");
        }


        @Override
        public void replace(Json before, Json after) {
            if (getBase().getId().equals(before.getId())) {
                setBase(after);
                notifyDataSetChanged();
                return;
            }
            super.replace(before, after);
        }

        public Json getBase() {
            return base;
        }

        public void setBase(Json base) {
            this.base = base;
        }
    }

}
