package com.kxfang.level.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

/**
 * Level view with a horizon indicating whether device is parallel to horizon.
 */
public class HorizonLevelView extends LevelView {

  private final float HORIZON_TILT_THRESHOLD = 40.0f;
  private final float HORIZON_TILT = 90.0f;
  private float mRotation;
  private float mTilt;

  private int mHorizonColor = Color.BLACK;
  private int mBackgroundColor = Color.WHITE;

  private Paint mHorizonPaint;
  private Paint mTextPaint;

  public HorizonLevelView(Context context, AttributeSet attrs) {
    super(context, attrs);

    mHorizonPaint = new Paint();
    mHorizonPaint.setStyle(Paint.Style.FILL);
    mHorizonPaint.setColor(mHorizonColor);

    mTextPaint = new Paint();
    mTextPaint.setAntiAlias(true);
    mTextPaint.setColor(Color.BLUE);
    mTextPaint.setTextSize(230.0f);
    mTextPaint.setTextAlign(Paint.Align.CENTER);
  }

  @Override
  protected void onDataChange(float[] values) {
    mRotation = OrientationUtils.getRotationDegrees(values[0], values[1]);
    mTilt = OrientationUtils.getDeviceTilt(values[2]);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    canvas.drawColor(mBackgroundColor);

    canvas.save();
    canvas.rotate(mRotation, getCentreX(), getCenterY());
    canvas.save();
    canvas.translate(-10 * getWidth(), 0);
    canvas.drawRect(Float.MIN_VALUE, getHorizonHeight(mTilt), Float.MAX_VALUE, Float.MAX_VALUE, mHorizonPaint);
    canvas.restore();
    String text = (int) (-1 * ((mRotation + 45) % 90 - 45)) + "°";
    drawCenterText(canvas, text, mTextPaint);
    canvas.restore();
  }

  private float getHorizonHeight(float tilt) {
    float middleHeight = getHeight() / 2;
    float differencePct = (HORIZON_TILT - tilt) / (HORIZON_TILT - HORIZON_TILT_THRESHOLD);
    return (1 - differencePct) * middleHeight;
  }
}
