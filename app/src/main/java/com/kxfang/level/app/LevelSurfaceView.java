package com.kxfang.level.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;

public class LevelSurfaceView extends SurfaceView {

  /**
   * Constructor to be used to inflate the view
   */
  public LevelSurfaceView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void render(float[] values) {
    Canvas c = getHolder().lockCanvas();
    c.drawColor(Color.BLACK);
    Paint p = new Paint();
    p.setColor(Color.WHITE);
    c.drawRect(0.0f, 0.0f, Math.abs(values[0]) * 100, Math.abs(values[1]) * 100, p);
    getHolder().unlockCanvasAndPost(c);
  }
}
