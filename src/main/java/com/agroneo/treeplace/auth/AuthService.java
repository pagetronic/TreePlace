package com.agroneo.treeplace.auth;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;

import com.agroneo.treeplace.R;
import com.agroneo.treeplace.api.Api;
import com.agroneo.treeplace.api.ApiResponse;
import com.agroneo.treeplace.api.Json;

import static android.accounts.AccountManager.KEY_BOOLEAN_RESULT;

public class AuthService extends Service {

    private static long expire = 0;

    public static void setAccountActive(Context ctx, String account_name) {
        SharedPreferences settings = ctx.getSharedPreferences("settings", MODE_PRIVATE);
        if (account_name == null) {
            settings.edit().remove("account_name").apply();
        } else {
            settings.edit().putString("account_name", account_name).apply();
        }
    }

    public static String getAccountActive(Context ctx) {
        SharedPreferences settings = ctx.getSharedPreferences("settings", MODE_PRIVATE);
        return settings.getString("account_name", null);
    }

    public static void add(Context ctx, String email, String access_token, String refresh_token) {
        AccountManager am = AccountManager.get(ctx);
        Account account = new Account(email, ctx.getResources().getString(R.string.account_type));
        am.addAccountExplicitly(account, refresh_token, new Bundle());
        am.setAuthToken(account, "access", access_token);
        setAccountActive(ctx, email);
    }

    public static Account[] getAccounts(Context ctx) {
        return AccountManager.get(ctx).getAccountsByType(ctx.getResources().getString(R.string.account_type));
    }

    public static boolean control(Context ctx) {
        String account_name = getAccountActive(ctx);
        if (account_name == null) {
            return true;
        }
        AccountManager am = AccountManager.get(ctx);
        for (Account account : am.getAccountsByType(ctx.getResources().getString(R.string.account_type))) {
            if (account.name.equals(account_name)) {
                return true;
            }
        }

        setAccountActive(ctx, null);
        return false;
    }

    @Override
    public IBinder onBind(Intent intent) {

        AgroneoAuthenticator authenticator = new AgroneoAuthenticator(this);
        return authenticator.getIBinder();
    }

    private static class AgroneoAuthenticator extends AbstractAccountAuthenticator {

        private final Context mContext;

        public AgroneoAuthenticator(Context context) {
            super(context);
            this.mContext = context;
        }

        @Override
        public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
            return null;
        }

        @Override
        public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
            Intent intent = new Intent(mContext, AuthActivity.class);
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
            Bundle bundle = new Bundle();
            bundle.putParcelable(AccountManager.KEY_INTENT, intent);
            return bundle;
        }

        @Override
        public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {

            final AccountManager am = AccountManager.get(mContext);


            String access_token = null;

            if (expire > System.currentTimeMillis() - 3000 * 1000) {
                try {
                    access_token = am.blockingGetAuthToken(account, authTokenType, false);
                } catch (Exception e) {
                }
            } else {
                Resources resources = mContext.getResources();
                ApiResponse rez = Api.post(null, "/token",
                        new Json()
                                .put("grant_type", "refresh_token")
                                .put("client_id", resources.getString(R.string.client_id))
                                .put("client_secret", resources.getString(R.string.client_secret))
                                .put("refresh_token", am.getPassword(account))
                );

                if (rez.getCode() == 200) {
                    Json data = rez.getResult();
                    if (data != null && !data.isEmpty()) {
                        am.invalidateAuthToken(resources.getString(R.string.account_type), access_token);
                        access_token = data.getString("access_token");
                        am.setAuthToken(account, "access", access_token);
                        am.setPassword(account, data.getString("refresh_token"));
                        expire = System.currentTimeMillis();

                    }
                }
            }
            if (!TextUtils.isEmpty(access_token)) {
                final Bundle result = new Bundle();
                result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                result.putString(AccountManager.KEY_AUTHTOKEN, access_token);
                return result;
            }

            final Intent intent = new Intent(mContext, AuthActivity.class);
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
            final Bundle bundle = new Bundle();
            bundle.putParcelable(AccountManager.KEY_INTENT, intent);

            return bundle;
        }


        @Override
        public String getAuthTokenLabel(String authTokenType) {
            if (authTokenType.equals("access")) {
                return "access_token";
            }
            return "";
        }

        @Override
        public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
            final Bundle result = new Bundle();
            result.putBoolean(KEY_BOOLEAN_RESULT, false);
            return result;
        }

        @Override
        public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
            return null;
        }

        @Override
        public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
            return null;
        }
    }

}
