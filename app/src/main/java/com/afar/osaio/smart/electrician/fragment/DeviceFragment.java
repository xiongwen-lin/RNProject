package com.afar.osaio.smart.electrician.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.base.NooieBaseSupportFragment;
import com.afar.osaio.smart.electrician.activity.AddDeviceSelectLinkageActivity;
import com.afar.osaio.smart.electrician.activity.WrongDeviceActivity;
import com.afar.osaio.smart.electrician.adapter.MixNewDeviceAdapter;
import com.afar.osaio.smart.electrician.bean.DeviceHelper;
import com.afar.osaio.smart.electrician.bean.MixDeviceBean;
import com.afar.osaio.smart.electrician.eventbus.HomeLoadingEvent;
import com.afar.osaio.smart.electrician.eventbus.ListStyleSwitchEvent;
import com.afar.osaio.smart.electrician.manager.DeviceManager;
import com.afar.osaio.smart.electrician.manager.PowerStripHelper;
import com.afar.osaio.smart.electrician.presenter.HomePresenter;
import com.afar.osaio.smart.electrician.presenter.IHomePresenter;
import com.afar.osaio.smart.electrician.util.SpacesItemDecoration;
import com.afar.osaio.smart.electrician.view.IHomeView;
import com.afar.osaio.smart.electrician.widget.RecyclerViewEmptySupport;
import com.afar.osaio.smart.scan.activity.AddCameraSelectActivity;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.entity.BannerResult;
import com.tuya.smart.api.MicroContext;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.bean.WeatherBean;
import com.tuya.smart.panelcaller.api.AbsPanelCallerService;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.GroupBean;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.afar.osaio.smart.electrician.adapter.MixNewDeviceAdapter.VIEW_GRID;
import static com.afar.osaio.smart.electrician.adapter.MixNewDeviceAdapter.VIEW_LINEAR;


public class DeviceFragment extends NooieBaseSupportFragment implements IHomeView, DeviceManager.IDeviceListenerCallBack, DeviceManager.IHomeBeanCallback {

    @BindView(R.id.rcvDevices)
    RecyclerViewEmptySupport rcvDevices;
    @BindView(R.id.emptyView)
    View emptyView;
    @BindView(R.id.clAddDevice)
    View clAddDevice;

    private Unbinder unbinder;
    private IHomePresenter homePresenter;
    private MixNewDeviceAdapter mMixDeviceAdapter;

    private SpacesItemDecoration spacesItemDecoration;

    private volatile static DeviceFragment mDeviceFragment;

    private View mContentView;

    public static Fragment newInstance() {
        if (mDeviceFragment == null) {
            synchronized (DeviceFragment.class) {
                if (mDeviceFragment == null) {
                    mDeviceFragment = new DeviceFragment();
                }
            }
        }
        return mDeviceFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_device, container, false);
        unbinder = ButterKnife.bind(this, mContentView);
        EventBus.getDefault().register(this);
        initView();
        initData();
        return mContentView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        EventBus.getDefault().unregister(this);
        if (homePresenter != null) {
            homePresenter.release();
        }
    }

    private void initView() {
        setupDeviceList();
    }

    private void initData() {
        homePresenter = new HomePresenter(this);
        DeviceManager.getInstance().setDeviceHomeBeanCallback(this);
       // DeviceManager.getInstance().setAllDeviceListenerCallBack(this);
    }

    private void setupDeviceList() {
        mMixDeviceAdapter = new MixNewDeviceAdapter();
        mMixDeviceAdapter.setListener(new MixNewDeviceAdapter.DeviceItemListener() {
            @Override
            public void onItemClick(MixDeviceBean device) {
                if (device != null && device.getDeviceBean() != null) {
                    if (PowerStripHelper.getInstance().isUserTuyaPanel(device.getDeviceBean())
                            || PowerStripHelper.getInstance().isPlug(device.getDeviceBean())
                            || PowerStripHelper.getInstance().isWallSwitch(device.getDeviceBean())
                            || PowerStripHelper.getInstance().isLamp(device.getDeviceBean())
                            || PowerStripHelper.getInstance().isPowerStrip(device.getDeviceBean())
                            || PowerStripHelper.getInstance().isMultiWallSwitch(device.getDeviceBean())
                            || PowerStripHelper.getInstance().isPetFeeder(device.getDeviceBean())
                            || PowerStripHelper.getInstance().isAirPurifier(device.getDeviceBean())) {
                        AbsPanelCallerService service = MicroContext.getServiceManager().findServiceByInterface(AbsPanelCallerService.class.getName());
                        service.goPanelWithCheckAndTip(getActivity(), device.getDeviceBean().getDevId());
                    } else {
                        WrongDeviceActivity.toWrongDeviceActivity(getActivity(), device.getDeviceBean().getDevId());
                    }
                }
            }

            @Override
            public void onSwitchClick(MixDeviceBean device, boolean isChecked) {
                if (device.getDeviceBean() != null) {
                    if (!TextUtils.isEmpty(device.getDeviceBean().getName())) {
                        boolean isLamp = PowerStripHelper.getInstance().isLamp(device.getDeviceBean());
                    }
                    EventBus.getDefault().post(new HomeLoadingEvent(ConstantValue.HOME_SHOW_LOADING));
                    if (PowerStripHelper.getInstance().isPowerStrip(device.getDeviceBean())
                            || PowerStripHelper.getInstance().isMultiWallSwitch(device.getDeviceBean())) {//控制排插的开关
                        if (PowerStripHelper.getInstance().isThreeHolesPowerStrip(device.getDeviceBean())
                                || PowerStripHelper.getInstance().isThreeHolesWallSwitch(device.getDeviceBean())) {
                            Map<String, Object> map = new HashMap<>();
                            map.put(PowerStripHelper.getInstance().getSwitch_1_id(), isChecked);
                            map.put(PowerStripHelper.getInstance().getSwitch_2_id(), isChecked);
                            map.put(PowerStripHelper.getInstance().getSwitch_3_id(), isChecked);
                            homePresenter.controlStrip(device.getDeviceBean().getDevId(), map);
                        } else if (PowerStripHelper.getInstance().isFiveHolesPowerStrip(device.getDeviceBean())) {
                            Map<String, Object> map = new HashMap<>();
                            map.put(PowerStripHelper.getInstance().getSwitch_1_id(), isChecked);
                            map.put(PowerStripHelper.getInstance().getSwitch_2_id(), isChecked);
                            map.put(PowerStripHelper.getInstance().getSwitch_3_id(), isChecked);
                            map.put(PowerStripHelper.getInstance().getSwitch_4_id(), isChecked);
                            map.put(PowerStripHelper.getInstance().getSwitch_usb1_id(), isChecked);
                            homePresenter.controlStrip(device.getDeviceBean().getDevId(), map);
                        } else if (PowerStripHelper.getInstance().isDimmerPlug(device.getDeviceBean())) {
                            homePresenter.controlDevice(device.getDeviceBean().devId, isChecked);
                        } else {
                            Map<String, Object> map = new HashMap<>();
                            map.put(PowerStripHelper.getInstance().getSwitch_1_id(), isChecked);
                            map.put(PowerStripHelper.getInstance().getSwitch_2_id(), isChecked);
                            homePresenter.controlStrip(device.getDeviceBean().getDevId(), map);
                        }
                    } else if (PowerStripHelper.getInstance().isLamp(device.getDeviceBean())) {//控制智能灯
                        if (PowerStripHelper.getInstance().isOldDPLamp(device.getDeviceBean())) {
                            homePresenter.controlLamp(device.getDeviceBean().getDevId(), PowerStripHelper.getInstance().getLed_switch_id(), isChecked);
                        } else {
                            homePresenter.controlLamp(device.getDeviceBean().getDevId(), PowerStripHelper.getInstance().getSwitch_led_id(), isChecked);
                        }
                    } else {//控制单个插座的开关
                        homePresenter.controlDevice(device.getDeviceBean().devId, isChecked);
                    }
                }
            }
        });
        GridLayoutManager layoutManager = new GridLayoutManager(NooieApplication.mCtx, 2);
        layoutManager.setSmoothScrollbarEnabled(true);
        layoutManager.setAutoMeasureEnabled(true);
        rcvDevices.setLayoutManager(layoutManager);
        mMixDeviceAdapter.setType(VIEW_GRID);
        rcvDevices.setAdapter(mMixDeviceAdapter);
        ((SimpleItemAnimator) rcvDevices.getItemAnimator()).setSupportsChangeAnimations(false);

        int leftRight = DisplayUtil.dpToPx(requireActivity(), 16);
        int topBottom = DisplayUtil.dpToPx(requireActivity(), 16);
        spacesItemDecoration = new SpacesItemDecoration(leftRight, topBottom);
        rcvDevices.addItemDecoration(spacesItemDecoration);

        rcvDevices.setEmptyView(emptyView);
        clAddDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //AddDeviceSelectLinkageActivity.toAddDeviceSelectLinkageActivity(getActivity(), false);
                AddCameraSelectActivity.toAddCameraSelectActivity(getActivity());
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onListStyleSwitchEvent(ListStyleSwitchEvent event) {
        if (event.getSortType() == VIEW_GRID) {
            GridLayoutManager layoutManager = new GridLayoutManager(NooieApplication.mCtx, 2);
            layoutManager.setSmoothScrollbarEnabled(true);
            layoutManager.setAutoMeasureEnabled(true);
            rcvDevices.setLayoutManager(layoutManager);
            mMixDeviceAdapter.setType(VIEW_GRID);
            ((SimpleItemAnimator) rcvDevices.getItemAnimator()).setSupportsChangeAnimations(false);
            if (rcvDevices.getItemDecorationCount() == 0) {
                rcvDevices.addItemDecoration(spacesItemDecoration);
            }
        } else if (event.getSortType() == VIEW_LINEAR) {
            rcvDevices.removeItemDecoration(spacesItemDecoration);
            LinearLayoutManager layoutManager = new LinearLayoutManager(NooieApplication.mCtx);
            rcvDevices.setLayoutManager(layoutManager);
            mMixDeviceAdapter.setType(VIEW_LINEAR);
        }
    }

    @Override
    public void onDpUpdate(String devId, String dpStr) {
        NooieLog.e("---------------------->>>> HomeActivity onDpUpdate() devId " + devId + " dpStr " + dpStr);
        homePresenter.loadDeviceBean(devId);
    }

    @Override
    public void onRemoved(String devId) {
        NooieLog.e("---------------------->>>> HomeActivity onRemoved() devId " + devId);
        removeDevice(devId);
    }

    @Override
    public void onStatusChanged(String devId, boolean online) {
        NooieLog.e("---------------------->>>> HomeActivity onStatusChanged() devId " + devId + "  online " + online);
        homePresenter.loadDeviceBean(devId);
    }

    @Override
    public void onNetworkStatusChanged(String devId, boolean status) {
        NooieLog.e("---------------------->>>> HomeActivity onNetworkStatusChanged（） devId " + devId + " status  " + status);
    }

    @Override
    public void onDevInfoUpdate(String devId) {
        NooieLog.e("---------------------->>>> HomeActivity onDevInfoUpdate（） devId " + devId);
    }

    @Override
    public void notifyLoadUserInfoState(String result) {

    }

    @Override
    public void loadHomeDetailSuccess(HomeBean homeBean) {

    }

    @Override
    public void loadHomeDetailFailed(String error) {

    }

    @Override
    public void notifyControlGroupState(String result) {

    }

    @Override
    public void notifyGroupDpUpdate(long groupId, String dps) {

    }

    @Override
    public void notifyControlDeviceState(String result, String msg) {

    }

    @Override
    public void notifyControlDeviceSuccess(String result) {

    }

    @Override
    public void notifyDeviceDpUpdate(String deviceId, String dps) {

    }

    @Override
    public void notifyLoadHomesSuccess(List<HomeBean> homes) {

    }

    @Override
    public void notifyLoadHomesFailed(String msg) {

    }

    @Override
    public void notifyChangeHomeState(String msg) {

    }

    @Override
    public void notifyLoadDeviceSuccess(String devId, DeviceBean deviceBean) {
        List<MixDeviceBean> datas = mMixDeviceAdapter.getData();
        if (CollectionUtil.isNotEmpty(datas)) {
            for (int i = 0; i < datas.size(); i++) {
                if (!datas.get(i).isGroupBean() && datas.get(i).getDeviceBean().getDevId().equals(devId)) {
                    MixDeviceBean mixDeviceBean = new MixDeviceBean();
                    mixDeviceBean.setDeviceBean(deviceBean);
                    mMixDeviceAdapter.updateItemChange(i, mixDeviceBean);
                }
            }
        }
    }

    @Override
    public void notifyLoadGroupSuccess(long groupId, GroupBean groupBean) {

    }

    @Override
    public void notifyLoadHomeListSuccess(String code, List<HomeBean> list) {

    }

    @Override
    public void onCheckNetworkStatus(String result, boolean isNetworkUsable) {

    }

    @Override
    public void onGetWeatherSuccess(WeatherBean weatherBean) {

    }

    @Override
    public void onGetWeatherFail(String errorCode, String errorMsg) {

    }

    @Override
    public void onLoadBannerSuccess(List<BannerResult.BannerInfo> bannerList) {

    }

    @Override
    public void onLoadBannerFail(String msg) {

    }

    //------------------------ 移除设备item---------------
    private void removeDevice(String devId) {
        List<MixDeviceBean> datas = mMixDeviceAdapter.getData();
        if (CollectionUtil.isNotEmpty(datas)) {
            for (int i = 0; i < datas.size(); i++) {
                if (!datas.get(i).isGroupBean() && (datas.get(i).getDeviceBean().getDevId().equals(devId))) {
                    mMixDeviceAdapter.itemRemoved(i);
                }
            }
        }
    }

    @Override
    public void callbackHomeBean(HomeBean homeBean) {
        mMixDeviceAdapter.setData(DeviceHelper.convertDeviceBean(homeBean));
    }
}
