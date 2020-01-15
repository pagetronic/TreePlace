package live.page.android.sys;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.agroneo.droid.BuildConfig;
import com.agroneo.droid.R;

import live.page.android.auth.Accounts;
import live.page.android.auth.AccountsChooser;

@SuppressLint("Registered")
public class PageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.base_pop);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        AccountsChooser.make(this);

        if (BuildConfig.DEBUG) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        Accounts.intentCode(this);
    }

    public void setView(int layout) {
        ((LinearLayout) findViewById(R.id.host)).addView(getLayoutInflater().inflate(layout, null));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
