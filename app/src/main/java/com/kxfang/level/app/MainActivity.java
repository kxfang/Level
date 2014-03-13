package com.kxfang.level.app;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

  private Button mScreenOverlayButton;
  private BullsEyeCalibrationFragment mCalibrationFragment;
  private LevelFragment mLevelFragment;

  private boolean mCalibrating;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mLevelFragment = (LevelFragment) getFragmentManager().findFragmentById(R.id.level_fragment);

    mCalibrationFragment = new BullsEyeCalibrationFragment();
    mCalibrationFragment.setCalibrationListener(
        new BullsEyeCalibrationFragment.CalibrationListener() {
          @Override
          public void onCalibrate() {
            getFragmentManager().popBackStack();
            mLevelFragment.calibrate();
          }
        });

    mScreenOverlayButton = (Button) findViewById(R.id.button_screen_overlay);
    mScreenOverlayButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (mCalibrating) {
          mCalibrating = false;
          getFragmentManager().popBackStack();
        } else {
          mCalibrating = true;
          FragmentTransaction transaction = getFragmentManager().beginTransaction();
          transaction.setCustomAnimations(R.animator.slide_in_y, 0, 0, R.animator.slide_out_y);
          transaction.add(R.id.main_layout, mCalibrationFragment);
          transaction.addToBackStack(null);
          transaction.commit();
        }
      }
    });
  }
}
