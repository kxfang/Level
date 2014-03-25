package com.kxfang.level.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
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
              }
            });
      TextView calibrationText = (TextView) calibrationView.findViewById(R.id.text_left);
      calibrationText.setText(mStringResId);
      return calibrationView;
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mLevelFragment = (LevelFragment) getFragmentManager().findFragmentById(R.id.level_fragment);
    mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

    mScreenOverlayButton = (Button) findViewById(R.id.button_screen_overlay);
    mScreenOverlayButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (!ToastManager.getInstance().hideToast(mCalibrationToastId)) {
          mCalibrationToastId = ToastManager.getInstance().showToast(
              getFragmentManager(),
              R.id.main_layout,
              0,
              new CalibrationViewListener(R.string.calibration));
        }
      }
    });

    // TODO: Use raw sensor data for determining which view to calibrate
    mShakeDetector = new ShakeDetector(new ShakeDetector.Listener() {
      @Override
      public void hearShake() {
        new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_DeviceDefault_Dialog)
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
    });
  }

  @Override
  protected void onStop() {
    super.onStop();
    ToastManager.getInstance().clearToasts();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mShakeDetector.start(mSensorManager);
  }

  @Override
  protected void onPause() {
    super.onPause();
    mShakeDetector.stop();
  }
}
