package com.afar.osaio.widget.helper;

import android.util.ArrayMap;

import com.afar.osaio.R;
import com.nooie.sdk.bean.IpcType;
import com.afar.osaio.util.ConstantValue;

import java.util.Map;

public class ResHelper {

    private Map<String, Integer> mDeviceIconMap = new ArrayMap<>();
    private Map<String, Integer> mDeviceSmallIconMap = new ArrayMap<>();
    private Map<String, Integer> mDeviceBigIconMap = new ArrayMap<>();
    private Map<String, Integer> mFlashLightOnMap = new ArrayMap<>();
    private Map<String, Integer> mFlashLightOffMap = new ArrayMap<>();
    private Map<String, Integer> mDeviceResetIconMap = new ArrayMap<>();
    private Map<String, Integer> mDeviceScanIconMap = new ArrayMap<>();
    private Map<String, Integer> mDeviceLightIconMap = new ArrayMap<>();
    private Map<String, Integer> mConnectionQcIconMap = new ArrayMap<>();
    private Map<String, Integer> mConnectionApIconMap = new ArrayMap<>();
    private Map<String, Integer> mConnectionDvIconMap = new ArrayMap<>();
    private Map<String, Integer> mConnectionLanIconMap = new ArrayMap<>();
    private Map<String, Integer> mDeviceScanCodeGuideIconMap = new ArrayMap<>();
    private Map<String, Integer> mGatewayAndCameraIconMap = new ArrayMap<>();
    private Map<String, Integer> mGatewayAndCameraRedIconMap = new ArrayMap<>();

    private Map<Integer, Integer> mProductCategoryStringMap = new ArrayMap<>();

    private ResHelper() {
        init();
    }

    private static final class ResHelperHolder {
        private static final ResHelper INSTANCE = new ResHelper();
    }

    public static ResHelper getInstance() {
        return ResHelperHolder.INSTANCE;
    }

    private void init() {
        setupResMap();
    }

    private void setupResMap() {
        mDeviceIconMap.put(IpcType.PC420.getType(), R.drawable.device_icon);
        mDeviceIconMap.put(IpcType.PC440.getType(), R.drawable.device_icon);
        mDeviceIconMap.put(IpcType.PC530.getType(), R.drawable.device_icon_360);
        mDeviceIconMap.put(IpcType.PC530PRO.getType(), R.drawable.device_icon_360);
        mDeviceIconMap.put(IpcType.PC540.getType(), R.drawable.device_icon_360);
        mDeviceIconMap.put(IpcType.PC660PRO.getType(), R.drawable.device_icon_360);
        mDeviceIconMap.put(IpcType.PC730.getType(), R.drawable.device_icon_outdoor);
        mDeviceIconMap.put(IpcType.PC770.getType(), R.drawable.device_icon_outdoor);
        mDeviceIconMap.put(IpcType.EC810_CAM.getType(), R.drawable.device_icon_lp_810);
        mDeviceIconMap.put(IpcType.EC810PRO.getType(), R.drawable.device_icon_lp_810);
        mDeviceIconMap.put(IpcType.EC810_PLUS.getType(), R.drawable.device_icon_ec810_plus);
        mDeviceIconMap.put(IpcType.HC320.getType(), R.drawable.device_icon_lp_hc_320);
        mDeviceIconMap.put(IpcType.MC120.getType(), R.drawable.device_icon);
        mDeviceIconMap.put(IpcType.SC100.getType(), R.drawable.device_icon_360);
        mDeviceIconMap.put(IpcType.SC210.getType(), R.drawable.device_icon_360);
        mDeviceIconMap.put(IpcType.SC220.getType(), R.drawable.device_icon_360);

        mDeviceIconMap.put(IpcType.C1.getType(), R.drawable.device_icon);
        mDeviceIconMap.put(IpcType.C1PRO.getType(), R.drawable.device_icon);
        mDeviceIconMap.put(IpcType.A1.getType(), R.drawable.device_icon);
        mDeviceIconMap.put(IpcType.P3.getType(), R.drawable.device_icon_360);
        mDeviceIconMap.put(IpcType.P3Pro.getType(), R.drawable.device_icon_360);
        mDeviceIconMap.put(IpcType.Q1.getType(), R.drawable.device_icon_outdoor);
        mDeviceIconMap.put(IpcType.T1.getType(), R.drawable.device_icon_outdoor);
        mDeviceIconMap.put(IpcType.T1PRO.getType(), R.drawable.device_icon_outdoor);
        mDeviceIconMap.put(IpcType.T2.getType(), R.drawable.device_icon_outdoor);
        mDeviceIconMap.put(IpcType.W0_CAM.getType(), R.drawable.device_icon_lp_810);
        mDeviceIconMap.put(IpcType.W1.getType(), R.drawable.device_icon_lp_810);
        mDeviceIconMap.put(IpcType.W2.getType(), R.drawable.ic_device_cam_w2);
        mDeviceIconMap.put(IpcType.M1.getType(), R.drawable.device_icon);
        mDeviceIconMap.put(IpcType.P1.getType(), R.drawable.device_icon_360);
        mDeviceIconMap.put(IpcType.P1PRO.getType(), R.drawable.device_icon_360);
        mDeviceIconMap.put(IpcType.P2.getType(), R.drawable.device_icon_360);
        mDeviceIconMap.put(IpcType.P4.getType(), R.drawable.device_icon_360);
        mDeviceIconMap.put(IpcType.K1.getType(), R.drawable.device_icon_360);
        mDeviceIconMap.put(IpcType.K1PRO.getType(), R.drawable.device_icon_360);
        mDeviceIconMap.put(IpcType.K2.getType(), R.drawable.device_icon_360);

        mDeviceIconMap.put(IpcType.TC100.getType(), R.drawable.device_icon);
        mDeviceIconMap.put(IpcType.XC100.getType(), R.drawable.device_icon);
        mDeviceIconMap.put(IpcType.TR100.getType(), R.drawable.device_icon_360);
        mDeviceIconMap.put(IpcType.TS200.getType(), R.drawable.device_icon_360);
        mDeviceIconMap.put(IpcType.TS100.getType(), R.drawable.device_icon_outdoor);

        mDeviceSmallIconMap.put("VL-MX1800", R.drawable.device_add_icon_lp_device_with_router);
        mDeviceSmallIconMap.put(IpcType.PC420.getType(), R.drawable.device_small_icon);
        mDeviceSmallIconMap.put(IpcType.PC440.getType(), R.drawable.device_small_icon_tc100);
        mDeviceSmallIconMap.put(IpcType.PC530.getType(), R.drawable.device_small_icon_360);
        mDeviceSmallIconMap.put(IpcType.PC530PRO.getType(), R.drawable.device_small_icon_360);
        mDeviceSmallIconMap.put(IpcType.PC540.getType(), R.drawable.device_small_icon_360);
        mDeviceSmallIconMap.put(IpcType.PC660PRO.getType(), R.drawable.device_small_icon_360);
        mDeviceSmallIconMap.put(IpcType.PC730.getType(), R.drawable.device_small_icon_outdoor);
        mDeviceSmallIconMap.put(IpcType.PC770.getType(), R.drawable.device_small_icon_pc770);
        mDeviceSmallIconMap.put(IpcType.EC810_CAM.getType(), R.drawable.device_small_icon_lp_810);
        mDeviceSmallIconMap.put(IpcType.EC810PRO.getType(), R.drawable.device_small_icon_lp_810);
        mDeviceSmallIconMap.put(IpcType.EC810_PLUS.getType(), R.drawable.device_small_icon_ec810_plus);
        mDeviceSmallIconMap.put(IpcType.HC320.getType(), R.drawable.device_small_icon_lp_hc_320);
        mDeviceSmallIconMap.put(IpcType.MC120.getType(), R.drawable.device_small_icon_mc120);
        mDeviceSmallIconMap.put(IpcType.SC100.getType(), R.drawable.device_small_icon_sc100);
        mDeviceSmallIconMap.put(IpcType.SC210.getType(), R.drawable.device_small_icon_sc210);
        mDeviceSmallIconMap.put(IpcType.SC220.getType(), R.drawable.device_small_icon_sc220);

        mDeviceSmallIconMap.put(IpcType.C1.getType(), R.drawable.device_small_icon);
        mDeviceSmallIconMap.put(IpcType.C1PRO.getType(), R.drawable.device_small_icon);
        mDeviceSmallIconMap.put(IpcType.A1.getType(), R.drawable.device_small_icon_tc100);
        mDeviceSmallIconMap.put(IpcType.P3.getType(), R.drawable.device_small_icon_360);
        mDeviceSmallIconMap.put(IpcType.P3Pro.getType(), R.drawable.device_small_icon_360);
        mDeviceSmallIconMap.put(IpcType.Q1.getType(), R.drawable.device_small_icon_outdoor);
        mDeviceSmallIconMap.put(IpcType.T1.getType(), R.drawable.device_small_icon_pc770);
        mDeviceSmallIconMap.put(IpcType.T1PRO.getType(), R.drawable.device_small_icon_pc770);
        mDeviceSmallIconMap.put(IpcType.T2.getType(), R.drawable.device_small_icon_outdoor);
        mDeviceSmallIconMap.put(IpcType.W0_CAM.getType(), R.drawable.device_small_icon_lp_810);
        mDeviceSmallIconMap.put(IpcType.W1.getType(), R.drawable.device_small_icon_lp_810);
        mDeviceSmallIconMap.put(IpcType.W2.getType(), R.drawable.ic_device_cam_w2);
        mDeviceSmallIconMap.put(IpcType.M1.getType(), R.drawable.device_small_icon_mc120);
        mDeviceSmallIconMap.put(IpcType.P1.getType(), R.drawable.device_small_icon_sc100);
        mDeviceSmallIconMap.put(IpcType.P1PRO.getType(), R.drawable.device_small_icon_sc100);
        mDeviceSmallIconMap.put(IpcType.P2.getType(), R.drawable.device_small_icon_sc210);
        mDeviceSmallIconMap.put(IpcType.P4.getType(), R.drawable.device_small_icon_sc220);
        mDeviceSmallIconMap.put(IpcType.K1.getType(), R.drawable.device_small_icon_ts200);
        mDeviceSmallIconMap.put(IpcType.K1PRO.getType(), R.drawable.device_small_icon_ts200);
        mDeviceSmallIconMap.put(IpcType.K2.getType(), R.drawable.device_small_icon_360);

        mDeviceSmallIconMap.put(IpcType.TC100.getType(), R.drawable.device_small_icon_tc100);
        mDeviceSmallIconMap.put(IpcType.XC100.getType(), R.drawable.device_small_icon_tc100);
        mDeviceSmallIconMap.put(IpcType.TR100.getType(), R.drawable.device_small_icon_tr100);
        mDeviceSmallIconMap.put(IpcType.TS200.getType(), R.drawable.device_small_icon_ts200);
        mDeviceSmallIconMap.put(IpcType.TS100.getType(), R.drawable.device_small_icon_ts100);

        mDeviceBigIconMap.put(IpcType.PC420.getType(), R.drawable.nooie_cam_big);
        mDeviceBigIconMap.put(IpcType.PC440.getType(), R.drawable.nooie_cam_big);
        mDeviceBigIconMap.put(IpcType.PC530.getType(), R.drawable.nooie360_cam_big);
        mDeviceBigIconMap.put(IpcType.PC530PRO.getType(), R.drawable.nooie360_cam_big);
        mDeviceBigIconMap.put(IpcType.PC540.getType(), R.drawable.nooie360_cam_big);
        mDeviceBigIconMap.put(IpcType.PC660PRO.getType(), R.drawable.nooie360_cam_big);
        mDeviceBigIconMap.put(IpcType.PC730.getType(), R.drawable.nooie_outdoor_cam_big);
        mDeviceBigIconMap.put(IpcType.PC770.getType(), R.drawable.nooie_outdoor_cam_big);
        mDeviceBigIconMap.put(IpcType.MC120.getType(), R.drawable.nooie_cam_big);
        mDeviceBigIconMap.put(IpcType.SC100.getType(), R.drawable.nooie360_cam_big);
        mDeviceBigIconMap.put(IpcType.SC210.getType(), R.drawable.nooie360_cam_big);
        mDeviceBigIconMap.put(IpcType.SC220.getType(), R.drawable.nooie360_cam_big);
        mDeviceBigIconMap.put(IpcType.HC320.getType(), R.drawable.device_add_icon_lp_hc_320);

        mDeviceBigIconMap.put(IpcType.C1.getType(), R.drawable.nooie_cam_big);
        mDeviceBigIconMap.put(IpcType.C1PRO.getType(), R.drawable.nooie_cam_big);
        mDeviceBigIconMap.put(IpcType.A1.getType(), R.drawable.nooie_cam_big);
        mDeviceBigIconMap.put(IpcType.P3.getType(), R.drawable.nooie360_cam_big);
        mDeviceBigIconMap.put(IpcType.P3Pro.getType(), R.drawable.nooie360_cam_big);
        mDeviceBigIconMap.put(IpcType.Q1.getType(), R.drawable.nooie_outdoor_cam_big);
        mDeviceBigIconMap.put(IpcType.T1.getType(), R.drawable.nooie_outdoor_cam_big);
        mDeviceBigIconMap.put(IpcType.T1PRO.getType(), R.drawable.nooie_outdoor_cam_big);
        mDeviceBigIconMap.put(IpcType.T2.getType(), R.drawable.nooie_outdoor_cam_big);
        mDeviceBigIconMap.put(IpcType.M1.getType(), R.drawable.nooie_cam_big);
        mDeviceBigIconMap.put(IpcType.P1.getType(), R.drawable.nooie360_cam_big);
        mDeviceBigIconMap.put(IpcType.P1PRO.getType(), R.drawable.nooie360_cam_big);
        mDeviceBigIconMap.put(IpcType.P2.getType(), R.drawable.nooie360_cam_big);
        mDeviceBigIconMap.put(IpcType.P4.getType(), R.drawable.nooie360_cam_big);
        mDeviceBigIconMap.put(IpcType.K1.getType(), R.drawable.nooie360_cam_big);
        mDeviceBigIconMap.put(IpcType.K1PRO.getType(), R.drawable.nooie360_cam_big);
        mDeviceBigIconMap.put(IpcType.K2.getType(), R.drawable.nooie360_cam_big);

        mDeviceBigIconMap.put(IpcType.TC100.getType(), R.drawable.nooie_cam_big);
        mDeviceBigIconMap.put(IpcType.XC100.getType(), R.drawable.nooie_cam_big);
        mDeviceBigIconMap.put(IpcType.TR100.getType(), R.drawable.nooie360_cam_big);
        mDeviceBigIconMap.put(IpcType.TS200.getType(), R.drawable.nooie360_cam_big);
        mDeviceBigIconMap.put(IpcType.TS100.getType(), R.drawable.nooie_outdoor_cam_big);

        mFlashLightOnMap.put(IpcType.PC420.getType(), R.drawable.red_light);
        mFlashLightOnMap.put(IpcType.PC440.getType(), R.drawable.red_light);
        mFlashLightOnMap.put(IpcType.PC530.getType(), R.drawable.red_light360);
        mFlashLightOnMap.put(IpcType.PC530PRO.getType(), R.drawable.red_light360);
        mFlashLightOnMap.put(IpcType.PC540.getType(), R.drawable.red_light360);
        mFlashLightOnMap.put(IpcType.PC660PRO.getType(), R.drawable.red_light360);
        mFlashLightOnMap.put(IpcType.PC730.getType(), R.drawable.red_light_outdoor);
        mFlashLightOnMap.put(IpcType.PC770.getType(), R.drawable.red_light_outdoor);
        mFlashLightOnMap.put(IpcType.MC120.getType(), R.drawable.red_light);
        mFlashLightOnMap.put(IpcType.SC100.getType(), R.drawable.red_light360);
        mFlashLightOnMap.put(IpcType.SC210.getType(), R.drawable.red_light360);
        mFlashLightOnMap.put(IpcType.SC220.getType(), R.drawable.red_light360);
        mFlashLightOnMap.put(IpcType.HC320.getType(), R.drawable.device_add_icon_lp_hc_320);
        mFlashLightOnMap.put(IpcType.EC810PRO.getType(), R.drawable.device_add_icon_lp_device);
        mFlashLightOnMap.put(IpcType.EC810_PLUS.getType(), R.drawable.device_add_icon_ec810_plus);

        mFlashLightOnMap.put(IpcType.C1.getType(), R.drawable.red_light);
        mFlashLightOnMap.put(IpcType.C1PRO.getType(), R.drawable.red_light);
        mFlashLightOnMap.put(IpcType.A1.getType(), R.drawable.red_light);
        mFlashLightOnMap.put(IpcType.P3.getType(), R.drawable.red_light360);
        mFlashLightOnMap.put(IpcType.P3Pro.getType(), R.drawable.red_light360);
        mFlashLightOnMap.put(IpcType.Q1.getType(), R.drawable.red_light_outdoor);
        mFlashLightOnMap.put(IpcType.T1.getType(), R.drawable.red_light_outdoor);
        mFlashLightOnMap.put(IpcType.T1PRO.getType(), R.drawable.red_light_outdoor);
        mFlashLightOnMap.put(IpcType.T2.getType(), R.drawable.red_light_outdoor);
        mFlashLightOnMap.put(IpcType.M1.getType(), R.drawable.red_light);
        mFlashLightOnMap.put(IpcType.P1.getType(), R.drawable.red_light360);
        mFlashLightOnMap.put(IpcType.P1PRO.getType(), R.drawable.red_light360);
        mFlashLightOnMap.put(IpcType.P2.getType(), R.drawable.red_light360);
        mFlashLightOnMap.put(IpcType.P4.getType(), R.drawable.red_light360);
        mFlashLightOnMap.put(IpcType.K1.getType(), R.drawable.red_light360);
        mFlashLightOnMap.put(IpcType.K1PRO.getType(), R.drawable.red_light360);
        mFlashLightOnMap.put(IpcType.K2.getType(), R.drawable.red_light360);
        mFlashLightOnMap.put(IpcType.W1.getType(), R.drawable.device_add_icon_lp_device);
        mFlashLightOnMap.put(IpcType.W2.getType(), R.drawable.ic_device_cam_w2);

        mFlashLightOnMap.put(IpcType.TC100.getType(), R.drawable.red_light);
        mFlashLightOnMap.put(IpcType.XC100.getType(), R.drawable.red_light);
        mFlashLightOnMap.put(IpcType.TR100.getType(), R.drawable.red_light360);
        mFlashLightOnMap.put(IpcType.TS200.getType(), R.drawable.red_light360);
        mFlashLightOnMap.put(IpcType.TS100.getType(), R.drawable.red_light_outdoor);

        mFlashLightOffMap.put(IpcType.PC420.getType(), R.drawable.red_light_no);
        mFlashLightOffMap.put(IpcType.PC440.getType(), R.drawable.red_light_no);
        mFlashLightOffMap.put(IpcType.PC530.getType(), R.drawable.red_light_no360);
        mFlashLightOffMap.put(IpcType.PC530PRO.getType(), R.drawable.red_light_no360);
        mFlashLightOffMap.put(IpcType.PC540.getType(), R.drawable.red_light_no360);
        mFlashLightOffMap.put(IpcType.PC660PRO.getType(), R.drawable.red_light_no360);
        mFlashLightOffMap.put(IpcType.PC730.getType(), R.drawable.red_light_no_outdoor);
        mFlashLightOffMap.put(IpcType.PC770.getType(), R.drawable.red_light_no_outdoor);
        mFlashLightOffMap.put(IpcType.MC120.getType(), R.drawable.red_light_no);
        mFlashLightOffMap.put(IpcType.SC100.getType(), R.drawable.red_light_no360);
        mFlashLightOffMap.put(IpcType.SC210.getType(), R.drawable.red_light_no360);
        mFlashLightOffMap.put(IpcType.SC220.getType(), R.drawable.red_light_no360);
        mFlashLightOffMap.put(IpcType.HC320.getType(), R.drawable.device_add_icon_lp_hc_320);

        mFlashLightOffMap.put(IpcType.C1.getType(), R.drawable.red_light_no);
        mFlashLightOffMap.put(IpcType.C1PRO.getType(), R.drawable.red_light_no);
        mFlashLightOffMap.put(IpcType.A1.getType(), R.drawable.red_light_no);
        mFlashLightOffMap.put(IpcType.P3.getType(), R.drawable.red_light_no360);
        mFlashLightOffMap.put(IpcType.P3Pro.getType(), R.drawable.red_light_no360);
        mFlashLightOffMap.put(IpcType.Q1.getType(), R.drawable.red_light_no_outdoor);
        mFlashLightOffMap.put(IpcType.T1.getType(), R.drawable.red_light_no_outdoor);
        mFlashLightOffMap.put(IpcType.T1PRO.getType(), R.drawable.red_light_no_outdoor);
        mFlashLightOffMap.put(IpcType.T2.getType(), R.drawable.red_light_no_outdoor);
        mFlashLightOffMap.put(IpcType.M1.getType(), R.drawable.red_light_no);
        mFlashLightOffMap.put(IpcType.P1.getType(), R.drawable.red_light_no360);
        mFlashLightOffMap.put(IpcType.P1PRO.getType(), R.drawable.red_light_no360);
        mFlashLightOffMap.put(IpcType.P2.getType(), R.drawable.red_light_no360);
        mFlashLightOffMap.put(IpcType.P4.getType(), R.drawable.red_light_no360);
        mFlashLightOffMap.put(IpcType.K1.getType(), R.drawable.red_light_no360);
        mFlashLightOffMap.put(IpcType.K1PRO.getType(), R.drawable.red_light_no360);
        mFlashLightOffMap.put(IpcType.K2.getType(), R.drawable.red_light_no360);

        mFlashLightOffMap.put(IpcType.TC100.getType(), R.drawable.red_light_no);
        mFlashLightOffMap.put(IpcType.XC100.getType(), R.drawable.red_light_no);
        mFlashLightOffMap.put(IpcType.TR100.getType(), R.drawable.red_light_no360);
        mFlashLightOffMap.put(IpcType.TS200.getType(), R.drawable.red_light_no360);
        mFlashLightOffMap.put(IpcType.TS100.getType(), R.drawable.red_light_no_outdoor);

        mDeviceResetIconMap.put(IpcType.PC420.getType(), R.drawable.reset_cam720);
        mDeviceResetIconMap.put(IpcType.PC440.getType(), R.drawable.reset_cam720);
        mDeviceResetIconMap.put(IpcType.PC530.getType(), R.drawable.reset_cam360);
        mDeviceResetIconMap.put(IpcType.PC530PRO.getType(), R.drawable.reset_cam360);
        mDeviceResetIconMap.put(IpcType.PC540.getType(), R.drawable.reset_cam360);
        mDeviceResetIconMap.put(IpcType.PC660PRO.getType(), R.drawable.reset_cam360);
        mDeviceResetIconMap.put(IpcType.PC730.getType(), R.drawable.reset_cam_outdoor);
        mDeviceResetIconMap.put(IpcType.PC770.getType(), R.drawable.reset_cam_outdoor);
        mDeviceResetIconMap.put(IpcType.MC120.getType(), R.drawable.reset_cam720);
        mDeviceResetIconMap.put(IpcType.SC100.getType(), R.drawable.reset_cam360);
        mDeviceResetIconMap.put(IpcType.SC210.getType(), R.drawable.reset_cam360);
        mDeviceResetIconMap.put(IpcType.HC320.getType(), R.drawable.device_reset_icon_lp_hc_320);
        mDeviceResetIconMap.put(IpcType.EC810_CAM.getType(), R.drawable.device_reset_icon_lp_device);
        mDeviceResetIconMap.put(IpcType.EC810PRO.getType(), R.drawable.device_reset_icon_lp_device);
        mDeviceResetIconMap.put(IpcType.EC810_PLUS.getType(), R.drawable.device_reset_icon_ec810_plus);

        mDeviceResetIconMap.put(IpcType.C1.getType(), R.drawable.reset_cam720);
        mDeviceResetIconMap.put(IpcType.C1PRO.getType(), R.drawable.reset_cam720);
        mDeviceResetIconMap.put(IpcType.A1.getType(), R.drawable.reset_cam720);
        mDeviceResetIconMap.put(IpcType.P3.getType(), R.drawable.reset_cam360);
        mDeviceResetIconMap.put(IpcType.P3Pro.getType(), R.drawable.reset_cam360);
        mDeviceResetIconMap.put(IpcType.Q1.getType(), R.drawable.reset_cam_outdoor);
        mDeviceResetIconMap.put(IpcType.T1.getType(), R.drawable.reset_cam_outdoor);
        mDeviceResetIconMap.put(IpcType.T1PRO.getType(), R.drawable.reset_cam_outdoor);
        mDeviceResetIconMap.put(IpcType.T2.getType(), R.drawable.reset_cam_outdoor);
        mDeviceResetIconMap.put(IpcType.M1.getType(), R.drawable.reset_cam720);
        mDeviceResetIconMap.put(IpcType.P1.getType(), R.drawable.reset_cam360);
        mDeviceResetIconMap.put(IpcType.P1PRO.getType(), R.drawable.reset_cam360);
        mDeviceResetIconMap.put(IpcType.P2.getType(), R.drawable.reset_cam360);
        mDeviceResetIconMap.put(IpcType.P4.getType(), R.drawable.reset_cam360);
        mDeviceResetIconMap.put(IpcType.K1.getType(), R.drawable.reset_cam360);
        mDeviceResetIconMap.put(IpcType.K1PRO.getType(), R.drawable.reset_cam360);
        mDeviceResetIconMap.put(IpcType.K2.getType(), R.drawable.reset_cam360);
        mDeviceResetIconMap.put(IpcType.W1.getType(), R.drawable.device_reset_icon_lp_device);
        mDeviceResetIconMap.put(IpcType.W2.getType(), R.drawable.ic_device_reset_w2);

        mDeviceResetIconMap.put(IpcType.TC100.getType(), R.drawable.reset_cam720);
        mDeviceResetIconMap.put(IpcType.XC100.getType(), R.drawable.reset_cam720);
        mDeviceResetIconMap.put(IpcType.TR100.getType(), R.drawable.reset_cam360);
        mDeviceResetIconMap.put(IpcType.TS200.getType(), R.drawable.reset_cam360);
        mDeviceResetIconMap.put(IpcType.TS100.getType(), R.drawable.reset_cam_outdoor);

        mDeviceScanIconMap.put(IpcType.PC420.getType(), R.drawable.diagram);
        mDeviceScanIconMap.put(IpcType.PC440.getType(), R.drawable.diagram);
        mDeviceScanIconMap.put(IpcType.PC530.getType(), R.drawable.diagram360);
        mDeviceScanIconMap.put(IpcType.PC530PRO.getType(), R.drawable.diagram360);
        mDeviceScanIconMap.put(IpcType.PC540.getType(), R.drawable.diagram360);
        mDeviceScanIconMap.put(IpcType.PC660PRO.getType(), R.drawable.diagram360);
        mDeviceScanIconMap.put(IpcType.PC730.getType(), R.drawable.diagram_outdoor);
        mDeviceScanIconMap.put(IpcType.PC770.getType(), R.drawable.diagram_outdoor);
        mDeviceScanIconMap.put(IpcType.MC120.getType(), R.drawable.diagram);
        mDeviceScanIconMap.put(IpcType.SC100.getType(), R.drawable.diagram360);
        mDeviceScanIconMap.put(IpcType.SC210.getType(), R.drawable.diagram360);
        mDeviceScanIconMap.put(IpcType.SC220.getType(), R.drawable.diagram360);

        mDeviceScanIconMap.put(IpcType.C1.getType(), R.drawable.diagram);
        mDeviceScanIconMap.put(IpcType.C1PRO.getType(), R.drawable.diagram);
        mDeviceScanIconMap.put(IpcType.A1.getType(), R.drawable.diagram);
        mDeviceScanIconMap.put(IpcType.P3.getType(), R.drawable.diagram360);
        mDeviceScanIconMap.put(IpcType.P3Pro.getType(), R.drawable.diagram360);
        mDeviceScanIconMap.put(IpcType.Q1.getType(), R.drawable.diagram_outdoor);
        mDeviceScanIconMap.put(IpcType.T1.getType(), R.drawable.diagram_outdoor);
        mDeviceScanIconMap.put(IpcType.T1PRO.getType(), R.drawable.diagram_outdoor);
        mDeviceScanIconMap.put(IpcType.T2.getType(), R.drawable.diagram_outdoor);
        mDeviceScanIconMap.put(IpcType.M1.getType(), R.drawable.diagram);
        mDeviceScanIconMap.put(IpcType.P1.getType(), R.drawable.diagram360);
        mDeviceScanIconMap.put(IpcType.P1PRO.getType(), R.drawable.diagram360);
        mDeviceScanIconMap.put(IpcType.P2.getType(), R.drawable.diagram360);
        mDeviceScanIconMap.put(IpcType.P4.getType(), R.drawable.diagram360);
        mDeviceScanIconMap.put(IpcType.K1.getType(), R.drawable.diagram360);
        mDeviceScanIconMap.put(IpcType.K1PRO.getType(), R.drawable.diagram360);
        mDeviceScanIconMap.put(IpcType.K2.getType(), R.drawable.diagram360);

        mDeviceScanIconMap.put(IpcType.TC100.getType(), R.drawable.diagram);
        mDeviceScanIconMap.put(IpcType.XC100.getType(), R.drawable.diagram);
        mDeviceScanIconMap.put(IpcType.TR100.getType(), R.drawable.diagram360);
        mDeviceScanIconMap.put(IpcType.TS200.getType(), R.drawable.diagram360);
        mDeviceScanIconMap.put(IpcType.TS100.getType(), R.drawable.diagram_outdoor);

        mDeviceLightIconMap.put(IpcType.PC420.getType(), R.drawable.light_cam720);
        mDeviceLightIconMap.put(IpcType.PC440.getType(), R.drawable.light_cam720);
        mDeviceLightIconMap.put(IpcType.PC530.getType(), R.drawable.light_cam360);
        mDeviceLightIconMap.put(IpcType.PC530PRO.getType(), R.drawable.light_cam360);
        mDeviceLightIconMap.put(IpcType.PC540.getType(), R.drawable.light_cam360);
        mDeviceLightIconMap.put(IpcType.PC660PRO.getType(), R.drawable.light_cam360);
        mDeviceLightIconMap.put(IpcType.PC730.getType(), R.drawable.light_cam_outdoor);
        mDeviceLightIconMap.put(IpcType.PC770.getType(), R.drawable.light_cam_outdoor);
        mDeviceLightIconMap.put(IpcType.MC120.getType(), R.drawable.light_cam720);
        mDeviceLightIconMap.put(IpcType.SC100.getType(), R.drawable.light_cam360);
        mDeviceLightIconMap.put(IpcType.SC210.getType(), R.drawable.light_cam360);
        mDeviceLightIconMap.put(IpcType.SC220.getType(), R.drawable.light_cam360);
        mDeviceLightIconMap.put(IpcType.HC320.getType(), R.drawable.device_add_icon_lp_hc_320);
        mDeviceLightIconMap.put(IpcType.EC810_PLUS.getType(), R.drawable.device_icon_ec810_plus);

        mDeviceLightIconMap.put(IpcType.C1.getType(), R.drawable.light_cam720);
        mDeviceLightIconMap.put(IpcType.C1PRO.getType(), R.drawable.light_cam720);
        mDeviceLightIconMap.put(IpcType.A1.getType(), R.drawable.light_cam720);
        mDeviceLightIconMap.put(IpcType.P3.getType(), R.drawable.light_cam360);
        mDeviceLightIconMap.put(IpcType.P3Pro.getType(), R.drawable.light_cam360);
        mDeviceLightIconMap.put(IpcType.Q1.getType(), R.drawable.light_cam_outdoor);
        mDeviceLightIconMap.put(IpcType.T1.getType(), R.drawable.light_cam_outdoor);
        mDeviceLightIconMap.put(IpcType.T1PRO.getType(), R.drawable.light_cam_outdoor);
        mDeviceLightIconMap.put(IpcType.T2.getType(), R.drawable.light_cam_outdoor);
        mDeviceLightIconMap.put(IpcType.M1.getType(), R.drawable.light_cam720);
        mDeviceLightIconMap.put(IpcType.P1.getType(), R.drawable.light_cam360);
        mDeviceLightIconMap.put(IpcType.P1PRO.getType(), R.drawable.light_cam360);
        mDeviceLightIconMap.put(IpcType.P2.getType(), R.drawable.light_cam360);
        mDeviceLightIconMap.put(IpcType.P4.getType(), R.drawable.light_cam360);
        mDeviceLightIconMap.put(IpcType.K1.getType(), R.drawable.light_cam360);
        mDeviceLightIconMap.put(IpcType.K1PRO.getType(), R.drawable.light_cam360);
        mDeviceLightIconMap.put(IpcType.K2.getType(), R.drawable.light_cam360);
        mDeviceLightIconMap.put(IpcType.W1.getType(), R.drawable.device_icon_ec810_plus);

        mDeviceLightIconMap.put(IpcType.TC100.getType(), R.drawable.light_cam720);
        mDeviceLightIconMap.put(IpcType.XC100.getType(), R.drawable.light_cam720);
        mDeviceLightIconMap.put(IpcType.TR100.getType(), R.drawable.light_cam360);
        mDeviceLightIconMap.put(IpcType.TS200.getType(), R.drawable.light_cam360);
        mDeviceLightIconMap.put(IpcType.TS100.getType(), R.drawable.light_cam_outdoor);

        mConnectionQcIconMap.put(IpcType.PC420.getType(), R.drawable.connection_mode_qc);
        mConnectionQcIconMap.put(IpcType.PC440.getType(), R.drawable.connection_mode_qc);
        mConnectionQcIconMap.put(IpcType.PC530.getType(), R.drawable.connection_mode_wireless);
        mConnectionQcIconMap.put(IpcType.PC530PRO.getType(), R.drawable.connection_mode_wireless);
        mConnectionQcIconMap.put(IpcType.PC540.getType(), R.drawable.connection_mode_wireless);
        mConnectionQcIconMap.put(IpcType.PC660PRO.getType(), R.drawable.connection_mode_wireless);
        mConnectionQcIconMap.put(IpcType.PC730.getType(), R.drawable.connection_mode_qc_outdoor);
        mConnectionQcIconMap.put(IpcType.PC770.getType(), R.drawable.connection_mode_qc_outdoor);
        mConnectionQcIconMap.put(IpcType.MC120.getType(), R.drawable.connection_mode_qc);
        mConnectionQcIconMap.put(IpcType.SC100.getType(), R.drawable.connection_mode_wireless);
        mConnectionQcIconMap.put(IpcType.SC210.getType(), R.drawable.connection_mode_wireless);
        mConnectionQcIconMap.put(IpcType.SC220.getType(), R.drawable.connection_mode_wireless);
        mConnectionQcIconMap.put(IpcType.HC320.getType(), R.drawable.connection_mode_qc_hc_320);

        mConnectionQcIconMap.put(IpcType.C1.getType(), R.drawable.connection_mode_qc);
        mConnectionQcIconMap.put(IpcType.C1PRO.getType(), R.drawable.connection_mode_qc);
        mConnectionQcIconMap.put(IpcType.A1.getType(), R.drawable.connection_mode_qc);
        mConnectionQcIconMap.put(IpcType.P3.getType(), R.drawable.connection_mode_wireless);
        mConnectionQcIconMap.put(IpcType.P3Pro.getType(), R.drawable.connection_mode_wireless);
        mConnectionQcIconMap.put(IpcType.Q1.getType(), R.drawable.connection_mode_qc_outdoor);
        mConnectionQcIconMap.put(IpcType.T1.getType(), R.drawable.connection_mode_qc_outdoor);
        mConnectionQcIconMap.put(IpcType.T1PRO.getType(), R.drawable.connection_mode_qc_outdoor);
        mConnectionQcIconMap.put(IpcType.T2.getType(), R.drawable.connection_mode_qc_outdoor);
        mConnectionQcIconMap.put(IpcType.M1.getType(), R.drawable.connection_mode_qc);
        mConnectionQcIconMap.put(IpcType.P1.getType(), R.drawable.connection_mode_wireless);
        mConnectionQcIconMap.put(IpcType.P1PRO.getType(), R.drawable.connection_mode_wireless);
        mConnectionQcIconMap.put(IpcType.P2.getType(), R.drawable.connection_mode_wireless);
        mConnectionQcIconMap.put(IpcType.P4.getType(), R.drawable.connection_mode_wireless);
        mConnectionQcIconMap.put(IpcType.K1.getType(), R.drawable.connection_mode_wireless);
        mConnectionQcIconMap.put(IpcType.K1PRO.getType(), R.drawable.connection_mode_wireless);
        mConnectionQcIconMap.put(IpcType.K2.getType(), R.drawable.connection_mode_wireless);
        mConnectionQcIconMap.put(IpcType.W2.getType(), R.drawable.illus_connect_scanning_w2);

        mConnectionQcIconMap.put(IpcType.TC100.getType(), R.drawable.connection_mode_qc);
        mConnectionQcIconMap.put(IpcType.XC100.getType(), R.drawable.connection_mode_qc);
        mConnectionQcIconMap.put(IpcType.TR100.getType(), R.drawable.connection_mode_wireless);
        mConnectionQcIconMap.put(IpcType.TS200.getType(), R.drawable.connection_mode_wireless);
        mConnectionQcIconMap.put(IpcType.TS100.getType(), R.drawable.connection_mode_qc_outdoor);

        mConnectionApIconMap.put(IpcType.PC420.getType(), R.drawable.connection_mode_ap);
        mConnectionApIconMap.put(IpcType.PC440.getType(), R.drawable.connection_mode_ap);
        mConnectionApIconMap.put(IpcType.PC530.getType(), R.drawable.connection_mode_ap);
        mConnectionApIconMap.put(IpcType.PC530PRO.getType(), R.drawable.connection_mode_ap);
        mConnectionApIconMap.put(IpcType.PC540.getType(), R.drawable.connection_mode_ap);
        mConnectionApIconMap.put(IpcType.PC660PRO.getType(), R.drawable.connection_mode_ap);
        mConnectionApIconMap.put(IpcType.PC730.getType(), R.drawable.connection_mode_ap_outdoor);
        mConnectionApIconMap.put(IpcType.PC770.getType(), R.drawable.connection_mode_ap_outdoor);
        mConnectionApIconMap.put(IpcType.MC120.getType(), R.drawable.connection_mode_ap);
        mConnectionApIconMap.put(IpcType.SC100.getType(), R.drawable.connection_mode_ap);
        mConnectionApIconMap.put(IpcType.SC210.getType(), R.drawable.connection_mode_ap);
        mConnectionApIconMap.put(IpcType.SC220.getType(), R.drawable.connection_mode_ap);

        mConnectionApIconMap.put(IpcType.C1.getType(), R.drawable.connection_mode_ap);
        mConnectionApIconMap.put(IpcType.C1PRO.getType(), R.drawable.connection_mode_ap);
        mConnectionApIconMap.put(IpcType.A1.getType(), R.drawable.connection_mode_ap);
        mConnectionApIconMap.put(IpcType.P3.getType(), R.drawable.connection_mode_ap);
        mConnectionApIconMap.put(IpcType.P3Pro.getType(), R.drawable.connection_mode_ap);
        mConnectionApIconMap.put(IpcType.Q1.getType(), R.drawable.connection_mode_ap_outdoor);
        mConnectionApIconMap.put(IpcType.T1.getType(), R.drawable.connection_mode_ap_outdoor);
        mConnectionApIconMap.put(IpcType.T1PRO.getType(), R.drawable.connection_mode_ap_outdoor);
        mConnectionApIconMap.put(IpcType.T2.getType(), R.drawable.connection_mode_ap_outdoor);
        mConnectionApIconMap.put(IpcType.M1.getType(), R.drawable.connection_mode_ap);
        mConnectionApIconMap.put(IpcType.P1.getType(), R.drawable.connection_mode_ap);
        mConnectionApIconMap.put(IpcType.P1PRO.getType(), R.drawable.connection_mode_ap);
        mConnectionApIconMap.put(IpcType.P2.getType(), R.drawable.connection_mode_ap);
        mConnectionApIconMap.put(IpcType.P4.getType(), R.drawable.connection_mode_ap);
        mConnectionApIconMap.put(IpcType.K1.getType(), R.drawable.connection_mode_ap_360);
        mConnectionApIconMap.put(IpcType.K1PRO.getType(), R.drawable.connection_mode_ap_360);
        mConnectionApIconMap.put(IpcType.K2.getType(), R.drawable.connection_mode_ap);
        mConnectionApIconMap.put(IpcType.W2.getType(), R.drawable.illus_connect_network_w2);

        mConnectionApIconMap.put(IpcType.TC100.getType(), R.drawable.connection_mode_ap);
        mConnectionApIconMap.put(IpcType.XC100.getType(), R.drawable.connection_mode_ap);
        mConnectionApIconMap.put(IpcType.TR100.getType(), R.drawable.connection_mode_ap_360);
        mConnectionApIconMap.put(IpcType.TS200.getType(), R.drawable.connection_mode_ap_360);
        mConnectionApIconMap.put(IpcType.TS100.getType(), R.drawable.connection_mode_ap_outdoor);

        mConnectionDvIconMap.put(IpcType.MC120.getType(), R.drawable.connection_mode_dc);
        mConnectionDvIconMap.put(IpcType.HC320.getType(), R.drawable.connection_mode_dc_hc_320);

        mConnectionDvIconMap.put(IpcType.M1.getType(), R.drawable.connection_mode_dc);

        mConnectionLanIconMap.put(IpcType.PC530.getType(), R.drawable.connection_mode_lan);
        mConnectionLanIconMap.put(IpcType.PC530PRO.getType(), R.drawable.connection_mode_lan);
        mConnectionLanIconMap.put(IpcType.PC540.getType(), R.drawable.connection_mode_lan);
        mConnectionLanIconMap.put(IpcType.PC660PRO.getType(), R.drawable.connection_mode_lan);
        mConnectionLanIconMap.put(IpcType.PC730.getType(), R.drawable.connection_mode_lan);
        mConnectionLanIconMap.put(IpcType.PC770.getType(), R.drawable.connection_mode_lan);
        mConnectionLanIconMap.put(IpcType.SC100.getType(), R.drawable.connection_mode_lan);
        mConnectionLanIconMap.put(IpcType.SC210.getType(), R.drawable.connection_mode_lan);
        mConnectionLanIconMap.put(IpcType.SC220.getType(), R.drawable.connection_mode_lan);

        mConnectionLanIconMap.put(IpcType.P3.getType(), R.drawable.connection_mode_lan);
        mConnectionLanIconMap.put(IpcType.P3Pro.getType(), R.drawable.connection_mode_lan);
        mConnectionLanIconMap.put(IpcType.Q1.getType(), R.drawable.connection_mode_lan);
        mConnectionLanIconMap.put(IpcType.T1.getType(), R.drawable.connection_mode_lan);
        mConnectionLanIconMap.put(IpcType.T1PRO.getType(), R.drawable.connection_mode_lan);
        mConnectionLanIconMap.put(IpcType.T2.getType(), R.drawable.connection_mode_lan);
        mConnectionLanIconMap.put(IpcType.P1.getType(), R.drawable.connection_mode_lan);
        mConnectionLanIconMap.put(IpcType.P1PRO.getType(), R.drawable.connection_mode_lan);
        mConnectionLanIconMap.put(IpcType.P2.getType(), R.drawable.connection_mode_lan);
        mConnectionLanIconMap.put(IpcType.P4.getType(), R.drawable.connection_mode_lan);
        mConnectionLanIconMap.put(IpcType.K1.getType(), R.drawable.connection_mode_lan);
        mConnectionLanIconMap.put(IpcType.K1PRO.getType(), R.drawable.connection_mode_lan);
        mConnectionLanIconMap.put(IpcType.K2.getType(), R.drawable.connection_mode_lan);

        mConnectionLanIconMap.put(IpcType.TR100.getType(), R.drawable.connection_mode_lan);
        mConnectionLanIconMap.put(IpcType.TS200.getType(), R.drawable.connection_mode_lan);
        mConnectionLanIconMap.put(IpcType.TS100.getType(), R.drawable.connection_mode_lan);

        mDeviceScanCodeGuideIconMap.put(IpcType.PC530.getType(), R.drawable.scan_code_guide_360);
        mDeviceScanCodeGuideIconMap.put(IpcType.PC530PRO.getType(), R.drawable.scan_code_guide_360);
        mDeviceScanCodeGuideIconMap.put(IpcType.PC540.getType(), R.drawable.scan_code_guide_360);
        mDeviceScanCodeGuideIconMap.put(IpcType.PC660PRO.getType(), R.drawable.scan_code_guide_360);
        mDeviceScanCodeGuideIconMap.put(IpcType.PC730.getType(), R.drawable.scan_code_guide_360);
        mDeviceScanCodeGuideIconMap.put(IpcType.PC770.getType(), R.drawable.scan_code_guide_360);
        mDeviceScanCodeGuideIconMap.put(IpcType.SC100.getType(), R.drawable.scan_code_guide_360);
        mDeviceScanCodeGuideIconMap.put(IpcType.SC210.getType(), R.drawable.scan_code_guide_360);
        mDeviceScanCodeGuideIconMap.put(IpcType.SC220.getType(), R.drawable.scan_code_guide_360);

        mDeviceScanCodeGuideIconMap.put(IpcType.P3.getType(), R.drawable.scan_code_guide_360);
        mDeviceScanCodeGuideIconMap.put(IpcType.P3Pro.getType(), R.drawable.scan_code_guide_360);
        mDeviceScanCodeGuideIconMap.put(IpcType.Q1.getType(), R.drawable.scan_code_guide_360);
        mDeviceScanCodeGuideIconMap.put(IpcType.T1.getType(), R.drawable.scan_code_guide_360);
        mDeviceScanCodeGuideIconMap.put(IpcType.T1PRO.getType(), R.drawable.scan_code_guide_360);
        mDeviceScanCodeGuideIconMap.put(IpcType.T2.getType(), R.drawable.scan_code_guide_360);
        mDeviceScanCodeGuideIconMap.put(IpcType.W1.getType(), R.drawable.scan_code_guide_360);
        mDeviceScanCodeGuideIconMap.put(IpcType.W2.getType(), R.drawable.scan_code_guide_360);
        mDeviceScanCodeGuideIconMap.put(IpcType.P1.getType(), R.drawable.scan_code_guide_360);
        mDeviceScanCodeGuideIconMap.put(IpcType.P1PRO.getType(), R.drawable.scan_code_guide_360);
        mDeviceScanCodeGuideIconMap.put(IpcType.P2.getType(), R.drawable.scan_code_guide_360);
        mDeviceScanCodeGuideIconMap.put(IpcType.P4.getType(), R.drawable.scan_code_guide_360);
        mDeviceScanCodeGuideIconMap.put(IpcType.K1.getType(), R.drawable.scan_code_guide_360);
        mDeviceScanCodeGuideIconMap.put(IpcType.K1PRO.getType(), R.drawable.scan_code_guide_360);
        mDeviceScanCodeGuideIconMap.put(IpcType.K2.getType(), R.drawable.scan_code_guide_360);

        mDeviceScanCodeGuideIconMap.put(IpcType.TR100.getType(), R.drawable.scan_code_guide_360);
        mDeviceScanCodeGuideIconMap.put(IpcType.TS200.getType(), R.drawable.scan_code_guide_360);
        mDeviceScanCodeGuideIconMap.put(IpcType.TS100.getType(), R.drawable.scan_code_guide_360);

        mGatewayAndCameraIconMap.put(IpcType.EC810_CAM.getType(), R.drawable.gateway_camera_icon_2);
        mGatewayAndCameraIconMap.put(IpcType.EC810PRO.getType(), R.drawable.gateway_camera_icon_2);
        mGatewayAndCameraIconMap.put(IpcType.EC810_PLUS.getType(), R.drawable.gateway_camera_icon_ec810_plus);

        mGatewayAndCameraRedIconMap.put(IpcType.W0_CAM.getType(), R.drawable.gateway_camera_icon_1);
        mGatewayAndCameraRedIconMap.put(IpcType.W1.getType(), R.drawable.gateway_camera_icon_1);
        mGatewayAndCameraRedIconMap.put(IpcType.W2.getType(), R.drawable.gateway_camera_red_icon_ec810_plus);

        mProductCategoryStringMap.put(ConstantValue.PRODUCT_TYPE_ROUTER, R.string.add_camera_product_category_router);
        mProductCategoryStringMap.put(ConstantValue.PRODUCT_TYPE_CARD, R.string.add_camera_product_category_card);
        mProductCategoryStringMap.put(ConstantValue.PRODUCT_TYPE_HEAD, R.string.add_camera_product_category_head);
        mProductCategoryStringMap.put(ConstantValue.PRODUCT_TYPE_GUN, R.string.add_camera_product_category_gun);
        mProductCategoryStringMap.put(ConstantValue.PRODUCT_TYPE_LOW_POWER, R.string.add_camera_product_category_lp);
        mProductCategoryStringMap.put(ConstantValue.PRODUCT_TYPE_MINI, R.string.add_camera_product_category_mini);
    }

    public int getDeviceIconByType(String type) {
        type = IpcType.getIpcType(type).getType();
        return getResByType(type, mDeviceIconMap, R.drawable.device_icon);
    }

    public int getDeviceSmallIconByType(String type) {
        // 新添路由器类型,没有设定设备类型,调用时返回 IPC-UNKNOWN
        if ("IPC-UNKNOWN".equals(type)) { // IPC-UNKNOWN  VL-MX1800
            return R.drawable.device_add_icon_lp_device_with_router;
        } else {
            type = IpcType.getIpcType(type).getType();
        }
        return getResByType(type, mDeviceSmallIconMap, R.drawable.device_small_icon);
    }

    public int getDeviceBigIconByType(String type) {
        type = IpcType.getIpcType(type).getType();
        return getResByType(type, mDeviceBigIconMap, R.drawable.nooie_cam_big);
    }

    public int getFlashLightOnIconByType(String type) {
        type = IpcType.getIpcType(type).getType();
        return getResByType(type, mFlashLightOnMap, R.drawable.red_light);
    }

    public int getFlashLightOffIconByType(String type) {
        type = IpcType.getIpcType(type).getType();
        return getResByType(type, mFlashLightOffMap, R.drawable.red_light_no);
    }

    public int getDeviceResetIconByType(String type) {
        type = IpcType.getIpcType(type).getType();
        return getResByType(type, mDeviceResetIconMap, R.drawable.reset_cam720);
    }

    public int getDeviceScanIconByType(String type) {
        type = IpcType.getIpcType(type).getType();
        return getResByType(type, mDeviceScanIconMap, R.drawable.diagram);
    }

    public int getDeviceLightIconByType(String type) {
        type = IpcType.getIpcType(type).getType();
        return getResByType(type, mDeviceLightIconMap, R.drawable.light_cam720);
    }

    public int getConnectionModeQcIconByType(String type) {
        type = IpcType.getIpcType(type).getType();
        return getResByType(type, mConnectionQcIconMap, R.drawable.connection_mode_qc);
    }

    public int getConnectionModeApIconByType(String type) {
        type = IpcType.getIpcType(type).getType();
        return getResByType(type, mConnectionApIconMap, R.drawable.connection_mode_ap);
    }

    public int getConnectionModeDvIconByType(String type) {
        type = IpcType.getIpcType(type).getType();
        return getResByType(type, mConnectionDvIconMap, R.drawable.connection_mode_dc);
    }

    public int getConnectionModeLanIconByType(String type) {
        type = IpcType.getIpcType(type).getType();
        return getResByType(type, mConnectionLanIconMap, R.drawable.connection_mode_lan);
    }

    public int getDeviceScanCodeGuideByType(String type) {
        type = IpcType.getIpcType(type).getType();
        return getResByType(type, mDeviceScanCodeGuideIconMap, R.drawable.scan_code_guide_360);
    }

    public int getGatewayAndCameraIconByType(String type) {
        type = IpcType.getIpcType(type).getType();
        return getResByType(type, mGatewayAndCameraIconMap, R.drawable.gateway_camera_icon_2);
    }

    public int getGatewayAndCameraRedIconByType(String type) {
        type = IpcType.getIpcType(type).getType();
        return getResByType(type, mGatewayAndCameraRedIconMap, R.drawable.gateway_camera_icon_1);
    }

    public int getProductCategoryStringByType(int type) {
        return getResByType(type, mProductCategoryStringMap, R.string.add_camera_product_category_card);
    }

    private <T> int getResByType(T type, Map<T, Integer> resIdMap, int defaultResId) {
        if (type != null && resIdMap != null && resIdMap.containsKey(type)) {
            return resIdMap.get(type);
        } else {
            return defaultResId;
        }
    }
}
