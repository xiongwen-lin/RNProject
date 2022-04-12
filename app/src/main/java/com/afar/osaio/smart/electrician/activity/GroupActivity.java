package com.afar.osaio.smart.electrician.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.electrician.adapter.HomeManagerDeviceAdapter;
import com.afar.osaio.smart.electrician.eventbus.GroupManageEvent;
import com.afar.osaio.smart.electrician.eventbus.HomeLoadingEvent;
import com.afar.osaio.smart.electrician.manager.DeviceManager;
import com.afar.osaio.smart.electrician.manager.FamilyManager;
import com.afar.osaio.smart.electrician.manager.GroupHelper;
import com.afar.osaio.smart.electrician.manager.PowerStripHelper;
import com.afar.osaio.smart.electrician.presenter.GroupPresenter;
import com.afar.osaio.smart.electrician.presenter.HomePresenter;
import com.afar.osaio.smart.electrician.presenter.IGroupPresenter;
import com.afar.osaio.smart.electrician.presenter.IHomePresenter;
import com.afar.osaio.smart.electrician.util.ErrorHandleUtil;
import com.afar.osaio.smart.electrician.util.SpacesItemDecoration;
import com.afar.osaio.smart.electrician.view.IGroupView;
import com.afar.osaio.smart.electrician.view.IHomeView;
import com.afar.osaio.smart.home.activity.HomeActivity;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.entity.BannerResult;
import com.tuya.smart.api.MicroContext;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.bean.WeatherBean;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.panelcaller.api.AbsPanelCallerService;
import com.tuya.smart.sdk.api.IGroupListener;
import com.tuya.smart.sdk.api.ITuyaGroup;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.GroupBean;
import com.tuya.smart.sdk.bean.ProductBean;
import com.tuya.smart.sdk.bean.Timer;
import com.tuya.smart.sdk.bean.TimerTask;

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

/**
 * GroupActivity
 *
 * @author Administrator
 * @date 2019/3/20
 */
public class GroupActivity extends BaseActivity implements IGroupView, IHomeView, DeviceManager.IDeviceListenerCallBack {

    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.rcvGroupDevice)
    RecyclerView rcvGroupDevice;


    private IGroupPresenter mGroupPresenter;
    private IHomePresenter homePresenter;
    private HomeManagerDeviceAdapter mDeviceAdapter;
    private long mGroupId;
    private ITuyaGroup mITuyaGroup;
    private List<Timer> mTimerList = new ArrayList<>();

    private String mGroupType;

    public static void toGroupActivity(Activity from, long groupId) {
        Intent intent = new Intent(from, GroupActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_GROUP_ID, groupId);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        ButterKnife.bind(this);
        NooieLog.e("-----------------------> onCreate homeID " + FamilyManager.getInstance().getCurrentHomeId());
        EventBus.getDefault().register(this);
        initView();
        initData();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        ivRight.setImageResource(R.drawable.menu_setting_icon_state_list);
        tvTitle.setText(R.string.create_group);
        setupDeviceList();
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
            return;
        } else {
            mGroupId = getCurrentIntent().getLongExtra(ConstantValue.INTENT_KEY_GROUP_ID, 0);
            DeviceManager.getInstance().setGroupDeviceListenerCallBack(this);
            mGroupPresenter = new GroupPresenter(this, mGroupId);
            homePresenter = new HomePresenter(this);
            setupGroup(mGroupId);
            registerGroupListener();
        }
    }

    private void registerGroupListener() {
        mITuyaGroup = TuyaHomeSdk.newGroupInstance(mGroupId);
        mITuyaGroup.registerGroupListener(new IGroupListener() {
            @Override
            public void onDpUpdate(long groupId, String dps) {
                NooieLog.e("GroupActivity dps  " + dps);
            }

            @Override
            public void onDpCodeUpdate(long groupId, Map<String, Object> dpCodeMap) {

            }

            @Override
            public void onGroupInfoUpdate(long groupId) {
                mGroupPresenter.onGroupInfoUpdate(groupId);
                NooieLog.e("GroupActivity onGroupInfoUpdate ");
            }

            @Override
            public void onGroupRemoved(long l) {
                if (!isPause()) {
                    HomeActivity.toHomeActivity(GroupActivity.this, HomeActivity.TYPE_REMOVE_GROUP);
                    finish();
                }
            }
        });
    }

    private void setupGroup(final long groupId) {
        showLoadingDialog();
        TuyaHomeSdk.newHomeInstance(FamilyManager.getInstance().getCurrentHomeId()).getHomeDetail(new ITuyaHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean homeBean) {
                getGroupBean(groupId);
                hideLoadingDialog();
            }

            @Override
            public void onError(String code, String msg) {
                getGroupBean(groupId);
                hideLoadingDialog();
            }
        });
    }

    private void getGroupBean(long groupId) {
        GroupBean group = TuyaHomeSdk.getDataInstance().getGroupBean(groupId);
        if (group != null) {
            tvTitle.setText(group.getName());
            showDevices(group.getDeviceBeans());
            if (!TextUtils.isEmpty(group.getProductId()) &&
                    (group.getProductId().equals(ConstantValue.SMART_PLUG_PRODUCTID)
                            || group.getProductId().equals(ConstantValue.SMART_PLUG_PRODUCTID_NEW)
                            || group.getProductId().equals(ConstantValue.SMART_PLUG_PRODUCTID_OLD)
                            || group.getProductId().equals(ConstantValue.SMART_PLUG_US_PRODUCTID_TWO)
                            || group.getProductId().equals(ConstantValue.SMART_PLUG_US_PRODUCTID_THREE)
                            || group.getProductId().equals(ConstantValue.SMART_PLUG_EU_PRODUCTID)
                            || group.getProductId().equals(ConstantValue.SMART_PLUG_UK_PRODUCTID)
                            || group.getProductId().equals(ConstantValue.SMART_PLUG_UK_PRODUCTID_TWO)
                            || group.getProductId().equals(ConstantValue.SMART_PLUG_UK_PRODUCTID_THREE)
                            || group.getProductId().equals(ConstantValue.SMART_PLUG_EU_PRODUCTID_TWO)
                            || group.getProductId().equals(ConstantValue.SMART_PLUG_EU_PRODUCTID_THREE)
                            || group.getProductId().equals(ConstantValue.SMART_PLUG_JP_PRODUCTID_ONE)
                            || group.getProductId().equals(ConstantValue.SMART_PLUG_EU_PRODUCTID_EIGHT)
                            || group.getProductId().equals(ConstantValue.SMART_PLUG_EU_PRODUCTID_NINE)
                            || group.getProductId().equals(ConstantValue.SMART_PLUG_US_PRODUCTID_FIVE)
                            || group.getProductId().equals(ConstantValue.SMART_PLUG_EU_PRODUCTID_TEN)
                            || group.getProductId().equals(ConstantValue.SMART_PLUG_US_PRODUCTID_SIX)
                            || group.getProductId().equals(ConstantValue.SMART_PLUG_UK_PRODUCTID_SIX)
                            || group.getProductId().equals(ConstantValue.SMART_PLUG_UK_PRODUCTID_SEVEN)
                    )) {
                mGroupType = ConstantValue.GROUP_FOR_PLUG;
            } else {
                mGroupType = ConstantValue.GROUP_FOR_LAMP;
            }
            ProductBean productBean = TuyaHomeSdk.getDataInstance().getProductBean(group.getProductId());
            GroupHelper.getInstance().getDPs(productBean.getSchemaInfo().getSchema());
            mGroupPresenter.getTimerWithTask(String.valueOf(mGroupId), String.valueOf(mGroupId));
        }
    }

    private void setupDeviceList() {
        mDeviceAdapter = new HomeManagerDeviceAdapter();
        mDeviceAdapter.setListener(new HomeManagerDeviceAdapter.HomeManagerDeviceListener() {
            @Override
            public void onItemClickListener(DeviceBean device) {
                if (device != null) {
                    if (PowerStripHelper.getInstance().isUserTuyaPanel(device)
                            || PowerStripHelper.getInstance().isPlug(device)
                            || PowerStripHelper.getInstance().isWallSwitch(device)
                            || PowerStripHelper.getInstance().isLamp(device)
                            || PowerStripHelper.getInstance().isPowerStrip(device)
                            || PowerStripHelper.getInstance().isMultiWallSwitch(device)) {
                        AbsPanelCallerService service = MicroContext.getServiceManager().findServiceByInterface(AbsPanelCallerService.class.getName());
                        service.goPanelWithCheckAndTip(GroupActivity.this, device.getDevId());
                    } else {
                        WrongDeviceActivity.toWrongDeviceActivity(GroupActivity.this, device.getDevId());
                    }

                }
            }


            @Override
            public void onSwitchClick(DeviceBean device, boolean isChecked) {
                if (device != null) {
                    EventBus.getDefault().post(new HomeLoadingEvent(ConstantValue.HOME_SHOW_LOADING));
                    if (PowerStripHelper.getInstance().isPowerStrip(device)
                            || PowerStripHelper.getInstance().isMultiWallSwitch(device)) {//控制排插的开关
                        if (PowerStripHelper.getInstance().isThreeHolesPowerStrip(device)
                                || PowerStripHelper.getInstance().isThreeHolesWallSwitch(device)) {
                            Map<String, Object> map = new HashMap<>();
                            map.put(PowerStripHelper.getInstance().getSwitch_1_id(), isChecked);
                            map.put(PowerStripHelper.getInstance().getSwitch_2_id(), isChecked);
                            map.put(PowerStripHelper.getInstance().getSwitch_3_id(), isChecked);
                            homePresenter.controlStrip(device.getDevId(), map);
                        } else if (PowerStripHelper.getInstance().isFiveHolesPowerStrip(device)) {
                            Map<String, Object> map = new HashMap<>();
                            map.put(PowerStripHelper.getInstance().getSwitch_1_id(), isChecked);
                            map.put(PowerStripHelper.getInstance().getSwitch_2_id(), isChecked);
                            map.put(PowerStripHelper.getInstance().getSwitch_3_id(), isChecked);
                            map.put(PowerStripHelper.getInstance().getSwitch_4_id(), isChecked);
                            map.put(PowerStripHelper.getInstance().getSwitch_usb1_id(), isChecked);
                            homePresenter.controlStrip(device.getDevId(), map);
                        } else {
                            Map<String, Object> map = new HashMap<>();
                            map.put(PowerStripHelper.getInstance().getSwitch_1_id(), isChecked);
                            map.put(PowerStripHelper.getInstance().getSwitch_2_id(), isChecked);
                            homePresenter.controlStrip(device.getDevId(), map);
                        }
                    } else if (PowerStripHelper.getInstance().isLamp(device)) {//控制智能灯
                        if (PowerStripHelper.getInstance().isOldDPLamp(device)) {
                            NooieLog.e("----------onCheckedChanged isChecked old " + isChecked);
                            homePresenter.controlLamp(device.getDevId(), PowerStripHelper.getInstance().getLed_switch_id(), isChecked);
                        } else {
                            NooieLog.e("----------onCheckedChanged isChecked new " + isChecked);
                            homePresenter.controlLamp(device.getDevId(), PowerStripHelper.getInstance().getSwitch_led_id(), isChecked);
                        }
                    } else {//控制单个插座的开关
                        homePresenter.controlDevice(device.devId, isChecked);
                    }
                }
            }
        });

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rcvGroupDevice.setLayoutManager(layoutManager);
        rcvGroupDevice.setAdapter(mDeviceAdapter);

        int leftRight = DisplayUtil.dpToPx(this, 16);
        int topBottom = DisplayUtil.dpToPx(this, 16);
        rcvGroupDevice.addItemDecoration(new SpacesItemDecoration(leftRight, topBottom));
    }

    private void showDevices(List<DeviceBean> devices) {
        if (CollectionUtil.isNotEmpty(devices)) {
            mDeviceAdapter.setData(devices);
        }
    }


    @Override
    public void notifyCreateGroupScheduleState(String msg) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(msg)) {
//            toggleScheduleOption();
        } else {
            ToastUtil.showToast(this, R.string.get_fail);
        }
    }

    @Override
    public void notifyCleanGroupScheduleState(String msg) {

    }

    @Override
    public void notifyOnGroupInfoUpdate(GroupBean groupBean) {
        if (groupBean != null) {
            tvTitle.setText(groupBean.getName());
        }
    }

    @Override
    public void notifyGetTimerWithTaskSuccess(TimerTask timerTask) {
        mTimerList.clear();
        if (mTimerList != null) {
            if (CollectionUtil.isNotEmpty(timerTask.getTimerList())) {
                mTimerList = timerTask.getTimerList();
            }
        }
    }

    @Override
    public void notifyGetTimerWithTaskFail(String errorCode, String errorMsg) {
        ErrorHandleUtil.toastTuyaError(this, errorMsg);
    }

    @Override
    public void notifyUpdateTimerStatusWithTaskSuccess(int position, Timer timer, boolean isOpen) {
        hideLoadingDialog();
    }

    @Override
    public void notifyUpdateTimerStatusWithTaskFail(String errorCode, String errorMsg, int position, Timer timer) {
        hideLoadingDialog();
    }

    @Override
    public void notifyRemoveTimerWithTaskSuccess(int position) {
//        mScheduleAdapter.itemRemoved(position);
//        showDeleteButton(mScheduleAdapter.getData());
        hideLoadingDialog();
        mGroupPresenter.getTimerWithTask(String.valueOf(mGroupId), String.valueOf(mGroupId));
    }

    @Override
    public void notifyRemoveTimerWithTaskFail(String errorCode, String errorMsg) {
        hideLoadingDialog();
        ErrorHandleUtil.toastTuyaError(this, errorMsg);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        DeviceManager.getInstance().release();
        if (homePresenter != null) {
            homePresenter.release();
        }
        if (mITuyaGroup != null) {
            mITuyaGroup.unRegisterGroupListener();
            mITuyaGroup.onDestroy();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGroupManageEvent(GroupManageEvent event) {
        if (event.isChange()) {
            GroupBean group = TuyaHomeSdk.getDataInstance().getGroupBean(mGroupId);
            if (group != null) {
                showDevices(group.getDeviceBeans());
            }
        }
    }

    @OnClick({R.id.ivLeft, R.id.ivRight})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.ivRight:
                if (mGroupType.equals(ConstantValue.GROUP_FOR_PLUG)) {
                    GroupSettingActivity.toGroupSettingActivity(GroupActivity.this, ConstantValue.REQUEST_CODE_GROUP_SETTING, mGroupId, ConstantValue.GROUP_TYPE_PLUG);
                } else {
                    GroupSettingActivity.toGroupSettingActivity(GroupActivity.this, ConstantValue.REQUEST_CODE_GROUP_SETTING, mGroupId, ConstantValue.GROUP_TYPE_LAMP);
                }
                break;
        }
    }

    @Override
    public void onDpUpdate(String devId, String dpStr) {
        NooieLog.e("---------------------->>>> GroupActivity onDpUpdate() devId " + devId + " dpStr " + dpStr);
        homePresenter.loadDeviceBean(devId);
    }

    @Override
    public void onRemoved(String devId) {

    }

    @Override
    public void onStatusChanged(String devId, boolean online) {

    }

    @Override
    public void onNetworkStatusChanged(String devId, boolean status) {

    }

    @Override
    public void onDevInfoUpdate(String devId) {

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
        List<DeviceBean> datas = mDeviceAdapter.getData();
        if (CollectionUtil.isNotEmpty(datas)) {
            for (int i = 0; i < datas.size(); i++) {
                if (datas.get(i).getDevId().equals(devId)) {
                    mDeviceAdapter.updateItemChange(i, deviceBean);
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
    public void onLoadBannerSuccess(List<BannerResult.BannerInfo> urlList) {

    }

    @Override
    public void onLoadBannerFail(String msg) {

    }
}
