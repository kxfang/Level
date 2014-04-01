package me.kfang.levelly.filter;

/**
 * A low pass filter that conditionally applies given a threshold.
 */
public class LowPassFilter implements FloatFilter {
  private float[] mPrevious;
  private final float ALPHA;
  private final float THRESHOLD;

  public LowPassFilter(float alpha, float threshold) {
    ALPHA = alpha;
    THRESHOLD = threshold;
    mPrevious = null;
  }

  @Override
  public float[] next(float[] next) {
    if (Math.abs(next[0]) > THRESHOLD && Math.abs(next[1]) > THRESHOLD) {
      mPrevious = null;
      return next;
    }
    if (mPrevious == null) {
      mPrevious = new float[next.length];
      mPrevious = next;
      return next;
    }
    for (int i = 0; i < next.length; i++) {
      next[i] = mPrevious[i] + ALPHA * (next[i] - mPrevious[i]);
      mPrevious[i] = next[i];
    }
    return next;
  }
}
