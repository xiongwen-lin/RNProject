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
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.TplContract;
import com.afar.osaio.base.TplPresenter;
import com.afar.osaio.bean.RouterConfigureLink;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.scan.adapter.RouterConfigureLinkAdapter;
import com.afar.osaio.smart.scan.adapter.listener.RouterConfigureLinkListener;
import com.afar.osaio.util.ConstantValue;
import com.nooie.data.EventDictionary;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RouterSettingActivity extends BaseActivity implements TplContract.View {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.rcvRouterConfigure)
    RecyclerView rcvRouterConfigure;

    private TplContract.Presenter mPresenter;
    private RouterConfigureLinkAdapter mRouterConfigureLinkAdapter;

    public static void toRouterSettingActivity(Context from) {
        Intent intent = new Intent(from, RouterSettingActivity.class);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router_setting);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    private void initData() {
        new TplPresenter(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.router_setting_title);
        setupRouterConfigureLink();
    }

    private void setupRouterConfigureLink() {
        mRouterConfigureLinkAdapter = new RouterConfigureLinkAdapter();
        mRouterConfigureLinkAdapter.setListener(new RouterConfigureLinkListener() {
            @Override
            public void onItemClick(RouterConfigureLink data) {
                if (data != null) {
                    addSelectRouterSettingEvent(data.getType());
                    RouterConfigureActivity.toRouterConfigureActivity(RouterSettingActivity.this, data.getTitle(), data.getType());
                }
            }

            @Override
            public void onItemLongClick(RouterConfigureLink data) {
            }
        });
        mRouterConfigureLinkAdapter.setData(createRouterConfigureLink());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rcvRouterConfigure.setLayoutManager(layoutManager);
        rcvRouterConfigure.setAdapter(mRouterConfigureLinkAdapter);
    }

    private List<RouterConfigureLink> createRouterConfigureLink() {
        List<RouterConfigureLink> routerConfigureLinks = new ArrayList<>();
        routerConfigureLinks.add(new RouterConfigureLink(getString(R.string.router_setting_netgear), ConstantValue.ROUTER_TYPE_NET_GEAR));
        routerConfigureLinks.add(new RouterConfigureLink(getString(R.string.router_setting_asus), ConstantValue.ROUTER_TYPE_ASUS));
        routerConfigureLinks.add(new RouterConfigureLink(getString(R.string.router_setting_d_link), ConstantValue.ROUTER_TYPE_D_LINK));
        routerConfigureLinks.add(new RouterConfigureLink(getString(R.string.router_setting_tp_link), ConstantValue.ROUTER_TYPE_TP_LINK));
        return routerConfigureLinks;
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeData();
    }

    private void resumeData() {
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
        if (mRouterConfigureLinkAdapter != null) {
            mRouterConfigureLinkAdapter.release();
            mRouterConfigureLinkAdapter = null;
        }

        if (rcvRouterConfigure != null) {
            rcvRouterConfigure.setAdapter(null);
            rcvRouterConfigure = null;
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

    @Override
    public String getExternal() {
        return NooieDeviceHelper.createDistributionNetworkExternal(false);
    }

    private void addSelectRouterSettingEvent(int routerType) {
        if (routerType == ConstantValue.ROUTER_TYPE_NET_GEAR) {
            NooieDeviceHelper.trackDNEvent(EventDictionary.EVENT_ID_CLICK_CONFIGURE_NET_GEAR);
        } else if (routerType == ConstantValue.ROUTER_TYPE_ASUS) {
            NooieDeviceHelper.trackDNEvent(EventDictionary.EVENT_ID_CLICK_ASUS);
        } else if (routerType == ConstantValue.ROUTER_TYPE_D_LINK) {
            NooieDeviceHelper.trackDNEvent(EventDictionary.EVENT_ID_CLICK_D_LINK);
        } else if (routerType == ConstantValue.ROUTER_TYPE_TP_LINK) {
            NooieDeviceHelper.trackDNEvent(EventDictionary.EVENT_ID_CLICK_TP_LINK);
        }
    }
}
