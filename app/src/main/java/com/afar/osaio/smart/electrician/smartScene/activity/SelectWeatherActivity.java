package com.afar.osaio.smart.electrician.smartScene.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.electrician.smartScene.adapter.WeatherFuncAdapter;
import com.afar.osaio.smart.electrician.smartScene.presenter.IWeatherPresenter;
import com.afar.osaio.smart.electrician.smartScene.presenter.WeatherPresenter;
import com.afar.osaio.smart.electrician.smartScene.view.IWeatherView;
import com.afar.osaio.smart.electrician.util.DialogUtil;
import com.afar.osaio.smart.electrician.util.ErrorHandleUtil;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nooie.common.hardware.bluetooth.BluetoothHelper;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.home.sdk.bean.scene.PlaceFacadeBean;
import com.tuya.smart.home.sdk.bean.scene.SceneBean;
import com.tuya.smart.home.sdk.bean.scene.SceneCondition;
import com.tuya.smart.home.sdk.bean.scene.condition.ConditionListBean;
import com.tuya.smart.home.sdk.bean.scene.condition.property.EnumProperty;
import com.tuya.smart.home.sdk.bean.scene.condition.property.ValueProperty;
import com.tuya.smart.home.sdk.bean.scene.condition.rule.EnumRule;
import com.tuya.smart.home.sdk.bean.scene.condition.rule.ValueRule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.EasyPermissions;

public class SelectWeatherActivity extends BaseActivity implements IWeatherView {
    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.rvWeather)
    RecyclerView rvWeather;
    @BindView(R.id.ivLess)
    ImageView ivLess;
    @BindView(R.id.ivEqual)
    ImageView ivEqual;
    @BindView(R.id.ivMore)
    ImageView ivMore;
    @BindView(R.id.tvValue)
    TextView tvValue;
    @BindView(R.id.tvUnit)
    TextView tvUnit;
    @BindView(R.id.sbBrightBar)
    SeekBar sbBrightBar;
    @BindView(R.id.tvMax)
    TextView tvMax;
    @BindView(R.id.tvMin)
    TextView tvMin;
    @BindView(R.id.clWeatherSlider)
    View clWeatherSlider;
    @BindView(R.id.tvCity)
    TextView tvCity;
    @BindView(R.id.tvHumidityTips)
    TextView tvHumidityTips;

    private WeatherFuncAdapter mAdapter;
    private ConditionListBean mCondition;
    private ValueProperty valueProperty;
    private String weatherKey;
    private String weatherValue;
    private List<SceneCondition> conditionList;
    private PlaceFacadeBean mPlaceBean;
    private String locationProvider;
    private IWeatherPresenter mPresenter;
    private String sceneCondition;
    private String sceneTask;
    private boolean isAdd;
    private boolean isModify;
    private int position;
    private SceneBean mSceneBean;

    private static final String[] PERMS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    public static void toSelectWeatherActivity(Activity from, Serializable conditionListBean, String sceneCondition, String sceneTask, boolean isAdd) {
        Intent intent = new Intent(from, SelectWeatherActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_CONDITIONLISTBEAN, conditionListBean);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENECONDITION, sceneCondition);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENETASK, sceneTask);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEE_ADD, isAdd);
        from.startActivity(intent);
    }

    public static void toSelectWeatherActivity(Activity from, Serializable conditionListBean, String sceneCondition, String sceneTask, boolean isAdd, Serializable sceneBean) {
        Intent intent = new Intent(from, SelectWeatherActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_CONDITIONLISTBEAN, conditionListBean);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENECONDITION, sceneCondition);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENETASK, sceneTask);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEE_ADD, isAdd);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEBEAN, sceneBean);
        from.startActivity(intent);
    }

    public static void toSelectWeatherActivity(Activity from, Serializable conditionListBean, String sceneCondition, String sceneTask, boolean isModify, int position) {
        Intent intent = new Intent(from, SelectWeatherActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_CONDITIONLISTBEAN, conditionListBean);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENECONDITION, sceneCondition);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENETASK, sceneTask);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEE_MODIFY, isModify);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEE_POSITION, position);
        from.startActivity(intent);
    }

    public static void toSelectWeatherActivity(Activity from, Serializable conditionListBean, String sceneCondition, String sceneTask, boolean isModify, int position, Serializable sceneBean) {
        Intent intent = new Intent(from, SelectWeatherActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_CONDITIONLISTBEAN, conditionListBean);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENECONDITION, sceneCondition);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENETASK, sceneTask);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEE_MODIFY, isModify);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEE_POSITION, position);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEBEAN, sceneBean);
        from.startActivity(intent);
    }

    public static void toSelectWeatherActivity(Activity from, Serializable conditionListBean) {
        Intent intent = new Intent(from, SelectWeatherActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_CONDITIONLISTBEAN, conditionListBean);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSlideable(false);
        setContentView(R.layout.activity_choose_weather);
        ButterKnife.bind(this);
        initData();
        initView();
        processExtraData();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter = new WeatherPresenter(this);
        if (!BluetoothHelper.isLocationEnabled(NooieApplication.mCtx)) {
            showCheckLocationEnableDialog(getResources().getString(R.string.location_tips));
            return;
        }
        if (EasyPermissions.hasPermissions(SelectWeatherActivity.this, PERMS)) {//已有权限
            getLocation();
        } else {
            DialogUtil.showConfirmWithSubMsgDialog(this, R.string.enable_location, R.string.enable_location_tips, R.string.cancel, R.string.confirm_upper, new DialogUtil.OnClickConfirmButtonListener() {
                @Override
                public void onClickRight() {
                    requestPermissions(PERMS);
                }

                @Override
                public void onClickLeft() {

                }
            });
        }
    }

    private void getLocation() {
        //1.获取位置管理器
        LocationManager locationManager = (LocationManager) NooieApplication.mCtx.getSystemService(Context.LOCATION_SERVICE);
        //2.获取位置提供器，GPS或是NetWork
        List<String> providers = locationManager.getProviders(true);
        if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            //如果是网络定位
            locationProvider = LocationManager.NETWORK_PROVIDER;
        } else if (providers.contains(LocationManager.GPS_PROVIDER)) {
            //如果是GPS定位
            locationProvider = LocationManager.GPS_PROVIDER;
        } else {
            //Toast.makeText(this, "没有可用的位置提供器", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(locationProvider);
        if (location != null) {
            mPresenter.getCityByLatLng(String.valueOf(location.getLongitude()), String.valueOf(location.getLatitude()));
        } else {
            // 监视地理位置变化，第二个和第三个参数分别为更新的最短时间minTime和最短距离minDistace
            locationManager.requestLocationUpdates(locationProvider, 0, 0, mListener);
        }
    }

    LocationListener mListener = new LocationListener() {
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        // 如果位置发生变化，重新显示
        @Override
        public void onLocationChanged(Location location) {
            mPresenter.getCityByLatLng(String.valueOf(location.getLongitude()), String.valueOf(location.getLatitude()));
        }
    };

    private void initView() {
        setUpWeather();
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        ivLess.setSelected(true);
        ivRight.setImageResource(R.drawable.define_black);
    }

    private void setUpFunction() {
        if (mCondition.getType() != null && mCondition.getProperty() != null) {
            if (mCondition.getType().equals("temp") || mCondition.getType().equals("windSpeed")) {
                rvWeather.setVisibility(View.GONE);
                clWeatherSlider.setVisibility(View.VISIBLE);
                valueProperty = (ValueProperty) mCondition.getProperty();
                if (valueProperty != null) {
                    tvMin.setText(valueProperty.getMin() + valueProperty.getUnit());
                    tvMax.setText(valueProperty.getMax() + valueProperty.getUnit());
                }
                setUpSeekBar();
            } else {
                rvWeather.setVisibility(View.VISIBLE);
                clWeatherSlider.setVisibility(View.GONE);
                List<String> key = new ArrayList<>();
                List<String> value = new ArrayList<>();
                EnumProperty enumProperty = (EnumProperty) mCondition.getProperty();
                if (enumProperty.getEnums() != null) {
                    for (Map.Entry<Object, String> entry : enumProperty.getEnums().entrySet()) {
                        key.add((String) entry.getKey());
                        value.add(entry.getValue());
                        NooieLog.e("------key:" + entry.getKey() + "   value:" + entry.getValue());
                    }
                    NooieLog.e("--------key size " + key.size() + "  -----value size  " + value.size());
                }
                mAdapter.setData(key, value);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        NooieLog.e("----------SelectWeatherActivity onNewIntent");
        processExtraData();
    }

    private void initData() {
        conditionList = new ArrayList<>();
        mPlaceBean = new PlaceFacadeBean();
    }

    private void processExtraData() {
        if (getCurrentIntent() == null) {
            finish();
        } else {
            mCondition = (ConditionListBean) getCurrentIntent().getSerializableExtra(ConstantValue.INTENT_KEY_CONDITIONLISTBEAN);
            sceneCondition = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SCENECONDITION);
            mSceneBean = (SceneBean) getCurrentIntent().getSerializableExtra(ConstantValue.INTENT_KEY_SCENEBEAN);
            sceneTask = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SCENETASK);
            isAdd = getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_SCENEE_ADD, false);
            isModify = getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_SCENEE_MODIFY, false);
            position = getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_SCENEE_POSITION, 0);
            if (CollectionUtil.isNotEmpty(conditionList)) {
                conditionList.clear();
            }

            if (mCondition != null) {
                tvTitle.setText(mCondition.getName());
                if (mCondition.getType().equals("humidity")) {
                    tvHumidityTips.setVisibility(View.VISIBLE);
                }
                setUpFunction();
            }

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
                    List<String> dpList = mAdapter.getKeyList();
                    for (Object o : list) {
                        String obj = new Gson().toJson(o);
                        List<Object> expr = new Gson().fromJson(obj, new TypeToken<List<Object>>() {
                        }.getType());
                        NooieLog.e("---------select weather expr " + expr);
                        for (int i = 0; i < expr.size(); i++) {
                            NooieLog.e("---------select weather expr item " + expr.get(i));

                            if (rvWeather.getVisibility() == View.VISIBLE) {
                                for (int j = 0; j < dpList.size(); j++) {
                                    if (expr.get(2).toString().equals(dpList.get(j))) {
                                        mAdapter.changeSelected(j);
                                        weatherKey = expr.get(2).toString();
                                    }
                                }
                            } else {
                                resetButton();
                                if (expr.get(1).toString().equals("<")) {
                                    ivLess.setSelected(true);
                                } else if (expr.get(1).toString().equals("==")) {
                                    ivEqual.setSelected(true);
                                } else if (expr.get(1).toString().equals(">")) {
                                    ivMore.setSelected(true);
                                }
                                String[] value = expr.get(2).toString().split("\\.");
                                tvValue.setText(value[0]);
                                if (sceneCondition.getEntitySubIds().equals("temp")) {
                                    NooieLog.e("--------------temp sbbrightbar " + (Integer.valueOf(value[0]) + 40));
                                    sbBrightBar.setProgress((Integer.valueOf(value[0]) + 40));
                                } else {
                                    sbBrightBar.setProgress(Integer.valueOf(value[0]));
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    @OnClick({R.id.ivLeft, R.id.ivLess, R.id.ivEqual, R.id.ivMore, R.id.ivMinus, R.id.ivAdd, R.id.ivRight})
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
            case R.id.ivMinus: {
                doMinus();
                break;
            }
            case R.id.ivAdd: {
                doAdd();
                break;
            }
            case R.id.ivRight: {
                if (rvWeather.getVisibility() == View.VISIBLE) {
                    if (TextUtils.isEmpty(weatherKey)) {
                        ToastUtil.showToast(this, R.string.please_choose);
                        return;
                    } else {
                        EnumRule enumRule = EnumRule.newInstance(
                                mCondition.getType(),  //类别
                                weatherKey        //选定的枚举值
                        );
                        SceneCondition weatherCondition = SceneCondition.createWeatherCondition(
                                mPlaceBean,    //城市
                                mCondition.getType(),        //类别
                                enumRule            //规则
                        );
                        weatherCondition.setEntityName(mPlaceBean.getCity());
                        weatherCondition.setExprDisplay(mCondition.getName() + ":" + weatherValue);
                        if (isModify) {
                            conditionList.set(position, weatherCondition);
                        } else {
                            conditionList.add(weatherCondition);
                        }
                    }
                } else {
                    String operator = "";
                    if (ivMore.isSelected()) {
                        operator = ">";
                    } else if (ivEqual.isSelected()) {
                        operator = "==";
                    } else if (ivLess.isSelected()) {
                        operator = "<";
                    }
                    ValueRule tempRule = ValueRule.newInstance(
                            mCondition.getType(),  //类别
                            operator,     //运算规则(">", "==", "<")
                            Integer.valueOf(tvValue.getText().toString())       //临界值
                    );
                    SceneCondition tempCondition = SceneCondition.createWeatherCondition(
                            mPlaceBean,   //城市
                            mCondition.getType(),            //类别
                            tempRule           //规则
                    );
                    if (operator.equals("==")) {
                        tempCondition.setEntityName(mPlaceBean.getCity());
                        tempCondition.setExprDisplay(mCondition.getName() + ":=" + tvValue.getText().toString() + valueProperty.getUnit());
                    } else {
                        tempCondition.setEntityName(mPlaceBean.getCity());
                        tempCondition.setExprDisplay(mCondition.getName() + ":" + operator + tvValue.getText().toString() + valueProperty.getUnit());
                    }
                    if (isModify) {
                        conditionList.set(position, tempCondition);
                    } else {
                        conditionList.add(tempCondition);
                    }
                }
                if (isAdd || isModify) {
                    if (mSceneBean != null) {
                        CreateNewSmartActivity.toCreateNewSmartActivity(this, sceneTask, new Gson().toJson(conditionList), mSceneBean);
                        break;
                    } else {
                        CreateNewSmartActivity.toCreateNewSmartActivity(this, sceneTask, new Gson().toJson(conditionList));
                        break;
                    }
                }
                CreateSceneActivity.toCreateSceneActivity(this, true, new Gson().toJson(conditionList));
                break;
            }
        }
    }

    private void setUpSeekBar() {
        if (valueProperty != null) {
            tvUnit.setText(valueProperty.getUnit());
            sbBrightBar.setMax(valueProperty.getMax());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                sbBrightBar.setMin(valueProperty.getMin());
            }
            if (mCondition.getType().equals("temp")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sbBrightBar.setMin(0);
                }
                sbBrightBar.setMax(80);
                sbBrightBar.setProgress(60);
                tvValue.setText("20");
            } else {
                sbBrightBar.setProgress(valueProperty.getMin());
                tvValue.setText(String.valueOf(valueProperty.getMin()));
            }
            sbBrightBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (progress < valueProperty.getMin()) {
                        NooieLog.e("---------min  " + valueProperty.getMin());
                        sbBrightBar.setProgress(valueProperty.getMin());
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (seekBar != null) {
                        if (mCondition.getType().equals("temp")) {
                            tvValue.setText(String.valueOf((seekBar.getProgress() - 40)));
                        } else {
                            tvValue.setText(String.valueOf(seekBar.getProgress()));
                        }
                    }
                }
            });
        }
    }

    private void setUpWeather() {
        mAdapter = new WeatherFuncAdapter();
        mAdapter.setListener(new WeatherFuncAdapter.WeatherFuncItemListener() {
            @Override
            public void onItemClick(int position) {
                mAdapter.changeSelected(position);
                weatherKey = mAdapter.getSelectedKey(position);
                weatherValue = mAdapter.getSelectedValue(position);
                NooieLog.e("-------click position  " + position + " key " + weatherKey + " value " + weatherValue);
            }

            @Override
            public void onGetSelectValue(String value) {
                NooieLog.e("-------SelectWeather onGetSelectValue " + value);
                weatherValue = value;
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        rvWeather.setLayoutManager(layoutManager);
        rvWeather.setAdapter(mAdapter);
    }

    private void resetButton() {
        ivEqual.setSelected(false);
        ivLess.setSelected(false);
        ivMore.setSelected(false);
    }

    private void doMinus() {
        int result = Integer.valueOf(tvValue.getText().toString()) - 1;
        if (valueProperty != null) {
            if (result >= valueProperty.getMin()) {
                tvValue.setText(String.valueOf(result));
                if (mCondition.getType().equals("temp")) {
                    sbBrightBar.setProgress((result + 40));
                } else {
                    sbBrightBar.setProgress(result);
                }
            }
        }
    }

    private void doAdd() {
        int result = Integer.valueOf(tvValue.getText().toString()) + 1;
        if (valueProperty != null) {
            if (result <= valueProperty.getMax()) {
                tvValue.setText(String.valueOf(result));
                if (mCondition.getType().equals("temp")) {
                    sbBrightBar.setProgress((result + 40));
                } else {
                    sbBrightBar.setProgress(result);
                }
            }
        }
    }

    @Override
    public void permissionsGranted() {
        getLocation();
    }

    @Override
    public void notifyGetConditionListSuccess(List<ConditionListBean> conditionList) {

    }

    @Override
    public void notifyGetConditionListFail(String errorMsg) {

    }

    @Override
    public void notifyGetCityByLatLngSuccess(PlaceFacadeBean placeFacadeBean) {
        mPlaceBean = placeFacadeBean;
        tvCity.setText(mPlaceBean.getCity());
    }

    @Override
    public void notifyGetCityByLatLngFail(String errorMsg) {
        ErrorHandleUtil.toastTuyaError(this, errorMsg);
    }
}
