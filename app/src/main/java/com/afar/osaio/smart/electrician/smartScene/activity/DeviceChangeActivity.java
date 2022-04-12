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
import com.afar.osaio.smart.electrician.smartScene.adapter.DeviceChangeAdapter;
import com.afar.osaio.smart.electrician.smartScene.presenter.DeviceChangePresenter;
import com.afar.osaio.smart.electrician.smartScene.presenter.IDeviceChangePresenter;
import com.afar.osaio.smart.electrician.smartScene.view.IDeviceChangeView;
import com.afar.osaio.smart.electrician.util.ErrorHandleUtil;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.home.sdk.bean.scene.SceneBean;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.io.Serializable;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DeviceChangeActivity extends BaseActivity implements IDeviceChangeView {

    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.rvDev)
    RecyclerView rcvDev;

    private IDeviceChangePresenter mPresenter;
    private DeviceChangeAdapter mAdapter;
    private String sceneCondition;
    private String sceneTask;
    private boolean isAdd;
    private SceneBean mSceneBean;

    public static void toDeviceChangeActivity(Activity from, String sceneCondition, String sceneTask, boolean isAdd, Serializable sceneBean) {
        Intent intent = new Intent(from, DeviceChangeActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENECONDITION, sceneCondition);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENETASK, sceneTask);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEE_ADD, isAdd);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEBEAN, sceneBean);
        from.startActivity(intent);
    }

    public static void toDeviceChangeActivity(Activity from, String sceneCondition, String sceneTask, boolean isAdd) {
        Intent intent = new Intent(from, DeviceChangeActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENECONDITION, sceneCondition);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENETASK, sceneTask);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEE_ADD, isAdd);
        from.startActivity(intent);
    }

    public static void toDeviceChangeActivity(Activity from) {
        Intent intent = new Intent(from, DeviceChangeActivity.class);
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
        NooieLog.e("---------->DeviceChangeActivity onNewIntent");
        processExtraData();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(getText(R.string.all_device));
        setupDeviceList();
    }

    private void processExtraData() {
        if (getCurrentIntent() == null) {
            finish();
        } else {
            sceneCondition = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SCENECONDITION);
            sceneTask = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SCENETASK);
            mSceneBean = (SceneBean) getCurrentIntent().getSerializableExtra(ConstantValue.INTENT_KEY_SCENEBEAN);
            isAdd = getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_SCENEE_ADD, false);
        }
    }

    private void setupDeviceList() {
        mAdapter = new DeviceChangeAdapter();
        mAdapter.setListener(new DeviceChangeAdapter.DeviceItemListener() {
            @Override
            public void onItemClick(DeviceBean device) {
                if (isAdd) {
                    if (mSceneBean != null) {
                        SelectFunctionActivity.toSelectFunctionActivity(DeviceChangeActivity.this, device.getDevId(), "", false, sceneCondition,sceneTask,isAdd,mSceneBean);
                    } else {
                        SelectFunctionActivity.toSelectFunctionActivity(DeviceChangeActivity.this, device.getDevId(), "", false, sceneCondition,sceneTask,isAdd);
                    }
                } else {
                    SelectFunctionActivity.toSelectFunctionActivity(DeviceChangeActivity.this, device.getDevId(), "", false, "");
                }
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        rcvDev.setLayoutManager(layoutManager);
        rcvDev.setAdapter(mAdapter);
    }

    private void initData() {
        mPresenter = new DeviceChangePresenter(this);
        mPresenter.getConditionDevList();
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
    public void notifyGetConditionDevListSuccess(List<DeviceBean> deviceBeanList) {
        mAdapter.setData(deviceBeanList);
    }

    @Override
    public void notifyGetConditionDevListFail(String errorMsg) {
        ErrorHandleUtil.toastTuyaError(this, errorMsg);
    }
}
