package live.page.android.sys;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.agroneo.droid.BuildConfig;
import com.agroneo.droid.R;

import live.page.android.auth.AccountsChooser;

public abstract class PageActivity extends AppCompatActivity {

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

}
