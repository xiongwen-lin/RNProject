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
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.lpipc.contract.AddLowPowerIpcContract;
import com.afar.osaio.smart.lpipc.presenter.AddLowPowerIpcPresenter;
import com.afar.osaio.util.ConstantValue;
import com.nooie.data.EventDictionary;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddLowPowerIpcActivity extends BaseActivity implements AddLowPowerIpcContract.View {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.tvRight)
    TextView tvRight;
    @BindView(R.id.tvDeviceLinkPage)
    TextView tvDeviceLinkPage;

    private AddLowPowerIpcContract.Presenter mPresenter;

    public static void toAddLowPowerIpcActivity(Context from, Bundle param) {
        Intent intent = new Intent(from, AddLowPowerIpcActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_START_PARAM, param);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_low_power_ipc);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    private void initData() {
        new AddLowPowerIpcPresenter(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.add_low_power_ipc_title);
        tvRight.setVisibility(View.VISIBLE);
        tvRight.setText(R.string.add_low_power_ipic_right_btn);
        tvRight.setVisibility(View.GONE);
        //tvDeviceLinkPage.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
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
        release();
    }

    private void release() {
    }

    @OnClick({R.id.ivLeft, R.id.tvRight, R.id.tvDeviceLinkPage, R.id.btnDone})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.tvRight: {
                AddLpCameraActivity.toAddLpCameraActivity(this, getStartParam());
                break;
            }
            case R.id.tvDeviceLinkPage:
                break;
            case R.id.btnDone:
                NooieDeviceHelper.trackDNEvent(EventDictionary.EVENT_ID_167);
                AddGatewayActivity.toAddGatewayActivity(this, getStartParam());
                break;
        }
    }

    @Override
    public void setPresenter(@NonNull AddLowPowerIpcContract.Presenter presenter) {
        mPresenter = presenter;
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
