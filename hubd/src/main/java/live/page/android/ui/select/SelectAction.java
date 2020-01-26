package live.page.android.ui.select;

import java.util.ArrayList;
import java.util.List;

import live.page.android.api.Json;

public abstract class SelectAction {

    protected void onChoices(List<Json> values) {
        List<String> values_id = new ArrayList<>();
        for (Json value : values) {
            values_id.add(value.getId());

        }
        onValues(values_id);
        if (values_id.size() > 0) {
            onValue(values_id.get(0));
            onChoice(values.get(0));
        } else {
            onValue(null);
        }
    }

    public void onValues(List<String> values) {

    }

    public void onChoice(Json value) {
    }

    public void onValue(String value) {
    }
}
