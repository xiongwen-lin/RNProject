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
import com.afar.osaio.smart.electrician.presenter.AddHomePresenter;
import com.afar.osaio.smart.electrician.presenter.IAddHomePresenter;
import com.afar.osaio.smart.electrician.util.ErrorHandleUtil;
import com.afar.osaio.smart.electrician.view.IAddHomeView;
import com.afar.osaio.smart.electrician.widget.InputFrameView;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.tuya.smart.home.sdk.bean.HomeBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 添加家庭
 */
public class AddHomeActivity extends BaseActivity implements IAddHomeView {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ipvHomeName)
    InputFrameView ipvHomeName;

    private IAddHomePresenter mAddHomePresenter;

    private static final int MIN_CLICK_DELAY_TIME = 1000;
    private static long lastClickTime = 0;

    public static void toAddHomeActivity(Activity from) {
        Intent intent = new Intent(from, AddHomeActivity.class);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_home);
        ButterKnife.bind(this);
        initView();
        initData();
    }


    public void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.add_home);
        ivRight.setImageResource(R.drawable.menu_bar_finish_black);
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
                    }
                });
        ipvHomeName.setInputMaxLen(ConstantValue.HOME_NAME_MAX_LENGTH);

    }

    public void initData() {
        mAddHomePresenter = new AddHomePresenter(this);
    }

    @OnClick({R.id.ivLeft, R.id.ivRight})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft: {
                finish();
                break;
            }
            case R.id.ivRight: {
                long curClickTime = System.currentTimeMillis();
                if ((curClickTime - lastClickTime) >= MIN_CLICK_DELAY_TIME) {
                    // 超过点击间隔后再将lastClickTime重置为当前点击时间
                    lastClickTime = curClickTime;
                    if (TextUtils.isEmpty(ipvHomeName.getInputText())) {
                        ToastUtil.showToast(this, R.string.add_home_tip);
                    } else {
                        List<String> roomList = new ArrayList<>();
                        roomList.add("客厅");
                        roomList.add("主卧");
                        mAddHomePresenter.createHome(ipvHomeName.getInputText(), roomList);
                    }
                }
                break;
            }
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
        ErrorHandleUtil.toastTuyaError(this,msg);
    }

}
