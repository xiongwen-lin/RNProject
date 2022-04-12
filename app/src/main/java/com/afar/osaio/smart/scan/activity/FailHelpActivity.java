package com.afar.osaio.smart.scan.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.application.adapter.FAQAdapter;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.TplContract;
import com.afar.osaio.base.TplPresenter;
import com.afar.osaio.bean.FAQBean;
import com.afar.osaio.util.ConstantValue;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FailHelpActivity extends BaseActivity implements TplContract.View {

    public static final int TYPE_FAIL_HELP_AP_CAMERA = 1;

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.rcvHelp)
    RecyclerView rcvHelp;
    private FAQAdapter mAdapter;

    private TplContract.Presenter mPresenter;

    public static void toFailHelpActivity(Context from, int helpType) {
        Intent intent = new Intent(from, FailHelpActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_TYPE, helpType);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fail_help);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    private void initData() {
        new TplPresenter(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        setupFAQListView();
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeData();
    }

    private void resumeData() {
    }

    private void setupFAQListView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rcvHelp.setLayoutManager(layoutManager);
        mAdapter = new FAQAdapter();
        mAdapter.setListener(new FAQAdapter.OnFAQItemClickListener() {
            @Override
            public void onItemClick(int position, FAQBean faqBean) {
            }
        });
        rcvHelp.setAdapter(mAdapter);
        int helpType = getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_DATA_TYPE, TYPE_FAIL_HELP_AP_CAMERA);
        refreshData(helpType);
    }

    private void refreshData(int helpType) {
        String title = getString(R.string.help);
        List<FAQBean> helpDatas = new ArrayList<>();
        switch (helpType) {
            case TYPE_FAIL_HELP_AP_CAMERA: {
                int[] helpTitleId = {R.string.scan_device_fail_help_ap_title_1, R.string.scan_device_fail_help_ap_title_2, R.string.scan_device_fail_help_ap_title_3};
                int[] helpContentId = {R.string.scan_device_fail_help_ap_content_1, R.string.scan_device_fail_help_ap_content_2, R.string.scan_device_fail_help_ap_content_3};
                if (helpTitleId == null || helpContentId == null || helpContentId.length != helpContentId.length && helpTitleId.length > 0) {
                    break;
                }
                for (int i = 0; i < helpTitleId.length; i++) {
                    helpDatas.add(new FAQBean(getString(helpTitleId[i]), getString(helpContentId[i]), null, true));
                }
                break;
            }
        }
        if (tvTitle != null) {
            tvTitle.setText(title);
        }
        if (mAdapter != null) {
            mAdapter.setData(helpDatas);
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
        if (mAdapter != null) {
            mAdapter = null;
        }
        if (rcvHelp != null) {
            rcvHelp.setAdapter(null);
            rcvHelp = null;
        }
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
