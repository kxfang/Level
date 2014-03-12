package com.kxfang.level.app.filter;

/**
 * A low pass filter over sensor data.
 */
public class LowPassFilter implements FloatFilter {
  private final float THRESHOLD;
  private float mPrevious = 0;

  public LowPassFilter(float threshold) {
    THRESHOLD = threshold;
  }

  @Override
  public float next(float next) {
    return mPrevious + THRESHOLD * (next - mPrevious);
  }
}
