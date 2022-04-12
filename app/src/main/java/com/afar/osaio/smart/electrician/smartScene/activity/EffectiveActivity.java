package com.afar.osaio.smart.electrician.smartScene.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.electrician.util.DialogUtil;
import com.afar.osaio.smart.electrician.util.ErrorHandleUtil;
import com.afar.osaio.smart.electrician.widget.SelectTimePopupWindows;
import com.afar.osaio.util.ConstantValue;
import com.google.gson.Gson;
import com.nooie.common.hardware.bluetooth.BluetoothHelper;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.scene.PlaceFacadeBean;
import com.tuya.smart.home.sdk.bean.scene.PreCondition;
import com.tuya.smart.home.sdk.bean.scene.PreConditionExpr;
import com.tuya.smart.home.sdk.callback.ITuyaResultCallback;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.EasyPermissions;

import static com.afar.osaio.util.ConstantValue.REQUEST_CODE_REPEAT;


public class EffectiveActivity extends BaseActivity {

    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.tvFrequency)
    TextView tvFrequency;
    @BindView(R.id.tvCity)
    TextView tvCity;
    @BindView(R.id.ivAll)
    ImageView ivAll;
    @BindView(R.id.ivDay)
    ImageView ivDay;
    @BindView(R.id.ivNight)
    ImageView ivNight;
    @BindView(R.id.ivCustom)
    ImageView ivCustom;
    @BindView(R.id.tvCustomTime)
    TextView tvCustomTime;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;

    private StringBuffer sbValue;
    private String week;
    private String locationProvider;
    private PlaceFacadeBean mPlaceBean;
    private SelectTimePopupWindows selectTimePopupWindows;
    private String startTime;
    private String endTime;
    private PreConditionExpr mPreConditionExpr;
    private String[] startArray;
    private String[] endArray;

    private static final String[] PERMS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    public static void toEffectiveActivity(Activity from, int requestCode) {
        Intent intent = new Intent(from, EffectiveActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_EFFECT, requestCode);
        from.startActivityForResult(intent, requestCode);
    }

    public static void toEffectiveActivity(Activity from, int requestCode, Serializable preConditionExpr) {
        Intent intent = new Intent(from, EffectiveActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_EFFECT, requestCode);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENE_PRECONDITIONEXPR, preConditionExpr);
        from.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSlideable(false);
        setContentView(R.layout.activity_effective);
        ButterKnife.bind(this);
        initView();
        initData();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!BluetoothHelper.isLocationEnabled(NooieApplication.mCtx)) {
            showCheckLocationEnableDialog(getResources().getString(R.string.location_tips));
            return;
        }
        if (EasyPermissions.hasPermissions(EffectiveActivity.this, PERMS)) {//已有权限
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
            TuyaHomeSdk.getSceneManagerInstance().getCityByLatLng(String.valueOf(location.getLongitude()), String.valueOf(location.getLatitude()), new ITuyaResultCallback<PlaceFacadeBean>() {
                @Override
                public void onSuccess(PlaceFacadeBean result) {
                    mPlaceBean = result;
                    tvCity.setText(result.getCity());
                }

                @Override
                public void onError(String errorCode, String errorMessage) {
                    ErrorHandleUtil.toastTuyaError(EffectiveActivity.this, errorMessage);
                }
            });
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
            TuyaHomeSdk.getSceneManagerInstance().getCityByLatLng(String.valueOf(location.getLongitude()), String.valueOf(location.getLatitude()), new ITuyaResultCallback<PlaceFacadeBean>() {
                @Override
                public void onSuccess(PlaceFacadeBean result) {
                    mPlaceBean = result;
                    tvCity.setText(result.getCity());
                }

                @Override
                public void onError(String errorCode, String errorMessage) {
                    ErrorHandleUtil.toastTuyaError(EffectiveActivity.this, errorMessage);
                }
            });
        }
    };

    @Override
    public void permissionsGranted() {
        getLocation();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(getText(R.string.effective_time));
        ivRight.setImageResource(R.drawable.define_black);
        ivAll.setSelected(true);
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
        } else {
            mPlaceBean = new PlaceFacadeBean();
            mPreConditionExpr = (PreConditionExpr) getCurrentIntent().getSerializableExtra(ConstantValue.INTENT_KEY_SCENE_PRECONDITIONEXPR);
            if (mPreConditionExpr != null) {
                if (mPreConditionExpr.getTimeInterval().equals(PreCondition.TIMEINTERVAL_ALLDAY)) {
                    resetButton();
                    ivAll.setSelected(true);
                } else if (mPreConditionExpr.getTimeInterval().equals(PreCondition.TIMEINTERVAL_DAYTIME)) {
                    resetButton();
                    ivDay.setSelected(true);
                } else if (mPreConditionExpr.getTimeInterval().equals(PreCondition.TIMEINTERVAL_NIGHT)) {
                    resetButton();
                    ivNight.setSelected(true);
                } else if (mPreConditionExpr.getTimeInterval().equals(PreCondition.TIMEINTERVAL_CUSTOM)) {
                    resetButton();
                    ivCustom.setSelected(true);
                    startArray = mPreConditionExpr.getStart().split(":");
                    endArray = mPreConditionExpr.getEnd().split(":");
                    startTime = mPreConditionExpr.getStart();
                    endTime = mPreConditionExpr.getEnd();
                    int startTime = Integer.valueOf(startArray[0]) * 60 + Integer.valueOf(startArray[1]);
                    int endTime = Integer.valueOf(endArray[0]) * 60 + Integer.valueOf(endArray[1]);
                    if (startTime > endTime) {
                        tvCustomTime.setText(mPreConditionExpr.getStart() + "-" + mPreConditionExpr.getEnd() + " Next Day");
                    } else {
                        //tvCustomTime.setText(mPreConditionExpr.getStart() + "-" + mPreConditionExpr.getEnd() + " Same Day");
                        tvCustomTime.setText(mPreConditionExpr.getStart() + "-" + mPreConditionExpr.getEnd());
                    }
                }
                StringBuffer buffer = new StringBuffer();
                if (!TextUtils.isEmpty(mPreConditionExpr.getLoops()) && mPreConditionExpr.getLoops().length() == 7) {
                    char[] chars = mPreConditionExpr.getLoops().toCharArray();
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
                    if (TextUtils.isEmpty(buffer)) {
                        tvFrequency.setText(R.string.every_day);
                    } else {
                        if (buffer.toString().equals(NooieApplication.mCtx.getResources().getString(R.string.sun) + " "
                                + NooieApplication.mCtx.getResources().getString(R.string.mon) + " "
                                + NooieApplication.mCtx.getResources().getString(R.string.tues) + " "
                                + NooieApplication.mCtx.getResources().getString(R.string.wed) + " "
                                + NooieApplication.mCtx.getResources().getString(R.string.thurs) + " "
                                + NooieApplication.mCtx.getResources().getString(R.string.fri) + " "
                                + NooieApplication.mCtx.getResources().getString(R.string.sat))) {
                            tvFrequency.setText(R.string.every_day);
                        } else {
                            tvFrequency.setText(buffer.toString());
                        }
                    }
                }
            }
        }
    }

    @OnClick({R.id.ivLeft, R.id.clRepeat, R.id.ivRight, R.id.clAll, R.id.clDay, R.id.clNight, R.id.clCustom})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft: {
                finish();
                break;
            }
            case R.id.clRepeat: {
                if (mPreConditionExpr != null) {
                    RepeatActivity.toRepeatActivity(this, REQUEST_CODE_REPEAT, true, mPreConditionExpr.getLoops());
                } else {
                    RepeatActivity.toRepeatActivity(this, REQUEST_CODE_REPEAT, true);
                }
                break;
            }
            case R.id.ivRight: {
                setupPreCondition();
                finish();
                break;
            }
            case R.id.clAll: {
                resetButton();
                tvCustomTime.setText(R.string.custom_time);
                startArray = null;
                endArray = null;
                ivAll.setSelected(true);
                break;
            }
            case R.id.clDay: {
                resetButton();
                tvCustomTime.setText(R.string.custom_time);
                startArray = null;
                endArray = null;
                ivDay.setSelected(true);
                break;
            }
            case R.id.clNight: {
                resetButton();
                tvCustomTime.setText(R.string.custom_time);
                startArray = null;
                endArray = null;
                ivNight.setSelected(true);
                break;
            }
            case R.id.clCustom: {
                resetButton();
                showPopMenu();
                if (startArray != null && startArray.length > 0) {
                    selectTimePopupWindows.setStartTime(Integer.valueOf(startArray[0]), Integer.valueOf(startArray[1]));
                }
                if (endArray != null && endArray.length > 0) {
                    selectTimePopupWindows.setEndTime(Integer.valueOf(endArray[0]), Integer.valueOf(endArray[1]));
                }
                if (tvCustomTime.getText().toString().equals(getResources().getString(R.string.custom_time))) {
                    tvCustomTime.setText("00:00-23:59");
                }
                ivCustom.setSelected(true);
                break;
            }
        }
    }

    private void setupPreCondition() {
        PreCondition preCondition = new PreCondition();
        PreConditionExpr expr = new PreConditionExpr();
        if (mPlaceBean != null) {
            expr.setCityName(mPlaceBean.getCity());
            expr.setCityId(String.valueOf(mPlaceBean.getCityId()));
        }
        if (ivAll.isSelected()) {
            expr.setTimeInterval(PreCondition.TIMEINTERVAL_ALLDAY);
        } else if (ivDay.isSelected()) {
            expr.setTimeInterval(PreCondition.TIMEINTERVAL_DAYTIME);
        } else if (ivNight.isSelected()) {
            expr.setTimeInterval(PreCondition.TIMEINTERVAL_NIGHT);
        } else if (ivCustom.isSelected()) {
            expr.setTimeInterval(PreCondition.TIMEINTERVAL_CUSTOM);
            if (TextUtils.isEmpty(startTime)) {
                expr.setStart("00:00");
            } else {
                expr.setStart(startTime);
            }
            if (TextUtils.isEmpty(endTime)) {
                expr.setEnd("23:59");
            } else {
                expr.setEnd(endTime);
            }
        }
        if (TextUtils.isEmpty(week)) {
            week = "0000000";
        }
        expr.setLoops(week);
        preCondition.setCondType(PreCondition.TYPE_TIME_CHECK);
        expr.setTimeZoneId(TimeZone.getDefault().getID());
        preCondition.setExpr(expr);
        List<PreCondition> preConditions = new ArrayList<>();
        preConditions.add(preCondition);
        Intent intent = new Intent();
        intent.putExtra(ConstantValue.INTENT_KEY_PRECONDITION, new Gson().toJson(preConditions));
        if (ivAll.isSelected()) {
            intent.putExtra(ConstantValue.INTENT_KEY_SCENE_EFFECT, NooieApplication.mCtx.getResources().getString(R.string.all_day));
        } else if (ivDay.isSelected()) {
            intent.putExtra(ConstantValue.INTENT_KEY_SCENE_EFFECT, NooieApplication.mCtx.getResources().getString(R.string.daytime));
        } else if (ivNight.isSelected()) {
            intent.putExtra(ConstantValue.INTENT_KEY_SCENE_EFFECT, NooieApplication.mCtx.getResources().getString(R.string.at_night));
        } else if (ivCustom.isSelected()) {
            if (tvCustomTime.getText().toString().equals(getResources().getString(R.string.custom_time))) {
                // intent.putExtra(ConstantValue.INTENT_KEY_SCENE_EFFECT, "00:00-23:59 Same Day");
                intent.putExtra(ConstantValue.INTENT_KEY_SCENE_EFFECT, "00:00-23:59");
            } else {
                intent.putExtra(ConstantValue.INTENT_KEY_SCENE_EFFECT, tvCustomTime.getText().toString());
            }
        }
        setResult(RESULT_OK, intent);
    }

    private void showPopMenu() {
        if (selectTimePopupWindows != null) {
            selectTimePopupWindows.dismiss();
        }

        selectTimePopupWindows = new SelectTimePopupWindows(EffectiveActivity.this);

        selectTimePopupWindows.setListener(new SelectTimePopupWindows.SelectTimeListener() {
            @Override
            public void onGetStartEndTime(String start, String end, String overOneDay) {
                startTime = start;
                endTime = end;
                if (!TextUtils.isEmpty(startTime)) {
                    startArray = startTime.split(":");
                }
                if (!TextUtils.isEmpty(endTime)) {
                    endArray = endTime.split(":");
                }

                if (TextUtils.isEmpty(start)) {
                    if (TextUtils.isEmpty(end)) {
                        tvCustomTime.setText("00:00" + "-" + "23:59" + " " + overOneDay);
                    } else {
                        tvCustomTime.setText("00:00" + "-" + end + " " + overOneDay);
                    }
                } else {
                    if (TextUtils.isEmpty(end)) {
                        tvCustomTime.setText(start + "-" + "23:59" + " " + overOneDay);
                    } else {
                        tvCustomTime.setText(start + "-" + end + " " + overOneDay);
                    }
                }
            }
        });

        selectTimePopupWindows.showAtLocation(findViewById(R.id.clEffective),
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    private void resetButton() {
        ivAll.setSelected(false);
        ivDay.setSelected(false);
        ivNight.setSelected(false);
        ivCustom.setSelected(false);
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
                    NooieLog.e("--------------选中的星期  " + sbValue.toString() + " 选中的key " + week);
                    break;
            }
        }
    }

}
