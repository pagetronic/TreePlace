package live.page.android.threads.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import live.page.android.R;
import live.page.android.api.Json;
import live.page.android.ui.select.SelectAction;
import live.page.android.ui.select.SelectDialog;
import live.page.android.utils.Fx;

public class SeoNav {

    public static class NavAdapter extends RecyclerView.Adapter<NavAdapter.NavView> {

        private List<Json> items = new ArrayList<>();
        private Context context;
        private boolean isAdmin;

        public NavAdapter(Context context, boolean isAdmin) {
            this.context = context;
            this.isAdmin = isAdmin;
        }

        @NonNull
        @Override
        public NavView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.thread_nav, parent, false);
            return new NavView(view);
        }

        @Override
        public void onBindViewHolder(@NonNull NavView holder, int position) {
            holder.make(items.get(position), isAdmin);

        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public void add(Json item) {
            items.add(item);
            notifyDataSetChanged();
        }


        private class NavView extends RecyclerView.ViewHolder {

            public NavView(@NonNull View itemView) {
                super(itemView);
            }

            public void make(Json item, boolean isAdmin) {
                TextView separator = itemView.findViewById(R.id.separator);
                TextView title = itemView.findViewById(R.id.title);
                TextView intro = itemView.findViewById(R.id.intro);
                ImageView icon = itemView.findViewById(R.id.icon);

                if (isAdmin) {
                    if (item.getString("type") != null) {
                        makeAdmin(itemView.findViewById(R.id.admin), item.getString("type"));
                    } else {
                        itemView.findViewById(R.id.admin).setVisibility(View.GONE);
                    }
                }

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

                    if (item.containsKey("intro") || item.containsKey("text")) {
                        intro.setVisibility(View.VISIBLE);
                        intro.setText(item.getString("intro", item.getString("text", "")));
                    } else {
                        intro.setVisibility(View.GONE);
                    }
                    icon.setImageResource(item.getInteger("icon"));

                }
            }

            private void makeAdmin(View admin, String type) {
                admin.setVisibility(View.VISIBLE);
                switch (type) {
                    case "pages":
                        admin.setOnClickListener((v) -> {
                            new SelectDialog(context, "/threads", null, false, new SelectAction() {

                                @Override
                                public void onValue(String value) {
                                    Fx.log(value);
                                }
                            });
                        });
                        break;
                    case "forums":
                        break;
                }

            }
        }
    }
}
