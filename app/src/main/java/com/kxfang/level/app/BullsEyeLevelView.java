package com.kxfang.level.app;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;

public class BullsEyeLevelView extends LevelView {

  public enum Config {
    DOWN,
    UP
  }

  private Config mConfig;

  private float mTheta = 1.0f;
  private float mRotation = 1.0f;

  private RectF mArcDimensions;

  // Paint objects to optimize onDraw
  private Paint mTextPaint;
  private Paint mCirclePaint;
  private Paint mArcPaint;

  private int mBackgroundColor = Color.BLACK;
  private int mCircleColor = Color.argb(255, 120, 120, 120);

  private TimeInterpolator mBackgroundInterpolator;
  private ArgbEvaluator mArgbEvaluator;

  private boolean mIsFlat;

  /**
   * Constructor to be used to inflate the view
   */
  public BullsEyeLevelView(Context context, AttributeSet attrs) {
    super(context, attrs);

    mConfig = Config.DOWN;
    mIsFlat = false;

    // TODO: refactor into xml
    mTextPaint = getIndicatorPaint();

    mCirclePaint = new Paint();
    mCirclePaint.setColor(mCircleColor);
    mCirclePaint.setAntiAlias(true);

    mArcPaint = new Paint(getIndicatorPaint());
    mArcPaint.setStyle(Paint.Style.STROKE);

    mBackgroundInterpolator = new TimeInterpolator(getBackgroundFadeDuration());
    mArgbEvaluator = new ArgbEvaluator();
  }

  public void setConfig(Config config) {
    if (config == null || mConfig == config) {
      return;
    }
    mConfig = config;

    int temp = mBackgroundColor;
    mBackgroundColor = mCircleColor;
    mCircleColor = temp;

    mCirclePaint.setColor(mCircleColor);

    invalidate();
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    mArcDimensions = new RectF(
        getCenterX() - getTextBufferRadius(),
        getCenterY() - getTextBufferRadius(),
        getCenterX() + getTextBufferRadius(),
        getCenterY() + getTextBufferRadius());
  }

  @Override
  protected void onDataChange(float[] values) {
    mTheta = OrientationUtils.getDeviceTilt(values[2]);
    mRotation = OrientationUtils.getRotationDegrees(values[0], values[1]);
  }

  @Override
  protected void onDraw(Canvas c) {
    super.onDraw(c);
    boolean flat = isFlat(mTheta, mRotation);

    if (flat != mIsFlat) {
      if (flat && !mIsFlat) {
        mBackgroundInterpolator.start();
        Log.d("FLAT", "starting");
      } else if (!flat && mIsFlat) {
        mBackgroundInterpolator.reverse();
        Log.d("FLAT", "reversing");
      }
      mIsFlat = flat;
    }

    c.drawColor(
        (Integer) mArgbEvaluator.evaluate(
            mBackgroundInterpolator.getProgress(),
            mBackgroundColor,
            CONFIRMATION_COLOR));

    c.saveLayer(null, null, Canvas.MATRIX_SAVE_FLAG);
    c.rotate(mRotation, getCenterX(), getCenterY());

    c.drawCircle(
        getCenterX(),
        flat ? getCenterY() : getCircleY(mTheta),
        getCircleRadius(),
        mCirclePaint);
    c.drawArc(mArcDimensions, 135, 90, false, mArcPaint);
    c.drawArc(mArcDimensions, 315, 90, false, mArcPaint);
    String text = Math.round(mTheta) + "°";
    drawCenterText(c, text, mTextPaint);
    c.restore();
  }

  private boolean isFlat(float theta, float rotation) {
    return Math.round(theta) == 0 || Math.round(theta) == 180;
  }

  private float getCircleRadius() {
    return getTextBufferRadius() - 20.0f;
  }

  private float getCircleY(float theta) {
    if (mConfig == Config.DOWN) {
      return getBottomCircleY(theta);
    }
    return getTopCircleY(theta);
  }

  private float getTopCircleY(float theta) {
    float circleRadius = getCircleRadius();
    return (getHeight() / 2 + circleRadius)
        * (1 - ((mConfig == Config.DOWN ? theta : 180 - theta) / 45)) - circleRadius;
  }

  private float getBottomCircleY(float theta) {
    return getHeight() - getTopCircleY(theta);
  }
}
