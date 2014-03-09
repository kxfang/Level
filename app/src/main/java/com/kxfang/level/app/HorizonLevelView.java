package com.kxfang.level.app;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

/**
 * Level view with a horizon indicating whether device is parallel to horizon.
 */
public class HorizonLevelView extends LevelView {

  private final float HORIZON_TILT_THRESHOLD = 45.5f;
  private final float HORIZON_TILT = 90.0f;
  private float mRotation;
  private float mTilt;
  private boolean mIsLevel;

  private int mHorizonColor = Color.BLACK;
  private int mBackgroundColor = Color.argb(255, 231, 229, 229);
  private TimeInterpolator mConfirmationTransitionInterpolator;
  private ArgbEvaluator mColorEvaluator;

  private Paint mHorizonPaint;

  public HorizonLevelView(Context context, AttributeSet attrs) {
    super(context, attrs);

    mHorizonPaint = new Paint();
    mHorizonPaint.setStyle(Paint.Style.FILL);
    mHorizonPaint.setColor(mHorizonColor);
    mHorizonPaint.setAntiAlias(true);

    mConfirmationTransitionInterpolator = new TimeInterpolator(getBackgroundFadeDuration());
    mColorEvaluator = new ArgbEvaluator();

    mIsLevel = false;
  }

  @Override
  protected void onDataChange(float[] values) {
    mRotation = OrientationUtils.getRotationDegrees(values[0], values[1]);
    mTilt = OrientationUtils.getDeviceTilt(values[2]);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    canvas.drawColor(mBackgroundColor);

    float displayRotation = getDisplayRotation(mRotation);
    boolean isLevel = isLevel(displayRotation);
    if (isLevel != mIsLevel) {
      if (isLevel) {
        mConfirmationTransitionInterpolator.start();
      } else {
        mConfirmationTransitionInterpolator.reverse();
      }
      mIsLevel = isLevel;
    }

    float progress = mConfirmationTransitionInterpolator.getProgress();
    canvas.drawColor((Integer) mColorEvaluator.evaluate(
        progress,
        mBackgroundColor,
        CONFIRMATION_COLOR_ACCENT
    ));
    mHorizonPaint.setColor(
        (Integer) mColorEvaluator.evaluate(
            progress,
            mHorizonColor,
            CONFIRMATION_COLOR));

    canvas.save();
    canvas.rotate(mRotation, getCentreX(), getCenterY());
    canvas.save();
    canvas.translate(-10 * getWidth(), 0);
    canvas.drawRect(
        Float.MIN_VALUE,
        getHorizonHeight(mTilt),
        Float.MAX_VALUE,
        Float.MAX_VALUE,
        mHorizonPaint);
    canvas.restore();
    String text = Math.round(displayRotation) + "°";
    drawCenterText(canvas, text, getIndicatorPaint());
    canvas.restore();

    drawHorizonIndicators(canvas);
  }

  private void drawHorizonIndicators(Canvas c) {
    float centerX = getCentreX();
    float centerY = getCenterY();
    float lineLength = getWidth() / 8;
    float bufferSpace = getTextBufferRadius();
    if (isLandScape(mRotation)) {
      c.drawLine(
          centerX,
          centerY + bufferSpace + lineLength,
          centerX,
          centerY + bufferSpace,
          getIndicatorPaint());
      c.drawLine(
          centerX,
          centerY - bufferSpace - lineLength,
          centerX,
          centerY - bufferSpace,
          getIndicatorPaint());
    } else {
      c.drawLine(
          centerX + bufferSpace + lineLength,
          centerY,
          centerX + bufferSpace,
          centerY,
          getIndicatorPaint());
      c.drawLine(
          centerX - bufferSpace - lineLength,
          centerY,
          centerX - bufferSpace,
          centerY,
          getIndicatorPaint());
    }
  }

  private boolean isLandScape(float rotation) {
    return ((rotation > 45 && rotation < 135) || (rotation > 225 && rotation < 315));
  }

  private float getDisplayRotation(float mRotation) {
    return -1 * ((mRotation + 45) % 90 - 45);
  }

  private boolean isLevel(float rotation) {
    return Math.abs(rotation) < 0.5f;
  }

  private float getHorizonHeight(float tilt) {
    float middleHeight = getHeight() / 2;
    float differencePct = (HORIZON_TILT - tilt) / (HORIZON_TILT - HORIZON_TILT_THRESHOLD);
    return (1 - differencePct) * middleHeight;
  }
}
