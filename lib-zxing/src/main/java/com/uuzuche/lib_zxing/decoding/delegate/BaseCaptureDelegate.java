package com.uuzuche.lib_zxing.decoding.delegate;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;

import com.google.zxing.Result;

public interface BaseCaptureDelegate {

    void handleDecode(Result result, Bitmap barcode);

    void drawViewfinder();

    void onGetScanResult(int resultCode, Intent data);

    void onGetQueryMessage(Intent data);

    Handler getHandler();
}
