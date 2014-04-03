package me.kfang.levelly.filter;

/**
 * A low pass filter that conditionally applies given a threshold.
 */
public class ThresholdLowPassFilter implements FloatFilter {
  private LowPassFilter mFilter;
  private final float ALPHA;
  private final float THRESHOLD;

  public ThresholdLowPassFilter(float alpha, float threshold) {
    ALPHA = alpha;
    THRESHOLD = threshold;
  }

  @Override
  public float[] next(float[] next) {
    if (Math.abs(next[0]) > THRESHOLD && Math.abs(next[1]) > THRESHOLD) {
      mFilter = null;
      return next;
    }
    if (mFilter == null) {
      mFilter = new LowPassFilter(ALPHA);
    }
    return mFilter.next(next);
  }
}
