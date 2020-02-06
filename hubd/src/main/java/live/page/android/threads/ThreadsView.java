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
import live.page.android.api.Json;
import live.page.android.auto.PageActivity;
import live.page.android.utils.Command;
import live.page.android.utils.Since;

public class ThreadsView extends PageActivity {


    private View firstView = null;
    private View lastView = null;
    private ImageButton jumper = null;
    private ViewPropertyAnimator jumperAnim;
    private boolean jumperLocked = false;

    @Override
    protected int getLayout() {
        return R.layout.thread;
    }

    @Override
    protected void onCreate() {

        final ThreadAdapter adapter = new ThreadAdapter();
        ListView list = findViewById(R.id.thread);
        list.setAdapter(adapter);

        final SwipeRefreshLayout swiper = findViewById(R.id.swiper);
        final String url = "/threads/" + getIntent().getStringExtra("id");
        adapter.get(url);
        swiper.setOnRefreshListener(() -> adapter.get(swiper, url));


        final LayoutInflater inflater = getLayoutInflater();
        firstView = inflater.inflate(R.layout.thread_post, new LinearLayout(this));

        lastView = inflater.inflate(R.layout.thread_reply, new LinearLayout(this));
        lastView.findViewById(R.id.cancel).setVisibility(View.GONE);
        lastView.findViewById(R.id.title).setVisibility(View.GONE);


        jumper = findViewById(R.id.reply);
        jumper.setOnClickListener(v -> {
            jumper.setVisibility(View.GONE);
            jumperLocked = true;
            list.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    if (scrollState == 0) {
                        jumperLocked = false;
                        lastView.findViewById(R.id.text).requestFocus();
                        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                                .toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                        list.setOnScrollListener(null);
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                }
            });
            list.smoothScrollToPosition(adapter.getCount() - 1);
        });


    }

    private void viewJumper(boolean visible) {
        if (jumperAnim == null) {
            jumperAnim = jumper.animate();
        }

        if (visible && jumper.getVisibility() != View.VISIBLE && !jumperLocked) {
            jumperAnim.cancel();
            jumper.setVisibility(View.VISIBLE);
            jumper.setAlpha(0F);
            jumperAnim.cancel();
            jumperAnim.setDuration(400);
            jumperAnim.setInterpolator(new AccelerateInterpolator());
            jumperAnim.alpha(1F);
            jumperAnim.start();

        }

        if (!visible && jumper.getVisibility() != View.GONE) {
            jumperAnim.cancel();
            jumperAnim.setDuration(400);
            jumper.setAlpha(1F);
            jumperAnim.setInterpolator(new AccelerateInterpolator());
            jumperAnim.alpha(0F);
            jumperAnim.withEndAction(() -> jumper.setVisibility(View.GONE));
            jumperAnim.start();
        }
    }

    private class ThreadAdapter extends ApiAdapter {
        private Json data = null;

        private ThreadAdapter() {
            super(getContext(), R.layout.thread_post);
        }

        private View getHeadPost() {
            if (data == null) {
                return new View(context);
            }
            return getView(firstView, data);
        }

        private View getFormReply() {
            if (data == null) {
                return new View(context);
            }
            viewJumper(false);
            return lastView;
        }


        public boolean command(View view, final Json post) {
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
                            if (!replace(post, post.clone().put("editable", true))) {
                                data.put("editable", true);
                                notifyDataSetChanged();
                            }
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

        @Override
        public View getView(final View convertView, final Json thread) {

            viewJumper(true);

            if (thread.getBoolean("editable", false)) {

                return PostEditor.edit(getContext(), getLayoutInflater(), thread, new PostEditor() {
                    @Override
                    void success(Json data) {
                        if (!replace(thread, data)) {
                            data.remove("editable");
                            notifyDataSetChanged();
                        }
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


            convertView.findViewById(R.id.command).setOnClickListener(v -> command(convertView, thread));
            convertView.setOnLongClickListener(v -> command(convertView, thread));

            return convertView;
        }

        @Override
        public int getCount() {
            return super.getCount() + 2;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            Json item = (Json) getItem(position - 1);

            if (item != null && item.getBoolean("progress", false)) {
                return progressView();
            }

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
            this.data = data;
            return data.getJson("posts");
        }
    }


}
