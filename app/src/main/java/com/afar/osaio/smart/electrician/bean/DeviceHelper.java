package com.afar.osaio.smart.electrician.bean;

import android.text.TextUtils;

import com.afar.osaio.smart.electrician.util.Base64Util;
import com.afar.osaio.smart.electrician.util.HexUtil;
import com.afar.osaio.util.ConstantValue;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.GroupBean;
import com.tuya.smart.sdk.bean.GroupDeviceBean;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * DeviceHelper
 *
 * @author Administrator
 * @date 2019/3/8
 */
public class DeviceHelper {

    public static final String TIME_SPLIT = ":";

    public static final String SWITCH = "switch";
    public static final String COUNTDOWN = "countdown";
    //public static final String RAND_TIME = "randomTime";
    //public static final String CYCLE_TIME = "cycleTime";
    //public static final String NORMAL_TIME = "normalTime";
    public static final String RAND_TIME = "random_time";
    public static final String RAND_TIME_2 = "randomTime"; // randomTime
    public static final String CYCLE_TIME = "cycle_time"; // cycleTime
    public static final String CYCLE_TIME_2 = "cycleTime";
    public static final String NORMAL_TIME = "normal_time"; // normalTime
    public static final String NORMAL_TIME_2 = "normalTime";
    public static final String RELAY_STATUS = "relay_status";
    public static final String CHILD_LOCK = "child_lock";
    public static final String ADD_ELE = "add_ele";//增加电量
    public static final String CUR_CURRENT = "cur_current";//当前电流
    public static final String CUR_POWER = "cur_power";//当前功率
    public static final String CUR_VOLTAGE = "cur_voltage";//当前电压
    public static final String TEST_BIT = "test_bit";
    public static final String VOLTAGE_COE = "voltage_coe";
    public static final String ELECTRIC_COE = "electric_coe";
    public static final String POWER_COE = "power_coe";
    public static final String ELECTRICITY_COE = "electricity_coe";
    public static final String FAULT = "fault";

    private String switch_id = "1";
    private String countDown_id = "9";
    private String randomTime_id = "101";
    private String cycleTime_id = "102";
    private String normalTime_id = "103";
    private String relayStatus_id = "39";
    private String childLock_id = "41";
    private String addEle_id = "17";
    private String curCurrent_id = "18";
    private String curPower_id = "19";
    private String curVoltage_id = "20";
    private String testBit_id = "21";
    private String voltageCoe_id = "22";
    private String electricCoe_id = "23";
    private String powerCoe_id = "24";
    private String electricityCoe_id = "25";
    private String fault_id = "26";

    private volatile static DeviceHelper instance;

    private DeviceHelper() {
    }

    public static DeviceHelper getInstance() {
        if (instance == null) {
            synchronized (DeviceHelper.class) {
                if (instance == null) {
                    instance = new DeviceHelper();
                }
            }
        }
        return instance;
    }

    public static Map<String, Object> convertDps(String dps) {
        if (!TextUtils.isEmpty(dps)) {
            Gson gson = new Gson();
            Map<String, Object> dpsMap = gson.fromJson(dps, new TypeToken<Map<String, Object>>() {
            }.getType());
            return dpsMap;
        }

        return null;
    }

    public static boolean convertBoolDp(String dpId, String dps) {
        return convertDp(dpId, dps) != null ? (boolean) convertDp(dpId, dps) : false;
    }

    public int convertIntDp(String dpId, String dps) {
        return convertDp(dpId, dps) != null ? (int) convertDp(dpId, dps) : 0;
    }

    public static Object convertDp(String dpId, String dps) {
        Map<String, Object> dpsMap = convertDps(dps);
        if (dpsMap != null) {
            if (dpsMap.containsKey(dpId) && dpsMap.get(dpId) != null) {
                return dpsMap.get(dpId);
            }
        }
        return null;
    }

    public static List<DeviceBean> covertDeviceBean(List<GroupDeviceBean> groupDevices) {
        List<DeviceBean> devices = new ArrayList<>();
        for (GroupDeviceBean groupDeviceBean : CollectionUtil.safeFor(groupDevices)) {
            if (groupDeviceBean.getDeviceBean() != null) {
                devices.add(groupDeviceBean.getDeviceBean());
            }
        }
        return devices;
    }

    public static List<MixDeviceBean> convertMixDeviceBean(HomeBean homeBean) {
        List<MixDeviceBean> devices = new ArrayList<>();
        if (homeBean == null) {
            return devices;
        }

        for (GroupBean groupBean : CollectionUtil.safeFor(homeBean.getGroupList())) {
            MixDeviceBean mixDeviceBean = new MixDeviceBean();
            mixDeviceBean.setGroupBean(groupBean);
            devices.add(mixDeviceBean);
        }

        for (GroupBean groupBean : CollectionUtil.safeFor(homeBean.getSharedGroupList())) {
            MixDeviceBean mixDeviceBean = new MixDeviceBean();
            mixDeviceBean.setGroupBean(groupBean);
            devices.add(mixDeviceBean);
        }

        for (DeviceBean deviceBean : CollectionUtil.safeFor(homeBean.getDeviceList())) {
            MixDeviceBean mixDeviceBean = new MixDeviceBean();
            mixDeviceBean.setDeviceBean(deviceBean);
            devices.add(mixDeviceBean);
        }
        for (DeviceBean deviceBean : CollectionUtil.safeFor(homeBean.getSharedDeviceList())) {
            MixDeviceBean mixDeviceBean = new MixDeviceBean();
            mixDeviceBean.setDeviceBean(deviceBean);
            devices.add(mixDeviceBean);
        }
        return devices;
    }

    public static List<MixDeviceBean> convertMixDeviceBean(List<DeviceBean> deviceList, List<GroupBean> groupList) {
        List<MixDeviceBean> devices = new ArrayList<>();
        for (GroupBean groupBean : CollectionUtil.safeFor(groupList)) {
            MixDeviceBean mixDeviceBean = new MixDeviceBean();
            mixDeviceBean.setGroupBean(groupBean);
            devices.add(mixDeviceBean);
        }

        for (DeviceBean deviceBean : CollectionUtil.safeFor(deviceList)) {
            MixDeviceBean mixDeviceBean = new MixDeviceBean();
            mixDeviceBean.setDeviceBean(deviceBean);
            devices.add(mixDeviceBean);
        }
        return devices;
    }

    public static List<MixDeviceBean> convertDeviceBean(HomeBean homeBean) {
        List<MixDeviceBean> devices = new ArrayList<>();
        if (homeBean == null) {
            return devices;
        }
        for (DeviceBean deviceBean : CollectionUtil.safeFor(homeBean.getDeviceList())) {
            MixDeviceBean mixDeviceBean = new MixDeviceBean();
            mixDeviceBean.setDeviceBean(deviceBean);
            devices.add(mixDeviceBean);
        }
        for (DeviceBean deviceBean : CollectionUtil.safeFor(homeBean.getSharedDeviceList())) {
            MixDeviceBean mixDeviceBean = new MixDeviceBean();
            mixDeviceBean.setDeviceBean(deviceBean);
            devices.add(mixDeviceBean);
        }
        return devices;
    }

    public static List<MixDeviceBean> convertGroupDeviceBean(HomeBean homeBean) {
        List<MixDeviceBean> devices = new ArrayList<>();
        if (homeBean == null) {
            return devices;
        }

        for (GroupBean groupBean : CollectionUtil.safeFor(homeBean.getGroupList())) {
            MixDeviceBean mixDeviceBean = new MixDeviceBean();
            mixDeviceBean.setGroupBean(groupBean);
            devices.add(mixDeviceBean);
        }

        for (GroupBean groupBean : CollectionUtil.safeFor(homeBean.getSharedGroupList())) {
            MixDeviceBean mixDeviceBean = new MixDeviceBean();
            mixDeviceBean.setGroupBean(groupBean);
            devices.add(mixDeviceBean);
        }

        Collections.sort(devices, new Comparator<MixDeviceBean>() {
            @Override
            public int compare(MixDeviceBean o1, MixDeviceBean o2) {
                return (int) (o1.getGroupBean().getTime() - o2.getGroupBean().getTime());
            }
        });
        return devices;
    }

    /**
     * 根据deviceBean获取 到对应的DP值
     */
    public void getDPs(DeviceBean deviceBean) {
        String test = deviceBean.getProductBean().getSchemaInfo().getSchema();
        NooieLog.e("-------------> test " + test);
        Gson gson = new Gson();
        List<Schema> schemas = gson.fromJson(test, new TypeToken<List<Schema>>() {
        }.getType());
        for (Schema schema : schemas) {
            if (schema.getCode().contains(SWITCH)) {
                setSwitch_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(COUNTDOWN)) {
                setCountDown_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(RAND_TIME) || schema.getCode().contains(RAND_TIME_2)) {
                setRandomTime_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(CYCLE_TIME) || schema.getCode().contains(CYCLE_TIME_2)) {
                setCycleTime_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(NORMAL_TIME) || schema.getCode().contains(NORMAL_TIME_2)) {
                setNormalTime_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(RELAY_STATUS)) {
                setRelayStatus_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(CHILD_LOCK)) {
                setChildLock_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(ADD_ELE)) {
                setAddEle_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(CUR_CURRENT)) {
                setCurCurrent_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(CUR_POWER)) {
                setCurPower_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(CUR_VOLTAGE)) {
                setCurVoltage_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(TEST_BIT)) {
                setTestBit_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(VOLTAGE_COE)) {
                setVoltageCoe_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(ELECTRIC_COE)) {
                setElectricCoe_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(POWER_COE)) {
                setPowerCoe_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(ELECTRICITY_COE)) {
                setElectricityCoe_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(FAULT)) {
                setFault_id(String.valueOf(schema.getId()));
            }
        }
    }

    public static DeviceInfoBean convertDeviceInfoBean(String object) {
        Gson gson = new Gson();
        DeviceInfoBean resultBean = gson.fromJson(object, DeviceInfoBean.class);
        return resultBean;
    }

    public static PowerBean convertPowerBean(String object) {
        NooieLog.e("-----------result  " + object);
        Gson gson = new Gson();
        PowerBean powerBean = gson.fromJson(object, new TypeToken<PowerBean>() {
        }.getType());
        NooieLog.e("sum  " + powerBean.getSum() + "  thisDay  " + powerBean.getThisDay() + " years  " + powerBean.getYears());
        Map<String, Map<String, String>> years = powerBean.getYears();
        for (Map.Entry<String, Map<String, String>> year : years.entrySet()) {
            NooieLog.e("key:  " + year.getKey() + "  value  " + year.getValue());
            for (Map.Entry<String, String> month : years.get(year.getKey()).entrySet()) {
                NooieLog.e("key month:  " + month.getKey() + "  value  " + month.getValue());
            }
        }
        return powerBean;
    }

    public static PowerDayBean convertPowerDayBean(String object) {
        Gson gson = new Gson();
        PowerDayBean powerDayBean = gson.fromJson(object, new TypeToken<PowerDayBean>() {
        }.getType());
        NooieLog.e("days  " + powerDayBean.getDays() + "  total  " + powerDayBean.getTotal() + " values  " + powerDayBean.getValues());
        if (powerDayBean.getDays() != null && !powerDayBean.getDays().isEmpty()) {
            for (int i = 0; i < powerDayBean.getDays().size(); i++) {
                NooieLog.e("-----days  " + powerDayBean.getDays().get(i));
            }
        }
        if (powerDayBean.getValues() != null && !powerDayBean.getValues().isEmpty()) {
            for (int i = 0; i < powerDayBean.getValues().size(); i++) {
                NooieLog.e("-----values  " + powerDayBean.getValues().get(i));
            }
        }

        return powerDayBean;
    }

    public boolean isHaveNormalTime(Map<String, Object> dpsMap) {
        if (dpsMap == null)
            return false;
        return (dpsMap.containsKey(getRandomTime_id()) && base64Decode(dpsMap.get(getRandomTime_id()).toString()).length() >= 20);
    }

    public boolean isHaveCountDown(Map<String, Object> dpsMap) {
        if (dpsMap == null)
            return false;
        return dpsMap.containsKey(getCountDown_id());
    }

    public boolean isHaveCycleTime(Map<String, Object> dpsMap) {
        if (dpsMap == null)
            return false;
        return (dpsMap.containsKey(getCycleTime_id()) && base64Decode(dpsMap.get(getCycleTime_id()).toString()).length() >= 20);
    }

    public boolean isHaveRandomTime(Map<String, Object> dpsMap) {
        if (dpsMap == null)
            return false;
        return (dpsMap.containsKey(getRandomTime_id()) && base64Decode(dpsMap.get(getRandomTime_id()).toString()).length() >= 12);
    }

    public boolean isHaveChildLock(Map<String, Object> dpsMap) {
        if (dpsMap == null)
            return false;
        return (dpsMap.containsKey(getChildLock_id()));
    }

    public boolean getChildLockValue(Map<String, Object> dpsMap) {
        if (isHaveChildLock(dpsMap)) {
            return (boolean) dpsMap.get(getChildLock_id());
        }
        return false;
    }

    public boolean isHaveRelayStatus(Map<String, Object> dpsMap) {
        if (dpsMap == null)
            return false;
        return (dpsMap.containsKey(getRelayStatus_id()));
    }

    public String getRelayStatusValue(Map<String, Object> dpsMap) {
        if (isHaveRelayStatus(dpsMap)) {
            return (String) dpsMap.get(getRelayStatus_id());
        }
        return "";
    }

    public int getCountDownTime(Map<String, Object> dpsMap) {
        if (isHaveCountDown(dpsMap)) {
            String countDown = String.valueOf(dpsMap.get(getCountDown_id()));
            long countDownTime = (long) Double.parseDouble(countDown);
            return (int) countDownTime;
        }
        return 0;
    }

    public String getCycleTime(Map<String, Object> dpsMap) {
        if (dpsMap == null)
            return "";
        return base64Decode(dpsMap.get(getCycleTime_id()).toString());
    }

    public String getRandomTime(Map<String, Object> dpsMap) {
        if (dpsMap == null)
            return "";
        return base64Decode(dpsMap.get(getRandomTime_id()).toString());
    }

    public String getTodayWeek() {
        String[] weekHexStr = {"00000001", "00000010", "00000100", "00001000", "00010000", "00100000", "01000000"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;
        return weekHexStr[w];
    }

    public String base64Encode(String hexStr) {
        if (TextUtils.isEmpty(hexStr))
            return "";
        return Base64Util.byteArrayToBase64(HexUtil.decode(hexStr));
    }

    public String base64Decode(String base64Str) {
        int sLen = base64Str.length();
        int numGroups = sLen / 4;
        if (TextUtils.isEmpty(base64Str) || (4 * numGroups != sLen))
            return "";
        return HexUtil.encodeToString(Base64Util.base64ToByteArray(base64Str));
    }

    /**
     * 得到十进制星期数
     *
     * @param weekList
     * @return
     */
    public int getDecimalWeek(List<Integer> weekList) {
        int week = 0;

        if (weekList == null || weekList.size() == 0) {
            return week;
        }

        for (int i = 0; i < weekList.size(); i++) {
            switch (weekList.get(i)) {
                case Calendar.MONDAY:
                    week += 2;
                    break;
                case Calendar.TUESDAY:
                    week += 4;
                    break;
                case Calendar.WEDNESDAY:
                    week += 8;
                    break;
                case Calendar.THURSDAY:
                    week += 16;
                    break;
                case Calendar.FRIDAY:
                    week += 32;
                    break;
                case Calendar.SATURDAY:
                    week += 64;
                    break;
                case Calendar.SUNDAY:
                    week += 1;
                    break;
                default:
                    break;
            }
        }
        return week;
    }

    public String addZeroForNum(String str, int strLength) {
        int strLen = str.length();
        if (strLen < strLength) {
            while (strLen < strLength) {
                StringBuffer sb = new StringBuffer();
                sb.append("0").append(str);// 左补0
                str = sb.toString();
                strLen = str.length();
            }
        }
        return str;
    }

    public String getBeginTimeDes(String scheduleTime, int startIndex, int endIndex) {
        int onH = Integer.parseInt(scheduleTime.substring(startIndex, endIndex), 16) / ConstantValue.HOUR_MINUTE;
        int onM = Integer.parseInt(scheduleTime.substring(startIndex, endIndex), 16) % ConstantValue.HOUR_MINUTE;
        return String.format(ConstantValue.TIME_FORMAT_SHORT, onH) + TIME_SPLIT + String.format(ConstantValue.TIME_FORMAT_SHORT, onM);
    }

    public String getEndTimeDes(String scheduleTime, int startIndex, int endIndex) {
        String off = scheduleTime.substring(startIndex, endIndex);
        int offH = Integer.parseInt(off, 16) / ConstantValue.HOUR_MINUTE;
        int offM = Integer.parseInt(off, 16) % ConstantValue.HOUR_MINUTE;
        return String.format(ConstantValue.TIME_FORMAT_SHORT, offH) + TIME_SPLIT + String.format(ConstantValue.TIME_FORMAT_SHORT, offM);
    }

    public int getCurrentFromTime(int hour, int min) {
        return hour * ConstantValue.HOUR_MINUTE + min;
    }

    public int getCurrentToTime(boolean isOverOneDay, int hour, int min) {
        if (isOverOneDay) {
            return (hour + 24) * ConstantValue.HOUR_MINUTE + min;
        } else {
            return hour * ConstantValue.HOUR_MINUTE + min;
        }
    }

   /* public static boolean haveIntersection(int from, int to, int begin, int end){
        if (to < from){
            to = to + 24*60;
        }
        if (end < begin){
            end = end + 24*60;
        }
        return (begin > to || end < from) ? false : true;
    }*/

    public int getRandTimeFrom(String randomTime) {
        String on = randomTime.substring(4, 8);
        int onH = Integer.parseInt(on, 16) / ConstantValue.HOUR_MINUTE;
        int onM = Integer.parseInt(on, 16) % ConstantValue.HOUR_MINUTE;
        int from = onH * 60 + onM;
        return from;
    }

    public int getRandTimeTo(String randomTime) {
        String off = randomTime.substring(8, 12);
        int offH = Integer.parseInt(off, 16) / ConstantValue.HOUR_MINUTE;
        int offM = Integer.parseInt(off, 16) % ConstantValue.HOUR_MINUTE;
        int to = offH * 60 + offM;
        return to;
    }

    public int getCycleTimeFrom(String cycleTime) {
        String on = cycleTime.substring(4, 8);
        int onH = Integer.parseInt(on, 16) / ConstantValue.HOUR_MINUTE;
        int onM = Integer.parseInt(on, 16) % ConstantValue.HOUR_MINUTE;
        int from = onH * 60 + onM;
        return from;
    }

    public int getCycleTimeTo(String cycleTime) {
        String off = cycleTime.substring(8, 12);
        int offH = Integer.parseInt(off, 16) / ConstantValue.HOUR_MINUTE;
        int offM = Integer.parseInt(off, 16) % ConstantValue.HOUR_MINUTE;
        int to = offH * 60 + offM;
        return to;
    }

    public boolean isOpen(String isOpen) {
        if (isOpen.equals(ConstantValue.SCHEDULE_ON)) {
            return true;
        } else if (isOpen.equals(ConstantValue.SCHEDULE_OFF)) {
            return false;
        }
        return false;
    }

    public String getOnceStr() {
        Calendar cal = Calendar.getInstance();
        int i = cal.get(Calendar.DAY_OF_WEEK);

        switch (i) {
            case 1:
                return "00000001";
            case 2:
                return "00000010";
            case 3:
                return "00000100";
            case 4:
                return "00001000";
            case 5:
                return "00010000";
            case 6:
                return "00100000";
            case 7:
                return "01000000";
            default:
                return "00000000";
        }
    }

    //判断是否是带电量的插头
    public boolean isPowerPlug(DeviceBean deviceBean) {
        if (deviceBean == null) {
            return false;
        }
        String productId = deviceBean.getProductId();
        if (productId.equals(ConstantValue.SMART_PLUG_EU_PRODUCTID_TWO)
                || productId.equals(ConstantValue.SMART_PLUG_EU_PRODUCTID_THREE)) {
            return true;
        }
        return false;
    }

    //判断设备是否是墙壁开关
    public boolean isWallSwitch(DeviceBean deviceBean) {
        if (deviceBean == null) {
            return false;
        }
        String productId = deviceBean.getProductId();
        if (productId.equals(ConstantValue.SMART_SWITCH_PRODUCTID)
                || productId.equals(ConstantValue.SMART_SWITCH_PRODUCTID_TWO)) {
            return true;
        }
        return false;
    }

    public String getSwitch_id() {
        return switch_id;
    }

    public void setSwitch_id(String switch_id) {
        this.switch_id = switch_id;
    }

    public String getCountDown_id() {
        return countDown_id;
    }

    public void setCountDown_id(String countDown_id) {
        this.countDown_id = countDown_id;
    }

    public String getRandomTime_id() {
        return randomTime_id;
    }

    public void setRandomTime_id(String randomTime_id) {
        this.randomTime_id = randomTime_id;
    }

    public String getCycleTime_id() {
        return cycleTime_id;
    }

    public void setCycleTime_id(String cycleTime_id) {
        this.cycleTime_id = cycleTime_id;
    }

    public String getNormalTime_id() {
        return normalTime_id;
    }

    public void setNormalTime_id(String normalTime_id) {
        this.normalTime_id = normalTime_id;
    }

    public String getRelayStatus_id() {
        return relayStatus_id;
    }

    public void setRelayStatus_id(String relayStatus_id) {
        this.relayStatus_id = relayStatus_id;
    }

    public String getChildLock_id() {
        return childLock_id;
    }

    public void setChildLock_id(String childLock_id) {
        this.childLock_id = childLock_id;
    }

    public String getAddEle_id() {
        return addEle_id;
    }

    public void setAddEle_id(String addEle_id) {
        this.addEle_id = addEle_id;
    }

    public String getCurCurrent_id() {
        return curCurrent_id;
    }

    public void setCurCurrent_id(String curCurrent_id) {
        this.curCurrent_id = curCurrent_id;
    }

    public String getCurPower_id() {
        return curPower_id;
    }

    public void setCurPower_id(String curPower_id) {
        this.curPower_id = curPower_id;
    }

    public String getCurVoltage_id() {
        return curVoltage_id;
    }

    public void setCurVoltage_id(String curVoltage_id) {
        this.curVoltage_id = curVoltage_id;
    }

    public String getTestBit_id() {
        return testBit_id;
    }

    public void setTestBit_id(String testBit_id) {
        this.testBit_id = testBit_id;
    }

    public String getVoltageCoe_id() {
        return voltageCoe_id;
    }

    public void setVoltageCoe_id(String voltageCoe_id) {
        this.voltageCoe_id = voltageCoe_id;
    }

    public String getElectricCoe_id() {
        return electricCoe_id;
    }

    public void setElectricCoe_id(String electricCoe_id) {
        this.electricCoe_id = electricCoe_id;
    }

    public String getPowerCoe_id() {
        return powerCoe_id;
    }

    public void setPowerCoe_id(String powerCoe_id) {
        this.powerCoe_id = powerCoe_id;
    }

    public String getElectricityCoe_id() {
        return electricityCoe_id;
    }

    public void setElectricityCoe_id(String electricityCoe_id) {
        this.electricityCoe_id = electricityCoe_id;
    }

    public String getFault_id() {
        return fault_id;
    }

    public void setFault_id(String fault_id) {
        this.fault_id = fault_id;
    }
}
