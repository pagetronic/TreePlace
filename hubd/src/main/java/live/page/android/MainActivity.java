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

import live.page.android.api.Json;
import live.page.android.auth.Accounts;
import live.page.android.auth.AccountsChooser;

public abstract class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawer;
    protected Json user;

    protected abstract void init();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user = Accounts.getProfile(getBaseContext());

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

        AccountsChooser.make(this);
        init();

    }

    protected void addMenu(List<MenuFragment> fragments) {
        addMenu(fragments.toArray(new MenuFragment[0]));
    }

    protected void addMenu(MenuFragment... fragments) {

        final Menu menus = navigationView.getMenu();
        for (final MenuFragment fragment : fragments) {
            MenuItem menu = menus.add(fragment.getTitle());
            menu.setOnMenuItemClickListener(item -> {
                loadFragment(fragment);
                return true;
            });
        }
        navigationView.invalidate();
    }

    protected void loadFragment(MenuFragment fragmentMenu) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        Fragment fragment = fm.findFragmentByTag(fragmentMenu.getId());
        if (fragment == null) {
            fragment = fragmentMenu.getFragment();
        }
        transaction.replace(R.id.host_fragment, fragment);
        transaction.commit();
        setTitle(fragmentMenu.getTitle());
        drawer.closeDrawers();
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

    protected class MenuFragment {
        private Fragment fragment;
        private String title;
        private String id;

        public MenuFragment(String id, String title, Fragment fragment) {
            this.id = id;
            this.title = title;
            this.fragment = fragment;

        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public Fragment getFragment() {
            return fragment;
        }
    }
}
