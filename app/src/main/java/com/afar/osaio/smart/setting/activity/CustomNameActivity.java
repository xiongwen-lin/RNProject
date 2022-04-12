package com.afar.osaio.smart.setting.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.nooie.data.EventDictionary;
import com.nooie.sdk.bean.IpcType;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.widget.FButton;
import com.afar.osaio.widget.InputFrameView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CustomNameActivity extends BaseActivity {

    public static void toCustomNameActivity(Activity from, int requestCode, int customNameType, String title, String inputTitle, String model, String eventId) {
        Intent intent = new Intent(from, CustomNameActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_TYPE, customNameType);
        intent.putExtra(ConstantValue.INTENT_KEY_TITLE, title);
        intent.putExtra(ConstantValue.INTENT_KEY_NICK_NAME, inputTitle);
        intent.putExtra(ConstantValue.INTENT_KEY_IPC_MODEL, model);
        intent.putExtra(ConstantValue.INTENT_KEY_EVENT_ID, eventId);
        from.startActivityForResult(intent, requestCode);
    }

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivDeviceIcon)
    ImageView ivDeviceIcon;
    @BindView(R.id.ipvCustomName)
    InputFrameView ipvCustomName;
    @BindView(R.id.btnDone)
    FButton btnDone;

    private String mToastTip;
    private String mDeviceModel;

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_custom_name);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
            return;
        } else {
            mDeviceModel = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_IPC_MODEL);
        }
    }
    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        String title = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_TITLE);
        tvTitle.setText(title);

        int customNameType = getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_DATA_TYPE, ConstantValue.NOOIE_CUSTOM_NAME_TYPE_USER);
        String inputTitle = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_NICK_NAME);
        switch (customNameType) {
            case ConstantValue.NOOIE_CUSTOM_NAME_TYPE_USER:
                ivDeviceIcon.setVisibility(View.GONE);
                inputTitle = TextUtils.isEmpty(inputTitle) ? getString(R.string.account_nick_name) : inputTitle;
                mToastTip = getString(R.string.name_device_enter_custom_name);
                break;
            case ConstantValue.NOOIE_CUSTOM_NAME_TYPE_DEVICE:
                ivDeviceIcon.setVisibility(View.VISIBLE);
                mToastTip = getString(R.string.name_device_enter_custom_name);
                IpcType mDeviceType = TextUtils.isEmpty(mDeviceModel) ? IpcType.IPC_720 : IpcType.getIpcType(mDeviceModel);
                ivDeviceIcon.setImageResource(R.drawable.device_icon);
                if (mDeviceType == IpcType.IPC_100) {
                    ivDeviceIcon.setImageResource(R.drawable.device_icon_360);
                } else if (mDeviceType == IpcType.IPC_200) {
                    ivDeviceIcon.setImageResource(R.drawable.device_icon_outdoor);
                }
                break;
        }

        setupCountryInput(inputTitle);
    }

    private void setupCountryInput(String inputTitle) {
        ipvCustomName.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT).setInputTitle(inputTitle)
                .setInputBtn(R.drawable.close_icon_state_list)
                .setOnClickListener(new InputFrameView.OnInputFrameClickListener() {
                    @Override
                    public void onInputBtnClick() {
                        ipvCustomName.setEtInputText("");
                    }

                    @Override
                    public void onEditorAction() {
                        hideInputMethod();
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
        if (!TextUtils.isEmpty(ipvCustomName.getInputText())) {
            btnDone.setEnabled(true);
            btnDone.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
        } else {
            btnDone.setEnabled(false);
            btnDone.setTextColor(getResources().getColor(R.color.unable_clickable_color));
        }
    }

    @OnClick({R.id.ivLeft, R.id.btnDone})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.btnDone:
                if (TextUtils.isEmpty(ipvCustomName.getInputText())) {
                    ToastUtil.showToast(CustomNameActivity.this, mToastTip);
                    break;
                }

                Intent intent = new Intent();
                intent.putExtra(ConstantValue.INTENT_KEY_NICK_NAME, ipvCustomName.getInputText());
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }

    @Override
    public String getEventId(int trackType) {
        String eventId = getIntent() != null ? getIntent().getStringExtra(ConstantValue.INTENT_KEY_EVENT_ID) : null;
        return eventId;
    }

    @Override
    public String getPageId() {
        String eventId = getIntent() != null ? getIntent().getStringExtra(ConstantValue.INTENT_KEY_EVENT_ID) : null;
        if (EventDictionary.EVENT_ID_ACCESS_RENAME_NICKNAME.equalsIgnoreCase(eventId)) {
            return EventDictionary.EVENT_PAGE_CHANGE_USER_NAME;
        } else if (EventDictionary.EVENT_ID_ACCESS_RENAME_NICKNAME.equalsIgnoreCase(eventId)) {
            return EventDictionary.EVENT_PAGE_CHANGE_ACCOUNT;
        }
        return null;
    }

}
