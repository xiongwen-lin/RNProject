package com.afar.osaio.smart.electrician.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.afar.osaio.R;
import com.afar.osaio.base.ActivityStack;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.electrician.adapter.HomeManagerDeviceAdapter;
import com.afar.osaio.smart.electrician.adapter.MixScheduleAdapter;
import com.afar.osaio.smart.electrician.bean.MixScheduleBean;
import com.afar.osaio.smart.electrician.bean.Schedule;
import com.afar.osaio.smart.electrician.bean.ScheduleHelper;
import com.afar.osaio.smart.electrician.manager.FamilyManager;
import com.afar.osaio.smart.electrician.manager.GroupHelper;
import com.afar.osaio.smart.electrician.presenter.GroupPresenter;
import com.afar.osaio.smart.electrician.presenter.IGroupPresenter;
import com.afar.osaio.smart.electrician.util.ErrorHandleUtil;
import com.afar.osaio.smart.electrician.view.IGroupView;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.google.gson.Gson;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.widget.NooieSwipeRecyclerView;
import com.nooie.widget.OnItemMenuClickListener;
import com.nooie.widget.SwipeMenu;
import com.nooie.widget.SwipeMenuBridge;
import com.nooie.widget.SwipeMenuCreator;
import com.nooie.widget.SwipeMenuItem;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.GroupBean;
import com.tuya.smart.sdk.bean.ProductBean;
import com.tuya.smart.sdk.bean.Timer;
import com.tuya.smart.sdk.bean.TimerTask;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GroupScheduleActivity extends BaseActivity implements IGroupView {

    public static final int SCHEDULE_DELETE = 0;
    public static final int SCHEDULE_DONE = 1;

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.btnGroupScheduleOption)
    TextView btnGroupScheduleOption;
    @BindView(R.id.rcvGroupSchedule)
    NooieSwipeRecyclerView rcvGroupSchedule;

    private long mGroupId;
    private String mGroupType;
    private MixScheduleAdapter mScheduleAdapter;
    private HomeManagerDeviceAdapter mDeviceAdapter;
    private IGroupPresenter mGroupPresenter;
    private List<Timer> mTimerList = new ArrayList<>();
    private List<MixScheduleBean> schedulesList;

    public static void toGroupScheduleActivity(Activity from, long groupId) {
        Intent intent = new Intent(from, GroupScheduleActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_GROUP_ID, groupId);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_schedule);
        ButterKnife.bind(this);
        initView();
        initData();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.group_schedule);
        ivRight.setImageResource(R.drawable.menu_icon_add_state_list);
        setupScheduleView();
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
            return;
        } else {
            mGroupId = getCurrentIntent().getLongExtra(ConstantValue.INTENT_KEY_GROUP_ID, 0);
            mGroupPresenter = new GroupPresenter(this, mGroupId);
            setupGroup(mGroupId);
        }
    }

    @OnClick({R.id.btnGroupScheduleOption, R.id.ivLeft, R.id.ivRight})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ivLeft: {
                ActivityStack.instance().removeGroupSettingActivity();
                GroupSettingActivity.toGroupSettingActivity(this, ConstantValue.REQUEST_CODE_GROUP_SETTING, mGroupId, mGroupType);
                finish();
                break;
            }
            case R.id.ivRight: {
                doGroupShedule();
                break;
            }
            case R.id.btnGroupScheduleOption: {
                /* toggleScheduleOption();*/
                break;
            }
        }
    }

    private void doGroupShedule() {
        if (CollectionUtil.isNotEmpty(mDeviceAdapter.getData())) {//群组里有设备
            ScheduleActionActivity.toScheduleActionActivity(this, "", mGroupId, mGroupType);
        } else {//群组里没有设备
            ToastUtil.showToast(this, getResources().getString(R.string.select_least_one_device));
        }
    }

    private void toggleScheduleOption() {
        if ((int) btnGroupScheduleOption.getTag() == SCHEDULE_DELETE) {
            btnGroupScheduleOption.setTag(SCHEDULE_DONE);
            btnGroupScheduleOption.setText(R.string.DONE);

            for (int i = 0; i < mScheduleAdapter.getItemCount(); i++) {
                rcvGroupSchedule.smoothOpenRightMenu(i);
            }
            rcvGroupSchedule.resetOldSwipedLayout();
        } else {
            btnGroupScheduleOption.setTag(SCHEDULE_DELETE);
            btnGroupScheduleOption.setText(R.string.delete_uppercase);
            for (int i = 0; i < mScheduleAdapter.getItemCount(); i++) {
                rcvGroupSchedule.smoothCloseRightMenu(i);
            }
        }
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

    private void showDevices(List<DeviceBean> devices) {
        if (CollectionUtil.isNotEmpty(devices)) {
            mDeviceAdapter.setData(devices);
        }
    }

    private void setupScheduleView() {
        btnGroupScheduleOption.setTag(SCHEDULE_DELETE);
        mDeviceAdapter = new HomeManagerDeviceAdapter();
        mScheduleAdapter = new MixScheduleAdapter();
        mScheduleAdapter.setListener(new MixScheduleAdapter.ScheduleAdapterListener() {

            @Override
            public void onItemClick(Schedule schedule) {

            }

            @Override
            public void onItemTimerClick(int position, Timer timer) {
                doUpdateTimer(position, timer);
            }

            @Override
            public void onSwitchClick(Schedule schedule, String orginTime, String newTime, boolean isChecked) {

            }

            @Override
            public void onTimerSwitchClick(Timer timer, boolean isChecked, int position) {
                showLoadingDialog();
                mGroupPresenter.updateTimerStatusWithTask(String.valueOf(mGroupId), String.valueOf(mGroupId), timer.getTimerId(), isChecked, position, timer);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rcvGroupSchedule.setLayoutManager(layoutManager);
        rcvGroupSchedule.setItemViewSwipeEnabled(false);

        // 创建菜单：
        SwipeMenuCreator mSwipeMenuCreator = new SwipeMenuCreator() {
            @Override
            public void onCreateMenu(SwipeMenu leftMenu, SwipeMenu rightMenu, int viewType) {
                SwipeMenuItem deleteItem = new SwipeMenuItem(GroupScheduleActivity.this);
                deleteItem.setImage(R.drawable.delete_white)
                        .setText("")
                        .setBackground(R.drawable.lamp_schedule_operate_list_radius)
                        .setTextColor(Color.WHITE) // 文字颜色。
                        .setTextSize(10) // 文字大小。
                        .setWidth(getResources().getDimensionPixelOffset(R.dimen.dp_86))
                        .setHeight(ViewGroup.LayoutParams.MATCH_PARENT);

                rightMenu.addMenuItem(deleteItem);
            }
        };
        // 设置监听器。
        rcvGroupSchedule.setSwipeMenuCreator(mSwipeMenuCreator);

        OnItemMenuClickListener mMenuItemClickListener = new OnItemMenuClickListener() {

            @Override
            public void onItemClick(SwipeMenuBridge menuBridge, int adapterPosition) {
                // 任何操作必须先关闭菜单，否则可能出现Item菜单打开状态错乱。

                if (menuBridge.getPosition() == 0) {
                    int flag = menuBridge.getFlag();
                    if (flag == 0) {
                        menuBridge.setText(R.string.confirm_upper);
                        menuBridge.setFlag(1);
                    } else {
                        menuBridge.setFlag(0);
                        rcvGroupSchedule.smoothCloseMenu();

                        if (schedulesList != null && adapterPosition < schedulesList.size()) {
                            MixScheduleBean bean = schedulesList.get(adapterPosition);
                            if (bean != null && bean.isTimerBean()) {
                                showLoadingDialog();
                                mGroupPresenter.removeTimerWithTask(String.valueOf(mGroupId), String.valueOf(mGroupId), bean.getTimerBean().getTimerId(), adapterPosition);
                            } else {

                            }
                        }
                    }
                }
            }
        };
        // 菜单点击监听。
        rcvGroupSchedule.setOnItemMenuClickListener(mMenuItemClickListener);

        rcvGroupSchedule.setAdapter(mScheduleAdapter);
    }

    private void doUpdateTimer(int position, Timer timer) {
        ScheduleActionUpdateActivity.toScheduleActionUpdateActivity(this, "", mGroupId, new Gson().toJson(timer), mGroupType);
    }

    private void showDeleteButton(List<MixScheduleBean> schedules) {
        btnGroupScheduleOption.setVisibility((schedules == null || schedules.size() <= 0) ? View.GONE : View.VISIBLE);
    }

    private void updateUI() {
        mScheduleAdapter.clearAll();
        schedulesList = ScheduleHelper.convertMixScheduleBean(mTimerList);
        mScheduleAdapter.setData(schedulesList);
        /*showDeleteButton(mScheduleAdapter.getData());*/
    }

    @Override
    public void notifyCreateGroupScheduleState(String msg) {

    }

    @Override
    public void notifyCleanGroupScheduleState(String msg) {

    }

    @Override
    public void notifyOnGroupInfoUpdate(GroupBean groupBean) {

    }

    @Override
    public void notifyGetTimerWithTaskSuccess(TimerTask timerTask) {
        mTimerList.clear();
        if (mTimerList != null) {
            if (CollectionUtil.isNotEmpty(timerTask.getTimerList())) {
                mTimerList = timerTask.getTimerList();
            }
        }
        updateUI();
    }

    @Override
    public void notifyGetTimerWithTaskFail(String errorCode, String errorMsg) {
        ErrorHandleUtil.toastTuyaError(this, errorMsg);
    }

    @Override
    public void notifyUpdateTimerStatusWithTaskSuccess(int position, Timer timer, boolean isOpen) {
        NooieLog.e("----------timer  " + timer.toString() + "  ----isOpen  " + isOpen);
        hideLoadingDialog();
    }

    @Override
    public void notifyUpdateTimerStatusWithTaskFail(String errorCode, String errorMsg, int position, Timer timer) {
        ErrorHandleUtil.toastTuyaError(GroupScheduleActivity.this, errorMsg);
        hideLoadingDialog();
    }

    @Override
    public void notifyRemoveTimerWithTaskSuccess(int position) {
        hideLoadingDialog();
        mGroupPresenter.getTimerWithTask(String.valueOf(mGroupId), String.valueOf(mGroupId));
    }

    @Override
    public void notifyRemoveTimerWithTaskFail(String errorCode, String errorMsg) {
        hideLoadingDialog();
        ErrorHandleUtil.toastTuyaError(this, errorMsg);
    }
}
