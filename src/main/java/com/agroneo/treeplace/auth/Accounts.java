package com.agroneo.treeplace.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.agroneo.treeplace.R;
import com.agroneo.treeplace.api.ApiAsync;
import com.agroneo.treeplace.api.ApiResult;
import com.agroneo.treeplace.api.Json;

public class Accounts {

    public static final String tokenType = "access";
    public static final String keyAccount = "account_name";

    public static void addAccount(final Context ctx, final String email, final String access_token, final String refresh_token, final ApiResult onresult) {


        final Account account = new Account(email, ctx.getResources().getString(R.string.account_type));
        final AccountManager am = AccountManager.get(ctx);
        //password / refresh_token must to be set next
        am.addAccountExplicitly(account, refresh_token, new Bundle());
        am.setPassword(account, refresh_token);
        am.setAuthToken(account, tokenType, access_token);
        AuthService.setAccountActive(ctx, email);

        ApiAsync.get(ctx, "/profile", new ApiResult() {

            @Override
            public void success(Json data) {
                am.setUserData(account, "avatar", data.getString("logo"));
                am.setUserData(account, "name", data.getString("name"));
                onresult.success(data);
            }

            @Override
            public void error(int code, Json data) {
                onresult.error(code, data);
            }
        });


    }

    public static boolean accountActiveRemoved(Context ctx) {
        String account_name = getAccountNameActive(ctx);
        if (account_name == null) {
            return true;
        }
        AccountManager am = AccountManager.get(ctx);
        for (Account account : am.getAccountsByType(ctx.getResources().getString(R.string.account_type))) {
            if (account.name.equals(account_name)) {
                return true;
            }
        }

        AuthService.setAccountActive(ctx, null);
        return false;
    }

    public static void getAccessToken(Context ctx, final Token token) {
        String account_name = getAccountNameActive(ctx);
        AccountManager am = AccountManager.get(ctx);
        for (Account account : am.getAccountsByType(ctx.getResources().getString(R.string.account_type))) {
            if (account.name.equals(account_name)) {
                am.getAuthToken(account, tokenType, new Bundle(), false, new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {

                        try {
                            Object access_token = future.getResult().get(AccountManager.KEY_AUTHTOKEN);
                            token.get(access_token == null ? null : access_token.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                            token.get(null);
                        }
                    }
                }, null);
                return;
            }
        }
        token.get(null);
    }

    public static String getAccountData(Context ctx, String account_name, String key) {
        AccountManager am = AccountManager.get(ctx);
        for (Account account : am.getAccountsByType(ctx.getResources().getString(R.string.account_type))) {
            if (account.name.equals(account_name)) {
                return am.getUserData(account, key);
            }
        }
        return null;
    }

    public static String getAccountNameActive(Context ctx) {
        SharedPreferences settings = ctx.getSharedPreferences("settings", Context.MODE_PRIVATE);
        return settings.getString(keyAccount, null);
    }

    public static void invalidateAccount(Context ctx) {
        String account_name = getAccountNameActive(ctx);
        if (account_name == null) {
            return;
        }
        AccountManager am = AccountManager.get(ctx);
        for (Account account : am.getAccountsByType(ctx.getResources().getString(R.string.account_type))) {
            if (account.name.equals(account_name)) {
                am.clearPassword(account);
                am.invalidateAuthToken(ctx.getResources().getString(R.string.account_type), am.peekAuthToken(account, tokenType));
            }
        }
    }

    public static void invalidateAuthToken(Context ctx, String access_token) {
        AccountManager am = AccountManager.get(ctx);
        am.invalidateAuthToken(ctx.getResources().getString(R.string.account_type), access_token);
    }

    public interface Token {
        void get(String access_token);
    }
}
