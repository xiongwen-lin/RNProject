package com.afar.osaio.smart.lpipc.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.cache.GatewayDeviceCache;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.lpipc.contract.AddLpSuitContract;
import com.afar.osaio.smart.lpipc.presenter.AddLpSuitPresenter;
import com.afar.osaio.util.ConstantValue;
import com.nooie.data.EventDictionary;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddLpSuitActivity extends BaseActivity implements AddLpSuitContract.View {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;

    private AddLpSuitContract.Presenter mPresenter;
    private int mGatewayNum = 0;

    public static void toAddLpSuitActivity (Context from, Bundle param) {
        Intent intent = new Intent(from, AddLpSuitActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_START_PARAM, param);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lp_suit);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    private void initData() {
        new AddLpSuitPresenter(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.add_camera_title);
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeData();
    }

    private void resumeData() {
        mGatewayNum = GatewayDeviceCache.getInstance().cacheSize();
        if (mPresenter != null) {
            mPresenter.getGatewayNum();
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
        releaseRes();
    }

    @Override
    public void releaseRes() {
        super.releaseRes();
        ivLeft = null;
        tvTitle = null;
    }

    @OnClick({R.id.ivLeft, R.id.vGatewayContainer, R.id.vLpCameraContainer})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.vGatewayContainer: {
                NooieDeviceHelper.trackDNEvent(EventDictionary.EVENT_ID_163);
                gotoAddGateway();
                break;
            }
            case R.id.vLpCameraContainer: {
                NooieDeviceHelper.trackDNEvent(EventDictionary.EVENT_ID_164);
                gotoAddLpCamera();
                break;
            }
        }
    }

    @Override
    public void setPresenter(@NonNull AddLpSuitContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onGetGatewayNumResult(String result, int deviceNum) {
        if (isDestroyed()) {
            return;
        }

        mGatewayNum = ConstantValue.SUCCESS.equalsIgnoreCase(result) ? deviceNum : mGatewayNum;
    }

    @Override
    public String getExternal() {
        return NooieDeviceHelper.createDistributionNetworkExternal(false);
    }

    private boolean checkIsAddGateway() {
        return mGatewayNum > 0;
    }

    private void gotoAddLpCamera() {
        if (checkIsAddGateway()) {
            Bundle param = new Bundle();
            param.putString(ConstantValue.INTENT_KEY_IPC_MODEL, getDeviceModel());
            param.putBoolean(ConstantValue.INTENT_KEY_DATA_PARAM_1, checkIsAddGateway());
            AddLpCameraActivity.toAddLpCameraActivity(this, param);
        } else {
            gotoAddGateway();
        }
    }

    private void gotoAddGateway() {
        Bundle param = new Bundle();
        param.putString(ConstantValue.INTENT_KEY_IPC_MODEL, getDeviceModel());
        param.putBoolean(ConstantValue.INTENT_KEY_DATA_PARAM_1, checkIsAddGateway());
        AddLowPowerIpcActivity.toAddLowPowerIpcActivity(this, param);
    }

    private String getDeviceModel() {
        if (getStartParam() == null) {
            return null;
        }
        return getStartParam().getString(ConstantValue.INTENT_KEY_IPC_MODEL);
    }
}
