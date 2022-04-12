package com.afar.osaio.smart.electrician.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.base.NooieBaseMainFragment;
import com.afar.osaio.message.activity.MessageActivity;
import com.afar.osaio.smart.electrician.activity.AddDeviceSelectLinkageActivity;
import com.afar.osaio.smart.electrician.activity.CreateGroupActivity;
import com.afar.osaio.smart.electrician.activity.HomeListActivity;
import com.afar.osaio.smart.electrician.adapter.BannerHolderView;
import com.afar.osaio.smart.electrician.adapter.DeviceTypeAdapter;
import com.afar.osaio.smart.electrician.adapter.HomeListPopAdapter;
import com.afar.osaio.smart.electrician.adapter.ImageHolderView;
import com.afar.osaio.smart.electrician.eventbus.HomeEvent;
import com.afar.osaio.smart.electrician.eventbus.HomeFamilyChangeEvent;
import com.afar.osaio.smart.electrician.eventbus.ListStyleSwitchEvent;
import com.afar.osaio.smart.electrician.eventbus.WeatherEvent;
import com.afar.osaio.smart.electrician.manager.DeviceManager;
import com.afar.osaio.smart.electrician.manager.FamilyManager;
import com.afar.osaio.smart.electrician.presenter.HomePresenter;
import com.afar.osaio.smart.electrician.presenter.IHomePresenter;
import com.afar.osaio.smart.electrician.smartScene.activity.TeckinSmartSceneActivity;
import com.afar.osaio.smart.electrician.util.DialogUtil;
import com.afar.osaio.smart.electrician.util.ErrorHandleUtil;
import com.afar.osaio.smart.electrician.view.IHomeView;
import com.afar.osaio.smart.event.HomeActionEvent;
import com.afar.osaio.smart.event.MsgCountUpdateEvent;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.preference.GlobalPrefs;
import com.afar.osaio.widget.RelativePopupMenu;
import com.afar.osaio.widget.bean.RelativePopMenuItem;
import com.aspsine.swipetoloadlayout.OnRefreshListener;
import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.bigkoo.convenientbanner.holder.Holder;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.entity.BannerResult;
import com.tuya.smart.api.service.MicroServiceManager;
import com.tuya.smart.commonbiz.bizbundle.family.api.AbsBizBundleFamilyService;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.bean.WeatherBean;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.GroupBean;
import com.tuya.smart.sdk.enums.TempUnitEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import me.yokeyword.eventbusactivityscope.EventBusActivityScope;

import static com.afar.osaio.smart.electrician.adapter.MixNewDeviceAdapter.VIEW_GRID;
import static com.afar.osaio.smart.electrician.adapter.MixNewDeviceAdapter.VIEW_LINEAR;


public class SmartHomeFragment extends NooieBaseMainFragment implements IHomeView, OnRefreshListener, DeviceManager.IHomeBeanCallback, DeviceManager.IHomeDataListener,
        DeviceManager.IDeviceListenerCallBack, DeviceManager.IGroupListenerCallBack, SwipeRefreshLayout.OnRefreshListener, DeviceManager.IHomeRefreshListenerCallBack {

    private final int TAG_GRID = 0;
    private final int TAG_LINEAR = 1;

    private final static int HOME_LIST_CLOSE = 0;
    private final static int HOME_LIST_OPEN = 1;
    private static final int MIN_CLICK_DELAY_TIME = 1000;
    private static long lastClickTime = 0;

    @BindView(R.id.tvTitleLeft)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.ivNews)
    ImageView ivNews;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.ivSwitchSortStyle)
    ImageView ivSwitchSortStyle;
    @BindView(R.id.containerTitle)
    View containerTitle;
    @BindView(R.id.containerTitleLeft)
    View containerTitleLeft;
    @BindView(R.id.tvWeather)
    TextView tvWeather;
    @BindView(R.id.tvTemp)
    TextView tvTemp;
    @BindView(R.id.tvTempUnit)
    TextView tvTempUnit;


    @BindView(R.id.ivWeather)
    ImageView ivWeather;
    @BindView(R.id.flHomeList)
    FrameLayout flHomeList;
    @BindView(R.id.rcvHomeList)
    RecyclerView rcvHomeList;
    @BindView(R.id.srlRefresh)
    SwipeRefreshLayout srlRefresh;
    @BindView(R.id.tlSwitchType)
    TabLayout tlSwitchType;
    @BindView(R.id.vpSwitchType)
    ViewPager vpSwitchType;
    @BindView(R.id.llad)
    ConvenientBanner cbAdvertise;
    @BindView(R.id.ivCancel)
    ImageView ivCancel;
    @BindView(R.id.ivPersonMsgPoint)
    ImageView ivPersonMsgPoint;
    @BindView(R.id.appbar_layout)
    AppBarLayout appBarLayout;
    private List<String> mDeviceType;
    private List<Fragment> mDeviceTypeFragment;
    private DeviceFragment mDeviceFragment;
    private GroupFragment mGroupFragment;
    private DeviceTypeAdapter mDeviceTypeAdatper;

    private Unbinder unbinder;
    private IHomePresenter homePresenter;
    private HomeListPopAdapter mHomesAdapter;
    RelativePopupMenu mDeviceListMenu;

    private volatile static SmartHomeFragment mHomeFragment;

    private View mContentView;

    public static Fragment newInstance() {
        if (mHomeFragment == null) {
            synchronized (SmartHomeFragment.class) {
                if (mHomeFragment == null) {
                    mHomeFragment = new SmartHomeFragment();
                }
            }
        }
        return mHomeFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_home_new, container, false);
        unbinder = ButterKnife.bind(this, mContentView);
        initView();
        initData();
        return mContentView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        EventBusActivityScope.getDefault(_mActivity).unregister(this);
        DeviceManager.getInstance().release();
        if (homePresenter != null) {
            homePresenter.release();
        }
    }

    private void initView() {
        EventBusActivityScope.getDefault(_mActivity).register(this);
        containerTitle.setVisibility(View.GONE);
        containerTitleLeft.setVisibility(View.VISIBLE);
        ivRight.setImageResource(R.drawable.nav_more_state_list);
        ivNews.setImageResource(R.drawable.ic_nav_news);
        ivNews.setVisibility(View.GONE);
        tvTitle.setTag(HOME_LIST_CLOSE);
        ivSwitchSortStyle.setTag(TAG_GRID);
        ivLeft.setVisibility(View.INVISIBLE);
        flHomeList.setVisibility(View.GONE);
        setupDeviceListMenu();
        setupHomeListView();
        setRefresh();
    }

    private void setRefresh() {
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, final int verticalOffset) {
                srlRefresh.setEnabled(verticalOffset >= 0);//页面滑动到顶部，才可以下拉刷新
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (cbAdvertise != null) {
            cbAdvertise.startTurning();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (cbAdvertise != null) {
            cbAdvertise.stopTurning();
        }
    }

    private void initData() {
        homePresenter = new HomePresenter(this);
        //homePresenter.loadBanner();
        tvTitle.setTag(HOME_LIST_CLOSE);
        DeviceManager.getInstance().setHomeFragChangedListener(this);
        DeviceManager.getInstance().setAllHomeBeanCallback(this);
        DeviceManager.getInstance().setAllHomeRefreshListenerCallBack(this);
        srlRefresh.setOnRefreshListener(this);
        srlRefresh.setColorSchemeColors(getResources().getColor(R.color.background));
        setUpDeviceGroupFragment();
    }

    private void setUpDeviceGroupFragment() {
        mDeviceType = new ArrayList<>();
        mDeviceTypeFragment = new ArrayList<>();
        mDeviceType.add(getResources().getString(R.string.device));
        mDeviceType.add(getResources().getString(R.string.group));
        mDeviceTypeFragment.add(mDeviceFragment = new DeviceFragment());
        mDeviceTypeFragment.add(mGroupFragment = new GroupFragment());

        mDeviceTypeAdatper = new DeviceTypeAdapter(getChildFragmentManager(), mDeviceType, mDeviceTypeFragment);

        vpSwitchType.setAdapter(mDeviceTypeAdatper);
        vpSwitchType.setOffscreenPageLimit(mDeviceTypeFragment.size());
        tlSwitchType.setupWithViewPager(vpSwitchType);
    }

    private void setupHomeListView() {
        flHomeList.setVisibility(View.GONE);
        mHomesAdapter = new HomeListPopAdapter();
        mHomesAdapter.setListener(new HomeListPopAdapter.HomeListListener() {
            @Override
            public void onItemClick(HomeBean homeBean) {
                toggleHomeList();
                if (homeBean != null) {
                    AbsBizBundleFamilyService service = MicroServiceManager.getInstance().findServiceByInterface(AbsBizBundleFamilyService.class.getName());
                    //设置为当前家庭的homeId
                    service.setCurrentHomeId(homeBean.getHomeId());
                    EventBus.getDefault().post(new HomeFamilyChangeEvent(homeBean.getHomeId()));
                } else {
                    //NameHomeActivity.toNameHomeActivity(getActivity(), ConstantValue.REQUEST_CODE_ADD_HOME, 0, null);
                }
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rcvHomeList.setLayoutManager(layoutManager);
        rcvHomeList.setAdapter(mHomesAdapter);
    }

    @OnClick({R.id.tvTitleLeft, R.id.flHomeList, R.id.ivRight, R.id.ivSwitchSortStyle, R.id.clWeather, R.id.ivNews, R.id.ivCancel})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.flHomeList:
            case R.id.tvTitleLeft: {
                toggleHomeList();
                break;
            }
            case R.id.ivRight: {
                long curClickTime = System.currentTimeMillis();
                if ((curClickTime - lastClickTime) >= MIN_CLICK_DELAY_TIME) {
                    // 超过点击间隔后再将lastClickTime重置为当前点击时间
                    lastClickTime = curClickTime;
                    //showPopMenu();
                    mDeviceListMenu.showAsDropDown(ivRight, -DisplayUtil.dpToPx(NooieApplication.mCtx, 120), -DisplayUtil.dpToPx(NooieApplication.mCtx, 5));
                }
                break;
            }
            case R.id.ivSwitchSortStyle: {
                NooieLog.e("---------------ivSort tag " + ivSwitchSortStyle.getTag());
                if ((int) ivSwitchSortStyle.getTag() == TAG_GRID) {
                    ivSwitchSortStyle.setImageResource(R.drawable.ic_list_matrix_off);
                    ivSwitchSortStyle.setTag(TAG_LINEAR);
                    EventBus.getDefault().post(new ListStyleSwitchEvent(VIEW_LINEAR));
                } else if ((int) ivSwitchSortStyle.getTag() == TAG_LINEAR) {
                    ivSwitchSortStyle.setTag(TAG_GRID);
                    ivSwitchSortStyle.setImageResource(R.drawable.ic_list_matrix_on);
                    EventBus.getDefault().post(new ListStyleSwitchEvent(VIEW_GRID));
                }
                break;
            }
            case R.id.clWeather: {
                EventBus.getDefault().post(new HomeActionEvent(HomeActionEvent.HOME_ACTION_LOCATION_PERMISSION));
                break;
            }
            case R.id.ivNews: {
                MessageActivity.toMessageActivity(_mActivity);
                break;
            }
            case R.id.ivCancel: {
                cbAdvertise.setVisibility(View.GONE);
                ivCancel.setVisibility(View.GONE);
                break;
            }
        }
    }

    public void toggleHomeList() {
        if ((int) tvTitle.getTag() == HOME_LIST_OPEN) {
            tvTitle.setTag(HOME_LIST_CLOSE);
            setToggleDown();
            flHomeList.setVisibility(View.GONE);
            ivRight.setEnabled(true);
        } else {
            tvTitle.setTag(HOME_LIST_OPEN);
            setToggleUp();
            homePresenter.getHomeList();
            flHomeList.setVisibility(View.VISIBLE);
            ivRight.setEnabled(false);
        }
    }

    private void setToggleDown() {
        Drawable drawable = getResources().getDrawable(R.drawable.ic_arrow_down);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        tvTitle.setCompoundDrawables(null, null, drawable, null);
    }

    private void setToggleUp() {
        Drawable drawable = getResources().getDrawable(R.drawable.ic_arrow_up_bg);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        tvTitle.setCompoundDrawables(null, null, drawable, null);
    }

    private void setupDeviceListMenu() {
        List<RelativePopMenuItem> menuItems = new ArrayList<>();
       /* RelativePopMenuItem addDeviceItem = new RelativePopMenuItem();
        addDeviceItem.setId(RelativePopupMenu.MENU_FIRST);
        addDeviceItem.setIcon(R.drawable.ic_home_add);
        addDeviceItem.setTitle(getString(R.string.add_device));
        menuItems.add(addDeviceItem);*/
        RelativePopMenuItem createGroupItem = new RelativePopMenuItem();
        createGroupItem.setId(RelativePopupMenu.MENU_SECOND);
        createGroupItem.setIcon(R.drawable.ic_home_group);
        createGroupItem.setTitle(getString(R.string.create_group));
        menuItems.add(createGroupItem);
        RelativePopMenuItem familyManageItem = new RelativePopMenuItem();
        familyManageItem.setId(RelativePopupMenu.MENU_THIRD);
        familyManageItem.setIcon(R.drawable.ic_home_family);
        familyManageItem.setTitle(getString(R.string.manage_home));
        menuItems.add(familyManageItem);
        RelativePopMenuItem autoManageItem = new RelativePopMenuItem();
        autoManageItem.setId(RelativePopupMenu.MENU_FOURTH);
        autoManageItem.setIcon(R.drawable.ic_home_automatic);
        autoManageItem.setTitle(getString(R.string.auto_control));
        menuItems.add(autoManageItem);
        mDeviceListMenu = new RelativePopupMenu(getActivity());
        mDeviceListMenu.setHeight(RecyclerView.LayoutParams.WRAP_CONTENT)
                .setWidth(DisplayUtil.dpToPx(NooieApplication.mCtx, 175))
                .showIcon(true)
                .showTriangle(true)
                .dimBackground(false)
                .needAnimationStyle(true)
                .setAnimationStyle(R.style.TRM_ANIM_STYLE)
                .addMenuList(menuItems)
                .setOnMenuItemClickListener(new RelativePopupMenu.OnMenuItemClickListener() {
                    @Override
                    public void onMenuItemClick(int position) {
                        if (mDeviceListMenu != null) {
                            mDeviceListMenu.dismiss();
                        }
                        switch (position) {
                            case RelativePopupMenu.MENU_FIRST:
                                AddDeviceSelectLinkageActivity.toAddDeviceSelectLinkageActivity(getActivity(), false);
                                break;
                            case RelativePopupMenu.MENU_SECOND:
                                doCreateGroup();
                                break;
                            case RelativePopupMenu.MENU_THIRD:
                                HomeListActivity.toHomeListActivity(getActivity());
                                break;
                            case RelativePopupMenu.MENU_FOURTH:
                                TeckinSmartSceneActivity.toTeckinSmartSceneActivity(getActivity());
                                break;
                        }
                    }
                });
    }

    private void doCreateGroup() {
        if (FamilyManager.getInstance().getCurrentHomeId() == FamilyManager.DEFAULT_HOME_ID) {
            return;
        }
        TuyaHomeSdk.newHomeInstance(FamilyManager.getInstance().getCurrentHomeId()).getHomeDetail(new ITuyaHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean homeBean) {
                if (homeBean != null && homeBean.getDeviceList().size() > 0) {
                    CreateGroupActivity.toCreateCroupActivity(getActivity());
                } else {
                    DialogUtil.showInformationDialog(getActivity(), NooieApplication.mCtx.getString(R.string.create_group), getActivity().getString(R.string.create_group_middle_tip));
                }
            }

            @Override
            public void onError(String code, String msg) {
                ErrorHandleUtil.toastTuyaError(getActivity(), code);
                NooieLog.e("-------- onError code == " + code + "  msg == " + msg);
            }
        });
    }

    private void showHomeList(List<HomeBean> homes) {
        if (homes != null && homes.size() > 0) {
            tvTitle.setTag(HOME_LIST_CLOSE);
            setToggleDown();
            mHomesAdapter.setData(homes);
        }
    }

    @Override
    public void onRefresh() {
        EventBus.getDefault().post(new HomeEvent(ConstantValue.HOME_ONREFRESH));
    }

    private void stopRefresh() {
        if (srlRefresh != null && srlRefresh.isRefreshing()) {
            srlRefresh.setRefreshing(false);
        }
    }

    @Override
    public void callbackHomeBean(HomeBean homeBean) {
        // mMixDeviceAdapter.setData(DeviceHelper.convertDeviceBean(homeBean));
        //mMixGroupAdapter.setData(DeviceHelper.convertGroupDeviceBean(homeBean));
    }

    // ------------------ 对单个设备的监听 --------------
    @Override
    public void onDpUpdate(String devId, String dpStr) {

    }

    @Override
    public void onRemoved(String devId) {
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

    //-------------------- 对群组监听 ----------------------------
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
        if (getActivity() == null) {
            return;
        }
        if (homes != null) {
            showHomeList(homes);
            // NooieLog.e("---------------------> homeName size "+homes.size()+"  home id ");
            for (HomeBean home : homes) {
                NooieLog.e("---------------------> homeName size " + "  home id " + home.getHomeId());
                if (home.getHomeId() == FamilyManager.getInstance().getCurrentHomeId()) {
                    NooieLog.e("---------------------> homeName " + home.getName());
                    tvTitle.setText(home.getName());
                    return;
                }
            }
        }
    }

    /**
     * 接收消息未读数
     *
     * @param event
     */
    @Subscribe
    public void onMsgCountUpdate(MsgCountUpdateEvent event) {
        if (checkActivityIsDestroy() || checkNull(event)) {
            return;
        }
        //displayMsgUnreadPoint(event.count > 0);
    }

    private void displayMsgUnreadPoint(boolean show) {
        if (checkActivityIsDestroy() || checkNull(ivPersonMsgPoint)) {
            return;
        }
        ivPersonMsgPoint.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void notifyLoadHomeDetailSuccess(HomeBean homeBean) {
        if (getActivity() == null) {
            return;
        }
        if (homeBean != null) {
            tvTitle.setText(homeBean.getName());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWeatherEvent(WeatherEvent event) {
        NooieLog.e("SmartHomeFragment weatherbean " + event.toString());
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

    }

    @Override
    public void notifyLoadHomeListSuccess(String code, List<HomeBean> list) {
        if (code.equals(ConstantValue.SUCCESS) && list != null) {
            mHomesAdapter.setData(list);
        }
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
    public void onLoadBannerSuccess(List<BannerResult.BannerInfo> bannerlist) {
        List<String> urlList = new ArrayList<>();
        for (BannerResult.BannerInfo bannerInfo : bannerlist) {
            NooieLog.e("--------onLoadBannerSuccess url " + bannerInfo.getImg_url());
            if (bannerInfo.getImg_url() != null) {
                urlList.add(bannerInfo.getImg_url());
            }
        }
        cbAdvertise.setPages(new CBViewHolderCreator() {
            @Override
            public Holder createHolder(View itemView) {
                return new BannerHolderView(itemView);
            }

            @Override
            public int getLayoutId() {
                return R.layout.item_image;
            }
        }, urlList)
                //设置两个点图片作为翻页指示器，不设置则没有指示器，可以根据自己需求自行配合自己的指示器，不需要圆点指示器可以不设
                .setPageIndicator(new int[]{R.drawable.point_gray, R.drawable.point_black})
                //设置指示器的位置（左、中、右）
                .setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.CENTER_HORIZONTAL)
                //设置指示器是否可见
                .setPointViewVisible(true);
        if (urlList != null) {
            ivCancel.setVisibility(View.VISIBLE);
            cbAdvertise.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoadBannerFail(String msg) {

    }

    @Override
    public void onFragmentRefresh() {

    }

    @Override
    public void onFragmentStopRefresh() {
        if (srlRefresh != null && srlRefresh.isRefreshing()) {
            srlRefresh.setRefreshing(false);
        }
    }
}
