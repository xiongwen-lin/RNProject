package com.afar.osaio.util;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

public class CompatUtil {

    public static int getColor(Context context, int id) {
        if (context == null) {
            return 0;
        }
        try {
            return ContextCompat.getColor(context, id);
        } catch (Exception e) {
            return 0;
        }
    }

    public static Drawable getDrawable(Context context, int id) {
        if (context == null) {
            return null;
        }
        try {
            return ContextCompat.getDrawable(context, id);
        } catch (Exception e) {
            return null;
        }
    }
}
