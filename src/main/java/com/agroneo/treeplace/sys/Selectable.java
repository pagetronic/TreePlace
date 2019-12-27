package com.agroneo.treeplace.sys;

import android.app.Activity;
import android.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.agroneo.treeplace.R;
import com.agroneo.treeplace.api.Json;

public class Selectable {

    public Selectable(final Activity activity, final String url, final View view) {

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setCancelable(true);
                builder.setView(R.layout.selectable_list);
                final AlertDialog dialog = builder.show();
                dialog.show();

                final EditText search = dialog.findViewById(R.id.search);
                ListView list = dialog.findViewById(R.id.list);
                final Json data = new Json("action", "search").put("search", "");
                final ApiAdapter adapter = new ApiAdapter(activity) {
                    @Override
                    public View getView(View view, Json item) {
                        ((TextView) view.findViewById(R.id.title)).setText(item.getString("name"));
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
}
