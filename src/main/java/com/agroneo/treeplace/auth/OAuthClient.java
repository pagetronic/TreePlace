package com.agroneo.treeplace.auth;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.agroneo.treeplace.R;
import com.agroneo.treeplace.api.ApiAsync;
import com.agroneo.treeplace.api.ApiResult;
import com.agroneo.treeplace.api.Json;

class OAuthClient {

    public static void sign(String type, Activity ctx) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://fr.agroneo.com/oauth?" + type +
                "&scheme=" + ctx.getString(R.string.scheme_auth) +
                "&client_id=" + ctx.getString(R.string.client_id)));
        ctx.startActivity(browserIntent);
    }

    public static void validate(Activity ctx, String code, ApiResult after) {

        ApiAsync.post(null, ctx, "/token", new Json()
                .put("grant_type", "authorization_code")
                .put("client_id", ctx.getString(R.string.client_id))
                .put("client_secret", ctx.getString(R.string.client_secret))
                .put("code", code), after);
    }
}
