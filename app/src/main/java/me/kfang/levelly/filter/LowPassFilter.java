package me.kfang.levelly.filter;

import java.util.Arrays;

/**
 * A generic low pass filter.
 */
public class LowPassFilter implements FloatFilter {
  private float[] mPrevious;
  private final float ALPHA;

  public LowPassFilter(float alpha) {
    ALPHA = alpha;
    mPrevious = null;
  }

  @Override
  public float[] next(float[] next) {
    if (mPrevious == null) {
      mPrevious = Arrays.copyOf(next, next.length);
      return next;
    }
    for (int i = 0; i < next.length; i++) {
      next[i] = mPrevious[i] + ALPHA * (next[i] - mPrevious[i]);
      mPrevious[i] = next[i];
    }
    return next;
  }
}
