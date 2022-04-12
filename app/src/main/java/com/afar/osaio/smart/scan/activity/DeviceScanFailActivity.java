package com.afar.osaio.smart.scan.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.TplContract;
import com.afar.osaio.base.TplPresenter;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.home.activity.HomeActivity;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.widget.helper.ResHelper;
import com.nooie.common.utils.configure.FontUtil;
import com.nooie.data.EventDictionary;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DeviceScanFailActivity extends BaseActivity implements TplContract.View {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.tvDeviceScanFailTip)
    TextView tvDeviceScanFailTip;
    @BindView(R.id.tvDeviceScanFailModel)
    TextView tvDeviceScanFailModel;
    @BindView(R.id.tvDeviceScanFailDeviceId)
    TextView tvDeviceScanFailDeviceId;
    @BindView(R.id.ivDeviceScanFailIcon)
    ImageView ivDeviceScanFailIcon;

    private TplContract.Presenter mPresenter;

    public static void toDeviceScanFailActivity(Context from, Bundle param) {
        Intent intent = new Intent(from, DeviceScanFailActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_PARAM, param);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSlideable(false);
        setContentView(R.layout.activity_device_scan_fail);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    private void initData() {
        new TplPresenter(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.device_scan_fail_title);
        setupUidRepeatTipView();
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
    }

    @OnClick({R.id.ivLeft, R.id.btnDeviceScanFailConfirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                gotoAddSelectDevicePage();
                break;
            case R.id.btnDeviceScanFailConfirm:
                NooieDeviceHelper.trackDNEvent(EventDictionary.EVENT_ID_REPEAT_CLICK_BTN_RETURN);
                gotoHomePage();
                break;
        }
    }

    @Override
    public void setPresenter(@NonNull TplContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onBackPressed() {
        gotoAddSelectDevicePage();
    }

    private void setupUidRepeatTipView() {
        String homeTxt = getString(R.string.device_scan_fail_confirm_btn);
        String text = String.format(getString(R.string.device_scan_fail_tip), getString(R.string.device_scan_fail_confirm_btn), getString(R.string.support_email_address_uuid_repeat));
        SpannableStringBuilder style = new SpannableStringBuilder();

        if (TextUtils.isEmpty(text) || TextUtils.isEmpty(homeTxt)) {
            return;
        }

        int textStart = text.indexOf(homeTxt);
        int textEnd = text.indexOf(homeTxt) + homeTxt.length();

        //设置文字
        style.append(text);
        //设置部分文字点击事件
        ClickableSpan conditionClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                NooieDeviceHelper.trackDNEvent(EventDictionary.EVENT_ID_UUID_REPEAT_CLICK_TEXT_RETURN);
                gotoHomePage();
            }
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setUnderlineText(false);
                ds.setTypeface(FontUtil.loadTypeface(getApplicationContext(), "fonts/manrope-semibold.otf"));
            }
        };
        style.setSpan(conditionClickableSpan, textStart, textEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvDeviceScanFailTip.setText(style);
        //设置部分文字颜色
        ForegroundColorSpan conditionForegroundColorSpan = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.theme_gray));

        if (textStart < textEnd) {
            style.setSpan(conditionForegroundColorSpan, textStart, textEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        //配置给TextView
        tvDeviceScanFailTip.setMovementMethod(LinkMovementMethod.getInstance());
        tvDeviceScanFailTip.setText(style);

        StringBuilder modelSb = new StringBuilder()
                .append(getString(R.string.camera_settings_cam_info_model))
                .append(":")
                .append(NooieDeviceHelper.convertModelToString(getModel()));
        StringBuilder deviceIdSb = new StringBuilder()
                .append(getString(R.string.camera_settings_cam_info_device_id))
                .append(":")
                .append(getDeviceId());

        ivDeviceScanFailIcon.setImageResource(ResHelper.getInstance().getDeviceIconByType(getModel()));
        tvDeviceScanFailModel.setText(modelSb.toString());
        tvDeviceScanFailDeviceId.setText(deviceIdSb.toString());
    }

    private void gotoHomePage() {
        HomeActivity.toHomeActivity(this);
        finish();
    }

    private void gotoAddSelectDevicePage() {
        AddCameraSelectActivity.toAddCameraSelectActivity(this);
        finish();
    }

    private Bundle getParam() {
        if (getCurrentIntent() == null) {
            return null;
        }
        return getCurrentIntent().getBundleExtra(ConstantValue.INTENT_KEY_DATA_PARAM);
    }

    private String getModel() {
        if (getParam() == null) {
            return new String();
        }
        return getParam().getString(ConstantValue.INTENT_KEY_IPC_MODEL);
    }

    private String getDeviceId() {
        if (getParam() == null) {
            return new String();
        }
        return getParam().getString(ConstantValue.INTENT_KEY_DEVICE_ID);
    }

    @Override
    public String getExternal() {
        return NooieDeviceHelper.createDistributionNetworkExternal(false);
    }
}
