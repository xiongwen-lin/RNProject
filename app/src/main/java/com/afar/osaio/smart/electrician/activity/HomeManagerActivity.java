package com.afar.osaio.smart.electrician.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.electrician.adapter.HomeDeviceAdapter;
import com.afar.osaio.smart.electrician.adapter.HomeManagerListAdapter;
import com.afar.osaio.smart.electrician.adapter.HomeOwnerAdapter;
import com.afar.osaio.smart.electrician.adapter.HomeShareUserAdapter;
import com.afar.osaio.smart.electrician.eventbus.HomeChangeEvent;
import com.afar.osaio.smart.electrician.eventbus.UpdateProfileEvent;
import com.afar.osaio.smart.electrician.manager.FamilyManager;
import com.afar.osaio.smart.electrician.manager.PowerStripHelper;
import com.afar.osaio.smart.electrician.presenter.HomeManagerPresenter;
import com.afar.osaio.smart.electrician.presenter.IHomeManagerPresenter;
import com.afar.osaio.smart.electrician.util.ErrorHandleUtil;
import com.afar.osaio.smart.electrician.util.SpacesItemDecoration;
import com.afar.osaio.smart.electrician.view.IHomeManagerView;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.tuya.smart.api.MicroContext;
import com.tuya.smart.api.service.MicroServiceManager;
import com.tuya.smart.commonbiz.bizbundle.family.api.AbsBizBundleFamilyService;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.anntation.MemberRole;
import com.tuya.smart.home.sdk.api.ITuyaHomeChangeListener;
import com.tuya.smart.home.sdk.api.ITuyaHomeManager;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.bean.MemberBean;
import com.tuya.smart.home.sdk.bean.SharedUserInfoBean;
import com.tuya.smart.panelcaller.api.AbsPanelCallerService;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.GroupBean;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * HomeManagerActivity
 *
 * @author Administrator
 * @date 2019/3/13
 */
public class HomeManagerActivity extends BaseActivity implements IHomeManagerView, ITuyaHomeChangeListener {

    private final static int HOME_LIST_CLOSE = 0;
    private final static int HOME_LIST_OPEN = 1;

    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.btnHomeManagerOwnerAdd)
    TextView btnHomeManagerOwnerAdd;
    @BindView(R.id.btnHomeManagerGuestAdd)
    TextView btnHomeManagerGuestAdd;
    @BindView(R.id.rcvHomeManagerDevice)
    RecyclerView rcvHomeManagerDevice;
    @BindView(R.id.rcvHomeManagerOwner)
    RecyclerView rcvHomeManagerOwner;
    @BindView(R.id.rcvHomeManagerGuest)
    RecyclerView rcvHomeManagerGuest;
    @BindView(R.id.rcvHomeList)
    RecyclerView rcvHomeList;
    @BindView(R.id.flHomeList)
    FrameLayout flHomeList;

    private IHomeManagerPresenter mHomeManagerPresenter;

    private HomeDeviceAdapter mDevicesAdapter;
    private HomeOwnerAdapter mOwnersAdapter;
    private HomeShareUserAdapter mGuestsAdapter;
    private HomeManagerListAdapter mHomesAdapter;
    private long mHomeId;
    private boolean isAdmin;
    private int mRole;// -1:invalid role 0:家庭普通成员 1:家庭管理员 无增删除其他管理员权限 2:家庭超级管理员 拥有者
    private ITuyaHomeManager mTuyaHomeManager;

    public static void toHomeManagerActivity(Activity from, long homeId) {
        Intent intent = new Intent(from, HomeManagerActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_HOME_ID, homeId);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_manager);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        initView();
        initData();
        registerHomeChangeListener();
    }

    private void initView() {
        tvTitle.setTag(HOME_LIST_CLOSE);
        tvTitle.setText(R.string.home);
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        ivRight.setImageResource(R.drawable.menu_setting_icon_state_list);
        flHomeList.setVisibility(View.GONE);
        setupDevicesView();
        setupOwnersView();
        setupGuestsView();
        setupHomeListView();
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
            return;
        } else {
            mHomeId = getCurrentIntent().getLongExtra(ConstantValue.INTENT_KEY_HOME_ID, 0);
            mHomeManagerPresenter = new HomeManagerPresenter(this);
            mHomeManagerPresenter.loadHomes(true);
        }
    }

    private void showHome(HomeBean homeBean) {
        if (homeBean != null) {
            tvTitle.setText(homeBean.getName());
            isAdmin = homeBean.isAdmin();
            showHomeManagerDevice(homeBean.getDeviceList());
        }
    }

    private void setupHomeListView() {
        mHomesAdapter = new HomeManagerListAdapter();
        mHomesAdapter.setListener(new HomeManagerListAdapter.HomeListListener() {
            @Override
            public void onItemClick(HomeBean homeBean) {
                toggleHomeList();
                if (homeBean != null) {
                    mHomeManagerPresenter.changeCurrentHome(homeBean.getHomeId());
                } else {
                    NameHomeActivity.toNameHomeActivity(HomeManagerActivity.this, ConstantValue.REQUEST_CODE_ADD_HOME, 0, null);
                }
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rcvHomeList.setLayoutManager(layoutManager);
        rcvHomeList.setAdapter(mHomesAdapter);
        rcvHomeList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == event.ACTION_UP) {
//                    toggleHomeList();
                }
                return false;
            }

        });
    }


    private void setupDevicesView() {
        mDevicesAdapter = new HomeDeviceAdapter();
        mDevicesAdapter.setListener(new HomeDeviceAdapter.HomeDeviceListener() {
            @Override
            public void onAddClickListener() {
                AddDeviceSelectLinkageActivity.toAddDeviceSelectLinkageActivity(HomeManagerActivity.this, false);
            }

            @Override
            public void onItemClickListener(DeviceBean device) {
                if (PowerStripHelper.getInstance().isUserTuyaPanel(device)
                        || PowerStripHelper.getInstance().isPlug(device)
                        || PowerStripHelper.getInstance().isWallSwitch(device)
                        || PowerStripHelper.getInstance().isLamp(device)
                        || PowerStripHelper.getInstance().isPowerStrip(device)
                        || (PowerStripHelper.getInstance().isMultiWallSwitch(device))
                ) {
                    AbsPanelCallerService service = MicroContext.getServiceManager().findServiceByInterface(AbsPanelCallerService.class.getName());
                    service.goPanelWithCheckAndTip(HomeManagerActivity.this, device.getDevId());
                } else {
                    WrongDeviceActivity.toWrongDeviceActivity(HomeManagerActivity.this, device.getDevId());
                }
            }
        });
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        rcvHomeManagerDevice.setLayoutManager(layoutManager);
        rcvHomeManagerDevice.setAdapter(mDevicesAdapter);
    }

    public void setupOwnersView() {
        mOwnersAdapter = new HomeOwnerAdapter();
        mOwnersAdapter.setListener(new HomeOwnerAdapter.HomeOwnerListener() {
            @Override
            public void onItemClick(MemberBean member) {
                if (!TextUtils.isEmpty(member.getUid())) {
                    if (member.getAccount().equals(mUid)) {
                        MyProfileActivity.toMyProfileActivity(HomeManagerActivity.this, member.getMemberId(), member.getNickName());
                    } else {
                        FamilyMemberActivity.toFamilyMemberActivity(HomeManagerActivity.this, ConstantValue.REQUEST_CODE_MEMBER_INFO,
                                member.getMemberId(), member.getAccount(), member.getRole(), mRole, member.getNickName(), member.getHeadPic());
                    }
                }
            }
        });

        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);

        rcvHomeManagerOwner.setLayoutManager(layoutManager);
        rcvHomeManagerOwner.setAdapter(mOwnersAdapter);
        ((SimpleItemAnimator) rcvHomeManagerOwner.getItemAnimator()).setSupportsChangeAnimations(false);

        int leftRight = DisplayUtil.dpToPx(this, 16);
        int topBottom = DisplayUtil.dpToPx(this, 16);
        rcvHomeManagerOwner.addItemDecoration(new SpacesItemDecoration(leftRight, topBottom));
    }

    public void setupGuestsView() {
        mGuestsAdapter = new HomeShareUserAdapter();
        mGuestsAdapter.setListener(new HomeShareUserAdapter.HomeShareUserListener() {
            @Override
            public void onItemClick(SharedUserInfoBean sharedUserInfoBean) {
                MemberActivity.toMemberActivity(HomeManagerActivity.this, ConstantValue.REQUEST_CODE_MEMBER_INFO_REMOVE, sharedUserInfoBean.getMemeberId(),
                        sharedUserInfoBean.getUserName(), isAdmin, "", ConstantValue.REMOVE_HOME_GUEST, sharedUserInfoBean.getRemarkName(), sharedUserInfoBean.getIconUrl());
            }
        });

        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);

        rcvHomeManagerGuest.setLayoutManager(layoutManager);
        rcvHomeManagerGuest.setAdapter(mGuestsAdapter);
        ((SimpleItemAnimator) rcvHomeManagerGuest.getItemAnimator()).setSupportsChangeAnimations(false);

        int leftRight = DisplayUtil.dpToPx(this, 16);
        int topBottom = DisplayUtil.dpToPx(this, 16);
        rcvHomeManagerGuest.addItemDecoration(new SpacesItemDecoration(leftRight, topBottom));
    }

    @OnClick({R.id.tvTitle, R.id.ivRight, R.id.btnHomeManagerOwnerAdd, R.id.btnHomeManagerGuestAdd, R.id.flHomeList, R.id.ivLeft})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.flHomeList:
            case R.id.tvTitle: {
                toggleHomeList();
                break;
            }
            case R.id.ivRight: {
                HomeSettingActivity.toHomeSettingActivity(HomeManagerActivity.this,
                        ConstantValue.REQUEST_CODE_HOME_MANAGE, FamilyManager.getInstance().getCurrentHomeId(), isAdmin);
                break;
            }
            case R.id.btnHomeManagerOwnerAdd: {
                AddOwnerActivity.toAddOwnerActivity(HomeManagerActivity.this, FamilyManager.getInstance().getCurrentHomeId(),
                        FamilyManager.getInstance().getCurrentHome().getName(), ConstantValue.REQUEST_CODE_ADD_MEMBER_HOME);
                break;
            }
            case R.id.btnHomeManagerGuestAdd: {
                AddGuestActivity.toAddGuestActivity(HomeManagerActivity.this, FamilyManager.getInstance().getCurrentHomeId(),
                        FamilyManager.getInstance().getCurrentHome().getName(), ConstantValue.REQUEST_CODE_ADD_MEMBER_HOME);
                break;
            }
            case R.id.ivLeft: {
                finish();
                break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ConstantValue.REQUEST_CODE_ADD_MEMBER_HOME: {
                    mHomeManagerPresenter.loadHomeMembers(FamilyManager.getInstance().getCurrentHomeId());
                    mHomeManagerPresenter.loadUserShareList(FamilyManager.getInstance().getCurrentHomeId());
                    break;
                }
                case ConstantValue.REQUEST_CODE_ADD_HOME: {
                    int homeId = data != null ? data.getIntExtra(ConstantValue.INTENT_KEY_ADD_HOME_ID, 0) : 0;
                    if (homeId != 0) {
                    }
                    mHomeManagerPresenter.loadHomes(false);
                    break;
                }
                case ConstantValue.REQUEST_CODE_MEMBER_INFO: {
                    mHomeManagerPresenter.loadHomeMembers(FamilyManager.getInstance().getCurrentHomeId());
                    mHomeManagerPresenter.loadUserShareList(FamilyManager.getInstance().getCurrentHomeId());
                    break;
                }
                case ConstantValue.REQUEST_CODE_MEMBER_INFO_REMOVE: {
                    if (mHomeId != 0) {
                        mHomeManagerPresenter.loadUserShareList(mHomeId);
                    } else {
                        mHomeManagerPresenter.loadUserShareList(FamilyManager.getInstance().getCurrentHomeId());
                    }
                    break;
                }

                case ConstantValue.REQUEST_CODE_HOME_MANAGE: {
//                    isChange = true;
                    mHomeManagerPresenter.loadHomes(false);
                    mHomeManagerPresenter.getHomeDetail(FamilyManager.getInstance().getCurrentHomeId());
                    break;
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void toggleHomeList() {
        if ((int) tvTitle.getTag() == HOME_LIST_OPEN) {
            tvTitle.setTag(HOME_LIST_CLOSE);
            setToggleDown();
            flHomeList.setVisibility(View.GONE);
            ivLeft.setVisibility(View.VISIBLE);
            ivRight.setVisibility(View.VISIBLE);
        } else {
            tvTitle.setTag(HOME_LIST_OPEN);
            setToggleUp();
            flHomeList.setVisibility(View.VISIBLE);
            ivLeft.setVisibility(View.GONE);
            ivRight.setVisibility(View.GONE);
        }
    }

    private void setToggleDown() {
        Drawable drawable = getResources().getDrawable(R.drawable.home_toggle_down_black);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        tvTitle.setCompoundDrawables(null, null, drawable, null);
    }

    private void setToggleUp() {
        Drawable drawable = getResources().getDrawable(R.drawable.home_toggle_up_black);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        tvTitle.setCompoundDrawables(null, null, drawable, null);
    }

    private void showHomeList(List<HomeBean> homes) {
        if (homes != null && homes.size() > 0) {
            tvTitle.setTag(HOME_LIST_CLOSE);
            setToggleDown();
            mHomesAdapter.setData(homes);
        }
    }

    private void showHomeManagerDevice(List<DeviceBean> devices) {
        if (devices != null) {
            mDevicesAdapter.setData(devices);
        }
    }

    private void showHomeManagerOwner(List<MemberBean> members) {
        if (members != null) {
            mOwnersAdapter.setData(members);
            for (MemberBean memberBean : members) {
                if (memberBean.getAccount().equals(mUid)) {
                    mRole = memberBean.getRole();
                    if (memberBean.getRole() == MemberRole.ROLE_OWNER) {//如果是家庭的创建者
                        btnHomeManagerOwnerAdd.setVisibility(View.VISIBLE);
                    } else {
                        btnHomeManagerOwnerAdd.setVisibility(View.GONE);
                    }
                    return;
                }
            }
        }
    }

    private void showHomeManagerGuest(List<SharedUserInfoBean> sharedUserInfoBeanList) {
        if (sharedUserInfoBeanList != null) {
            mGuestsAdapter.setData(sharedUserInfoBeanList);
        }
    }

    private void updateUI(HomeBean homeBean) {
        if (homeBean != null) {
            tvTitle.setText(homeBean.getName());
            showHomeManagerDevice(homeBean.getDeviceList());
            mHomeManagerPresenter.loadHomeMembers(homeBean.getHomeId());
            mHomeManagerPresenter.loadUserShareList(homeBean.getHomeId());
        }
    }

    public void registerHomeChangeListener() {
        mTuyaHomeManager = TuyaHomeSdk.getHomeManagerInstance();
        mTuyaHomeManager.registerTuyaHomeChangeListener(this);
    }

    private void unRegisterHomeChangeListener() {
        mTuyaHomeManager.unRegisterTuyaHomeChangeListener(this);
        mTuyaHomeManager.onDestroy();
    }

    @Override
    public void notifyGetHomeDetailSuccess(HomeBean homeBean) {
        if (isDestroyed()) {
            return;
        }
        showHome(homeBean);
    }

    @Override
    public void notifyGetHomeDetailFailed(String msg) {
    }

    @Override
    public void notifyHomeDevices(List<DeviceBean> devices) {
        if (isDestroyed()) {
            return;
        }
        showHomeManagerDevice(devices);
    }

    @Override
    public void notifyHomeMemberSuccess(List<MemberBean> members) {
        if (isDestroyed()) {
            return;
        }
        hideLoadingDialog();
        showHomeManagerOwner(members);
    }

    @Override
    public void notifyHomeMemberFailed(String msg) {
        if (isDestroyed()) {
            return;
        }
        hideLoadingDialog();
        ErrorHandleUtil.toastTuyaError(this,getString(R.string.get_fail));
    }

    @Override
    public void notifyLoadHomesSuccess(List<HomeBean> homes, boolean isUpdate) {
        if (isDestroyed()) {
            return;
        }
        if (homes != null) {
            if (homes.size() > 0) {
                showHomeList(homes);
                if (isUpdate) {
                    if (mHomeId != 0) {
                        mHomeManagerPresenter.getHomeDetail(mHomeId);
                        mHomeManagerPresenter.loadHomeMembers(mHomeId);
                        mHomeManagerPresenter.loadUserShareList(mHomeId);
                    } else {
                        mHomeManagerPresenter.getHomeDetail(FamilyManager.getInstance().getCurrentHomeId());
                        mHomeManagerPresenter.loadHomeMembers(FamilyManager.getInstance().getCurrentHomeId());
                        mHomeManagerPresenter.loadUserShareList(FamilyManager.getInstance().getCurrentHomeId());
                    }
                }
            }

        }
    }

    @Override
    public void notifyLoadHomesFailed(String msg) {
        if (isDestroyed()) {
            return;
        }
        hideLoadingDialog();
        ErrorHandleUtil.toastTuyaError(this, msg);
    }

    @Override
    public void notifyResetHomeState(String msg) {
        if (isDestroyed()) {
            return;
        }
        if (ConstantValue.SUCCESS.equalsIgnoreCase(msg)) {
            updateUI(FamilyManager.getInstance().getCurrentHome());
        } else {
            hideLoadingDialog();
            ErrorHandleUtil.toastTuyaError(this, msg);
        }
    }

    @Override
    public void notifyChangeHomeState(String msg) {
        if (isDestroyed()) {
            return;
        }
        if (ConstantValue.SUCCESS.equalsIgnoreCase(msg)) {
            updateUI(FamilyManager.getInstance().getCurrentHome());
            AbsBizBundleFamilyService service = MicroServiceManager.getInstance().findServiceByInterface(AbsBizBundleFamilyService.class.getName());
            //设置为当前家庭的homeId
            service.setCurrentHomeId(FamilyManager.getInstance().getCurrentHomeId());
            EventBus.getDefault().post(new HomeChangeEvent(FamilyManager.getInstance().getCurrentHomeId()));
        } else {
            ErrorHandleUtil.toastTuyaError(this, msg);
        }
    }

    @Override
    public void notifyUserShareListSuccess(List<SharedUserInfoBean> sharedUserInfoBeanList) {
        if (isDestroyed()) {
            return;
        }
        showHomeManagerGuest(sharedUserInfoBeanList);
    }

    @Override
    public void notifyUserShareListFail(String msg) {
        if (isDestroyed()) {
            return;
        }
        ErrorHandleUtil.toastTuyaError(this, msg);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateProfileEvent(UpdateProfileEvent event) {
        mHomeManagerPresenter.loadHomeMembers(FamilyManager.getInstance().getCurrentHomeId());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        unRegisterHomeChangeListener();
    }


    //----------------- ITuyaHomeChangeListener -----------------
    @Override
    public void onHomeAdded(long homeId) {
    }

    @Override
    public void onHomeInvite(long l, String s) {

    }

    @Override
    public void onHomeRemoved(long homeId) {
        mHomeManagerPresenter.resetHome();
    }

    @Override
    public void onHomeInfoChanged(long homeId) {
        mHomeManagerPresenter.getHomeDetail(homeId);
    }

    @Override
    public void onSharedDeviceList(List<DeviceBean> sharedDevices) {
        if (sharedDevices != null) {
        }
    }

    @Override
    public void onSharedGroupList(List<GroupBean> sharedGroups) {
        if (sharedGroups != null) {
        }
    }

    @Override
    public void onServerConnectSuccess() {
        mHomeManagerPresenter.getHomeDetail(FamilyManager.getInstance().getCurrentHomeId());
    }

    //-----------ITuyaHomeChangeListener end -------------------

}
