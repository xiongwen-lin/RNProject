package com.afar.osaio.smart.setting.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.device.helper.DeviceSettingHelper;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.player.delegate.PlayState;
import com.afar.osaio.smart.setting.adapter.PresetPointAdapter;
import com.afar.osaio.smart.setting.adapter.listener.PresetPointListener;
import com.afar.osaio.smart.setting.component.PresetPointSortComponent;
import com.afar.osaio.smart.setting.contract.PresetPointContract;
import com.afar.osaio.smart.setting.presenter.PresetPointPresenter;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.widget.InputFrameView;
import com.afar.osaio.widget.NormalTextAndIconInputDialog;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.file.FileUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.api.network.base.bean.entity.PresetPointConfigure;
import com.nooie.sdk.bean.IpcType;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.listener.OnActionResultListener;
import com.nooie.sdk.listener.OnPlayerListener;
import com.nooie.sdk.media.NooieMediaPlayer;
import com.nooie.sdk.media.listener.PlayerGestureListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PresetPointActivity extends BaseActivity implements PresetPointContract.View, OnPlayerListener {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.player)
    NooieMediaPlayer player;
    @BindView(R.id.ivDirectionControlBg)
    ImageView ivDirectionControlBg;
    @BindView(R.id.ivGestureLeftArrow)
    ImageView ivGestureLeftArrow;
    @BindView(R.id.ivGestureTopArrow)
    ImageView ivGestureTopArrow;
    @BindView(R.id.ivGestureRightArrow)
    ImageView ivGestureRightArrow;
    @BindView(R.id.ivGestureBottomArrow)
    ImageView ivGestureBottomArrow;
    @BindView(R.id.ivPresetPointPreviewBg)
    ImageView ivPresetPointPreviewBg;
    @BindView(R.id.ivPresetPointAdd)
    ImageView ivPresetPointAdd;
    @BindView(R.id.tvPresetPointAddTip)
    TextView tvPresetPointAddTip;
    @BindView(R.id.vPresetPointContainer)
    View vPresetPointContainer;
    @BindView(R.id.rvPresetPointList)
    RecyclerView rvPresetPointList;

    private PresetPointContract.Presenter mPresenter;
    private String mDeviceId;
    private PlayState mPlayerState;
    private PresetPointAdapter mPresetPointAdapter;
    private PresetPointSortComponent mPresetPointSortComponent;

    public static void toPresetPointActivity(Context from, String deviceId, String model) {
        Intent intent = new Intent(from, PresetPointActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        intent.putExtra(ConstantValue.INTENT_KEY_IPC_MODEL, model);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSlideable(false);
        setContentView(R.layout.activity_preset_point);
        ButterKnife.bind(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initData();
        initView();
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
            return;
        }

        mDeviceId = getIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
        BindDevice device = !TextUtils.isEmpty(mDeviceId) ? getDevice(mDeviceId) : null;
        if (device == null) {
            finish();
            return;
        }
        new PresetPointPresenter(this);
        mPlayerState = new PlayState(PlayState.PLAY_TYPE_LIVE, PlayState.PLAY_STATE_INIT, System.currentTimeMillis());
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText((getDevice(mDeviceId) != null ?  getDevice(mDeviceId).getName() : new String()));
        setPlayPtzCtrl();
        setPresetPointList();
        if (mPresenter != null) {
            mPresenter.getPresetPoints(mDeviceId, true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeData();
    }

    private void resumeData() {
        preparePlayer();
        startVideo(mDeviceId);
    }

    @Override
    public void onPause() {
        super.onPause();
        resetVideo(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.destroy();
        }
        if (player != null) {
            player.destroy();
            player = null;
        }
        hideLoading();
        hideDeletePresetPointDialog();
        hidePresetPointEditView();
        releaseRes();
    }

    @Override
    public void releaseRes() {
        super.releaseRes();
        ivLeft = null;
        tvTitle = null;
        ivDirectionControlBg = null;
        ivGestureLeftArrow = null;
        ivGestureTopArrow = null;
        ivGestureRightArrow = null;
        ivGestureBottomArrow = null;
        ivPresetPointPreviewBg = null;
        ivPresetPointAdd = null;
        tvPresetPointAddTip = null;
        vPresetPointContainer = null;
        if (rvPresetPointList != null) {
            rvPresetPointList.setAdapter(null);
        }
        if (mPresetPointAdapter != null) {
            mPresetPointAdapter.release();
        }
        if (mPresetPointSortComponent != null) {
            mPresetPointSortComponent.release();
        }
        rvPresetPointList = null;
        mPresetPointAdapter = null;
        mPresetPointSortComponent = null;
    }

    @OnClick({R.id.ivLeft, R.id.ivPresetPointAdd})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                if (isPlayStarting()) {
                    showPlayerIsReleasing(true);
                    break;
                }
                finish();
                break;
            case R.id.ivPresetPointAdd:
                startScreenShot(mUserAccount, mDeviceId, 1);
                break;
        }
    }

    @Override
    public boolean onKeyUp(int code, KeyEvent event) {
        if (isPlayStarting()) {
            showPlayerIsReleasing(true);
            return true;
        }
        return super.onKeyUp(code, event);
    }

    @Override
    public void setPresenter(@NonNull PresetPointContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onVideoStart(NooieMediaPlayer player) {
    }

    @Override
    public void onVideoStop(NooieMediaPlayer player) {
    }

    @Override
    public void onAudioStart(NooieMediaPlayer player) {
    }

    @Override
    public void onAudioStop(NooieMediaPlayer player) {
    }

    @Override
    public void onTalkingStart(NooieMediaPlayer player) {
    }

    @Override
    public void onTalkingStop(NooieMediaPlayer player) {
    }

    @Override
    public void onRecordStart(NooieMediaPlayer player, boolean result, String file) {
    }

    @Override
    public void onRecordStop(NooieMediaPlayer player, boolean result, String file) {
    }

    @Override
    public void onRecordTimer(NooieMediaPlayer player, int duration) {
    }

    @Override
    public void onSnapShot(NooieMediaPlayer player, boolean result, String path) {
        if (isDestroyed()) {
            return;
        }
        NooieLog.d("-->> debug PresetPointActivity onSnapShot: result=" + result + " path=" + path + " mCurrentScreenShotPath=" + mCurrentScreenShotPath);
        if (result && !TextUtils.isEmpty(mCurrentScreenShotPath) && mCurrentScreenShotPath.equalsIgnoreCase(path)) {
            if (mPresenter != null) {
                mPresenter.startCompressScreenShot(mUserAccount, mDeviceId, path);
            } else {
                hideLoading();
            }
        } else {
            hideLoading();
            ToastUtil.showToast(this, R.string.preset_point_shot_icon_fail);
        }
    }

    @Override
    public void onFps(NooieMediaPlayer player, int fps) {
    }

    @Override
    public void onBitrate(NooieMediaPlayer player, double bitrate) {
    }

    @Override
    public void onBufferingStart(NooieMediaPlayer player) {
    }

    @Override
    public void onBufferingStop(NooieMediaPlayer player) {
    }

    @Override
    public void onPlayFinish(NooieMediaPlayer player) {
    }

    @Override
    public void onPlayOneFinish(NooieMediaPlayer player) {
    }

    @Override
    public void onPlayFileBad(NooieMediaPlayer player) {
    }

    @Override
    public void onCompressScreenShot(String account, String deviceId, String thumbnailPath) {
        if (isDestroyed() || !checkIsCurrentDevice(deviceId)) {
            return;
        }
        hideLoading();
        if (!TextUtils.isEmpty(mCurrentScreenShotPath) && mCurrentScreenShotPath.equalsIgnoreCase(thumbnailPath)) {
            showPresetPointEditView(mCurrentScreenShotPath);
        }
    }

    @Override
    public void onGetPresetPoints(int result, List<PresetPointConfigure> presetPointConfigures) {
        if (isDestroyed()) {
            return;
        }
        if (result == SDKConstant.SUCCESS) {
            displayPresetPointEditView(CollectionUtil.isNotEmpty(presetPointConfigures));
            if (mPresetPointAdapter != null) {
                mPresetPointAdapter.setData(presetPointConfigures);
            }
        }
    }

    @Override
    public void onCheckAddPresetPointPosition(int result, String name, int position, List<PresetPointConfigure> presetPointConfigures) {
        NooieLog.d("-->> debug PresetPointActivity onCheckAddPresetPointPosition: 1000 result=" + result);
        if (isDestroyed()) {
            return;
        }
        boolean isPresetPointFull = result == SDKConstant.SUCCESS && CollectionUtil.size(presetPointConfigures) >= DeviceSettingHelper.PRESET_POINT_MAX_LEN;
        NooieLog.d("-->> debug PresetPointActivity onCheckAddPresetPointPosition: 1001");
        if (isPresetPointFull) {
            if (mPresenter != null) {
                mPresenter.getPresetPoints(mDeviceId, false);
            }
            return;
        }
        if (result == SDKConstant.SUCCESS) {
            boolean isSetPowerOnPresetPoint = CollectionUtil.isEmpty(presetPointConfigures);
            int correctPosition = DeviceSettingHelper.getCorrectPresetPointPosition(position, presetPointConfigures);
            NooieLog.d("-->> debug PresetPointActivity onCheckAddPresetPointPosition: 1002 position=" + position + " correctPosition=" + correctPosition);
            if (mPresenter != null) {
                mPresenter.addPresetPoint(mUserAccount, mDeviceId, isSetPowerOnPresetPoint, name, correctPosition, FileUtil.getTempPresetPointThumbnail(NooieApplication.mCtx, mUserAccount, mDeviceId, position), FileUtil.getPresetPointThumbnail(NooieApplication.mCtx, mUserAccount, mDeviceId, correctPosition));
            }
        } else {
        }
    }

    @Override
    public void onAddPresetPoint(int result, List<PresetPointConfigure> presetPointConfigures) {
        if (isDestroyed()) {
            return;
        }
        hideLoading();
        if (result == SDKConstant.SUCCESS) {
            displayPresetPointEditView(CollectionUtil.isNotEmpty(presetPointConfigures));
            if (mPresetPointAdapter != null) {
                mPresetPointAdapter.setData(presetPointConfigures);
            }
        }
    }

    @Override
    public void onSortPresetPointConfigureList(int result, String deviceId) {
        if (isDestroyed() && !checkIsCurrentDevice(deviceId)) {
            return;
        }
        hideLoading();
        if (result == SDKConstant.SUCCESS) {
        } else if (mPresenter != null) {
            mPresenter.getPresetPoints(deviceId, false);
        }
    }

    @Override
    public void onEditPresetPointConfigure(int result, String deviceId, PresetPointConfigure presetPointConfigure) {
        if (isDestroyed()) {
            return;
        }
        hideLoading();
        if (result == SDKConstant.SUCCESS) {
            if (mPresetPointAdapter != null) {
                mPresetPointAdapter.updateItemName(presetPointConfigure);
            }
        }
    }

    @Override
    public void onDeletePresetPointConfigure(int result, String deviceId, PresetPointConfigure presetPointConfigure) {
        if (isDestroyed() || !checkIsCurrentDevice(deviceId)) {
            return;
        }
        hideLoading();
        if (result == SDKConstant.SUCCESS) {
            if (presetPointConfigure != null && mPresetPointAdapter != null) {
                mPresetPointAdapter.deleteItemByPosition(presetPointConfigure);
                displayPresetPointEditView(CollectionUtil.isNotEmpty(mPresetPointAdapter.getData()));
            }
        } else {
        }
    }

    @Override
    public void onTurnPresetPoint(int result, String deviceId) {
        if (isDestroyed()) {
            return;
        }
        hideLoading();
    }

    private void preparePlayer() {
        player.setPlayerListener(this);
        updatePlayerState(PlayState.PLAY_STATE_INIT);
    }

    private void startVideo(String deviceId) {
        BindDevice device = getDevice(deviceId);
        if (player == null || device == null) {
            return;
        }
        boolean isOn = device != null ? (device.getOnline() == ApiConstant.ONLINE_STATUS_ON && device.getOpen_status() == ApiConstant.OPEN_STATUS_ON) : false;
        if (!isOn) {
            return;
        }
        int modelType = NooieDeviceHelper.convertNooieModel(IpcType.getIpcType(device.getType()), device.getType());
        int streamType = NooieDeviceHelper.convertStreamType(IpcType.getIpcType(device.getType()), device.getType());
        NooieLog.d("-->> NooiePlayActivity startVideo deviceId=" + mDeviceId + " modelType=" + modelType + " streamType=" + streamType);
        updatePlayerState(PlayState.PLAY_STATE_START);
        player.startNooieLive(deviceId, 0, modelType, streamType, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                NooieLog.d("-->> NooiePlayActivity startVideo Live NooieLive onResult deviceId=" + mDeviceId + " code=" + code);
                updatePlayerState(PlayState.PLAY_STATE_FINISH);
            }
        });
    }

    private void resetVideo(boolean destroy) {
        NooieLog.d("-->> NooiePlayActivity resetDefault 1");
        if (player != null) {
            if (destroy) {
                player.setPlayerListener(null);
            }
            player.stop();
        }
        NooieLog.d("-->> NooiePlayActivity resetDefault 2");
    }

    private void setPlayPtzCtrl() {
        setPtzControlOrientation(IpcType.getIpcType(getDeviceModel()));
        displayGestureGuideView(false, ConstantValue.GESTURE_TOUCH_UP);
        player.setGestureListener(new PlayerGestureListener() {
            @Override
            public void onMoveLeft(NooieMediaPlayer player) {
                displayGestureGuideView(true, ConstantValue.GESTURE_MOVE_LEFT);
            }

            @Override
            public void onMoveRight(NooieMediaPlayer player) {
                displayGestureGuideView(true, ConstantValue.GESTURE_MOVE_RIGHT);
            }

            @Override
            public void onMoveUp(NooieMediaPlayer player) {
                displayGestureGuideView(true, ConstantValue.GESTURE_MOVE_TOP);
            }

            @Override
            public void onMoveDown(NooieMediaPlayer player) {
                displayGestureGuideView(true, ConstantValue.GESTURE_MOVE_BOTTOM);
            }

            @Override
            public void onTouchUp(NooieMediaPlayer player) {
                displayGestureGuideView(false, ConstantValue.GESTURE_TOUCH_UP);
            }

            @Override
            public void onTouchDown(NooieMediaPlayer player) {
                displayGestureGuideView(true, ConstantValue.GESTURE_TOUCH_DOWN);
            }

            @Override
            public void onSingleClick(NooieMediaPlayer player) {
            }

            @Override
            public void onDoubleClick(NooieMediaPlayer player) {
            }
        });
    }

    private void setPtzControlOrientation(IpcType type) {
        if (player != null) {
            player.setPtzHorizontal(NooieDeviceHelper.isSupportPtzControlHorizontal(type));
            player.setPtzVertical(NooieDeviceHelper.isSupportPtzControlVertical(type));
        }
    }

    private void displayGestureGuideView(boolean show, int gestureType) {
        if (isDestroyed() || checkNull(ivDirectionControlBg, ivGestureLeftArrow, ivGestureTopArrow, ivGestureRightArrow, ivGestureBottomArrow)) {
            return;
        }

        BindDevice device = getDevice(mDeviceId);
        boolean isOpen = device != null ? (device.getOnline() == ApiConstant.ONLINE_STATUS_ON && device.getOpen_status() == ApiConstant.OPEN_STATUS_ON) : false;
        boolean isSupportPtzControlVertical = NooieDeviceHelper.isSupportPtzControlVertical(IpcType.getIpcType(getDeviceModel()));
        boolean isSupportPtzControlHorizontal = NooieDeviceHelper.isSupportPtzControlHorizontal(IpcType.getIpcType(getDeviceModel()));
        if (!isOpen) {
            return;
        }

        if (!show) {
            ivDirectionControlBg.setVisibility(View.GONE);
            ivGestureLeftArrow.setVisibility(View.GONE);
            ivGestureTopArrow.setVisibility(View.GONE);
            ivGestureRightArrow.setVisibility(View.GONE);
            ivGestureBottomArrow.setVisibility(View.GONE);
            return;
        }

        switch(gestureType) {
            case ConstantValue.GESTURE_TOUCH_DOWN : {
                ivDirectionControlBg.setVisibility(View.VISIBLE);
                ivGestureLeftArrow.setImageResource(R.drawable.gesture_left_arrow);
                ivGestureTopArrow.setImageResource(R.drawable.gesture_left_arrow);
                ivGestureRightArrow.setImageResource(R.drawable.gesture_left_arrow);
                ivGestureBottomArrow.setImageResource(R.drawable.gesture_left_arrow);
                ivGestureLeftArrow.setVisibility(isSupportPtzControlHorizontal ? View.VISIBLE : View.GONE);
                ivGestureTopArrow.setVisibility(isSupportPtzControlVertical ? View.VISIBLE : View.GONE);
                ivGestureRightArrow.setVisibility(isSupportPtzControlHorizontal ? View.VISIBLE : View.GONE);
                ivGestureBottomArrow.setVisibility(isSupportPtzControlVertical ? View.VISIBLE : View.GONE);
                break;
            }
            case ConstantValue.GESTURE_TOUCH_UP : {
                ivDirectionControlBg.setVisibility(View.GONE);
                ivGestureLeftArrow.setImageResource(R.drawable.gesture_left_arrow);
                ivGestureTopArrow.setImageResource(R.drawable.gesture_left_arrow);
                ivGestureRightArrow.setImageResource(R.drawable.gesture_left_arrow);
                ivGestureBottomArrow.setImageResource(R.drawable.gesture_left_arrow);
                ivGestureLeftArrow.setVisibility(View.GONE);
                ivGestureTopArrow.setVisibility(View.GONE);
                ivGestureRightArrow.setVisibility(View.GONE);
                ivGestureBottomArrow.setVisibility(View.GONE);
                break;
            }
            case ConstantValue.GESTURE_MOVE_LEFT : {
                ivGestureLeftArrow.setImageResource(R.drawable.gesture_left__active_arrow);
                ivGestureLeftArrow.setVisibility(isSupportPtzControlHorizontal ? View.VISIBLE : View.GONE);
                ivGestureTopArrow.setVisibility(isSupportPtzControlVertical ? View.VISIBLE : View.GONE);
                ivGestureRightArrow.setVisibility(isSupportPtzControlHorizontal ? View.VISIBLE : View.GONE);
                ivGestureBottomArrow.setVisibility(isSupportPtzControlVertical ? View.VISIBLE : View.GONE);
                break;
            }
            case ConstantValue.GESTURE_MOVE_TOP : {
                ivGestureTopArrow.setImageResource(R.drawable.gesture_left__active_arrow);
                ivGestureLeftArrow.setVisibility(isSupportPtzControlHorizontal ? View.VISIBLE : View.GONE);
                ivGestureTopArrow.setVisibility(isSupportPtzControlVertical ? View.VISIBLE : View.GONE);
                ivGestureRightArrow.setVisibility(isSupportPtzControlHorizontal ? View.VISIBLE : View.GONE);
                ivGestureBottomArrow.setVisibility(isSupportPtzControlVertical ? View.VISIBLE : View.GONE);
                break;
            }
            case ConstantValue.GESTURE_MOVE_RIGHT : {
                ivGestureRightArrow.setImageResource(R.drawable.gesture_left__active_arrow);
                ivGestureLeftArrow.setVisibility(isSupportPtzControlHorizontal ? View.VISIBLE : View.GONE);
                ivGestureTopArrow.setVisibility(isSupportPtzControlVertical ? View.VISIBLE : View.GONE);
                ivGestureRightArrow.setVisibility(isSupportPtzControlHorizontal ? View.VISIBLE : View.GONE);
                ivGestureBottomArrow.setVisibility(isSupportPtzControlVertical ? View.VISIBLE : View.GONE);
                break;
            }
            case ConstantValue.GESTURE_MOVE_BOTTOM : {
                ivGestureBottomArrow.setImageResource(R.drawable.gesture_left__active_arrow);
                ivGestureLeftArrow.setVisibility(isSupportPtzControlHorizontal ? View.VISIBLE : View.GONE);
                ivGestureTopArrow.setVisibility(isSupportPtzControlVertical ? View.VISIBLE : View.GONE);
                ivGestureRightArrow.setVisibility(isSupportPtzControlHorizontal ? View.VISIBLE : View.GONE);
                ivGestureBottomArrow.setVisibility(isSupportPtzControlVertical ? View.VISIBLE : View.GONE);
                break;
            }
        }
    }

    private void displayPresetPointEditView(boolean isHasPresetPoint) {
        if (checkNull(vPresetPointContainer, ivPresetPointPreviewBg, ivPresetPointAdd, tvPresetPointAddTip)) {
            return;
        }
        vPresetPointContainer.setVisibility(isHasPresetPoint ? View.VISIBLE : View.GONE);
        ivPresetPointPreviewBg.setVisibility(isHasPresetPoint ? View.GONE : View.VISIBLE);
        ivPresetPointAdd.setVisibility(isHasPresetPoint ? View.GONE : View.VISIBLE);
        tvPresetPointAddTip.setVisibility(isHasPresetPoint ? View.GONE : View.VISIBLE);
    }

    private String mCurrentScreenShotPath;
    private void startScreenShot(String account, String deviceId, int position) {
        mCurrentScreenShotPath = FileUtil.getTempPresetPointThumbnail(NooieApplication.mCtx, account, deviceId, position);
        if (TextUtils.isEmpty(mCurrentScreenShotPath)) {
            return;
        }
        showLoading();
        player.snapShot(mCurrentScreenShotPath);
    }

    private NormalTextAndIconInputDialog mPresetPointEditDialog = null;
    private void showPresetPointEditView(String presetPointScreenShotPath) {
        hidePresetPointEditView();
        mPresetPointEditDialog = createPresetPointDialog(presetPointScreenShotPath);
        mPresetPointEditDialog.show();
    }

    private void showPresetPointEditView(String account, String deviceId, PresetPointConfigure presetPointConfigure) {
        hidePresetPointEditView();
        mPresetPointEditDialog = createPresetPointDialog(account, deviceId, presetPointConfigure);
        mPresetPointEditDialog.show();
    }

    private void hidePresetPointEditView() {
        if (mPresetPointEditDialog != null) {
            mPresetPointEditDialog.dismiss();
            mPresetPointEditDialog = null;
        }
    }

    private NormalTextAndIconInputDialog createPresetPointDialog(String presetPointScreenShotPath) {
        return createPresetPointDialog(presetPointScreenShotPath, new InputFrameView.OnInputFrameClickListener() {
            @Override
            public void onInputBtnClick() {
            }

            @Override
            public void onEditorAction() {
                tryCheckAddPresetPointPosition();
            }

            @Override
            public void onEtInputClick() {
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isDestroyed() || checkNull(view)) {
                    return;
                }
                switch (view.getId()) {
                    case R.id.btnPresetPointCancel:
                        break;
                    case R.id.btnPresetPointConfirm:
                        tryCheckAddPresetPointPosition();
                        break;
                }
            }
        });
    }

    private void tryCheckAddPresetPointPosition() {
        if (isDestroyed() || checkNull(mPresetPointEditDialog)) {
            return;
        }
        String presetPointName = mPresetPointEditDialog != null ? mPresetPointEditDialog.getInputText() : new String();
        if (TextUtils.isEmpty(presetPointName)) {
            ToastUtil.showToast(PresetPointActivity.this, R.string.preset_point_name_empty);
            return;
        }
        mPresetPointEditDialog.dismiss();
        int presetPointPosition = mPresetPointEditDialog != null ? DeviceSettingHelper.getTempPresetPointPositionByPath(mPresetPointEditDialog.getIconPath()) : DeviceSettingHelper.PRESET_POINT_FIRST_POSITION;
        if (mPresenter != null) {
            mPresenter.checkAddPresetPointPosition(mDeviceId, presetPointName, presetPointPosition);
        }
    }

    private NormalTextAndIconInputDialog createPresetPointDialog(String account, String deviceId, PresetPointConfigure presetPointConfigure) {
        return createPresetPointDialog(FileUtil.getPresetPointThumbnail(NooieApplication.mCtx, account, deviceId, presetPointConfigure.getPosition()), new InputFrameView.OnInputFrameClickListener() {
            @Override
            public void onInputBtnClick() {
            }

            @Override
            public void onEditorAction() {
                tryEditPresetPointConfigure(presetPointConfigure);
            }

            @Override
            public void onEtInputClick() {
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isDestroyed() || checkNull(view, mPresetPointEditDialog)) {
                    return;
                }
                switch (view.getId()) {
                    case R.id.btnPresetPointCancel:
                        break;
                    case R.id.btnPresetPointConfirm:
                        tryEditPresetPointConfigure(presetPointConfigure);
                        break;
                }
            }
        });
    }

    private void tryEditPresetPointConfigure(PresetPointConfigure presetPointConfigure) {
        if (isDestroyed() || checkNull(mPresetPointEditDialog)) {
            return;
        }
        String presetPointName = mPresetPointEditDialog != null ? mPresetPointEditDialog.getInputText() : new String();
        if (TextUtils.isEmpty(presetPointName)) {
            ToastUtil.showToast(PresetPointActivity.this, R.string.preset_point_name_empty);
            return;
        }
        mPresetPointEditDialog.dismiss();
        if (presetPointConfigure != null && mPresenter != null) {
            showLoading();
            presetPointConfigure.setName(presetPointName);
            mPresenter.editPresetPointConfigure(mDeviceId, presetPointConfigure);
        }
    }

    private NormalTextAndIconInputDialog createPresetPointDialog(String presetPointScreenShotPath, InputFrameView.OnInputFrameClickListener onInputFrameClickListener, View.OnClickListener buttonListener) {
        NormalTextAndIconInputDialog normalTextAndIconInputDialog = new NormalTextAndIconInputDialog(this);
        normalTextAndIconInputDialog.setCancelable(true);
        normalTextAndIconInputDialog.setInputFrameHint(getString(R.string.preset_point_enter_name_hint));
        normalTextAndIconInputDialog.setInputFrameOnClickListener(new InputFrameView.OnInputFrameClickListener() {
            @Override
            public void onInputBtnClick() {
                if (onInputFrameClickListener != null) {
                    onInputFrameClickListener.onInputBtnClick();
                }
            }

            @Override
            public void onEditorAction() {
                if (onInputFrameClickListener != null) {
                    onInputFrameClickListener.onEditorAction();
                }
            }

            @Override
            public void onEtInputClick() {
                if (onInputFrameClickListener != null) {
                    onInputFrameClickListener.onEtInputClick();
                }
            }
        });
        normalTextAndIconInputDialog.setButtonListener(buttonListener);
        normalTextAndIconInputDialog.setPresetPointEditIcon(presetPointScreenShotPath);
        return normalTextAndIconInputDialog;
    }

    private void setPresetPointList() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        rvPresetPointList.setLayoutManager(gridLayoutManager);
        mPresetPointAdapter = new PresetPointAdapter();
        mPresetPointAdapter.setAccountAndDeviceId(mUserAccount, mDeviceId);
        mPresetPointAdapter.setListener(new PresetPointListener() {
            @Override
            public void onStartDragItem(RecyclerView.ViewHolder holder) {
                if (mPresetPointSortComponent != null) {
                    mPresetPointSortComponent.startDrag(holder);
                }
            }

            @Override
            public void onItemClick(int position, PresetPointConfigure presetPointConfigure) {
                if (mPresenter != null) {
                    showLoading();
                    mPresenter.turnPresetPoint(mDeviceId, presetPointConfigure);
                }
            }

            @Override
            public void onItemDeleteClick(int position, PresetPointConfigure presetPointConfigure) {
                showDeletePresetPointDialog(presetPointConfigure);
            }

            @Override
            public void onItemEditClick(int position, PresetPointConfigure presetPointConfigure) {
                if (isDestroyed()) {
                    return;
                }
                showPresetPointEditView(mUserAccount, mDeviceId, presetPointConfigure);
            }

            @Override
            public void onItemAddClick(int presetPointPosition) {
                startScreenShot(mUserAccount, mDeviceId, presetPointPosition);
            }
        });
        rvPresetPointList.setAdapter(mPresetPointAdapter);

        mPresetPointSortComponent = new PresetPointSortComponent();
        mPresetPointSortComponent.setRv(rvPresetPointList);
        mPresetPointSortComponent.setAdapter(mPresetPointAdapter);
        mPresetPointSortComponent.setupItemTouchHelper();
        mPresetPointSortComponent.setListener(new PresetPointSortComponent.PresetPointSortComponentListener() {
            @Override
            public void onDataChange(List<PresetPointConfigure> presetPointConfigures) {
                if (isDestroyed() || CollectionUtil.isEmpty(presetPointConfigures)) {
                    return;
                }
                if (mPresenter != null) {
                    showLoading();
                    mPresenter.sortPresetPointConfigureList(mDeviceId, presetPointConfigures);
                }
            }
        });
    }

    private Dialog mDeletePresetPointDialog = null;
    private void showDeletePresetPointDialog(PresetPointConfigure presetPointConfigure) {
        if (presetPointConfigure == null) {
            return;
        }
        hideDeletePresetPointDialog();
        mDeletePresetPointDialog = DialogUtils.showConfirmWithSubMsgDialog(this, getString(R.string.dialog_tip_title), getString(R.string.preset_point_delete_tip), R.string.cancel, R.string.confirm_upper, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                if (isDestroyed() || presetPointConfigure == null) {
                    return;
                }
                if (mPresenter != null) {
                    showLoading();
                    mPresenter.deletePresetPointConfigure(mUserAccount, mDeviceId, presetPointConfigure);
                }
            }

            @Override
            public void onClickLeft() {
            }
        });
    }

    private void hideDeletePresetPointDialog() {
        if (mDeletePresetPointDialog != null) {
            mDeletePresetPointDialog.dismiss();
            mDeletePresetPointDialog = null;
        }
    }

    private void showPlayerIsReleasing(boolean isExit) {
        ToastUtil.showToast(this, R.string.play_close_when_loading_tip);
    }

    private void updatePlayerState(int state) {
        if (mPlayerState == null) {
            mPlayerState = new PlayState(PlayState.PLAY_TYPE_LIVE, PlayState.PLAY_STATE_INIT, System.currentTimeMillis());
        }
        mPlayerState.setState(state);
    }

    public boolean isPlayStarting() {
        return mPlayerState != null && mPlayerState.getState() == PlayState.PLAY_STATE_START && (Math.abs(System.currentTimeMillis() - mPlayerState.getTime()) < PlayState.CMD_TIMEOUT);
    }

    private BindDevice getDevice(String deviceId) {
        return !TextUtils.isEmpty(deviceId) ? NooieDeviceHelper.getDeviceById(deviceId) : null;
    }

    private boolean checkIsCurrentDevice(String deviceId) {
        return !TextUtils.isEmpty(deviceId) && deviceId.equalsIgnoreCase(mDeviceId);
    }

    private String getDeviceModel() {
        if (getCurrentIntent() == null) {
            return null;
        }
        return getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_IPC_MODEL);
    }
}
