package com.agroneo.treeplace.auth;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
        String accountName = getIntent().getStringExtra("accountName");
        if (accountName != null) {
            ((TextView) findViewById(R.id.login_email)).setText(accountName);
            findViewById(R.id.login_password).requestFocus();
        }

        oauth();
        login();
        register();
        recover();


    }

    private void oauth() {
        Button facebook = findViewById(R.id.facebook);
        Button google = findViewById(R.id.google);
    }


    private void login() {
        findViewById(R.id.login)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loading(true);
                        final String email = ((TextView) findViewById(R.id.login_email)).getText().toString().trim();
                        final String password = ((TextView) findViewById(R.id.login_password)).getText().toString().trim();
                        final Resources resources = getResources();

                        ApiAsync.post(getBaseContext(), "/token",
                                new Json("grant_type", "password")
                                        .put("client_id", resources.getString(R.string.client_id))
                                        .put("client_secret", resources.getString(R.string.client_secret))
                                        .put("email", email)
                                        .put("password", password),

                                new ApiResult() {
                                    @Override
                                    public void success(Json data) {
                                        if (!data.getString("access_token", "").equals("") && !data.getString("refresh_token", "").equals("")) {

                                            AuthService.addAccount(getBaseContext(), email, data.getString("access_token"), data.getString("refresh_token"), new ApiResult() {

                                                @Override
                                                public void success(Json data) {
                                                    Intent resultIntent = new Intent();
                                                    setResult(Activity.RESULT_OK, resultIntent);
                                                    finish();
                                                }

                                                @Override
                                                public void error(int code, Json data) {
                                                    loading(false);
                                                }
                                            });

                                        } else {
                                            loading(false);
                                        }
                                    }

                                    @Override
                                    public void error(int code, Json data) {
                                        if (data != null) {
                                            Log.e("AGRO", data.toString());
                                        }

                                        loading(false);
                                    }
                                });
                    }
                });
    }


    private void register() {
        Button register = findViewById(R.id.register);
        TextView register_name = findViewById(R.id.register_name);
        TextView register_email = findViewById(R.id.register_email);
        TextView register_password = findViewById(R.id.register_password);
    }


    private void recover() {
        Button recover = findViewById(R.id.recover);
        TextView recover_email = findViewById(R.id.recover_email);
    }

    private void loading(boolean active) {
        if (active) {
            findViewById(R.id.progress).setVisibility(View.VISIBLE);
            findViewById(R.id.form).setVisibility(View.GONE);
        } else {

            findViewById(R.id.progress).setVisibility(View.GONE);
            findViewById(R.id.form).setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }


}
