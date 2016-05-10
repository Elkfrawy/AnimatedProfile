package com.elkfrawy.animatedprofile.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.view.View;

/**
 * Created by Ayman Elkfrawy on 9/11/2015.
 */
public class Utility {

//    public static final float BITMAP_SCALE = 0.4f;
//    public static final float BLUR_RADIUS = 7.5f;

    /**
     *
     * @param context
     * @param image
     * @param bitmapScale a rescale rate for returned image
     * @param blurRadius blur rage is 0 < range <= 25
     * @return the image scaled with bitmapScale rate and blurred using specified blurRadius
     */
    public static Bitmap blur(Context context, Bitmap image, float bitmapScale, float blurRadius) {
        int width = Math.round(image.getWidth() * bitmapScale);
        int height = Math.round(image.getHeight() * bitmapScale);

        Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
        theIntrinsic.setRadius(blurRadius);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);

        return outputBitmap;
    }

    public static int[] getAbsoluteXY(View view) {
        int[] values = new int[2];
        view.getLocationInWindow(values);
        return values;
    }

    /**
     *
     * @param value
     * @param min
     * @param max
     * @return value if it lies between min and max, other wise return min or max whichever closer
     */
    public static float clamp(float value, float min, float max) {
        return Math.max(Math.min(value, max), min);
    }

}
