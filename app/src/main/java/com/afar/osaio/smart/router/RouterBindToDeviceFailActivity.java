package com.afar.osaio.smart.router;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.afar.osaio.R;
import com.afar.osaio.smart.home.activity.HomeActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RouterBindToDeviceFailActivity extends RouterBaseActivity {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.tvTips)
    TextView tvTips;
    @BindView(R.id.ivIcon)
    ImageView ivIcon;

    public static void toRouterBindToDeviceFailActivity(Context from) {
        Intent intent = new Intent(from, RouterBindToDeviceFailActivity.class);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router_bind_fail);
        ButterKnife.bind(this);

        initView();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        //tvTitle.setText(R.string.router_bind_device_fail);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    @OnClick({R.id.ivLeft, R.id.btnRecover, R.id.layout})
    public void onViewClicked(View view) {
        switch(view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.btnRecover:
                RouterBindToDeviceActivity.toRouterBindToDeviceActivity(this);
                break;
            case R.id.layout:
                HomeActivity.toHomeActivity(this);
                //RouterResetNameActivity.toRouterResetNameActivity(RouterBindToDeviceFailActivity.this);
                break;
        }
    }
}
