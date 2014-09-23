package me.kfang.levelly.color;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import me.kfang.levelly.app.R;

/**
 * Manages color choice for the app.
 */
public class ColorSet {

  private static final String BLUE = "blue";
  private static final String ORANGE = "orange";
  private static final String PURPLE = "purple";
  private static final String RED = "red";
  private static final String GREEN = "green";
  private static final String INDIGO = "indigo";
  private static final String PINK = "pink";
  private static final String TEAL = "teal";
  private static final String GREY = "grey";
  private static final String CYAN = "cyan";

  private static final List<String> RANDOM_SET = Arrays.asList(
      BLUE, ORANGE, PURPLE, RED, INDIGO, PINK, TEAL, GREY, CYAN);

  private static ColorSet sGlobalColorSet = null;

  private int mPrimaryColor, mSecondaryColor;

  private ColorSet(int primaryColor, int secondaryColor) {
    mPrimaryColor = primaryColor;
    mSecondaryColor = secondaryColor;
  }

  private ColorSet(Context context, String color) {
    Resources r = context.getResources();
    int resId;
    if (color.equals(BLUE)) {
      resId = R.array.blue;
    } else if (color.equals(GREEN)) {
      resId = R.array.green;
    } else if (color.equals(ORANGE)) {
      resId = R.array.orange;
    } else if (color.equals(PURPLE)) {
      resId = R.array.purple;
    } else if (color.equals(RED)) {
      resId = R.array.red;
    } else if (color.equals(INDIGO)) {
      resId = R.array.indigo;
    } else if (color.equals(PINK)) {
      resId = R.array.pink;
    } else if (color.equals(TEAL)) {
      resId = R.array.teal;
    } else if (color.equals(GREY)) {
      resId = R.array.grey;
    } else if (color.equals(CYAN)) {
      resId = R.array.cyan;
    } else {
      throw new IllegalArgumentException(color + " is an invalid color");
    }
    TypedArray typedArray = r.obtainTypedArray(resId);
    try {
      mPrimaryColor = typedArray.getColor(0, Color.BLACK);
      mSecondaryColor = typedArray.getColor(1, Color.BLACK);
    } finally {
      typedArray.recycle();
    }
  }

  public static ColorSet copyOf(ColorSet colorSet) {
    return new ColorSet(colorSet.mPrimaryColor, colorSet.mSecondaryColor);
  }

  public static ColorSet globalColorSet(Context context) {
    if (sGlobalColorSet == null) {
      sGlobalColorSet = randomColorSet(context);
    }
    return sGlobalColorSet;
  }

  public static ColorSet randomColorSet(Context context) {
    return new ColorSet(context, RANDOM_SET.get(new Random().nextInt(RANDOM_SET.size())));
  }

  public static ColorSet greenColorSet(Context context) {
    return new ColorSet(context, GREEN);
  }

  public int getForegroundColor() {
    return Color.WHITE;
  }

  public int getPrimaryColor() {
    return mPrimaryColor;
  }

  public int getSecondaryColor() {
    return mSecondaryColor;
  }

  public void invert() {
    int temp = mPrimaryColor;
    mPrimaryColor = mSecondaryColor;
    mSecondaryColor = temp;
  }
}
