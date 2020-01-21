package com.agroneo.droid;

import com.agroneo.droid.specimens.SpecimensFragment;

import live.page.android.MainActivity;
import live.page.android.sys.Settings;
import live.page.android.threads.ForumsFragment;

public class MainAgroneo extends MainActivity {
    @Override
    protected void init() {
        MenuFragment questions = new MenuFragment("ROOT", "Questions", new ForumsFragment("/questions?lng=" + Settings.getLng(this)));
        MenuFragment specimens = new MenuFragment("SPECIMENS", "Specimens", new SpecimensFragment());
        addMenu(questions, specimens);
        loadFragment(questions);
    }

}
