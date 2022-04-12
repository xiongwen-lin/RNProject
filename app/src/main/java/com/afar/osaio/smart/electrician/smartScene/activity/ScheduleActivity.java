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
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.electrician.smartScene.adapter.HourWheelAdapter;
import com.afar.osaio.smart.electrician.smartScene.adapter.MinutesWheelAdapter;
import com.afar.osaio.smart.electrician.smartScene.adapter.ScheduleHourWheelAdapter;
import com.afar.osaio.smart.electrician.util.DateUtil;
import com.afar.osaio.smart.setting.adapter.AllHourWheelAdapter;
import com.afar.osaio.util.ConstantValue;
import com.contrarywind.listener.OnItemSelectedListener;
import com.contrarywind.view.WheelView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.home.sdk.bean.scene.SceneBean;
import com.tuya.smart.home.sdk.bean.scene.SceneCondition;
import com.tuya.smart.home.sdk.bean.scene.condition.rule.TimerRule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.afar.osaio.util.ConstantValue.REQUEST_CODE_REPEAT;

public class ScheduleActivity extends BaseActivity {
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.wheelStartHour)
    WheelView wheelStartHour;
    @BindView(R.id.wheelStartMinutes)
    WheelView wheelStartMinutes;
    @BindView(R.id.tvFrequency)
    TextView tvFrequency;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;

    private ScheduleHourWheelAdapter mHourWheelAdapter;
    private MinutesWheelAdapter mMinutesWheelAdapter;
    private StringBuffer sbValue;
    private String week;
    private List<SceneCondition> conditionList;
    private String sceneCondition;
    private String sceneTask;
    private boolean isAdd;
    private boolean isModify;
    private int position;
    private SceneBean mSceneBean;

    public static void toScheduleActivity(Activity from) {
        Intent intent = new Intent(from, ScheduleActivity.class);
        from.startActivity(intent);
    }

    public static void toScheduleActivity(Activity from, String sceneCondition, String sceneTask, boolean isAdd, Serializable sceneBean) {
        Intent intent = new Intent(from, ScheduleActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENECONDITION, sceneCondition);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENETASK, sceneTask);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEE_ADD, isAdd);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEBEAN, sceneBean);
        from.startActivity(intent);
    }

    public static void toScheduleActivity(Activity from, String sceneCondition, String sceneTask, boolean isAdd) {
        Intent intent = new Intent(from, ScheduleActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENECONDITION, sceneCondition);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENETASK, sceneTask);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEE_ADD, isAdd);
        from.startActivity(intent);
    }

    public static void toScheduleActivity(Activity from, String sceneCondition, String sceneTask, boolean isModify, int position) {
        Intent intent = new Intent(from, ScheduleActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENECONDITION, sceneCondition);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENETASK, sceneTask);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEE_MODIFY, isModify);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEE_POSITION, position);
        from.startActivity(intent);
    }

    public static void toScheduleActivity(Activity from, String sceneCondition, String sceneTask, boolean isModify, int position, Serializable sceneBean) {
        Intent intent = new Intent(from, ScheduleActivity.class);
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
        setSlideable(false);
        setContentView(R.layout.activity_scene_schedule);
        ButterKnife.bind(this);
        initView();
        initData();
        processExtraData();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(getText(R.string.schedule));
        sbValue = new StringBuffer();
        ivRight.setImageResource(R.drawable.define_black);
        setupFromAndToTime();
    }

    private void initData() {
        conditionList = new ArrayList<>();
    }

    private void processExtraData() {
        if (getCurrentIntent() == null) {
            finish();
        } else {
            mSceneBean = (SceneBean) getCurrentIntent().getSerializableExtra(ConstantValue.INTENT_KEY_SCENEBEAN);
            sceneCondition = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SCENECONDITION);
            sceneTask = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SCENETASK);
            isAdd = getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_SCENEE_ADD, false);
            isModify = getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_SCENEE_MODIFY, false);
            position = getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_SCENEE_POSITION, 0);
            if (CollectionUtil.isNotEmpty(conditionList)) {
                conditionList.clear();
            }
            if (!TextUtils.isEmpty(sceneCondition)) {
                List<SceneCondition> sceneConditions = new Gson().fromJson(sceneCondition, new TypeToken<List<SceneCondition>>() {
                }.getType());
                if (CollectionUtil.isEmpty(sceneConditions)){
                    return;
                }
                conditionList.addAll(sceneConditions);

                if (isModify) {
                    SceneCondition sceneCondition = conditionList.get(position);
                    List<Object> list = sceneCondition.getExpr();

                    for (Object o : list) {
                        String obj = new Gson().toJson(o);
                        Map<String, String> map = new Gson().fromJson(obj, new TypeToken<Map<String, String>>() {
                        }.getType());
                        NooieLog.e("---------schedule map " + map);
                        for (Map.Entry<String, String> entry : map.entrySet()) {
                            NooieLog.e("---------schedule key:" + entry.getKey() + "  value:" + entry.getValue());
                            if (entry.getKey().equals("loops")) {
                                StringBuffer buffer = new StringBuffer();
                                if (!TextUtils.isEmpty(entry.getValue()) && entry.getValue().length() == 7) {
                                    char[] chars = entry.getValue().toCharArray();
                                    for (int i = 0; i < chars.length; i++) {
                                        if (i == 0 && chars[i] == '1') {
                                            buffer.append(NooieApplication.mCtx.getResources().getString(R.string.sun) + " ");
                                        }

                                        if (i == 1 && chars[i] == '1') {
                                            buffer.append(NooieApplication.mCtx.getResources().getString(R.string.mon) + " ");
                                        }

                                        if (i == 2 && chars[i] == '1') {
                                            buffer.append(NooieApplication.mCtx.getResources().getString(R.string.tues) + " ");
                                        }

                                        if (i == 3 && chars[i] == '1') {
                                            buffer.append(NooieApplication.mCtx.getResources().getString(R.string.wed) + " ");
                                        }

                                        if (i == 4 && chars[i] == '1') {
                                            buffer.append(NooieApplication.mCtx.getResources().getString(R.string.thurs) + " ");
                                        }

                                        if (i == 5 && chars[i] == '1') {
                                            buffer.append(NooieApplication.mCtx.getResources().getString(R.string.fri) + " ");
                                        }

                                        if (i == 6 && chars[i] == '1') {
                                            buffer.append(NooieApplication.mCtx.getResources().getString(R.string.sat));
                                        }
                                    }
                                    NooieLog.e("----------loops " + buffer);
                                    if (entry.getValue().equals("0000000")) {
                                        tvFrequency.setText(R.string.once);
                                    } else if (entry.getValue().equals("1111111")) {
                                        tvFrequency.setText(R.string.every_day);
                                    } else {
                                        week = entry.getValue();
                                        tvFrequency.setText(buffer.toString());
                                    }
                                }
                            }
                            if (entry.getKey().equals("time")) {
                                String[] time = entry.getValue().split(":");
                                wheelStartHour.setCurrentItem(Integer.valueOf(time[0]));
                                wheelStartMinutes.setCurrentItem(Integer.valueOf(time[1]));
                            }
                        }
                    }
                }
            }
        }
    }

    private void setupFromAndToTime() {

        mHourWheelAdapter = new ScheduleHourWheelAdapter();
        mMinutesWheelAdapter = new MinutesWheelAdapter();

        wheelStartHour.setAdapter(mHourWheelAdapter);
        wheelStartHour.setCurrentItem(DateUtil.getHour());

        wheelStartMinutes.setAdapter(mMinutesWheelAdapter);
        wheelStartMinutes.setCurrentItem(DateUtil.getMinute());

        wheelStartHour.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
            }
        });

        wheelStartMinutes.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        NooieLog.e("----------ScheduleActivity onNewIntent");
        processExtraData();
    }

    @OnClick({R.id.ivLeft, R.id.clRepeat, R.id.ivRight})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft: {
                finish();
                break;
            }
            case R.id.clRepeat: {
                if (!TextUtils.isEmpty(week)) {
                    RepeatActivity.toRepeatActivity(this, REQUEST_CODE_REPEAT, false, week);
                } else {
                    RepeatActivity.toRepeatActivity(this, REQUEST_CODE_REPEAT, false);
                }
                break;
            }
            case R.id.ivRight: {
                String time = addZeroForNum(String.valueOf(getCurrentH()), 2) + ":" + addZeroForNum(String.valueOf(getCurrentM()), 2);
                String date = DateUtil.getYear() + addZeroForNum(String.valueOf(DateUtil.getMonth()), 2) + addZeroForNum(String.valueOf(DateUtil.getDay()), 2);
                NooieLog.e("-------currentTime  " + time + " date " + date);
                if (TextUtils.isEmpty(week)) {
                    week = "0000000";
                }
                TimerRule timerRule = TimerRule.newInstance(TimeZone.getDefault().getID(), week, time, date);
                if (TextUtils.isEmpty(sbValue)) {
                    sbValue.append(addZeroForNum(String.valueOf(DateUtil.getMonth()), 2)).append("/").append(addZeroForNum(String.valueOf(DateUtil.getDay()), 2));
                }
                SceneCondition timerCondition = SceneCondition.createTimerCondition(
                        sbValue.toString(),
                        "Schedule",
                        "timer",
                        timerRule
                );

                timerCondition.setEntityName("Schedule:" + time);
                timerCondition.setExprDisplay(date);
                if (isModify) {
                    conditionList.set(position, timerCondition);
                }else {
                    conditionList.add(timerCondition);
                }
                if (isAdd || isModify) {
                    if (mSceneBean != null) {
                        CreateNewSmartActivity.toCreateNewSmartActivity(this, sceneTask, new Gson().toJson(conditionList), mSceneBean);
                        break;
                    } else {
                        CreateNewSmartActivity.toCreateNewSmartActivity(this, sceneTask, new Gson().toJson(conditionList));
                        break;
                    }
                } else {
                    CreateSceneActivity.toCreateSceneActivity(this, true, new Gson().toJson(conditionList));
                }
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case REQUEST_CODE_REPEAT:
                    ArrayList<String> selectDate = data.getStringArrayListExtra(ConstantValue.INTENT_KEY_REPEAT);
                    week = data.getStringExtra(ConstantValue.INTENT_KEY_REPEAT_VALUE);
                    sbValue = new StringBuffer();
                    for (String date : selectDate) {
                        sbValue.append(date).append(" ");
                    }
                    tvFrequency.setText(sbValue);
                    if (selectDate.size() == 7) {
                        tvFrequency.setText(R.string.every_day);
                    }
                    if (selectDate.size() == 0) {
                        tvFrequency.setText(R.string.once);
                    }
                    NooieLog.e("--------------选中的星期  " + sbValue.toString() + " 选中的key " + week);
                    break;
            }
        }
    }

    //左边补零
    public String addZeroForNum(String str, int strLength) {
        int strLen = str.length();
        if (strLen < strLength) {
            while (strLen < strLength) {
                StringBuffer sb = new StringBuffer();
                sb.append("0").append(str);// 左补0
                str = sb.toString();
                strLen = str.length();
            }
        }
        return str;
    }
}

