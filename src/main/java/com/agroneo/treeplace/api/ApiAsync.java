package com.agroneo.treeplace.api;

import android.content.Context;
import android.os.AsyncTask;
import com.agroneo.treeplace.auth.Accounts;

public class ApiAsync extends AsyncTask<Object, Integer, ApiResponse> {

	public static void post(Context ctx, String url, Json data, ApiResult func) {
		new ApiAsync(ctx, func).execute("POST", url, data);
	}

	public static void get(Context ctx, String url, ApiResult func) {
		new ApiAsync(ctx, func).execute("GET", url);
	}

	private ApiResult func = null;
	private String access_token = null;

	private ApiAsync(Context ctx, ApiResult func) {
		this.access_token = Accounts.getAccessToken(ctx);
		this.func = func;
	}

	@Override
	protected ApiResponse doInBackground(Object... params) {
		if (params[0].equals("POST")) {
			return Api.post(access_token, (String) params[1], (Json) params[2]);
		} else {
			return Api.get(access_token, (String) params[1]);
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

