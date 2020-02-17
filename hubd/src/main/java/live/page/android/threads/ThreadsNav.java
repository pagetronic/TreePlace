package live.page.android.threads;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import live.page.android.R;
import live.page.android.api.Json;
import live.page.android.ui.select.SelectAction;
import live.page.android.ui.select.SelectDialog;
import live.page.android.utils.Fx;

@SuppressLint("ViewConstructor")
public class ThreadsNav extends RecyclerView {

    private NavAdapter adapter;

    public ThreadsNav(@NonNull Context context, boolean isAdmin) {
        super(context);

        setLayoutManager(new LinearLayoutManager(getContext()));
        setAdapter(new NavAdapter(context, isAdmin));

        if (isAdmin) {

            ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {

                @Override
                public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull ViewHolder viewHolder) {
                    if (adapter.getItem(viewHolder.getAdapterPosition()).containsKey("drag")) {
                        return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT);
                    }
                    return ItemTouchHelper.ACTION_STATE_IDLE;
                }

                @Override
                public boolean canDropOver(@NonNull RecyclerView recyclerView, @NonNull ViewHolder current, @NonNull ViewHolder target) {
                    return adapter.getItem(target.getAdapterPosition()).getString("drag", "").equals(adapter.getItem(current.getAdapterPosition()).getString("drag"));
                }

                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    adapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                    return true;
                }


                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                    adapter.onItemDismiss(viewHolder.getAdapterPosition());
                }

            };

            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);

            itemTouchHelper.attachToRecyclerView(ThreadsNav.this);

        }
    }

    public void add(Json item) {
        adapter.add(item);
    }

    public void setAdapter(NavAdapter adapter) {
        this.adapter = adapter;
        super.setAdapter(adapter);
    }

    public int getCount() {
        return adapter.getItemCount();
    }

    public void refresh() {
        adapter.notifyDataSetChanged();
    }

    private class NavAdapter extends RecyclerView.Adapter<NavAdapter.NavView> {

        private List<Json> items = new ArrayList<>();
        private Context context;
        private boolean isAdmin;

        public NavAdapter(Context context, boolean isAdmin) {
            this.context = context;
            this.isAdmin = isAdmin;
        }

        public void onItemDismiss(int position) {
            items.remove(position);
            notifyItemRemoved(position);
        }

        public void onItemMove(int fromPosition, int toPosition) {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(items, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(items, i, i - 1);
                }
            }
            notifyItemMoved(fromPosition, toPosition);
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
        }

        public Json getItem(int position) {
            return items.get(position);
        }


        private class NavView extends RecyclerView.ViewHolder {

            private NavView(@NonNull View itemView) {
                super(itemView);
            }

            private void make(Json item, boolean isAdmin) {
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
