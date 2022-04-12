package com.afar.osaio.smart.setting.activity;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.BuildConfig;
import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.bean.CurrentDeviceParam;
import com.afar.osaio.bean.ShortLinkDeviceParam;
import com.afar.osaio.smart.cache.BleApDeviceInfoCache;
import com.afar.osaio.smart.mixipc.activity.ChangeDevicePasswordActivity;
import com.nooie.common.utils.notify.NotificationUtil;
import com.nooie.sdk.bean.IpcType;
import com.afar.osaio.smart.device.bean.DeviceCfg;
import com.afar.osaio.smart.device.bean.NooieDevice;
import com.afar.osaio.smart.scan.activity.RenameDeviceActivity;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.home.activity.HomeActivity;
import com.afar.osaio.smart.setting.presenter.INooieDeviceInfoPresenter;
import com.afar.osaio.smart.setting.presenter.NooieDeviceInfoPresenter;
import com.afar.osaio.smart.setting.view.INooieDeviceInfoView;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.util.Util;
import com.afar.osaio.widget.LabelTextItemView;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.network.IPv4IntTransformer;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.AppVersionResult;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.db.entity.BleApDeviceEntity;
import com.nooie.sdk.device.bean.DevInfo;
import com.nooie.sdk.device.bean.hub.CameraInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * NooieDeviceInfoActivity
 *
 * @author Administrator
 * @date 2019/4/19
 */
public class NooieDeviceInfoActivity extends BaseActivity implements INooieDeviceInfoView {
    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.tvOwner)
    TextView tvOwner;
    @BindView(R.id.tvModel)
    TextView tvModel;
    @BindView(R.id.tvDeviceId)
    TextView tvDeviceId;
    @BindView(R.id.tvIp)
    TextView tvIp;
    @BindView(R.id.tvMac)
    TextView tvMac;
    @BindView(R.id.containerFirmware)
    ConstraintLayout containerFirmware;
    @BindView(R.id.tvCameraAlias)
    TextView tvCameraAlias;
    @BindView(R.id.tvFwVersion)
    TextView tvFwVersion;
    @BindView(R.id.tvUpdateState)
    TextView tvUpdateState;
    @BindView(R.id.tvUpdaterRetry)
    TextView tvUpdaterRetry;
    @BindView(R.id.livDeviceBattery)
    LabelTextItemView livDeviceBattery;
    @BindView(R.id.ivNameNext)
    ImageView ivNameNext;
    @BindView(R.id.livDeviceBluetoothName)
    LabelTextItemView livDeviceBluetoothName;
    @BindView(R.id.livDeviceSsid)
    LabelTextItemView livDeviceSsid;
    @BindView(R.id.livDeviceHotSpotPw)
    LabelTextItemView livDeviceHotSpotPw;
    @BindView(R.id.btnDeviceRestore)
    TextView btnDeviceRestore;

    private static final int RENAME_REQUEST_CODE = 0x10;

    private String mDeviceId;
    private INooieDeviceInfoPresenter mDeviceInfoPresenter;

    private boolean mIsMyDevice = false;
    private String mDeviceModel;
    private String mCurrentVersion;
    private String mNewVersion;
    private String mPackageKey;
    private String mMd5;
    private String mUpdateTipLog = "";
    private boolean mIsStartDeviceUpdated = false;
    private boolean mIsRefreshDevice = false;
    private boolean mIsSubDevice = false;
    private boolean mIsLpDevice = false;
    private int mConnectionMode;
    private Dialog mShowUpdateDialog;
    private Dialog mShowUpdateConfirmDialog;
    private Dialog mShowClipboardDialog;

    public static void toNooieDeviceInfoActivity(Context from, String deviceId, int type, boolean isSubDevice, boolean isLpDevice, int connectionMode, String model) {
        Intent intent = new Intent(from, NooieDeviceInfoActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_TYPE, type);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_PARAM_1, isSubDevice);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_PARAM_2, isLpDevice);
        intent.putExtra(ConstantValue.INTENT_KEY_CONNECTION_MODE, connectionMode);
        intent.putExtra(ConstantValue.INTENT_KEY_IPC_MODEL, model);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_info);
        ButterKnife.bind(this);

        initView();
        initData();
        loadDeviceInfo();
        //mDeviceInfoPresenter.loadFirmwareVersion(mDeviceId);
        shortLinkDeviceInit();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.camera_settings_cam_info);
        tvTitle.setTextColor(getResources().getColor(R.color.theme_text_color));
        ivRight.setVisibility(View.GONE);

        tvUpdateState.setVisibility(View.GONE);
        tvUpdateState.setTag(ApiConstant.DEVICE_UPDATE_TYPE_UNKNOWN);
        livDeviceBattery.displayLabelRight_1(View.VISIBLE).displayArrow(View.GONE);
        livDeviceBluetoothName.displayLabelRight_1(View.VISIBLE).displayArrow(View.GONE);
        livDeviceSsid.displayLabelRight_1(View.VISIBLE).displayArrow(View.GONE);
        livDeviceHotSpotPw.displayLabelRight_1(View.GONE).displayArrow(View.VISIBLE);
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
        } else {
            mDeviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
            mIsSubDevice = getCurrentIntent() != null && getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_DATA_PARAM_1, false);
            mIsLpDevice = getCurrentIntent() != null && getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_DATA_PARAM_2, false);
            mConnectionMode = getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_QC);
            mDeviceInfoPresenter = new NooieDeviceInfoPresenter(this);
            BindDevice device = getDevice(mConnectionMode);
            if (device != null) {
                updateUI(device);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        tryResumeData();
        //registerShortLinkKeepListener();
    }

    @Override
    public void onPause() {
        super.onPause();
        //unRegisterShortLinkKeepListener();
    }

    private void loadDeviceInfo() {
        if (!TextUtils.isEmpty(mDeviceId) && mDeviceInfoPresenter != null) {
            DeviceCfg deviceCfg = new DeviceCfg(mDeviceId, "", IpcType.getIpcType(mDeviceModel), (mIsMyDevice ? ApiConstant.BIND_TYPE_OWNER : ApiConstant.BIND_TYPE_SHARE), mConnectionMode, mIsSubDevice);
            mDeviceInfoPresenter.loadInfos(deviceCfg);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDeviceInfoPresenter != null) {
            mDeviceInfoPresenter.stopQueryDeviceUpdateState();
            mDeviceInfoPresenter.stopUpdateProcessTask();
            mDeviceInfoPresenter.stopQueryUpgradeForTimeout();
            mDeviceInfoPresenter.destroy();
            mDeviceInfoPresenter = null;
        }
        hideClipboardDialog();
        hideUpdateDialog();
        hideShowUpdateConfirmDialog();
        release();
    }

    private void release() {
        ivLeft = null;
        tvTitle = null;
        ivRight = null;
        tvOwner = null;
        tvModel = null;
        tvDeviceId = null;
        tvIp = null;
        tvMac = null;
        containerFirmware = null;
        tvUpdateState = null;
        tvUpdaterRetry = null;
        if (livDeviceBattery != null) {
            livDeviceBattery.release();
            livDeviceBattery = null;
        }
        if (livDeviceBluetoothName != null) {
            livDeviceBluetoothName.release();
            livDeviceBluetoothName = null;
        }
        if (livDeviceSsid != null) {
            livDeviceSsid.release();
            livDeviceSsid = null;
        }
        if (livDeviceHotSpotPw != null) {
            livDeviceHotSpotPw.release();
            livDeviceHotSpotPw = null;
        }
        mDeviceModel = null;
        mCurrentVersion = null;
        mNewVersion = null;
        mPackageKey = null;
        mMd5 = null;
        mUpdateTipLog = null;
        btnDeviceRestore = null;
        ivNameNext = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && resultCode == RESULT_OK) {
            switch (requestCode) {
                case RENAME_REQUEST_CODE:
                    String name = data.getStringExtra(ConstantValue.INTENT_KEY_DEVICE_NAME);
                    if (!TextUtils.isEmpty(name) && !name.equals(tvCameraAlias.getText().toString())) {
                        tvCameraAlias.setText(name);
                    }
                    break;
            }
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (mIsStartDeviceUpdated && keyCode == KeyEvent.KEYCODE_BACK) {
            //setIsForceDisconnectShortLinkDevice(true);
            checkIsNeedToDisconnectShortLinkDevice();
            HomeActivity.toHomeActivity(this);
            finish();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @OnClick({R.id.ivLeft, R.id.tvUpdateState, R.id.containerDeviceId, R.id.containerName, R.id.containerFirmware, R.id.tvDeviceIdCopy, R.id.livDeviceHotSpotPw, R.id.btnDeviceRestore})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                setIsGotoOtherPage(true);
                if (mIsStartDeviceUpdated) {
                    //setIsForceDisconnectShortLinkDevice(true);
                    checkIsNeedToDisconnectShortLinkDevice();
                    HomeActivity.toHomeActivity(this);
                }
                finish();
                break;
            case R.id.containerName:
                if (isDeviceNameEditable()) {
                    BindDevice device = getDevice(mConnectionMode);
                    String model = device != null ? device.getType() : mDeviceModel;
                    Bundle param = new Bundle();
                    param.putString(ConstantValue.INTENT_KEY_DEVICE_ID, mDeviceId);
                    param.putString(ConstantValue.INTENT_KEY_DEVICE_NAME, tvCameraAlias.getText().toString().trim());
                    param.putString(ConstantValue.INTENT_KEY_IPC_MODEL, model);
                    param.putInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, mConnectionMode);
                    RenameDeviceActivity.toRenameDeviceActivity(this, param, RENAME_REQUEST_CODE);
                }
                break;
            case R.id.tvUpdateState:
            //case R.id.containerFirmware:
                if (isCanUpdate()) {
                    showUpdateDialog();
                }
                break;
            case R.id.tvUpdaterRetry:
                break;
            case R.id.containerDeviceId:
                showClipboardDialog();
                break;
            case R.id.tvDeviceIdCopy:
                if (getClipBoard() != null) {
                    getClipBoard().setPrimaryClip(ClipData.newPlainText("deviceId", tvDeviceId.getText().toString().trim()));
                    ToastUtil.showToast(NooieDeviceInfoActivity.this, R.string.copy_finish_tip);
                }
                break;
            case R.id.livDeviceHotSpotPw: {
                BleApDeviceEntity bleApDeviceEntity = BleApDeviceInfoCache.getInstance().getBleApDeviceEntityByDeviceId(mDeviceId);
                String ssid = bleApDeviceEntity != null ? bleApDeviceEntity.getSsid() : "";
                String bleDeviceId = bleApDeviceEntity != null ? bleApDeviceEntity.getBleDeviceId() : "";
                Bundle param = new Bundle();
                param.putString(ConstantValue.INTENT_KEY_DEVICE_ID, mDeviceId);
                param.putInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, mConnectionMode);
                param.putString(ConstantValue.INTENT_KEY_IPC_MODEL, getDeviceModel());
                param.putString(ConstantValue.INTENT_KEY_SSID, ssid);
                param.putString(ConstantValue.INTENT_KEY_BLE_DEVICE, bleDeviceId);
                ChangeDevicePasswordActivity.toModifyCameraPasswordActivity(this, param);
                setIsGotoOtherPage(true);
                break;
            }
            case R.id.btnDeviceRestore:
                setIsGotoOtherPage(true);
                BindDevice device = getDevice(mConnectionMode);
                String pDeviceId = device != null ? device.getPuuid() : "";
                DeviceTestToolActivity.toDeviceTestToolActivity(this, mDeviceId, pDeviceId);
                break;
        }
    }

    private boolean isDeviceNameEditable() {
        return mIsMyDevice;
    }

    private boolean isCanUpdate() {
        boolean isUpdating = tvUpdateState.getTag() != null && ((Integer)tvUpdateState.getTag() == ApiConstant.DEVICE_UPDATE_TYPE_DOWNLOAD_START
                || ((Integer)tvUpdateState.getTag() == ApiConstant.DEVICE_UPDATE_TYPE_DOWNLOAD_FINISH) || (Integer)tvUpdateState.getTag() == ApiConstant.DEVICE_UPDATE_TYPE_INSTALL_START
                || (Integer)tvUpdateState.getTag() == ApiConstant.DEVICE_UPDATE_TYPE_UPDATE_SUCCESS || (Integer)tvUpdateState.getTag() == ApiConstant.DEVICE_UPDATE_TYPE_UPATE_FINISH);
        return !isUpdating && mIsMyDevice;
    }

    private void showUpdateDialog() {
        hideUpdateDialog();
        String message = String.format(getString(R.string.camera_settings_cam_firmware_update_info), mNewVersion, mUpdateTipLog);
        mShowUpdateDialog = DialogUtils.showUpdatesDialog(NooieDeviceInfoActivity.this, getString(R.string.camera_settings_cam_firmware_update), message, getString(R.string.cancel), getString(R.string.next_upper), new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                showUpdateConfirmDialog();
            }

            @Override
            public void onClickLeft() {
            }
        }, null);
    }

    private void hideUpdateDialog() {
        if (mShowUpdateDialog != null) {
            mShowUpdateDialog.dismiss();
            mShowUpdateDialog = null;
        }
    }

    private void showUpdateConfirmDialog() {
        hideShowUpdateConfirmDialog();
        mShowUpdateConfirmDialog = DialogUtils.showConfirmWithSubMsgDialog(this, R.string.camera_settings_cam_firmware_update, R.string.camera_settings_cam_update_confirm_reset_info, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                if (isUpdateInfoValid()) {
                    mDeviceInfoPresenter.startUpdateDevice(mUserAccount, mDeviceId, mDeviceModel, mNewVersion, mPackageKey, mMd5, mIsSubDevice);
                    refreshNooieUpdateState(ApiConstant.DEVICE_UPDATE_TYPE_DOWNLOAD_START, 0);
                } else {
                    ToastUtil.showToast(NooieDeviceInfoActivity.this, R.string.get_fail);
                }
            }

            @Override
            public void onClickLeft() {
            }
        });
    }

    private void hideShowUpdateConfirmDialog() {
        if (mShowUpdateConfirmDialog != null) {
            mShowUpdateConfirmDialog.dismiss();
            mShowUpdateConfirmDialog = null;
        }
    }

    private void showClipboardDialog() {
        hideClipboardDialog();
        mShowClipboardDialog = DialogUtils.showInformationDialog(this, getString(R.string.camera_settings_cam_info_device_id), tvDeviceId.getText().toString().trim(), false);
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

    @Override
    public void onLoadDeviceSuccess(NooieDevice device) {
        if (isDestroyed() || checkNull(device)) {
            return;
        }
        BindDevice bindDevice = device != null ? device.getDevice() : null;
        if (bindDevice != null) {
            updateUI(bindDevice);
        }

        AppVersionResult appVersionResult = device != null ? device.getAppVersionResult() : null;
        onLoadFirmwareInfoSuccess(appVersionResult);

        boolean isCanUpdate = (appVersionResult != null && NooieDeviceHelper.compareVersion(appVersionResult.getVersion_code(), appVersionResult.getCurrentVersionCode()) > 0) && isCanUpdate() && isUpdateInfoValid();
        if (getCurrentIntent() != null && getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_DATA_TYPE, ConstantValue.CAM_INFO_TYPE_NORMAL) == ConstantValue.CAM_INFO_TYPE_DIRECT && isCanUpdate) {
            showUpdateConfirmDialog();
        }
    }

    private boolean isUpdateInfoValid() {
        boolean isUpdate = !TextUtils.isEmpty(mDeviceId) && !TextUtils.isEmpty(mDeviceModel) && !TextUtils.isEmpty(mNewVersion) && !TextUtils.isEmpty(mPackageKey);
        isUpdate = mIsLpDevice ? (isUpdate && !TextUtils.isEmpty(mMd5)) : isUpdate;
        return isUpdate;
    }

    private void updateUI(BindDevice device) {
        if (checkNull(tvModel, tvCameraAlias, tvOwner, tvDeviceId, tvFwVersion, tvIp, tvMac, livDeviceBattery, ivNameNext, livDeviceBluetoothName, livDeviceSsid, livDeviceHotSpotPw)) {
            return;
        }
        boolean isRechargeableDevice = false;
        boolean isShowBleApInfo = mConnectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT && NooieDeviceHelper.isSupportBluetooth(getDeviceModel());
        if (device != null) {
            tvModel.setText(NooieDeviceHelper.convertModelToString(device.getType()));
            tvCameraAlias.setText(device.getName());
            tvOwner.setText(device.getNickname());
            tvDeviceId.setText(convertDeviceIdText(device.getUuid()));
            tvFwVersion.setText(device.getVersion());
            tvIp.setText(IPv4IntTransformer.bigNumToIP(device.getLocal_ip()));
            tvMac.setText(device.getMac());
            mIsMyDevice = device.getBind_type() == ApiConstant.BIND_TYPE_OWNER;
            mCurrentVersion = device.getVersion();
            //NooieLog.d("-->> debug NooieDeviceInfoActivity updateUI: battery=" + NooieDeviceHelper.computeBattery(IpcType.MC120, device.getBattery_level()));
            livDeviceBattery.setLabelRight_1(getDeviceBattery(device.getType(), device.getBattery_level()));
            isRechargeableDevice = NooieDeviceHelper.isRechargeableDevice(device.getType());
            isShowBleApInfo = mConnectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT && NooieDeviceHelper.isSupportBluetooth(device.getType());
        }
        ivNameNext.setVisibility(isDeviceNameEditable() ? View.VISIBLE : View.INVISIBLE);
        livDeviceBattery.setVisibility(mIsLpDevice || isRechargeableDevice ? View.VISIBLE : View.GONE);
        livDeviceBluetoothName.setVisibility(isShowBleApInfo ? View.VISIBLE : View.GONE);
        livDeviceSsid.setVisibility(isShowBleApInfo ? View.VISIBLE : View.GONE);
        livDeviceHotSpotPw.setVisibility(isShowBleApInfo ? View.VISIBLE : View.GONE);
        if (isShowBleApInfo) {
            BleApDeviceEntity bleApDeviceEntity = BleApDeviceInfoCache.getInstance().getBleApDeviceEntityByDeviceId(mDeviceId);
            String ssid = bleApDeviceEntity != null ? bleApDeviceEntity.getSsid() : "";
            livDeviceBluetoothName.setLabelRight_1(ssid);
            livDeviceSsid.setLabelRight_1(ssid);
        }
        btnDeviceRestore.setVisibility(mIsMyDevice && NooieApplication.LOG_MODE && BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
        List<Integer> hideViewIds = new ArrayList<>();
        if (mConnectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            //int[] viewIds = {R.id.containerName, R.id.view3, R.id.view4, R.id.view5, R.id.containerFirmware, R.id.view7, R.id.view8, R.id.view9, R.id.view10, R.id.view11};
            if (NooieDeviceHelper.mergeIpcType(getDeviceModel()) == IpcType.HC320) {
                int[] viewIds = {R.id.view4, R.id.view5, R.id.view7, R.id.view8, R.id.view9, R.id.view10, R.id.view11};
                for (int i = 0; i < viewIds.length; i++) {
                    if (!hideViewIds.contains(viewIds[i])) {
                        hideViewIds.add(viewIds[i]);
                    }
                }
            } else {
                int[] viewIds = {R.id.containerName, R.id.view3, R.id.view4, R.id.view5, R.id.containerFirmware, R.id.view7, R.id.view8, R.id.view9, R.id.view10, R.id.view11};
                for (int i = 0; i < viewIds.length; i++) {
                    if (!hideViewIds.contains(viewIds[i])) {
                        hideViewIds.add(viewIds[i]);
                    }
                }
            }

        }
        for (Integer hideViewId : CollectionUtil.safeFor(hideViewIds)) {
            if (findViewById(hideViewId) != null) {
                findViewById(hideViewId).setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onLoadDeviceFailed(String msg) {
        if (isDestroyed()) {
            return;
        }
    }

    @Override
    public void onGetDeviceInfo(String result, DevInfo devInfo) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result) && devInfo != null) {
            BindDevice device = getDevice(mConnectionMode);
            if (device != null) {
                device.setUuid(TextUtils.isEmpty(devInfo.uuid) ? device.getUuid() : devInfo.uuid);
                device.setType(TextUtils.isEmpty(devInfo.model) ? device.getType() : devInfo.model);
                device.setModel(TextUtils.isEmpty(devInfo.model) ? device.getModel() : devInfo.model);
                device.setBattery_level(devInfo.battery);
                updateUI(device);
            }
        }
    }

    @Override
    public void onLoadFirmwareInfoSuccess(AppVersionResult result) {
        if (isDestroyed()) {
            return;
        }
        if (result != null) {
            boolean isShowUpdate = Util.convertDeviceVersion(mCurrentVersion) != 0 && Util.convertDeviceVersion(mCurrentVersion) < Util.convertDeviceVersion(result.getVersion_code())
                    && checkDeviceActive(getDevice(mConnectionMode));
            if (isShowUpdate) {
                refreshNooieUpdateState(ApiConstant.DEVICE_UPDATE_TYPE_UPDATABLE, 0);
                mDeviceModel = result.getModel();
                mNewVersion = result.getVersion_code();
                mPackageKey = result.getKey();
                mMd5 = result.getMd5();
                mUpdateTipLog = result.getLog();
            }
        }

        if (mIsMyDevice && mDeviceInfoPresenter != null) {
            mDeviceInfoPresenter.queryDeviceUpgradeTime(mDeviceId, mUserAccount, false);
        }
    }

    @Override
    public void onLoadFirmwareInfoFailed(String msg) {
        if (isDestroyed()) {
            return;
        }
    }

    @Override
    public void onQueryNooieDeviceUpdateStatusSuccess(int type, int process) {
        if (isDestroyed()) {
            return;
        }

        refreshNooieUpdateState(type,process);
    }

    private void refreshNooieUpdateState(int type, int process) {
        if (checkNull(tvUpdateState, tvFwVersion)) {
            return;
        }
        String updateStateTitle = tvFwVersion.getText().toString();
        boolean showUpdate = tvUpdateState.getVisibility() == View.VISIBLE ? true : false;
        tvUpdateState.setTag(type);
        switch (type) {
            case ApiConstant.DEVICE_UPDATE_TYPE_UPDATABLE: {
                showUpdate = true;
                tvUpdateState.setText(R.string.camera_settings_update);
                break;
            }
            case ApiConstant.DEVICE_UPDATE_TYPE_NORMAL: {
                break;
            }
            case ApiConstant.DEVICE_UPDATE_TYPE_DOWNLOAD_START: {
                updateStateTitle = getUpdateProcessText(getResources().getString(R.string.camera_settings_downloading), process);
                showUpdate = false;
                break;
            }
            case ApiConstant.DEVICE_UPDATE_TYPE_DOWNLOAD_FINISH: {
                updateStateTitle = getUpdateProcessText(getResources().getString(R.string.camera_settings_downloaded), process);
                showUpdate = false;
                break;
            }
            case ApiConstant.DEVICE_UPDATE_TYPE_INSTALL_START: {
                updateStateTitle = getUpdateProcessText(getResources().getString(R.string.camera_settings_installing), process);
                showUpdate = false;
                break;
            }
            case ApiConstant.DEVICE_UPDATE_TYPE_UPDATE_SUCCESS: {
                updateStateTitle = getUpdateProcessText(getResources().getString(R.string.camera_settings_update_success), process);
                showUpdate = false;
                break;
            }
            case ApiConstant.DEVICE_UPDATE_TYPE_UPDATE_FAILED: {
                updateStateTitle = getResources().getString(R.string.camera_settings_update_fail);
                showUpdate = true;
                tvUpdateState.setText(R.string.camera_settings_update_retry);
                break;
            }
            case ApiConstant.DEVICE_UPDATE_TYPE_UPATE_FINISH: {
                updateStateTitle = getResources().getString(R.string.camera_settings_update_finish);
                showUpdate = false;
                ToastUtil.showToast(this, R.string.upgrade_success);
                refreshDeviceInfo();
                break;
            }
            default: {
                updateStateTitle = "";
                showUpdate = false;
                break;
            }
        }

        tvUpdateState.setVisibility(showUpdate ? View.VISIBLE : View.GONE);
        tvFwVersion.setText(updateStateTitle);
        if (!mIsMyDevice) {
            tvUpdateState.setVisibility(View.GONE);
        }
    }

    private String getUpdateProcessText(String label, int process) {
        StringBuilder sb = new StringBuilder();
        sb.append(label);
        if (mDeviceInfoPresenter != null && mDeviceInfoPresenter.getUpdateProcess() > 0) {
            sb.append(" ");
            sb.append(process);
            sb.append("%");
        }
        return sb.toString();
    }

    private void refreshDeviceInfo() {
        if (mIsRefreshDevice && mDeviceInfoPresenter != null) {
            mIsRefreshDevice = false;
            loadDeviceInfo();
        }
    }

    @Override
    public void onQueryNooieDeviceUpdateStatusFailed(String msg) {
        if (isDestroyed()) {
            return;
        }
    }

    @Override
    public void notifyDeviceRenameState(String result) {
        if (isDestroyed()) {
            return;
        }
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            Intent intent = new Intent(ConstantValue.BROADCAST_KEY_UPDATE_CAMERA);
            NotificationUtil.sendBroadcast(NooieApplication.mCtx, intent);
        } else {
            ToastUtil.showToast(this, result);
        }
    }

    @Override
    public void onStartUpdateDeviceResult(String result) {
        if (isDestroyed()) {
            return;
        }
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            mIsStartDeviceUpdated = true;
            mIsRefreshDevice = true;
        } else {
            onQueryNooieDeviceUpdateStatusSuccess(ApiConstant.DEVICE_UPDATE_TYPE_UPDATE_FAILED, 0);
        }
    }

    @Override
    public void onGetCamInfoResult(String result, CameraInfo cameraInfo) {
        if (isDestroyed() || checkNull(livDeviceBattery)) {
            return;
        }
        hideLoading();
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result) && cameraInfo != null) {
            //livDeviceBattery.setLabelRight_1(NooieDeviceHelper.convertBattery(cameraInfo.batteryLevel) + "%");
        }
    }

    @Override
    public void onCheckDeviceUpgradeScheduleResult(String result, boolean isUpgradeFinish) {
        if (isDestroyed()) {
            return;
        }
        refreshNooieUpdateState(ApiConstant.DEVICE_UPDATE_TYPE_UPATE_FINISH, 100);
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
            return NooieDeviceHelper.getDeviceByConnectionMode(connectionMode, mDeviceId, defaultModel);
        } else {
            return NooieDeviceHelper.getDeviceById(mDeviceId);
        }
    }

    private String convertDeviceIdText(String deviceId) {
        if (TextUtils.isEmpty(deviceId)) {
            return deviceId;
        }
        if (NooieDeviceHelper.checkApFutureCode(deviceId)) {
            return new String();
        }
        return deviceId;
    }

    private String getDeviceModel() {
        if (getCurrentIntent() == null) {
            return null;
        }
        return getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_IPC_MODEL);
    }

    @Override
    public ShortLinkDeviceParam getShortLinkDeviceParam() {
        if (getCurrentIntent() == null) {
            return null;
        }
        String model = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_IPC_MODEL);
        boolean isInit = getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_DATA_TYPE, ConstantValue.CAM_INFO_TYPE_NORMAL) == ConstantValue.CAM_INFO_TYPE_DIRECT;
        ShortLinkDeviceParam shortLinkDeviceParam = new ShortLinkDeviceParam(mUid, mDeviceId, model, mIsSubDevice, isInit, mConnectionMode);
        return shortLinkDeviceParam;
    }

    @Override
    public CurrentDeviceParam getCurrentDeviceParam() {
        if (TextUtils.isEmpty(mDeviceId)) {
            return null;
        }
        String model = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_IPC_MODEL);
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

    private void tryResumeData() {
        boolean isTryConnectShortLinkDevice = getShortLinkDeviceParam() != null
                && NooieDeviceHelper.isSortLinkDevice(getShortLinkDeviceParam().getModel(), getShortLinkDeviceParam().isSubDevice(), getShortLinkDeviceParam().getConnectionMode())
                && checkDeviceActive(getDevice(mConnectionMode));
        if (isTryConnectShortLinkDevice) {
            tryConnectShortLinkDevice();
        }
    }

    private boolean checkDeviceActive(BindDevice device) {
        boolean isDeviceActive = device == null || (device.getOnline() == ApiConstant.ONLINE_STATUS_ON);
        return isDeviceActive;
    }

    private String getDeviceBattery(String model, int battery) {
        if (NooieDeviceHelper.checkIsExternalPower(model, battery)) {
            return getString(R.string.camera_info_external_power_tip);
        }
        StringBuilder batterySb = new StringBuilder();
        batterySb.append(NooieDeviceHelper.computeBattery(model, battery)).append("%");
        return batterySb.toString();
    }
}
