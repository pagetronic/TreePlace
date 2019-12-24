package com.agroneo.treeplace.api;

import android.accounts.AccountManager;
import android.content.Context;

import com.agroneo.treeplace.BuildConfig;
import com.agroneo.treeplace.R;

import org.apache.commons.io.IOUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ApiRequest {

    private static final String API_HOST = BuildConfig.DEBUG ? "http://api.agroneo.com" : "https://api.agroneo.com";
    private Context ctx;
    private HttpURLConnection connection;
    private boolean abort = false;
    private String access_token = null;

    public ApiRequest(Context ctx, String path) {
        this.ctx = ctx;
        try {
            connection = (HttpURLConnection) new URL(API_HOST + path).openConnection();
            connection.setRequestProperty("User-Agent", "Agroneo TreePlace Android V3");
            connection.setRequestProperty("Accept", "application/json");

            if (BuildConfig.DEBUG) {
                connection.setReadTimeout(Integer.MAX_VALUE);
                connection.setConnectTimeout(Integer.MAX_VALUE);
            } else {
                connection.setReadTimeout(15000);
                connection.setConnectTimeout(15000);
            }
            connection.setUseCaches(false);
        } catch (IOException ignore) {
        }
    }

    public void setAuthToken(String access_token) {
        if (access_token != null) {
            this.access_token = access_token;
            connection.setRequestProperty("Authorization", "Bearer " + access_token);
        }
    }

    public ApiResponse post(Json data) {

        try {

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            OutputStream os = connection.getOutputStream();

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
            writer.write(data.toString(true));
            writer.flush();
            writer.close();
            String response = IOUtils.toString(connection.getResponseCode() != HttpURLConnection.HTTP_OK ? connection.getErrorStream() : connection.getInputStream());
            connection.disconnect();
            os.close();
            if (!isAbort()) {
                return new ApiResponse(connection.getResponseCode(), response);
            }

        } catch (IOException ignore) {
        }
        return null;
    }

    public ApiResponse get() {

        try {

            connection.setRequestMethod("GET");

            String response = IOUtils.toString(connection.getResponseCode() != 200 ? connection.getErrorStream() : connection.getInputStream());
            connection.disconnect();
            if (!isAbort()) {
                return new ApiResponse(connection.getResponseCode(), response);
            }

        } catch (IOException ignore) {
        }
        return null;
    }


    public void abort() {
        abort = true;
        try {
            connection.disconnect();
        } catch (Exception ignore) {
        }
    }


    public boolean isAbort() {
        return abort;
    }

    public void invalidateAuthToken() {
        if (access_token != null) {
            AccountManager am = AccountManager.get(ctx);
            am.invalidateAuthToken(ctx.getString(R.string.account_type), access_token);
        }
    }
}
