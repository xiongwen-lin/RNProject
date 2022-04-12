package com.afar.osaio.smart.mixipc.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.mixipc.contract.ConnectBluetoothContract;
import com.afar.osaio.smart.mixipc.presenter.ConnectBluetoothPresenter;
import com.afar.osaio.util.ConstantValue;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ConnectBluetoothActivity extends BaseActivity implements ConnectBluetoothContract.View {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;

    private ConnectBluetoothContract.Presenter mPresenter;

    public static void toConnectBluetoothActivity(Context from, Bundle param) {
        Intent intent = new Intent(from, ConnectBluetoothActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_START_PARAM, param);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_bluetooth);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    private void initData() {
        new ConnectBluetoothPresenter(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.connect_bluetooth_title);
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
    }

    @OnClick({R.id.ivLeft, R.id.btnConnectBluetooth})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.btnConnectBluetooth: {
                Bundle param = new Bundle();
                param.putString(ConstantValue.INTENT_KEY_IPC_MODEL, getDeviceModel());
                param.putInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, getConnectionMode());
                param.putInt(ConstantValue.INTENT_KEY_DATA_PARAM_1, getBluetoothScanType());
                param.putString(ConstantValue.INTENT_KEY_BLE_DEVICE, getBleDeviceId());
                BluetoothScanActivity.toBluetoothScanActivity(this, param);
                break;
            }
        }
    }

    @Override
    public void setPresenter(@NonNull ConnectBluetoothContract.Presenter presenter) {
        mPresenter = presenter;
    }

    private String getDeviceModel() {
        if (getStartParam() == null) {
            return null;
        }
        return getStartParam().getString(ConstantValue.INTENT_KEY_IPC_MODEL);
    }

    private int getConnectionMode() {
        if (getStartParam() == null) {
            return ConstantValue.CONNECTION_MODE_QC;
        }
        return getStartParam().getInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_QC);
    }

    private int getBluetoothScanType() {
        if (getStartParam() == null) {
            return ConstantValue.BLUETOOTH_SCAN_TYPE_NEW;
        }
        return getStartParam().getInt(ConstantValue.INTENT_KEY_DATA_PARAM_1, ConstantValue.BLUETOOTH_SCAN_TYPE_NEW);
    }

    private String getBleDeviceId() {
        if (getStartParam() == null) {
            return null;
        }
        return getStartParam().getString(ConstantValue.INTENT_KEY_BLE_DEVICE);
    }
}
