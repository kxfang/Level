package me.kfang.levelly.app;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages the calibration of the sensor.
 */
public class CalibrationManager {
  private static final String PREFERENCES_FILENAME = "calibration.prefs";
  private static final String FLAT_PREFIX = "flat_";
  private static final String HORIZONTAL_PREFIX = "horizon_";
  private static final CalibrationManager INSTANCE = new CalibrationManager();

  private Map<String, Float> mCalibrationPrefsMap;

  private CalibrationManager() {
    mCalibrationPrefsMap = new HashMap<String, Float>();
  }

  public static CalibrationManager getInstance() {
    return INSTANCE;
  }

  private void storeCalibration(Context context, float[] values, boolean horizontal) {
    String prefix = horizontal ? HORIZONTAL_PREFIX : FLAT_PREFIX;
    for (int i = 0; i < values.length; i++) {
      mCalibrationPrefsMap.put(prefix + i, values[i]);
    }
  }

  private void loadCalibration(Context context, float[] values, boolean horizontal) {
    String prefix = horizontal ? HORIZONTAL_PREFIX : FLAT_PREFIX;
    SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_FILENAME, 0);
    for (int i = 0; i < values.length; i++) {
      values[i] = prefs.getFloat(prefix + i, 0);
    }
  }

  public void storeHorizontalCalibration(Context context, float value) {
    float[] v = { value };
    storeCalibration(context, v, true);
  }

  public float loadHorizontalCalibration(Context context) {
    float[] v = new float[1];
    loadCalibration(context, v, true);
    return v[0];
  }

  public void storeFlatCalibration(Context context, float[] values) {
    storeCalibration(context, values, false);
  }

  public void loadFlatCalibration(Context context, float[] values) {
    loadCalibration(context, values, false);
  }

  public void clearCalibration(Context context) {
    mCalibrationPrefsMap.clear();
    context
        .getSharedPreferences(PREFERENCES_FILENAME, 0)
        .edit()
        .clear()
        .commit();
  }

  public void commit(Context context) {
    SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES_FILENAME, 0).edit();
    for (Map.Entry<String, Float> entry : mCalibrationPrefsMap.entrySet()) {
      editor.putFloat(entry.getKey(), entry.getValue());
    }
    editor.commit();
  }
}

