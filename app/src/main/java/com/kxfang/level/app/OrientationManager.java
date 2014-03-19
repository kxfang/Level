package com.kxfang.level.app;

import android.content.pm.ActivityInfo;
import android.hardware.SensorManager;
import android.view.Surface;

/**
 * Static methods for calculating the device orientation.
 */
public final class OrientationManager {

  private static final OrientationManager INSTANCE = new OrientationManager();

  private int mCurrentOrientation;
  private float mRotation;
  private float mTilt;
  private float mAccurateDeviceRotation;
  private float mAccurateDeviceRotationTilt;


  private OrientationManager() {
    mCurrentOrientation = Surface.ROTATION_0;
  }

  public static OrientationManager getInstance() {
    return INSTANCE;
  }

  public void setCurrentPosition(float tilt, float rotation) {
    mRotation = rotation;
    mTilt = tilt;

    // Cache the device rotation when it is meaningfully tilted.
    if (mTilt > 20 && mTilt < 160) {
      mAccurateDeviceRotation = mRotation;
      mAccurateDeviceRotationTilt = mTilt;
    }
  }

  public int getCurrentOrientation() {
    if (mAccurateDeviceRotation > 45 && mAccurateDeviceRotation <= 135) {
      return Surface.ROTATION_90;
    } else if (mAccurateDeviceRotation > 135 && mAccurateDeviceRotation <= 225) {
      return Surface.ROTATION_180;
    } else if (mAccurateDeviceRotation > 225 && mAccurateDeviceRotation <= 315) {
      return Surface.ROTATION_270;
    } else {
      return Surface.ROTATION_0;
    }
  }

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

  public static float getHorizonOffset(float rotation) {
    return -1 * ((rotation + 45) % 90 - 45);
  }
 }
