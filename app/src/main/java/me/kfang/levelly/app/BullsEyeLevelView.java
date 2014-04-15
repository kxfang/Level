package me.kfang.levelly.app;

import android.animation.ArgbEvaluator;
import android.animation.FloatEvaluator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.animation.AccelerateDecelerateInterpolator;

import me.kfang.levelly.color.ColorSet;

public class BullsEyeLevelView extends LevelView {

  public enum Config {
    DOWN,
    UP
  }


  private int mArcIndicatorTransformStartTilt = 14;
  private int mArcIndicatorTransformEndTilt = 25;
  private float mAxisArcIndicatorTransformStartTilt = 17;
  private float mAxisIndicatorTransformStartTilt = 17;
  private int mLineIndicatorTransformStartTilt = 26;
  private int mLineIndicatorTransformEndTilt = 35;

  private float mArcIndicatorStubLength = dpToPx(17.5f);

  private Config mConfig;
  private boolean mShowTransition;

  private float mTilt = 1.0f;
  private float mRotation = 1.0f;

  private float mXTilt;
  private float mYTilt;

  private RectF mArcDimensions;

  // Paint objects to optimize onDraw
  private Paint mTextPaint;
  private Paint mCirclePaint;
  private Paint mArcPaint;
  private Paint mAxisIndicatorPaint;

  private TimeInterpolator mBackgroundInterpolator;
  private TimeInterpolator mRotationInterpolator;
  private ArgbEvaluator mArgbEvaluator;
  private FloatEvaluator mFloatEvaluator;
  private AccelerateDecelerateInterpolator mAnimationInterpolator;

  private boolean mIsFlat;
  private float mAlignmentRotationStart;

  /**
   * Constructor to be used to inflate the view
   */
  public BullsEyeLevelView(Context context, AttributeSet attrs) {
    super(context, attrs);

    mConfig = Config.DOWN;
    mIsFlat = false;
    TypedArray attributes = context.getResources().obtainAttributes(attrs, R.styleable.LevelView);
    try {
      mShowTransition = attributes.getBoolean(R.styleable.LevelView_showTransition, false);
    } finally {
      attributes.recycle();
    }

    // TODO: refactor into xml
    mTextPaint = getIndicatorPaint();

    mCirclePaint = new Paint();
    mCirclePaint.setAntiAlias(true);

    mArcPaint = new Paint(getIndicatorPaint());
    mArcPaint.setStyle(Paint.Style.STROKE);

    mAxisIndicatorPaint = new Paint(getSubTextPaint());
    mAxisIndicatorPaint.setTextAlign(Paint.Align.RIGHT);

    mBackgroundInterpolator = new TimeInterpolator(getBackgroundFadeDuration());
    mRotationInterpolator = new TimeInterpolator(getBackgroundFadeDuration());
    mArgbEvaluator = new ArgbEvaluator();
    mFloatEvaluator = new FloatEvaluator();
    mAnimationInterpolator = new AccelerateDecelerateInterpolator();
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
  protected void onDataChange(DevicePosition position) {
    mTilt = position.getTilt();
    mRotation = position.getRotation();
    mXTilt = position.getXTilt();
    mYTilt = position.getYTilt();
  }

  @Override
  protected void onDraw(Canvas c) {
    super.onDraw(c);
    boolean flat = isLevel(mTilt);

    if (flat != mIsFlat) {
      if (flat && !mIsFlat) {
        mBackgroundInterpolator.start();
        mRotationInterpolator.start();
        mAlignmentRotationStart = mRotation;
      } else if (!flat && mIsFlat) {
        mBackgroundInterpolator.reverse();
        mRotationInterpolator.reset();
      }
      mIsFlat = flat;
    }

    float textRotation;

    if (mIsFlat) {
      float dstRotation;

      switch (OrientationManager.getInstance().getCurrentOrientation()) {
        case Surface.ROTATION_90:
          dstRotation = 90;
          break;
        case Surface.ROTATION_180:
          dstRotation = 180;
          break;
        case Surface.ROTATION_270:
          dstRotation = 270;
          break;
        default:
          dstRotation = 0;
      }

      if (dstRotation - mAlignmentRotationStart < -180 ) {
        dstRotation += 360;
      } else if (dstRotation - mAlignmentRotationStart > 180) {
        mAlignmentRotationStart += 360;
      }

      textRotation = mFloatEvaluator.evaluate(
          mAnimationInterpolator.getInterpolation(mRotationInterpolator.getProgress()),
          mAlignmentRotationStart,
          dstRotation);
    } else {
      textRotation = mRotation;
    }

    c.drawColor(
        (Integer) mArgbEvaluator.evaluate(
            mBackgroundInterpolator.getProgress(),
            getColorSet().getSecondaryColor(),
            getAlignmentColorSet().getSecondaryColor()));

    c.save();
    c.rotate(mRotation, getCenterX(), getCenterY());

    mCirclePaint.setColor(
        (Integer) mArgbEvaluator.evaluate(
            mBackgroundInterpolator.getProgress(),
            getColorSet().getPrimaryColor(),
            getAlignmentColorSet().getPrimaryColor()));
    c.drawCircle(
        getCenterX(),
        getCircleY(mTilt),
        getCircleRadius(),
        mCirclePaint);
    c.restore();

    c.save();
    c.rotate(textRotation, getCenterX(), getCenterY());
    String text = getIndicatorText(mTilt);
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
        OrientationManager.isLandscape(mRotation));
  }

  private void updatePaintColors() {
    mCirclePaint.setColor(getColorSet().getPrimaryColor());
    mArcPaint.setColor(getColorSet().getForegroundColor());
    mAxisIndicatorPaint.setColor(getColorSet().getForegroundColor());
  }

  private void drawArcIndicators(Canvas c) {
    float tiltStart = getConfig().showAxisInclination() ?
        mAxisArcIndicatorTransformStartTilt
        : mArcIndicatorTransformStartTilt;
    float tiltEnd = mArcIndicatorTransformEndTilt;

    float sweepAngle = getConfig().showAxisInclination() ? 180 : 90;
    c.drawArc(
        mArcDimensions,
        getTransformValue(mTilt, tiltStart, tiltEnd, 135, 180),
        getTransformValue(mTilt, tiltStart, tiltEnd, sweepAngle, 0),
        false,
        mArcPaint);

    c.drawArc(
        mArcDimensions,
        getTransformValue(mTilt, tiltStart, tiltEnd, 315, 360),
        getTransformValue(mTilt, tiltStart, tiltEnd, sweepAngle, 0),
        false,
        mArcPaint);

    if (!getConfig().showAxisInclination()) {
      for (int i = -1; i <= 1; i += 2) {
        c.drawLine(
            getCenterX() + i * getTextBufferRadius(),
            getCenterY(),
            getCenterX()
                + i * (getTextBufferRadius()
                + getTransformValue(mTilt, tiltStart, tiltEnd, mArcIndicatorStubLength, 0)),
            getCenterY(),
            getIndicatorPaint()
        );
      }
    } else if (mTilt <= mArcIndicatorTransformEndTilt) {
      // Draw axis indicators
      float subtextPadding = getResources().getDimensionPixelSize(R.dimen.subtext_padding);

      // X axis
      c.drawText(
          getAxisText("x", mXTilt),
          getCenterX() - getTextBufferRadius() - subtextPadding,
          getCenterY() - subtextPadding,
          mAxisIndicatorPaint);

      float start;
      float end;
      float axisTiltMax;
      float axisTransitionStart;

      if (mXTilt < 0) {
        start = getCenterX() + getTextBufferRadius();
        end = getWidth() + dpToPx(50);
        axisTiltMax = -mAxisArcIndicatorTransformStartTilt / 2;
        axisTransitionStart = end;
      } else {
        start = getCenterX() - getTextBufferRadius();
        end = -dpToPx(50);
        axisTiltMax = mAxisArcIndicatorTransformStartTilt / 2;
        axisTransitionStart = end;
      }

      c.drawLine(
          getTransformValue(mTilt, mAxisIndicatorTransformStartTilt, mArcIndicatorTransformEndTilt, start, axisTransitionStart),
          getCenterY(),
          getTransformValue(mTilt, mAxisIndicatorTransformStartTilt, mArcIndicatorTransformEndTilt, getTransformValue(mXTilt, 0, axisTiltMax, start, end), axisTransitionStart),
          getCenterY(),
          getIndicatorPaint()
      );

      // Y axis
      c.drawText(
          getAxisText("y", mYTilt),
          getCenterX() - subtextPadding,
          getCenterY() - getTextBufferRadius() - subtextPadding,
          mAxisIndicatorPaint);

      if (mYTilt > 0) {
        start = getCenterY() + getTextBufferRadius();
        end = getHeight() + dpToPx(50);
        axisTiltMax = mAxisArcIndicatorTransformStartTilt;
        axisTransitionStart = end;
      } else {
        start = getCenterY() - getTextBufferRadius();
        end = -dpToPx(50);
        axisTiltMax = -mAxisArcIndicatorTransformStartTilt;
        axisTransitionStart = end;
      }

      c.drawLine(
          getCenterX(),
          getTransformValue(mTilt, mAxisIndicatorTransformStartTilt, mArcIndicatorTransformEndTilt, start, axisTransitionStart),
          getCenterX(),
          getTransformValue(mTilt, mAxisIndicatorTransformStartTilt, mArcIndicatorTransformEndTilt, getTransformValue(mYTilt, 0, axisTiltMax, start, end), axisTransitionStart),
          getIndicatorPaint()
      );
    }
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
        * (1 - ((mConfig == Config.DOWN ? theta : 180 - theta) / TRANSFORM_THRESHOLD)) - circleRadius;
  }

  private float getBottomCircleY(float theta) {
    return getHeight() - getTopCircleY(theta);
  }
}
