package com.afar.osaio.util;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import androidx.annotation.NonNull;

import com.uuzuche.lib_zxing.activity.CodeUtils;

/**
 * Created by victor on 2018/7/10
 * Email is victor.qiao.0604@gmail.com
 */
public class QRCodeAsyncTask extends AsyncTask<String, Void, Bitmap> {
    private OnLoadFinishListener listener;

    public QRCodeAsyncTask(@NonNull OnLoadFinishListener listener) {
        this.listener = listener;
    }

    public interface OnLoadFinishListener {
        void onLoadBitmap(Bitmap bitmap);
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
        return CodeUtils.createImage(strings[0], 540, 540, null);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        listener.onLoadBitmap(bitmap);
    }
}
