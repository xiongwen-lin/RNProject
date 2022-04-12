package com.afar.osaio.smart.electrician.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.afar.osaio.R;
import com.afar.osaio.application.activity.SmartThingActivity;
import com.afar.osaio.application.activity.WebViewActivity;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.electrician.bean.PowerStripName;
import com.afar.osaio.smart.electrician.eventbus.LampRenameEvent;
import com.afar.osaio.smart.electrician.manager.FamilyManager;
import com.afar.osaio.smart.electrician.manager.PowerStripHelper;
import com.afar.osaio.smart.electrician.presenter.ILampSettingPresenter;
import com.afar.osaio.smart.electrician.presenter.LampSettingPresenter;
import com.afar.osaio.smart.electrician.util.DialogUtil;
import com.afar.osaio.smart.electrician.util.ErrorHandleUtil;
import com.afar.osaio.smart.electrician.view.ILampSettingView;
import com.afar.osaio.smart.home.activity.HomeActivity;
import com.afar.osaio.smart.hybrid.webview.HybridWebViewActivity;
import com.afar.osaio.util.CommonUtil;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.widget.FButton;
import com.afar.osaio.widget.MediaPopupWindows;
import com.afar.osaio.widget.PhotoPopupWindows;
import com.nooie.common.utils.configure.FontUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.BindTyDeviceResult;
import com.tuya.smart.android.device.bean.UpgradeInfoBean;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.IDevListener;
import com.tuya.smart.sdk.api.IOtaListener;
import com.tuya.smart.sdk.api.ITuyaDevice;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.OTAErrorMessageBean;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * LampSettingActivity
 *
 * @author Administrator
 * @date 2019/8/20
 */
public class LampSettingActivity extends BaseActivity implements ILampSettingView {

    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.containerNickname)
    View containerNickname;
    @BindView(R.id.tvNickName)
    TextView tvNickName;
    @BindView(R.id.name_divider)
    View nameDivider;
    @BindView(R.id.containerSharing)
    View containerSharing;
    @BindView(R.id.share_divider)
    View shareDivider;
    @BindView(R.id.containerInformation)
    View containerInformation;
    @BindView(R.id.info_divider)
    View infoDivider;
    @BindView(R.id.containerFirmware)
    View containerFirmware;
    @BindView(R.id.containerContactUS)
    View containerContactUS;
    @BindView(R.id.contact_divider)
    View contactDivider;
    @BindView(R.id.tvFirmware)
    TextView tvFirmware;
    @BindView(R.id.tvCurrentFirmware)
    TextView tvCurrentFirmware;
    @BindView(R.id.firmware_divider)
    View firwareDivider;
    @BindView(R.id.btnDeviceRemove)
    FButton btnDeviceRemove;
    @BindView(R.id.tvResetFactory)
    TextView tvResetFactory;
    @BindView(R.id.containerThirdControl)
    View containerThirdControl;
    @BindView(R.id.llSupportAlexa)
    View llSupportAlexa;
    @BindView(R.id.llSupportGoogle)
    View llSupportGoogle;
    @BindView(R.id.llSupportSmartThing)
    View llSupportSmartThing;

    private ILampSettingPresenter mPresenter;
    private String mDeviceId;
    private UpgradeInfoBean mUpgradeInfo;
    private boolean isShare;
    private boolean isFirstUpgradeOnProcess;
    private ITuyaDevice mDevice;
    private DeviceBean mLampbean;
    private boolean isUpgradeFailure;
    private MediaPopupWindows mPopMenus;

    public static void toLampSettingActivity(Context from, String deviceId, Boolean isShare) {
        Intent intent = new Intent(from, LampSettingActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_IS_SHARE, isShare);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSlideable(false);
        setContentView(R.layout.activity_lamp_setting);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        initData();
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
            return;
        } else {
            mDeviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
            isShare = getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_DEVICE_IS_SHARE, false);
            initView();
            mPresenter = new LampSettingPresenter(this, mDeviceId);
            mPresenter.loadDeviceInfo(mDeviceId);
            registerDeviceListener();
            if (!isShare) {
                setupResetFactoryTv();
                setupUpgrade();
                mPresenter.loadOtaInfo();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        DeviceBean deviceBean = TuyaHomeSdk.getDataInstance().getDeviceBean(mDeviceId);
        if (mPresenter != null && deviceBean != null) {
            mPresenter.isSupportThirdParty(deviceBean.getProductId());
        }
    }

    private void registerDeviceListener() {
        mDevice = TuyaHomeSdk.newDeviceInstance(mDeviceId);
        mDevice.registerDevListener(new IDevListener() {
            @Override
            public void onDpUpdate(String devId, String dps) {

            }

            @Override
            public void onRemoved(String devId) {
                NooieLog.e("------------>> onRemoved devId " + devId);
                if (!isPause()) {
                    HomeActivity.toHomeActivity(LampSettingActivity.this, HomeActivity.TYPE_REMOVE_DEVICE);
                    finish();
                }
            }

            @Override
            public void onStatusChanged(String devId, boolean online) {

            }

            @Override
            public void onNetworkStatusChanged(String devId, boolean status) {

            }

            @Override
            public void onDevInfoUpdate(String devId) {
                NooieLog.e("------------>> onDevInfoUpdate devId " + devId);
                mPresenter.onDevInfoUpdate(devId);
            }
        });
    }

    @OnClick({R.id.ivLeft, R.id.containerNickname, R.id.containerSharing, R.id.containerInformation, R.id.tvFirmware, R.id.llSupportAlexa, R.id.llSupportGoogle,R.id.llSupportSmartThing,
            R.id.btnDeviceRemove, R.id.containerCreateGroup, R.id.containerContactUS})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ivLeft: {
                finish();
                break;
            }
            case R.id.containerNickname: {
                if (PowerStripHelper.getInstance().isLamp(mLampbean)) {
                    if (PowerStripHelper.getInstance().isLightModulator(mLampbean)) {
                        RenameDeviceActivity.toRenameDeviceActivity(LampSettingActivity.this, mDeviceId, ConstantValue.RENAME_MODULATOR, tvNickName.getText().toString());
                    } else if (PowerStripHelper.getInstance().isLampStrip(mLampbean)) {
                        RenameDeviceActivity.toRenameDeviceActivity(LampSettingActivity.this, mDeviceId, ConstantValue.RENAME_LIGHT_STRIP, tvNickName.getText().toString());
                    } else {
                        RenameDeviceActivity.toRenameDeviceActivity(LampSettingActivity.this, mDeviceId, ConstantValue.RENAME_LAMP, tvNickName.getText().toString());
                    }
                } else if (PowerStripHelper.getInstance().isPowerStrip(mLampbean)) {
                    PowerStripRenameActivity.toPowerStripRenameActivity(LampSettingActivity.this, mDeviceId, tvNickName.getText().toString(), "", ConstantValue.POWER_STRIP_RENAME);
                } else if (PowerStripHelper.getInstance().isWallSwitch(mLampbean)) {
                    RenameDeviceActivity.toRenameDeviceActivity(LampSettingActivity.this, mDeviceId, ConstantValue.RENAME_SWITCH, tvNickName.getText().toString());
                } else if (PowerStripHelper.getInstance().isPetFeeder(mLampbean)) {
                    RenameDeviceActivity.toRenameDeviceActivity(LampSettingActivity.this, mDeviceId, ConstantValue.RENAME_PETFEEDER, tvNickName.getText().toString());
                } else if (PowerStripHelper.getInstance().isAirPurifier(mLampbean)) {
                    RenameDeviceActivity.toRenameDeviceActivity(LampSettingActivity.this, mDeviceId, ConstantValue.RENAME_AIRPURIFIER, tvNickName.getText().toString());
                } else {
                    RenameDeviceActivity.toRenameDeviceActivity(LampSettingActivity.this, mDeviceId, ConstantValue.RENAME_PLUG, tvNickName.getText().toString());
                }

                break;
            }
            case R.id.containerSharing: {
                DeviceShareUsersActivity.toDeviceShareUsersActivity(LampSettingActivity.this, mDeviceId, FamilyManager.getInstance().getCurrentHomeId(), tvNickName.getText().toString(), 0);
                break;
            }
            case R.id.containerInformation: {
                DeviceInfoActivity.toDeviceInfoActivity(LampSettingActivity.this, mDeviceId);
                break;
            }
            case R.id.containerCreateGroup: {
                //CreateTeckinGroupActivity.toCreateTeckinGroupActivity(LampSettingActivity.this, mDeviceId);
                break;
            }
            case R.id.containerThirdControl: {
                break;
            }

            case R.id.tvFirmware: {
//                dealUpdate(mUpgradeInfo);
                if (mUpgradeInfo != null && mUpgradeInfo.getUpgradeStatus() == UpgradeInfoBean.UPGRADE_STATUS_READY) {
                    showUpgradeDialog();
                }
                break;
            }
            case R.id.llSupportAlexa: {
                WebViewActivity.toWebViewActivity(this, "file:///android_asset/html/alexa.html", getResources().getString(R.string.connect_to_alexa));
                break;
            }

            case R.id.llSupportGoogle: {
                WebViewActivity.toWebViewActivity(this, "file:///android_asset/html/google.html", getResources().getString(R.string.connect_to_assistant));
                break;
            }

            case R.id.llSupportSmartThing: {
                SmartThingActivity.toSmartThingActivity(this);
                break;
            }
            case R.id.btnDeviceRemove: {
                DialogUtil.showConfirmWithSubMsgDialog(this, R.string.remove_device, String.format(getResources().getString(R.string.device_remove_tip), tvNickName.getText().toString()), R.string.cancel, R.string.confirm_upper, new DialogUtil.OnClickConfirmButtonListener() {
                    @Override
                    public void onClickRight() {
                        if (!isShare) {
                            mPresenter.removeDevice();
                        } else {
                            mPresenter.removeShareDevice(mDeviceId);
                        }
                    }

                    @Override
                    public void onClickLeft() {

                    }
                });
                break;
            }
            case R.id.containerContactUS: {
                showPopMenu();
                break;
            }
        }
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
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/3041529699447693")));
                } catch (Exception e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/groups/porik")));
                }
            }

            @Override
            public void onYoutubeClick() {
                try {
                    getPackageManager().getPackageInfo("com.google.android.youtube", 0);
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("youtube://www.youtube.com/channel/UCHvXyEdQGyPv5XD_n31pQiw")));
                } catch (Exception e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/channel/UCHvXyEdQGyPv5XD_n31pQiw")));
                }
            }

            @Override
            public void onInstagramClick() {
                try {
                    getPackageManager().getPackageInfo("com.instagram.android", 0);
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("instagram://user?username=porik_official")));
                } catch (Exception e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/porik_official/")));
                }
            }

            @Override
            public void onEmailClick() {
                StringBuilder mailToSb = new StringBuilder();
                mailToSb.append("mailto:");
                mailToSb.append(getString(R.string.porik_email));
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

    private void setupUpgrade() {
        mPresenter.registerOtaListener(new IOtaListener() {
            @Override
            public void onSuccess(int otaType) {
                Log.e("monitor_success", "=========================================>onSuccess");
                mPresenter.loadOtaInfo();
            }

            @Override
            public void onFailure(int otaType, String code, String error) {
                Log.e("monitor_failure", "=========================================>onFailure---error" + error);
                if (!isUpgradeFailure) {
                    isUpgradeFailure = true;
                    mPresenter.loadOtaInfo();
                    ErrorHandleUtil.toastTuyaError(LampSettingActivity.this, error);
                }
            }

            @Override
            public void onFailureWithText(int otaType, String code, OTAErrorMessageBean messageBean) {

            }

            @Override
            public void onProgress(int otaType, int progress) {
                Log.e("monitor_progress", "=========================================>升级到=====" + progress + "%");
                if (!isFirstUpgradeOnProcess) {
                    isFirstUpgradeOnProcess = true;
                    mPresenter.loadOtaInfo();
                }
            }

            @Override
            public void onTimeout(int otaType) {

            }

            @Override
            public void onStatusChanged(int otaStatus, int otaType) {

            }
        });
    }

    @Override
    public void notifyLoadDeviceInfo(DeviceBean device) {
        if (device != null) {
            mLampbean = device;
            tvNickName.setText(device.getName());
            if (!PowerStripHelper.getInstance().isPlug(device)) {
                containerContactUS.setVisibility(View.GONE);
                contactDivider.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void notifyRemoveDeviceState(String msg) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(msg)) {
            HomeActivity.toHomeActivity(LampSettingActivity.this, HomeActivity.TYPE_REMOVE_DEVICE);
            finish();
        } else {
            ErrorHandleUtil.toastTuyaError(this, msg);
        }
    }

    @Override
    public void notifyLoadOtaInfoSuccess(UpgradeInfoBean upgradeInfo) {
        if (upgradeInfo != null) {
            mUpgradeInfo = upgradeInfo;
            switch (upgradeInfo.getUpgradeStatus()) {
                case UpgradeInfoBean.UPGRADE_STATUS_DEFAULT: {
                    tvFirmware.setText(upgradeInfo.getCurrentVersion());
                    break;
                }
                case UpgradeInfoBean.UPGRADE_STATUS_READY: {
                    tvFirmware.setText(R.string.update);
                    tvCurrentFirmware.setText(mUpgradeInfo.getCurrentVersion());
                    tvCurrentFirmware.setVisibility(View.VISIBLE);
                    break;
                }
                case UpgradeInfoBean.UPGRADE_STATUS_UPGRADING: {
                    hideLoadingDialog();
                    tvCurrentFirmware.setVisibility(View.GONE);
                    tvFirmware.setText(R.string.updating);
                    break;
                }
            }
        }
    }

    private void showUpgradeDialog() {
        DialogUtil.showConfirmWithSubMsgDialog(LampSettingActivity.this, R.string.firmware_update_title, String.format(getResources().getString(R.string.firmware_update_tip), mUpgradeInfo.getVersion()), R.string.ignore, R.string.upgrade_upper, new DialogUtil.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                mPresenter.startOta();
                showLoadingDialog();
                isUpgradeFailure = false;
            }

            @Override
            public void onClickLeft() {

            }
        });
    }

    @Override
    public void notifyLoadOtaInfoFailed(String msg) {
        ErrorHandleUtil.toastTuyaError(this, msg);
    }

    @Override
    public void notifyResetFactory(String msg) {
        hideLoadingDialog();
        if (ConstantValue.SUCCESS.equalsIgnoreCase(msg)) {
            HomeActivity.toHomeActivity(this, HomeActivity.TYPE_REMOVE_DEVICE);
            finish();
        } else {
            ErrorHandleUtil.toastTuyaError(this, msg);
        }
    }

    @Override
    public void notifyOnDevInfoUpdateState(DeviceBean deviceBean) {
        tvNickName.setText(deviceBean.getName());
    }

    @Override
    public void notifyloadSwitchNameSuccess(List<PowerStripName> powerStripNames) {

    }

    @Override
    public void notifyloadSwitchNameFail(String errorCode, String errorMsg) {

    }

    @Override
    public void notifyShowThirdParty(BindTyDeviceResult.BindTyDevice bindTyDevice) {
        if (bindTyDevice != null) {
            boolean isAlexa = bindTyDevice.getIs_alexa() == ApiConstant.THIRD_PARTY_CONTROL_SUPPORT;
            boolean isGoogleAssistant = bindTyDevice.getIs_google() == ApiConstant.THIRD_PARTY_CONTROL_SUPPORT;
            boolean isSmartThing = bindTyDevice.getIs_smart_thing() == ApiConstant.THIRD_PARTY_CONTROL_SUPPORT;
            if (isAlexa || isGoogleAssistant || isSmartThing) {
                containerThirdControl.setVisibility(View.VISIBLE);
                llSupportAlexa.setVisibility(isAlexa ? View.VISIBLE : View.GONE);
                llSupportGoogle.setVisibility(isGoogleAssistant ? View.VISIBLE : View.GONE);
                llSupportSmartThing.setVisibility(isSmartThing ? View.VISIBLE : View.GONE);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDevice != null) {
            mDevice.unRegisterDevListener();
        }
        if (!isShare) {
            mPresenter.release();
        }
        EventBus.getDefault().unregister(this);
    }

    private void initView() {
        tvTitle.setText(R.string.settings);
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        setupViewVisible(isShare ? View.GONE : View.VISIBLE);
    }

    private void setupResetFactoryTv() {
        final SpannableStringBuilder style = new SpannableStringBuilder();
        String factorySeting = getString(R.string.factory_settings);
        String text = String.format(getString(R.string.reset_factory_settings), factorySeting);
        style.append(text);
        //设置部分文字点击事件
        ClickableSpan conditionClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                showResetFactoryDialog();
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setTypeface(FontUtil.loadTypeface(getApplicationContext(), "fonts/manrope-semibold.otf"));
                ds.setUnderlineText(false);
            }
        };
        style.setSpan(conditionClickableSpan, text.indexOf(factorySeting), text.indexOf(factorySeting) + factorySeting.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvResetFactory.setText(style);
        ForegroundColorSpan conditionForegroundColorSpan = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.theme_green_subtext_color));
        style.setSpan(conditionForegroundColorSpan, text.indexOf(factorySeting), text.indexOf(factorySeting) + factorySeting.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvResetFactory.setMovementMethod(LinkMovementMethod.getInstance());
        tvResetFactory.setText(style);
    }

    private void showResetFactoryDialog() {
        DialogUtil.showConfirmWithSubMsgDialog(this, getResources().getString(R.string.device_settings_reset),
                getResources().getString(R.string.device_settings_reset_info), R.string.cancel, R.string.confirm_upper, new DialogUtil.OnClickConfirmButtonListener() {
                    @Override
                    public void onClickRight() {
                        showLoadingDialog();
                        mPresenter.resetFactory();
                    }

                    @Override
                    public void onClickLeft() {

                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLampRenameEvent(LampRenameEvent event) {
        tvNickName.setText(event.getNewName());
    }

    private void setupViewVisible(int visible) {
        containerNickname.setVisibility(visible);
        nameDivider.setVisibility(visible);
        containerSharing.setVisibility(visible);
        shareDivider.setVisibility(visible);
        containerInformation.setVisibility(visible);
        infoDivider.setVisibility(visible);
        containerFirmware.setVisibility(visible);
        firwareDivider.setVisibility(visible);
        tvResetFactory.setVisibility(visible);
    }
}
