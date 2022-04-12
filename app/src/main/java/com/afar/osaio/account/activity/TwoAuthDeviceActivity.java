package com.afar.osaio.account.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.account.contract.TwoAuthDeviceContract;
import com.afar.osaio.account.presenter.TwoAuthDevicePresenter;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.widget.LabelTextItemView;
import com.nooie.common.utils.time.DateTimeUtil;
import com.nooie.sdk.bean.SDKConstant;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TwoAuthDeviceActivity extends BaseActivity implements TwoAuthDeviceContract.View {

    private static final String PATTERN_HH_MM_A_MM_DD_YYYY = "hh:mm a MM/dd/yyyy";

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.livTwoAuthDeviceName)
    LabelTextItemView livTwoAuthDeviceName;
    @BindView(R.id.livTwoAuthDeviceModel)
    LabelTextItemView livTwoAuthDeviceModel;
    @BindView(R.id.livTwoAuthDeviceLastActivityTime)
    LabelTextItemView livTwoAuthDeviceLastActivityTime;

    private TwoAuthDeviceContract.Presenter mPresenter;

    public static void toTwoAuthDeviceActivity(Activity from, int requestCode, Bundle data) {
        Intent intent = new Intent(from, TwoAuthDeviceActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_PARAM, data);
        from.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_auth_device);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
            return;
        }
        new TwoAuthDevicePresenter(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.two_auth_device_title);
        livTwoAuthDeviceName.displayLabelRight_1(View.VISIBLE).displayArrow(View.GONE).setLabelTitle(getString(R.string.camera_settings_cam_info_camera_name));
        livTwoAuthDeviceModel.displayLabelRight_1(View.VISIBLE).displayArrow(View.GONE).setLabelTitle(getString(R.string.camera_settings_cam_info_model));
        livTwoAuthDeviceLastActivityTime.displayLabelRight_1(View.VISIBLE).displayArrow(View.GONE).setLabelTitle(getString(R.string.two_auth_device_last_activity_time_label));
        refreshView();
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeData();
    }

    private void resumeData() {
    }

    private void refreshView() {
        Bundle data = getCurrentIntent() != null ? getCurrentIntent().getBundleExtra(ConstantValue.INTENT_KEY_DATA_PARAM) : null;
        if (data != null) {
            String deviceName = data.getString(ConstantValue.KEY_TWO_AUTH_DEVICE_NAME);
            String devicePhoneModel = data.getString(ConstantValue.KEY_TWO_AUTH_DEVICE_MODEL);
            long deviceLastTime = data.getLong(ConstantValue.KEY_TWO_AUTH_DEVICE_LAST_TIME, 0);
            livTwoAuthDeviceName.setLabelRight_1(deviceName);
            livTwoAuthDeviceModel.setLabelRight_1(devicePhoneModel);
            String lastActivityTime = deviceLastTime > 0 ? DateTimeUtil.formatDate(NooieApplication.mCtx, deviceLastTime * 1000L, PATTERN_HH_MM_A_MM_DD_YYYY) : null;
            lastActivityTime = lastActivityTime != null ? lastActivityTime.toUpperCase() : new String();
            livTwoAuthDeviceLastActivityTime.setLabelRight_1(lastActivityTime);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.destroy();
        }
        hideLoading();
        releaseRes();
    }

    public void releaseRes() {
        ivLeft = null;
        tvTitle = null;
        if (livTwoAuthDeviceName != null) {
            livTwoAuthDeviceName.release();
            livTwoAuthDeviceName = null;
        }
        if (livTwoAuthDeviceModel != null) {
            livTwoAuthDeviceModel.release();
            livTwoAuthDeviceModel = null;
        }
        if (livTwoAuthDeviceLastActivityTime != null) {
            livTwoAuthDeviceLastActivityTime.release();
            livTwoAuthDeviceLastActivityTime = null;
        }
    }

    @OnClick({R.id.ivLeft, R.id.btnTwoAuthDeviceRemove})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft: {
                finish();
                break;
            }
            case R.id.btnTwoAuthDeviceRemove: {
                Bundle data = getCurrentIntent() != null ? getCurrentIntent().getBundleExtra(ConstantValue.INTENT_KEY_DATA_PARAM) : null;
                String devicePhoneId = data != null ? data.getString(ConstantValue.KEY_TWO_AUTH_DEVICE_PHONE_ID) : null;
                if (!TextUtils.isEmpty(devicePhoneId) && mPresenter != null) {
                    showLoading();
                    mPresenter.removeTwoAuthDevice(devicePhoneId);
                }
                break;
            }
        }
    }

    @Override
    public void setPresenter(@NonNull TwoAuthDeviceContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onRemoveTwoAuthDevice(int state) {
        if (isDestroyed()) {
            return;
        }
        hideLoading();
        if (SDKConstant.SUCCESS == state) {
            setResult(RESULT_OK);
            finish();
        }
    }
}
