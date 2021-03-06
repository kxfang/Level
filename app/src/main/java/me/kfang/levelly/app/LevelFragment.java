package me.kfang.levelly.app;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import me.kfang.levelly.color.ColorSet;
import me.kfang.levelly.filter.CalibrationFilter;
import me.kfang.levelly.filter.FloatFilter;
import me.kfang.levelly.filter.LowPassFilter;
import me.kfang.levelly.filter.ThresholdLowPassFilter;

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

  private DevicePosition mLevelViewPosition;

  private float[] mSensorValues = new float[3];
  private float mRotationCalibrationOffset;

  private LevelView.Config mLevelViewConfig;

  public enum CalibrationType {
    SURFACE,
    HORIZON
  }

  // SensorEventListener
  private SensorFilter mSensorEventListener = new SensorFilter(new SensorFilter.Listener() {
    @Override
    public void onSensorChanged(SensorEvent sensorEvent, float[] filteredValues) {
      for (int i = 0; i < mSensorValues.length; i++) {
        mSensorValues[i] = sensorEvent.values[i];
      }
      OrientationManager.getInstance().setCurrentPosition(
          OrientationManager.getDeviceTilt(mSensorValues[0], mSensorValues[1], mSensorValues[2]),
          OrientationManager.getRotationDegrees(mSensorValues[0], mSensorValues[1]));
      setPosition(
          OrientationManager.getDeviceTilt(filteredValues[0], filteredValues[1], filteredValues[2]),
          OrientationManager.getRotationDegrees(filteredValues[0], filteredValues[1]),
          OrientationManager.getXTilt(filteredValues[0], filteredValues[1], filteredValues[2]),
          OrientationManager.getYTilt(filteredValues[0], filteredValues[1], filteredValues[2]));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
      // Do nothing
    }
  }, new ArrayList<FloatFilter>());

  private void setPosition(float deviceTilt, float rotation, float xTilt, float yTilt) {
    if (deviceTilt > 34.5f
        && deviceTilt < 144.5f) {
      setActiveLevelView(mHorizonLevelView);
    } else if (deviceTilt < 35.5f) {
      mBullsEyeLevelView.setConfig(BullsEyeLevelView.Config.DOWN);
      setActiveLevelView(mBullsEyeLevelView);
    } else if (deviceTilt > 145.5f) {
      mBullsEyeLevelView.setConfig(BullsEyeLevelView.Config.UP);
      setActiveLevelView(mBullsEyeLevelView);
    }

    if (mActiveLevelView == mHorizonLevelView) {
      mLevelViewPosition.setRotation(rotation + mRotationCalibrationOffset);
    } else {
      mLevelViewPosition.setRotation(rotation);
    }
    mLevelViewPosition.setTilt(deviceTilt);
    mLevelViewPosition.setXTilt(xTilt);
    mLevelViewPosition.setYTilt(yTilt);
    mActiveLevelView.setPosition(mLevelViewPosition);
  }

  private void setTilt(float deviceTilt) {
    setPosition(deviceTilt, mLevelViewPosition.getRotation(), 0, 0);
  }

  private void setRotation(float rotation) {
    setPosition(mLevelViewPosition.getTilt(), rotation, 0, 0);
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
  }

  private List<FloatFilter> getDefaultFilters(float[] calibrationValues) {
    List<FloatFilter> filters = new LinkedList<FloatFilter>();
    filters.add(CalibrationFilter.withOffsets(calibrationValues));
    filters.add(new ThresholdLowPassFilter(0.75f, 0.008f));
    return filters;
  }

  private void unregisterListeners() {
    mSensorManager.unregisterListener(mSensorEventListener);
  }

  // Public methods
  public void setFilterChain(List<? extends FloatFilter> filterChain) {
    if (filterChain == null) {
      mSensorEventListener.setFilters(new ArrayList<FloatFilter>());
    } else {
      mSensorEventListener.setFilters(filterChain);
    }
  }

  public void calibrate(final CalibrationType type) {
    final float[] calibrationOffsets = Arrays.copyOf(mSensorValues, 3);
    final float horizontalRotationOffset;
    float deviceRotation =
        OrientationManager.getRotationDegrees(calibrationOffsets[0], calibrationOffsets[1]);
    ObjectAnimator animator;
    if (type == CalibrationType.SURFACE) {
      CalibrationManager.getInstance().storeFlatCalibration(getActivity(), calibrationOffsets);
      animator = ObjectAnimator.ofFloat(this, "tilt", mLevelViewPosition.getTilt(), 0);
      horizontalRotationOffset = mRotationCalibrationOffset;
    } else {
      horizontalRotationOffset = OrientationManager.getHorizonOffset(deviceRotation);
      CalibrationManager.getInstance().storeHorizontalCalibration(
          getActivity(), horizontalRotationOffset);

      float animateSrc;
      float animateDst;
      mRotationCalibrationOffset = 0;
      animateSrc = mLevelViewPosition.getRotation();
      animateDst = deviceRotation + horizontalRotationOffset;

      animator = ObjectAnimator.ofFloat(
          this,
          "rotation",
          animateSrc,
          animateDst);
    }

    mSensorManager.unregisterListener(mSensorEventListener);
    animator.setDuration(1000);
    animator.setInterpolator(new AccelerateDecelerateInterpolator());
    animator.addListener(new Animator.AnimatorListener() {
      @Override
      public void onAnimationStart(Animator animator) {
      }

      @Override
      public void onAnimationEnd(Animator animator) {
        if (type == CalibrationType.SURFACE) {
          setFilterChain(getDefaultFilters(calibrationOffsets));
        } else {
          mRotationCalibrationOffset = horizontalRotationOffset;
        }
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
  }

  public void clearCalibration() {
    CalibrationManager.getInstance().clearCalibration(getActivity());
    setFilterChain(getDefaultFilters(new float[3]));
    mRotationCalibrationOffset = 0;
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
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    mLevelViewConfig = new LevelView.Config(
        preferences.getBoolean(SettingsFragment.PREF_SHOW_DECIMAL, false),
        preferences.getBoolean(SettingsFragment.PREF_SHOW_INCLINE, false),
        preferences.getBoolean(SettingsFragment.PREF_SHOW_AXIS_INCLINATION, false));

    mBullsEyeLevelView.setConfig(mLevelViewConfig);
    mHorizonLevelView.setConfig(mLevelViewConfig);

    float[] calibration = new float[3];
    CalibrationManager.getInstance().loadFlatCalibration(getActivity(), calibration);
    setFilterChain(getDefaultFilters(calibration));
    mRotationCalibrationOffset =
        CalibrationManager.getInstance().loadHorizontalCalibration(getActivity());
    registerListeners();
  }

  @Override
  public void onPause() {
    super.onPause();
    unregisterListeners();
    CalibrationManager.getInstance().commit(getActivity());
  }
}
