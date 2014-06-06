package me.kfang.levelly.app;

import android.animation.FloatEvaluator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import me.kfang.levelly.color.ColorSet;

/**
 * Abstract view that renders the level data from a sensor.
 */
public abstract class LevelView extends View {

  public static final int TRANSFORM_THRESHOLD = 35;

  private final float PIXEL_DENSITY = getResources().getDisplayMetrics().density;

  private final long BACKGROUND_FADE_DURATION;
  private final float INDICATOR_TEXT_SIZE = dpToPx(100);
  private final float INDICATOR_SHOW_DECIMAL_TEXT_SIZE = dpToPx(70);
  private final float SUB_TEXT_SIZE = dpToPx(20);
  private final String SUB_TEXT_TYPEFACE = "sans-serif-light";
  private final float INDICATOR_STROKE_WIDTH = dpToPx(2.5f);
  private final int NUM_HORIZON_INDICATORS = 3;

  private Paint mIndicatorPaint;
  private Paint mSubTextPaint;

  private Rect mTextBounds;
  private float mHorizonIndicatorLength;

  private boolean mWasLevel = false;

  private FloatEvaluator mFloatEvaluator;

  private ColorSet mColorSet;
  private ColorSet mAlignmentColorSet;

  private Config mConfig;

  public static class Config {
    private boolean mShowDecimal;
    private boolean mShowInclination;
    private boolean mShowAxisInclination;

    public Config(boolean showDecimal, boolean showInclination, boolean showAxisInclination) {
      this.mShowDecimal = showDecimal;
      this.mShowInclination = showInclination;
      this.mShowAxisInclination = showAxisInclination;
    }

    public boolean showDecimal() {
      return mShowDecimal;
    }

    public void setShowDecimal(boolean mShowDecimal) {
      this.mShowDecimal = mShowDecimal;
    }

    public boolean showInclination() {
      return mShowInclination;
    }

    public void setShowInclination(boolean mShowInclination) {
      this.mShowInclination = mShowInclination;
    }

    public boolean showAxisInclination() {
      return mShowAxisInclination;
    }
  }

  public LevelView(Context context, AttributeSet attrs) {
    super(context, attrs);
    BACKGROUND_FADE_DURATION =
        context.getResources().getInteger(android.R.integer.config_longAnimTime);

    mTextBounds = new Rect();

    TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LevelView);
    String indicatorTypeface = null;
    try {
      indicatorTypeface = typedArray.getString(R.styleable.LevelView_typeface);
    } finally {
      typedArray.recycle();
    }

    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN
        && indicatorTypeface.equals("sans-serif-thin")) {
      indicatorTypeface = "sans-serif-light";
    }

    mIndicatorPaint = new Paint();
    mIndicatorPaint.setAntiAlias(true);
    mIndicatorPaint.setTextSize(INDICATOR_TEXT_SIZE);
    mIndicatorPaint.setTextAlign(Paint.Align.CENTER);
    mIndicatorPaint.setStrokeWidth(INDICATOR_STROKE_WIDTH);
    if (indicatorTypeface != null) {
      mIndicatorPaint.setTypeface(Typeface.create(indicatorTypeface, Typeface.NORMAL));
    }

    mSubTextPaint = new Paint(mIndicatorPaint);
    mSubTextPaint.setTextSize(SUB_TEXT_SIZE);
    mSubTextPaint.setTypeface(Typeface.create(SUB_TEXT_TYPEFACE, Typeface.NORMAL));

    mFloatEvaluator = new FloatEvaluator();

    mColorSet = ColorSet.greenColorSet(context);

    mAlignmentColorSet = ColorSet.greenColorSet(context);
  }

  /**
   * Notify that the view should re-render according to new data values.
   */
  public void setPosition(DevicePosition position) {
    onDataChange(position);
    invalidate();
  }

  public void setConfig(Config config) {
    mConfig = config;

    if (config.showDecimal()) {
      mIndicatorPaint.setTextSize(INDICATOR_SHOW_DECIMAL_TEXT_SIZE);
    } else {
      mIndicatorPaint.setTextSize(INDICATOR_TEXT_SIZE);
    }
  }

  public void setColorSet(ColorSet colorSet) {
    mIndicatorPaint.setColor(mColorSet.getForegroundColor());
    mSubTextPaint.setColor(mColorSet.getForegroundColor());

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
   */
  protected abstract void onDataChange(DevicePosition position);

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

  protected void drawSubText(Canvas c, String text, Paint paint) {
    c.drawText(
        text,
        getCenterX(),
        getCenterY() + getTextBufferRadius() - SUB_TEXT_SIZE,
        paint);
  }

  protected float getTextBufferRadius() {
    return getWidth() / 3.5f;
  }

  protected Paint getIndicatorPaint() {
    return mIndicatorPaint;
  }

  protected Paint getSubTextPaint() {
    return mSubTextPaint;
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

  protected Config getConfig() {
    return mConfig;
  }

  protected String getAxisText(float value) {
    if (Math.abs(value) >= 0.05) {
      return String.format("%.1f°", value);
    } else {
      return String.format("%.1f°", 0f);
    }
  }

  protected String getIndicatorText(float value) {
    if (mConfig.showDecimal()) {
      if (Math.abs(value) >= 0.05) {
        return String.format("%.1f°", value);
      } else {
        return String.format("%.1f°", 0f);
      }
    } else {
      int roundedValue;
      if (isLevel(value)) {
        if (value > 90) {
          roundedValue = 180;
        } else {
          roundedValue = 0;
        }
      } else {
        if (value < 0) {
          roundedValue = Math.min(-1, Math.round(value));
        } else if (value > 90) {
          roundedValue = Math.min(179, Math.round(value));
        } else {
          roundedValue = Math.max(1, Math.round(value));
        }
      }
      return String.format("%d°", roundedValue);
    }
  }

  protected boolean isLevel(float value) {
    float threshold = mWasLevel ? 0.35f : 0.25f;
    mWasLevel = Math.abs(value) < threshold || Math.abs(value) > 180 - threshold;
    return mWasLevel;
  }

  protected float dpToPx(float dp) {
    return dp * PIXEL_DENSITY;
  }
}
