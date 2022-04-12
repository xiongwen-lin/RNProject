package com.afar.osaio.account.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.TplContract;
import com.afar.osaio.base.TplPresenter;
import com.afar.osaio.util.ConstantValue;
import com.alibaba.android.arouter.launcher.ARouter;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class StartUpGuideActivity extends BaseActivity implements TplContract.View {

    private TplContract.Presenter mPresenter;

    public static void toStartUpGuideActivity(Context from) {
        Intent intent = new Intent(from, StartUpGuideActivity.class);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup_guide);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeData();
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
    }

    @OnClick({R.id.btnStartUpGuideNext})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnStartUpGuideNext:
                //redirectGotoHomePage();
//                SignInActivity.toSignInActivity(this, "", "", true);
                ARouter.getInstance().build("/user/login")
//                        .withString("userAccount", "")
//                        .withString("password", "")
//                        .withBoolean("isClearTask", true)
                        .navigation();
                finish();
                break;
        }
    }

    @Override
    public void setPresenter(@NonNull TplContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public int getStatusBarMode() {
        return ConstantValue.STATUS_BAR_DARK_MODE;
    }

    private void initData() {
        new TplPresenter(this);
    }

    private void initView() {
    }

    private void resumeData() {
    }
}
