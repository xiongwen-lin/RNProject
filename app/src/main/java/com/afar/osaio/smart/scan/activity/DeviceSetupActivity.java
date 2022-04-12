package com.afar.osaio.smart.scan.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.widget.FButton;
import com.nooie.data.EventDictionary;
import com.nooie.eventtracking.EventTrackingApi;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DeviceSetupActivity extends BaseActivity {

    public static void toDeviceSetupActivity(Context from, String deviceId, String model) {
        Intent intent = new Intent(from,DeviceSetupActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        intent.putExtra(ConstantValue.INTENT_KEY_IPC_MODEL, model);
        from.startActivity(intent);
    }

    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.deviceSetupContainer)
    ScrollView deviceSetupContainer;
    @BindView(R.id.btnNavigation)
    FButton btnNavigation;

    private List<Integer> mOutdoorSetups = new ArrayList<>();
    private String mDeviceModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_setup);
        ButterKnife.bind(this);
        initView();
        switchCheckDeviceConnection(true);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void initView() {
        tvTitle.setText(R.string.device_setup_title);
        setupOutdoorSetups();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        switchCheckDeviceConnection(false);
        releaseRes();
    }

    @Override
    public void releaseRes() {
        super.releaseRes();
        tvTitle = null;
        deviceSetupContainer = null;
        btnNavigation = null;
    }

    private int mCurrentStep;
    private void setupOutdoorSetups() {
        mOutdoorSetups.add(R.layout.layout_device_setup_outdoor_1);
        mOutdoorSetups.add(R.layout.layout_device_setup_outdoor_2);
        //mOutdoorSetups.add(R.layout.layout_device_setup_outdoor_3);
        mOutdoorSetups.add(R.layout.layout_device_setup_outdoor_4);

        mCurrentStep = 0;
        View setupContainerView = LayoutInflater.from(this).inflate(mOutdoorSetups.get(mCurrentStep), deviceSetupContainer, false);
        deviceSetupContainer.addView(setupContainerView);
        btnNavigation.setText(getResources().getString(R.string.next));
    }

    @OnClick({R.id.ivLeft, R.id.btnNavigation})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ivLeft: {
                finish();
                break;
            }
            case R.id.btnNavigation: {
                if (mCurrentStep >= mOutdoorSetups.size() - 1) {
                    String deviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
                    String model = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_IPC_MODEL);
                    if (!TextUtils.isEmpty(deviceId)) {
                        NooieNameDeviceActivity.toNooieNameDeviceActivity(DeviceSetupActivity.this, deviceId, model);
                    }
                    finish();
                    break;
                }
                mCurrentStep++;
                deviceSetupContainer.removeAllViews();
                View setupContainerView = LayoutInflater.from(this).inflate(mOutdoorSetups.get(mCurrentStep), deviceSetupContainer, false);
                deviceSetupContainer.addView(setupContainerView);
                btnNavigation.setText(mCurrentStep == mOutdoorSetups.size() - 1 ? getResources().getString(R.string.ok) : getResources().getString(R.string.next));
                String eventId = null;
                String pageId = null;
                if (mCurrentStep == 1) {
                    eventId = EventDictionary.EVENT_ID_ACCESS_DEVICE_SETUP_2;
                    pageId = EventDictionary.EVENT_PAGE_DEVICE_SETUP_2;
                } else if (mCurrentStep == 2) {
                    eventId = EventDictionary.EVENT_ID_ACCESS_DEVICE_SETUP_3;
                    pageId = EventDictionary.EVENT_PAGE_DEVICE_SETUP_3;
                }
                if (!TextUtils.isEmpty(eventId) && !TextUtils.isEmpty(pageId)) {
                    EventTrackingApi.getInstance().trackNormalEvent(eventId, pageId, EventDictionary.EVENT_CODE_NONE, "", "");
                }
                break;
            }
        }
    }
}
