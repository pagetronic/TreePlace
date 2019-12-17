package com.agroneo.treeplace.api;

import com.agroneo.treeplace.BuildConfig;

import org.apache.commons.io.IOUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Api {

    private static final String API_HOST = BuildConfig.DEBUG ? "http://api.agroneo.com" : "https://api.agroneo.com";

    public static ApiResponse post(String access_token, String path, Json data) {

        try {
            HttpURLConnection conn = connect(access_token, path);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            OutputStream os = conn.getOutputStream();

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
            writer.write(data.toString(true));
            writer.flush();
            writer.close();
            String response = IOUtils.toString(conn.getResponseCode() != HttpURLConnection.HTTP_OK ? conn.getErrorStream() : conn.getInputStream());
            conn.disconnect();
            os.close();
            return new ApiResponse(conn.getResponseCode(), response);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ApiResponse get(String access_token, String path) {

        try {
            HttpURLConnection conn = connect(access_token, path);
            conn.setRequestMethod("GET");

            String response = IOUtils.toString(conn.getResponseCode() != HttpURLConnection.HTTP_OK ? conn.getErrorStream() : conn.getInputStream());
            conn.disconnect();
            return new ApiResponse(conn.getResponseCode(), response);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static HttpURLConnection connect(String access_token, String path) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(API_HOST + path).openConnection();
            conn.setRequestProperty("User-Agent", "Agroneo TreePlace Android V3");
            conn.setRequestProperty("Accept", "application/json");
            if (access_token != null) {
                conn.setRequestProperty("Authorization", "Bearer " + access_token);
            }
            if (BuildConfig.DEBUG) {
                conn.setReadTimeout(Integer.MAX_VALUE);
                conn.setConnectTimeout(Integer.MAX_VALUE);
            } else {
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
            }
            conn.setUseCaches(false);
            return conn;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
