package com.agroneo.treeplace.sys;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.agroneo.treeplace.R;
import com.agroneo.treeplace.auth.AuthActivity;
import com.agroneo.treeplace.auth.AuthService;
import com.bumptech.glide.Glide;

public class AccountsChooser {

    public static void make(final Activity activity) {

        ImageView avatar = activity.findViewById(R.id.avatar);
        final String account_name = AuthService.getAccountNameActive(activity);

        if (account_name != null) {
            String logo = AuthService.getAccountData(activity, account_name, "avatar");
            if (logo != null) {
                Glide.with(activity).load(Uri.parse(logo + "@" + avatar.getWidth()))
                        .error(R.drawable.logo)
                        .circleCrop()
                        .into(avatar);
            }
        }

        final Account[] accounts = AccountManager.get(activity).getAccountsByType(activity.getResources().getString(R.string.account_type));
        if (accounts.length > 0) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);


            builder.setTitle(R.string.select_account);
            final LayoutInflater inflater = activity.getLayoutInflater();
            View convertView = inflater.inflate(R.layout.accounts, null);
            builder.setView(convertView);
            builder.setCancelable(true);
            final AlertDialog dialog = builder.show();
            ListView list = convertView.findViewById(R.id.accounts);
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

                    View view = inflater.inflate(R.layout.accounts_item, null);

                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (position >= accounts.length) {
                                activity.startActivityForResult(new Intent(activity, AuthActivity.class), 1);
                                dialog.cancel();
                            } else {
                                AuthService.setAccountActive(activity, accounts[position].name);
                                dialog.cancel();
                                activity.recreate();
                            }
                        }
                    });

                    ImageView avatar = view.findViewById(R.id.avatar);
                    TextView text = view.findViewById(R.id.name);
                    TextView email = view.findViewById(R.id.email);

                    if (position >= accounts.length) {
                        avatar.setVisibility(View.INVISIBLE);
                        email.setVisibility(View.INVISIBLE);
                        text.setText(R.string.new_account);
                        return view;
                    }

                    text.setText(AuthService.getAccountData(activity, accounts[position].name, "name"));
                    email.setText(accounts[position].name);
                    String logo = AuthService.getAccountData(activity, accounts[position].name, "avatar");

                    Glide.with(activity).load(Uri.parse(logo + "@" + avatar.getWidth()))
                            .error(R.drawable.logo)
                            .circleCrop()
                            .into(avatar);

                    return view;
                }
            });
        } else {
            activity.startActivityForResult(new Intent(activity, AuthActivity.class), 1);
        }
    }

}
