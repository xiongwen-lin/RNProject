package com.afar.osaio.smart.setting.presenter;

import android.graphics.RectF;

import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.setting.contract.DetectionZoneContract;
import com.afar.osaio.util.ConstantValue;
import com.nooie.sdk.base.Constant;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.device.bean.AreaRect;
import com.nooie.sdk.device.bean.MTAreaInfo;
import com.nooie.sdk.listener.OnActionResultListener;
import com.nooie.sdk.listener.OnGetMTAreaListener;
import com.nooie.sdk.processor.cmd.DeviceCmdApi;

public class DetectionZonePresenter implements DetectionZoneContract.Presenter {

    private DetectionZoneContract.View mTaskView;

    public DetectionZonePresenter(DetectionZoneContract.View view) {
        mTaskView = view;
        mTaskView.setPresenter(this);
    }

    @Override
    public void destroy() {
        if (mTaskView != null) {
            mTaskView.setPresenter(null);
            mTaskView = null;
        }
    }

    @Override
    public void updateMtAreaInfo(String deviceId, int type, RectF selectZoneRectF) {
        DeviceCmdApi.getInstance().getMTAreaInfo(deviceId, new OnGetMTAreaListener() {
            @Override
            public void onGetAreaInfoResult(int result, MTAreaInfo info) {
                if (result == SDKConstant.CODE_CACHE) {
                    return;
                }
                if (result == Constant.OK && info != null) {
                    AreaRect areaRect = NooieDeviceHelper.convertAreaRect(info.horMaxSteps, info.verMaxSteps, selectZoneRectF);
                    AreaRect[] areaRects = createAreaRect(areaRect);
                    if (!isAreaRectsSame(info.areaRects, areaRects)) {
                        info.areaRects = areaRects;
                        DeviceCmdApi.getInstance().setMTAreaInfo(deviceId, info, new OnActionResultListener() {
                            @Override
                            public void onResult(int code) {
                                if (mTaskView != null) {
                                    mTaskView.onUpdateMtAreaInfo((code == Constant.OK ? ConstantValue.SUCCESS : ConstantValue.ERROR), type, info);
                                }
                            }
                        });
                    } else {
                        if (mTaskView != null) {
                            mTaskView.onUpdateMtAreaInfo(ConstantValue.SUCCESS, type, info);
                        }
                    }
                } else {
                    if (mTaskView != null) {
                        mTaskView.onUpdateMtAreaInfo(ConstantValue.ERROR, type, null);
                    }
                }
            }
        });
    }

    private AreaRect[] createAreaRect(AreaRect areaRect) {
        AreaRect[] areaRects = new AreaRect[5];
        for (int i = 0; i < areaRects.length; i++) {
            areaRects[i] = new AreaRect();
        }
        if (areaRect != null) {
            areaRects[0].ltX = areaRect.ltX;
            areaRects[0].ltY = areaRect.ltY;
            areaRects[0].rbX = areaRect.rbX;
            areaRects[0].rbY = areaRect.rbY;
        }
        return areaRects;
    }

    @Override
    public void updateMtAreaInfo(String deviceId, int type, boolean state) {
        DeviceCmdApi.getInstance().getMTAreaInfo(deviceId, new OnGetMTAreaListener() {
            @Override
            public void onGetAreaInfoResult(int result, MTAreaInfo info) {
                if (result == SDKConstant.CODE_CACHE) {
                    return;
                }
                if (result == Constant.OK && info != null) {
                    if (info.state != state) {
                        boolean originalState = info.state;
                        info.state = state;
                        info.areaRects = convertDefaultAreaRects(info);
                        DeviceCmdApi.getInstance().setMTAreaInfo(deviceId, info, new OnActionResultListener() {
                            @Override
                            public void onResult(int code) {
                                if (mTaskView != null) {
                                    if (info != null && code != Constant.OK) {
                                        info.state = originalState;
                                    }
                                    mTaskView.onUpdateMtAreaInfo((code == Constant.OK ? ConstantValue.SUCCESS : ConstantValue.ERROR), type, info);
                                }
                            }
                        });
                    } else {
                        if (mTaskView != null) {
                            mTaskView.onUpdateMtAreaInfo(ConstantValue.SUCCESS, type, info);
                        }
                    }
                } else {
                    if (mTaskView != null) {
                        mTaskView.onUpdateMtAreaInfo(ConstantValue.ERROR, type, null);
                    }
                }
            }
        });
    }

    @Override
    public void setMtAreaInfo(String deviceId, MTAreaInfo mtAreaInfo) {
        DeviceCmdApi.getInstance().getMTAreaInfo(deviceId, new OnGetMTAreaListener() {
            @Override
            public void onGetAreaInfoResult(int result, MTAreaInfo info) {
                if (result == SDKConstant.CODE_CACHE) {
                    return;
                }
                if (result == Constant.OK) {
                    if (!isMTAreaInfoSame(info, mtAreaInfo)) {
                        DeviceCmdApi.getInstance().setMTAreaInfo(deviceId, mtAreaInfo, new OnActionResultListener() {
                            @Override
                            public void onResult(int code) {
                                if (mTaskView != null) {
                                    mTaskView.onSetMtAreaInfo(code == Constant.OK ? ConstantValue.SUCCESS : ConstantValue.ERROR);
                                }
                            }
                        });
                    } else {
                        if (mTaskView != null) {
                            mTaskView.onSetMtAreaInfo(ConstantValue.SUCCESS);
                        }
                    }
                } else {
                    if (mTaskView != null) {
                        mTaskView.onSetMtAreaInfo(ConstantValue.ERROR);
                    }
                }
            }
        });
    }

    private boolean isMTAreaInfoSame(MTAreaInfo mtAreaInfo, MTAreaInfo newInfo) {
        if (mtAreaInfo == null) {
            return false;
        }

        if (newInfo == null) {
            return true;
        }

        if (mtAreaInfo.state != newInfo.state || mtAreaInfo.verMaxSteps != newInfo.verMaxSteps || mtAreaInfo.horMaxSteps != newInfo.horMaxSteps || mtAreaInfo.level != newInfo.level || !isAreaRectsSame(mtAreaInfo.areaRects, newInfo.areaRects)) {
            return false;
        }
        return true;
    }

    private boolean isAreaRectsSame(AreaRect[] areaRects, AreaRect[] newAreaRects) {
        if (areaRects == null || newAreaRects == null) {
            return false;
        }

        int len = Math.min(areaRects.length, newAreaRects.length);
        if (len < 1) {
            return false;
        }
        boolean isSame = true;
        //当前只比较第一个
        len = 1;
        for (int i = 0; i < len; i++) {
            AreaRect areaRect = areaRects[i];
            AreaRect newAreaRect = newAreaRects[i];
            if (areaRect == null || newAreaRect == null || areaRect.ltX != newAreaRect.ltX || areaRect.ltY != newAreaRect.ltY || areaRect.rbX != newAreaRect.rbX || areaRect.rbY != newAreaRect.rbY) {
                isSame = false;
                break;
            }
        }
        return isSame;
    }

    @Override
    public void getMtAreaInfo(String deviceId) {
        DeviceCmdApi.getInstance().getMTAreaInfo(deviceId, new OnGetMTAreaListener() {
            @Override
            public void onGetAreaInfoResult(int result, MTAreaInfo info) {
                if (result == SDKConstant.CODE_CACHE) {
                    return;
                }
                if (mTaskView != null) {
                    mTaskView.onGetMtAreaInfo((result == Constant.OK ? ConstantValue.SUCCESS : ConstantValue.ERROR), info);
                }
            }
        });
    }

    private AreaRect[] convertDefaultAreaRects(MTAreaInfo info) {
        if (info == null) {
            return null;
        }
        if (!info.state || NooieDeviceHelper.isMtAreaInfoInvalid(info)) {
            return info.areaRects;
        }

        if (NooieDeviceHelper.isAreaRectInValid(info.areaRects[0])) {
            if (info.areaRects[0] == null) {
                info.areaRects[0] = new AreaRect();
            }
            info.areaRects[0].ltX = 0;
            info.areaRects[0].ltY = 0;
            info.areaRects[0].rbX = info.horMaxSteps - 1;
            info.areaRects[0].rbY = info.verMaxSteps - 1;
        }
        return info.areaRects;
    }
}
