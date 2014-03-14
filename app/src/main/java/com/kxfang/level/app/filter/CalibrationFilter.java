package com.kxfang.level.app.filter;

import android.util.Log;
import android.view.ViewDebug;

import Jama.Matrix;

/**
 * A filter that applies stored calibration values.
 */
public class CalibrationFilter implements FloatFilter {

  private Matrix mOffsets;
  private Matrix mBasisZ;
  private Matrix mBasisY;
  private Matrix mBasisX;
  private Matrix mBasisMatrix;
  private Matrix mBMatrix;

  private final double[] UNIT_X = {1, 0, 0};
  private final double[] UNIT_Y = {0, 1, 0};

  private CalibrationFilter(float[] offsets) {
    double[] offsetsCopy = new double[offsets.length];
    for (int i = 0; i < offsets.length; i++) {
      offsetsCopy[i] = offsets[i];
    }
    mOffsets = new Matrix(offsetsCopy, offsets.length);
    mBasisZ = mOffsets.copy();
    normalize(mBasisZ);

    mBasisX = project(new Matrix(UNIT_X, UNIT_X.length), mBasisZ);
    normalize(mBasisX);
    mBasisY = project(new Matrix(UNIT_Y, UNIT_Y.length), mBasisZ);
    normalize(mBasisY);

    double[][] basisArray = {
        mBasisX.getColumnPackedCopy(),
        mBasisY.getColumnPackedCopy(),
        mBasisZ.getColumnPackedCopy() };

    mBasisMatrix = new Matrix(basisArray).transpose();
    mBMatrix = new Matrix(1, UNIT_X.length).transpose();
    Log.d("BASISMATRIX", "basis");
    log(mBasisMatrix);
  }

  public static FloatFilter withOffsets(float[] offsets) {
    if (Math.abs(offsets[0] + offsets[1]) < 0.0001f) {
      return new IdentityFilter();
    }
    return new CalibrationFilter(offsets);
  }

  private void normalize(Matrix m) {
    if (m.getColumnDimension() > 1) {
      throw new IllegalArgumentException("Matrix is not one-dimensional");
    }

    double sum = 0;
    for (double d : m.getColumnPackedCopy()) {
      sum += d * d;
    }

    sum = Math.sqrt(sum);

    for (int i = 0; i < m.getRowDimension(); i++) {
      m.set(i, 0, m.get(i, 0) / sum);
    }
  }

  private double dotProduct(Matrix vec1, Matrix vec2) {
    log(vec1);
    log(vec2);
    if (vec1.getColumnDimension() > 1 || vec2.getColumnDimension() > 1) {
      throw new IllegalArgumentException("Matrix is not one-dimensional");
    }
    if (vec1.getRowDimension() != vec2.getRowDimension()) {
      throw new IllegalArgumentException("Vectors not the same length");
    }

    double result = 0;
    Matrix product = vec1.arrayTimes(vec2);
    for (int i = 0; i < product.getRowDimension(); i++) {
      result += product.get(i, 0);
    }
    return result;
  }

  private Matrix project(Matrix vec, Matrix norm) {
    Matrix perp = norm.times(dotProduct(vec, norm));
    return vec.minus(perp);
  }

  private void log(Matrix m) {
    Log.d("MATRIX", m.getRowDimension() + " x " + m.getColumnDimension());
    for (int i = 0; i < m.getRowDimension(); i++) {
      String s = "";
      for (int j = 0; j < m.getColumnDimension(); j++) {
        s += m.get(i, j) + ", ";
      }
      Log.d("MATRIX", s);
    }
  }

  @Override
  public float[] next(float[] next) {
//    log(mBMatrix);
    for (int i = 0; i < next.length; i++) {
      mBMatrix.set(i, 0, next[i]);
    }

    Matrix x = mBasisMatrix.solve(mBMatrix);

    for (int i = 0; i < x.getRowDimension(); i++) {
      next[i] = (float) x.get(i, 0);
    }
    return next;
  }
}
