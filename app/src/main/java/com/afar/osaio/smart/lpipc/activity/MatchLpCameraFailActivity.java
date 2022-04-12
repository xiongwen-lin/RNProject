package com.afar.osaio.smart.lpipc.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.TplContract;
import com.afar.osaio.base.TplPresenter;
import com.afar.osaio.util.ConstantValue;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MatchLpCameraFailActivity extends BaseActivity implements TplContract.View {

    public static final int FAIL_HELP_FOR_LP_CAMERA = 1;
    public static final int FAIL_HELP_FOR_AP_CAMERA = 2;

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.tvGuideTitle)
    TextView tvGuideTitle;
    @BindView(R.id.tvGuideTip)
    TextView tvGuideTip;

    private TplContract.Presenter mPresenter;

    public static void toMatchLpCameraFailActivity(Context from, int helpType) {
        Intent intent = new Intent(from, MatchLpCameraFailActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_TYPE, helpType);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_lp_camera_fail);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    private void initData() {
        new TplPresenter(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        //tvTitle.setText(R.string.match_lp_camera_fail_title);
        setupView();
        tvGuideTip.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeData();
    }

    private void resumeData() {
    }

    private void setupView() {
        int failHelpType = getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_DATA_TYPE, FAIL_HELP_FOR_LP_CAMERA);
        switch (failHelpType) {
            case FAIL_HELP_FOR_LP_CAMERA: {
                tvTitle.setText(R.string.match_lp_camera_fail_title);
                tvGuideTitle.setVisibility(View.GONE);
                tvGuideTip.setText(R.string.match_lp_camera_fail_guide_tip);
                break;
            }
            case FAIL_HELP_FOR_AP_CAMERA: {
                tvTitle.setText(R.string.help);
                tvGuideTitle.setVisibility(View.VISIBLE);
                tvGuideTitle.setText(R.string.match_camera_help_ap_guide_title);
                tvGuideTip.setText(R.string.match_camera_help_ap_guide_tip);
                break;
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
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
        tvGuideTip = null;
    }

    @OnClick({R.id.ivLeft})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
        }
    }

    @Override
    public void setPresenter(@NonNull TplContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
