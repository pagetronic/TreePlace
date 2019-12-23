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
import com.agroneo.treeplace.api.ApiResponse;
import com.agroneo.treeplace.api.ApiSync;
import com.agroneo.treeplace.api.Json;

import static android.accounts.AccountManager.KEY_BOOLEAN_RESULT;

public class AuthService extends Service {

    public static void setAccountActive(Context ctx, String account_name) {
        SharedPreferences settings = ctx.getSharedPreferences("settings", MODE_PRIVATE);
        if (account_name == null) {
            settings.edit().remove(Accounts.keyAccount).apply();
        } else {
            settings.edit().putString(Accounts.keyAccount, account_name).apply();
        }
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
            Intent intent = Accounts.getAuthIntent(mContext);
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
            Bundle bundle = new Bundle();
            bundle.putParcelable(AccountManager.KEY_INTENT, intent);
            return bundle;
        }

        @Override
        public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {

            final AccountManager am = AccountManager.get(mContext);

            String access_token = am.peekAuthToken(account, authTokenType);

            if (TextUtils.isEmpty(access_token)) {
                String refresh_token = am.getPassword(account);
                if (refresh_token != null) {
                    Resources resources = mContext.getResources();
                    ApiResponse rez = ApiSync.post(null, "/token",
                            new Json()
                                    .put("grant_type", "refresh_token")
                                    .put("client_id", resources.getString(R.string.client_id))
                                    .put("client_secret", resources.getString(R.string.client_secret))
                                    .put("refresh_token", refresh_token)
                    );
                    if (rez.getCode() == 200) {
                        Json data = rez.getResult();
                        if (data != null && !data.isEmpty()) {
                            am.invalidateAuthToken(resources.getString(R.string.account_type), access_token);
                            access_token = data.getString("access_token", "");
                            refresh_token = data.getString("refresh_token", "");
                            if (!refresh_token.equals("") && !access_token.equals("")) {
                                am.setAuthToken(account, Accounts.tokenType, access_token);
                                am.setPassword(account, refresh_token);
                            }
                        }
                    }
                }
            }
            if (!TextUtils.isEmpty(access_token)) {

                ApiResponse rez = ApiSync.get(access_token, "/profile");

                if (rez != null && rez.getCode() == 200) {
                    Json data = rez.getResult();

                    am.setUserData(account, "id", data.getId());
                    am.setUserData(account, "email", data.getString("email"));
                    am.setUserData(account, "avatar", data.getString("logo"));
                    am.setUserData(account, "name", data.getString("name"));

                    final Bundle result = new Bundle();
                    result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                    result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                    result.putString(AccountManager.KEY_AUTHTOKEN, access_token);
                    return result;
                }
            }

            final Intent intent = Accounts.getAuthIntent(mContext, "user=" + am.getUserData(account, "id"));
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
            final Bundle bundle = new Bundle();
            bundle.putParcelable(AccountManager.KEY_INTENT, intent);
            return bundle;
        }


        @Override
        public String getAuthTokenLabel(String authTokenType) {
            if (authTokenType.equals(Accounts.tokenType)) {
                return "access_token";
            }
            return "";
        }

        @Override
        public Bundle hasFeatures(AccountAuthenticatorResponse response, Account
                account, String[] features) throws NetworkErrorException {
            final Bundle result = new Bundle();
            result.putBoolean(KEY_BOOLEAN_RESULT, false);
            return result;
        }

        @Override
        public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account
                account, Bundle options) throws NetworkErrorException {
            return null;
        }

        @Override
        public Bundle updateCredentials(AccountAuthenticatorResponse response, Account
                account, String authTokenType, Bundle options) throws NetworkErrorException {
            return null;
        }
    }
}
