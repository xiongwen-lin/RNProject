package com.afar.osaio.smart.electrician.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.electrician.presenter.INameGroupPresenter;
import com.afar.osaio.smart.electrician.presenter.NameGroupPresenter;
import com.afar.osaio.smart.electrician.util.CommonUtil;
import com.afar.osaio.smart.electrician.util.ErrorHandleUtil;
import com.afar.osaio.smart.electrician.view.INameGroupView;
import com.afar.osaio.smart.electrician.widget.InputFrameView;
import com.afar.osaio.smart.home.activity.HomeActivity;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * NameGroupActivity
 *
 * @author Administrator
 * @date 2019/3/20
 */
public class NameGroupActivity extends BaseActivity implements INameGroupView {

    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.ipvGroupName)
    InputFrameView ipvGroupName;
    @BindView(R.id.btnGroupSave)
    Button btnGroupSave;

    private INameGroupPresenter mNameGroupPresenter;
    private int mNameType;
    private String mGroupName;

    public static void toNameGroupActivity(Context from, String productId, ArrayList<String> deviceIds, int nameType) {
        Intent intent = new Intent(from, NameGroupActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_PRODUCT_ID, productId);
        intent.putStringArrayListExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceIds);
        intent.putExtra(ConstantValue.INTENT_KEY_GROUP_NAME_TYPE, nameType);
        from.startActivity(intent);
    }

    public static void toNameGroupActivity(Activity from, int requestCode, long groupId, int nameType,String groupName) {
        Intent intent = new Intent(from, NameGroupActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_GROUP_ID, groupId);
        intent.putExtra(ConstantValue.INTENT_KEY_GROUP_NAME_TYPE, nameType);
        intent.putExtra(ConstantValue.INTENT_KEY_GROUP_NAME,groupName);
        from.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_group);
        ButterKnife.bind(this);
        initView();
        initData();
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
        tvTitle.setText(R.string.name_your_group);
        ivRight.setVisibility(View.INVISIBLE);
        ipvGroupName.setInputTitle(getResources().getString(R.string.name))
                .setEtInputType(InputType.TYPE_CLASS_TEXT)
                .setInputBtn(R.drawable.close_icon_state_list)
                .setOnClickListener(new InputFrameView.OnInputFrameClickListener() {
                    @Override
                    public void onInputBtnClick() {
                        ipvGroupName.setEtInputText("");
                    }

                    @Override
                    public void onEditorAction() {
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
        if (!TextUtils.isEmpty(ipvGroupName.getInputText().toString().trim()) && !TextUtils.isEmpty(ipvGroupName.getInputText().toString().trim())) {
            btnGroupSave.setEnabled(true);
            btnGroupSave.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
        } else {
            btnGroupSave.setEnabled(false);
            btnGroupSave.setTextColor(getResources().getColor(R.color.unable_clickable_color));
        }
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
            return;
        } else {
            mNameType = getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_GROUP_NAME_TYPE, ConstantValue.GROUP_CREATE);
            if(getCurrentIntent().hasExtra(ConstantValue.INTENT_KEY_GROUP_NAME)){
                ipvGroupName.setEtInputText(getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_GROUP_NAME));
                ipvGroupName.setEtSelection(TextUtils.isEmpty(ipvGroupName.getInputText().toString()) ? 0 : ipvGroupName.getInputText().toString().length());
            }
            mNameGroupPresenter = new NameGroupPresenter(this);
        }
    }

    @OnClick({R.id.ivLeft, R.id.btnGroupSave})
    public void onViewClick(View view) {
        if(CommonUtil.isFastClick()){
            return;
        }
        switch (view.getId()) {
            case R.id.ivLeft: {
                finish();
                break;
            }
            case R.id.btnGroupSave: {
                if (ipvGroupName.getInputText().length() > ConstantValue.MAX_GROUP_NAME_LENGTH){
                    ToastUtil.showToast(NameGroupActivity.this,getString(R.string.name_too_long));
                    break;
                }
                showLoadingDialog();
                if (mNameType == ConstantValue.GROUP_CREATE) {
                    String productId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_PRODUCT_ID);
                    List<String> deviceIds = getCurrentIntent().getStringArrayListExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
                    if (!TextUtils.isEmpty(productId) && !TextUtils.isEmpty(ipvGroupName.getInputText())) {
                        mNameGroupPresenter.createGroup(productId, ipvGroupName.getInputText(), deviceIds);
                    }
                } else if(mNameType == ConstantValue.GROUP_RENAME) {
                    long groupId = getCurrentIntent().getLongExtra(ConstantValue.INTENT_KEY_GROUP_ID, 0);
                    if (!TextUtils.isEmpty(ipvGroupName.getInputText())) {
                        mNameGroupPresenter.renameGroup(groupId, ipvGroupName.getInputText());
                    }
                }
                break;
            }
        }
    }

    @Override
    public void notifyNameGroupState(String msg) {
        hideLoadingDialog();
        if (ConstantValue.SUCCESS.equalsIgnoreCase(msg)) {
            switch (mNameType) {
                case ConstantValue.GROUP_CREATE: {
                    HomeActivity.toHomeActivity(NameGroupActivity.this,HomeActivity.TYPE_ADD_GROUP);
                    break;
                }
                case ConstantValue.GROUP_RENAME: {
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    break;
                }
            }
            finish();
        } else {
            ErrorHandleUtil.toastTuyaError(this,msg);
        }
    }
}
