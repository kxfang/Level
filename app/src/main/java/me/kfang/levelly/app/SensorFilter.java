package me.kfang.levelly.app;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import me.kfang.levelly.filter.FloatFilter;

import java.util.Arrays;
import java.util.List;

/**
 * A SensorListener decorator that passes data through a list of filters.
 */
public class SensorFilter implements SensorEventListener {

  private Listener mListener;
  private List<? extends FloatFilter> mFilters;

  public interface Listener {
    public void onSensorChanged(SensorEvent sensorEvent, float[] filteredValues);
    public void onAccuracyChanged(Sensor sensor, int i);
  }

  public SensorFilter(Listener listener, List<? extends FloatFilter> filters) {
    mListener = listener;
    mFilters = filters;
  }

  public void setFilters(List<? extends FloatFilter> filters) {
    mFilters = filters;
  }

  @Override
  public void onSensorChanged(SensorEvent sensorEvent) {
    float[] filtered = Arrays.copyOf(sensorEvent.values, sensorEvent.values.length);
    for (FloatFilter filter : mFilters) {
      filtered = filter.next(filtered);
    }
    mListener.onSensorChanged(sensorEvent, filtered);
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int i) {
    mListener.onAccuracyChanged(sensor, i);
  }
}
