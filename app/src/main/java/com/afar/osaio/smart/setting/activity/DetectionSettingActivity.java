package com.afar.osaio.smart.setting.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.util.ConstantValue;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * DetectionSetting
 *
 * @author Administrator
 * @date 2019/4/8
 */
public class DetectionSettingActivity extends BaseActivity {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.containerMotionDetection)
    View containerMotionDetection;
    @BindView(R.id.containerSoundDetection)
    View containerSoundDetection;
    @BindView(R.id.tvMotionDetectionTitle)
    TextView tvMotionDetectionTitle;

    private String mDeviceId;
    private String mModel;

    public static void toDetectionSettingActivity(Context from, String deviceId, String model) {
        Intent intent = new Intent(from, DetectionSettingActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        intent.putExtra(ConstantValue.INTENT_KEY_IPC_MODEL, model);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection_setting);
        ButterKnife.bind(this);
        initData();
        initView();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.detection_setting_title);
        setupDetectionSettingView();
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
            return;
        } else {
            mDeviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
            mModel = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_IPC_MODEL);
        }
    }

    private void setupDetectionSettingView() {
        if (NooieDeviceHelper.isLpDevice(mModel)) {
            containerSoundDetection.setVisibility(View.GONE);
            tvMotionDetectionTitle.setText(R.string.cam_setting_pir_detection);
        }
    }

    @OnClick({R.id.ivLeft, R.id.containerMotionDetection, R.id.containerSoundDetection})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ivLeft: {
                finish();
                break;
            }
            case R.id.containerMotionDetection: {
                if (NooieDeviceHelper.isLpDevice(mModel)) {
                    Bundle param = new Bundle();
                    param.putString(ConstantValue.INTENT_KEY_DEVICE_ID, mDeviceId);
                    param.putInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_QC);
                    DevicePIRActivity.toDevicePIRActivity(this, param);
                } else {
                    NooieDetectionActivity.toNooieDetectionActivity(this, mDeviceId, ConstantValue.NOOIE_DETECT_TYPE_MOTION, true);
                }
                break;
            }
            case R.id.containerSoundDetection: {
                NooieDetectionActivity.toNooieDetectionActivity(this, mDeviceId, ConstantValue.NOOIE_DETECT_TYPE_SOUND, true);
                break;
            }
        }
    }

}
