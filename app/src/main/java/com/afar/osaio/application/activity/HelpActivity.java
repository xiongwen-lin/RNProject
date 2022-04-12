package com.afar.osaio.application.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
 * Created by victor on 2018/7/10
 * Email is victor.qiao.0604@gmail.com
 */
public class HelpActivity extends BaseActivity {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;

    public static void toHelpActivity(Context from) {
        Intent intent = new Intent(from, HelpActivity.class);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        ButterKnife.bind(this);

        initView();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        ivRight.setVisibility(View.GONE);
        tvTitle.setText(R.string.help);
    }

    @OnClick({R.id.ivLeft, R.id.containerUserManual, R.id.containerFAQ})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.containerUserManual:
                UserManualActivity.toUserManualActivity(HelpActivity.this);
                break;
            case R.id.containerFAQ:
                FAQActivity.toFAQActivity(HelpActivity.this);
                break;
        }
    }
}
