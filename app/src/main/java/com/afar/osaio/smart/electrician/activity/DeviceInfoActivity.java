package com.afar.osaio.smart.electrician.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.electrician.bean.DeviceInfoBean;
import com.afar.osaio.smart.electrician.presenter.DeviceInfoPresenter;
import com.afar.osaio.smart.electrician.presenter.IDeviceInfoPresenter;
import com.afar.osaio.smart.electrician.util.DialogUtil;
import com.afar.osaio.smart.electrician.view.IDeviceInfoView;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.tuya.smart.sdk.bean.DeviceBean;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * DeviceInfoActivity
 *
 * @author Administrator
 * @date 2019/3/18
 */
public class DeviceInfoActivity extends BaseActivity implements IDeviceInfoView {

    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.tvInfoModel)
    TextView tvInfoModel;
    @BindView(R.id.tvInfoName)
    TextView tvInfoName;
    @BindView(R.id.tvInfoOwner)
    TextView tvInfoOwner;
    @BindView(R.id.tvInfoDeviceId)
    TextView tvInfoDeviceId;
    @BindView(R.id.tvInfoIp)
    TextView tvInfoIp;
    @BindView(R.id.containerInfoModel)
    LinearLayout containerInfoModel;
    @BindView(R.id.model_thin)
    View modelThin;
    @BindView(R.id.tvMac)
    TextView tvMac;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;

    private String mDeviceId;
    private IDeviceInfoPresenter mDeviceInfoPresenter;

    public static void toDeviceInfoActivity(Context from, String deviceId) {
        Intent intent = new Intent(from, DeviceInfoActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_information);
        ButterKnife.bind(this);
        initView();
        initData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDeviceInfoPresenter.release();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.information);
    }

    private void initData() {
        if (getCurrentIntent() == null || TextUtils.isEmpty(getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID))) {
            finish();
            return;
        } else {
            mDeviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
            mDeviceInfoPresenter = new DeviceInfoPresenter(this, mDeviceId);
            mDeviceInfoPresenter.loadDeviceInfo(mDeviceId);
            mDeviceInfoPresenter.getDeviceIp(mDeviceId);
        }
    }

    @OnClick({R.id.containerInfoDeviceId, R.id.ivLeft})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.containerInfoDeviceId:
                showCopyDialog();
                break;
            case R.id.ivLeft:
                finish();
                break;
        }
    }

    private void showCopyDialog() {
        DialogUtil.showConfirmWithSubMsgDialog(this, getResources().getString(R.string.device_id), mDeviceId, R.string.hide_upper_case,
                R.string.copy_upper_case, new DialogUtil.OnClickConfirmButtonListener() {
                    @Override
                    public void onClickRight() {
                        ClipboardManager clipboard = (ClipboardManager) DeviceInfoActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                        if (clipboard != null) {
                            clipboard.setPrimaryClip(ClipData.newPlainText(null, mDeviceId));//参数一：标签，可为空，参数二：要复制到剪贴板的文本
                            if (clipboard.hasPrimaryClip()) {
                                clipboard.getPrimaryClip().getItemAt(0).getText();
                            }
                        }
                        ToastUtil.showToast(DeviceInfoActivity.this, getResources().getString(R.string.copy_success));
                    }

                    @Override
                    public void onClickLeft() {

                    }
                });
    }


    private void showDeviceInfo(DeviceBean device) {
        if (device != null) {
            if (device.getProductId().equals(ConstantValue.SMART_PLUG_PRODUCTID)) {
                tvInfoModel.setText(ConstantValue.SMART_PORIK_PLUG);
            } else if (device.getProductId().equals(ConstantValue.SMART_PLUG_EU_PRODUCTID_TWO)
                    || device.getProductId().equals(ConstantValue.SMART_PLUG_EU_PRODUCTID_FIVE)) {
                tvInfoModel.setText(ConstantValue.SMART_PLUG_NAME_JP);
            } else if (device.getProductId().equals(ConstantValue.SMART_LAMP_PRODUCTID_ELEVEN)
                    || device.getProductId().equals(ConstantValue.SMART_LAMP_PRODUCTID_THIRTEEN)) {
                tvInfoModel.setText(ConstantValue.SMART_PORIK_LAMP);
            } else if (device.getProductId().equals(ConstantValue.SMART_PLUG_EU_PRODUCTID)
                    || device.getProductId().equals(ConstantValue.SMART_PLUG_EU_PRODUCTID_EIGHT)
                    || device.getProductId().equals(ConstantValue.SMART_PLUG_EU_PRODUCTID_FOUR)
            ) {
                tvInfoModel.setText(ConstantValue.SMART_PORIK_PLUG_TWO);
            } else if (device.getProductId().equals(ConstantValue.SMART_PLUG_EU_PRODUCTID_NINE)) {
                tvInfoModel.setText(ConstantValue.SMART_PLUG_EU_NAME_FOUR);
            } else if (device.getProductId().equals(ConstantValue.SMART_PLUG_PRODUCTID_NEW)
                    || device.getProductId().equals(ConstantValue.SMART_PLUG_PRODUCTID_OLD)
                    || device.getProductId().equals(ConstantValue.SMART_PLUG_PRODUCTID_NEW_TWO)
            ) {
                tvInfoModel.setText(ConstantValue.SMART_PLUG_US_NAME);
            }
           /* else if (device.getProductId().equals(ConstantValue.SMART_PLUG_JP_PRODUCTID)) {
                tvInfoModel.setText(ConstantValue.SMART_PLUG_NAME_JP);
            }*/
            else if (device.getProductId().equals(ConstantValue.SMART_LAMP_PRODUCTID)) {
                tvInfoModel.setText(ConstantValue.SMART_LAMP_NAME);
            } else if (device.getProductId().equals(ConstantValue.SMART_LAMP_PRODUCTID_TWO)
                    || device.getProductId().equals(ConstantValue.SMART_LAMP_PRODUCTID_THREE)
                    || device.getProductId().equals(ConstantValue.SMART_LAMP_PRODUCTID_FIVE)
                    || device.getProductId().equals(ConstantValue.SMART_LAMP_PRODUCTID_SIX)
                    || device.getProductId().equals(ConstantValue.SMART_LAMP_PRODUCTID_NINE)
                    || device.getProductId().equals(ConstantValue.SMART_LAMP_PRODUCTID_TEN)
            ) {
                tvInfoModel.setText(ConstantValue.SMART_LAMP_NAME_TWO);
            } else if (device.getProductId().equals(ConstantValue.SMART_LAMP_PRODUCTID_FOUR)) {
                tvInfoModel.setText(ConstantValue.SMART_LAMP_NAME_THREE);
            } else if (device.getProductId().equals(ConstantValue.SMART_PLUG_US_PRODUCTID_TWO)
                    || device.getProductId().equals(ConstantValue.SMART_PLUG_US_PRODUCTID_THREE)
                    || device.getProductId().equals(ConstantValue.SMART_PLUG_US_PRODUCTID_FOUR)
            ) {
                tvInfoModel.setText(ConstantValue.SMART_PLUG_US_NAME_TWO);
            } else if (device.getProductId().equals(ConstantValue.SMART_PLUG_UK_PRODUCTID_TWO)
                    || device.getProductId().equals(ConstantValue.SMART_PLUG_UK_PRODUCTID_THREE)) {
                tvInfoModel.setText(ConstantValue.SMART_PLUG_UK_NAME);
            } else if (device.getProductId().equals(ConstantValue.SMART_PLUG_UK_PRODUCTID)) {
                tvInfoModel.setText(ConstantValue.SMART_PORIK_PLUG_UK_NAME);
            } else if (device.getProductId().equals(ConstantValue.SMART_PLUG_EU_PRODUCTID_THREE)
                    || device.getProductId().equals(ConstantValue.SMART_PLUG_EU_PRODUCTID_SIX)
            ) {
                tvInfoModel.setText(ConstantValue.SMART_PLUG_EU_NAME_TWO);
            } else if (device.getProductId().equals(ConstantValue.SMART_STRIP_PRODUCTID)
                    || device.getProductId().equals(ConstantValue.SMART_STRIP_PRODUCTID_NEW)) {
                tvInfoModel.setText(ConstantValue.SMART_STRIP);
            } else if (device.getProductId().equals(ConstantValue.SMART_STRIP_PRODUCTID_TWO)) {
                tvInfoModel.setText(ConstantValue.SMART_STRIP_TWO);
            } else if (device.getProductId().equals(ConstantValue.SMART_STRIP_PRODUCTID_THREE)) {
                tvInfoModel.setText(ConstantValue.SMART_STRIP_THREE);
            } else if (device.getProductId().equals(ConstantValue.SMART_STRIP_PRODUCTID_FOUR)) {
                tvInfoModel.setText(ConstantValue.SMART_STRIP_FOUR);
            } else if (device.getProductId().equals(ConstantValue.SMART_SWITCH_PRODUCTID)
                    || device.getProductId().equals(ConstantValue.SMART_SWITCH_PRODUCTID_FIVE)
            ) {
                tvInfoModel.setText(ConstantValue.SMART_SWITCH);
            } else if (device.getProductId().equals(ConstantValue.SMART_SWITCH_PRODUCTID_TWO)
                    || device.getProductId().equals(ConstantValue.SMART_SWITCH_PRODUCTID_SIX)
            ) {
                tvInfoModel.setText(ConstantValue.SMART_SWITCH_TWO);
            } else if (device.getProductId().equals(ConstantValue.SMART_FLOOR_LAMP_PRODUCTID)
                    || device.getProductId().equals(ConstantValue.SMART_FLOOR_LAMP_PRODUCTID_TWO)) {
                tvInfoModel.setText(ConstantValue.SMART_FLOOR_LAMP);
            } else if (device.getProductId().equals(ConstantValue.SMART_LIGHT_MODULATOR_PRODUCTID)) {
                tvInfoModel.setText(ConstantValue.SMART_LIGHT_MODULATOR);
            } else if (device.getProductId().equals(ConstantValue.SMART_LIGHT_STRIP_PRODUCTID)) {
                tvInfoModel.setText(ConstantValue.SMART_LIGHT_STRIP);
            } else if (device.getProductId().equals(ConstantValue.SMART_LIGHT_STRIP_PRODUCTID_TWO)) {
                tvInfoModel.setText(ConstantValue.SMART_LIGHT_STRIP_TWO);
            } else if (device.getProductId().equals(ConstantValue.SMART_LIGHT_STRIP_PRODUCTID_THREE)) {
                tvInfoModel.setText(ConstantValue.SMART_LIGHT_STRIP_THREE);
            } else if (device.getProductId().equals(ConstantValue.SMART_LIGHT_STRIP_PRODUCTID_FOUR)) {
                tvInfoModel.setText(ConstantValue.SMART_LIGHT_STRIP_FOUR);
            } else if (device.getProductId().equals(ConstantValue.SMART_PLUG_JP_PRODUCTID_ONE)) {
                tvInfoModel.setText(ConstantValue.SMART_PLUG_NAME_JP);
            } else if (device.getProductId().equals(ConstantValue.SMART_SWITCH_PRODUCTID_THREE)) {
                tvInfoModel.setText(ConstantValue.SMART_SWITCH_THREE);
            } else if (device.getProductId().equals(ConstantValue.SMART_SWITCH_PRODUCTID_FOUR)) {
                tvInfoModel.setText(ConstantValue.SMART_SWITCH_FOUR);
            } else if (device.getProductId().equals(ConstantValue.SMART_STRIP_PRODUCTID_FIVE)) {
                tvInfoModel.setText(ConstantValue.SMART_STRIP_FIVE);
            } else if (device.getProductId().equals(ConstantValue.SMART_STRIP_PRODUCTID_SIX)) {
                tvInfoModel.setText(ConstantValue.SMART_STRIP_SIX);
            } else if (device.getProductId().equals(ConstantValue.SMART_PLUG_UK_PRODUCTID_FOUR)) {
                tvInfoModel.setText(ConstantValue.SMART_PLUG_UK_NAME_THREE);
            } else if (device.getProductId().equals(ConstantValue.SMART_PLUG_UK_PRODUCTID_FIVE)) {
                tvInfoModel.setText(ConstantValue.SMART_PLUG_UK_NAME_TWO);
            } else if (device.getProductId().equals(ConstantValue.SMART_LAMP_PRODUCTID_SEVEN)
                    || device.getProductId().equals(ConstantValue.SMART_LAMP_PRODUCTID_EIGHT)
                    || device.getProductId().equals(ConstantValue.SMART_LAMP_PRODUCTID_TWELVE)
            ) {
                tvInfoModel.setText(ConstantValue.SMART_LAMP_NAME_FOUR);
            } else if (device.getProductId().equals(ConstantValue.SMART_STRIP_PRODUCTID_SEVEN)) {
                tvInfoModel.setText(ConstantValue.SMART_STRIP_SEVEN);
            } else if (device.getProductId().equals(ConstantValue.SMART_PLUG_EU_PRODUCTID_SEVEN)) {
                tvInfoModel.setText(ConstantValue.SMART_PLUG_EU_NAME_THREE);
            } else if (device.getProductId().equals(ConstantValue.SMART_BULB_PRODUCTID)) {
                tvInfoModel.setText(ConstantValue.SMART_BULB);
            } else if (device.getProductId().equals(ConstantValue.SMART_FEEDER_PRODUCTID)) {
                tvInfoModel.setText(ConstantValue.SMART_FEEDER);
            } else if (device.getProductId().equals(ConstantValue.SMART_AIR_PURIFIER_PRODUCTID)) {
                tvInfoModel.setText(ConstantValue.SMART_AIR_PURIFIER);
            } else if (device.getProductId().equals(ConstantValue.SMART_PLUG_US_PRODUCTID_FIVE)) {
                tvInfoModel.setText(ConstantValue.SMART_PLUG_US_NAME_THREE);
            } else if (device.getProductId().equals(ConstantValue.SMART_PLUG_EU_PRODUCTID_TEN)) {
                tvInfoModel.setText(ConstantValue.SMART_PLUG_EU_NAME_FIVE);
            } else if (device.getProductId().equals(ConstantValue.SMART_PLUG_US_PRODUCTID_SIX)) {
                tvInfoModel.setText(ConstantValue.SMART_PLUG_US_NAME_FOUR);
            } else if (device.getProductId().equals(ConstantValue.SMART_PLUG_UK_PRODUCTID_SIX)) {
                tvInfoModel.setText(ConstantValue.SMART_PLUG_UK_NAME_FIVE);
            } else if (device.getProductId().equals(ConstantValue.SMART_PLUG_UK_PRODUCTID_SEVEN)) {
                tvInfoModel.setText(ConstantValue.SMART_PLUG_UK_NAME_FOUR);
            } else {
                tvInfoModel.setText(ConstantValue.DEVICE_DEFAULT_NAME);
            }

            tvInfoName.setText(device.getName());
            tvInfoOwner.setText(device.getUuid());
            tvInfoDeviceId.setText(device.getDevId());

            //判断isShara是不是分享设备
            //true  显示自己的邮箱账号
            //false 被分享者查找指定设备是谁共享过来的
            mDeviceInfoPresenter.queryShareDev(device.getDevId());
        }
    }

    @Override
    public void notifyLoadDeviceInfo(DeviceBean device) {
        showDeviceInfo(device);

    }

    @Override
    public void getDeviceIpSuccess(DeviceInfoBean deviceInfoBean) {
        if (deviceInfoBean != null) {
            tvInfoIp.setText(deviceInfoBean.getIp());
            tvMac.setText(deviceInfoBean.getMac());
        }
    }

    @Override
    public void getDeviceIpFail(String error) {

    }
}
