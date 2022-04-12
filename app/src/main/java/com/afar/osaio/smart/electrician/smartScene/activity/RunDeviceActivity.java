package com.afar.osaio.smart.electrician.smartScene.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.electrician.bean.MixDeviceBean;
import com.afar.osaio.smart.electrician.smartScene.adapter.DeviceAdapter;
import com.afar.osaio.smart.electrician.smartScene.presenter.IRunDevicePresenter;
import com.afar.osaio.smart.electrician.smartScene.presenter.RunDevicePresenter;
import com.afar.osaio.smart.electrician.smartScene.view.IRunDeviceView;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.home.sdk.bean.scene.SceneBean;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.GroupBean;

import java.io.Serializable;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RunDeviceActivity extends BaseActivity implements IRunDeviceView {

    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.rvDev)
    RecyclerView rcvDev;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;

    private IRunDevicePresenter mPresenter;
    private DeviceAdapter mAdapter;
    private String sceneCondition;
    private String sceneTask;
    private boolean isAdd;
    private SceneBean mSceneBean;

    public static void toRunDeviceActivity(Activity from, String sceneCondition, String sceneTask, boolean isAdd) {
        Intent intent = new Intent(from, RunDeviceActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENECONDITION, sceneCondition);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENETASK, sceneTask);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEE_ADD, isAdd);
        from.startActivity(intent);
    }

    public static void toRunDeviceActivity(Activity from, String sceneCondition, String sceneTask, boolean isAdd, Serializable sceneBean) {
        Intent intent = new Intent(from, RunDeviceActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENECONDITION, sceneCondition);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENETASK, sceneTask);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEE_ADD, isAdd);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEBEAN, sceneBean);
        from.startActivity(intent);
    }

    public static void toRunDeviceActivity(Activity from, String sceneCondition) {
        Intent intent = new Intent(from, RunDeviceActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENECONDITION, sceneCondition);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_device);
        ButterKnife.bind(this);
        initView();
        initData();
        processExtraData();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        NooieLog.e("---------->RunDeviceActivity onNewIntent");
        processExtraData();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(getText(R.string.all_device));
        setupDeviceList();
    }

    private void initData() {
        mPresenter = new RunDevicePresenter(this);
        mPresenter.getDevAndGroupTask();
    }

    private void processExtraData() {
        if (getCurrentIntent() == null) {
            finish();
        } else {
            sceneCondition = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SCENECONDITION);
            mSceneBean = (SceneBean) getCurrentIntent().getSerializableExtra(ConstantValue.INTENT_KEY_SCENEBEAN);
            sceneTask = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SCENETASK);
            isAdd = getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_SCENEE_ADD, false);
        }
    }

    private void setupDeviceList() {
        mAdapter = new DeviceAdapter();
        mAdapter.setListener(new DeviceAdapter.DeviceItemListener() {
            @Override
            public void onItemClick(MixDeviceBean device) {
                if (device != null) {
                    if (device.isGroupBean()) {
                        GroupBean groupBean = device.getGroupBean();
                        if (groupBean != null) {
                            if (isAdd) {
                                if (mSceneBean != null) {
                                    SelectFunctionActivity.toSelectFunctionActivity(RunDeviceActivity.this, "", String.valueOf(groupBean.getId()), true, sceneCondition, sceneTask, isAdd, mSceneBean);
                                } else {
                                    SelectFunctionActivity.toSelectFunctionActivity(RunDeviceActivity.this, "", String.valueOf(groupBean.getId()), true, sceneCondition, sceneTask, isAdd);
                                }
                            } else {
                                SelectFunctionActivity.toSelectFunctionActivity(RunDeviceActivity.this, "", String.valueOf(groupBean.getId()), true, sceneCondition);
                            }
                        }
                    } else {
                        DeviceBean deviceBean = device.getDeviceBean();
                        if (deviceBean != null) {
                            if (isAdd) {
                                if (mSceneBean != null) {
                                    SelectFunctionActivity.toSelectFunctionActivity(RunDeviceActivity.this, deviceBean.getDevId(), "", true, sceneCondition, sceneTask, isAdd, mSceneBean);
                                } else {
                                    SelectFunctionActivity.toSelectFunctionActivity(RunDeviceActivity.this, deviceBean.getDevId(), "", true, sceneCondition, sceneTask, isAdd);
                                }
                            } else {
                                SelectFunctionActivity.toSelectFunctionActivity(RunDeviceActivity.this, deviceBean.getDevId(), "", true, sceneCondition);
                            }
                        }
                    }
                }
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        rcvDev.setLayoutManager(layoutManager);
        rcvDev.setAdapter(mAdapter);
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
    public void notifyGetTaskDevAndGoupListSuccess(List<MixDeviceBean> mixDeviceBeanList) {
        mAdapter.setData(mixDeviceBeanList);
    }
}
