package com.agroneo.droid;

import com.agroneo.droid.specimens.SpecimensView;

import live.page.android.MainActivity;
import live.page.android.threads.ForumsView;

public class MainAgroneo extends MainActivity {
    @Override
    protected void init() {
        MenuFragment questions = new MenuFragment("ROOT", "Questions", new ForumsView("/questions"));
        MenuFragment specimens = new MenuFragment("SPECIMENS", "Specimens", new SpecimensView());
        addMenu(questions, specimens);
        loadFragment(questions);
    }

}
