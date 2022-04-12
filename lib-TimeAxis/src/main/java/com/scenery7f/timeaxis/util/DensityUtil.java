package com.scenery7f.timeaxis.util;

import android.content.Context;

import java.lang.ref.WeakReference;

/**
 * Created by snoopy on 2017/9/15.
 */
public class DensityUtil {
    private static float density = 1;
    private static WeakReference<Context> weakReference;

    public static void setContext(Context context) {
        weakReference = new WeakReference<>(context);
        if (weakReference.get() != null) {
            density = weakReference.get().getResources().getDisplayMetrics().density;
        }
    }

    public static int dip2px(float dipValue){
        return (int)(dipValue * density + 0.5f);
    }

    public static int px2dip(float pxValue){
        return density == 0 ? (int)pxValue : (int)(pxValue / density + 0.5f);
    }
}
