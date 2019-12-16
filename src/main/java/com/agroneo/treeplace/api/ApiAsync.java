package com.agroneo.treeplace.api;

import android.os.AsyncTask;

public class ApiAsync extends AsyncTask<Object, Integer, ApiResponse> {

	public static void post(String url, Json data, ApiResult func) {
		new ApiAsync(func).post(url, data);
	}

	public static void get(String url, ApiResult func, String... params) {
		new ApiAsync(func).get(url, params);
	}

	private ApiResult func = null;

	private ApiAsync(ApiResult func) {
		this.func = func;
	}

	private void get(String url, String... params) {
		execute("GET", url, params);
	}

	private void post(String url, Json data) {
		execute("POST", url, data);
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
		if (rez == null) {
			func.error(-1, null);
		} else if (rez.getCode() != 200) {
			func.error(rez.getCode(), rez.getResult());
		} else {
			func.success(rez.getResult());
		}
	}


}
