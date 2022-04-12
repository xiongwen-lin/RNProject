package com.afar.osaio.smart.electrician.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.util.ConstantValue;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HotSpotTipsActivity extends BaseActivity {
    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.ivStateStep1)
    ImageView ivStateStep1;
    @BindView(R.id.ivStateStep2)
    ImageView ivStateStep2;
    @BindView(R.id.ivStateStep3)
    ImageView ivStateStep3;
    @BindView(R.id.ivStateStep4)
    ImageView ivStateStep4;
    @BindView(R.id.tvApModeStep1)
    TextView tvApModeStep1;
    @BindView(R.id.tvApModeStep2)
    TextView tvApModeStep2;
    @BindView(R.id.tvApModeStep3)
    TextView tvApModeStep3;
    @BindView(R.id.tvApModeStep4)
    TextView tvApModeStep4;


    private String mAddType;

    public static void toHotSpotTipsActivity(Context from, String addType) {
        Intent intent = new Intent(from, HotSpotTipsActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ADD_TYPE, addType);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_in_hot_spot_tips);
        ButterKnife.bind(this);
        setupView();
        initData();
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
        } else {
            mAddType = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ADD_TYPE);
            if (mAddType.equals(ConstantValue.ADD_DEVICE)) {
                ivStateStep1.setImageResource(R.drawable.device_reset_step1);
                ivStateStep2.setImageResource(R.drawable.power_strip_reset_step2);
                ivStateStep3.setImageResource(R.drawable.device_reset_step3);
                ivStateStep4.setImageResource(R.drawable.device_reset_step4);
                ivStateStep4.setVisibility(View.VISIBLE);
            } else if (mAddType.equals(ConstantValue.ADD_POWERSTRIP)) {
                ivStateStep1.setImageResource(R.drawable.power_strip_reset_step1);
                ivStateStep2.setImageResource(R.drawable.power_strip_reset_step2);
                ivStateStep3.setImageResource(R.drawable.power_strip_reset_step3);
                ivStateStep4.setImageResource(R.drawable.power_strip_reset_step4);
                ivStateStep4.setVisibility(View.VISIBLE);
            } else if (mAddType.equals(ConstantValue.ADD_SWITCH)) {
                ivStateStep1.setImageResource(R.drawable.power_strip_reset_step1);
                ivStateStep2.setImageResource(R.drawable.power_strip_reset_step2);
                ivStateStep3.setImageResource(R.drawable.power_strip_reset_step3);
                ivStateStep4.setImageResource(R.drawable.power_strip_reset_step4);
                ivStateStep4.setVisibility(View.VISIBLE);
                tvApModeStep2.setText(R.string.scan_rest_switch_guide_info_2);
                tvApModeStep4.setText(R.string.scan_rest_switch_guide_info_4);
            } else if (mAddType.equals(ConstantValue.ADD_LAMP)) {
                ivStateStep1.setImageResource(R.drawable.light_reset_step1);
                ivStateStep2.setImageResource(R.drawable.light_reset_step2);
                ivStateStep3.setImageResource(R.drawable.light_reset_step3);
                ivStateStep4.setImageResource(R.drawable.light_reset_step4);
                ivStateStep4.setVisibility(View.VISIBLE);
                tvApModeStep1.setText(R.string.light_scan_reset_device_guide_info_1);
                tvApModeStep2.setText(R.string.light_scan_reset_device_guide_info_2);
                tvApModeStep3.setText(R.string.light_scan_reset_device_guide_info_3);
                tvApModeStep4.setText(R.string.light_scan_reset_device_guide_info_4);
            } else if (mAddType.equals(ConstantValue.ADD_LIGHT_STRIP)) {
                ivStateStep1.setImageResource(R.drawable.light_strip_reset_step1);
                ivStateStep2.setImageResource(R.drawable.light_strip_reset_step2);
                ivStateStep3.setImageResource(R.drawable.light_strip_reset_step3);
                ivStateStep4.setImageResource(R.drawable.light_strip_reset_step4);
                ivStateStep4.setVisibility(View.VISIBLE);
                tvApModeStep1.setText(R.string.light_strip_reset_device_guide_info_1);
                tvApModeStep2.setText(R.string.light_scan_reset_device_guide_info_2);
                tvApModeStep3.setText(R.string.light_scan_reset_device_guide_info_3);
                tvApModeStep4.setText(R.string.light_strip_scan_reset_device_guide_info_4);
            } else if (mAddType.equals(ConstantValue.ADD_LIGHT_MODULATOR)) {
                ivStateStep1.setImageResource(R.drawable.light_modulator_reset_step1);
                ivStateStep2.setImageResource(R.drawable.power_strip_reset_step2);
                ivStateStep3.setImageResource(R.drawable.power_strip_reset_step3);
                ivStateStep4.setImageResource(R.drawable.power_strip_reset_step4);
                ivStateStep4.setVisibility(View.VISIBLE);
            } else if (mAddType.equals(ConstantValue.ADD_PET_FEEDER)) {
                tvApModeStep2.setText(R.string.reset_feed_pet);
                tvApModeStep3.setText(R.string.scan_rest_feeder_guide_info_3);
                tvApModeStep4.setVisibility(View.GONE);
                ivStateStep2.setImageResource(R.drawable.img_mode_default);
                ivStateStep3.setImageResource(R.drawable.ap_third_default);
            }
        }
    }

    private void setupView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.connect_in_AP_mode);
        ivRight.setVisibility(View.INVISIBLE);
    }

    @OnClick({R.id.ivLeft, R.id.btnNext})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
               finish();
                break;
            case R.id.btnNext:
                InputWiFiPsdActivity.toInputWiFiPsdActivity(this, ConstantValue.AP_MODE, mAddType);
                break;
        }
    }

}
