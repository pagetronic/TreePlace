package live.page.android.ui.select;

import android.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import live.page.android.R;
import live.page.android.api.ApiAdapter;
import live.page.android.api.Json;

class SelectAdapter extends ApiAdapter {

    private final List<Json> selection = new ArrayList<>();
    private boolean multiple;
    private AlertDialog dialog;

    public SelectAdapter(@NonNull ListView list, boolean multiple, AlertDialog dialog) {
        super(list);
        this.multiple = multiple;
        this.dialog = dialog;
    }


    @Override
    public int getCount() {
        return super.getCount() + getVisibleSelection().size();
    }

    @Override
    public Object getItem(int position) {

        if (position < getVisibleSelection().size()) {
            return getVisibleSelection().get(position);
        }
        return super.getItem(position - getVisibleSelection().size());
    }

    @Override
    public View getView(View view, Json item) {

        ((TextView) view.findViewById(R.id.title)).setText(item.getString("title", item.getString("name")));
        view.setOnClickListener(v -> {
            if (multiple) {
                addValue(item);
                TextView option = new TextView(context);
                option.setText(item.getString("title", item.getString("name")));

            } else {
                selection.clear();
                setValue(item);
                dialog.cancel();
            }
        });

        ((TextView) view.findViewById(R.id.title)).setText(item.getString("title", item.getString("name")));
        ImageView action = view.findViewById(R.id.action);
        if (selected(item)) {
            action.setVisibility(View.VISIBLE);
            action.setOnClickListener((v) -> removeValue(item));
        } else {
            action.setVisibility(View.GONE);
            action.setOnClickListener(null);
        }


        return view;
    }


    private List<Json> getVisibleSelection() {
        List<Json> visible_selection = new ArrayList<>();
        for (Json option : selection) {
            if (!containsId(option.getId())) {
                visible_selection.add(option);
            }
        }
        return visible_selection;
    }


    private boolean containsId(String id) {
        if (id == null) {
            return false;
        }
        for (Json item : items) {
            if (item.getId() != null && item.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public List<Json> getChoices() {
        return selection;
    }

    public List<String> getValues() {
        List<String> values = new ArrayList<>();
        for (Json select : selection) {
            values.add(select.getId());
        }
        return values;
    }

    public int size() {
        return getCount();
    }

    public boolean selected(Json item) {
        if (item == null) {
            return false;
        }
        for (int position = 0; position < selection.size(); position++) {
            if (selection.get(position).getId().equals(item.getId())) {
                return true;
            }
        }
        return false;
    }

    public void addValue(Json item) {
        if (selected(item)) {
            return;
        }
        selection.add(item);
        notifyDataSetChanged();
    }

    public void addValues(List<Json> items) {
        if (items == null) {
            return;
        }
        for (Json item : items) {
            if (!selected(item)) {
                selection.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public void removeValue(Json item) {
        selection.remove(item);
        notifyDataSetChanged();
    }

    public void setValue(Json item) {
        selection.clear();
        selection.add(item);
        notifyDataSetChanged();

    }
}
