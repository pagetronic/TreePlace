package com.renseigner.droid;

import live.page.android.MainActivity;
import live.page.android.threads.ForumsView;

public class MainRenseigner extends MainActivity {
    @Override
    protected void init() {
        MenuFragment def = new MenuFragment("ROOT", "Questions", new ForumsView("/questions"));
        addMenu(def);
        loadFragment(def);

    }
}
