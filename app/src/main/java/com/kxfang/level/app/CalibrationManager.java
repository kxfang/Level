package com.kxfang.level.app;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages the calibration of the sensor.
 */
public class CalibrationManager {
  private static final String PREFERENCES_FILENAME = "calibration.prefs";
  private static final CalibrationManager INSTANCE = new CalibrationManager();

  private CalibrationManager() { }

  public static CalibrationManager getInstance() {
    return INSTANCE;
  }

  public void storeCalibration(Context context, float[] values) {
    SharedPreferences.Editor prefs = context.getSharedPreferences(PREFERENCES_FILENAME, 0).edit();
    for (int i = 0; i < values.length; i++) {
      prefs.putFloat("" + i, values[i]);
    }
    prefs.commit();
  }

  public void loadCalibration(Context context, float[] values) {
    SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_FILENAME, 0);
    for (int i = 0; i < values.length; i++) {
      values[i] = prefs.getFloat("" + i, 0);
    }
  }

  public void clearCalibration(Context context) {
    context
        .getSharedPreferences(PREFERENCES_FILENAME, 0)
        .edit()
        .clear()
        .commit();
  }
}

