package live.page.android.auto;

import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import live.page.android.BuildConfig;
import live.page.android.R;
import live.page.android.api.Json;
import live.page.android.auth.Accounts;
import live.page.android.auth.AccountsChooser;

public abstract class PageActivity extends AppCompatActivity {

    protected Json user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = Accounts.getProfile(getContext());

        setContentView(R.layout.base_pop);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        AccountsChooser.make(this);

        if (BuildConfig.DEBUG) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        ((LinearLayout) findViewById(R.id.host)).addView(getLayoutInflater().inflate(getLayout(), null));
        onCreate();
    }

    protected abstract int getLayout();

    protected abstract void onCreate();

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public NavigationView getNavRight() {
        return findViewById(R.id.nav_right);
    }

    public void removeNavRight() {
        ((DrawerLayout) findViewById(R.id.drawer_layout)).removeView(getNavRight());
    }

    public Context getContext() {
        return this;
    }
}
