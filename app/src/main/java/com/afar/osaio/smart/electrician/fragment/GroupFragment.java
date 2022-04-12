package com.afar.osaio.smart.electrician.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.base.NooieBaseSupportFragment;
import com.afar.osaio.smart.electrician.activity.GroupActivity;
import com.afar.osaio.smart.electrician.adapter.MixNewGroupAdapter;
import com.afar.osaio.smart.electrician.bean.DeviceHelper;
import com.afar.osaio.smart.electrician.bean.MixDeviceBean;
import com.afar.osaio.smart.electrician.eventbus.HomeLoadingEvent;
import com.afar.osaio.smart.electrician.eventbus.ListStyleSwitchEvent;
import com.afar.osaio.smart.electrician.manager.DeviceManager;
import com.afar.osaio.smart.electrician.manager.GroupHelper;
import com.afar.osaio.smart.electrician.manager.PowerStripHelper;
import com.afar.osaio.smart.electrician.presenter.HomePresenter;
import com.afar.osaio.smart.electrician.presenter.IHomePresenter;
import com.afar.osaio.smart.electrician.util.SpacesItemDecoration;
import com.afar.osaio.smart.electrician.view.IHomeView;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.entity.BannerResult;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.bean.WeatherBean;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.GroupBean;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.afar.osaio.smart.electrician.adapter.MixNewDeviceAdapter.VIEW_GRID;
import static com.afar.osaio.smart.electrician.adapter.MixNewDeviceAdapter.VIEW_LINEAR;

public class GroupFragment extends NooieBaseSupportFragment implements IHomeView, DeviceManager.IGroupListenerCallBack, DeviceManager.IHomeBeanCallback {

    @BindView(R.id.rcvGroups)
    RecyclerView rcvGroups;

    private Unbinder unbinder;
    private IHomePresenter homePresenter;
    private MixNewGroupAdapter mMixNewGroupAdapter;

    private SpacesItemDecoration spacesItemDecoration;

    private volatile static GroupFragment mGroupFragment;

    private View mContentView;
    /**
     * 该分组内的当前所有设备数量
     */
    private int mGroupNum;

    public static Fragment newInstance() {
        if (mGroupFragment == null) {
            synchronized (GroupFragment.class) {
                if (mGroupFragment == null) {
                    mGroupFragment = new GroupFragment();
                }
            }
        }
        return mGroupFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_group, container, false);
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
        setupGroupList();
    }

    private void initData() {
        homePresenter = new HomePresenter(this);
        DeviceManager.getInstance().setAllGroupListenerCallBack(this);
        DeviceManager.getInstance().setGroupHomeBeanCallback(this);
    }

    private void setupGroupList() {
        mMixNewGroupAdapter = new MixNewGroupAdapter();
        mMixNewGroupAdapter.setListener(new MixNewGroupAdapter.GroupItemListener() {
            @Override
            public void onItemClick(MixDeviceBean device) {
                if (device != null && device.isGroupBean()) {
                    /*if (GroupHelper.getInstance().isLampGroup(device.getGroupBean())) {
                        LampGroupActivity.toLampGroupActivity(getActivity(), device.getGroupBean().getId());
                    } else {
                        GroupActivity.toGroupActivity(getActivity(), device.getGroupBean().getId());
                    }*/
                    GroupActivity.toGroupActivity(getActivity(), device.getGroupBean().getId());
                }
            }

            @Override
            public void onSwitchClick(MixDeviceBean device, boolean isChecked) {
                if (device.isGroupBean()) {
                    EventBus.getDefault().post(new HomeLoadingEvent(ConstantValue.HOME_SHOW_LOADING));
                    if (GroupHelper.getInstance().isLampGroup(device.getGroupBean())) {//智能灯的群组
                        if (GroupHelper.getInstance().isOldDPLamp(device.getGroupBean())) {
                            homePresenter.controlLampGroup(device.getGroupBean().getId(), PowerStripHelper.getInstance().getLed_switch_id(), isChecked);
                        } else {
                            homePresenter.controlLampGroup(device.getGroupBean().getId(), PowerStripHelper.getInstance().getSwitch_led_id(), isChecked);
                        }
                    } else {//智能插座，排插
                        homePresenter.controlGroup(device.getGroupBean().getId(), isChecked);
                    }
                }
            }
        });
        GridLayoutManager layoutManager = new GridLayoutManager(NooieApplication.mCtx, 2);
        layoutManager.setSmoothScrollbarEnabled(true);
        layoutManager.setAutoMeasureEnabled(true);
        rcvGroups.setLayoutManager(layoutManager);
        mMixNewGroupAdapter.setType(VIEW_GRID);
        rcvGroups.setAdapter(mMixNewGroupAdapter);
        ((SimpleItemAnimator) rcvGroups.getItemAnimator()).setSupportsChangeAnimations(false);
        int leftRight = DisplayUtil.dpToPx(requireActivity(), 16);
        int topBottom = DisplayUtil.dpToPx(requireActivity(), 16);
        spacesItemDecoration = new SpacesItemDecoration(leftRight, topBottom);
        rcvGroups.addItemDecoration(spacesItemDecoration);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onListStyleSwitchEvent(ListStyleSwitchEvent event) {
        if (event.getSortType() == VIEW_GRID) {
            GridLayoutManager layoutManager = new GridLayoutManager(NooieApplication.mCtx, 2);
            layoutManager.setSmoothScrollbarEnabled(true);
            layoutManager.setAutoMeasureEnabled(true);
            rcvGroups.setLayoutManager(layoutManager);
            mMixNewGroupAdapter.setType(VIEW_GRID);
            ((SimpleItemAnimator) rcvGroups.getItemAnimator()).setSupportsChangeAnimations(false);
            if (rcvGroups.getItemDecorationCount() == 0) {
                rcvGroups.addItemDecoration(spacesItemDecoration);
            }
        } else if (event.getSortType() == VIEW_LINEAR) {
            rcvGroups.removeItemDecoration(spacesItemDecoration);
            LinearLayoutManager layoutManager = new LinearLayoutManager(NooieApplication.mCtx);
            rcvGroups.setLayoutManager(layoutManager);
            mMixNewGroupAdapter.setType(VIEW_LINEAR);
        }
    }

    @Override
    public void callbackHomeBean(HomeBean homeBean) {
        mMixNewGroupAdapter.setData(DeviceHelper.convertGroupDeviceBean(homeBean));
    }

    @Override
    public void onDpUpdate(long groupId, String dps) {
        NooieLog.e("---------------------->>>> HomeActivity onDpUpdate（） groupId " + groupId + " dps " + dps);
        homePresenter.loadGroupBean(groupId);
    }

    @Override
    public void onGroupInfoUpdate(long groupId) {
        NooieLog.e("---------------------->>>> HomeActivity onGroupInfoUpdate（） groupId " + groupId);
        homePresenter.loadGroupBean(groupId);
    }

    @Override
    public void onGroupRemoved(long groupId) {
        NooieLog.e("------->>> HomeActivity onGroupRemoved groupId " + groupId);
        removeGroup(groupId);
    }

    //------------------------ 移除群组item---------------
    private void removeGroup(long groupId) {
        List<MixDeviceBean> datas = mMixNewGroupAdapter.getData();
        if (CollectionUtil.isNotEmpty(datas)) {
            for (int i = 0; i < datas.size(); i++) {
                if (datas.get(i).isGroupBean() && (datas.get(i).getGroupBean().getId() == groupId)) {
                    mMixNewGroupAdapter.itemRemoved(i);
                }
            }
        }
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

    }

    @Override
    public void notifyLoadGroupSuccess(long groupId, GroupBean groupBean) {
        List<MixDeviceBean> datas = mMixNewGroupAdapter.getData();
        mGroupNum = datas.size();
        if (CollectionUtil.isNotEmpty(datas)) {
            for (int i = 0; i < datas.size(); i++) {
                if (datas.get(i).isGroupBean() && (datas.get(i).getGroupBean().getId() == groupId)) {
                    MixDeviceBean mixDeviceBean = new MixDeviceBean();
                    mixDeviceBean.setGroupBean(groupBean);
                    mMixNewGroupAdapter.updateItemChange(i, mixDeviceBean);
                }
            }
        }
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
