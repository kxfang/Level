package com.kxfang.level.app;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kxfang.level.app.color.ColorSet;
import com.kxfang.level.app.filter.CalibrationFilter;
import com.kxfang.level.app.filter.FloatFilter;
import com.kxfang.level.app.filter.LowPassFilter;
import com.squareup.seismic.ShakeDetector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Fragment encapsulating the main view of the Level.
 */
public class LevelFragment extends Fragment {

  private static final String TAG = "LevelFragment";

  private SensorManager mSensorManager;
  private BullsEyeLevelView mBullsEyeLevelView;
  private HorizonLevelView mHorizonLevelView;
  private LevelView mActiveLevelView;

  private ShakeDetector mShakeDetector;

  private DevicePosition mLevelViewPosition;

  private float[] mSensorValues;

  // SensorEventListener
  private FilteringSensorListener mSensorEventListener = new FilteringSensorListener(new SensorEventListener() {
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
      mSensorValues = sensorEvent.values;
      setPosition(
          OrientationUtils.getDeviceTilt(mSensorValues[2]),
          OrientationUtils.getRotationDegrees(mSensorValues[0], mSensorValues[1]));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
      // Do nothing
    }
  }, new ArrayList<FloatFilter>());

  private void setPosition(float deviceTilt, float rotation) {
    if (deviceTilt > 44.5f
        && deviceTilt < 134.5f) {
      setActiveLevelView(mHorizonLevelView);
    } else if (deviceTilt < 45.5f) {
      mBullsEyeLevelView.setConfig(BullsEyeLevelView.Config.DOWN);
      setActiveLevelView(mBullsEyeLevelView);
    } else if (deviceTilt > 135.5f) {
      mBullsEyeLevelView.setConfig(BullsEyeLevelView.Config.UP);
      setActiveLevelView(mBullsEyeLevelView);
    }

    mLevelViewPosition.setRotation(rotation);
    mLevelViewPosition.setTilt(deviceTilt);
    mActiveLevelView.setPosition(mLevelViewPosition);
  }

  private void setTilt(float deviceTilt) {
    setPosition(deviceTilt, mLevelViewPosition.getRotation());
  }

  private void setActiveLevelView(LevelView levelView) {
    if (mActiveLevelView == null) {
      mActiveLevelView = levelView;
      levelView.setVisibility(View.VISIBLE);
    } else if (mActiveLevelView != levelView) {
      levelView.setVisibility(View.VISIBLE);
      mActiveLevelView.setVisibility(View.GONE);
      mActiveLevelView = levelView;
    }
  }

  private void registerListeners() {
    mSensorManager.registerListener(
        mSensorEventListener,
        mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
        SensorManager.SENSOR_DELAY_FASTEST);
    mShakeDetector.start(mSensorManager);
  }

  private List<FloatFilter> getDefaultFilters(float[] calibrationValues) {
    List<FloatFilter> filters = new LinkedList<FloatFilter>();
    filters.add(new CalibrationFilter(calibrationValues));
    filters.add(new LowPassFilter(0.75f, 0.008f));
    return filters;
  }

  private void unregisterListeners() {
    mSensorManager.unregisterListener(mSensorEventListener);
    mShakeDetector.stop();
  }

  // Public methods
  public void setFilterChain(List<? extends FloatFilter> filterChain) {
    if (filterChain == null) {
      mSensorEventListener.setFilters(new ArrayList<FloatFilter>());
    } else {
      mSensorEventListener.setFilters(filterChain);
    }
  }

  public void calibrate() {
    final float[] calibrationOffsets = Arrays.copyOf(mSensorValues, 3);
    calibrationOffsets[2] -= SensorManager.GRAVITY_EARTH;
    CalibrationManager.getInstance().storeCalibration(getActivity(), calibrationOffsets);

    mSensorManager.unregisterListener(mSensorEventListener);
    ObjectAnimator animator = ObjectAnimator.ofFloat(this, "tilt", mLevelViewPosition.getTilt(), 0);
    animator.addListener(new Animator.AnimatorListener() {
      @Override
      public void onAnimationStart(Animator animator) {
      }

      @Override
      public void onAnimationEnd(Animator animator) {
        setFilterChain(getDefaultFilters(calibrationOffsets));
        registerListeners();
      }

      @Override
      public void onAnimationCancel(Animator animator) {
      }

      @Override
      public void onAnimationRepeat(Animator animator) {
      }
    });
    animator.start();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mLevelViewPosition = new DevicePosition(0, 0);
    mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
    mShakeDetector = new ShakeDetector(new ShakeDetector.Listener() {
      @Override
      public void hearShake() {
        CalibrationManager.getInstance().clearCalibration(getActivity());
        setFilterChain(getDefaultFilters(new float[3]));
      }
    });
  }

  private void logFloatValues(float[] values) {
    for (int i = 0; i < 3; i++) {
        Log.d("TAG", "" + (char) (i + 'x') + ": " + values[i]);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_level, container);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mBullsEyeLevelView = (BullsEyeLevelView) view.findViewById(R.id.view_bulls_eye_level);
    mHorizonLevelView = (HorizonLevelView) view.findViewById(R.id.view_horizon_level);
    ColorSet cs = ColorSet.globalColorSet(getActivity());
    mBullsEyeLevelView.setColorSet(cs);
    mHorizonLevelView.setColorSet(ColorSet.copyOf(cs));
  }

  @Override
  public void onResume() {
    super.onResume();
    float[] calibration = new float[3];
    CalibrationManager.getInstance().loadCalibration(getActivity(), calibration);
    setFilterChain(getDefaultFilters(calibration));
    registerListeners();
  }

  @Override
  public void onPause() {
    super.onPause();
    unregisterListeners();
  }
}
