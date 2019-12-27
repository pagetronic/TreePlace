package live.page.android.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import com.agroneo.treeplace.R;
import live.page.android.api.ApiAsync;
import live.page.android.api.ApiResult;
import live.page.android.api.Json;
import live.page.android.sys.Fx;

import java.util.Locale;

public class Accounts {

    public static final String tokenType = "access";
    public static final String keyAccount = "account_name";

    public static void addAccount(final Context ctx, final String access_token, final String refresh_token, final ApiResult onresult) {


        ApiAsync.get(access_token, ctx, "/profile", new ApiResult() {

            @Override
            public void success(Json data) {
                String email = data.getString("email");
                final Account account = new Account(email, ctx.getResources().getString(R.string.account_type));
                final AccountManager am = AccountManager.get(ctx);
                //password / refresh_token must to be set next
                am.addAccountExplicitly(account, refresh_token, new Bundle());
                am.setPassword(account, refresh_token);
                am.setAuthToken(account, tokenType, access_token);
                AuthService.setAccountActive(ctx, email);

                am.setUserData(account, "id", data.getId());
                am.setUserData(account, "email", data.getString("email"));
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

    public static void getAccessToken(final Context ctx, final Token token) {
        String account_name = getAccountNameActive(ctx);
        AccountManager am = AccountManager.get(ctx);
        for (Account account : am.getAccountsByType(ctx.getResources().getString(R.string.account_type))) {
            if (account.name.equals(account_name)) {
                am.getAuthToken(account, tokenType, new Bundle(), false, new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {

                        try {
                            Bundle result = future.getResult();
                            String access_token = result.getString(AccountManager.KEY_AUTHTOKEN);
                            if (access_token != null) {
                                token.get(access_token);
                            } else {
                                Intent intent = (Intent) result.get(AccountManager.KEY_INTENT);
                                ctx.startActivity(intent, result);
                                token.get(null);
                            }
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


    public static Intent getAuthIntent(Context ctx) {

        String url = getDomain() + "auth?" + "scope=email,gaia" +
                "&response_type=code" +
                "&client_id=" + ctx.getString(R.string.client_id) +
                "&scheme=" + ctx.getString(R.string.scheme_auth);
        return new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    }

    public static String getDomain() {

        String lng = Locale.getDefault().getDisplayLanguage();
        if (lng.startsWith("fr")) {
            return "https://fr.agroneo.com/";
        }
        if (lng.startsWith("pt")) {
            return "https://pt.agroneo.com/";
        }
        if (lng.startsWith("es")) {
            return "https://es.agroneo.com/";
        }
        return "https://en.agroneo.com/";

    }

    public static void intentCode(final Activity activity) {

        Intent intent = activity.getIntent();
        String action = intent.getAction();
        if (action != null) {

            Uri data = intent.getData();
            if (action.equals(Intent.ACTION_VIEW) && data != null && data.getScheme().equals(activity.getString(R.string.scheme_auth))) {

                ApiAsync.post(null, activity, "/token", new Json()
                        .put("grant_type", "authorization_code")
                        .put("client_id", activity.getString(R.string.client_id))
                        .put("client_secret", activity.getString(R.string.client_secret))
                        .put("code", data.getHost()), new ApiResult() {
                    @Override
                    public void success(Json data) {

                        Accounts.addAccount(activity, data.getString("access_token"), data.getString("refresh_token"), new ApiResult() {

                            @Override
                            public void success(Json data) {
                                activity.recreate();
                            }

                            @Override
                            public void error(int code, Json data) {
                                Fx.log(data);
                            }
                        });
                    }

                    @Override
                    public void error(int code, Json data) {
                        Fx.log(data);

                    }
                });
            }
        }
    }

    public interface Token {
        void get(String access_token);
    }
}