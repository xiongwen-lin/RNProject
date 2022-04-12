package com.afar.osaio.smart.router;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afar.osaio.R;
import com.afar.osaio.routerdata.GetRouterDataFromCloud;
import com.afar.osaio.routerdata.SendHttpRequest;
import com.afar.osaio.smart.home.activity.HomeActivity;
import com.afar.osaio.smart.routerlocal.RouterDao;
import com.afar.osaio.smart.scan.presenter.IRouterScanPresenter;
import com.afar.osaio.smart.scan.presenter.RouterScanPresenter;
import com.afar.osaio.smart.scan.view.IRouterScanView;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.widget.FButton;
import com.afar.osaio.widget.FirmwareUpdateInfoDialog;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.entity.AppVersionResult;
import com.nooie.sdk.api.network.base.bean.entity.DeviceBindStatusResult;
import com.nooie.sdk.api.network.setting.SettingService;
import com.nooie.sdk.device.bean.APPairStatus;
import com.suke.widget.SwitchButton;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class RouterDetalisSetActivity extends RouterBaseActivity implements SendHttpRequest.getRouterReturnInfo, IRouterScanView, FirmwareUpdateInfoDialog.UpdateListener {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.btnRemove)
    FButton btnRemove;
    @BindView(R.id.layout_router_info)
    View layout_router_info;
    @BindView(R.id.layout_router_name)
    View layout_router_name;
    @BindView(R.id.layout_reboot_router)
    View layout_reboot_router;
    @BindView(R.id.router_name)
    TextView router_name;
    @BindView(R.id.fmVersion)
    TextView fmVersion;
    @BindView(R.id.ledSwitch)
    SwitchButton ledSwitch;
    @BindView(R.id.enablaSwitch)
    SwitchButton enablaSwitch;
    @BindView(R.id.layout_version)
    View layout_version;

    private String routerName = "";
    private String routerMac = "";
    private String fmVersionString = "";
    private String ledStatus = "";
    private IRouterScanPresenter mScanDevPresenter;
    private static RouterDao routerDao;
    private boolean isFirst = true;
    private FirmwareUpdateInfoDialog firmwareUpdateInfoDialog;
    private String model = "";

    public static void toRouterDetalisSetActivity(Context from, String routerName, String routerMac) {
        Intent intent = new Intent(from, RouterDetalisSetActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_NAME, routerName);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_MAC, routerMac);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router_detalis_set);
        ButterKnife.bind(this);

        initView();
        initData();

    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.router_detail_setting_title);

        routerName = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_NAME);
        router_name.setText(routerName);
        routerMac = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_MAC);

        ledSwitch.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                // 进入设置页配置Led状态时,页会进入一次checked,导致会loading


                setRouterLed(isChecked);


            }
        });

        enablaSwitch.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                setRouterRemoteManage(isChecked);
            }
        });
    }

    private void initData() {
        mScanDevPresenter = new RouterScanPresenter(this);
        getRouterInfo();
        routerDao = RouterDao.getInstance(this);
    }

    private void delayScan() {
        mScanDevPresenter.startCountDown();
        mScanDevPresenter.startScanDevice();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @OnClick({R.id.ivLeft, R.id.btnRemove, R.id.layout_router_info, R.id.layout_router_name, R.id.layout_reboot_router, R.id.layout_version})
    public void onViewClicked(View v) {
        switch (v.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.btnRemove:
                showRemoveRouterDialog("");
                break;
            case R.id.layout_router_name:
                RouterResetNameActivity.toRouterResetNameActivity(RouterDetalisSetActivity.this, "setting", router_name.getText().toString());
                break;
            case R.id.layout_router_info:
                RouterInfoActivity.toRouterInfoActivity(this, routerName, routerMac);
                break;
            case R.id.layout_reboot_router:
                showRebootRouterDialog();
                break;
            case R.id.layout_version:
                setCheakRouterVersion();
                break;
        }
    }

    private String newVersion = "";

    private void dealwithRouterUpdata(JSONObject jsonObject) {
        int num = 0;
        Message message = new Message();
        try {
            num = Integer.parseInt(jsonObject.getString("cloudFwStatus"));
            switch (num) {
                case 1:
                    ToastUtil.showToast(this, "路由器设备无网络");
                    break;
                case 2:
                    ToastUtil.showToast(this, "当前版本已为最新版本");
                    break;
                case 3:
                    ToastUtil.showToast(this, "检测中,请等待......");
                    break;
                case 4:
                    newVersion = jsonObject.getString("newVersion");
                    message.what = 0;
                    handler.sendMessage(message);
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getRouterInfo() {
//        showLoadingDialog();
        GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
        try {
            routerDataFromCloud.getInitCfg();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void CloudSrvVersionCheck() {
        GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
        try {
            routerDataFromCloud.cloudSrvVersionCheck();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void rebootRouter() {
        showLoadingDialog();
        try {
            GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
            routerDataFromCloud.rebootSystem();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setRouterLed(boolean isOpen) {
        showLoadingDialog();
        try {
            GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
            routerDataFromCloud.setLedCfg(isOpen ? "1" : "0");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 检测固件升级
     */
    private void setCheakRouterVersion() {
        showLoadingDialog();
        try {
            GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
            routerDataFromCloud.getCloudSrvCheckStatus();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setRouterRemoteManage(boolean isRemoteManage) {
        showLoadingDialog();
        try {
            GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
            routerDataFromCloud.setRemoteCfg("1024", isRemoteManage ? "1" : "0");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setUpdataRouter() {
        showLoadingDialog();
        try {
            GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
            routerDataFromCloud.setUpgradeFW();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setRouterUnbind() {
        showLoadingDialog();
        try {
            GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
            routerDataFromCloud.setRouterUnBind("1");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Dialog mShowRebootRouterDialog;

    private void showRebootRouterDialog() {
        hideRebootRouterDialog();
        mShowRebootRouterDialog = DialogUtils.rebootRouterDialog(this, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                rebootRouter();
            }

            @Override
            public void onClickLeft() {

            }
        }, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            }
        });
    }

    private void hideRebootRouterDialog() {
        if (mShowRebootRouterDialog != null) {
            mShowRebootRouterDialog.dismiss();
            mShowRebootRouterDialog = null;
        }
    }

    private Dialog mShowRemoveRouterDialog;

    private void showRemoveRouterDialog(String device) {
        hideRemoveRouterDialog();
        mShowRemoveRouterDialog = DialogUtils.removeRouterDialog(this, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                // 解绑
                //setRouterUnbind();
                routerDao.deleteRouter(routerMac);
                HomeActivity.toHomeActivity(RouterDetalisSetActivity.this);
            }

            @Override
            public void onClickLeft() {

            }
        }, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            }
        });
    }

    private void hideRemoveRouterDialog() {
        if (mShowRemoveRouterDialog != null) {
            mShowRemoveRouterDialog.dismiss();
            mShowRemoveRouterDialog = null;
        }
    }

    /**
     * 显示固件升级
     */
    private Dialog mShowUpdataRouterDialog;

    private void showUpdataRouterDialog() {
        hideUpdataRouterDialog();
        mShowUpdataRouterDialog = DialogUtils.updataRouterDialog(this, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                //setUpdataRouter();
                RouterBindToDeviceActivity.toRouterBindToDeviceActivity(RouterDetalisSetActivity.this, "firmware", newVersion);
            }

            @Override
            public void onClickLeft() {

            }
        }, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            }
        });
    }

    private void hideUpdataRouterDialog() {
        if (mShowUpdataRouterDialog != null) {
            mShowUpdataRouterDialog.dismiss();
            mShowUpdataRouterDialog = null;
        }
    }

    private Dialog mShowConnectionRouterDialog;

    private void showConnectionRouterDialog() {
        hideBackupRouterDialog();
        mShowConnectionRouterDialog = DialogUtils.connectionRouterFairDialog(this, new DialogUtils.OnClickConfirmButtonListener() {

            @Override
            public void onClickLeft() {
                hideBackupRouterDialog();
            }

            @Override
            public void onClickRight() {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        }, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            }
        });
    }

    private void hideBackupRouterDialog() {
        if (mShowConnectionRouterDialog != null) {
            mShowConnectionRouterDialog.dismiss();
            mShowConnectionRouterDialog = null;
        }
    }

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

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            hideLoadingDialog();
            if (msg.what == 0) {
                if (!TextUtils.isEmpty(model)) {
                    getDeviceHardVersion(model);
                }
            }
            if (msg.what == 1) {
                fmVersion.setText(newVersion);
            } else if (msg.what == 2) {
                fmVersion.setText(fmVersionString);
                ledSwitch.setChecked(Integer.parseInt(ledStatus) == 1 ? true : false);
                // 检测版本// 设置该命令后,点击进入时间设置发现以前设置的同步时区又不生效了
                //CloudSrvVersionCheck();
            } else if (msg.what == 3) {
                stopScanForPlan();
                finish();
            } else if (msg.what == 4) {
                stopScanForPlan();
                finish();
            } else if (msg.what == 5) {
                showConnectionRouterDialog();
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            routerName = data.getStringExtra(ConstantValue.INTENT_KEY_DEVICE_NAME);
            RouterDao routerDao = RouterDao.getInstance(this);
            routerDao.updateRouter(routerName, routerMac);
            router_name.setText(routerName);
        }
    }

    @Override
    public void routerReturnInfo(String info, String topicurlString) {
        hideLoadingDialog();
        try {
            if (!"error".equals(info) && "getCloudSrvCheckStatus".equals(topicurlString)) {
                dealwithRouterUpdata(new JSONObject(info));
            } else if (!"error".equals(info) && "setUpgradeFW".equals(topicurlString) && !TextUtils.isEmpty(info)) {
                Message message = new Message();
                message.what = 1;
                JSONObject jsonObject = new JSONObject(info);
                model = jsonObject.getString("model");
                handler.sendMessage(message);
            } else if (!"error".equals(info) && "getInitCfg".equals(topicurlString)) {
                JSONObject jsonObject = new JSONObject(info);
                fmVersionString = jsonObject.getString("fmVersion");
                ledStatus = jsonObject.getString("ledStatus");
                model = jsonObject.getString("model");
                Message message = new Message();
                message.what = 2;
                handler.sendMessage(message);
            } else if (!"error".equals(info) && "setVictureUnbind".equals(topicurlString)) {
                delayScan();
            }

            if ("error".equals(info) || "".equals(info)) {
                Message message = new Message();
                message.what = 5;
                handler.sendMessage(message);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void showLoadingDialog() {
        showLoading("");
    }

    @Override
    public void hideLoadingDialog() {
        hideLoading();
    }

    @Override
    public void onUpdateTimer(int seconds) {
    }

    @Override
    public void onTimerFinish() {
        Message message = new Message();
        message.what = 4;
        handler.sendMessage(message);
    }

    @Override
    public void onScanDeviceSuccess() {
        Message message = new Message();
        message.what = 3;
        handler.sendMessage(message);
    }

    @Override
    public void onScanDeviceFailed(String msg) {

    }

    @Override
    public void onScanDeviceByOther(DeviceBindStatusResult result) {

    }

    @Override
    public void onLoadRecentBindDeviceSuccess(boolean isScanSuccess) {

    }

    @Override
    public void onLoadRecentBindDeviceFailed(String msg) {

    }

    @Override
    public void onQueryAPPairStatus(String result, APPairStatus status) {

    }

    /**
     * 显示升级对话框
     */
    private void showUpdateDialog(String log, String version) {
        if (null == firmwareUpdateInfoDialog) {
            firmwareUpdateInfoDialog = new FirmwareUpdateInfoDialog(this, log, version, this);
        }

        if (!firmwareUpdateInfoDialog.isShowing()) {
            firmwareUpdateInfoDialog.show();
        }

    }

    @Override
    public void confirmUpdate() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() { // 延迟 500 毫秒显示
                showUpdataRouterDialog();
            }
        }, 500);

    }


    /**
     * 获取 固件信息
     */
    private void getDeviceHardVersion(String model) {
        SettingService.getService().getHardVersion(model)
                .flatMap(new Func1<BaseResponse<AppVersionResult>, Observable<BaseResponse<AppVersionResult>>>() {
                    @Override
                    public Observable<BaseResponse<AppVersionResult>> call(BaseResponse<AppVersionResult> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            NooieLog.d("-->> debug HomePresenter mode=" + response.getData().getModel() + " version=" + response.getData().getVersion_code());
                            String log = response.getData().getLog();
                            String version_code = response.getData().getVersion_code();
                            Log.e("输出日志", log);
                            Log.e("输出版本", version_code);
                        }
                        return Observable.just(response);
                    }
                })
                .onErrorReturn(new Func1<Throwable, BaseResponse<AppVersionResult>>() {
                    @Override
                    public BaseResponse<AppVersionResult> call(Throwable throwable) {
                        Log.e("输出", "请求失败");
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<AppVersionResult>>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(BaseResponse<AppVersionResult> appVersionResultBaseResponse) {
                        String log = appVersionResultBaseResponse.getData().getLog();
                        String versionCode = "New Version:"+appVersionResultBaseResponse.getData().getVersion_code();
                        showUpdateDialog(log,versionCode);
                    }
                });

    }

}
