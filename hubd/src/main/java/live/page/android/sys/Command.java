package live.page.android.sys;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.IdRes;

import java.util.List;

import live.page.android.R;

public abstract class Command {
    public String title;
    public int icon = -1;

    public Command(String title) {
        this.title = title;
    }

    public Command(String title, @IdRes int icon) {
        this.title = title;
        this.icon = icon;
    }

    public static void make(final Context context, List<Command> commands) {
        make(context, commands.toArray(new Command[0]));
    }

    public static void make(final Context context, final Command... commands) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        ListView list = new ListView(context);
        builder.setView(list);
        builder.setCancelable(true);
        final AlertDialog dialog = builder.show();
        list.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return commands.length;
            }

            @Override
            public Object getItem(int position) {
                return commands[position];
            }


            @Override
            public long getItemId(int position) {
                return -1;
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(context).inflate(R.layout.selectable_option, new LinearLayout(context));
                }
                final Command command = (Command) getItem(position);
                ((TextView) convertView.findViewById(R.id.title)).setText(command.title);
                ImageView icon = convertView.findViewById(R.id.icon);
                if (command.icon != -1) {
                    icon.setVisibility(View.VISIBLE);
                    icon.setImageResource(command.icon);
                } else {
                    icon.setVisibility(View.GONE);
                }
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        command.onClick();
                        dialog.cancel();
                    }
                });
                return convertView;
            }
        });
    }

    public abstract void onClick();
}
