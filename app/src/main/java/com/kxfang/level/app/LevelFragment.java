package com.kxfang.level.app;

import android.app.Fragment;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kxfang.level.app.color.ColorSet;
import com.kxfang.level.app.filter.FloatFilter;
import com.kxfang.level.app.filter.MovingAverageFilter;

import java.util.Arrays;
import java.util.Collections;
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

  // List of filters for sensor data to pass through
  private volatile List<? extends FloatFilter> mFilterChain;

  // SensorEventListener
  private SensorEventListener mSensorEventListener = new SensorEventListener() {
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
      float [] filteredValues = Arrays.copyOf(sensorEvent.values, sensorEvent.values.length);

      // Copy the filter chain reference in case it changes
      List<? extends FloatFilter> filterChain = mFilterChain;

      for (int i = 0; i < sensorEvent.values.length; i++) {
        for (FloatFilter filter : filterChain) {
          filteredValues[i] = filter.next(filteredValues[i]);
        }
      }

      float deviceTilt = OrientationUtils.getDeviceTilt(filteredValues[2]);
      if (deviceTilt > 45.5f
          && deviceTilt < 134.5f) {
        setActiveLevelView(mHorizonLevelView);
      } else if (deviceTilt < 44.5f) {
        mBullsEyeLevelView.setConfig(BullsEyeLevelView.Config.DOWN);
        setActiveLevelView(mBullsEyeLevelView);
      } else if (deviceTilt > 135.5f) {
        mBullsEyeLevelView.setConfig(BullsEyeLevelView.Config.UP);
        setActiveLevelView(mBullsEyeLevelView);
      }
      mActiveLevelView.render(filteredValues);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
      // Do nothing
    }
  };

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

  // Public methods
  public void setFilterChain(List<? extends FloatFilter> filterChain) {
    if (filterChain == null) {
      mFilterChain = Collections.emptyList();
    } else {
      mFilterChain = filterChain;
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (mFilterChain == null) {
      mFilterChain = Collections.emptyList();
    }

    // TODO: Pass in filter chain elsewhere
    setFilterChain(Collections.singletonList(new MovingAverageFilter(10)));

    mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
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
    ColorSet cs = ColorSet.randomColorSet(getActivity());
    mBullsEyeLevelView.setColorSet(cs);
    mHorizonLevelView.setColorSet(ColorSet.copyOf(cs));
  }

  @Override
  public void onResume() {
    super.onResume();
    mSensorManager.registerListener(
        mSensorEventListener,
        mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
        SensorManager.SENSOR_DELAY_FASTEST);
  }

  @Override
  public void onPause() {
    super.onPause();
    mSensorManager.unregisterListener(mSensorEventListener);
  }
}
