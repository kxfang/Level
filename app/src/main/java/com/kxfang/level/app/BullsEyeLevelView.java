package com.kxfang.level.app;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;

import com.kxfang.level.app.color.ColorSet;

public class BullsEyeLevelView extends LevelView {

  public enum Config {
    DOWN,
    UP
  }

  private int mArcIndicatorTransformStartTilt = 17;
  private int mArcIndicatorTransformEndTilt = 30;
  private int mLineIndicatorTransformStartTilt = 32;
  private int mLineIndicatorTransformEndTilt = 44;


  private Config mConfig;

  private float mTilt = 1.0f;
  private float mRotation = 1.0f;

  private RectF mArcDimensions;

  // Paint objects to optimize onDraw
  private Paint mTextPaint;
  private Paint mCirclePaint;
  private Paint mArcPaint;

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

    getColorSet().invert();
    getAlignmentColorSet().invert();
    updatePaintColors();

    mArcIndicatorTransformStartTilt = 180 - mArcIndicatorTransformStartTilt;
    mArcIndicatorTransformEndTilt = 180 - mArcIndicatorTransformEndTilt;
    mLineIndicatorTransformStartTilt = 180 - mLineIndicatorTransformStartTilt;
    mLineIndicatorTransformEndTilt = 180 - mLineIndicatorTransformEndTilt;

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
  public void setColorSet(ColorSet cs) {
    super.setColorSet(cs);
    updatePaintColors();
  }

  @Override
  protected void onDataChange(float[] values) {
    mTilt = OrientationUtils.getDeviceTilt(values[2]);
    mRotation = OrientationUtils.getRotationDegrees(values[0], values[1]);
  }

  @Override
  protected void onDraw(Canvas c) {
    super.onDraw(c);
    boolean flat = isFlat(mTilt, mRotation);

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
            getColorSet().getSecondaryColor(),
            getAlignmentColorSet().getSecondaryColor()));

    c.saveLayer(null, null, Canvas.MATRIX_SAVE_FLAG);
    c.rotate(mRotation, getCenterX(), getCenterY());

    mCirclePaint.setColor(
        (Integer) mArgbEvaluator.evaluate(
            mBackgroundInterpolator.getProgress(),
            getColorSet().getPrimaryColor(),
            getAlignmentColorSet().getPrimaryColor()));
    c.drawCircle(
        getCenterX(),
        flat ? getCenterY() : getCircleY(mTilt),
        getCircleRadius(),
        mCirclePaint);
    String text = Math.round(mTilt) + "°";
    drawCenterText(c, text, mTextPaint);
    c.restore();
    drawArcIndicators(c);

    drawHorizonIndicators(
        c,
        getTransformValue(
            mTilt,
            mLineIndicatorTransformStartTilt,
            mLineIndicatorTransformEndTilt,
            0,
            getHorizonIndicatorLength()),
        OrientationUtils.isLandscape(mRotation));
  }

  private void updatePaintColors() {
    mCirclePaint.setColor(getColorSet().getPrimaryColor());
    mArcPaint.setColor(getColorSet().getForegroundColor());
  }

  private void drawArcIndicators(Canvas c) {
    float tiltStart = mArcIndicatorTransformStartTilt;
    float tiltEnd = mArcIndicatorTransformEndTilt;
    c.drawArc(
        mArcDimensions,
        getTransformValue(mTilt, tiltStart, tiltEnd, 135, 180),
        getTransformValue(mTilt, tiltStart, tiltEnd, 90, 0),
        false,
        mArcPaint);

    c.drawArc(
        mArcDimensions,
        getTransformValue(mTilt, tiltStart, tiltEnd, 315, 360),
        getTransformValue(mTilt, tiltStart, tiltEnd, 90, 0),
        false,
        mArcPaint);

    for (int i = -1; i <= 1; i+=2) {
      c.drawLine(
          getCenterX() + i * getTextBufferRadius(),
          getCenterY(),
          getCenterX()
              + i * (getTextBufferRadius() + getTransformValue(mTilt, tiltStart, tiltEnd, 35, 0)),
          getCenterY(),
          getIndicatorPaint()
      );
    }
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
