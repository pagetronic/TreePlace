package live.page.android.ui.select;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import live.page.android.R;
import live.page.android.api.Json;

public class SelectView extends LinearLayout {

    private String url;
    private String hint;
    private ImageView arrow;
    private boolean multiple = false;
    private SelectDialog selectable;

    public SelectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(LinearLayout.HORIZONTAL);

        if (attrs != null) {
            TypedArray attributes = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.selectable, 0, 0);
            url = attributes.getString(R.styleable.selectable_url);
            hint = attributes.getString(R.styleable.selectable_hint);
            multiple = attributes.getBoolean(R.styleable.selectable_multiple, false);
            attributes.recycle();
        }

        setOrientation(LinearLayout.HORIZONTAL);
        setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 10F));
        setHint();

        arrow = new ImageView(context);
        arrow.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        arrow.setBackground(context.getDrawable(android.R.drawable.arrow_down_float));
        addView(arrow);


        setOnClickListener(v -> selectable = new SelectDialog(getContext(), url, getChoices(), multiple, new SelectAction() {
            @Override
            public void onChoices(List<Json> choices) {
                setChoices(choices);
            }
        }));
    }


    private void setHint() {
        removeAllViews();
        TextView text = new TextView(getContext());
        text.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        text.setHint(hint);
        addView(text);

    }

    public List<String> getValues() {
        return selectable.getValues();
    }

    public String getValue() {
        if (getValues().size() == 0) {
            return null;
        }
        return getValues().get(0);
    }

    private List<Json> getChoices() {
        if (selectable == null) {
            return new ArrayList<>();
        }
        return selectable.getChoices();
    }

    private void setChoices(List<Json> choices) {
        choices.forEach(selectable::add);

    }

    private void setChoice(Json choice) {
        setChoices(Collections.singletonList(choice));

    }

}
