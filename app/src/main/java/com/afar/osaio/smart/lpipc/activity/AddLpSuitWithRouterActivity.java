package com.afar.osaio.smart.lpipc.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.bean.LpSuitAddDeviceBean;
import com.afar.osaio.smart.cache.GatewayDeviceCache;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.lpipc.adapter.LpSuitAddDeviceAdapter;
import com.afar.osaio.smart.lpipc.adapter.listener.LpSuitAddDeviceListener;
import com.afar.osaio.smart.lpipc.contract.AddLpSuitContract;
import com.afar.osaio.smart.lpipc.presenter.AddLpSuitPresenter;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.widget.helper.ResHelper;
import com.nooie.data.EventDictionary;
import com.nooie.sdk.bean.IpcType;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddLpSuitWithRouterActivity extends BaseActivity implements AddLpSuitContract.View {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.rvLpSuitWithRouter)
    RecyclerView rvLpSuitWithRouter;

    private AddLpSuitContract.Presenter mPresenter;
    private int mGatewayNum = 0;
    private LpSuitAddDeviceAdapter mAdapter;

    public static void toAddLpSuitWithRouterActivity (Context from, Bundle param) {
        Intent intent = new Intent(from, AddLpSuitWithRouterActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_START_PARAM, param);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lp_suit_with_router);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    private void initData() {
        new AddLpSuitPresenter(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.add_camera_title);
        setupLpSuitAddDeviceAdapter();
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeData();
    }

    private void resumeData() {
        mGatewayNum = GatewayDeviceCache.getInstance().cacheSize();
        if (mPresenter != null) {
            mPresenter.getGatewayNum();
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
    public void setPresenter(@NonNull AddLpSuitContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onGetGatewayNumResult(String result, int deviceNum) {
        if (isDestroyed()) {
            return;
        }

        mGatewayNum = ConstantValue.SUCCESS.equalsIgnoreCase(result) ? deviceNum : mGatewayNum;
    }

    @Override
    public String getExternal() {
        return NooieDeviceHelper.createDistributionNetworkExternal(false);
    }

    private void setupLpSuitAddDeviceAdapter() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvLpSuitWithRouter.setLayoutManager(layoutManager);

        List<LpSuitAddDeviceBean> deviceBeans = new ArrayList<>();
        LpSuitAddDeviceBean hubAndCamDeviceBean = new LpSuitAddDeviceBean();
        hubAndCamDeviceBean.setType(ConstantValue.LP_SUIT_ADD_DEVICE_TYPE_HUM_AND_CAM);
        hubAndCamDeviceBean.setTitle(getString(R.string.add_lp_suit_pro_lp_hub_camera_title));
        hubAndCamDeviceBean.setDesc(getString(R.string.add_lp_suit_pro_lp_hub_camera_desc));
        hubAndCamDeviceBean.setIconRes(R.drawable.device_icon_gateway_cam);

        LpSuitAddDeviceBean camDeviceBean = new LpSuitAddDeviceBean();
        camDeviceBean.setType(ConstantValue.LP_SUIT_ADD_DEVICE_TYPE_CAM_WITH_ROUTER);
        camDeviceBean.setTitle(getString(R.string.add_lp_suit_pro_lp_camera_title));
        camDeviceBean.setDesc(getString(R.string.add_lp_suit_pro_lp_camera_desc));
        camDeviceBean.setIconRes(R.drawable.device_small_icon_lp_810);

        deviceBeans.add(camDeviceBean);
        deviceBeans.add(hubAndCamDeviceBean);
        mAdapter = new LpSuitAddDeviceAdapter();
        mAdapter.setData(deviceBeans);
        mAdapter.setListener(new LpSuitAddDeviceListener() {
            @Override
            public void onItemClick(LpSuitAddDeviceBean device) {
                gotoAddLpSuitDevice(device);
            }
        });

        rvLpSuitWithRouter.setAdapter(mAdapter);
    }

    private boolean checkIsAddGateway() {
        return mGatewayNum > 0;
    }

    private void gotoAddLpSuitDevice(LpSuitAddDeviceBean deviceBean) {
        if (deviceBean == null) {
            return;
        }
        Bundle param = new Bundle();
        param.putString(ConstantValue.INTENT_KEY_IPC_MODEL, getDeviceModel());
        param.putBoolean(ConstantValue.INTENT_KEY_DATA_PARAM_1, checkIsAddGateway());
        if (deviceBean.getType() == ConstantValue.LP_SUIT_ADD_DEVICE_TYPE_HUM_AND_CAM && !checkIsAddGateway()) {
            NooieDeviceHelper.trackDNEvent(EventDictionary.EVENT_ID_160);
            AddLowPowerIpcActivity.toAddLowPowerIpcActivity(this, param);
        } else if (deviceBean.getType() == ConstantValue.LP_SUIT_ADD_DEVICE_TYPE_HUM_AND_CAM) {
            NooieDeviceHelper.trackDNEvent(EventDictionary.EVENT_ID_160);
            AddLpSuitActivity.toAddLpSuitActivity(this, param);
        } else {
            NooieDeviceHelper.trackDNEvent(EventDictionary.EVENT_ID_161);
            AddProLpCameraActivity.toAddProLpCameraActivity(this, param);
        }
    }

    private String getDeviceModel() {
        if (getStartParam() == null) {
            return IpcType.EC810PRO.getType();
        }
        return getStartParam().getString(ConstantValue.INTENT_KEY_IPC_MODEL);
    }
}
