package com.afar.osaio.account.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignInAndUpActivity extends BaseActivity {

    public static  void toSignInAndUpActivity(Context from) {
        Intent intent = new Intent(from, SignInAndUpActivity.class);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_sign_in_and_up);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void initView() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @OnClick({R.id.btnGoToSignUp, R.id.btnGoToSignIn})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.btnGoToSignUp: {
                SignInActivity.toSignInActivity(SignInAndUpActivity.this);
                break;
            }
            case R.id.btnGoToSignIn: {
                SignInActivity.toSignInActivity(SignInAndUpActivity.this);
                break;
            }
        }
    }
}
