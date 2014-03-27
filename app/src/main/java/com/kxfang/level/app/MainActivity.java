package com.kxfang.level.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.squareup.seismic.ShakeDetector;

public class MainActivity extends Activity {

  private Button mScreenOverlayButton;

  // Calibration view
  private SlidingToastFragment mCalibrationFragment;
  private View mCalibrationView;
  private Button mCalibrationButton;

  // Confirmation view
  private SlidingToastFragment mConfirmationFragment;
  private TextView mConfirmationText;

  private LevelFragment mLevelFragment;

  private ShakeDetector mShakeDetector;

  private SensorManager mSensorManager;

  private OrientationEventListener mOrientationEventListener;

  private boolean mShowConfirmation;
  private int mCalibrationToastId;
  private boolean mUiVisible = true;
  private boolean mShouldHideUi;
  private boolean mCalibrating = false;

  private Handler mHandler;

  private class CalibrationViewListener implements SlidingToastFragment.OnCreateViewListener {

    private int mStringResId;

    private CalibrationViewListener(int stringResId) {
      mStringResId = stringResId;
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
          mLevelFragment.calibrate();
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
    switch (item.getItemId()) {
      case R.id.action_calibrate_bullseye:
      case R.id.action_calibrate_horizon:
        ToastManager.getInstance().hideToast(mCalibrationToastId);
        mCalibrationToastId = ToastManager.getInstance().showToast(
            getFragmentManager(),
            R.id.main_layout,
            0,
            new CalibrationViewListener(R.string.calibration));
        mCalibrating = true;
        break;
      case R.id.action_calibrate_reset:
        resetCalibration();
        break;
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
    setContentView(R.layout.activity_main);

    mHandler = new Handler(Looper.getMainLooper());

    mLevelFragment = (LevelFragment) getFragmentManager().findFragmentById(R.id.level_fragment);
    mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

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

    // TODO: Use raw sensor data for determining which view to calibrate
    mShakeDetector = new ShakeDetector(new ShakeDetector.Listener() {
      @Override
      public void hearShake() {
        resetCalibration();
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
    enableUiAutoHide();
    mShakeDetector.start(mSensorManager);

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
    mShakeDetector.stop();
    disableUiAutoHide();
    ToastManager.getInstance().clearToasts();
  }

  private void toggleUi() {
    if (mUiVisible) {
      hideUi();
    } else {
      showUi(4000);
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

  private void showUi(long timeOut) {
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
