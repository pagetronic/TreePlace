package com.agroneo.treeplace.sys;

import android.view.View;

public class Animators {
    public static void gone(final Runnable end, View... views) {
        final View last = views[views.length - 1];
        for (final View view : views) {
            view.clearAnimation();
            view.animate().alpha(0F).setDuration(200).withEndAction(new Runnable() {
                @Override
                public void run() {
                    view.setVisibility(View.GONE);
                    if (last.equals(view)) {
                        end.run();
                    }
                }
            });
        }
    }

    public static void visible(View... views) {
        for (final View view : views) {
            view.clearAnimation();
            view.setAlpha(0F);
            view.setVisibility(View.VISIBLE);
            view.animate().alpha(1F).setDuration(200).withEndAction(new Runnable() {
                @Override
                public void run() {
                    view.setVisibility(View.VISIBLE);

                }
            });
        }
    }
}
