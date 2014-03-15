package com.kxfang.level.app;

import android.animation.FloatEvaluator;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * A RelativeLayout that enables sliding in from off-screen.
 */
public class SlideableRelativeLayout extends RelativeLayout {

  private FloatEvaluator mFloatEvaluator;

  public SlideableRelativeLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    mFloatEvaluator = new FloatEvaluator();
  }

  @Override
  protected void onSizeChanged (int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    setY(-1 * h);
  }

  public float getSlideInFractionY() {
    return 1 - getY() / getHeight();
  }

  public void setSlideInFractionY(float fractionY) {
    setY(mFloatEvaluator.evaluate(fractionY, 0, getHeight()));
  }
}
