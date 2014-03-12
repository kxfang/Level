package com.kxfang.level.app;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import com.kxfang.level.app.filter.FloatFilter;

import java.util.List;

/**
 * A SensorListener decorator that passes data through a list of filters.
 */
public class FilteringSensorListener implements SensorEventListener {

  private SensorEventListener mListener;
  private List<? extends FloatFilter> mFilters;

  public FilteringSensorListener(SensorEventListener listener, List<? extends FloatFilter> filters) {
    mListener = listener;
    mFilters = filters;
  }

  public void setFilters(List<? extends FloatFilter> filters) {
    mFilters = filters;
  }

  @Override
  public void onSensorChanged(SensorEvent sensorEvent) {
    for (int i = 0; i < sensorEvent.values.length; i++) {
      for (FloatFilter filter : mFilters) {
        sensorEvent.values[i] = filter.next(sensorEvent.values[i]);
      }
    }
    mListener.onSensorChanged(sensorEvent);
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int i) {
    mListener.onAccuracyChanged(sensor, i);
  }
}
