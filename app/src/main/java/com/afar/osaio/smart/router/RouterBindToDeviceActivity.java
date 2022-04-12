package com.afar.osaio.smart.router;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
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
import com.afar.osaio.smart.scan.presenter.IRouterScanPresenter;
import com.afar.osaio.smart.scan.presenter.RouterScanPresenter;
import com.afar.osaio.smart.scan.view.IRouterScanView;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.afar.osaio.widget.FirewareUpdateDialog;
import com.afar.osaio.widget.RoundProgress;
import com.nooie.common.base.GlobalData;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.entity.DeviceBindStatusResult;
import com.nooie.sdk.device.bean.APPairStatus;

import org.json.JSONException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RouterBindToDeviceActivity extends RouterBaseActivity implements IRouterScanView, SendHttpRequest.getRouterReturnInfo {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.roundProgress)
    RoundProgress roundProgress;
    @BindView(R.id.bind_device)
    TextView bind_device;
    @BindView(R.id.firmware_upgrading)
    TextView firmware_upgrading;
    @BindView(R.id.firmware_upgrading_tip1)
    TextView firmware_upgrading_tip1;
    @BindView(R.id.firmware_upgrading_tip2)
    TextView firmware_upgrading_tip2;

    private IRouterScanPresenter mScanDevPresenter;
    private int max = /*120*/ 200;
    private int count = 0;
    private String seting = "";
    private String newVersion = "";

    private FirewareUpdateDialog firewareUpdateDialog;

    public static void toRouterBindToDeviceActivity(Context from) {
        Intent intent = new Intent(from, RouterBindToDeviceActivity.class);
        from.startActivity(intent);
    }

    public static void toRouterBindToDeviceActivity(Context from, String set, String newVersion) {
        Intent intent = new Intent(from, RouterBindToDeviceActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_SETTING, set);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_UPGRADE_VERSION, newVersion);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router_bind);
        ButterKnife.bind(this);

        initView();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.router_bind_device_title);
        seting = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_SETTING);
        newVersion = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_UPGRADE_VERSION);
        mScanDevPresenter = new RouterScanPresenter(this);
        if ("firmware".equals(seting)) {
            tvTitle.setText(R.string.router_firmware_upgrade);
            bind_device.setVisibility(View.GONE);
            firmware_upgrading.setVisibility(View.VISIBLE);
            firmware_upgrading_tip1.setVisibility(View.VISIBLE);
            firmware_upgrading_tip2.setVisibility(View.VISIBLE);
        } else {
            //mScanDevPresenter = new RouterScanPresenter(this);
            bind_device.setVisibility(View.VISIBLE);
        }
        roundProgress.setMax(max);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ("firmware".equals(seting)) {
            setUpgradeFWTest();
        } else {
            setBindRouter();
        }
    }

    private void delayScan() {
        mScanDevPresenter.startCountDown();
        mScanDevPresenter.startScanDevice();
    }

    private void setUpgradeFWTest() {
        GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
        try {
            routerDataFromCloud.setUpgradeFWTest();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setBindRouter() {
        GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
        try {
            routerDataFromCloud.setRouterBind(GlobalData.getInstance().getUid(), "8.0", "1");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class TimeThread extends Thread {
        @Override
        public void run() {
            do {
                try {
                    Thread.sleep(1000);
                    Message msg = new Message();
                    msg.what = 4;
                    handler.sendMessage(msg);
                    count++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (count < 200);
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

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                roundProgress.setProgress((int) (msg.arg1));
            } else if (msg.what == 2) {
                stopScanForPlan();
                roundProgress.setProgress((int) (max));
                HomeActivity.toHomeActivity(RouterBindToDeviceActivity.this);
            } else if (msg.what == 3) {
                stopScanForPlan();
                RouterBindToDeviceFailActivity.toRouterBindToDeviceFailActivity(RouterBindToDeviceActivity.this);
            } else if (msg.what == 4) {
                if (count < 200) {
                    roundProgress.setProgress((int) (count));
                } else {
                    roundProgress.setProgress((int) (max));
                    HomeActivity.toHomeActivity(RouterBindToDeviceActivity.this);
                }
            } else if (msg.what == 5) {
                showConnectionRouterDialog();
            }
        }
    };

    @OnClick({R.id.ivLeft})
    public void onViewClicked(View v) {
        switch (v.getId()) {
            case R.id.ivLeft:
                if (count == 0 || count >= 200) {
                    finish();
                } else {
                    showUpdateDialog();
                }
                break;
        }
    }

    @Override
    public void routerReturnInfo(String info, String topicurlString) {
        if (!"error".equals(info) && "setVictureBindCfg".equals(topicurlString)) {
            delayScan();
        } else if (!"error".equals(info) && "setUpgradeFW".equals(topicurlString)) {
            new TimeThread().start();
        } else if ("error".equals(info) || "".equals(info)) {
            Message message = new Message();
            message.what = 5;
            handler.sendMessage(message);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScanForPlan();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

    @Override
    public void onUpdateTimer(int seconds) {
        Message message = new Message();
        message.what = 1;
        message.arg1 = seconds;
        handler.sendMessage(message);
    }

    @Override
    public void onTimerFinish() {
        Message message = new Message();
        message.what = 3;
        handler.sendMessage(message);
    }

    @Override
    public void onScanDeviceSuccess() {
        Message message = new Message();
        message.what = 2;
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
     * 显示修改 对话框
     */
    private void showUpdateDialog() {
        if (null == firewareUpdateDialog) {
            firewareUpdateDialog = new FirewareUpdateDialog(this);
        }

        firewareUpdateDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (count == 0 || count >= 200) {
            finish();
        } else {
            showUpdateDialog();
        }
    }
}

