package com.afar.osaio.smart.router;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.afar.osaio.R;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.widget.InputFrameView;
import com.afar.osaio.widget.NEventFButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RouterResetNameActivity extends RouterBaseActivity {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ipvPsd)
    InputFrameView ipvPsd;
    @BindView(R.id.btnDone)
    NEventFButton btnDone;
    @BindView(R.id.ivRouter)
    ImageView ivRouter;

    private static int DEFAULT_INPUT_TEXT_MAX_LEN = 30;
    private String overFlag = "";
    private String routerName = "";

    public static void toRouterResetNameActivity (Context from) {
        Intent intent = new Intent(from, RouterResetNameActivity.class);
        from.startActivity(intent);
    }

    public static void toRouterResetNameActivity(Activity activity, String overFlag, String routerName) {
        Intent intent = new Intent(activity, RouterResetNameActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_SETTING, overFlag);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_NAME, routerName);
        activity.startActivityForResult(intent, 1);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router_reset_name);
        ButterKnife.bind(this);

        initView();
        initData();
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.router_device_name);
        setupInputFrameView();
    }

    private void initData() {
        overFlag = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_SETTING);
        routerName = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_NAME);
        if (routerName != null) {
            ipvPsd.setEtInputText(routerName);
            btnDone.setText(getString(R.string.save));
            btnDone.setEnabled(true);
            btnDone.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
        }

        if ("device".equals(overFlag)) {
            ivRouter.setImageResource(R.drawable.ic_connect_device);
        }
    }

    private void setupInputFrameView() {
        ipvPsd.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT)
                .setInputTitle(getString(R.string.router_device_name))
                .setTextSSIDWatcher()
                .setEtInputToggle(true)
                .setEtInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                .setInputBtnType(InputFrameView.INPUT_BTN_TYPE_TEXT)
                .setEtInputMaxLength(DEFAULT_INPUT_TEXT_MAX_LEN)
                .setInputBtnIsShow(true)
                .setTextInputBtnColor(R.color.gray_cc616161)
                .setTextInputBtn(getTextLengthTip(0, DEFAULT_INPUT_TEXT_MAX_LEN))
                .setOnClickListener(new InputFrameView.OnInputFrameClickListener() {
                    @Override
                    public void onInputBtnClick() {
                    }

                    @Override
                    public void onEditorAction() {
                        hideInputMethod();
                        //onViewClicked(btnDone);
                    }

                    @Override
                    public void onEtInputClick() {
                    }
                })
                .setInputTextChangeListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String psdStr = ipvPsd.getInputTextNoTrim();
                        int psdLen = psdStr != null ? psdStr.length() : 0;
                        updateInputFrameTextLengthTip(psdLen);
                        checkBtnEnable();
                    }
                });
        checkBtnEnable();
    }

    private String getTextLengthTip(int currentLength, int maxLength) {
        if (currentLength < 0 || maxLength < 1) {
            return new String();
        }
        if (currentLength > maxLength) {
            return new StringBuilder().append(maxLength).append("/").append(maxLength).toString();
        }
        return new StringBuilder().append(currentLength).append("/").append(maxLength).toString();
    }

    public void checkBtnEnable() {
        String psdStr = ipvPsd.getInputTextNoTrim();
        int psdLen = psdStr != null ? psdStr.length() : 0;
        if (/*psdLen == 0 || */psdLen > 0/*ConstantValue.DEVICE_WIFI_MIN_LEN*/) {
            btnDone.setEnabled(true);
            btnDone.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
        } else {
            btnDone.setEnabled(false);
            btnDone.setTextColor(getResources().getColor(R.color.unable_clickable_color));
        }
    }

    public void updateInputFrameTextLengthTip(int currentLength) {
        if (ipvPsd != null) {
            ipvPsd.setTextInputBtn(getTextLengthTip(currentLength, DEFAULT_INPUT_TEXT_MAX_LEN));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @OnClick({R.id.ivLeft, R.id.btnDone})
    public void onViewClicked(View v) {
        switch(v.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.btnDone:
                hideInputMethod();
                if ("setting".equals(overFlag) || "device".equals(overFlag)) {
                    setIntentResult();
                } else {
                    String psdStr = ipvPsd.getInputTextNoTrim();
                    RouterSetWifiActivity.toRouterSetWifiActivity(this, psdStr);
                }
                break;
        }
    }

    private void setIntentResult() {
        String psdStr = ipvPsd.getInputTextNoTrim();
        Intent intent = new Intent();
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_NAME, psdStr);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void showLoadingDialog() {
        showLoading("");
    }

    @Override
    public void hideLoadingDialog() {
        hideLoading();
    }
}
