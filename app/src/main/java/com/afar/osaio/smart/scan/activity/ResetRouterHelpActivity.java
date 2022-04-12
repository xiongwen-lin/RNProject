package com.afar.osaio.smart.scan.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.router.RouterConnectionWifiActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ResetRouterHelpActivity extends BaseActivity {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivIcon1)
    ImageView ivIcon1;
    @BindView(R.id.ivIcon2)
    ImageView ivIcon2;

    public static void toResetRouterHelpActivity(Context from) {
        Intent intent = new Intent(from, ResetRouterHelpActivity.class);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_router_help);
        ButterKnife.bind(this);

        setupView();
    }

    @SuppressLint("SetTextI18n")
    private void setupView() {
        tvTitle.setText(R.string.router_how_to_reset_the_router);
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @OnClick({R.id.ivLeft, R.id.btnResetSuccess})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.btnResetSuccess:
                //finish();
                RouterConnectionWifiActivity.toRouterConnectionWifiActivity(ResetRouterHelpActivity.this);
                //ConnectionRouterActivity.toConnectionRouterActivity(ResetRouterHelpActivity.this);
                break;
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
}
