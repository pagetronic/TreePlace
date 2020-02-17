package live.page.android.ui.select;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import live.page.android.R;
import live.page.android.api.Json;
import live.page.android.utils.Settings;

public class SelectDialog {

    private SelectAdapter selection;

    public SelectDialog(final Context ctx, final String url, List<Json> initial, final boolean multiple, final SelectAction onChoice) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setCancelable(true);
        builder.setView(R.layout.selectable);
        final AlertDialog dialog = builder.show();
        dialog.show();


        final EditText search = dialog.findViewById(R.id.search);
        ListView list = dialog.findViewById(R.id.list);

        final Json data = new Json("action", "search").put("search", "").put("lng", Settings.getLng(ctx));

        selection = new SelectAdapter(list, multiple, dialog);
        selection.addValues(initial);
        list.setAdapter(selection);
        selection.post(url, data);

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                selection.clear();
                data.put("search", search.getText().toString());
                selection.post(url, data);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        dialog.setOnCancelListener((d) -> onChoice.onChoices(selection.getChoices()));
    }

    public SelectDialog(Context context, String url, SelectAction selectAction) {
        this(context, url, new ArrayList<>(), false, selectAction);
    }


    public List<String> getValues() {
        return selection.getValues();
    }

    public String getValue() {
        if (selection.size() == 0) {
            return null;
        }
        return getValues().get(0);
    }

    public List<Json> getChoices() {
        return selection.getChoices();
    }

    private void setChoices(List<Json> choices) {
        choices.forEach(this::add);

    }

    private void setChoice(Json choice) {
        setChoices(Collections.singletonList(choice));

    }

    public void add(Json item) {
        selection.addValue(item);
    }
}
