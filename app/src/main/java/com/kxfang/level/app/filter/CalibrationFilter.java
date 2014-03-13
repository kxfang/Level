package com.kxfang.level.app.filter;

import android.hardware.SensorManager;
import android.util.Log;

import com.kxfang.level.app.LevelFragment;
import com.kxfang.level.app.LevelView;
import com.kxfang.level.app.OrientationUtils;

/**
 * A filter that applies stored calibration values.
 */
public class CalibrationFilter implements FloatFilter {

  private float[] mOffsets;

  float mTiltOffset;
  float mXYOffsetRatio;

  float mXCorrectionAngle;
  float mYCorrectionAngle;

  private CalibrationFilter(float[] offsets) {
    mOffsets = offsets;
    mTiltOffset = OrientationUtils.getDeviceTilt(offsets[2]);
    mXYOffsetRatio = offsets[1] / (offsets[1] + offsets[0]);

    mXCorrectionAngle = getAngle(offsets[0], offsets[2]);
    mYCorrectionAngle = getAngle(offsets[1], offsets[2]);
  }

  public static FloatFilter withOffset(float[] offsets) {
    float ratio = offsets[1] / offsets[0];
    if (Float.isNaN(ratio) || Float.isInfinite(ratio)) {
      // No calibration filtering needed.
      return new DummyFilter();
    }
    return new CalibrationFilter(offsets);
  }

  private float getAngle(float o, float h) {
    return (float) Math.atan(o/h);
  }

  @Override
  public float[] next(float[] next) {
    float[] calibrated = new float[next.length];

    float rawTilt = OrientationUtils.getDeviceTilt(next[2]);
    float correctedTilt = Math.abs(rawTilt - mTiltOffset);

    float correctedXAngle = Math.abs(getAngle(next[0], next[2]) - mXCorrectionAngle);
    float correctedYAngle = Math.abs(getAngle(next[1], next[2]) - mYCorrectionAngle);
    Math.toRadians(correctedTilt);
    Math.cos(Math.toRadians(correctedTilt));
    double a = SensorManager.GRAVITY_EARTH * Math.cos(Math.toRadians(correctedTilt));
    float b = (float) a;
    calibrated[2] = b;
//    calibrated[2] =
//        (float) (SensorManager.GRAVITY_EARTH * Math.cos(Math.toRadians(correctedTilt)));
//    float squareDiff =
//        SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH - next[2] * next[2];

//    float ratio;
//    if (Math.abs(next[0] * next[0] + next[1] * next[1]) < 0.001f) {
//      ratio = 0;
//    } else {
//      ratio = (next[0] * next[0]) / (next[0] * next[0] + next[1] * next[1]);
//    }
//    calibrated[0] = 0;//(float) (Math.sqrt(squareDiff * ratio));
//    calibrated[1] = 0; //(float) (Math.sqrt(squareDiff * (1- ratio)));


//    next[0] =
//        (float) (next[2] * Math.tan(correctedXAngle));
//    next[1] =
//        (float) (next[2] * Math.tan(correctedYAngle));

//    float hypotenuse = (float) Math.sqrt(squareDiff);
//    Log.d("CTA", "" + Math.sqrt(next[0] * next[0] + next[1] * next[1] + next[2] * next[2]));
//    LevelFragment.logFloatValues(next);
//    Log.d("CTAG", "" + next[0] + ", " + next[1]);// Math.sqrt(next[0] * next[0] + next[1] * next[1] + next[2] * next[2]));
    return calibrated;
  }
}
