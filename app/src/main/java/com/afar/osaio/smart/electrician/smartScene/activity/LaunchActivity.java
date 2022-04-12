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
import com.afar.osaio.smart.electrician.smartScene.adapter.LaunchAdapter;
import com.afar.osaio.smart.electrician.smartScene.presenter.IRunAndAutoPresenter;
import com.afar.osaio.smart.electrician.smartScene.presenter.RunAndAutoPresenter;
import com.afar.osaio.smart.electrician.smartScene.view.IRunAndAutoView;
import com.afar.osaio.smart.electrician.util.ErrorHandleUtil;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.scene.SceneBean;
import com.tuya.smart.home.sdk.bean.scene.SceneTask;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LaunchActivity extends BaseActivity implements IRunAndAutoView {
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.rcvLaunch)
    RecyclerView rcvLaunch;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;

    private IRunAndAutoPresenter mPresenter;
    private LaunchAdapter mAdapter;
    private List<SceneBean> launchList;
    private List<SceneTask> taskList;
    private String sceneCondition;
    private String sceneTask;
    private boolean isAdd;
    private boolean isModify;
    private int position;
    private SceneBean mSceneBean;

    public static void toLaunchActivity(Activity from, String sceneCondition) {
        Intent intent = new Intent(from, LaunchActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENECONDITION, sceneCondition);
        from.startActivity(intent);
    }

    public static void toLaunchActivity(Activity from, String sceneCondition, String sceneTask, boolean isAdd, Serializable sceneBean) {
        Intent intent = new Intent(from, LaunchActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENECONDITION, sceneCondition);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENETASK, sceneTask);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEE_ADD, isAdd);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEBEAN, sceneBean);
        from.startActivity(intent);
    }

    public static void toLaunchActivity(Activity from, String sceneCondition, String sceneTask, boolean isAdd) {
        Intent intent = new Intent(from, LaunchActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENECONDITION, sceneCondition);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENETASK, sceneTask);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEE_ADD, isAdd);
        from.startActivity(intent);
    }

    public static void toLaunchActivity(Activity from, String sceneCondition, String sceneTask, boolean isModify, int position) {
        Intent intent = new Intent(from, LaunchActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENECONDITION, sceneCondition);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENETASK, sceneTask);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEE_MODIFY, isModify);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEE_POSITION, position);
        from.startActivity(intent);
    }

    public static void toLaunchActivity(Activity from, String sceneCondition, String sceneTask, boolean isModify, int position, Serializable sceneBean) {
        Intent intent = new Intent(from, LaunchActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENECONDITION, sceneCondition);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENETASK, sceneTask);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEE_MODIFY, isModify);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEE_POSITION, position);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEBEAN, sceneBean);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        ButterKnife.bind(this);
        initView();
        initData();
        processExtraData();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(getText(R.string.select_launch));
        ivRight.setImageResource(R.drawable.define_black);
    }

    private void initData() {
        setupLaunch();
        mPresenter = new RunAndAutoPresenter(this);
        taskList = new ArrayList<>();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        NooieLog.e("----------LaunchActivity onNewIntent");
        processExtraData();
    }

    private void processExtraData() {
        if (getCurrentIntent() == null) {
            finish();
        } else {
            sceneCondition = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SCENECONDITION);
            sceneTask = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SCENETASK);
            mSceneBean = (SceneBean) getCurrentIntent().getSerializableExtra(ConstantValue.INTENT_KEY_SCENEBEAN);
            isAdd = getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_SCENEE_ADD, false);
            isModify = getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_SCENEE_MODIFY, false);
            position = getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_SCENEE_POSITION, 0);
            if (CollectionUtil.isNotEmpty(taskList)) {
                taskList.clear();
            }
            if (!TextUtils.isEmpty(sceneTask)) {
                List<SceneTask> sceneTasks = new Gson().fromJson(sceneTask, new TypeToken<List<SceneTask>>() {
                }.getType());
                if (CollectionUtil.isNotEmpty(sceneTasks)){
                    taskList.addAll(sceneTasks);
                }
            }

            mPresenter.getSceneList();
        }
    }

    private void setupLaunch() {
        mAdapter = new LaunchAdapter();
        mAdapter.setListener(new LaunchAdapter.LaunchItemListener() {
            @Override
            public void onItemClick(int position, SceneBean sceneBean) {
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        rcvLaunch.setLayoutManager(layoutManager);
        rcvLaunch.setAdapter(mAdapter);
    }

    @OnClick({R.id.ivLeft, R.id.ivRight})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft: {
                finish();
                break;
            }
            case R.id.ivRight: {
                launchList.clear();
                if (mAdapter != null) {
                    launchList = mAdapter.getSelectedScene();
                    if (CollectionUtil.isEmpty(launchList)) {
                        ToastUtil.showToast(this, R.string.please_choose);
                        return;
                    }

                    for (SceneBean sceneBean : launchList) {
                        SceneTask sceneTask = TuyaHomeSdk.getSceneManagerInstance().createSceneTask(sceneBean);
                        sceneTask.setEntityName(sceneBean.getName());
                        taskList.add(sceneTask);
                    }
                }
                NooieLog.e("---------launchList size  " + taskList.size());
                if (mSceneBean != null) {
                    CreateNewSmartActivity.toCreateNewSmartActivity(this, new Gson().toJson(taskList), sceneCondition, mSceneBean);
                    finish();
                } else {
                    CreateNewSmartActivity.toCreateNewSmartActivity(this, new Gson().toJson(taskList), sceneCondition);
                    finish();
                }
                break;
            }
        }
    }

    @Override
    public void notifyGetSceneListSuccess(List<SceneBean> sceneBeanList) {
        launchList = new ArrayList<>();
        launchList.clear();
        for (SceneBean sceneBean : sceneBeanList) {
            if (sceneBean != null && sceneBean.getConditions() == null) {
                launchList.add(sceneBean);
            }
        }

        if (CollectionUtil.isNotEmpty(taskList)) {
            for (int i = 0; i < launchList.size(); i++) {
                for (SceneTask sceneTask : taskList) {
                    if (sceneTask.getEntityId().equals(launchList.get(i).getId())) {
                        mAdapter.addSelectedScenes(launchList.get(i));
                    }
                }
            }

            Iterator<SceneTask> iterator = taskList.iterator();
            while (iterator.hasNext()) {
                SceneTask sceneTask = iterator.next();
                if (sceneTask.getActionExecutor().equals("ruleTrigger")) {
                    iterator.remove();
                }
            }
        }
        mAdapter.setData(launchList);
    }

    @Override
    public void notifyGetSceneListFail(String errorMsg) {
        ErrorHandleUtil.toastTuyaError(this, errorMsg);
    }

    @Override
    public void notifyExecuteSceneSuccess() {

    }

    @Override
    public void notifyExecuteSceneFail(String errorMsg) {

    }
}

