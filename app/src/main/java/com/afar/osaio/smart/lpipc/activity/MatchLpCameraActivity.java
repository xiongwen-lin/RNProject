package com.afar.osaio.smart.lpipc.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.home.activity.HomeActivity;
import com.afar.osaio.smart.lpipc.contract.MatchLpCameraContract;
import com.afar.osaio.smart.lpipc.presenter.MatchLpCameraPresenter;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.widget.helper.ResHelper;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.data.EventDictionary;
import com.nooie.sdk.api.network.base.bean.entity.DeviceBindStatusResult;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MatchLpCameraActivity extends BaseActivity implements MatchLpCameraContract.View {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.tvMatchFailedLinkPage)
    TextView tvMatchFailedLinkPage;
    @BindView(R.id.ivMatchDeviceIcon)
    ImageView ivMatchDeviceIcon;

    private MatchLpCameraContract.Presenter mPresenter;

    public static void toMatchLpCameraActivity(Context from, Bundle param) {
        Intent intent = new Intent(from, MatchLpCameraActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_START_PARAM, param);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_lp_camera);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    private void initData() {
        new MatchLpCameraPresenter(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.add_lp_camera_title);
        //tvMatchFailedLinkPage.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        ivMatchDeviceIcon.setImageResource(ResHelper.getInstance().getGatewayAndCameraIconByType(getDeviceModel()));
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeData();
    }

    private void resumeData() {
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
        hideMatchLpCameraParedDialog();
        //tryStopQueryDeviceBindStatus();
        releaseRes();
        release();
    }

    private void release() {
    }

    @OnClick({R.id.ivLeft, R.id.btnDone, R.id.tvMatchFailedLinkPage})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.btnDone:
                NooieDeviceHelper.trackDNEvent(EventDictionary.EVENT_ID_173);
                HomeActivity.toHomeActivity(this);
                finish();
                break;
            case R.id.tvMatchFailedLinkPage:
                MatchLpCameraFailActivity.toMatchLpCameraFailActivity(this, MatchLpCameraFailActivity.FAIL_HELP_FOR_LP_CAMERA);
                break;
        }
    }

    private void tryStartQueryDeviceBindStatus() {
        if (mPresenter != null) {
            showLoading(false);
            mPresenter.queryDeviceBindStatus();
            mPresenter.startCountDown();
        }
    }

    private void tryStopQueryDeviceBindStatus() {
        if (mPresenter != null) {
            mPresenter.stopQueryDeviceBindStatusTask();
        }
    }

    @Override
    public void setPresenter(@NonNull MatchLpCameraContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onQueryDeviceBindStatus(String result, DeviceBindStatusResult bindResult) {
        if (isDestroyed()) {
            return;
        }
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            dealQueryDeviceBindStatus(bindResult);
        } else {
            hideLoading();
        }
    }

    private void dealQueryDeviceBindStatus(DeviceBindStatusResult bindResult) {
        int type = bindResult != null ? bindResult.getType() : -1;
        NooieLog.d("-->> MatchLpCameraActivity queryDeviceBindStatus type=" + type);
        boolean isStopLoadingDialog = type == 1 || type == 2 || type == 100 || type == 101;
        if (isStopLoadingDialog) {
            hideLoading();
        }
        if (type == 1) {
            if (mPresenter != null) {
                showLoading();
                mPresenter.getRecentBindDevice();
            }
        } else if (type == 2) {
            showMatchLpCameraParedDailog();
        } else if (type == 100) {
            ToastUtil.showToast(this, R.string.match_lp_camera_pared_failed);
        } else if (type == 101) {
            ToastUtil.showToast(this, R.string.match_lp_camera_pared_failed);
        }
    }

    private AlertDialog mMatchLpCameraParedDialog;

    private void showMatchLpCameraParedDailog() {
        hideMatchLpCameraParedDialog();
        mMatchLpCameraParedDialog = DialogUtils.showInformationDialog(this, getString(R.string.match_lp_camera_pared_title), getString(R.string.match_lp_camera_pared_content), getString(R.string.confirm), true, false, new DialogUtils.OnClickInformationDialogLisenter() {
            @Override
            public void onConfirmClick() {
            }
        });
    }

    private void hideMatchLpCameraParedDialog() {
        if (mMatchLpCameraParedDialog != null) {
            mMatchLpCameraParedDialog.dismiss();
        }
    }

    @Override
    public void onGetBindDeviceSuccess(String result, boolean isSuccess) {
        hideLoading();
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result) && isSuccess) {
            HomeActivity.toHomeActivity(this);
            finish();
        } else {
            ToastUtil.showToast(this, R.string.match_lp_camera_pared_failed);
        }
    }

    @Override
    public String getExternal() {
        return NooieDeviceHelper.createDistributionNetworkExternal(false);
    }

    private String getDeviceModel() {
        if (getStartParam() == null) {
            return null;
        }
        return getStartParam().getString(ConstantValue.INTENT_KEY_IPC_MODEL);
    }
}
