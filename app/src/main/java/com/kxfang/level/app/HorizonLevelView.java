package com.kxfang.level.app;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

/**
 * Level view with a horizon indicating whether device is parallel to horizon.
 */
public class HorizonLevelView extends LevelView {

  private final float HORIZON_TILT_THRESHOLD = 45.5f;
  private final float HORIZON_TILT = 90.0f;
  private final float LINE_INDICATOR_TRANSFORM_START = 30;
  private final float LINE_INDICATOR_TRANSFORM_END = 44;
  private float mRotation;
  private float mTilt;
  private boolean mIsLevel;

  private TimeInterpolator mAlignmentTransitionInterpolator;
  private ArgbEvaluator mColorEvaluator;

  private Paint mHorizonPaint;

  public HorizonLevelView(Context context, AttributeSet attrs) {
    super(context, attrs);

    mHorizonPaint = new Paint();
    mHorizonPaint.setStyle(Paint.Style.FILL);
    mHorizonPaint.setAntiAlias(true);

    mAlignmentTransitionInterpolator = new TimeInterpolator(getBackgroundFadeDuration());
    mColorEvaluator = new ArgbEvaluator();

    mIsLevel = false;
  }

  @Override
  protected void onDataChange(DevicePosition position) {
    mRotation = position.getRotation();
    mTilt = position.getTilt();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    canvas.drawColor(getColorSet().getPrimaryColor());

    float displayRotation = getDisplayRotation(mRotation);
    boolean isLevel = isLevel(displayRotation);
    if (isLevel != mIsLevel) {
      if (isLevel) {
        mAlignmentTransitionInterpolator.start();
      } else {
        mAlignmentTransitionInterpolator.reverse();
      }
      mIsLevel = isLevel;
    }

    float progress = mAlignmentTransitionInterpolator.getProgress();
    canvas.drawColor((Integer) mColorEvaluator.evaluate(
        progress,
        getColorSet().getPrimaryColor(),
        getAlignmentColorSet().getPrimaryColor()
    ));
    mHorizonPaint.setColor(
        (Integer) mColorEvaluator.evaluate(
            progress,
            getColorSet().getSecondaryColor(),
            getAlignmentColorSet().getSecondaryColor()));

    canvas.save();
    canvas.rotate(mRotation, getCenterX(), getCenterY());
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

    drawHorizonIndicators(
        canvas,
        getTransformValue(
            Math.abs(displayRotation),
            LINE_INDICATOR_TRANSFORM_START,
            LINE_INDICATOR_TRANSFORM_END,
            getHorizonIndicatorLength(),
            0),
        OrientationUtils.isLandscape(mRotation));
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
