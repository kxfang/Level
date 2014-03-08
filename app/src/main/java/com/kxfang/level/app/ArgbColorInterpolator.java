package com.kxfang.level.app;

import android.animation.ArgbEvaluator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

/**
 * Class for interpolating ARGB color fading animations.
 */
public class ArgbColorInterpolator {
  private Interpolator mInterpolator;

  private final int mStartColor;
  private final int mTransitionColor;

  private long mStartTime = 0;
  private long mDurationMillis;

  // Holds the current authoritative "now" at beginning of each call.
  private long mNowMillis;

  private boolean mReversed;

  private ArgbEvaluator mArgbEvaluator;

  /**
   * Sets up a interpolator that transitions between mainColor and transitionColor
   */
  public ArgbColorInterpolator(
      int mainColor,
      int transitionColor,
      long durationMillis) {
    mInterpolator = new LinearInterpolator();
    mStartColor = mainColor;
    mTransitionColor = transitionColor;
    mDurationMillis = durationMillis;
    mReversed = false;

    mArgbEvaluator = new ArgbEvaluator();
  }

  public void start() {
    setNow();
    if (mStartTime == 0) {
      mStartTime = mNowMillis;
    }
    if (mReversed) {
      mStartTime = mNowMillis - Math.max(getRemainingMillis(), 0);
      mReversed = false;
    }
  }

  public void reverse() {
    setNow();
    if (!mReversed) {
      mStartTime = mNowMillis - Math.max(getRemainingMillis(), 0);
      mReversed = true;
    }
  }

  public int getColor() {
    setNow();
    return (Integer) mArgbEvaluator.evaluate(getProgress(), mStartColor, mTransitionColor);
  }

  private void setNow() {
    mNowMillis = System.currentTimeMillis();
  }

  private long getRemainingMillis() {
    return mDurationMillis - getProgressMillis();
  }

  private long getProgressMillis() {
    if (mStartTime == 0) {
      return 0;
    }
    return mNowMillis - mStartTime;
  }

  private float getProgress() {
    float progress =  getProgressMillis() / (float) mDurationMillis;
    progress = Math.min(progress, 1.0f);
    return mReversed ? 1 - progress : progress;
  }
}
