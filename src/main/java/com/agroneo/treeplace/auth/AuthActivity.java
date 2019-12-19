package com.agroneo.treeplace.auth;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.agroneo.treeplace.R;
import com.agroneo.treeplace.api.ApiAsync;
import com.agroneo.treeplace.api.ApiResult;
import com.agroneo.treeplace.api.Json;
import com.agroneo.treeplace.sys.Animators;

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

        comodo();
        oauth();
        login();
        register();
        recover();

    }

    private void comodo() {
        final View form_register = findViewById(R.id.form_register);
        final View form_login = findViewById(R.id.form_login);
        final View form_recover = findViewById(R.id.form_recover);
        final View form_gafa = findViewById(R.id.form_gafa);


        final Button tab_login = findViewById(R.id.tab_login);
        final Button tab_register = findViewById(R.id.tab_register);
        final Button tab_recover = findViewById(R.id.tab_recover);

        tab_login.setBackgroundResource(R.drawable.tab_active);
        tab_register.setBackgroundResource(android.R.color.transparent);
        tab_recover.setBackgroundResource(android.R.color.transparent);

        tab_login.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (form_login.getVisibility() == View.VISIBLE) {
                    return;
                }
                tab_login.setBackgroundResource(R.drawable.tab_active);
                tab_register.setBackgroundResource(android.R.color.transparent);
                tab_recover.setBackgroundResource(android.R.color.transparent);
                Animators.gone(new Runnable() {
                    @Override
                    public void run() {
                        Animators.visible(form_login, form_gafa);
                    }
                }, form_gafa, form_register, form_recover);

            }
        });
        tab_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (form_register.getVisibility() == View.VISIBLE) {
                    return;
                }
                tab_login.setBackgroundResource(android.R.color.transparent);
                tab_register.setBackgroundResource(R.drawable.tab_active);
                tab_recover.setBackgroundResource(android.R.color.transparent);
                Animators.gone(new Runnable() {
                    @Override
                    public void run() {
                        Animators.visible(form_register, form_gafa);
                    }
                }, form_gafa, form_recover, form_login);

            }
        });
        tab_recover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (form_recover.getVisibility() == View.VISIBLE) {
                    return;
                }
                tab_login.setBackgroundResource(android.R.color.transparent);
                tab_register.setBackgroundResource(android.R.color.transparent);
                tab_recover.setBackgroundResource(R.drawable.tab_active);
                Animators.gone(new Runnable() {
                    @Override
                    public void run() {
                        Animators.visible(form_recover);
                    }
                }, form_register, form_login, form_gafa);

            }
        });


    }

    private void oauth() {
        Button facebook = findViewById(R.id.facebook);
        Button google = findViewById(R.id.google);
        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoogleOauth.sign(AuthActivity.this);
            }
        });

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

                                            Accounts.addAccount(getBaseContext(), email, data.getString("access_token"), data.getString("refresh_token"), new ApiResult() {

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 100) {
            GoogleOauth.result(data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
