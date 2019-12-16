package com.agroneo.treeplace.auth;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.agroneo.treeplace.R;
import com.agroneo.treeplace.api.ApiAsync;
import com.agroneo.treeplace.api.ApiResult;
import com.agroneo.treeplace.api.Json;

public class AuthActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_auth);

		findViewById(R.id.login)
				.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				final String email = ((TextView) findViewById(R.id.login_email)).getText().toString().trim();
				final String password = ((TextView) findViewById(R.id.login_password)).getText().toString().trim();
				final Resources resources = getResources();

				ApiAsync.post(getApplicationContext(), "/token",
						new Json("grant_type", "password")
								.put("client_id", resources.getString(R.string.client_id))
								.put("client_secret", resources.getString(R.string.client_secret))
								.put("email", email)
								.put("password", password),

						new ApiResult() {
							@Override
							public void success(Json data) {
								Accounts.add(getApplicationContext(), email, data.getString("access_token"), data.getString("refresh_token"));
								Intent resultIntent = new Intent();
								setResult(Activity.RESULT_OK, resultIntent);
								finish();
							}

							@Override
							public void error(int code, Json data) {
								if (data != null) {
									Log.e("AGRO", data.toString());
								}
							}
						});
			}
		});

	}


	@Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED);
		super.onBackPressed();
	}


}
