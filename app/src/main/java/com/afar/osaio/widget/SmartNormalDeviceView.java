package com.afar.osaio.widget;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.device.helper.SmartDeviceHelper;
import com.afar.osaio.smart.electrician.manager.PowerStripHelper;
import com.afar.osaio.smart.home.bean.SmartBaseDevice;
import com.afar.osaio.smart.home.bean.SmartCameraDevice;
import com.afar.osaio.smart.home.bean.SmartRouterDevice;
import com.afar.osaio.smart.home.bean.SmartTyDevice;
import com.afar.osaio.util.CompatUtil;
import com.afar.osaio.widget.listener.SmartNormalDeviceViewListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.csdn.roundview.RoundImageView;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.nooie.sdk.device.bean.PirStateV2;
import com.nooie.sdk.device.bean.SensitivityLevel;
import com.suke.widget.SwitchButton;

import butterknife.BindView;
import butterknife.ButterKnife;

/***********************************************************
 * 作者: zhengruidong@apemans.com
 * 日期: 2021/11/29 8:00 下午
 * 说明:
 *
 * 备注:
 *
 ***********************************************************/
public class SmartNormalDeviceView extends LinearLayout {

    @BindView(R.id.ivItemSmartNormalDevicePreview)
    RoundImageView ivItemSmartNormalDevicePreview;
    @BindView(R.id.ivItemSmartNormalDeviceThumbnail)
    ImageView ivItemSmartNormalDeviceThumbnail;
    @BindView(R.id.tvItemSmartNormalDeviceName)
    TextView tvItemSmartNormalDeviceName;
    @BindView(R.id.tvItemSmartNormalDeviceState)
    TextView tvItemSmartNormalDeviceState;
    @BindView(R.id.ivItemSmartNormalDeviceMore)
    ImageView ivItemSmartNormalDeviceMore;
    @BindView(R.id.btnItemSmartNormalDeviceSwitch)
    SwitchImageButton btnItemSmartNormalDeviceSwitch;
    @BindView(R.id.btnItemSmartNormalDeviceSwitchButton)
    SwitchButton btnItemSmartNormalDeviceSwitchButton;
    @BindView(R.id.ivCloud)
    ImageView ivCloud;

    private SmartNormalDeviceViewListener mListener;

    public SmartNormalDeviceView(Context context) {
        super(context);
        init();
    }

    public SmartNormalDeviceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void refreshView(SmartBaseDevice device) {
        if (device == null) {
            return;
        }
        if (device instanceof SmartCameraDevice) {
            SmartCameraDevice cameraDevice = (SmartCameraDevice) device;
            boolean isOnLine = SmartDeviceHelper.checkIsDeviceStateOn(device.deviceState);
            boolean isSwitchOn = SmartDeviceHelper.checkIsDeviceSwitchStateOn(device.deviceSwitchState);
            ivItemSmartNormalDevicePreview.setVisibility(VISIBLE);
            ivItemSmartNormalDeviceThumbnail.setVisibility(GONE);
            loadDevicePreview(device.deviceIconUrl, R.drawable.default_preview, ivItemSmartNormalDevicePreview);
            String deviceName = !TextUtils.isEmpty(device.deviceName) ? device.deviceName : NooieDeviceHelper.convertModelToString(device.model);
            tvItemSmartNormalDeviceName.setText(device.deviceName);
            ivCloud.setVisibility(SmartDeviceHelper.checkIsCloudStateActive(cameraDevice.cloudState) ? VISIBLE : GONE);
            if (SmartDeviceHelper.checkDeviceInfoTypeIsBleNetSpot(cameraDevice.deviceInfoType)) {
                tvItemSmartNormalDeviceState.setText(getResources().getString(R.string.home_item_ble_ap_tip));
            } else if (SmartDeviceHelper.checkDeviceInfoTypeIsBleDirectLink(cameraDevice.deviceInfoType)) {
                tvItemSmartNormalDeviceState.setText("");
            } else {
                String deviceStateText = isOnLine ? "" : getResources().getString(R.string.offline);
                tvItemSmartNormalDeviceState.setText(deviceStateText);
            }
            int btnDeviceSwitchVisibility = isOnLine && !NooieDeviceHelper.isLpDevice(device.model)
                    && SmartDeviceHelper.checkIsOwnerDevice(device.bindType)
                    && cameraDevice != null && !SmartDeviceHelper.checkDeviceInfoTypeIsBleNetSpot(cameraDevice.deviceInfoType) ? VISIBLE : GONE;
            btnItemSmartNormalDeviceSwitchButton.setVisibility(btnDeviceSwitchVisibility);
            btnItemSmartNormalDeviceSwitch.setVisibility(GONE);
            btnItemSmartNormalDeviceSwitch.initBtn(R.drawable.ic_public_switch_on, R.drawable.ic_public_switch_off);
            if (btnItemSmartNormalDeviceSwitch.isOn() != isSwitchOn) {
                btnItemSmartNormalDeviceSwitch.toggleNoCallback();
            }
            if (btnItemSmartNormalDeviceSwitchButton.isChecked() != isSwitchOn) {
                btnItemSmartNormalDeviceSwitchButton.toggleNoCallback();
            }
            ivItemSmartNormalDeviceMore.setVisibility(GONE);
            btnItemSmartNormalDeviceSwitch.setListener(new SwitchImageButton.OnStateChangeListener() {
                @Override
                public void onStateChange(boolean on) {
                    if (mListener != null) {
                        mListener.onSwitchBtnClick(device, on);
                    }
                }
            });
            btnItemSmartNormalDeviceSwitchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                    if (mListener != null) {
                        mListener.onSwitchBtnClick(device, isChecked);
                    }
                }
            });
        } else if (device instanceof SmartTyDevice) {
            SmartTyDevice tyDevice = (SmartTyDevice) device;
            boolean isOnLine = SmartDeviceHelper.checkIsDeviceStateOn(device.deviceState);
            boolean isSwitchOn = SmartDeviceHelper.checkIsDeviceSwitchStateOn(device.deviceSwitchState);
            ivItemSmartNormalDevicePreview.setVisibility(GONE);
            ivItemSmartNormalDeviceThumbnail.setVisibility(VISIBLE);
            loadDeviceThumbnail(device.deviceIconUrl, R.drawable.ic_list_placeholder, ivItemSmartNormalDeviceThumbnail);
            tvItemSmartNormalDeviceName.setText(device.deviceName);
            tvItemSmartNormalDeviceState.setText(isOnLine ? "" : getResources().getString(R.string.offline));
            ivCloud.setVisibility(GONE);
            btnItemSmartNormalDeviceSwitch.setVisibility(GONE);
            btnItemSmartNormalDeviceSwitchButton.setVisibility((isOnLine ? View.VISIBLE : GONE));
            if (!tyDevice.productId.isEmpty() && isOnLine && PowerStripHelper.getInstance().checkPetFeederValid(tyDevice.productId)) {
                btnItemSmartNormalDeviceSwitchButton.setVisibility(GONE);
            }
            btnItemSmartNormalDeviceSwitch.initBtn(R.drawable.ic_public_switch_on, R.drawable.ic_public_switch_off);
            if (btnItemSmartNormalDeviceSwitch.isOn() != isSwitchOn) {
                btnItemSmartNormalDeviceSwitch.toggleNoCallback();
            }
            if (btnItemSmartNormalDeviceSwitchButton.isChecked() != isSwitchOn) {
                btnItemSmartNormalDeviceSwitchButton.toggleNoCallback();
            }
            ivItemSmartNormalDeviceMore.setVisibility(GONE);
            btnItemSmartNormalDeviceSwitch.setListener(new SwitchImageButton.OnStateChangeListener() {
                @Override
                public void onStateChange(boolean on) {
                    if (mListener != null) {
                        mListener.onSwitchBtnClick(device, on);
                    }
                }
            });
            btnItemSmartNormalDeviceSwitchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                    if (mListener != null) {
                        mListener.onSwitchBtnClick(device, isChecked);
                    }
                }
            });
        } else if (device instanceof SmartRouterDevice) {
            SmartRouterDevice routerDevice = (SmartRouterDevice) device;
            ivItemSmartNormalDevicePreview.setVisibility(GONE);
            ivCloud.setVisibility(GONE);
            ivItemSmartNormalDeviceThumbnail.setVisibility(VISIBLE);
            ivItemSmartNormalDeviceThumbnail.setImageResource(R.drawable.device_add_icon_lp_device_with_router);
            tvItemSmartNormalDeviceName.setText(device.deviceName);
            tvItemSmartNormalDeviceState.setText(device.deviceState);
            btnItemSmartNormalDeviceSwitch.setVisibility(GONE);
            btnItemSmartNormalDeviceSwitchButton.setVisibility(GONE);
            ivItemSmartNormalDeviceMore.setVisibility(VISIBLE);
        } else {
            btnItemSmartNormalDeviceSwitch.setListener(null);
            btnItemSmartNormalDeviceSwitchButton.setOnCheckedChangeListener(null);
        }
    }

    public void setListener(SmartNormalDeviceViewListener listener) {
        mListener = listener;
    }

    private void init() {
        View lvpView = LayoutInflater.from(getContext()).inflate(R.layout.layout_smart_normal_device, this, false);
        addView(lvpView);
        bindView(lvpView);
    }

    private void bindView(View view) {
        ButterKnife.bind(this, view);
    }

    private void loadDevicePreview(String url, int defaultResId, ImageView view) {
        Glide.with(getContext())
                .load(url)
                .apply(new RequestOptions()
                        .dontTransform().transform(new MultiTransformation<Bitmap>(new CenterCrop(), new RoundedCorners(DisplayUtil.dpToPx(NooieApplication.mCtx, 6))))
                        .placeholder(defaultResId)
                        .error(defaultResId)
                        .format(DecodeFormat.PREFER_RGB_565).diskCacheStrategy(DiskCacheStrategy.NONE)
                )
                .transition(withCrossFade())
                .into(view);
    }

    private void loadDeviceThumbnail(String url, int defaultResId, ImageView view) {
        Glide.with(getContext())
                .load(url)
                .apply(new RequestOptions()
                                //.dontTransform().transform(new MultiTransformation<Bitmap>(new CenterCrop(), new RoundedCorners(DisplayUtil.dpToPx(NooieApplication.mCtx, 6))))
                                .placeholder(defaultResId)
                                .error(defaultResId)
                                .format(DecodeFormat.PREFER_RGB_565)
                        //.diskCacheStrategy(DiskCacheStrategy.NONE)
                )
                //.transition(withCrossFade())
                .into(view);
    }
}
