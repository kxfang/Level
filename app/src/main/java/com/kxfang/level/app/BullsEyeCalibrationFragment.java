package com.kxfang.level.app;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.kxfang.level.app.color.ColorSet;

/**
 * Fragment used for calibration of the level.
 */
public class BullsEyeCalibrationFragment extends Fragment {

  private Button mCalibrationButton;
  private CalibrationListener mCalibrationListener;

  public interface CalibrationListener {
    public void onCalibrate();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_calibration, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    view.findViewById(R.id.menu_linear_layout).setBackgroundColor(
        ColorSet.globalColorSet(getActivity()).getPrimaryColor());

    mCalibrationButton = (Button) view.findViewById(R.id.button_calibration);
    mCalibrationButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mCalibrationListener.onCalibrate();
      }
    });
  }

  public void setCalibrationListener(CalibrationListener listener) {
    mCalibrationListener = listener;
  }
}
