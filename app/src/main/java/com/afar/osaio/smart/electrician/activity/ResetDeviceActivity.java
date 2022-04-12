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
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * AddDeviceActivity
 *
 * @author jiangzt
 * @date 2019/4/24
 */
public class ResetDeviceActivity extends BaseActivity {

    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.ivStateStep1)
    ImageView ivStateStep1;
    @BindView(R.id.ivStateStep2)
    ImageView ivStateStep2;
    @BindView(R.id.ivStateStep3)
    ImageView ivStateStep3;
    @BindView(R.id.tvResetStep1)
    TextView tvResetStep1;
    @BindView(R.id.tvResetStep2)
    TextView tvResetStep2;
    @BindView(R.id.tvResetStep3)
    TextView tvResetStep3;
    @BindView(R.id.gifIvAirPurifier)
    GifImageView gifIvAirPurifier;

    private String mAddType;

    public static void toResetDeviceActivity(Context from, String addType) {
        Intent intent = new Intent(from, ResetDeviceActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ADD_TYPE, addType);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_device);
        ButterKnife.bind(this);
        setupView();
    }

    private void setupView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.reset_device);
        ivRight.setVisibility(View.INVISIBLE);
        mAddType = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ADD_TYPE);
        if (mAddType.equals(ConstantValue.ADD_DEVICE)) {
            ivStateStep1.setImageResource(R.drawable.device_reset_step1);
            ivStateStep2.setImageResource(R.drawable.power_strip_reset_step2);
            ivStateStep3.setImageResource(R.drawable.device_reset_step3);
        } else if (mAddType.equals(ConstantValue.ADD_POWERSTRIP)) {
            ivStateStep1.setImageResource(R.drawable.power_strip_reset_step1);
            ivStateStep2.setImageResource(R.drawable.power_strip_reset_step2);
            ivStateStep3.setImageResource(R.drawable.power_strip_reset_step3);
        } else if (mAddType.equals(ConstantValue.ADD_SWITCH)) {
            ivStateStep1.setImageResource(R.drawable.power_strip_reset_step1);
            ivStateStep2.setImageResource(R.drawable.power_strip_reset_step2);
            ivStateStep3.setImageResource(R.drawable.power_strip_reset_step3);
            tvResetStep2.setText(R.string.scan_rest_switch_guide_info_2);
        } else if (mAddType.equals(ConstantValue.ADD_LAMP)) {
            ivStateStep1.setImageResource(R.drawable.light_reset_step1);
            ivStateStep2.setImageResource(R.drawable.light_reset_step2);
            ivStateStep3.setImageResource(R.drawable.light_reset_step3);
            tvResetStep1.setText(R.string.light_scan_reset_device_guide_info_1);
            tvResetStep2.setText(R.string.light_scan_reset_device_guide_info_2);
            tvResetStep3.setText(R.string.light_scan_reset_device_guide_info_3);
        } else if (mAddType.equals(ConstantValue.ADD_LIGHT_STRIP)) {
            ivStateStep1.setImageResource(R.drawable.light_strip_reset_step1);
            ivStateStep2.setImageResource(R.drawable.light_strip_reset_step2);
            ivStateStep3.setImageResource(R.drawable.light_strip_reset_step3);
            tvResetStep1.setText(R.string.light_strip_reset_device_guide_info_1);
            tvResetStep2.setText(R.string.light_scan_reset_device_guide_info_2);
            tvResetStep3.setText(R.string.light_scan_reset_device_guide_info_3);
        } else if (mAddType.equals(ConstantValue.ADD_LIGHT_MODULATOR)) {
            ivStateStep1.setImageResource(R.drawable.light_modulator_reset_step1);
            ivStateStep2.setImageResource(R.drawable.power_strip_reset_step2);
            ivStateStep3.setImageResource(R.drawable.power_strip_reset_step3);
        } else if (mAddType.equals(ConstantValue.ADD_PET_FEEDER)) {
            tvResetStep2.setText(R.string.reset_feed_pet);
            tvResetStep3.setText(R.string.scan_rest_feeder_guide_info_3);
            ivStateStep2.setImageResource(R.drawable.img_mode_default);
            ivStateStep3.setImageResource(R.drawable.ap_third_default);
        } else if (mAddType.equals(ConstantValue.ADD_AIR_PURIFIER)) {
            tvResetStep2.setText(R.string.reset_air_purifier_tips2);
            tvResetStep3.setText(R.string.reset_air_purifier_tips3);
            ivStateStep2.setImageResource(R.drawable.ap_third_purifier);
            ivStateStep3.setImageResource(R.drawable.ap_third_default);
            ivStateStep3.setVisibility(View.GONE);
            gifIvAirPurifier.setVisibility(View.VISIBLE);
            try {
                GifDrawable gifDrawable = new GifDrawable(getResources(), R.raw.ic_purifier_flash);
                gifIvAirPurifier.setImageDrawable(gifDrawable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @OnClick({R.id.ivLeft, R.id.btnReset})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.btnReset:
                finish();
              //  InputWiFiPsdActivity.toInputWiFiPsdActivity(this, ConstantValue.EC_MODE, mAddType);
                break;
        }
    }
}
