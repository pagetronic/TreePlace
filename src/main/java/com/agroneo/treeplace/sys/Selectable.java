package com.agroneo.treeplace.sys;

import android.app.Activity;
import android.app.AlertDialog;
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
                builder.setView(R.layout.selectable);
                final AlertDialog dialog = builder.show();
                dialog.show();

                EditText search = dialog.findViewById(R.id.search);
                ListView list = dialog.findViewById(R.id.list);

                ApiAdapter adapter = new ApiAdapter(activity, R.layout.species) {
                    @Override
                    public View getView(View view, Json item) {
                        ((TextView) view.findViewById(R.id.name)).setText(item.getString("name"));
                        Fx.log(item.getString("name"));
                        return view;
                    }
                };
                list.setAdapter(adapter);
                adapter.post(url, new Json("action", "search").put("q", ""));

            }
        });
    }
}
