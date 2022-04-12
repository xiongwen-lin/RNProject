package com.afar.osaio.account.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.account.adapter.TwoAuthDeviceAdapter;
import com.afar.osaio.account.adapter.TwoAuthDeviceListener;
import com.afar.osaio.account.contract.TwoAuthDetailContract;
import com.afar.osaio.account.presenter.TwoAuthDetailPresenter;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.widget.LabelSwItemView;
import com.nooie.common.base.GlobalData;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.configure.PhoneUtil;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.TwoAuthDevice;
import com.nooie.sdk.api.network.base.bean.entity.UserInfoResult;
import com.suke.widget.SwitchButton;

import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TwoAuthDetailActivity extends BaseActivity implements TwoAuthDetailContract.View {

    private static final int REQUEST_CODE_FOR_TWO_AUTH_DEVICE = 1;

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.lswTwoAuth)
    LabelSwItemView lswTwoAuth;
    @BindView(R.id.tvTwoAuthDeviceNameLabel)
    TextView tvTwoAuthDeviceNameLabel;
    @BindView(R.id.tvTwoAuthDeviceName)
    TextView tvTwoAuthDeviceName;
    @BindView(R.id.vTwoAuthNameDividerLine)
    View vTwoAuthNameDividerLine;
    @BindView(R.id.tvTwoAuthDeviceListLabel)
    TextView tvTwoAuthDeviceListLabel;
    @BindView(R.id.rvTwoAuthDevices)
    RecyclerView rvTwoAuthDevices;

    private TwoAuthDetailContract.Presenter mPresenter;
    private TwoAuthDeviceAdapter mTwoAuthDeviceAdapter;

    public static void toTwoAuthDetailActivity(Context from) {
        Intent intent = new Intent(from, TwoAuthDetailActivity.class);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_auth_detail);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            return;
        }
        new TwoAuthDetailPresenter(this);
        if (mPresenter != null) {
            mPresenter.getUserInfo();
            mPresenter.getTwoAuthDevice();
        }
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.two_auth_login_title);
        lswTwoAuth.setLabelTitle(getString(R.string.two_auth_login_title)).displayLabelRightSw(View.VISIBLE);
        lswTwoAuth.setLabelRightSwListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (isDestroyed() || mPresenter == null) {
                    return;
                }
                displayDeviceInfoView(isChecked);
                if (isChecked) {
                    showLoading();
                    mPresenter.openTwoAuth();
                } else {
                    showLoading();
                    mPresenter.closeTwoAuth();
                }
            }
        });
        tvTwoAuthDeviceName.setText(PhoneUtil.getPhoneName(NooieApplication.mCtx));
        setupTwoAuthDeviceView();
        displayDeviceInfoView(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeData();
    }

    private void resumeData() {
    }

    private void setupTwoAuthDeviceView() {
        mTwoAuthDeviceAdapter = new TwoAuthDeviceAdapter();
        mTwoAuthDeviceAdapter.setListener(new TwoAuthDeviceListener() {
            @Override
            public void onItemClick(TwoAuthDevice device) {
                if (isDestroyed() || device == null) {
                    return;
                }
                Bundle data = new Bundle();
                data.putString(ConstantValue.KEY_TWO_AUTH_DEVICE_NAME, device.getPhone_name());
                data.putString(ConstantValue.KEY_TWO_AUTH_DEVICE_PHONE_ID, device.getPhone_code());
                data.putString(ConstantValue.KEY_TWO_AUTH_DEVICE_MODEL, device.getPhone_model());
                data.putLong(ConstantValue.KEY_TWO_AUTH_DEVICE_LAST_TIME, device.getLast_login_time());
                TwoAuthDeviceActivity.toTwoAuthDeviceActivity(TwoAuthDetailActivity.this, REQUEST_CODE_FOR_TWO_AUTH_DEVICE, data);
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvTwoAuthDevices.setLayoutManager(layoutManager);
        rvTwoAuthDevices.setAdapter(mTwoAuthDeviceAdapter);
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
        if (lswTwoAuth != null) {
            lswTwoAuth.release();
        }
        tvTwoAuthDeviceNameLabel = null;
        tvTwoAuthDeviceName = null;
        vTwoAuthNameDividerLine = null;
        tvTwoAuthDeviceListLabel = null;
        if (rvTwoAuthDevices != null) {
            rvTwoAuthDevices.setAdapter(null);
        }
        if (mTwoAuthDeviceAdapter != null) {
            mTwoAuthDeviceAdapter.release();
        }
        rvTwoAuthDevices = null;
        mTwoAuthDeviceAdapter = null;
    }

    @OnClick({R.id.ivLeft})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
        }
    }

    @Override
    public void setPresenter(@NonNull TwoAuthDetailContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_FOR_TWO_AUTH_DEVICE) {
                if (mPresenter != null) {
                    mPresenter.getTwoAuthDevice();
                }
            }
        }
    }

    @Override
    public void onGetUserInfo(UserInfoResult result) {
        if (result != null) {
            refreshUserInfo(result);
        }
    }

    private void refreshUserInfo(UserInfoResult result) {
        if (isDestroyed() || result == null || lswTwoAuth == null) {
            return;
        }
        boolean isTwoAuth = result.getTwo_auth() == ApiConstant.TWO_AUTH_OPEN;
        if (isTwoAuth != lswTwoAuth.isLabelRightSwCheck()) {
            lswTwoAuth.toggleLabelRightSw();
        }
        displayDeviceInfoView(isTwoAuth);
    }

    @Override
    public void onOpenTwoAuth(BaseResponse response) {
        if (isDestroyed()) {
            return;
        }
        hideLoading();
        if (response != null && response.getCode() == StateCode.SUCCESS.code) {
        } else {
            if (lswTwoAuth != null) {
                lswTwoAuth.toggleLabelRightSw();
            }
            displayDeviceInfoView(false);
            ToastUtil.showToast(this, R.string.get_fail);
        }
    }

    @Override
    public void onCloseTwoAuth(BaseResponse response) {
        if (isDestroyed()) {
            return;
        }
        hideLoading();
        if (response != null && response.getCode() == StateCode.SUCCESS.code) {
        } else {
            if (lswTwoAuth != null) {
                lswTwoAuth.toggleLabelRightSw();
            }
            displayDeviceInfoView(true);
            ToastUtil.showToast(this, R.string.get_fail);
        }
    }

    @Override
    public void onGetTwoAuthDevice(List<TwoAuthDevice> devices) {
        if (isDestroyed()) {
            return;
        }
        showTwoAuthDevices(filterTwoAuthDevices(devices, GlobalData.getInstance().getPhoneId()));
    }

    private List<TwoAuthDevice> filterTwoAuthDevices(List<TwoAuthDevice> devices, String phoneCode) {
        if (CollectionUtil.isEmpty(devices) || TextUtils.isEmpty(phoneCode)) {
            return devices;
        }
        Iterator<TwoAuthDevice> iterator = devices.iterator();
        while (iterator.hasNext()) {
            TwoAuthDevice twoAuthDevice = iterator.next();
            if (twoAuthDevice != null && phoneCode.equalsIgnoreCase(twoAuthDevice.getPhone_code())) {
                iterator.remove();
            }
        }
        return devices;
    }

    private void showTwoAuthDevices(List<TwoAuthDevice> devices) {
        if (checkNull(lswTwoAuth, mTwoAuthDeviceAdapter)) {
            return;
        }
        mTwoAuthDeviceAdapter.setData(NooieDeviceHelper.sortTwoAuthDevices(devices));
        boolean isShowTrustDeviceList = lswTwoAuth.isLabelRightSwCheck() && CollectionUtil.isNotEmpty(mTwoAuthDeviceAdapter.getData());
        displayTrustDeviceList(isShowTrustDeviceList);
    }

    private void displayDeviceInfoView(boolean show) {
        if (checkNull(tvTwoAuthDeviceNameLabel, tvTwoAuthDeviceName, vTwoAuthNameDividerLine, mTwoAuthDeviceAdapter)) {
            return;
        }
        int visibility = show ? View.VISIBLE : View.GONE;
        tvTwoAuthDeviceNameLabel.setVisibility(visibility);
        tvTwoAuthDeviceName.setVisibility(visibility);
        vTwoAuthNameDividerLine.setVisibility(visibility);
        boolean isShowTrustDeviceList = show && CollectionUtil.isNotEmpty(mTwoAuthDeviceAdapter.getData());
        displayTrustDeviceList(isShowTrustDeviceList);
    }

    private void displayTrustDeviceList(boolean show) {
        if (checkNull(tvTwoAuthDeviceListLabel, rvTwoAuthDevices)) {
            return;
        }
        int visibility = show ? View.VISIBLE : View.GONE;
        tvTwoAuthDeviceListLabel.setVisibility(visibility);
        rvTwoAuthDevices.setVisibility(visibility);
    }
}
