package com.afar.osaio.smart.router;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.bean.DetectionSchedule;
import com.afar.osaio.smart.device.bean.ParentalControlDeviceInfo;
import com.afar.osaio.smart.home.adapter.ParentalControlAdapter;
import com.afar.osaio.smart.setting.activity.RouterDeviceCreateDetectionScheduleActivity;
import com.afar.osaio.util.ConstantValue;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * 选择设备
 */
public class RouterAddParentalControlActivity extends RouterBaseActivity implements ParentalControlAdapter.OnClickParentalControlItemLicktener {

    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.recyviewItem)
    RecyclerView recyviewItem;

    //private AddParentalControlAdapter addParentalControlAdapter;
    private ParentalControlAdapter parentalControlAdapter;
    // 当前在线设备
    private List<ParentalControlDeviceInfo> onLinedeviceList = new ArrayList<>();
    // 受保护设备
    private List<ParentalControlDeviceInfo> deviceList = new ArrayList<>();
    private boolean isAdd = false;
    private JSONArray onlineMsgJson;

    public static void toRouterAddParentalControlActivity(Context from) {
        Intent intent = new Intent(from, RouterAddParentalControlActivity.class);
        from.startActivity(intent);
    }

    public static void toRouterAddParentalControlActivity(Activity activity, String onlineMsgString, List<ParentalControlDeviceInfo> deviceList) {
        Intent intent = new Intent(activity, RouterAddParentalControlActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_ONLINE_MSG, onlineMsgString);
        intent.putExtra(ConstantValue.INTENT_KEY_ONLINE_DEVIVE, (Serializable) deviceList);
        activity.startActivityForResult(intent, 1);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_router_parental_control);
        ButterKnife.bind(this);

        initView();
        initData();
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.router_add_parent_control_select_device);
        recyviewItem.setTag("");

        recyviewItem.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initData() {
        parentalControlAdapter = new ParentalControlAdapter();
        parentalControlAdapter.setParentalControlClickListener(this);
        recyviewItem.setAdapter(parentalControlAdapter);
        setDeviceListInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @OnClick({R.id.ivLeft})
    public void onViewClicked(View view) {
        switch(view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
        }
    }

    private void setDeviceListInfo() {
        try {
            ParentalControlDeviceInfo parentalControlDeviceInfo = null;
            if (getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_ONLINE_MSG) != null && !"".equals(getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_ONLINE_MSG))) {
                onlineMsgJson = new JSONArray(getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_ONLINE_MSG));
            } else {
                return;
            }

            deviceList = (List<ParentalControlDeviceInfo>)getCurrentIntent().getSerializableExtra(ConstantValue.INTENT_KEY_ONLINE_DEVIVE);
            if (deviceList == null || deviceList.size() <= 0) {
                for (int i = 0; i < onlineMsgJson.length(); i ++) {
                    parentalControlDeviceInfo = new ParentalControlDeviceInfo(onlineMsgJson.getJSONObject(i).getString("mac"),
                            onlineMsgJson.getJSONObject(i).getString("name"), "", "", null, false, false);
                    onLinedeviceList.add(parentalControlDeviceInfo);
                }
                //return;
            } else {
                for (int i = 0; i < deviceList.size(); i++) {
                    deviceList.get(i).setShowStatus(true);
                }
                onLinedeviceList.addAll(deviceList);
                for (int i = 0; i < onlineMsgJson.length(); i++) {
                    isAdd = false;
                    for (int j = 0; j < onLinedeviceList.size(); j++) {
                        if (onlineMsgJson.getJSONObject(i).getString("mac").equals(onLinedeviceList.get(j).getDeviceMac())) {
                            isAdd = false;
                            break;
                        } else {
                            isAdd = true;
                        }
                    }

                    if (isAdd) {
                        parentalControlDeviceInfo = new ParentalControlDeviceInfo(onlineMsgJson.getJSONObject(i).getString("mac"),
                                onlineMsgJson.getJSONObject(i).getString("name"), "", "", null, false, false);
                        onLinedeviceList.add(parentalControlDeviceInfo);
                    }
                }
            }
            parentalControlAdapter.setData(onLinedeviceList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void gotoCreateSchedule(DetectionSchedule schedule, boolean isProtected) {
        for (int i = 0; i < onLinedeviceList.size(); i++) {
            if (onLinedeviceList.get(i).getDeviceMac().equals(recyviewItem.getTag())) {
                if (!isProtected) {
                    int scheduleId = schedule != null ? schedule.getId() : 0;
                    RouterDeviceCreateDetectionScheduleActivity.toRouterDeviceCreateDetectionScheduleActivity(
                            RouterAddParentalControlActivity.this,
                            ConstantValue.REQUEST_CODE_SELECT_SCHEDULE,
                            schedule, scheduleId, onLinedeviceList.get(i));
                }
            }
        }
    }

    @Override
    public void ParentalControlItemClick(String deviceMac, String deviceName, boolean isProtected) {
        recyviewItem.setTag(deviceMac);
        gotoCreateSchedule(null, isProtected);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        finish();
        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void onStart() {
        super.onStart();
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
}
