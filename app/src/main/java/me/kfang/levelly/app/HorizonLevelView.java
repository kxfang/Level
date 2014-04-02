package me.kfang.levelly.app;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

/**
 * Level view with a horizon indicating whether device is parallel to horizon.
 */
public class HorizonLevelView extends LevelView {

  private final float HORIZON_TILT_THRESHOLD = TRANSFORM_THRESHOLD;
  private final float HORIZON_TILT = 90.0f;
  private final float LINE_INDICATOR_TRANSFORM_START = 30;
  private final float LINE_INDICATOR_TRANSFORM_END = 44;
  private float mRotation;
  private float mTilt;
  private boolean mIsLevel;

  private TimeInterpolator mAlignmentTransitionInterpolator;
  private ArgbEvaluator mColorEvaluator;

  private Paint mHorizonPaint;

  private String mInclinationIndicator;

  public HorizonLevelView(Context context, AttributeSet attrs) {
    super(context, attrs);

    mHorizonPaint = new Paint();
    mHorizonPaint.setStyle(Paint.Style.FILL);
    mHorizonPaint.setAntiAlias(true);

    mAlignmentTransitionInterpolator = new TimeInterpolator(getBackgroundFadeDuration());
    mColorEvaluator = new ArgbEvaluator();

    mIsLevel = false;

    mInclinationIndicator = context.getResources().getString(R.string.label_inclination);
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

    String text = getIndicatorText(displayRotation);
    drawCenterText(canvas, text, getIndicatorPaint());

    if (getConfig().showInclination()) {
      String subText = getIndicatorText(mTilt);
      drawSubText(canvas, String.format(mInclinationIndicator, subText), getSubTextPaint());
    }

    canvas.restore();



    drawHorizonIndicators(
        canvas,
        getTransformValue(
            Math.abs(displayRotation),
            LINE_INDICATOR_TRANSFORM_START,
            LINE_INDICATOR_TRANSFORM_END,
            getHorizonIndicatorLength(),
            0),
        OrientationManager.isLandscape(mRotation));
  }

  private float getDisplayRotation(float rotation) {
    return OrientationManager.getHorizonOffset(rotation);
  }

  private float getHorizonHeight(float tilt) {
    float middleHeight = getHeight() / 2;
    float differencePct = (HORIZON_TILT - tilt) / (HORIZON_TILT - HORIZON_TILT_THRESHOLD);
    return (1 - differencePct) * middleHeight;
  }
}
