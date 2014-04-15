package me.kfang.levelly.app;

import android.hardware.SensorManager;
import android.view.Surface;

import java.util.ArrayList;
import java.util.List;

/**
 * Static methods for calculating the device orientation.
 */
public final class OrientationManager {

  private static final OrientationManager INSTANCE = new OrientationManager();
  private static final float ORIENTATION_CHANGE_THRESHOLD = 5;

  private int mCurrentOrientation;
  private float mRotation;
  private float mTilt;
  private float mAccurateDeviceRotation;
  private float mAccurateDeviceRotationTilt;

  public interface OrientationChangeListener {
    public void onOrientationChanged(int oldOrientation, int newOrientation);
  }

  List<OrientationChangeListener> mListeners;


  private OrientationManager() {
    mCurrentOrientation = Surface.ROTATION_0;
    mListeners = new ArrayList<OrientationChangeListener>();
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

    int newOrientation = getCurrentOrientation();
    if (mCurrentOrientation != newOrientation) {
      for (OrientationChangeListener l : mListeners) {
        l.onOrientationChanged(mCurrentOrientation, newOrientation);
      }
      mCurrentOrientation = newOrientation;
    }
  }

  public void addOrientationChangeListener(OrientationChangeListener listener) {
    mListeners.add(listener);
  }

  public void removeOrientationChangeListener(OrientationChangeListener listener) {
    mListeners.remove(listener);
  }

  public int getCurrentOrientation() {
    if (mCurrentOrientation != Surface.ROTATION_90
        && mAccurateDeviceRotation > 45 + ORIENTATION_CHANGE_THRESHOLD
        && mAccurateDeviceRotation <= 135 - ORIENTATION_CHANGE_THRESHOLD) {
      return Surface.ROTATION_90;
    } else if (mCurrentOrientation != Surface.ROTATION_180
        && mAccurateDeviceRotation > 135 + ORIENTATION_CHANGE_THRESHOLD
        && mAccurateDeviceRotation <= 225 - ORIENTATION_CHANGE_THRESHOLD) {
      return Surface.ROTATION_180;
    } else if (mCurrentOrientation != Surface.ROTATION_270
        && mAccurateDeviceRotation > 225 + ORIENTATION_CHANGE_THRESHOLD
        && mAccurateDeviceRotation <= 315 - ORIENTATION_CHANGE_THRESHOLD) {
      return Surface.ROTATION_270;
    } else if (mCurrentOrientation != Surface.ROTATION_0
        && (mAccurateDeviceRotation > 315 + ORIENTATION_CHANGE_THRESHOLD
        || mAccurateDeviceRotation <= 35 - ORIENTATION_CHANGE_THRESHOLD)) {
      return Surface.ROTATION_0;
    } else {
      return mCurrentOrientation;
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

  private static double getGravityMagnitude(float x, float y, float z) {
    return Math.sqrt(x * x + y * y + z * z);
  }

  public static float getDeviceTilt(float x, float y, float z) {
    float tilt = (float) Math.toDegrees(Math.acos(z / getGravityMagnitude(x, y, z)));
    if (Float.isNaN(tilt)) {
      return z < 0 ? 180 : 0;
    }
    return tilt;
  }

  public static float getXTilt(float x, float y, float z) {
    double projectionLength = Math.sqrt(y * y + z * z);
    float angle = (float) Math.toDegrees(Math.acos(projectionLength / getGravityMagnitude(x, y, z)));
    if (x < 0) {
      angle *= -1;
    }
    return angle;
  }

  public static float getYTilt(float x, float y, float z) {
    return getXTilt(y, x, z);
  }

  public static boolean isLandscape(float rotation) {
    return ((rotation > 45 && rotation < 135) || (rotation > 225 && rotation < 315));
  }

  public static float getHorizonOffset(float rotation) {
    return -1 * ((rotation + 45) % 90 - 45);
  }
 }
