package com.kxfang.level.app;

import android.app.Fragment;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;

import com.kxfang.level.app.filter.FloatFilter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Fragment encapsulating the main view of the Level.
 */
public class LevelFragment extends Fragment implements SurfaceHolder.Callback {

  private static final String TAG = "LevelFragment";

  // Surface states
  private volatile boolean mSurfaceCreated = false;
  private volatile boolean mSurfaceDestroyed = false;

  private SensorManager mSensorManager;
  private LevelSurfaceView mLevelSurfaceView;

  // List of filters for sensor data to pass through
  private volatile List<FloatFilter> mFilterChain;

  // SensorEventListener
  private SensorEventListener mSensorEventListener = new SensorEventListener() {
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
      if (mSurfaceCreated && !mSurfaceDestroyed) {
        float [] filteredValues = Arrays.copyOf(sensorEvent.values, sensorEvent.values.length);

        // Copy the filter chain reference in case it changes
        List<FloatFilter> filterChain = mFilterChain;

        for (int i = 0; i < sensorEvent.values.length; i++) {
          for (FloatFilter filter : filterChain) {
            filteredValues[i] = filter.next(filteredValues[i]);
          }
        }

        mLevelSurfaceView.render(filteredValues);
      }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
      // Do nothing
    }
  };

  // Background thread that listens to sensor events
  private HandlerThread mSensorThread;
  private Handler mSensorEventHandler;


  // Public methods
  public void setFilterChain(List<FloatFilter> filterChain) {
    if (filterChain == null) {
      mFilterChain = Collections.emptyList();
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (mFilterChain == null) {
      mFilterChain = Collections.emptyList();
    }

    mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

    mSensorThread = new HandlerThread("sensorThread");
    mSensorThread.start();
    mSensorEventHandler = new Handler(mSensorThread.getLooper());
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_level, container);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mLevelSurfaceView = (LevelSurfaceView) view.findViewById(R.id.view_level_surface);
    mLevelSurfaceView.getHolder().addCallback(this);
  }

  @Override
  public void onResume() {
    super.onResume();
    mSensorManager.registerListener(
        mSensorEventListener,
        mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
        SensorManager.SENSOR_DELAY_GAME,
        mSensorEventHandler);
    Log.d(TAG, "Registered listener");
  }

  @Override
  public void onPause() {
    super.onPause();
    mSensorManager.unregisterListener(mSensorEventListener);
  }

  // SurfaceHolder.Callback implementation
  @Override
  public void surfaceCreated(SurfaceHolder surfaceHolder) {
    mSurfaceCreated = true;
  }

  @Override
  public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
    // Do nothing
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    mSurfaceDestroyed = true;
  }
}
