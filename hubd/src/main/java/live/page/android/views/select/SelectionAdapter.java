package live.page.android.views.select;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import live.page.android.R;
import live.page.android.api.Json;

class SelectionAdapter extends ArrayAdapter<Json> {

    private Selectable selectable;

    public SelectionAdapter(@NonNull Selectable selectable) {

        super(selectable.getContext(), R.layout.selectable_option);
        this.selectable = selectable;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.selectable_option, null);
            convertView.setBackground(null);
        }
        Json item = getItem(position);

        ImageView action = convertView.findViewById(R.id.action);
        action.setVisibility(View.VISIBLE);
        action.setOnClickListener(v -> selectable.remove(item));

        ((TextView) convertView.findViewById(R.id.title)).setText(item.getString("title", item.getString("name")));

        return convertView;
    }


    public List<Json> getChoices() {
        List<Json> choices = new ArrayList<>();
        for (int i = 0; i < getCount(); i++) {
            choices.add(getItem(i));
        }
        return choices;
    }

    public List<String> getValues() {
        List<String> values = new ArrayList<>();
        for (int i = 0; i < getCount(); i++) {
            values.add(getItem(i).getId());
        }
        return values;
    }

    public int size() {
        return getCount();
    }

    public boolean contains(Json item) {
        if (item == null) {
            return false;
        }
        for (int i = 0; i < getCount(); i++) {
            if (getItem(i).getId().equals(item.getId())) {
                return true;
            }
        }
        return false;
    }
}
