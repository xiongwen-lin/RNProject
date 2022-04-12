package com.afar.osaio.smart.electrician.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.electrician.eventbus.GroupManageEvent;
import com.afar.osaio.smart.electrician.manager.FamilyManager;
import com.afar.osaio.smart.electrician.presenter.GroupSettingPresenter;
import com.afar.osaio.smart.electrician.presenter.IGroupSettingPresenter;
import com.afar.osaio.smart.electrician.util.DialogUtil;
import com.afar.osaio.smart.electrician.util.ErrorHandleUtil;
import com.afar.osaio.smart.electrician.view.IGroupSettingView;
import com.afar.osaio.smart.home.activity.HomeActivity;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.sdk.api.IGroupListener;
import com.tuya.smart.sdk.api.ITuyaGroup;
import com.tuya.smart.sdk.bean.GroupBean;
import com.tuya.smart.sdk.bean.TimerTask;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * GroupSettingActivity
 *
 * @author Administrator
 * @date 2019/3/21
 */
public class GroupSettingActivity extends BaseActivity implements IGroupSettingView {

    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvGroupName)
    TextView tvGroupName;
    @BindView(R.id.tvGroupDeviceNum)
    TextView tvGroupDeviceNum;
    @BindView(R.id.tvGroupSchedule)
    TextView tvGroupSchedule;

    private IGroupSettingPresenter mGroupSettingPresenter;
    private long mGroupId;
    private String mGroupType;
    private boolean isChange;
    private ITuyaGroup mITuyaGroup;

    public static void toGroupSettingActivity(Activity from, int requestCode, long groupId, String groupType) {
        Intent intent = new Intent(from, GroupSettingActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_GROUP_ID, groupId);
        intent.putExtra(ConstantValue.INTENT_KEY_GROUP_TYPE, groupType);
        from.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_setting);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        initView();
        initData();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.settings);
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
            return;
        } else {
            isChange = false;
            mGroupSettingPresenter = new GroupSettingPresenter(this);
            mGroupId = getCurrentIntent().getLongExtra(ConstantValue.INTENT_KEY_GROUP_ID, 0);
            mGroupType = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_GROUP_TYPE);
            loadGroupInfo();
            registerGroupListener();
        }
    }

    private void loadGroupInfo() {
        showLoadingDialog();
        TuyaHomeSdk.newHomeInstance(FamilyManager.getInstance().getCurrentHomeId()).getHomeDetail(new ITuyaHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean homeBean) {
                loadGroup();
            }

            @Override
            public void onError(String code, String msg) {
                loadGroup();
            }
        });
    }

    private void loadGroup() {
        GroupBean groupBean = TuyaHomeSdk.getDataInstance().getGroupBean(mGroupId);
        mGroupSettingPresenter.getTimerWithTask(String.valueOf(mGroupId), String.valueOf(mGroupId));
        setupGroupSetting(groupBean);
        hideLoadingDialog();
    }

    @Override
    public void onResume() {
        super.onResume();
        mGroupSettingPresenter.getTimerWithTask(String.valueOf(mGroupId), String.valueOf(mGroupId));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ConstantValue.REQUEST_CODE_GROUP_RENAME: {
                    isChange = true;
                    GroupBean groupBean = TuyaHomeSdk.getDataInstance().getGroupBean(mGroupId);
                    setupGroupSetting(groupBean);
                    break;
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick({R.id.ivLeft, R.id.containerGroupName, R.id.containerGroupManageDevice, R.id.containerGroupSchedule, R.id.btnGroupRemove})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ivLeft: {
                if (isChange) {
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                }
                finish();
                break;
            }
            case R.id.containerGroupName: {
                GroupBean groupBean = TuyaHomeSdk.getDataInstance().getGroupBean(mGroupId);
                if (groupBean != null) {
                    NameGroupActivity.toNameGroupActivity(GroupSettingActivity.this, ConstantValue.REQUEST_CODE_GROUP_RENAME, mGroupId, ConstantValue.GROUP_RENAME, groupBean.getName());
                }
                break;
            }
            case R.id.containerGroupManageDevice: {
                GroupBean groupBean = TuyaHomeSdk.getDataInstance().getGroupBean(mGroupId);
                if (groupBean != null) {
                    if (mGroupType.equals(ConstantValue.GROUP_TYPE_PLUG) || mGroupType.equals(ConstantValue.GROUP_FOR_PLUG)) {
                        ManageDeviceActivity.toManageDeviceActivity(GroupSettingActivity.this, mGroupId, groupBean.getProductId());
                    } else if (mGroupType.equals(ConstantValue.GROUP_TYPE_LAMP) || mGroupType.equals(ConstantValue.GROUP_FOR_LAMP)) {
                        ManageLampDeviceActivity.toManageLampDeviceActivity(GroupSettingActivity.this, mGroupId, groupBean.getProductId());
                    }
                }
                break;
            }
            case R.id.containerGroupSchedule: {
                GroupBean groupBean = TuyaHomeSdk.getDataInstance().getGroupBean(mGroupId);
                if (groupBean != null) {
                    GroupScheduleActivity.toGroupScheduleActivity(this, mGroupId);
                }
                break;
            }
            case R.id.btnGroupRemove: {
                DialogUtil.showConfirmWithSubMsgDialog(this, R.string.dismiss_group, String.format(getResources().getString(R.string.group_remove_tip), tvGroupName.getText().toString()), R.string.cancel, R.string.confirm_upper, new DialogUtil.OnClickConfirmButtonListener() {
                    @Override
                    public void onClickRight() {
                        mGroupSettingPresenter.removeGroup(mGroupId);
                    }

                    @Override
                    public void onClickLeft() {

                    }
                });
                break;
            }
        }
    }

    private void registerGroupListener() {
        mITuyaGroup = TuyaHomeSdk.newGroupInstance(mGroupId);
        mITuyaGroup.registerGroupListener(new IGroupListener() {
            @Override
            public void onDpUpdate(long groupId, String dps) {
                NooieLog.e("groupsetting dps  " + dps);
            }

            @Override
            public void onDpCodeUpdate(long groupId, Map<String, Object> dpCodeMap) {

            }

            @Override
            public void onGroupInfoUpdate(long groupId) {
                mGroupSettingPresenter.onGroupInfoUpdate(groupId);
                NooieLog.e("groupsetting onGroupInfoUpdate ");
            }

            @Override
            public void onGroupRemoved(long l) {
                HomeActivity.toHomeActivity(GroupSettingActivity.this, HomeActivity.TYPE_REMOVE_GROUP);
                finish();
            }
        });
    }

    private void setupGroupSetting(GroupBean groupBean) {
        if (groupBean != null) {
            tvGroupName.setText(groupBean.getName());
            tvGroupDeviceNum.setText(groupBean.getDeviceBeans() == null ? "0" : groupBean.getDeviceBeans().size() + "");
        }
    }

    @Override
    public void notifyRemoveGroupState(String msg) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(msg)) {
            HomeActivity.toHomeActivity(this, HomeActivity.TYPE_ADD_GROUP);
            finish();
        } else {
        }
    }

    @Override
    public void notifyOnGroupInfoUpdate(GroupBean groupBean) {
        if (groupBean != null) {
            tvGroupName.setText(groupBean.getName());
        }
    }

    @Override
    public void notifyGetTimerWithTaskSuccess(TimerTask timerTask) {
        if (CollectionUtil.isNotEmpty(timerTask.getTimerList())) {
            tvGroupSchedule.setText(String.valueOf(timerTask.getTimerList().size()));
        } else {
            tvGroupSchedule.setText("0");
        }
    }

    @Override
    public void notifyGetTimerWithTaskFail(String errorCode, String errorMsg) {
        ErrorHandleUtil.toastTuyaError(this, errorMsg);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (mITuyaGroup != null) {
            mITuyaGroup.unRegisterGroupListener();
            mITuyaGroup.onDestroy();
        }
    }

    //修改group的name或改变group的设备数量
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGroupManageEvent(GroupManageEvent event) {
        loadGroupInfo();
    }
}
