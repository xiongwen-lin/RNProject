package com.afar.osaio.smart.bridge;

import com.afar.osaio.smart.bridge.helper.YRBridgeDeviceHelper;
import com.afar.osaio.smart.bridge.helper.YRUserInfoPresenter;
import com.apemans.platformbridge.bridge.contract.IBridgeDeviceHelper;
import com.apemans.platformbridge.bridge.YROriginBridgeManager;
import com.apemans.platformbridge.bridge.contract.YRIUserInfoHelper;

/***********************************************************
 * 作者: zhengruidong@apemans.com
 * 日期: 2022/2/17 10:41 上午
 * 说明:
 *
 * 备注:
 *
 ***********************************************************/
public class BridgeManager {

    private static class SingleInstance {
        private final static BridgeManager INSTANCE = new BridgeManager();
    }

    public static BridgeManager getInstance() {
        return SingleInstance.INSTANCE;
    }

    public void init() {
        registerBridgeDeviceManager(new YRBridgeDeviceHelper());
    }

    public void userInit() {
        registerBridgeUserManager(new YRUserInfoPresenter());
    }

    private void registerBridgeDeviceManager(IBridgeDeviceHelper manager) {
        YROriginBridgeManager.INSTANCE.setBridgeDeviceManager(manager);
    }

    private void registerBridgeUserManager(YRIUserInfoHelper manager) {
        YROriginBridgeManager.INSTANCE.setYrUserBridgeManager(manager);
    }
}
