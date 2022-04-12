package com.afar.osaio.smart.lpipc.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.TplContract;
import com.afar.osaio.base.TplPresenter;
import com.afar.osaio.smart.scan.activity.InputWiFiPsdActivity;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.widget.helper.ResHelper;
import com.nooie.sdk.bean.IpcType;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddProLpCameraActivity extends BaseActivity implements TplContract.View {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.tvProDeviceLinkPage)
    TextView tvProDeviceLinkPage;
    @BindView(R.id.ivAddProDeviceIcon)
    ImageView ivAddProDeviceIcon;

    private TplContract.Presenter mPresenter;

    public static void toAddProLpCameraActivity(Context from, Bundle param) {
        Intent intent = new Intent(from, AddProLpCameraActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_START_PARAM, param);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pro_lp_camera);
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
        //tvProDeviceLinkPage.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        ivAddProDeviceIcon.setImageResource(ResHelper.getInstance().getFlashLightOnIconByType(getDeviceModel()));
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
        tvProDeviceLinkPage = null;
    }

    @OnClick({R.id.ivLeft, R.id.btnDone, R.id.tvProDeviceLinkPage})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.btnDone: {
                Bundle param = new Bundle();
                param.putString(ConstantValue.INTENT_KEY_IPC_MODEL, getDeviceModel());
                param.putInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_QC);
                InputWiFiPsdActivity.toInputWiFiPsdActivity(this, param);
                break;
            }
            case R.id.tvProDeviceLinkPage: {
                Bundle param = new Bundle();
                param.putString(ConstantValue.INTENT_KEY_IPC_MODEL, getDeviceModel());
                param.putInt(ConstantValue.INTENT_KEY_DATA_PARAM_1, ConstantValue.INCORRECT_LIGHT_PAGE_ERROR_TYPE_ROUTER_DEVICE);
                LpCameraIncorrectLightActivity.toLpCameraIncorrectLightActivity(this, param);
                break;
            }
        }
    }

    @Override
    public void setPresenter(@NonNull TplContract.Presenter presenter) {
        mPresenter = presenter;
    }

    private String getDeviceModel() {
        if (getStartParam() == null) {
            return IpcType.EC810PRO.getType();
        }
        return getStartParam().getString(ConstantValue.INTENT_KEY_IPC_MODEL);
    }
}
