package com.kxfang.level.app;

import android.animation.FloatEvaluator;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;

/**
 * A RelativeLayout that enables sliding in from off-screen.
 */
public class SlideableFrameLayout extends FrameLayout {

  private FloatEvaluator mFloatEvaluator;
  private int mOrientation;
  private float mScreenWidth;
  private float mScreenHeight;
  private float mFullScreenHeight;
  private float mXStart;
  private float mXEnd;
  private float mYStart;
  private float mYEnd;
  private float mScreenMargin;
  private boolean mSlideIn;
  private Context mContext;

  public SlideableFrameLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    mFloatEvaluator = new FloatEvaluator();
    mScreenMargin = context.getResources().getDimension(R.dimen.ui_component_margin);
    mContext = context;
  }

  @Override
  protected void onSizeChanged (int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    setAlpha(0);
    updateOrientation();
  }

  public float getSlideInFraction() {
    return 1 - getY() / (getHeight() + 50);
  }

  public void setSlideInFraction(float fraction) {
    setX(mFloatEvaluator.evaluate(fraction, mXStart, mXEnd));
    setY(mFloatEvaluator.evaluate(fraction, mYStart, mYEnd));
    setAlpha(fraction);
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    updateOrientation();
  }

  public void setOrientation(int orientation) {
    mOrientation = orientation;
  }

  public void setSlideIn(boolean slideIn) {
    mSlideIn = slideIn;
  }

  private void updateOrientation() {
    DisplayMetrics displayMetrics = new DisplayMetrics();
    ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    mScreenWidth = displayMetrics.widthPixels;
    mScreenHeight = displayMetrics.heightPixels;

    View parent = (View) getParent();
    if (parent != null) {
      mFullScreenHeight = parent.getHeight();
    } else {
      mFullScreenHeight = mScreenHeight;
    }

// Coordinates for making toast appear on top of screen
//    mXStart = mScreenWidth / 2 - getWidth() / 2;
//    mYStart = -1 * getHeight();
//    mXEnd = mXStart;
//    mYEnd = mYStart + getHeight() + mScreenMargin;
//
//    switch (mOrientation) {
//      case Surface.ROTATION_90:
//        mXStart = mScreenWidth + getHeight();
//        mXEnd = mScreenWidth - mScreenMargin;
//        mYStart = mScreenHeight / 2 - getWidth() / 2;
//        mYEnd = mYStart;
//        break;
//      case Surface.ROTATION_180:
//        mXStart = mXStart + getWidth();
//        mXEnd = mXStart;
//        mYStart = mScreenHeight + getHeight();
//        mYEnd = mScreenHeight - mScreenMargin;
//        break;
//      case Surface.ROTATION_270:
//        mXStart = mXStart - getHeight() - mScreenMargin;
//        mYStart = mScreenHeight / 2 + getWidth() / 2;
//        mYEnd = mYStart;
//        break;
//    }

    float translation = getHeight() + mScreenMargin;
    mXStart = mScreenWidth / 2 - getWidth() / 2;
    mYStart = mScreenHeight;
    mXEnd = mXStart;
    mYEnd = mYStart - translation;

    switch (mOrientation) {
      case Surface.ROTATION_90:
        mXStart = 0;
        mXEnd = translation;
        mYStart = mFullScreenHeight / 2 - getWidth() / 2;
        mYEnd = mYStart;
        break;
      case Surface.ROTATION_180:
        mXStart = mXStart + getWidth();
        mXEnd = mXStart;
        mYStart = mScreenHeight + getHeight();
        mYEnd = mScreenHeight - mScreenMargin;
        break;
      case Surface.ROTATION_270:
        mXStart = mScreenWidth;
        mXEnd = mXStart - translation;
        mYStart = mFullScreenHeight / 2 + getWidth() / 2;
        mYEnd = mYStart;
        break;
    }

    if (mSlideIn) {
      setX(mXStart);
      setY(mYStart);
    } else {
      setX(mXEnd);
      setY(mYEnd);
    }
  }
}
