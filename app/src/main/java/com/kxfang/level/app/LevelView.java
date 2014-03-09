package com.kxfang.level.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.view.View;

/**
 * Abstract view that renders the level data from a sensor.
 */
public abstract class LevelView extends View {
  public static final int CONFIRMATION_COLOR = 0xff99cc00;

  private final long BACKGROUND_FADE_DURATION;

  private Rect mTextBounds;

  public LevelView(Context context, AttributeSet attrs) {
    super(context, attrs);
    BACKGROUND_FADE_DURATION =
        context.getResources().getInteger(android.R.integer.config_shortAnimTime);
    mTextBounds = new Rect();
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

  protected float getCentreX() {
    return getWidth() / 2;
  }

  protected float getCenterY() {
    return getHeight() / 2;
  }

  protected void drawCenterText(Canvas c, String text, Paint paint) {
    paint.getTextBounds(text, 0, text.length(), mTextBounds);
    c.drawText(
        text,
        getCentreX(),
        getCenterY() + (mTextBounds.bottom - mTextBounds.top)/2,
        paint);
  }
}
