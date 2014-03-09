package com.kxfang.level.app;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
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

  // Paint objects to optimize onDraw
  private Paint mTextPaint;
  private Paint mCirclePaint;

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
    mTextPaint = new Paint();
    mTextPaint.setColor(Color.WHITE);
    mTextPaint.setAntiAlias(true);
    mTextPaint.setTextAlign(Paint.Align.CENTER);
    mTextPaint.setTextSize(230.0f);
    mTextPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));

    mCirclePaint = new Paint();
    mCirclePaint.setColor(mCircleColor);
    mCirclePaint.setAntiAlias(true);
    mCirclePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));

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
    c.rotate(mRotation, getCentreX(), getCenterY());

    c.drawCircle(
        getCentreX(),
        flat ? getCenterY() : getTopPositiveCircleY(mTheta),
        getCircleRadius() + 5.0f,
        mCirclePaint);
    c.drawCircle(
        getCentreX(),
        flat ? getCenterY() : getBottomPositiveCircleY(mTheta),
        getCircleRadius(),
        mCirclePaint);
    String text = Math.round(mTheta) + "°";
    drawCenterText(c, text, mTextPaint);
    c.restore();
  }

  private boolean isFlat(float theta, float rotation) {
    return Math.round(theta) == 0 || Math.round(theta) == 180;
  }

  private float getCircleRadius() {
    return getWidth() / 4.0f;
  }

  private float getTopPositiveCircleY(float theta) {
    float circleRadius = getCircleRadius();
    return (getHeight() / 2 + circleRadius)
        * (1 - ((mConfig == Config.DOWN ? theta : 180 - theta) / 45)) - circleRadius;
  }

  private float getBottomPositiveCircleY(float theta) {
    return getHeight() - getTopPositiveCircleY(theta);
  }
}
