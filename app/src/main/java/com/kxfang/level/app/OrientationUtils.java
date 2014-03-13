package com.kxfang.level.app;

import android.hardware.SensorManager;

/**
 * Static methods for calculating the device orientation.
 */
public final class OrientationUtils {

  private OrientationUtils() { }

  public static float getRotationDegrees(float x, float y) {
    float rotation = (float) Math.toDegrees(Math.atan(x / y));
    if ((x < 0 && y < 0) || (x >= 0 && y < 0)) {
      rotation += 180;
    }
    if (rotation < 0) {
      rotation += 360;
    }
    return rotation;
  }

  public static float getDeviceTilt(float z) {
    float tilt = (float) Math.toDegrees(Math.acos(z / SensorManager.GRAVITY_EARTH));
    if (Float.isNaN(tilt)) {
      return 0;
    }
    return tilt;
  }

  public static boolean isLandscape(float rotation) {
    return ((rotation > 45 && rotation < 135) || (rotation > 225 && rotation < 315));
  }
}
