package com.agroneo.treeplace.api;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;

import com.agroneo.treeplace.R;
import com.agroneo.treeplace.auth.AuthService;

public class ApiAsync extends AsyncTask<Object, Integer, ApiResponse> {

    private ApiResult func = null;
    private String access_token = null;

    private ApiAsync(ApiResult func, String access_token) {
        this.access_token = access_token;
        this.func = func;
    }

    public static void post(Context ctx, final String url, final Json data, final ApiResult func) {
        getAccessToken(ctx, new Token() {
            @Override
            public void get(String access_token) {
                new ApiAsync(func, access_token).execute("POST", url, data);
            }
        });
    }

    public static void get(Context ctx, final String url, final ApiResult func) {
        getAccessToken(ctx, new Token() {
            @Override
            public void get(String access_token) {
                new ApiAsync(func, access_token).execute("GET", url);
            }
        });
    }

    private static void invalidateAccessToken(final Context ctx) {
        getAccessToken(ctx, new Token() {
                    @Override
                    public void get(String access_token) {
                        AccountManager am = AccountManager.get(ctx);
                        am.invalidateAuthToken(ctx.getResources().getString(R.string.account_type), access_token);
                    }
                }
        );
    }

    private static void getAccessToken(Context ctx, final Token token) {
        String account_name = AuthService.getAccountActive(ctx);
        AccountManager am = AccountManager.get(ctx);
        for (Account account : am.getAccountsByType(ctx.getResources().getString(R.string.account_type))) {
            if (account.name.equals(account_name)) {
                am.getAuthToken(account, "access", new Bundle(), true, new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {

                        try {
                            Bundle authTokenBundle = future.getResult();
                            token.get(authTokenBundle.get(AccountManager.KEY_AUTHTOKEN).toString());
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

    @Override
    protected ApiResponse doInBackground(Object... params) {
        if (params[0].equals("POST")) {
            return Api.post(access_token, (String) params[1], (Json) params[2]);
        } else {
            return Api.get(access_token, (String) params[1]);
        }
    }

    @Override
    protected void onPostExecute(ApiResponse rez) {

        if (func == null) {
            return;
        }
        if (rez == null) {
            func.error(-1, new Json());
            return;
        }
        if (rez.getCode() != 200) {
            func.error(rez.getCode(), rez.getResult());
            return;
        }
        func.success(rez.getResult());
    }

    private interface Token {
        void get(String access_token);
    }
}

