package com.kxfang.level.app.filter;

/**
 * Filter interface used to process a stream of floats.
 */
public interface FloatFilter {

  /**
   * Retrieve the filtered value based on some given value.
   * @param next the values to process.
   * @return the filtered values.
   */
  public float[] next(float[] next);
}
