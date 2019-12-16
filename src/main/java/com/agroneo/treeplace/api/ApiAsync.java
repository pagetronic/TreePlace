package com.agroneo.treeplace.api;

import android.os.AsyncTask;

public class ApiAsync extends AsyncTask<Object, Integer, ApiResponse> {

	public static void post(String url, Json data) {
		new ApiAsync(null).execute("POST", url, data);
	}

	public static void post(String url, Json data, ApiResult func) {
		new ApiAsync(func).execute("POST", url, data);
	}

	public static void get(String url, ApiResult func, String... params) {
		new ApiAsync(func).execute("GET", url, params);
	}

	private ApiResult func = null;

	private ApiAsync(ApiResult func) {
		this.func = func;
	}

	@Override
	protected ApiResponse doInBackground(Object... params) {
		if (params[0].equals("POST")) {
			return Api.post((String) params[1], (Json) params[2]);
		} else {
			return Api.get((String) params[1], (String) params[2]);
		}
	}

	@Override
	protected void onPostExecute(ApiResponse rez) {

		if (func == null) {
			return;
		}
		if (rez == null) {
			func.error(-1, null);
			return;
		}
		if (rez.getCode() != 200) {
			func.error(rez.getCode(), rez.getResult());
			return;
		}
		func.success(rez.getResult());
	}
}

