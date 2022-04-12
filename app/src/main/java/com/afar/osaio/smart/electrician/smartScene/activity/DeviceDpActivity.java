package com.afar.osaio.smart.electrician.smartScene.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.electrician.smartScene.adapter.DeviceDpAdapter;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.scene.SceneBean;
import com.tuya.smart.home.sdk.bean.scene.SceneCondition;
import com.tuya.smart.home.sdk.bean.scene.SceneTask;
import com.tuya.smart.home.sdk.bean.scene.condition.rule.BoolRule;
import com.tuya.smart.home.sdk.bean.scene.condition.rule.EnumRule;
import com.tuya.smart.home.sdk.bean.scene.condition.rule.ValueRule;
import com.tuya.smart.home.sdk.bean.scene.dev.TaskListBean;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DeviceDpActivity extends BaseActivity {
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.clSlider)
    View clSlider;
    @BindView(R.id.tvValue)
    TextView tvValue;
    @BindView(R.id.sbBrightBar)
    SeekBar sbBrightBar;
    @BindView(R.id.tvMax)
    TextView tvMax;
    @BindView(R.id.tvMin)
    TextView tvMin;
    @BindView(R.id.rcvTask)
    RecyclerView rcvTask;
    @BindView(R.id.ivLess)
    ImageView ivLess;
    @BindView(R.id.ivEqual)
    ImageView ivEqual;
    @BindView(R.id.ivMore)
    ImageView ivMore;
    @BindView(R.id.tvUnit)
    TextView tvUnit;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;

    private TaskListBean mTask;
    private DeviceDpAdapter mAdapter;
    private List<SceneTask> taskList;
    private List<SceneCondition> conditionList;
    private String mDeviceId;
    private String mGroupId;
    private String dpKey;
    private String dpValue;
    private String dpId;
    private boolean isTask;
    private DeviceBean deviceBean;
    private String sceneCondition;
    private String sceneTask;
    private boolean isAdd;
    private boolean isModify;
    private int position;
    private SceneBean mSceneBean;

    public static void toDeviceDpActivity(Activity from, String taskListBean, String devId, String groupId, boolean isTask, String sceneCondition) {
        Intent intent = new Intent(from, DeviceDpActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_TASKLISTBEAN, taskListBean);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, devId);
        intent.putExtra(ConstantValue.INTENT_KEY_GROUP_ID, groupId);
        intent.putExtra(ConstantValue.INTENT_KEY_IS_TASK, isTask);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENECONDITION, sceneCondition);
        from.startActivity(intent);
    }

    public static void toDeviceDpActivity(Activity from, String taskListBean, String devId, String groupId, boolean isTask, String sceneCondition, String sceneTask, boolean isAdd, Serializable sceneBean) {
        Intent intent = new Intent(from, DeviceDpActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_TASKLISTBEAN, taskListBean);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, devId);
        intent.putExtra(ConstantValue.INTENT_KEY_GROUP_ID, groupId);
        intent.putExtra(ConstantValue.INTENT_KEY_IS_TASK, isTask);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENECONDITION, sceneCondition);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENETASK, sceneTask);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEE_ADD, isAdd);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEBEAN, sceneBean);
        from.startActivity(intent);
    }

    public static void toDeviceDpActivity(Activity from, String taskListBean, String devId, String groupId, boolean isTask, String sceneCondition, String sceneTask, boolean isAdd) {
        Intent intent = new Intent(from, DeviceDpActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_TASKLISTBEAN, taskListBean);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, devId);
        intent.putExtra(ConstantValue.INTENT_KEY_GROUP_ID, groupId);
        intent.putExtra(ConstantValue.INTENT_KEY_IS_TASK, isTask);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENECONDITION, sceneCondition);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENETASK, sceneTask);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEE_ADD, isAdd);
        from.startActivity(intent);
    }

    public static void toDeviceDpActivity(Activity from, String taskListBean, String devId, String groupId, boolean isTask, String sceneCondition, String sceneTask, boolean isModify, int position) {
        Intent intent = new Intent(from, DeviceDpActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_TASKLISTBEAN, taskListBean);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, devId);
        intent.putExtra(ConstantValue.INTENT_KEY_GROUP_ID, groupId);
        intent.putExtra(ConstantValue.INTENT_KEY_IS_TASK, isTask);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENECONDITION, sceneCondition);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENETASK, sceneTask);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEE_MODIFY, isModify);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEE_POSITION, position);
        from.startActivity(intent);
    }

    public static void toDeviceDpActivity(Activity from, String taskListBean, String devId, String groupId, boolean isTask, String sceneCondition, String sceneTask, boolean isModify, int position, Serializable sceneBean) {
        Intent intent = new Intent(from, DeviceDpActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_TASKLISTBEAN, taskListBean);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, devId);
        intent.putExtra(ConstantValue.INTENT_KEY_GROUP_ID, groupId);
        intent.putExtra(ConstantValue.INTENT_KEY_IS_TASK, isTask);
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
        setContentView(R.layout.activity_device_dp);
        ButterKnife.bind(this);
        initData();
        initView();
        processExtraData();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        ivRight.setImageResource(R.drawable.define_black);
        ivLess.setSelected(true);
    }

    private void setUpSeekBar() {
        if (mTask.getValueSchemaBean() != null) {
            tvValue.setText(String.valueOf(mTask.getValueSchemaBean().getMin()));
            sbBrightBar.setMax(mTask.getValueSchemaBean().getMax());
            /**seekbar setMin方法需要API>=26才能执行*/
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                sbBrightBar.setMin(mTask.getValueSchemaBean().getMin());
                sbBrightBar.setProgress(mTask.getValueSchemaBean().getMin());
            }
            sbBrightBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (progress < mTask.getValueSchemaBean().getMin()) {
                        NooieLog.e("---------min  " + mTask.getValueSchemaBean().getMin());
                        sbBrightBar.setProgress(mTask.getValueSchemaBean().getMin());
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (seekBar != null) {
                        tvValue.setText(String.valueOf(seekBar.getProgress()));
                    }
                }
            });
        }
    }

    private void showDpView() {
        if (mTask.getType() != null) {
            if (mTask.getType().equals("bool")) {
                rcvTask.setVisibility(View.VISIBLE);
            } else if (mTask.getType().equals("value") && mTask.getValueSchemaBean() != null) {
                if (!isTask) {
                    ivLess.setVisibility(View.VISIBLE);
                    ivEqual.setVisibility(View.VISIBLE);
                    ivMore.setVisibility(View.VISIBLE);
                }
                rcvTask.setVisibility(View.GONE);
                tvUnit.setText(mTask.getValueSchemaBean().getUnit());
                tvMin.setText(mTask.getValueSchemaBean().getMin() + mTask.getValueSchemaBean().getUnit());
                tvMax.setText(mTask.getValueSchemaBean().getMax() + mTask.getValueSchemaBean().getUnit());
                clSlider.setVisibility(View.VISIBLE);
            } else if (mTask.getType().equals("enum")) {
                rcvTask.setVisibility(View.VISIBLE);
            }
        }
    }

    private void initData() {
        setupDeviceDpAdapter();
        taskList = new ArrayList<>();
        conditionList = new ArrayList<>();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        NooieLog.e("----------DeviceDpActivity onNewIntent");
        processExtraData();
    }

    private void processExtraData() {
        if (getCurrentIntent() == null) {
            finish();
        } else {
            String taskListBean = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_TASKLISTBEAN);
            mDeviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
            mGroupId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_GROUP_ID);
            deviceBean = TuyaHomeSdk.getDataInstance().getDeviceBean(mDeviceId);
            mSceneBean = (SceneBean) getCurrentIntent().getSerializableExtra(ConstantValue.INTENT_KEY_SCENEBEAN);
            isTask = getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_IS_TASK, false);
            sceneCondition = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SCENECONDITION);
            sceneTask = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SCENETASK);
            isAdd = getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_SCENEE_ADD, false);
            isModify = getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_SCENEE_MODIFY, false);
            position = getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_SCENEE_POSITION, 0);
            mTask = new Gson().fromJson(taskListBean, TaskListBean.class);

            taskList.clear();
            conditionList.clear();

            if (mTask != null) {
                tvTitle.setText(mTask.getName());
                showDpView();
                setUpSeekBar();
            }

            List<String> key = new ArrayList<>();
            List<String> value = new ArrayList<>();
            if (mTask.getTasks() != null) {
                for (Map.Entry<Object, String> entry : mTask.getTasks().entrySet()) {
                    key.add((String) entry.getKey());
                    value.add(entry.getValue());
                    NooieLog.e("------key:" + entry.getKey() + "   value:" + entry.getValue());
                }
                NooieLog.e("--------key size " + key.size() + "  -----value size  " + value.size());
            }
            mAdapter.setData(key, value);

            if (isAdd || isModify) {
                if (isTask) {
                    if (!TextUtils.isEmpty(sceneTask)) {
                        List<SceneTask> sceneTasks = new Gson().fromJson(sceneTask, new TypeToken<List<SceneTask>>() {
                        }.getType());
                        if (CollectionUtil.isEmpty(sceneTasks)) {
                            return;
                        }
                        taskList.addAll(sceneTasks);

                        if (isModify) {
                            SceneTask task = taskList.get(position);
                            if (task.getExecutorProperty() != null) {
                                for (Map.Entry<String, Object> entry : task.getExecutorProperty().entrySet()) {
                                    NooieLog.e("sceneTask entry  " + entry.getKey() + " value " + entry.getValue());
                                    if (rcvTask.getVisibility() == View.VISIBLE) {
                                        mAdapter.setSelected(String.valueOf(entry.getValue()));
                                        dpKey = String.valueOf(entry.getValue());
                                    } else {
                                        String[] brightValue = String.valueOf(entry.getValue()).split("\\.");
                                        tvValue.setText(brightValue[0]);
                                        sbBrightBar.setProgress(Integer.valueOf(brightValue[0]));
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (!TextUtils.isEmpty(sceneCondition)) {
                        List<SceneCondition> sceneConditions = new Gson().fromJson(sceneCondition, new TypeToken<List<SceneCondition>>() {
                        }.getType());
                        if (CollectionUtil.isEmpty(sceneConditions)) {
                            return;
                        }
                        conditionList.addAll(sceneConditions);

                        if (isModify) {
                            SceneCondition sceneCondition = conditionList.get(position);
                            List<Object> list = sceneCondition.getExpr();

                            for (Object o : list) {
                                //用gson将sceneCondition.getExpr里边的object解析出来
                                String obj = new Gson().toJson(o);
                                List<Object> expr = new Gson().fromJson(obj, new TypeToken<List<Object>>() {
                                }.getType());
                                NooieLog.e("---------Device DP expr " + expr);
                                for (int i = 0; i < expr.size(); i++) {
                                    NooieLog.e("---------Device DP expr item " + expr.get(i));
                                    if (rcvTask.getVisibility() == View.VISIBLE) {
                                        mAdapter.setSelected(expr.get(2).toString());
                                        dpKey = expr.get(2).toString();
                                        break;
                                    } else {
                                        resetButton();
                                        if (expr.get(1).toString().equals("<")) {
                                            ivLess.setSelected(true);
                                        } else if (expr.get(1).toString().equals("==")) {
                                            ivEqual.setSelected(true);
                                        } else if (expr.get(1).toString().equals(">")) {
                                            ivMore.setSelected(true);
                                        }
                                        String[] valu = expr.get(2).toString().split("\\.");
                                        tvValue.setText(valu[0]);
                                        sbBrightBar.setProgress(Integer.valueOf(valu[0]));
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void setupDeviceDpAdapter() {
        mAdapter = new DeviceDpAdapter();
        mAdapter.setListener(new DeviceDpAdapter.DpItemListener() {
            @Override
            public void onItemClick(int position) {
                mAdapter.changeSelected(position);
                dpKey = mAdapter.getSelectedKey(position);
                dpValue = mAdapter.getSelectedValue(position);
                NooieLog.e("-------click position  " + position + " key " + dpKey + "  value " + dpValue);
            }

            @Override
            public void onGetSelectValue(String value) {
                NooieLog.e("-------DeviceDp onGetSelectValue " + value);
                dpValue = value;
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        rcvTask.setLayoutManager(layoutManager);
        rcvTask.setAdapter(mAdapter);
    }

    @OnClick({R.id.ivLeft, R.id.ivRight, R.id.ivMinus, R.id.ivAdd, R.id.ivLess, R.id.ivEqual, R.id.ivMore})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft: {
                finish();
                break;
            }
            case R.id.ivLess: {
                resetButton();
                ivLess.setSelected(true);
                break;
            }
            case R.id.ivEqual: {
                resetButton();
                ivEqual.setSelected(true);
                break;
            }
            case R.id.ivMore: {
                resetButton();
                ivMore.setSelected(true);
                break;
            }
            case R.id.ivRight: {
                if (mTask == null) {
                    return;
                }
                if (isTask) {
                    HashMap<String, Object> taskMap = new HashMap<>();
                    if (rcvTask.getVisibility() == View.VISIBLE) {
                        if (TextUtils.isEmpty(dpKey)) {
                            ToastUtil.showToast(this, R.string.please_choose);
                            return;
                        } else {
                            if (dpKey.equals("true")) {
                                taskMap.put(String.valueOf(mTask.getDpId()), true);
                            } else if (dpKey.equals("false")) {
                                taskMap.put(String.valueOf(mTask.getDpId()), false);
                            } else {
                                taskMap.put(String.valueOf(mTask.getDpId()), dpKey);
                            }
                        }
                    } else {
                        taskMap.put(String.valueOf(mTask.getDpId()), Integer.valueOf(tvValue.getText().toString()));
                    }
                    if ((mTask.getValueSchemaBean().getMin() == 0) && (mTask.getValueSchemaBean().getMax() == 86400) && (Integer.parseInt(tvValue.getText().toString()) < 60)) {
                        ToastUtil.showToast(this, R.string.countdown_one_min);
                        return;
                    }
                    if (TextUtils.isEmpty(mDeviceId)) {
                        SceneTask createDpGroupTask = TuyaHomeSdk.getSceneManagerInstance().createDpGroupTask(Long.parseLong(mGroupId), taskMap);
                        taskList.add(createDpGroupTask);
                    } else {
                        SceneTask deviceTask = TuyaHomeSdk.getSceneManagerInstance().createDpTask(mDeviceId, taskMap);
                        if (rcvTask.getVisibility() == View.VISIBLE) {
                            deviceTask.setEntityName(mTask.getName() + ":" + dpValue);
                        } else {
                            deviceTask.setEntityName(mTask.getName() + ":" + tvValue.getText().toString());
                        }
                        if (isModify) {
                            taskList.set(position, deviceTask);
                        } else {
                            taskList.add(deviceTask);
                        }
                    }
                    if (mSceneBean != null) {
                        CreateNewSmartActivity.toCreateNewSmartActivity(this, new Gson().toJson(taskList), sceneCondition, mSceneBean);
                        finish();
                    } else {
                        CreateNewSmartActivity.toCreateNewSmartActivity(this, new Gson().toJson(taskList), sceneCondition);
                        finish();
                    }
                } else {
                    if (TextUtils.isEmpty(mTask.getType())) {
                        return;
                    }
                    if (mTask.getType().equals("bool")) {
                        if (TextUtils.isEmpty(dpKey)) {
                            ToastUtil.showToast(this, R.string.please_choose);
                            return;
                        } else {
                            if (dpKey.equals("true")) {
                                dpId = "dp" + mTask.getDpId();
                                BoolRule boolRule = BoolRule.newInstance(dpId, true);
                                SceneCondition devCondition = SceneCondition.createDevCondition(
                                        deviceBean,    //设备
                                        String.valueOf(mTask.getDpId()),        //dpId
                                        boolRule    //规则
                                );
                                devCondition.setEntityName(deviceBean.getName());
                                devCondition.setExprDisplay(mTask.getName() + ":" + dpValue);
                                if (isModify) {
                                    conditionList.set(position, devCondition);
                                } else {
                                    conditionList.add(devCondition);
                                }
                            } else if (dpKey.equals("false")) {
                                dpId = "dp" + mTask.getDpId();
                                BoolRule boolRule = BoolRule.newInstance(dpId, false);
                                SceneCondition devCondition = SceneCondition.createDevCondition(
                                        deviceBean,    //设备
                                        String.valueOf(mTask.getDpId()),        //dpId
                                        boolRule    //规则
                                );
                                devCondition.setEntityName(deviceBean.getName());
                                devCondition.setExprDisplay(mTask.getName() + ":" + dpValue);
                                if (isModify) {
                                    conditionList.set(position, devCondition);
                                } else {
                                    conditionList.add(devCondition);
                                }
                            }
                        }
                    } else if (mTask.getType().equals("value")) {
                        String operator = "";
                        if (ivMore.isSelected()) {
                            operator = ">";
                        } else if (ivEqual.isSelected()) {
                            operator = "==";
                        } else if (ivLess.isSelected()) {
                            operator = "<";
                        }
                        dpId = "dp" + mTask.getDpId();
                        ValueRule tempRule = ValueRule.newInstance(
                                dpId,  //类别
                                operator,      //运算规则(">", "==", "<")
                                Integer.valueOf(tvValue.getText().toString())       //临界值
                        );
                        SceneCondition devCondition = SceneCondition.createDevCondition(
                                deviceBean,    //设备
                                String.valueOf(mTask.getDpId()),        //dpId
                                tempRule    //规则
                        );
                        if (operator.equals("==")) {
                            devCondition.setEntityName(deviceBean.getName());
                            devCondition.setExprDisplay(mTask.getName() + ":=" + tvValue.getText().toString());
                        } else {
                            devCondition.setEntityName(deviceBean.getName());
                            devCondition.setExprDisplay(mTask.getName() + ":" + operator + tvValue.getText().toString());
                        }
                        if (isModify) {
                            conditionList.set(position, devCondition);
                        } else {
                            conditionList.add(devCondition);
                        }
                    } else if (mTask.getType().equals("enum")) {
                        if (TextUtils.isEmpty(dpKey)) {
                            ToastUtil.showToast(this, R.string.please_choose);
                            return;
                        } else {
                            dpId = "dp" + mTask.getDpId();
                            EnumRule enumRule = EnumRule.newInstance(
                                    dpId,  //类别
                                    dpKey        //选定的枚举值
                            );
                            SceneCondition devCondition = SceneCondition.createDevCondition(
                                    deviceBean,    //设备
                                    String.valueOf(mTask.getDpId()),        //dpId
                                    enumRule    //规则
                            );
                            devCondition.setEntityName(deviceBean.getName());
                            devCondition.setExprDisplay(mTask.getName() + ":" + dpValue);
                            if (isModify) {
                                conditionList.set(position, devCondition);
                            } else {
                                conditionList.add(devCondition);
                            }
                        }
                    }
                    if (isAdd || isModify) {
                        if (mSceneBean != null) {
                            CreateNewSmartActivity.toCreateNewSmartActivity(this, sceneTask, new Gson().toJson(conditionList), mSceneBean);
                            finish();
                            break;
                        } else {
                            CreateNewSmartActivity.toCreateNewSmartActivity(this, sceneTask, new Gson().toJson(conditionList));
                            finish();
                            break;
                        }
                    }
                    CreateSceneActivity.toCreateSceneActivity(this, true, new Gson().toJson(conditionList));
                }
                break;
            }
            case R.id.ivMinus: {
                doMinus();
                break;
            }
            case R.id.ivAdd: {
                doAdd();
                break;
            }
        }
    }

    private void resetButton() {
        ivEqual.setSelected(false);
        ivLess.setSelected(false);
        ivMore.setSelected(false);
    }

    private void doMinus() {
        int result = Integer.valueOf(tvValue.getText().toString()) - 1;
        if (mTask.getValueSchemaBean() != null) {
            if (result >= mTask.getValueSchemaBean().getMin()) {
                tvValue.setText(String.valueOf(result));
                sbBrightBar.setProgress(result);
            }
        }
    }

    private void doAdd() {
        int result = Integer.valueOf(tvValue.getText().toString()) + 1;
        if (mTask.getValueSchemaBean() != null) {
            if (result <= mTask.getValueSchemaBean().getMax()) {
                tvValue.setText(String.valueOf(result));
                sbBrightBar.setProgress(result);
            }
        }
    }
}

