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

public class LevelView extends View {

  private float mTheta = 1.0f;
  private float mRotation = 1.0f;

  // Paint objects to optimize onDraw
  private Paint mTextPaint;
  private Paint mPositiveCirclePaint;

  private ArgbColorInterpolator mBackgroundInterpolator;

  private final long BACKGROUND_FADE_DURATION = 200;
  private boolean mIsFlat;
  private final int CONFIRMATION_COLOR = 0xff99cc00;

  /**
   * Constructor to be used to inflate the view
   */
  public LevelView(Context context, AttributeSet attrs) {
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
        new ArgbColorInterpolator(Color.BLACK, CONFIRMATION_COLOR, BACKGROUND_FADE_DURATION);
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
    Rect textBounds = new Rect();
    mTextPaint.getTextBounds(text, 0, text.length(), textBounds);
    c.drawText(
        text,
        getCentreX(),
        getCenterY() + (textBounds.bottom - textBounds.top)/2,
        mTextPaint);
    c.restore();
  }

  private boolean isFlat(float theta, float rotation) {
    return (int) theta == 0;
  }

  private float getCircleRadius() {
    return getWidth() / 4.5f;
  }

  private float getTopPositiveCircleY(float theta) {
    return (getHeight() / 2) * (1 - (theta / 45));
  }

  private float getBottomPositiveCircleY(float theta) {
    return getHeight() - getTopPositiveCircleY(theta);
  }

  private float getCentreX() {
    return getWidth() / 2;
  }

  private float getCenterY() {
    return getHeight() / 2;
  }
}
