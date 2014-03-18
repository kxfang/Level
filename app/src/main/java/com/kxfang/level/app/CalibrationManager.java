package com.kxfang.level.app;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages the calibration of the sensor.
 */
public class CalibrationManager {
  private static final String PREFERENCES_FILENAME = "calibration.prefs";
  private static final String FLAT_PREFIX = "flat_";
  private static final String HORIZONTAL_PREFIX = "horizon_"
  private static final CalibrationManager INSTANCE = new CalibrationManager();

  private CalibrationManager() { }

  public static CalibrationManager getInstance() {
    return INSTANCE;
  }

  private void storeCalibration(Context context, float[] values, boolean horizontal) {
    String prefix = horizontal ? HORIZONTAL_PREFIX : FLAT_PREFIX;
    SharedPreferences.Editor prefs = context.getSharedPreferences(PREFERENCES_FILENAME, 0).edit();
    for (int i = 0; i < values.length; i++) {
      prefs.putFloat(prefix + i, values[i]);
    }
    prefs.commit();
  }

  private void loadCalibration(Context context, float[] values, boolean horizontal) {
    String prefix = horizontal ? HORIZONTAL_PREFIX : FLAT_PREFIX;
    SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_FILENAME, 0);
    for (int i = 0; i < values.length; i++) {
      values[i] = prefs.getFloat(prefix + i, 0);
    }
  }

  public void storeHorizontalCalibration(Context context, float[] values) {
    storeCalibration(context, values, true);
  }

  public void loadHorizontalCalibration(Context context, float[] values) {
    loadCalibration(context, values, true);
  }

  public void storeFlatCalibration(Context context, float[] values) {
    storeCalibration(context, values, false);
  }

  public void loadFlatCalibration(Context context, float[] values) {
    loadCalibration(context, values, false);
  }

  public void clearCalibration(Context context) {
    context
        .getSharedPreferences(PREFERENCES_FILENAME, 0)
        .edit()
        .clear()
        .commit();
  }
}

