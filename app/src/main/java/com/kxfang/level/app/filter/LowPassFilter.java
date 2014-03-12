package com.kxfang.level.app.filter;

/**
 * A low pass filter that conditionally applies given a threshold.
 */
public class LowPassFilter implements FloatFilter {
  private float mPrevious;
  private boolean mPreviousSet;
  private final float ALPHA;
  private final float THRESHOLD;

  public LowPassFilter(float alpha, float threshold) {
    ALPHA = alpha;
    THRESHOLD = threshold;
    mPreviousSet = false;

  }

  @Override
  public float next(float next) {
    if (Math.abs(next) > THRESHOLD) {
      mPreviousSet = false;
      return next;
    }
    if (!mPreviousSet) {
      mPreviousSet = true;
      mPrevious = next;
      return next;
    }
    float ret = mPrevious + THRESHOLD * (next - mPrevious);
    mPrevious = ret;
    return ret;
  }
}
