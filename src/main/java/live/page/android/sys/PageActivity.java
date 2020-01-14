package live.page.android.sys;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.agroneo.droid.R;

@SuppressLint("Registered")
public class PageActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setCustomView(R.layout.app_toolbar);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setDisplayUseLogoEnabled(false);

    }

    @Override
    public boolean onNavigateUp() {
        onBackPressed();
        return true;
    }

}
