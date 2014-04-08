package me.kfang.levelly.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import me.kfang.levelly.color.ColorSet;
import me.kfang.levelly.util.AppRater;
import me.kfang.levelly.util.LaunchCounter;

public class MainActivity extends Activity {

  private Button mScreenOverlayButton;

  private LevelFragment mLevelFragment;

  private int mCalibrationToastId;
  private boolean mUiVisible = true;
  private boolean mShouldHideUi;
  private boolean mCalibrating = false;

  private boolean mSupported = true;

  private Handler mHandler;

  private class CalibrationViewListener implements SlidingToastFragment.OnCreateViewListener {

    private int mStringResId;
    private LevelFragment.CalibrationType mType;

    private CalibrationViewListener(int stringResId, LevelFragment.CalibrationType calibrationType) {
      mStringResId = stringResId;
      mType = calibrationType;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View calibrationView = inflater.inflate(R.layout.toast_text_button, container, true);

      Button calibrationButton = (Button) calibrationView.findViewById(R.id.button_right);
      calibrationButton.setText(R.string.calibrate_button);

      calibrationButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          ToastManager.getInstance().hideToast(mCalibrationToastId);
          ToastManager.getInstance().showSimpleToast(
              getFragmentManager(),
              R.id.main_layout,
              R.drawable.ic_confirmation,
              R.string.calibrate_confirm);
          mLevelFragment.calibrate(mType);
          enableUiAutoHide();
          hideUi();
          mCalibrating = false;
        }
      });

      TextView calibrationText = (TextView) calibrationView.findViewById(R.id.text_left);
      calibrationText.setText(mStringResId);
      return calibrationView;
    }
  };

  private Runnable mHideUiRunnable = new Runnable() {
    @Override
    public void run() {
      if (mShouldHideUi) {
        hideUi();
      }
    }
  };

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public void onPanelClosed(int featureId, Menu menu) {
    super.onPanelClosed(featureId, menu);
    enableUiAutoHide();
    if (mUiVisible && mShouldHideUi && !mCalibrating) {
      mHandler.removeCallbacks(mHideUiRunnable);
      mHandler.postDelayed(mHideUiRunnable, 2000);
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if (id == R.id.action_calibrate_reset) {
      resetCalibration();
    } else if (id == R.id.action_settings) {
      Intent intent = new Intent(this, SettingsActivity.class);
      startActivity(intent);
    } else if (id == R.id.action_calibrate_bullseye || id == R.id.action_calibrate_horizon) {
      int stringResId;
      LevelFragment.CalibrationType type;
      if (id == R.id.action_calibrate_bullseye) {
        stringResId = R.string.calibrate_surface;
        type = LevelFragment.CalibrationType.SURFACE;
      } else {
        stringResId = R.string.calibrate_horizon;
        type = LevelFragment.CalibrationType.HORIZON;
      }

      ToastManager.getInstance().hideToast(mCalibrationToastId);
      mCalibrationToastId = ToastManager.getInstance().showToast(
          getFragmentManager(),
          R.id.main_layout,
          0,
          new CalibrationViewListener(stringResId, type));
      mCalibrating = true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public boolean onMenuOpened(int featureId, Menu menu) {
    disableUiAutoHide();
    return super.onMenuOpened(featureId, menu);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    AppRater.getInstance().notifyLaunch(prefs);

    SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    if (sensorManager.getSensorList(Sensor.TYPE_GRAVITY).isEmpty()) {
      mSupported = false;
      setContentView(R.layout.activity_main_no_sensor);
      return;
    }

    setContentView(R.layout.activity_main);
    findViewById(R.id.main_layout).setBackgroundColor(ColorSet.globalColorSet(this).getSecondaryColor());

    mHandler = new Handler(Looper.getMainLooper());

    mLevelFragment = (LevelFragment) getFragmentManager().findFragmentById(R.id.level_fragment);

    mScreenOverlayButton = (Button) findViewById(R.id.button_screen_overlay);
    mScreenOverlayButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (ToastManager.getInstance().hideToast(mCalibrationToastId)) {
          mCalibrating = false;
          hideUi();
        } else {
          toggleUi();
        }
      }
    });
  }

  private void resetCalibration() {
    new AlertDialog.Builder(MainActivity.this)
        .setMessage(R.string.reset_confirmation_message)
        .setTitle(R.string.reset_confirmation_title)
        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            ToastManager.getInstance().showSimpleToast(
                getFragmentManager(),
                R.id.main_layout,
                R.drawable.ic_confirmation,
                R.string.calibrate_restore);
            mLevelFragment.clearCalibration();
          }
        })
        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            // Dismiss
          }
        })
        .create()
        .show();
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (!mSupported) {
      if (getActionBar() != null) {
        getActionBar().hide();
      }
      return;
    }

    showUi();

    enableUiAutoHide();
    mHandler.postDelayed(mHideUiRunnable, 1500);

    getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(
        new View.OnSystemUiVisibilityChangeListener() {
      @Override
      public void onSystemUiVisibilityChange(int i) {
        if ((i & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
          mHandler.postDelayed(mHideUiRunnable, 3000);
          mUiVisible = true;
        } else {
          mHandler.removeCallbacks(mHideUiRunnable);
          mUiVisible = false;
        }
      }
    });
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (mSupported) {
      disableUiAutoHide();
      ToastManager.getInstance().clearToasts();
    }
  }

  private void toggleUi() {
    if (mUiVisible) {
      hideUi();
    } else {
      showUi();
    }
  }

  private void hideUi() {
    int hideUiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
        | View.SYSTEM_UI_FLAG_FULLSCREEN; // hide status bar

    getWindow().getDecorView().setSystemUiVisibility(hideUiFlags);
  }

  private void showUi() {
    int showUiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
    getWindow().getDecorView().setSystemUiVisibility(showUiFlags);
  }

  private void disableUiAutoHide() {
    mShouldHideUi = false;
    mHandler.removeCallbacks(mHideUiRunnable);
  }

  private void enableUiAutoHide() {
    mShouldHideUi = true;
  }
}
