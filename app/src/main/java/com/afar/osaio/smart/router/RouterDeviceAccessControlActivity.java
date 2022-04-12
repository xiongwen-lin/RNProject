package com.afar.osaio.smart.router;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.routerdata.GetRouterDataFromCloud;
import com.afar.osaio.routerdata.SendHttpRequest;
import com.afar.osaio.smart.device.bean.RouterDeviceAccessControlInfo;
import com.afar.osaio.smart.home.adapter.RouterDeviceAccessControlAdapter;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RouterDeviceAccessControlActivity extends RouterBaseActivity implements RouterDeviceAccessControlAdapter.OnAccessControlClick,
        SendHttpRequest.getRouterReturnInfo {

    public static void toRouterDeviceAccessControlActivity(Context from, String onlineMsgString) {
        Intent intent = new Intent(from, RouterDeviceAccessControlActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_ONLINE_MSG, onlineMsgString);
        from.startActivity(intent);
    }

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.device_recy)
    RecyclerView device_recy;
    @BindView(R.id.newDevice)
    TextView newDevice;
    @BindView(R.id.whiteList)
    TextView whiteList;
    @BindView(R.id.blackList)
    TextView blackList;
    @BindView(R.id.layout_view)
    View layout_view;
    @BindView(R.id.mode_type)
    View mode_type;
    @BindView(R.id.textTips)
    TextView textTips;
    @BindView(R.id.passwordMode)
    TextView passwordMode;
    @BindView(R.id.authMode)
    TextView authMode;

    private RouterDeviceAccessControlAdapter routerDeviceAccessControlAdapter;
    private List<RouterDeviceAccessControlInfo> routerDeviceAccessControlInfoList = new ArrayList<>();
    private JSONArray onlineMsgJson;
    private JSONArray ruleJson;
    private String routerReturnString = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router_access_control);
        ButterKnife.bind(this);

        initView();
        initData();
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.router_access_control_setting);
        layout_view.setTag(1);
        passwordMode.setTag(true);
        device_recy.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initData() {
        routerDeviceAccessControlAdapter = new RouterDeviceAccessControlAdapter();
        routerDeviceAccessControlAdapter.setOnAccessControlListener(this);
        device_recy.setAdapter(routerDeviceAccessControlAdapter);
        setDeviceAccessControlData();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void setDeviceAccessControlData() {
        if (routerDeviceAccessControlInfoList == null) {
            return;
        }

        try {
            if (getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_ONLINE_MSG) != null && !"".equals(getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_ONLINE_MSG))) {
                onlineMsgJson = new JSONArray(getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_ONLINE_MSG));
            } else {
                return;
            }

            for (int i = 0; i < onlineMsgJson.length(); i++) {
                RouterDeviceAccessControlInfo controlDeviceInfo = null;
                controlDeviceInfo = new RouterDeviceAccessControlInfo(onlineMsgJson.getJSONObject(i).getString("mac"),
                        onlineMsgJson.getJSONObject(i).getString("name"),
                        "8:00 - 13:00", "",1);
                routerDeviceAccessControlInfoList.add(controlDeviceInfo);
            }
            routerDeviceAccessControlAdapter.setData(routerDeviceAccessControlInfoList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     *
     * @param position
     * @param deviceMac
     * @param deviceName
     * @param isSure
     */
    @Override
    public void dealwithItem(int position, String deviceMac, String deviceName, boolean isSure, String ruleName) {
        dealwithClickDeviceInfo(position, deviceMac, deviceName, isSure, ruleName);
    }

    @OnClick({R.id.ivLeft, R.id.newDevice, R.id.whiteList, R.id.blackList, R.id.passwordMode, R.id.authMode})
    public void onViewClicked(View view) {
        switch(view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.newDevice:
                setViewShow(true, 1);
                break;
            case R.id.whiteList:
                setViewShow(true, 2);
                break;
            case R.id.blackList:
                setViewShow(true, 3);
                break;
            case R.id.passwordMode:
                setViewShow(true, 0);
                break;
            case R.id.authMode:
                setViewShow(false, 0);
                break;
        }
    }

    private void setDeviceList() {

    }

    private void dealwithClickDeviceInfo(int position, String deviceMac, String deviceName, boolean isSure, String ruleName) {

        if (isSure) {
            // 只有新设备才有sure 设置白名单接口
        } else {
            // 新设备列表
            if ((int)layout_view.getTag() == 1) {
                // 设置黑名单
                setBlackListRule(deviceMac, deviceName, position);
            } else if ((int)layout_view.getTag() == 2) {
                // 删除白名单
            } else {
                // 删除黑名单
                //deletBlackListRule(ruleName);
            }
        }
    }

    private void setBlackListRule(String deviceMac, String deviceName, int position) {
        routerDeviceAccessControlAdapter.removeDevice(position);
        try {
            GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
            routerDataFromCloud.setMacFilterRules(deviceMac, deviceName, "1");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getBlackListRule() {
        try {
            GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
            routerDataFromCloud.getMacFilterRules();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void deletBlackListRule(String ruleName) {
        try {
            GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
            routerDataFromCloud.delMacFilterRules(ruleName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getBlacklistRules(String info) {
        // 标题
        RouterDeviceAccessControlInfo accessControlInfo = new RouterDeviceAccessControlInfo("", "", "" , "",1);
        List<RouterDeviceAccessControlInfo>  deviceAccessControlInfos= new ArrayList<>();
        deviceAccessControlInfos.add(accessControlInfo);
        if (info == null || "".equals(info)) {
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject(info);
            ruleJson = jsonObject.getJSONArray("rule");
            if (ruleJson.length() <= 0) {
                routerDeviceAccessControlAdapter.setData(deviceAccessControlInfos);
                return;
            }
            for (int i = 0; i < ruleJson.length(); i++) {
                RouterDeviceAccessControlInfo routerDeviceInfo = new RouterDeviceAccessControlInfo(ruleJson.getJSONObject(i).getString("mac"),
                        ruleJson.getJSONObject(i).getString("desc"),
                        "",
                        ruleJson.getJSONObject(i).getString("delRuleName"),
                        3);
                deviceAccessControlInfos.add(routerDeviceInfo);
            }
            routerDeviceAccessControlAdapter.setData(deviceAccessControlInfos);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setViewShow(boolean isShow, int type) {
        if (type == 0) {
            if ((boolean)passwordMode.getTag() == true) {
                showAccessRouterModeDialog(isShow);
            } else {
                passwordMode.setTag(true);
                setAccessModeView(isShow);
            }
        } else {
            newDevice.setTextColor(type == 1 ? getResources().getColor(R.color.theme_green) : getResources().getColor(R.color.gray_6B7487));
            whiteList.setTextColor(type == 2 ? getResources().getColor(R.color.theme_green) : getResources().getColor(R.color.gray_6B7487));
            blackList.setTextColor(type == 3 ? getResources().getColor(R.color.theme_green) : getResources().getColor(R.color.gray_6B7487));
        }

        if (type > 0)
            layout_view.setTag(type);
    }

    private void setAccessModeView(boolean isShow) {
        mode_type.setVisibility(isShow ? View.INVISIBLE : View.VISIBLE);
        device_recy.setVisibility(isShow ? View.INVISIBLE : View.VISIBLE);
        textTips.setVisibility(!isShow ? View.INVISIBLE : View.VISIBLE);
        passwordMode.setBackgroundResource(isShow ? R.drawable.button_round_blue_state : R.drawable.bg_round_gray_state);
        authMode.setBackgroundResource(isShow ? R.drawable.bg_round_gray_state : R.drawable.button_round_blue_state);
    }

    private Dialog mShowAccessRouterModeDialog;
    private void showAccessRouterModeDialog(boolean isShow) {
        hideRebootRouterDialog();
        mShowAccessRouterModeDialog = DialogUtils.accessRouterModeDialog(this, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                passwordMode.setTag(false);
                setAccessModeView(isShow);
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
        if (mShowAccessRouterModeDialog != null) {
            mShowAccessRouterModeDialog.dismiss();
            mShowAccessRouterModeDialog = null;
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

    private Handler handler = new Handler(){
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
        if ("getMacFilterRules".equals(topicurlString) && !"error".equals(info)) {
            getBlacklistRules(info);
        } else if ("setMacFilterRules".equals(topicurlString)) {

        } else if ("error".equals(info) || "".equals(info)) {
            Message message = new Message();
            message.what = 0;
            handler.sendMessage(message);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
