package me.kfang.levelly.app;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by kfang on 14-3-31.
 */
public class SettingsFragment extends PreferenceFragment {

  public static final String PREF_SHOW_DECIMAL = "pref_show_decimal";
  public static final String PREF_SHOW_INCLINE = "pref_show_incline";
  public static final String PREF_SHOW_AXIS_INCLINATION = "pref_show_axis_inclination";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);
  }
}
