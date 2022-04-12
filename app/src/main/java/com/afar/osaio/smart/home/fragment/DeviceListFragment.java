package com.afar.osaio.smart.home.fragment;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.widget.WiFiDialog;
import com.aspsine.swipetoloadlayout.OnLoadMoreListener;
import com.aspsine.swipetoloadlayout.OnRefreshListener;
import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;
import com.afar.osaio.account.helper.MyAccountHelper;
import com.afar.osaio.bean.ApDeviceInfo;
import com.afar.osaio.bean.DeviceItem;
import com.afar.osaio.bean.DeviceTypeInfo;
import com.afar.osaio.bean.ProductType;
import com.afar.osaio.routerdata.GetRouterDataFromCloud;
import com.afar.osaio.routerdata.SendHttpRequest;
import com.afar.osaio.smart.cache.BleApDeviceInfoCache;
import com.afar.osaio.smart.cache.DeviceListCache;
import com.afar.osaio.smart.device.Component.DeviceSortComponent;
import com.afar.osaio.smart.device.helper.DeviceConnectionHelper;
import com.afar.osaio.smart.event.NetworkChangeEvent;
import com.afar.osaio.smart.home.adapter.DeviceListAdapter;
import com.afar.osaio.smart.home.adapter.DeviceTypeAdapter;
import com.afar.osaio.smart.home.adapter.DevicesAdapter;
import com.afar.osaio.smart.home.adapter.DvDeviceListAdapter;
import com.afar.osaio.smart.home.adapter.RouterDeviceAdapter;
import com.afar.osaio.smart.home.adapter.listener.BleApDeviceListener;
import com.afar.osaio.smart.home.adapter.listener.DvDeviceListListener;
import com.afar.osaio.smart.lpipc.contract.GatewaySettingsContract;
import com.afar.osaio.smart.lpipc.presenter.GatewaySettingsPresenter;
import com.afar.osaio.smart.mixipc.activity.BluetoothScanActivity;
import com.afar.osaio.smart.mixipc.activity.ConnectApDeviceActivity;
import com.afar.osaio.smart.push.firebase.analytics.FirebaseAnalyticsManager;
import com.afar.osaio.smart.push.firebase.analytics.FirebaseConstant;
import com.afar.osaio.smart.router.RouterDetalisActivity;
import com.afar.osaio.smart.router.RouterOfflineHelpActivity;
import com.afar.osaio.smart.routerlocal.RouterInfo;
import com.afar.osaio.smart.scan.activity.AddCameraSelectActivity;
import com.afar.osaio.smart.event.DeviceChangeEvent;
import com.afar.osaio.smart.event.TabSelectedEvent;
import com.afar.osaio.smart.home.adapter.listener.DeviceListListener;
import com.afar.osaio.smart.scan.bean.NetworkChangeResult;
import com.afar.osaio.smart.scan.helper.ApHelper;
import com.afar.osaio.smart.smartlook.activity.LookDeviceActivity;
import com.afar.osaio.smart.smartlook.adapter.LockDeviceAdapter;
import com.afar.osaio.smart.smartlook.adapter.listener.LockDeviceListener;
import com.afar.osaio.smart.smartlook.cache.LockDeviceCache;
import com.nooie.common.utils.configure.CountryUtil;
import com.nooie.common.utils.notify.NotificationUtil;
import com.nooie.data.EventDictionary;
import com.nooie.sdk.api.network.base.bean.entity.GatewayDevice;
import com.nooie.sdk.base.Constant;
import com.nooie.sdk.bean.IpcType;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.db.entity.BleApDeviceEntity;
import com.nooie.sdk.db.entity.BleDeviceEntity;
import com.afar.osaio.util.DialogUtils;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.base.NooieBaseMainFragment;
import com.afar.osaio.base.NooieBaseSupportActivity;
import com.afar.osaio.smart.device.bean.ListDeviceItem;
import com.afar.osaio.smart.cache.DeviceInfoCache;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.home.contract.DeviceListContract;
import com.afar.osaio.smart.home.presenter.DeviceListPresenter;
import com.afar.osaio.smart.player.activity.NooiePlayActivity;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.widget.RelativePopupMenu;
import com.afar.osaio.widget.bean.RelativePopMenuItem;
import com.nooie.sdk.device.bean.APPairStatus;

import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.yokeyword.eventbusactivityscope.EventBusActivityScope;

public class DeviceListFragment extends NooieBaseMainFragment implements DeviceListContract.View, OnRefreshListener, OnLoadMoreListener,
        DeviceTypeAdapter.OnSetSelectDeviceTypeListener, SendHttpRequest.getRouterReturnInfo, GatewaySettingsContract.View {

    private final static int NETWORK_WEAK_TIP_CLOSE_STATE = 1;

    private DeviceListContract.Presenter mPresenter;
    private int currentItem = 0; // 切换Item
    private String routerConnectMsg = ""; // 获取设备连接状态

    public static DeviceListFragment newInstance() {
        Bundle args = new Bundle();
        DeviceListFragment fragment = new DeviceListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static void addRouterDevice(String deviceName, String deviceMac, int deviceType) {
        ListDeviceItem listDeviceItem = new ListDeviceItem(deviceName, deviceMac, deviceType, 1, "0");
        mRouterList.add(listDeviceItem);
    }

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.swipe_target)
    RecyclerView rvDevice;
    @BindView(R.id.sl_device_list)
    SwipeToLoadLayout swipeToLoadLayout;
    RelativePopupMenu mDeviceListMenu;
    @BindView(R.id.nwtDeviceList)
    View nwtDeviceList;
    @BindView(R.id.ivCloseIcon)
    ImageView ivCloseIcon;
    @BindView(R.id.deviceType)
    RecyclerView deviceType;
    @BindView(R.id.vMenuBarDivider)
    View vMenuBarDivider;
    @BindView(R.id.btnDeviceSortEdit)
    ImageView btnDeviceSortEdit;
    @BindView(R.id.vDeviceTypeDivider)
    View vDeviceTypeDivider;

    private GatewaySettingsContract.Presenter mGatewayPresenter;
    // 设备类型分类
    private DeviceTypeAdapter deviceTypeAdapter;
    private RouterDeviceAdapter mRouterDeviceAdapter;

    private DeviceSortComponent mDeviceSortComponent;
    private LockDeviceAdapter mLockDeviceAdapter;
    private DeviceListAdapter mDeviceListAdapter;
    private DvDeviceListAdapter mDvDeviceListAdapter;
    private DeviceChangeBroadcastReceiver mDeviceChangeBroadcastReceiver;
    private DeviceApHelperListener mDeviceApHelperListener;
    private RouterInfo mClickRouterInfo;
    private Dialog mFindApDirectConnectionDeviceDialog = null;
    private Dialog mReconnectToWifiDialog = null;
    private Dialog mDeleteDeviceDialog;
    private static List<ListDeviceItem> mRouterList = new ArrayList<>();
    private static List<ListDeviceItem> mRouterBindList = new ArrayList<>();
    private String routerWifiMac = "";
    private boolean checkPassword = false;
    private boolean mIsShowApDirectModeTip = false;

    private static final int MIN_DELAY_TIME = 1000; // 两次点击间隔不能少于1000ms
    private static long lastClickTime;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new DeviceListPresenter(this);
        new GatewaySettingsPresenter(this);
        registerDeviceChangeReceiver();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_list1, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ivLeft.setVisibility(View.GONE);
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.home_tab_label_devices);
        ivRight.setImageResource(R.drawable.menu_bar_right_add_icon);
        displayNetworkWeakTip(false);
        setupDeviceListMenu();
        setupDeviceListView();
        setupDeviceProductListView();
        setupIpcDeviceView();
        setupBleApDeviceView();
        setupDvDeviceListView();
        setupLockDevicesView();
        setRouterDeviceView();
        refreshDeviceSortEdit();
    }

    private void initView(View view) {
        ButterKnife.bind(this, view);
        EventBusActivityScope.getDefault(_mActivity).register(this);
        //setBindRouter();
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        NooieLog.d("-->> DeviceListFragment onResume");
        resumeData();
        registerDeviceApHelperListener();
    }

    /**
     * 防止快速点击
     *
     * @return
     */
    public static boolean isFastClick() {
        boolean flag = true;
        long currentClickTime = System.currentTimeMillis();
        if ((currentClickTime - lastClickTime) >= MIN_DELAY_TIME) {
            flag = false;
        }
        lastClickTime = currentClickTime;
        return flag;
    }

    private void resumeData() {
        if (MyAccountHelper.getInstance().isLogin()) {
            boolean isSortingDevice = mDeviceListAdapter != null && mDeviceListAdapter.isItemDragEnable();
            if (isSortingDevice) {
                return;
            }
            enableDeviceListViewRefresh(true);
            enableDeviceListViewLoadMore(false);
            if (mPresenter != null) {
                mPresenter.refreshDevices(mUserAccount, mUid);
            }
        } else {
            if (mPresenter != null) {
                mPresenter.stopRefreshTask();
                mPresenter.stopLoadMoreTask();
            }
            enableDeviceListViewRefreshOrLoadMore(false);
            if (!checkDeviceListTypeValid(mCurrentDeviceListType)) {
                mCurrentDeviceListType = DEVICE_LIST_TYPE_ALL;
            }

            if ((int) deviceType.getTag() == 1) {
                showDeviceItem(DRAG_ROUTER_TYPE);
            } else {
                showDeviceItem(mCurrentDeviceListType);
            }
            resetView();
        }
        if (mPresenter != null) {
            mPresenter.loadBleApDevices(mUserAccount);
        }
        getRouterInitCfg();
        if (ApHelper.getInstance().checkBleApDeviceConnectingExist() && mPresenter != null) {
            NooieLog.d("-->> debug DeviceListFragment resumeData: 1001");
            mPresenter.checkBleApDeviceConnecting();
            mPresenter.checkApDirectWhenNetworkChange();
        } else if (!ApHelper.getInstance().checkBleApDeviceConnectingExist() && mCurrentDeviceListType != DEVICE_LIST_TYPE_ALL) {
            NooieLog.d("-->> debug DeviceListFragment resumeData: 1002");
            if ((int) deviceType.getTag() == 0) {
                showDeviceItem(DEVICE_LIST_TYPE_ALL);
            }
            checkApDirectConnection();
        }
        cancelDeviceSort();
    }

    private void resetView() {
        if (mLockDeviceAdapter != null) {
            mLockDeviceAdapter.clearData();
        }

        if (mDeviceListAdapter != null) {
            mDeviceListAdapter.clearData();
        }

        if (mDvDeviceListAdapter != null) {
            mDvDeviceListAdapter.clearData();
        }

        if (mRouterDeviceAdapter != null) {
            mRouterDeviceAdapter.clearData();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        NooieLog.d("-->> DeviceListFragment onPause");
        unregisterDeviceApHelperListener();
    }

    private void setupDeviceListMenu() {
        List<Integer> menuIdList = new ArrayList<>();
        menuIdList.add(RelativePopupMenu.MENU_FIRST);
        mDeviceListMenu = new RelativePopupMenu(getActivity());
        mDeviceListMenu.setHeight(RecyclerView.LayoutParams.WRAP_CONTENT)
                .setWidth(DisplayUtil.dpToPx(NooieApplication.mCtx, 120))
                .showIcon(true)
                .dimBackground(false)
                .needAnimationStyle(true)
                .setAnimationStyle(R.style.TRM_ANIM_STYLE)
                .addMenuList(getMenuItems(menuIdList))
                .setOnMenuItemClickListener(new RelativePopupMenu.OnMenuItemClickListener() {
                    @Override
                    public void onMenuItemClick(int position) {
                        if (mDeviceListMenu != null) {
                            mDeviceListMenu.dismiss();
                        }
                        switch (position) {
                            case RelativePopupMenu.MENU_FIRST:
                                NooieDeviceHelper.trackDNEvent(true, EventDictionary.EVENT_ID_CLICK_MENU_LIST_ADD);
                                AddCameraSelectActivity.toAddCameraSelectActivity(_mActivity);
                                break;
                            case RelativePopupMenu.MENU_SECOND:
                                clickMenuSort();
                                break;
                        }
                    }
                });
    }

    private List<RelativePopMenuItem> getMenuItems(List<Integer> menuIdList) {
        List<RelativePopMenuItem> menuItems = new ArrayList<>();
        RelativePopMenuItem addDeviceItem = new RelativePopMenuItem();
        addDeviceItem.setId(RelativePopupMenu.MENU_FIRST);
        addDeviceItem.setIcon(R.drawable.nav_menu_item_add);
        addDeviceItem.setTitle(getString(R.string.home_nav_menu_add));

        RelativePopMenuItem sortDeviceItem = new RelativePopMenuItem();
        sortDeviceItem.setId(RelativePopupMenu.MENU_SECOND);
        sortDeviceItem.setIcon(R.drawable.nav_menu_item_sort);
        sortDeviceItem.setTitle(getString(R.string.home_nav_menu_sort));
        if (CollectionUtil.isEmpty(menuIdList)) {
            menuItems.add(addDeviceItem);
            //menuItems.add(sortDeviceItem);
            return menuItems;
        }

        if (menuIdList.contains(RelativePopupMenu.MENU_FIRST)) {
            menuItems.add(addDeviceItem);
        }

        if (menuIdList.contains(RelativePopupMenu.MENU_SECOND)) {
            //menuItems.add(sortDeviceItem);
        }
        return menuItems;
    }

    public void setupDeviceListView() {
        swipeToLoadLayout.setOnRefreshListener(this);
        swipeToLoadLayout.setOnLoadMoreListener(this);
        enableDeviceListViewRefresh(true);
        enableDeviceListViewLoadMore(false);

        //rvDevice.setLayoutManager(new LinearLayoutManager(NooieApplication.mCtx));
        GridLayoutManager gridLayoutManager = new GridLayoutManager(NooieApplication.mCtx, 2);
        rvDevice.setLayoutManager(gridLayoutManager);
        // deviceType
        deviceType.setLayoutManager(new GridLayoutManager(NooieApplication.mCtx, 5));
    }

    private void startRefresh() {
        if (!MyAccountHelper.getInstance().isLogin()) {
            return;
        }
        if (swipeToLoadLayout != null) {
            swipeToLoadLayout.setRefreshing(true);
        }
    }

    private void stopRefresh() {
        if (swipeToLoadLayout != null && swipeToLoadLayout.isRefreshing()) {
            boolean isRefreshEnable = swipeToLoadLayout.isRefreshEnabled();
            if (!isRefreshEnable) {
                enableDeviceListViewRefresh(true);
            }
            swipeToLoadLayout.setRefreshing(false);
            if (!isRefreshEnable) {
                enableDeviceListViewRefresh(false);
            }
        }
    }

    private void stopLoadMore() {
        if (swipeToLoadLayout != null && swipeToLoadLayout.isLoadingMore()) {
            swipeToLoadLayout.setLoadingMore(false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBusActivityScope.getDefault(_mActivity).unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.destroy();
        }

        if (mGatewayPresenter != null) {
            mGatewayPresenter.destroy();
        }

        unRegisterDeviceChangeReceiver();
        destroyDeviceApHelperListener();
        hideFindApDirectConnectionDialog();
        hideDeleteDeviceDialog();
    }

    @Override
    public void setPresenter(DeviceListContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Subscribe
    public void onTabSelectedEvent(TabSelectedEvent event) {
        NooieLog.d("-->> Receive TabSelectedEvent DeviceListFragment onTabSelectedEvent  position=" + event.position);
        if (event == null || event.position != HomeFragment.SECOND) {
            return;
        }
        //refreshData();
        resumeData();
    }

    @Subscribe
    public void onDeviceChangeEvent(DeviceChangeEvent event) {
        if (event == null) {
            return;
        }

        if (event.action == DeviceChangeEvent.DEVICE_CHANGE_ACTION_UPDATE) {
            startRefresh();
        } else if (event.action == DeviceChangeEvent.DEVICE_CHANGE_ACTION_FIND_GRAND_LOCATION_PERMISSION) {
            checkApDirectConnection();
        }
    }

    @Subscribe
    public void onNetworkChangeEvent(NetworkChangeEvent event) {
        if (event == null) {
            return;
        }
        //getBindRouterDevice();
        getRouterInitCfg();

//        getSysStatusCfg();// 判断是否在线
        // 绑定设备时采用
//        mRouterDeviceAdapter.upData("", routerWifiMac);
        Log.e("网络连接码", "" + event.state);
        displayNetworkWeakTip(event.state == NetworkChangeEvent.NETWORK_CHANGE_DISCONNECTED);
    }

    @Override
    public void onRefresh() {
        if (MyAccountHelper.getInstance().isLogin() && mPresenter != null) {
            mPresenter.refreshDevices(mUserAccount, mUid);
        }
        if (mPresenter != null) {
            mPresenter.loadBleApDevices(mUserAccount);
        }
        getRouterInitCfg();
    }

    @Override
    public void onLoadMore() {
        if (mPresenter != null) {
            mPresenter.loadMoreDevice(mUserAccount);
        }
    }

    @Override
    public void onLoadDeviceSuccess(List<ListDeviceItem> devices) {
        stopRefresh();
        stopLoadMore();
        setFirebaseUserProperty();
    }

    @OnClick({R.id.ivLeft, R.id.ivRight, R.id.ivCloseIcon, R.id.btnDeviceSortEdit})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                clickMenuLeft();
                break;
            case R.id.ivRight:
                clickTopRight();
                break;
            case R.id.ivCloseIcon:
                displayNetworkWeakTip(false);
                saveNetworkWeakTipClose();
                break;
            case R.id.btnDeviceSortEdit:
                clickMenuSort();
                break;
        }
    }

    private void clickTopRight() {
        //refreshPopupMenu();
        if (mDeviceListAdapter != null && !mDeviceListAdapter.isItemDragEnable()) {
            //mDeviceListMenu.showAsDropDown(ivRight, -DisplayUtil.dpToPx(NooieApplication.mCtx, 80), -DisplayUtil.dpToPx(NooieApplication.mCtx, 10));
            NooieDeviceHelper.trackDNEvent(true, EventDictionary.EVENT_ID_CLICK_MENU_LIST_ADD);
            AddCameraSelectActivity.toAddCameraSelectActivity(_mActivity);
        } else if (mDeviceListAdapter != null) {
            if (mDeviceSortComponent != null) {
                Map<String, Integer> updateSortDevices = mDeviceSortComponent.getUpdateSortDevices();
                if (mPresenter != null) {
                    mPresenter.updateDeviceSort(mUserAccount, updateSortDevices, mDeviceSortComponent.getDeviceIdAndIdMap());
                }
                mDeviceSortComponent.setDragEnable(false);
            }
            Map<String, Integer> bleApDeviceSortMap = mDeviceListAdapter.getBleDeviceSortMap();
            if (mPresenter != null) {
                mPresenter.updateBleApDeviceSort(mUserAccount, bleApDeviceSortMap);
            }
            mDeviceListAdapter.toggleItemDrag();
            showItemDrag(false);
        }
    }

    private void clickMenuSort() {
        if (!((_mActivity != null && ((NooieBaseSupportActivity) _mActivity).checkLogin("", ""))
                || ApHelper.getInstance().checkBleApDeviceConnectingExist()) || mCurrentDeviceListType != DEVICE_LIST_TYPE_ALL) {
            return;
        }
        boolean isDeviceSortEnable = mCurrentDeviceListType == DEVICE_LIST_TYPE_ALL && mDeviceListAdapter != null && mDeviceListAdapter.isDeviceSortEnable();
        if (!isDeviceSortEnable) {
            return;
        }
        if (mDeviceListAdapter != null) {
            mDeviceListAdapter.toggleItemDrag();
        }
        showItemDrag(true);
    }

    private void clickMenuLeft() {
        if (mDeviceListAdapter != null && mDeviceListAdapter.isItemDragEnable()) {
            if (mDeviceListAdapter != null) {
                mDeviceListAdapter.toggleItemDrag();
            }
            showItemDrag(false);
            //refreshDeviceListByCache();
            updateDeviceItemAdapter();
        } else {
            if (mCurrentDeviceListType != DEVICE_LIST_TYPE_ALL) {
                ivLeft.setVisibility(View.GONE);
                if ((int) deviceType.getTag() == 0) {
                    showDeviceItem(DEVICE_LIST_TYPE_ALL);
                }
            }
        }
    }

    private void showItemDrag(boolean show) {
        if (checkNull(ivLeft, ivRight)) {
            return;
        }
        if (show) {
            deviceType.setVisibility(View.GONE);
            ivLeft.setVisibility(View.VISIBLE);
            ivRight.setImageResource(R.drawable.menu_confirm_icon_state_list);
            tvTitle.setGravity(Gravity.CENTER);
            vMenuBarDivider.setVisibility(View.VISIBLE);
        } else {
            deviceType.setVisibility(View.VISIBLE);
            ivLeft.setVisibility(View.GONE);
            ivRight.setImageResource(R.drawable.menu_bar_right_add_icon);
            tvTitle.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            vMenuBarDivider.setVisibility(View.GONE);
        }
        enableDeviceListViewRefreshOrLoadMore(!show);
        displayDeviceTypeNavView(!show);
    }

    private void cancelDeviceSort() {
        if (checkNull(mDeviceListAdapter, ivLeft)) {
            return;
        }
        if (mDeviceListAdapter.isItemDragEnable()) {
            mDeviceListAdapter.toggleItemDrag();
            showItemDrag(false);
        }
    }

    @Override
    public void onLoadDeviceSuccessEnd() {
        //DeviceConfigureService.getInstance().log();
        stopRefresh();
        stopLoadMore();
        preConnectDevice(getAllIpcDeviceList());
        updateDeviceItemAdapter();
        if ((int) deviceType.getTag() == 1) {
            showDeviceItem(DRAG_ROUTER_TYPE);
        } else {
            showDeviceItem(mCurrentDeviceListType);
        }
        refreshDeviceSortEdit();
    }

    private void checkApDirectConnection() {
        //hide auto check ap
        /*
        NooieLog.d("-->> debug DeviceListFragment checkApDirectConnection: 1000 state=" + ApHelper.getInstance().getApDirectConnectionCheckState());
        if (ApHelper.getInstance().apDirectConnectionCheckFinish()) {
            //ApHelper.getInstance().setApDirectConnectionCheckState(ApHelper.AP_DIRECT_CONNECTION_CHECK_NORMAL);
            return;
        }
        if (checkActivityIsDestroy() || checkIsPause() || mPresenter == null) {
            return;
        }
        NooieLog.d("-->> debug DeviceListFragment checkApDirectConnection: 1001");
        mPresenter.checkApDirectConnection();
        NooieLog.d("-->> debug DeviceListFragment checkApDirectConnection: 1002");
         */
    }

    @Override
    public void onLoadDeviceFailed(String result) {
    }

    @Override
    public void notifyUpdateDeviceOpenStatusResult(String result, String deviceId, int status) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            if (mDeviceListAdapter != null) {
                mDeviceListAdapter.updateItemOpenStatus(ProductType.PRODUCT_IPC, deviceId, status);
            }
            NooieDeviceHelper.updateDeviceOpenStatus(deviceId, status);
        } else {
            if (mPresenter != null) {
                mPresenter.getDeviceOpenStatus(deviceId);
            }
            ToastUtil.showToast(_mActivity, R.string.camera_setting_warn_msg_set_sleep_fail);
        }
        ((NooieBaseSupportActivity) _mActivity).hideLoading();
    }

    @Override
    public void notifyGetDeviceOpenStatusResult(String result, String deviceId, int status) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            if (mDeviceListAdapter != null) {
                mDeviceListAdapter.updateItemOpenStatus(ProductType.PRODUCT_IPC, deviceId, status);
            }
            NooieDeviceHelper.updateDeviceOpenStatus(deviceId, status);
        } else {
        }
    }

    @Override
    public void notifyUpdateDeviceCacheSort(String result) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            updateDeviceItemAdapter();
        }
    }

    private void refreshDeviceList(List<ListDeviceItem> devices) {
        mRouterList.clear();
        mRouterList.addAll(CollectionUtil.safeFor(devices));
        mRouterDeviceAdapter.setData(mRouterList);
    }

    private void preConnectDevice(List<ListDeviceItem> deviceList) {
        List<BindDevice> bindDevices = new ArrayList<>();
        List<BindDevice> offLineDevices = new ArrayList<>();
        for (ListDeviceItem device : CollectionUtil.safeFor(deviceList)) {
            if (device != null && device.getBindDevice() != null && device.getBindDevice().getOnline() == ApiConstant.ONLINE_STATUS_ON) {
                bindDevices.add(device.getBindDevice());
            } else if (device != null && device.getBindDevice() != null) {
                offLineDevices.add(device.getBindDevice());
            }
        }
        NooieDeviceHelper.removeOffLineDeviceConn(offLineDevices);
        NooieDeviceHelper.tryConnectionToDevice(mUid, bindDevices, false);
    }

    @Override
    public void onSelectItem(int position) {
        /*if (MyAccountHelper.getInstance().isLogin()) {
        } else if (position == 1) {
            if (!isFastClick()) {
                SignInActivity.toSignInActivity(getActivity(), "", "", false);
            }
        }*/
        deviceTypeAdapter.setSelectDeviceType(position);
        deviceType.setTag(position);
        if (position == 1) {
            showDeviceItem(DRAG_ROUTER_TYPE);
        } else {
            showDeviceItem(ApHelper.getInstance().checkBleApDeviceConnectingExist() ? DEVICE_LIST_TYPE_DV : DEVICE_LIST_TYPE_ALL);
        }
        currentItem = position;
        refreshDeviceSortEdit();
    }


    @Override
    public void onDeleteDeviceResult(String result, String deviceId) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            NooieDeviceHelper.sendRemoveDeviceBroadcast(ConstantValue.REMOVE_DEVICE_TYPE_IPC, deviceId);
        }
    }

    @Override
    public void onCheckApDirectConnection(int state, int resultType, String ssid, APPairStatus status, String uuid) {
        NooieLog.d("-->> debug DeviceListFragment onCheckApDirectConnection: 1000 state=" + state + " status=" + status + " ssid=" + ssid + " uuid=" + uuid);
        if (checkActivityIsDestroy() || checkIsPause()) {
            return;
        }
        NooieLog.d("-->> debug DeviceListFragment onCheckApDirectConnection: 1001");
        if (state == SDKConstant.SUCCESS && status == APPairStatus.AP_PAIR_NO_RECV_WIFI && ApHelper.getInstance().apDirectConnectionCheckable()) {
            NooieLog.d("-->> debug DeviceListFragment onCheckApDirectConnection: 1002");
            showFindApDirectConnectionDialog(ssid);
        } else if (state == SDKConstant.ERROR && resultType == ApHelper.CHECK_AP_DIRECT_CONNECTION_RESULT_GET_SSID_FAIL) {
            NooieLog.d("-->> debug DeviceListFragment onCheckApDirectConnection: 1003");
            //EventBus.getDefault().post(new HomeActionEvent(HomeActionEvent.HOME_ACTION_LOCATION_PERMISSION));
        }
    }

    @Override
    public void onStartAPDirectConnect(int state, int connectionMode, String deviceSsid, boolean isAccessLive) {
        NooieLog.d("-->> debug DeviceListFragment onStartAPDirectConnect: 1000 state=" + state + " connectionMode=" + connectionMode);
        if (checkActivityIsDestroy() || checkIsPause() || _mActivity == null) {
            ApHelper.getInstance().setApDirectConnectionCheckState(ApHelper.AP_DIRECT_CONNECTION_CHECK_NORMAL);
            return;
        }
        if (state == SDKConstant.SUCCESS && connectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            NooieLog.d("-->> debug DeviceListFragment onStartAPDirectConnect: 1001");
            gotoApDirectConnectionDevice(true, deviceSsid, isAccessLive);
        } else {
            NooieLog.d("-->> debug DeviceListFragment onStartAPDirectConnect: 1002");
            ApHelper.getInstance().setApDirectConnectionCheckState(ApHelper.AP_DIRECT_CONNECTION_CHECK_NORMAL);
        }
    }

    @Override
    public void onStopAPDirectConnection(int state) {
        if (checkActivityIsDestroy()) {
            return;
        }
    }

    @Override
    public void onLoadApDevice(int state, ApDeviceInfo device) {
    }

    @Override
    public void onUpdateApDeviceOpenStatus(int state, String deviceSsid, String deviceId, int status) {
        if (checkActivityIsDestroy() || checkNull(mDvDeviceListAdapter)) {
            return;
        }
        if (state == SDKConstant.SUCCESS) {
            mDvDeviceListAdapter.updateDvDeviceOpenStatus(deviceId, status);
        } else {
            int openStatus = status == ApiConstant.OPEN_STATUS_ON ? ApiConstant.OPEN_STATUS_OFF : ApiConstant.OPEN_STATUS_ON;
            mDvDeviceListAdapter.updateDvDeviceOpenStatus(deviceId, openStatus);
        }
        ((NooieBaseSupportActivity) _mActivity).hideLoading();
    }

    @Override
    public void onCheckApDirectWhenNetworkChange(int state, NetworkChangeResult result) {
        NooieLog.d("-->> debug DeviceListFragment onCheckApDirectWhenNetworkChange: 1000");
        if (checkActivityIsDestroy() || checkIsPause() || checkNull(mDeviceListAdapter, mDvDeviceListAdapter, mPresenter)) {
            return;
        }
        NooieLog.d("-->> debug DeviceListFragment onCheckApDirectWhenNetworkChange: 1001");
        if (state == SDKConstant.SUCCESS) {
            NooieLog.d("-->> debug DeviceListFragment onCheckApDirectWhenNetworkChange: 1002");
            boolean isNeedToStopApDirectConnection = result != null && result.getIsConnected() && !TextUtils.isEmpty(result.getSsid())
                    && !(NooieDeviceHelper.checkApFutureCode(result.getSsid()) || NooieDeviceHelper.checkBluetoothApFutureCode(result.getSsid(), ""));
            if (!isNeedToStopApDirectConnection) {
                NooieLog.d("-->> debug DeviceListFragment onCheckApDirectWhenNetworkChange: 1003");
                return;
            }
            NooieLog.d("-->> debug DeviceListFragment onCheckApDirectWhenNetworkChange: 1004");
            if (mCurrentDeviceListType != DEVICE_LIST_TYPE_ALL) {
                NooieLog.d("-->> debug DeviceListFragment onCheckApDirectWhenNetworkChange: 1005");
                cancelDeviceSort();
                if ((int) deviceType.getTag() == 0) {
                    showDeviceItem(DEVICE_LIST_TYPE_ALL);
                }
            }
            mPresenter.stopAPDirectConnection(null);
        }
    }

    @Override
    public void onLoadBleApDevices(int result, List<BleApDeviceEntity> bleApDeviceEntities) {

        NooieLog.d("-->> debug DeviceListFragment ble ap deivce size=" + CollectionUtil.size(bleApDeviceEntities));
        updateDeviceItemAdapter();
        if ((int) deviceType.getTag() == 0) {
            showDeviceItem(mCurrentDeviceListType);
        } else {
            showDeviceItem(DRAG_ROUTER_TYPE);
        }
    }

    @Override
    public void checkBleApDeviceConnecting(int state, ApDeviceInfo result) {
        if (checkActivityIsDestroy() || checkIsPause()) {
            return;
        }
        boolean isAbleToSetupApMode = ApHelper.getInstance().checkBleApDeviceConnectingExist() && state == SDKConstant.SUCCESS && result != null && result.getBindDevice() != null;
        if (isAbleToSetupApMode) {
            List<ApDeviceInfo> devices = new ArrayList<>();
            devices.add(result);
            if (mDvDeviceListAdapter != null) {
                stopRefresh();
                stopLoadMore();
                cancelDeviceSort();
                enableDeviceListViewRefreshOrLoadMore(false);
                if ((int) deviceType.getTag() == 0) {
                    showDeviceItem(DEVICE_LIST_TYPE_DV);
                    showApDirectModeTip();
                }
                mDvDeviceListAdapter.setData(devices);
            }
        } else {
            String model = "";
            if (ApHelper.getInstance().getCurrentApDeviceInfo() != null && ApHelper.getInstance().getCurrentApDeviceInfo().getBindDevice() != null) {
                model = ApHelper.getInstance().getCurrentApDeviceInfo().getBindDevice().getType();
            }
            NooieLog.d("-->> debug DeviceListFragment checkBleApDeviceConnecting: 1002 model=" + model);
            ApHelper.getInstance().tryResetApConnectMode(model, new ApHelper.APDirectListener() {
                @Override
                public void onSwitchConnectionMode(boolean result, int connectionMode, String deviceId) {
                    stopRefresh();
                    stopLoadMore();
                    cancelDeviceSort();
                    enableDeviceListViewRefreshOrLoadMore(true);
                    showDeviceItem(DEVICE_LIST_TYPE_ALL);
                }
            });
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
    public void onUpdateBleApDeviceSort(int state) {
        if (checkActivityIsDestroy()) {
            return;
        }
        if (state == SDKConstant.SUCCESS) {
            updateDeviceItemAdapter();
        }
    }

    @Override
    public void onLoadRouterDevices(int result, List<ListDeviceItem> devices) {
        if (checkActivityIsDestroy()) {
            return;
        }
        NooieLog.d("-->> debug DeviceListFragment onLoadRouterDevices deivce size=" + CollectionUtil.size(devices));
        refreshDeviceList(devices);
        if ((int) deviceType.getTag() == 0) {
            showDeviceItem(mCurrentDeviceListType);
        } else {
            showDeviceItem(DRAG_ROUTER_TYPE);
        }
    }

    @Override
    public void onDeleteRouterDevice(int state, String routerDevice) {
        if (checkActivityIsDestroy() || checkNull(mRouterDeviceAdapter)) {
            return;
        }
        if (state == SDKConstant.SUCCESS) {
            mRouterDeviceAdapter.removeRouterDevice(routerDevice);
        }
    }

    private List<ListDeviceItem> getAllIpcDeviceList() {
        DeviceInfoCache.getInstance().replaceCaches();
        List<ListDeviceItem> devices = NooieDeviceHelper.convertListDeviceItem(DeviceInfoCache.getInstance().getAllDeviceInfo());
        return devices;
    }

    private List<BleApDeviceEntity> getAllBleApDeviceList() {
        List<BleApDeviceEntity> deviceList = new ArrayList<>();
        deviceList.addAll(CollectionUtil.safeFor(BleApDeviceInfoCache.getInstance().getAllBleApDevice()));
        return CollectionUtil.isEmpty(deviceList) ? deviceList : NooieDeviceHelper.sortBleApDevices(deviceList);
    }

    @Override
    public void notifyLoadLockDeviceSuccess(String result, List<BleDeviceEntity> lockDevices) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            NooieLog.d("-->> DeviceListFragment notifyLoadLockDeviceSuccess size=" + CollectionUtil.size(lockDevices));
            mLockDeviceAdapter.setData(LockDeviceCache.getInstance().getAllCache());
            updateDeviceItemAdapter();
            if ((int) deviceType.getTag() == 0) {
                showDeviceItem(mCurrentDeviceListType);
            }
        }
    }

    private void setupDeviceProductListView() {
        mDeviceListAdapter = new DeviceListAdapter();
        mDeviceListAdapter.setListener(new DeviceListListener() {
            @Override
            public void onItemMoreClick(ProductType productType) {
                if ((int) deviceType.getTag() == 0) {
                    if (ProductType.PRODUCT_IPC == productType) {
                        showDeviceItem(DEVICE_LIST_TYPE_IPC);
                    } else if (ProductType.PRODUCT_LOCK == productType) {
                        showDeviceItem(DEVICE_LIST_TYPE_LOCK);
                    }
                }
            }

            @Override
            public void onAddDevice() {
                NooieDeviceHelper.trackDNEvent(true, EventDictionary.EVENT_ID_CLICK_ADD_DEVICE);
                AddCameraSelectActivity.toAddCameraSelectActivity(_mActivity);
            }
        });

        mDeviceSortComponent = new DeviceSortComponent();
        mDeviceListAdapter.setDeviceCortComponent(mDeviceSortComponent);
    }

    private void setupIpcDeviceView() {
        setupIpcDeviceListener();

        deviceTypeAdapter = new DeviceTypeAdapter();
        setDeviceTypeInfo();
        deviceTypeAdapter.setDeviceTypeSelectListener(this);
        deviceType.setAdapter(deviceTypeAdapter);
    }

    private void setDeviceTypeInfo() {
        List<DeviceTypeInfo> deviceTypeInfoList = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            DeviceTypeInfo deviceTypeInfo = new DeviceTypeInfo();
            if (i == 0) {
                deviceTypeInfo.setSelect(true);
                deviceType.setTag(0);
            } else {
                deviceTypeInfo.setSelect(false);
            }

            if (i == 0) {
                deviceTypeInfo.setDeviceType(NooieApplication.mCtx.getString(R.string.camera));
            } else if (i == 1) {
                deviceTypeInfo.setDeviceType(NooieApplication.mCtx.getString(R.string.router_detail_router));
            }
            deviceTypeInfoList.add(deviceTypeInfo);
        }
        deviceTypeAdapter.setDeviceTypeInfo(deviceTypeInfoList);
    }

    private void setupIpcDeviceListener() {
        setupDevicesAdapterListener(new DevicesAdapter.OnItemClickListener() {
            @Override
            public void onClickItem(ListDeviceItem device) {
                if (device == null) {
                    return;
                }
                if (device.getOnline() == ApiConstant.ONLINE_STATUS_ON) {
                    openDevice(device);
                } else if (device.isOpenCloud()) {
                    openDevice(device);
                } else {
                    //showOfflineDialog(device);
                    showDeleteDeviceDialog(device);
                }
            }

            @Override
            public void onClickRefresh(ListDeviceItem device) {
                openDevice(device);
            }

            @Override
            public void onChangeSleep(ListDeviceItem item, boolean openCamera) {
                if (item != null && !TextUtils.isEmpty(item.getDeviceId())) {
                    ((NooieBaseSupportActivity) _mActivity).showLoading(true);
                    if (mPresenter != null) {
                        mPresenter.updateDeviceOpenStatus(item.getDeviceId(), openCamera ? ApiConstant.OPEN_STATUS_ON : ApiConstant.OPEN_STATUS_OFF);
                    }
                }
            }

            @Override
            public void onAddTop(ListDeviceItem device) {
            }

            @Override
            public void onStartDragItem(RecyclerView.ViewHolder holder) {
                if (mDeviceSortComponent != null) {
                    mDeviceSortComponent.startDrag(holder);
                }
            }

            private void openDevice(ListDeviceItem device) {
                if (device == null || device.getBindDevice() == null) {
                    return;
                }
                String deviceId = device.getBindDevice().getUuid();
                String model = TextUtils.isEmpty(device.getBindDevice().getType()) ? DeviceInfoCache.getInstance().getDeviceModelById(deviceId) : device.getBindDevice().getType();
                //NooiePlayActivity.startPlayActivity(_mActivity, deviceId, IpcType.getIpcType(model), ConstantValue.NOOIE_PLAYBACK_TYPE_LIVE, ConstantValue.NOOIE_PLAYBACK_SOURCE_TYPE_NORMAL, 0, ConstantValue.ROUTE_SOURCE_NORMAL, ConstantValue.CONNECTION_MODE_QC);
                NooiePlayActivity.startPlayActivity(_mActivity, deviceId, model, ConstantValue.NOOIE_PLAYBACK_TYPE_LIVE, ConstantValue.NOOIE_PLAYBACK_SOURCE_TYPE_NORMAL, 0, ConstantValue.ROUTE_SOURCE_NORMAL, ConstantValue.CONNECTION_MODE_QC, new String());
            }

            @Override
            public void onAddDevice() {
                NooieDeviceHelper.trackDNEvent(true, EventDictionary.EVENT_ID_CLICK_ADD_DEVICE);
                AddCameraSelectActivity.toAddCameraSelectActivity(_mActivity);
            }
        });
    }

    private void setupDevicesAdapterListener(DevicesAdapter.OnItemClickListener listener) {
        if (mDeviceListAdapter != null) {
            mDeviceListAdapter.setIpcListener(listener);
        }
    }

    private void setupBleApDeviceView() {
        if (mDeviceListAdapter != null) {
            mDeviceListAdapter.setBleApIpcListener(new BleApDeviceListener() {
                @Override
                public void onItemClickListener(BleApDeviceEntity device) {
                    tryGotoConnectBleApDevice(device);
                }

                @Override
                public void onAccessClick(BleApDeviceEntity device) {
                    tryGotoConnectBleApDevice(device);
                }
            });
        }
    }

    public void setRouterDeviceView() {
        mRouterDeviceAdapter = new RouterDeviceAdapter();
        if (mRouterDeviceAdapter != null) {
            mRouterDeviceAdapter.setOnItemClickListener(new RouterDeviceAdapter.OnItemClickListener() {
                @Override
                public void onClickItem(ListDeviceItem device) {
                }

                @Override
                public void onAddDevice() {
                    NooieDeviceHelper.trackDNEvent(true, EventDictionary.EVENT_ID_CLICK_ADD_DEVICE);
                    AddCameraSelectActivity.toAddCameraSelectActivity(_mActivity);
                }
            });

            mRouterDeviceAdapter.setRouterDeviceClickListener(new RouterDeviceAdapter.OnRouterDeviceClickListener() {
                @Override
                public void onRouterItemClick(String device, String routerName, String routerMac,
                                              String isbind, String isOnline) {
                    mClickRouterInfo = new RouterInfo(routerName, routerMac, isbind, isOnline);
                    //getCheckPasswordResult();
                    dealwithRouterClickItem();
                }

                @Override
                public void onRouterLongItemClick(String device, String routerName, String routerMac) {
                    showRemoveRouterDialog(device, routerMac);
                }
            });
        }
    }

    private void tryGotoConnectBleApDevice(BleApDeviceEntity device) {
        if (!NooieDeviceHelper.checkBleApDeviceEntityValid(device)) {
            return;
        }
        if (mPresenter != null) {
            mPresenter.checkBeforeConnectBleDevice(device.getBleDeviceId(), device.getModel(), device.getSsid());
        }
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

    private void setupDvDeviceListView() {
        mDvDeviceListAdapter = new DvDeviceListAdapter();
        mDvDeviceListAdapter.setListener(new DvDeviceListListener() {
            @Override
            public void onItemClick(ApDeviceInfo deviceInfo) {
                gotoPlayForDvDevice(deviceInfo);
            }

            @Override
            public void onChangeSleep(ApDeviceInfo deviceInfo, boolean isOpen) {
                changeOpenStatusForDvDevice(deviceInfo, isOpen);
            }
        });
    }

    private void setupLockDevicesView() {
        mLockDeviceAdapter = new LockDeviceAdapter();
        setupLockDeviceListener();
    }

    private void setupLockDeviceListener() {
        setupLockDeviceAdapterListener(new LockDeviceListener() {
            @Override
            public void onItemClick(BleDeviceEntity bleDeviceEntity) {
                if (bleDeviceEntity != null) {
                    boolean isAdmin = bleDeviceEntity.getUserType() == ConstantValue.BLE_USER_TYPE_ADMIN;
                    LookDeviceActivity.toLookDeviceActivity(_mActivity, bleDeviceEntity.getDeviceId(), ConstantValue.ROUTE_SOURCE_NORMAL, bleDeviceEntity.getPhone(), bleDeviceEntity.getPassword(), isAdmin, bleDeviceEntity.getName(), bleDeviceEntity.getSec());
                }
            }
        });
    }

    private void setupLockDeviceAdapterListener(LockDeviceListener listener) {
        if (mDeviceListAdapter != null) {
            mDeviceListAdapter.setLockListener(listener);
        }

        if (mLockDeviceAdapter != null) {
            mLockDeviceAdapter.setListener(listener);
        }
    }

    private static final int DEVICE_LIST_TYPE_ALL = 1;
    private static final int DEVICE_LIST_TYPE_IPC = 2;
    private static final int DEVICE_LIST_TYPE_LOCK = 3;
    private static final int DEVICE_LIST_TYPE_DV = 4;
    private static final int DRAG_ROUTER_TYPE = 0x05;
    private int mCurrentDeviceListType = DEVICE_LIST_TYPE_ALL;

    private void showDeviceItem(int deviceListType) {
        if (checkActivityIsDestroy() || checkNull(ivLeft, rvDevice, mDeviceListAdapter, mLockDeviceAdapter, mRouterDeviceAdapter)) {
            return;
        }

        switch (deviceListType) {
            case DEVICE_LIST_TYPE_ALL:
                mCurrentDeviceListType = DEVICE_LIST_TYPE_ALL;
                ivLeft.setVisibility(View.GONE);
                rvDevice.setAdapter(mDeviceListAdapter);
                mIsShowApDirectModeTip = false;
                break;
            case DEVICE_LIST_TYPE_IPC:
                mCurrentDeviceListType = DEVICE_LIST_TYPE_IPC;
                ivLeft.setVisibility(View.VISIBLE);
                break;
            case DEVICE_LIST_TYPE_LOCK:
                mCurrentDeviceListType = DEVICE_LIST_TYPE_LOCK;
                ivLeft.setVisibility(View.VISIBLE);
                rvDevice.setAdapter(mLockDeviceAdapter);
                break;
            case DEVICE_LIST_TYPE_DV:
                mCurrentDeviceListType = DEVICE_LIST_TYPE_DV;
                ivLeft.setVisibility(View.GONE);
                rvDevice.setAdapter(mDvDeviceListAdapter);
                break;
            case DRAG_ROUTER_TYPE:
                mCurrentDeviceListType = DRAG_ROUTER_TYPE;
                ivLeft.setVisibility(View.GONE);
                rvDevice.setAdapter(mRouterDeviceAdapter);
                break;
        }
    }

    private void updateDeviceItemAdapter() {
        if (checkDeviceListIsSorting()) {
            return;
        }
        List<DeviceItem> deviceItems = new ArrayList<>();

        if (CollectionUtil.isNotEmpty(getAllIpcDeviceList())) {
            DeviceItem<ListDeviceItem> deviceItem = new DeviceItem<>();
            deviceItem.setProductType(ProductType.PRODUCT_IPC);
            deviceItem.setDatas(getAllIpcDeviceList());
            deviceItems.add(deviceItem);
        }

        if (CollectionUtil.isNotEmpty(getAllBleApDeviceList())) {
            DeviceItem<BleApDeviceEntity> deviceItem = new DeviceItem<>();
            deviceItem.setProductType(ProductType.PRODUCT_BLE_AP_IPC);
            deviceItem.setDatas(getAllBleApDeviceList());
            deviceItems.add(deviceItem);
        }

        if (mDeviceListAdapter != null) {
            mDeviceListAdapter.setData(deviceItems);
        }
    }

    private void gotoApDirectConnectionDevice(boolean isAuto, String deviceSsid, boolean isAccessLive) {
        NooieLog.d("-->> debug DeviceListFragment gotoApDirectConnectionDevice: 1000");
        if (checkActivityIsDestroy() || checkIsPause() || _mActivity == null || TextUtils.isEmpty(deviceSsid)) {
            if (isAuto) {
                ApHelper.getInstance().setApDirectConnectionCheckState(ApHelper.AP_DIRECT_CONNECTION_CHECK_NORMAL);
            }
            return;
        }
        NooieLog.d("-->> debug DeviceListFragment gotoApDirectConnectionDevice: 1001");
        if (!isAccessLive) {
            if (isAuto) {
                ApHelper.getInstance().setApDirectConnectionCheckState(ApHelper.AP_DIRECT_CONNECTION_CHECK_FINISH);
            }
            return;
        }
        DeviceConnectionHelper.getInstance().removeConnectionsForAp();
        NooiePlayActivity.startPlayActivity(_mActivity, ConstantValue.VICTURE_AP_DIRECT_DEVICE_ID, IpcType.MC120.getType(), ConstantValue.NOOIE_PLAYBACK_TYPE_LIVE, ConstantValue.NOOIE_PLAYBACK_SOURCE_TYPE_NORMAL, 0, ConstantValue.ROUTE_SOURCE_ADD_DEVICE, ConstantValue.CONNECTION_MODE_AP_DIRECT, deviceSsid);
        if (isAuto) {
            ApHelper.getInstance().setApDirectConnectionCheckState(ApHelper.AP_DIRECT_CONNECTION_CHECK_FINISH);
        }
        NooieLog.d("-->> debug DeviceListFragment gotoApDirectConnectionDevice: 1002");
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

    private void changeOpenStatusForDvDevice(ApDeviceInfo deviceInfo, boolean open) {
        if (deviceInfo != null && deviceInfo.getBindDevice() != null && !TextUtils.isEmpty(deviceInfo.getBindDevice().getUuid()) && mPresenter != null) {
            ((NooieBaseSupportActivity) _mActivity).showLoading(true);
            int openStatus = open ? ApiConstant.OPEN_STATUS_ON : ApiConstant.OPEN_STATUS_OFF;
            mPresenter.updateApDeviceOpenStatus(deviceInfo.getDeviceSsid(), deviceInfo.getBindDevice().getUuid(), openStatus);
        }
    }

    private boolean checkDeviceListTypeValid(int type) {
        return type != DEVICE_LIST_TYPE_ALL || type != DEVICE_LIST_TYPE_IPC || type != DEVICE_LIST_TYPE_LOCK || type != DEVICE_LIST_TYPE_DV;
    }

    private void showDeleteDeviceDialog(ListDeviceItem device) {
        hideDeleteDeviceDialog();
        mDeleteDeviceDialog = DialogUtils.showConfirmWithSubMsgDialog(_mActivity, getString(R.string.camera_settings_remove_camera_confirm),
                String.format(getString(R.string.camera_settings_remove_info_confirm), (device != null && device.getBindDevice() != null ? device.getBindDevice().getName() : "")),
                R.string.camera_settings_no_remove, R.string.camera_settings_remove, new DialogUtils.OnClickConfirmButtonListener() {
                    @Override
                    public void onClickRight() {
                        if (device != null && mPresenter != null) {
                            mPresenter.removeDevice(mUserAccount, device.getDeviceId());
                        }
                    }

                    @Override
                    public void onClickLeft() {
                    }
                });
    }

    private void hideDeleteDeviceDialog() {
        if (mDeleteDeviceDialog != null) {
            mDeleteDeviceDialog.dismiss();
            mDeleteDeviceDialog = null;
        }
    }

    private void showFindApDirectConnectionDialog(String deviceSsid) {
        if (checkActivityIsDestroy() || checkIsPause() || _mActivity == null || TextUtils.isEmpty(deviceSsid)) {
            return;
        }
        hideFindApDirectConnectionDialog();
        if (mFindApDirectConnectionDeviceDialog == null) {
            mFindApDirectConnectionDeviceDialog = DialogUtils.showConfirmWithSubMsgDialog(_mActivity, getString(R.string.dialog_tip_title), getString(R.string.switch_connection_mode_found_ap_direct_connection), R.string.cancel, R.string.confirm_upper, new DialogUtils.OnClickConfirmButtonListener() {
                @Override
                public void onClickRight() {
                    if (checkActivityIsDestroy() || checkIsPause()) {
                        return;
                    }
                    if (mPresenter != null) {
                        NooieLog.d("-->> debug DeviceListFragment showFindApDirectConnectionDialog onClickRight: 1000");
                        ApHelper.getInstance().setApDirectConnectionCheckState(ApHelper.AP_DIRECT_CONNECTION_CHECK_STARTING);
                        mPresenter.startAPDirectConnect(deviceSsid, true);
                    }
                }

                @Override
                public void onClickLeft() {
                    if (checkActivityIsDestroy() || checkIsPause()) {
                        return;
                    }
                    if (mPresenter != null) {
                        NooieLog.d("-->> debug DeviceListFragment showFindApDirectConnectionDialog onClickRight: 1000");
                        ApHelper.getInstance().setApDirectConnectionCheckState(ApHelper.AP_DIRECT_CONNECTION_CHECK_STARTING);
                        mPresenter.startAPDirectConnect(deviceSsid, false);
                    }
                }
            });
        }
        mFindApDirectConnectionDeviceDialog.show();
    }

    private void hideFindApDirectConnectionDialog() {
        if (mFindApDirectConnectionDeviceDialog != null) {
            mFindApDirectConnectionDeviceDialog.dismiss();
            mFindApDirectConnectionDeviceDialog = null;
        }
    }

    private void displayNetworkWeakTip(boolean show) {
        if (checkNull(nwtDeviceList)) {
            return;
        }
        if (checkNetworkWeakTipClose()) {
            show = false;
        }
        nwtDeviceList.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void saveNetworkWeakTipClose() {
        if (ivCloseIcon != null) {
            ivCloseIcon.setTag(NETWORK_WEAK_TIP_CLOSE_STATE);
        }
    }

    private boolean checkNetworkWeakTipClose() {
        return ivCloseIcon != null && ivCloseIcon.getTag() != null && (Integer) ivCloseIcon.getTag() == NETWORK_WEAK_TIP_CLOSE_STATE;
    }

    private void registerDeviceChangeReceiver() {
        if (mDeviceChangeBroadcastReceiver == null) {
            mDeviceChangeBroadcastReceiver = new DeviceChangeBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter(ConstantValue.BROADCAST_KEY_REMOVE_CAMERA);
            intentFilter.addAction(ConstantValue.BROADCAST_KEY_UPDATE_CAMERA);
            NotificationUtil.registerLocalReceiver(NooieApplication.mCtx, mDeviceChangeBroadcastReceiver, intentFilter);
        }
    }

    private void unRegisterDeviceChangeReceiver() {
        if (mDeviceChangeBroadcastReceiver != null) {
            NotificationUtil.unregisterLocalReceiver(NooieApplication.mCtx, mDeviceChangeBroadcastReceiver);
            mDeviceChangeBroadcastReceiver = null;
        }
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

    private void enableDeviceListViewRefresh(boolean enable) {
        if (checkActivityIsDestroy() || checkNull(swipeToLoadLayout)) {
            return;
        }
        swipeToLoadLayout.setRefreshEnabled(enable);
    }

    private void enableDeviceListViewLoadMore(boolean enable) {
        if (checkActivityIsDestroy() || checkNull(swipeToLoadLayout)) {
            return;
        }
        swipeToLoadLayout.setLoadMoreEnabled(enable);
    }

    private void enableDeviceListViewRefreshOrLoadMore(boolean enable) {
        if (checkActivityIsDestroy() || checkNull(swipeToLoadLayout)) {
            return;
        }
        swipeToLoadLayout.setRefreshEnabled(enable);
        swipeToLoadLayout.setLoadMoreEnabled(enable);
    }

    private void setFirebaseUserProperty() {
        if (!MyAccountHelper.getInstance().isLogin()) {
            return;
        }
        int numberOfIpc = 0;
        int numberOfGuestIpc = 0;
        List<ListDeviceItem> deviceItems = DeviceListCache.getInstance().getAllCache();
        for (ListDeviceItem deviceItem : CollectionUtil.safeFor(deviceItems)) {
            if (deviceItem != null && deviceItem.getBindDevice() != null && deviceItem.getBindDevice().getBind_type() == ApiConstant.BIND_TYPE_OWNER) {
                numberOfIpc++;
            }
        }
        numberOfGuestIpc = CollectionUtil.size(deviceItems) - numberOfIpc;
        Map<String, String> userProperty = new HashMap<>();
        userProperty.put(FirebaseConstant.USER_PROPERTY_NUMBER_OF_IPC, String.valueOf(numberOfIpc));
        userProperty.put(FirebaseConstant.USER_PROPERTY_NUMBER_OF_GUEST_IPC, String.valueOf(numberOfGuestIpc));
        FirebaseAnalyticsManager.getInstance().setUseProperty(userProperty);
    }

    class DeviceChangeBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mPresenter != null) {
                startRefresh();
            }
        }
    }

    private class DeviceApHelperListener implements ApHelper.ApHelperListener {

        @Override
        public void onApHeartBeatResponse(int code) {
            if (checkActivityIsDestroy() || checkIsPause() || checkNull(mDeviceListAdapter, mDvDeviceListAdapter)) {
                return;
            }
            NooieLog.d("-->> debug DeviceApHelperListener onApHeartBeatResponse: code=" + code + " checkApDirectConnectionIsError=" + ApHelper.getInstance().checkApDirectConnectionIsError() + " currentConnectionMode=" + ApHelper.getInstance().getCurrentConnectionMode());
            if (code == Constant.OK && ApHelper.getInstance().checkBleApDeviceConnectingExist()) {
                boolean isNeedToLoadDevice = mCurrentDeviceListType != DEVICE_LIST_TYPE_DV || CollectionUtil.isEmpty(mDvDeviceListAdapter.getData());
                if (isNeedToLoadDevice && mPresenter != null) {
                    mPresenter.checkBleApDeviceConnecting();
                }
            } else if (code == Constant.ERROR && ApHelper.getInstance().checkApDirectConnectionIsError() && mPresenter != null) {
                mPresenter.stopAPDirectConnection(null);
                stopRefresh();
                stopLoadMore();
                cancelDeviceSort();
                enableDeviceListViewRefreshOrLoadMore(true);
                showDeviceItem(DEVICE_LIST_TYPE_ALL);
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

    private void getBindRouterDevice() {
        if (mGatewayPresenter != null) {
            mGatewayPresenter.getGatewayDevices(mUserAccount, mUid);
        }
    }

    private void getRouterInitCfg() {
        boolean isLoadRouterDevice = MyAccountHelper.getInstance().isLogin() && !(mDeviceListAdapter != null && mDeviceListAdapter.isItemDragEnable());
        if (!isLoadRouterDevice) {
            return;
        }
        // 本地通信
        GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
        try {
            routerDataFromCloud.getInitCfg();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 与后台交互测试
       /* GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
        try {
            routerDataFromCloud.getOnlineInitCfg();
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
    }

    private void dealwithRouterClickItem() {
        // wifi直连
        if (!"".equals(routerWifiMac) && "0".equals(mClickRouterInfo.getIsbind())) {
            if (mClickRouterInfo.getIsOnline().equals("1")) {
                RouterDetalisActivity.toRouterDetalisActivity(_mActivity, mClickRouterInfo.getRouterName(), mClickRouterInfo.getRouterMac(), mClickRouterInfo.getIsbind());
            } else if (mClickRouterInfo.getIsOnline().equals("0")) {
                RouterOfflineHelpActivity.toRouterOfflineHelpActivity(getActivity());
            } else {
                showConnectionWifiDialog();
            }
        } else if ("".equals(routerWifiMac)) {
            showConnectionWifiDialog();
        }
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                updataRouterStatus();
            } else if (msg.what == 2) {
                if (mPresenter != null) {
                    mPresenter.loadRouterDevices(mUserAccount, routerWifiMac);
                }
            }
        }
    };

    @Override
    public void routerReturnInfo(String info, String topicurlString) {
        try {
            Message message = new Message();
            if ("error".equals(info) && "getInitCfg".equals(topicurlString)) {
                message.what = 1;
            } else if (!"error".equals(info) && "getInitCfg".equals(topicurlString)) {
                routerWifiMac = new JSONObject(info).getString("mac");
                message.what = 2;
            }
            handler.sendMessage(message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 提示连接wifi失败去连接wifi
     */
    private WiFiDialog wiFiDialog;
    private void showConnectionWifiDialog() {
        if (null == wiFiDialog) {
            wiFiDialog = new WiFiDialog(getActivity(), true);
        }
        if (!wiFiDialog.isShowing()) {
            wiFiDialog.show();
        }
    }

    private Dialog mShowNoConnectRouterDialog;
    private void showNoConnectRouterDialog() {
        hideNoConnectRouterDialog();
        mShowNoConnectRouterDialog = DialogUtils.noConnectRouterDialog(getActivity(), new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                //startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS),10086);
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

    private void hideNoConnectRouterDialog() {
        if (mShowNoConnectRouterDialog != null) {
            mShowNoConnectRouterDialog.dismiss();
            mShowNoConnectRouterDialog = null;
        }
    }

    private Dialog mShowRemoveRouterDialog;

    private void showRemoveRouterDialog(String device, String routerMac) {
        hideRemoveRouterDialog();
        mShowRemoveRouterDialog = DialogUtils.removeRouterDialog(getActivity(), new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                /*if (mRouterList.size() == 1) {
                    showDeviceItem(DEVICE_LIST_TYPE_ALL);
                }*/
                if (mPresenter != null) {
                    mPresenter.deleteRouterDevice(routerMac);
                }
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
    public void setPresenter(@NonNull GatewaySettingsContract.Presenter presenter) {
        mGatewayPresenter = presenter;
    }

    @Override
    public void onGetGatewayDevicesResult(String result, List<GatewayDevice> gatewayDevices) {
        // gatewayDevices设备列表
        if (!CollectionUtil.isNotEmpty(gatewayDevices)) {
            return;
        }
        mRouterBindList.clear();
        for (GatewayDevice gatewayDevice : gatewayDevices) {
            NooieLog.d("------> GetGatewayDevicesResult Online: " + gatewayDevice.getOnline());
            ListDeviceItem listDeviceItem = new ListDeviceItem(gatewayDevice.getName(), gatewayDevice.getMac(), 6, 1, "1");
            mRouterBindList.add(listDeviceItem);
        }
    }

    @Override
    public void onDeleteSubDeviceResult(String result, String deviceId, String pDeviceId) {
    }

    private void refreshPopupMenu() {
        if (currentItem == 0) {

            if (checkActivityIsDestroy() || checkNull(mDeviceListMenu, mDeviceListAdapter)) {
                return;
            }
            boolean isHideSortMenu = CollectionUtil.isEmpty(mDeviceListAdapter.getData()) || !(mCurrentDeviceListType == DEVICE_LIST_TYPE_ALL && mDeviceListAdapter.isDeviceSortEnable());
            List<Integer> menuIdList = new ArrayList<>();
            if (isHideSortMenu) {
                menuIdList.add(RelativePopupMenu.MENU_FIRST);
            }
            //mDeviceListMenu.addMenuList(getMenuItems(menuIdList));
            mDeviceListMenu.refreshMenuView(getMenuItems(menuIdList));
        } else if (currentItem == 1) { // 当选择为
            List<Integer> menuIdList = new ArrayList<>();
            /*if (mRouterList.size() > 0) {
                menuIdList.add(RelativePopupMenu.MENU_FIRST);
                menuIdList.add(RelativePopupMenu.MENU_SECOND);
            } else {
                menuIdList.add(RelativePopupMenu.MENU_FIRST);
            }*/
            // 路由器不排序
            menuIdList.add(RelativePopupMenu.MENU_FIRST);
            mDeviceListMenu.refreshMenuView(getMenuItems(menuIdList));
        }
    }

    /**
     * 没有连接路由器wifi,显示设备未连接
     */
    private void updataRouterStatus() {
        routerWifiMac = "";
        if (mPresenter != null) {
            // 获取本地所有路由器设备
            mPresenter.loadRouterDevices(mUserAccount, "");
        }
    }

    private void showApDirectModeTip() {
        NooieLog.d("-->> debug DeviceListFragment showApDirectModeTip mIsShowApDirectModeTip=" + mIsShowApDirectModeTip);
        boolean isNoneOtherDevice = DeviceListCache.getInstance().isEmpty() && BleApDeviceInfoCache.getInstance().cacheSize() <= 1;
        if (checkActivityIsDestroy() || mIsShowApDirectModeTip || isNoneOtherDevice) {
            return;
        }
        mIsShowApDirectModeTip = true;
        ToastUtil.showToast(_mActivity, getString(R.string.home_ap_direct_mode_tip));
    }

    private boolean checkDeviceListIsSorting() {
        return mDeviceListAdapter != null && mDeviceListAdapter.isItemDragEnable();
    }

    private void refreshDeviceSortEdit() {
        if (checkActivityIsDestroy() || checkNull(mDeviceListAdapter, btnDeviceSortEdit) || checkDeviceListIsSorting()) {
            return;
        }
        if (currentItem == 0) {
            boolean isHideSortMenu = CollectionUtil.isEmpty(mDeviceListAdapter.getData()) || !(mCurrentDeviceListType == DEVICE_LIST_TYPE_ALL && mDeviceListAdapter.isDeviceSortEnable());
            btnDeviceSortEdit.setVisibility(isHideSortMenu ? View.GONE : View.VISIBLE);
        } else if (currentItem == 1) {
            // 路由器不排序
            btnDeviceSortEdit.setVisibility(View.GONE);
        }
    }

    private void displayDeviceTypeNavView(boolean show) {
        //show = false;//不支持路由器时，打开来屏蔽路由器入口
        int visible = show ? View.VISIBLE : View.GONE;
        deviceType.setVisibility(visible);
        btnDeviceSortEdit.setVisibility(visible);
        vDeviceTypeDivider.setVisibility(visible);
    }

    /**
     * 适配时区同步接口和检测固件版本接口冲突问题
     * 描述：设置好时区后,如果再发送检测固件版本接口（原先设置好的时区会被打乱）
     * 设置该接口可以避免此问题（这个接口本来是用来绑定的，但由于路由器第一版不上绑定,所以这边就没有设置user_uuid参数）
     */
    private void setBindRouter() {
        String regin = "1";
        if ("CN".equals(CountryUtil.getCountryKey(NooieApplication.mCtx))) {
            regin = "1";
        } else if ("US".equals(CountryUtil.getCountryKey(NooieApplication.mCtx))) {
            regin = "0";
        } else if ("EU".equals(CountryUtil.getCountryKey(NooieApplication.mCtx))) {
            regin = "2";
        }
        GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
        try {
            routerDataFromCloud.setRouterBind("", timeZoneConversion(), regin);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 路由器有关时区设定特定格式（吉翁规定为该格式）
     * @return
     */
    private String timeZoneConversion() {
        String timeZone = CountryUtil.getTimeZone();
        StringBuilder stringBuilder = new StringBuilder();
        if (timeZone.contains("+")) {
            stringBuilder.append("+");
        } else {
            stringBuilder.append("-");
        }

        if (Integer.parseInt(timeZone.substring(1,2)) == 0) {
            stringBuilder.append(timeZone.substring(2,3));
        } else {
            stringBuilder.append(timeZone.substring(1,3));
        }
        return stringBuilder.toString();
    }
}
