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
import com.afar.osaio.smart.electrician.smartScene.adapter.WeatherAdapter;
import com.afar.osaio.smart.electrician.smartScene.presenter.IWeatherPresenter;
import com.afar.osaio.smart.electrician.smartScene.presenter.WeatherPresenter;
import com.afar.osaio.smart.electrician.smartScene.view.IWeatherView;
import com.afar.osaio.smart.electrician.util.ErrorHandleUtil;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.home.sdk.bean.scene.PlaceFacadeBean;
import com.tuya.smart.home.sdk.bean.scene.SceneBean;
import com.tuya.smart.home.sdk.bean.scene.condition.ConditionListBean;

import java.io.Serializable;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WeatherActivity extends BaseActivity implements IWeatherView {

    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.rvWeather)
    RecyclerView rvWeather;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;

    private IWeatherPresenter mPresenter;
    private WeatherAdapter mAdapter;
    private String sceneCondition;
    private String sceneTask;
    private boolean isAdd;
    private SceneBean mSceneBean;

    public static void toWeatherActivity(Activity from) {
        Intent intent = new Intent(from, WeatherActivity.class);
        from.startActivity(intent);
    }

    public static void toWeatherActivity(Activity from, String sceneCondition, String sceneTask, boolean isAdd, Serializable sceneBean) {
        Intent intent = new Intent(from, WeatherActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENECONDITION, sceneCondition);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENETASK, sceneTask);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEE_ADD, isAdd);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEBEAN, sceneBean);
        from.startActivity(intent);
    }

    public static void toWeatherActivity(Activity from, String sceneCondition, String sceneTask, boolean isAdd) {
        Intent intent = new Intent(from, WeatherActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENECONDITION, sceneCondition);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENETASK, sceneTask);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEE_ADD, isAdd);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSlideable(false);
        setContentView(R.layout.activity_weather);
        ButterKnife.bind(this);
        initView();
        initData();
        processExtraData();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        NooieLog.e("---------->WeatherActivity onNewIntent");
        processExtraData();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(getText(R.string.weather_changes));
        setupWeather();
    }

    private void initData() {
        mPresenter = new WeatherPresenter(this);
        mPresenter.getWeatherCondition();
    }

    private void processExtraData() {
        if (getCurrentIntent() == null) {
            finish();
        } else {
            mSceneBean = (SceneBean) getCurrentIntent().getSerializableExtra(ConstantValue.INTENT_KEY_SCENEBEAN);
            sceneCondition = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SCENECONDITION);
            sceneTask = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SCENETASK);
            isAdd = getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_SCENEE_ADD, false);
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
    public void notifyGetConditionListSuccess(List<ConditionListBean> conditionList) {
        mAdapter.setData(conditionList);
    }

    @Override
    public void notifyGetConditionListFail(String errorMsg) {
        ErrorHandleUtil.toastTuyaError(this, errorMsg);
    }

    @Override
    public void notifyGetCityByLatLngSuccess(PlaceFacadeBean placeFacadeBean) {

    }

    @Override
    public void notifyGetCityByLatLngFail(String errorMsg) {

    }

    private void setupWeather() {
        mAdapter = new WeatherAdapter();
        mAdapter.setListener(new WeatherAdapter.WeatherItemListener() {
            @Override
            public void onItemClick(ConditionListBean conditionListBean) {
                if (isAdd) {
                    if (mSceneBean != null) {
                        SelectWeatherActivity.toSelectWeatherActivity(WeatherActivity.this, conditionListBean, sceneCondition, sceneTask, isAdd, mSceneBean);
                    } else {
                        SelectWeatherActivity.toSelectWeatherActivity(WeatherActivity.this, conditionListBean, sceneCondition, sceneTask, isAdd);
                    }
                } else {
                    SelectWeatherActivity.toSelectWeatherActivity(WeatherActivity.this, conditionListBean);
                }
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        rvWeather.setLayoutManager(layoutManager);
        rvWeather.setAdapter(mAdapter);
    }
}
