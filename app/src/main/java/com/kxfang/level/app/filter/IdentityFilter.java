package com.kxfang.level.app.filter;

/**
 * A filter that applies no transformations.
 */
public class IdentityFilter implements FloatFilter {
  @Override
  public float[] next(float[] next) {
    return next;
  }
}
