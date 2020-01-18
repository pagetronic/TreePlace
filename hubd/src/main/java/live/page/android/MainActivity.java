package live.page.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;

import java.util.List;

import live.page.android.api.ApiAsync;
import live.page.android.api.ApiResult;
import live.page.android.api.Json;
import live.page.android.auth.Accounts;
import live.page.android.auth.AccountsChooser;
import live.page.android.threads.ForumsFragment;

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout._main);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);

        Json base = new Json("id", "ROOT").put("url", "/threads?lng=fr").put("title", "Threads");
        makeMenu(base);
        loadFragment(base);

        ApiAsync.get(getBaseContext(), "/forums/root?lng=fr", new ApiResult() {
            @Override
            public void success(Json data) {
                List<Json> result = data.getListJson("result");
                if (result != null) {
                    makeMenu(result.toArray(new Json[0]));
                }
            }
        });
        AccountsChooser.make(this);

    }

    private void makeMenu(final Json... forums) {
        final Menu menus = navigationView.getMenu();
        for (final Json forum : forums) {
            MenuItem menu = menus.add(forum.getId());
            menu.setTitle(forum.getString("title"));
            menu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    loadFragment(forum);
                    drawer.closeDrawers();
                    return true;
                }
            });
        }
        navigationView.invalidate();
    }

    private void loadFragment(Json forum) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(forum.getId());
        FragmentTransaction transaction = fm.beginTransaction();
        if (fragment == null) {
            fragment = new ForumsFragment(forum);
            transaction.add(fragment, forum.getId());
        }
        transaction.replace(R.id.host_fragment, fragment);
        transaction.commit();
        setTitle(forum.getString("title"));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Accounts.intentCode(this, intent);
        super.onNewIntent(intent);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (!Accounts.accountActiveRemoved(getBaseContext())) {
            recreate();
        }
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
        getMenuInflater().inflate(R.menu.action, menu);
        return true;
    }


}
