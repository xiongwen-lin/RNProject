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
import com.afar.osaio.base.TplContract;
import com.afar.osaio.base.TplPresenter;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.widget.helper.ResHelper;
import com.nooie.data.EventDictionary;
import com.nooie.sdk.bean.IpcType;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LpCameraIncorrectLightActivity extends BaseActivity implements TplContract.View {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.tvGuideTipOne)
    TextView tvGuideTipOne;
    @BindView(R.id.tvGuideTipTwo)
    TextView tvGuideTipTwo;
    @BindView(R.id.ivDeviceIconLpPro)
    ImageView ivDeviceIconLpPro;
    @BindView(R.id.ivDeviceIconOne)
    ImageView ivDeviceIconOne;
    @BindView(R.id.ivDeviceIconTwo)
    ImageView ivDeviceIconTwo;

    private TplContract.Presenter mPresenter;

    public static void toLpCameraIncorrectLightActivity(Context from, Bundle param) {
        Intent intent = new Intent(from, LpCameraIncorrectLightActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_START_PARAM, param);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lp_camera_incorrect_light);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    private void initData() {
        new TplPresenter(this);
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
        releaseRes();
        release();
    }

    private void release() {
    }

    @OnClick({R.id.ivLeft, R.id.btnNext, R.id.btnReset, R.id.btnResetLpPro})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.btnNext:
            case R.id.btnReset:
            case R.id.btnResetLpPro:
                NooieDeviceHelper.trackDNEvent(EventDictionary.EVENT_ID_161);
                finish();
                break;
        }
    }

    @Override
    public void setPresenter(@NonNull TplContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public String getExternal() {
        return NooieDeviceHelper.createDistributionNetworkExternal(false);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        if (getErrorType() == ConstantValue.INCORRECT_LIGHT_PAGE_ERROR_TYPE_SUB_DEVICE) {
            tvTitle.setText(R.string.lp_camera_incorrect_light_title);
            ivDeviceIconOne.setImageResource(ResHelper.getInstance().getFlashLightOnIconByType(getDeviceModel()));
            ivDeviceIconTwo.setImageResource(ResHelper.getInstance().getDeviceResetIconByType(getDeviceModel()));
            if (NooieDeviceHelper.mergeIpcType(IpcType.getIpcType(getDeviceModel())) != IpcType.EC810_CAM) {
                tvGuideTipTwo.setText(R.string.lp_camera_incorrect_light_guide_tip_2_ec810_pro);
            }
        } else {
            tvTitle.setText(R.string.add_camera_no_red_light);
            ivDeviceIconLpPro.setImageResource(ResHelper.getInstance().getDeviceResetIconByType(getDeviceModel()));
            int[] hideViewIds = {R.id.tvGuideTipOne, R.id.ivDeviceIconOne, R.id.btnNext, R.id.tvGuideTipTwo, R.id.ivDeviceIconTwo, R.id.btnReset};
            for (int i = 0; i < hideViewIds.length; i++) {
                findViewById(hideViewIds[i]).setVisibility(View.GONE);
            }
            int[] showViewIds = {R.id.tvGuideTipLpPro, R.id.ivDeviceIconLpPro, R.id.btnResetLpPro};
            for (int i = 0; i < showViewIds.length; i++) {
                findViewById(showViewIds[i]).setVisibility(View.VISIBLE);
            }
        }
    }

    private String getDeviceModel() {
        if (getStartParam() == null) {
            return null;
        }
        return getStartParam().getString(ConstantValue.INTENT_KEY_IPC_MODEL);
    }

    private int getErrorType() {
        if (getStartParam() == null) {
            return ConstantValue.INCORRECT_LIGHT_PAGE_ERROR_TYPE_SUB_DEVICE;
        }
        return getStartParam().getInt(ConstantValue.INTENT_KEY_DATA_PARAM_1, ConstantValue.INCORRECT_LIGHT_PAGE_ERROR_TYPE_SUB_DEVICE);
    }
}
