package com.afar.osaio.smart.scan.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.TplContract;
import com.afar.osaio.base.TplPresenter;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.widget.FButton;
import com.afar.osaio.widget.InputFrameView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class InputDeviceIdActivity extends BaseActivity implements TplContract.View {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ipvDeviceId)
    InputFrameView ipvDeviceId;
    @BindView(R.id.btnDone)
    FButton btnDone;

    private TplContract.Presenter mPresenter;

    public static void toInputDeviceIdActivity(Activity from, int requestCode) {
        Intent intent = new Intent(from, InputDeviceIdActivity.class);
        from.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_device_id);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    private void initData() {
        new TplPresenter(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.input_device_id_title);
        setupInputFrameView();
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeData();
    }

    private void resumeData() {
    }

    private void setupInputFrameView() {
        ipvDeviceId.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT)
                .setInputTitle(getString(R.string.camera_settings_cam_info_device_id))
                .setInputBtnIsShow(false)
                .setOnClickListener(new InputFrameView.OnInputFrameClickListener() {
                    @Override
                    public void onInputBtnClick() {
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
        if (ipvDeviceId != null && !TextUtils.isEmpty(ipvDeviceId.getInputText())) {
            btnDone.setEnabled(true);
            btnDone.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
        } else {
            btnDone.setEnabled(false);
            btnDone.setTextColor(getResources().getColor(R.color.unable_clickable_color));
        }
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
        if (ipvDeviceId != null) {
            ipvDeviceId.release();
            ipvDeviceId = null;
        }
        btnDone = null;
    }

    @OnClick({R.id.ivLeft, R.id.btnDone})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.btnDone:
                if (ipvDeviceId == null || ipvDeviceId.getInputText() == null) {
                    break;
                }
                if (ipvDeviceId.getInputText().length() > ConstantValue.DEVICE_ID_MAX_LEN) {
                    ToastUtil.showToast(this, R.string.input_device_id_max_len_tip);
                    break;
                }
                Bundle data = new Bundle();
                data.putString(ConstantValue.INTENT_KEY_DEVICE_ID, ipvDeviceId != null ? ipvDeviceId.getInputText() : "");
                finishForResult(data);
                break;
        }
    }

    @Override
    public void setPresenter(@NonNull TplContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
