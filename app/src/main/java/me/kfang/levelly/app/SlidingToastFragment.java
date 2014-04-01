package me.kfang.levelly.app;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;

/**
 * A fragment that slides in to display information.
 */
public class SlidingToastFragment extends Fragment {

  private OnCreateViewListener mOnCreateViewListener;

  private boolean mShowing;

  private SlideableFrameLayout mFrameLayout;

  private Handler mHandler;
  private Runnable mAutoHideRunnable = new Runnable() {
    @Override
    public void run() {
      if (isShowing()) {
        slideOut(getActivity().getFragmentManager());
      }
    }
  };

  public interface SlideOutListener {
    public void onSlideOut(SlidingToastFragment fragment);
  }

  private SlideOutListener mSlideOutListener;

  public SlidingToastFragment(OnCreateViewListener onCreateViewListener) {
    mOnCreateViewListener = onCreateViewListener;
  }

  public interface OnCreateViewListener {
    public View onCreateView(
        LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    SlideableFrameLayout v =
        (SlideableFrameLayout) inflater.inflate(R.layout.fragment_banner, container, false);
    mOnCreateViewListener.onCreateView(inflater, v, savedInstanceState);

    int currentOrientation = OrientationManager.getInstance().getCurrentOrientation();
    v.setRotation(getRotation(currentOrientation));
    v.setOrientation(currentOrientation);
    mFrameLayout = v;
    return v;
  }

  @Override
  public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
    if (nextAnim == 0) {
      return super.onCreateAnimator(transit, enter, nextAnim);
    }
    Animator animator = AnimatorInflater.loadAnimator(getActivity(), nextAnim);
    if (nextAnim == R.animator.slide_out && animator != null) {
      animator.addListener(new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
          if (mSlideOutListener != null) {
            mSlideOutListener.onSlideOut(SlidingToastFragment.this);
          }
        }
      });
    } else if (nextAnim == R.animator.fade_in) {
      mFrameLayout.setSlideIn(false);
    }
    if (animator != null
        && (nextAnim == R.animator.fade_out || nextAnim == R.animator.slide_out)) {
      animator.addListener(new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
          mShowing = false;
        }
      });
    }
    return animator;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
  }

  public void setSlideOutListener(SlideOutListener slideOutListener) {
    mSlideOutListener = slideOutListener;
  }

  public void slideIn(final FragmentManager manager, int layoutParent, long autoHideMillis) {
    addWithAnimation(manager, layoutParent, autoHideMillis, R.animator.slide_in);
  }

  public void fadeIn(final FragmentManager manager, int layoutParent, long autoHideMillis) {
    addWithAnimation(manager, layoutParent, autoHideMillis, R.animator.fade_in);
  }

  private void addWithAnimation(
      final FragmentManager manager,
      int layoutParent,
      long autoHideMillis,
      int animatorId) {
    FragmentTransaction transaction = manager.beginTransaction();
    if (animatorId != 0) {
      transaction.setCustomAnimations(animatorId, 0, 0, 0);
    }
    transaction.add(layoutParent, this);
    transaction.commit();
    mShowing = true;

    if (mHandler == null) {
      mHandler = new Handler(Looper.getMainLooper());
    }
    if (autoHideMillis != 0) {
      mHandler.postDelayed(mAutoHideRunnable, autoHideMillis);
    }
  }

  // TODO: prevent from running when activity no longer valid
  public void slideOut(FragmentManager manager) {
    removeWithAnimation(manager, R.animator.slide_out);
  }

  public void removeToast() {
    removeWithAnimation(getFragmentManager(), 0);
  }

  private void removeWithAnimation(FragmentManager manager, int animatorId) {
    mHandler.removeCallbacks(mAutoHideRunnable);
    FragmentTransaction transaction = manager.beginTransaction();
    if (animatorId != 0) {
      transaction.setCustomAnimations(0, animatorId, 0, 0);
    } else {
      mShowing = false;
    }
    transaction.remove(this);
    transaction.commit();
  }

  public void fadeOut(FragmentManager manager) {
    removeWithAnimation(manager, R.animator.fade_out);
  }

  public boolean isShowing() {
    return mShowing;
  }

  private int getRotation(int orientation) {
    switch (orientation) {
      case Surface.ROTATION_90:
        return 90;
      case Surface.ROTATION_180:
        return 180;
      case Surface.ROTATION_270:
        return 270;
      default:
        return 0;
    }
  }
}
