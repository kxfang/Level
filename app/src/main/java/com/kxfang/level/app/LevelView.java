package com.kxfang.level.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.View;

public class LevelView extends View {

  private float mTheta;
  private float mRotation;

  // Paint objects to optimize onDraw
  Paint mTextPaint;

  /**
   * Constructor to be used to inflate the view
   */
  public LevelView(Context context, AttributeSet attrs) {
    super(context, attrs);

    // TODO: refactor into xml
    mTextPaint = new Paint();
    mTextPaint.setColor(Color.WHITE);
    mTextPaint.setAntiAlias(true);
    mTextPaint.setTextAlign(Paint.Align.CENTER);
    mTextPaint.setTextSize(250.0f);
    mTextPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));
  }

  public void render(float[] values) {

    mTheta = (float) Math.toDegrees(Math.acos(values[2] / SensorManager.GRAVITY_EARTH));
    mRotation = (float) Math.toDegrees(Math.atan(values[0] / values[1]));
    if ((values[0] < 0 && values[1] < 0) || (values[0] >= 0 && values[1] < 0)) {
      mRotation += 180;
    }

    invalidate();
  }

  @Override
  protected void onDraw(Canvas c) {
    super.onDraw(c);
    c.drawColor(Color.BLACK);
    c.saveLayer(null, null, Canvas.MATRIX_SAVE_FLAG);
    c.rotate(mRotation, getCentreX(), getCenterY());
    String text = "" + (int) mTheta;
    Rect textBounds = new Rect();
    mTextPaint.getTextBounds(text, 0, text.length(), textBounds);
    c.drawText(
        text,
        getCentreX(),
        getCenterY() + (textBounds.bottom - textBounds.top)/2,
        mTextPaint);
    c.restore();
  }

  private float getCentreX() {
    return getRight() / 2;
  }

  private float getCenterY() {
    return getBottom() / 2;
  }
}
