package com.afar.osaio.smart.lpipc.activity;

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
import com.afar.osaio.base.TplContract;
import com.afar.osaio.base.TplPresenter;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.widget.FButton;
import com.afar.osaio.widget.helper.ResHelper;
import com.nooie.data.EventDictionary;
import com.nooie.sdk.bean.IpcType;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddLpCameraActivity extends BaseActivity implements TplContract.View {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.tvDeviceLinkPage)
    TextView tvDeviceLinkPage;
    @BindView(R.id.tvAddDeviceGuideTip)
    TextView tvAddDeviceGuideTip;
    @BindView(R.id.btnDone)
    FButton btnDone;
    @BindView(R.id.ivAddDeviceIcon)
    ImageView ivAddDeviceIcon;
    @BindView(R.id.ivAddGatewayIcon)
    ImageView ivAddGatewayIcon;
    @BindView(R.id.ivAddCenterIcon)
    ImageView ivAddCenterIcon;
    @BindView(R.id.ivAddLpDeviceIcon)
    ImageView ivAddLpDeviceIcon;

    private TplContract.Presenter mPresenter;

    public static void toAddLpCameraActivity(Context from, Bundle param) {
        Intent intent = new Intent(from, AddLpCameraActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_START_PARAM, param);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_lp_camera);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    private void initData() {
        new TplPresenter(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.add_lp_camera_title);
        //tvDeviceLinkPage.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        ivAddDeviceIcon.setImageResource(ResHelper.getInstance().getGatewayAndCameraRedIconByType(getDeviceModel()));
        showLpCameraView(true);
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
        releaseRes();
    }

    @Override
    public void releaseRes() {
        super.releaseRes();
        ivLeft = null;
        tvTitle = null;
        tvDeviceLinkPage = null;
        tvAddDeviceGuideTip = null;
        btnDone = null;
        ivAddDeviceIcon = null;
        ivAddGatewayIcon = null;
        ivAddCenterIcon = null;
        ivAddLpDeviceIcon = null;
    }

    @OnClick({R.id.ivLeft, R.id.btnDone, R.id.tvDeviceLinkPage})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.btnDone:
                gotoNextAfterConfirm(true);
                break;
            case R.id.tvDeviceLinkPage: {
                NooieDeviceHelper.trackDNEvent(EventDictionary.EVENT_ID_169);
                Bundle param = new Bundle();
                param.putString(ConstantValue.INTENT_KEY_IPC_MODEL, getDeviceModel());
                param.putInt(ConstantValue.INTENT_KEY_DATA_PARAM_1, ConstantValue.INCORRECT_LIGHT_PAGE_ERROR_TYPE_SUB_DEVICE);
                LpCameraIncorrectLightActivity.toLpCameraIncorrectLightActivity(this, param);
                break;
            }
        }
    }

    @Override
    public void setPresenter(@NonNull TplContract.Presenter presenter) {
        mPresenter = presenter;
    }

    private void showLpCameraView(boolean isAddGateway) {
        if (isAddGateway) {
            ivAddGatewayIcon.setVisibility(View.GONE);
            ivAddCenterIcon.setVisibility(View.GONE);
            ivAddLpDeviceIcon.setVisibility(View.GONE);
            ivAddDeviceIcon.setVisibility(View.VISIBLE);
            tvAddDeviceGuideTip.setVisibility(View.VISIBLE);
            tvDeviceLinkPage.setVisibility(View.VISIBLE);
            tvAddDeviceGuideTip.setText(NooieDeviceHelper.mergeIpcType(getDeviceModel()) == IpcType.EC810PRO ? R.string.add_lp_camera_guide_tip_ec810_pro : R.string.add_lp_camera_guide_tip);
            btnDone.setText(R.string.next);
        } else {
            ivAddGatewayIcon.setVisibility(View.VISIBLE);
            ivAddCenterIcon.setVisibility(View.VISIBLE);
            ivAddLpDeviceIcon.setVisibility(View.VISIBLE);
            ivAddDeviceIcon.setVisibility(View.GONE);
            tvAddDeviceGuideTip.setVisibility(View.VISIBLE);
            tvDeviceLinkPage.setVisibility(View.GONE);
            tvAddDeviceGuideTip.setText(R.string.add_lp_camera_guide_tip_no_hub);
            btnDone.setText(R.string.confirm);
        }
    }

    private void gotoNextAfterConfirm(boolean isAddGateway) {
        if (isAddGateway) {
            MatchLpCameraActivity.toMatchLpCameraActivity(this, getStartParam());
        } else {
            finish();
        }
    }

    private String getDeviceModel() {
        if (getStartParam() == null) {
            return null;
        }
        return getStartParam().getString(ConstantValue.INTENT_KEY_IPC_MODEL);
    }

    private boolean getIsAddGatewayAdd() {
        if (getStartParam() == null) {
            return true;
        }
        return getStartParam().getBoolean(ConstantValue.INTENT_KEY_DATA_PARAM_1, true);
    }

    @Override
    public String getEventId(int trackType) {
        boolean isGatewayAdded = getIntent() != null ? getIntent().getBooleanExtra(ConstantValue.INTENT_KEY_DATA_PARAM, false) : false;
        if (isGatewayAdded) {
            return EventDictionary.EVENT_ID_ACCESS_ADD_LP_DEVICE_CAM_OP;
        } else {
            return EventDictionary.EVENT_ID_ACCESS_LP_SUIT_TIP;
        }
    }

    @Override
    public String getPageId() {
        boolean isGatewayAdded = getIntent() != null ? getIntent().getBooleanExtra(ConstantValue.INTENT_KEY_DATA_PARAM, false) : false;
        if (isGatewayAdded) {
            return EventDictionary.EVENT_PAGE_ADD_LP_CAM_OPERATION;
        } else {
            return EventDictionary.EVENT_PAGE_ADD_LP_CAM_WITHOUT_GATEWAY;
        }
    }

    @Override
    public String getExternal() {
        return NooieDeviceHelper.createDistributionNetworkExternal(false);
    }
}
