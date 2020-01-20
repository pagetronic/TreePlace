package live.page.android.sys;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import live.page.android.R;

public abstract class Command {
    public String title;

    public Command(String title) {
        this.title = title;
    }

    public static void make(final Context context, Command... commands) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        ListView list = new ListView(context);
        builder.setView(list);
        builder.setCancelable(true);
        final AlertDialog dialog = builder.show();
        list.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return 1;
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return -1;
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {

                View view = LayoutInflater.from(context).inflate(R.layout.selectable_option, null);

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });
                return view;
            }
        });
    }

    public abstract void onClick();
}
