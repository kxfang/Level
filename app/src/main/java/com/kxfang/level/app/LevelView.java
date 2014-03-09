package com.kxfang.level.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * Abstract view that renders the level data from a sensor.
 */
public abstract class LevelView extends View {
  public static final int CONFIRMATION_COLOR = 0xff99cc00;
  public static final int CONFIRMATION_COLOR_ACCENT = Color.argb(255, 201, 233, 123);

  private final long BACKGROUND_FADE_DURATION;

  private Paint mIndicatorPaint;

  private Rect mTextBounds;

  public LevelView(Context context, AttributeSet attrs) {
    super(context, attrs);
    BACKGROUND_FADE_DURATION =
        context.getResources().getInteger(android.R.integer.config_shortAnimTime);
    mTextBounds = new Rect();

    mIndicatorPaint = new Paint();
    mIndicatorPaint.setAntiAlias(true);
    mIndicatorPaint.setColor(Color.WHITE);
    mIndicatorPaint.setTextSize(200.0f);
    mIndicatorPaint.setTextAlign(Paint.Align.CENTER);
    mIndicatorPaint.setStrokeWidth(5.0f);
  }

  /**
   * Notify that the view should re-render according to new data values.
   */
  public void render(float[] values) {
    onDataChange(values);
    invalidate();
  }

  /**
   * Method to be overriden by subclasses to change their state when the sensor data changes.
   * @param values the new data values.
   */
  protected abstract void onDataChange(float[] values);

  protected long getBackgroundFadeDuration() {
    return BACKGROUND_FADE_DURATION;
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
}
