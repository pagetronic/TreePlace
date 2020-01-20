package live.page.android.auth;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;

import live.page.android.R;
import live.page.android.api.ApiRequest;
import live.page.android.api.ApiResponse;
import live.page.android.api.Json;

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

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
        @Override
        public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {

            final AccountManager am = AccountManager.get(mContext);

            String access_token = am.peekAuthToken(account, authTokenType);

            if (TextUtils.isEmpty(access_token)) {
                String refresh_token = am.getPassword(account);
                if (refresh_token != null) {
                    ApiRequest req = new ApiRequest(mContext, "/token");
                    ApiResponse rez = req.post(
                            new Json()
                                    .put("grant_type", "refresh_token")
                                    .put("client_id", mContext.getString(R.string.client_id))
                                    .put("client_secret", mContext.getString(R.string.client_secret))
                                    .put("refresh_token", refresh_token)
                    );
                    if (rez.getCode() == 200) {
                        Json data = rez.getResult();
                        if (data != null && !data.isEmpty()) {
                            am.invalidateAuthToken(mContext.getString(R.string.account_type), access_token);
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

                ApiRequest req = new ApiRequest(mContext, "/profile");
                req.setAuthToken(access_token);
                ApiResponse rez = req.get();

                if (rez != null && rez.getCode() == 200) {
                    Json data = rez.getResult();

                    am.setUserData(account, "profile", data.toString(true));

                    final Bundle result = new Bundle();
                    result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                    result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                    result.putString(AccountManager.KEY_AUTHTOKEN, access_token);
                    return result;
                }
            }

            am.removeAccountExplicitly(account);
            if (am.getAccounts().length == 0) {
                final Intent intent = Accounts.getAuthIntent(mContext);
                intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
                final Bundle bundle = new Bundle();
                bundle.putParcelable(AccountManager.KEY_INTENT, intent);
                return bundle;
            }

            return new Bundle();
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
