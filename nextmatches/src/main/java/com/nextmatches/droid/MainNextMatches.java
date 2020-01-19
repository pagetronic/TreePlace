package com.nextmatches.droid;

import java.util.ArrayList;
import java.util.List;

import live.page.android.MainActivity;
import live.page.android.api.ApiAsync;
import live.page.android.api.ApiResult;
import live.page.android.api.Json;
import live.page.android.threads.ForumsFragment;

public class MainNextMatches extends MainActivity {

    @Override
    protected void init() {

        MenuFragment def = new MenuFragment("ROOT", "Threads", new ForumsFragment("/threads"));
        addMenu(def);
        loadFragment(def);

        ApiAsync.get(getBaseContext(), "/forums/root?lng=fr", new ApiResult() {
            @Override
            public void success(Json data) {
                List<Json> result = data.getListJson("result");
                if (result != null) {
                    List<MenuFragment> frags = new ArrayList<>();
                    for (Json forum : result) {
                        frags.add(new MenuFragment(forum.getId(), forum.getString("title"), new ForumsFragment(forum.getString("url"))));
                    }
                    addMenu(frags);
                }
            }
        });

    }
}
