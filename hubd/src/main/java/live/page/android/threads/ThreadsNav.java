package live.page.android.threads;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import live.page.android.R;
import live.page.android.api.ApiAsync;
import live.page.android.api.Json;
import live.page.android.ui.select.SelectAction;
import live.page.android.ui.select.SelectDialog;
import live.page.android.utils.Fx;

@SuppressLint("ViewConstructor")
public class ThreadsNav extends RecyclerView {

    private NavAdapter adapter;
    private String thread_id;
    private boolean isEditor;
    private ThreadsView threadsView;


    public ThreadsNav(@NonNull ThreadsView threadsView, String thread_id, boolean isEditor) {
        super(threadsView);
        this.thread_id = thread_id;
        this.isEditor = isEditor;
        this.threadsView = threadsView;

        setLayoutManager(new LinearLayoutManager(getContext()));
        setAdapter(new NavAdapter());

        if (isEditor) {

            ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {

                @Override
                public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull ViewHolder viewHolder) {
                    Json item = adapter.getItem(viewHolder.getAdapterPosition());
                    if (item.containsKey("type") && !item.containsKey("separator")) {
                        return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT);
                    }
                    return ItemTouchHelper.ACTION_STATE_IDLE;
                }

                @Override
                public boolean canDropOver(@NonNull RecyclerView recyclerView, @NonNull ViewHolder current, @NonNull ViewHolder target) {
                    Json targetItem = adapter.getItem(target.getAdapterPosition());
                    if (targetItem.containsKey("separator")) {
                        return false;
                    }
                    Json currentItem = adapter.getItem(current.getAdapterPosition());
                    return targetItem.getString("type", "").equals(currentItem.getString("type"));
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

    public static boolean makeNav(ThreadsView threadsView, NavigationView nav, Json thread, boolean isEditor) {

        nav.removeAllViews();
        ThreadsNav lateralNav = new ThreadsNav(threadsView, thread.getId(), isEditor);
        nav.addView(lateralNav);

        List<Json> pages = thread.getListJson("pages");
        if (pages.size() > 0 || isEditor) {
            lateralNav.add(new Json("separator", threadsView.getString(R.string.related_pages)).put("type", "pages"));
            for (Json page : pages) {
                lateralNav.add(page.put("type", "pages"));
            }
        }
        List<Json> forums = thread.getListJson("forums");
        if (forums.size() > 0 || isEditor) {
            lateralNav.add(new Json("separator", threadsView.getString(R.string.related_forums)).put("type", "forums"));
            for (Json forum : forums) {
                lateralNav.add(forum.put("type", "forums"));
            }
        }
        List<Json> branch = thread.getListJson("branch");
        if (branch.size() > 0) {
            lateralNav.add(new Json("separator", threadsView.getString(R.string.same_posts)));
            for (Json post : branch) {
                lateralNav.add(post);
            }
        }
        if (lateralNav.getCount() == 0) {
            return false;
        }
        lateralNav.refresh();

        return true;

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

    private void sortedAdmin(String type) {
        List<String> sort = new ArrayList<>();
        for (Json item : adapter.getItems()) {
            if (!item.containsKey("separator") && item.getString("type", "xx").equals(type)) {
                sort.add(item.getId());
            }
        }
        switch (type) {
            case "pages":
                ApiAsync.post(getContext(), "/threads", new Json("action", "pages_sort").put("id", thread_id).put("pages", sort), null);
                break;

            case "forums":
                ApiAsync.post(getContext(), "/threads", new Json("action", "parents_sort").put("id", thread_id).put("parents", sort), null);
                break;
        }
    }

    private void removeAdmin(Json item) {
        switch (item.getString("type", "")) {
            case "pages":
                ApiAsync.post(getContext(), "/threads", new Json("action", "pages_remove").put("id", thread_id).put("page_id", item.getId()), null);
                break;

            case "forums":
                ApiAsync.post(getContext(), "/threads", new Json("action", "parents_remove").put("id", thread_id).put("parent", "Forums(" + item.getId() + ")"), null);
                break;
        }
    }

    private void addItemAdmin(View admin, String type) {
        admin.setVisibility(View.VISIBLE);
        switch (type) {
            case "pages":
                admin.setOnClickListener((v) -> new SelectDialog(getContext(), "/edit", new SelectAction() {
                    @Override
                    public void onChoice(Json value) {
                        adapter.addItemTo(value, type);
                    }

                    @Override
                    public void onValue(String value) {
                        if (value == null) {
                            return;
                        }
                        ApiAsync.post(getContext(), "/threads", new Json("action", "pages_add").put("id", thread_id).put("page_id", value), null, true);
                    }
                }));
                break;

            case "forums":
                admin.setOnClickListener((v) -> new SelectDialog(getContext(), "/forums", new SelectAction() {
                    @Override
                    public void onChoice(Json value) {
                        adapter.addItemTo(value, type);
                    }

                    @Override
                    public void onValue(String value) {
                        if (value == null) {
                            return;
                        }
                        ApiAsync.post(getContext(), "/threads", new Json("action", "parents_add").put("id", thread_id).put("parent", "Forums(" + value + ")"), null, true);
                    }
                }));
                break;
        }

    }

    private class NavAdapter extends RecyclerView.Adapter<NavAdapter.NavView> {

        private List<Json> items = new ArrayList<>();

        private void onItemDismiss(int position) {
            removeAdmin(items.get(position));
            items.remove(position);
            notifyItemRemoved(position);
        }

        private void onItemMove(int fromPosition, int toPosition) {
            String type = getItem(fromPosition).getString("type");
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
            sortedAdmin(type);
        }


        @NonNull
        @Override
        public NavView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.thread_nav, parent, false);
            return new NavView(view);
        }

        @Override
        public void onBindViewHolder(@NonNull NavView holder, int position) {
            holder.make(items.get(position), isEditor);

        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        private List<Json> getItems() {
            return items;
        }

        public void add(Json item) {
            items.add(item);
        }

        private Json getItem(int position) {
            return items.get(position);
        }


        private void addItemTo(Json item, String type) {
            int position = 0;
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getString("type", "").equals(type)) {
                    position = i + 1;
                }
            }
            item.put("type", type);
            items.add(position, item);
            adapter.notifyItemInserted(position);

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
                    if (item.containsKey("separator") && !item.getString("type", "").equals("")) {
                        addItemAdmin(itemView.findViewById(R.id.admin), item.getString("type"));
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
                        intro.setText(Fx.truncate(item.getString("intro", item.getString("text", "")), 150));
                    } else {
                        intro.setVisibility(View.GONE);
                    }

                    String type = item.getString("type", "posts");
                    icon.setImageResource(type.equals("pages") ? R.drawable.page : type.equals("forums") ? R.drawable.forum : R.drawable.post);
                    switch (type) {
                        case "pages":
                            itemView.setOnClickListener(v -> Fx.browse(getContext(), Uri.parse("https://" + item.getString("domain") + item.getString("url"))));
                            break;
                        case "forums":
                            itemView.setOnClickListener(v -> {
//todo treat as threadsView.load()
                                Intent intent = new Intent(getContext(), ThreadsView.class);
                                intent.putExtra("forum_id", item.getId());
                                threadsView.startActivity(intent);
                                threadsView.finish();
                            });

                            break;
                        case "posts":
                            itemView.setOnClickListener(v -> threadsView.load(item.getId()));
                            break;
                        default:
                            itemView.setOnClickListener(null);
                    }
                }
            }

        }
    }

}
