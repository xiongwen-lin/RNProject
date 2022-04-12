package com.afar.osaio.smart.electrician.smartScene.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.electrician.smartScene.adapter.HourWheelAdapter;
import com.afar.osaio.smart.electrician.smartScene.adapter.MinutesWheelAdapter;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.contrarywind.listener.OnItemSelectedListener;
import com.contrarywind.view.WheelView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.scene.SceneBean;
import com.tuya.smart.home.sdk.bean.scene.SceneTask;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DelayActivity extends BaseActivity {
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.wheelStartHour)
    WheelView wheelStartHour;
    @BindView(R.id.wheelStartMinutes)
    WheelView wheelStartMinutes;
    @BindView(R.id.wheelStartSecond)
    WheelView wheelStartSecond;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;

    private HourWheelAdapter mHourWheelAdapter;
    private MinutesWheelAdapter mMinutesWheelAdapter;
    private MinutesWheelAdapter mSecondWheelAdapter;
    private int minute;
    private int second;
    private List<SceneTask> taskList;
    private String sceneCondition;
    private String sceneTask;
    private boolean isAdd;
    private boolean isModify;
    private SceneBean mSceneBean;
    private int position;

    public static void toDelayActivity(Activity from, String sceneCondition, String sceneTask, boolean isAdd) {
        Intent intent = new Intent(from, DelayActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENECONDITION, sceneCondition);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENETASK, sceneTask);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEE_ADD, isAdd);
        from.startActivity(intent);
    }

    public static void toDelayActivity(Activity from, String sceneCondition, String sceneTask, boolean isAdd, Serializable sceneBean) {
        Intent intent = new Intent(from, DelayActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENECONDITION, sceneCondition);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENETASK, sceneTask);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEE_ADD, isAdd);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEBEAN, sceneBean);
        from.startActivity(intent);
    }

    public static void toDelayActivity(Activity from, String sceneCondition, String sceneTask, boolean isModify, int position) {
        Intent intent = new Intent(from, DelayActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENECONDITION, sceneCondition);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENETASK, sceneTask);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEE_MODIFY, isModify);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEE_POSITION, position);
        from.startActivity(intent);
    }

    public static void toDelayActivity(Activity from, String sceneCondition, String sceneTask, boolean isModify, int position, Serializable sceneBean) {
        Intent intent = new Intent(from, DelayActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENECONDITION, sceneCondition);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENETASK, sceneTask);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEE_MODIFY, isModify);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEE_POSITION, position);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEBEAN, sceneBean);
        from.startActivity(intent);
    }

    public static void toDelayActivity(Activity from, String sceneCondition) {
        Intent intent = new Intent(from, DelayActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENECONDITION, sceneCondition);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSlideable(false);
        setContentView(R.layout.activity_delay);
        ButterKnife.bind(this);
        initView();
        initData();
        processExtraData();
    }

    private void initView() {
        tvTitle.setText(getText(R.string.delay));
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        ivRight.setImageResource(R.drawable.define_black);
        setupFromAndToTime();
    }

    private void initData() {
        taskList = new ArrayList<>();
    }

    private void processExtraData() {
        if (getCurrentIntent() == null) {
            finish();
        } else {
            sceneCondition = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SCENECONDITION);
            sceneTask = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SCENETASK);
            isAdd = getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_SCENEE_ADD, false);
            mSceneBean = (SceneBean) getCurrentIntent().getSerializableExtra(ConstantValue.INTENT_KEY_SCENEBEAN);
            isModify = getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_SCENEE_MODIFY, false);
            position = getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_SCENEE_POSITION, 0);
            taskList.clear();

            wheelStartHour.setCurrentItem(0);
            wheelStartMinutes.setCurrentItem(0);
            wheelStartSecond.setCurrentItem(0);

            if (!TextUtils.isEmpty(sceneTask)) {
                List<SceneTask> sceneTasks = new Gson().fromJson(sceneTask, new TypeToken<List<SceneTask>>() {
                }.getType());
                if (CollectionUtil.isEmpty(sceneTasks)) {
                    return;
                }
                taskList.addAll(sceneTasks);

                if (isModify) {
                    SceneTask task = taskList.get(position);
                    wheelStartSecond.setCurrentItem(Integer.valueOf(task.getExecutorProperty().get("seconds").toString()));
                    int min = Integer.valueOf(task.getExecutorProperty().get("minutes").toString());
                    NooieLog.e("-----------minutest " + min);
                    wheelStartMinutes.setCurrentItem((min % 60));
                    wheelStartHour.setCurrentItem((min / 60));
                }
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        NooieLog.e("----------DelayActivity onNewIntent");
        processExtraData();
    }

    private void setupFromAndToTime() {

        mHourWheelAdapter = new HourWheelAdapter();
        mMinutesWheelAdapter = new MinutesWheelAdapter();
        mSecondWheelAdapter = new MinutesWheelAdapter();

        wheelStartHour.setAdapter(mHourWheelAdapter);
        wheelStartHour.setCurrentItem(0);

        wheelStartMinutes.setAdapter(mMinutesWheelAdapter);
        wheelStartMinutes.setCurrentItem(0);

        wheelStartSecond.setAdapter(mSecondWheelAdapter);
        wheelStartSecond.setCurrentItem(0);

        wheelStartHour.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                NooieLog.e("---------DelayActivity hour index  " + index);
                if (index == 5) {
                    wheelStartMinutes.setCurrentItem(0);
                    wheelStartSecond.setCurrentItem(0);
                }
            }
        });

        wheelStartMinutes.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                if (wheelStartHour.getCurrentItem() == 5) {
                    wheelStartMinutes.setCurrentItem(0);
                    wheelStartSecond.setCurrentItem(0);
                    ToastUtil.showToast(DelayActivity.this, "can't more than 300min");
                }
            }
        });

        wheelStartSecond.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                if (wheelStartHour.getCurrentItem() == 5) {
                    wheelStartMinutes.setCurrentItem(0);
                    wheelStartSecond.setCurrentItem(0);
                    ToastUtil.showToast(DelayActivity.this, "can't more than 300min");
                }
            }
        });
    }

    private int getCurrentH() {
        int currentH = 0;
        if (wheelStartHour != null && mHourWheelAdapter != null) {
            currentH = mHourWheelAdapter.getValue(wheelStartHour.getCurrentItem());
        }
        return currentH;
    }

    private int getCurrentM() {
        int currentM = 0;
        if (wheelStartMinutes != null && mMinutesWheelAdapter != null) {
            currentM = mMinutesWheelAdapter.getValue(wheelStartMinutes.getCurrentItem());
        }
        return currentM;
    }

    private int getCurrentS() {
        int currentS = 0;
        if (wheelStartSecond != null && mSecondWheelAdapter != null) {
            currentS = mSecondWheelAdapter.getValue(wheelStartSecond.getCurrentItem());
        }
        return currentS;
    }

    @OnClick({R.id.ivLeft, R.id.ivRight})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft: {
                finish();
                break;
            }
            case R.id.ivRight: {
                doTime();
                if (minute == 0 && second == 0) {
                    ToastUtil.showToast(this, R.string.set_time_delay);
                    return;
                }
                if ((minute * 60 + second) > 18000) {
                    ToastUtil.showToast(this, "can't more than 300min");
                    return;
                }
                SceneTask sceneTask = TuyaHomeSdk.getSceneManagerInstance().createDelayTask(minute, second);
                if (minute == 0) {
                    sceneTask.setEntityName("Delay" + second + "s");
                } else if (second == 0) {
                    sceneTask.setEntityName("Delay" + minute + "min");
                } else {
                    sceneTask.setEntityName("Delay" + minute + "min" + second + "s");
                }
                if (isModify) {
                    taskList.set(position, sceneTask);
                } else {
                    taskList.add(sceneTask);
                }
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

    private void doTime() {
        minute = getCurrentH() * 60 + getCurrentM();
        second = getCurrentS();
        NooieLog.e("-----------minute  " + minute + "  second  " + second);
    }
}

