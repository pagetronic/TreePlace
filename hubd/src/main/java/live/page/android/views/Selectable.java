package live.page.android.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import java.util.ArrayList;
import java.util.List;

import live.page.android.R;
import live.page.android.api.Json;

public class Selectable extends LinearLayout {

    private String url;
    private String hint;
    private LinearLayout choices;
    private ImageView arrow;
    private List<String> values = new ArrayList<>();
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

        choices = new LinearLayout(context);
        choices.setOrientation(LinearLayout.HORIZONTAL);
        choices.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 10F));
        setHint();
        addView(choices);

        arrow = new ImageView(context);
        arrow.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        arrow.setBackground(context.getDrawable(android.R.drawable.arrow_down_float));
        addView(arrow);


        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectable(getContext(), url, multiple, new Select() {
                    @Override
                    public void onChoice(List<Json> choices) {

                        setChoice(choices);
                    }
                });
            }
        });
    }

    public static void selectable(Context ctx, String url, List<Json> options, boolean multiple, Select onChoice) {
        selectable(ctx, url, multiple, onChoice);
    }

    public static void selectable(final Context ctx, final String url, final boolean multiple, final Select onChoice) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setCancelable(true);
        builder.setView(R.layout.selectable);
        final AlertDialog dialog = builder.show();
        dialog.show();

        final List<Json> values = new ArrayList<>();

        final EditText search = dialog.findViewById(R.id.search);
        ListView list = dialog.findViewById(R.id.list);

        //   ((ListView) dialog.findViewById(R.id.selected));

        final Json data = new Json("action", "search").put("search", "").put("lng", "fr");
        final ApiAdapter adapter = new ApiAdapter(ctx) {
            @Override
            public View getView(final View view, final Json item) {
                ((TextView) view.findViewById(R.id.title)).setText(item.getString("title", item.getString("name")));
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (multiple) {
                            if (!values.contains(item)) {
                                values.add(item);
                                // selected.add(item);
                            }
                        } else {
                            values.add(0, item);
                            dialog.cancel();
                        }
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
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                onChoice.onChoice(values);
            }
        });
    }

    public void setUrl(String url) {
        this.url = url;
    }

    private void setChoice(List<Json> chooses) {

        values.clear();
        for (Json choose : chooses) {
            choices.removeAllViews();
            TextView text = new TextView(getContext());
            text.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            if (choose == null) {
                text.setHint(hint);
            } else {
                text.setText(choose.getString("name"));
            }
            values.add(choose.getId());
            choices.addView(text);
        }
    }


    private void setHint() {
        choices.removeAllViews();
        TextView text = new TextView(getContext());
        text.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        text.setHint(hint);
        choices.addView(text);

    }

    public List<String> getValues() {
        return values;
    }

    public String getValue() {
        if (values.size() == 0) {
            return null;
        }
        return values.get(0);
    }

    public static abstract class Select {
        public abstract void onChoice(List<Json> choice);
    }
}
