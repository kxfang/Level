package com.kxfang.level.app.filter;

/**
 *
 */
public class MovingAverageFilter implements FloatFilter {
  private final int MAX_SAMPLES;
  private int mSampleCount;
  private double mSum;
  private float[] mSamples;
  private int mNext = 0;

  public MovingAverageFilter(int sampleSize) {
    MAX_SAMPLES = sampleSize;
    mSamples = new float[MAX_SAMPLES];
  }

  @Override
  public float next(float next) {
    mSum += next;
    if (mSampleCount < MAX_SAMPLES) {
      mSampleCount++;
      return (float) (mSum / mSampleCount);
    } else {
      mSum -= mSamples[mNext];
      mSamples[mNext] = next;
      mNext = (mNext + 1) % MAX_SAMPLES;
      return (float) (mSum / MAX_SAMPLES);
    }
  }
}
