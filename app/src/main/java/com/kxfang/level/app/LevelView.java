package com.kxfang.level.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * Abstract view that renders the level data from a sensor.
 */
public abstract class LevelView extends View {
  private final long BACKGROUND_FADE_DURATION;

  public LevelView(Context context, AttributeSet attrs) {
    super(context, attrs);
    BACKGROUND_FADE_DURATION =
        context.getResources().getInteger(android.R.integer.config_shortAnimTime);
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

  protected float getRotationDegrees(float x, float y) {
    float rotation = (float) Math.toDegrees(Math.atan(x / y));
    if ((x < 0 && y < 0) || (x >= 0 && y < 0)) {
      rotation += 180;
    }
    return rotation;
  }
}
