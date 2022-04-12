package com.afar.osaio.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.afar.osaio.R;
import com.afar.osaio.widget.base.BaseScanCameraView;
import com.afar.osaio.widget.bean.GifBean;
import com.afar.osaio.widget.listener.BaseScanCameraListener;
import com.nooie.common.utils.log.NooieLog;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import pl.droidsonroids.gif.AnimationListener;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class ScanBleDeviceView extends BaseScanCameraView {

    public static final int TYPE_SCAN_BLE_DEVICE_ANIM_DEFAULT = 0;

    public static final String TAG = "ScanCameraView";

    @BindView(R.id.givScanFrame)
    GifImageView givScamFrame;

    private BaseScanCameraListener mListener;
    private GifDrawable mGifLoop;

    private Map<Integer,GifBean> mCamLoopRaws = new HashMap<>();

    public ScanBleDeviceView(Context context) {
        super(context);
        init();
    }

    public ScanBleDeviceView(Context context, AttributeSet attrs) {
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
        mCamLoopRaws.put(TYPE_SCAN_BLE_DEVICE_ANIM_DEFAULT, new GifBean(R.raw.cam_360_loop, 0.8f, 0));
    }

    public void setupScanAnimView(int type) {
        try {
            setupScanLoop(type);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setupScanLoop(int type) throws Exception {
        GifBean gifBean = mCamLoopRaws.containsKey(type) && mCamLoopRaws.get(type) != null ? mCamLoopRaws.get(type) : new GifBean(R.raw.cam_360_loop, 1.0f, 0);
        int rawId = gifBean.getRaw();
        float speed = gifBean.getSpeed();
        int count = gifBean.getCount();
        mGifLoop = new GifDrawable(getResources(), rawId);
        mGifLoop.stop();
        givScamFrame.setImageDrawable(mGifLoop);
        mGifLoop.setSpeed(speed);
        mGifLoop.addAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationCompleted(int loopNumber) {
                NooieLog.d("-->> ScanBleDeviceView onAnimationCompleted loopNumber=" + loopNumber + " anim state=" + mAnimState);
                if (mAnimState == ANIM_STATE_STOP) {
                    stopScanLoop();
                }
            }
        });
    }

    @Override
    public void startScanLoop() {
        mAnimState = ANIM_STATE_LOOP;
        if (mGifLoop != null) {
            mGifLoop.start();
        }
    }

    public static final int ANIM_STATE_LOOP = 1;
    public static final int ANIM_STATE_STOP = 2;
    private int mAnimState = ANIM_STATE_LOOP;
    @Override
    public void stopScanLoop() {
        mAnimState = ANIM_STATE_STOP;
        if (mGifLoop != null) {
            mGifLoop.stop();
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
        mAnimState = ANIM_STATE_STOP;
    }

    @Override
    public void release() {
        if (mGifLoop != null) {
            mGifLoop.stop();
            mGifLoop.recycle();
        }
    }

    @Override
    public void setListener(BaseScanCameraListener listener) {
        mListener = listener;
    }
}
