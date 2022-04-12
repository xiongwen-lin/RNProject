package com.afar.osaio.smart.electrician.smartScene.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.util.ConstantValue;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreateSceneActivity extends BaseActivity {

    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.clSetCondition)
    View clSetCondition;
    @BindView(R.id.clSetTask)
    View clSetTask;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;

    private boolean isCondition;
    private String sceneCondition;

    public static void toCreateSceneActivity(Activity from, boolean isCondition, String sceneCondition) {
        Intent intent = new Intent(from, CreateSceneActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_IS_CONDITION, isCondition);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENECONDITION, sceneCondition);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSlideable(false);
        setContentView(R.layout.activity_create_smart_scene);
        ButterKnife.bind(this);
        initView();
        initData();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(getString(R.string.create_smart));
        if (isCondition) {
            clSetCondition.setVisibility(View.GONE);
            clSetTask.setVisibility(View.VISIBLE);
        }
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
        } else {
            isCondition = getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_IS_CONDITION, false);
            sceneCondition = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SCENECONDITION);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        isCondition = intent.getBooleanExtra(ConstantValue.INTENT_KEY_IS_CONDITION, false);
        sceneCondition = intent.getStringExtra(ConstantValue.INTENT_KEY_SCENECONDITION);
        if (isCondition) {
            clSetCondition.setVisibility(View.GONE);
            clSetTask.setVisibility(View.VISIBLE);
        }
    }

    @OnClick({R.id.ivLeft, R.id.clLaunch, R.id.clWeather, R.id.clSchedule, R.id.clDeviceChange, R.id.clRunDevice, R.id.clSelectSmart, R.id.clDelay})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft: {
                finish();
                break;
            }
            case R.id.clLaunch: {
                clSetCondition.setVisibility(View.GONE);
                clSetTask.setVisibility(View.VISIBLE);
                break;
            }
            case R.id.clWeather: {
                WeatherActivity.toWeatherActivity(this);
                break;
            }
            case R.id.clSchedule: {
                ScheduleActivity.toScheduleActivity(this);
                break;
            }
            case R.id.clDeviceChange: {
                DeviceChangeActivity.toDeviceChangeActivity(this);
                break;
            }
            case R.id.clRunDevice: {
                RunDeviceActivity.toRunDeviceActivity(this, sceneCondition);
                break;
            }
            case R.id.clSelectSmart: {
                SelectSmartActivity.toSelectSmartActivity(this, sceneCondition);
                break;
            }
            case R.id.clDelay: {
                DelayActivity.toDelayActivity(this, sceneCondition);
                break;
            }
        }
    }

}
