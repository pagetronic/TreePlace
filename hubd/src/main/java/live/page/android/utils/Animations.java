package live.page.android.utils;

import android.animation.Animator;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;

public class Animations {
    public static void moveOut(final View view, final Events events) {
        final ViewPropertyAnimator anim = view.animate();
        anim.setDuration(400);
        anim.translationX(view.getWidth());
        anim.setInterpolator(new AccelerateInterpolator());
        anim.start();
        anim.setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                events.finished();
                view.setTranslationX(0);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

    }

    public abstract static class Events {

        abstract public void finished();


    }
}
