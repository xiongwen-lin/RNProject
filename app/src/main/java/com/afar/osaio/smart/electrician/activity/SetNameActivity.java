package com.afar.osaio.smart.electrician.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.cache.UserInfoCache;
import com.afar.osaio.smart.electrician.presenter.ISetNamePresenter;
import com.afar.osaio.smart.electrician.presenter.SetNamePresenter;
import com.afar.osaio.smart.electrician.util.ErrorHandleUtil;
import com.afar.osaio.smart.electrician.view.ISetNameView;
import com.afar.osaio.smart.electrician.widget.InputFrameView;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.apemans.yrcxsdk.data.YRCXSDKDataManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SetNameActivity extends BaseActivity implements ISetNameView {

    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ipvSetName)
    InputFrameView ipvSetName;
    @BindView(R.id.btnDone)
    Button btnDone;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;

    private String mName;
    private long mMemberId;
    private int mNameType;
    private ISetNamePresenter mSetNamePresenter;


    public static void toSetNameActivity(Activity from, int requestCode, String name, long memberId) {
        Intent intent = new Intent(from, SetNameActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_NAME, name);
        intent.putExtra(ConstantValue.INTENT_KEY_MEMBER_ID, memberId);
        intent.putExtra(ConstantValue.INTENT_KEY_NAME_TYPE, requestCode);
        from.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_name);
        ButterKnife.bind(this);
        initData();
        initView();

    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
        } else {
            mName = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_NAME);
            mMemberId = getCurrentIntent().getLongExtra(ConstantValue.INTENT_KEY_MEMBER_ID, 0);
            mNameType = getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_NAME_TYPE, ConstantValue.REQUEST_CODE_MEMBER_RENAME);
        }
        mSetNamePresenter = new SetNamePresenter(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(getString(R.string.account_setting));
        setupInputView();
    }

    private void setupInputView() {
        ipvSetName.setEtInputText(mName);
        ipvSetName.setInputTitle(getResources().getString(R.string.name))
                .setEtInputMaxLength(ConstantValue.MAX_NAME_LENGTH)
                .setInputBtn(R.drawable.close_icon_state_list)
                .setEtInputType(InputType.TYPE_CLASS_TEXT)
                .setOnClickListener(new InputFrameView.OnInputFrameClickListener() {
                    @Override
                    public void onInputBtnClick() {
                        ipvSetName.setEtInputText("");
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
        checkBtnEnable();
    }

    public void checkBtnEnable() {
        if (!TextUtils.isEmpty(ipvSetName.getInputText().toString().trim()) && !TextUtils.isEmpty(ipvSetName.getInputText().toString().trim())) {
            btnDone.setEnabled(true);
            btnDone.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
        } else {
            btnDone.setEnabled(false);
            btnDone.setTextColor(getResources().getColor(R.color.unable_clickable_color));
        }
    }


    @OnClick({R.id.ivLeft, R.id.btnDone})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.btnDone:
                if (ipvSetName.getInputText().equals(UserInfoCache.getInstance().getUserInfo().getNickname())) {
                    finish();
                } else if (ipvSetName.getInputText().length() > 64 && mMemberId == 0) {
                    ToastUtil.showToast(this, getResources().getString(R.string.name_too_long));
                } else if (ipvSetName.getInputText().length() > 50 && mMemberId != 0) {
                    ToastUtil.showToast(this, getResources().getString(R.string.name_too_long));
                } else if (mNameType == ConstantValue.REQUEST_CODE_MEMBER_RENAME || mMemberId != 0) {
                    mSetNamePresenter.modifyMemberNickname(mMemberId, ipvSetName.getInputText());
                } else {
                    mSetNamePresenter.modifyUserNickname(ipvSetName.getInputText());
                }
                break;
        }
    }

    @Override
    public void notifySetMemberNameResult(String result) {
        if (result.equals(ConstantValue.SUCCESS)) {
            Intent intent = new Intent();
            intent.putExtra(ConstantValue.INTENT_KEY_NICK_NAME, ipvSetName.getInputText());
            setResult(RESULT_OK, intent);
            finish();
        } else {
            ErrorHandleUtil.toastTuyaError(this, result);
        }
    }

    @Override
    public void notifySetSelfNameResult(String result) {
        if (result.equals(ConstantValue.SUCCESS)) {
            // 更新YRCXSDK 和 YRBusiness 模块中的缓存信息
            YRCXSDKDataManager.INSTANCE.setUserNickname(ipvSetName.getInputText());
            Intent intent = new Intent();
            intent.putExtra(ConstantValue.INTENT_KEY_NICK_NAME, ipvSetName.getInputText());
            setResult(RESULT_OK, intent);
            finish();
        } else {
            ErrorHandleUtil.toastTuyaError(this, result);
        }
    }


}
