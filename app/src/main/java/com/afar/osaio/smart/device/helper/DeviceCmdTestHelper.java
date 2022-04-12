package com.afar.osaio.smart.device.helper;

import com.afar.osaio.base.NooieApplication;
import com.nooie.common.base.GlobalData;
import com.nooie.common.utils.configure.CountryUtil;
import com.nooie.common.utils.data.DataHelper;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.time.DateTimeUtil;
import com.nooie.sdk.device.DeviceCmdService;
import com.nooie.sdk.device.bean.DevAllSettingsV2;
import com.nooie.sdk.device.bean.DevInfo;
import com.nooie.sdk.device.bean.FormatInfo;
import com.nooie.sdk.device.bean.ICRMode;
import com.nooie.sdk.device.bean.RecordFragment;
import com.nooie.sdk.device.listener.OnDeviceFormatInfoListener;
import com.nooie.sdk.device.listener.OnICRModeListener;
import com.nooie.sdk.device.listener.OnSwitchStateListener;
import com.nooie.sdk.listener.OnActionResultListener;
import com.nooie.sdk.listener.OnGetAllSettingsV2Listener;
import com.nooie.sdk.listener.OnGetDevInfoListener;
import com.nooie.sdk.listener.OnGetRecDatesListener;
import com.nooie.sdk.listener.OnGetSdcardRecordListener;
import com.nooie.sdk.listener.OnGetTimeListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class DeviceCmdTestHelper {

    private String mDeviceId;
    private String mPDeviceId;
    private String mAccount;
    private String mUid;

    private DeviceCmdTestHelper() {
    }

    private static class DeviceCmdTestHelperHolder {
        private static final DeviceCmdTestHelper INSTANCE = new DeviceCmdTestHelper();
    }

    public static DeviceCmdTestHelper getInstance() {
        return DeviceCmdTestHelperHolder.INSTANCE;
    }

    public void init(boolean isTestCmd, String deviceId, String pDeviceId, String account, String uid) {
        if (isTestCmd) {
            mDeviceId = deviceId;
            mPDeviceId = pDeviceId;
            mAccount = account;
            mUid = uid;
            startTest(0);
        }
    }

    public void startTest(int testType) {
        Observable.just(1)
                .delay(5 * 1000, TimeUnit.MILLISECONDS)
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Integer integer) {
                        if (testType == 0) {
                            testApGetCmd(mDeviceId, mPDeviceId, mAccount, mUid);
                            testApSetCmd(mDeviceId, mPDeviceId, mAccount, mUid, true);
                        } else if (testType == 1) {
                        }
                    }
                });
    }

    public void testApGetCmd(String deviceId, String pDeviceId, String account, String uid) {
        NooieLog.d("-->> DeviceCmdTestHelper testApGetCmd deviceId=" + deviceId);
        List<Integer> cmdIds = new ArrayList<>();
        for (int i = 0; i < 13; i++) {
            cmdIds.add(i);
        }
        int todayTimeStamp = (int)(DateTimeUtil.getUtcTodayStartTimeStamp() / 1000L);
        Observable.from(cmdIds)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Integer value) {
                        //NooieLog.d("-->> DeviceCmdTestHelper testApGetCmd onNext value=" + value);
                        switch (value) {
                            case 0 : {
                                DeviceCmdService.getInstance(NooieApplication.mCtx).getSDcardRecordList(mDeviceId, todayTimeStamp, new OnGetSdcardRecordListener() {
                                    @Override
                                    public void onGetSdcardRecordInfo(int code, RecordFragment[] records) {
                                        NooieLog.d("-->> DeviceCmdTestHelper testApGetCmd onGetSdcardRecordInfo code=" + code);
                                    }
                                });
                                break;
                            }
                            case 1 : {
                                DeviceCmdService.getInstance(NooieApplication.mCtx).getCamAllSettingsV2(deviceId, new OnGetAllSettingsV2Listener() {
                                    @Override
                                    public void onGetAllSettingsV2(int code, DevAllSettingsV2 settings) {
                                        NooieLog.d("-->> DeviceCmdTestHelper testApGetCmd onGetAllSettingsV2 code=" + code);
                                    }
                                });
                                break;
                            }
                            case 2 : {
                                DeviceCmdService.getInstance(NooieApplication.mCtx).getCamRecordWithAudio(deviceId, new OnSwitchStateListener() {
                                    @Override
                                    public void onStateInfo(int code, boolean on) {
                                        NooieLog.d("-->> DeviceCmdTestHelper testApGetCmd getCamRecordWithAudio onStateInfo code=" + code);
                                    }
                                });
                                break;
                            }
                            case 3 : {
                                DeviceCmdService.getInstance(NooieApplication.mCtx).getLED(deviceId, new OnSwitchStateListener() {
                                    @Override
                                    public void onStateInfo(int code, boolean on) {
                                        NooieLog.d("-->> DeviceCmdTestHelper testApGetCmd getLED onStateInfo code=" + code);
                                    }
                                });
                                break;
                            }
                            case 4 : {
                                DeviceCmdService.getInstance(NooieApplication.mCtx).getRotateImg(deviceId, new OnSwitchStateListener() {
                                    @Override
                                    public void onStateInfo(int code, boolean on) {
                                        NooieLog.d("-->> DeviceCmdTestHelper testApGetCmd getRotateImg onStateInfo code=" + code);
                                    }
                                });
                                break;
                            }
                            case 5 : {
                                DeviceCmdService.getInstance(NooieApplication.mCtx).getLoopRecord(deviceId, new OnSwitchStateListener() {
                                    @Override
                                    public void onStateInfo(int code, boolean on) {
                                        NooieLog.d("-->> DeviceCmdTestHelper testApGetCmd getLoopRecord onStateInfo code=" + code);
                                    }
                                });
                                break;
                            }
                            case 6 : {
                                DeviceCmdService.getInstance(NooieApplication.mCtx).getSleep(deviceId, new OnSwitchStateListener() {
                                    @Override
                                    public void onStateInfo(int code, boolean on) {
                                        NooieLog.d("-->> DeviceCmdTestHelper testApGetCmd getSleep onStateInfo code=" + code);
                                    }
                                });
                                break;
                            }
                            case 7 : {
                                DeviceCmdService.getInstance(NooieApplication.mCtx).getIcr(deviceId, new OnICRModeListener() {
                                    @Override
                                    public void onIcr(int code, ICRMode mode) {
                                        NooieLog.d("-->> DeviceCmdTestHelper testApGetCmd getIcr onIcr code=" + code);
                                    }
                                });
                                break;
                            }
                            case 8 : {
                                DeviceCmdService.getInstance(NooieApplication.mCtx).getTime(deviceId, new OnGetTimeListener() {
                                    @Override
                                    public void onGetTime(int result, int mode, float timeZone, int timeOffset) {
                                        NooieLog.d("-->> DeviceCmdTestHelper testApGetCmd getTime onGetTime code=" + result);
                                    }
                                });
                                break;
                            }
                            case 9: {
                                break;
                            }
                            case 10 : {
                                DeviceCmdService.getInstance(NooieApplication.mCtx).getDevInfo(deviceId, new OnGetDevInfoListener() {
                                    @Override
                                    public void onDevInfo(int code, DevInfo info) {
                                        NooieLog.d("-->> DeviceCmdTestHelper testApGetCmd getDevInfo onDevInfo code=" + code);
                                    }
                                });
                                break;
                            }
                            case 11 : {
                                DeviceCmdService.getInstance(NooieApplication.mCtx).getFormatInfo(deviceId, new OnDeviceFormatInfoListener() {
                                    @Override
                                    public void onDeviceFormatInfo(int code, FormatInfo info) {
                                        NooieLog.d("-->> DeviceCmdTestHelper testApGetCmd getFormatInfo onDeviceFormatInfo code=" + code);
                                    }
                                });
                                break;
                            }
                            case 12 : {
                                DeviceCmdService.getInstance(NooieApplication.mCtx).getSDcardRecDay(deviceId, new OnGetRecDatesListener() {
                                    @Override
                                    public void onRecDates(int code, int[] ints, int i1) {
                                        NooieLog.d("-->> DeviceCmdTestHelper testApGetCmd getSDcardRecDay onResult code=" + code);
                                    }
                                });
                                break;
                            }
                        }
                    }
                });
    }

    public void testApSetCmd(String deviceId, String pDeviceId, String account, String uid, boolean isTest) {
        if (!isTest) {
            return;
        }
        NooieLog.d("-->> DeviceCmdTestHelper testApGetCmd deviceId=" + deviceId);
        List<Integer> cmdIds = new ArrayList<>();
        for (int i = 0; i < 13; i++) {
            cmdIds.add(i);
        }
        int todayTimeStamp = (int) (DateTimeUtil.getUtcTodayStartTimeStamp() / 1000L);
        boolean state = false;
        String tmpDeviceId = "0f9f42aadcd737b355d35917fc961144";

        Observable.from(cmdIds)
                .delay(10 * 1000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Integer value) {
                        //NooieLog.d("-->> DeviceCmdTestHelper testApGetCmd onNext value=" + value);
                        switch (value) {
                            case 0 : {
                                break;
                            }
                            case 1 : {
                                DeviceCmdService.getInstance(NooieApplication.mCtx).factoryReset(tmpDeviceId, new OnActionResultListener() {
                                    @Override
                                    public void onResult(int code) {
                                        NooieLog.d("-->> DeviceCmdTestHelper testApSetCmd factoryReset onResult code=" + code);
                                    }
                                });
                                break;
                            }
                            case 2 : {
                                DeviceCmdService.getInstance(NooieApplication.mCtx).setCamRecordWithAudio(deviceId, state, new OnActionResultListener() {
                                    @Override
                                    public void onResult(int code) {
                                        NooieLog.d("-->> DeviceCmdTestHelper testApSetCmd setCamRecordWithAudio onStateInfo code=" + code);
                                    }
                                });
                                break;
                            }
                            case 3 : {
                                DeviceCmdService.getInstance(NooieApplication.mCtx).setLED(deviceId, state, new OnActionResultListener() {
                                    @Override
                                    public void onResult(int code) {
                                        NooieLog.d("-->> DeviceCmdTestHelper setLED onStateInfo code=" + code);
                                    }
                                });
                                break;
                            }
                            case 4 : {
                                DeviceCmdService.getInstance(NooieApplication.mCtx).setRotateImg(deviceId, state, new OnActionResultListener() {
                                    @Override
                                    public void onResult(int code) {
                                        NooieLog.d("-->> DeviceCmdTestHelper setRotateImg onStateInfo code=" + code);
                                    }
                                });
                                break;
                            }
                            case 5 : {
                                DeviceCmdService.getInstance(NooieApplication.mCtx).setLoopRecord(deviceId, state, new OnActionResultListener() {
                                    @Override
                                    public void onResult(int code) {
                                        NooieLog.d("-->> DeviceCmdTestHelper setLoopRecord onStateInfo code=" + code);
                                    }
                                });
                                break;
                            }
                            case 6 : {
                                DeviceCmdService.getInstance(NooieApplication.mCtx).setSleep(deviceId, false, new OnActionResultListener() {
                                    @Override
                                    public void onResult(int code) {
                                        NooieLog.d("-->> DeviceCmdTestHelper setSleep onStateInfo code=" + code);
                                    }
                                });
                                break;
                            }
                            case 7 : {
                                DeviceCmdService.getInstance(NooieApplication.mCtx).setIcr(deviceId, ICRMode.ICR_MODE_AUTO, new OnActionResultListener() {
                                    @Override
                                    public void onResult(int code) {
                                        NooieLog.d("-->> DeviceCmdTestHelper setIcr onIcr code=" + code);
                                    }
                                });
                                break;
                            }
                            case 8 : {
                                try {
                                    float timeZone = DataHelper.toFloat(CountryUtil.getCurrentTimezone());
                                    long currentTime = System.currentTimeMillis();
                                    SimpleDateFormat formatStr = new SimpleDateFormat(DateTimeUtil.PATTERN_YMD_HMS_1);
                                    long networkTime = (currentTime + GlobalData.getInstance().getGapTime() * 1000L);
                                    String localNetworkTimeStr = DateTimeUtil.localToUtc(networkTime, DateTimeUtil.PATTERN_YMD_HMS_1);
                                    Date date1 = formatStr.parse(localNetworkTimeStr);
                                    int timeOffset = (int)((currentTime - date1.getTime()) / 1000L);
                                    DeviceCmdService.getInstance(NooieApplication.mCtx).setTime(deviceId, 1, timeZone, timeOffset, new OnActionResultListener() {
                                        @Override
                                        public void onResult(int code) {
                                            NooieLog.d("-->> DeviceCmdTestHelper setTime onGetTime code=" + code);
                                        }
                                    });
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                            }
                            case 9: {
                                DeviceCmdService.getInstance(NooieApplication.mCtx).formatSdCard(tmpDeviceId, new OnActionResultListener() {
                                    @Override
                                    public void onResult(int code) {
                                        NooieLog.d("-->> DeviceCmdTestHelper formatSdCard onResult code=" + code);
                                    }
                                });
                                break;
                            }
                            case 10 : {
                                String deviceId = "0f9f42aadcd737b355d35917fc961144";
                                String  model = "PC530";
                                String  newVersion = "";
                                String  packageKey = "";
                                String  md5 = "5d69d561e84d3ce10900edab655892ad";
                                DeviceCmdService.getInstance(NooieApplication.mCtx).upgrade(deviceId, model, newVersion, packageKey, new OnActionResultListener() {
                                    @Override
                                    public void onResult(int code) {
                                        NooieLog.d("-->> DeviceCmdTestHelper upgrade onResult code=" + code);
                                    }
                                });
                                break;
                            }
                        }
                    }
                });
    }
}

