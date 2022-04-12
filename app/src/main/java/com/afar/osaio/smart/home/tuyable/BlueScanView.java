package com.afar.osaio.smart.home.tuyable;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import com.afar.osaio.R;
import com.afar.osaio.widget.base.BaseScanCameraView;
import com.afar.osaio.widget.bean.GifBean;
import com.afar.osaio.widget.listener.BaseScanCameraListener;
import com.nooie.sdk.bean.IpcType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import pl.droidsonroids.gif.AnimationListener;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class BlueScanView extends BaseScanCameraView {

    public static final String TAG = "ScanCameraView";

    @BindView(R.id.givScanFrame)
    GifImageView givScamFrame;

    private BaseScanCameraListener mListener;
    private GifDrawable blueScanGif;


    private Map<String,GifBean> mCamLoopRaws = new HashMap<>();


    public BlueScanView(Context context) {
        super(context);
        init();
    }

    public BlueScanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    public void init() {
        super.init();

        //setupScanCameraView();
    }

    @Override
    public int getLayoutId() {
        return R.layout.layout_scam_camera;
    }



    public void setupScanView()  {
        //GifBean gifBean = mCamLoopRaws.containsKey(model.getType()) && mCamLoopRaws.get(model.getType()) != null ? mCamLoopRaws.get(model.getType()) : new GifBean(R.raw.cam_360_loop, 1.0f, 0);
        GifBean gifBean =new GifBean(R.raw.illus_connect_search, 1f, 0);
        int rawId = gifBean.getRaw();
        float speed = gifBean.getSpeed();
        int count = gifBean.getCount();

        try {
            blueScanGif = new GifDrawable(getResources(), rawId);
            givScamFrame.setImageDrawable(blueScanGif);
            blueScanGif.setSpeed(speed);
            blueScanGif.start();
        }catch (Exception e){
            Log.e("setupScanView","AddTuYaBlePopupWindows--setupScanView()--failed" +e.getMessage());
        }

    }



    @Override
    public void startScanLoop() {
        if (blueScanGif != null) {
            blueScanGif.start();
        }
    }

    @Override
    public void stopScanLoop() {
        if (blueScanGif != null) {
            blueScanGif.stop();
        }
    }

    @Override
    public void startScanSuccess() {

    }

    @Override
    public void startScanFailed() {

    }


    @Override
    public void stopScan(boolean success) {
        stopScanLoop();
        if (success) {
            startScanSuccess();
        } else {
            startScanFailed();
        }
    }

    @Override
    public void closeScan() {
        if (blueScanGif != null) {
            blueScanGif.stop();
            blueScanGif.recycle();
        }
    }

    @Override
    public void release() {
        mListener = null;
        closeScan();
        blueScanGif = null;

        if (mCamLoopRaws != null) {
            mCamLoopRaws.clear();
            mCamLoopRaws = null;
        }

        givScamFrame = null;
    }

    @Override
    public void setListener(BaseScanCameraListener listener) {
        mListener = listener;
    }
}
