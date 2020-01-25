package live.page.android.api;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;

import live.page.android.auth.Accounts;
import live.page.android.sys.Fx;

public class ApiAsync extends AsyncTask<Object, Integer, ApiResponse> {


    private ApiResult func;
    private ApiRequest req;

    private ApiAsync(ApiRequest req, ApiResult func) {
        this.func = func;
        this.req = req;
    }

    public static ApiRequest post(final Context ctx, final String url, final Json data, final ApiResult func) {
        return post(ctx, url, data, func, false);
    }

    public static ApiRequest post(final Context ctx, final String url, final Json data, final ApiResult func, boolean loading) {
        final ApiRequest req = new ApiRequest(ctx, url);
        final AlertDialog waiter = (loading) ? Fx.loading(ctx, new Fx.Action() {
            @Override
            public void doIt() {
                req.abort();
            }
        }) : null;

        Accounts.getAccessToken(ctx, access_token -> {
            if (!req.isAbort()) {
                req.setAuthToken(access_token);
                post(req, ctx, url, data, new ApiResult() {

                    @Override
                    public void success(Json data1) {
                        if (waiter != null) {
                            waiter.hide();
                        }
                        func.success(data1);
                    }

                    @Override
                    public void error(int code, Json data1) {
                        if (waiter != null) {
                            waiter.hide();
                        }
                        func.error(code, data1);
                    }
                });
            }
        });
        return req;
    }

    public static ApiRequest post(final String access_token, final Context ctx, final String url, final Json data, final ApiResult func) {
        ApiRequest req = new ApiRequest(ctx, url);
        req.setAuthToken(access_token);
        post(req, ctx, url, data, func);
        return req;
    }

    private static void post(final ApiRequest req, final Context ctx, final String url, final Json data, final ApiResult func) {
        send("POST", req, ctx, url, data, func);
    }

    public static ApiRequest get(final Context ctx, final String url, final ApiResult func) {
        return get(ctx, url, func, false);
    }

    public static ApiRequest get(final Context ctx, final String url, final ApiResult func, boolean loading) {

        final ApiRequest req = new ApiRequest(ctx, url);
        final AlertDialog waiter = (loading) ? Fx.loading(ctx, new Fx.Action() {
            @Override
            public void doIt() {
                req.abort();
            }
        }) : null;


        Accounts.getAccessToken(ctx, access_token -> {

            req.setAuthToken(access_token);
            if (!req.isAbort()) {
                get(req, ctx, url, new ApiResult() {

                    @Override
                    public void success(Json data) {
                        if (waiter != null) {
                            waiter.hide();
                        }
                        func.success(data);
                    }

                    @Override
                    public void error(int code, Json data) {
                        if (waiter != null) {
                            waiter.hide();
                        }
                        func.error(code, data);
                    }
                });
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
        send("GET", req, ctx, url, null, func);
    }

    private static void send(final String method, final ApiRequest req, final Context ctx, final String url, final Json data, final ApiResult func) {

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
                switch (data.getString("error", "")) {
                    case "EXPIRED_ACCESS_TOKEN":
                        req.invalidateAuthToken();
                        send(method, req, ctx, url, data, func);
                        break;
                    case "AUTHORIZATION_SCOPE_ERROR":
                    case "INVALID_ACCESS_TOKEN":
                        Accounts.invalidateAccount(ctx);
                        break;
                    default:
                        func.error(code, data);
                        break;
                }
                func.error(code, data);
            }
        }).execute(req, method, data);

    }

    @Override
    protected ApiResponse doInBackground(Object... params) {
        if (params[1].equals("POST")) {
            return ((ApiRequest) params[0]).post((Json) params[2]);
        } else {
            return ((ApiRequest) params[0]).get();
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

