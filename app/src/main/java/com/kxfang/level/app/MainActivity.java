package com.kxfang.level.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.squareup.seismic.ShakeDetector;

public class MainActivity extends Activity {

  private Button mScreenOverlayButton;
  private SlidingBannerFragment mCalibrationFragment;
  private LevelFragment mLevelFragment;

  private SlidingBannerFragment mConfirmationFragment;

  private ShakeDetector mShakeDetector;

  private boolean mShowConfirmation;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mLevelFragment = (LevelFragment) getFragmentManager().findFragmentById(R.id.level_fragment);

    mConfirmationFragment = new SlidingBannerFragment(
        R.drawable.ic_action_done,
        getResources().getString(R.string.calibrate_confirm),
        null);

    mCalibrationFragment = new SlidingBannerFragment(
        0,
        getResources().getString(R.string.calibration),
        getResources().getString(R.string.calibrate_button));

    mCalibrationFragment.setButtonClickListener(
        new SlidingBannerFragment.ButtonClickListener() {
          @Override
          public void onClick() {
            getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
              @Override
              public void onBackStackChanged() {
                if (mShowConfirmation && !mConfirmationFragment.isShowing()) {
                  mConfirmationFragment.slideIn(getFragmentManager(), R.id.main_layout, true);
                  mShowConfirmation = false;
                  getFragmentManager().removeOnBackStackChangedListener(this);
                }
              }
            });
            mShowConfirmation = true;
            getFragmentManager().popBackStack();
            mLevelFragment.calibrate();
          }
        }
    );

    mScreenOverlayButton = (Button) findViewById(R.id.button_screen_overlay);
    mScreenOverlayButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (!mCalibrationFragment.isShowing()) {
          mCalibrationFragment.slideIn(getFragmentManager(), R.id.main_layout, false);
        } else {
          mCalibrationFragment.slideOut(getFragmentManager());
        }
      }
    });

    mShakeDetector = new ShakeDetector(new ShakeDetector.Listener() {
      @Override
      public void hearShake() {
        new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_DeviceDefault_Dialog)
            .setMessage(R.string.reset_confirmation_message)
            .setTitle(R.string.reset_confirmation_title)
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialogInterface, int i) {
                mCalibrationFragment.slideOut(getFragmentManager());
                mLevelFragment.clearCalibration();
                SlidingBannerFragment clearCalibration = new SlidingBannerFragment(
                    R.drawable.ic_action_done,
                    getResources().getString(R.string.calibrate_restore),
                    null);
                clearCalibration.slideIn(getFragmentManager(), R.id.main_layout, true);
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
    });
  }

  @Override
  protected void onResume() {
    super.onResume();
    mShakeDetector.start((SensorManager) getSystemService(SENSOR_SERVICE));
  }

  @Override
  protected void onPause() {
    super.onPause();
    mShakeDetector.stop();
  }
}
