package com.afar.osaio.smart.scan.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.nooie.data.EventDictionary;
import com.nooie.sdk.bean.IpcType;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.widget.helper.ResHelper;
import com.nooie.common.utils.graphics.DisplayUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by victor on 2018/11/13
 * Email is victor.qiao.0604@gmail.com
 */
public class NothingHappenActivity extends BaseActivity {
    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.container)
    LinearLayout container;
    @BindView(R.id.ivDistance)
    ImageView ivDistance;
    @BindView(R.id.textView4)
    TextView textView4;

    private String mSsid;
    private String mPsd;
    private IpcType mDeviceType;

    public static void toNothingHappenActivity(Context from, String ssid, String psd, String model, int connectionMode) {
        Intent intent = new Intent(from, NothingHappenActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_SSID, ssid);
        intent.putExtra(ConstantValue.INTENT_KEY_PSD, psd);
        intent.putExtra(ConstantValue.INTENT_KEY_IPC_MODEL, model);
        intent.putExtra(ConstantValue.INTENT_KEY_CONNECTION_MODE, connectionMode);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nothing_happen);
        ButterKnife.bind(this);

        initData();
        setupView();
    }

    private void initData() {
        mSsid = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SSID);
        mPsd = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_PSD);
        mDeviceType = IpcType.getIpcType(getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_IPC_MODEL));
    }

    private void setupView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.add_camera_nothing_happened_title);
        ivRight.setVisibility(View.INVISIBLE);
        /*
        if (mDeviceType == IpcType.IPC_100) {
            textView4.setText(getString(R.string.add_camera_nothing_happened_help_1080));
        } else if (mDeviceType == IpcType.IPC_200) {
            textView4.setText(getString(R.string.add_camera_nothing_happened_help_1080));
        } else if(mDeviceType == IpcType.IPC_1080) {
            textView4.setText(getString(R.string.add_camera_nothing_happened_help_1080));
        } else {
            textView4.setText(getString(R.string.add_camera_nothing_happened_help));
        }
        */
        textView4.setText(getString(R.string.add_camera_nothing_happened_help));
        /*
        if (NooieDeviceHelper.mergeIpcType(mDeviceType) == IpcType.PC530) {
            ivDistance.setImageResource(R.drawable.diagram360);
        } else if (NooieDeviceHelper.mergeIpcType(mDeviceType) == IpcType.PC730) {
            ivDistance.setImageResource(R.drawable.diagram_outdoor);
        } else {
            ivDistance.setImageResource(R.drawable.diagram);
        }
        */
        ivDistance.setImageResource(ResHelper.getInstance().getDeviceScanIconByType(mDeviceType != null ? mDeviceType.getType() : ""));

        /*
        if (mDeviceType == IpcType.EC810PRO) {
            tvTitle.setText(R.string.wifi_qr_code_scan_no_response_ec_810_pro);
            textView4.setText(R.string.nothing_happened_help_ec_810_pro);
            textView4.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            int[] hideViewIds = {R.id.ivDistance, R.id.btnTry, R.id.textView5, R.id.btnSmartLink};
            for (int i = 0; i < hideViewIds.length; i++) {
                findViewById(hideViewIds[i]).setVisibility(View.GONE);
            }
            int margin = com.uuzuche.lib_zxing.DisplayUtil.dip2px(NooieApplication.mCtx, 50);
            int marginStartAndEnd = com.uuzuche.lib_zxing.DisplayUtil.dip2px(NooieApplication.mCtx, 30);
            ((ViewGroup.MarginLayoutParams)textView4.getLayoutParams()).setMargins(marginStartAndEnd, margin, marginStartAndEnd, margin);
        }
         */

        int screen_h = DisplayUtil.SCREEN_HIGHT_PX - DisplayUtil.dpToPx(NooieApplication.mCtx,
                DisplayUtil.HEADER_BAR_HEIGHT_DP) - DisplayUtil.getStatusBarHeight(NooieApplication.mCtx);
        screen_h = Math.max(DisplayUtil.SCREEN_CONTENT_MIN_HEIGHT_PX, screen_h);
        container.setMinimumHeight(screen_h);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseRes();
    }

    @Override
    public void releaseRes() {
        super.releaseRes();
        ivLeft = null;
        tvTitle = null;
        ivRight = null;
        container = null;
    }

    @OnClick({R.id.ivLeft, R.id.btnSmartLink, R.id.btnTry})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.btnSmartLink:
                NooieScanActivity.toNooieScanActivity(NothingHappenActivity.this, mSsid, mPsd, mDeviceType.getType(), getConnectionMode(), null);
                break;
            case R.id.btnTry:
                NooieDeviceHelper.trackDNEvent(EventDictionary.EVENT_ID_CLICK_TRY_AGAIN_AFTER_SCAN_CODE_FAIL);
                finish();
                break;
        }
    }

    private int getConnectionMode() {
        int connectionMode = getCurrentIntent() != null ? getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_QC) : ConstantValue.CONNECTION_MODE_QC;
        return connectionMode;
    }

    @Override
    public String getExternal() {
        return NooieDeviceHelper.createDistributionNetworkExternal(false);
    }
}
