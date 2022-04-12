package com.afar.osaio.smart.lpipc.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.aspsine.swipetoloadlayout.OnLoadMoreListener;
import com.aspsine.swipetoloadlayout.OnRefreshListener;
import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;
import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.sdk.cache.DeviceConfigureCache;
import com.afar.osaio.smart.device.helper.NooieCloudHelper;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.lpipc.adapter.GatewayAdapter;
import com.afar.osaio.smart.lpipc.adapter.listener.GatewayListener;
import com.afar.osaio.smart.lpipc.contract.GatewaySettingsContract;
import com.afar.osaio.smart.lpipc.presenter.GatewaySettingsPresenter;
import com.afar.osaio.smart.player.activity.NooiePlayActivity;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.api.network.base.bean.entity.GatewayDevice;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GatewaySettingsActivity extends BaseActivity implements GatewaySettingsContract.View, OnRefreshListener, OnLoadMoreListener {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.swipe_target)
    RecyclerView rcvGateways;
    @BindView(R.id.sl_device_list)
    SwipeToLoadLayout swipeToLoadLayout;

    private GatewaySettingsContract.Presenter mPresenter;
    private GatewayAdapter mGatewayAdapter;

    public static void toGatewaySettingsActivity(Context from) {
        Intent intent = new Intent(from, GatewaySettingsActivity.class);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gateway_settings);
        ButterKnife.bind(this);

        initData();
        initView();
        registerDevicesChangeReceiver();
    }

    private void initData() {
        new GatewaySettingsPresenter(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.gateway_settings_title);
        setupGatewayView();
    }

    private void setupGatewayView() {
        swipeToLoadLayout.setOnRefreshListener(this);
        swipeToLoadLayout.setOnLoadMoreListener(this);
        swipeToLoadLayout.setLoadMoreEnabled(false);

        mGatewayAdapter = new GatewayAdapter();
        mGatewayAdapter.setListener(new GatewayListener() {
            @Override
            public void onGatewayItemClick(GatewayDevice device) {
                if (device != null) {
                    GatewayInfoActivity.toGatewayInfoActivity(GatewaySettingsActivity.this, device.getUuid());
                }
            }

            @Override
            public void onGatewaySubDeviceClick(BindDevice device) {
                if (device == null) {
                    return;
                }

                boolean isOpenCloud = NooieDeviceHelper.getDeviceInfoById(device.getUuid()) != null ? NooieDeviceHelper.getDeviceInfoById(device.getUuid()).isOpenCloud() : false;
                if (!isOpenCloud) {
                    isOpenCloud = DeviceConfigureCache.getInstance().getDeviceConfigure(device.getUuid()) != null ? NooieCloudHelper.isOpenCloud(DeviceConfigureCache.getInstance().getDeviceConfigure(device.getUuid()).getEndTime(), device.getZone()) : false;
                }
                if (device.getOnline() == ApiConstant.ONLINE_STATUS_ON) {
                    NooiePlayActivity.startPlayActivity(GatewaySettingsActivity.this, device.getUuid(), device.getType(), ConstantValue.NOOIE_PLAYBACK_TYPE_LIVE, ConstantValue.NOOIE_PLAYBACK_SOURCE_TYPE_NORMAL, 0, ConstantValue.ROUTE_SOURCE_NORMAL, ConstantValue.CONNECTION_MODE_QC, new String());
                } else if (isOpenCloud && device.getBind_type() == ApiConstant.BIND_TYPE_OWNER) {
                    NooiePlayActivity.startPlayActivity(GatewaySettingsActivity.this, device.getUuid(), device.getType(), ConstantValue.NOOIE_PLAYBACK_TYPE_LIVE, ConstantValue.NOOIE_PLAYBACK_SOURCE_TYPE_NORMAL, 0, ConstantValue.ROUTE_SOURCE_NORMAL, ConstantValue.CONNECTION_MODE_QC, new String());
                } else {
                    showDeleteDeviceDialog(device.getUuid(), device.getPuuid(), (!TextUtils.isEmpty(device.getName()) ? device.getName() : device.getUuid()));
                }
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rcvGateways.setLayoutManager(layoutManager);
        rcvGateways.setAdapter(mGatewayAdapter);
    }

    private void startRefresh() {
        if (swipeToLoadLayout != null) {
            swipeToLoadLayout.setRefreshing(true);
        }
    }

    private void stopRefresh() {
        if (swipeToLoadLayout != null && swipeToLoadLayout.isRefreshing()) {
            swipeToLoadLayout.setRefreshing(false);
        }
    }

    private void stopLoadMore() {
        if (swipeToLoadLayout != null && swipeToLoadLayout.isLoadingMore()) {
            swipeToLoadLayout.setLoadingMore(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeData();
    }

    private void resumeData() {
        if (mPresenter != null) {
            mPresenter.getGatewayDevices(mUserAccount, mUid);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        unRegisterDevicesChangeReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.destroy();
        }
        releaseRes();
        release();
    }

    private void release() {
        if (mGatewayAdapter != null) {
            mGatewayAdapter.release();
            mGatewayAdapter = null;
        }

        if (swipeToLoadLayout != null) {
            swipeToLoadLayout.setOnRefreshListener(null);
            swipeToLoadLayout.setOnLoadMoreListener(null);
            swipeToLoadLayout = null;
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
    public void setPresenter(@NonNull GatewaySettingsContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onRefresh() {
        if (mPresenter != null) {
            mPresenter.getGatewayDevices(mUserAccount, mUid);
        }
    }

    @Override
    public void onLoadMore() {
    }

    @Override
    public void onGetGatewayDevicesResult(String result, List<GatewayDevice> gatewayDevices) {
        if (isDestroyed()) {
            return;
        }
        stopRefresh();
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            if (CollectionUtil.isEmpty(gatewayDevices)) {
                finish();
            }
            showGatewayDevices(gatewayDevices);
        } else {
        }
    }

    private void showGatewayDevices(List<GatewayDevice> gatewayDevices) {
        if (checkNull(mGatewayAdapter)) {
            return;
        }
        if (mGatewayAdapter != null) {
            mGatewayAdapter.setData(gatewayDevices);
        }
    }

    @Override
    public void onReceiveDeviceChange(Intent intent) {
        if (isDestroyed()) {
            return;
        }
        if (mPresenter != null) {
            mPresenter.getGatewayDevices(mUserAccount, mUid);
        }
    }

    @Override
    public void onDeleteSubDeviceResult(String result, String deviceId, String pDeviceId) {
        if (isDestroyed()) {
            return;
        }
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            if (mGatewayAdapter != null) {
                mGatewayAdapter.removeSubDevice(pDeviceId, deviceId);
            }
            NooieDeviceHelper.sendRemoveDeviceBroadcast(ConstantValue.REMOVE_DEVICE_TYPE_IPC, deviceId);
        }
    }

    private Dialog mDeleteDeviceDialog;

    private void showDeleteDeviceDialog(String deviceId, String pDeviceId, String name) {
        hideDeleteDeviceDialog();
        mDeleteDeviceDialog = DialogUtils.showConfirmWithSubMsgDialog(this, getString(R.string.camera_settings_remove_camera_confirm),
                String.format(getString(R.string.camera_settings_remove_info_confirm), name),
                R.string.camera_settings_no_remove, R.string.camera_settings_remove, new DialogUtils.OnClickConfirmButtonListener() {
                    @Override
                    public void onClickRight() {
                        if (mPresenter != null) {
                            mPresenter.removeSubDevice(mUserAccount, deviceId, pDeviceId);
                        }
                    }

                    @Override
                    public void onClickLeft() {
                    }
                });
    }

    private void hideDeleteDeviceDialog() {
        if (mDeleteDeviceDialog != null) {
            mDeleteDeviceDialog.dismiss();
            ;
            mDeleteDeviceDialog = null;
        }
    }

}
