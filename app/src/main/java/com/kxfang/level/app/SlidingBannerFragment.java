package com.kxfang.level.app;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.kxfang.level.app.color.ColorSet;

/**
 * A fragment that slides in to display information.
 */
public class SlidingBannerFragment extends Fragment {
  private Button mButton;
  private TextView mTextView;
  private ButtonClickListener mButtonClickListener;
  private ImageView mImageView;

  private int mDrawableId;
  private String mText;
  private String mButtonText;

  private boolean mShowing;

  public SlidingBannerFragment(int drawableId, String text, String buttonText) {
    mDrawableId = drawableId;
    mText = text;
    mButtonText = buttonText;
  }

  public interface ButtonClickListener {
    public void onClick();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_banner, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    mButton = (Button) view.findViewById(R.id.button_banner);
    mTextView = (TextView) view.findViewById(R.id.text_banner);
    mImageView = (ImageView) view.findViewById(R.id.image_banner);

    if (mDrawableId != 0) {
      mImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_done));
    }
    mTextView.setText(mText);
    if (mButtonText == null) {
      mButton.setVisibility(View.GONE);
    } else {
      mButton.setText(mButtonText);
    }
    if (mDrawableId == 0) {
      mImageView.setVisibility(View.GONE);
    }
    updateClickListener();
  }

  public void slideIn(final FragmentManager manager, int layoutParent, boolean autoHide) {
    FragmentTransaction transaction = manager.beginTransaction();
    transaction.setCustomAnimations(R.animator.slide_in_y, 0, 0, R.animator.slide_out_y);
    transaction.add(layoutParent, this);
    transaction.addToBackStack(null);
    transaction.commit();
    mShowing = true;

    if (autoHide) {
      Handler h = new Handler(Looper.getMainLooper());
      h.postDelayed(new Runnable() {
        @Override
        public void run() {
          if (isShowing()) {
            slideOut(manager);
          }
        }
      }, 3000);
    }
  }

  public void slideOut(FragmentManager manager) {
    manager.popBackStack();
    mShowing = false;
  }

  public boolean isShowing() {
    return mShowing;
  }

  public void setButtonClickListener(ButtonClickListener listener) {
    mButtonClickListener = listener;
    updateClickListener();
  }

  private void updateClickListener() {
    if (mButton == null || mButtonClickListener == null) {
      return;
    }
    mButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mButtonClickListener.onClick();
      }
    });
  }
}
