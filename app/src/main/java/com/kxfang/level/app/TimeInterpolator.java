package com.kxfang.level.app;

import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

/**
 * Class for interpolating timed transitions.
 */
public class TimeInterpolator {
  private Interpolator mInterpolator;

  private long mStartTime = 0;
  private long mDurationMillis;

  // Holds the current authoritative "now" at beginning of each call.
  private long mNowMillis;

  private boolean mReversed;
  /**
   * Sets up a interpolator that interpolates progress between transitions.
   */
  public TimeInterpolator(long durationMillis) {
    mInterpolator = new LinearInterpolator();
    mDurationMillis = durationMillis;
    mReversed = false;
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

  public void reset() {
    mReversed = false;
    mStartTime = 0;
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

  public float getProgress() {
    setNow();
    float progress =  getProgressMillis() / (float) mDurationMillis;
    progress = Math.min(progress, 1.0f);
    return mReversed ? 1 - progress : progress;
  }
}
