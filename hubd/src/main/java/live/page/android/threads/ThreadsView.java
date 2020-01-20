package live.page.android.threads;

import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import live.page.android.R;
import live.page.android.api.Json;
import live.page.android.sys.PageActivity;
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
        adapter.get("/threads/" + getIntent().getStringExtra("id") + "?paging=first");
        final LayoutInflater inflater = getLayoutInflater();
        firstView = inflater.inflate(R.layout.thread_post, new LinearLayout(this));
        lastView = inflater.inflate(R.layout.thread_reply, new LinearLayout(this));

    }

    private class ThreadAdapter extends ApiAdapter {
        private Json data = null;

        private ThreadAdapter() {
            super(ThreadsView.this, R.layout.thread_post);
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

            ((TextView) convertView.findViewById(R.id.title)).setText(Html.fromHtml(thread.getString("title", ""), Html.FROM_HTML_MODE_LEGACY));
            ((TextView) convertView.findViewById(R.id.text)).setText(PostParser.parse(thread.getString("text", ""), thread.getListJson("docs"), thread.getListJson("links")));

            Glide.with(context).load(Uri.parse(thread.getJson("user").getString("avatar") + "@64x64"))
                    .error(R.drawable.logo)
                    .into((ImageView) convertView.findViewById(R.id.avatar));

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
