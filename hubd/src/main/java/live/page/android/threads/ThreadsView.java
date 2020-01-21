package live.page.android.threads;

import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import live.page.android.R;
import live.page.android.api.Json;
import live.page.android.sys.Command;
import live.page.android.sys.PageActivity;
import live.page.android.sys.Since;
import live.page.android.views.ApiAdapter;

public class ThreadsView extends PageActivity {


    private View firstView = null;
    private View lastView = null;


    @Override
    protected int getLayout() {
        return R.layout.thread;
    }

    @Override
    protected void onCreate() {
        final ThreadAdapter adapter = new ThreadAdapter();
        ((ListView) findViewById(R.id.thread)).setAdapter(adapter);

        //TODO : manage "paging prev"
        adapter.get("/threads/" + getIntent().getStringExtra("id") + "?paging=first");

        final SwipeRefreshLayout swiper = findViewById(R.id.swiper);
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swiper.setRefreshing(false);
                adapter.clear();
                adapter.get("/threads/" + getIntent().getStringExtra("id") + "?paging=first");
            }
        });

        final LayoutInflater inflater = getLayoutInflater();
        firstView = inflater.inflate(R.layout.thread_post, new LinearLayout(this));
        lastView = inflater.inflate(R.layout.thread_reply, new LinearLayout(this));

    }

    public boolean command(View view, final Json post) {
        List<Command> options = new ArrayList<>();
        if (user != null) {

            options.add(new Command(getString(R.string.rapid_comment)) {
                @Override
                public void onClick() {
                    PostEditor.rapid(getBaseContext(), post.getId(), new PostEditor() {
                        @Override
                        void success() {

                        }
                    });
                }
            });

            if (user.getId().equals(post.getJson("user").getId()) || user.getBoolean("editor", false)) {
                options.add(new Command(getString(R.string.edit), R.drawable.edit) {
                    @Override
                    public void onClick() {
                        PostEditor.edit(getBaseContext(), post.getId(), new PostEditor() {
                            @Override
                            void success() {

                            }
                        });
                    }
                });
                options.add(new Command(getString(R.string.delete), R.drawable.delete) {
                    @Override
                    public void onClick() {
                        PostEditor.delete(getBaseContext(), post.getId(), new PostEditor() {
                            @Override
                            void success() {

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

    private class ThreadAdapter extends ApiAdapter {
        private Json data = null;

        private ThreadAdapter() {
            super(getBaseContext(), R.layout.thread_post);
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
        public View getView(final View convertView, final Json thread) {


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


            convertView.findViewById(R.id.command).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    command(convertView, thread);
                }
            });
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return command(convertView, thread);
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
