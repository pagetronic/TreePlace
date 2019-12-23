package com.agroneo.treeplace.sys;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.agroneo.treeplace.R;

public class Selectable {
    public static void make(final Activity activity, String url, final TextView view) {
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

                list.setAdapter(new BaseAdapter() {

                    @Override
                    public int getCount() {
                        return 0;
                    }

                    @Override
                    public Object getItem(int position) {
                        return null;
                    }

                    @Override
                    public long getItemId(int position) {
                        return 0;
                    }

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        return null;
                    }
                });


            }
        });
    }
}
