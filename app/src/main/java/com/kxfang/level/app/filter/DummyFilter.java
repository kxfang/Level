package com.kxfang.level.app.filter;

/**
 * A filter that does no work filtering.
 */
public class DummyFilter implements FloatFilter {
  @Override
  public float[] next(float[] next) {
    return next;
  }
}
