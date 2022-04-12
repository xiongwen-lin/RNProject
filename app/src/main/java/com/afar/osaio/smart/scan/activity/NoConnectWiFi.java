package com.afar.osaio.smart.scan.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by victor on 2018/11/12
 * Email is victor.qiao.0604@gmail.com
 */
public class NoConnectWiFi extends BaseActivity {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;

    public static void toNoConnectWiFi(Context from) {
        Intent intent = new Intent(from, NoConnectWiFi.class);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_connect_wifi);
        ButterKnife.bind(this);
        setupView();
    }

    private void setupView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.add_camera_not_connect_to_wifi);
        ivRight.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseRes();
    }

    @Override
    public void releaseRes() {
        super.releaseRes();
        ivLeft = null;
        tvTitle = null;
        ivRight = null;
    }

    @OnClick({R.id.ivLeft, R.id.btnDone})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.btnDone:
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                finish();
                break;
        }
    }
}
