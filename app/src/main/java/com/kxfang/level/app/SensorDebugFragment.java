package com.kxfang.level.app;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.TextView;

/**
 * A fragment that displays the raw sensor values on screen.
 */
public class SensorDebugFragment extends Fragment {

  private static SensorDebugFragment sInstance = null;

  private TextView mRawDataText;
  private TextView mFilteredDataText;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    if (sInstance == null) {
      sInstance = this;
    }
    return inflater.inflate(R.layout.fragment_debug, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mRawDataText = (TextView) view.findViewById(R.id.text_raw_data);
    mFilteredDataText = (TextView) view.findViewById(R.id.text_filtered_data);
  }

  public void setData(float[] raw, float[] filtered) {
    String r = "x: " + raw[0] + "\ny: " + raw[1] + "\nz: " + raw[2] + "\ntotal: " + (float)Math.sqrt(raw[0]*raw[0] + raw[1]*raw[1] + raw[2]*raw[2]);
    String f = "x: " + filtered[0] + "\ny: " + filtered[1] + "\nz: " + filtered[2] + "\ntotal: " + (float)Math.sqrt(filtered[0]*filtered[0] + filtered[1]*filtered[1] + filtered[2]*filtered[2]);
    mRawDataText.setText(r);
    mFilteredDataText.setText(f);
  }

  public static SensorDebugFragment get() {
    return sInstance;
  }
}
