package com.kxfang.level.app;

import android.animation.FloatEvaluator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.kxfang.level.app.color.ColorSet;

/**
 * Abstract view that renders the level data from a sensor.
 */
public abstract class LevelView extends View {
  private final long BACKGROUND_FADE_DURATION;
  private final float INDICATOR_TEXT_SIZE = 200.0f;
  private final float INDICATOR_STROKE_WIDTH = 5.0f;
  private final int NUM_HORIZON_INDICATORS = 3;

  private Paint mIndicatorPaint;

  private Rect mTextBounds;
  private float mHorizonIndicatorLength;

  private FloatEvaluator mFloatEvaluator;

  private ColorSet mColorSet;
  private ColorSet mAlignmentColorSet;

  public LevelView(Context context, AttributeSet attrs) {
    super(context, attrs);
    BACKGROUND_FADE_DURATION =
        context.getResources().getInteger(android.R.integer.config_shortAnimTime);

    mTextBounds = new Rect();

    TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LevelView);
    String indicatorTypeface = null;
    try {
      indicatorTypeface = typedArray.getString(R.styleable.LevelView_typeface);
    } finally {
      typedArray.recycle();
    }

    mIndicatorPaint = new Paint();
    mIndicatorPaint.setAntiAlias(true);
    mIndicatorPaint.setTextSize(INDICATOR_TEXT_SIZE);
    mIndicatorPaint.setTextAlign(Paint.Align.CENTER);
    mIndicatorPaint.setStrokeWidth(INDICATOR_STROKE_WIDTH);
    if (indicatorTypeface != null) {
      mIndicatorPaint.setTypeface(Typeface.create(indicatorTypeface, Typeface.NORMAL));
    }

    mFloatEvaluator = new FloatEvaluator();

    mColorSet = ColorSet.greenColorSet(context);

    mAlignmentColorSet = ColorSet.greenColorSet(context);
  }

  /**
   * Notify that the view should re-render according to new data values.
   */
  public void render(float[] values) {
    onDataChange(values);
    invalidate();
  }

  public void setColorSet(ColorSet colorSet) {
    mIndicatorPaint.setColor(mColorSet.getForegroundColor());
    mColorSet = colorSet;
  }

  protected ColorSet getColorSet() {
    return mColorSet;
  }

  protected ColorSet getAlignmentColorSet() {
    return mAlignmentColorSet;
  }

  /**
   * Method to be overriden by subclasses to change their state when the sensor data changes.
   * @param values the new data values.
   */
  protected abstract void onDataChange(float[] values);

  protected long getBackgroundFadeDuration() {
    return BACKGROUND_FADE_DURATION;
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    mHorizonIndicatorLength = w / 9;
  }

  protected float getCenterX() {
    return getWidth() / 2;
  }

  protected float getCenterY() {
    return getHeight() / 2;
  }

  protected void drawCenterText(Canvas c, String text, Paint paint) {
    paint.getTextBounds(text, 0, text.length(), mTextBounds);
    c.drawText(
        text,
        getCenterX(),
        getCenterY() + (mTextBounds.bottom - mTextBounds.top)/2,
        paint);
  }

  protected float getTextBufferRadius() {
    return getWidth() / 3.5f;
  }

  protected Paint getIndicatorPaint() {
    return mIndicatorPaint;
  }

  protected float getHorizonIndicatorLength() {
    return mHorizonIndicatorLength;
  }

  protected void drawHorizonIndicators(Canvas c, float lineLength, boolean isLandscape) {
    float centerX = getCenterX();
    float centerY = getCenterY();

    float xDimension, yDimension;
    float lineStartDistance = getTextBufferRadius() + getHorizonIndicatorLength();
    c.save();
    if (isLandscape) {
      xDimension = getHeight();
      yDimension = getWidth();
      c.rotate(90, centerX, centerY);
    } else {
      xDimension = getWidth();
      yDimension = getHeight();
    }


    for (int i = NUM_HORIZON_INDICATORS * -1 + 1; i < NUM_HORIZON_INDICATORS; i++) {
      float heightIncrement = i * (yDimension / NUM_HORIZON_INDICATORS / 2);
      float drawLength = i % 2 == 0 ? lineLength : lineLength / 2;
      for (int j = -1; j <= 1; j += 2) {
        for (int k = -1; k <= 1; k += 2) {
          float yCoord = centerY + j * heightIncrement;
          c.drawLine(
              centerX + k * lineStartDistance,
              yCoord,
              centerX + k * lineStartDistance - k * drawLength,
              yCoord,
              getIndicatorPaint());
        }
      }
    }
    c.restore();
  }

  protected float getTransformValue(
      float tilt,
      float tiltStart,
      float tiltEnd,
      float start,
      float end) {
    float fraction =
        Math.max(
            0,
            Math.min(1, (tilt - tiltStart) / (tiltEnd - tiltStart)));

    return mFloatEvaluator.evaluate(fraction, start, end);
  }
}
