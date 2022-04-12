package com.afar.osaio.smart.router;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.routerdata.GetRouterDataFromCloud;
import com.afar.osaio.routerdata.SendHttpRequest;
import com.afar.osaio.smart.scan.activity.ResetRouterHelpActivity;
import com.afar.osaio.util.DialogUtils;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.util.preference.GlobalPrefs;
import com.afar.osaio.widget.InputFrameView;
import com.afar.osaio.widget.NEventFButton;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 路由器 Admin Password 设置页
 * 资源的最后一次提交
 */
public class RecoverRouterBackupActivity extends RouterBaseActivity implements SendHttpRequest.getRouterReturnInfo {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ipvPsd)
    InputFrameView ipvPsd;
    @BindView(R.id.btnDone)
    NEventFButton btnDone;
    @BindView(R.id.forgetPsd)
    TextView forgetPsd;

    public static void toRecoverRouterBackupActivity(Context from) {
        Intent intent = new Intent(from, RecoverRouterBackupActivity.class);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover_router_backup);
        ButterKnife.bind(this);

        initView();
        initData();
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.router_admin_password_title);
        setupInputFrameView();
    }

    private void initData() {
        GlobalPrefs prefs = GlobalPrefs.getPreferences(NooieApplication.mCtx);
        String routerName = prefs.getRouterNameBackup();
//        tvTitle.setText(routerName);
    }

    private void setupInputFrameView() {
        ipvPsd.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT)
                .setInputTitle(getString(R.string.router_admin_password))
                .setTextPasswordWatcher()
                .setEtInputToggle(true)
                .setEtInputMaxLength(32)
                .setBtnInputView(true)
                .setInputBtn(R.drawable.eye_close_icon_state_list)
                /*.setInputBtn(R.drawable.eye_open_icon_state_list)
                .setEtInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)*/
                .setOnClickListener(new InputFrameView.OnInputFrameClickListener() {
                    @Override
                    public void onInputBtnClick() {
                    }

                    @Override
                    public void onEditorAction() {
                        hideInputMethod();
                        onViewClicked(btnDone);
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
                        checkBtnEnable();
                    }
                });
        checkBtnEnable();
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

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @OnClick({R.id.ivLeft, R.id.btnDone, R.id.forgetPsd})
    public void onViewClicked(View v) {
        switch (v.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.forgetPsd:
                showForgotPasswordDialog();
                break;
            case R.id.btnDone:
                hideInputMethod();
                String psdStr = ipvPsd.getInputTextNoTrim();
                getCheckPasswordResult(psdStr);
                break;
        }
    }

    /**
     * hide input method
     */
    protected void hideInputMethod() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }
    }

    private void getCheckPasswordResult(String psdStr) {
        showLoadingDialog();
        GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
        try {
            routerDataFromCloud.getCheckPasswordResult(psdStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Dialog mShowForgotPasswordDialog;

    private void showForgotPasswordDialog() {
        hideForgotPasswordDialog();
        mShowForgotPasswordDialog = DialogUtils.forgotPasswordDialog(this, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                ResetRouterHelpActivity.toResetRouterHelpActivity(RecoverRouterBackupActivity.this);
            }

            @Override
            public void onClickLeft() {

            }
        });
    }

    private void hideForgotPasswordDialog() {
        if (mShowForgotPasswordDialog != null) {
            mShowForgotPasswordDialog.dismiss();
            mShowForgotPasswordDialog = null;
        }
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                ConnectionRouterActivity.toConnectionRouterActivity(RecoverRouterBackupActivity.this);
                saveRouterInfo();
            } else if (msg.what == 2) {
                String text = String.format(getString(R.string.router_admin_password_error), ipvPsd.getInputTextNoTrim());
                ToastUtil.showToast(RecoverRouterBackupActivity.this, text);
            }
        }
    };

    /**
     * 保存路由器信息,
     */
    private void saveRouterInfo() {
        String routerPassword = ipvPsd.getInputTextNoTrim();
        GlobalPrefs prefs = GlobalPrefs.getPreferences(NooieApplication.mCtx);
        prefs.setRouterAdminPassword(routerPassword, false);
    }

    @Override
    public void routerReturnInfo(String info, String topicurlString) {
        hideLoadingDialog();
        try {
            if (!"error".equals(info) && "getCheckPasswordResult".equals(topicurlString)) {
                Message message = new Message();
                if ("OK".equals(new JSONObject(info).getString("checkPassword"))) {
                    message.what = 1;
                } else {
                    message.what = 2;
                }
                handler.sendMessage(message);
            } else if ("error".equals(info) || "".equals(info)) {
                ToastUtil.showToast(this, getString(R.string.router_firmware_version));
            }
        } catch (JSONException e) {
            ToastUtil.showToast(this, getString(R.string.router_firmware_version));
            e.printStackTrace();
        }
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
