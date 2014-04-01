package me.kfang.levelly.app;

import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Singleton for managing app-wide custom toasts.
 */
public class ToastManager {

  private static ToastManager sInstance = new ToastManager();
  private static int sToastId = 1;

  private ToastState mCurrentToast;
  private LinkedList<ToastState> mToastQueue;

  private SlidingToastFragment.SlideOutListener mSlideOutListener =
      new SlidingToastFragment.SlideOutListener() {
    @Override
    public void onSlideOut(SlidingToastFragment fragment) {
      mCurrentToast = null;
      showNextToast();
    }
  };

  private class ToastState {
    private int mId;
    private FragmentManager mFragmentManager;
    private SlidingToastFragment.OnCreateViewListener mListener;
    private int mLayoutParent;
    private long mAutoHideMillis;
    private SlidingToastFragment mToastFragment;
    private long mDisplayTime;

    private ToastState(
        int id,
        FragmentManager fragmentManager,
        SlidingToastFragment.OnCreateViewListener onCreateViewListener,
        int layoutParent,
        long autoHideMillis) {
      mId = id;
      mFragmentManager = fragmentManager;
      mListener = onCreateViewListener;
      mLayoutParent = layoutParent;
      mAutoHideMillis = autoHideMillis;
      mToastFragment = new SlidingToastFragment(mListener);
      mDisplayTime = -1;
    }

    private ToastState(ToastState other) {
      mId = other.mId;
      mFragmentManager = other.mFragmentManager;
      mListener = other.mListener;
      mLayoutParent = other.mLayoutParent;
      mToastFragment = new SlidingToastFragment(mListener);
      mDisplayTime = other.mDisplayTime;
      if (other.mAutoHideMillis != 0) {
        mAutoHideMillis = other.mAutoHideMillis - (System.currentTimeMillis() - mDisplayTime);
      }
    }

    public int getId() {
      return mId;
    }

    public SlidingToastFragment getToastFragment() {
      return mToastFragment;
    }

    public FragmentManager getFragmentManager() {
      return mFragmentManager;
    }

    public SlidingToastFragment.OnCreateViewListener getListener() {
      return mListener;
    }

    public int getLayoutParent() {
      return mLayoutParent;
    }

    public long getAutoHide() {
      return mAutoHideMillis;
    }

    public long getDisplayTime() {
      return mDisplayTime;
    }

    public void setDisplayTime(long displayTime) {
      mDisplayTime = displayTime;
    }

    public boolean hasShown() {
      return !mToastFragment.isShowing() && mDisplayTime != -1;
    }
  }

  private class SimpleToastCreateViewListener implements SlidingToastFragment.OnCreateViewListener {

    private int mImgResId;
    private int mStringResId;

    private SimpleToastCreateViewListener(int imgResId, int stringResId) {
      mImgResId = imgResId;
      mStringResId = stringResId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View contentView = inflater.inflate(R.layout.toast_confirmation, container);
      TextView textView = (TextView) contentView.findViewById(R.id.text_right);
      textView.setText(mStringResId);
      ImageView imageView = (ImageView) contentView.findViewById(R.id.image_left);
      imageView.setImageResource(mImgResId);
      return contentView;
    }
  }

  private class OrientationChangeListener implements OrientationManager.OrientationChangeListener {
    @Override
    public void onOrientationChanged(int oldOrientation, int newOrientation) {
      if (mCurrentToast != null && mCurrentToast.getToastFragment().isShowing()) {
        long now = System.currentTimeMillis();
        if (mCurrentToast.getAutoHide() != 0
            && (now - mCurrentToast.getDisplayTime() > mCurrentToast.getAutoHide() - 1000)) {
          hideToast(mCurrentToast.getId());
        } else {
          ToastState newTs = new ToastState(mCurrentToast);
          mToastQueue.add(0, newTs);
          SlidingToastFragment currentToastFragment = mCurrentToast.getToastFragment();
          currentToastFragment.setSlideOutListener(null);
          currentToastFragment.fadeOut(mCurrentToast.getFragmentManager());
          mCurrentToast = null;
          showNextToast(false);
        }
      }
    }
  }

  private ToastManager() {
    OrientationManager.getInstance().addOrientationChangeListener(new OrientationChangeListener());
    mToastQueue = new LinkedList<ToastState>();
  }

  public static ToastManager getInstance() {
    return sInstance;
  }

  public int showSimpleToast(
      final FragmentManager fragmentManager,
      final int layoutParent,
      final int imgResId,
      final int stringId) {
    return showToast(
        fragmentManager,
        layoutParent,
        3000,
        new SimpleToastCreateViewListener(imgResId, stringId));
  }

  public int showToast(
      final FragmentManager fragmentManager,
      final int layoutParent,
      final long autoHideMillis,
      SlidingToastFragment.OnCreateViewListener listener) {

    ToastState ts =
        new ToastState(sToastId, fragmentManager, listener, layoutParent, autoHideMillis);
    sToastId++;

    mToastQueue.add(ts);

    if (mCurrentToast == null) {
      showNextToast();
    }

    return ts.getId();
  }

  public void hideAllToasts() {
    mToastQueue.clear();
    if (mCurrentToast != null) {
      hideToast(mCurrentToast.getId());
    }
  }

  public void clearToasts() {
    mToastQueue.clear();
    if (mCurrentToast != null) {
      mCurrentToast.getToastFragment().removeToast();
      mCurrentToast = null;
    }
  }

  public boolean hideToast(int toastId) {
    if (mCurrentToast == null) {
      return false;
    }
    if (mCurrentToast.getId() == toastId && mCurrentToast.hasShown()) {
      mCurrentToast = null;
      return true;
    }
    if (mCurrentToast.getId() == toastId) {
      mCurrentToast.getToastFragment().slideOut(mCurrentToast.getFragmentManager());
      return true;
    } else {
      // Otherwise skip this toast if it's in the queue
      Iterator<ToastState> it = mToastQueue.iterator();
      while (it.hasNext()) {
        ToastState ts = it.next();
        if (ts.getId() == toastId) {
          it.remove();
          return true;
        }
      }
    }
    return false;
  }

  private void showNextToast() {
    showNextToast(true);
  }

  private void showNextToast(boolean slide) {
    if (!mToastQueue.isEmpty()
        && (mCurrentToast == null
        || mCurrentToast.hasShown())) {
      ToastState ts = mToastQueue.remove();
      SlidingToastFragment toastFragment = ts.getToastFragment();
      toastFragment.setSlideOutListener(mSlideOutListener);
      mCurrentToast = ts;
      if (slide) {
        mCurrentToast.setDisplayTime(System.currentTimeMillis());
        toastFragment.slideIn(ts.getFragmentManager(), ts.getLayoutParent(), ts.getAutoHide());
      } else {
        toastFragment.fadeIn(ts.getFragmentManager(), ts.getLayoutParent(), ts.getAutoHide());
      }
    } else if (mToastQueue.isEmpty()
        && mCurrentToast != null
        && mCurrentToast.hasShown()) {
      mCurrentToast = null;
    }
  }
}
