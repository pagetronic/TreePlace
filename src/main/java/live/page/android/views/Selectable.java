package live.page.android.views;

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

import com.agroneo.droid.R;

import java.util.ArrayList;
import java.util.List;

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

                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setCancelable(true);
                builder.setView(R.layout.selectable_list);
                final AlertDialog dialog = builder.show();
                dialog.show();

                final EditText search = dialog.findViewById(R.id.search);
                ListView list = dialog.findViewById(R.id.list);
                final Json data = new Json("action", "search").put("search", "");
                final ApiAdapter adapter = new ApiAdapter(getContext()) {
                    @Override
                    public View getView(View view, final Json item) {
                        ((TextView) view.findViewById(R.id.title)).setText(item.getString("name"));
                        view.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (multiple) {
                                    if (!values.contains(item.getId())) {
                                        addChoices(item);
                                    }
                                } else {
                                    setChoice(item);

                                }
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
        });
    }


    public void setUrl(String url) {
        this.url = url;
    }

    private void setChoice(Json choice) {

        choices.removeAllViews();
        TextView text = new TextView(getContext());
        text.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        if (choice == null) {
            text.setHint(hint);
        } else {
            text.setText(choice.getString("name"));
        }
        values.clear();
        values.add(choice.getId());
        choices.addView(text);
    }

    private void addChoices(Json choice) {
        TextView text = new TextView(getContext());
        text.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        text.setText(choice.getString("name"));
        values.add(choice.getId());
        choices.addView(text);
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
}
