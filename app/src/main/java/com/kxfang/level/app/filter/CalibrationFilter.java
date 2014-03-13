package com.kxfang.level.app.filter;

/**
 * A filter that applies stored calibration values.
 */
public class CalibrationFilter implements FloatFilter {

  private float[] mOffsets;

  public CalibrationFilter(float[] offsets) {
    mOffsets = offsets;
  }

  @Override
  public float[] next(float[] next) {
    for (int i = 0; i < next.length; i++) {
      next[i] -= mOffsets[i];
    }
    return next;
  }
}
