package com.skylex_news_feed.news_feed.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.Interpolator;

/**
 * My helper class for animating views in android
 */
public class AnimationUtil {

    //region FIELDS

    AnimatorSet rootSet;

    private final float defTranslationYFromValue = 5f;
    private final float defTranslationYToValue = 0f;

    private final float defFadeInFromValue = 0.0f;
    private final float defFadeInToValue = 1.0f;

    private float translationYFromValue;
    private float translationYToValue;

    private float fadeInFromValue;
    private float fadeInToValue;

    //endregion

    // region CONSTRUCTORS
    public AnimationUtil() {

        rootSet = new AnimatorSet();

    }

    // endregion


    public void makeVisibleWithSlideUpFadeIn(View view, int slideUpDuration, int fadeInDuration, float translationYFromValue, float translationYToValue, float fadeInFromValue, float fadeInToValue) {


        this.translationYFromValue = translationYFromValue;
        this.translationYToValue = translationYToValue;
        this.fadeInFromValue = fadeInFromValue;
        this.fadeInToValue = fadeInToValue;

        view.setVisibility(View.VISIBLE);

        ObjectAnimator translationY = ObjectAnimator.ofFloat(view, "translationX", translationYFromValue, translationYToValue);
        translationY.setDuration(slideUpDuration);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view, "alpha", fadeInFromValue, fadeInToValue);
        fadeIn.setDuration(fadeInDuration);

        rootSet.playTogether(translationY, fadeIn);
        rootSet.start();
    }


    public void reverseMakeVisibleWithSlideUpFadeIn(View view, int slideDownDuration, int fadeOutDuration) {

        ObjectAnimator translationY = ObjectAnimator.ofFloat(view, "translationY", translationYToValue, translationYFromValue);
        translationY.setDuration(slideDownDuration);

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(view, "alpha", fadeInToValue, fadeInFromValue);
        fadeOut.setDuration(fadeOutDuration);

        rootSet.playTogether(translationY, fadeOut);
        rootSet.start();

        rootSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

                view.setVisibility(View.GONE);
                rootSet.removeAllListeners();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    public void rotate(View view, int rotateDuration, float fromValue, float toValue) {

        ObjectAnimator rotate = ObjectAnimator.ofFloat(view, "rotation", fromValue, toValue);
        rotate.setDuration(rotateDuration);

        rotate.start();
    }

    /**
     * Fades out a view from {@code fromValue} to {@code toValue}
     * @param view view to be
     * @param duration duration of animation
     * @param fromValue value to fade out from
     * @param toValue value to fade out to
     * @param endVisibility visibility to be set on the view after it has been animated.
     * @return an Object animator
     */
    public ObjectAnimator fadeOut(View view, int duration, float fromValue, float toValue, int endVisibility) {

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(view, "alpha", fromValue, toValue);
        fadeOut.setDuration(duration);

        fadeOut.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(endVisibility);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        return fadeOut;
    }

    /**
     * Fades in a view form {@code fromValue} to {@code toValue}
     * @param view view to be faded in
     * @param duration duration of animation
     * @param fromValue start alpha value
     * @param toValue end alpha value
     * @return object animator for the animation.
     */
    public ObjectAnimator fadeIn(View view, int duration, float fromValue, float toValue) {

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view, "alpha", fromValue, toValue);
        fadeIn.setDuration(duration);
        fadeIn.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                view.setVisibility(View.VISIBLE);
            }
        });

        return fadeIn;
    }

    /**
     * Slides a view vertically from {@code fromValue} to {@code toValue}
     * @param view view to slide
     * @param duration duration of animation
     * @param fromValue start value
     * @param toValue end value
     * @param interpolator interpolator to be applied to the animation
     * @return object animator for the animation
     */
    public ObjectAnimator slideY(View view, int duration, float fromValue, float toValue, Interpolator interpolator) {
        ObjectAnimator slideUp = ObjectAnimator.ofFloat(view, "translationY", fromValue, toValue);
        slideUp.setDuration(duration);
        slideUp.setInterpolator(interpolator);

        return slideUp;
    }

    /**
     * Slides a view horizontally from {@code fromValue} to {@code toValue}
     * @param view view to slide
     * @param duration duration of animation
     * @param fromValue start value
     * @param toValue end value
     * @param interpolator interpolator to be applied to the animation
     * @return object animator for the animation
     */
    public ObjectAnimator slideX(View view, int duration, float fromValue, float toValue, Interpolator interpolator) {
        ObjectAnimator slideSideways = ObjectAnimator.ofFloat(view, "translationX", fromValue, toValue);
        slideSideways.setDuration(duration);
        slideSideways.setInterpolator(interpolator);

        return slideSideways;
    }


}
