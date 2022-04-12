package com.afar.osaio.message.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieBaseSupportActivity;
import com.afar.osaio.base.TplContract;
import com.afar.osaio.base.TplPresenter;
import com.afar.osaio.smart.home.fragment.MessageFragment;

public class MessageActivity extends NooieBaseSupportActivity implements TplContract.View {

    private TplContract.Presenter mPresenter;

    public static void toMessageActivity(Context from) {
        Intent intent = new Intent(from, MessageActivity.class);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        if (findFragment(MessageFragment.class) == null) {
            loadRootFragment(R.id.fl_message_container, MessageFragment.newInstance());
        }

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
    }

    @Override
    public void setPresenter(@NonNull TplContract.Presenter presenter) {
        mPresenter = presenter;
    }

    public void onClickNavLeft() {
        finish();
    }

    private void initData() {
        new TplPresenter(this);
    }

    private void initView() {
    }

    private void resumeData() {
    }
}
