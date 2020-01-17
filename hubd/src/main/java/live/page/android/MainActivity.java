package live.page.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import live.page.android.api.ApiAsync;
import live.page.android.api.ApiResult;
import live.page.android.api.Json;
import live.page.android.auth.Accounts;
import live.page.android.auth.AccountsChooser;
import live.page.android.threads.ForumsFragment;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout._main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);


        final NavigationView navigationView = findViewById(R.id.nav_view);
        final Menu menus = navigationView.getMenu();

//TODO make all dynamic !
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_forums).setDrawerLayout(drawer).build();
        final NavController navController = Navigation.findNavController(this, R.id.host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        ApiAsync.get(getBaseContext(), "/forums/root?lng=fr", new ApiResult() {
            @Override
            public void success(Json data) {
                for (final Json forum : data.getListJson("result")) {
                    MenuItem menu = menus.add(forum.getId());
                    menu.setTitle(forum.getString("title"));
                    menu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            FragmentManager fm = getSupportFragmentManager();
                            Fragment fragment = fm.findFragmentByTag(forum.getId());
                            FragmentTransaction transaction = fm.beginTransaction();
                            if (fragment == null) {
                                fragment = new ForumsFragment(forum);
                                transaction.add(fragment, forum.getId());
                            }
                            transaction.replace(R.id.host_fragment, fragment);
                            transaction.commit();

                            return false;
                        }
                    });

                }
                navigationView.invalidate();
            }
        });
        AccountsChooser.make(this);


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

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


}
