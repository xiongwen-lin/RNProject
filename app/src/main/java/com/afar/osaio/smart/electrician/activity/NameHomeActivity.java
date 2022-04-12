package com.afar.osaio.smart.electrician.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.electrician.presenter.INameHomePresenter;
import com.afar.osaio.smart.electrician.presenter.NameHomePresenter;
import com.afar.osaio.smart.electrician.util.CommonUtil;
import com.afar.osaio.smart.electrician.util.ErrorHandleUtil;
import com.afar.osaio.smart.electrician.view.INameHomeView;
import com.afar.osaio.smart.electrician.widget.InputFrameView;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.widget.FButton;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NameHomeActivity extends BaseActivity implements INameHomeView {

    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ipvHomeName)
    InputFrameView ipvHomeName;
    @BindView(R.id.btnDone)
    FButton btnDone;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;

    INameHomePresenter mNameHomePresenter;
    private long mHomeId;
    private int mHomeType;
    private String mHomeName;

    public static void toNameHomeActivity(Activity from, int requestCode, long homeId, String homeName) {
        Intent intent = new Intent(from, NameHomeActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_HOME_TYPE, requestCode);
        intent.putExtra(ConstantValue.INTENT_KEY_HOME_ID, homeId);
        intent.putExtra(ConstantValue.INTENT_KEY_HOME_NAME, homeName);
        from.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_home);
        ButterKnife.bind(this);
        initData();
        initView();
    }

    public void initView() {
        tvTitle.setText(R.string.name_your_home);
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        ipvHomeName.setInputTitle(getResources().getString(R.string.name))
                .setInputBtn(R.drawable.close_icon_state_list)
                .setEtInputType(InputType.TYPE_CLASS_TEXT)
                .setOnClickListener(new InputFrameView.OnInputFrameClickListener() {
                    @Override
                    public void onInputBtnClick() {
                        ipvHomeName.setEtInputText("");
                    }

                    @Override
                    public void onEditorAction() {
                        hideInputMethod();
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

        ipvHomeName.setInputMaxLen(ConstantValue.HOME_NAME_MAX_LENGTH);
        checkBtnEnable();
    }

    public void initData() {
        if (getCurrentIntent() == null) {
            finish();
        } else {
            mHomeId = getCurrentIntent().getLongExtra(ConstantValue.INTENT_KEY_HOME_ID, 0);
            mHomeType = getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_HOME_TYPE, 0);
            mHomeName = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_HOME_NAME);
            mNameHomePresenter = new NameHomePresenter(this);
            if (mHomeName != null) {
                ipvHomeName.setEtInputText(mHomeName);
            }
        }
    }


    public void checkBtnEnable() {
        if (!TextUtils.isEmpty(ipvHomeName.getInputText().toString().trim()) && !TextUtils.isEmpty(ipvHomeName.getInputText().toString().trim())) {
            btnDone.setEnabled(true);
            btnDone.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
        } else {
            btnDone.setEnabled(false);
            btnDone.setTextColor(getResources().getColor(R.color.unable_clickable_color));
        }
    }


    @OnClick({R.id.ivLeft, R.id.btnDone})
    public void onViewClick(View view) {
        if (CommonUtil.isFastClick()) {
            return;
        }
        switch (view.getId()) {
            case R.id.ivLeft: {
                finish();
                break;
            }
            case R.id.btnDone: {
                if (ipvHomeName.getInputText().length() > ConstantValue.MAX_FAMILY_NAME_LENGTH) {
                    ToastUtil.showToast(NameHomeActivity.this, getString(R.string.name_too_long));
                } else if (mHomeType == ConstantValue.REQUEST_CODE_ADD_HOME) {
                    List<String> roomList = new ArrayList<>();
                    roomList.add("客厅");
                    roomList.add("主卧");
                    mNameHomePresenter.createHome(ipvHomeName.getInputText(), roomList);
                    showLoadingDialog();
                } else {
                    if (ipvHomeName.getInputText().equals(mHomeName)) {
                        finish();
                    } else {
                        mNameHomePresenter.updateHome(TuyaHomeSdk.getDataInstance().getHomeBean(mHomeId), ipvHomeName.getInputText());
                        showLoadingDialog();
                    }
                }
                break;
            }
        }
    }

    @Override
    public void notifyUpdateHomeState(String msg) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(msg)) {
            setResult(RESULT_OK, new Intent());
            finish();
        } else {
            hideLoadingDialog();
            ErrorHandleUtil.toastTuyaError(this, msg);
        }
    }

    @Override
    public void notifyCreateHomeSuccess(HomeBean homeBean) {
        if (homeBean != null) {
            Intent intent = new Intent();
            intent.putExtra(ConstantValue.INTENT_KEY_ADD_HOME_ID, homeBean.getHomeId());
            setResult(RESULT_OK, intent);
        }
        finish();
    }

    @Override
    public void notifyCreateHomeFailed(String msg) {
        hideLoadingDialog();
        ErrorHandleUtil.toastTuyaError(this, msg);
    }
}
