package live.page.android.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import live.page.android.R;
import live.page.android.api.Json;

public class AccountsChooser {

    public static void make(final Activity activity) {

        ImageView avatar = activity.findViewById(R.id.avatar);

        final String account_name = Accounts.getAccountNameActive(activity);

        if (account_name != null) {
            Json profile = Accounts.getProfile(activity, account_name);
            if (profile != null) {
                String logo = profile.getString("logo");
                if (logo != null) {
                    Glide.with(activity).load(Uri.parse(logo + "@128"))
                            .error(R.drawable.logo)
                            .circleCrop()
                            .into(avatar);
                }
            }
        }


        avatar.setOnClickListener(v -> {

            final Account[] accounts = AccountManager.get(activity).getAccountsByType(activity.getResources().getString(R.string.account_type));
            if (accounts.length > 0) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(activity);


                builder.setTitle(R.string.select_account);
                final LayoutInflater inflater = activity.getLayoutInflater();

                ListView list = new ListView(activity);

                builder.setView(list);
                builder.setCancelable(true);
                final AlertDialog dialog = builder.show();
                list.setAdapter(new BaseAdapter() {
                    @Override
                    public int getCount() {
                        return accounts.length + 1;
                    }

                    @Override
                    public Object getItem(int position) {
                        return accounts.length < position ? accounts[position] : null;
                    }

                    @Override
                    public long getItemId(int position) {
                        return -1;
                    }

                    @Override
                    public View getView(final int position, View convertView, ViewGroup parent) {

                        View view = inflater.inflate(R.layout.accounts_view, null);

                        view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Accounts.authBrowser(activity);
                                dialog.cancel();
                            }
                        });

                        ImageView avatar1 = view.findViewById(R.id.avatar);
                        TextView text = view.findViewById(R.id.title);
                        TextView email = view.findViewById(R.id.email);

                        if (position >= accounts.length) {
                            email.setVisibility(View.GONE);
                            text.setText(R.string.new_account);
                            return view;
                        }
                        Json profile = Accounts.getProfile(activity, account_name);
                        text.setText(profile.getString("name"));
                        email.setText(profile.getString("email"));

                        Glide.with(activity).load(Uri.parse(profile.getString("logo") + "@128"))
                                .error(R.drawable.logo)
                                .circleCrop()
                                .into(avatar1);

                        return view;
                    }
                });
            } else {
                Accounts.authBrowser(activity);
            }
        });

    }

}
