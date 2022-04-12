package com.afar.osaio.util;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.afar.osaio.base.NooieApplication;

public class GlideUtil {

    public static void loadImage(Context context, Object source, RequestOptions options, ImageView view) {
        if (context == null) {
            return;
        }
        Glide.with(context).load(source).apply(options).into(view);
    }

    public static void loadImageNormal(Context context, Object source, int errorId, ImageView view) {
        loadImage(context, source, new RequestOptions().error(errorId), view);
    }

    /**
     * Must put it in subThread thread
     */
    public static void clearMemoryCache() {
        Glide.get(NooieApplication.mCtx).clearMemory();
    }

    /**
     * FUCK: Must put it in Main thread
     */
    public static void clearDiskCache() {
        Glide.get(NooieApplication.mCtx).clearDiskCache();
    }
}
