package com.afar.osaio.smart.home.presenter;

import com.afar.osaio.smart.device.helper.SmartDeviceHelper;
import com.afar.osaio.smart.electrician.bean.SwitchBean;
import com.afar.osaio.smart.electrician.manager.DeviceManager;
import com.afar.osaio.smart.electrician.manager.FamilyManager;
import com.afar.osaio.smart.electrician.model.DeviceModel;
import com.afar.osaio.smart.electrician.model.IDeviceModel;
import com.afar.osaio.smart.home.bean.SmartAppliancesDevice;
import com.afar.osaio.smart.home.bean.SmartElectricianDevice;
import com.afar.osaio.smart.home.bean.SmartLightDevice;
import com.afar.osaio.smart.home.bean.SmartTyDevice;
import com.afar.osaio.smart.home.contract.SmartTuyaDeviceContract;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.bean.SDKConstant;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmartTuyaDevicePresenter implements SmartTuyaDeviceContract.Presenter {

    private Map<String, IDeviceModel> mDeviceModels = new HashMap<>();

    private SmartTuyaDeviceContract.View mTaskView;

    public SmartTuyaDevicePresenter(SmartTuyaDeviceContract.View view) {
        mTaskView = view;
    }

    @Override
    public void destroy() {
        if (mTaskView != null) {
            mTaskView = null;
        }
    }

    @Override
    public void loadHomeDetail(long homeId) {
        NooieLog.e("-----------> SmartDeviceListPresenter loadHomeDetail1 homeId="+homeId);
        if (homeId == FamilyManager.DEFAULT_HOME_ID) {
            return;
        }
        TuyaHomeSdk.newHomeInstance(homeId).getHomeDetail(new ITuyaHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean homeBean) {
                if (homeBean == null) {
                    NooieLog.e("-----------> SmartDeviceListPresenter loadHomeDetail2 onSuccess homeId="+homeId);
                    if (mTaskView != null) {
                        mTaskView.onLoadTuyaDevices(SDKConstant.SUCCESS, homeBean);
                    }
                    return;
                }
                NooieLog.e("-----------> SmartDeviceListPresenter loadHomeDetail3 onSuccess homeId="+homeId);
                FamilyManager.getInstance().setCurrentHome(homeBean);

                DeviceManager.getInstance().syncGetDevList(homeBean);
                DeviceManager.getInstance().notifyLoadHomeDetailSuccess(homeBean);
                //DeviceManager.getInstance().setHomeStatusListenerCallBack(this);
                DeviceManager.getInstance().registerHomeStatusListener(homeBean.getHomeId());
//                if (homeView != null) {
//                    homeView.loadHomeDetailSuccess(homeBean);
//                }
                if (mTaskView != null) {
                    mTaskView.onLoadTuyaDevices(SDKConstant.SUCCESS, homeBean);
                }
            }

            @Override
            public void onError(String code, String msg) {
//                if (homeView != null) {
//                    NooieLog.e("------------->loadHomeDetail code " + code + " msg " + msg + "  homeId " + homeId);
//                    homeView.loadHomeDetailFailed(msg);
//                }
                NooieLog.e("--------->getHomeDetail SmartDeviceListPresenter onError msg="+msg);
                if (mTaskView != null) {
                    mTaskView.onLoadTuyaDevices(SDKConstant.SUCCESS, null);
                }
            }
        });
    }

    @Override
    public List<SmartTyDevice> getSmartTyDevices() {
        return SmartDeviceHelper.sortSmartTyDevice(SmartDeviceHelper.convertSmartTyDeviceList(DeviceManager.getInstance().getAllTuyaDevice()));
    }

    @Override
    public List<SmartElectricianDevice> getElectricianDevices() {
        //return DeviceManager.getInstance().getAllTuyaDevice();
        return null;
    }

    @Override
    public List<SmartLightDevice> getLightDevices() {
        //return DeviceManager.getInstance().getAllTuyaDevice();
        return null;
    }

    @Override
    public List<SmartAppliancesDevice> getAppliancesDevices() {
        //return DeviceManager.getInstance().getAllTuyaDevice();
        return null;
    }

    @Override
    public void controlDevice(String deviceId, boolean open) {
        SwitchBean switchBean = new SwitchBean(ConstantValue.DP_ID_SWITCH_ON);
        switchBean.setOpen(open);
        IDeviceModel deviceModel;
        if (mDeviceModels.containsKey(deviceId) && mDeviceModels.get(deviceId) != null) {
            deviceModel = mDeviceModels.get(deviceId);
        } else {
            deviceModel = new DeviceModel(deviceId);
            mDeviceModels.put(deviceId, deviceModel);
        }

        deviceModel.sendCommand(switchBean.getDpId(), switchBean.isOpen(), new IResultCallback() {
            @Override
            public void onError(String code, String msg) {
                NooieLog.e("--------SmartTuyaDevicePresenter controlDevice code " + code + " msg " + msg);
                if (mTaskView != null) {
                    mTaskView.notifyControlDeviceState();
                }
            }

            @Override
            public void onSuccess() {
                NooieLog.e("--------SmartTuyaDevicePresenter controlDevice onSuccess ");
                if (mTaskView != null) {
                    mTaskView.notifyControlDeviceState();
                }
            }
        });
    }

    @Override
    public void controlLamp(String devId, String dpId, boolean open) {
        IDeviceModel deviceModel;
        if (mDeviceModels.containsKey(devId) && mDeviceModels.get(devId) != null) {
            deviceModel = mDeviceModels.get(devId);
        } else {
            deviceModel = new DeviceModel(devId);
            mDeviceModels.put(devId, deviceModel);
        }

        deviceModel.sendCommand(dpId, open, new IResultCallback() {
            @Override
            public void onError(String code, String msg) {
                NooieLog.e("--------SmartTuyaDevicePresenter controlLamp code " + code + " msg " + msg);
                if (mTaskView != null) {
                    mTaskView.notifyControlDeviceState();
                }
            }

            @Override
            public void onSuccess() {
                NooieLog.e("--------SmartTuyaDevicePresenter controlLamp onSuccess ");
                if (mTaskView != null) {
                    mTaskView.notifyControlDeviceState();
                }
            }
        });
    }

    @Override
    public void controlStrip(String devId, Map<String, Object> dpsMap) {
        IDeviceModel deviceModel;
        if (mDeviceModels.containsKey(devId) && mDeviceModels.get(devId) != null) {
            deviceModel = mDeviceModels.get(devId);
        } else {
            deviceModel = new DeviceModel(devId);
            mDeviceModels.put(devId, deviceModel);
        }
        if (dpsMap != null) {
            deviceModel.sendCommands(dpsMap, new IResultCallback() {
                @Override
                public void onError(String code, String error) {
                    NooieLog.e("--------SmartTuyaDevicePresenter controlStrip code " + code + " error " + error);
                    if (mTaskView != null) {
                        mTaskView.notifyControlDeviceState();
                    }
                }

                @Override
                public void onSuccess() {
                    NooieLog.e("--------SmartTuyaDevicePresenter controlStrip onSuccess ");
                    if (mTaskView != null) {
                        mTaskView.notifyControlDeviceState();
                    }
                }
            });
        }
    }

    @Override
    public void loadDeviceBean(String deviceId) {
        DeviceBean deviceBean = TuyaHomeSdk.getDataInstance().getDeviceBean(deviceId);
        if (mTaskView != null) {
            mTaskView.notifyLoadDeviceSuccess(deviceId, deviceBean);
        }
    }
}
