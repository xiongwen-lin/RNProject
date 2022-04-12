package com.afar.osaio.application.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.adapter.DeviceManualAdapter;
import com.afar.osaio.base.BaseActivity;
import com.nooie.sdk.bean.IpcType;
import com.afar.osaio.bean.UserManualBean;
import com.afar.osaio.smart.hybrid.webview.HybridWebViewActivity;
import com.afar.osaio.util.ConstantValue;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserManualActivity extends BaseActivity {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.rcvDeviceManual)
    RecyclerView rcvDeviceManual;
    DeviceManualAdapter mManualeAdapter;

    public static void toUserManualActivity(Context context) {
        Intent intent = new Intent(context, UserManualActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_manual);
        ButterKnife.bind(this);

        initView();
        initData();;
    }

    public void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.help_user_manual);
    }

    public void initData() {
        List<UserManualBean> userManualBeanList = new ArrayList<>();
        userManualBeanList.add(new UserManualBean(IpcType.IPC_720.getType(), "Osaio Cam", ConstantValue.URL_USER_MANUAL_VICTURE));
        setupManualeList(userManualBeanList);
    }

    public void setupManualeList(List<UserManualBean> userManualBeans) {
        mManualeAdapter = new DeviceManualAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rcvDeviceManual.setLayoutManager(layoutManager);
        rcvDeviceManual.setAdapter(mManualeAdapter);
        mManualeAdapter.setData(userManualBeans);
        mManualeAdapter.setListener(new DeviceManualAdapter.OnManualItemClickListener() {
            @Override
            public void onItemClick(UserManualBean userManualBean) {
                if (userManualBean != null) {
                    Bundle param = new Bundle();
                    param.putString(ConstantValue.INTENT_KEY_URL, userManualBean.getUrl());
                    param.putString(ConstantValue.INTENT_KEY_TITLE, userManualBean.getDeviceAlias());
                    param.putBoolean(ConstantValue.INTENT_KEY_DATA_PARAM, true);
                    param.putBoolean(ConstantValue.INTENT_KEY_DATA_PARAM_1, true);
                    HybridWebViewActivity.toHybridWebViewActivity(UserManualActivity.this, param);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @OnClick({R.id.ivLeft})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
        }
    }
}
