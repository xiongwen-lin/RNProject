package com.afar.osaio.smart.scan.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.home.activity.HomeActivity;
import com.afar.osaio.smart.lpipc.contract.DeviceScanCodeContract;
import com.afar.osaio.smart.lpipc.presenter.DeviceScanCodePresenter;
import com.afar.osaio.smart.player.activity.NooiePlayActivity;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.afar.osaio.widget.helper.ResHelper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.nooie.common.hardware.camera.CameraCompatHelper;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.tool.TaskUtil;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.uuzuche.lib_zxing.camera.CameraManager;
import com.uuzuche.lib_zxing.decoding.BaseCaptureActivityHandler;
import com.uuzuche.lib_zxing.decoding.InactivityTimer;
import com.uuzuche.lib_zxing.decoding.delegate.BaseCaptureDelegate;
import com.uuzuche.lib_zxing.view.ViewfinderView;

import java.io.IOException;
import java.util.Vector;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ScanCodeActivity extends BaseActivity implements DeviceScanCodeContract.View, SurfaceHolder.Callback, BaseCaptureDelegate {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.tvSwitchPhoneLight)
    TextView tvSwitchPhoneLight;
    @BindView(R.id.tvGotoEnterDeviceId)
    TextView tvGotoEnterDeviceId;
    @BindView(R.id.ivScanCodeGuide)
    ImageView ivScanCodeGuide;
    private AlertDialog mAddGatewayResultDialog;

    private DeviceScanCodeContract.Presenter mPresenter;

    private BaseCaptureActivityHandler handler;
    @BindView(R.id.vfvScanCodeFinder)
    public ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;
    @BindView(R.id.svScanCodePreview)
    public SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Camera camera;

    public void restartScan() {
        viewfinderView.start();
        // TODO start scan
    }

    public void stopScan() {
        viewfinderView.stop();
        camera.startPreview();

        // TODO stop scan
    }

    public static void toScanCodeActivity(Context from, String model) {
        Intent intent = new Intent(from, ScanCodeActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_IPC_MODEL, model);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_code);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    private void initData() {
        new DeviceScanCodePresenter(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.scan_code_title);
       /* tvGotoEnterDeviceId.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        tvSwitchPhoneLight.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);*/
        tvSwitchPhoneLight.setTag(FLASH_LIGHT_CLOSE);
        tvSwitchPhoneLight.setText(R.string.device_scan_code_switch_phone_light_on);
        String model = getCurrentIntent() != null ? getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_IPC_MODEL) : "";
        ivScanCodeGuide.setImageResource(ResHelper.getInstance().getDeviceScanCodeGuideByType(model));
        setupScanView();
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeData();
    }

    private void resumeData() {
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;

        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;
    }

    private void setupScanView() {
        CameraManager.init(getApplication());

        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
        surfaceHolder = surfaceView.getHolder();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.destroy();
        }
        if (inactivityTimer != null) {
            inactivityTimer.shutdown();
            inactivityTimer = null;
        }
        if (camera != null) {
            camera = null;
        }
        hideAddGatewayResultDialog();
        releaseRes();
    }

    @Override
    public void releaseRes() {
        super.releaseRes();
        if (mediaPlayer !=  null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        ivLeft = null;
        tvTitle = null;
        tvSwitchPhoneLight = null;
        ivScanCodeGuide = null;
        viewfinderView = null;
        surfaceHolder = null;
        surfaceView = null;
    }

    @OnClick({R.id.ivLeft, R.id.tvSwitchPhoneLight, R.id.tvGotoEnterDeviceId})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.tvSwitchPhoneLight:
                switchPhoneLight();
                break;
            case R.id.tvGotoEnterDeviceId:
                InputDeviceIdActivity.toInputDeviceIdActivity(this, ConstantValue.REQUEST_CODE_INPUT_DEVICE_ID);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ConstantValue.REQUEST_CODE_INPUT_DEVICE_ID && resultCode == RESULT_OK) {
            String deviceId = data != null && data.getBundleExtra(ConstantValue.INTENT_KEY_DATA_PARAM) != null ? data.getBundleExtra(ConstantValue.INTENT_KEY_DATA_PARAM).getString(ConstantValue.INTENT_KEY_DEVICE_ID) : "";
            TaskUtil.delayAction(800, new TaskUtil.OnDelayTimeFinishListener() {
                @Override
                public void onFinish() {
                    if (isDestroyed()) {
                        return;
                    }
                    if (!TextUtils.isEmpty(deviceId) && mPresenter != null) {
                        showLoading();
                        mPresenter.bindDevice(deviceId);
                    }
                }
            });
        }
    }

    private static final int FLASH_LIGHT_CLOSE = 0;
    private static final int FLASH_LIGHT_OPEN = 1;
    private void switchPhoneLight() {
        if (tvSwitchPhoneLight == null || tvSwitchPhoneLight.getTag() == null) {
            return;
        }

        if ((Integer)tvSwitchPhoneLight.getTag() == FLASH_LIGHT_OPEN) {
            tvSwitchPhoneLight.setTag(FLASH_LIGHT_CLOSE);
            tvSwitchPhoneLight.setText(R.string.device_scan_code_switch_phone_light_on);
            switchFlashLight(false);
        } else {
            tvSwitchPhoneLight.setTag(FLASH_LIGHT_OPEN);
            tvSwitchPhoneLight.setText(R.string.device_scan_code_switch_phone_light_off);
            switchFlashLight(true);
        }
    }

    @Override
    public void setPresenter(@NonNull DeviceScanCodeContract.Presenter presenter) {
        mPresenter = presenter;
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
            camera = CameraManager.get().getCamera();
        } catch (Exception e) {
            e.printStackTrace();
            NooieLog.d("-->> DeviceScanCodeActivity initCamera failed");
            return;
        }
        if (handler == null) {
            handler = new BaseCaptureActivityHandler(this, decodeFormats, characterSet, viewfinderView);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
        if (camera != null) {
            if (camera != null && CameraManager.get().isPreviewing()) {
                if (!CameraManager.get().isUseOneShotPreviewCallback()) {
                    camera.setPreviewCallback(null);
                }
                camera.stopPreview();
                CameraManager.get().getPreviewCallback().setHandler(null, 0);
                CameraManager.get().getAutoFocusCallback().setHandler(null, 0);
                CameraManager.get().setPreviewing(false);
            }
        }
    }

    /**
     * Handler scan result
     *
     * @param result
     * @param barcode
     */
    @Override
    public void handleDecode(Result result, Bitmap barcode) {
        if (isDestroyed()) {
            return;
        }
        if (inactivityTimer != null) {
            inactivityTimer.onActivity();
        }
        playBeepSoundAndVibrate();

        if (result == null || TextUtils.isEmpty(result.getText())) {
            NooieLog.d("-->> DeviceScanCodeActivity handleDecode get result faild");
        } else {
            NooieLog.d("-->> DeviceScanCodeActivity handleDecode get result text=" + result.getText());
            tryToAddGateway(result);
        }
    }

    @Override
    public Handler getHandler() {
        return handler;
    }

    @Override
    public void drawViewfinder() {
        if (isDestroyed() || checkNull(viewfinderView)) {
            return;
        }
        viewfinderView.drawViewfinder();
    }

    @Override
    public void onGetScanResult(int resultCode, Intent data) {
    }

    @Override
    public void onGetQueryMessage(Intent data) {
    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(
                    com.uuzuche.lib_zxing.R.raw.qrcode_completed);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final MediaPlayer.OnCompletionListener beepListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };

    private void switchFlashLight(boolean openOrClose) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //CameraCompatHelper.changeFlashLight(NooieApplication.mCtx, openOrClose);
        } else {
            //CameraCompatHelper.changeFlashLight(camera, openOrClose);
        }
        CameraCompatHelper.changeFlashLight(camera, openOrClose);
    }

    private void tryToAddGateway(Result result) {
        if (result != null && !TextUtils.isEmpty(result.getText())) {
            String uuid = result.getText();
            if (!TextUtils.isEmpty(uuid) && mPresenter != null) {
                showLoading();
                mPresenter.bindDevice(uuid);
                return;
            }
        }

        showAddGatewayFailedDialog(StateCode.UNKNOWN.code);
    }

    private String getUuidFromRQCode(String result) {
        String uuid = "";
        try {
            if (result != null && result.contains("uuid:")) {
                uuid = result.substring(result.indexOf(":") + 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uuid;
    }

    private void showAddGatewayFailedDialog(int code) {
        hideAddGatewayResultDialog();
        if (code == StateCode.DEVICE_BINDED.code) {
            mAddGatewayResultDialog = DialogUtils.showInformationDialog(this, getString(R.string.scan_code_add_gateway_other_title), getString(R.string.scan_code_add_gateway_other), getString(R.string.ok), false, false, new DialogUtils.OnClickInformationDialogLisenter() {
                @Override
                public void onConfirmClick() {
                    finish();
                }
            });
        } else if (code == StateCode.UUID_NOT_EXISTED.code) {
            mAddGatewayResultDialog = DialogUtils.showInformationDialog(this, getString(R.string.scan_code_add_gateway_failed_title), getString(R.string.device_id_invalid_tip), getString(R.string.ok), false, false, new DialogUtils.OnClickInformationDialogLisenter() {
                @Override
                public void onConfirmClick() {
                    finish();
                }
            });
        } else {
            mAddGatewayResultDialog = DialogUtils.showInformationDialog(this, getString(R.string.scan_code_add_gateway_failed_title), getString(R.string.scan_code_add_gateway_again), getString(R.string.ok), false, false, new DialogUtils.OnClickInformationDialogLisenter() {
                @Override
                public void onConfirmClick() {
                    finish();
                }
            });
        }
    }

    private void hideAddGatewayResultDialog() {
        if (mAddGatewayResultDialog != null) {
            mAddGatewayResultDialog.dismiss();
            mAddGatewayResultDialog = null;
        }
    }

    @Override
    public void notifyBindGatewayResult(String result, String deviceId, int code) {
        hideLoading();
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            HomeActivity.toHomeActivity(this);
            finish();
        } else if (code == StateCode.DEVICE_BOUND_BY_SELF.code || NooieDeviceHelper.getDeviceById(deviceId) != null) {
            BindDevice device = NooieDeviceHelper.getDeviceById(deviceId);
            if (device != null && device.getOnline() == ApiConstant.ONLINE_STATUS_ON) {
                //NooiePlayActivity.startPlayActivity(this, deviceId, IpcType.getIpcType(device.getType()), ConstantValue.NOOIE_PLAYBACK_TYPE_LIVE, ConstantValue.NOOIE_PLAYBACK_SOURCE_TYPE_NORMAL, 0, ConstantValue.ROUTE_SOURCE_ADD_DEVICE, ConstantValue.CONNECTION_MODE_QC);
                NooiePlayActivity.startPlayActivity(this, deviceId, device.getType(), ConstantValue.NOOIE_PLAYBACK_TYPE_LIVE, ConstantValue.NOOIE_PLAYBACK_SOURCE_TYPE_NORMAL, 0, ConstantValue.ROUTE_SOURCE_ADD_DEVICE, ConstantValue.CONNECTION_MODE_QC, new String());
            } else {
                HomeActivity.toHomeActivity(this);
            }
            finish();
        } else {
            showAddGatewayFailedDialog(code);
        }
    }
}
