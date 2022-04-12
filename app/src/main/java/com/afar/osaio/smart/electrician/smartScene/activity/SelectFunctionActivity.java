package com.afar.osaio.smart.electrician.smartScene.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.electrician.smartScene.adapter.DeviceFuncAdapter;
import com.afar.osaio.smart.electrician.smartScene.presenter.ISelectFunctionPresenter;
import com.afar.osaio.smart.electrician.smartScene.presenter.SelectFunctionPresenter;
import com.afar.osaio.smart.electrician.smartScene.view.ISelectFunctionView;
import com.afar.osaio.smart.electrician.util.ErrorHandleUtil;
import com.afar.osaio.util.ConstantValue;
import com.google.gson.Gson;
import com.tuya.smart.home.sdk.bean.scene.SceneBean;
import com.tuya.smart.home.sdk.bean.scene.dev.TaskListBean;

import java.io.Serializable;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SelectFunctionActivity extends BaseActivity implements ISelectFunctionView {

    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.rvDev)
    RecyclerView rcvDev;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;

    private ISelectFunctionPresenter mPresenter;
    private DeviceFuncAdapter mAdapter;
    private String mDeviceId;
    private String mGroupId;
    private boolean isTask;
    private String sceneCondition;
    private String sceneTask;
    private boolean isAdd;
    private SceneBean mSceneBean;

    public static void toSelectFunctionActivity(Activity from, String deviceId, String groupId, boolean isTask, String sceneCondition, String sceneTask, boolean isAdd) {
        Intent intent = new Intent(from, SelectFunctionActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        intent.putExtra(ConstantValue.INTENT_KEY_GROUP_ID, groupId);
        intent.putExtra(ConstantValue.INTENT_KEY_IS_TASK, isTask);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENECONDITION, sceneCondition);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENETASK, sceneTask);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEE_ADD, isAdd);
        from.startActivity(intent);
    }

    public static void toSelectFunctionActivity(Activity from, String deviceId, String groupId, boolean isTask, String sceneCondition, String sceneTask, boolean isAdd, Serializable sceneBean) {
        Intent intent = new Intent(from, SelectFunctionActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        intent.putExtra(ConstantValue.INTENT_KEY_GROUP_ID, groupId);
        intent.putExtra(ConstantValue.INTENT_KEY_IS_TASK, isTask);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENECONDITION, sceneCondition);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENETASK, sceneTask);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEE_ADD, isAdd);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEBEAN, sceneBean);
        from.startActivity(intent);
    }

    public static void toSelectFunctionActivity(Activity from, String deviceId, String groupId, boolean isTask, String sceneCondition) {
        Intent intent = new Intent(from, SelectFunctionActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        intent.putExtra(ConstantValue.INTENT_KEY_GROUP_ID, groupId);
        intent.putExtra(ConstantValue.INTENT_KEY_IS_TASK, isTask);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENECONDITION, sceneCondition);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_function);
        ButterKnife.bind(this);
        initView();
        initData();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(getText(R.string.select_function));
        setupDeviceList();
    }

    private void setupDeviceList() {
        mAdapter = new DeviceFuncAdapter();
        mAdapter.setListener(new DeviceFuncAdapter.FunctionItemListener() {
            @Override
            public void onItemClick(TaskListBean taskListBean) {
                if (TextUtils.isEmpty(mDeviceId)) {
                    if (isAdd) {
                        if (mSceneBean != null) {
                            DeviceDpActivity.toDeviceDpActivity(SelectFunctionActivity.this, new Gson().toJson(taskListBean), "", mGroupId, isTask, sceneCondition, sceneTask, isAdd, mSceneBean);
                        } else {
                            DeviceDpActivity.toDeviceDpActivity(SelectFunctionActivity.this, new Gson().toJson(taskListBean), "", mGroupId, isTask, sceneCondition, sceneTask, isAdd);
                        }
                    } else {
                        DeviceDpActivity.toDeviceDpActivity(SelectFunctionActivity.this, new Gson().toJson(taskListBean), "", mGroupId, isTask, sceneCondition);
                    }
                } else {
                    if (isAdd) {
                        if (mSceneBean != null) {
                            DeviceDpActivity.toDeviceDpActivity(SelectFunctionActivity.this, new Gson().toJson(taskListBean), mDeviceId, "", isTask, sceneCondition, sceneTask, isAdd, mSceneBean);
                        } else {
                            DeviceDpActivity.toDeviceDpActivity(SelectFunctionActivity.this, new Gson().toJson(taskListBean), mDeviceId, "", isTask, sceneCondition, sceneTask, isAdd);
                        }
                    } else {
                        DeviceDpActivity.toDeviceDpActivity(SelectFunctionActivity.this, new Gson().toJson(taskListBean), mDeviceId, "", isTask, sceneCondition);
                    }
                }
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        rcvDev.setLayoutManager(layoutManager);
        rcvDev.setAdapter(mAdapter);
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
        } else {
            mDeviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
            mGroupId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_GROUP_ID);
            isTask = getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_IS_TASK, false);
            sceneCondition = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SCENECONDITION);
            mSceneBean = (SceneBean) getCurrentIntent().getSerializableExtra(ConstantValue.INTENT_KEY_SCENEBEAN);
            sceneTask = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SCENETASK);
            isAdd = getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_SCENEE_ADD, false);
            mPresenter = new SelectFunctionPresenter(this);
            if (isTask) {
                if (TextUtils.isEmpty(mDeviceId)) {
                    mPresenter.getGroupTask(mGroupId);
                } else {
                    mPresenter.getDevTask(mDeviceId);
                }
            } else {
                mPresenter.getDevCondition(mDeviceId);
            }
        }
    }

    @OnClick({R.id.ivLeft})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft: {
                finish();
                break;
            }
        }
    }

    @Override
    public void notifyGetDeviceTaskListSuccess(List<TaskListBean> result) {
        mAdapter.setData(result);
    }

    @Override
    public void notifyGetDeviceTaskListFail(String errorMsg) {
        ErrorHandleUtil.toastTuyaError(this, errorMsg);
    }
}

