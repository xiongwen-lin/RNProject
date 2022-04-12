package com.afar.osaio.smart.electrician.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.electrician.adapter.DeviceSortAdapter;
import com.afar.osaio.smart.electrician.adapter.DeviceTitleAdapter;
import com.afar.osaio.smart.electrician.bean.DeviceGroupingBean;
import com.afar.osaio.smart.electrician.bean.DeviceTypeBean;
import com.afar.osaio.smart.home.activity.HomeActivity;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.log.NooieLog;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * AddDeviceSelectActivity
 *
 * @author Administrator
 * @date 2019/6/19
 */
public class AddDeviceSelectLinkageActivity extends BaseActivity {

    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.rcvDeviceTitle)
    RecyclerView rcvDeviceTitle;
    @BindView(R.id.rcvDeviceType)
    RecyclerView rcvDeviceType;
    @BindView(R.id.mbDividerLine)
    View mbDividerLine;

    private boolean isFromLogin;
    private DeviceTitleAdapter mDeviceTitleAdapter;
    private DeviceSortAdapter mDeviceSortAdapter;
    private List<Object> deviceGroupings;

    public static void toAddDeviceSelectLinkageActivity(Activity from, boolean isFromLogin) {
        Intent intent = new Intent(from, AddDeviceSelectLinkageActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_IS_FROM_LOGIN, isFromLogin);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSlideable(false);
        setContentView(R.layout.activity_add_device_select_linkage);
        ButterKnife.bind(this);
        initView();
        initData();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mDeviceTitleAdapter.changeSelected(0);
        setupElectricalList();
        mDeviceSortAdapter.setData(deviceGroupings);
    }

    private void initData() {
        isFromLogin = getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_IS_FROM_LOGIN, false);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.add_device);
        mbDividerLine.setVisibility(View.VISIBLE);
        deviceGroupings = new ArrayList<>();
        setupDeviceTitleView();
        setupDeviceTypeView();
    }

    private void setupDeviceTitleView() {
        mDeviceTitleAdapter = new DeviceTitleAdapter();
        mDeviceTitleAdapter.setListener(new DeviceTitleAdapter.DeviceTitleListener() {
            @Override
            public void onItemClick(int position) {
                //mDeviceTitleAdapter.changeSelected(position);
                NooieLog.e("-----------设备类型  " + position);
                if (position == 0) {
                    setupElectricalList();
                } else if (position == 1) {
                    setupLightingList();
                } else if (position == 2) {
                    setupSmallApplicancesList();
                }
                mDeviceSortAdapter.setData(deviceGroupings);
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rcvDeviceTitle.setLayoutManager(layoutManager);
        rcvDeviceTitle.setAdapter(mDeviceTitleAdapter);

        List<String> titles = new ArrayList<>();
        titles.add(getResources().getString(R.string.electrical));
        titles.add(getResources().getString(R.string.lighting));
        //titles.add(getString(R.string.small_appliances));
        mDeviceTitleAdapter.setData(titles);
        mDeviceTitleAdapter.changeSelected(0);
    }

    private void setupDeviceTypeView() {
        mDeviceSortAdapter = new DeviceSortAdapter();
        setupElectricalList();
        mDeviceSortAdapter.setOnItemClickListener(new DeviceSortAdapter.OnItemClickListener() {
            @Override
            public void onClick(View itemView, int position, String name) {
                NooieLog.e("----------postion " + position + "  name " + name);
                if (name.equals("SP10") || name.equals("SP11") || name.equals("SP20") || name.equals("SP21") || name.equals("SP22") || name.equals("SP23") || name.equals("SP27") || name.equals("SP31") || (name.equals(getString(R.string.plug_lower) + "\n" + getString(R.string.ble_wifi)))) {
                    AddDeviceActivity.toAddDeviceActivity(AddDeviceSelectLinkageActivity.this, ConstantValue.ADD_DEVICE, isFromLogin, name);
                } else if (name.equals("SR40") || name.equals("SR41") || name.equals("SR42") || name.equals("SR43") || name.equals(getString(R.string.switch_teckin) + "\n(Wi-Fi)")) {
                    AddDeviceActivity.toAddDeviceActivity(AddDeviceSelectLinkageActivity.this, ConstantValue.ADD_SWITCH, isFromLogin, name);
                } else if (name.equals("SS30N") || name.equals("SS31") || name.equals("SS32") || name.equals("SS33") || name.equals("SS34") || name.equals("SS36") || name.equals("SS60") || name.equals("SS42") || name.equals(getString(R.string.power_strip) + "\n(Wi-Fi)")) {
                    AddDeviceActivity.toAddDeviceActivity(AddDeviceSelectLinkageActivity.this, ConstantValue.ADD_POWERSTRIP, isFromLogin, name);
                } else if (name.equals("SB30") || name.equals("SB50") || name.equals("SB53") || name.equals("SB60") || name.equals("FL41") || name.equals("DL46") || name.equals(getString(R.string.bulb) + "\n(Wi-Fi)") || name.equals(getString(R.string.bulb) + "\n" + getString(R.string.ble_wifi)) || name.equals(getString(R.string.floor_lamp) + "\n(Wi-Fi)")) {
                    AddDeviceActivity.toAddDeviceActivity(AddDeviceSelectLinkageActivity.this, ConstantValue.ADD_LAMP, isFromLogin, name);
                } else if (name.equals("SL02") || name.equals("SL07") || name.equals("SL08") || name.equals("SL12") || (name.equals(getString(R.string.strip_light) + "\n" + getString(R.string.ble_wifi)))) {
                    AddDeviceActivity.toAddDeviceActivity(AddDeviceSelectLinkageActivity.this, ConstantValue.ADD_LIGHT_STRIP, isFromLogin, name);
                } else if (name.equals("SR46") || name.equals(getString(R.string.dimmer_switch) + "\n(Wi-Fi)")) {
                    AddDeviceActivity.toAddDeviceActivity(AddDeviceSelectLinkageActivity.this, ConstantValue.ADD_LIGHT_MODULATOR, isFromLogin, name);
                } else if (name.equals(getString(R.string.pet_feeder) + "\n" + getString(R.string.ble_wifi))) {
                    AddDeviceActivity.toAddDeviceActivity(AddDeviceSelectLinkageActivity.this, ConstantValue.ADD_PET_FEEDER, isFromLogin, name);
                }
            }
        });
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (deviceGroupings.get(position) instanceof DeviceGroupingBean) {
                    return 3;
                } else {
                    return 1;
                }

            }
        });
        rcvDeviceType.setLayoutManager(layoutManager);
        rcvDeviceType.setAdapter(mDeviceSortAdapter);
        mDeviceSortAdapter.setData(deviceGroupings);
    }

    @OnClick({R.id.ivLeft})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ivLeft: {
                onBackPressed();
                break;
            }
        }
    }

    private void setupElectricalList() {
       /* DeviceGroupingBean indoorPlugBean = new DeviceGroupingBean();
        indoorPlugBean.setGroupingTitle(getResources().getString(R.string.indoor_plug));
        DeviceTypeBean sp10 = new DeviceTypeBean();
        sp10.setDeviceName("SP10");
        sp10.setDevicePic(R.drawable.sp10);
        DeviceTypeBean sp11 = new DeviceTypeBean();
        sp11.setDeviceName("SP11");
        sp11.setDevicePic(R.drawable.sp11);
        DeviceTypeBean sp20 = new DeviceTypeBean();
        sp20.setDeviceName("SP20");
        sp20.setDevicePic(R.drawable.sp20);
        DeviceTypeBean sp21 = new DeviceTypeBean();
        sp21.setDeviceName("SP21");
        sp21.setDevicePic(R.drawable.sp21);
        DeviceTypeBean sp22 = new DeviceTypeBean();
        sp22.setDeviceName("SP22");
        sp22.setDevicePic(R.drawable.sp22);
        DeviceTypeBean sp23 = new DeviceTypeBean();
        sp23.setDeviceName("SP23");
        sp23.setDevicePic(R.drawable.sp23);
        DeviceTypeBean sp27 = new DeviceTypeBean();
        sp27.setDeviceName("SP27");
        sp27.setDevicePic(R.drawable.sp27);
        DeviceTypeBean sp31 = new DeviceTypeBean();
        sp31.setDeviceName("SP31");
        sp31.setDevicePic(R.drawable.sp31);
        DeviceTypeBean ss30n = new DeviceTypeBean();
        ss30n.setDeviceName("SS30N");
        ss30n.setDevicePic(R.drawable.ss30n);
        DeviceTypeBean ss60 = new DeviceTypeBean();
        ss60.setDeviceName("SS60");
        ss60.setDevicePic(R.drawable.ss60);

        DeviceGroupingBean outdoorPlugBean = new DeviceGroupingBean();
        outdoorPlugBean.setGroupingTitle(getResources().getString(R.string.outdoor_plug));
        DeviceTypeBean ss31 = new DeviceTypeBean();
        ss31.setDeviceName("SS31");
        ss31.setDevicePic(R.drawable.ss31);
        DeviceTypeBean ss32 = new DeviceTypeBean();
        ss32.setDeviceName("SS32");
        ss32.setDevicePic(R.drawable.ss32);
        DeviceTypeBean ss33 = new DeviceTypeBean();
        ss33.setDeviceName("SS33");
        ss33.setDevicePic(R.drawable.ss33);
        DeviceTypeBean ss34 = new DeviceTypeBean();
        ss34.setDeviceName("SS34");
        ss34.setDevicePic(R.drawable.ss34);
        DeviceTypeBean ss36 = new DeviceTypeBean();
        ss36.setDeviceName("SS36");
        ss36.setDevicePic(R.drawable.ss36);
        DeviceTypeBean ss42 = new DeviceTypeBean();
        ss42.setDeviceName("SS42");
        ss42.setDevicePic(R.drawable.ss42);

        DeviceGroupingBean switchBean = new DeviceGroupingBean();
        switchBean.setGroupingTitle(getResources().getString(R.string.wall_switch));
        DeviceTypeBean sr41 = new DeviceTypeBean();
        sr41.setDeviceName("SR41");
        sr41.setDevicePic(R.drawable.sr41);
        DeviceTypeBean sr42 = new DeviceTypeBean();
        sr42.setDeviceName("SR42");
        sr42.setDevicePic(R.drawable.sr42);
        DeviceTypeBean sr43 = new DeviceTypeBean();
        sr43.setDeviceName("SR43");
        sr43.setDevicePic(R.drawable.sr43);
        DeviceTypeBean sr40 = new DeviceTypeBean();
        sr40.setDeviceName("SR40");
        sr40.setDevicePic(R.drawable.sr40);
        DeviceTypeBean sr46 = new DeviceTypeBean();
        sr46.setDeviceName("SR46");
        sr46.setDevicePic(R.drawable.sr46);

        deviceGroupings.clear();
        deviceGroupings.add(indoorPlugBean);
        deviceGroupings.add(sp10);
        deviceGroupings.add(sp11);
        deviceGroupings.add(sp20);
        deviceGroupings.add(sp21);
        deviceGroupings.add(sp22);
        deviceGroupings.add(sp23);
        deviceGroupings.add(sp27);
        deviceGroupings.add(sp31);
        deviceGroupings.add(ss30n);
        deviceGroupings.add(ss60);
        deviceGroupings.add(outdoorPlugBean);
        deviceGroupings.add(ss31);
        deviceGroupings.add(ss32);
        deviceGroupings.add(ss33);
        deviceGroupings.add(ss34);
        deviceGroupings.add(ss36);
        deviceGroupings.add(ss42);
        deviceGroupings.add(switchBean);
        deviceGroupings.add(sr40);
        deviceGroupings.add(sr41);
        deviceGroupings.add(sr42);
        deviceGroupings.add(sr43);
        deviceGroupings.add(sr46);*/

        DeviceGroupingBean space = new DeviceGroupingBean();
        DeviceTypeBean plug = new DeviceTypeBean();
        plug.setDeviceName(getString(R.string.plug_lower) + "\n" + getString(R.string.ble_wifi));
        plug.setDevicePic(R.drawable.ic_device_socket_single);
        DeviceTypeBean powerstrip = new DeviceTypeBean();
        powerstrip.setDeviceName(getString(R.string.power_strip) + "\n(Wi-Fi)");
        powerstrip.setDevicePic(R.drawable.ic_device_socket_multiple);
        DeviceTypeBean switchBean = new DeviceTypeBean();
        switchBean.setDeviceName(getString(R.string.switch_teckin) + "\n(Wi-Fi)");
        switchBean.setDevicePic(R.drawable.ic_device_switch);
        DeviceTypeBean dimmer = new DeviceTypeBean();
        dimmer.setDeviceName(getString(R.string.dimmer_switch) + "\n(Wi-Fi)");
        dimmer.setDevicePic(R.drawable.ic_device_switch_light);
        deviceGroupings.clear();
        deviceGroupings.add(plug);
        deviceGroupings.add(powerstrip);
        deviceGroupings.add(space);
        deviceGroupings.add(switchBean);
        deviceGroupings.add(dimmer);
    }

    private void setupLightingList() {
        /*DeviceGroupingBean bulbBean = new DeviceGroupingBean();
        bulbBean.setGroupingTitle(getResources().getString(R.string.bulb));
        DeviceTypeBean sb30 = new DeviceTypeBean();
        sb30.setDeviceName("SB30");
        sb30.setDevicePic(R.drawable.sb50);
        DeviceTypeBean sb50 = new DeviceTypeBean();
        sb50.setDeviceName("SB50");
        sb50.setDevicePic(R.drawable.sb50);
        DeviceTypeBean sb53 = new DeviceTypeBean();
        sb53.setDeviceName("SB53");
        sb53.setDevicePic(R.drawable.sb53);
        DeviceTypeBean sb60 = new DeviceTypeBean();
        sb60.setDeviceName("SB60");
        sb60.setDevicePic(R.drawable.sb50);
        DeviceTypeBean dl46 = new DeviceTypeBean();
        dl46.setDeviceName("DL46");
        dl46.setDevicePic(R.drawable.dl46);

        DeviceGroupingBean stripLightBean = new DeviceGroupingBean();
        stripLightBean.setGroupingTitle(getResources().getString(R.string.strip_light));
        DeviceTypeBean sl02 = new DeviceTypeBean();
        sl02.setDeviceName("SL02");
        sl02.setDevicePic(R.drawable.sl02);
        DeviceTypeBean sl07 = new DeviceTypeBean();
        sl07.setDeviceName("SL07");
        sl07.setDevicePic(R.drawable.sl02);
        DeviceTypeBean sl08 = new DeviceTypeBean();
        sl08.setDeviceName("SL08");
        sl08.setDevicePic(R.drawable.sl02);
        DeviceTypeBean sl12 = new DeviceTypeBean();
        sl12.setDeviceName("SL12");
        sl12.setDevicePic(R.drawable.sl02);

        DeviceGroupingBean floorLampBean = new DeviceGroupingBean();
        floorLampBean.setGroupingTitle(getResources().getString(R.string.floor_lamp));
        DeviceTypeBean fl41 = new DeviceTypeBean();
        fl41.setDeviceName("FL41");
        fl41.setDevicePic(R.drawable.fl41);

        deviceGroupings.clear();
        deviceGroupings.add(bulbBean);
        deviceGroupings.add(sb30);
        deviceGroupings.add(sb50);
        deviceGroupings.add(sb53);
        deviceGroupings.add(sb60);
        deviceGroupings.add(dl46);
        deviceGroupings.add(stripLightBean);
        deviceGroupings.add(sl02);
        deviceGroupings.add(sl07);
        deviceGroupings.add(sl08);
        deviceGroupings.add(sl12);
        deviceGroupings.add(floorLampBean);
        deviceGroupings.add(fl41);*/
        DeviceTypeBean lamp = new DeviceTypeBean();
        lamp.setDeviceName(getString(R.string.bulb) + "\n(Wi-Fi)");
        lamp.setDevicePic(R.drawable.ic_device_light);
        DeviceTypeBean lampBluetooth = new DeviceTypeBean();
        lampBluetooth.setDeviceName(getString(R.string.bulb) + "\n" + getString(R.string.ble_wifi));
        lampBluetooth.setDevicePic(R.drawable.ic_device_light);
        DeviceTypeBean strip = new DeviceTypeBean();
        strip.setDeviceName(getString(R.string.strip_light) + "\n" + getString(R.string.ble_wifi));
        strip.setDevicePic(R.drawable.ic_device_light_strip);
        DeviceTypeBean floorLamp = new DeviceTypeBean();
        floorLamp.setDeviceName(getString(R.string.floor_lamp) + "\n(Wi-Fi)");
        floorLamp.setDevicePic(R.drawable.ic_device_light_fill);
        deviceGroupings.clear();
        deviceGroupings.add(lamp);
        deviceGroupings.add(lampBluetooth);
        deviceGroupings.add(strip);
        deviceGroupings.add(floorLamp);
    }

    private void setupSmallApplicancesList() {
        DeviceTypeBean feeder = new DeviceTypeBean();
        feeder.setDeviceName(getString(R.string.pet_feeder) + "\n" + getString(R.string.ble_wifi));
        feeder.setDevicePic(R.drawable.ic_device_light_feeder);
        deviceGroupings.clear();
        deviceGroupings.add(feeder);
    }

    @Override
    public void onBackPressed() {
        if (isFromLogin) {
            HomeActivity.toHomeActivity(this);
            finish();
        }
        super.onBackPressed();
    }

}
