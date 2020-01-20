package live.page.android.api;

import android.content.Context;
import android.os.AsyncTask;

import live.page.android.auth.Accounts;

public class ApiAsync extends AsyncTask<Object, Integer, ApiResponse> {


    private ApiResult func;
    private ApiRequest req;

    private ApiAsync(ApiRequest req, ApiResult func) {
        this.func = func;
        this.req = req;
    }

    public static ApiRequest post(final Context ctx, final String url, final Json data, final ApiResult func) {
        final ApiRequest req = new ApiRequest(ctx, url);
        Accounts.getAccessToken(ctx, new Accounts.Token() {
            @Override
            public void get(final String access_token) {
                if (!req.isAbort()) {
                    req.setAuthToken(access_token);
                    post(req, data, func);
                }
            }
        });
        return req;
    }

    public static ApiRequest post(final String access_token, final Context ctx, final String url, final Json data, final ApiResult func) {
        ApiRequest req = new ApiRequest(ctx, url);
        req.setAuthToken(access_token);
        post(req, data, func);
        return req;
    }

    private static void post(final ApiRequest req, final Json data, final ApiResult func) {

        new ApiAsync(req, new ApiResult() {

            @Override
            public void success(Json data) {
                if (req.isAbort()) {
                    return;
                }
                func.success(data);

            }

            @Override
            public void error(int code, Json data) {
                if (req.isAbort()) {
                    return;
                }
                if (code == 401 && "EXPIRED_ACCESS_TOKEN".equals(data.getString("error"))) {
                    req.invalidateAuthToken();
                    post(req, data, func);
                    return;
                }
                func.error(code, data);
            }
        }).execute("POST", data);

    }

    public static ApiRequest get(final Context ctx, final String url, final ApiResult func) {

        final ApiRequest req = new ApiRequest(ctx, url);
        Accounts.getAccessToken(ctx, new Accounts.Token() {
            @Override
            public void get(final String access_token) {

                req.setAuthToken(access_token);
                if (!req.isAbort()) {
                    ApiAsync.get(req, ctx, url, func);
                }
            }
        });
        return req;
    }

    public static ApiRequest get(final String access_token, final Context ctx, final String url, final ApiResult func) {

        final ApiRequest req = new ApiRequest(ctx, url);
        req.setAuthToken(access_token);
        if (!req.isAbort()) {
            ApiAsync.get(req, ctx, url, func);
        }
        return req;
    }

    private static void get(final ApiRequest req, final Context ctx, final String url, final ApiResult func) {

        new ApiAsync(req, new ApiResult() {

            @Override
            public void success(Json data) {
                if (req.isAbort()) {
                    return;
                }
                func.success(data);
            }

            @Override
            public void error(int code, Json data) {
                if (req.isAbort()) {
                    return;
                }
                if (data == null) {
                    func.error(code, data);
                    return;
                }
                switch (data.getString("error", "")) {
                    case "EXPIRED_ACCESS_TOKEN":
                        req.invalidateAuthToken();
                        post(ctx, url, data, func);
                        break;
                    case "AUTHORIZATION_SCOPE_ERROR":
                    case "INVALID_ACCESS_TOKEN":
                        Accounts.invalidateAccount(ctx);
                        break;
                    default:
                        func.error(code, data);
                        break;
                }
            }
        }).execute("GET", url);
    }

    @Override
    protected ApiResponse doInBackground(Object... params) {
        if (params[0].equals("POST")) {
            return req.post((Json) params[1]);
        } else {
            return req.get();
        }
    }

    @Override
    protected void onPostExecute(ApiResponse rez) {
        if (req.isAbort()) {
            return;
        }

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

}

