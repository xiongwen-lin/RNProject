package com.afar.osaio.smart.electrician.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.afar.osaio.R;
import com.afar.osaio.application.activity.WebViewActivity;
import com.afar.osaio.smart.bluetooth.activity.BaseBluetoothActivity;
import com.afar.osaio.smart.electrician.manager.PowerStripHelper;
import com.afar.osaio.smart.electrician.presenter.IScanDevicePresenter;
import com.afar.osaio.smart.electrician.presenter.ScanDevicePresenter;
import com.afar.osaio.smart.electrician.util.CommonUtil;
import com.afar.osaio.smart.electrician.view.IScanDeviceView;
import com.afar.osaio.smart.electrician.widget.RoundProgressBar;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.nooie.common.hardware.bluetooth.BluetoothHelper;
import com.nooie.common.utils.configure.FontUtil;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.sdk.bean.DeviceBean;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * ScanDeviceActivity
 *
 * @author Administrator
 * @date 2019/3/5
 */
public class ScanDeviceActivity extends BaseBluetoothActivity implements IScanDeviceView {

    @BindView(R.id.scanFailedContainer)
    ScrollView scanFailedContainer;
    @BindView(R.id.btnScanConnectAp)
    Button btnScanConnectAp;
    @BindView(R.id.tvScanFailedGuideInfo3)
    TextView tvScanFailedGuideInfo3;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;

    @BindView(R.id.roundProgressBar)
    RoundProgressBar roundProgressBar;
    @BindView(R.id.tvConnect)
    TextView tvConnect;

    private String mSSID;
    private String mPsd;
    private int mConMode;
    private String mToken;
    private IScanDevicePresenter mScanDevPresenter;

    private DeviceBean mDeviceBean;
    private String mAddType;
    private int deviceType;
    private String uuid;
    private String address;
    private String mac;
    private Dialog mShowBluetoothDisconnectDialog = null;

    public static void toScanDeviceActivity(Context from, String ssid, String psd, int mode, String token, String addType) {
        Intent intent = new Intent(from, ScanDeviceActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_PSD, psd);
        intent.putExtra(ConstantValue.INTENT_KEY_SSID, ssid);
        intent.putExtra(ConstantValue.INTENT_KEY_CONFIG_MODE, mode);
        intent.putExtra(ConstantValue.INTENT_KEY_TOKEN, token);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ADD_TYPE, addType);
        from.startActivity(intent);
    }

    public static void toScanDeviceActivity(Context from, String ssid, String psd, int mode, String token, String addType,int deviceType,String uuid,String address,String mac) {
        Intent intent = new Intent(from, ScanDeviceActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_PSD, psd);
        intent.putExtra(ConstantValue.INTENT_KEY_SSID, ssid);
        intent.putExtra(ConstantValue.INTENT_KEY_CONFIG_MODE, mode);
        intent.putExtra(ConstantValue.INTENT_KEY_TOKEN, token);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ADD_TYPE, addType);
        intent.putExtra(ConstantValue.INTENT_KEY_BLUE_DEVICE_TYPE, deviceType);
        intent.putExtra(ConstantValue.INTENT_KEY_BLUE_DEVICE_UUID, uuid);
        intent.putExtra(ConstantValue.INTENT_KEY_BLUE_DEVICE_ADDRESS, address);
        intent.putExtra(ConstantValue.INTENT_KEY_BLUE_DEVICE_MAC, mac);
        from.startActivity(intent);
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_device);
        ButterKnife.bind(this);
        initData();
        initView();
        startScan();
        setupClickableTv();
    }

    //开始添加设备
    private void startScan() {
        mScanDevPresenter.startDeviceSearch(mConMode, mSSID, mPsd, mToken,deviceType,uuid,address,mac);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        NooieLog.e("------------ScanDeviceActivity onNewIntent");
        if (intent != null) {
            mSSID = intent.getStringExtra(ConstantValue.INTENT_KEY_SSID);
            mPsd = intent.getStringExtra(ConstantValue.INTENT_KEY_PSD);
            mConMode = intent.getIntExtra(ConstantValue.INTENT_KEY_CONFIG_MODE, ConstantValue.EC_MODE);
            mToken = intent.getStringExtra(ConstantValue.INTENT_KEY_TOKEN);
            mAddType = intent.getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ADD_TYPE);
        }
        roundProgressBar.setDirection(RoundProgressBar.Direction.FORWARD);
        startScan();
    }

    @Override
    public void onResume() {
        super.onResume();
        NooieLog.e("----->>>> onResume mSSID " + mSSID + "  mPsd " + mPsd + " mToken " + mToken + "  mAddType " + mAddType);
        keepScreenLongLight();
    }

    @Override
    public void onPause() {
        super.onPause();
        clearKeepScreenLongLight();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mScanDevPresenter.release();
        roundProgressBar.release();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.pairing_please_wait);
        roundProgressBar.setProgressChangeListener(new RoundProgressBar.ProgressChangeListener() {
            @Override
            public void onFinish() {

            }

            @Override
            public void onProgressChanged(int progress) {
                if (progress == 100) {
                    NooieLog.e("------>>>100 finish");
                    ScanFailedActivity.toScanFailedActivity(ScanDeviceActivity.this, mSSID, mPsd, mAddType);
                }
            }
        });
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
        } else {
            mSSID = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SSID);
            mPsd = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_PSD);
            mConMode = getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_CONFIG_MODE, ConstantValue.EC_MODE);
            mToken = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_TOKEN);
            mAddType = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ADD_TYPE);
            mScanDevPresenter = new ScanDevicePresenter(this);
            if ( mConMode == ConstantValue.BLUE_MODE){
                deviceType = getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_BLUE_DEVICE_TYPE,301); //301双模蓝牙
                uuid = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_BLUE_DEVICE_UUID);
                address = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_BLUE_DEVICE_ADDRESS);
                mac = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_BLUE_DEVICE_MAC);
                initBle();
                if (!BluetoothHelper.isBluetoothOn()){
                    stopDeviceSearchBlue();
                    BluetoothHelper.startBluetooth(this, ConstantValue.REQUEST_CODE_FOR_ENABLE_BLUE);
                }
            }
        }
    }

    @OnClick({R.id.btnScanReset, R.id.btnScanConnectAp, R.id.ivLeft})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.btnScanReset: {
                ResetDeviceActivity.toResetDeviceActivity(ScanDeviceActivity.this, mAddType);
                finish();
                break;
            }
            case R.id.btnScanConnectAp: {
                ConnectInApModeActivity.toConnectInApModeActivity(ScanDeviceActivity.this, mSSID, mPsd, mAddType);
                finish();
                break;
            }
            case R.id.ivLeft: {
                finish();
                break;
            }
        }
    }

    //配网成功
    @Override
    public void onDeviceSearchSuccess(DeviceBean deviceBean) {
        NooieLog.e("------>>> onDeviceSearchSuccess  " + String.valueOf(deviceBean));
        mDeviceBean = deviceBean;
        //todo 显示成功图案，进入命名界面
        tvTitle.setText(R.string.paired_ok);
        tvConnect.setText(getResources().getString(R.string.connecting_succeeded));
        roundProgressBar.setSuccess();
        toNameDevActivity();
    }

    //配网失败
    @Override
    public void onDeviceSearchFailed() {
        //todo 显示配网失败
        NooieLog.e("------>>> onDeviceSearchFailed  ");
        ScanFailedActivity.toScanFailedActivity(ScanDeviceActivity.this, mSSID, mPsd, mAddType);
    }

    private void toNameDevActivity() {
        CommonUtil.delayAction(1000 * 2, new CommonUtil.OnDelayTimeFinishListener() {
            @Override
            public void onFinish() {
                if (PowerStripHelper.getInstance().isLamp(mDeviceBean)) {
                    if (PowerStripHelper.getInstance().isLampStrip(mDeviceBean)) {//灯带
                        NameDeviceActivity.toNameDeviceActivity(ScanDeviceActivity.this, mDeviceBean.getDevId(), ConstantValue.ADD_LIGHT_STRIP,mDeviceBean.getProductId());
                    } else if (PowerStripHelper.getInstance().isLightModulator(mDeviceBean)) {//调光器
                        NameDeviceActivity.toNameDeviceActivity(ScanDeviceActivity.this, mDeviceBean.getDevId(), ConstantValue.ADD_LIGHT_MODULATOR,mDeviceBean.getProductId());
                    } else {//灯
                        NameDeviceActivity.toNameDeviceActivity(ScanDeviceActivity.this, mDeviceBean.getDevId(), ConstantValue.ADD_LAMP,mDeviceBean.getProductId());
                    }
                } else if (PowerStripHelper.getInstance().isPlug(mDeviceBean)) {//单插
                    NameDeviceActivity.toNameDeviceActivity(ScanDeviceActivity.this, mDeviceBean.getDevId(), ConstantValue.ADD_DEVICE,mDeviceBean.getProductId());
                } else if (PowerStripHelper.getInstance().isPowerStrip(mDeviceBean)
                        || PowerStripHelper.getInstance().isDimmerPlug(mDeviceBean)) {//排插
                    NameDeviceActivity.toNameDeviceActivity(ScanDeviceActivity.this, mDeviceBean.getDevId(), ConstantValue.ADD_POWERSTRIP,mDeviceBean.getProductId());
                } else if (PowerStripHelper.getInstance().isWallSwitch(mDeviceBean)
                        || PowerStripHelper.getInstance().isMultiWallSwitch(mDeviceBean)) {//开关
                    NameDeviceActivity.toNameDeviceActivity(ScanDeviceActivity.this, mDeviceBean.getDevId(), ConstantValue.ADD_SWITCH,mDeviceBean.getProductId());
                } else if (PowerStripHelper.getInstance().isPetFeeder(mDeviceBean)) {
                    NameDeviceActivity.toNameDeviceActivity(ScanDeviceActivity.this, mDeviceBean.getDevId(), ConstantValue.ADD_PET_FEEDER,mDeviceBean.getProductId());
                } else if (PowerStripHelper.getInstance().isAirPurifier(mDeviceBean)) {
                    NameDeviceActivity.toNameDeviceActivity(ScanDeviceActivity.this, mDeviceBean.getDevId(), ConstantValue.ADD_AIR_PURIFIER,mDeviceBean.getProductId());
                } else {
                    NooieLog.e("----------------mDeviceBean  " + mDeviceBean);
                    WrongDeviceActivity.toWrongDeviceActivity(ScanDeviceActivity.this, mDeviceBean.getDevId());
                }
            }
        });
    }

    private void setupClickableTv() {
        final SpannableStringBuilder style = new SpannableStringBuilder();
        String faq = getString(R.string.faq);
        String text = String.format(getString(R.string.scan_failed_guide_info_3), faq);

        //设置文字
        style.append(text);

        //设置部分文字点击事件
        ClickableSpan conditionClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                WebViewActivity.toWebViewActivity(ScanDeviceActivity.this, "file:///android_asset/web/FQA.html", getString(R.string.faq));
            }
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setUnderlineText(false);
                ds.setTypeface(FontUtil.loadTypeface(getApplicationContext(), "fonts/manrope-semibold.otf"));
            }
        };
        style.setSpan(conditionClickableSpan, text.indexOf(faq), text.indexOf(faq) + faq.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvScanFailedGuideInfo3.setText(style);

        //设置部分文字颜色
        ForegroundColorSpan conditionForegroundColorSpan = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.theme_gray));
        style.setSpan(conditionForegroundColorSpan, text.indexOf(faq), text.indexOf(faq) + faq.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


        //配置给TextView
        tvScanFailedGuideInfo3.setMovementMethod(LinkMovementMethod.getInstance());
        tvScanFailedGuideInfo3.setText(style);
    }

    @Override
    public void bluetoothStateOffChange() { //蓝牙关闭
        NooieLog.d("-->> debug AddDeviceActivity bluetoothStateOffChange() ");
        if ( mConMode == ConstantValue.BLUE_MODE) {
            stopDeviceSearchBlue();
            showBluetoothDisconnectDialog();
        }
    }

    private void stopDeviceSearchBlue() {
        mScanDevPresenter.setDeviceBlueState(uuid,false);
    }

    /**
     * 蓝牙断开，请求重连
     */
    private void showBluetoothDisconnectDialog() {
        if (mShowBluetoothDisconnectDialog != null) {
            mShowBluetoothDisconnectDialog.dismiss();
            mShowBluetoothDisconnectDialog = null;
        }
        mShowBluetoothDisconnectDialog = DialogUtils.showInformationDialog(this, getString(R.string.bluetooth_scan_operation_tip_disconnect_title), getString(R.string.bluetooth_scan_operation_tip_disconnect_content), getString(R.string.bluetooth_scan_operation_tip_disconnect_ok), false, false, new DialogUtils.OnClickInformationDialogLisenter() {
            @Override
            public void onConfirmClick() {
                BluetoothHelper.startBluetooth(ScanDeviceActivity.this, ConstantValue.REQUEST_CODE_FOR_ENABLE_BLUE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mConMode == ConstantValue.BLUE_MODE && requestCode == ConstantValue.REQUEST_CODE_FOR_ENABLE_BLUE) {
            if (resultCode == RESULT_OK) {
                startScan();
                mScanDevPresenter.setDeviceBlueState(uuid,true);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
