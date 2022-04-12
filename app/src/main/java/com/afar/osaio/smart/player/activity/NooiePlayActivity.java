package com.afar.osaio.smart.player.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;
import com.blog.www.guideview.Component;
import com.blog.www.guideview.Guide;
import com.blog.www.guideview.GuideBuilder;
import com.afar.osaio.notification.NotificationManager;
import com.afar.osaio.smart.device.helper.DeviceConnectionHelper;
import com.afar.osaio.smart.device.listener.ConnectShortLinkDeviceListener;
import com.afar.osaio.smart.media.activity.PhotoMediaActivity;
import com.afar.osaio.smart.player.listener.OnStartPlaybackListener;
import com.afar.osaio.smart.push.firebase.analytics.FirebaseConstant;
import com.afar.osaio.smart.setting.activity.DevicePIRActivity;
import com.afar.osaio.smart.setting.activity.NooieDetectionActivity;
import com.afar.osaio.smart.setting.activity.OtaUpdateTipActivity;
import com.afar.osaio.util.CommonUtil;
import com.afar.osaio.widget.helper.RecyclerMarginClickHelper;
import com.nooie.common.utils.configure.CountryUtil;
import com.nooie.common.utils.file.MediaStoreUtil;
import com.nooie.common.utils.json.GsonHelper;
import com.nooie.common.utils.tool.TaskUtil;
import com.nooie.data.EventDictionary;
import com.nooie.eventtracking.EventTrackingApi;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.cache.DeviceConfigureCache;
import com.nooie.sdk.cache.DeviceConnectionCache;
import com.afar.osaio.smart.device.bean.CloudFileBean;
import com.afar.osaio.smart.player.delegate.PlayState;
import com.afar.osaio.smart.player.delegate.PlayerDelegate;
import com.afar.osaio.smart.scan.helper.ApHelper;
import com.afar.osaio.widget.component.PanelComponent;
import com.nooie.common.base.GlobalData;
import com.nooie.common.bean.CConstant;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.file.FileUtil;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.time.DateTimeUtil;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.AppVersionResult;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.api.network.base.bean.entity.PackInfoResult;
import com.nooie.sdk.db.entity.DeviceConfigureEntity;
import com.nooie.sdk.db.entity.DeviceHardVersionEntity;
import com.nooie.sdk.processor.cloud.CloudManager;
import com.nooie.sdk.cache.DetectionThumbnailCache;
import com.nooie.sdk.processor.cloud.helper.CloudHelper;
import com.nooie.sdk.processor.cloud.listener.DetectionThumbnailCacheListener;
import com.nooie.sdk.base.Constant;
import com.nooie.sdk.device.DeviceCmdService;
import com.nooie.sdk.device.bean.RecordFragment;
import com.nooie.sdk.device.listener.OnSwitchStateListener;
import com.nooie.sdk.listener.OnActionResultListener;
import com.nooie.sdk.media.NooieMediaPlayer;
import com.nooie.sdk.media.listener.PlayerClickListener;
import com.nooie.sdk.media.listener.PlayerDoubleClickListener;
import com.nooie.sdk.media.listener.PlayerGestureListener;
import com.nooie.sdk.processor.cmd.DeviceCmdApi;
import com.scenery7f.timeaxis.view.UtcTimerShaft;
import com.afar.osaio.BuildConfig;
import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.nooie.sdk.bean.IpcType;
import com.afar.osaio.smart.device.bean.CloudRecordInfo;
import com.afar.osaio.smart.device.bean.DeviceInfo;
import com.afar.osaio.smart.device.adapter.NooiePlayerDevicesAdapter;
import com.afar.osaio.smart.device.helper.NooieCloudHelper;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.home.activity.HomeActivity;
import com.afar.osaio.smart.player.presenter.LivePlayerPresenter;
import com.afar.osaio.smart.player.contract.PlayContract;
import com.afar.osaio.smart.player.presenter.PlaybackComponent;
import com.afar.osaio.smart.setting.activity.NooieDeviceInfoActivity;
import com.afar.osaio.smart.setting.activity.NooieDeviceSettingActivity;
import com.afar.osaio.smart.setting.activity.NooieStorageActivity;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.util.Util;
import com.afar.osaio.util.preference.GlobalPrefs;
import com.afar.osaio.widget.CalenderBean;
import com.afar.osaio.widget.NormalTextIconView;
import com.afar.osaio.widget.UtcDateSelectView;
import com.afar.osaio.widget.listener.OnRecyclerItemClickListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.droidsonroids.gif.AnimationListener;
import pl.droidsonroids.gif.GifDrawable;
import pub.devrel.easypermissions.EasyPermissions;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_FLING;
import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE;
import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL;

/**
 * Created by Victor on 2019/4/1
 * Email: victor.qiao.0604@gmail.com
 * Copyright © 2020年 Victor. All rights reserved.
 */

public class NooiePlayActivity extends NooieBasePlayerActivity implements PlayContract.View {

    @BindView(R.id.playerMenuBar)
    View playerMenuBar;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.ivTitleRightIcon)
    ImageView ivTitleRightIcon;
    @BindView(R.id.tvLive)
    TextView tvLive;
    @BindView(R.id.containerOperationPortrait)
    View containerOperationPortrait;
    @BindView(R.id.containerCtrlPortrait)
    ViewGroup containerCtrlPortrait;
    @BindView(R.id.tivPhoto)
    NormalTextIconView tivPhoto;
    @BindView(R.id.tivTalk)
    NormalTextIconView tivTalk;
    @BindView(R.id.tivRecord)
    NormalTextIconView tivRecord;
    @BindView(R.id.tivSnapShot)
    NormalTextIconView tivSnapShot;
    @BindView(R.id.tivAudio)
    NormalTextIconView tivAudio;
    @BindView(R.id.ivAudioPlaybackPortrait)
    ImageView ivAudioPlaybackPortrait;
    @BindView(R.id.tivAlarm)
    NormalTextIconView tivAlarm;
    @BindView(R.id.tivFlashLight)
    NormalTextIconView tivFlashLight;
    @BindView(R.id.containerDateTimePortrait)
    ConstraintLayout containerDateTimePortrait;
    @BindView(R.id.dateSelectViewPortrait)
    UtcDateSelectView dateSelectViewPortrait;
    @BindView(R.id.ivPlaybackIcon)
    ImageView ivPlaybackIcon;
    @BindView(R.id.ivPlaybackFirst)
    ImageView ivSwitchCloudSd;
    @BindView(R.id.ivPlaybackSecond)
    ImageView ivOtherPlayback;
    @BindView(R.id.ivPlaybackSwitchArrow)
    ImageView ivNextPlayback;
    @BindView(R.id.playbackSwitchContainer)
    View containerOtherPlayback;
    @BindView(R.id.ivSeeHistoryBuyCloud)
    ImageView ivSeeHistoryBuyCloud;
    @BindView(R.id.tvSeeHistory)
    TextView tvSeeHistory;
    @BindView(R.id.containerCloudTipPortrait)
    View containerCloudTipPortrait;
    @BindView(R.id.btnPlayFullScreenPortrait)
    ImageView btnPlayFullScreenPortrait;

    @BindView(R.id.containerPlaybackPortrait)
    View containerPlaybackPortrait;
    @BindView(R.id.swipe_target)
    RecyclerView rcvPlaybackDetection;
    @BindView(R.id.timerShaftPortrait)
    UtcTimerShaft timerShaftPortrait;
    @BindView(R.id.sl_device_list)
    SwipeToLoadLayout swtllPlaybackDetection;
    @BindView(R.id.vPlaybackVideoEmpty)
    View vPlaybackVideoEmpty;
    @BindView(R.id.vPlaybackDetectionSettingBg)
    View vPlaybackDetectionSettingBg;
    @BindView(R.id.tvPlaybackDetectionSettingTip)
    TextView tvPlaybackDetectionSettingTip;
    @BindView(R.id.tvPlaybackDetectionSettingEnable)
    TextView tvPlaybackDetectionSettingEnable;

    @BindView(R.id.rvDeviceList)
    RecyclerView rvDeviceList;

    //Land
    @BindView(R.id.containerDateAndPlaybackLand)
    View containerDateAndPlaybackLand;
    @BindView(R.id.containerDateTimeLand)
    View containerDateTimeLand;
    @BindView(R.id.dateSelectViewLand)
    UtcDateSelectView dateSelectViewLand;
    @BindView(R.id.ivSwitchCloudSdLand)
    ImageView ivSwitchCloudSdLand;
    @BindView(R.id.ivOtherPlaybackLand)
    ImageView ivOtherPlaybackLand;
    @BindView(R.id.ivNextPlaybackLand)
    ImageView ivNextPlaybackLand;
    @BindView(R.id.containerOtherPlaybackLand)
    LinearLayout containerOtherPlaybackLand;
    @BindView(R.id.ivSeeHistoryBuyCloudLand)
    ImageView ivSeeHistoryBuyCloudLand;
    @BindView(R.id.tvSeeHistoryLand)
    TextView tvSeeHistoryLand;

    @BindView(R.id.containerPlaybackLand)
    View containerPlaybackLand;
    @BindView(R.id.timerShaftLand)
    UtcTimerShaft timerShaftLand;

    @BindView(R.id.containerOperationLand)
    View containerOperationLand;
    @BindView(R.id.containerCtrlLand)
    ViewGroup containerCtrlLand;
    @BindView(R.id.ivRecordLand)
    ImageView ivRecordLand;
    @BindView(R.id.ivSnapShotLand)
    ImageView ivSnapShotLand;
    @BindView(R.id.ivAlarmLand)
    ImageView ivAlarmLand;
    @BindView(R.id.ivTalkLand)
    ImageView ivTalkLand;
    @BindView(R.id.ivAudioLand)
    ImageView ivAudioLand;
    @BindView(R.id.ivCloudSaveLand)
    ImageView ivCloudSaveLand;
    @BindView(R.id.ivFlashLightLand)
    ImageView ivFlashLightLand;
    @BindView(R.id.containerPlaybackSave)
    View containerPlaybackSave;
    @BindView(R.id.btnPlayFullScreen)
    ImageView btnPlayFullScreen;
    @BindView(R.id.containerPlaybackController)
    View containerPlaybackController;
    @BindView(R.id.btnPlaybackPre)
    ImageView btnPlaybackPre;
    @BindView(R.id.btnPlaybackRestartOrStop)
    ImageView btnPlaybackRestartOrStop;
    @BindView(R.id.btnPlaybackNext)
    ImageView btnPlaybackNext;

    @BindView(R.id.tvCameraNameLand)
    TextView tvCameraNameLand;
    @BindView(R.id.deviceListContainer)
    RelativeLayout deviceListContainer;
    @BindView(R.id.tvCurrentDeviceName)
    TextView tvCurrentDeviceName;
    @BindView(R.id.rvDeviceListLand)
    RecyclerView rvDeviceListLand;
    @BindView(R.id.vPlayerBg)
    View vPlayerBg;
    @BindView(R.id.deviceNameTitleLandContainer)
    View deviceNameTitleLandContainer;
    @BindView(R.id.ivCameraNameLandIcon)
    ImageView ivCameraNameLandIcon;

    @BindView(R.id.tvFps)
    TextView tvFps;
    @BindView(R.id.tvBitrate)
    TextView tvBitrate;
    @BindView(R.id.containerLpDeviceController)
    View containerLpDeviceController;
    @BindView(R.id.ivPanelComponent)
    ImageView ivPanelComponent;
    @BindView(R.id.tvPanelTip)
    TextView tvPanelTip;
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
    @BindView(R.id.bubbleContainer)
    View bubbleContainer;

    private IpcType mDeviceType;
    private String mModel;
    private PlayContract.Presenter mPlayerPresenter;
    private boolean mOpenCloud;
    private boolean mHasSDCard;
    private boolean mIsEventCloud;
    private boolean mIsOwner = false;
    private boolean mIsSubDevice = false;
    private boolean mIsLpDevice = false;
    private String mPDeviceId;
    private int mConnectionMode = ConstantValue.CONNECTION_MODE_QC;
    private String mDeviceSsid;
    private List<DeviceInfo> mAllDeviceInfo = new ArrayList<>();
    private NooiePlayerDevicesAdapter mDeviceListAdapter;

    private PlaybackComponent mPlaybackComponent;
    private boolean mIsSubscribeCloud = false;
    private long mPlaybackSelectedDay = 0;
    private boolean mIsLeaveLivePage = false;
    private AlertDialog mLiveLimitTimeDialog;
    private Dialog mCloudSubscribeTipDialog;
    private Dialog mShowUpdateDialog;

    private static final String[] LEVELS = {"CLOSE", "LOW", "MEDIUM", "HEIGHT"};

    public static void startPlayLiveActivity(Context from, String deviceId, IpcType ipcType, String ip, int port) {
        Intent intent = new Intent(from, NooiePlayActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        intent.putExtra(ConstantValue.INTENT_KEY_IPC_MODEL, ipcType.getType());
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_IP, ip);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_PORT, port);
        from.startActivity(intent);
    }

    public static void startPlayActivity(Context from, String deviceId, IpcType ipcType, int playbackType, int playbackSourceType, long directTime, int routeSource, int connectionMode) {
        Intent intent = new Intent(from, NooiePlayActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        intent.putExtra(ConstantValue.INTENT_KEY_IPC_MODEL, ipcType.getType());
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_TYPE, playbackType);
        intent.putExtra(ConstantValue.INTENT_KEY_TIME_STAMP, directTime);
        intent.putExtra(ConstantValue.INTENT_KEY_START, playbackSourceType);
        intent.putExtra(ConstantValue.INTENT_KEY_ROUTE_SOURCE, routeSource);
        intent.putExtra(ConstantValue.INTENT_KEY_CONNECTION_MODE, connectionMode);
        from.startActivity(intent);
    }

    public static void startPlayActivity(Context from, String deviceId, String model, int playbackType, int playbackSourceType, long directTime, int routeSource, int connectionMode, String deviceSsid) {
        Intent intent = new Intent(from, NooiePlayActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        intent.putExtra(ConstantValue.INTENT_KEY_IPC_MODEL, model);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_TYPE, playbackType);
        intent.putExtra(ConstantValue.INTENT_KEY_TIME_STAMP, directTime);
        intent.putExtra(ConstantValue.INTENT_KEY_START, playbackSourceType);
        intent.putExtra(ConstantValue.INTENT_KEY_ROUTE_SOURCE, routeSource);
        intent.putExtra(ConstantValue.INTENT_KEY_CONNECTION_MODE, connectionMode);
        intent.putExtra(ConstantValue.INTENT_KEY_SSID, deviceSsid);
        from.startActivity(intent);
    }

    public static void startPlayActivityBySingleTop(Context from, String deviceId, String model, int playbackType, int playbackSourceType, long directTime, int routeSource, int connectionMode) {
        Intent intent = new Intent(from, NooiePlayActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        intent.putExtra(ConstantValue.INTENT_KEY_IPC_MODEL, model);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_TYPE, playbackType);
        intent.putExtra(ConstantValue.INTENT_KEY_TIME_STAMP, directTime);
        intent.putExtra(ConstantValue.INTENT_KEY_START, playbackSourceType);
        intent.putExtra(ConstantValue.INTENT_KEY_ROUTE_SOURCE, routeSource);
        intent.putExtra(ConstantValue.INTENT_KEY_CONNECTION_MODE, connectionMode);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        NooieLog.d("-->> NooiePlayActivity onCreate");
        super.onCreate(savedInstanceState);
        setSlideable(false);
        setContentView(R.layout.activity_nooie_play);
        ButterKnife.bind(this);
        //竖屏显示
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        setOrientationType(ORIENTATION_TYPE_PORTRAIT);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);

        initView();
        initData();

        setPlayPtzCtrl();
        setupDetectionThumbnailListener();
        checkDeviceConfigure();
        tryDisconnectBluetooth();
    }

    @Override
    public void onResume() {
        NooieLog.d("-->> NooiePlayActivity onResume 1");
        super.onResume();
        mDevicePowerMode = ConstantValue.DEVICE_POWER_MODE_NORMAL;
        if (mIsLive) {
            resumeData(true, false, false);
            if (mIsLeaveLivePage) {
                mIsLeaveLivePage = false;
                loadStorageInfo();
            }
        } else {
            resumePlaybackData(true);
        }
        onConfigurationChanged(getResources().getConfiguration());
        keepScreenLongLight();
        NooieLog.d("-->> NooiePlayActivity onResume 2");
        if (mConnectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT && mPlayerPresenter != null) {
            //long deviceTime = System.currentTimeMillis() / 1000L + (long)(DataHelper.toFloat(CountryUtil.getCurrentTimezone()) * 3600);
            long deviceTime = NooieDeviceHelper.getCurrentTimeMillisForApDevice(mModel, CountryUtil.getCurrentTimeZone()) / 1000L;
            NooieLog.d("-->> debug NooiePlayActivity onResume: deviceTime=" + deviceTime + " date=" + DateTimeUtil.localToUtc(deviceTime * 1000L, DateTimeUtil.PATTERN_YMD_HMS_1));
            mPlayerPresenter.setTimeForApDevice(mDeviceId, deviceTime, mModel);
            ApHelper.getInstance().updateApDirectConnectionErrorCount(true);
            NotificationManager.getInstance().cancelAllNotifications();
            checkApDeviceUpgrade();
        }
        registerDeviceCmdReceiver();
    }

    private void checkApDeviceUpgrade() {
        BindDevice device = getDevice(mConnectionMode, mDeviceId);
        String version = device != null ? device.getVersion() : "";
        boolean isCheckUpdateInfo = isFirstLaunch() && mIsOwner && !TextUtils.isEmpty(mModel) && !TextUtils.isEmpty(version);
        if (!isCheckUpdateInfo) {
            return;
        }
        if (mPlayerPresenter != null) {
            mPlayerPresenter.getApDeviceUpgradeInfo(mModel, version);
        }
    }

    @Override
    protected void onPause() {
        NooieLog.d("-->> NooiePlayActivity onPause 1 play isPlayStarting=" + isPlayStarting());
        super.onPause();
        displayLiveTag(false);
        displayFpsAndBit(false);
        resetDefault(true, true);
        clearKeepScreenLongLight();
        Util.delayTask(1000, new Util.OnDelayTaskFinishListener() {
            @Override
            public void onFinish() {
                for (String file : CollectionUtil.safeFor(mSaveRcecordFile)) {
                    sendRefreshPicture(file);
                }
            }
        });
        unregisterDeviceCmdReceiver();
        hideLiveLimitTimeDialog();
        hideDeviceListView(mIsLandScreen);
        hidePanelGuideView();
        NooieLog.d("-->> NooiePlayActivity onPause 2");
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        NooieLog.d("-->> NooiePlayActivity onDestroy 1");
        super.onDestroy();
        if (player != null) {
            player.destroy();
            player = null;
        }
        if (!checkIsShortLinkDeviceFromDeviceMsg()) {
            destroyConnectShortLinkDevice(mModel, mIsSubDevice, mConnectionMode);
        }
        unregisterDetectionThumbnailListener(mThumbnailCacheListener);
        hideUpdateDialog();
        hideUpdatingWarningDialog();
        //hideLiveLimitTimeDialog();
        hideCloudSubscribeTipDialog();
        releaseRes();
        NooieLog.d("-->> NooiePlayActivity onDestroy 2");
    }

    @Override
    public void releaseRes() {
        if (mPlayerPresenter != null) {
            mPlayerPresenter.stopQueryDeviceTalkGuide(true);
            mPlayerPresenter.stopTalkBubbleTask();
            mPlayerPresenter.destroy();
        }

        if (mCloudDataList != null) {
            mCloudDataList.clear();
            mCloudDataList = null;
        }
        if (mSDCardDataList != null) {
            mSDCardDataList.clear();
            mSDCardDataList = null;
        }

        if (mPlaybackComponent != null) {
            mPlaybackComponent.release();
            mPlaybackComponent = null;
        }

        if (mAllDeviceInfo != null) {
            mAllDeviceInfo.clear();
            mAllDeviceInfo = null;
        }

        if (mDeviceListAdapter != null) {
            mDeviceListAdapter.release();
        }

        ivLeft = null;
        tvTitle = null;
        ivRight = null;
        ivTitleRightIcon = null;
        playerMenuBar = null;
        tvLive = null;
        containerOperationPortrait = null;
        containerCtrlPortrait = null;
        ivAudioPlaybackPortrait = null;
        containerDateTimePortrait = null;
        if (dateSelectViewPortrait != null) {
            dateSelectViewPortrait.release();
            dateSelectViewPortrait = null;
        }
        ivPlaybackIcon = null;
        ivSwitchCloudSd = null;
        ivOtherPlayback = null;
        ivNextPlayback = null;
        containerOtherPlayback = null;
        ivSeeHistoryBuyCloud = null;
        tvSeeHistory = null;
        containerCloudTipPortrait = null;
        btnPlayFullScreenPortrait = null;
        containerPlaybackPortrait = null;
        if (rcvPlaybackDetection != null) {
            rcvPlaybackDetection.setAdapter(null);
            rcvPlaybackDetection = null;
        }
        if (timerShaftPortrait != null) {
            timerShaftPortrait.release();
            timerShaftPortrait = null;
        }
        if (rvDeviceList != null) {
            rvDeviceList.setAdapter(null);
            rvDeviceList = null;
        }
        containerDateAndPlaybackLand = null;
        containerDateTimeLand = null;
        if (dateSelectViewLand != null) {
            dateSelectViewLand.release();
            dateSelectViewLand = null;
        }
        ivSwitchCloudSdLand = null;
        ivOtherPlaybackLand = null;
        ivNextPlaybackLand = null;
        containerOtherPlaybackLand = null;
        ivSeeHistoryBuyCloudLand = null;
        tvSeeHistoryLand = null;
        containerPlaybackLand = null;
        if (timerShaftLand != null) {
            timerShaftLand.release();
            timerShaftLand = null;
        }
        containerOperationLand = null;
        containerCtrlLand = null;
        ivRecordLand = null;
        ivSnapShotLand = null;
        ivTalkLand = null;
        ivAudioLand = null;
        ivCloudSaveLand = null;
        containerPlaybackSave = null;
        btnPlayFullScreen = null;
        containerPlaybackController = null;
        btnPlaybackPre = null;
        btnPlaybackRestartOrStop = null;
        btnPlaybackNext = null;
        tvCameraNameLand = null;
        deviceListContainer = null;
        tvCurrentDeviceName = null;
        if (rvDeviceListLand != null) {
            rvDeviceListLand.setAdapter(null);
            rvDeviceListLand = null;
        }
        tvFps = null;
        tvBitrate = null;
        containerLpDeviceController = null;
        ivThumbnail = null;
        tvRecordTime = null;
        tvRecordTimeLand = null;

        if (tivAlarm != null) {
            tivAlarm.release();
            tivAlarm = null;
        }
        if (ivAlarmLand != null) {
            ivAlarmLand = null;
        }
        if (tivTalk != null) {
            tivTalk.release();
            tivTalk = null;
        }
        if (tivPhoto != null) {
            tivPhoto = null;
        }
        if (tivRecord != null) {
            tivRecord = null;
        }
        if (tivSnapShot != null) {
            tivSnapShot = null;
        }
        if (tivAudio != null) {
            tivAudio = null;
        }
        if (tivPhoto != null) {
            tivPhoto = null;
        }
        if (tivFlashLight != null) {
            tivFlashLight.release();
            tivFlashLight = null;
        }
        ivFlashLightLand = null;
        releaseGuideView();
        vPlayerBg = null;
        ivDirectionControlBg = null;
        ivGestureLeftArrow = null;
        ivGestureTopArrow = null;
        ivGestureRightArrow = null;
        ivGestureBottomArrow = null;
        bubbleContainer = null;
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        ivRight.setImageResource(R.drawable.settings_icon_state_list);
        player = findViewById(R.id.player);
        ivThumbnail = findViewById(R.id.ivThumbnail);
        tvRecordTime = findViewById(R.id.tvRecordTime);
        tvRecordTimeLand = findViewById(R.id.tvRecordTimeLand);
        setupDataSelectView();
        setupPlayerCtrlView();
        containerOtherPlayback.setTag(SWITCH_CLOUD_SDCARD_HIDE);
        ivOtherPlayback.setTag(SWITCH_CURRENT_IS_CLOUD);
        updatePlayBackType(false, false);
        ivPlaybackIcon.setTag(ConstantValue.PLAY_DISPLAY_TYPE_DETAIL);
        ivPlaybackIcon.setImageResource(R.drawable.play_display_detail);
        tivTalk.setTextIcon(R.drawable.talk_off_icon_state_list);
        tivAlarm.setTextIcon(R.drawable.alarm_off_icon_state_list);
        displayTalkBubble(false);

        setOrientationType(ORIENTATION_TYPE_PORTRAIT);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        setAngleChangeListener(new AngleChangeListener() {
            @Override
            public void onChange(int orientation) {
            }

            @Override
            public void onScreenOrientationChange(boolean isPortrait) {
                if (isPortrait == isLandscape()) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                }
            }
        });
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
            return;
        } else {
            mDeviceId = getIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
            mModel = TextUtils.isEmpty(getIntent().getStringExtra(ConstantValue.INTENT_KEY_IPC_MODEL)) ? IpcType.IPC_1080.getType() : getIntent().getStringExtra(ConstantValue.INTENT_KEY_IPC_MODEL);
            mDeviceType = TextUtils.isEmpty(getIntent().getStringExtra(ConstantValue.INTENT_KEY_IPC_MODEL)) ? IpcType.IPC_1080 : IpcType.getIpcType(getIntent().getStringExtra(ConstantValue.INTENT_KEY_IPC_MODEL));
            mConnectionMode = getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_QC);
            mDeviceSsid = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SSID);
            BindDevice device = getDevice(mConnectionMode, mDeviceId);
            if (TextUtils.isEmpty(mDeviceId) || device == null) {
                finish();
                return;
            }

            new LivePlayerPresenter(this);

            if (device != null) {
                tvTitle.setText(device.getName());
                tvCameraNameLand.setText(device.getName());
                tvCurrentDeviceName.setText(device.getName());
                mIsOwner = device.getBind_type() == ApiConstant.BIND_TYPE_OWNER;
                mIsSubDevice = NooieDeviceHelper.isSubDevice(device.getPuuid(), device.getType());
                mIsLpDevice = NooieDeviceHelper.isLpDevice(device.getType());
                mPDeviceId = device.getPuuid();
            }

            DeviceConfigureEntity deviceConfigureEntity = DeviceConfigureCache.getInstance().getDeviceConfigure(mDeviceId);
            mIsEventCloud = deviceConfigureEntity != null && NooieCloudHelper.isEventCloud(deviceConfigureEntity.getIsEvent());

            mPlaybackComponent = new PlaybackComponent();
            mPlaybackComponent.setView(this);
            int playbackType = getIntent().getIntExtra(ConstantValue.INTENT_KEY_DATA_TYPE, ConstantValue.NOOIE_PLAYBACK_TYPE_LIVE);
            int playbackSourceType = getIntent().getIntExtra(ConstantValue.INTENT_KEY_START, ConstantValue.NOOIE_PLAYBACK_SOURCE_TYPE_NORMAL);
            int modelType = NooieDeviceHelper.convertNooieModel(mDeviceType, mModel);
            long directTime = getCurrentIntent().getLongExtra(ConstantValue.INTENT_KEY_TIME_STAMP, 0);
            mPlaybackComponent.initData(mDeviceId, mIsOwner, mIsSubDevice, mIsLpDevice, mConnectionMode, modelType, playbackType, playbackSourceType, directTime, mModel, timerShaftPortrait, timerShaftLand, rcvPlaybackDetection, swtllPlaybackDetection);
            mPlaybackComponent.setupPlayback(playbackType, playbackSourceType, directTime);
            mPlaybackComponent.setPlayer(player);

            mIsLive = playbackType == ConstantValue.NOOIE_PLAYBACK_TYPE_LIVE;
            if (!mIsLive) {
                ivOtherPlayback.setTag(playbackType == ConstantValue.NOOIE_PLAYBACK_TYPE_CLOUD ? SWITCH_CURRENT_IS_CLOUD : SWITCH_CURRENT_IS_SDCARD);
                mPlaybackSelectedDay = directTime;
            }

            if (mConnectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
                containerOtherPlayback.setTag(SWITCH_CLOUD_SDCARD_HIDE);
                ivOtherPlayback.setTag(SWITCH_CURRENT_IS_SDCARD);
                setOrientationType(ORIENTATION_TYPE_NONE);
                ApHelper.getInstance().setIsEnterApDevicePage(true);
            } else {
                DeviceCmdService.getInstance(NooieApplication.mCtx).apRemoveConn(ConstantValue.VICTURE_AP_DIRECT_DEVICE_ID);
                DeviceCmdService.getInstance(NooieApplication.mCtx).apRemoveConn(mDeviceId);
                DeviceCmdService.getInstance(NooieApplication.mCtx).apRemoveConn(ConstantValue.DEFAULT_UUID_AP_P2P);
            }

            mPlayerDelegate = new PlayerDelegate();
            mPlayerDelegate.setPlayState(mIsLive ? PlayState.PLAY_TYPE_LIVE : (playbackType == ConstantValue.NOOIE_PLAYBACK_TYPE_CLOUD ? PlayState.PLAY_TYPE_CLOUD_PLAYBACK : PlayState.PLAY_TYPE_SD_PLAYBACK), PlayState.PLAY_STATE_INIT, 0);
            mPlaybackComponent.setPlayerDelegate(mPlayerDelegate);
            dateSelectViewLand.setPlayState(mPlayerDelegate.getPlayState());
            dateSelectViewPortrait.setPlayState(mPlayerDelegate.getPlayState());
            dateSelectViewLand.setPlayerDelegate(mPlayerDelegate);
            dateSelectViewPortrait.setPlayerDelegate(mPlayerDelegate);

            updatePlayBackType(false, false);
            updateCurrentItem(true);

            setupDeviceListView(true);

            //init audio remember
            GlobalPrefs.getPreferences(NooieApplication.mCtx).loadAudioInfo();
            setupAudioAlarmView();
            NooieLog.d("-->> NooiePlayActivity initData deviceId=" + mDeviceId + " pDeviceId=" + mPDeviceId + " deviceType=" + mDeviceType.getType());
            loadStorageInfo();
        }
    }

    private BindDevice getDevice(int connectionMode, String deviceId) {
        if (connectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            String defaultModel = mIsLpDevice ? IpcType.HC320.getType() : IpcType.MC120.getType();
            return NooieDeviceHelper.getDeviceByConnectionMode(connectionMode, deviceId, defaultModel);
        } else {
            return NooieDeviceHelper.getDeviceById(deviceId);
        }
    }
    private int getBindType() {
        return mIsOwner ? ApiConstant.BIND_TYPE_OWNER : ApiConstant.BIND_TYPE_SHARE;
    }

    private void setupAudioAlarmView() {
        tivAlarm.setTag(ConstantValue.ALARM_AUDIO_STATE_OFF);
        tivAlarm.setTextIcon(R.drawable.alarm_off_icon_state_list);
        ivAlarmLand.setImageResource(R.drawable.alarm_off_land_icon_state_list);
        tivAlarm.setVisibility(checkAlarmAudioEnable() ? View.VISIBLE : View.GONE);
        ivAlarmLand.setVisibility(checkAlarmAudioEnable() ? View.VISIBLE : View.GONE);
        tivFlashLight.setVisibility(checkFlashLightEnable() ? View.VISIBLE : View.GONE);
        ivFlashLightLand.setVisibility(checkFlashLightEnable() ? View.VISIBLE : View.GONE);
        tivFlashLight.setTag(ConstantValue.STATE_OFF);

        boolean isHideTalkIcon = (mConnectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT && NooieDeviceHelper.mergeIpcType(mDeviceType) == IpcType.MC120) || mDeviceType == IpcType.HC320;
        tivTalk.setVisibility(isHideTalkIcon ? View.GONE : View.VISIBLE);
        ivTalkLand.setVisibility(isHideTalkIcon ? View.GONE : View.VISIBLE);
        tivPhoto.setVisibility(NooieDeviceHelper.isSupportShootingSetting(mModel, mConnectionMode) ? View.VISIBLE : View.GONE);
    }

    private void resumeData(boolean isQueryUpdate, boolean isStartLpDevice, boolean delayStartVideo) {
        NooieLog.d("-->> NooiePlayActivity resumeData 1000");
        refreshDeviceInfo();
        refreshPlayerControl();
        displayFpsAndBit(true);
        player.setPlayerListener(this);
        if (delayStartVideo) {
            NooieLog.d("-->> debug NooiePlayActivity resumeData: 1001");
            int delayTime = NooieDeviceHelper.isSortLinkDevice(mModel, mIsSubDevice, mConnectionMode) ? 400 : 200;
            TaskUtil.delayAction(delayTime, new TaskUtil.OnDelayTimeFinishListener() {
                @Override
                public void onFinish() {
                    NooieLog.d("-->> debug NooiePlayActivity onFinish: 1002");
                    if (isPause() || isDestroyed()) {
                        return;
                    }
                    NooieLog.d("-->> debug NooiePlayActivity onFinish: 1003");
                    startVideo(isStartLpDevice);
                }
            });
        } else {
            NooieLog.d("-->> debug NooiePlayActivity resumeData: 1004");
            startVideo(isStartLpDevice);
        }
        if (isQueryUpdate && mConnectionMode != ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            mPlayerPresenter.queryNooieDeviceUpdateStatus(mDeviceId, mUserAccount);
        }
    }

    private void resumePlaybackData(boolean isQueryUpdate) {
        refreshPlayerControl();
        player.setPlayerListener(this);
        displayFpsAndBit(true);
        startPlaybackVideo();
        if (isQueryUpdate  && mConnectionMode != ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            mPlayerPresenter.queryNooieDeviceUpdateStatus(mDeviceId, mUserAccount);
        }
    }

    private void setPlayPtzCtrl() {
        setPtzControlOrientation(mDeviceType);
        player.setPlayerClickListener(new PlayerClickListener() {
            @Override
            public void onClick(NooieMediaPlayer player) {
                clickScreenOnLand();
            }
        });

        player.setPlayerDoubleClickListener(new PlayerDoubleClickListener() {
            @Override
            public void onDoubleClick(NooieMediaPlayer player) {
                doubleClickScreenOnLand();
            }
        });

        displayGestureGuideView(false, GESTURE_TOUCH_UP, mDeviceType);
        player.setGestureListener(new PlayerGestureListener() {
            @Override
            public void onMoveLeft(NooieMediaPlayer player) {
                displayGestureGuideView(true, GESTURE_MOVE_LEFT, mDeviceType);
            }

            @Override
            public void onMoveRight(NooieMediaPlayer player) {
                displayGestureGuideView(true, GESTURE_MOVE_RIGHT, mDeviceType);
            }

            @Override
            public void onMoveUp(NooieMediaPlayer player) {
                displayGestureGuideView(true, GESTURE_MOVE_TOP, mDeviceType);
            }

            @Override
            public void onMoveDown(NooieMediaPlayer player) {
                displayGestureGuideView(true, GESTURE_MOVE_BOTTOM, mDeviceType);
            }

            @Override
            public void onTouchUp(NooieMediaPlayer player) {
                displayGestureGuideView(false, GESTURE_TOUCH_UP, mDeviceType);
            }

            @Override
            public void onTouchDown(NooieMediaPlayer player) {
                displayGestureGuideView(true, GESTURE_TOUCH_DOWN, mDeviceType);
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

    private void resetDefault(boolean destroy, boolean isDestroyShortLinkDevice) {
        NooieLog.d("-->> NooiePlayActivity resetDefault 1");
        if (player != null) {
            //NooieLog.d("-->> NooieLiveActivity resetDefault playing=" + player.isPlayingng() + " recording=" + player.isRecording() + " sounding=" + player.isWaveout() + " talking=" + player.isTalking());
            stopAlarmAudio(mDeviceId, null);
            stopAlarmAudioMonitor();
            stopRetryTask();
            if (destroy) {
                player.setPlayerListener(null);
            }
            player.stop();
        }
        openFlashLight(false, false);
        /*
        if (isDestroyShortLinkDevice) {
            destroyConnectShortLinkDevice(mModel, mIsSubDevice, mConnectionMode);
        }

         */
        resetPlayerCtrl();
        stopLpCameraCameraTask(mLpCameraTaskId, true);
        stopLpDevicePlaybackTask();
        stopLpDeviceShortLinkTask();
        changePhoneVolume(false);
        sendPlaybackEvent();
        NooieLog.d("-->> NooiePlayActivity resetDefault 2");
    }

    private void stopPlayer() {
        NooieLog.d("-->> NooiePlayActivity stopPlayer 1");
        if (player != null) {
            stopAlarmAudio(mDeviceId, null);
            stopAlarmAudioMonitor();
            stopRetryTask();
            player.stop();
        }
        NooieLog.d("-->> NooiePlayActivity stopPlayer 2");
    }

    private void resetPlayerCtrl() {
        if (!isDestroyed() && !checkNull(tivRecord, ivRecordLand, tivAlarm, ivAlarmLand, tivTalk, ivTalkLand, tivAudio, ivAudioLand, ivAudioPlaybackPortrait, tivFlashLight, ivFlashLightLand)) {
            tivRecord.setTextIcon(R.drawable.record_icon_state_list);
            ivRecordLand.setImageResource(R.drawable.record_land_icon_state_list);
            tivAlarm.setTextIcon(R.drawable.alarm_off_icon_state_list);
            ivAlarmLand.setImageResource(R.drawable.alarm_off_land_icon_state_list);
            tivTalk.setTextIcon(R.drawable.talk_off_icon_state_list);
            ivTalkLand.setImageResource(R.drawable.talk_off_land_icon_state_list);
            tivAudio.setTextIcon(R.drawable.audio_off_icon_state_list);
            ivAudioLand.setImageResource(R.drawable.audio_off_land_icon_state_list);
            ivAudioPlaybackPortrait.setImageResource(R.drawable.audio_off_land_icon_state_list);
            tivFlashLight.setTextIcon(R.drawable.flash_light_off_icon_state_list);
            ivFlashLightLand.setImageResource(R.drawable.flash_light_off_land_icon_state_list);
            stopRecordTimer();
            DisplayUtil.enableViewAndChildren(containerCtrlLand, false);
            DisplayUtil.enableViewAndChildren(containerCtrlPortrait, false);
            tivAlarm.setIvIconEnable(false);
            tivTalk.setIvIconEnable(false);
            tivPhoto.setIvIconEnable(true);
            ivAudioPlaybackPortrait.setEnabled(false);
        }
    }

    private void refreshDeviceInfo() {
        BindDevice device = getDevice(mConnectionMode, mDeviceId);
        if (TextUtils.isEmpty(mDeviceId) || device == null) {
            finish();
            return;
        }
        if (device != null) {
            tvTitle.setText(device.getName());
            tvCameraNameLand.setText(device.getName());
            tvCurrentDeviceName.setText(device.getName());
        }
    }

    private void refreshPlayerControl() {
        if (checkNull(player, tivAlarm, ivAlarmLand, tivTalk, ivTalkLand, tivAudio, ivAudioLand, ivAudioPlaybackPortrait, tivFlashLight, ivFlashLightLand)) {
            return;
        }
        DisplayUtil.enableViewAndChildren(containerCtrlLand, player.isPlayingng());
        DisplayUtil.enableViewAndChildren(containerCtrlPortrait, player.isPlayingng());
        tivAlarm.setIvIconEnable(player.isPlayingng());
        tivTalk.setIvIconEnable(player.isPlayingng());
        tivPhoto.setIvIconEnable(true);
        ivAudioPlaybackPortrait.setEnabled(player.isPlayingng());

        if (player.isWaveout()) {
            tivAudio.setTextIcon(R.drawable.audio_on_icon_state_list);
            ivAudioLand.setImageResource(R.drawable.audio_on_land_icon_state_list);
            ivAudioPlaybackPortrait.setImageResource(R.drawable.audio_on_land_icon_state_list);
        } else {
            tivAudio.setTextIcon(R.drawable.audio_off_icon_state_list);
            ivAudioLand.setImageResource(R.drawable.audio_off_land_icon_state_list);
            ivAudioPlaybackPortrait.setImageResource(R.drawable.audio_off_land_icon_state_list);
        }
    }

    private void setupDeviceListView(boolean isInit) {
        if (mAllDeviceInfo == null) {
            mAllDeviceInfo = new ArrayList<>();
        }
        mAllDeviceInfo.clear();
        if (mConnectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            tvTitle.setTextColor(getResources().getColor(R.color.theme_text_color));
            tvCameraNameLand.setTextColor(getResources().getColor(R.color.theme_white));
            ivCameraNameLandIcon.setVisibility(View.GONE);
            ivTitleRightIcon.setVisibility(View.GONE);
            if (!isInit && mDeviceListAdapter != null) {
                mDeviceListAdapter.setData(mAllDeviceInfo);
            }
            return;
        }
        for (DeviceInfo device : CollectionUtil.safeFor(NooieDeviceHelper.getAllDeviceInfo())) {
            if (device != null && device.getNooieDevice() != null && !TextUtils.isEmpty(device.getNooieDevice().getUuid()) && !device.getNooieDevice().getUuid().equalsIgnoreCase(mDeviceId) && (device.getNooieDevice().getOnline() == ApiConstant.ONLINE_STATUS_ON || device.isOpenCloud())) {
                mAllDeviceInfo.add(device);
            }
        }

        if (isInit) {
            mDeviceListAdapter = new NooiePlayerDevicesAdapter(mAllDeviceInfo);
            mDeviceListAdapter.setListener(new NooiePlayerDevicesAdapter.OnClickLiveDeviceItemListener() {
                @Override
                public void onClickItem(String deviceId) {
                    hideDeviceListAnim(500, false);
                    hideDeviceListAnim(500, true);
                    changeCurrentDevice(deviceId);
                }
            });
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            rvDeviceList.setLayoutManager(layoutManager);
            rvDeviceList.setAdapter(mDeviceListAdapter);
            RecyclerMarginClickHelper recyclerMarginClickHelper = new RecyclerMarginClickHelper();
            recyclerMarginClickHelper.setOnMarginClickListener(rvDeviceList, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NooieLog.d("-->> debug NooiePlayActivity recyclerMarginClickHelper onClick: ");
                    hideDeviceListView(false);
                }
            });

            LinearLayoutManager landLayoutManager = new LinearLayoutManager(this);
            rvDeviceListLand.setLayoutManager(landLayoutManager);
            rvDeviceListLand.setAdapter(mDeviceListAdapter);
        } else {
            mDeviceListAdapter.setData(mAllDeviceInfo);
        }

        if (mAllDeviceInfo.size() > 0) {
            tvTitle.setTextColor(getResources().getColor(R.color.theme_text_color));
            tvCameraNameLand.setTextColor(getResources().getColor(R.color.theme_white));
            ivCameraNameLandIcon.setVisibility(View.VISIBLE);
            ivTitleRightIcon.setVisibility(View.VISIBLE);
        } else {
            tvTitle.setTextColor(getResources().getColor(R.color.theme_text_color));
            tvCameraNameLand.setTextColor(getResources().getColor(R.color.theme_white));
            ivCameraNameLandIcon.setVisibility(View.GONE);
            ivTitleRightIcon.setVisibility(View.GONE);
        }
    }

    private void toggleDeviceListView() {
        if (checkNull(mAllDeviceInfo, rvDeviceList, tvTitle, ivTitleRightIcon) || CollectionUtil.isEmpty(mAllDeviceInfo)) {
            return;
        }

        if (rvDeviceList.getVisibility() == View.VISIBLE) {
            tvTitle.setTextColor(getResources().getColor(R.color.theme_text_color));
            ivTitleRightIcon.setVisibility(View.VISIBLE);
            hideDeviceListAnim(500, false);
        } else {
            tvTitle.setTextColor(getResources().getColor(R.color.theme_green));
            ivTitleRightIcon.setVisibility(View.GONE);
            showDeviceListAnim(false);
        }
    }

    private void toggleDeviceListLandView() {
        if (checkNull(deviceListContainer, tvCameraNameLand) || CollectionUtil.isEmpty(mAllDeviceInfo)) {
            return;
        }

        if (deviceListContainer.getVisibility() == View.VISIBLE) {
            tvCameraNameLand.setTextColor(getResources().getColor(R.color.theme_white));
            hideDeviceListAnim(500, true);
        } else {
            tvCameraNameLand.setTextColor(getResources().getColor(R.color.theme_white));
            showDeviceListAnim(true);
        }
    }

    private void hideDeviceListTitleView(boolean isLand) {
        if (isDestroyed() || checkNull(mAllDeviceInfo, tvCameraNameLand, tvTitle, ivTitleRightIcon) || CollectionUtil.isEmpty(mAllDeviceInfo)) {
            return;
        }

        if (isLand) {
            tvCameraNameLand.setTextColor(getResources().getColor(R.color.theme_white));
        } else {
            tvTitle.setTextColor(getResources().getColor(R.color.theme_text_color));
            ivTitleRightIcon.setVisibility(View.VISIBLE);
        }
    }

    private void hideDeviceListView(boolean isLand) {
        if (isDestroyed() || checkNull(mAllDeviceInfo, tvCameraNameLand, tvTitle, ivTitleRightIcon) || CollectionUtil.isEmpty(mAllDeviceInfo)) {
            return;
        }

        if (isLand) {
            tvCameraNameLand.setTextColor(getResources().getColor(R.color.theme_white));
        } else {
            tvTitle.setTextColor(getResources().getColor(R.color.theme_text_color));
            ivTitleRightIcon.setVisibility(View.VISIBLE);
        }
        hideDeviceListAnim(0, isLand);
    }

    private void changeCurrentDevice(String deviceId) {
        if (isPlayStarting()) {
            showPlayerIsReleasing(true);
            return;
        }
        if (TextUtils.isEmpty(deviceId) || mDeviceId.equalsIgnoreCase(deviceId)) {
            return;
        }
        stopAlarmAudio(mDeviceId, null);

        mDeviceId = deviceId;
        BindDevice device = getDevice(mConnectionMode, mDeviceId);
        if (device != null) {
            tvTitle.setText(device.getName());
            tvCameraNameLand.setText(device.getName());
            tvCurrentDeviceName.setText(device.getName());
            mIsOwner = device.getBind_type() == ApiConstant.BIND_TYPE_OWNER;
            mModel = TextUtils.isEmpty(device.getType()) ? IpcType.IPC_1080.getType() : device.getType();
            mDeviceType = TextUtils.isEmpty(device.getType()) ? IpcType.IPC_1080 : IpcType.getIpcType(device.getType());
            mIsSubDevice = NooieDeviceHelper.isSubDevice(device.getPuuid(), device.getType());
            mIsLpDevice = NooieDeviceHelper.isLpDevice(device.getType());
            mPDeviceId = device.getPuuid();
        }
        mIsSubscribeCloud = false;
        mHasSDCard = false;
        mOpenCloud = false;
        DeviceConfigureEntity deviceConfigureEntity = DeviceConfigureCache.getInstance().getDeviceConfigure(mDeviceId);
        mIsEventCloud = deviceConfigureEntity != null && NooieCloudHelper.isEventCloud(deviceConfigureEntity.getIsEvent());
        if (mPlayerPresenter != null) {
            mPlayerPresenter.resetDataEffectCache();
        }

        setPtzControlOrientation(mDeviceType);
        if (isLandscape()) {
            /*
            showActionViewLand(0);
            containerDateTimeLand.setVisibility(isShowPlaybackView(true) ? View.VISIBLE : View.GONE);
             */
            hideActionViewLand(0);
        } else {
            containerDateTimePortrait.setVisibility(isShowPlaybackView(false) ? View.VISIBLE : View.GONE);
            containerOtherPlayback.setVisibility(isShowPlaybackView(false) ? View.VISIBLE : View.GONE);
        }
        setupRecentDays();
        dateSelectViewPortrait.updateData();
        dateSelectViewLand.updateData();
        containerOtherPlayback.setTag(SWITCH_CLOUD_SDCARD_HIDE);
        ivOtherPlayback.setTag(SWITCH_CURRENT_IS_CLOUD);
        ivPlaybackIcon.setTag(ConstantValue.PLAY_DISPLAY_TYPE_DETAIL);
        ivPlaybackIcon.setImageResource(R.drawable.play_display_detail);
        updatePlayBackType(false, false);
        containerCloudTipPortrait.setVisibility(View.GONE);
        setupDeviceListView(false);
        setupAudioAlarmView();
        if (mPlayerPresenter != null) {
            mPlayerPresenter.stopQueryDeviceTalkGuide(true);
        }
        displayTalkBubble(false);

        if (mPlaybackComponent != null) {
            mPlaybackComponent.resetPlayback(mDeviceId, mIsOwner, mIsSubDevice, mIsLpDevice, mConnectionMode, NooieDeviceHelper.convertNooieModel(mDeviceType, mModel), ConstantValue.NOOIE_PLAYBACK_TYPE_LIVE, ConstantValue.NOOIE_PLAYBACK_SOURCE_TYPE_NORMAL, 0, mModel);
        }
        gotoLive(true);
        checkDeviceConfigure();
        loadStorageInfo();
    }

    private void setupDataSelectView() {
        dateSelectViewPortrait.setData(mCloudDataList);
        dateSelectViewPortrait.setOnStartScroolListener(mScrollListener);
        dateSelectViewPortrait.setOnRecyclerItemClickListener(new OnRecyclerItemClickListener() {
            @Override
            public void onClickItem(Calendar object) {
                if (isPlayStarting()) {
                    showPlayerIsReleasing(false);
                }
            }

            @Override
            public void onClickItem(Calendar object, boolean isLive, long currentSeekDay) {
                NooieLog.d("-->> debug NooiePlayActivity onClickItem: 1 isLive=" + isLive + " isLoadStorageFinish=" + isLoadStorageFinish() + " mIsLive=" + mIsLive);
                if (isLive && mIsLive) {
                    return;
                }
                if (dateSelectViewLand != null) {
                    dateSelectViewLand.updateCurrentState(object, isLive, currentSeekDay);
                }
                if (!mOpenCloud && !mHasSDCard) {
                    return;
                }
                if (object == null) {
                    gotoLive(false);
                } else {
                    gotoPlayback(object);
                }
            }
        });

        dateSelectViewLand.setData(mCloudDataList);
        dateSelectViewLand.setTextColor(R.color.theme_white);
        dateSelectViewLand.setOnStartScroolListener(mScrollListener);
        dateSelectViewLand.setOnRecyclerItemClickListener(new OnRecyclerItemClickListener() {
            @Override
            public void onClickItem(Calendar object) {
                if (isPlayStarting()) {
                    showPlayerIsReleasing(false);
                }
            }

            @Override
            public void onClickItem(Calendar object, boolean isLive, long currentSeekDay) {
                NooieLog.d("-->> debug NooiePlayActivity onClickItem: 2 isLive=" + isLive + " isLoadStorageFinish=" + isLoadStorageFinish() + " mIsLive=" + mIsLive);
                if (isLive && mIsLive) {
                    return;
                }
                if (dateSelectViewPortrait != null) {
                    dateSelectViewPortrait.updateCurrentState(object, isLive, currentSeekDay);
                }
                if (!mOpenCloud && !mHasSDCard) {
                    return;
                }
                if (object == null) {
                    gotoLive(false);
                } else {
                    gotoPlayback(object);
                }
            }
        });
    }

    private void setupPlayerCtrlView() {
        tivPhoto.setTextIcon(R.drawable.photo_icon_state_list);
        tivPhoto.setTextTitle(getResources().getString(R.string.nooie_play_camera_photo_title));
        tivTalk.setTextIcon(R.drawable.talk_level_list);
        tivTalk.setTextTitle(getResources().getString(R.string.nooie_play_talk_title));
        tivRecord.setTextIcon(R.drawable.record_icon_state_list);
        tivRecord.setTextTitle(getResources().getString(R.string.nooie_play_record_title));
        tivSnapShot.setTextIcon(R.drawable.snapshot_icon_state_list);
        tivSnapShot.setTextTitle(getResources().getString(R.string.nooie_play_snap_shot_title));
        tivAudio.setTextIcon(R.drawable.audio_off_icon_state_list);
        tivAudio.setTextTitle(getResources().getString(R.string.nooie_play_audio_title));
        tivAlarm.setTextIcon(R.drawable.alarm_level_list);
        tivAlarm.setTextTitle(getResources().getString(R.string.nooie_play_alarm_title));
        tivFlashLight.setTextIcon(R.drawable.flash_light_off_icon_state_list);
        tivFlashLight.setTextTitle(getString(R.string.cam_setting_flash_light));
    }

    private boolean mIsLive = true;

    private void gotoLive(boolean isNewDevice) {
        mIsLive = true;
        mPlaybackSelectedDay = 0;
        BindDevice device = getDevice(mConnectionMode, mDeviceId);
        boolean isOnLine = device != null ? (device.getOnline() == ApiConstant.ONLINE_STATUS_ON && device.getOpen_status() == ApiConstant.OPEN_STATUS_ON) : false;
        mDevicePowerMode = isOnLine && mIsLpDevice ? ConstantValue.DEVICE_POWER_MODE_LP_SLEEP : ConstantValue.DEVICE_POWER_MODE_NORMAL;
        resetDefault(true, true);
        if (isLandscape()) {
            showLandPlayer();
        } else {
            showPortraitPlayer();
        }
        displayFpsAndBit(true);
        showCameraNameLand(true);
        updateCurrentItem(true);
        if (mOpenCloud) {
            gotoCloudState();
        } else if (mHasSDCard) {
            gotoSDCardState();
        }
        resumeData(isNewDevice, false, true);
    }

    private void gotoPlayback(Calendar object) {
        if (checkNull(object, mPlaybackComponent, mPlayerPresenter, ivOtherPlayback)) {
            return;
        }

        mIsLive = false;
        mPlaybackSelectedDay = object.getTimeInMillis();
        mDevicePowerMode = ConstantValue.DEVICE_POWER_MODE_NORMAL;
        resetDefault(true, isCloudState());
        if (isLandscape()) {
            showLandPlayer();
        } else {
            showPortraitPlayer();
        }
        displayFpsAndBit(true);
        showCameraNameLand(true);
        player.setPlayerListener(this);
        mPlayerPresenter.stopLoadRecordTask();
        boolean cloud = isCloudState();
        mPlaybackComponent.clearRecordList(cloud);
        mPlaybackComponent.setupPlayback(cloud ? ConstantValue.NOOIE_PLAYBACK_TYPE_CLOUD : ConstantValue.NOOIE_PLAYBACK_TYPE_SD, ConstantValue.NOOIE_PLAYBACK_SOURCE_TYPE_NORMAL, object.getTimeInMillis());
        mPlaybackTaskId = UUID.randomUUID().toString();
        startLoadPlaybackList(cloud, object.getTimeInMillis() / 1000L, true);
    }

    private void switchPlayback() {
        if (mIsLive || checkNull(ivOtherPlayback, mPlaybackComponent)) {
            return;
        }
        resetDefault(true, true);
        player.setPlayerListener(this);
        displayFpsAndBit(true);
        mPlayerPresenter.stopLoadRecordTask();
        boolean cloud = isCloudState();
        mPlaybackComponent.clearRecordList(cloud);
        mPlaybackComponent.setPlaybackType(cloud ? ConstantValue.NOOIE_PLAYBACK_TYPE_CLOUD : ConstantValue.NOOIE_PLAYBACK_TYPE_SD);
        mPlaybackTaskId = UUID.randomUUID().toString();
        startLoadPlaybackList(cloud, (mPlaybackComponent.getTodayStartTime() / 1000L), true);
    }

    private RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            /*
            if ((Integer) containerOtherPlayback.getTag() == SWITCH_CLOUD_SDCARD_SHOW) {
                hideOtherPlaybackViewAnim(500);
            }
            */
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == SCROLL_STATE_TOUCH_SCROLL) {
                // 开始滑动
                //mHandler.removeCallbacks(autoHideActionViewLandWorker);
            } else if (newState == SCROLL_STATE_FLING) {
                // 自己滚动
            } else if (newState == SCROLL_STATE_IDLE) {
                // 滑动结束
                /*
                if (isLandscape() && containerCtrlLand.isShown()) {
                    mHandler.postDelayed(autoHideActionViewLandWorker, ACTION_LAND_WIDGET_SHOW_LONG);
                }
                */
            } else {
            }
        }
    };

    @Override
    public void screenOrientationChanged(boolean landscape) {
        if (checkNull(player, tvLive)) {
            return;
        }
        resize();
        showRecordTimer(mRecording);
        if (landscape) {
            setPlayerMargin(0, 0);
            showCameraNameLand(true);
            tvLive.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            //通过布局inflate的使用下面的方法设置margin
            if (tvLive.getLayoutParams() != null) {
                ((ViewGroup.MarginLayoutParams) tvLive.getLayoutParams()).setMargins(0, (int) getResources().getDimension(R.dimen.dp_22), (int) getResources().getDimension(R.dimen.dp_12), 0);
            }
            displayPortraitPlayer(false);
            ((ViewGroup.MarginLayoutParams) player.getLayoutParams()).setMargins(0, 0, 0, DisplayUtil.dpToPx(NooieApplication.mCtx, 0));
            displayLandPlayer(true);
        } else {
            //setPlayerMargin(DisplayUtil.dpToPx(NooieApplication.mCtx, 21), DisplayUtil.dpToPx(NooieApplication.mCtx, 21));
            setPlayerMargin(0, 0);
            showCameraNameLand(true);
            tvLive.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            if (tvLive.getLayoutParams() != null) {
                ((ViewGroup.MarginLayoutParams) tvLive.getLayoutParams()).setMargins(0, (int) getResources().getDimension(R.dimen.dp_10), (int) getResources().getDimension(R.dimen.dp_12), 0);
            }
            displayLandPlayer(false);
            ((ViewGroup.MarginLayoutParams) player.getLayoutParams()).setMargins(0, 0, 0, DisplayUtil.dpToPx(NooieApplication.mCtx, 40));
            displayPortraitPlayer(true);
        }
    }

    private static final float SCREEN_FACTOR = (float) 16 / 9;
    public void resize() {
        if (checkNull(player)) {
            return;
        }
        try {
            DisplayMetrics dm = DisplayUtil.getDisplayRealMetrics(NooieApplication.mCtx);
            int screenWidth = dm.widthPixels;
            int screenHeigh = dm.heightPixels;
            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                ViewGroup.LayoutParams lp = player.getLayoutParams();
                lp.width = screenWidth;
                lp.height = (int) (screenWidth / SCREEN_FACTOR);
                player.setLayoutParams(lp);
                ((ViewGroup.MarginLayoutParams) player.getLayoutParams()).setMargins(0, 0, 0, 0);
            } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                //ViewGroup.LayoutParams lp = player.getLayoutParams();
                ViewGroup.LayoutParams lp = getScreenLpByFactor(player.getLayoutParams(), screenWidth, screenHeigh);
                //lp.width = screenWidth;
                //lp.height = screenHeigh;
                player.setLayoutParams(lp);
                ((ViewGroup.MarginLayoutParams) player.getLayoutParams()).setMargins(0, 0, 0, 0);
            }
        } catch (Exception e) {
            NooieLog.d("-->> NooiePlayActivity resize fail");
            e.printStackTrace();
        }

    }

    private ViewGroup.LayoutParams getScreenLpByFactor(ViewGroup.LayoutParams lp, int width, int height) {
        if (lp == null || width == 0 || height == 0) {
            return lp;
        }
        float factor = (float) width / height;
        NooieLog.d("-->> NooiePlayActivity getScreenLpByFactor width=" + width + " height=" + height);
        if (factor > SCREEN_FACTOR) {
            width = (int)(height * SCREEN_FACTOR);
        } else {
            height = (int)(width / SCREEN_FACTOR);
        }
        NooieLog.d("-->> NooiePlayActivity getScreenLpByFactor lp width=" + width + " height=" + height);
        lp.width = width;
        lp.height = height;
        return lp;
    }

    private void showCameraNameLand(boolean show) {
        if (checkNull(tvRecordTimeLand, deviceNameTitleLandContainer)) {
            return;
        }
        if (show) {
            if (isLandscape() && !tvRecordTimeLand.isShown()) {
                tvCameraNameLand.setVisibility(View.VISIBLE);
                deviceNameTitleLandContainer.setVisibility(View.VISIBLE);
            }
        } else {
            tvCameraNameLand.setVisibility(View.GONE);
            deviceNameTitleLandContainer.setVisibility(View.GONE);
        }
    }

    private void showRecordTimer(boolean show) {
        if (checkNull(tvRecordTime, tvRecordTimeLand)) {
            return;
        }
        if (show) {
            if (isLandscape()) {
                tvRecordTime.setVisibility(View.GONE);
                tvRecordTimeLand.setVisibility(View.VISIBLE);
            } else {
                tvRecordTime.setVisibility(View.VISIBLE);
                tvRecordTimeLand.setVisibility(View.GONE);
            }
        } else {
            tvRecordTime.setVisibility(View.GONE);
            tvRecordTimeLand.setVisibility(View.GONE);
        }
    }

    private void displayPortraitPlayer(boolean show) {
        if (checkNull(playerMenuBar, containerOperationPortrait, containerDateTimePortrait, containerOtherPlayback)) {
            return;
        }
        hideDeviceListAnim(500, false);
        hideDeviceListTitleView(false);
        int visibility = show ? View.VISIBLE : View.GONE;
        playerMenuBar.setVisibility(visibility);
        containerOperationPortrait.setVisibility(visibility);
        containerDateTimePortrait.setVisibility(visibility);
        containerOtherPlayback.setVisibility(visibility);

        if (show) {
            showPortraitPlayer();
            containerDateTimePortrait.setVisibility(isShowPlaybackView(false) ? View.VISIBLE : View.GONE);
            containerOtherPlayback.setVisibility(isShowPlaybackView(false) ? View.VISIBLE : View.GONE);
        } else {
            hidePortraitPlayer();
        }
    }

    private void showPortraitPlayer() {
        if (mIsLive) {
            showPortraitLive();
        } else {
            showPortraitPlayback();
        }
        btnPlayFullScreenPortrait.setVisibility(View.VISIBLE);
    }

    private void showPortraitLive() {
        containerCtrlPortrait.setVisibility(View.VISIBLE);
        containerPlaybackPortrait.setVisibility(View.GONE);
        ivPlaybackIcon.setVisibility(View.GONE);
        displayPlaybackDetecion();
        displayPlaybackVideoEmptyView();
        ivAudioPlaybackPortrait.setVisibility(View.GONE);

        displayLpDeviceRestartView(true);
    }

    private void showPortraitPlayback() {
        containerCtrlPortrait.setVisibility(View.GONE);
        containerPlaybackPortrait.setVisibility(View.VISIBLE);
        ivPlaybackIcon.setVisibility(View.VISIBLE);
        displayPlaybackDetecion();
        displayPlaybackVideoEmptyView();
        ivAudioPlaybackPortrait.setVisibility(View.VISIBLE);

        displayLpDeviceRestartView(false);
    }

    private void hidePortraitPlayer() {
        ivAudioPlaybackPortrait.setVisibility(View.GONE);
        btnPlayFullScreenPortrait.setVisibility(View.GONE);
    }

    private void displayLandPlayer(boolean show) {
        if (checkNull(vPlayerBg)) {
            return;
        }
        hideDeviceListAnim(500, true);
        hideDeviceListTitleView(true);
        vPlayerBg.setVisibility(show ? View.VISIBLE : View.GONE);
        displayTalkBubble(false);
        if (show) {
            showLandPlayer();
            showActionViewLand(0);
        } else {
            hideActionViewLand(0);
        }
    }

    private void showLandPlayer() {
        if (mIsLive) {
            showLandLive();
        } else {
            boolean isCloud = isCloudState();
            showLandPlayback(isCloud, false);
            delaySyncTimeShaft();
        }
    }

    private void showLandLive() {
        if (isDestroyed() || checkNull(containerPlaybackLand, containerPlaybackController, containerPlaybackSave, ivCloudSaveLand, containerCtrlLand, containerDateTimeLand, ivRecordLand, ivSnapShotLand, ivAudioLand, ivTalkLand, ivAlarmLand, ivFlashLightLand)) {
            return;
        }
        containerPlaybackLand.setVisibility(View.GONE);
        containerPlaybackController.setVisibility(View.GONE);
        containerPlaybackSave.setVisibility(View.GONE);
        ivCloudSaveLand.setVisibility(View.GONE);

        containerCtrlLand.setVisibility(View.VISIBLE);
        containerDateTimeLand.setVisibility(isShowPlaybackView(true) ? View.VISIBLE : View.GONE);
        ivRecordLand.setVisibility(View.VISIBLE);
        ivSnapShotLand.setVisibility(View.VISIBLE);
        ivAudioLand.setVisibility(View.VISIBLE);
        boolean isHideTalkIcon = (mConnectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT && NooieDeviceHelper.mergeIpcType(mDeviceType) == IpcType.MC120) || mDeviceType == IpcType.HC320;
        ivTalkLand.setVisibility(isHideTalkIcon ? View.GONE : View.VISIBLE);
        ivAlarmLand.setVisibility(checkAlarmAudioEnable() ? View.VISIBLE : View.GONE);
        ivFlashLightLand.setVisibility(checkFlashLightEnable() ? View.VISIBLE : View.GONE);

        displayLpDeviceRestartView(true);
    }

    private void showLandPlayback(boolean isCloud, boolean isDoubleClick) {
        if (isDestroyed() || checkNull(containerPlaybackSave, containerDateTimeLand, containerCtrlLand, containerPlaybackLand, containerPlaybackController, ivRecordLand, ivSnapShotLand, ivCloudSaveLand, ivAudioLand, ivTalkLand, ivAlarmLand, ivFlashLightLand, btnPlaybackPre, btnPlaybackNext)) {
            return;
        }
        containerPlaybackSave.setVisibility(View.GONE);
        containerDateTimeLand.setVisibility(View.GONE);

        containerCtrlLand.setVisibility(View.VISIBLE);
        containerPlaybackLand.setVisibility(View.VISIBLE);
        containerPlaybackController.setVisibility(isDoubleClick ? View.VISIBLE : View.GONE);
        ivRecordLand.setVisibility(isCloud ? View.GONE : View.VISIBLE);
        ivSnapShotLand.setVisibility(isCloud ? View.GONE : View.VISIBLE);
        ivCloudSaveLand.setVisibility(isCloud ? View.VISIBLE : View.GONE);
        ivAudioLand.setVisibility(View.VISIBLE);
        ivTalkLand.setVisibility(View.GONE);
        ivAlarmLand.setVisibility(View.GONE);
        ivFlashLightLand.setVisibility(View.GONE);

        btnPlaybackPre.setVisibility(isCloud ? View.VISIBLE : View.GONE);
        btnPlaybackNext.setVisibility(isCloud ? View.VISIBLE : View.GONE);

        displayLpDeviceRestartView(false);
    }

    private void displayCloudSave(boolean show) {
        if (show) {
            containerCtrlLand.setVisibility(View.GONE);
            containerPlaybackSave.setVisibility(View.VISIBLE);
        } else {
            containerPlaybackSave.setVisibility(View.GONE);
            containerCtrlLand.setVisibility(View.VISIBLE);
        }
    }

    private int mDevicePowerMode = ConstantValue.DEVICE_POWER_MODE_NORMAL;
    private void displayLpDeviceRestartView(boolean show) {
        if (checkNull(containerLpDeviceController)) {
            return;
        }

        if (show) {
            containerLpDeviceController.setVisibility(mIsLpDevice && mDevicePowerMode == ConstantValue.DEVICE_POWER_MODE_LP_SLEEP ? View.VISIBLE : View.GONE);
            displayFpsAndBit(!(mIsLpDevice && mDevicePowerMode == ConstantValue.DEVICE_POWER_MODE_LP_SLEEP));
        } else {
            containerLpDeviceController.setVisibility(View.GONE);
            displayFpsAndBit(true);
        }
    }

    private void displayPlaybackDetecion() {
        if (checkNull(ivPlaybackIcon, mPlaybackComponent, timerShaftPortrait)) {
            return;
        }
        swtllPlaybackDetection.setVisibility(View.VISIBLE);
        int displayType = ivPlaybackIcon.getTag() != null && (Integer)ivPlaybackIcon.getTag() == ConstantValue.PLAY_DISPLAY_TYPE_NORMAL ? ConstantValue.PLAY_DISPLAY_TYPE_NORMAL : ConstantValue.PLAY_DISPLAY_TYPE_DETAIL;
        ivPlaybackIcon.setImageResource(displayType == ConstantValue.PLAY_DISPLAY_TYPE_NORMAL ? R.drawable.play_display_normal : R.drawable.play_display_detail);
        mPlaybackComponent.setDetectionDisplayType(displayType);
        timerShaftPortrait.setVisibility(displayType == ConstantValue.PLAY_DISPLAY_TYPE_NORMAL ? View.VISIBLE : View.GONE);
        delaySyncTimeShaft();
    }

    private void delaySyncTimeShaft() {
        Util.delayTask(500, new Util.OnDelayTaskFinishListener() {
            @Override
            public void onFinish() {
                if (isDestroyed() || checkNull(mPlaybackComponent)) {
                    return;
                }
                if (mPlaybackComponent != null) {
                    mPlaybackComponent.syncTimeShaft();
                }
            }
        });
    }

    private void clickScreenOnLand() {
        if (isDestroyed() || checkNull(containerOperationLand)) {
            return;
        }
        if (isLandscape()) {
            boolean isCloud = isCloudState();
            if (mIsLive) {
                showLandLive();
            } else {
                showLandPlayback(isCloud, false);
            }
            changeSystemScreenUi();
            if (containerOperationLand.isShown()) {
                hideActionViewLand(500);
            } else {
                showActionViewLand(600);
            }
        }
    }

    private void doubleClickScreenOnLand() {
        if (isDestroyed() || checkNull(containerOperationLand)) {
            return;
        }
        if (!mIsLive && isLandscape()) {
            boolean isCloud = isCloudState();
            showLandPlayback(isCloud, true);
            changeSystemScreenUi();
            if (containerOperationLand.isShown()) {
                hideActionViewLand(500);
            } else {
                showActionViewLand(600);
            }
        }
    }

    private void startVideo(boolean isStartLpDevice) {
        BindDevice device = getDevice(mConnectionMode, mDeviceId);
        boolean isOnLine = device != null ? (device.getOnline() == ApiConstant.ONLINE_STATUS_ON && device.getOpen_status() == ApiConstant.OPEN_STATUS_ON) : false;
        if (!isOnLine) {
            String file = getDevicePreviewFile(mDeviceId);
            //todo set the preview background for offline
            //player.setBackground(BitmapDrawable.createFromPath(file) != null ? BitmapDrawable.createFromPath(file) : getResources().getDrawable(R.drawable.default_preview));
            return;
        }
        int modelType = NooieDeviceHelper.convertNooieModel(mDeviceType, mModel);
        int streamType = NooieDeviceHelper.convertStreamType(mDeviceType, mModel);
        NooieLog.d("-->> NooiePlayActivity startVideo deviceId=" + mDeviceId + " isSubDevice=" + mIsSubDevice + " isLpDevice=" + mIsLpDevice + " modelType=" + modelType + " streamType=" + streamType);
        if (mIsLpDevice) {
            startLpLive(mDeviceId, mModel, mConnectionMode, modelType, streamType, mIsSubDevice, isStartLpDevice);
        } else {
            startLive(mDeviceId, mModel, mConnectionMode, modelType, streamType);
        }
    }

    private PlayerDelegate mPlayerDelegate;
    private void updatePlayState(int type, int state) {
        if (mPlayerDelegate != null) {
            mPlayerDelegate.setPlayState(type, state, System.currentTimeMillis());
        }
    }

    private void startLpLive(String deviceId, String model, int connectionMode, int modelType, int streamType, boolean isSubDevice, boolean isStartLpDevice) {
        if (!isStartLpDevice) {
            mDevicePowerMode = ConstantValue.DEVICE_POWER_MODE_LP_SLEEP;
            displayLpDeviceRestartView(true);
            return;
        }
        if (player == null) {
            NooieLog.d("-->> NooiePlayActivity startVideo startLpLive deviceId=" + deviceId);
            return;
        }
        mDevicePowerMode = ConstantValue.DEVICE_POWER_MODE_LP_ACTIVE;
        String taskId = UUID.randomUUID().toString();
        NooieLog.d("-->> NooiePlayActivity startVideo LpLive taskId=" + taskId);
        displayLpDeviceRestartView(false);
        mLpCameraTaskId = taskId;
        updatePlayState(PlayState.PLAY_TYPE_LIVE, PlayState.PLAY_STATE_START);
        if (connectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            if (NooieDeviceHelper.mergeIpcType(model) == IpcType.HC320) {
                player.startAPP2PLive(DeviceCmdApi.getInstance().getApDeviceId(deviceId, model), 0, modelType, new OnActionResultListener() {
                    @Override
                    public void onResult(int code) {
                        NooieLog.d("-->> NooiePlayActivity startVideo LpLive MHAPLive onResult deviceId=" + deviceId + " code=" + code + " taskId=" + taskId);
                        onStartLpLive(code, taskId);
                    }
                });
            } else {
                player.startAPLive(DeviceCmdApi.getInstance().getApDeviceId(deviceId, model), 0, modelType, new OnActionResultListener() {
                    @Override
                    public void onResult(int code) {
                        NooieLog.d("-->> NooiePlayActivity startVideo LpLive MHAPLive onResult deviceId=" + deviceId + " code=" + code + " taskId=" + taskId);
                        onStartLpLive(code, taskId);
                    }
                });
            }
        } else {
            if (isSubDevice) {
                player.startMhLive(deviceId, modelType, streamType, new OnActionResultListener() {
                    @Override
                    public void onResult(int code) {
                        NooieLog.d("-->> NooiePlayActivity startVideo LpLive MhLive onResult deviceId=" + deviceId + " code=" + code + " taskId=" + taskId);
                        onStartLpLive(code, taskId);
                        addDeviceConnectionMark(code, mPDeviceId);
                    }
                });
            } else {
                player.startMhLive(deviceId, modelType, streamType, new OnActionResultListener() {
                    @Override
                    public void onResult(int code) {
                        NooieLog.d("-->> NooiePlayActivity startVideo LpLive MhLive onResult deviceId=" + deviceId + " code=" + code + " taskId=" + taskId);
                        onStartLpLive(code, taskId);
                        addDeviceConnectionMark(code, deviceId);
                    }
                });
            }
        }
    }

    private void onStartLpLive(int code, String taskId) {
        NooieLog.d("-->> NooiePlayActivity onStartLpLive deviceId=" + mDeviceId + " code=" + code + " taskId=" + taskId);
        if (code == Constant.OK) {
            startLpCameraTask(taskId);
        } else {
            mDevicePowerMode = ConstantValue.DEVICE_POWER_MODE_NORMAL;
            displayLpDeviceRestartView(false);
            stopLpCameraCameraTask(taskId, false);
        }
        updatePlayState(PlayState.PLAY_TYPE_LIVE, PlayState.PLAY_STATE_FINISH);
    }

    private void startLive(String deviceId, String model, int connectionMode, int modelType, int steamType) {
        if (player == null) {
            NooieLog.d("-->> NooiePlayActivity startVideo startLive deviceId=" + deviceId);
            return;
        }
        updatePlayState(PlayState.PLAY_TYPE_LIVE, PlayState.PLAY_STATE_START);
        if (connectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            player.startAPLive(DeviceCmdApi.getInstance().getApDeviceId(deviceId, model), 0, modelType, new OnActionResultListener() {
                @Override
                public void onResult(int code) {
                    NooieLog.d("-->> NooiePlayActivity startVideo Live APLive onResult deviceId=" + deviceId + " code=" + code);
                    onStartLive(code);
                    addDeviceConnectionMark(code, deviceId);
                }
            });
        } else {
            if (IpcType.PC420F_TYPE.equalsIgnoreCase(model)) {
                player.startMhLive(deviceId, modelType, steamType, new OnActionResultListener() {
                    @Override
                    public void onResult(int code) {
                        NooieLog.d("-->> NooiePlayActivity startVideo Live NooieLive onResult deviceId=" + mDeviceId + " code=" + code);
                        onStartLive(code);
                        addDeviceConnectionMark(code, deviceId);
                    }
                });
                return;
            }
            player.startNooieLive(deviceId, 0, modelType, steamType, new OnActionResultListener() {
                @Override
                public void onResult(int code) {
                    NooieLog.d("-->> NooiePlayActivity startVideo Live NooieLive onResult deviceId=" + mDeviceId + " code=" + code);
                    onStartLive(code);
                    addDeviceConnectionMark(code, deviceId);
                }
            });
        }
    }

    private void onStartLive(int code) {
        updatePlayState(PlayState.PLAY_TYPE_LIVE, PlayState.PLAY_STATE_FINISH);
    }

    private void restartVideo(boolean delayStartVideo) {
        if (mIsLive && mIsLpDevice) {
            resumeData(false, true, delayStartVideo);
        }
    }

    private void restartLpVideo(String model, boolean isSubDevice, int connectionMode) {
        if (NooieDeviceHelper.isSortLinkDevice(model, isSubDevice, connectionMode)) {
            startConnectShortLinkDevice(new ConnectResultListener() {
                @Override
                public void onConnectResult(boolean result, boolean isNewConnect) {
                    if (result) {
                        restartVideo(isNewConnect);
                    }
                }
            });
        } else {
            restartVideo(false);
        }
    }

    private String mConnectShortLinkDeviceTaskId = null;
    private void startConnectShortLinkDevice(ConnectResultListener listener) {
        stopLpDeviceShortLinkTask();
        stopConnectShortLinkDevice();
        if (DeviceConnectionCache.getInstance().isConnectionExist(mDeviceId)) {
            if (listener != null) {
                listener.onConnectResult(true, false);
            }
            return;
        }
        showLoading();
        mConnectShortLinkDeviceTaskId = createSortLinkDeviceTaskId(mUid, mDeviceId);
        DeviceConnectionHelper.getInstance().startConnectShortLinkDevice(mConnectShortLinkDeviceTaskId, mUid, getDevice(mConnectionMode, mDeviceId), new ConnectShortLinkDeviceListener() {
            @Override
            public void onResult(int code, String taskId, String account, String deviceId) {
                if (isDestroyed()) {
                    return;
                }
                if (TextUtils.isEmpty(mConnectShortLinkDeviceTaskId) || TextUtils.isEmpty(mUid) || TextUtils.isEmpty(mDeviceId)) {
                    stopConnectShortLinkDevice();
                    hideLoading();
                    return;
                }
                if (!mConnectShortLinkDeviceTaskId.equals(taskId)) {
                    return;
                }

                boolean isIgnoreCallback = code == DeviceConnectionHelper.CONNECT_SHORT_LINK_DEVICE_RECEIVE_FAIL;
                if (isIgnoreCallback) {
                    return;
                }
                stopConnectShortLinkDevice();
                hideLoading();
                NooieLog.d("-->> debug NooiePlayActivity startConnectShortLinkDevice onResult: code=" + code + " taskId=" + taskId + " account=" + account + " deviceId=" + deviceId);
                boolean isConnectShortLinkDeviceFinish = code == DeviceConnectionHelper.CONNECT_SHORT_LINK_DEVICE_SUCCESS;
                if (isConnectShortLinkDeviceFinish) {
                    if (listener != null) {
                        listener.onConnectResult(true, true);
                    }
                } else if (code == DeviceConnectionHelper.CONNECT_SHORT_LINK_DEVICE_CONNECT_P2P_FAIL) {
                    //ToastUtil.showToast(NooiePlayActivity.this, "P2p connect error, please retry");
                }
            }
        });
    }

    private void stopConnectShortLinkDevice() {
        DeviceConnectionHelper.getInstance().stopConnectShortLinkDevice();
        mConnectShortLinkDeviceTaskId = null;
    }

    private void destroyConnectShortLinkDevice(String model, boolean isSubDevice, int connectionMode) {
        if (!NooieDeviceHelper.isSortLinkDevice(model, isSubDevice, connectionMode) || checkIsShortLinkDeviceFromDeviceMsg()) {
            return;
        }
        stopConnectShortLinkDevice();
        DeviceConnectionCache.getInstance().removeConnection(mDeviceId);
    }

    private void startLoadPlaybackList(boolean cloud, long start, boolean isNeedDelay) {
        if (cloud) {
            startLoadDeviceCloudRecordList(start);
        } else {
            checkBeforeLoadingSDPlaybackList(mModel, mIsSubDevice, mConnectionMode, start, isNeedDelay);
        }
    }

    private void startLoadDeviceCloudRecordList(long start) {
        if (mPlayerPresenter != null) {
            mPlayerPresenter.loadDeviceCloudRecordList(mDeviceId, start, mIsLpDevice, mUserAccount, getBindType(), mPlaybackTaskId);
        }
    }

    private void startLoadDeviceSdCardRecordList(long start, boolean isNewConnect, boolean isShortLinkDevice) {
        if (mPlayerPresenter == null) {
            return;
        }
        /*
        if (!isShortLinkDevice) {
            mPlayerPresenter.loadDeviceSdCardRecordList(mDeviceId, start, mIsLpDevice, mPlaybackTaskId);
            return;
        }
         */
        tryDelayToSendCmd(isNewConnect, isShortLinkDevice, new TaskUtil.OnDelayTimeFinishListener() {
            @Override
            public void onFinish() {
                if (isDestroyed() || mPlayerPresenter == null) {
                    return;
                }
                mPlayerPresenter.checkBeforeLoadDeviceSdCardRecordList(mDeviceId, start, mIsLpDevice, mPlaybackTaskId, isShortLinkDevice);
            }
        });
    }

    private void checkBeforeLoadingSDPlaybackList(String model, boolean isSubDevice, int connectionMode, long start, boolean isNeedDelay) {
        if (NooieDeviceHelper.isSortLinkDevice(model, isSubDevice, connectionMode)) {
            startConnectShortLinkDevice(new ConnectResultListener() {
                @Override
                public void onConnectResult(boolean result, boolean isNewConnect) {
                    startLoadDeviceSdCardRecordList(start, (isNeedDelay || isNewConnect), true);
                }
            });
        } else {
            startLoadDeviceSdCardRecordList(start, isNeedDelay, false);
        }
    }

    private void stopLpDevicePlaybackTask() {
        if (mPlayerPresenter != null) {
            mPlayerPresenter.stopLpCameraPlayBackTask();
        }
    }

    private void stopLpDeviceShortLinkTask() {
        if (mPlayerPresenter != null) {
            mPlayerPresenter.stopLpCameraShortLinkTask();
        }
    }

    private void stopLpVideo() {
        mDevicePowerMode = ConstantValue.DEVICE_POWER_MODE_LP_SLEEP;
        displayLpDeviceRestartView(true);
        displayLiveTag(false);
        resetDefault(true, true);
    }

    private String mLpCameraTaskId;
    private void startLpCameraTask(String taskId) {
        boolean isNeedToStartTask = !TextUtils.isEmpty(mLpCameraTaskId) && (mLpCameraTaskId.equalsIgnoreCase(taskId));
        if (mPlayerPresenter != null) {
            //mLpCameraTaskId = taskId;
            mPlayerPresenter.startLpCameraPlayTask(mDeviceId, Constant.PLAY_TYPE_MH_LIVE);
        }
    }

    private void stopLpCameraCameraTask(String taskId, boolean isForceStop) {
        NooieLog.d("-->> NooiePlayActivity stopLpCameraCameraTask taskId=" + taskId + " mLpCameraTaskId=" + mLpCameraTaskId);
        boolean isNeedToStopTask = isForceStop || TextUtils.isEmpty(mLpCameraTaskId) || (mLpCameraTaskId.equalsIgnoreCase(taskId));
        if (isNeedToStopTask && mPlayerPresenter != null) {
            mPlayerPresenter.stopLpCameraPlayTask();
            mLpCameraTaskId = "";
        }
    }

    private String mPlaybackTaskId;
    private void startPlaybackVideo() {
        BindDevice device = getDevice(mConnectionMode, mDeviceId);
        boolean isOnLine = device != null ? (device.getOnline() == ApiConstant.ONLINE_STATUS_ON && device.getOpen_status() == ApiConstant.OPEN_STATUS_ON) : false;
        if (!isOnLine && !isCloudState()) {
            String file = getDevicePreviewFile(mDeviceId);
            //todo set the preview background for offline
            //player.setBackground(BitmapDrawable.createFromPath(file) != null ? BitmapDrawable.createFromPath(file) : getResources().getDrawable(R.drawable.default_preview));
            return;
        }
        if (checkNull(mPlayerPresenter, ivOtherPlayback, mPlaybackComponent)) {
            return;
        }
        mPlayerPresenter.stopLoadRecordTask();
        boolean cloud = isCloudState();
        mPlaybackComponent.clearRecordList(cloud);
        mPlaybackComponent.setPlaybackType(cloud ? ConstantValue.NOOIE_PLAYBACK_TYPE_CLOUD : ConstantValue.NOOIE_PLAYBACK_TYPE_SD);
        mPlaybackTaskId = UUID.randomUUID().toString();
        startLoadPlaybackList(cloud, (mPlaybackComponent.getTodayStartTime() / 1000L), false);
    }

    private boolean isPlaybackTaskValid(String taskId) {
        boolean isPlaybackTask = !TextUtils.isEmpty(mPlaybackTaskId) && (mPlaybackTaskId.equalsIgnoreCase(taskId));
        return isPlaybackTask;
    }

    private void loadStorageInfo() {
        BindDevice device = getDevice(mConnectionMode, mDeviceId);
        boolean isOnLine = device != null ? (device.getOnline() == ApiConstant.ONLINE_STATUS_ON && device.getOpen_status() == ApiConstant.OPEN_STATUS_ON) : false;
        loadDeviceInfo(isOnLine);
    }

    private void loadDeviceInfo(boolean isOnLine) {
        mIsSubscribeCloud = false;
        if (mConnectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            mPlayerPresenter.getStorageInfoByAp(mUserAccount, mDeviceId, isOnLine, mIsSubDevice);
        } else {
            updateLoadStorageTime(System.currentTimeMillis() + PlayState.CMD_TIMEOUT);
            mPlayerPresenter.getDeviceStorageState(mUserAccount, mDeviceId, isOnLine, mIsSubDevice, mConnectionMode, getBindType(), NooieDeviceHelper.isSortLinkDevice(mModel, mIsSubDevice, mConnectionMode));
        }
    }

    private boolean isPlayStarting() {
        return mPlayerDelegate != null && mPlayerDelegate.isPlayStarting();
    }

    private void addDeviceConnectionMark(int code, String deviceId) {
        if (isDestroyed() || TextUtils.isEmpty(deviceId)) {
            return;
        }
        if (code == Constant.OK) {
            DeviceConnectionCache.getInstance().resetCacheDataMarkNum(deviceId);
        } else {
            DeviceConnectionCache.getInstance().addCacheDataMarkNum(deviceId);
        }
    }

    private static final int MEDIA_OPERATION_MAX_COUNT = 3;
    private int mStartAlarmCount = 0;

    private Subscription mRetryTask = null;
    private void startRetryTask(Observer<Integer> observer) {
        stopRetryTask();
        mRetryTask = Observable.just(mStartAlarmCount)
                .delay(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    private void stopRetryTask() {
        if (mRetryTask != null && !mRetryTask.isUnsubscribed()) {
            mRetryTask.unsubscribe();
            mRetryTask = null;
        }
    }

    private void tryStartAlarmAudio(boolean isRetry) {
        if (!isRetry) {
            mStartAlarmCount = 0;
        }
        startAlarmAudio(mDeviceId, true, 0, 60, 1, isRetry, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                if (code == Constant.OK) {
                } else {
                    stopAlarmAudioMonitor();
                    resetAlarmIcon();
                }
            }
        });
    }

    private void startAlarmAudio(final String deviceId, final boolean on, final int id, final int time, final int num, boolean isRestry, final OnActionResultListener listener) {
        if (mPlayerPresenter != null) {
            if (!isRestry) {
                showLoading();
            }
            mPlayerPresenter.getDeviceAlarmAudio(mDeviceId, new OnSwitchStateListener() {
                @Override
                public void onStateInfo(int code, boolean enable) {
                    if (isDestroyed() || checkNull(mPlayerPresenter)) {
                        return;
                    }
                    NooieLog.d("-->> NooiePlayActivity startAlarmAudio onStateInfo code=" + code + " enable=" + enable);
                    if (code == Constant.OK && enable) {
                        mPlayerPresenter.setDeviceAlarmAudio(deviceId, on, id, time, num, new OnActionResultListener() {
                            @Override
                            public void onResult(int code) {
                                if (isDestroyed()) {
                                    return;
                                }
                                //mStartAlarmCount = 0;
                                hideLoading();
                                if (listener != null) {
                                    listener.onResult(code);
                                }
                            }
                        });
                    } else {
                        if (mStartAlarmCount < MEDIA_OPERATION_MAX_COUNT) {
                            mStartAlarmCount++;
                            startRetryTask(new Observer<Integer>() {
                                @Override
                                public void onCompleted() {
                                }

                                @Override
                                public void onError(Throwable e) {
                                }

                                @Override
                                public void onNext(Integer count) {
                                    if (isDestroyed()) {
                                        return;
                                    }
                                    tryStartAlarmAudio(true);
                                }
                            });
                            return;
                        }
                        //mStartAlarmCount = 0;
                        hideLoading();
                        if (listener != null) {
                            listener.onResult(Constant.ERROR);
                        }
                    }
                }
            });
        }
    }

    public void stopAlarmAudio(String deviceId, OnActionResultListener listener) {
        if (!checkAlarmAudioEnable()) {
            return;
        }
        if (mPlayerPresenter != null) {
            mPlayerPresenter.setDeviceAlarmAudio(deviceId, false, 0, 0, 0, listener);
        }
    }

    private void tryStartTalk(boolean isRetry) {
        if (!isRetry) {
            mStartAlarmCount = 0;
        }

        startTalk(isRetry, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                NooieLog.d("-->> NooiePlayActivity onResult start talk code=" + code);
                if (code == Constant.OK && !player.isWaveout()) {
                    player.setWaveoutState(true);
                    changePhoneVolume(true);
                }

                if (code == Constant.ERROR) {
                    resetTalkIcon();
                }
            }
        });
    }

    private void startTalk(boolean isRetry, final OnActionResultListener listener) {
        if (player != null) {
            if (checkAlarmAudioEnable() && mPlayerPresenter != null) {
                if (!isRetry) {
                    showLoading();
                }
                mPlayerPresenter.getDeviceAlarmAudio(mDeviceId, new OnSwitchStateListener() {
                    @Override
                    public void onStateInfo(int code, boolean enable) {
                        if (isDestroyed() || checkNull(player)) {
                            return;
                        }
                        if (code == Constant.OK && enable) {
                            hideLoading();
                            player.startTalk(null);
                            if (listener != null) {
                                listener.onResult(Constant.OK);
                            }
                        } else {
                            if (mStartAlarmCount < MEDIA_OPERATION_MAX_COUNT) {
                                mStartAlarmCount++;
                                startRetryTask(new Observer<Integer>() {
                                    @Override
                                    public void onCompleted() {
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                    }

                                    @Override
                                    public void onNext(Integer count) {
                                        tryStartTalk(true);
                                    }
                                });
                                return;
                            }
                            hideLoading();
                            if (listener != null){
                                listener.onResult(Constant.ERROR);
                            }
                        }
                    }
                });
                return;
            }
            player.startTalk(null);
            if (listener != null) {
                listener.onResult(Constant.OK);
            }
        }
    }

    /**
     * 记忆用户最后一次喇叭点击状态
     *
     * @param deviceId
     * @param open     当前喇叭的状态,返回点击喇叭后的结果（即与当前状态相反）
     */
    private void saveAudioState(String deviceId, boolean open) {
        GlobalPrefs prefs = GlobalPrefs.getPreferences(NooieApplication.mCtx);
        prefs.saveAudioInfo(deviceId, open ? ConstantValue.AUDIO_STATE_OFF : ConstantValue.AUDIO_STATE_ON);
    }

    private boolean getAudioState(String deviceId) {
        GlobalPrefs prefs = GlobalPrefs.getPreferences(NooieApplication.mCtx);
        return prefs.getAudioInfo(deviceId) == ConstantValue.AUDIO_STATE_ON ? true : false;
    }

    @Override
    public void onVideoStart(NooieMediaPlayer player) {
        if (isDestroyed() && checkNull(player, tvLive)) {
            return;
        }
        NooieLog.d("-->> NooiePlayActivity onVideoStart");
        super.onVideoStart(player);
        displayLiveTag(mIsLive);
        refreshPlayerControl();
        String file = FileUtil.getPreviewThumbSavePath(NooieApplication.mCtx, mDeviceId);
        player.snapShot(file);
        boolean isOpenAudio = getAudioState(mDeviceId);
        player.setWaveoutState(isOpenAudio);
        changePhoneVolume(isOpenAudio);
        EventTrackingApi.getInstance().trackNormalEvent(EventDictionary.EVENT_ID_CAMERA_VIDEO_START);
    }

    private void saveDevicePreviewFile(String deviceId, String file) {
        String account = GlobalData.getInstance().getAccount();
        String key = String.format("%s_%s", GlobalPrefs.KEY_DEVICE_PREVIEW, deviceId);
        // delete old files
        String oldFile = GlobalPrefs.getString(NooieApplication.mCtx, account, key, "");
        FileUtil.deleteFile(oldFile);
        GlobalPrefs.putString(NooieApplication.mCtx, account, key, file);
    }

    @Override
    public void onVideoStop(NooieMediaPlayer player) {
        if (isDestroyed() || checkNull(tvLive)) {
            return;
        }
        NooieLog.d("-->> NooiePlayActivity onVideoStop");
        super.onVideoStop(player);
        displayLiveTag(false);
    }

    @Override
    public void onAudioStart(NooieMediaPlayer player) {
        if (isDestroyed() || checkNull(tivAudio, ivAudioLand, ivAudioPlaybackPortrait)) {
            return;
        }
        NooieLog.d("-->> NooiePlayActivity onAudioStart");
        super.onAudioStart(player);
        tivAudio.setTextIcon(R.drawable.audio_on_icon_state_list);
        ivAudioLand.setImageResource(R.drawable.audio_on_land_icon_state_list);
        ivAudioPlaybackPortrait.setImageResource(R.drawable.audio_on_land_icon_state_list);
    }

    @Override
    public void onAudioStop(NooieMediaPlayer player) {
        if (isDestroyed() || checkNull(tivAudio, ivAudioLand, ivAudioPlaybackPortrait)) {
            return;
        }
        NooieLog.d("-->> NooiePlayActivity onAudioStop");
        super.onAudioStop(player);
        tivAudio.setTextIcon(R.drawable.audio_off_icon_state_list);
        ivAudioLand.setImageResource(R.drawable.audio_off_land_icon_state_list);
        ivAudioPlaybackPortrait.setImageResource(R.drawable.audio_off_land_icon_state_list);
    }

    @Override
    public void onTalkingStart(NooieMediaPlayer player) {
        if (isDestroyed()) {
            return;
        }
        NooieLog.d("-->> NooiePlayActivity onTalkingStart");
        super.onTalkingStart(player);
    }

    @Override
    public void onTalkingStop(NooieMediaPlayer player) {
        if (isDestroyed()) {
            return;
        }
        NooieLog.d("-->> NooiePlayActivity onTalkingStop");
        super.onTalkingStop(player);
    }


    @Override
    public void onRecordStart(NooieMediaPlayer player, boolean result, String file) {
        if (isDestroyed()) {
            return;
        }
        NooieLog.d("-->> NooiePlayActivity onRecordStart file=" + file);
        if (result) {
            if (mSaveRcecordFile != null) {
                mSaveRcecordFile.add(file);
            }
        } else {
        }
    }

    @Override
    public void onRecordStop(NooieMediaPlayer player, boolean result, String file) {
        if (isDestroyed()) {
            return;
        }
        super.onRecordStop(player, result, file);
        NooieLog.d("-->> NooiePlayActivity onRecordStop file=" + file);
        if (result) {
            if (mSaveRcecordFile.contains(file)) {
                mSaveRcecordFile.remove(file);
            }
            showVideoThumbnail(file, true);
            if (mPlayerPresenter != null) {
                mPlayerPresenter.sendRecordEventTracking(file);
            }
            updateFileInMediaStore(mUserAccount, file, MediaStoreUtil.MEDIA_TYPE_VIDEO_MP4);
        } else {
        }
    }

    @Override
    public void onRecordTimer(NooieMediaPlayer player, int duration) {
        int h = duration / (60 * 60);
        int s = duration % (60 * 60);
        int m = s / 60;
        s = s % 60;
        NooieLog.d("-->> NooiePlayActivity onRecordTimer duration=" + String.format("%02d:%02d:%02d", h, m, s));
          updateRecordTimer(duration);
    }

    @Override
    public void onFps(NooieMediaPlayer player, int fps) {
        if (isDestroyed() || checkNull(tvFps) || !BuildConfig.DEBUG) {
            return;
        }
        tvFps.setText(String.format("%dfps", fps));
    }

    @Override
    public void onBitrate(NooieMediaPlayer player, double bitrate) {
        if (isDestroyed() || checkNull(tvBitrate)) {
            return;
        }
        tvBitrate.setText(String.format("%.0fKb/s", bitrate));
    }

    @Override
    public void onBufferingStart(NooieMediaPlayer player) {
        if (isDestroyed()) {
            return;
        }

        NooieLog.d("-->> NooiePlayActivity onBufferingStart");
    }

    @Override
    public void onBufferingStop(NooieMediaPlayer player) {
        if (isDestroyed()) {
            return;
        }

        NooieLog.d("-->> NooiePlayActivity onBufferingStop");
    }

    @Override
    public void onPlayFinish(NooieMediaPlayer player) {
        if (isDestroyed() || checkNull(player)) {
            return;
        }

        NooieLog.d("-->> NooiePlayActivity onPlayFinish");
        stopPlayer();
        player.setPlayerListener(this);
        displayFpsAndBit(true);
        resetPlayerCtrl();
        showCameraNameLand(true);
        if (System.currentTimeMillis() - mLastPlayFileBadTip < 1000) {
            Util.delayTask(1000, new Util.OnDelayTaskFinishListener() {
                @Override
                public void onFinish() {
                    if (isDestroyed()) {
                        return;
                    }
                    ToastUtil.showToast(NooiePlayActivity.this, R.string.nooie_play_video_play_finish);
                }
            });
            return;
        }
        ToastUtil.showToast(this, R.string.nooie_play_video_play_finish);
    }

    @Override
    public void onPlayOneFinish(NooieMediaPlayer player) {
        NooieLog.d("-->> NooiePlayActivity onPlayOneFinish");
    }

    private long mLastPlayFileBadTip = 0;
    @Override
    public void onPlayFileBad(NooieMediaPlayer player) {
        if (isDestroyed()) {
            return;
        }
        mLastPlayFileBadTip = System.currentTimeMillis();
        NooieLog.d("-->> NooiePlayActivity onPlayFileBad");
        ToastUtil.showToast(this, R.string.nooie_play_video_play_bad);
    }

    private void displayLiveTag(boolean show) {
        if (checkNull(tvLive)) {
            return;
        }
        tvLive.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    private void displayFpsAndBit(boolean show) {
        if (checkNull(tvFps, tvBitrate)) {
            return;
        }
        tvFps.setText("");
        tvBitrate.setText("");
        tvFps.setVisibility(View.INVISIBLE);
        tvBitrate.setVisibility(checkIsDisplayFpsAndBit(show) ? View.VISIBLE : View.INVISIBLE);
    }

    private boolean checkIsDisplayFpsAndBit(boolean show) {
        if (mConnectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            return show;
        }
        if (mIsLive && show) {
            BindDevice device = getDevice(mConnectionMode, mDeviceId);
            boolean isOnLine = device != null ? (device.getOnline() == ApiConstant.ONLINE_STATUS_ON && device.getOpen_status() == ApiConstant.OPEN_STATUS_ON) : false;
            show = isOnLine;
        }
        return show;
    }

    @Override
    public void onReceiveDeviceCmdConnect(String deviceId) {
        if (isDestroyed() || TextUtils.isEmpty(deviceId) || !deviceId.equalsIgnoreCase(mDeviceId)) {
            return;
        }
        mIsCheckDisconnect = false;
    }

    private boolean mIsCheckDisconnect = false;
    @Override
    public void onReceiveDeviceCmdDisconnect(String deviceId) {
        if (isDestroyed() || mIsCheckDisconnect || TextUtils.isEmpty(deviceId) || !deviceId.equalsIgnoreCase(mDeviceId)) {
            return;
        }
        mIsCheckDisconnect = true;
        if (BuildConfig.DEBUG) {
            NooieLog.d("-->> debug NooiePlayActivity onReceiveDeviceCmdDisconnect: deviceId=" + deviceId);
            //ToastUtil.showToast(this, "Device p2p disconnected");
        }
    }

    private void displayGestureGuideView(boolean show, int gestureType, IpcType type) {
        if (isDestroyed() || checkNull(ivDirectionControlBg, ivGestureLeftArrow, ivGestureTopArrow, ivGestureRightArrow, ivGestureBottomArrow) || !mIsLive || !NooieDeviceHelper.isSupportPtzControl(type)) {
            return;
        }

        BindDevice device = getDevice(mConnectionMode, mDeviceId);
        boolean isOpen = device != null ? (device.getOnline() == ApiConstant.ONLINE_STATUS_ON && device.getOpen_status() == ApiConstant.OPEN_STATUS_ON) : false;
        boolean isSupportPtzControlVertical = NooieDeviceHelper.isSupportPtzControlVertical(type);
        boolean isSupportPtzControlHorizontal = NooieDeviceHelper.isSupportPtzControlHorizontal(type);
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

    @Override
    public void onLpCameraPlayFinish(String result, String deviceId, int playType) {
        if (isDestroyed()) {
            return;
        }

        if (ConstantValue.SUCCESS.equalsIgnoreCase(result) && mIsLpDevice) {
            boolean isShowLiveTimeDialog = mIsLive && mDevicePowerMode == ConstantValue.DEVICE_POWER_MODE_LP_ACTIVE;
            NooieLog.d("-->> NooiePlayActivity onLpCameraPlayFinish deviceId=" + deviceId + " isLive=" + mIsLive + " powermode=" + mDevicePowerMode);
            if (isOldEcCam()) {
                stopLpVideo();
            }
            if (isShowLiveTimeDialog) {
                showLiveLimitTimeDialog();
            }
        }
    }

    private boolean isOldEcCam() {
        return mDeviceType == IpcType.EC810_CAM && (getDevice(mConnectionMode, mDeviceId) != null && NooieDeviceHelper.compareVersion(getDevice(mConnectionMode, mDeviceId).getVersion(), "1.0.61") <=0);
    }

    @OnClick({R.id.ivTitleRightIcon, R.id.tvTitle, R.id.ivLeft, R.id.ivLeftLand, R.id.ivRight, R.id.tvCameraNameLand, R.id.ivCameraNameLandIcon, R.id.ivCloseIcon, R.id.deviceListContainer, R.id.tivPhoto, R.id.tivRecord, R.id.tivSnapShot, R.id.tivAlarm, R.id.tivTalk, R.id.tivAudio, R.id.tivFlashLight, R.id.ivPlaybackFirst, R.id.ivPlaybackSwitchArrow, R.id.ivPlaybackSecond,
            R.id.ivRecordLand, R.id.ivSnapShotLand, R.id.ivAlarmLand, R.id.ivTalkLand, R.id.ivAudioLand, R.id.ivAudioPlaybackPortrait, R.id.ivFlashLightLand, R.id.ivCloudSaveLand, R.id.ivSeeHistoryBuyCloud, R.id.ivSeeHistoryBuyCloudLand, R.id.ivSwitchCloudSdLand, R.id.ivOtherPlaybackLand,
    R.id.ivPlaybackSaveClose, R.id.btnPlaybackSavePhoto, R.id.btnPlaybackSaveStory, R.id.btnPlayFullScreenPortrait, R.id.btnPlayFullScreen, R.id.btnPlaybackPre, R.id.btnPlaybackNext, R.id.btnPlaybackRestartOrStop, R.id.ivPlaybackIcon, R.id.btnLpDeviceRestart, R.id.tvLpDeviceRestartTip, R.id.bubbleContainer, R.id.tvPlaybackDetectionSettingEnable})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivTitleRightIcon:
            case R.id.tvTitle: {
                toggleDeviceListView();
                break;
            }
            case R.id.ivCameraNameLandIcon:
            case R.id.tvCameraNameLand: {
                toggleDeviceListLandView();
                break;
            }
            case R.id.deviceListContainer:
            case R.id.ivCloseIcon: {
                hideDeviceListAnim(500, true);
                break;
            }
            case R.id.ivLeftLand: {
                gotoLive(false);
                break;
            }
            case R.id.ivLeft: {
                if (isPlayStarting()) {
                    showPlayerIsReleasing(true);
                    break;
                }
                if (mConnectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
                    if (getCurrentIntent() != null && getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_ROUTE_SOURCE, ConstantValue.ROUTE_SOURCE_NORMAL) == ConstantValue.ROUTE_SOURCE_ADD_DEVICE) {
                        HomeActivity.toHomeActivity(NooiePlayActivity.this);
                    }
                    break;
                }
                if (getCurrentIntent() != null && getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_ROUTE_SOURCE, ConstantValue.ROUTE_SOURCE_NORMAL) == ConstantValue.ROUTE_SOURCE_ADD_DEVICE) {
                    HomeActivity.toHomeActivity(this);
                }
                finish();
                break;
            }
            case R.id.ivRight: {
                if (isPlayStarting()) {
                    showPlayerIsReleasing(true);
                    break;
                }
                if (!mIsLive) {
                    gotoLive(false);
                    showLoading();
                    TaskUtil.delayAction(2000, new TaskUtil.OnDelayTimeFinishListener() {
                        @Override
                        public void onFinish() {
                            hideLoading();
                            NooieDeviceSettingActivity.toNooieDeviceSettingActivity(NooiePlayActivity.this, mDeviceId, ConstantValue.CAM_SETTING_TYPE_NORMAL, mIsSubscribeCloud, mIsSubDevice, mPDeviceId, mIsLpDevice, getBindType(), mConnectionMode, mDeviceSsid);
                            mIsLeaveLivePage = true;
                        }
                    });
                    break;
                }
                NooieDeviceSettingActivity.toNooieDeviceSettingActivity(NooiePlayActivity.this, mDeviceId, ConstantValue.CAM_SETTING_TYPE_NORMAL, mIsSubscribeCloud, mIsSubDevice, mPDeviceId, mIsLpDevice, getBindType(), mConnectionMode, mDeviceSsid);
                mIsLeaveLivePage = true;
                break;
            }
            case R.id.tivPhoto: {
                if (isPlayStarting()) {
                    showPlayerIsReleasing(true);
                    break;
                }
                //NooieLog.d("-->> NooiePlayActivity onViewClicked open media store deviceId" + mDeviceId + " isRecording=" + player.isRecording());
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !EasyPermissions.hasPermissions(NooieApplication.mCtx, CommonUtil.getStoragePermGroup())) {
                    requestPermission(CommonUtil.getStoragePermGroup());
                    break;
                }
                Bundle param = new Bundle();
                param.putString(ConstantValue.INTENT_KEY_DEVICE_ID, mDeviceId);
                param.putFloat(ConstantValue.INTENT_KEY_DATA_PARAM_1, CountryUtil.getCurrentTimeZone());
                PhotoMediaActivity.toPhotoMediaActivity(this, param);
                break;
            }
            case R.id.btnPlaybackSaveStory:
            case R.id.tivRecord:
            case R.id.ivRecordLand: {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !EasyPermissions.hasPermissions(NooieApplication.mCtx, CommonUtil.getStoragePermGroup())) {
                    requestPermission(CommonUtil.getStoragePermGroup());
                    break;
                }
                NooieLog.d("-->> NooiePlayActivity onViewClicked recordState deviceId=" + mDeviceId + " isRecording=" + mRecording + " recordState=" + player.isRecording());
                if (checkStartRecordingInvalid(player.isPlayingng(), mRecording)) {
                    return;
                }
                if (mRecording) {
                    tivRecord.setTextIcon(R.drawable.record_icon_state_list);
                    ivRecordLand.setImageResource(R.drawable.record_land_icon_state_list);
                    stopRecordTimer();
                    showCameraNameLand(true);
                    player.stopRecord();
                } else {
                    tivRecord.setTextIcon(R.drawable.recording_icon_state_list);
                    ivRecordLand.setImageResource(R.drawable.recording_land_icon_state_list);
                    showCameraNameLand(false);
                    startRecordTimer();
                    player.startRecord(FileUtil.getNooieSavedRecordPath(NooieApplication.mCtx, mDeviceId, CConstant.MEDIA_TYPE_MP4, mUserAccount));
                }
                break;
            }
            case R.id.btnPlaybackSavePhoto:
            case R.id.tivSnapShot:
            case R.id.ivSnapShotLand: {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !EasyPermissions.hasPermissions(NooieApplication.mCtx, CommonUtil.getStoragePermGroup())) {
                    requestPermission(CommonUtil.getStoragePermGroup());
                    break;
                }
                player.snapShot(FileUtil.getNooieSavedScreenShotPath(NooieApplication.mCtx, mDeviceId, CConstant.MEDIA_TYPE_JPEG, mUserAccount));
                break;
            }
            case R.id.tivAlarm:
            case R.id.ivAlarmLand: {
                if (tivAlarm.getTag() != null && (Integer) tivAlarm.getTag() == ConstantValue.ALARM_AUDIO_STATE_ON) {
                    tivAlarm.setTag(ConstantValue.ALARM_AUDIO_STATE_OFF);
                    tivAlarm.setTextIcon(R.drawable.alarm_off_icon_state_list);
                    ivAlarmLand.setImageResource(R.drawable.alarm_off_land_icon_state_list);
                    stopAlarmAudioMonitor();
                    stopAlarmAudio(mDeviceId, null);
                } else {
                    isOperationForIPC200(view.getId());
                    tivAlarm.setTag(ConstantValue.ALARM_AUDIO_STATE_ON);
                    tivAlarm.setTextIcon(R.drawable.alarm_on_icon_state_list);
                    ivAlarmLand.setImageResource(R.drawable.alarm_on_land_icon_state_list);
                    startAlarmAudioMonitor();
                    tryStartAlarmAudio(false);
                }
                break;
            }
            case R.id.tivTalk:
            case R.id.ivTalkLand: {
                if (!EasyPermissions.hasPermissions(NooieApplication.mCtx, RECORD_AUDIO_PERMS)) {
                    requestPermission(RECORD_AUDIO_PERMS);
                    break;
                }
                if (player.isTalking()) {
                    tivTalk.setTextIcon(R.drawable.talk_off_icon_state_list);
                    ivTalkLand.setImageResource(R.drawable.talk_off_land_icon_state_list);
                    player.stopTalk();
                    if (!getAudioState(mDeviceId) && player.isWaveout()) {
                        player.setWaveoutState(false);
                        changePhoneVolume(false);
                    }
                } else {
                    if (mPlayerPresenter != null) {
                        mPlayerPresenter.queryDeviceTalkGuide(mDeviceId, mUserAccount, mIsOwner);
                    }
                    isOperationForIPC200(view.getId());
                    tivTalk.setTextIcon(R.drawable.talk_on_icon_state_list);
                    ivTalkLand.setImageResource(R.drawable.talk_on_land_icon_state_list);
                    tryStartTalk(false);
                }
                break;
            }
            case R.id.tivAudio:
            case R.id.ivAudioLand:
            case R.id.ivAudioPlaybackPortrait: {
                saveAudioState(mDeviceId, player.isWaveout());
                if (player.isWaveout()) {
                    tivAudio.setTextIcon(R.drawable.audio_off_icon_state_list);
                    ivAudioLand.setImageResource(R.drawable.audio_off_land_icon_state_list);
                    ivAudioPlaybackPortrait.setImageResource(R.drawable.audio_off_land_icon_state_list);
                    player.setWaveoutState(false);
                    changePhoneVolume(false);
                } else {
                    isOperationForIPC200(view.getId());
                    tivAudio.setTextIcon(R.drawable.audio_on_icon_state_list);
                    ivAudioLand.setImageResource(R.drawable.audio_on_land_icon_state_list);
                    ivAudioPlaybackPortrait.setImageResource(R.drawable.audio_on_land_icon_state_list);
                    player.setWaveoutState(true);
                    changePhoneVolume(true);
                }
                break;
            }
            case R.id.ivSeeHistoryBuyCloud:
            case R.id.ivSeeHistoryBuyCloudLand: {
                gotoStorage();
                break;
            }
            case R.id.ivSwitchCloudSdLand: {
                if (mHasSDCard && mOpenCloud && (Integer) ivOtherPlayback.getTag() != SWITCH_CURRENT_IS_CLOUD) {
                    gotoCloudState();
                    switchPlayback();
                } else if (mHasSDCard && !mOpenCloud) {
                    gotoStorage();
                }
                break;
            }
            case R.id.ivOtherPlaybackLand: {
                if (mHasSDCard && mOpenCloud && (Integer) ivOtherPlayback.getTag() != SWITCH_CURRENT_IS_SDCARD) {
                    gotoSDCardState();
                    switchPlayback();
                }
                break;
            }
            case R.id.ivPlaybackFirst:
            case R.id.ivPlaybackSwitchArrow: {
                if (isPlayStarting()) {
                    showPlayerIsReleasing(true);
                    break;
                }
                if (!mIsOwner) {
                    break;
                }
                if (mHasSDCard && mOpenCloud) {
                    if ((Integer) containerOtherPlayback.getTag() == SWITCH_CLOUD_SDCARD_SHOW) {
                        hideOtherPlaybackViewAnim(500);
                    } else {
                        showOtherPlaybackViewAnim();
                    }
                }
                break;
            }
            case R.id.ivPlaybackSecond: {
                if (!mIsOwner) {
                    break;
                }
                if (mHasSDCard && mOpenCloud) {
                    if (isCloudState()) {
                        gotoSDCardState();
                        switchPlayback();
                    } else {
                        gotoCloudState();
                        switchPlayback();
                    }
                    hideOtherPlaybackViewAnim(500);
                }
                break;
            }
            case R.id.ivCloudSaveLand: {
                displayCloudSave(true);
                break;
            }
            case R.id.ivPlaybackSaveClose: {
                displayCloudSave(false);
                break;
            }
            case R.id.btnPlayFullScreenPortrait: {
                //横竖屏显示,配合onConfigurationChanged
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            }
            case R.id.btnPlayFullScreen: {
                //横竖屏显示,配合onConfigurationChanged
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            }
            case R.id.btnPlaybackPre:
                if (mPlaybackComponent != null) {
                    mPlaybackComponent.jumpToAlarm(true);
                }
                break;
            case R.id.btnPlaybackNext:
                if (mPlaybackComponent != null) {
                    mPlaybackComponent.jumpToAlarm(false);
                }
                break;
            case R.id.btnPlaybackRestartOrStop:
                break;
            case R.id.ivPlaybackIcon:
                if (mPlaybackComponent != null && ivPlaybackIcon !=null && ivPlaybackIcon.getTag() != null) {
                    swtllPlaybackDetection.setVisibility(View.VISIBLE);
                    int displayType = (Integer)ivPlaybackIcon.getTag() == ConstantValue.PLAY_DISPLAY_TYPE_NORMAL ? ConstantValue.PLAY_DISPLAY_TYPE_DETAIL : ConstantValue.PLAY_DISPLAY_TYPE_NORMAL;
                    ivPlaybackIcon.setTag(displayType);
                    ivPlaybackIcon.setImageResource(displayType == ConstantValue.PLAY_DISPLAY_TYPE_NORMAL ? R.drawable.play_display_normal : R.drawable.play_display_detail);
                    mPlaybackComponent.setDetectionDisplayType(displayType);
                    timerShaftPortrait.setVisibility(displayType == ConstantValue.PLAY_DISPLAY_TYPE_NORMAL ? View.VISIBLE : View.GONE);
                    delaySyncTimeShaft();
                    displayPlaybackVideoEmptyView();
                }
                break;
            case R.id.btnLpDeviceRestart:
            case R.id.tvLpDeviceRestartTip:
                restartLpVideo(mModel, mIsSubDevice, mConnectionMode);
                break;
            case R.id.bubbleContainer:
                displayTalkBubble(false);
                break;
            case R.id.tivFlashLight:
            case R.id.ivFlashLightLand:
                toggleFlashLight();
                break;
            case R.id.tvPlaybackDetectionSettingEnable :
                openDetectionSetting();
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
    public void onBackPressed() {
        super.onBackPressed();
        if (mConnectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            if (getCurrentIntent() != null && getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_ROUTE_SOURCE, ConstantValue.ROUTE_SOURCE_NORMAL) == ConstantValue.ROUTE_SOURCE_ADD_DEVICE) {
                HomeActivity.toHomeActivity(NooiePlayActivity.this);
                finish();
            }
            return;
        }
        if (getCurrentIntent() != null && getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_ROUTE_SOURCE, ConstantValue.ROUTE_SOURCE_NORMAL) == ConstantValue.ROUTE_SOURCE_ADD_DEVICE) {
            HomeActivity.toHomeActivity(this);
            finish();
        }
    }

    private void showPlayerIsReleasing(boolean isExit) {
        ToastUtil.showToast(this, R.string.play_close_when_loading_tip);
    }

    private boolean checkAlarmAudioEnable() {
        if (mConnectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            return false;
        }
        return NooieDeviceHelper.mergeIpcType(mDeviceType) == IpcType.PC730;
    }

    private boolean isOperationForIPC200(int id) {
        if (!checkAlarmAudioEnable()) {
            return false;
        }

        if (!(id == R.id.tivAlarm || id == R.id.ivAlarmLand)) {
            if (tivAlarm.getTag() != null && (Integer) tivAlarm.getTag() == ConstantValue.ALARM_AUDIO_STATE_ON) {
                tivAlarm.setTag(ConstantValue.ALARM_AUDIO_STATE_OFF);
                //tivAlarm.runEndIconAnim();
                tivAlarm.setTextIcon(R.drawable.alarm_off_icon_state_list);
                ivAlarmLand.setImageResource(R.drawable.alarm_off_land_icon_state_list);
                stopAlarmAudio(mDeviceId, null);
            }
        }

        /*
        if (!(id == R.id.tivTalk || id == R.id.ivTalkLand)) {
            tivTalk.setIvIconOnOrOff(false);
            ivTalkLand.setImageResource(R.drawable.talk_off_land_icon_state_list);
            if (player.isTalking()) {
                player.stopTalk();
            }
        }

        if (!(id == R.id.tivAudio || id == R.id.ivAudioLand || id == R.id.ivAudioPlaybackPortrait)) {
            tivAudio.setTextIcon(R.drawable.audio_off_icon_state_list);
            ivAudioLand.setImageResource(R.drawable.audio_off_land_icon_state_list);
            ivAudioPlaybackPortrait.setImageResource(R.drawable.audio_off_land_icon_state_list);
            if (player.isWaveout()) {
                player.setWaveoutState(false);
            }
        }
         */

        if (!(id == R.id.tivTalk || id == R.id.ivTalkLand || id == R.id.tivAudio || id == R.id.ivAudioLand || id == R.id.ivAudioPlaybackPortrait)) {
            tivTalk.setTextIcon(R.drawable.talk_off_icon_state_list);
            ivTalkLand.setImageResource(R.drawable.talk_off_land_icon_state_list);
            if (player.isTalking()) {
                player.stopTalk();
            }

            tivAudio.setTextIcon(R.drawable.audio_off_icon_state_list);
            ivAudioLand.setImageResource(R.drawable.audio_off_land_icon_state_list);
            ivAudioPlaybackPortrait.setImageResource(R.drawable.audio_off_land_icon_state_list);
            if (player.isWaveout()) {
                player.setWaveoutState(false);
                changePhoneVolume(false);
            }
        }

        return true;
    }

    public Subscription mAlarmAudioMonitorTask = null;

    public void startAlarmAudioMonitor() {
        stopAlarmAudioMonitor();
        mAlarmAudioMonitorTask = Observable.just(System.currentTimeMillis())
                .delay(60, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Long time) {
                        resetAlarmIcon();
                    }
                });
    }

    public void stopAlarmAudioMonitor() {
        if (mAlarmAudioMonitorTask != null && !mAlarmAudioMonitorTask.isUnsubscribed()) {
            mAlarmAudioMonitorTask.unsubscribe();
            mAlarmAudioMonitorTask = null;
        }
    }

    public void resetAlarmIcon() {
        if (checkNull(tivAlarm, ivAlarmLand)) {
            return;
        }

        tivAlarm.setTag(ConstantValue.ALARM_AUDIO_STATE_OFF);
        tivAlarm.setTextIcon(R.drawable.alarm_off_icon_state_list);
        ivAlarmLand.setImageResource(R.drawable.alarm_off_land_icon_state_list);
    }

    public void resetTalkIcon() {
        if (checkNull(tivTalk, ivTalkLand)) {
            return;
        }

        tivTalk.setTextIcon(R.drawable.talk_off_icon_state_list);
        ivTalkLand.setImageResource(R.drawable.talk_off_land_icon_state_list);
    }

    private void checkUpdateInfo() {
        BindDevice device = getDevice(mConnectionMode, mDeviceId);
        boolean isOnLine = device != null ? (device.getOnline() == ApiConstant.ONLINE_STATUS_ON && device.getOpen_status() == ApiConstant.OPEN_STATUS_ON) : false;
        boolean isCheckUpdateInfo = isFirstLaunch() && mIsOwner && isOnLine;
        if (!isCheckUpdateInfo) {
            return;
        }
        String model = device != null ? device.getType() : "";
        if (!TextUtils.isEmpty(mDeviceId) && !TextUtils.isEmpty(model)) {
            mPlayerPresenter.loadFirmwareVersion(mDeviceId, model);
        }
    }

    @Override
    public void setPresenter(@NonNull PlayContract.Presenter presenter) {
        mPlayerPresenter = presenter;
    }

    @Override
    public void onLoadFirmwareInfoSuccess(AppVersionResult result) {
        if (isDestroyed() || result == null) {
            return;
        }
        boolean isNewVersion = !TextUtils.isEmpty(result.getCurrentVersionCode()) && !TextUtils.isEmpty(result.getVersion_code()) && Util.convertDeviceVersion(result.getCurrentVersionCode()) < Util.convertDeviceVersion(result.getVersion_code()) ? true : false;
        if (isNewVersion) {
            String versionKey = mDeviceId + "_" + GlobalPrefs.KEY_UPGRADE_TIP_VERSION;
            final String switchOffKey = mDeviceId + "_" + GlobalPrefs.KEY_UPGRADE_TIP_OFF;
            boolean show = !GlobalPrefs.getBoolean(NooieApplication.mCtx, mUserAccount, switchOffKey, false);
            if (!show) {
                return;
            }
            showUpdateDialog(result.getVersion_code(), TextUtils.isEmpty(result.getLog()) ? "" : result.getLog(), switchOffKey);
        }
    }

    private void showUpdateDialog(String versionCode, String log, String switchOffKey) {
        hideUpdateDialog();
        mShowUpdateDialog = DialogUtils.showUpdatesDialog(this, versionCode, log, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                if (isPlayStarting()) {
                    showPlayerIsReleasing(true);
                    return;
                }
                if (mConnectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
                    Bundle param = new Bundle();
                    param.putString(ConstantValue.INTENT_KEY_DEVICE_ID, mDeviceId);
                    param.putString(ConstantValue.INTENT_KEY_IPC_MODEL, mModel);
                    param.putInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, mConnectionMode);
                    OtaUpdateTipActivity.toOtaUpdateTipActivity(NooiePlayActivity.this, param);
                    return;
                }
                NooieDeviceInfoActivity.toNooieDeviceInfoActivity(NooiePlayActivity.this, mDeviceId, ConstantValue.CAM_INFO_TYPE_DIRECT, mIsSubDevice, mIsLpDevice, mConnectionMode, mModel);
            }

            @Override
            public void onClickLeft() {
            }
        }, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                GlobalPrefs.putBoolean(NooieApplication.mCtx, mUserAccount, switchOffKey, (isChecked ? true : false));
            }
        });
    }

    private void hideUpdateDialog() {
        if (mShowUpdateDialog != null) {
            mShowUpdateDialog.dismiss();
            mShowUpdateDialog = null;
        }
    }

    @Override
    public void onLoadFirmwareInfoFailed(String msg) {
        if (isDestroyed()) {
            return;
        }
    }

    @Override
    public void onLoadSDCardRecentDaySuccess(int[] recentDays) {
        if (isDestroyed() || mSDCardDataList == null && dateSelectViewPortrait == null) {
            return;
        }
        if (recentDays != null && recentDays.length > 0 && recentDays.length == mSDCardDataList.size()) {
            int recentDayLen = recentDays.length;
            for (int i = 0; i < mSDCardDataList.size(); i++) {
                //recentDays返回的是倒序的日期
                int recentDayIndex = (recentDayLen - 1) - i;
                if (recentDayLen > i && recentDayIndex >= 0) {
                    int recentDayState = recentDays[recentDayIndex];
                    mSDCardDataList.get(i).setHaveSDCardRecord(recentDayState != 0);
                } else {
                    mSDCardDataList.get(i).setHaveSDCardRecord(false);
                }
            }
            updateCurrentItem(!isCloudState());
        } else if (recentDays != null) {
            for (int i = 0; i < mSDCardDataList.size(); i++) {
                mSDCardDataList.get(i).setHaveSDCardRecord(false);
            }
            updateCurrentItem(!isCloudState());
        }
    }

    @Override
    public void onRequestShortLinkDeviceFormatInfo(String user, String deviceId, final boolean isOpenCloud, int status, boolean isSubDevice, boolean isShortLinkDevice) {
        if (NooieDeviceHelper.isSortLinkDevice(mModel, mIsSubDevice, mConnectionMode)) {
            startConnectShortLinkDevice(new ConnectResultListener() {
                @Override
                public void onConnectResult(boolean result, boolean isNewConnect) {
                    if (isDestroyed() || mPlayerPresenter == null) {
                        return;
                    }
                    if (result) {
                        //mPlayerPresenter.startLpCameraShortLinkTask(deviceId);
                        tryDelayToSendCmd(isNewConnect, true, new TaskUtil.OnDelayTimeFinishListener() {
                            @Override
                            public void onFinish() {
                                if (isDestroyed() || mPlayerPresenter == null) {
                                    return;
                                }
                                mPlayerPresenter.getDeviceFormatInfo(user, deviceId, isOpenCloud, status, isSubDevice, isShortLinkDevice);
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    public void onLoadPackInfoSuccess(PackInfoResult result) {
        if (isDestroyed() || checkNull(dateSelectViewPortrait, dateSelectViewLand)) {
            return;
        }

        if (result != null) {
            List<CalenderBean> cloudDataList = getUtcRecentDays(result.getFile_time(), ConstantValue.CLOUD_PACK_DEFAULT_DAY_NUM);
            if (CollectionUtil.isNotEmpty(cloudDataList)  && mCloudDataList != null) {
                mCloudDataList.clear();
                mCloudDataList.addAll(cloudDataList);
                updateCurrentItem(isCloudState());
            }
        }
        mIsSubscribeCloud = result != null && NooieCloudHelper.isSubscribeCloud(result.getStatus());
    }

    @Override
    public void notifyGetDeviceStorageState(boolean isOpenCloud, boolean isHasSDCard, int status) {
        if (isDestroyed()) {
            return;
        }
        mOpenCloud = isOpenCloud;
        mHasSDCard = isHasSDCard;
        mIsSubscribeCloud = NooieCloudHelper.isSubscribeCloud(status);
        DeviceConfigureEntity deviceConfigureEntity = DeviceConfigureCache.getInstance().getDeviceConfigure(mDeviceId);
        mIsEventCloud = deviceConfigureEntity != null && NooieCloudHelper.isEventCloud(deviceConfigureEntity.getIsEvent());
        updatePlayBackType(isOpenCloud, isHasSDCard);
    }

    @Override
    public void onLoadStorageResult(long time) {
        updateLoadStorageTime(time);
        NooieLog.d("-->> debug NooiePlayActivity onLoadStorageResult:  time=" + time + " isLoadStorageFinish=" + isLoadStorageFinish());
    }

    public void updateLoadStorageTime(long time) {
        if (mPlayerDelegate != null) {
            mPlayerDelegate.setLoadStorageTime(time);
        }
    }

    private boolean isLoadStorageFinish() {
        return mPlayerDelegate == null || mPlayerDelegate.isLoadStorageFinish();
    }

    private boolean isShowDateAndPlaybackView(boolean isLandscape) {
        //需要支持分享设备云回放打开
        if (mConnectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            boolean isShowCloudDateView = mIsOwner && isLandscape && mHasSDCard;
            return isShowCloudDateView;
        } else {
            boolean isShowCloudDateView = mIsOwner ? (isLandscape || mOpenCloud || mHasSDCard) : mOpenCloud;
            return isShowCloudDateView;
        }
    }

    private boolean isShowPlaybackView(boolean isLandscape) {
        //需要支持分享设备云回放打开
        boolean isShowCloudDateView = mIsOwner ? (isLandscape || mOpenCloud || mHasSDCard) : mOpenCloud;
        return isShowCloudDateView;
    }

    private void updatePlayBackType(boolean isOpenCloud, boolean isHasSDCard) {
        /*
        if (isDestroyed() || !mIsOwner || checkNull(containerCloudTipPortrait, tvSeeHistoryLand, ivSeeHistoryBuyCloudLand, ivSwitchCloudSd, containerOtherPlayback, dateSelectViewPortrait, ivSwitchCloudSdLand, containerOtherPlaybackLand, dateSelectViewLand, ivNextPlayback, ivNextPlaybackLand)) {
            return;
        }
        */
        if (isDestroyed() || checkNull(containerCloudTipPortrait, tvSeeHistoryLand, ivSeeHistoryBuyCloudLand, ivSwitchCloudSd, containerOtherPlayback, dateSelectViewPortrait, ivSwitchCloudSdLand, containerOtherPlaybackLand, dateSelectViewLand, ivNextPlayback, ivNextPlaybackLand)) {
            return;
        }
        if (!mIsOwner) {
            //需要支持分享设备云回放打开
            updatePlaybackViewForShare(isOpenCloud);
            return;
        }
        containerDateTimePortrait.setVisibility((isOpenCloud || isHasSDCard) ? View.VISIBLE : View.GONE);
        containerOtherPlayback.setVisibility((isOpenCloud || isHasSDCard) ? View.VISIBLE : View.GONE);
        if (isOpenCloud || isHasSDCard) {
            containerCloudTipPortrait.setVisibility(View.GONE);
            tvSeeHistoryLand.setVisibility(View.GONE);
            ivSeeHistoryBuyCloudLand.setVisibility(View.GONE);

            ivSwitchCloudSd.setVisibility(View.VISIBLE);
            //containerOtherPlayback.setVisibility(View.VISIBLE);
            ivNextPlayback.setVisibility(View.GONE);
            ivSwitchCloudSdLand.setVisibility(View.VISIBLE);
            ivOtherPlaybackLand.setVisibility(View.VISIBLE);
            containerOtherPlaybackLand.setVisibility(View.VISIBLE);
            dateSelectViewLand.setVisibility(View.VISIBLE);

            if (isOpenCloud && isHasSDCard) {
                ivNextPlayback.setVisibility(View.VISIBLE);
            } else if (isOpenCloud) {
                ivOtherPlaybackLand.setVisibility(View.GONE);
                containerOtherPlaybackLand.setVisibility(View.GONE);
            } else {
                ivNextPlaybackLand.setVisibility(View.GONE);
                containerCloudTipPortrait.setVisibility(View.VISIBLE);
            }

            if (isOpenCloud) {
                gotoCloudState();
            } else if (isHasSDCard) {
                gotoSDCardState();
            }
        } else {
            containerCloudTipPortrait.setVisibility(View.VISIBLE);
            tvSeeHistoryLand.setVisibility(View.VISIBLE);
            ivSeeHistoryBuyCloudLand.setVisibility(View.VISIBLE);

            ivSwitchCloudSd.setVisibility(View.GONE);
            containerOtherPlayback.setVisibility(View.GONE);
            ivNextPlayback.setVisibility(View.GONE);
            ivSwitchCloudSdLand.setVisibility(View.GONE);
            ivOtherPlaybackLand.setVisibility(View.GONE);
            containerOtherPlaybackLand.setVisibility(View.GONE);
            dateSelectViewLand.setVisibility(View.GONE);
        }

        if (mConnectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            containerCloudTipPortrait.setVisibility(View.GONE);
            tvSeeHistoryLand.setVisibility(View.GONE);
            ivSeeHistoryBuyCloudLand.setVisibility(View.GONE);
            ivSwitchCloudSdLand.setVisibility(View.GONE);
        }
    }

    private void updatePlaybackViewForShare(boolean isOpenCloud) {
        if (isDestroyed() || checkNull(containerCloudTipPortrait, tvSeeHistoryLand, ivSeeHistoryBuyCloudLand, ivSwitchCloudSd, containerOtherPlayback, dateSelectViewPortrait, ivSwitchCloudSdLand, containerOtherPlaybackLand, dateSelectViewLand, ivNextPlayback, ivNextPlaybackLand)) {
            return;
        }
        containerDateTimePortrait.setVisibility(isOpenCloud ? View.VISIBLE : View.GONE);
        containerOtherPlayback.setVisibility(isOpenCloud ? View.VISIBLE : View.GONE);
        if (isOpenCloud) {
            containerCloudTipPortrait.setVisibility(View.GONE);
            tvSeeHistoryLand.setVisibility(View.GONE);
            ivSeeHistoryBuyCloudLand.setVisibility(View.GONE);

            ivSwitchCloudSd.setVisibility(View.VISIBLE);
            containerOtherPlayback.setVisibility(View.VISIBLE);
            ivNextPlayback.setVisibility(View.GONE);
            ivSwitchCloudSdLand.setVisibility(View.VISIBLE);
            ivOtherPlaybackLand.setVisibility(View.GONE);
            containerOtherPlaybackLand.setVisibility(View.GONE);
            dateSelectViewLand.setVisibility(View.VISIBLE);
            gotoCloudState();
        } else {
            containerCloudTipPortrait.setVisibility(View.GONE);
            tvSeeHistoryLand.setVisibility(View.GONE);
            ivSeeHistoryBuyCloudLand.setVisibility(View.GONE);

            ivSwitchCloudSd.setVisibility(View.GONE);
            containerOtherPlayback.setVisibility(View.GONE);
            ivNextPlayback.setVisibility(View.GONE);
            ivSwitchCloudSdLand.setVisibility(View.GONE);
            ivOtherPlaybackLand.setVisibility(View.GONE);
            containerOtherPlaybackLand.setVisibility(View.GONE);
            dateSelectViewLand.setVisibility(View.GONE);
        }
    }

    private void updateCurrentItem(boolean isUpdate) {
        if (isUpdate) {
            dateSelectViewPortrait.setCurrentItem(mIsLive, mPlaybackSelectedDay);
            dateSelectViewPortrait.scrollToCurrent();
            dateSelectViewLand.setCurrentItem(mIsLive, mPlaybackSelectedDay);
            dateSelectViewLand.scrollToCurrent();
        }
        scrollLiveItem();
    }

    private void scrollLiveItem() {
        if (isDestroyed() || checkNull(dateSelectViewPortrait, dateSelectViewLand) || !mIsLive) {
            return;
        }
        dateSelectViewPortrait.scrollToLive();
        dateSelectViewLand.scrollToLive();
    }

    private boolean isCloudState() {
        return ivOtherPlayback != null && ivOtherPlayback.getTag() != null && (Integer) ivOtherPlayback.getTag() == SWITCH_CURRENT_IS_CLOUD;
    }

    private void gotoCloudState() {
        if (isDestroyed() || ivSwitchCloudSd == null || ivOtherPlayback == null || ivOtherPlaybackLand == null || ivSwitchCloudSd == null || ivSwitchCloudSdLand == null || dateSelectViewPortrait == null || dateSelectViewLand == null) {
            return;
        }
        if (isCloudState()) {
            //return;
        }

        ivSwitchCloudSd.setImageResource(R.drawable.cloud_button_state_list);
        ivOtherPlayback.setImageResource(R.drawable.sdcard_button_state_list);
        ivOtherPlayback.setTag(SWITCH_CURRENT_IS_CLOUD);
        ivOtherPlaybackLand.setImageResource(R.drawable.sd_gray_land);
        ivSwitchCloudSdLand.setImageResource(R.drawable.cloud_white_land);

        dateSelectViewPortrait.setData(mCloudDataList);
        dateSelectViewPortrait.scrollToCurrent();
        dateSelectViewLand.setData(mCloudDataList);
        dateSelectViewLand.scrollToCurrent();
    }

    private void gotoSDCardState() {
        if (isDestroyed() || ivSwitchCloudSd == null || ivOtherPlayback == null || ivOtherPlaybackLand == null || ivSwitchCloudSd == null || ivSwitchCloudSdLand == null || dateSelectViewPortrait == null || dateSelectViewLand == null) {
            return;
        }
        if ((Integer) ivOtherPlayback.getTag() == SWITCH_CURRENT_IS_SDCARD) {
            //return;
        }

        ivOtherPlaybackLand.setImageResource(R.drawable.sd_white_land);
        if (mHasSDCard && mOpenCloud) {
            ivSwitchCloudSdLand.setImageResource(R.drawable.cloud_gray_land);
        } else if (mHasSDCard) {
            ivSwitchCloudSdLand.setImageResource(R.drawable.buy_cloud_state_list);
        }
        ivSwitchCloudSd.setImageResource(R.drawable.sdcard_button_state_list);
        ivOtherPlayback.setImageResource(R.drawable.cloud_button_state_list);
        ivOtherPlayback.setTag(SWITCH_CURRENT_IS_SDCARD);

        dateSelectViewPortrait.setData(mSDCardDataList);
        dateSelectViewPortrait.scrollToCurrent();
        dateSelectViewLand.setData(mSDCardDataList);
        dateSelectViewLand.scrollToCurrent();
    }

    private void gotoStorage() {
        if (mIsSubscribeCloud) {
            showCloudSubscribeTipDialog();
        } else {
            NooieStorageActivity.toCloudBuyPage(NooiePlayActivity.this, mDeviceId, NooieCloudHelper.createEnterMark(mUid), FirebaseConstant.EVENT_CLOUD_ORIGIN_FROM_LIVE);
        }
    }

    @Override
    public void onLoadDeviceSdCardRecordSuccess(List<CloudRecordInfo> cloudRecordInfos, String taskId) {
        NooieLog.d("-->> NooiePlayActivity onLoadDeviceSdCardRecordSuccess isPause=" + isPause() + " isDestroy=" + isDestroyed());
        displayLoading(false);
        if (isDestroyed() || checkNull(mPlaybackComponent) || mIsLive || !isPlaybackTaskValid(taskId)) {
            return;
        }
        if (ivPlaybackIcon != null) {
            ivPlaybackIcon.setTag(ConstantValue.PLAY_DISPLAY_TYPE_NORMAL);
            displayPlaybackDetecion();
        }
        if (mPlaybackComponent != null) {
            mPlaybackComponent.showRecordList(cloudRecordInfos, isPause(), new OnStartPlaybackListener() {
                @Override
                public void onPreStartPlayback(String deviceId, boolean isCloud, boolean isExist) {
                    if (isDestroyed() || isPause()) {
                        return;
                    }
                    /*
                    boolean isStartLpDevicePlaybackTask = !TextUtils.isEmpty(deviceId) && deviceId.equalsIgnoreCase(mDeviceId) && !isCloud && isExist
                            && NooieDeviceHelper.isSortLinkDevice(mModel, mIsSubDevice, mConnectionMode) && !mIsLive;
                    if (isStartLpDevicePlaybackTask && mPlayerPresenter != null) {
                        mPlayerPresenter.startLpCameraPlayBackTask(mDeviceId, Constant.PLAY_TYPE_MH_PLAYBACK_SDCARD);
                    }

                     */
                }
            });
        }
        displayPlaybackVideoEmptyView();
    }

    @Override
    public void onLoadDeviceSdCardRecordFailed(String msg) {
    }

    @Override
    public void onLoadDeviceCloudRecordSuccess(String taskId, List<CloudRecordInfo> cloudRecordInfos, List<RecordFragment> recordFragments, String fileType, int expireDate, String picType, String filePrefix) {
        NooieLog.d("-->> NooiePlayActivity onLoadDeviceCloudRecordSuccess isPause=" + isPause() + " isDestroy=" + isDestroyed());
        displayLoading(false);
        if (isDestroyed() || checkNull(mPlaybackComponent) || mIsLive || !isPlaybackTaskValid(taskId)) {
            return;
        }
        if (ivPlaybackIcon != null) {
            int showType = !(mIsSubDevice || mIsLpDevice || mIsEventCloud) ? ConstantValue.PLAY_DISPLAY_TYPE_NORMAL : ConstantValue.PLAY_DISPLAY_TYPE_DETAIL;
            ivPlaybackIcon.setTag(showType);
            displayPlaybackDetecion();
        }
        if (mPlaybackComponent != null) {
            mPlaybackComponent.showCloudRecordList(cloudRecordInfos, recordFragments, fileType, expireDate, picType, filePrefix, isPause());
        }
        displayPlaybackVideoEmptyView();
    }

    @Override
    public void onLoadDeviceCloudRecordFailed(String msg) {
    }

    @Override
    public void onLoadDetections(String result, int requestType, int page, long timeStamp, long seekTime, String fileType, int expiration, String filePrefix, int bindType) {
        if (isDestroyed()) {
            return;
        }
        String sort = false ? "ASC" : "DESC";
        if (ConstantValue.CLOUD_RECORD_REQUEST_NORMAL == requestType && mPlayerPresenter != null) {
            mPlayerPresenter.loadDeviceMsgByTime(mUserAccount, mUid, mDeviceId, seekTime, ApiConstant.DEVICE_MSG_DIRECTION_FORWARD, (timeStamp + DateTimeUtil.DAY_SECOND_COUNT * 1000L), 0, 10, sort, fileType, expiration, filePrefix, bindType);
        } else if (ConstantValue.CLOUD_RECORD_REQUEST_MORE== requestType && mPlayerPresenter != null) {
            mPlayerPresenter.loadMoreDeviceMsgByTime(mUserAccount, mUid, page, mDeviceId, seekTime, ApiConstant.DEVICE_MSG_DIRECTION_FORWARD, (timeStamp + DateTimeUtil.DAY_SECOND_COUNT * 1000L), 0, 10, sort, fileType, expiration, filePrefix, bindType);
        }
    }

    @Override
    public void onLoadDeviceMsgResult(String result, int requestType, String account, String uid, String deviceId, List<CloudFileBean> cloudFileBeans, String fileType, int expiration, String filePrefix) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            List<Bundle> cloudFiles = new ArrayList<>();
            for (CloudFileBean cloudFileBean : CollectionUtil.safeFor(cloudFileBeans)) {
                if (cloudFileBean != null) {
                    Bundle cloudFile = new Bundle();
                    long startTime = (cloudFileBean.getBaseTime() / 1000L) + cloudFileBean.getStartTime();
                    cloudFile.putLong(CloudHelper.KEY_CLOUD_FILE_START_TIME, startTime);
                    cloudFile.putString(CloudHelper.KEY_CLOUD_FILE_SAVE_FILE_NAME, FileUtil.getDetectionThumbnailFilename(startTime));
                    cloudFile.putString(CloudHelper.KEY_CLOUD_FILE_PRE_SIGN_URL, cloudFileBean.getPreSignUrl());
                    cloudFiles.add(cloudFile);
                }
            }
            if (mPlaybackComponent != null) {
                mPlaybackComponent.setDetectionsData(requestType, cloudFileBeans);
            }
            CloudManager.getInstance().downloadCloudFile(CloudHelper.createDownloadCloudFileParams(mUserAccount, mDeviceId, mUid, cloudFiles, fileType, expiration, ApiConstant.BIND_TYPE_OWNER, filePrefix, FileUtil.getDetectionThumbnailPathInPrivate(NooieApplication.mCtx, mUserAccount, mDeviceId).getPath()));
        }
    }

    private void updateDetectionThumbnail() {
        if (isDestroyed() ||  checkNull(mPlaybackComponent)) {
            return;
        }
        if (mPlaybackComponent != null) {
            mPlaybackComponent.refreshPlaybackDetections();
        }
    }

    private DetectionThumbnailCacheListener mThumbnailCacheListener;
    private void setupDetectionThumbnailListener() {
        mThumbnailCacheListener = new DetectionThumbnailCacheListener() {

            @Override
            public void onUpdateCache() {
                updateDetectionThumbnail();
            }
        };
        registerDetectionThumbnailListener(mThumbnailCacheListener);
    }

    private void registerDetectionThumbnailListener(DetectionThumbnailCacheListener listener) {
        DetectionThumbnailCache.getInstance().addListener(listener);
    }

    public void unregisterDetectionThumbnailListener(DetectionThumbnailCacheListener listener) {
        DetectionThumbnailCache.getInstance().removeListener(listener);
    }

    @Override
    public void onQueryDeviceUpdateState(int type) {
        if (isDestroyed()) {
            return;
        }
        hideUpdateDialog();
        if (type == ApiConstant.DEVICE_UPDATE_TYPE_NORMAL || type == ApiConstant.DEVICE_UPDATE_TYPE_UPATE_FINISH) {
            checkDeviceGuide();
        } else {
            setOrientationType(ORIENTATION_TYPE_NONE);
            showUpdatingWainingDialog();
        }
    }

    @Override
    public void showNoRecording(boolean isEmpty, long time) {
        if (isEmpty) {
            ToastUtil.showToast(this, String.format(getResources().getString(R.string.seek_no_record), ""));
        } else {
            showNoRecording(time);
        }
    }

    private void showNoRecording(long time) {
        ToastUtil.showToast(this, String.format(getResources().getString(R.string.seek_no_record), DateTimeUtil.getUtcTimeString(time, DateTimeUtil.PATTERN_YMD_HMS_1)));
    }

    private Dialog mShowUpdatingWarningDialog;
    private void showUpdatingWainingDialog() {
        hideUpdatingWarningDialog();
        mShowUpdatingWarningDialog = DialogUtils.showInformationNormalDialog(this, getResources().getString(R.string.camera_settings_updating), getResources().getString(R.string.camera_settings_updating_tips), false, false, new DialogUtils.OnClickInformationDialogLisenter() {
            @Override
            public void onConfirmClick() {
                finish();
            }
        });
    }

    private void hideUpdatingWarningDialog() {
        if (mShowUpdatingWarningDialog !=null) {
            mShowUpdatingWarningDialog.dismiss();
            mShowUpdatingWarningDialog = null;
        }
    }

    public void checkDeviceGuide() {
        /*
        if (NooieDeviceHelper.mergeIpcType(mDeviceType) != IpcType.PC530) {
            checkUpdateInfo();
            return;
        }
         */

        if (mPlayerPresenter != null) {
            //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            setOrientationType(ORIENTATION_TYPE_PORTRAIT);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            mPlayerPresenter.queryDeviceGuide(mDeviceId, mUserAccount, mIsOwner);
        } else {
            setOrientationType(ORIENTATION_TYPE_NONE);
        }
    }

    @Override
    public void onQueryDeviceGuide(String result, boolean isShowGuide) {
        if (isDestroyed()) {
            return;
        }
        if (ConstantValue.SUCCESS.equals(result)) {
            if (isShowGuide) {
                if (NooieDeviceHelper.mergeIpcType(mDeviceType) == IpcType.PC530) {
                    TaskUtil.delayAction(2 * 1000, new TaskUtil.OnDelayTimeFinishListener() {
                        @Override
                        public void onFinish() {
                            showPanelGuideView();
                        }
                    });
                } else {
                    setOrientationType(ORIENTATION_TYPE_NONE);
                }
            } else {
                //横竖屏显示,配合onConfigurationChanged
                //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                setOrientationType(ORIENTATION_TYPE_NONE);
                checkUpdateInfo();
            }
            /*
            if (isShowGuide && (getCurrentIntent() != null && getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_ROUTE_SOURCE, ConstantValue.ROUTE_SOURCE_NORMAL) == ConstantValue.ROUTE_SOURCE_ADD_DEVICE)) {
                showPanelGuideView();
            } else {
                //横竖屏显示,配合onConfigurationChanged
                //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                checkUpdateInfo();
            }
             */
        } else {
            //横竖屏显示,配合onConfigurationChanged
            //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            setOrientationType(ORIENTATION_TYPE_NONE);
            checkUpdateInfo();
        }
    }

    @Override
    public void onQueryDeviceTalkGuide(String result, boolean isShowGuide) {
        if (isDestroyed()) {
            return;
        }
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            if (isShowGuide) {
                showTalkGuideView();
            }
        }
    }

    @Override
    public void onShowTalkBubble(String result) {
        if (isDestroyed()) {
            return;
        }
        displayTalkBubble(false);
    }

    @Override
    public void onGetFlashLight(int state, boolean on) {
        if (isDestroyed()) {
            return;
        }
        if (state == SDKConstant.SUCCESS) {
            boolean isFlashLightOn = tivFlashLight != null && tivFlashLight.getTag() != null && (Integer) tivFlashLight.getTag() == ConstantValue.STATE_ON;
        }
    }

    @Override
    public void onSetFlashLight(int state) {
        if (isDestroyed()) {
            return;
        }
        hideLoading();
    }

    private Guide mPanelGuide;
    private void showPanelGuideView() {
        if (isDestroyed()) {
            return;
        }
        setOrientationType(ORIENTATION_TYPE_PORTRAIT);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        PanelComponent panelComponent = new PanelComponent(DisplayUtil.SCREEN_WIDTH_PX, new PanelComponent.PanelComponentListener() {
            @Override
            public void onNextClick() {
                if (mPanelGuide != null) {
                    mPanelGuide.dismiss();
                }
            }
        });
        mPanelGuide = showCustomGuideView(player, null, new GuideBuilder.OnVisibilityChangedListener() {
            @Override
            public void onShown() {
                showDeviceGuideAnim();
            }

            @Override
            public void onDismiss() {
                hideDeviceGuideAnim();
                setOrientationType(ORIENTATION_TYPE_NONE);
            }
        }, panelComponent, NooiePlayActivity.this);
    }

    private void hidePanelGuideView() {
        hideDeviceGuideAnim();
        if (mPanelGuide != null) {
            mPanelGuide.dismiss();
        }
    }

    private void showTalkGuideView() {
        if (isDestroyed()) {
            return;
        }
        if (mPlayerPresenter != null) {
            displayTalkBubble(true);
            mPlayerPresenter.startTalkBubbleTask();
        }
    }

    public Guide showCustomGuideView(View targetView, GuideBuilder guideBuilder, GuideBuilder.OnVisibilityChangedListener listener, Component component, Activity containerActivity) {
        if (guideBuilder == null) {
            guideBuilder = new GuideBuilder();
            guideBuilder = guideBuilder.setTargetView(targetView)
                    .setAlpha(100)
                    .setHighTargetCorner(1)
                    .setHighTargetPadding(0)
                    .setOverlayTarget(false)
                    .setAutoDismiss(false)
                    .setOutsideTouchable(false);
        }
        guideBuilder.setOnVisibilityChangedListener(listener);
        guideBuilder.addComponent(component);
        Guide guide = guideBuilder.createGuide();
        guide.setShouldCheckLocInWindow(false);
        guide.show(containerActivity);
        return guide;
    }

    private void displayTalkBubble(boolean show) {
        if (isDestroyed() || checkNull(bubbleContainer)) {
            return;
        }
        bubbleContainer.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private GifDrawable mDeviceGuideAnim;
    private void showDeviceGuideAnim() {
        if (checkNull(ivPanelComponent, tvPanelTip)) {
            return;
        }
        try {
            ivPanelComponent.setVisibility(View.VISIBLE);
            tvPanelTip.setVisibility(View.VISIBLE);
            int guideAnimRes = NooieDeviceHelper.isSupportPtzControlVertical(mDeviceType) ? R.raw.device_guide_ipc_100 : R.raw.device_guide_ipc_100_horizontal;
            mDeviceGuideAnim = new GifDrawable(getResources(), guideAnimRes);
            mDeviceGuideAnim.setLoopCount(1);
            mDeviceGuideAnim.setSpeed(1.0f);
            mDeviceGuideAnim.addAnimationListener(new AnimationListener() {
                @Override
                public void onAnimationCompleted(int loopNumber) {
                }
            });
            ivPanelComponent.setImageDrawable(mDeviceGuideAnim);
            mDeviceGuideAnim.start();
        } catch (Exception e) {
            ivPanelComponent.setVisibility(View.GONE);
            tvPanelTip.setVisibility(View.GONE);
        }
    }

    private void hideDeviceGuideAnim() {
        if (checkNull(ivPanelComponent, tvPanelTip)) {
            return;
        }
        try {
            if (mDeviceGuideAnim != null) {
                mDeviceGuideAnim.stop();
                ivPanelComponent.setVisibility(View.GONE);
                tvPanelTip.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            ivPanelComponent.setVisibility(View.GONE);
            tvPanelTip.setVisibility(View.GONE);
        }
    }

    private void releaseGuideView() {
        if (mPanelGuide != null) {
            mPanelGuide.dismiss();
            mPanelGuide = null;
        }
        if (mDeviceGuideAnim != null) {
            mDeviceGuideAnim.recycle();
            mDeviceGuideAnim = null;
        }
        ivPanelComponent = null;
        tvPanelTip = null;
    }

    private void checkDeviceConfigure() {
        if (mPlayerPresenter != null) {
            mPlayerPresenter.checkDeviceConfigure(mUserAccount, mDeviceId);
        }
    }

    @Override
    public void displayLoading(boolean show) {
        if (isDestroyed()) {
            return;
        }

        if (show) {
            //showLoading();
            showNormalLoading(false);
        } else {
            hideLoading();
        }
    }

    @Override
    public void onLpDeviceCountDownTask(int state, int type, String deviceId) {
        if (isDestroyed()) {
            return;
        }
        if (state != SDKConstant.SUCCESS) {
            return;
        }
        if (type == ConstantValue.LP_DEVICE_COUNTDOWN_TYPE_PLAYBACK) {
            boolean isShowPlaybackLimitDialog = !TextUtils.isEmpty(deviceId) && deviceId.equalsIgnoreCase(mDeviceId)
                    && NooieDeviceHelper.isSortLinkDevice(mModel, mIsSubDevice, mConnectionMode) && !mIsLive;
            if (isShowPlaybackLimitDialog) {
                showPlaybackLimitTimeDialog();
            }
        } else if (type == ConstantValue.LP_DEVICE_COUNTDOWN_TYPE_SHORT_LINK) {
            boolean isNeedToStopLpDeviceShortLink = !TextUtils.isEmpty(deviceId) && deviceId.equalsIgnoreCase(mDeviceId)
                    && NooieDeviceHelper.isSortLinkDevice(mModel, mIsSubDevice, mConnectionMode);
            if (isNeedToStopLpDeviceShortLink) {
                destroyConnectShortLinkDevice(mModel, mIsSubDevice, mConnectionMode);
            }
        }
    }

    @Override
    public void onGetApDeviceUpgradeInfo(int state, DeviceHardVersionEntity result, String version) {
        if (isDestroyed()) {
            return;
        }
        if (mConnectionMode != ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            return;
        }
        if (state == SDKConstant.SUCCESS) {
            boolean isDeviceHardVersionInvalid = TextUtils.isEmpty(version) || result == null || TextUtils.isEmpty(result.getVersionCode());
            if (isDeviceHardVersionInvalid) {
                return;
            }
            final String switchOffKey = mDeviceId + "_" + GlobalPrefs.KEY_UPGRADE_TIP_OFF;
            boolean ignoreShow = GlobalPrefs.getBoolean(NooieApplication.mCtx, mUserAccount, switchOffKey, false);
            if (ignoreShow) {
                return;
            }
            boolean isShowUpgrade = NooieDeviceHelper.compareVersion(result.getVersionCode(), version) > 0;
            if (isShowUpgrade) {
                showUpdateDialog(result.getVersionCode(), result.getLog(), switchOffKey);
            }
        }
    }

    @Override
    public String getCurDeviceId() {
        if (mConnectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            return null;
        }
        return mDeviceId;
    }

    @Override
    public void updateFileInMediaStore(String account, String path, String mediaType) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return;
        }
        if (mPlayerPresenter != null) {
            mPlayerPresenter.updateFileToMediaStore(account, path, mediaType);
        }
    }

    private void showOtherPlaybackViewAnim() {
        if (isDestroyed() || checkNull(ivOtherPlayback, containerOtherPlayback, ivNextPlayback)) {
            return;
        }
        if ((Integer) containerOtherPlayback.getTag() == SWITCH_CLOUD_SDCARD_SHOW) {
            return;
        }

        mHandler.removeCallbacks(autoHideOtherPlaybackViewAnimWorker);

        ivOtherPlayback.setVisibility(View.VISIBLE);
        // portrait
        ObjectAnimator translationXAnimator = ObjectAnimator.ofFloat(ivOtherPlayback, "translationY", 0f, DisplayUtil.dpToPx(NooieApplication.mCtx, 30));
        translationXAnimator.setDuration(600);
        translationXAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (checkNull(containerOtherPlayback, mHandler, autoHideOtherPlaybackViewAnimWorker)) {
                    return;
                }
                containerOtherPlayback.setTag(SWITCH_CLOUD_SDCARD_SHOW);
                mHandler.postDelayed(autoHideOtherPlaybackViewAnimWorker, SWITCH_WIDGET_SHOW_LONG);
            }
        });
        translationXAnimator.start();

        ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(ivNextPlayback, "rotation", 0f, 180f);
        rotationAnimator.setDuration(600);
        rotationAnimator.start();

        // land
        /*
        int w = ivSwitchCloudSdLand.getWidth();
        float x = containerOtherPlaybackLand.getX();
        ObjectAnimator translationXAnimatorLand = ObjectAnimator.ofFloat(containerOtherPlaybackLand, "translationX",
                0, 0 - ivSwitchCloudSdLand.getWidth());
        translationXAnimatorLand.setDuration(600);
        translationXAnimatorLand.start();

        ObjectAnimator rotationAnimatorLand = ObjectAnimator.ofFloat(ivNextPlaybackLand, "rotation", 180f, 0f);
        rotationAnimatorLand.setDuration(600);
        rotationAnimatorLand.start();
        */
    }

    @Override
    protected void hideOtherPlaybackViewAnim(int duration) {
        super.hideOtherPlaybackViewAnim(duration);

        if (isDestroyed() || containerOtherPlayback == null || ivNextPlayback == null) {
            return;
        }

        if ((Integer) containerOtherPlayback.getTag() == SWITCH_CLOUD_SDCARD_HIDE) {
            return;
        }

        mHandler.removeCallbacks(autoHideOtherPlaybackViewAnimWorker);

        // portrait
        ObjectAnimator translationXAnimator = ObjectAnimator.ofFloat(ivOtherPlayback, "translationY", DisplayUtil.dpToPx(NooieApplication.mCtx, 30), 0f);
        translationXAnimator.setDuration(duration);
        translationXAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (containerOtherPlayback != null) {
                    containerOtherPlayback.setTag(SWITCH_CLOUD_SDCARD_HIDE);
                }
            }
        });
        translationXAnimator.start();

        ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(ivNextPlayback, "rotation", 180f, 0f);
        rotationAnimator.setDuration(duration);
        rotationAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (ivOtherPlayback != null) {
                    ivOtherPlayback.setVisibility(View.GONE);
                }
            }
        });
        rotationAnimator.start();

        // land
        /*
        ObjectAnimator translationXAnimatorLand = ObjectAnimator.ofFloat(containerOtherPlaybackLand, "translationX",
                0 - ivSwitchCloudSdLand.getWidth(), 0);
        translationXAnimatorLand.setDuration(duration);
        translationXAnimatorLand.start();

        ObjectAnimator rotationAnimatorLand = ObjectAnimator.ofFloat(ivNextPlaybackLand, "rotation", 0f, 180f);
        rotationAnimatorLand.setDuration(duration);
        rotationAnimatorLand.start();
        */
    }

    private void showActionViewLand(int duration) {

        if (isDestroyed() || checkNull(tvCameraNameLand, dateSelectViewLand, containerOperationLand)) {
            return;
        }

        mHandler.removeCallbacks(autoHideActionViewLandWorker);
        DialogUtils.showWithPositionAnim(containerOperationLand, DialogUtils.ShowFromType.RIGHT, duration, new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //tvCameraNameLand.setEnabled(true);
                if (checkNull(mHandler, autoHideActionViewLandWorker, dateSelectViewLand)) {
                    return;
                }
                mHandler.postDelayed(autoHideActionViewLandWorker, ACTION_LAND_WIDGET_SHOW_LONG);
                //动画结束，界面显示后才能滑动
                dateSelectViewLand.scrollToCurrent();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        displayCtrlLandBottom();
        if (isShowDateAndPlaybackView(true)) {
            DialogUtils.showWithPositionAnim(containerDateAndPlaybackLand, DialogUtils.ShowFromType.BOTTOM, duration);
        } else if (containerDateAndPlaybackLand.isShown()) {
            DialogUtils.hideWithPositionAnim(containerDateAndPlaybackLand, DialogUtils.ShowFromType.BOTTOM, 0);
        }
    }

    @Override
    protected void hideActionViewLand(int duration) {
        super.hideActionViewLand(duration);

        if (isDestroyed() || checkNull(tvCameraNameLand, containerOperationLand, containerDateAndPlaybackLand, containerPlaybackController)) {
            return;
        }

        mHandler.removeCallbacks(autoHideActionViewLandWorker);
        //tvCameraNameLand.setEnabled(false);
        hideDeviceListAnim(0, true);
        displayCtrlLandBottom();
        DialogUtils.hideWithPositionAnim(containerOperationLand, DialogUtils.ShowFromType.RIGHT, duration);
        containerPlaybackController.setVisibility(View.GONE);
        if (isShowDateAndPlaybackView(true)) {
            DialogUtils.hideWithPositionAnim(containerDateAndPlaybackLand, DialogUtils.ShowFromType.BOTTOM, duration);
        } else if (containerDateAndPlaybackLand.isShown()) {
            DialogUtils.hideWithPositionAnim(containerDateAndPlaybackLand, DialogUtils.ShowFromType.BOTTOM, 0);
        }
    }

    public void displayCtrlLandBottom() {
    }

    private void showDeviceListAnim(boolean land) {
        if (isDestroyed() || deviceListContainer == null || rvDeviceList == null) {
            return;
        }
        if (land) {
            if (deviceListContainer.isShown()) {
                return;
            }
            showCameraNameLand(false);
            DialogUtils.showWithPositionAnim(deviceListContainer, DialogUtils.ShowFromType.TOP, 600);
        } else {
            if (rvDeviceList.isShown()) {
                return;
            }

            DialogUtils.showWithPositionAnim(rvDeviceList, DialogUtils.ShowFromType.TOP, 600);
        }
    }

    private void hideDeviceListAnim(int duration, boolean land) {
        if (isDestroyed() || deviceListContainer == null || rvDeviceList == null) {
            return;
        }
        if (land) {
            if (!deviceListContainer.isShown()) {
                return;
            }
            showCameraNameLand(true);
            DialogUtils.hideWithPositionAnim(deviceListContainer, DialogUtils.ShowFromType.TOP, duration);
        } else {
            if (!rvDeviceList.isShown()) {
                return;
            }
            DialogUtils.hideWithPositionAnim(rvDeviceList, DialogUtils.ShowFromType.TOP, duration);
        }
    }

    private void showLiveLimitTimeDialog() {
        showVideoLimitTimeDialog(new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                if (isOldEcCam()) {
                    restartVideo(false);
                }
            }

            @Override
            public void onClickLeft() {
                stopLpVideo();
                checkIsReturnHomePageForLpDeviceStopping();
            }
        });
    }

    private void showPlaybackLimitTimeDialog() {
        hideLiveLimitTimeDialog();
        showVideoLimitTimeDialog(new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickLeft() {
            }

            @Override
            public void onClickRight() {
            }
        });
    }

    private void showVideoLimitTimeDialog(DialogUtils.OnClickConfirmButtonListener listener) {
        hideLiveLimitTimeDialog();
        mLiveLimitTimeDialog = DialogUtils.showConfirmWithSubMsgDialog(this, R.string.nooie_play_limit_time_title, R.string.nooie_play_limit_time_content, R.string.cancel, R.string.confirm_upper, listener);
    }

    private void hideLiveLimitTimeDialog() {
        if (mLiveLimitTimeDialog != null) {
            mLiveLimitTimeDialog.dismiss();
        }
    }

    private void showCloudSubscribeTipDialog() {
        hideCloudSubscribeTipDialog();
        mCloudSubscribeTipDialog = DialogUtils.showInformationNormalDialog(this, "", getString(R.string.nooie_play_cloud_subscribe_wait_for_preview), false, new DialogUtils.OnClickInformationDialogLisenter() {
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

    private void checkIsReturnHomePageForLpDeviceStopping() {
        if (mConnectionMode != ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            return;
        }
        redirectGotoHomePage();
    }

    public boolean checkIsAddDeviceApHelperListener() {
        return true;
    }

    private boolean checkFlashLightEnable() {
        return NooieDeviceHelper.isSupportFlashLight(mModel);
    }

    private void toggleFlashLight() {
        if (!checkFlashLightEnable() || checkNull(tivFlashLight, ivFlashLightLand)) {
            return;
        }
        boolean isFlashLightOn = tivFlashLight != null && tivFlashLight.getTag() != null && (Integer)tivFlashLight.getTag() == ConstantValue.STATE_ON;
        if (isFlashLightOn) {
            tivFlashLight.setTextIcon(R.drawable.flash_light_off_icon_state_list);
            ivFlashLightLand.setImageResource(R.drawable.flash_light_off_land_icon_state_list);
        } else {
            tivFlashLight.setTextIcon(R.drawable.flash_light_on_icon_state_list);
            ivFlashLightLand.setImageResource(R.drawable.flash_light_on_land_icon_state_list);
            sendOpenFlashLightEvent();
        }
        openFlashLight(!isFlashLightOn, true);
    }

    private void openFlashLight(boolean on, boolean showLoading) {
        if (tivFlashLight != null) {
            tivFlashLight.setTag(on ? ConstantValue.STATE_ON : ConstantValue.STATE_OFF);
        }
        if (!checkFlashLightEnable()) {
            return;
        }
        if (mPlayerPresenter != null) {
            if (showLoading) {
                showLoading();
            }
            mPlayerPresenter.setFlashLight(mDeviceId, on);
        }
    }

    private void tryDelayToSendCmd(boolean isNewConnect, boolean isShortLinkDevice, TaskUtil.OnDelayTimeFinishListener listener) {
        if (!isNewConnect) {
            if (listener != null) {
                listener.onFinish();
            }
            return;
        }
        int delayTime = isShortLinkDevice ? 1200 : 200;
        TaskUtil.delayAction(delayTime, listener);
    }

    private void displayPlaybackVideoEmptyView() {
        if (checkNull(swtllPlaybackDetection, timerShaftPortrait, vPlaybackVideoEmpty)) {
            return;
        }
        boolean isShowPlaybackVideoEmptyView = CollectionUtil.isEmpty(timerShaftPortrait.recordList);
        if (isShowPlaybackVideoEmptyView) {
            swtllPlaybackDetection.setVisibility(View.GONE);
            timerShaftPortrait.setVisibility(View.GONE);
            vPlaybackVideoEmpty.setVisibility(View.VISIBLE);
        } else {
            vPlaybackVideoEmpty.setVisibility(View.GONE);
        }
        displayDetectionSetting(isShowPlaybackVideoEmptyView);
    }

    /**
     * 提示开启motion/PIR条件
     * 设备在线、主人设备、非直连
     * 用户进入了云视频的回放+处于today+当前无文件；
     * 且低功耗相机PIR没开/常电设备Motion开关没开。
     */
    private void displayDetectionSetting(boolean isPlaybackListEmpty) {
        if (checkNull(vPlaybackDetectionSettingBg, tvPlaybackDetectionSettingTip, tvPlaybackDetectionSettingEnable)) {
            return;
        }
        BindDevice device = getDevice(mConnectionMode, mDeviceId);
        boolean isDetectionSettingDisable = mPlayerPresenter != null && !mPlayerPresenter.getIsDeviceDetectionOn();
        boolean isMyDevice = device != null ? device.getBind_type() == ApiConstant.BIND_TYPE_OWNER : false;
        boolean isOnline = device != null ? device.getOnline() == ApiConstant.ONLINE_STATUS_ON : false;
        boolean isSelectToday = DateTimeUtil.isTodayUtc(mPlaybackSelectedDay);
        boolean isShowDetectionSetting = isMyDevice && isOnline && mConnectionMode != ConstantValue.CONNECTION_MODE_AP_DIRECT
                && !mIsLive && mOpenCloud && isCloudState() && isDetectionSettingDisable && isSelectToday && isPlaybackListEmpty;
        vPlaybackDetectionSettingBg.setVisibility(isShowDetectionSetting ? View.VISIBLE : View.GONE);
        tvPlaybackDetectionSettingTip.setVisibility(isShowDetectionSetting ? View.VISIBLE : View.GONE);
        tvPlaybackDetectionSettingEnable.setVisibility(isShowDetectionSetting ? View.VISIBLE : View.GONE);
    }

    private void openDetectionSetting() {
        if (mIsLpDevice) {
            Bundle param = new Bundle();
            param.putString(ConstantValue.INTENT_KEY_DEVICE_ID, mDeviceId);
            param.putInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, mConnectionMode);
            DevicePIRActivity.toDevicePIRActivity(this, param);
        } else {
            NooieDetectionActivity.toNooieDetectionActivity(this, mDeviceId, ConstantValue.NOOIE_DETECT_TYPE_MOTION, true);
        }
    }

    @Override
    public String getEventId(int trackType) {
        int connectionMode = getIntent() != null ? getIntent().getIntExtra(ConstantValue.INTENT_KEY_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_QC) : ConstantValue.CONNECTION_MODE_QC;
        int playbackType = getIntent() != null ? getIntent().getIntExtra(ConstantValue.INTENT_KEY_DATA_TYPE, ConstantValue.NOOIE_PLAYBACK_TYPE_LIVE) : ConstantValue.NOOIE_PLAYBACK_TYPE_LIVE;
        if (playbackType == ConstantValue.NOOIE_PLAYBACK_TYPE_LIVE) {
            if (connectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
                return EventDictionary.EVENT_ID_ACCESS_AP_LIVE;
            } else {
                return EventDictionary.EVENT_ID_ACCESS_CAMERA_LIVE;
            }
        }
        return null;
    }

    @Override
    public int getTrackType() {
        return EventDictionary.EVENT_TRACK_TYPE_START;
    }

    /**
     * 返回页面埋点的页码,空值使用默认
     * @return
     */
    @Override
    public String getPageId() {
        int connectionMode = getIntent() != null ? getIntent().getIntExtra(ConstantValue.INTENT_KEY_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_QC) : ConstantValue.CONNECTION_MODE_QC;
        int playbackType = getIntent() != null ? getIntent().getIntExtra(ConstantValue.INTENT_KEY_DATA_TYPE, ConstantValue.NOOIE_PLAYBACK_TYPE_LIVE) : ConstantValue.NOOIE_PLAYBACK_TYPE_LIVE;
        if (playbackType == ConstantValue.NOOIE_PLAYBACK_TYPE_LIVE) {
            if (connectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
                return EventDictionary.EVENT_PAGE_AP_DIRECT_PLAYER;
            } else {
                return EventDictionary.EVENT_PAGE_P2P_PLAYER;
            }
        }
        return null;
    }

    private void sendPlaybackEvent() {
        if (isDestroyed() || mPlaybackComponent == null) {
            return;
        }
        try {
            ArrayMap<String, Object> playbackEvent = mPlaybackComponent.getPlaybackEvent();
            if (playbackEvent != null && playbackEvent.containsKey(EventDictionary.EXTERNAL_KEY_END_TIME) && playbackEvent.get(EventDictionary.EXTERNAL_KEY_END_TIME) != null && (Long)playbackEvent.get(EventDictionary.EXTERNAL_KEY_END_TIME) == 0L) {
                playbackEvent.put(EventDictionary.EXTERNAL_KEY_END_TIME, (System.currentTimeMillis() / 1000L));
                //NooieLog.d("-->> debug NooiePlayActivity sendPlaybackEvent: playbackEvent=" + GsonHelper.convertToJson(playbackEvent));
                EventTrackingApi.getInstance().trackNormalEvent(EventDictionary.EVENT_ID_PLAY_RECORD_VIDEO, GsonHelper.convertToJson(playbackEvent));
                mPlaybackComponent.clearPlaybackEvent();
            }
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
    }

    private void sendOpenFlashLightEvent() {
        try {
            ArrayMap<String, Object> externalMap = new ArrayMap<>();
            externalMap.put("deviceModel", mModel);
            EventTrackingApi.getInstance().trackNormalEvent(EventDictionary.EVENT_ID_160, "", 0, GsonHelper.convertToJson(externalMap), mDeviceId);
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
    }

    private boolean checkIsShortLinkDeviceFromDeviceMsg() {
        boolean isShortLinkDeviceFromDeviceMsg = getCurrentIntent() != null && getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_START, ConstantValue.NOOIE_PLAYBACK_SOURCE_TYPE_NORMAL) == ConstantValue.NOOIE_PLAYBACK_SOURCE_TYPE_DIRECT
                && mDeviceId != null && mDeviceId.equalsIgnoreCase(getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID)) && NooieDeviceHelper.isSortLinkDevice(mModel, mIsSubDevice, mConnectionMode);
        return isShortLinkDeviceFromDeviceMsg;
    }

    private interface ConnectResultListener {
        void onConnectResult(boolean result, boolean isNewConnect);
    }
}



