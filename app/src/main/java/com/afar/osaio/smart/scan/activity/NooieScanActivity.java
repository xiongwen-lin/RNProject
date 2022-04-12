package com.afar.osaio.smart.scan.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.ContextCompat;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.hybrid.webview.HybridWebViewActivity;
import com.afar.osaio.widget.MediaPopupWindows;
import com.nooie.data.EventDictionary;
import com.nooie.data.entity.external.DeviceScanState;
import com.nooie.sdk.bean.IpcType;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.scan.helper.ApHelper;
import com.nooie.common.base.GlobalData;
import com.nooie.common.utils.configure.CountryUtil;
import com.nooie.common.utils.tool.TaskUtil;
import com.afar.osaio.smart.home.activity.HomeActivity;
import com.afar.osaio.smart.scan.bean.NooieScanDeviceCache;
import com.afar.osaio.smart.scan.presenter.INooieScanPresenter;
import com.afar.osaio.smart.scan.presenter.NooieScanPresenter;
import com.afar.osaio.smart.scan.view.INooieScanView;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.afar.osaio.widget.FButton;
import com.afar.osaio.widget.ScanCameraView;
import com.afar.osaio.widget.base.BaseScanCameraView;
import com.afar.osaio.widget.listener.BaseScanCameraListener;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.api.network.base.bean.entity.DeviceBindStatusResult;
import com.nooie.sdk.device.bean.APNetCfg;
import com.nooie.sdk.device.bean.APPairStatus;
import com.nooie.sdk.receiver.NetworkWatcher;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * NooieScanActivity
 *
 * @author Administrator
 * @date 2019/4/16
 */
public class NooieScanActivity extends BaseActivity implements INooieScanView {

    private final static String SCAN_PROCESS_SUCCESS_END = "100%";
    private final static String SCAN_PROCESS_FAIL_END = "99%";

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.tvNum)
    TextView tvNum;
    @BindView(R.id.btnSolve)
    Button btnSolve;
    @BindView(R.id.btnContactUs)
    Button btnContactUs;
    @BindView(R.id.scanCameraContainer)
    LinearLayout scanCameraContainer;
    BaseScanCameraView mBaseScanCameraView;
    @BindView(R.id.btnCameraInstallation)
    FButton btnCameraInstallation;
    @BindView(R.id.btnCameraInstalled)
    FButton btnCameraInstalled;
    @BindView(R.id.tvConnectionStep_1)
    TextView tvConnectionStep_1;
    @BindView(R.id.tvConnectionStep_2)
    TextView tvConnectionStep_2;
    @BindView(R.id.tvConnectionStep_3)
    TextView tvConnectionStep_3;
    @BindView(R.id.tvConnectionHelp_1)
    TextView tvConnectionHelp_1;
    @BindView(R.id.tvConnectionHelp_2)
    TextView tvConnectionHelp_2;
    @BindView(R.id.tvConnectionHelp_3)
    TextView tvConnectionHelp_3;

    private String mSSID;
    private String mPsd;
    private IpcType mDeviceType;
    private String mApSSID;
    private boolean mIsCheckApSwitchNetwork = false;
    private INooieScanPresenter mScanDevPresenter;
    private DeviceBindStatusResult mDeviceBindStatusResult;
    private MediaPopupWindows mPopMenus;

    public static void toNooieScanActivity(Context from, String ssid, String psd, String model, int connectionMode, String apSsid) {
        Intent intent = new Intent(from, NooieScanActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_PSD, psd);
        intent.putExtra(ConstantValue.INTENT_KEY_SSID, ssid);
        intent.putExtra(ConstantValue.INTENT_KEY_IPC_MODEL, model);
        intent.putExtra(ConstantValue.INTENT_KEY_CONNECTION_MODE, connectionMode);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_PARAM, apSsid);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //hideStatusBar();

        setContentView(R.layout.activity_scan_camera);
        ButterKnife.bind(this);

        NooieLog.d("-->> debug NooieScanActivity onCreate: ");
        initData();
        initView();
        prepareScan();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.add_camera_paring);
        tvNum.setVisibility(View.GONE);
        setupScan();
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
        } else {
            mSSID = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SSID);
            mPsd = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_PSD);
            mDeviceType = IpcType.getIpcType(getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_IPC_MODEL));
            mApSSID = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DATA_PARAM);

            mScanDevPresenter = new NooieScanPresenter(this);
            switchCheckDeviceConnection(true);
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

        mPopMenus.showAtLocation(this.findViewById(R.id.containerNooieScan),
                Gravity.TOP | Gravity.BOTTOM, 0, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        keepScreenLongLight();
        /*
        if (mIsFinishScan) {
            return;
        }
        NooieScanDeviceCache.getInstance().clear();
        tryStartCanApDevice();
        startScan();
         */
    }

    private void prepareScan() {
        NooieLog.d("-->> debug NooieScanActivity prepareScan: 1 mIsFinishScan=" + mIsFinishScan);
        if (mIsFinishScan) {
            return;
        }
        addNetworkWatcher();
        NooieScanDeviceCache.getInstance().clear();
        tryStartCanApDevice();
        startScan();
        NooieLog.d("-->> debug NooieScanActivity prepareScan: 2");
    }

    @Override
    protected void onPause() {
        super.onPause();
        NooieLog.d("-->> debug NooieScanActivity onPause: ");
        clearKeepScreenLongLight();
    }

    @Override
    protected void onStop() {
        super.onStop();
        NooieLog.d("-->> debug NooieScanActivity onStop: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NooieLog.d("-->> debug NooieScanActivity onDestroy: 1");
        removeNetworkWatcherListener();
        stopScan();
        if (mBaseScanCameraView != null) {
            mBaseScanCameraView.closeScan();
        }
        if (mScanDevPresenter != null) {
            mScanDevPresenter.destroy();
        }
        switchCheckDeviceConnection(false);
        releaseRes();
        NooieLog.d("-->> debug NooieScanActivity onDestroy: 2");
    }

    @Override
    public void releaseRes() {
        super.releaseRes();
        ivLeft = null;
        tvTitle = null;
        ivRight = null;
        tvNum = null;
        btnSolve = null;
        btnContactUs = null;
        if (mBaseScanCameraView != null) {
            mBaseScanCameraView.release();
            mBaseScanCameraView = null;
        }
        if (scanCameraContainer != null) {
            scanCameraContainer.removeAllViews();
            scanCameraContainer = null;
        }
        btnCameraInstallation = null;
        btnCameraInstalled = null;
        tvConnectionStep_1 = null;
        tvConnectionStep_2 = null;
        tvConnectionStep_3 = null;
        tvConnectionHelp_1 = null;
        tvConnectionHelp_2 = null;
        tvConnectionHelp_3 = null;
    }

    @OnClick({R.id.ivLeft, R.id.btnSolve, R.id.btnCameraInstallation, R.id.btnCameraInstalled, R.id.tvConnectionHelp_1, R.id.tvConnectionHelp_2, R.id.tvConnectionHelp_3, R.id.btnContactUs})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.tvConnectionHelp_1:
            case R.id.tvConnectionHelp_2:
            case R.id.tvConnectionHelp_3:
                gotoConnectionHelp(getConnectionMode());
                finish();
                break;
            case R.id.btnSolve:
                //ScanSolveProblemActivity.toScanSolveProblemActivity(NooieScanActivity.this, mDeviceType.getType());
                gotoScanSolveProblem(getConnectionMode());
                finish();
                break;
            case R.id.btnContactUs:
                showPopMenu();
                break;
            case R.id.btnCameraInstallation:
                NooieLog.d("-->> NooieScanActivity onViewClicked bind device id=" + mBindDeviceId);
                if (!TextUtils.isEmpty(mBindDeviceId)) {
                    DeviceSetupActivity.toDeviceSetupActivity(NooieScanActivity.this, mBindDeviceId, mDeviceType.getType());
                }
                finish();
                break;
            case R.id.btnCameraInstalled:
                NooieLog.d("-->> NooieScanActivity onViewClicked bind device id=" + mBindDeviceId);
                if (!TextUtils.isEmpty(mBindDeviceId)) {
                    NooieNameDeviceActivity.toNooieNameDeviceActivity(NooieScanActivity.this, mBindDeviceId, mDeviceType.getType());
                }
                finish();
                break;
        }
    }

    private void setupScan() {
        if (mBaseScanCameraView == null) {
            /*
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            if (mDeviceType == IpcType.IPC_100) {
                mBaseScanCameraView = new ScanCameraView(getApplicationContext());
                ((ScanCameraView) mBaseScanCameraView).setupScanCameraView(mDeviceType);
                ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 0, (int)(DisplayUtil.SCREEN_HIGHT_PX*0.1));
                mBaseScanCameraView.setLayoutParams(params);
            } else if (mDeviceType == IpcType.IPC_200) {
                mBaseScanCameraView = new ScanCameraView(getApplicationContext());
                ((ScanCameraView) mBaseScanCameraView).setupScanCameraView(mDeviceType);
                ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 0, (int)(DisplayUtil.SCREEN_HIGHT_PX*0.1));
                mBaseScanCameraView.setLayoutParams(params);
            } else {
                mBaseScanCameraView = new ScanDeviceView(getApplicationContext());
                mBaseScanCameraView.setLayoutParams(layoutParams);
            }
            */

            mBaseScanCameraView = new ScanCameraView(getApplicationContext());
            ((ScanCameraView) mBaseScanCameraView).setupScanCameraView(mDeviceType);
            ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, (int) (DisplayUtil.SCREEN_HIGHT_PX * 0.1));
            mBaseScanCameraView.setLayoutParams(params);
        }

        scanCameraContainer.removeAllViews();
        scanCameraContainer.addView(mBaseScanCameraView);

        mBaseScanCameraView.setListener(new BaseScanCameraListener() {
            @Override
            public void onScanSuccess() {
                gotoScanResultActivity(NooieScanDeviceCache.getInstance().getDeviceInfoEntityList(), true);
            }

            @Override
            public void onScanFailed() {
                gotoScanResultActivity(NooieScanDeviceCache.getInstance().getDeviceInfoEntityList(), false);
            }
        });
    }

    private void startScan() {
        NooieLog.d("-->> debug NooieScanActivity startScan: 1");
        delayScan();
        mBaseScanCameraView.startScanLoop();
        NooieLog.d("-->> debug NooieScanActivity startScan: 2");
        /*
        if (mDeviceType == IpcType.IPC_720 || mDeviceType == IpcType.IPC_1080) {
            mBaseScanCameraView.post(new Runnable() {
                @Override
                public void run() {
                    mBaseScanCameraView.startScanLoop();
                    //delayScan();
                }
            });
        } else {
            mBaseScanCameraView.startScanLoop();
            //delayScan();
        }
        */
    }

    private void delayScan() {
        NooieLog.d("-->> debug NooieScanActivity delayScan: ");
        tvNum.setVisibility(View.VISIBLE);
        //tvNum.setText(String.format("%ds", NooieScanPresenter.SCAN_LIMIT_TIME));
        tvNum.setText("0%");
        mScanDevPresenter.startCountDown();
        mScanDevPresenter.startScanDevice();

        btnSolve.setVisibility(View.GONE);
        btnContactUs.setVisibility(View.GONE);
        btnCameraInstallation.setVisibility(View.GONE);
        btnCameraInstalled.setVisibility(View.GONE);
    }

    private void stopScan() {
        NooieLog.d("-->> debug NooieScanActivity stopScan: ");
        stopScanForPlan();
        if (mBaseScanCameraView != null) {
            mBaseScanCameraView.stopScanLoop();
        }
    }

    private void tryStartCanApDevice() {
        NooieLog.d("-->> NooieScanActivity tryStartCanApDevice 1 account=" + mUserAccount + " uid=" + mUid + " ssid=" + mSSID + " psd=" + mPsd);
        if (getConnectionMode() == ConstantValue.CONNECTION_MODE_QC || TextUtils.isEmpty(mSSID)) {
            return;
        }

        NooieLog.d("-->> debug NooieScanActivity tryStartCanApDevice: 2");
        APNetCfg apNetCfg = new APNetCfg();
        apNetCfg.ssid = mSSID;
        apNetCfg.psd = mPsd;
        apNetCfg.uid = mUid;
        apNetCfg.region = TextUtils.isEmpty(GlobalData.getInstance().getRegion()) ? ApHelper.getInstance().getCurrentRegion().toUpperCase() : GlobalData.getInstance().getRegion().toUpperCase();
        apNetCfg.zone = CountryUtil.getCurrentTimezone() + ".00";
        if (TextUtils.isEmpty(mPsd)) {
            apNetCfg.encrypt = ApHelper.ENCRYPT_OPEN;
        }

        if (mScanDevPresenter != null) {
            mScanDevPresenter.startScanApDevice(apNetCfg);
            refreshAPPairStatusView(APPairStatus.AP_PAIR_RECVED_WIFI);
        }
    }

    @Override
    public void onUpdateTimer(int seconds) {
        /*
        tvNum.setVisibility(seconds > 0 ? View.VISIBLE : View.GONE);
        tvNum.setText(String.format("%ds", seconds));
        */
        tvNum.setVisibility(seconds >= 0 ? View.VISIBLE : View.GONE);
        tvNum.setText(String.format("%d", seconds) + "%");
    }

    private boolean mIsFinishScan = false;

    private void stopScanForPlan() {
        NooieLog.d("-->> debug NooieScanActivity stopScanForPlan: ");
        if (mScanDevPresenter != null) {
            mScanDevPresenter.stopCountDown();
            mScanDevPresenter.stopScanDevice();
        }
        stopQueryRecentBindDevice();
    }

    private void stopQueryRecentBindDevice() {
        NooieLog.d("-->> debug NooieScanActivity stopQueryRecentBindDevice: ");
        if (mScanDevPresenter != null) {
            mScanDevPresenter.stopQueryRecentBindDeviceTask();
        }
    }

    @Override
    public void onTimerFinish() {
        //mIsFinishScan = true;
        //stopScanForPlan();

        //mScanDevPresenter.loadRecentBindDevice(false);
        NooieLog.d("-->> debug NooieScanActivity onTimerFinish: ");
        onLoadRecentBindDeviceSuccess(false);
    }

    @Override
    public void onScanDeviceSuccess() {
        if (isDestroyed()) {
            return;
        }
        NooieLog.d("-->> debug NooieScanActivity onScanDeviceSuccess: ");
        //mIsFinishScan = true;
        //stopScanForPlan();
        if (mScanDevPresenter != null) {
            mScanDevPresenter.loadRecentBindDevice(mUid, true);
        }
    }

    @Override
    public void onScanDeviceByOther(DeviceBindStatusResult result) {
        NooieLog.d("-->> debug NooieScanActivity onScanDeviceByOther: ");
        //mIsFinishScan = true;
        //stopScanForPlan();
        //mScanDevPresenter.loadRecentBindDevice(false);
        onLoadRecentBindDeviceSuccess(false);
    }

    @Override
    public void onScanDeviceFailed(String msg) {
        NooieLog.d("-->> debug NooieScanActivity onScanDeviceFailed: ");
    }

    @Override
    public void onLoadRecentBindDeviceSuccess(boolean isScanSuccess) {
        if (isDestroyed() || checkNull(mBaseScanCameraView, tvNum)) {
            return;
        }
        NooieLog.d("-->> debug NooieScanActivity onLoadRecentBindDeviceSuccess: isScanSuccess=" + isScanSuccess);
        mIsFinishScan = true;
        stopScanForPlan();
        mBaseScanCameraView.stopScan(isScanSuccess);
        if (isScanSuccess) {
            tvNum.setText(SCAN_PROCESS_SUCCESS_END);
            refreshAPPairStatusView(APPairStatus.AP_PAIR_ONLINE_SUCC);
        } else {
            tvNum.setText(SCAN_PROCESS_FAIL_END);
            addWifiScanAnimationEvent(true);
        }
    }

    @Override
    public void onLoadRecentBindDeviceFailed(String msg) {
        if (isDestroyed() || checkNull(mBaseScanCameraView)) {
            return;
        }
        NooieLog.d("-->> debug NooieScanActivity onLoadRecentBindDeviceFailed: ");
        mBaseScanCameraView.stopScan(false);
    }

    private void gotoScanResultActivity(final List<BindDevice> devices, final boolean isSuccess) {
        NooieLog.d("-->> debug NooieScanActivity gotoScanResultActivity: isSuccess=" + isSuccess);
        addScanResultEvent(devices, isSuccess);
        TaskUtil.delayAction(1000 * 1, new TaskUtil.OnDelayTimeFinishListener() {
            @Override
            public void onFinish() {
                if (isDestroyed()) {
                    return;
                }
                if (isSuccess) {
                    gotoNameActivity(devices);
                } else {
                    toResultActivity(devices);
                }
            }
        });
    }

    private boolean isDeviceCanAdd(BindDevice device) {
        return device != null && !TextUtils.isEmpty(device.getUuid()) && device.getBind_type() != ApiConstant.BIND_TYPE_SHARE;
    }

    private String mBindDeviceId = null;

    private void gotoNameActivity(List<BindDevice> devices) {
        NooieLog.d("-->> debug NooieScanActivity gotoNameActivity: 1");
        if (isDestroyed()) {
            return;
        }
        NooieLog.d("-->> debug NooieScanActivity gotoNameActivity: 2");
        for (BindDevice device : CollectionUtil.safeFor(devices)) {
            if (isDeviceCanAdd(device)) {
                mBindDeviceId = device.getUuid();
                if (tvTitle != null) {
                    tvTitle.setText(R.string.add_camera_connected);
                }
                //boolean isShowOutdoorGuide = TextUtils.isEmpty(device.getType()) ? NooieDeviceHelper.mergeIpcType(mDeviceType) == IpcType.PC730 : (NooieDeviceHelper.mergeIpcType(mDeviceType) == IpcType.PC730 && NooieDeviceHelper.mergeIpcType(IpcType.getIpcType(device.getType())) == IpcType.PC730);
                boolean isShowOutdoorGuide = NooieDeviceHelper.mergeIpcType(mDeviceType) == IpcType.PC730;
                if (isShowOutdoorGuide && !checkNull(btnCameraInstallation, btnCameraInstalled)) {
                    btnCameraInstallation.setVisibility(View.VISIBLE);
                    btnCameraInstalled.setVisibility(View.VISIBLE);
                    return;
                }
                NooieLog.d("-->> NooieScanActivity toNameActivity");
                //to name activity
                NooieNameDeviceActivity.toNooieNameDeviceActivity(NooieScanActivity.this, device.getUuid(), TextUtils.isEmpty(device.getType()) ? mDeviceType.getType() : device.getType());
                finish();
                return;
            }
        }
        NooieLog.d("-->> NooieScanActivity toNameActivity no device to add");
        addGotoHomeEvent();
        HomeActivity.toHomeActivity(this);
        finish();
        //toResultActivity(devices);
    }

    private void toResultActivity(List<BindDevice> devices) {
        NooieLog.d("-->> NooieScanActivity toResultActivity 1");
        if (isDestroyed()) {
            return;
        }
        boolean isGotoDeviceScanFailPage = mDeviceBindStatusResult != null && mDeviceBindStatusResult.getType() == ApiConstant.QUERY_BIND_TYPE_REPEAT && !TextUtils.isEmpty(mDeviceBindStatusResult.getUuid())
                && mDeviceBindStatusResult.getData() != null && !TextUtils.isEmpty(mDeviceBindStatusResult.getData().getModel());
        if (isGotoDeviceScanFailPage) {
            NooieDeviceHelper.trackDNEvent(EventDictionary.EVENT_ID_ACCESS_CONNECT_ERROR_PAGE, "072", NooieDeviceHelper.createUuidRepeatDNExternal(mDeviceBindStatusResult.getUuid(), mDeviceBindStatusResult.getData().getModel()), "");
            Bundle param = new Bundle();
            param.putString(ConstantValue.INTENT_KEY_DEVICE_ID, mDeviceBindStatusResult.getUuid());
            param.putString(ConstantValue.INTENT_KEY_IPC_MODEL, mDeviceBindStatusResult.getData().getModel());
            mDeviceBindStatusResult = null;
            gotoDeviceScanFailActivity(param);
            return;
        }
        NooieLog.d("-->> NooieScanActivity toResultActivity 2");
        if (!checkNull(tvTitle, tvNum)) {
            tvTitle.setText(R.string.add_camera_connected_failed);
            tvNum.setVisibility(View.INVISIBLE);
        }
        if (CollectionUtil.isEmpty(NooieScanDeviceCache.getInstance().getBindByOtherDeviceInfoEntityList())) {
            if (btnSolve != null) {
                btnSolve.setVisibility(View.VISIBLE);
            }
            if (btnContactUs != null) {
                btnContactUs.setVisibility(View.VISIBLE);
            }
            //refreshAPPairStatusView(APPairStatus.AP_PAIR_ONLINE_FAILED);
            NooieDeviceHelper.trackDNEvent(EventDictionary.EVENT_ID_ACCESS_WIFI_CONNECT_FAIL_TIP_PAGE);
        } else {
            ScanCameraResultActivity.toScanCameraResultActivity(NooieScanActivity.this, mDeviceType.getType(), getConnectionMode());
            finish();
        }
    }

    private void gotoDeviceScanFailActivity(Bundle param) {
        DeviceScanFailActivity.toDeviceScanFailActivity(this, param);
        finish();
    }

    @Override
    public void onQueryAPPairStatus(String result, APPairStatus status) {
        NooieLog.d("-->> debug NooieScanActivity onQueryAPPairStatus: ");
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            NooieLog.d("-->> NooieScanActivity onQueryAPPairStatus status=" + getApConnectionTip(status));
            refreshAPPairStatusView(status);
        }
    }

    @Override
    public void onScanDeviceRepeatBound(DeviceBindStatusResult result) {
        NooieLog.d("-->> debug NooieScanActivity onScanDeviceRepeatBound: ");
        mDeviceBindStatusResult = result;
        if (result != null) {
            onLoadRecentBindDeviceSuccess(false);
        }
    }

    @Override
    public void onCheckApSwitchNetwork(boolean isApSwitch) {
        if (isApSwitch) {
            mIsCheckApSwitchNetwork = true;
            NooieDeviceHelper.trackDNEvent(EventDictionary.EVENT_ID_DEVICE_DISCONNECT_AP_AND_SWITCH_NETWORK);
        }
    }

    private void refreshAPPairStatusView(APPairStatus status) {
        if (getConnectionMode() != ConstantValue.CONNECTION_MODE_AP) {
            displayAPPairStatusView(APPairStatus.AP_PAIR_NO_RECV_WIFI);
            return;
        }
        if (checkNull(status, tvConnectionStep_1, tvConnectionStep_2, tvConnectionStep_3)) {
            return;
        }
        if (status == APPairStatus.AP_PAIR_NO_RECV_WIFI || status == APPairStatus.AP_PAIR_RECVED_WIFI) {
            tvConnectionStep_1.setText(getApConnectionTip(status));
            displayAPPairStatusView(APPairStatus.AP_PAIR_RECVED_WIFI);
        } else if (status == APPairStatus.AP_PAIR_CONNECTING_WIFI || status == APPairStatus.AP_PAIR_CONN_WIFI_SUCC || status == APPairStatus.AP_PAIR_CONN_WIFI_FAILED) {
            tvConnectionStep_1.setText(getString(R.string.scan_device_init_network_success));
            tvConnectionStep_2.setText(getApConnectionTip(status));
            displayAPPairStatusView(APPairStatus.AP_PAIR_CONNECTING_WIFI);
        } else if (status == APPairStatus.AP_PAIR_START_ONLINE || status == APPairStatus.AP_PAIR_ONLINE_SUCC || status == APPairStatus.AP_PAIR_ONLINE_FAILED) {
            tvConnectionStep_1.setText(getString(R.string.scan_device_init_network_success));
            tvConnectionStep_2.setText(getString(R.string.scan_device_network_connect_sucess));
            tvConnectionStep_3.setText(getApConnectionTip(status));
            displayAPPairStatusView(APPairStatus.AP_PAIR_START_ONLINE);
        } else {
        }
        displayAPPairStatusFailView(status);
    }

    private void displayAPPairStatusView(APPairStatus status) {
        if (checkNull(tvConnectionStep_1, tvConnectionStep_2, tvConnectionStep_3)) {
            return;
        }
        tvConnectionStep_1.setVisibility(View.GONE);
        tvConnectionStep_2.setVisibility(View.GONE);
        tvConnectionStep_3.setVisibility(View.GONE);
        if (status == APPairStatus.AP_PAIR_RECVED_WIFI) {
            tvConnectionStep_1.setVisibility(View.VISIBLE);
            tvConnectionStep_1.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.theme_green));
        } else if (status == APPairStatus.AP_PAIR_CONNECTING_WIFI) {
            tvConnectionStep_1.setVisibility(View.VISIBLE);
            tvConnectionStep_2.setVisibility(View.VISIBLE);
            tvConnectionStep_1.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.gray_a1a1a1));
            tvConnectionStep_2.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.theme_green));
        } else if (status == APPairStatus.AP_PAIR_START_ONLINE) {
            tvConnectionStep_1.setVisibility(View.VISIBLE);
            tvConnectionStep_2.setVisibility(View.VISIBLE);
            tvConnectionStep_3.setVisibility(View.VISIBLE);
            tvConnectionStep_1.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.gray_a1a1a1));
            tvConnectionStep_2.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.gray_a1a1a1));
            tvConnectionStep_3.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.theme_green));
        }
    }

    private void displayAPPairStatusFailView(APPairStatus status) {
        if (checkNull(tvConnectionHelp_1, tvConnectionHelp_2, tvConnectionHelp_3)) {
            return;
        }
        tvConnectionHelp_1.setVisibility(View.GONE);
        tvConnectionHelp_2.setVisibility(View.GONE);
        tvConnectionHelp_3.setVisibility(View.GONE);
        if (status == APPairStatus.AP_PAIR_RECVED_WIFI) {
            //tvConnectionHelp_1.setVisibility(View.VISIBLE);
        } else if (status == APPairStatus.AP_PAIR_CONN_WIFI_FAILED) {
            tvConnectionHelp_2.setVisibility(View.VISIBLE);
        } else if (status == APPairStatus.AP_PAIR_ONLINE_FAILED) {
            tvConnectionHelp_3.setVisibility(View.VISIBLE);
        }
    }

    private void gotoScanSolveProblem(int connectionMode) {
        NooieDeviceHelper.trackDNEvent(EventDictionary.EVENT_ID_CLICK_SOLVE_WIFI_CONNECT_FAIL);
        StringBuilder urlBuilder = new StringBuilder("file:///android_asset/html/solve/");
        if (NooieDeviceHelper.mergeIpcType(mDeviceType) == IpcType.PC530) {
            urlBuilder.append("cam_connection_failed_360.html");
        } else if (NooieDeviceHelper.mergeIpcType(mDeviceType) == IpcType.PC730) {
            urlBuilder.append("cam_connection_failed_200.html");
        } else if (NooieDeviceHelper.mergeIpcType(mDeviceType) == IpcType.EC810PRO) {
            urlBuilder.append("cam_connection_failed_ec810_pro.html");
        } else if (NooieDeviceHelper.mergeIpcType(mDeviceType) == IpcType.EC810_PLUS) {
            urlBuilder.append("cam_connection_failed_ec810_plus.html");
        } else if (NooieDeviceHelper.mergeIpcType(mDeviceType) == IpcType.HC320) {
            urlBuilder.append("cam_connection_failed_hc320.html");
        } else {
            urlBuilder.append("cam_connection_failed_1080.html");
        }
        Bundle param = new Bundle();
        param.putString(ConstantValue.INTENT_KEY_URL, urlBuilder.toString());
        param.putString(ConstantValue.INTENT_KEY_TITLE, getString(R.string.help));
        param.putBoolean(ConstantValue.INTENT_KEY_DATA_PARAM, true);
        param.putBoolean(ConstantValue.INTENT_KEY_DATA_PARAM_1, true);
        HybridWebViewActivity.toHybridWebViewActivity(this, param);
    }

    private void gotoConnectionHelp(int connectionMode) {
        if (connectionMode == ConstantValue.CONNECTION_MODE_AP) {
            FailHelpActivity.toFailHelpActivity(this, FailHelpActivity.TYPE_FAIL_HELP_AP_CAMERA);
        }
    }

    private int getConnectionMode() {
        int connectionMode = getCurrentIntent() != null ? getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_QC) : ConstantValue.CONNECTION_MODE_QC;
        return connectionMode;
    }

    private String getApConnectionTip(APPairStatus status) {
        if (status == APPairStatus.AP_PAIR_NO_RECV_WIFI || status == APPairStatus.AP_PAIR_RECVED_WIFI) {
            return getString(R.string.scan_device_init_network);
        } else if (status == APPairStatus.AP_PAIR_NO_RECV_WIFI) {
            return getString(R.string.scan_device_init_network_fail);
        } else if (status == APPairStatus.AP_PAIR_CONNECTING_WIFI) {
            return getString(R.string.scan_device_network_connect);
        } else if (status == APPairStatus.AP_PAIR_CONN_WIFI_SUCC) {
            return getString(R.string.scan_device_network_connect_sucess);
        } else if (status == APPairStatus.AP_PAIR_CONN_WIFI_FAILED) {
            return getString(R.string.scan_device_network_connect_fail);
        } else if (status == APPairStatus.AP_PAIR_START_ONLINE) {
            return getString(R.string.scan_device_service_connect);
        } else if (status == APPairStatus.AP_PAIR_ONLINE_SUCC) {
            return getString(R.string.scan_device_service_connect_sucess);
        } else if (status == APPairStatus.AP_PAIR_ONLINE_FAILED) {
            return getString(R.string.scan_device_service_connect_fail);
        } else {
            return "";
        }
    }

    private void addNetworkWatcher() {
        addNetworkWatcherListener(new NetworkWatcher.OnNetworkChangedListener() {
            @Override
            public void onChanged() {
            }

            @Override
            public void onConnectivityChanged() {
                if (isDestroyed() || mScanDevPresenter == null || mIsCheckApSwitchNetwork || TextUtils.isEmpty(mApSSID)) {
                    return;
                }
                mScanDevPresenter.checkApSwitchNetwork(mApSSID);
            }
        });
    }

    @Override
    public String getEventId(int trackType) {
        return EventDictionary.EVENT_ID_ACCESS_DEVICE_SCAN_PAGE;
    }

    private void addScanResultEvent(List<BindDevice> devices, boolean isSuccess) {
        DeviceScanState deviceScanState = mScanDevPresenter != null ? mScanDevPresenter.getDeviceScanState() : new DeviceScanState();
        int networkMode = getConnectionMode() == ConstantValue.CONNECTION_MODE_AP ? EventDictionary.DISTRIBUTE_NETWORK_MODE_AP : EventDictionary.DISTRIBUTE_NETWORK_MODE_QC;
        int networkResult = isSuccess ? EventDictionary.DISTRIBUTE_NETWORK_RESULT_SUCCESS : EventDictionary.DISTRIBUTE_NETWORK_RESULT_FAIL;
        int devOnline = EventDictionary.DISTRIBUTE_NETWORK_DEV_ONLINE_N;
        if (isSuccess) {
            devOnline = CollectionUtil.isEmpty(devices) ? EventDictionary.DISTRIBUTE_NETWORK_DEV_ONLINE_N : EventDictionary.DISTRIBUTE_NETWORK_DEV_ONLINE_Y;
        }
        int networkReason = 0;
        if (!isSuccess) {
            if (networkMode == ConstantValue.CONNECTION_MODE_AP) {
                if (deviceScanState.getApSendState() == 0) {
                    networkReason = EventDictionary.DISTRIBUTE_NETWORK_AP_REASON_SEND_FAIL;
                } else if (deviceScanState.getApQueryState() == 0) {
                    networkReason = EventDictionary.DISTRIBUTE_NETWORK_AP_REASON_NO_STATE;
                } else if (deviceScanState.getBindInfoState() == ApiConstant.QUERY_BIND_TYPE_REPEAT) {
                    networkReason = EventDictionary.DISTRIBUTE_NETWORK_UUID_REPEAT;
                } else if (deviceScanState.getBindInfoState() == 0) {
                    networkReason = EventDictionary.DISTRIBUTE_NETWORK_AP_REASON_GET_INFO_FAIL;
                }
            } else {
                if (deviceScanState.getBindState() == -1) {
                    networkReason = EventDictionary.DISTRIBUTE_NETWORK_QC_REASON_NO_STATE;
                } else if (deviceScanState.getBindInfoState() == ApiConstant.QUERY_BIND_TYPE_REPEAT) {
                    networkReason = EventDictionary.DISTRIBUTE_NETWORK_UUID_REPEAT;
                } else if (deviceScanState.getBindInfoState() == 0) {
                    networkReason = EventDictionary.DISTRIBUTE_NETWORK_QC_REASON_GET_INFO_FAIL;
                }
            }
        }
        NooieDeviceHelper.trackDNEvent(EventDictionary.EVENT_ID_DEVICE_SCAN_RESULT, NooieDeviceHelper.createConnectionResultDNExternal(networkMode, networkResult, devOnline, networkReason));
    }

    private void addWifiScanAnimationEvent(boolean isStart) {
        NooieDeviceHelper.trackDNEvent(isStart ? EventDictionary.EVENT_ID_ANIMATION_WIFI_CONNECT_START : EventDictionary.EVENT_ID_ANIMATION_WIFI_CONNECT_STOP);
    }

    private void addGotoHomeEvent() {
        NooieDeviceHelper.trackDNEvent(EventDictionary.EVENT_ID_JUMP_HOME);
    }

}

