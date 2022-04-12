package com.afar.osaio.widget.base;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.afar.osaio.widget.listener.BaseScanCameraListener;

import butterknife.ButterKnife;

abstract public class BaseScanCameraView extends FrameLayout {

    public BaseScanCameraListener mListener;

    public BaseScanCameraView(Context context) {
        super(context);
    }

    public BaseScanCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init() {
        View view = LayoutInflater.from(getContext()).inflate(getLayoutId(), this, false);
        ButterKnife.bind(this, view);
        addView(view);
    }

    public void setListener(BaseScanCameraListener listener) {
        mListener = listener;
    }

    abstract public int getLayoutId();
    abstract public void startScanLoop();
    abstract public void stopScanLoop();
    abstract public void startScanSuccess();
    abstract public void startScanFailed();
    abstract public void stopScan(boolean success);
    abstract public void closeScan();
    abstract public void release();
}
