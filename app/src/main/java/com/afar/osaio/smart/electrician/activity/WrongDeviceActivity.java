package com.afar.osaio.smart.electrician.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.electrician.util.ErrorHandleUtil;
import com.afar.osaio.smart.home.activity.HomeActivity;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.IResultCallback;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * NameDeviceActivity
 *
 * @author Administrator
 * @date 2019/3/6
 */
public class WrongDeviceActivity extends BaseActivity {

    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;

    private String mDeviceId;

    public static void toWrongDeviceActivity(Context from, String deviceId) {
        Intent intent = new Intent(from, WrongDeviceActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wrong_device);
        ButterKnife.bind(this);
        initData();
        initView();
    }

    private void initData() {
        mDeviceId  = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.not_exist_device);
    }

    @OnClick({R.id.ivLeft, R.id.btnRemove})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft: {
                finish();
                break;
            }
            case R.id.btnRemove: {
                showLoadingDialog();
                TuyaHomeSdk.newDeviceInstance(mDeviceId).removeDevice(new IResultCallback() {
                    @Override
                    public void onError(String code, String error) {
                        NooieLog.e("------>>> code " + code + "  error " + error);
                        hideLoadingDialog();
                        ErrorHandleUtil.toastTuyaError(WrongDeviceActivity.this, error);
                    }

                    @Override
                    public void onSuccess() {
                        hideLoadingDialog();
                        HomeActivity.toHomeActivity(WrongDeviceActivity.this,HomeActivity.TYPE_REMOVE_DEVICE);
                    }
                });

                break;
            }
        }
    }

}
