package com.afar.osaio.message.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.bean.ShortLinkDeviceParam;
import com.afar.osaio.smart.device.helper.NooieCloudHelper;
import com.afar.osaio.smart.push.firebase.analytics.FirebaseConstant;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.tool.TaskUtil;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.api.network.base.bean.entity.DeviceMessage;
import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.nooie.sdk.bean.IpcType;
import com.afar.osaio.smart.device.bean.DeviceInfo;
import com.afar.osaio.bean.VideoDataType;
import com.afar.osaio.message.activity.adapter.DeviceMessageAdapter;
import com.afar.osaio.message.model.RefreshType;
import com.afar.osaio.message.presenter.DeviceMsgPresenterImpl;
import com.afar.osaio.message.presenter.IDeviceMsgPresenter;
import com.afar.osaio.message.view.IDeviceMessageView;
import com.afar.osaio.smart.setting.activity.DetectionSettingActivity;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.player.activity.NooiePlayActivity;
import com.afar.osaio.smart.setting.activity.NooieStorageActivity;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.afar.osaio.util.ToastUtil;
import com.aspsine.swipetoloadlayout.OnLoadMoreListener;
import com.aspsine.swipetoloadlayout.OnRefreshListener;
import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;
import com.nooie.common.bean.DataEffect;
import com.nooie.common.bean.DataEffectCache;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.cache.DeviceConfigureCache;
import com.nooie.sdk.cache.DeviceConnectionCache;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by victor on 2018/7/3
 * Email is victor.qiao.0604@gmail.com
 */
public class DeviceMessageActivity extends BaseActivity implements IDeviceMessageView, OnRefreshListener, OnLoadMoreListener {

    public static final int DEVICE_MSG_BUTTON_CONTAINER_TO_MUCH = 0;
    public static final int DEVICE_MSG_BUTTON_CONTAINER_SUBSCRIBE = 1;
    public static final int DEVICE_MSG_BUTTON_CONTAINER_DELETE = 2;
    public static final int DEVICE_MSG_BUTTON_CONTAINER_NORMAL = 3;

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.tvRight)
    TextView tvRight;
    @BindView(R.id.swipe_target)
    RecyclerView swipeTarget;
    @BindView(R.id.swipeToLoadLayout)
    SwipeToLoadLayout swipeToLoadLayout;
    @BindView(R.id.vDeviceMsgTimeLine)
    View vDeviceMsgTimeLine;
    @BindView(R.id.clvDeviceMsgBottomContainer)
    View clvDeviceMsgBottomContainer;
    @BindView(R.id.tvDeviceMsgToMuchTip)
    TextView tvDeviceMsgToMuchTip;
    @BindView(R.id.btnDeviceMsgToMuchClose)
    ImageView btnDeviceMsgToMuchClose;
    @BindView(R.id.tvDeviceMsgSubscribeTip)
    TextView tvDeviceMsgSubscribeTip;
    @BindView(R.id.btnDeviceMsgSubscribe)
    ImageView btnDeviceMsgSubscribe;
    @BindView(R.id.btnDeviceMsgDeleteCancel)
    TextView btnDeviceMsgDeleteCancel;
    @BindView(R.id.btnDeviceMsgDeleteConfirm)
    TextView btnDeviceMsgDeleteConfirm;
    @BindView(R.id.vBtnCenterLine)
    View vBtnCenterLine;

    private DeviceMessageAdapter mDeviceMessageAdapter;

    private IDeviceMsgPresenter mDeviceMsgPresenter;
    private RefreshType mRefreshType;
    private boolean mOpenCloud;
    private boolean mHaveSDCard = false;
    private String mDeviceId;
    private boolean mIsOwner = false;
    private DataEffectCache mDataEffectCache = new DataEffectCache();
    private final static String DE_KEY_IS_OWNER = "is_owner";
    private boolean mIsEventCloud;
    private boolean mIsSubscribeCloud = false;
    private int mStorageDayNum = 0;
    private boolean mIsSDCardMounting = false;

    public static void toDeviceMsgActivity(Context from, String deviceID, String name) {
        Intent intent = new Intent(from, DeviceMessageActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceID);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_NAME, name);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_msg);
        ButterKnife.bind(this);

        if (!initData()) {
            finish();
            return;
        }
        NooieLog.d("-->> debug DeviceMessage monitor 1000 deviceId=" + mDeviceId);
        initView();

        if (!NooieDeviceHelper.isSortLinkDevice(getShortLinkDeviceParam())) {
            showLoading();
        }
        mIsShortLinkConnectionExist = getShortLinkDeviceParam() != null && DeviceConnectionCache.getInstance().isConnectionExist(getShortLinkDeviceParam().getDeviceId());
        mRefreshType = RefreshType.SET_DATA;
        ivRight.setEnabled(false);
        ivRight.setVisibility(View.GONE);
        mDeviceMsgPresenter.checkNooieDeviceIsOpenCloud(mUserAccount, mUid, mDeviceId, getBindType());
    }

    private boolean initData() {
        if (getCurrentIntent() == null) {
            return false;
        } else {
            setDataEffects();
            mDeviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
            DeviceInfo deviceInfo = NooieDeviceHelper.getDeviceInfoById(mDeviceId);
            if (TextUtils.isEmpty(mDeviceId) || deviceInfo == null) {
                return false;
            }
            mIsSubscribeCloud = DeviceConfigureCache.getInstance().getDeviceConfigure(mDeviceId) != null ? NooieCloudHelper.isSubscribeCloud(DeviceConfigureCache.getInstance().getDeviceConfigure(mDeviceId).getStatus()) : false;
            mDeviceMsgPresenter = new DeviceMsgPresenterImpl(this);
            mDeviceMsgPresenter.checkIsOwnerDevice(mDeviceId, mDataEffectCache.get(DE_KEY_IS_OWNER));
        }
        return true;
    }

    private void setDataEffects() {
        DataEffect<Boolean> mIsOwnerDe = new DataEffect<>();
        mIsOwnerDe.setKey(DE_KEY_IS_OWNER);
        mIsOwnerDe.setValue(false);
        mIsOwnerDe.setEffective(false);
        mDataEffectCache.put(mIsOwnerDe.getKey(), mIsOwnerDe);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        String deviceName = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_NAME);
        tvTitle.setText(deviceName);
        ivRight.setImageResource(R.drawable.delete_icon_state_list);
        ivRight.setTag(DEVICE_MSG_BUTTON_CONTAINER_NORMAL);
        tvRight.setText(R.string.message_device_select_all);
        tvRight.setVisibility(View.GONE);

        setupDeviceMsgView();
        btnDeviceMsgToMuchClose.setTag(ConstantValue.STATE_ON);
        setupSensitivityClickableTv();
    }

    private void setupDeviceMsgView() {
        swipeToLoadLayout.setOnRefreshListener(this);
        swipeToLoadLayout.setOnLoadMoreListener(this);

        mDeviceMessageAdapter = new DeviceMessageAdapter(this);
        swipeTarget.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        swipeTarget.setAdapter(mDeviceMessageAdapter);
        mDeviceMessageAdapter.setOnMsgItemClickListener(new DeviceMessageAdapter.OnMsgItemClickListener() {
            @Override
            public void onItemGoToViewClick(final DeviceMessage message) {
                mDataEffectCache.checkDataEffective(DE_KEY_IS_OWNER, new DataEffectCache.CheckDataCallback() {
                    @Override
                    public void onResult(boolean isEffective) {
                        if (isEffective) {
                            clickItem(message, false);
                        } else {
                            mDeviceMsgPresenter.checkIsOwnerDevice(mDeviceId, mDataEffectCache.get(DE_KEY_IS_OWNER));
                        }
                    }
                });
            }

            @Override
            public void onClickItem(final DeviceMessage message) {
                mDataEffectCache.checkDataEffective(DE_KEY_IS_OWNER, new DataEffectCache.CheckDataCallback() {
                    @Override
                    public void onResult(boolean isEffective) {
                        if (isEffective) {
                            clickItem(message, true);
                        } else {
                            mDeviceMsgPresenter.checkIsOwnerDevice(mDeviceId, mDataEffectCache.get(DE_KEY_IS_OWNER));
                        }
                    }
                });
            }

            @Override
            public void onDeleteItem(int position, DeviceMessage message) {
                if (message != null && !TextUtils.isEmpty(String.valueOf(message.getId()))) {
                    List<String> delIds = new ArrayList<String>();
                    delIds.add(String.valueOf(message.getId()));
                    mDeviceMsgPresenter.deleteDeviceMessages(delIds);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        tryToLoadDeviceSetting();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopRefresh();
        stopLoadMore();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDeviceMsgPresenter != null) {
            mDeviceMsgPresenter = null;
        }
        hideMsgDetailDialog();
        hideMsgDetailForPlaybackDialog();
        hideMsgDetailForSubscribeDialog();
        hideConfirmDeleteDialog();
        hideDeleteAllMsgDialog();
    }

    @Override
    public void releaseRes() {
        super.releaseRes();
        ivLeft = null;
        tvTitle = null;
        ivRight = null;
        tvRight = null;
        if (swipeToLoadLayout != null) {
            swipeToLoadLayout.setOnRefreshListener(null);
            swipeToLoadLayout.setOnLoadMoreListener(null);
        }
        if (swipeTarget != null) {
            swipeTarget.setAdapter(null);
            swipeTarget = null;
        }
        if (mDeviceMessageAdapter != null) {
            mDeviceMessageAdapter = null;
        }
        vDeviceMsgTimeLine = null;
        clvDeviceMsgBottomContainer = null;
        tvDeviceMsgToMuchTip = null;
        btnDeviceMsgToMuchClose = null;
        tvDeviceMsgSubscribeTip = null;
        btnDeviceMsgSubscribe = null;
        btnDeviceMsgDeleteCancel = null;
        btnDeviceMsgDeleteConfirm = null;
        vBtnCenterLine = null;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        NooieLog.d("-->> debug DeviceMessageActivity onNewIntent: deviceId=" + mDeviceId);
        if (getCurrentIntent() == null) {
            return;
        }
        if (!isDestroyed()) {
            hideCloudSubscribeTipDialog();
            hideConfirmDeleteDialog();
            hideDeleteAllMsgDialog();
            hideMsgDetailDialog();
            hideMsgDetailForPlaybackDialog();
            hideMsgDetailForSubscribeDialog();
            hideLoading();
        }
        NooieLog.d("-->> debug DeviceMessageActivity onNewIntent: getCurrentIntent deviceId=" + getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID));
        if (checkIsCurrentDevice(getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID))) {
            startRefresh();
        } else {
            changeDevice();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!mIsShortLinkConnectionExist) {
            checkIsNeedToDisconnectShortLinkDevice();
        }
    }

    private void changeDevice() {
        mDeviceId = getCurrentIntent() != null ? getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID) : new String();
        DeviceInfo deviceInfo = NooieDeviceHelper.getDeviceInfoById(mDeviceId);
        if (TextUtils.isEmpty(mDeviceId) || deviceInfo == null) {
            return;
        }
        setDataEffects();
        mIsOwner = false;
        mOpenCloud = false;
        mIsEventCloud = false;
        mStorageDayNum = 0;
        mIsSubscribeCloud = false;
        mHaveSDCard = false;
        mDeviceMsgPresenter.checkIsOwnerDevice(mDeviceId, mDataEffectCache.get(DE_KEY_IS_OWNER));

        String deviceName = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_NAME);
        tvTitle.setText(deviceName);
        hideButtonContainer();

        showLoading();
        mRefreshType = RefreshType.SET_DATA;
        ivRight.setEnabled(false);
        ivRight.setVisibility(View.GONE);
        mDeviceMsgPresenter.checkNooieDeviceIsOpenCloud(mUserAccount, mUid, mDeviceId, getBindType());
    }

    @OnClick({R.id.ivLeft, R.id.ivRight, R.id.tvRight, R.id.btnDeviceMsgToMuchClose, R.id.tvDeviceMsgSubscribeTip, R.id.btnDeviceMsgSubscribe, R.id.btnDeviceMsgDeleteCancel, R.id.btnDeviceMsgDeleteConfirm, R.id.clvDeviceMsgBottomContainer})
    public void onViewClicked(View v) {
        switch (v.getId()) {
            case R.id.ivLeft:
                if (!mIsShortLinkConnectionExist) {
                    checkIsNeedToDisconnectShortLinkDevice();
                }
                finish();
                break;
            case R.id.ivRight:
                showDeleteAllMsgDialog();
                break;
            case R.id.tvRight: {
                if (mDeviceMessageAdapter != null) {
                    mDeviceMessageAdapter.setAllSelect();
                }
                break;
            }
            case R.id.btnDeviceMsgToMuchClose: {
                hideButtonContainer();
                if (btnDeviceMsgToMuchClose != null) {
                    btnDeviceMsgToMuchClose.setTag(ConstantValue.STATE_OFF);
                }
                displayButtonContainer();
                break;
            }
            case R.id.tvDeviceMsgSubscribeTip:
            case R.id.btnDeviceMsgSubscribe: {
                gotoStorage(FirebaseConstant.EVENT_CLOUD_ORIGIN_FROM_DEVICE_MESSAGE);
                break;
            }
            case R.id.btnDeviceMsgDeleteCancel: {
                hideButtonContainer();
                ivRight.setImageResource(R.drawable.delete_icon_state_list);
                ivRight.setTag(DEVICE_MSG_BUTTON_CONTAINER_NORMAL);
                swipeToLoadLayout.setRefreshEnabled(true);
                swipeToLoadLayout.setLoadMoreEnabled(true);
                if (mDeviceMessageAdapter != null) {
                    mDeviceMessageAdapter.setIsDeletingMsg(false);
                }
                break;
            }
            case R.id.btnDeviceMsgDeleteConfirm: {
                List<String> deleteMsgIds = new ArrayList<>();
                if (mDeviceMessageAdapter != null && CollectionUtil.isNotEmpty(mDeviceMessageAdapter.getDeleteMsgIds())) {
                    deleteMsgIds.addAll(mDeviceMessageAdapter.getDeleteMsgIds());
                } else {
                    ToastUtil.showToast(DeviceMessageActivity.this, R.string.message_device_select_msg_first);
                    break;
                }
                hideButtonContainer();
                ivRight.setImageResource(R.drawable.delete_icon_state_list);
                ivRight.setTag(DEVICE_MSG_BUTTON_CONTAINER_NORMAL);
                swipeToLoadLayout.setRefreshEnabled(true);
                swipeToLoadLayout.setLoadMoreEnabled(true);
                mDeviceMessageAdapter.setIsDeletingMsg(false);
                showConfirmDeleteDialog(deleteMsgIds);
                break;
            }
            case R.id.clvDeviceMsgBottomContainer: {
                break;
            }
        }
    }

    private void clickItem(final DeviceMessage message, boolean showDialog) {
        if (message == null) {
            return;
        }

        if (!mIsOwner) {
            clickItemBySharer(message, showDialog);
            return;
        }

        clickItemByOwner(message, showDialog);
    }

    private void playbackOnCloudOrSD(boolean cloud, DeviceMessage message, int bindType) {
        long duration = System.currentTimeMillis() - message.getTime() * 1000L;
        DeviceInfo deviceInfo = NooieDeviceHelper.getDeviceInfoById(message.getUuid());
        boolean loopRecordStatus = deviceInfo != null ? deviceInfo.getLoopRecordStatus() : false;
        boolean isLpDevice = deviceInfo != null && deviceInfo.getNooieDevice() != null && NooieDeviceHelper.isLpDevice(deviceInfo.getNooieDevice().getType());
        //boolean isPlaybackDirect = isLpDevice || mIsEventCloud;
        boolean isPlaybackDirect = isLpDevice && mIsEventCloud;
        //boolean isPlaySDInsteadOfCloud = bindType == ApiConstant.BIND_TYPE_OWNER && cloud && mHaveSDCard && NooieCloudHelper.isCloudEventMsgInvalid(mStorageDayNum, message.getDevice_time() * 1000L);
        //if (cloud && !isPlaySDInsteadOfCloud) {
        if (cloud) {
            int minDuration = isLpDevice ? 15 * 1000 : 2 * 60 * 1000;
            if (!isPlaybackDirect && duration < minDuration) {
                gotoLive();
            } else {
                openPlayback(message, VideoDataType.CLOUD);
            }
        } else {
            if (loopRecordStatus) {
                int minDuration = isLpDevice ? 15 * 1000 : 5 * 60 * 1000;
                if (!isPlaybackDirect && duration < minDuration) {
                    gotoLive();
                } else {
                    openPlayback(message, VideoDataType.DISK);
                }
            } else {
                int minDuration = isLpDevice ? 15 * 1000 : 2 * 60 * 1000;
                if (!isPlaybackDirect && duration < minDuration) {
                    gotoLive();
                } else {
                    openPlayback(message, VideoDataType.DISK);
                }
            }
        }
    }

    private void gotoLive() {
        DeviceInfo deviceInfo = NooieDeviceHelper.getDeviceInfoById(mDeviceId);
        //IpcType deviceModelType = deviceInfo != null && deviceInfo.getNooieDevice() != null ? IpcType.getIpcType(deviceInfo.getNooieDevice().getType()): IpcType.IPC_1080;
        //NooiePlayActivity.startPlayActivity(this, mDeviceId, deviceModelType, ConstantValue.NOOIE_PLAYBACK_TYPE_LIVE, ConstantValue.NOOIE_PLAYBACK_SOURCE_TYPE_NORMAL, 0, ConstantValue.ROUTE_SOURCE_NORMAL, ConstantValue.CONNECTION_MODE_QC);
        String deviceModel = deviceInfo != null && deviceInfo.getNooieDevice() != null ? deviceInfo.getNooieDevice().getType(): IpcType.IPC_1080.getType();
        NooiePlayActivity.startPlayActivityBySingleTop(this, mDeviceId, deviceModel, ConstantValue.NOOIE_PLAYBACK_TYPE_LIVE, ConstantValue.NOOIE_PLAYBACK_SOURCE_TYPE_NORMAL, 0, ConstantValue.ROUTE_SOURCE_NORMAL, ConstantValue.CONNECTION_MODE_QC);
    }

    private void openPlayback(DeviceMessage message, VideoDataType type) {
        if (message == null && TextUtils.isEmpty(message.getUuid())) {
            return;
        }

        int playbackType = type == VideoDataType.CLOUD ? ConstantValue.NOOIE_PLAYBACK_TYPE_CLOUD : ConstantValue.NOOIE_PLAYBACK_TYPE_SD;
        DeviceInfo deviceInfo = NooieDeviceHelper.getDeviceInfoById(mDeviceId);
        //IpcType deviceModelType = deviceInfo != null && deviceInfo.getNooieDevice() != null ? IpcType.getIpcType(deviceInfo.getNooieDevice().getType()): IpcType.IPC_1080;
        //NooiePlayActivity.startPlayActivity(this, mDeviceId, deviceModelType, playbackType, ConstantValue.NOOIE_PLAYBACK_SOURCE_TYPE_DIRECT, message.getDevice_time() * 1000L, ConstantValue.ROUTE_SOURCE_NORMAL, ConstantValue.CONNECTION_MODE_QC);
        String deviceModel = deviceInfo != null && deviceInfo.getNooieDevice() != null ? deviceInfo.getNooieDevice().getType(): IpcType.IPC_1080.getType();
        if (!(NooieDeviceHelper.isSortLinkDevice(getShortLinkDeviceParam()) && mIsSDCardMounting)) {
            NooiePlayActivity.startPlayActivityBySingleTop(this, mDeviceId, deviceModel, playbackType, ConstantValue.NOOIE_PLAYBACK_SOURCE_TYPE_DIRECT, message.getDevice_time() * 1000L, ConstantValue.ROUTE_SOURCE_NORMAL, ConstantValue.CONNECTION_MODE_QC);
            return;
        }
        showLoading();
        TaskUtil.delayAction(2 * 1000, new TaskUtil.OnDelayTimeFinishListener() {
            @Override
            public void onFinish() {
                hideLoading();
                NooiePlayActivity.startPlayActivityBySingleTop(DeviceMessageActivity.this, mDeviceId, deviceModel, playbackType, ConstantValue.NOOIE_PLAYBACK_SOURCE_TYPE_DIRECT, message.getDevice_time() * 1000L, ConstantValue.ROUTE_SOURCE_NORMAL, ConstantValue.CONNECTION_MODE_QC);
            }
        });
    }

    public void gotoStorage(String eventKey) {
        if (mIsSubscribeCloud) {
            showCloudSubscribeTipDialog();
        } else {
            NooieStorageActivity.toCloudBuyPage(this, mDeviceId, NooieCloudHelper.createEnterMark(mUid), eventKey);
        }
    }

    public void clickItemByOwner(DeviceMessage message, boolean showDialog) {
        NooieLog.d("-->> debug DeviceMessage monitor 1001 deviceId=" + mDeviceId + " openCloud=" + mOpenCloud + " hasSD=" + mHaveSDCard + " mStorageDayNum=" + mStorageDayNum);
        if (message == null) {
            return;
        }

        if (message.getType() == ApiConstant.DEVICE_MSG_TYPE_SD_LEAK) {
            boolean isSubDevice = getDevice() != null ? NooieDeviceHelper.isLpDevice(getDevice().getType()) : false;
            boolean isLpDevice = getDevice() != null ? NooieDeviceHelper.isSubDevice(getDevice().getPuuid(), getDevice().getType()) : false;
            String model = getDevice() != null ? getDevice().getType() : new String();
            NooieStorageActivity.toNooieStorageActivity(DeviceMessageActivity.this, mDeviceId, isSubDevice, ConstantValue.CONNECTION_MODE_QC, ApiConstant.BIND_TYPE_OWNER, isLpDevice, model);
            return;
        }

        //boolean isMsgJumpDisable = mOpenCloud && !mHaveSDCard && NooieCloudHelper.isCloudEventMsgInvalid(mStorageDayNum, message.getDevice_time() * 1000L);
        boolean isMsgJumpDisable = mOpenCloud && NooieCloudHelper.isCloudEventMsgInvalid(mStorageDayNum, message.getDevice_time() * 1000L);
        NooieLog.d("-->> debug DeviceMessage monitor 1002 deviceId=" + mDeviceId + " isJump=" + isMsgJumpDisable);
        if (isMsgJumpDisable) {
            showMsgDetailDialog(message);
            return;
        }

        boolean onLine = getDevice() != null ? getDevice().getOnline() == ApiConstant.ONLINE_STATUS_ON && getDevice().getOpen_status() == ApiConstant.OPEN_STATUS_ON : false;
        if (mOpenCloud || (mHaveSDCard && onLine)) {
            if (showDialog) {
                showMsgDetailForPlaybackDialog(message, ApiConstant.BIND_TYPE_OWNER);
            } else {
                playbackOnCloudOrSD(mOpenCloud, message, ApiConstant.BIND_TYPE_OWNER);
            }
        } else {
            if (mHaveSDCard) {
                showMsgDetailDialog(message);
            } else {
                NooieLog.d("-->> debug DeviceMessage monitor 1003 deviceId=" + mDeviceId);
                showMsgDetailForSubscribeDialog(message);
            }
        }
    }

    public void clickItemBySharer(DeviceMessage message, boolean showDialog) {
        NooieLog.d("-->> debug DeviceMessage monitor 1004 deviceId=" + mDeviceId + " openCloud=" + mOpenCloud + " hasSD=" + mHaveSDCard + " mStorageDayNum=" + mStorageDayNum);
        if (message == null) {
            return;
        }

        boolean isMsgJumpDisable = !mOpenCloud || NooieCloudHelper.isCloudEventMsgInvalid(mStorageDayNum, message.getDevice_time() * 1000L);
        NooieLog.d("-->> debug DeviceMessage monitor 1005 deviceId=" + mDeviceId + " isJump=" + isMsgJumpDisable);
        if (isMsgJumpDisable) {
            showMsgDetailDialog(message);
            return;
        }

        if (mOpenCloud) {
            if (showDialog) {
                showMsgDetailForPlaybackDialog(message, ApiConstant.BIND_TYPE_SHARE);
            } else {
                playbackOnCloudOrSD(true, message, ApiConstant.BIND_TYPE_SHARE);
            }
        }
        /*
        else {
            showMsgDetailDialog(message);
        }
         */
    }

    public void displayButtonContainer() {
        if (isDestroyed() || checkNull(btnDeviceMsgToMuchClose, btnDeviceMsgDeleteCancel, mDeviceMessageAdapter)) {
            return;
        }

        if (btnDeviceMsgDeleteCancel.getVisibility() == View.VISIBLE) {
            return;
        }

        if (mIsOwner && mOpenCloud && btnDeviceMsgToMuchClose.getTag() != null && (Integer)btnDeviceMsgToMuchClose.getTag() == ConstantValue.STATE_ON && CollectionUtil.isNotEmpty(mDeviceMessageAdapter.getDataSet()) && mDeviceMessageAdapter.getDataSet().size() > 30) {
            showButtonContainer(DEVICE_MSG_BUTTON_CONTAINER_TO_MUCH);
        } else if (mIsOwner && !mOpenCloud && CollectionUtil.isNotEmpty(mDeviceMessageAdapter.getDataSet())) {
            showButtonContainer(DEVICE_MSG_BUTTON_CONTAINER_SUBSCRIBE);
        } else if (!mIsOwner || CollectionUtil.isEmpty(mDeviceMessageAdapter.getDataSet())) {
            showButtonContainer(DEVICE_MSG_BUTTON_CONTAINER_NORMAL);
        }
    }

    private void showButtonContainer(int type) {
        if (checkNull(clvDeviceMsgBottomContainer, tvDeviceMsgToMuchTip, btnDeviceMsgToMuchClose, tvDeviceMsgSubscribeTip, btnDeviceMsgSubscribe, btnDeviceMsgDeleteCancel, btnDeviceMsgDeleteConfirm, vBtnCenterLine)) {
            return;
        }

        displayViewAll(false, tvDeviceMsgToMuchTip, btnDeviceMsgToMuchClose, tvDeviceMsgSubscribeTip, btnDeviceMsgSubscribe, btnDeviceMsgDeleteCancel, btnDeviceMsgDeleteConfirm, vBtnCenterLine);
        switch (type) {
            case DEVICE_MSG_BUTTON_CONTAINER_TO_MUCH: {
                displayViewAll(true, tvDeviceMsgToMuchTip, btnDeviceMsgToMuchClose);
                break;
            }
            case DEVICE_MSG_BUTTON_CONTAINER_SUBSCRIBE: {
                displayViewAll(true, tvDeviceMsgSubscribeTip, btnDeviceMsgSubscribe);
                break;
            }
            case DEVICE_MSG_BUTTON_CONTAINER_DELETE: {
                displayViewAll(true, btnDeviceMsgDeleteCancel, btnDeviceMsgDeleteConfirm, vBtnCenterLine);
                break;
            }
        }
    }

    private void hideButtonContainer() {
        if (checkNull(clvDeviceMsgBottomContainer, tvDeviceMsgToMuchTip, btnDeviceMsgToMuchClose, tvDeviceMsgSubscribeTip, btnDeviceMsgSubscribe, btnDeviceMsgDeleteCancel, btnDeviceMsgDeleteConfirm, vBtnCenterLine)) {
            return;
        }
        displayViewAll(false, tvDeviceMsgToMuchTip, btnDeviceMsgToMuchClose, tvDeviceMsgSubscribeTip, btnDeviceMsgSubscribe, btnDeviceMsgDeleteCancel, btnDeviceMsgDeleteConfirm, vBtnCenterLine);
    }

    private void setupSensitivityClickableTv() {
        final SpannableStringBuilder style = new SpannableStringBuilder();
        String sensitivity = getString(R.string.motion_detect_sensitivity);
        String text = String.format(getString(R.string.message_device_msg_too_much_tip), sensitivity);

        //设置文字
        style.append(text);

        //设置部分文字点击事件
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                String model = NooieDeviceHelper.getDeviceById(mDeviceId) != null ? NooieDeviceHelper.getDeviceById(mDeviceId).getType() : "";
                if (!TextUtils.isEmpty(model) && checkDeviceIsOnline()) {
                    DetectionSettingActivity.toDetectionSettingActivity(DeviceMessageActivity.this, mDeviceId, model);
                }
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setUnderlineText(false);
            }
        };
        style.setSpan(clickableSpan, text.indexOf(sensitivity), text.indexOf(sensitivity) + sensitivity.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvDeviceMsgToMuchTip.setText(style);

        //设置部分文字颜色
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.theme_green));
        style.setSpan(foregroundColorSpan, text.indexOf(sensitivity), text.indexOf(sensitivity) + sensitivity.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        //配置给TextView
        tvDeviceMsgToMuchTip.setMovementMethod(LinkMovementMethod.getInstance());
        tvDeviceMsgToMuchTip.setText(style);
    }

    private Dialog mConfirmDeleteDialog = null;
    private void showConfirmDeleteDialog(final List<String> deleteMsgIds) {
        hideConfirmDeleteDialog();
        mConfirmDeleteDialog = DialogUtils.showConfirmWithSubMsgDialog(this, R.string.message_delete_message, R.string.message_delete_message_sub_msg,
                R.string.settings_no_delete, R.string.confirm_upper, new DialogUtils.OnClickConfirmButtonListener() {
                    @Override
                    public void onClickRight() {
                        mDeviceMsgPresenter.deleteNooieMessageByIds(mDeviceId, deleteMsgIds);
                    }

                    @Override
                    public void onClickLeft() {
                    }
                });
    }

    private void hideConfirmDeleteDialog() {
        if (mConfirmDeleteDialog != null) {
            mConfirmDeleteDialog.dismiss();
            mConfirmDeleteDialog = null;
        }
    }

    private Dialog mDeleteAllMsgDialog = null;
    private void showDeleteAllMsgDialog() {
        hideDeleteAllMsgDialog();
        mDeleteAllMsgDialog = DialogUtils.showConfirmWithSubMsgDialog(this, R.string.message_delete_message, R.string.message_delete_message_sub_msg,
                R.string.settings_no_delete, R.string.confirm_upper, new DialogUtils.OnClickConfirmButtonListener() {
                    @Override
                    public void onClickRight() {
                        if (mDeviceMsgPresenter != null) {
                            mDeviceMsgPresenter.deleteAllMessages(mDeviceId);
                        }
                    }

                    @Override
                    public void onClickLeft() {
                    }
                });
    }

    private void hideDeleteAllMsgDialog() {
        if (mDeleteAllMsgDialog != null) {
            mDeleteAllMsgDialog.dismiss();
        }
    }

    private void startRefresh() {
        swipeToLoadLayout.setRefreshing(true);
    }

    private void stopRefresh() {
        if (swipeToLoadLayout.isRefreshing()) {
            swipeToLoadLayout.setRefreshing(false);
        }
    }

    private void startLoadMore() {
        swipeToLoadLayout.setLoadingMore(true);
    }

    private void stopLoadMore() {
        if (swipeToLoadLayout.isLoadingMore()) {
            swipeToLoadLayout.setLoadingMore(false);
        }
    }

    @Override
    public void onRefresh() {
        if (isDestroyed() || checkNull(ivRight, mDeviceMessageAdapter, mDeviceMsgPresenter)) {
            return;
        }
        ivRight.setEnabled(false);
        ivRight.setVisibility(View.GONE);
        mDeviceMessageAdapter.setDataSet(new ArrayList<DeviceMessage>());
        mRefreshType = RefreshType.SET_DATA;
        mDeviceMsgPresenter.loadWarningMessage(0, mDeviceId, 30);
    }

    @Override
    public void onLoadMore() {
        if (isDestroyed() || checkNull(mDeviceMsgPresenter)) {
            return;
        }
        mRefreshType = RefreshType.LOAD_MORE;
        mDeviceMsgPresenter.loadWarningMessage(1, mDeviceId, 30);
    }

    @Override
    public void notifyHaveSDCardResult(int code, String deviceId, String result, boolean haveSDCard) {
        NooieLog.d("-->> debug DeviceMessage monitor 1006 deviceId=" + deviceId + " hasSD=" + haveSDCard + " result=" + result);
        if (isDestroyed() || !checkIsCurrentDevice(deviceId)) {
            NooieLog.d("-->> debug DeviceMessage monitor 1007 deviceId=" + deviceId);
            return;
        }
        NooieLog.d("-->> debug DeviceMessage monitor 1010 deviceId=" + deviceId + " mHasSD=" + mHaveSDCard + " hasSD=" + haveSDCard);
        mHaveSDCard = haveSDCard;
        if (code != SDKConstant.CODE_CACHE) {
            mIsSDCardMounting = false;
        }
    }

    @Override
    public void onLoadWarningMessage(String deviceId, @NonNull List<DeviceMessage> messages, boolean openCloud) {
        if (isDestroyed() || checkNull(ivRight, mDeviceMessageAdapter) || !checkIsCurrentDevice(deviceId)) {
            return;
        }

        mOpenCloud = openCloud;
        if (mRefreshType == RefreshType.SET_DATA) {
            if (!NooieDeviceHelper.isSortLinkDevice(getShortLinkDeviceParam())) {
                hideLoading();
            }
           /* ivRight.setEnabled(CollectionUtil.isNotEmpty(messages));
            ivRight.setVisibility(mIsOwner && CollectionUtil.isNotEmpty(messages) ? View.VISIBLE : View.GONE);*/
            mDeviceMessageAdapter.setDataSet(messages);
            stopRefresh();
        } else if (mRefreshType == RefreshType.LOAD_MORE) {
            if (CollectionUtil.isEmpty(messages)) {
                ToastUtil.showToast(this, R.string.message_no_more);
            } else if (CollectionUtil.isNotEmpty(messages)) {
                mDeviceMessageAdapter.insertItemsFromTail(messages);
            }
            stopLoadMore();
        }

        if (mDeviceMsgPresenter != null) {
            mDeviceMsgPresenter.setDeviceMsgReadState(mDeviceId);
        }
        displayButtonContainer();
    }

    @Override
    public void onHandleFailed(String deviceId, String message) {
        if (isDestroyed() || !checkIsCurrentDevice(deviceId)) {
            return;
        }

        stopRefresh();
        stopLoadMore();
    }

    @Override
    public void notifyDeleteMsgResult(String result) {
        if (ConstantValue.SUCCESS.equals(result)) {
        }
    }

    @Override
    public void notifyDeleteAllMsgResult(String deviceId, String result) {
        if (isDestroyed() || !checkIsCurrentDevice(deviceId)) {
            return;
        }
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            ToastUtil.showToast(this, getString(R.string.message_clear_success));
            startRefresh();
        } else {
            ToastUtil.showToast(this, getString(R.string.network_error0));
        }
    }

    @Override
    public void notifyCheckDataEffectResult(String deviceId, String key, DataEffect dataEffect) {
        if (isDestroyed() || !checkIsCurrentDevice(deviceId)) {
            return;
        }
        if (DE_KEY_IS_OWNER.equalsIgnoreCase(key)) {
            mDataEffectCache.put(key, dataEffect);
            if (mDataEffectCache.isDataEffective(key)) {
                mIsOwner = mDataEffectCache.get(key) != null ? (Boolean)mDataEffectCache.get(key).getValue() : mIsOwner;
            }
        }
    }

    @Override
    public void onCheckPackInfo(String deviceId, boolean isOpenCloud, int status, boolean isEvent, int storageDayNum) {
        if (!checkIsCurrentDevice(deviceId)) {
            return;
        }
        mOpenCloud = isOpenCloud;
        mIsSubscribeCloud = NooieCloudHelper.isSubscribeCloud(status);
        mIsEventCloud = isEvent;
        mStorageDayNum = storageDayNum;
    }

    @Override
    public ShortLinkDeviceParam getShortLinkDeviceParam() {
        if (getCurrentIntent() == null) {
            return null;
        }
        boolean isSubDevice = getDevice() != null ? NooieDeviceHelper.isSubDevice(getDevice().getPuuid(), getDevice().getType()) : false;
        String model = getDevice() != null ? getDevice().getType() : new String();
        ShortLinkDeviceParam shortLinkDeviceParam = new ShortLinkDeviceParam(mUid, mDeviceId, model, isSubDevice, true, ConstantValue.CONNECTION_MODE_QC);
        return shortLinkDeviceParam;
    }

    @Override
    public void dealAfterDeviceShortLink() {
        loadSDCardInfo(false);
    }

    private Dialog mCloudSubscribeTipDialog;
    private void showCloudSubscribeTipDialog() {
        hideCloudSubscribeTipDialog();
        mCloudSubscribeTipDialog = DialogUtils.showInformationNormalDialog(this, "", getString(R.string.nooie_play_cloud_subscribe_wait_for_preview), true, new DialogUtils.OnClickInformationDialogLisenter() {
            @Override
            public void onConfirmClick() {
            }
        });
    }

    private void hideCloudSubscribeTipDialog() {
        if (mCloudSubscribeTipDialog != null) {
            mCloudSubscribeTipDialog.dismiss();
        }
    }

    private Dialog mMsgDetailDialog = null;
    private void showMsgDetailDialog(DeviceMessage message) {
        hideMsgDetailDialog();
        mMsgDetailDialog = DialogUtils.showInformationNormalDialog(DeviceMessageActivity.this, DeviceMsgPresenterImpl.getWarnMsgDesc(message), getMsgContentForTip(mOpenCloud, mStorageDayNum, message), true, new DialogUtils.OnClickInformationDialogLisenter() {
            @Override
            public void onConfirmClick() {
            }
        });
    }

    private String getMsgContentForTip(boolean isOpenCloud, int storageDayNum, DeviceMessage message) {
        boolean isConvertDetectionMsg = message != null && (message.getType() == ApiConstant.DEVICE_MSG_TYPE_MOTION_DETECT || message.getType() == ApiConstant.DEVICE_MSG_TYPE_SOUND_DETECT || message.getType() == ApiConstant.DEVICE_MSG_TYPE_PIR_DETECT)
                && (isOpenCloud && NooieCloudHelper.isCloudEventMsgInvalid(storageDayNum, message.getDevice_time() * 1000L));
        if (isConvertDetectionMsg) {
            return new StringBuilder(DeviceMsgPresenterImpl.getWarnMsgContent(message)).append("\n").append(getString(R.string.message_cloud_video_expire)).toString();
        }
        return DeviceMsgPresenterImpl.getWarnMsgContent(message);
    }

    private void hideMsgDetailDialog() {
        if (mMsgDetailDialog != null) {
            mMsgDetailDialog.dismiss();
            mMsgDetailDialog = null;
        }
    }

    private Dialog mMsgDetailForPlaybackDialog = null;
    private void showMsgDetailForPlaybackDialog(DeviceMessage message, int bindType) {
        hideMsgDetailForPlaybackDialog();
        mMsgDetailForPlaybackDialog = DialogUtils.showConfirmWithSubMsgDialog(DeviceMessageActivity.this, DeviceMsgPresenterImpl.getWarnMsgDesc(message), getMsgContentForTip(mOpenCloud, mStorageDayNum, message), R.string.ignore, R.string.message_go_to_video, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                playbackOnCloudOrSD(mOpenCloud, message, bindType);
            }

            @Override
            public void onClickLeft() {
            }
        });
    }

    private void hideMsgDetailForPlaybackDialog() {
        if (mMsgDetailForPlaybackDialog != null) {
            mMsgDetailForPlaybackDialog.dismiss();
            mMsgDetailForPlaybackDialog = null;
        }
    }

    private Dialog mMsgDetailForSubscribeDialog = null;
    private void showMsgDetailForSubscribeDialog(DeviceMessage message) {
        hideMsgDetailForSubscribeDialog();
        mMsgDetailForSubscribeDialog = DialogUtils.showConfirmWithSubMsgDialog(this, R.string.message_no_storage, R.string.message_please_subscribe_cloud, R.string.ignore, R.string.subscribe_upper, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                gotoStorage(FirebaseConstant.EVENT_CLOUD_ORIGIN_FROM_DEVICE_MESSAGE_ITEM);
            }

            @Override
            public void onClickLeft() {
            }
        });
    }

    private void hideMsgDetailForSubscribeDialog() {
        if (mMsgDetailForSubscribeDialog != null) {
            mMsgDetailForSubscribeDialog.dismiss();
            mMsgDetailForSubscribeDialog = null;
        }
    }

    private int getBindType() {
        int bindType = ApiConstant.BIND_TYPE_OWNER;
        if (getDevice() != null) {
            bindType = getDevice().getBind_type();
        }
        return bindType;
    }

    private BindDevice getDevice() {
        return NooieDeviceHelper.getDeviceById(mDeviceId);
    }

    private boolean checkIsCurrentDevice(String deviceId) {
        return !TextUtils.isEmpty(deviceId) && !TextUtils.isEmpty(mDeviceId) && deviceId.equalsIgnoreCase(mDeviceId);
    }

    private boolean checkDeviceIsOnline() {
        boolean onLine = getDevice() != null ? getDevice().getOnline() == ApiConstant.ONLINE_STATUS_ON : false;
        return onLine;
    }

    private boolean mIsShortLinkConnectionExist = false;
    private void checkIsNeedStartShortLink() {
        boolean isTryConnectShortLinkDevice = NooieDeviceHelper.isSortLinkDevice(getShortLinkDeviceParam()) && checkDeviceIsOnline();
        if (isTryConnectShortLinkDevice) {
            tryConnectShortLinkDevice();
        }
    }

    private void tryToLoadDeviceSetting() {
        if (!checkDeviceIsOnline()) {
            return;
        }
        boolean isShortLinkConnectionExist = getShortLinkDeviceParam() != null && DeviceConnectionCache.getInstance().isConnectionExist(getShortLinkDeviceParam().getDeviceId());
        boolean isTryConnectShortLinkDevice = NooieDeviceHelper.isSortLinkDevice(getShortLinkDeviceParam());
        if (isTryConnectShortLinkDevice) {
            if (!isShortLinkConnectionExist) {
                tryConnectShortLinkDevice();
            } else {
                loadSDCardInfo(false);
            }
            return;
        }
        loadSDCardInfo(true);
    }

    private void loadSDCardInfo(boolean isMounted) {
        mIsSDCardMounting = !isMounted;
        if (mDeviceMsgPresenter != null) {
            mDeviceMsgPresenter.getNooieDeviceSdCardSate(mUserAccount, mDeviceId, isMounted);
        }
    }
}

