package live.page.android.api;

import android.accounts.AccountManager;
import android.content.Context;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import live.page.android.BuildConfig;
import live.page.android.R;
import live.page.android.sys.Fx;

public class ApiRequest {

    private static final String HTTP = BuildConfig.DEBUG ? "http://" : "https://";
    private Context ctx;
    private HttpURLConnection connection;
    private boolean aborted = false;
    private String access_token = null;

    public ApiRequest(Context ctx, String path) {
        this.ctx = ctx;
        try {
            connection = (HttpURLConnection) new URL(HTTP + ctx.getString(R.string.apiHost) + path).openConnection();
            connection.setRequestProperty("User-Agent", "TreePlace Android V3");
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
            connection.setDoOutput(true);
            OutputStream body = connection.getOutputStream();

            BufferedWriter payload = new BufferedWriter(new OutputStreamWriter(body, StandardCharsets.UTF_8));
            payload.write(data.toString(true));
            payload.flush();
            payload.close();

            String response = null;
            if (!isAbort()) {
                response = read();
            }

            body.close();

            if (!isAbort()) {
                int code = connection.getResponseCode();
                if (code != 200) {
                    Fx.log(code);
                }
                return new ApiResponse(code, response);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.disconnect();
                connection = null;
            } catch (Exception ignore) {
            }
        }
        return null;
    }


    public ApiResponse get() {

        try {

            connection.setRequestMethod("GET");
            connection.setDoOutput(false);

            String response = null;
            if (!isAbort()) {
                response = read();
            }

            if (!isAbort()) {
                int code = connection.getResponseCode();
                if (code != 200) {
                    Fx.log(code);
                }
                return new ApiResponse(code, response);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.disconnect();
                connection = null;
            } catch (Exception ignore) {
            }
        }
        return null;
    }


    public void abort() {
        aborted = true;
        try {
            connection.disconnect();
        } catch (Exception ignore) {
        }
    }


    private String read() {

        InputStream input = null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            input = connection.getResponseCode() != HttpURLConnection.HTTP_OK ? connection.getErrorStream() : connection.getInputStream();
            final int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            while (true) {
                int count = input.read(buffer);
                if (count == -1 || isAbort()) {
                    break;
                }
                outputStream.write(buffer, 0, count);
            }
            if (!isAbort()) {
                return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
            }

        } catch (IOException ignore) {

        } finally {

            if (input != null) {
                try {
                    input.close();
                } catch (Exception ignore) {

                }
            }

            try {
                outputStream.close();
            } catch (Exception ignore) {

            }

            try {
                connection.disconnect();
            } catch (Exception ignore) {

            }

        }
        return null;
    }

    public boolean isAbort() {
        return aborted;
    }

    public void invalidateAuthToken() {
        if (access_token != null) {
            AccountManager am = AccountManager.get(ctx);
            am.invalidateAuthToken(ctx.getString(R.string.account_type), access_token);
        }
    }
}
