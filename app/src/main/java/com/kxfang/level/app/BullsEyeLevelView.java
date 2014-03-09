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
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;

public class BullsEyeLevelView extends LevelView {

  private float mTheta = 1.0f;
  private float mRotation = 1.0f;

  // Paint objects to optimize onDraw
  private Paint mTextPaint;
  private Paint mPositiveCirclePaint;

  private ArgbColorInterpolator mBackgroundInterpolator;

  private boolean mIsFlat;

  /**
   * Constructor to be used to inflate the view
   */
  public BullsEyeLevelView(Context context, AttributeSet attrs) {
    super(context, attrs);

    mIsFlat = false;

    // TODO: refactor into xml
    mTextPaint = new Paint();
    mTextPaint.setColor(Color.WHITE);
    mTextPaint.setAntiAlias(true);
    mTextPaint.setTextAlign(Paint.Align.CENTER);
    mTextPaint.setTextSize(230.0f);
    mTextPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));

    mPositiveCirclePaint = new Paint();
    mPositiveCirclePaint.setColor(Color.WHITE);
    mPositiveCirclePaint.setAntiAlias(true);
    mPositiveCirclePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));

    mBackgroundInterpolator =
        new ArgbColorInterpolator(Color.BLACK, CONFIRMATION_COLOR, getBackgroundFadeDuration());
  }

  @Override
  protected void onDataChange(float[] values) {
    mTheta = OrientationUtils.getDeviceTilt(values[2]);
    mRotation = OrientationUtils.getRotationDegrees(values[0], values[1]);
  }

  @Override
  protected void onDraw(Canvas c) {
    super.onDraw(c);
    boolean flat = isFlat(mTheta, mRotation);

    if (flat != mIsFlat) {
      if (flat && !mIsFlat) {
        mBackgroundInterpolator.start();
        Log.d("FLAT", "starting");
      } else if (!flat && mIsFlat) {
        mBackgroundInterpolator.reverse();
        Log.d("FLAT", "reversing");
      }
      mIsFlat = flat;
    }

    c.drawColor(mBackgroundInterpolator.getColor());
    c.saveLayer(null, null, Canvas.MATRIX_SAVE_FLAG);
    c.rotate(mRotation, getCentreX(), getCenterY());

    c.drawCircle(
        getCentreX(),
        flat ? getCenterY() : getTopPositiveCircleY(mTheta),
        getCircleRadius() + 5.0f,
        mPositiveCirclePaint);
    c.drawCircle(
        getCentreX(),
        flat ? getCenterY() : getBottomPositiveCircleY(mTheta),
        getCircleRadius(),
        mPositiveCirclePaint);
    String text = (int) mTheta + "°";
    drawCenterText(c, text, mTextPaint);
    c.restore();
  }

  private boolean isFlat(float theta, float rotation) {
    return (int) theta == 0;
  }

  private float getCircleRadius() {
    return getWidth() / 4.5f;
  }

  private float getTopPositiveCircleY(float theta) {
    return (getHeight() / 2) * (1 - (theta / 35));
  }

  private float getBottomPositiveCircleY(float theta) {
    return getHeight() - getTopPositiveCircleY(theta);
  }
}
