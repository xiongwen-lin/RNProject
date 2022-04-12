package com.afar.osaio.smart.scan.activity;

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

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.TplContract;
import com.afar.osaio.base.TplPresenter;
import com.afar.osaio.bean.ConnectionModeBean;
import com.afar.osaio.smart.mixipc.activity.ConnectApDeviceActivity;
import com.nooie.data.EventDictionary;
import com.nooie.sdk.bean.IpcType;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.scan.adapter.ConnectionModeAdapter;
import com.afar.osaio.smart.scan.adapter.listener.ConnectionModeListener;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.widget.helper.ResHelper;
import com.nooie.common.utils.collection.CollectionUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ConnectionModeActivity extends BaseActivity implements TplContract.View {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.rcvConnectionMode)
    RecyclerView rcvConnectionMode;
    @BindView(R.id.tvTopTip)
    TextView tvTopTip;

    private TplContract.Presenter mPresenter;
    private ConnectionModeAdapter mAdapter;

    public static void toConnectionModeActivity(Context from, String model) {
        Intent intent = new Intent(from, ConnectionModeActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_IPC_MODEL, model);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_mode);
        ButterKnife.bind(this);
        initData();
        initView();
    }

    private void initData() {
        new TplPresenter(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.connection_mode_title);
        setupConnectionModeView();
    }

    private void setupConnectionModeView() {
        String model = getCurrentIntent() != null ? getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_IPC_MODEL) : IpcType.IPC_100.getType();
        List<ConnectionModeBean> connectionModeList = new ArrayList<>();
        String connectionModeTip = "";

        if (NooieDeviceHelper.mergeIpcType(IpcType.getIpcType(model)) == IpcType.PC530) {
            connectionModeList.add(new ConnectionModeBean(ConstantValue.CONNECTION_MODE_QC, getString(R.string.connection_mode_wireless_title), getString(R.string.connection_mode_wireless_content), ResHelper.getInstance().getConnectionModeQcIconByType(model), model));
            if (NooieDeviceHelper.isSupportDistributeNetworkForLan(model)) {
                connectionModeList.add(new ConnectionModeBean(ConstantValue.CONNECTION_MODE_LAN, getString(R.string.connection_mode_lan_title), getString(R.string.connection_mode_lan_content), ResHelper.getInstance().getConnectionModeLanIconByType(model), model));
            }
            connectionModeTip = getString(R.string.connection_mode_tip_lan);
            if (NooieDeviceHelper.isSupportDistributeNetworkForAp(model)) {
                connectionModeList.add(new ConnectionModeBean(ConstantValue.CONNECTION_MODE_AP, getString(R.string.connection_mode_hot_spot_title), getString(R.string.connection_mode_hot_spot_content), ResHelper.getInstance().getConnectionModeApIconByType(model), model));
            }
        } else if (NooieDeviceHelper.mergeIpcType(IpcType.getIpcType(model)) == IpcType.HC320) {
            connectionModeList.add(new ConnectionModeBean(ConstantValue.CONNECTION_MODE_QC, getString(R.string.connection_mode_qc_title_hc_320), getString(R.string.connection_mode_qc_content_hc_320), ResHelper.getInstance().getConnectionModeQcIconByType(model), model));
            connectionModeList.add(new ConnectionModeBean(ConstantValue.CONNECTION_MODE_AP_DIRECT, getString(R.string.connection_mode_dc_title_hc_320), getString(R.string.connection_mode_dc_content_hc_320), ResHelper.getInstance().getConnectionModeDvIconByType(model), model));
        } else if (NooieDeviceHelper.mergeIpcType(IpcType.getIpcType(model)) == IpcType.EC810_PLUS) {
            connectionModeList.add(new ConnectionModeBean(ConstantValue.CONNECTION_MODE_QC, getString(R.string.connection_mode_qc_title), getString(R.string.connection_mode_qc_content), ResHelper.getInstance().getConnectionModeQcIconByType(model), model));
            connectionModeList.add(new ConnectionModeBean(ConstantValue.CONNECTION_MODE_AP, getString(R.string.connection_mode_hot_spot_title), getString(R.string.connection_mode_hot_spot_content), ResHelper.getInstance().getConnectionModeApIconByType(model), model));
        } else {
            connectionModeList.add(new ConnectionModeBean(ConstantValue.CONNECTION_MODE_QC, getString(R.string.connection_mode_qc_title), getString(R.string.connection_mode_qc_content), ResHelper.getInstance().getConnectionModeQcIconByType(model), model));
            connectionModeList.add(new ConnectionModeBean(ConstantValue.CONNECTION_MODE_AP, getString(R.string.connection_mode_hot_spot_title), getString(R.string.connection_mode_hot_spot_content), ResHelper.getInstance().getConnectionModeApIconByType(model), model));
        }

        if (NooieDeviceHelper.mergeIpcType(IpcType.getIpcType(model)) == IpcType.MC120) {
            connectionModeList.add(new ConnectionModeBean(ConstantValue.CONNECTION_MODE_AP_DIRECT, getString(R.string.connection_mode_dc_title), getString(R.string.connection_mode_dc_content), ResHelper.getInstance().getConnectionModeDvIconByType(model), model));
        }

        connectionModeTip = !TextUtils.isEmpty(connectionModeTip) ? connectionModeTip : String.format(getString(R.string.connection_mode_tip_1), (CollectionUtil.size(connectionModeList) < 1 ? 1 : CollectionUtil.size(connectionModeList)));

        mAdapter = new ConnectionModeAdapter();
        mAdapter.setData(connectionModeList);
        mAdapter.setListener(new ConnectionModeListener() {
            @Override
            public void onItemClick(ConnectionModeBean data) {
                if (data != null) {
                    addConnectModeEvent(data);
                    gotoAddDevice(data.getConnectionMode());
                }
            }

            @Override
            public void onItemLongClick(ConnectionModeBean data) {
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rcvConnectionMode.setLayoutManager(layoutManager);
        rcvConnectionMode.setAdapter(mAdapter);
        tvTopTip.setText(connectionModeTip);
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
        tvTitle = null;
        ivLeft = null;
        if (mAdapter != null) {
            mAdapter.release();
            mAdapter = null;
        }

        if (rcvConnectionMode != null) {
            rcvConnectionMode.setAdapter(null);
            rcvConnectionMode = null;
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

    private void gotoAddDevice(int connectionMode) {
        if (connectionMode != ConstantValue.CONNECTION_MODE_AP_DIRECT && !checkLogin("", "")) {
            return;
        }
        String model = getCurrentIntent() != null ? getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_IPC_MODEL) : IpcType.IPC_100.getType();
        if (connectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            if (NooieDeviceHelper.mergeIpcType(model) == IpcType.HC320) {
                /*
                Bundle param = new Bundle();
                param.putString(ConstantValue.INTENT_KEY_IPC_MODEL, model);
                param.putInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, connectionMode);
                ConnectBluetoothActivity.toConnectBluetoothActivity(this, param);
                 */
                AddACameraActivity.toAddACameraActivity(this, model, connectionMode);
            } else {
                Bundle param = new Bundle();
                param.putString(ConstantValue.INTENT_KEY_IPC_MODEL, model);
                param.putInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, connectionMode);
                param.putBoolean(ConstantValue.INTENT_KEY_DATA_PARAM, false);
                ConnectApDeviceActivity.toConnectApDeviceActivity(this, param);
            }
        } else {
            AddACameraActivity.toAddACameraActivity(this, model, connectionMode);
        }
    }

    private void addConnectModeEvent(ConnectionModeBean data) {
        if (data == null) {
            return;
        }
        NooieDeviceHelper.trackDNEvent(EventDictionary.EVENT_ID_CLICK_CONNECTION_MODE, NooieDeviceHelper.createConnectionModeDNExternal(NooieDeviceHelper.convertEventConnectionMode(data.getModel(), data.getConnectionMode())));
    }

    @Override
    public String getExternal() {
        return NooieDeviceHelper.createDistributionNetworkExternal(false);
    }
}
