package live.page.android.views.select;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.List;

import live.page.android.R;
import live.page.android.api.ApiAdapter;
import live.page.android.api.Json;
import live.page.android.sys.Settings;

public class Selectable extends LinearLayout {

    private final SelectionAdapter selection = new SelectionAdapter(this);
    private String url;
    private String hint;
    private LinearLayout view;
    private ImageView arrow;
    private boolean multiple = false;

    public Selectable(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(LinearLayout.HORIZONTAL);

        if (attrs != null) {
            TypedArray attributes = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.selectable, 0, 0);
            url = attributes.getString(R.styleable.selectable_url);
            hint = attributes.getString(R.styleable.selectable_hint);
            multiple = attributes.getBoolean(R.styleable.selectable_multiple, false);
            attributes.recycle();
        }

        view = new LinearLayout(context);
        view.setOrientation(LinearLayout.HORIZONTAL);
        view.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 10F));
        setHint();
        addView(view);

        arrow = new ImageView(context);
        arrow.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        arrow.setBackground(context.getDrawable(android.R.drawable.arrow_down_float));
        addView(arrow);


        setOnClickListener(v -> selectable(getContext(), url, multiple, new SelectAction() {
            @Override
            public void onChoices(List<Json> choices) {

                setChoices(choices);
            }
        }));
    }

    public Selectable(Context ctx, String url, List<Json> options, boolean multiple, SelectAction onChoice) {
        super(ctx, null);
        selectable(ctx, url, multiple, onChoice);
    }


    public void remove(Json item) {
        if (item == null) {
            return;
        }
        selection.remove(item);
    }

    public void add(Json item) {
        if (item == null || selection.contains(item)) {
            return;
        }
        selection.add(item);
    }

    public void set(Json item) {
        selection.clear();
        if (item == null) {
            return;
        }
        selection.add(item);
    }


    private void selectable(final Context ctx, final String url, final boolean multiple, final SelectAction onChoice) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setCancelable(true);
        builder.setView(R.layout.selectable);
        final AlertDialog dialog = builder.show();
        dialog.show();


        final EditText search = dialog.findViewById(R.id.search);
        ListView list = dialog.findViewById(R.id.list);
        ((ListView) dialog.findViewById(R.id.selected)).setAdapter(selection);


        final Json data = new Json("action", "search").put("search", "").put("lng", Settings.getLng(ctx));
        final ApiAdapter adapter = new ApiAdapter(ctx) {
            @Override
            public View getView(final View view, final Json item) {
                ((TextView) view.findViewById(R.id.title)).setText(item.getString("title", item.getString("name")));
                view.setOnClickListener(v -> {
                    if (multiple) {
                        add(item);
                        TextView option = new TextView(context);
                        option.setText(item.getString("title", item.getString("name")));

                    } else {
                        set(item);
                        dialog.cancel();
                    }
                });
                return view;
            }
        };
        list.setAdapter(adapter);
        adapter.post(url, data);

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.clear();
                data.put("search", search.getText().toString());
                adapter.post(url, data);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public void setUrl(String url) {
        this.url = url;
    }

    private void setHint() {
        view.removeAllViews();
        TextView text = new TextView(getContext());
        text.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        text.setHint(hint);
        view.addView(text);

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

    private List<Json> getChoices() {
        return selection.getChoices();
    }

    private void setChoices(List<Json> choices) {
        selection.addAll(choices);
    }

    private void setChoice(Json choice) {
        setChoices(Collections.singletonList(choice));

    }
}
