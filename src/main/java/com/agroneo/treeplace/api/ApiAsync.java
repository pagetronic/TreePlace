package com.agroneo.treeplace.api;

import android.content.Context;
import android.os.AsyncTask;

import com.agroneo.treeplace.auth.Accounts;

public class ApiAsync extends AsyncTask<Object, Integer, ApiResponse> {


    private ApiResult func;
    private String access_token;

    private ApiAsync(ApiResult func, String access_token) {
        this.access_token = access_token;
        this.func = func;
    }

    public static void post(final Context ctx, final String url, final Json data, final ApiResult func) {
        Accounts.getAccessToken(ctx, new Accounts.Token() {
            @Override
            public void get(final String access_token) {
                new ApiAsync(new ApiResult() {

                    @Override
                    public void success(Json data) {
                        func.success(data);
                    }

                    @Override
                    public void error(int code, Json data) {
                        if (code == 401 && "EXPIRED_ACCESS_TOKEN".equals(data.getString("error"))) {
                            Accounts.invalidateAuthToken(ctx, access_token);
                            post(ctx, url, data, func);
                            return;
                        }
                        func.error(code, data);
                    }
                }, access_token).execute("POST", url, data);
            }
        });
    }

    public static void get(final Context ctx, final String url, final ApiResult func) {

        Accounts.getAccessToken(ctx, new Accounts.Token() {
            @Override
            public void get(final String access_token) {

                new ApiAsync(new ApiResult() {

                    @Override
                    public void success(Json data) {
                        func.success(data);
                    }

                    @Override
                    public void error(int code, Json data) {
                        switch (data.getString("error")) {
                            case "EXPIRED_ACCESS_TOKEN":
                                Accounts.invalidateAuthToken(ctx, access_token);
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
                }, access_token).execute("GET", url);
            }
        });
    }

    @Override
    protected ApiResponse doInBackground(Object... params) {
        if (params[0].equals("POST")) {
            return ApiSync.post(access_token, (String) params[1], (Json) params[2]);
        } else {
            return ApiSync.get(access_token, (String) params[1]);
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

}

