package me.kfang.levelly.filter;

/**
 * A filter that applies no transformations.
 */
public class IdentityFilter implements FloatFilter {
  @Override
  public float[] next(float[] next) {
    return next;
  }
}
