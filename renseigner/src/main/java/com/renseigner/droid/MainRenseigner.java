package com.renseigner.droid;

import live.page.android.MainActivity;
import live.page.android.threads.ForumsFragment;

public class MainRenseigner extends MainActivity {
    @Override
    protected void init() {
        MenuFragment def = new MenuFragment("ROOT", "Questions", new ForumsFragment("/questions"));
        addMenu(def);
        loadFragment(def);

    }
}
