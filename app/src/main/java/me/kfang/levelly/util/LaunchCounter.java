package me.kfang.levelly.util;

import android.content.SharedPreferences;

/**
 * Created by kfang on 14-4-7.
 */
public class LaunchCounter {

  private static final LaunchCounter INSTANCE = new LaunchCounter();
  private final String PREF_NAMESPACE = "LAUNCH_COUNTER_";
  private final String PREF_COUNTER = "count";
  private final String PREF_COUNTER_NAME = PREF_NAMESPACE + PREF_COUNTER;

  private int count;

  private LaunchCounter() { }

  public static LaunchCounter getInstance() {
    return INSTANCE;
  }

  public int getCount(SharedPreferences prefs) {
    if (count == 0) {
      count = prefs.getInt(PREF_COUNTER_NAME, 0);
    }
    return count;
  }

  public int increment(SharedPreferences prefs) {
    count = getCount(prefs) + 1;
    prefs.edit().putInt(PREF_COUNTER_NAME, count).commit();
    return count;
  }

}
