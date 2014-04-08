package me.kfang.levelly.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;

/**
 * Module that manages showing the rate the app item in the menu.
 */
public class AppRater {
  private static final AppRater INSTANCE = new AppRater();
  private static final int LAUNCH_THRESHOLD = 12;

  private int mCount;

  private AppRater() { }

  public static AppRater getInstance() {
    return INSTANCE;
  }

  public void notifyLaunch(SharedPreferences prefs) {
    LaunchCounter.getInstance().increment(prefs);
  }

  public boolean shouldShowRateApp(SharedPreferences prefs) {
    mCount = LaunchCounter.getInstance().getCount(prefs);
    return mCount > LAUNCH_THRESHOLD;
  }

  public Intent getRateAppIntent(Context context) {
    final Uri uri = Uri.parse("market://details?id=" + context.getApplicationContext().getPackageName());
    return new Intent(Intent.ACTION_VIEW, uri);
  }
}
