package com.afar.osaio.smart.setting.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.ArrayMap;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.afar.osaio.bean.CurrentDeviceParam;
import com.afar.osaio.bean.ShortLinkDeviceParam;
import com.afar.osaio.smart.device.helper.CopyWritingHelper;
import com.afar.osaio.smart.hybrid.webview.HybridWebViewActivity;
import com.afar.osaio.util.CompatUtil;
import com.afar.osaio.widget.LabelSwTagItemView;
import com.afar.osaio.widget.LabelTextItemView;
import com.afar.osaio.widget.MediaPopupWindows;
import com.afar.osaio.widget.YRTextIconView;
import com.nooie.common.utils.configure.FontUtil;
import com.nooie.common.utils.json.GsonHelper;
import com.nooie.common.utils.notify.NotificationUtil;
import com.nooie.data.EventDictionary;
import com.nooie.eventtracking.EventTrackingApi;
import com.nooie.sdk.base.AppStateManager;
import com.nooie.sdk.base.Constant;
import com.nooie.sdk.bean.DeviceComplexSetting;
import com.nooie.sdk.bean.IpcType;
import com.afar.osaio.smart.scan.helper.ApHelper;
import com.afar.osaio.widget.LabelActionItemView;
import com.afar.osaio.widget.LabelSwItemView;
import com.nooie.common.base.GlobalData;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.configure.CountryUtil;
import com.nooie.common.utils.data.DataHelper;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.time.DateTimeUtil;
import com.nooie.sdk.api.network.base.bean.entity.PackInfoResult;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.device.bean.DevAllSettingsV2;
import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.bean.DeviceSettingBean;
import com.afar.osaio.smart.device.bean.DeviceInfo;
import com.nooie.common.utils.tool.TaskUtil;
import com.afar.osaio.smart.device.activity.NooieShareDeviceActivity;
import com.afar.osaio.smart.device.helper.NooieCloudHelper;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.home.activity.HomeActivity;
import com.afar.osaio.smart.setting.presenter.INooieDeviceSettingPresenter;
import com.afar.osaio.smart.setting.presenter.NooieDeviceSettingPresenter;
import com.afar.osaio.smart.setting.view.INooieDeviceSettingView;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.widget.FButton;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.device.bean.DevInfo;
import com.nooie.sdk.device.bean.FormatInfo;
import com.nooie.sdk.device.bean.PirStateV2;
import com.nooie.sdk.device.bean.NooieMediaMode;
import com.nooie.sdk.device.bean.hub.CameraInfo;
import com.suke.widget.SwitchButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * NooieDeviceSettingActivity
 *
 * @author Administrator
 * @date 2019/4/17
 */
public class NooieDeviceSettingActivity extends BaseActivity implements INooieDeviceSettingView {

    private static final int THIRD_PARTY_ALEXA = 1;
    private static final int THIRD_PARTY_GOOGLE_ASSISTANT = 2;

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.switchStatusLight)
    SwitchButton switchStatusLight;
    @BindView(R.id.containerStatusLight)
    View containerStatusLight;
    @BindView(R.id.containerVideoAndAudio)
    View containerVideoAndAudio;
    @BindView(R.id.containerHomeAway)
    View containerHomeAway;
    @BindView(R.id.containerMotionDetection)
    View containerMotionDetection;
    @BindView(R.id.containerSoundDetection)
    View containerSoundDetection;
    @BindView(R.id.containerStorage)
    View containerStorage;
    @BindView(R.id.containerShare)
    View containerShare;
    @BindView(R.id.containerSyncTime)
    View containerSyncTime;
    @BindView(R.id.btnSyncTime)
    TextView btnSyncTime;
    @BindView(R.id.btnRemoveCamera)
    FButton btnRemoveCamera;
    @BindView(R.id.tvCamReset)
    TextView tvCamReset;
    @BindView(R.id.livFaceDetection)
    LabelSwItemView livFaceDetection;
    @BindView(R.id.livDetectionNotification)
    LabelSwItemView livDetectionNotification;
    @BindView(R.id.livPIRDetection)
    LabelActionItemView livPIRDetection;
    @BindView(R.id.vThirdPartyControl)
    View vThirdPartyControl;
    @BindView(R.id.tivAlexa)
    YRTextIconView tivAlexa;
    @BindView(R.id.tivAssistant)
    YRTextIconView tivAssistant;
    @BindView(R.id.livOpenPresetPoint)
    LabelActionItemView livOpenPresetPoint;
    @BindView(R.id.livSwitchConnectionMode)
    LabelActionItemView livSwitchConnectionMode;
    @BindView(R.id.livFlashLight)
    LabelTextItemView livFlashLight;
    @BindView(R.id.livSiren)
    LabelSwTagItemView livSiren;
    @BindView(R.id.livShootingSetting)
    LabelTextItemView livShootingSetting;

    private BindDevice mDevice;
    private INooieDeviceSettingPresenter mSettingPresenter;
    private String mDeviceId;
    private DevAllSettingsV2 mDevAllSettings = null;
    private CameraInfo mCameraInfo = null;
    private boolean mIsSubscribeCloud = false;
    private boolean mIsSubDevice = false;
    private boolean mIsLpDevice = false;
    private String mPDeviceId;
    private int mConnectionMode;
    private String mDeviceSsid;
    private Dialog mShowUnsubscribeDialog;
    private Dialog mShowRemoveDeviceDialog;
    private CustomAppStateManagerListener mAppStateManagerListener = null;
    private MediaPopupWindows mPopMenus;

    public static void toNooieDeviceSettingActivity(Context from, String deviceId, int type, boolean isOpenCloud, boolean isSubDevice, String pDeviceId, boolean isLpDevice, int bindType, int connectionMode, String deviceSsid) {
        Intent intent = new Intent(from, NooieDeviceSettingActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_TYPE, type);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_PORT, isOpenCloud);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_PARAM_1, isSubDevice);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_PARAM_2, pDeviceId);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_PARAM_3, isLpDevice);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_PARAM_4, bindType);
        intent.putExtra(ConstantValue.INTENT_KEY_CONNECTION_MODE, connectionMode);
        intent.putExtra(ConstantValue.INTENT_KEY_SSID, deviceSsid);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_setting);
        ButterKnife.bind(this);
        initData();
        initView();
        shortLinkDeviceInit();
        registerAppStateListener();
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
            return;
        } else {
            mDeviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
            mIsSubscribeCloud = getCurrentIntent() != null && getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_DEVICE_PORT, false);
            mIsSubDevice = getCurrentIntent() != null && getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_DATA_PARAM_1, false);
            mIsLpDevice = getCurrentIntent() != null && getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_DATA_PARAM_3, false);
            mPDeviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DATA_PARAM_2);
            mConnectionMode = getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_QC);
            mDevice = getDevice(mConnectionMode);
            mDeviceSsid = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SSID);
            mSettingPresenter = new NooieDeviceSettingPresenter(this);
        }
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.camera_settings_title);
        ivRight.setVisibility(View.GONE);
        setupDeviceSettingsView();
        updateUI(mDevice);
        if (mConnectionMode != ConstantValue.CONNECTION_MODE_AP_DIRECT && mSettingPresenter != null) {
            mSettingPresenter.getDevice(mDeviceId, mUserAccount);
        }
        if (mConnectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT && mSettingPresenter != null) {
            mSettingPresenter.getDevInfo(mDeviceId);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        tryResumeData();
        registerShortLinkKeepListener();
        tryStartBleApConnectionKeepingFrontTask();
        checkBleApDirectIsDestroy(mConnectionMode);
    }

    @Override
    public void onPause() {
        super.onPause();
        unRegisterShortLinkKeepListener();
        tryStopBleApConnectionKeepingFrontTask();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterAppStateListener();
        release();
        hideSyncDialog();
        hideUnsubscribeDialog();
        hideRemoveDeviceDialog();
        if (mSettingPresenter != null) {
            mSettingPresenter.detachView();
            mSettingPresenter = null;
        }
        mDevice = null;
        mDevAllSettings = null;
        mCameraInfo = null;
    }

    private void release() {
        if (switchStatusLight != null) {
            switchStatusLight.setOnCheckedChangeListener(null);
            switchStatusLight = null;
        }

        if (livDetectionNotification != null) {
            livDetectionNotification.release();
            livDetectionNotification = null;
        }

        if (livFaceDetection != null) {
            livFaceDetection.release();
            livFaceDetection = null;
        }

        if (livPIRDetection != null) {
            livPIRDetection.release();
            livPIRDetection = null;
        }
        if (livOpenPresetPoint != null) {
            livOpenPresetPoint.release();
            livOpenPresetPoint = null;
        }
        if (livSwitchConnectionMode != null) {
            livSwitchConnectionMode.release();
            livSwitchConnectionMode = null;
        }
        if (livFlashLight != null) {
            livFlashLight.release();
            livFlashLight = null;
        }
        if (livSiren != null) {
            livSiren.release();
            livSiren = null;
        }
        if (livShootingSetting != null) {
            livShootingSetting.release();
            livShootingSetting = null;
        }
        containerStatusLight = null;
        containerHomeAway = null;
        containerVideoAndAudio = null;
        containerMotionDetection = null;
        containerSoundDetection = null;
        containerSyncTime = null;
        containerShare = null;
        containerStorage = null;
        btnRemoveCamera = null;
        btnSyncTime = null;
        tvCamReset = null;
    }

    private void showPopMenu() {
        if (mPopMenus != null) {
            mPopMenus.dismiss();
        }

        mPopMenus = new MediaPopupWindows(this, new MediaPopupWindows.OnClickMediaListener() {
            @Override
            public void onFaceBookClick() {
                try {
                    getPackageManager().getPackageInfo("com.facebook.katana", 0);
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/106839131648494")));
                } catch (Exception e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/GNCC-Home-106839131648494")));
                }
            }

            @Override
            public void onYoutubeClick() {
                try {
                    getPackageManager().getPackageInfo("com.google.android.youtube", 0);
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("youtube://www.youtube.com/channel/UCZiTDE80vpROxN_Z76BOLFg")));
                } catch (Exception e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/channel/UCZiTDE80vpROxN_Z76BOLFg")));
                }
            }

            @Override
            public void onInstagramClick() {
                try {
                    getPackageManager().getPackageInfo("com.instagram.android", 0);
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("instagram://user?username=gncc_home")));
                } catch (Exception e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/gncc_home/")));
                }
            }

            @Override
            public void onEmailClick() {
                StringBuilder mailToSb = new StringBuilder();
                mailToSb.append("mailto:");
                mailToSb.append(getString(R.string.gncc_email));
                Uri uri = Uri.parse(mailToSb.toString());
                Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                startActivity(Intent.createChooser(intent, getString(R.string.about_select_email_application)));
            }
        });
        mPopMenus.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mPopMenus = null;
            }
        });

        mPopMenus.showAtLocation(this.findViewById(R.id.containerDeviceSetting),
                Gravity.TOP | Gravity.BOTTOM, 0, 0);
    }

    private void setupDeviceSettingsView() {

        switchStatusLight.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (mSettingPresenter != null) {
                    mSettingPresenter.setLedStatus(mDeviceId, isChecked);
                }
            }
        });

        livFlashLight.setVisibility(View.GONE);
        livSiren.setVisibility(View.GONE);

        livFaceDetection.displayLabelRightSw(View.VISIBLE);
        livDetectionNotification.displayLabelRightSw(View.VISIBLE);
        livPIRDetection.displayArrow(View.VISIBLE);
        livOpenPresetPoint.displayArrow(View.VISIBLE);
        livSwitchConnectionMode.displayArrow(View.VISIBLE);
        livFlashLight.displayLabelRight_1(View.VISIBLE).displayArrow(View.VISIBLE).setLabelRight_Color_1(CompatUtil.getColor(NooieApplication.mCtx, R.color.black_7a010C11));
        livSiren.displayLabelTag(View.VISIBLE).displayLabelRightSw(View.VISIBLE).setLabelTag(getString(R.string.cam_setting_siren_tag)).setTagColor(CompatUtil.getColor(NooieApplication.mCtx, R.color.black_7a010C11));
        livShootingSetting.displayLabelRight_1(View.VISIBLE).displayArrow(View.VISIBLE);

        livFaceDetection.setLabelRightSwListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (mSettingPresenter != null) {
                    showLoading();
                    mSettingPresenter.setDeviceAiMode(mDeviceId, isChecked);
                }
            }
        });

        livDetectionNotification.setLabelRightSwListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (mSettingPresenter != null) {
                    showLoading();
                    mSettingPresenter.updateDeviceNotice(mDeviceId, (isChecked ? ApiConstant.NOTICE_STATUS_ON : ApiConstant.NOTICE_STATUS_OFF));
                }
            }
        });

        livSiren.setLabelRightSwListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (mSettingPresenter != null) {
                    showLoading();
                    mSettingPresenter.setSiren(mDeviceId, isChecked);
                    sendSetSirenEvent(isChecked);
                }
            }
        });

        tivAlexa.setTextIcon(R.drawable.alexa_icon);
        tivAlexa.setTextIconBg(0);
        tivAlexa.setTextTitle(getString(R.string.alexa));
        tivAssistant.setTextIcon(R.drawable.google_assistant_icon);
        tivAssistant.setTextIconBg(0);
        tivAssistant.setTextTitle(getString(R.string.google_assistant));
        vThirdPartyControl.setVisibility(View.GONE);
    }

    private void updateUI(BindDevice device) {
        if (checkNull(device, containerStatusLight, containerVideoAndAudio, containerHomeAway, containerMotionDetection, containerSoundDetection, containerStorage, containerShare, containerSyncTime, livFaceDetection, livDetectionNotification, livPIRDetection, livOpenPresetPoint, livSwitchConnectionMode, livShootingSetting)) {
            return;
        }
        // setup UI visible
        boolean isMyDevice = device.getBind_type() == ApiConstant.BIND_TYPE_OWNER;
        boolean isOnline = device.getOnline() == ApiConstant.ONLINE_STATUS_ON;

        containerStatusLight.setVisibility(isMyDevice && isOnline && !NooieDeviceHelper.isNotSupportLedLight(device.getType()) ? View.VISIBLE : View.GONE);

        containerVideoAndAudio.setVisibility(isMyDevice && isOnline ? View.VISIBLE : View.GONE);

        containerHomeAway.setVisibility(isMyDevice && isOnline ? View.VISIBLE : View.GONE);

        containerMotionDetection.setVisibility(isMyDevice && isOnline ? View.VISIBLE : View.GONE);

        containerSoundDetection.setVisibility(isMyDevice && isOnline ? View.VISIBLE : View.GONE);

        containerStorage.setVisibility(isMyDevice ? View.VISIBLE : View.GONE);

        containerShare.setVisibility(isMyDevice && isOnline ? View.VISIBLE : View.GONE);

        containerSyncTime.setVisibility(isMyDevice && isOnline ? View.VISIBLE : View.GONE);

        setupResetTv(!mIsSubDevice && isMyDevice && isOnline);

        livFaceDetection.setVisibility(mIsLpDevice && isMyDevice && isOnline && NooieDeviceHelper.isSupportFaceDetection(device.getType()) ? View.VISIBLE : View.GONE);
        livDetectionNotification.setVisibility(isMyDevice && isOnline ? View.VISIBLE : View.GONE);
        livPIRDetection.setVisibility(mIsLpDevice && isMyDevice && isOnline ? View.VISIBLE : View.GONE);

        boolean openPresetPointIsShow = NooieDeviceHelper.isSupportPresetPoint(device.getType()) && isMyDevice && isOnline;
        livOpenPresetPoint.setVisibility(openPresetPointIsShow ? View.VISIBLE : View.GONE);
        boolean switchConnectionModeIsShow = (NooieDeviceHelper.mergeIpcType(device.getType()) == IpcType.MC120 || NooieDeviceHelper.mergeIpcType(device.getType()) == IpcType.HC320) && isMyDevice;
        livSwitchConnectionMode.setVisibility(switchConnectionModeIsShow ? View.VISIBLE : View.GONE);

        boolean shootingSettingShow = NooieDeviceHelper.isSupportShootingSetting(device.getType(), mConnectionMode) && isMyDevice;
        livShootingSetting.setVisibility(shootingSettingShow ? View.VISIBLE : View.GONE);

        boolean isNotice = device.getIs_notice() == ApiConstant.NOTICE_STATUS_ON;
        if (isNotice != livDetectionNotification.isLabelRightSwCheck()) {
            livDetectionNotification.toggleLabelRightSw();
        }

        boolean isAlexa = device.getIs_alexa() == ApiConstant.THIRD_PARTY_CONTROL_SUPPORT;
        boolean isAssistant = device.getIs_google() == ApiConstant.THIRD_PARTY_CONTROL_SUPPORT;
        if (isMyDevice && (isAlexa || isAssistant)) {
            vThirdPartyControl.setVisibility(View.VISIBLE);
            tivAlexa.setVisibility(isAlexa ? View.VISIBLE : View.GONE);
            tivAssistant.setVisibility(isAssistant ? View.VISIBLE : View.GONE);
        } else {
            vThirdPartyControl.setVisibility(View.GONE);
        }

        List<Integer> hideViewIds = new ArrayList<>();
        if (mIsLpDevice) {
            int[] viewIds = {R.id.containerHomeAway, R.id.containerMotionDetection, R.id.containerSoundDetection};
            for (int i = 0; i < viewIds.length; i++) {
                if (!hideViewIds.contains(viewIds[i])) {
                    hideViewIds.add(viewIds[i]);
                }
            }
        }
        if (mIsSubDevice) {
            int[] viewIds = {R.id.containerSyncTime};
            for (int i = 0; i < viewIds.length; i++) {
                if (!hideViewIds.contains(viewIds[i])) {
                    hideViewIds.add(viewIds[i]);
                }
            }
        }

        if (mConnectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            int[] viewIds = {R.id.livFaceDetection, R.id.livOpenPresetPoint, R.id.containerMotionDetection, R.id.containerSoundDetection, R.id.containerSyncTime, R.id.containerShare, R.id.livDetectionNotification, R.id.tvCamReset};
            for (int i = 0; i < viewIds.length; i++) {
                if (!hideViewIds.contains(viewIds[i])) {
                    hideViewIds.add(viewIds[i]);
                }
            }
        }

        for (Integer hideViewId : CollectionUtil.safeFor(hideViewIds)) {
            if (findViewById(hideViewId) != null) {
                findViewById(hideViewId).setVisibility(View.GONE);
            }
            /*
            if (mConnectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT && device != null && NooieDeviceHelper.mergeIpcType(device.getType()) == IpcType.HC320 && !mIsSubDevice && isMyDevice && isOnline) {
                tvCamReset.setVisibility(View.VISIBLE);
            }
             */
        }

        int camSettingType = getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_DATA_TYPE, ConstantValue.CAM_SETTING_TYPE_NORMAL);
        if (camSettingType == ConstantValue.CAM_SETTING_TYPE_DEVICE_OFFLINE) {
            int[] viewIds = {R.id.containerStatusLight, R.id.containerMotionDetection,
                    R.id.containerSoundDetection, R.id.containerShare, R.id.containerSyncTime, R.id.livFaceDetection, R.id.livPIRDetection, R.id.livOpenPresetPoint};
            for (int i = 0; i < viewIds.length; i++) {
                if (findViewById(viewIds[i]) != null) {
                    findViewById(viewIds[i]).setVisibility(View.GONE);
                }
            }
            containerStorage.setVisibility(View.VISIBLE);
        }
    }

    private void refreshAllSettings() {
        if (mIsLpDevice) {
            refreshAllSettingsForLpDevice();
            return;
        }
        if (checkNull(mDevAllSettings, switchStatusLight) || mDevAllSettings.commSettings == null) {
            return;
        }

        boolean isLedOpen = mDevAllSettings.commSettings.led == ConstantValue.CMD_STATE_ENABLE;
        if (switchStatusLight.isChecked() != isLedOpen) {
            switchStatusLight.toggleNoCallback();
        }
    }

    private void refreshAllSettingsForLpDevice() {
        if (checkNull(mCameraInfo, livFaceDetection)) {
            return;
        }

        if (mCameraInfo.ai != livFaceDetection.isLabelRightSwCheck()) {
            livFaceDetection.toggleLabelRightSw();
        }
    }

    private void refreshMediaMode(NooieMediaMode mediaMode) {
        if (checkNull(livShootingSetting)) {
            return;
        }
        String modeTxt = mediaMode != null ? NooieDeviceHelper.getFileSettingModeText(NooieApplication.mCtx, mediaMode.mode) : "";
        livShootingSetting.setLabelRight_1(modeTxt);
    }

    private void refreshLedLight(boolean on) {
        if (checkNull(switchStatusLight)) {
            return;
        }
        if (switchStatusLight.isChecked() != on) {
            switchStatusLight.toggleNoCallback();
        }
    }

    private void setupResetTv(boolean show) {
        if (!show) {
            tvCamReset.setVisibility(View.GONE);
            return;
        }

        tvCamReset.setVisibility(View.VISIBLE);
        SpannableStringBuilder style = new SpannableStringBuilder();
        String factorySetting = getResources().getString(R.string.camera_settings_factory_settings);
        String textContent = String.format(getResources().getString(R.string.camera_settings_reset_to), factorySetting);
        style.append(textContent);

        ClickableSpan clickSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                DialogUtils.showConfirmWithSubMsgDialog(NooieDeviceSettingActivity.this, R.string.camera_settings_reset, R.string.camera_settings_reset_info, R.string.cancel, R.string.confirm_upper, new DialogUtils.OnClickConfirmButtonListener() {
                    @Override
                    public void onClickRight() {
                        if (mSettingPresenter != null) {
                            if (mConnectionMode != ConstantValue.CONNECTION_MODE_AP_DIRECT) {
                                mSettingPresenter.setFactoryReset(mUserAccount, mDeviceId);
                            } else {
                                String model = mDevice != null ? mDevice.getType() : "";
                                mSettingPresenter.setFactoryResetForAp(mDeviceId, model);
                            }
                        }
                    }

                    @Override
                    public void onClickLeft() {
                    }
                });
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setUnderlineText(false);
                ds.setTypeface(FontUtil.loadTypeface(getApplicationContext(), "fonts/manrope-semibold.otf"));
            }
        };
        style.setSpan(clickSpan, textContent.indexOf(factorySetting), textContent.indexOf(factorySetting) + factorySetting.length(), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvCamReset.setText(style);

        ForegroundColorSpan colorSpan = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.theme_green_subtext_color));
        style.setSpan(colorSpan, textContent.indexOf(factorySetting), textContent.indexOf(factorySetting) + factorySetting.length(), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvCamReset.setMovementMethod(LinkMovementMethod.getInstance());
        tvCamReset.setText(style);
    }

    @OnClick({R.id.ivLeft, R.id.containerVideoAndAudio, R.id.containerHomeAway, R.id.containerStatusLight, R.id.btnSyncTime, R.id.containerMotionDetection, R.id.containerSoundDetection,
            R.id.containerStorage, R.id.containerShare, R.id.containerInfo, R.id.btnRemoveCamera, R.id.livPIRDetection, R.id.livOpenPresetPoint, R.id.livSwitchConnectionMode, R.id.livShootingSetting, R.id.tivAlexa, R.id.tivAssistant,
            R.id.livFlashLight, R.id.containerContactUS})
    public void onViewClicked(View view) {
        String model = mDevice != null ? mDevice.getType() : IpcType.PC420.getType();
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.containerVideoAndAudio:
                DeviceSettingBean deviceSettingBean = new DeviceSettingBean();
                if (!mIsLpDevice && mDevAllSettings != null && mDevAllSettings.commSettings != null) {
                    deviceSettingBean.setAudioRecOpen(mDevAllSettings.commSettings.audioRec == ConstantValue.CMD_STATE_ENABLE);
                    deviceSettingBean.setRotateOn(mDevAllSettings.commSettings.flip == ConstantValue.CMD_STATE_ENABLE);
                    deviceSettingBean.setIcr(mDevAllSettings.commSettings.icr);
                    deviceSettingBean.setMotionTracking(mDevAllSettings.commSettings.motTrack == ConstantValue.CMD_STATE_ENABLE);
                } else if (mIsLpDevice && mCameraInfo != null) {
                    deviceSettingBean.setRotateOn(mCameraInfo.videoRotate);
                    deviceSettingBean.setIcr(NooieDeviceHelper.convertICRMode(mCameraInfo.ir).getIntValue());
                }
                //String model = mDevice != null ? mDevice.getType() : IpcType.PC420.getType();
                DeviceAudioAndVideoActivity.toDeviceAudioAndVideoActivity(this, mDeviceId, deviceSettingBean, model, mConnectionMode, mIsSubDevice, mIsLpDevice);
                setIsGotoOtherPage(true);
                break;
            case R.id.containerHomeAway:
                HomeAwayActivity.toHomeAwayActivity(this, mDeviceId, mConnectionMode, mDeviceSsid);
                setIsGotoOtherPage(true);
                break;
            case R.id.containerStatusLight:
                switchStatusLight.toggle(true);
                break;
            case R.id.livOpenPresetPoint:
                PresetPointActivity.toPresetPointActivity(this, mDeviceId, model);
                break;
            case R.id.btnSyncTime:
                showSyncTimeDialog();
                break;
            case R.id.containerMotionDetection: {
                boolean openCamera = mDevAllSettings != null && mDevAllSettings.commSettings != null ? mDevAllSettings.commSettings.sleep != ConstantValue.CMD_STATE_ENABLE : true;
                NooieDetectionActivity.toNooieDetectionActivity(NooieDeviceSettingActivity.this, mDeviceId, ConstantValue.NOOIE_DETECT_TYPE_MOTION, openCamera);
                break;
            }
            case R.id.containerSoundDetection: {
                boolean openCamera = mDevAllSettings != null && mDevAllSettings.commSettings != null ? mDevAllSettings.commSettings.sleep != ConstantValue.CMD_STATE_ENABLE : true;
                NooieDetectionActivity.toNooieDetectionActivity(NooieDeviceSettingActivity.this, mDeviceId, ConstantValue.NOOIE_DETECT_TYPE_SOUND, openCamera);
                break;
            }
            case R.id.containerStorage:
                NooieStorageActivity.toNooieStorageActivity(this, mDeviceId, mIsSubDevice, mConnectionMode, getBindType(), mIsLpDevice, model);
                setIsGotoOtherPage(true);
                break;
            case R.id.containerShare:
                NooieShareDeviceActivity.toNooieShareDeviceActivity(this, mDeviceId);
                setIsGotoOtherPage(true);
                break;
            case R.id.containerInfo:
                NooieDeviceInfoActivity.toNooieDeviceInfoActivity(this, mDeviceId, ConstantValue.CAM_INFO_TYPE_NORMAL, mIsSubDevice, mIsLpDevice, mConnectionMode, model);
                setIsGotoOtherPage(true);
                break;
            case R.id.livPIRDetection: {
                Bundle param = new Bundle();
                param.putString(ConstantValue.INTENT_KEY_DEVICE_ID, mDeviceId);
                param.putInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, mConnectionMode);
                DevicePIRActivity.toDevicePIRActivity(this, param);
                setIsGotoOtherPage(true);
                break;
            }
            case R.id.livSwitchConnectionMode: {
                Bundle param = new Bundle();
                param.putString(ConstantValue.INTENT_KEY_DEVICE_ID, mDeviceId);
                param.putString(ConstantValue.INTENT_KEY_IPC_MODEL, model);
                param.putInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, mConnectionMode);
                SwitchConnectionModeActivity.toSwitchConnectionModeActivity(this, param);
                setIsGotoOtherPage(true);
                break;
            }
            case R.id.livShootingSetting:
                Bundle param = new Bundle();
                param.putString(ConstantValue.INTENT_KEY_DEVICE_ID, mDeviceId);
                param.putString(ConstantValue.INTENT_KEY_IPC_MODEL, model);
                param.putInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, mConnectionMode);
                FileSettingActivity.toFileSettingActivity(this, param);
                setIsGotoOtherPage(true);
                break;
            case R.id.btnRemoveCamera:
                tryToRemoveDevice();
                break;
            case R.id.tivAlexa:
                gotoThirdPartyPage(THIRD_PARTY_ALEXA);
                setIsGotoOtherPage(true);
                break;
            case R.id.tivAssistant:
                gotoThirdPartyPage(THIRD_PARTY_GOOGLE_ASSISTANT);
                setIsGotoOtherPage(true);
                break;
            case R.id.livFlashLight:
                gotoFlashLightPage();
                setIsGotoOtherPage(true);
                break;
            case R.id.containerContactUS:
                showPopMenu();
                break;
        }
    }

    private void showDeleteConfirmDialog() {
        DialogUtils.showConfirmWithSubMsgDialog(this, getString(R.string.camera_settings_remove_camera_confirm),
                String.format(getString(R.string.camera_settings_remove_info_confirm), (mDevice == null ? "" : mDevice.getName())),
                R.string.camera_settings_no_remove, R.string.confirm_upper, new DialogUtils.OnClickConfirmButtonListener() {
                    @Override
                    public void onClickRight() {
                        boolean isOnline = mDevice != null && mDevice.getOnline() == ApiConstant.ONLINE_STATUS_ON;
                        boolean isMyDevice = mDevice != null && mDevice.getBind_type() == ApiConstant.BIND_TYPE_OWNER;
                        mSettingPresenter.removeCamera(mDeviceId, mUid, mUserAccount, isOnline, isMyDevice, mIsSubDevice, mPDeviceId);
                    }

                    @Override
                    public void onClickLeft() {
                    }
                });
    }

    private void tryToRemoveDevice() {
        boolean isMyDevice = mDevice != null && mDevice.getBind_type() == ApiConstant.BIND_TYPE_OWNER;
        if (isMyDevice && mIsSubscribeCloud) {
            showUnsubscribeDialog();
        } else {
            showRemoveDeviceDialog();
        }
    }

    private void showUnsubscribeDialog() {
        hideUnsubscribeDialog();
        mShowUnsubscribeDialog = DialogUtils.showConfirmWithSubMsgDialog(this, R.string.unsubscribe, R.string.camera_settings_unsubcribe_cloud_content, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                String model = mDevice != null ? mDevice.getType() : new String();
                NooieStorageActivity.toNooieStorageActivity(NooieDeviceSettingActivity.this, mDeviceId, mIsSubDevice, mConnectionMode, getBindType(), mIsLpDevice, model);
            }

            @Override
            public void onClickLeft() {
            }
        });
    }

    private void showRemoveDeviceDialog() {
        hideRemoveDeviceDialog();
        String deviceName = mDevice == null ? "" : mDevice.getName();
        String model = mDevice != null ? mDevice.getType() : "";
        mShowRemoveDeviceDialog = DialogUtils.showConfirmWithSubMsgDialog(this, getString(R.string.camera_settings_remove_camera), String.format(getString(R.string.camera_settings_remove_info), deviceName), R.string.cancel, R.string.confirm_upper, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                if (mConnectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
                    if (mSettingPresenter != null) {
                        showLoading();
                        mSettingPresenter.removeApDevice(mUserAccount, mUid, mDeviceId, model);
                    }
                    return;
                }
                if (mSettingPresenter != null && mDevice != null) {
                    showLoading();
                    boolean isOnline = mDevice.getOnline() == ApiConstant.ONLINE_STATUS_ON;
                    boolean isMyDevice = mDevice.getBind_type() == ApiConstant.BIND_TYPE_OWNER;
                    boolean isRemoveFromServer = (IpcType.getIpcType(mDevice.getType()) == IpcType.PC420 && NooieDeviceHelper.compareVersion(mDevice.getVersion(), ConstantValue.MIN_DEVICE_REMOVE_SELF_420) < 0)
                            || (IpcType.getIpcType(mDevice.getType()) == IpcType.PC530 && NooieDeviceHelper.compareVersion(mDevice.getVersion(), ConstantValue.MIN_DEVICE_REMOVE_SELF_530) < 0)
                            || (IpcType.getIpcType(mDevice.getType()) == IpcType.EC810_CAM && NooieDeviceHelper.compareVersion(mDevice.getVersion(), ConstantValue.MIN_DEVICE_REMOVE_SELF_810) < 0);
                    if (isRemoveFromServer) {
                        mSettingPresenter.removeCamera(mDeviceId, mUid, mUserAccount, isOnline, isMyDevice, mIsSubDevice, mPDeviceId);
                        return;
                    }
                    mSettingPresenter.removeDevice(mDeviceId, mUid, mUserAccount, isOnline, isMyDevice, mIsSubDevice, mPDeviceId);
                }
            }

            @Override
            public void onClickLeft() {
            }
        });
    }

    private void hideUnsubscribeDialog() {
        if (mShowUnsubscribeDialog != null) {
            mShowUnsubscribeDialog.dismiss();
            mShowUnsubscribeDialog = null;
        }
    }

    private void hideRemoveDeviceDialog() {
        if (mShowRemoveDeviceDialog != null) {
            mShowRemoveDeviceDialog.dismiss();
            mShowRemoveDeviceDialog = null;
        }
    }

    private AlertDialog mSyncTimeDialog;

    private void showSyncTimeDialog() {
        hideSyncDialog();
        mSyncTimeDialog = DialogUtils.showConfirmWithSubMsgDialog(this, R.string.cam_setting_sync_time_title, R.string.cam_setting_sync_time_content, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                if (mSettingPresenter != null) {
                    try {
                        float timeZone = DataHelper.toFloat(CountryUtil.getCurrentTimezone());
                        long currentTime = System.currentTimeMillis();
                        String utcStr = DateTimeUtil.localToUtc(currentTime, DateTimeUtil.PATTERN_YMD_HMS_1);
                        SimpleDateFormat formatStr = new SimpleDateFormat(DateTimeUtil.PATTERN_YMD_HMS_1);
                        Date date = formatStr.parse(utcStr);
                        //int timeOffset = (int)((currentTime - date.getTime()) / 1000L);
                        long networkTime = (currentTime + GlobalData.getInstance().getGapTime() * 1000L);
                        String localNetworkTimeStr = DateTimeUtil.localToUtc(networkTime, DateTimeUtil.PATTERN_YMD_HMS_1);
                        Date date1 = formatStr.parse(localNetworkTimeStr);
                        int timeOffset = (int) ((currentTime - date1.getTime()) / 1000L);
                        StringBuilder timeLogSb = new StringBuilder();
                        timeLogSb.append("timeZone:" + timeZone + " ");
                        timeLogSb.append("currentTime:" + currentTime + " ");
                        timeLogSb.append("currentUtcTime:" + date.getTime() + " ");
                        timeLogSb.append("networkTime:" + networkTime + " ");
                        timeLogSb.append("networkLocalTimeTime:" + date1.getTime() + " ");
                        NooieLog.d("-->> NooieDeviceSettingActivity showSyncTimeDialog " + timeLogSb.toString());
                        //NooieLog.d("-->> NooieDeviceSettingActivity showSyncTimeDialog timeZone=" + timeZone + " currentTime=" + currentTime + " currentUtcTime=" + date.getTime() + " utcTimeStr=" + utcStr + " networkTime=" + (currentTime + GlobalPrefs.getPreferences(NooieApplication.mCtx).getGapTime() * 1000L) + " localNetworkTimeStr=" + localNetworkTimeStr);
                        //NooieLog.d("-->> NooieDeviceSettingActivity showSyncTimeDialog timeZone=" + timeZone + " systime=" + currentTime + " utcsys=" + date.getTime() + " timeoffset=" + timeOffset);
                        showLoading();
                        mSettingPresenter.setSyncTime(mDeviceId, 1, timeZone, timeOffset);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onClickLeft() {
            }
        });
    }

    private void hideSyncDialog() {
        if (mSyncTimeDialog != null) {
            mSyncTimeDialog.dismiss();
            mSyncTimeDialog = null;
        }
    }

    public DeviceInfo getDeviceInfo(String deviceId) {
        return NooieDeviceHelper.getDeviceInfoById(deviceId);
    }

    @Override
    public void showLoadingDialog() {
        //showLoading();
    }

    @Override
    public void hideLoadingDialog() {
        //hideLoading();
    }

    @Override
    public void onGetDeviceSetting(int code, DeviceComplexSetting complexSetting) {
        if (isDestroyed()) {
            return;
        }
        if (code != SDKConstant.CODE_CACHE) {
            hideLoading();
        }
        if (code != Constant.OK || complexSetting == null) {
            return;
        }
        if (complexSetting.getDevAllSettings() != null) {
            mDevAllSettings = complexSetting.getDevAllSettings();
            refreshAllSettings();
        } else if (complexSetting.getCameraInfo() != null) {
            mCameraInfo = complexSetting.getCameraInfo();
            refreshAllSettings();
        }
    }

    @Override
    public void onGetAllSettingResult(String msg, DevAllSettingsV2 settings) {
        if (isDestroyed()) {
            return;
        }

        hideLoading();
        if (ConstantValue.SUCCESS.equalsIgnoreCase(msg)) {
            mDevAllSettings = settings;
            refreshAllSettings();
            if (mDevAllSettings != null && mDevAllSettings.commSettings != null) {
                FormatInfo formatInfo = new FormatInfo();
                formatInfo.setFree(mDevAllSettings.commSettings.sdFree);
                formatInfo.setTotal(mDevAllSettings.commSettings.sdTotal);
                formatInfo.setFormatStatus(mDevAllSettings.commSettings.sdFree == mDevAllSettings.commSettings.sdTotal && mDevAllSettings.commSettings.sdFree < 0 ? ConstantValue.NOOIE_SD_STATUS_NO_SD : ConstantValue.NOOIE_SD_STATUS_NORMAL);
            }
        }
    }

    @Override
    public void onGetDeviceInfo(String result, DevInfo devInfo) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result) && devInfo != null) {
        }
    }

    @Override
    public void onNotifyGetDeviceSuccess(BindDevice device) {
        if (isDestroyed()) {
            return;
        }
        if (device != null) {
            mDevice = device;
            updateUI(device);
        }
    }

    @Override
    public void onNotifyGetDeviceFailed(String msg) {
        if (isDestroyed()) {
            return;
        }
    }

    @Override
    public void notifyGetLedFailed(String message) {
        if (isDestroyed()) {
            return;
        }
        //ToastUtil.showToast(this, message);
        enableLedSwitch(true);
    }

    @Override
    public void notifyGetLedSuccess(boolean open) {
        if (isDestroyed() || checkNull(switchStatusLight)) {
            return;
        }
        enableLedSwitch(true);
        refreshLedLight(open);
    }

    @Override
    public void notifySetLedResult(String result) {
        if (isDestroyed()) {
            return;
        }

        if (result.equals(ConstantValue.SUCCESS)) {
            ToastUtil.showToast(this, R.string.success);
        } else {
            ToastUtil.showToast(this, result);
        }
    }

    @Override
    public void notifyRemoveCameraResult(String result) {
        if (isDestroyed()) {
            return;
        }

        hideLoading();
        if (result.equals(ConstantValue.SUCCESS)) {
            sendRemoveCameraBroadcast();
            HomeActivity.toHomeActivity(this);
            finish();
        } else {
            ToastUtil.showToast(this, result);
        }
    }

    @Override
    public void notifyRestartDeviceSuccess(String deviceId) {
        if (isDestroyed()) {
            return;
        }

        int loadDuration = 1000 * 35;
        ToastUtil.showToast(this, R.string.settings_restarting, loadDuration);
        showLoading(false);

        TaskUtil.delayAction(loadDuration, new TaskUtil.OnDelayTimeFinishListener() {
            @Override
            public void onFinish() {
                hideLoading();
            }
        });
    }

    @Override
    public void notifyRestartDeviceFailed(String message) {
        if (isDestroyed()) {
            return;
        }
    }

    @Override
    public void notifyFactoryResetResult(String result) {
        if (isDestroyed()) {
            return;
        }

        if (result.equals(ConstantValue.SUCCESS)) {
            ToastUtil.showToast(this, getResources().getString(R.string.success));
            HomeActivity.toHomeActivity(this);
            finish();
        } else {
            ToastUtil.showToast(this, getString(R.string.network_error0));
        }
    }

    @Override
    public void notifySetSyncTimeResult(String result) {
        if (isDestroyed()) {
            return;
        }
        hideLoading();
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            ToastUtil.showToast(this, R.string.success);
        } else {
            ToastUtil.showToast(this, R.string.get_fail);
        }
    }

    @Override
    public void notifyLoadPackInfoResult(String msg, PackInfoResult result) {
        if (isDestroyed()) {
            return;
        }
        if (ConstantValue.SUCCESS.equalsIgnoreCase(msg)) {
            mIsSubscribeCloud = result != null && NooieCloudHelper.isSubscribeCloud(result.getStatus());
        } else {
            mIsSubscribeCloud = false;
        }
    }

    private void sendRemoveCameraBroadcast() {
        Intent intent = new Intent(ConstantValue.BROADCAST_KEY_REMOVE_CAMERA);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, mDeviceId);
        NotificationUtil.sendBroadcast(NooieApplication.mCtx, intent);
    }

    @Override
    public void onGetCamInfoResult(String result, CameraInfo info) {
        if (isDestroyed()) {
            return;
        }

        hideLoading();
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result) && info != null) {
            mCameraInfo = info;
            refreshAllSettings();
        }
    }

    @Override
    public void onSetDeviceFDModeResult(String result) {
        if (isDestroyed()) {
            return;
        }
        hideLoading();
    }

    @Override
    public void onSetDevicePDModeResult(String result) {
        if (isDestroyed()) {
            return;
        }
        hideLoading();
    }

    @Override
    public void onUpdateDeviceNoticeResult(String result) {
        if (isDestroyed()) {
            return;
        }
        hideLoading();
    }

    @Override
    public void onGetPirState(int state, PirStateV2 pirState) {
        if (isDestroyed()) {
            return;
        }
        if (state == SDKConstant.SUCCESS) {
            refreshFlashLight(mConnectionMode, mDevice, pirState);
        }
    }

    @Override
    public void onSetSiren(int state) {
        if (isDestroyed()) {
            return;
        }
        hideLoading();
    }

    @Override
    public void onGetFileSettingMode(int state, NooieMediaMode mode) {
        if (isDestroyed()) {
            return;
        }
        if (state == SDKConstant.SUCCESS) {
            refreshMediaMode(mode);
        }
    }

    @Override
    public void onRemoveApDevice(int state) {
        if (isDestroyed()) {
            return;
        }
        hideLoading();
        redirectGotoHomePage();
    }

    @Override
    public String getCurDeviceId() {
        if (mConnectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            return null;
        }
        return mDeviceId;
    }

    @Override
    public boolean checkIsAddDeviceApHelperListener() {
        return true;
    }

    private BindDevice getDevice(int connectionMode) {
        if (connectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            String defaultModel = mIsLpDevice ? IpcType.HC320.getType() : IpcType.MC120.getType();
            return NooieDeviceHelper.getDeviceByConnectionMode(mConnectionMode, mDeviceId, defaultModel);
        } else {
            return NooieDeviceHelper.getDeviceById(mDeviceId);
        }
    }

    private int getBindType() {
        int bindType = ApiConstant.BIND_TYPE_OWNER;
        if (getCurrentIntent() != null) {
            bindType = getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_DATA_PARAM_4, ApiConstant.BIND_TYPE_OWNER);
        } else if (mConnectionMode != ConstantValue.CONNECTION_MODE_AP_DIRECT && getDevice(mConnectionMode) != null) {
            bindType = getDevice(mConnectionMode).getBind_type();
        }
        return bindType;
    }

    private void gotoThirdPartyPage(int type) {
        StringBuilder urlBuilder = new StringBuilder(ConstantValue.THIRD_PARTY_CONTROL_PARENT_URL);
        if (type == THIRD_PARTY_ALEXA) {
            urlBuilder.append(ConstantValue.THIRD_PARTY_CONTROL_ALEXA_PATH);
        } else if (type == THIRD_PARTY_GOOGLE_ASSISTANT) {
            urlBuilder.append(ConstantValue.THIRD_PARTY_CONTROL_GOOGLE_ASSISTANT_PATH);
        } else {
            return;
        }
        Bundle param = new Bundle();
        param.putString(ConstantValue.INTENT_KEY_URL, urlBuilder.toString());
        param.putBoolean(ConstantValue.INTENT_KEY_DATA_PARAM, true);
        param.putBoolean(ConstantValue.INTENT_KEY_DATA_PARAM_1, true);
        HybridWebViewActivity.toHybridWebViewActivity(this, param);
    }

    @Override
    public void dealAfterDeviceShortLink() {
        resumeData();
    }

    @Override
    public ShortLinkDeviceParam getShortLinkDeviceParam() {
        if (mDevice == null) {
            return null;
        }
        ShortLinkDeviceParam shortLinkDeviceParam = new ShortLinkDeviceParam(mUid, mDeviceId, mDevice.getType(), mIsSubDevice, true, mConnectionMode);
        return shortLinkDeviceParam;
    }

    @Override
    public CurrentDeviceParam getCurrentDeviceParam() {
        if (TextUtils.isEmpty(mDeviceId)) {
            return null;
        }
        String model = mDevice != null ? mDevice.getType() : null;
        CurrentDeviceParam currentDeviceParam = null;
        if (mConnectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            currentDeviceParam = new CurrentDeviceParam();
            currentDeviceParam.setDeviceId(mDeviceId);
            currentDeviceParam.setConnectionMode(mConnectionMode);
            currentDeviceParam.setModel(model);
        } else {
        }
        return currentDeviceParam;
    }

    private void shortLinkDeviceInit() {
        if (getShortLinkDeviceParam() == null) {
            return;
        }
        if (!NooieDeviceHelper.isSortLinkDevice(getShortLinkDeviceParam().getModel(), getShortLinkDeviceParam().isSubDevice(), getShortLinkDeviceParam().getConnectionMode()) || !getShortLinkDeviceParam().isInit()) {
            return;
        }
        setIsDestroyShortLink(true);
    }

    private void refreshFlashLight(int connectionMode, BindDevice device, PirStateV2 pirState) {
        if (isDestroyed() || checkNull(livFlashLight, livSiren, pirState)) {
            return;
        }
        if (device == null) {
            return;
        }
        boolean isMyDevice = device.getBind_type() == ApiConstant.BIND_TYPE_OWNER;
        boolean isOnline = device.getOnline() == ApiConstant.ONLINE_STATUS_ON;
        boolean isPirOn = pirState.enable;
        boolean isSirenOn = pirState.siren;
        int lightMode = pirState.lightMode;
        boolean isShowFlashLight = NooieDeviceHelper.isSupportFlashLight(device.getType()) && isMyDevice && isOnline && isPirOn && connectionMode != ConstantValue.CONNECTION_MODE_AP_DIRECT;
        livFlashLight.setVisibility(isShowFlashLight ? View.VISIBLE : View.GONE);
        livFlashLight.setLabelRight_1(CopyWritingHelper.convertFlashLightModeTitle(this, pirState.lightMode));
        livSiren.setVisibility(isShowFlashLight ? View.VISIBLE : View.GONE);
        refreshSiren(isSirenOn);
        refreshFlashLightState(lightMode);
    }

    private void refreshFlashLightState(int mode) {
        if (isDestroyed() || checkNull(livFlashLight)) {
            return;
        }
        livFlashLight.setLabelRight_1(CopyWritingHelper.convertFlashLightModeTitle(NooieApplication.mCtx, mode));
    }

    private void refreshSiren(boolean enable) {
        if (isDestroyed() || checkNull(livSiren)) {
            return;
        }
        if (enable != livSiren.isLabelRightSwCheck()) {
            livSiren.toggleLabelRightSw();
        }
    }

    private void enableLedSwitch(boolean enable) {
        if (checkNull(switchStatusLight)) {
            return;
        }
        switchStatusLight.setEnabled(enable);
    }

    private void gotoFlashLightPage() {
        Bundle param = new Bundle();
        param.putString(ConstantValue.INTENT_KEY_DEVICE_ID, mDeviceId);
        FlashLightActivity.toFlashLightActivity(this, param);
    }

    private void sendSetSirenEvent(boolean on) {
        try {
            ArrayMap<String, Object> externalMap = new ArrayMap<>();
            String model = mDevice != null ? mDevice.getType() : "";
            if (!TextUtils.isEmpty(model)) {
                externalMap.put("deviceModel", model);
            }
            externalMap.put("state", convertSirenStatusForEvent(on));
            EventTrackingApi.getInstance().trackNormalEvent(EventDictionary.EVENT_ID_158, "", 0, GsonHelper.convertToJson(externalMap), mDeviceId);
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
    }

    private String convertSirenStatusForEvent(boolean on) {
        return on ? "turnOn" : "turnOff";
    }

    private void tryStartBleApConnectionKeepingFrontTask() {
        if (!checkIsBleApConnectionFrontKeepingEnable() || !ApHelper.getInstance().checkBleApDeviceConnectingExist()) {
            return;
        }
        Bundle param = new Bundle();
        param.putString(ApHelper.KEY_PARAM_DEVICE_MODEL, getCurrentDeviceParam().getModel());
        param.putInt(ApHelper.KEY_PARAM_CONNECTION_MODE, getCurrentDeviceParam().getConnectionMode());
        param.putString(ApHelper.KEY_PARAM_DEVICE_ID, mDeviceId);
        param.putString(ApHelper.KEY_PARAM_UID, mUid);
        param.putString(ApHelper.KEY_PARAM_ACCOUNT, mUserAccount);
        ApHelper.getInstance().startBleApDeviceConnectionFrontKeepingTask(param);
    }

    private void tryStopBleApConnectionKeepingFrontTask() {
        if (!checkIsBleApConnectionFrontKeepingEnable() || checkIsGotoOtherPage()) {
            return;
        }
        ApHelper.getInstance().stopBleApDeviceConnectionFrontKeepingTask();
    }

    private void tryResumeData() {
        boolean isTryConnectShortLinkDevice = getShortLinkDeviceParam() != null
                && NooieDeviceHelper.isSortLinkDevice(getShortLinkDeviceParam().getModel(), getShortLinkDeviceParam().isSubDevice(), getShortLinkDeviceParam().getConnectionMode())
                && checkDeviceActive(mDevice);
        if (isTryConnectShortLinkDevice) {
            tryConnectShortLinkDevice();
        } else {
            resumeData();
        }
    }

    private void resumeData() {
        if (mConnectionMode != ConstantValue.CONNECTION_MODE_AP_DIRECT && !checkDeviceActive(mDevice)) {
            return;
        }
        String model = mDevice != null ? mDevice.getType() : null;
        boolean isGetDeviceSetting = (mIsLpDevice && mCameraInfo == null) || (!mIsLpDevice && (mDevAllSettings == null || mDevAllSettings.commSettings == null));
        if (isGetDeviceSetting && mSettingPresenter != null) {
            showLoading();
            mSettingPresenter.getDeviceSetting(mDeviceId, model);
        }
        if (mConnectionMode != ConstantValue.CONNECTION_MODE_AP_DIRECT && mSettingPresenter != null) {
            mSettingPresenter.getDeviceStorageState(mDeviceId, getBindType());
        }
        boolean isRequestPir = mConnectionMode != ConstantValue.CONNECTION_MODE_AP_DIRECT && NooieDeviceHelper.isSupportFlashLight(model);
        if (isRequestPir && mSettingPresenter != null) {
            mSettingPresenter.getPirState(mDeviceId);
        }
        if (NooieDeviceHelper.isSupportShootingSetting(model, mConnectionMode) && mSettingPresenter != null) {
            mSettingPresenter.getFileSettingMode(mDeviceId);
        }
        boolean isNeedToGetLedLight = mIsLpDevice && mDevice != null && !NooieDeviceHelper.isNotSupportLedLight(mDevice.getType());
        if (isNeedToGetLedLight && mSettingPresenter != null) {
            enableLedSwitch(false);
            mSettingPresenter.getLedStatus(mDeviceId);
        }
    }

    private boolean checkDeviceActive(BindDevice device) {
        boolean isDeviceActive = device == null || (device.getOnline() == ApiConstant.ONLINE_STATUS_ON);
        return isDeviceActive;
    }

    private void registerAppStateListener() {
        if (mAppStateManagerListener == null) {
            mAppStateManagerListener = new CustomAppStateManagerListener();
        }
        AppStateManager.getInstance().addListener(mAppStateManagerListener);
    }

    private void unRegisterAppStateListener() {
        if (mAppStateManagerListener != null) {
            AppStateManager.getInstance().removeListener(mAppStateManagerListener);
        }
    }

    private class CustomAppStateManagerListener implements AppStateManager.AppStateManagerListener {
        @Override
        public void onAppBackground() {
            NooieLog.d("-->> debug NooieDeviceSettingActivity CustomAppStateManagerListener onAppBackground: ");
            checkIsNeedToDisconnectShortLinkDevice();
        }

        @Override
        public void onAppForeground() {
            NooieLog.d("-->> debug NooieDeviceSettingActivity CustomAppStateManagerListener onAppForeground");
            /*
            boolean isTryConnectShortLinkDevice = getShortLinkDeviceParam() != null
                    && NooieDeviceHelper.isSortLinkDevice(getShortLinkDeviceParam().getModel(), getShortLinkDeviceParam().isSubDevice(), getShortLinkDeviceParam().getConnectionMode())
                    && checkDeviceActive(mDevice);
            if (isTryConnectShortLinkDevice) {
                tryConnectShortLinkDevice();
            }

             */
        }
    }
}
