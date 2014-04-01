package me.kfang.levelly.app;

import android.animation.FloatEvaluator;
import android.animation.TypeEvaluator;

/**
* Wrapper class for rotation and tilt.
* Mutable to avoid garbage collection when drawing.
*/
public class DevicePosition {
  private float mRotation;
  private float mTilt;

  public static class DevicePositionEvaluator implements TypeEvaluator<DevicePosition> {
    private FloatEvaluator mFloatEvaluator;

    public DevicePositionEvaluator() {
      mFloatEvaluator = new FloatEvaluator();
    }

    @Override
    public DevicePosition evaluate(float progress, DevicePosition start, DevicePosition end) {
      return new DevicePosition(
          mFloatEvaluator.evaluate(progress, start.getRotation(), end.getRotation()),
          mFloatEvaluator.evaluate(progress, start.getTilt(), end.getTilt()));
    }
  }

  public DevicePosition(float rotation, float tilt) {
    mRotation = rotation;
    mTilt = tilt;
  }

  public void setRotation(float rotation) {
    mRotation = rotation;
  }

  public void setTilt(float tilt) {
    mTilt = tilt;
  }

  public float getRotation() {
    return mRotation;
  }

  public float getTilt() {
    return mTilt;
  }
}
