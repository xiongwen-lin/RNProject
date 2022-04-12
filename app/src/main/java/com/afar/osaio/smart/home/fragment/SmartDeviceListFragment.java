package com.afar.osaio.smart.home.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieBaseMainFragment;
import com.afar.osaio.base.NooieBaseSupportActivity;
import com.afar.osaio.bean.ApDeviceInfo;
import com.afar.osaio.smart.cache.BleApDeviceInfoCache;
import com.afar.osaio.smart.cache.DeviceListCache;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.electrician.activity.WrongDeviceActivity;
import com.afar.osaio.smart.electrician.eventbus.WeatherEvent;
import com.afar.osaio.smart.electrician.manager.DeviceManager;
import com.afar.osaio.smart.electrician.manager.PowerStripHelper;
import com.afar.osaio.smart.event.HomeActionEvent;
import com.afar.osaio.smart.event.NetworkChangeEvent;
import com.afar.osaio.smart.event.TabSelectedEvent;
import com.afar.osaio.smart.home.activity.HomeActivity;
import com.afar.osaio.smart.home.bean.SmartRouterDevice;
import com.afar.osaio.smart.home.contract.SmartRouterDeviceContract;
import com.afar.osaio.smart.mixipc.activity.BluetoothScanActivity;
import com.afar.osaio.smart.mixipc.activity.ConnectApDeviceActivity;
import com.afar.osaio.smart.player.activity.NooiePlayActivity;
import com.afar.osaio.smart.router.RouterDetalisActivity;
import com.afar.osaio.smart.router.RouterOfflineHelpActivity;
import com.afar.osaio.smart.scan.activity.AddCameraSelectActivity;
import com.afar.osaio.smart.scan.bean.NetworkChangeResult;
import com.afar.osaio.smart.scan.helper.ApHelper;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.util.preference.GlobalPrefs;
import com.afar.osaio.widget.TabFlowNormalView;
import com.afar.osaio.widget.WiFiDialog;
import com.afar.osaio.widget.bean.TabItemBean;
import com.afar.osaio.smart.device.bean.ListDeviceItem;
import com.afar.osaio.smart.device.helper.SmartDeviceHelper;
import com.afar.osaio.smart.home.adapter.SmartNormalDeviceAdapter;
import com.afar.osaio.smart.home.adapter.listener.SmartNormalDeviceListener;
import com.afar.osaio.smart.home.bean.SmartBaseDevice;
import com.afar.osaio.smart.home.bean.SmartCameraDevice;
import com.afar.osaio.smart.home.bean.SmartTyDevice;
import com.afar.osaio.smart.home.contract.SmartDeviceListContract;
import com.afar.osaio.smart.home.contract.SmartIpcDeviceContract;
import com.afar.osaio.smart.home.contract.SmartTuyaDeviceContract;
import com.afar.osaio.smart.home.presenter.SmartDeviceListPresenter;
import com.afar.osaio.widget.listener.TabFlowNormalListener;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.base.Constant;
import com.nooie.sdk.bean.IpcType;
import com.nooie.sdk.bean.SDKConstant;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.tuya.smart.api.MicroContext;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.api.ITuyaHomeChangeListener;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.panelcaller.api.AbsPanelCallerService;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.GroupBean;
import com.tuya.smart.sdk.enums.TempUnitEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.yokeyword.eventbusactivityscope.EventBusActivityScope;

/***********************************************************
 * 作者: zhengruidong@apemans.com
 * 日期: 2021/11/28 6:24 下午
 * 说明:
 *
 * 备注:
 *
 ***********************************************************/
public class SmartDeviceListFragment extends NooieBaseMainFragment implements SmartDeviceListContract.View, SmartIpcDeviceContract.View, SmartTuyaDeviceContract.View, SmartRouterDeviceContract.View, DeviceManager.IDeviceListenerCallBack, ITuyaHomeChangeListener {

    public static SmartDeviceListFragment newInstance() {
        Bundle args = new Bundle();
        SmartDeviceListFragment fragment = new SmartDeviceListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private final static int NETWORK_WEAK_TIP_CLOSE_STATE = 1;

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitleLeft)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.nwtSmartDeviceList)
    View nwtSmartDeviceList;
    @BindView(R.id.ivCloseIcon)
    ImageView ivCloseIcon;
    @BindView(R.id.vSmartDeviceListCategoryTabContainer)
    TabFlowNormalView vSmartDeviceListCategoryTabContainer;
    @BindView(R.id.rflSmartDeviceList)
    SmartRefreshLayout rflSmartDeviceList;
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
    @BindView(R.id.tvTempUnit)
    TextView tvTempUnit;

    private SmartDeviceListContract.Presenter mPresenter = null;
    private SmartNormalDeviceAdapter mSmartNormalDeviceAdapter = null;
    private TabItemBean mCurrentCategoryTab = null;

    private Dialog mDeleteIpcDeviceDialog;
    private WiFiDialog wiFiDialog;
    private Dialog mShowRemoveRouterDialog;

    DeviceApHelperListener mDeviceApHelperListener;
    private boolean mIsShowApDirectModeTip = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_smart_device_list, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupView();
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerDeviceApHelperListener();
        startRefreshData();
        setupTitle();
    }

    private void setupTitle() {
        if (GlobalPrefs.isStartFromRegister) {
            NooieLog.e("---->homeActivity Nice to meet you");
            tvTitle.setText(R.string.nice_to_meet_you);
            GlobalPrefs.isStartFromRegister = false;
        } else if (((HomeActivity) _mActivity).isFirstLaunch()) {
            NooieLog.e("---->homeActivity device");
            tvTitle.setText(R.string.welcome_back);
        } else {
            tvTitle.setText(R.string.device);
            NooieLog.e("---->homeActivity device");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWeatherEvent(WeatherEvent event) {
        NooieLog.e("SmartDeviceListFragment weatherbean " + event.toString());
        Float a = 0.0f;
        try {
             a = Float.valueOf(event.getTemp());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        if(TuyaHomeSdk.getUserInstance().getUser().getTempUnit()== TempUnitEnum.Fahrenheit.getType() && a!=0.0f){
            int temp  = (int) (a * 1.8f +32);
            tvTemp.setText(temp+"");
            tvTempUnit.setText("°F");
        }else {
            tvTemp.setText(event.getTemp());
            tvTempUnit.setText("℃");
        }

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
    public void onPause() {
        super.onPause();
        unregisterDeviceApHelperListener();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBusActivityScope.getDefault(_mActivity).unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hideDeleteIpcDeviceDialog();
        hideConnectionWifiDialog();
        hideRemoveRouterDialog();
        hideLoading();
        EventBus.getDefault().unregister(this);
        TuyaHomeSdk.getHomeManagerInstance().unRegisterTuyaHomeChangeListener(this);
        destroyDeviceApHelperListener();
        if (mPresenter != null) {
            mPresenter.unRegisterDeviceChangeReceiver();
            mPresenter.destroy();
        }
    }

    @OnClick({R.id.ivRight, R.id.ivCloseIcon, R.id.clWeather})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ivRight:
                gotoAddDevice("");
                break;
            case R.id.ivCloseIcon:
                displayNetworkWeakTip(false);
                saveNetworkWeakTipClose();
                break;
            case R.id.clWeather:
                EventBus.getDefault().post(new HomeActionEvent(HomeActionEvent.HOME_ACTION_LOCATION_PERMISSION));
                break;
        }
    }

    @Override
    public void setPresenter(@NonNull SmartDeviceListContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onLoadDeviceSuccess(List<ListDeviceItem> devices) {
        if (mPresenter != null) {
            mPresenter.updateDeviceCategory(mUserAccount, mUid, ConstantValue.TAB_DEVICE_CATEGORY_CAMERA, CollectionUtil.size(mPresenter.getCameraDevices()));
        }
        showSmartDeviceList();
    }

    @Override
    public void onLoadDeviceEnd(int code, List<ListDeviceItem> devices) {
        if (mPresenter != null) {
            mPresenter.updateDeviceCategory(mUserAccount, mUid, ConstantValue.TAB_DEVICE_CATEGORY_CAMERA, CollectionUtil.size(mPresenter.getCameraDevices()));
        }
        showSmartDeviceList();
        stopRefreshLoading();
    }

    @Override
    public void onLoadTuyaDevices(int code, HomeBean homeBean) {
        if (mPresenter != null) {
            mPresenter.updateDeviceCategory(mUserAccount, mUid, ConstantValue.TAB_DEVICE_CATEGORY_TUYA, CollectionUtil.size(mPresenter.getTyDevices()));
        }
        showSmartDeviceList();
    }

    @Override
    public void notifyLoadDeviceSuccess(String devId, DeviceBean deviceBean) {
        if (mSmartNormalDeviceAdapter != null) {
            mSmartNormalDeviceAdapter.updateTyDeviceBean(deviceBean);
        }
    }

    @Override
    public void notifyControlDeviceState() {
        ((NooieBaseSupportActivity) _mActivity).hideLoading();
    }

    @Override
    public void onLoadRouterDevices(int code, List<SmartRouterDevice> device) {
        if (mPresenter != null) {
            mPresenter.updateDeviceCategory(mUserAccount, mUid, ConstantValue.TAB_DEVICE_CATEGORY_ROUTER, CollectionUtil.size(mPresenter.getRouterDevices()));
        }
        showSmartDeviceList();
    }

    @Override
    public void onDeleteRouterDevice(int code, String deviceId) {
    }

    @Override
    public void onRefreshDeviceCategory(String account, String uid, long homeId, List<TabItemBean> tabItemBeans, boolean isRefreshDevice) {
//        if (CollectionUtil.size(tabItemBeans) > 0) {
//            if (mCurrentCategoryTab == null) {
//                mCurrentCategoryTab = tabItemBeans.get(0);
//            }
//            vSmartDeviceListCategoryTabContainer.updateTabs(tabItemBeans);
//        }
//        if (!isRefreshDevice) {
//            return;
//        }
//        boolean isDeviceNotEmpty = CollectionUtil.size(tabItemBeans) > 1;
//        NooieLog.d("-->> debug SmartDeviceListFragment onRefreshDeviceCategory tabSize " + CollectionUtil.size(tabItemBeans) + " isDeviceNotEmpty " + isDeviceNotEmpty);
//        if (isDeviceNotEmpty) {
//            vSmartDeviceListCategoryTabContainer.setVisibility(View.VISIBLE);
//            boolean isCurrentCategoryExist = mCurrentCategoryTab != null && SmartDeviceHelper.checkTabCategoryExist(mCurrentCategoryTab.tag, tabItemBeans);
//            if (!isCurrentCategoryExist) {
//                mCurrentCategoryTab = tabItemBeans.get(0);
//                showSmartDeviceList();
//            }
//            if (mPresenter != null) {
//                mPresenter.refreshDevice(account, uid, homeId);
//            }
//        } else {
//            vSmartDeviceListCategoryTabContainer.setVisibility(View.GONE);
//        }

        boolean isDeviceNotEmpty = CollectionUtil.size(tabItemBeans) > 1;
        NooieLog.d("-->> debug SmartDeviceListFragment onRefreshDeviceCategory tabSize " + CollectionUtil.size(tabItemBeans) + " isDeviceNotEmpty " + isDeviceNotEmpty);
        if (isDeviceNotEmpty) {
            vSmartDeviceListCategoryTabContainer.setVisibility(View.VISIBLE);
        } else {
            vSmartDeviceListCategoryTabContainer.setVisibility(View.INVISIBLE);
        }

        if (CollectionUtil.size(tabItemBeans) > 0) {
            boolean isCurrentCategoryExist = mCurrentCategoryTab != null && SmartDeviceHelper.checkTabCategoryExist(mCurrentCategoryTab.tag, tabItemBeans);
            if (!isCurrentCategoryExist) {
                mCurrentCategoryTab = tabItemBeans.get(0);
            }
//            if (mCurrentCategoryTab == null) {
//                mCurrentCategoryTab = tabItemBeans.get(0);
//            }
            vSmartDeviceListCategoryTabContainer.updateTabAdapterCurrentSelectionIndex(mCurrentCategoryTab, tabItemBeans);
            vSmartDeviceListCategoryTabContainer.updateItemSelect(mCurrentCategoryTab, tabItemBeans);
            vSmartDeviceListCategoryTabContainer.setTabs(tabItemBeans);
        }
        if (!isRefreshDevice) {
            return;
        }
//        boolean isDeviceNotEmpty = CollectionUtil.size(tabItemBeans) > 1;
//        NooieLog.d("-->> debug SmartDeviceListFragment onRefreshDeviceCategory tabSize " + CollectionUtil.size(tabItemBeans) + " isDeviceNotEmpty " + isDeviceNotEmpty);
//        if (isDeviceNotEmpty) {
//            vSmartDeviceListCategoryTabContainer.setVisibility(View.VISIBLE);
//        } else {
//            vSmartDeviceListCategoryTabContainer.setVisibility(View.GONE);
//        }
        boolean isCurrentCategoryExist = mCurrentCategoryTab != null && SmartDeviceHelper.checkTabCategoryExist(mCurrentCategoryTab.tag, tabItemBeans);
        if (!isCurrentCategoryExist) {
            mCurrentCategoryTab = tabItemBeans.get(0);
            showSmartDeviceList();
        }
        if (mPresenter != null) {
            mPresenter.refreshDevice(account, uid, homeId);
        }
    }

    @Override
    public void onReceiveDeviceChange() {
        startRefreshData();
    }

    @Override
    public void onCheckBleApDeviceConnecting(int state, ApDeviceInfo result) {
        NooieLog.d("-->> debug SmartDeviceListFragment onCheckBleApDeviceConnecting: 1001");
        if (checkActivityIsDestroy() || checkIsPause()) {
            NooieLog.d("-->> debug SmartDeviceListFragment onCheckBleApDeviceConnecting: 1002");
            return;
        }
        boolean isAbleToSetupApMode = ApHelper.getInstance().checkBleApDeviceConnectingExist() && state == SDKConstant.SUCCESS && result != null && result.getBindDevice() != null;
        NooieLog.d("-->> debug SmartDeviceListFragment onCheckBleApDeviceConnecting: 1003");
        if (isAbleToSetupApMode) {
            NooieLog.d("-->> debug SmartDeviceListFragment onCheckBleApDeviceConnecting: 1004");
            List<ApDeviceInfo> devices = new ArrayList<>();
            devices.add(result);
            showSmartDeviceList();
            showApDirectModeTip();
        } else {
            NooieLog.d("-->> debug SmartDeviceListFragment onCheckBleApDeviceConnecting: 1005");
            String model = "";
            if (ApHelper.getInstance().getCurrentApDeviceInfo() != null && ApHelper.getInstance().getCurrentApDeviceInfo().getBindDevice() != null) {
                model = ApHelper.getInstance().getCurrentApDeviceInfo().getBindDevice().getType();
            }
            NooieLog.d("-->> debug SmartDeviceListFragment checkBleApDeviceConnecting: 1002 model=" + model);
            ApHelper.getInstance().tryResetApConnectMode(model, new ApHelper.APDirectListener() {
                @Override
                public void onSwitchConnectionMode(boolean result, int connectionMode, String deviceId) {
                    showSmartDeviceList();
                }
            });
        }
    }

    @Override
    public void onCheckApDirectWhenNetworkChange(int state, NetworkChangeResult result) {
        NooieLog.d("-->> debug SmartDeviceListFragment onCheckApDirectWhenNetworkChange: 1000");
        if (checkActivityIsDestroy() || checkIsPause() || checkNull(mPresenter)) {
            return;
        }
        NooieLog.d("-->> debug SmartDeviceListFragment onCheckApDirectWhenNetworkChange: 1001");
        if (state == SDKConstant.SUCCESS) {
            NooieLog.d("-->> debug SmartDeviceListFragment onCheckApDirectWhenNetworkChange: 1002");
            boolean isNeedToStopApDirectConnection = result != null && result.getIsConnected() && !TextUtils.isEmpty(result.getSsid())
                    && !(NooieDeviceHelper.checkApFutureCode(result.getSsid()) || NooieDeviceHelper.checkBluetoothApFutureCode(result.getSsid(), ""));
            if (!isNeedToStopApDirectConnection) {
                NooieLog.d("-->> debug SmartDeviceListFragment onCheckApDirectWhenNetworkChange: 1003");
                return;
            }
            NooieLog.d("-->> debug SmartDeviceListFragment onCheckApDirectWhenNetworkChange: 1004");
            mPresenter.stopAPDirectConnection(null);
            showSmartDeviceList();
        }
    }

    @Override
    public void onCheckBeforeConnectBleDevice(int state, boolean result, String bleDeviceId, String model, String ssid) {
        if (checkActivityIsDestroy()) {
            return;
        }
        gotoConnectBleApDevice(result, bleDeviceId, model, ssid);
    }

    @Override
    public void onStopAPDirectConnection(int state) {
        if (checkActivityIsDestroy()) {
            return;
        }
    }

    @Override
    public void onUpdateDeviceOpenStatusResult(String result, String deviceId, boolean on) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            if (mSmartNormalDeviceAdapter != null) {
                mSmartNormalDeviceAdapter.updateItemOpenStatusForCamera(deviceId, on);
            }
            NooieDeviceHelper.updateDeviceOpenStatus(deviceId, (on ? ApiConstant.OPEN_STATUS_ON : ApiConstant.OPEN_STATUS_OFF));
        } else {
            if (mPresenter != null) {
                mPresenter.getDeviceOpenStatus(deviceId);
            }
            ToastUtil.showToast(_mActivity, R.string.camera_setting_warn_msg_set_sleep_fail);
        }
        ((NooieBaseSupportActivity) _mActivity).hideLoading();
    }

    @Override
    public void onGetDeviceOpenStatusResult(String result, String deviceId, boolean on) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            if (mSmartNormalDeviceAdapter != null) {
                mSmartNormalDeviceAdapter.updateItemOpenStatusForCamera(deviceId, on);
            }
            NooieDeviceHelper.updateDeviceOpenStatus(deviceId, (on ? ApiConstant.OPEN_STATUS_ON : ApiConstant.OPEN_STATUS_OFF));
        } else {
        }
    }

    @Override
    public void onUpdateApDeviceOpenStatus(int state, String deviceSsid, String deviceId, boolean on) {
        if (checkActivityIsDestroy() || checkNull(mSmartNormalDeviceAdapter)) {
            return;
        }
        if (state == SDKConstant.SUCCESS) {
            mSmartNormalDeviceAdapter.updateItemOpenStatusForCamera(deviceId, on);
        } else {
            mSmartNormalDeviceAdapter.updateItemOpenStatusForCamera(deviceId, on);
        }
        ((NooieBaseSupportActivity) _mActivity).hideLoading();
    }

    @Override
    public void onDeleteIpcDeviceResult(String result, String deviceId) {
        if (checkActivityIsDestroy()) {
            return;
        }
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            NooieDeviceHelper.sendRemoveDeviceBroadcast(ConstantValue.REMOVE_DEVICE_TYPE_IPC, deviceId);
        }
        hideLoading();
    }

    @Subscribe
    public void onTabSelectedEvent(TabSelectedEvent event) {
        NooieLog.d("-->> Receive TabSelectedEvent SmartDeviceListFragment onTabSelectedEvent  position=" + event.position);
        if (event == null || event.position != HomeFragment.FIRST) {
            return;
        }
        //resumeData();
    }

    @Subscribe
    public void onNetworkChangeEvent(NetworkChangeEvent event) {
        if (event == null) {
            return;
        }
        Log.e("网络连接码", "" + event.state);
        displayNetworkWeakTip(event.state == NetworkChangeEvent.NETWORK_CHANGE_DISCONNECTED);
    }

    private void initData() {
        new SmartDeviceListPresenter(this, this, this);
        DeviceManager.getInstance().setAllDeviceListenerCallBack(this);
        TuyaHomeSdk.getHomeManagerInstance().registerTuyaHomeChangeListener(this);
        if (mPresenter != null) {
            mPresenter.registerDeviceChangeReceiver();
        }
    }

    private void initView(View view) {
        ButterKnife.bind(this, view);
        EventBus.getDefault().register(this);
        EventBusActivityScope.getDefault(_mActivity).register(this);
    }

    private void setupView() {
        ivLeft.setVisibility(View.GONE);
        ivRight.setImageResource(R.drawable.menu_bar_right_add_icon);
        containerTitleLeft.setVisibility(View.VISIBLE);
        setupCategoryTabLayoutView();
        setupRefreshLayoutView();
        setupSmartDeviceListView();
        displayNetworkWeakTip(false);
    }

    private void setupCategoryTabLayoutView() {
        mCurrentCategoryTab = SmartDeviceHelper.createTabItemBean(ConstantValue.TAB_DEVICE_CATEGORY_ALL, ConstantValue.TAB_DEVICE_CATEGORY_ALL, 1, 1);
        vSmartDeviceListCategoryTabContainer.setListener(new TabFlowNormalListener() {
            @Override
            public void onItemClick(View view, TabItemBean data, int position) {
                NooieLog.d("-->> debug SmartDeviceListFragment setupCategoryTabLayoutView onItemClick position " + position);
                if (data != null) {
                    mCurrentCategoryTab = data;
                    showSmartDeviceList();
                }
            }
        });
    }

    private void setupRefreshLayoutView() {
        if (rflSmartDeviceList.getRefreshHeader() != null) {
            ClassicsHeader refreshHeader = (ClassicsHeader) rflSmartDeviceList.getRefreshHeader();
            refreshHeader.setEnableLastTime(false);
        }
        rflSmartDeviceList.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                startRefreshData();
            }
        });
    }

    private void setupSmartDeviceListView() {

        mSmartNormalDeviceAdapter = new SmartNormalDeviceAdapter();
        mSmartNormalDeviceAdapter.setListener(new SmartNormalDeviceListener() {
            @Override
            public void onItemClick(SmartBaseDevice device) {
                NooieLog.e("---------->dealOnItemClick onItemClick device");
                dealOnItemClick(device);
            }

            @Override
            public void onSwitchBtnClick(SmartBaseDevice device, boolean on) {
                NooieLog.e("---------->dealOnItemClick onSwitchBtnClick");
                dealOnSwitchBtnClick(device, on);
            }

            @Override
            public void onAddDeviceBtnClick() {
                NooieLog.e("---------->dealOnItemClick onAddDeviceBtnClick");
                gotoAddDevice("");
            }

            @Override
            public boolean onItemLongClick(SmartBaseDevice device) {
                NooieLog.e("---------->dealOnItemClick onAddDeviceBtnClick");
                return dealOnItemLongClick(device);
            }
        });
        rvSmartDeviceList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSmartDeviceList.setAdapter(mSmartNormalDeviceAdapter);

    }

    private void startRefreshData() {
        if (mPresenter != null) {
            mPresenter.tryRefreshDeviceCategory(mUserAccount, mUid);
        }
    }

    private void stopRefreshLoading() {
        if (checkActivityIsDestroy() || checkNull(rflSmartDeviceList)) {
            return;
        }
        rflSmartDeviceList.finishRefresh();
    }

    private void showSmartDeviceList() {
        if (checkActivityIsDestroy() || checkNull(mSmartNormalDeviceAdapter)) {
            return;
        }
        if (ApHelper.getInstance().checkBleApDeviceConnectingExist()) {
            displaySmartListPageView(true, 0);
            List<ApDeviceInfo> apDeviceInfoList = new ArrayList<>();
            apDeviceInfoList.add(ApHelper.getInstance().getCurrentApDeviceInfo());
            List<SmartBaseDevice> devices = SmartDeviceHelper.convertSmartDeviceList(SmartDeviceHelper.convertSmartCameraDeviceListOfNetSpot(apDeviceInfoList));
            mSmartNormalDeviceAdapter.setData(devices);
            return;
        }
        if (mPresenter == null) {
            return;
        }
        String tabCategory = mCurrentCategoryTab != null ? mCurrentCategoryTab.tag : ConstantValue.TAB_DEVICE_CATEGORY_ALL;
        List<SmartBaseDevice> devices = SmartDeviceHelper.filterSmartDevice(SmartDeviceHelper.mergeSmartDevice(mPresenter.getCameraDevices(), mPresenter.getTyDevices(), mPresenter.getRouterDevices()), tabCategory);
        displaySmartListPageView(false, CollectionUtil.size(devices));
        mSmartNormalDeviceAdapter.setData(devices);
        mIsShowApDirectModeTip = false;
    }

    private void displaySmartListPageView(boolean isNetSpotMode, int deviceSize) {
        if (isNetSpotMode) {
            vSmartDeviceListCategoryTabContainer.setVisibility(View.GONE);
        } else {
            vSmartDeviceListCategoryTabContainer.setVisibility((deviceSize > 0 ? View.VISIBLE : View.GONE));
        }
    }

    private void dealOnItemClick(SmartBaseDevice device) {
        if (device == null) {
            return;
        }
        if (device instanceof SmartCameraDevice) {
            startCameraDevice((SmartCameraDevice) device);
        } else if (device instanceof SmartTyDevice) {
            startTyDevice((SmartTyDevice) device);
        } else if (device instanceof SmartRouterDevice) {
            startRouterDevice((SmartRouterDevice) device);
        }
    }

    private boolean dealOnItemLongClick(SmartBaseDevice device) {
        if (device == null || TextUtils.isEmpty(device.deviceId)) {
            return false;
        }
        if (device instanceof SmartRouterDevice) {
            showRemoveRouterDialog(device.deviceId);
            return true;
        } else {
            return false;
        }
    }

    private void dealOnSwitchBtnClick(SmartBaseDevice device, boolean on) {
        if (device == null || mPresenter == null) {
            return;
        }
        if (device instanceof SmartCameraDevice) {
            SmartCameraDevice cameraDevice = (SmartCameraDevice) device;
            if (cameraDevice == null) {
                return;
            }
            ((NooieBaseSupportActivity) _mActivity).showLoading(true);
            if (SmartDeviceHelper.checkDeviceInfoTypeIsBleDirectLink(cameraDevice.deviceInfoType)) {
                mPresenter.updateApDeviceOpenStatus(cameraDevice.deviceSsid, cameraDevice.deviceId, on);
            } else {
                mPresenter.updateDeviceOpenStatus(cameraDevice.deviceId, on);
            }
        } else if (device instanceof SmartTyDevice) {
            SmartTyDevice smartTyDevice = (SmartTyDevice) device;
            if (smartTyDevice == null) {
                return;
            }
            ((NooieBaseSupportActivity) _mActivity).showLoading(true);
            if (PowerStripHelper.getInstance().checkThreeHolesDeviceValid(smartTyDevice.productId)) {
                Map<String, Object> map = new HashMap<>();
                map.put(PowerStripHelper.getInstance().getSwitch_1_id(), on);
                map.put(PowerStripHelper.getInstance().getSwitch_2_id(), on);
                map.put(PowerStripHelper.getInstance().getSwitch_3_id(), on);
                mPresenter.controlStrip(smartTyDevice.deviceId, map);
                return;
            }
            if (PowerStripHelper.getInstance().checkFiveHolesDeviceValid(smartTyDevice.productId)) {
                Map<String, Object> map = new HashMap<>();
                map.put(PowerStripHelper.getInstance().getSwitch_1_id(), on);
                map.put(PowerStripHelper.getInstance().getSwitch_2_id(), on);
                map.put(PowerStripHelper.getInstance().getSwitch_3_id(), on);
                map.put(PowerStripHelper.getInstance().getSwitch_4_id(), on);
                map.put(PowerStripHelper.getInstance().getSwitch_usb1_id(), on);
                mPresenter.controlStrip(smartTyDevice.deviceId, map);
                return;
            }
            if (PowerStripHelper.getInstance().checkDimmerDeviceValid(smartTyDevice.productId)) {
                mPresenter.controlDevice(smartTyDevice.deviceId, on);
                return;
            }
            if (PowerStripHelper.getInstance().checkNormalPowerStripValid(smartTyDevice.productId)) {
                Map<String, Object> map = new HashMap<>();
                map.put(PowerStripHelper.getInstance().getSwitch_1_id(), on);
                map.put(PowerStripHelper.getInstance().getSwitch_2_id(), on);
                mPresenter.controlStrip(smartTyDevice.deviceId, map);
                return;
            }
            if (PowerStripHelper.getInstance().checkOldLampValid(smartTyDevice.productId)) {
                mPresenter.controlLamp(smartTyDevice.deviceId, PowerStripHelper.getInstance().getLed_switch_id(), on);
                return;
            }
            if (PowerStripHelper.getInstance().checkNewLampValid(smartTyDevice.productId)) {
                mPresenter.controlLamp(smartTyDevice.deviceId, PowerStripHelper.getInstance().getSwitch_led_id(), on);
                return;
            }
            if (PowerStripHelper.getInstance().checkNormalSwitchValid(smartTyDevice.productId) || PowerStripHelper.getInstance().checkAirPurifierValid(smartTyDevice.productId)) {
                mPresenter.controlDevice(smartTyDevice.deviceId, on);
            }
        }
    }

    private void startCameraDevice(SmartCameraDevice device) {
        NooieLog.e("---------->dealOnItemClick startCameraDevice");
        if (device == null || TextUtils.isEmpty(device.deviceId) || TextUtils.isEmpty(device.model)) {
            return;
        }
        if (SmartDeviceHelper.checkDeviceInfoTypeIsBleDirectLink(device.deviceInfoType)) {
            gotoPlayForDvDevice(ApHelper.getInstance().getCurrentApDeviceInfo());
        } else if (SmartDeviceHelper.checkDeviceInfoTypeIsBleNetSpot(device.deviceInfoType)) {
            tryGotoConnectBleApDevice(device.deviceId, device.model, device.deviceSsid, device.bleDeviceId);
        } else {
            if (SmartDeviceHelper.checkIsDeviceStateOn(device.deviceState) || SmartDeviceHelper.checkIsCloudStateActive(device.cloudState)) {
                NooiePlayActivity.startPlayActivity(_mActivity, device.deviceId, device.model, ConstantValue.NOOIE_PLAYBACK_TYPE_LIVE, ConstantValue.NOOIE_PLAYBACK_SOURCE_TYPE_NORMAL, 0, ConstantValue.ROUTE_SOURCE_NORMAL, ConstantValue.CONNECTION_MODE_QC, new String());
            } else if (!SmartDeviceHelper.checkIsDeviceStateOn(device.deviceState)) {
                showDeleteIpcDeviceDialog(mUserAccount, device.deviceId, device.deviceName);
            }
        }
    }

    private void startTyDevice(SmartTyDevice device) {
        NooieLog.e("---------->dealOnItemClick startTyDevice");
        if (device == null || TextUtils.isEmpty(device.productId)) {
            return;
        }
        if (PowerStripHelper.getInstance().checkTuyaProductIdValid(device.productId)) {
            AbsPanelCallerService service = MicroContext.getServiceManager().findServiceByInterface(AbsPanelCallerService.class.getName());
            service.goPanelWithCheckAndTip(getActivity(), device.deviceId);
        } else {
            WrongDeviceActivity.toWrongDeviceActivity(getActivity(), device.deviceId);
        }
    }

    private void startRouterDevice(SmartRouterDevice device) {
        NooieLog.e("---------->dealOnItemClick startRouterDevice");
        if (device == null || TextUtils.isEmpty(device.deviceId)) {
            return;
        }
        if (SmartDeviceHelper.checkIsDeviceStateOn(device.deviceState)) {
            RouterDetalisActivity.toRouterDetalisActivity(_mActivity, device.deviceName, device.deviceId, device.isBind);
        } else {
            if (device.routerBindType == 0) {
                RouterOfflineHelpActivity.toRouterOfflineHelpActivity(_mActivity);
            } else {
                showConnectionWifiDialog();
            }
        }
    }

    private void gotoAddDevice(String eventId) {
        AddCameraSelectActivity.toAddCameraSelectActivity(_mActivity);
    }

    private void displayNetworkWeakTip(boolean show) {
        if (checkNull(nwtSmartDeviceList)) {
            return;
        }
        if (checkNetworkWeakTipClose()) {
            show = false;
        }
        nwtSmartDeviceList.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void saveNetworkWeakTipClose() {
        if (ivCloseIcon != null) {
            ivCloseIcon.setTag(NETWORK_WEAK_TIP_CLOSE_STATE);
        }
    }

    private boolean checkNetworkWeakTipClose() {
        return ivCloseIcon != null && ivCloseIcon.getTag() != null && (Integer) ivCloseIcon.getTag() == NETWORK_WEAK_TIP_CLOSE_STATE;
    }

    private void gotoConnectBleApDevice(boolean isHotSpotMatching, String bleDeviceId, String model, String ssid) {
        if (TextUtils.isEmpty(model)) {
            return;
        }
        Bundle param = new Bundle();
        param.putString(ConstantValue.INTENT_KEY_IPC_MODEL, model);
        if (NooieDeviceHelper.mergeIpcType(model) == IpcType.MC120) {
            param.putInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_AP_DIRECT);
            param.putBoolean(ConstantValue.INTENT_KEY_DATA_PARAM, false);
            ConnectApDeviceActivity.toConnectApDeviceActivity(_mActivity, param);
        } else if (NooieDeviceHelper.mergeIpcType(model) == IpcType.HC320 && isHotSpotMatching) {
            param.putString(ConstantValue.INTENT_KEY_SSID, ssid);
            param.putString(ConstantValue.INTENT_KEY_BLE_DEVICE, bleDeviceId);
            param.putString(ConstantValue.INTENT_KEY_PSD, "12345678");
            param.putInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_AP_DIRECT);
            param.putBoolean(ConstantValue.INTENT_KEY_DATA_PARAM, false);
            ConnectApDeviceActivity.toConnectApDeviceActivity(_mActivity, param);
        } else if (NooieDeviceHelper.mergeIpcType(model) == IpcType.HC320) {
            param.putInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_AP_DIRECT);
            param.putInt(ConstantValue.INTENT_KEY_DATA_PARAM_1, ConstantValue.BLUETOOTH_SCAN_TYPE_EXIST);
            param.putString(ConstantValue.INTENT_KEY_BLE_DEVICE, bleDeviceId);
            BluetoothScanActivity.toBluetoothScanActivity(_mActivity, param);
        }
    }

    private void tryGotoConnectBleApDevice(String deviceId, String model, String ssid, String bleDeviceId) {
        if (!NooieDeviceHelper.checkBleApDeviceValid(deviceId, model, ssid)) {
            return;
        }
        if (mPresenter != null) {
            mPresenter.checkBeforeConnectBleDevice(bleDeviceId, model, ssid);
        }
    }

    private void gotoPlayForDvDevice(ApDeviceInfo deviceInfo) {
        if (checkActivityIsDestroy() || checkNull(_mActivity)) {
            return;
        }
        if (deviceInfo == null || deviceInfo.getBindDevice() == null || TextUtils.isEmpty(deviceInfo.getBindDevice().getUuid()) || TextUtils.isEmpty(deviceInfo.getBindDevice().getType())) {
            return;
        }
        String deviceId = deviceInfo.getBindDevice().getUuid();
        String model = deviceInfo.getBindDevice().getType();
        String deviceSsid = deviceInfo.getDeviceSsid();
        NooiePlayActivity.startPlayActivity(_mActivity, deviceId, model, ConstantValue.NOOIE_PLAYBACK_TYPE_LIVE, ConstantValue.NOOIE_PLAYBACK_SOURCE_TYPE_NORMAL, 0, ConstantValue.ROUTE_SOURCE_ADD_DEVICE, ConstantValue.CONNECTION_MODE_AP_DIRECT, deviceSsid);
    }

    private void showApDirectModeTip() {
        NooieLog.d("-->> debug SmartDeviceListFragment showApDirectModeTip mIsShowApDirectModeTip=" + mIsShowApDirectModeTip);
        boolean isNoneOtherDevice = DeviceListCache.getInstance().isEmpty() && BleApDeviceInfoCache.getInstance().cacheSize() <= 1;
        if (checkActivityIsDestroy() || mIsShowApDirectModeTip || isNoneOtherDevice) {
            return;
        }
        mIsShowApDirectModeTip = true;
        ToastUtil.showToast(_mActivity, getString(R.string.home_ap_direct_mode_tip));
    }

    private void registerDeviceApHelperListener() {
        if (checkActivityIsDestroy()) {
            return;
        }
        if (mDeviceApHelperListener == null) {
            mDeviceApHelperListener = new DeviceApHelperListener();
        }
        ApHelper.getInstance().addListener(mDeviceApHelperListener);
    }

    private void unregisterDeviceApHelperListener() {
        if (mDeviceApHelperListener != null) {
            ApHelper.getInstance().removeListener(mDeviceApHelperListener);
        }
    }

    private void destroyDeviceApHelperListener() {
        if (mDeviceApHelperListener != null) {
            unregisterDeviceApHelperListener();
            mDeviceApHelperListener = null;
        }
    }

    private void showDeleteIpcDeviceDialog(String account, String deviceId, String name) {
        hideDeleteIpcDeviceDialog();
        mDeleteIpcDeviceDialog = DialogUtils.showConfirmWithSubMsgDialog(_mActivity, getString(R.string.camera_settings_remove_camera_confirm),
                String.format(getString(R.string.camera_settings_remove_info_confirm), name),
                R.string.camera_settings_no_remove, R.string.camera_settings_remove, new DialogUtils.OnClickConfirmButtonListener() {
                    @Override
                    public void onClickRight() {
                        if (!TextUtils.isEmpty(deviceId) && mPresenter != null) {
                            showLoading(true);
                            mPresenter.removeIpcDevice(account, deviceId);
                        }
                    }

                    @Override
                    public void onClickLeft() {
                    }
                });
    }

    private void hideDeleteIpcDeviceDialog() {
        if (mDeleteIpcDeviceDialog != null) {
            mDeleteIpcDeviceDialog.dismiss();
            mDeleteIpcDeviceDialog = null;
        }
    }

    /**
     * 提示连接wifi失败去连接wifi
     */
    private void showConnectionWifiDialog() {
        hideConnectionWifiDialog();
        wiFiDialog = new WiFiDialog(getActivity(), true);
        if (!wiFiDialog.isShowing()) {
            wiFiDialog.show();
        }
    }

    private void hideConnectionWifiDialog() {
        if (wiFiDialog != null) {
            wiFiDialog.dismiss();
            wiFiDialog = null;
        }
    }

    private void showRemoveRouterDialog(String deviceId) {
        hideRemoveRouterDialog();
        mShowRemoveRouterDialog = DialogUtils.removeRouterDialog(getActivity(), new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                /*
                if (mPresenter != null) {
                    mPresenter.deleteRouterDevice(deviceId);
                }

                 */
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

    @Override
    public void onDpUpdate(String devId, String dpStr) {
        NooieLog.e("----------->SmartDeviceListFragment onDpUpdate devId=" + devId + " dpStr=" + dpStr);
        if (mPresenter != null) {
            mPresenter.loadDeviceBean(devId);
        }
    }

    @Override
    public void onRemoved(String devId) {
        NooieLog.e("----------->SmartDeviceListFragment onRemoved devId=" + devId);
        /*if (mSmartNormalDeviceAdapter != null) {
            mSmartNormalDeviceAdapter.removeTyDeviceBean(devId);
        }*/
        onReceiveDeviceChange();
    }

    @Override
    public void onStatusChanged(String devId, boolean online) {
        NooieLog.e("----------->SmartDeviceListFragment onStatusChanged devId=" + devId + " online=" + online);
        if (mPresenter != null) {
            mPresenter.loadDeviceBean(devId);
        }
    }

    @Override
    public void onNetworkStatusChanged(String devId, boolean status) {
        NooieLog.e("----------->SmartDeviceListFragment onDpUpdate devId=" + devId + " status=" + status);
    }

    @Override
    public void onDevInfoUpdate(String devId) {
        NooieLog.e("----------->SmartDeviceListFragment onDpUpdate devId=" + devId);
    }

    @Override
    public void onHomeAdded(long homeId) {

    }

    @Override
    public void onHomeInvite(long homeId, String homeName) {

    }

    @Override
    public void onHomeRemoved(long homeId) {

    }

    @Override
    public void onHomeInfoChanged(long homeId) {

    }

    @Override
    public void onSharedDeviceList(List<DeviceBean> sharedDeviceList) {
        NooieLog.e("----------->SmartDeviceListFragment onSharedDeviceList sharedDeviceList=" + sharedDeviceList);
        onReceiveDeviceChange();
    }

    @Override
    public void onSharedGroupList(List<GroupBean> sharedGroupList) {

    }

    @Override
    public void onServerConnectSuccess() {

    }

    private class DeviceApHelperListener implements ApHelper.ApHelperListener {

        @Override
        public void onApHeartBeatResponse(int code) {
            if (checkActivityIsDestroy() || checkIsPause() || checkNull(mSmartNormalDeviceAdapter)) {
                return;
            }
            NooieLog.d("-->> debug DeviceApHelperListener onApHeartBeatResponse: code=" + code + " checkApDirectConnectionIsError=" + ApHelper.getInstance().checkApDirectConnectionIsError() + " currentConnectionMode=" + ApHelper.getInstance().getCurrentConnectionMode());
            if (code == Constant.OK && ApHelper.getInstance().checkBleApDeviceConnectingExist()) {
                boolean isNeedToLoadDevice = CollectionUtil.isEmpty(mSmartNormalDeviceAdapter.getData());
                if (isNeedToLoadDevice && mPresenter != null) {
                    mPresenter.checkBleApDeviceConnecting();
                }
            } else if (code == Constant.ERROR && ApHelper.getInstance().checkApDirectConnectionIsError() && mPresenter != null) {
                mPresenter.stopAPDirectConnection(null);
                showSmartDeviceList();
            }
        }

        @Override
        public void onNetworkChange() {
            if (checkActivityIsDestroy() || checkIsPause()) {
                return;
            }
            NooieLog.d("-->> debug DeviceApHelperListener onNetworkChange: ");
            if (mPresenter != null) {
                mPresenter.checkApDirectWhenNetworkChange();
            }
        }
    }

}
