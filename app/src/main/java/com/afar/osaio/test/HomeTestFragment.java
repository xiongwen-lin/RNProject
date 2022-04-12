package com.afar.osaio.test;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieBaseMainFragment;
import com.afar.osaio.bean.ApDeviceInfo;
import com.afar.osaio.smart.device.bean.ListDeviceItem;
import com.afar.osaio.smart.device.helper.SmartDeviceHelper;
import com.afar.osaio.smart.electrician.eventbus.WeatherEvent;
import com.afar.osaio.smart.electrician.manager.PowerStripHelper;
import com.afar.osaio.smart.event.HomeActionEvent;
import com.afar.osaio.smart.home.activity.HomeActivity;
import com.afar.osaio.smart.home.adapter.SmartNormalDeviceAdapter;
import com.afar.osaio.smart.home.adapter.listener.SmartNormalDeviceListener;
import com.afar.osaio.smart.home.bean.SmartBaseDevice;
import com.afar.osaio.smart.home.bean.SmartCameraDevice;
import com.afar.osaio.smart.home.bean.SmartTyDevice;
import com.afar.osaio.smart.home.contract.SmartDeviceListContract;
import com.afar.osaio.smart.home.contract.SmartIpcDeviceContract;
import com.afar.osaio.smart.home.contract.SmartTuyaDeviceContract;
import com.afar.osaio.smart.home.presenter.SmartDeviceListPresenter;
import com.afar.osaio.smart.scan.activity.AddCameraSelectActivity;
import com.afar.osaio.smart.scan.bean.NetworkChangeResult;
import com.afar.osaio.util.preference.GlobalPrefs;
import com.afar.osaio.widget.bean.TabItemBean;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.sdk.bean.DeviceBean;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.yokeyword.eventbusactivityscope.EventBusActivityScope;

/**
 * @Auther: 蛮羊
 * @datetime: 2021/11/30
 * @desc:
 */

public class HomeTestFragment extends NooieBaseMainFragment implements SmartDeviceListTestContract.View, SmartIpcDeviceContract.View, SmartTuyaDeviceContract.View {

    public static HomeTestFragment newInstance() {
        Bundle args = new Bundle();
        HomeTestFragment fragment = new HomeTestFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.tvTitleLeft)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.rvSmartDeviceList)
    RecyclerView rvSmartDeviceList;
    @BindView(R.id.tvWeather)
    TextView tvWeather;
    @BindView(R.id.tvTemp)
    TextView tvTemp;
    @BindView(R.id.ivWeather)
    ImageView ivWeather;
    @BindView(R.id.containerTitleLeft)
    View containerTitleLeft;

    private SmartDeviceListTestContract.Presenter mPresenter = null;
    private SmartNormalDeviceAdapter mSmartNormalDeviceAdapter = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_smart_device_list_test, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPresenter != null) {
          // mPresenter.refreshDevice(mUserAccount, mUid, 44596687);
        }
        setupTitle();
    }

    private void setupTitle() {
        if (((HomeActivity) _mActivity).isFirstLaunch()) {
            tvTitle.setText(R.string.welcome_back);
        } else if (GlobalPrefs.isStartFromRegister) {
            NooieLog.e("---->homeActivity Nice to meet you");
            tvTitle.setText(R.string.nice_to_meet_you);
            GlobalPrefs.isStartFromRegister = false;
        } else {
            tvTitle.setText(R.string.device);
            NooieLog.e("---->homeActivity device");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBusActivityScope.getDefault(_mActivity).unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        //EventBusActivityScope.getDefault(_mActivity).unregister(this);
        if (mPresenter != null) {
            mPresenter.destroy();
        }
    }

    @OnClick({R.id.ivRight, R.id.ivCloseIcon, R.id.clWeather})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ivRight:
                AddCameraSelectActivity.toAddCameraSelectActivity(_mActivity);
                break;
            case R.id.ivCloseIcon:
                break;
            case R.id.clWeather:
                EventBus.getDefault().post(new HomeActionEvent(HomeActionEvent.HOME_ACTION_LOCATION_PERMISSION));
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWeatherEvent(WeatherEvent event) {
        tvTemp.setText(event.getTemp());
        tvWeather.setText(event.getCondition());
        setTempPic(event.getIconUrl());
    }

    private void setTempPic(String iconUrl) {
        if (iconUrl.contains("RAIN") || iconUrl.contains("SHOWERS") || iconUrl.contains("RAIM") || iconUrl.contains("SLEET")) {
            ivWeather.setImageResource(R.drawable.rain);
        } else if (iconUrl.contains("SUNNY") || iconUrl.contains("CLEAR")) {
            ivWeather.setImageResource(R.drawable.sunny);
        } else if (iconUrl.contains("BLIZZARD") || iconUrl.contains("SNOW") || iconUrl.contains("HAIL") || iconUrl.contains("ICY") || iconUrl.contains("ICE") || iconUrl.contains("SNOW_SHOWERS")) {
            ivWeather.setImageResource(R.drawable.snow);
        } else if (iconUrl.contains("CLOUDY") || iconUrl.contains("OVERCAST")) {
            ivWeather.setImageResource(R.drawable.cloudy);
        } else if (iconUrl.contains("DUST") || iconUrl.contains("FOG") || iconUrl.contains("HAZE") || iconUrl.contains("SAND") || iconUrl.contains("SANDSTORM")) {
            ivWeather.setImageResource(R.drawable.dust);
        } else if (iconUrl.contains("LIGHTNING") || iconUrl.contains("THUNDERSHOWER") || iconUrl.contains("THUNDERSTORM")) {
            ivWeather.setImageResource(R.drawable.lightning);
        } else {
            ivWeather.setImageResource(R.drawable.ic_weather_null);
        }
    }

    @Override
    public void setPresenter(@NonNull SmartDeviceListTestContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void getHomeId(long homeId) {
        if (mPresenter != null) {
            mPresenter.refreshDevice(mUserAccount, mUid, homeId);
        }
    }

    @Override
    public void onRefreshDeviceCategory(String account, String uid, long homeId, List<TabItemBean> tabItemBeans) {

    }

    @Override
    public void onLoadDeviceSuccess(List<ListDeviceItem> devices) {

    }

    @Override
    public void onLoadDeviceEnd(int code, List<ListDeviceItem> devices) {
        if (mPresenter != null) {
            List<SmartCameraDevice> cameraDevices = mPresenter.getCameraDevices();
            showSmartDeviceList(false, SmartDeviceHelper.convertSmartDeviceList(cameraDevices));
        }
    }

    @Override
    public void onCheckBleApDeviceConnecting(int state, ApDeviceInfo result) {

    }

    @Override
    public void onCheckApDirectWhenNetworkChange(int state, NetworkChangeResult result) {

    }

    @Override
    public void onCheckBeforeConnectBleDevice(int state, boolean result, String bleDeviceId, String model, String ssid) {

    }

    @Override
    public void onStopAPDirectConnection(int state) {

    }

    @Override
    public void onUpdateDeviceOpenStatusResult(String result, String deviceId, boolean on) {

    }

    @Override
    public void onGetDeviceOpenStatusResult(String result, String deviceId, boolean on) {

    }

    @Override
    public void onUpdateApDeviceOpenStatus(int state, String deviceSsid, String deviceId, boolean on) {

    }

    @Override
    public void onDeleteIpcDeviceResult(String result, String deviceId) {

    }

    @Override
    public void onLoadTuyaDevices(int code, HomeBean homeBean) {
        if (mPresenter != null) {
            List<SmartTyDevice> tyDevices = mPresenter.getTyDevices();
            showSmartDeviceList(false, SmartDeviceHelper.convertSmartDeviceList(tyDevices));
        }
    }

    @Override
    public void notifyLoadDeviceSuccess(String devId, DeviceBean deviceBean) {

    }

    @Override
    public void notifyControlDeviceState() {

    }

    private void initData() {
        new SmartDeviceListTestPresenter(this, this, this);
        //mPresenter.createDefaultHome(0);
    }

    private void initView(View view) {
        ButterKnife.bind(this, view);
        EventBus.getDefault().register(this);
        EventBusActivityScope.getDefault(_mActivity).register(this);
        ivLeft.setVisibility(View.INVISIBLE);
        ivRight.setImageResource(R.drawable.menu_bar_right_add_icon);
        containerTitleLeft.setVisibility(View.VISIBLE);
        setupSmartDeviceListView();
    }

    private void setupSmartDeviceListView() {

        mSmartNormalDeviceAdapter = new SmartNormalDeviceAdapter();
        mSmartNormalDeviceAdapter.setListener(new SmartNormalDeviceListener() {

            @Override
            public void onItemClick(SmartBaseDevice device) {

            }

            @Override
            public void onSwitchBtnClick(SmartBaseDevice device, boolean on) {
                dealOnSwitchBtnClick(device, on);
            }

            @Override
            public void onAddDeviceBtnClick() {

            }

            @Override
            public boolean onItemLongClick(SmartBaseDevice device) {
                return false;
            }
        });
        rvSmartDeviceList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSmartDeviceList.setAdapter(mSmartNormalDeviceAdapter);

    }

    private void showSmartDeviceList(boolean isClear, List<SmartBaseDevice> devices) {
        if (mSmartNormalDeviceAdapter != null) {
            if (isClear) {
                mSmartNormalDeviceAdapter.setData(devices);
            } else {
                mSmartNormalDeviceAdapter.appendData(devices);
            }
        }
    }

    private void dealOnItemClick(SmartBaseDevice device) {
        if (device == null) {
            return;
        }
        if (device instanceof SmartCameraDevice) {
        } else if (device instanceof SmartTyDevice) {
        }
    }

    private void dealOnSwitchBtnClick(SmartBaseDevice device, boolean on) {
        if (device == null) {
            return;
        }
        if (device instanceof SmartCameraDevice) {
        } else if (device instanceof SmartTyDevice) {
            SmartTyDevice smartTyDevice = (SmartTyDevice) device;
            if (PowerStripHelper.getInstance().checkThreeHolesDeviceValid(smartTyDevice.productId)) {
                Map<String, Object> map = new HashMap<>();
                map.put(PowerStripHelper.getInstance().getSwitch_1_id(), on);
                map.put(PowerStripHelper.getInstance().getSwitch_2_id(), on);
                map.put(PowerStripHelper.getInstance().getSwitch_3_id(), on);
                mPresenter.controlStrip(smartTyDevice.deviceId, map);
            }
            if (PowerStripHelper.getInstance().checkFiveHolesDeviceValid(smartTyDevice.productId)) {
                Map<String, Object> map = new HashMap<>();
                map.put(PowerStripHelper.getInstance().getSwitch_1_id(), on);
                map.put(PowerStripHelper.getInstance().getSwitch_2_id(), on);
                map.put(PowerStripHelper.getInstance().getSwitch_3_id(), on);
                map.put(PowerStripHelper.getInstance().getSwitch_4_id(), on);
                map.put(PowerStripHelper.getInstance().getSwitch_usb1_id(), on);
                mPresenter.controlStrip(smartTyDevice.deviceId, map);
            }
            if (PowerStripHelper.getInstance().checkDimmerDeviceValid(smartTyDevice.productId)) {
                mPresenter.controlDevice(smartTyDevice.deviceId, on);
            }
            if (PowerStripHelper.getInstance().checkNormalPowerStripValid(smartTyDevice.productId)) {
                Map<String, Object> map = new HashMap<>();
                map.put(PowerStripHelper.getInstance().getSwitch_1_id(), on);
                map.put(PowerStripHelper.getInstance().getSwitch_2_id(), on);
                mPresenter.controlStrip(smartTyDevice.deviceId, map);
            }
            if (PowerStripHelper.getInstance().checkOldLampValid(smartTyDevice.productId)) {
                mPresenter.controlLamp(smartTyDevice.deviceId, PowerStripHelper.getInstance().getLed_switch_id(), on);
            }
            if (PowerStripHelper.getInstance().checkNewLampValid(smartTyDevice.productId)) {
                mPresenter.controlLamp(smartTyDevice.deviceId, PowerStripHelper.getInstance().getSwitch_led_id(), on);
            }
            if (PowerStripHelper.getInstance().checkNormalSwitchValid(smartTyDevice.productId)) {
                mPresenter.controlDevice(smartTyDevice.deviceId, on);
            }
        }
    }
}
