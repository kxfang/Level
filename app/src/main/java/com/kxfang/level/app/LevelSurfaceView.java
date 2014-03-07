package com.kxfang.level.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.view.SurfaceView;

public class LevelSurfaceView extends SurfaceView {

  /**
   * Constructor to be used to inflate the view
   */
  public LevelSurfaceView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void render(float[] values) {

    float theta = (float) Math.toDegrees(Math.acos(values[2] / SensorManager.GRAVITY_EARTH));
    float rotation = (float) Math.toDegrees(Math.atan(values[0] / values[1]));
    if ((values[0] < 0 && values[1] < 0) || (values[0] >= 0 && values[1] < 0)) {
      rotation += 180;
    }

    Canvas c = getHolder().lockCanvas();
    c.rotate(rotation, getCentreX(), getCenterY());
    c.drawColor(Color.BLACK);
    Paint p = new Paint();
    p.setColor(Color.WHITE);
    p.setAntiAlias(true);
    p.setTextAlign(Paint.Align.CENTER);
    p.setTextSize(250.0f);
    String text = "" + (int) theta;
    Rect textBounds = new Rect();
    p.getTextBounds(text, 0, text.length(), textBounds);
    c.drawText(text, getCentreX(), getCenterY() + (textBounds.bottom - textBounds.top)/2, p);
    getHolder().unlockCanvasAndPost(c);
  }

  private float getCentreX() {
    return getRight() / 2;
  }

  private float getCenterY() {
    return getBottom() / 2;
  }
}
