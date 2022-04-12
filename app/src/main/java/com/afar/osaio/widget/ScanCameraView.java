package com.afar.osaio.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.afar.osaio.R;
import com.nooie.sdk.bean.IpcType;
import com.afar.osaio.widget.base.BaseScanCameraView;
import com.afar.osaio.widget.bean.GifBean;
import com.afar.osaio.widget.listener.BaseScanCameraListener;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import pl.droidsonroids.gif.AnimationListener;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class ScanCameraView extends BaseScanCameraView {

    public static final String TAG = "ScanCameraView";

    @BindView(R.id.givScanFrame)
    GifImageView givScamFrame;

    private BaseScanCameraListener mListener;
    private GifDrawable mCam360Loop;
    private GifDrawable mCam360Success;
    private GifDrawable mCam360Failed;

    private Map<String,GifBean> mCamLoopRaws = new HashMap<>();
    private Map<String,GifBean> mCamSuccessRaws = new HashMap<>();
    private Map<String,GifBean> mCamFailedRaws = new HashMap<>();

    public ScanCameraView(Context context) {
        super(context);
        init();
    }

    public ScanCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    public void init() {
        super.init();
        initData();
        //setupScanCameraView();
    }

    @Override
    public int getLayoutId() {
        return R.layout.layout_scam_camera;
    }

    public void initData() {
       /* mCamLoopRaws.put(IpcType.PC420.getType(), new GifBean(R.raw.cam_loop, 0.8f, 0));
        mCamLoopRaws.put(IpcType.PC530.getType(), new GifBean(R.raw.cam_360_loop, 0.8f, 0));
        mCamLoopRaws.put(IpcType.PC730.getType(), new GifBean(R.raw.cam_outdoor_loop, 1.0f, 0));
        mCamSuccessRaws.put(IpcType.PC420.getType(), new GifBean(R.raw.cam_success, 0.8f, 1));
        mCamSuccessRaws.put(IpcType.PC530.getType(), new GifBean(R.raw.cam_360_success, 0.8f, 1));
        mCamSuccessRaws.put(IpcType.PC730.getType(), new GifBean(R.raw.cam_outdoor_success, 1f, 1));
        mCamFailedRaws.put(IpcType.PC420.getType(), new GifBean(R.raw.cam_failed, 0.8f, 1));
        mCamFailedRaws.put(IpcType.PC530.getType(), new GifBean(R.raw.cam_360_failed, 0.8f, 1));
        mCamFailedRaws.put(IpcType.PC730.getType(), new GifBean(R.raw.cam_outdoor_failed, 1f, 1));*/
        mCamLoopRaws.put(IpcType.PC420.getType(), new GifBean(R.raw.scan_loop, 0.8f, 0));
        mCamLoopRaws.put(IpcType.PC530.getType(), new GifBean(R.raw.scan_loop, 0.8f, 0));
        mCamLoopRaws.put(IpcType.PC730.getType(), new GifBean(R.raw.scan_loop, 1.0f, 0));
        mCamSuccessRaws.put(IpcType.PC420.getType(), new GifBean(R.raw.scan_success, 0.8f, 1));
        mCamSuccessRaws.put(IpcType.PC530.getType(), new GifBean(R.raw.scan_success, 0.8f, 1));
        mCamSuccessRaws.put(IpcType.PC730.getType(), new GifBean(R.raw.scan_success, 1f, 1));
        mCamFailedRaws.put(IpcType.PC420.getType(), new GifBean(R.raw.scan_failed, 0.8f, 1));
        mCamFailedRaws.put(IpcType.PC530.getType(), new GifBean(R.raw.scan_failed, 0.8f, 1));
        mCamFailedRaws.put(IpcType.PC730.getType(), new GifBean(R.raw.scan_failed, 1f, 1));
    }

    public void setupScanCameraView(IpcType model) {
        if (model == null || model != IpcType.PC420 || model != IpcType.PC530  || model != IpcType.PC730) {
            model = IpcType.PC420;
        }
        try {
            setupScanLoop(model);
            setupScanFailed(model);
            setupScanSuccess(model);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setupScanLoop(IpcType model) throws Exception {
        //GifBean gifBean = mCamLoopRaws.containsKey(model.getType()) && mCamLoopRaws.get(model.getType()) != null ? mCamLoopRaws.get(model.getType()) : new GifBean(R.raw.cam_360_loop, 1.0f, 0);
        GifBean gifBean = mCamLoopRaws.containsKey(model.getType()) && mCamLoopRaws.get(model.getType()) != null ? mCamLoopRaws.get(model.getType()) : new GifBean(R.raw.scan_loop, 1.0f, 0);
        int rawId = gifBean.getRaw();
        float speed = gifBean.getSpeed();
        int count = gifBean.getCount();
        mCam360Loop = new GifDrawable(getResources(), rawId);
        mCam360Loop.stop();
        givScamFrame.setImageDrawable(mCam360Loop);
        mCam360Loop.setSpeed(speed);
        mCam360Loop.addAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationCompleted(int loopNumber) {
                //NooieLog.d("-->> ScanCameraView loop loop_num=" + loopNumber + " duration=" + mCam360Loop.getDuration());
            }
        });
    }

    public void setupScanFailed(IpcType model) throws Exception {
        //GifBean gifBean = mCamFailedRaws.containsKey(model.getType()) && mCamFailedRaws.get(model.getType()) != null ? mCamFailedRaws.get(model.getType()) : new GifBean(R.raw.cam_360_failed, 1.0f, 0);
        GifBean gifBean = mCamFailedRaws.containsKey(model.getType()) && mCamFailedRaws.get(model.getType()) != null ? mCamFailedRaws.get(model.getType()) : new GifBean(R.raw.scan_failed, 1.0f, 0);
        int rawId = gifBean.getRaw();
        float speed = gifBean.getSpeed();
        int count = gifBean.getCount();
        mCam360Failed = new GifDrawable(getResources(), rawId);
        mCam360Failed.stop();
        mCam360Failed.setLoopCount(count);
        mCam360Failed.setSpeed(speed);
        mCam360Failed.addAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationCompleted(int loopNumber) {
                //NooieLog.d("-->> ScanCameraView failed loop_num=" + loopNumber + " duration=" + mCam360Failed.getDuration());
                if (mListener != null) {
                    mListener.onScanFailed();
                }
            }
        });
    }

    public void setupScanSuccess(IpcType model) throws Exception {
        //GifBean gifBean = mCamSuccessRaws.containsKey(model.getType()) && mCamSuccessRaws.get(model.getType()) != null ? mCamSuccessRaws.get(model.getType()) : new GifBean(R.raw.cam_360_success, 1.0f, 0);
        GifBean gifBean = mCamSuccessRaws.containsKey(model.getType()) && mCamSuccessRaws.get(model.getType()) != null ? mCamSuccessRaws.get(model.getType()) : new GifBean(R.raw.scan_success, 1.0f, 0);
        int rawId = gifBean.getRaw();
        float speed = gifBean.getSpeed();
        int count = gifBean.getCount();
        mCam360Success = new GifDrawable(getResources(), rawId);
        mCam360Success.stop();
        mCam360Success.setLoopCount(count);
        mCam360Success.setSpeed(speed);
        mCam360Success.addAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationCompleted(int loopNumber) {
                //NooieLog.d("-->> ScanCameraView success loop_num=" + loopNumber + " duration=" + mCam360Success.getDuration());
                if (mListener != null) {
                    mListener.onScanSuccess();
                }
            }
        });
    }

    @Override
    public void startScanLoop() {
        if (mCam360Loop != null) {
            mCam360Loop.start();
        }
    }

    @Override
    public void stopScanLoop() {
        if (mCam360Loop != null) {
            mCam360Loop.stop();
        }
    }

    @Override
    public void startScanSuccess() {
        if (mCam360Success != null) {
            givScamFrame.setImageDrawable(mCam360Success);
            mCam360Success.start();
        }
    }

    @Override
    public void startScanFailed() {
        if (mCam360Failed != null) {
            givScamFrame.setImageDrawable(mCam360Failed);
            mCam360Failed.start();
        }
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
        if (mCam360Loop != null) {
            mCam360Loop.stop();
            mCam360Loop.recycle();
        }
        if (mCam360Success != null) {
            mCam360Success.stop();
            mCam360Success.recycle();
        }
        if (mCam360Failed != null) {
            mCam360Failed.stop();
            mCam360Failed.recycle();
        }
    }

    @Override
    public void release() {
        mListener = null;
        closeScan();
        mCam360Loop = null;
        mCam360Success = null;
        mCam360Failed = null;
        if (mCamLoopRaws != null) {
            mCamLoopRaws.clear();
            mCamLoopRaws = null;
        }
        if (mCamSuccessRaws != null) {
            mCamSuccessRaws.clear();
            mCamSuccessRaws = null;
        }
        if (mCamFailedRaws != null) {
            mCamFailedRaws.clear();
            mCamFailedRaws = null;
        }
        givScamFrame = null;
    }

    @Override
    public void setListener(BaseScanCameraListener listener) {
        mListener = listener;
    }
}
