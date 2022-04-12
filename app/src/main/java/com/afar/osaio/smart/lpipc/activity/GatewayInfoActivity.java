package com.afar.osaio.smart.lpipc.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.device.bean.NooieDevice;
import com.afar.osaio.smart.cache.GatewayDeviceCache;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.lpipc.contract.GatewayInfoContract;
import com.afar.osaio.smart.lpipc.presenter.GatewayInfoPresenter;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.widget.LabelSwItemView;
import com.afar.osaio.widget.LabelTagItemView;
import com.afar.osaio.widget.LabelTextItemView;
import com.nooie.common.base.GlobalData;
import com.nooie.common.utils.configure.CountryUtil;
import com.nooie.common.utils.data.DataHelper;
import com.nooie.common.utils.time.DateTimeUtil;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.GatewayDevice;
import com.nooie.sdk.device.bean.FormatInfo;
import com.nooie.sdk.device.bean.hub.HubInfo;
import com.suke.widget.SwitchButton;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GatewayInfoActivity extends BaseActivity implements GatewayInfoContract.View {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.livGatewayId)
    LabelTextItemView livGatewayId;
    @BindView(R.id.livGatewayStatus)
    LabelTextItemView livGatewayStatus;
    @BindView(R.id.livGatewayMac)
    LabelTextItemView livGatewayMac;
    @BindView(R.id.livGatewayClearUpSpace)
    LabelTextItemView livGatewayClearUpSpace;
    @BindView(R.id.livGatewayUpgrade)
    LabelTagItemView livGatewayUpgrade;
    @BindView(R.id.livGatewayLed)
    LabelSwItemView livGatewayLed;
    @BindView(R.id.livGatewaySyncTime)
    LabelTextItemView livGatewaySyncTime;

    private AlertDialog mClearUpSpaceDialog;
    private AlertDialog mRestartDialogOne;
    private AlertDialog mRestartDialogTwo;
    private AlertDialog mUnbindDialog;
    private Dialog mShowUpdatingWaringDialog;

    private GatewayInfoContract.Presenter mPresenter;
    private String mDeviceId;
    private NooieDevice mDeviceFirmware;

    public static void toGatewayInfoActivity(Context from, String deviceId) {
        Intent intent = new Intent(from, GatewayInfoActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gateway_info);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    private void initData() {
        new GatewayInfoPresenter(this);
        if (getCurrentIntent() != null) {
            mDeviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
        }
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        setupItemView();

        if (GatewayDeviceCache.getInstance().getCacheById(mDeviceId) != null) {
            GatewayDevice gatewayDevice = GatewayDeviceCache.getInstance().getCacheById(mDeviceId);
            refreshDeviceInfo(gatewayDevice);
        }
        refreshStorage(null);
    }

    private void setupItemView() {
        livGatewayId.displayLabelRight_1(View.VISIBLE);
        livGatewayStatus.displayLabelRight_1(View.VISIBLE).displayArrow(View.GONE);
        livGatewayStatus.setTag(ApiConstant.ONLINE_STATUS_ON);
        livGatewayMac.displayLabelRight_1(View.VISIBLE).displayArrow(View.GONE);
        livGatewayLed.displayLabelRightSw(View.VISIBLE);
        livGatewaySyncTime.displayLabelRight_1(View.VISIBLE).setLabelRight_Color_1(ContextCompat.getColor(this, R.color.theme_green)).setLabelRight_1(getString(R.string.cam_setting_sync_time)).displayArrow(View.GONE);
        livGatewayClearUpSpace.displayLabelRight_1(View.VISIBLE);
        livGatewayUpgrade.setLabelTitleTag(R.drawable.device_new_version_tag);
        livGatewayUpgrade.displayLabelRight_1(View.VISIBLE);
        //livGatewayUpgrade.setClickable(false);
        livGatewayUpgrade.setTag(false);

        livGatewayLed.setLabelRightSwListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (mPresenter != null) {
                    showLoading();
                    mPresenter.setLed(mDeviceId, isChecked);
                }
            }
        });

        livGatewaySyncTime.setLabelRightListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkGatewayOffLine()) {
                    return;
                }
                if (mPresenter != null) {
                    try {
                        showLoading();
                        float timeZone = DataHelper.toFloat(CountryUtil.getCurrentTimezone());
                        long currentTime = System.currentTimeMillis();
                        SimpleDateFormat formatStr = new SimpleDateFormat(DateTimeUtil.PATTERN_YMD_HMS_1);
                        /*
                        String utcStr = DateTimeUtil.localToUtc(currentTime, DateTimeUtil.PATTERN_YMD_HMS_1);
                        Date date = formatStr.parse(utcStr);
                        int timeOffset = (int)((currentTime - date.getTime()) / 1000L);
                        */
                        long networkTime = (currentTime + GlobalData.getInstance().getGapTime() * 1000L);
                        String localNetworkTimeStr = DateTimeUtil.localToUtc(networkTime, DateTimeUtil.PATTERN_YMD_HMS_1);
                        Date date1 = formatStr.parse(localNetworkTimeStr);
                        int timeOffset = (int)((currentTime - date1.getTime()) / 1000L);
                        mPresenter.setSyncTime(mDeviceId, 1, timeZone, timeOffset);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void refreshDeviceInfo(GatewayDevice device) {
        if (checkNull(device)) {
            return;
        }

        //tvTitle.setText(device.getName());
        tvTitle.setText(device.getUuid());
        livGatewayId.setLabelRight_1(device.getUuid());
        livGatewayStatus.setLabelRight_1(device.getOnline() == ApiConstant.ONLINE_STATUS_ON ? getString(R.string.online) : getString(R.string.offline));
        livGatewayStatus.setTag(device.getOnline());
        livGatewayMac.setLabelRight_1(device.getMac());
        livGatewayUpgrade.setLabelRight_1(device.getVersion());
    }

    private void refreshGatewayInfo(HubInfo hubInfo) {
        if (checkNull(livGatewayLed)) {
            return;
        }
        if (hubInfo != null) {
            if (hubInfo.led != livGatewayLed.isLabelRightSwCheck()) {
                livGatewayLed.toggleLabelRightSw();
            }
            refreshStorage(hubInfo.fmtInfo);
        }
    }

    private void refreshStorage(FormatInfo formatInfo) {
        if (checkNull(livGatewayClearUpSpace)) {
            return;
        }
        if (formatInfo == null) {
            livGatewayClearUpSpace.setLabelRight_1(getString(R.string.storage_no_sd_card));
            livGatewayClearUpSpace.setTag(ConstantValue.HUB_SD_STATUS_NO_SD);
            return;
        }

        int status = NooieDeviceHelper.compateSdStatus(formatInfo.getFormatStatus());
        double free = Math.floor((formatInfo.getFree() / 1024.0) * 10 + 0.5) / 10;
        double total = Math.floor((formatInfo.getTotal() / 1024.0) * 10 + 0.5) / 10;
        livGatewayClearUpSpace.setTag(status);
        if (status == ConstantValue.HUB_SD_STATUS_NORMAL) {
            free = formatInfo.getFree();
            total = formatInfo.getTotal();
            double useSpace = (total - free >= 0) && total != 0 ? Math.floor((((total - free) / total) * 100) * 10 + 0.5) / 10 : -1;
            refreshUseSpace(useSpace);
        } else if (status == ConstantValue.HUB_SD_STATUS_FORMATING) {
            StringBuilder processSb = new StringBuilder();
            processSb.append(getResources().getString(R.string.storage_formatting));
            processSb.append("(");
            processSb.append(formatInfo.getProgress());
            processSb.append("%)");
            livGatewayClearUpSpace.setLabelRight_1(processSb.toString());
        } else if (status == ConstantValue.HUB_SD_STATUS_DAMAGE) {
            livGatewayClearUpSpace.setLabelRight_1(getString(R.string.storage_sd_card_damaged));
        } else {
            livGatewayClearUpSpace.setLabelRight_1(getString(R.string.storage_no_sd_card));
        }
    }

    private void refreshUseSpace(double useSpace) {
        if (useSpace >= 0) {
            StringBuilder useSpaceSb = new StringBuilder();
            useSpaceSb.append(useSpace);
            useSpaceSb.append("%");
            livGatewayClearUpSpace.setLabelRight_1(String.format(getString(R.string.gateway_info_use_space), useSpaceSb.toString()));
        } else {
            livGatewayClearUpSpace.setLabelRight_1(String.format(getString(R.string.gateway_info_use_space), String.valueOf(0)));
        }
    }

    private void refreshFirmwareUpgrade(NooieDevice deviceInfo) {
        if (deviceInfo == null) {
            return;
        }

        boolean isNewVersion = deviceInfo.getAppVersionResult() != null && NooieDeviceHelper.compareVersion(deviceInfo.getAppVersionResult().getVersion_code(), deviceInfo.getAppVersionResult().getCurrentVersionCode()) > 0;
        StringBuilder versionSb = new StringBuilder();
        if (isNewVersion) {
            versionSb.append("V ");
            versionSb.append(deviceInfo.getAppVersionResult().getCurrentVersionCode());
            livGatewayUpgrade.displayLabelTitleTag(View.VISIBLE);
            //livGatewayUpgrade.setClickable(true);
            livGatewayUpgrade.setTag(true);
        } else {
            //versionSb.append(getString(R.string.gateway_info_is_newest_version));
            versionSb.append("V ");
            versionSb.append(deviceInfo.getAppVersionResult().getCurrentVersionCode());
            livGatewayUpgrade.displayLabelTitleTag(View.GONE);
            //livGatewayUpgrade.setClickable(false);
            livGatewayUpgrade.setTag(false);
        }
        livGatewayUpgrade.setLabelRight_1(versionSb.toString());
    }

    private boolean checkGatewayOffLine() {
        boolean isGatewayOffLine = livGatewayStatus != null && livGatewayStatus.getTag() != null && (Integer)livGatewayStatus.getTag() == ApiConstant.ONLINE_STATUS_OFF;
        if (isGatewayOffLine) {
            ToastUtil.showLongToast(this, R.string.gateway_info_offline_tip, 2500);
        }
        return isGatewayOffLine;
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeData();
    }

    private void resumeData() {
        if (mPresenter != null) {
            showLoading();
            mPresenter.loadDeviceInfo(mUserAccount, mDeviceId);
            mPresenter.getGatewayInfo(mDeviceId);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.stopQuerySDCardFormatState();
            mPresenter.destroy();
        }
        hideClearUpSpaceDialog();
        hideRestartDialogOne();
        hideRestartDialogTwo();
        hideUnbindDialog();
        hideShowUpdatingWarningDialog();
        hideClipboardDialog();
        releaseRes();
        release();
    }

    private void release() {
        if (livGatewayId != null) {
            livGatewayId.release();
            livGatewayId = null;
        }
        if (livGatewayStatus != null) {
            livGatewayStatus.release();
            livGatewayStatus = null;
        }
        if (livGatewayMac != null) {
            livGatewayMac.release();
            livGatewayMac = null;
        }
        if (livGatewayLed != null) {
            livGatewayLed.release();
            livGatewayLed = null;
        }
        if (livGatewaySyncTime != null) {
            livGatewaySyncTime.release();
            livGatewaySyncTime = null;
        }
        if (livGatewayClearUpSpace != null) {
            livGatewayClearUpSpace.release();
            livGatewayClearUpSpace = null;
        }
        if (livGatewayUpgrade != null) {
            livGatewayUpgrade.release();
            livGatewayUpgrade = null;
        }

        mDeviceFirmware = null;
    }

    @OnClick({R.id.ivLeft, R.id.livGatewayId, R.id.btnRestart, R.id.btnUnbind, R.id.livGatewayClearUpSpace, R.id.livGatewayUpgrade})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.livGatewayId:
                showClipboardDialog();
                break;
            case R.id.livGatewayClearUpSpace:
                if (checkGatewayOffLine()) {
                    break;
                }
                boolean isClearUpAble = livGatewayClearUpSpace != null && livGatewayClearUpSpace.getTag() != null && (Integer) livGatewayClearUpSpace.getTag() == ConstantValue.HUB_SD_STATUS_NORMAL;
                if (isClearUpAble) {
                    showClearUpSpaceDialog();
                }
                break;
            case R.id.btnRestart:
                if (checkGatewayOffLine()) {
                    break;
                }
                showRestartDialogOne();
                break;
            case R.id.btnUnbind:
                showUnbindDialog();
                break;
            case R.id.livGatewayUpgrade:
                if (checkGatewayOffLine()) {
                    break;
                }

                if (livGatewayUpgrade.getTag() == null || !(Boolean)livGatewayUpgrade.getTag()) {
                    ToastUtil.showToast(this, R.string.gateway_info_is_newest_version);
                    break;
                }

                if (mPresenter != null) {
                    showLoading();
                    mPresenter.checkDeviceUpdateStatus(mDeviceId);
                }
                break;
        }
    }

    @Override
    public void setPresenter(@NonNull GatewayInfoContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void notifyDeviceInfoResult(String result, NooieDevice deviceInfo) {
        if (isDestroyed()) {
            return;
        }
        hideLoading();
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result) && deviceInfo != null) {
            refreshDeviceInfo(deviceInfo.getGatewayDevice());
            refreshFirmwareUpgrade(deviceInfo);
            mDeviceFirmware = deviceInfo;
        }
    }

    @Override
    public void onGetGatewayInfoResult(String result, HubInfo hubInfo) {
        if (isDestroyed()) {
            return;
        }
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            refreshGatewayInfo(hubInfo);
        }
    }

    @Override
    public void onDeleteGatewayDeviceResult(String result) {
        if (isDestroyed()) {
            return;
        }
        hideLoading();
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            NooieDeviceHelper.sendRemoveDeviceBroadcast(ConstantValue.REMOVE_DEVICE_TYPE_GATEWAY,mDeviceId);
            finish();
        }
    }

    @Override
    public void onRestartGatewayDeviceResult(String result) {
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
    public void onClearDeviceUserSpaceResult(String result) {
        if (isDestroyed()) {
            return;
        }
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            hideLoading();
            if (mPresenter != null) {
                mPresenter.startQuerySDCardFormatState(mDeviceId);
            }
        }
    }

    @Override
    public void onQuerySDStatusSuccess(FormatInfo formatInfo, int status, String freeGB, String totalGB, int progress) {
        if (isDestroyed()) {
            return;
        }

        refreshStorage(formatInfo);
    }

    @Override
    public void onSetSyncTimeResult(String result) {
        if (isDestroyed()) {
            return;
        }

        hideLoading();
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
        } else {
            ToastUtil.showToast(this, R.string.get_fail);
        }
    }

    @Override
    public void onSetLedResult(String result) {
        if (isDestroyed()) {
            return;
        }

        hideLoading();
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
        } else {
            ToastUtil.showToast(this, R.string.get_fail);
        }
    }

    @Override
    public void onCheckDeviceUpdateStatus(String result, int type) {
        if (isDestroyed()) {
            return;
        }

        hideLoading();
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            if (NooieDeviceHelper.isDeviceUpdating(type)) {
                showUpdatingWarningDialog();
                return;
            }

            if (mDeviceFirmware != null && mDeviceFirmware.getAppVersionResult() != null) {
                GatewayFirmwareActivity.toGatewayFirmwareActivity(this, mDeviceId, mDeviceFirmware.getAppVersionResult().getCurrentVersionCode(), mDeviceFirmware.getAppVersionResult().getVersion_code(), mDeviceFirmware.getAppVersionResult().getLog(), mDeviceFirmware.getAppVersionResult().getKey(), mDeviceFirmware.getAppVersionResult().getMd5(), mDeviceFirmware.getAppVersionResult().getModel());
            }
        }
    }

    private void showUpdatingWarningDialog() {
        hideShowUpdatingWarningDialog();
        mShowUpdatingWaringDialog = DialogUtils.showInformationNormalDialog(this, getResources().getString(R.string.camera_settings_updating), getResources().getString(R.string.camera_settings_updating_tips), false, false, new DialogUtils.OnClickInformationDialogLisenter() {
            @Override
            public void onConfirmClick() {
            }
        });
    }

    private void hideShowUpdatingWarningDialog() {
        if (mShowUpdatingWaringDialog != null) {
            mShowUpdatingWaringDialog.dismiss();
            mShowUpdatingWaringDialog = null;
        }
    }

    private void showClearUpSpaceDialog() {
        hideClearUpSpaceDialog();
        mClearUpSpaceDialog = DialogUtils.showConfirmWithSubMsgDialog(this, R.string.gateway_info_clear_up_space, R.string.gateway_info_confirm_clear_up_space, R.string.cancel, R.string.confirm_upper, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                if (mPresenter != null) {
                    showLoading();
                    mPresenter.clearDeviceUseSpace(mDeviceId);
                }
            }

            @Override
            public void onClickLeft() {
            }
        });
    }

    private void hideClearUpSpaceDialog() {
        if (mClearUpSpaceDialog != null) {
            mClearUpSpaceDialog.dismiss();
            mClearUpSpaceDialog = null;
        }
    }

    private void showRestartDialogOne() {
        hideRestartDialogOne();
        mRestartDialogOne = DialogUtils.showConfirmWithSubMsgDialog(this, R.string.gateway_info_restart_title, R.string.gateway_info_restart_content_1, R.string.cancel, R.string.confirm_upper, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                showRestartDialogTwo();
            }

            @Override
            public void onClickLeft() {
            }
        });
    }

    private void hideRestartDialogOne() {
        if (mRestartDialogOne != null) {
            mRestartDialogOne.dismiss();
            mRestartDialogOne = null;
        }
    }

    private void showRestartDialogTwo() {
        mRestartDialogTwo = DialogUtils.showInformationDialog(this, getString(R.string.gateway_info_restart_title), getString(R.string.gateway_info_restart_content_2), getString(R.string.confirm_upper), true, false, new DialogUtils.OnClickInformationDialogLisenter() {
            @Override
            public void onConfirmClick() {
                if (mPresenter != null) {
                    showLoading();
                    mPresenter.restartDevice(mDeviceId);
                }
            }
        });
    }

    private void hideRestartDialogTwo() {
        if (mRestartDialogTwo != null) {
            mRestartDialogTwo.dismiss();
            mRestartDialogTwo = null;
        }
    }

    private void showUnbindDialog() {
        hideUnbindDialog();
        mUnbindDialog = DialogUtils.showConfirmWithSubMsgDialog(this, R.string.gateway_info_unbind, R.string.gateway_info_unbind_content, R.string.cancel, R.string.confirm_upper, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                GatewayDevice gatewayDevice = GatewayDeviceCache.getInstance().getCacheById(mDeviceId);
                if (mPresenter != null) {
                    showLoading();
                    boolean isRemoveFromServer = gatewayDevice != null && NooieDeviceHelper.compareVersion(gatewayDevice.getVersion(), ConstantValue.MIN_DEVICE_REMOVE_SELF_810_HUB) < 0;
                    if (isRemoveFromServer) {
                        mPresenter.unbindDevice(mDeviceId, mUid, mUserAccount);
                        return;
                    }
                    boolean isGatewayOffLine = livGatewayStatus != null && livGatewayStatus.getTag() != null && (Integer)livGatewayStatus.getTag() == ApiConstant.ONLINE_STATUS_OFF;
                    mPresenter.unbindDevice(mDeviceId, mUid, mUserAccount, !isGatewayOffLine);
                }
            }

            @Override
            public void onClickLeft() {
            }
        });
    }

    private void hideUnbindDialog() {
        if (mUnbindDialog != null) {
            mUnbindDialog.dismiss();
            mUnbindDialog = null;
        }
    }

    private Dialog mShowClipboardDialog;
    private void showClipboardDialog() {
        hideClipboardDialog();
        mShowClipboardDialog = DialogUtils.showInformationNormalDialog(this, getString(R.string.gateway_info_device_id), livGatewayId.getTextLabelRight_1(), false, new DialogUtils.OnClickInformationDialogLisenter() {
            @Override
            public void onConfirmClick() {
                if (getClipBoard() != null) {
                    getClipBoard().setPrimaryClip(ClipData.newPlainText("deviceId", livGatewayId.getTextLabelRight_1()));
                    ToastUtil.showToast(GatewayInfoActivity.this, R.string.copy_finish_tip);
                }
            }
        });
    }

    private void hideClipboardDialog() {
        if (mShowClipboardDialog != null) {
            mShowClipboardDialog.dismiss();
            mShowClipboardDialog = null;
        }
    }

    private ClipboardManager getClipBoard() {
        return (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
    }
}
