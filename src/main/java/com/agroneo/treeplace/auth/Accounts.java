package com.agroneo.treeplace.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import com.agroneo.treeplace.R;
import com.agroneo.treeplace.api.Api;
import com.agroneo.treeplace.api.ApiResponse;
import com.agroneo.treeplace.api.Json;

public class Accounts {

	public static void setAccountActive(Context ctx, String account_name) {
		SharedPreferences settings = ctx.getSharedPreferences("settings", Context.MODE_PRIVATE);
		if (account_name == null) {
			settings.edit().remove("account_name").apply();
		} else {
			settings.edit().putString("account_name", account_name).apply();
		}
	}

	public static String getAccountActive(Context ctx) {
		SharedPreferences settings = ctx.getSharedPreferences("settings", Context.MODE_PRIVATE);
		return settings.getString("account_name", null);
	}

	public static String getAccessToken(Context ctx) {
		String account_name = getAccountActive(ctx);
		AccountManager am = AccountManager.get(ctx);
		for (Account account : am.getAccountsByType(ctx.getResources().getString(R.string.account_type))) {
			if (account.name.equals(account_name)) {
				return am.peekAuthToken(account, "access");
			}
		}
		return null;
	}

	public static void refreshToken(Context ctx, String refresh_token) {

		String account_name = getAccountActive(ctx);
		Resources resources = ctx.getResources();
		ApiResponse token = Api.post(null, "/token",
				new Json("grant_type", "refresh_token")
						.put("client_id", resources.getString(R.string.client_id))
						.put("client_secret", resources.getString(R.string.client_secret))
						.put("refresh_token", refresh_token)
		);
		Json data = token.getResult();
		AccountManager am = AccountManager.get(ctx);
		for (Account account : am.getAccountsByType(ctx.getResources().getString(R.string.account_type))) {
			if (account.name.equals(account_name)) {
				am.setAuthToken(account, "access", data.getString("access_token"));
				am.setAuthToken(account, "refresh", data.getString("refresh_token"));
				return;
			}
		}

	}

	public static void add(Context ctx, String email, String access_token, String refresh_token) {
		AccountManager am = AccountManager.get(ctx);
		Account account = new Account(email, ctx.getResources().getString(R.string.account_type));
		am.addAccountExplicitly(account, null, new Bundle());
		am.setAuthToken(account, "access", access_token);
		am.setAuthToken(account, "refresh", refresh_token);
		setAccountActive(ctx, email);
	}

	public static Account[] getAccounts(Context ctx) {
		return AccountManager.get(ctx).getAccountsByType(ctx.getResources().getString(R.string.account_type));
	}

	public static boolean control(Context ctx) {
		String account_name = getAccountActive(ctx);
		if (account_name == null) {
			return true;
		}
		AccountManager am = AccountManager.get(ctx);
		for (Account account : am.getAccountsByType(ctx.getResources().getString(R.string.account_type))) {
			if (account.name.equals(account_name)) {
				return true;
			}
		}

		setAccountActive(ctx, null);
		return false;
	}
}
