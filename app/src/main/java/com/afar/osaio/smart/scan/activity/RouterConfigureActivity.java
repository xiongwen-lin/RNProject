package com.afar.osaio.smart.scan.activity;

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
import com.afar.osaio.util.ConstantValue;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RouterConfigureActivity extends BaseActivity implements TplContract.View {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.tvRouterConfigureTopTip_1)
    TextView tvRouterConfigureTopTip_1;
    @BindView(R.id.tvRouterConfigureStep_1)
    TextView tvRouterConfigureStep_1;
    @BindView(R.id.ivRouterConfigureStp_1)
    ImageView ivRouterConfigureStp_1;
    @BindView(R.id.tvRouterConfigureStep_2)
    TextView tvRouterConfigureStep_2;
    @BindView(R.id.ivRouterConfigureStp_2)
    ImageView ivRouterConfigureStp_2;
    @BindView(R.id.tvRouterConfigureStep_3)
    TextView tvRouterConfigureStep_3;
    @BindView(R.id.ivRouterConfigureStp_3)
    ImageView ivRouterConfigureStp_3;
    @BindView(R.id.tvRouterConfigureStep_4)
    TextView tvRouterConfigureStep_4;
    @BindView(R.id.ivRouterConfigureStp_4)
    ImageView ivRouterConfigureStp_4;
    @BindView(R.id.tvRouterConfigureBottomTip_1)
    TextView tvRouterConfigureBottomTip_1;
    @BindView(R.id.tvRouterConfigureBottomTip_2)
    TextView tvRouterConfigureBottomTip_2;

    private TplContract.Presenter mPresenter;

    public static void toRouterConfigureActivity(Context from, String title, int type) {
        Intent intent = new Intent(from, RouterConfigureActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_TITLE, title);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_TYPE, type);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router_configure);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            return;
        }
        new TplPresenter(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_TITLE));
        setupConfigureView(getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_DATA_TYPE, ConstantValue.ROUTER_TYPE_NET_GEAR));
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeData();
    }

    private void resumeData() {
    }

    private void setupConfigureView(int type) {
        switch (type) {
            case ConstantValue.ROUTER_TYPE_NET_GEAR : {
                tvRouterConfigureTopTip_1.setVisibility(View.GONE);
                tvRouterConfigureStep_1.setVisibility(View.VISIBLE);
                ivRouterConfigureStp_1.setVisibility(View.VISIBLE);
                tvRouterConfigureStep_2.setVisibility(View.VISIBLE);
                ivRouterConfigureStp_2.setVisibility(View.VISIBLE);
                tvRouterConfigureStep_3.setVisibility(View.VISIBLE);
                ivRouterConfigureStp_3.setVisibility(View.VISIBLE);
                tvRouterConfigureStep_4.setVisibility(View.GONE);
                ivRouterConfigureStp_4.setVisibility(View.GONE);
                tvRouterConfigureBottomTip_1.setVisibility(View.VISIBLE);
                tvRouterConfigureBottomTip_2.setVisibility(View.VISIBLE);

                tvRouterConfigureTopTip_1.setText("");
                tvRouterConfigureStep_1.setText(R.string.router_configure_step_net_gear_1);
                ivRouterConfigureStp_1.setImageResource(R.drawable.router_netgear_1);
                tvRouterConfigureStep_2.setText(R.string.router_configure_step_net_gear_2);
                ivRouterConfigureStp_2.setImageResource(R.drawable.router_netgear_2);
                tvRouterConfigureStep_3.setText(R.string.router_configure_step_net_gear_3);
                ivRouterConfigureStp_3.setImageResource(R.drawable.router_netgear_3);
                //tvRouterConfigureStep_4.setText("");
                //ivRouterConfigureStp_4.setImageResource(R.drawable.device_icon);
                tvRouterConfigureBottomTip_1.setText(R.string.router_configure_bottom_tip_tp_link_1);
                tvRouterConfigureBottomTip_2.setText(R.string.router_configure_bottom_tip_tp_link_2);
                break;
            }
            case ConstantValue.ROUTER_TYPE_ASUS : {
                tvRouterConfigureTopTip_1.setVisibility(View.GONE);
                tvRouterConfigureStep_1.setVisibility(View.VISIBLE);
                ivRouterConfigureStp_1.setVisibility(View.VISIBLE);
                tvRouterConfigureStep_2.setVisibility(View.VISIBLE);
                ivRouterConfigureStp_2.setVisibility(View.VISIBLE);
                tvRouterConfigureStep_3.setVisibility(View.VISIBLE);
                ivRouterConfigureStp_3.setVisibility(View.VISIBLE);
                tvRouterConfigureStep_4.setVisibility(View.VISIBLE);
                ivRouterConfigureStp_4.setVisibility(View.VISIBLE);
                tvRouterConfigureBottomTip_1.setVisibility(View.VISIBLE);
                tvRouterConfigureBottomTip_2.setVisibility(View.VISIBLE);

                tvRouterConfigureTopTip_1.setText("");
                tvRouterConfigureStep_1.setText(R.string.router_configure_step_asus_1);
                ivRouterConfigureStp_1.setImageResource(R.drawable.router_asus_1);
                tvRouterConfigureStep_2.setText(R.string.router_configure_step_asus_2);
                ivRouterConfigureStp_2.setImageResource(R.drawable.router_asus_2);
                tvRouterConfigureStep_3.setText(R.string.router_configure_step_asus_3);
                ivRouterConfigureStp_3.setImageResource(R.drawable.router_asus_3);
                tvRouterConfigureStep_4.setText(R.string.router_configure_step_asus_4);
                ivRouterConfigureStp_4.setImageResource(R.drawable.router_asus_4);
                tvRouterConfigureBottomTip_1.setText(R.string.router_configure_bottom_tip_tp_link_1);
                tvRouterConfigureBottomTip_2.setText(R.string.router_configure_bottom_tip_tp_link_2);
                break;
            }
            case ConstantValue.ROUTER_TYPE_D_LINK : {
                tvRouterConfigureTopTip_1.setVisibility(View.GONE);
                tvRouterConfigureStep_1.setVisibility(View.VISIBLE);
                ivRouterConfigureStp_1.setVisibility(View.VISIBLE);
                tvRouterConfigureStep_2.setVisibility(View.VISIBLE);
                ivRouterConfigureStp_2.setVisibility(View.VISIBLE);
                tvRouterConfigureStep_3.setVisibility(View.VISIBLE);
                ivRouterConfigureStp_3.setVisibility(View.GONE);
                tvRouterConfigureStep_4.setVisibility(View.GONE);
                ivRouterConfigureStp_4.setVisibility(View.GONE);
                tvRouterConfigureBottomTip_1.setVisibility(View.VISIBLE);
                tvRouterConfigureBottomTip_2.setVisibility(View.VISIBLE);

                tvRouterConfigureTopTip_1.setText("");
                tvRouterConfigureStep_1.setText(R.string.router_configure_step_d_link_1);
                ivRouterConfigureStp_1.setImageResource(R.drawable.router_dlink_1);
                tvRouterConfigureStep_2.setText(R.string.router_configure_step_d_link_2);
                ivRouterConfigureStp_2.setImageResource(R.drawable.router_dlink_2);
                tvRouterConfigureStep_3.setText(R.string.router_configure_step_d_link_3);
                //ivRouterConfigureStp_3.setImageResource(R.drawable.router_netgear_3);
                //tvRouterConfigureStep_4.setText("");
                //ivRouterConfigureStp_4.setImageResource(R.drawable.device_icon);
                tvRouterConfigureBottomTip_1.setText(R.string.router_configure_bottom_tip_tp_link_1);
                tvRouterConfigureBottomTip_2.setText(R.string.router_configure_bottom_tip_tp_link_2);
                break;
            }
            case ConstantValue.ROUTER_TYPE_TP_LINK : {
                tvRouterConfigureTopTip_1.setVisibility(View.VISIBLE);
                tvRouterConfigureStep_1.setVisibility(View.VISIBLE);
                ivRouterConfigureStp_1.setVisibility(View.VISIBLE);
                tvRouterConfigureStep_2.setVisibility(View.VISIBLE);
                ivRouterConfigureStp_2.setVisibility(View.VISIBLE);
                tvRouterConfigureStep_3.setVisibility(View.VISIBLE);
                ivRouterConfigureStp_3.setVisibility(View.VISIBLE);
                tvRouterConfigureStep_4.setVisibility(View.VISIBLE);
                ivRouterConfigureStp_4.setVisibility(View.VISIBLE);
                tvRouterConfigureBottomTip_1.setVisibility(View.VISIBLE);
                tvRouterConfigureBottomTip_2.setVisibility(View.VISIBLE);

                tvRouterConfigureTopTip_1.setText(R.string.router_configure_top_tip_tp_link_1);
                tvRouterConfigureStep_1.setText(R.string.router_configure_step_tp_link_1);
                ivRouterConfigureStp_1.setImageResource(R.drawable.router_tplink_1);
                tvRouterConfigureStep_2.setText(R.string.router_configure_step_tp_link_2);
                ivRouterConfigureStp_2.setImageResource(R.drawable.router_tplink_2);
                tvRouterConfigureStep_3.setText(R.string.router_configure_step_tp_link_3);
                ivRouterConfigureStp_3.setImageResource(R.drawable.router_tplink_3);
                tvRouterConfigureStep_4.setText(R.string.router_configure_step_tp_link_4);
                ivRouterConfigureStp_4.setImageResource(R.drawable.router_tplink_4);
                tvRouterConfigureBottomTip_1.setText(R.string.router_configure_bottom_tip_tp_link_1);
                tvRouterConfigureBottomTip_2.setText(R.string.router_configure_bottom_tip_tp_link_2);
                break;
            }
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
        tvRouterConfigureTopTip_1 = null;
        tvRouterConfigureStep_1 = null;
        ivRouterConfigureStp_1 = null;
        tvRouterConfigureStep_2 = null;
        ivRouterConfigureStp_2 = null;
        tvRouterConfigureStep_3 = null;
        ivRouterConfigureStp_3 = null;
        tvRouterConfigureStep_4 = null;
        ivRouterConfigureStp_4 = null;
        tvRouterConfigureBottomTip_1 = null;
        tvRouterConfigureBottomTip_2 = null;
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
    public void setPresenter(@NonNull TplContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
