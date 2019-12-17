package com.agroneo.treeplace;

import android.accounts.Account;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.agroneo.treeplace.api.ApiAsync;
import com.agroneo.treeplace.api.ApiResult;
import com.agroneo.treeplace.api.Json;
import com.agroneo.treeplace.auth.Accounts;
import com.agroneo.treeplace.auth.AuthActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, R.string.add_specimen, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_search, R.id.nav_creation)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        ImageView avatar = findViewById(R.id.avatar);

        makeAccounts(avatar);


    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (!Accounts.control(getApplicationContext())) {
            recreate();
        }
    }

    private void makeAccounts(final ImageView avatar) {
        final String account_name = Accounts.getAccountActive(getApplicationContext());

        if (account_name != null) {
            ApiAsync.get(getApplicationContext(), "/profile",
                    new ApiResult() {
                        @Override
                        public void success(Json data) {
                            Glide.with(MainActivity.this).load(data.getString("logo") + "@" + avatar.getWidth())
                                    .circleCrop()
                                    .into(avatar);
                        }

                        @Override
                        public void error(int code, Json data) {
                            Log.e("Agro", data.toString());
                        }
                    }
            );
        }

        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Account[] accounts = Accounts.getAccounts(getApplicationContext());
                if (accounts.length > 0) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    final List<String> options = new ArrayList<>();
                    for (Account account : accounts) {
                        options.add(account.name);
                    }
                    options.add(getResources().getString(R.string.new_account));
                    builder.setTitle(R.string.select_account);
                    builder.setItems(options.toArray(new String[0]), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == options.size() - 1) {
                                startActivityForResult(new Intent(getApplicationContext(), AuthActivity.class), 1);
                            } else {
                                Accounts.setAccountActive(getApplicationContext(), options.get(which));
                                recreate();
                            }
                            dialog.cancel();
                        }
                    });
                    builder.show();
                } else {
                    startActivityForResult(new Intent(getApplicationContext(), AuthActivity.class), 1);
                }
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1) {
            recreate();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
