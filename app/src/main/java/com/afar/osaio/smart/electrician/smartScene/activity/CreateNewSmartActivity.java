package com.afar.osaio.smart.electrician.smartScene.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.electrician.eventbus.StyleEvent;
import com.afar.osaio.smart.electrician.manager.FamilyManager;
import com.afar.osaio.smart.electrician.smartScene.adapter.ConditionAdapter;
import com.afar.osaio.smart.electrician.smartScene.adapter.TaskAdapter;
import com.afar.osaio.smart.electrician.smartScene.presenter.CreateNewSmartPresenter;
import com.afar.osaio.smart.electrician.smartScene.presenter.ICreateNewSmartPresenter;
import com.afar.osaio.smart.electrician.smartScene.view.ICreateNewSmartView;
import com.afar.osaio.smart.electrician.util.DialogUtil;
import com.afar.osaio.smart.electrician.util.ErrorHandleUtil;
import com.afar.osaio.smart.electrician.widget.AddConditionPopupWindows;
import com.afar.osaio.smart.electrician.widget.AddTaskPopupWindows;
import com.afar.osaio.smart.electrician.widget.MetPopupWindows;
import com.afar.osaio.smart.electrician.widget.StylePopupWindows;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.scene.PreCondition;
import com.tuya.smart.home.sdk.bean.scene.PreConditionExpr;
import com.tuya.smart.home.sdk.bean.scene.SceneBean;
import com.tuya.smart.home.sdk.bean.scene.SceneCondition;
import com.tuya.smart.home.sdk.bean.scene.SceneTask;
import com.tuya.smart.home.sdk.bean.scene.condition.ConditionListBean;
import com.tuya.smart.home.sdk.bean.scene.dev.TaskListBean;
import com.tuya.smart.sdk.bean.DeviceBean;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.afar.osaio.util.ConstantValue.REQUEST_CODE_EFFECT;


public class CreateNewSmartActivity extends BaseActivity implements ICreateNewSmartView {
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.tvSceneName)
    TextView tvSceneName;
    @BindView(R.id.ivStyle)
    ImageView ivStyle;
    @BindView(R.id.rcvCondition)
    RecyclerView rcvCondition;
    @BindView(R.id.rcvTask)
    RecyclerView rcvTask;
    @BindView(R.id.clEffective)
    View clEffective;
    @BindView(R.id.ivAddCondition)
    ImageView ivAddCondition;
    @BindView(R.id.tvMet)
    TextView tvMet;
    @BindView(R.id.tvEffectiveTime)
    TextView tvEffectiveTime;
    @BindView(R.id.btnSave)
    Button btnSave;

    private List<SceneCondition> mConditionList;
    private List<SceneTask> mTaskList;
    private List<PreCondition> mPreConditionList;
    private StylePopupWindows stylePopupWindows;
    private MetPopupWindows metPopupWindows;
    private AddConditionPopupWindows addConditionPopupWindows;
    private AddTaskPopupWindows addTaskPopupWindows;
    private ConditionAdapter mConditionAdapter;
    private TaskAdapter mTaskAdapter;
    private ICreateNewSmartPresenter mPresenter;
    private String disPlayColor;
    private SceneBean mSceneBean;
    private String sceneTask;
    private String sceneCondition;

    public static void toCreateNewSmartActivity(Activity from, String sceneTask, String sceneCondition) {
        Intent intent = new Intent(from, CreateNewSmartActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENETASK, sceneTask);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENECONDITION, sceneCondition);
        from.startActivity(intent);
    }

    public static void toCreateNewSmartActivity(Activity from, String sceneTask, String sceneCondition, Serializable sceneBean) {
        Intent intent = new Intent(from, CreateNewSmartActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENETASK, sceneTask);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENECONDITION, sceneCondition);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEBEAN, sceneBean);
        from.startActivity(intent);
    }

    public static void toCreateNewSmartActivity(Activity from, Serializable sceneBean) {
        Intent intent = new Intent(from, CreateNewSmartActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEBEAN, sceneBean);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSlideable(false);
        setContentView(R.layout.activity_create_new_smart);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        initView();
        initData();
        processExtraData(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initView() {
        tvTitle.setText(getText(R.string.create_new_smart));
        ivLeft.setImageResource(R.drawable.scene_cancel);
        ivStyle.setColorFilter(Color.parseColor(ConstantValue.SMART_SCENE_COLOR_ONE));
        disPlayColor = ConstantValue.SMART_SCENE_COLOR_ONE;
    }

    private void initData() {
        mPresenter = new CreateNewSmartPresenter(this);
        mConditionList = new ArrayList<>();
        mTaskList = new ArrayList<>();
        mPreConditionList = new ArrayList<>();
        setupSceneCondition();
        setupSceneTask();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        NooieLog.e("----------CreateNewSmartActivity onNewIntent");
        processExtraData(true);
    }

    private void processExtraData(boolean isNewIntent) {
        if (getCurrentIntent() == null) {
            finish();
        } else {
            mSceneBean = (SceneBean) getCurrentIntent().getSerializableExtra(ConstantValue.INTENT_KEY_SCENEBEAN);
            sceneCondition = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SCENECONDITION);
            sceneTask = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SCENETASK);

            if (CollectionUtil.isNotEmpty(mConditionList)) {
                mConditionList.clear();
            }
            if (CollectionUtil.isNotEmpty(mTaskList)) {
                mTaskList.clear();
            }

            if (mSceneBean != null) {
                setupSceneBean(isNewIntent);
            } else {
                setConditionData();
                setTaskData();
                setupSceneName(isNewIntent);
            }
        }
    }

    private void setupSceneName(boolean isNewIntent) {
        if (!isNewIntent) {
            if (CollectionUtil.isEmpty(mConditionList)) {
                if (CollectionUtil.isNotEmpty(mTaskList)) {
                    SceneTask sceneTask = mTaskList.get(0);
                    if (sceneTask != null && !TextUtils.isEmpty(sceneTask.getActionExecutor())) {
                        if (sceneTask.getActionExecutor().equals("delay") || sceneTask.getActionExecutor().equals("dealy")) {
                            if (sceneTask.getExecutorProperty() != null) {
                                if (Integer.valueOf(sceneTask.getExecutorProperty().get("seconds").toString()) == 0) {
                                    tvSceneName.setText("Delay" + sceneTask.getExecutorProperty().get("minutes") + "min");
                                } else if (Integer.valueOf(sceneTask.getExecutorProperty().get("minutes").toString()) == 0) {
                                    tvSceneName.setText("Delay" + sceneTask.getExecutorProperty().get("seconds") + "s");
                                } else {
                                    tvSceneName.setText("Delay" + sceneTask.getExecutorProperty().get("minutes") + "min" + sceneTask.getExecutorProperty().get("seconds") + "s");
                                }
                            }
                        } else if (sceneTask.getActionExecutor().contains("rule")) {
                            if (sceneTask.getActionExecutor().equals("ruleDisable")) {
                                tvSceneName.setText("Stop using automation " + sceneTask.getEntityName());
                            } else if (sceneTask.getActionExecutor().equals("ruleEnable")) {
                                tvSceneName.setText("Start using automation " + sceneTask.getEntityName());
                            }
                        } else {
                            if (!TextUtils.isEmpty(sceneTask.getEntityId())) {
                                DeviceBean deviceBean = TuyaHomeSdk.getDataInstance().getDeviceBean(sceneTask.getEntityId());
                                tvSceneName.setText("\"" + deviceBean.getName() + "\"" + sceneTask.getEntityName());
                            }
                        }
                    }
                }
            } else {
                SceneCondition sceneCondition = mConditionList.get(0);
                if (CollectionUtil.isNotEmpty(mTaskList)) {
                    SceneTask sceneTask = mTaskList.get(0);
                    tvSceneName.setText("If " + sceneCondition.getEntityName() + " " + sceneCondition.getExprDisplay() + "," + sceneTask.getEntityName());
                }
            }
        }
    }

    private void setupSceneBean(boolean isNewIntent) {
        tvTitle.setText(R.string.scene_edit);
        ivRight.setImageResource(R.drawable.define_black);
        btnSave.setText(R.string.delete);

        if (!isNewIntent) {
            if (mSceneBean.getMatchType() == 1) {
                tvMet.setText(R.string.any_met);
            } else if (mSceneBean.getMatchType() == 2) {
                tvMet.setText(R.string.all_met);
            }

            mPreConditionList = mSceneBean.getPreConditions();
            if (CollectionUtil.isNotEmpty(mPreConditionList)) {
                PreCondition preCondition = mPreConditionList.get(0);
                if (preCondition != null) {
                    PreConditionExpr preConditionExpr = preCondition.getExpr();
                    if (preConditionExpr != null) {
                        if (preConditionExpr.getTimeInterval().equals(PreCondition.TIMEINTERVAL_ALLDAY)) {
                            tvEffectiveTime.setText(R.string.all_day);
                        } else if (preConditionExpr.getTimeInterval().equals(PreCondition.TIMEINTERVAL_DAYTIME)) {
                            tvEffectiveTime.setText(R.string.daytime);
                        } else if (preConditionExpr.getTimeInterval().equals(PreCondition.TIMEINTERVAL_NIGHT)) {
                            tvEffectiveTime.setText(R.string.at_night);
                        } else if (preConditionExpr.getTimeInterval().equals(PreCondition.TIMEINTERVAL_CUSTOM)) {
                            String[] start = preConditionExpr.getStart().split(":");
                            String[] end = preConditionExpr.getEnd().split(":");
                            int startTime = Integer.valueOf(start[0]) * 60 + Integer.valueOf(start[1]);
                            int endTime = Integer.valueOf(end[0]) * 60 + Integer.valueOf(end[1]);
                            if (startTime > endTime) {
                                tvEffectiveTime.setText(preConditionExpr.getStart() + "-" + preConditionExpr.getEnd() + " Next Day");
                            } else {
                                //tvEffectiveTime.setText(preConditionExpr.getStart() + "-" + preConditionExpr.getEnd() + " Same Day");
                                tvEffectiveTime.setText(preConditionExpr.getStart() + "-" + preConditionExpr.getEnd());
                            }
                        }
                    }
                }
            }

            tvSceneName.setText(mSceneBean.getName());

            if (mSceneBean.getDisplayColor() != null) {
                if (mSceneBean.getDisplayColor().contains("#")) {
                    disPlayColor = mSceneBean.getDisplayColor();
                } else {
                    disPlayColor = "#" + mSceneBean.getDisplayColor();
                }
                NooieLog.e("---------setupSceneBean displayColor  " + disPlayColor);
                ivStyle.setColorFilter(Color.parseColor(disPlayColor));
            }
        }

        if (TextUtils.isEmpty(sceneCondition)) {
            mConditionList = mSceneBean.getConditions();
            mTaskList = mSceneBean.getActions();
            if (CollectionUtil.isEmpty(mConditionList)) {
                tvMet.setVisibility(View.GONE);
            } else {
                ivAddCondition.setImageResource(R.drawable.scene_add);
                clEffective.setVisibility(View.VISIBLE);
            }
            mConditionAdapter.setData(mConditionList);

            if (CollectionUtil.isNotEmpty(mTaskList)) {
                mTaskAdapter.setData(mTaskList);
            }

        } else {
            if (sceneCondition.equals("null")) {
                mConditionList = mSceneBean.getConditions();
                if (CollectionUtil.isEmpty(mConditionList)) {
                    tvMet.setVisibility(View.GONE);
                } else {
                    ivAddCondition.setImageResource(R.drawable.scene_add);
                    clEffective.setVisibility(View.VISIBLE);
                }
                mConditionAdapter.setData(mConditionList);

            } else {
                mConditionList = new Gson().fromJson(sceneCondition, new TypeToken<List<SceneCondition>>() {
                }.getType());
                mConditionAdapter.setData(mConditionList);
                ivAddCondition.setImageResource(R.drawable.scene_add);
                clEffective.setVisibility(View.VISIBLE);
            }

            if (!TextUtils.isEmpty(sceneTask)) {
                mTaskList = new Gson().fromJson(sceneTask, new TypeToken<List<SceneTask>>() {
                }.getType());
                if (CollectionUtil.isNotEmpty(mTaskList)) {
                    mTaskAdapter.setData(mTaskList);
                }
            }
        }
    }

    private void setupSceneCondition() {
        mConditionAdapter = new ConditionAdapter();
        mConditionAdapter.setListener(new ConditionAdapter.ConditionItemListener() {
            @Override
            public void onItemClick(int i, SceneCondition sceneCondition) {
                NooieLog.e("-------condition " + sceneCondition);
                if (sceneCondition != null) {
                    if (sceneCondition.getEntityType() == 6) {
                        if (mSceneBean != null) {
                            ScheduleActivity.toScheduleActivity(CreateNewSmartActivity.this, new Gson().toJson(mConditionList), new Gson().toJson(mTaskList), true, i, mSceneBean);
                        } else {
                            ScheduleActivity.toScheduleActivity(CreateNewSmartActivity.this, new Gson().toJson(mConditionList), new Gson().toJson(mTaskList), true, i);
                        }
                    } else if (sceneCondition.getEntityType() == 3) {
                        mPresenter.getWeatherCondition(sceneCondition, i);
                    } else if (sceneCondition.getEntityType() == 1) {
                        mPresenter.getDevCondition(sceneCondition, i);
                    }
                }
            }
        });
        LinearLayoutManager conditionlayoutManager = new LinearLayoutManager(this);
        conditionlayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rcvCondition.setLayoutManager(conditionlayoutManager);
        rcvCondition.setAdapter(mConditionAdapter);
    }

    private void setupSceneTask() {
        mTaskAdapter = new TaskAdapter();
        mTaskAdapter.setListener(new TaskAdapter.TaskItemListener() {
            @Override
            public void onItemClick(int i, SceneTask sceneTask) {
                NooieLog.e("-------sceneTask " + sceneTask);
                if (sceneTask != null && !TextUtils.isEmpty(sceneTask.getActionExecutor())) {
                    if (sceneTask.getActionExecutor().equals("delay") || sceneTask.getActionExecutor().equals("dealy")) {
                        if (mSceneBean != null) {
                            DelayActivity.toDelayActivity(CreateNewSmartActivity.this, new Gson().toJson(mConditionList), new Gson().toJson(mTaskList), true, i, mSceneBean);
                        } else {
                            DelayActivity.toDelayActivity(CreateNewSmartActivity.this, new Gson().toJson(mConditionList), new Gson().toJson(mTaskList), true, i);
                        }
                    } else if (sceneTask.getActionExecutor().contains("rule")) {
                        if (sceneTask.getActionExecutor().equals("ruleTrigger")) {
                            if (mSceneBean != null) {
                                LaunchActivity.toLaunchActivity(CreateNewSmartActivity.this, new Gson().toJson(mConditionList), new Gson().toJson(mTaskList), true, i, mSceneBean);
                            } else {
                                LaunchActivity.toLaunchActivity(CreateNewSmartActivity.this, new Gson().toJson(mConditionList), new Gson().toJson(mTaskList), true, i);
                            }
                        } else {
                            if (mSceneBean != null) {
                                AutomationActivity.toAutomationActivity(CreateNewSmartActivity.this, new Gson().toJson(mConditionList), new Gson().toJson(mTaskList), true, i, mSceneBean);
                            } else {
                                AutomationActivity.toAutomationActivity(CreateNewSmartActivity.this, new Gson().toJson(mConditionList), new Gson().toJson(mTaskList), true, i);
                            }
                        }
                    } else {
                        if (sceneTask != null) {
                            mPresenter.getDevTask(sceneTask, i);
                        }
                    }
                }
            }
        });
        LinearLayoutManager tasklayoutManager = new LinearLayoutManager(this);
        tasklayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rcvTask.setLayoutManager(tasklayoutManager);
        rcvTask.setAdapter(mTaskAdapter);
    }

    private void setConditionData() {
        if (TextUtils.isEmpty(sceneCondition)) {
            tvMet.setVisibility(View.GONE);
        } else {
            if (sceneCondition.equals("null")) {
                tvMet.setVisibility(View.GONE);
            } else {
                ivAddCondition.setImageResource(R.drawable.scene_add);
                clEffective.setVisibility(View.VISIBLE);
                mConditionList = new Gson().fromJson(sceneCondition, new TypeToken<List<SceneCondition>>() {
                }.getType());
            }
        }
        mConditionAdapter.setData(mConditionList);
    }

    private void setTaskData() {
        mTaskList = new Gson().fromJson(sceneTask, new TypeToken<List<SceneTask>>() {
        }.getType());
        if (CollectionUtil.isNotEmpty(mTaskList)) {
            mTaskAdapter.setData(mTaskList);
        }
    }

    private void showStylePopMenu() {
        if (stylePopupWindows != null) {
            stylePopupWindows.dismiss();
        }

        stylePopupWindows = new StylePopupWindows(CreateNewSmartActivity.this, disPlayColor);

        stylePopupWindows.showAtLocation(findViewById(R.id.clCreateSmart),
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    private void showMetPopMenu() {
        if (metPopupWindows != null) {
            metPopupWindows.dismiss();
        }

        metPopupWindows = new MetPopupWindows(CreateNewSmartActivity.this);

        metPopupWindows.setListener(new MetPopupWindows.MetListener() {
            @Override
            public void onAllMetClick() {
                if (CollectionUtil.isNotEmpty(mConditionList)) {
                    int count = 0;
                    for (SceneCondition sceneCondition : mConditionList) {
                        if (sceneCondition.getEntityType() == 6) {
                            count = count + 1;
                            if (count == 2) {
                                ToastUtil.showToast(CreateNewSmartActivity.this, R.string.condition_not_support);
                                return;
                            }
                        }
                    }
                }
                tvMet.setText(R.string.all_met);
            }

            @Override
            public void onAnyMetClick() {
                tvMet.setText(R.string.any_met);
            }
        });

        metPopupWindows.showAtLocation(findViewById(R.id.clCreateSmart),
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    private void showAddConditionPopMenu() {
        if (addConditionPopupWindows != null) {
            addConditionPopupWindows.dismiss();
        }

        addConditionPopupWindows = new AddConditionPopupWindows(CreateNewSmartActivity.this);

        addConditionPopupWindows.setListener(new AddConditionPopupWindows.AddConditionListener() {
            @Override
            public void onWeatherChangeClick() {
                if (mSceneBean != null) {
                    WeatherActivity.toWeatherActivity(CreateNewSmartActivity.this, new Gson().toJson(mConditionList), new Gson().toJson(mTaskList), true, mSceneBean);
                } else {
                    WeatherActivity.toWeatherActivity(CreateNewSmartActivity.this, new Gson().toJson(mConditionList), new Gson().toJson(mTaskList), true);
                }
            }

            @Override
            public void onScheduleClick() {
                if (CollectionUtil.isNotEmpty(mConditionList)) {
                    for (SceneCondition sceneCondition : mConditionList) {
                        if (sceneCondition.getEntityType() == 6 && tvMet.getText().toString().equals(NooieApplication.mCtx.getResources().getString(R.string.all_met))) {
                            ToastUtil.showToast(CreateNewSmartActivity.this, R.string.condition_conflict_tip);
                            return;
                        }
                    }
                }
                if (mSceneBean != null) {
                    ScheduleActivity.toScheduleActivity(CreateNewSmartActivity.this, new Gson().toJson(mConditionList), new Gson().toJson(mTaskList), true, mSceneBean);
                } else {
                    ScheduleActivity.toScheduleActivity(CreateNewSmartActivity.this, new Gson().toJson(mConditionList), new Gson().toJson(mTaskList), true);
                }
            }

            @Override
            public void onDeviceChangeClick() {
                if (mSceneBean != null) {
                    DeviceChangeActivity.toDeviceChangeActivity(CreateNewSmartActivity.this, new Gson().toJson(mConditionList), new Gson().toJson(mTaskList), true, mSceneBean);
                } else {
                    DeviceChangeActivity.toDeviceChangeActivity(CreateNewSmartActivity.this, new Gson().toJson(mConditionList), new Gson().toJson(mTaskList), true);
                }
            }
        });

        addConditionPopupWindows.showAtLocation(findViewById(R.id.clCreateSmart),
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    private void showAddTaskPopMenu() {
        if (addTaskPopupWindows != null) {
            addTaskPopupWindows.dismiss();
        }

        addTaskPopupWindows = new AddTaskPopupWindows(CreateNewSmartActivity.this);

        addTaskPopupWindows.setListener(new AddTaskPopupWindows.AddTaskListener() {
            @Override
            public void onRunDeviceClick() {
                if (mSceneBean != null) {
                    RunDeviceActivity.toRunDeviceActivity(CreateNewSmartActivity.this, new Gson().toJson(mConditionList), new Gson().toJson(mTaskList), true, mSceneBean);
                } else {
                    RunDeviceActivity.toRunDeviceActivity(CreateNewSmartActivity.this, new Gson().toJson(mConditionList), new Gson().toJson(mTaskList), true);
                }
            }

            @Override
            public void onSelectSmartClick() {
                if (mSceneBean != null) {
                    SelectSmartActivity.toSelectSmartActivity(CreateNewSmartActivity.this, new Gson().toJson(mConditionList), new Gson().toJson(mTaskList), true, mSceneBean);
                } else {
                    SelectSmartActivity.toSelectSmartActivity(CreateNewSmartActivity.this, new Gson().toJson(mConditionList), new Gson().toJson(mTaskList), true);
                }
            }

            @Override
            public void onDelayClick() {
                if (mSceneBean != null) {
                    DelayActivity.toDelayActivity(CreateNewSmartActivity.this, new Gson().toJson(mConditionList), new Gson().toJson(mTaskList), true, mSceneBean);
                } else {
                    DelayActivity.toDelayActivity(CreateNewSmartActivity.this, new Gson().toJson(mConditionList), new Gson().toJson(mTaskList), true);
                }
            }
        });

        addTaskPopupWindows.showAtLocation(findViewById(R.id.clCreateSmart),
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStyleEvent(StyleEvent styleEvent) {
        NooieLog.e("---------StyleEvent color  " + styleEvent.getStyleColor());
        disPlayColor = styleEvent.getStyleColor();
        ivStyle.setColorFilter(Color.parseColor(styleEvent.getStyleColor()));
    }

    @OnClick({R.id.ivLeft, R.id.ivAddCondition, R.id.ivAddTask, R.id.clName, R.id.clStyle, R.id.clEffective, R.id.btnSave, R.id.tvMet, R.id.ivRight})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft: {
               /* if (mSceneBean != null) {
                    finish();
                } else {
                    showDiscardDialog();
                }*/
                showDiscardDialog();
                break;
            }
            case R.id.ivAddCondition: {
                if (CollectionUtil.isEmpty(mConditionList)) {
                    ToastUtil.showToast(this, R.string.condition_cannot_be_added);
                } else {
                    showAddConditionPopMenu();
                }
                break;
            }
            case R.id.ivAddTask: {
                showAddTaskPopMenu();
                break;
            }
            case R.id.clName: {
                DialogUtil.showReNameDialog(this, R.string.cancel, R.string.save_upper_case, tvSceneName.getText().toString(), new DialogUtil.OnClickInputDialogListener() {
                    @Override
                    public void onClickCancel() {

                    }

                    @Override
                    public void onClickSave(String text) {
                        tvSceneName.setText(text);
                    }
                });
                break;
            }
            case R.id.clStyle: {
                showStylePopMenu();
                break;
            }
            case R.id.clEffective: {
                if (CollectionUtil.isEmpty(mPreConditionList)) {
                    EffectiveActivity.toEffectiveActivity(this, REQUEST_CODE_EFFECT);
                } else {
                    PreCondition preCondition = mPreConditionList.get(0);
                    if (preCondition != null) {
                        PreConditionExpr preConditionExpr = preCondition.getExpr();
                        if (preCondition != null) {
                            EffectiveActivity.toEffectiveActivity(this, REQUEST_CODE_EFFECT, preConditionExpr);
                        } else {
                            EffectiveActivity.toEffectiveActivity(this, REQUEST_CODE_EFFECT);
                        }
                    }
                }
                break;
            }
            case R.id.btnSave: {
                if (mSceneBean != null) {
                    showDeleteSceneDialog(mSceneBean);
                } else {
                    if (mTaskList.get(mTaskList.size() - 1).getActionExecutor().equals("delay") || mTaskList.get(mTaskList.size() - 1).getActionExecutor().equals("dealy")) {
                        ToastUtil.showToast(this, R.string.delay_last_tip);
                        return;
                    }
                    if (tvMet.getText().toString().equals(getResources().getString(R.string.any_met))) {
                        mPresenter.createScene(FamilyManager.getInstance().getCurrentHomeId(), tvSceneName.getText().toString(), disPlayColor, mConditionList, mTaskList, mPreConditionList, SceneBean.MATCH_TYPE_OR);
                    } else {
                        mPresenter.createScene(FamilyManager.getInstance().getCurrentHomeId(), tvSceneName.getText().toString(), disPlayColor, mConditionList, mTaskList, mPreConditionList, SceneBean.MATCH_TYPE_AND);
                    }
                }
                break;
            }
            case R.id.tvMet: {
                showMetPopMenu();
                break;
            }
            case R.id.ivRight: {
                if (mSceneBean != null) {
                    mSceneBean.setName(tvSceneName.getText().toString());  //更改场景名称
                    mSceneBean.setDisplayColor(disPlayColor);
                    mSceneBean.setConditions(mConditionList); //更改场景条件
                    mSceneBean.setActions(mTaskList); //更改场景动作
                    mSceneBean.setPreConditions(mPreConditionList);
                    if (tvMet.getText().toString().equals(getResources().getString(R.string.any_met))) {
                        mSceneBean.setMatchType(SceneBean.MATCH_TYPE_OR);
                    } else {
                        mSceneBean.setMatchType(SceneBean.MATCH_TYPE_AND);
                    }
                    if (CollectionUtil.isNotEmpty(mTaskList)) {
                        if (mTaskList.get(mTaskList.size() - 1).getActionExecutor().equals("delay") || mTaskList.get(mTaskList.size() - 1).getActionExecutor().equals("dealy")) {
                            ToastUtil.showToast(this, R.string.delay_last_tip);
                            return;
                        }
                    }
                  /*  if (CollectionUtil.isEmpty(mConditionList) || CollectionUtil.isEmpty(mTaskList)) {
                        TeckinSmartSceneActivity.toTeckinSmartSceneActivity(this);
                        return;
                    }*/
                    mPresenter.modifyScene(mSceneBean.getId(), mSceneBean);
                }
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
       /* if (mSceneBean != null) {
            finish();
        } else {
            showDiscardDialog();
        }*/
        showDiscardDialog();
    }

    private void showDiscardDialog() {
        DialogUtil.showConfirmDialog(this, R.string.discard_change, R.string.no, R.string.discard, new DialogUtil.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                TeckinSmartSceneActivity.toTeckinSmartSceneActivity(CreateNewSmartActivity.this);
            }

            @Override
            public void onClickLeft() {

            }
        });
    }

    private void showDeleteSceneDialog(final SceneBean sceneBean) {
        DialogUtil.showConfirmDialog(this, NooieApplication.mCtx.getResources().getString(R.string.remove_scene) + sceneBean.getName(), R.string.cancel, R.string.confirm_upper, true, new DialogUtil.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                mPresenter.deleteScene(sceneBean.getId());
            }

            @Override
            public void onClickLeft() {

            }
        });
    }

    @Override
    public void notifyCreateSceneSuccess(SceneBean sceneBean) {
        NooieLog.e("----------notifyCreateSceneSuccess SceneBean " + sceneBean.toString());
        mPresenter.enableScene(sceneBean.getId());
    }

    @Override
    public void notifyCreateSceneFailed(String errorCode, String errorMsg) {
        NooieLog.e("------------createScene  errorCode  " + errorCode + " errorMsg  " + errorMsg);
        ErrorHandleUtil.toastTuyaError(this, errorMsg);
    }

    @Override
    public void notifyDeleteSceneSuccess() {
        TeckinSmartSceneActivity.toTeckinSmartSceneActivity(this);
    }

    @Override
    public void notifyDeleteSceneFailed(String errorCode, String errorMsg) {
        NooieLog.e("------------deleteScene  errorCode  " + errorCode + " errorMsg  " + errorMsg);
        ErrorHandleUtil.toastTuyaError(this, errorMsg);
    }

    @Override
    public void notifyModifySceneSuccess(SceneBean sceneBean) {
        NooieLog.e("----------notifyModifySceneSuccess SceneBean " + sceneBean.toString());
        TeckinSmartSceneActivity.toTeckinSmartSceneActivity(this);
    }

    @Override
    public void notifyModifySceneFailed(String errorCode, String errorMsg) {
        NooieLog.e("------------modify Scene errorCode  " + errorCode + " errorMsg  " + errorMsg);
        ErrorHandleUtil.toastTuyaError(this, errorMsg);
    }

    @Override
    public void notifyGetDeviceTaskListSuccess(List<TaskListBean> result, SceneTask sceneTask, int position) {
        String dpId = "1";
        if (sceneTask.getExecutorProperty() != null) {
            for (Map.Entry<String, Object> entry : sceneTask.getExecutorProperty().entrySet()) {
                NooieLog.e("setupSceneTask entry  " + entry.getKey() + " value " + entry.getValue());
                dpId = entry.getKey();
            }
        }
        for (TaskListBean taskListBean : result) {
            if (String.valueOf(taskListBean.getDpId()).equals(dpId)) {
                if (mSceneBean != null) {
                    DeviceDpActivity.toDeviceDpActivity(this, new Gson().toJson(taskListBean), sceneTask.getEntityId(), "", true, new Gson().toJson(mConditionList), new Gson().toJson(mTaskList), true, position, mSceneBean);
                } else {
                    DeviceDpActivity.toDeviceDpActivity(this, new Gson().toJson(taskListBean), sceneTask.getEntityId(), "", true, new Gson().toJson(mConditionList), new Gson().toJson(mTaskList), true, position);
                }
            }
        }
    }

    @Override
    public void notifyGetDeviceTaskListSuccess(List<TaskListBean> result, SceneCondition sceneCondition, int position) {
        String dpId = "1";
        if (!TextUtils.isEmpty(sceneCondition.getEntitySubIds())) {
            dpId = sceneCondition.getEntitySubIds();
        }
        for (TaskListBean taskListBean : result) {
            if (String.valueOf(taskListBean.getDpId()).equals(dpId)) {
                if (mSceneBean != null) {
                    DeviceDpActivity.toDeviceDpActivity(this, new Gson().toJson(taskListBean), sceneCondition.getEntityId(), "", false, new Gson().toJson(mConditionList), new Gson().toJson(mTaskList), true, position, mSceneBean);
                } else {
                    DeviceDpActivity.toDeviceDpActivity(this, new Gson().toJson(taskListBean), sceneCondition.getEntityId(), "", false, new Gson().toJson(mConditionList), new Gson().toJson(mTaskList), true, position);
                }
            }
        }
    }

    @Override
    public void notifyGetDeviceTaskListFail(String errorMsg) {
        ErrorHandleUtil.toastTuyaError(this, errorMsg);
    }

    @Override
    public void notifyGetConditionListSuccess(List<ConditionListBean> conditionList, SceneCondition sceneCondition, int position) {
        for (ConditionListBean conditionListBean : conditionList) {
            if (conditionListBean.getType().equals(sceneCondition.getEntitySubIds())) {
                if (mSceneBean != null) {
                    SelectWeatherActivity.toSelectWeatherActivity(this, conditionListBean, new Gson().toJson(mConditionList), new Gson().toJson(mTaskList), true, position, mSceneBean);
                } else {
                    SelectWeatherActivity.toSelectWeatherActivity(this, conditionListBean, new Gson().toJson(mConditionList), new Gson().toJson(mTaskList), true, position);
                }
            }
        }
    }

    @Override
    public void notifyGetConditionListFail(String errorMsg) {
        ErrorHandleUtil.toastTuyaError(this, errorMsg);
    }

    @Override
    public void notifyEnableScene() {
        TeckinSmartSceneActivity.toTeckinSmartSceneActivity(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case REQUEST_CODE_EFFECT:
                    String precondition = data.getStringExtra(ConstantValue.INTENT_KEY_PRECONDITION);
                    String effectTime = data.getStringExtra(ConstantValue.INTENT_KEY_SCENE_EFFECT);
                    tvEffectiveTime.setText(effectTime);
                    if (CollectionUtil.isNotEmpty(mPreConditionList)) {
                        mPreConditionList.clear();
                    }
                    mPreConditionList = new Gson().fromJson(precondition, new TypeToken<List<PreCondition>>() {
                    }.getType());
                    break;
            }
        }
    }
}


