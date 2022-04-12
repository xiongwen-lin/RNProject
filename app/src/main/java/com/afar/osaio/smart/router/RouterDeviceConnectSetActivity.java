package com.afar.osaio.smart.router;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afar.osaio.R;
import com.afar.osaio.bean.DetectionSchedule;
import com.afar.osaio.routerdata.GetRouterDataFromCloud;
import com.afar.osaio.routerdata.SendHttpRequest;
import com.afar.osaio.smart.setting.activity.RouterDeviceCreateDetectionScheduleActivity;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.afar.osaio.widget.InputFrameView;
import com.suke.widget.SwitchButton;

import org.json.JSONException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RouterDeviceConnectSetActivity extends RouterBaseActivity implements SendHttpRequest.getRouterReturnInfo {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.switchButton)
    SwitchButton switchButton;
    @BindView(R.id.maxUploadSpeed)
    InputFrameView maxUploadSpeed;
    @BindView(R.id.maxDownSpeed)
    InputFrameView maxDownSpeed;
    @BindView(R.id.upload_line)
    View upload_line;
    @BindView(R.id.download_line)
    View download_line;
    @BindView(R.id.layout_upload)
    View layout_upload;
    @BindView(R.id.layout_download)
    View layout_download;
    @BindView(R.id.parental_control)
    View parental_control;
    @BindView(R.id.tvParentalControl)
    TextView tvParentalControl;
    @BindView(R.id.deviceName)
    TextView deviceName;
    @BindView(R.id.wifiType)
    TextView wifiType;

    private DetectionSchedule detectionSchedule;
    private String deviceNameString = "";
    private String ip = "";
    private String mac = "";
    private String wifiTypeString = "";
    private String isWhite = "1"; // 1为选中

    public static void toRouterDeviceConnectSetActivity(Activity from, String deviceName, String ip, String mac, String wifiType, String isWhite) {
        Intent intent = new Intent(from, RouterDeviceConnectSetActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_NAME, deviceName);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_IP, ip);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_MAC, mac);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_WIFI_TYPE, wifiType);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_IS_WHITE, isWhite);
        from.startActivityForResult(intent, 1);
        //from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router_device_connect_set);
        ButterKnife.bind(this);

        initView();
        initData();
    }

    private void initView(){
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText("--");

        switchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                setViewShow(isChecked);
                if (isChecked){
                    isWhite = "1";
                }else{
                    isWhite = "0";
                }
            }
        });

        setupInputFrameView();
    }

    private void initData() {
        deviceNameString = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_NAME);
        ip = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_IP);
        mac = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_MAC);
        wifiTypeString = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_WIFI_TYPE);
        switchButton.setChecked("1".equals(getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_IS_WHITE)) ? true : false);
        tvTitle.setText(deviceNameString);
        wifiType.setText(wifiTypeString);
        if (deviceNameString != null && !"".equals(deviceNameString)) {
            deviceName.setText(deviceNameString);
        }

        isWhite = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_IS_WHITE);
    }

    private void setupInputFrameView() {
        maxUploadSpeed.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT)
                .setInputBtnType(InputFrameView.INPUT_BTN_TYPE_TEXT)
                .setHintTexe("")
                .setInputBtnIsShow(true)
                .setTextInputBtnColor(R.color.gray_cc616161)
                .setOnClickListener(new InputFrameView.OnInputFrameClickListener() {
                    @Override
                    public void onInputBtnClick() {
                    }

                    @Override
                    public void onEditorAction() {
                        hideInputMethod();
                    }

                    @Override
                    public void onEtInputClick() {
                    }
                })
                .setInputTextChangeListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });

        maxDownSpeed.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT)
                .setInputBtnType(InputFrameView.INPUT_BTN_TYPE_TEXT)
                .setHintTexe("")
                .setInputBtnIsShow(true)
                .setTextInputBtnColor(R.color.gray_cc616161)
                .setOnClickListener(new InputFrameView.OnInputFrameClickListener() {
                    @Override
                    public void onInputBtnClick() {
                    }

                    @Override
                    public void onEditorAction() {
                        hideInputMethod();
                    }

                    @Override
                    public void onEtInputClick() {
                    }
                })
                .setInputTextChangeListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
    }

    private void setViewShow(boolean isShow) {
        /*layout_download.setVisibility(isShow ? View.VISIBLE : View.GONE);
        layout_upload.setVisibility(isShow ? View.VISIBLE : View.GONE);
        upload_line.setVisibility(isShow ? View.VISIBLE : View.GONE);
        download_line.setVisibility(isShow ? View.VISIBLE : View.GONE);*/
        setDeviceAccessDeviceCfg(isShow);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        detectionSchedule = getDetectionScheduleInfo();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ConstantValue.REQUEST_CODE_SELECT_SCHEDULE:
                    if (intent != null) {
                        DetectionSchedule schedule = (DetectionSchedule)intent.getSerializableExtra(ConstantValue.INTENT_KEY_DATA_TYPE);
                        updateDetectionSchedule(schedule);
                    }
                    break;
                case 1:
                    deviceNameString = intent.getStringExtra(ConstantValue.INTENT_KEY_DEVICE_NAME);
                    deviceName.setText(deviceNameString);
                    setAccessDeviceCfg();
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    @OnClick({R.id.ivLeft, R.id.btnSave, R.id.device_details_info, R.id.parental_control, R.id.layout_name})
    public void onViewClicked(View view) {
        switch(view.getId()) {
            case R.id.ivLeft:
                setIntentResult();
                break;
            case R.id.layout_name:
                RouterResetNameActivity.toRouterResetNameActivity(RouterDeviceConnectSetActivity.this, "device", deviceName.getText().toString());
                break;
            case R.id.btnSave:
                setDeviceSpeed();
                break;
            case R.id.device_details_info:
                ConnectionDeviceDetailsActivity.toConnectionDeviceDetailsActivity(this, deviceNameString, ip, mac);
                break;
            case R.id.parental_control:
                setRouterTimeContorl(detectionSchedule);
                break;
        }
    }

    private void setAccessDeviceCfg() {
        showLoadingDialog();
        GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
        try {
            routerDataFromCloud.setAccessDeviceCfg("0", "white", "1", deviceNameString, mac);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setDeviceAccessDeviceCfg(boolean isShow) {
        showLoadingDialog();
        GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
        try {
            routerDataFromCloud.setAccessDeviceCfg(mac, "1", isShow ? "white" : "black");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setDeviceSpeed() {
        if (maxUploadSpeed.getInputTextNoTrim().length() > 0 || maxDownSpeed.getInputTextNoTrim().length() > 0) {
            showLoadingDialog();
            GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
            try {
                routerDataFromCloud.setAddQos(ip, maxUploadSpeed.getInputTextNoTrim(), maxDownSpeed.getInputTextNoTrim());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            setIntentResult();
        }
    }

    private void setIntentResult() {
        Intent intent = new Intent();
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_NAME, deviceNameString);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_IS_WHITE,isWhite);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void setRouterTimeContorl(DetectionSchedule schedule) {
        schedule.setEffective(true);
        int scheduleId = schedule != null ? schedule.getId() : 0;
        RouterDeviceCreateDetectionScheduleActivity.toRouterDeviceCreateDetectionScheduleActivity(RouterDeviceConnectSetActivity.this, ConstantValue.REQUEST_CODE_SELECT_SCHEDULE, schedule, scheduleId);
    }

    private DetectionSchedule getDetectionScheduleInfo() {
        String strTime = tvParentalControl.getText().toString();
        String[] timeList = strTime.split(" - ");
        String[] startTimeString = timeList[0].split(":");
        String[] endTimeString = timeList[1].split(":");
        int startH = Integer.parseInt(startTimeString[0]);
        int startM = Integer.parseInt(startTimeString[1]);
        int endH = Integer.parseInt(endTimeString[0]);
        int endM = Integer.parseInt(endTimeString[1]);

        int startTime = startH * 60 + startM;
        int endTime = endH * 60 + endM;

        DetectionSchedule detectionSchedule = new DetectionSchedule(startTime, endTime, true);
        return detectionSchedule;
    }

    private void updateDetectionSchedule(DetectionSchedule schedule) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(schedule.getStartH())
                    .append(":")
                    .append(schedule.getStartM())
                    .append(" - ")
                    .append(schedule.getEndH())
                    .append(":")
                    .append(schedule.getEndM());
        tvParentalControl.setText(stringBuffer);
    }

    private Dialog mShowConnectionRouterDialog;
    private void showConnectionRouterDialog() {
        hideBackupRouterDialog();
        mShowConnectionRouterDialog = DialogUtils.connectionRouterFairDialog(this, new DialogUtils.OnClickConfirmButtonListener() {

            @Override
            public void onClickLeft() {
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

    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                showConnectionRouterDialog();
            }
        }
    };

    @Override
    public void routerReturnInfo(String info, String topicurlString) {
        hideLoadingDialog();
        if (!"error".equals(info) && "setAccessDeviceCfg".equals(topicurlString)) {

        } else if (!"error".equals(info) && "setSmartQosCfg".equals(topicurlString)) {
            setIntentResult();
        } else if ("error".equals(info) || "".equals(info)) {
            Message message = new Message();
            message.what = 0;
            handler.sendMessage(message);
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
}
