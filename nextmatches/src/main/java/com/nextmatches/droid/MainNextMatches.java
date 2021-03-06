package com.nextmatches.droid;

import java.util.ArrayList;
import java.util.List;

import live.page.android.MainActivity;
import live.page.android.api.ApiAsync;
import live.page.android.api.ApiResult;
import live.page.android.api.Json;
import live.page.android.threads.ForumsView;
import live.page.android.utils.Settings;

public class MainNextMatches extends MainActivity {

    @Override
    protected void init() {

        MenuFragment def = new MenuFragment("ROOT", "Threads", new ForumsView("/threads"));
        addMenu(def);
        loadFragment(def);

        ApiAsync.get(this, "/forums/root?lng=" + Settings.getLng(this), new ApiResult() {
            @Override
            public void success(Json data) {
                List<Json> result = data.getListJson("result");
                if (result != null) {
                    List<MenuFragment> frags = new ArrayList<>();
                    for (Json forum : result) {
                        frags.add(new MenuFragment(forum.getId(), forum.getString("title"), new ForumsView(forum.getString("url"))));
                    }
                    addMenu(frags);
                }
            }
        });

    }
}
