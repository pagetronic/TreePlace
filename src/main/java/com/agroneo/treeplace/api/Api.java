package com.agroneo.treeplace.api;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Api {

	private static final String API_HOST = "https://api.agroneo.com";

	public static ApiResponse post(String path, Json data) {

		try {
			HttpURLConnection conn = connect(path);
			conn.setRequestMethod("POST");
			OutputStream os = conn.getOutputStream();

			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
			writer.write(data.toString(true));
			writer.flush();
			writer.close();
			os.close();

			String response = IOUtils.toString(conn.getResponseCode() != HttpURLConnection.HTTP_OK ? conn.getErrorStream() : conn.getInputStream());
			conn.disconnect();
			return new ApiResponse(conn.getResponseCode(), response);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ApiResponse get(String path, String... params) {

		try {
			if (params.length > 0) {
				path += "?" + StringUtils.join(path, "&");
			}
			HttpURLConnection conn = connect(path);
			conn.setRequestMethod("GET");

			String response = IOUtils.toString(conn.getResponseCode() != HttpURLConnection.HTTP_OK ? conn.getErrorStream() : conn.getInputStream());
			conn.disconnect();
			return new ApiResponse(conn.getResponseCode(), response);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static HttpURLConnection connect(String path) {
		try {
			URL url = new URL(API_HOST + path);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("User-Agent", "Agroneo TreePlace Android V3");
			conn.setReadTimeout(15000);
			conn.setConnectTimeout(15000);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			return conn;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
