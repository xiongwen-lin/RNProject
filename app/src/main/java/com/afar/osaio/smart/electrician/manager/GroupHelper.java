package com.afar.osaio.smart.electrician.manager;

import android.graphics.Color;
import android.text.TextUtils;

import com.afar.osaio.smart.electrician.bean.MixDeviceBean;
import com.afar.osaio.smart.electrician.bean.Schema;
import com.afar.osaio.smart.electrician.util.Base64Util;
import com.afar.osaio.smart.electrician.util.HexUtil;
import com.afar.osaio.util.ConstantValue;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nooie.common.utils.collection.CollectionUtil;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.GroupBean;
import com.tuya.smart.sdk.bean.GroupDeviceBean;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * GroupHelper
 *
 * @author Administrator
 * @date 2019/3/8
 */
public class GroupHelper {

    public static final String TIME_SPLIT = ":";
    public static final String SWITCH = "switch";
    public static final String SWITCH_1 = "switch_1";//单插开关，排插第一个插孔开关
    public static final String SWITCH_2 = "switch_2";//排插第二个插孔开关
    public static final String SWITCH_3 = "switch_3";//排插第三个插孔开关
    public static final String SWITCH_4 = "switch_4";//排插第四个插孔开关
    public static final String SWITCH_USB1 = "switch_usb1";//排插USB开关
    /*public static final String WORK_MODE = "work_mode";//灯的模式 white colour scene
    public static final String BRIGHT_VALUE = "bright_value";//灯的亮度值
    public static final String TEMP_VALUE = "temp_value";//灯的冷暖值
    public static final String COLOUR_DATA = "colour_data";//灯的彩光
    public static final String SCENE_DATA = "scene_data";//灯的场景*/
    public static final String SWITCH_LED = "switch_led";//灯的开关
    public static final String LED_SWITCH = "led_switch";//灯的开关
    public static final String WORK_MODE = "work_mode";//灯的模式 white colour scene
    public static final String BRIGHT_VALUE = "bright_value";//灯的亮度值
    public static final String TEMP_VALUE = "temp_value";//灯的冷暖值
    public static final String COLOUR_DATA = "colour_data";//灯的彩光
    public static final String SCENE_DATA = "scene_data";//灯的场景
    private static final String SOFT = "flash_scene_1";//柔光模式
    private static final String RAINBOW = "flash_scene_2";//缤纷模式
    private static final String SHINE = "flash_scene_3";//炫彩模式
    private static final String GORGEOUS = "flash_scene_4";//斑斓模式
    public static final String COUNTDOWN = "countdown";
    public static final String RAND_TIME = "random_time";
    public static final String CYCLE_TIME = "cycle_time";
    public static final String NORMAL_TIME = "normalTime";
    public static final String RELAY_STATUS = "relay_status";
    public static final String CHILD_LOCK = "child_lock";
    public static final String CONTROL_DATA = "control_data";

    private String switch_id;
    private String switch_1_id = "1";
    private String switch_2_id = "2";
    private String switch_3_id = "3";
    private String switch_4_id = "4";
    private String switch_usb1_id = "5";
    private String relayStatus_id = "7";
    /*private String work_mode_id = "21";//灯的模式
     private String bright_value_id = "22";//灯的亮度值
     private String temp_value_id = "23";//灯的冷暖值
     private String colour_data_id = "24";//灯的彩光
     private String scene_data_id = "25";//灯的场景*/
    private String switch_led_id = "20";//灯的开关
    private String countDown_id = "26";//灯的倒计时
    private String control_data_id = "28";//灯渐变色指令，用于滑动彩色盘时灯实时改变颜色
    private String led_switch_id = "1";//灯的开关
    private String work_mode_id = "2";//灯的模式
    private String bright_value_id = "3";//灯的亮度值
    private String temp_value_id = "4";//灯的冷暖值
    private String colour_data_id = "5";//灯的彩光
    private String scene_data_id = "6";//灯的场景
    private String soft_id = "7";
    private String rainbow_id = "8";
    private String shine_id = "9";
    private String gorgeous_id = "10";

    private String colour_gradient_id = "28";//灯渐变色指令，用于滑动彩色盘时灯实时改变颜色

    private String randomTime_id;
    private String cycleTime_id;
    private String normalTime_id;
    private String childLock_id;

    private volatile static GroupHelper instance;

    private GroupHelper() {
    }

    public static GroupHelper getInstance() {
        if (instance == null) {
            synchronized (GroupHelper.class) {
                if (instance == null) {
                    instance = new GroupHelper();
                }
            }
        }
        return instance;
    }

    public Map<String, Object> convertDps(String dps) {
        if (!TextUtils.isEmpty(dps)) {
            Gson gson = new Gson();
            Map<String, Object> dpsMap = gson.fromJson(dps, new TypeToken<Map<String, Object>>() {
            }.getType());
            return dpsMap;
        }

        return null;
    }

    //判断群组是否是智能灯的群组
    public boolean isLampGroup(GroupBean groupBean) {
        if (groupBean == null)
            return false;

        if (TextUtils.isEmpty(groupBean.getProductId()))
            return false;

        if (groupBean.getProductId().equals(ConstantValue.SMART_LAMP_PRODUCTID)
                || groupBean.getProductId().equals(ConstantValue.SMART_LAMP_PRODUCTID_TWO)
                || groupBean.getProductId().equals(ConstantValue.SMART_LAMP_PRODUCTID_THREE)
                || groupBean.getProductId().equals(ConstantValue.SMART_LAMP_PRODUCTID_FOUR)
                || groupBean.getProductId().equals(ConstantValue.SMART_LAMP_PRODUCTID_FIVE)
                || groupBean.getProductId().equals(ConstantValue.SMART_LAMP_PRODUCTID_SIX)
        )
            return true;

        return false;
    }

    //判断是否发送老dp的灯
    public boolean isOldDPLamp(GroupBean groupBean) {
        if (groupBean == null) {
            return false;
        }
        String productId = groupBean.getProductId();

        if (productId.equals(ConstantValue.SMART_LAMP_PRODUCTID)
                || productId.equals(ConstantValue.SMART_LAMP_PRODUCTID_TWO)
                || productId.equals(ConstantValue.SMART_LAMP_PRODUCTID_THREE)) {
            return true;
        }
        return false;
    }

    public boolean convertBoolDp(String dpId, String dps) {
        return convertDp(dpId, dps) != null ? (boolean) convertDp(dpId, dps) : false;
    }

    public int convertIntDp(String dpId, String dps) {
        return convertDp(dpId, dps) != null ? (int) convertDp(dpId, dps) : 0;
    }

    public Object convertDp(String dpId, String dps) {
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

    //判断设备是否是排插
    public boolean isPowerStrip(GroupBean groupBean) {
        if (groupBean == null) {
            return false;
        }
        if (TextUtils.isEmpty(groupBean.getProductId())) {
            return false;
        }
        String productId = groupBean.getProductId();
        if (productId.equals(ConstantValue.SMART_STRIP_PRODUCTID)
                || productId.equals(ConstantValue.SMART_STRIP_PRODUCTID_NEW)
                || productId.equals(ConstantValue.SMART_STRIP_PRODUCTID_TWO)
                || productId.equals(ConstantValue.SMART_STRIP_PRODUCTID_THREE)
                || productId.equals(ConstantValue.SMART_STRIP_PRODUCTID_FOUR)) {
            return true;
        }
        return false;
    }

    /**
     * 根据deviceBean获取 到对应的DP值
     */
    public void getDPs(String schemaStr) {
        Gson gson = new Gson();
        List<Schema> schemas = gson.fromJson(schemaStr, new TypeToken<List<Schema>>() {
        }.getType());

        for (Schema schema : schemas) {
            if (schema.getCode().contains(SWITCH_1)) {
                setSwitch_1_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(SWITCH_2)) {
                setSwitch_2_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(SWITCH_3)) {
                setSwitch_3_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(SWITCH_4)) {
                setSwitch_4_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(SWITCH_USB1)) {
                setSwitch_usb1_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(COUNTDOWN)) {
                setCountDown_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(RAND_TIME)) {
                setRandomTime_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(CYCLE_TIME)) {
                setCycleTime_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(NORMAL_TIME)) {
                setNormalTime_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(RELAY_STATUS)) {
                setRelayStatus_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(CHILD_LOCK)) {
                setChildLock_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(SWITCH_LED)) {
                setSwitch_led_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(LED_SWITCH)) {
                setLed_switch_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(WORK_MODE)) {
                setWork_mode_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(BRIGHT_VALUE)) {
                setBright_value_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(TEMP_VALUE)) {
                setTemp_value_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(COLOUR_DATA)) {
                setColour_data_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(SCENE_DATA)) {
                setScene_data_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(SOFT)) {
                setSoft_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(RAINBOW)) {
                setRainbow_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(SHINE)) {
                setShine_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(GORGEOUS)) {
                setGorgeous_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(CONTROL_DATA)) {
                setControl_data_id(String.valueOf(schema.getId()));
            }
        }
    }

    //判断灯是否开关
    public boolean isLampOpen(Map<String, Object> dpsMap) {
        if (dpsMap == null)
            return false;
        if (dpsMap.containsKey(getLed_switch_id())) {
            return (boolean) dpsMap.get(getLed_switch_id());
        }
        if (dpsMap.containsKey(getSwitch_led_id())) {
            return (boolean) dpsMap.get(getSwitch_led_id());
        }
        return false;
    }

    public boolean isDeviceOpen(Map<String, Object> dpsMap) {
        if (dpsMap == null) {
            return false;
        }
        if (dpsMap.containsKey(getSwitch_1_id())) {
            return (boolean) dpsMap.get(getSwitch_1_id());
        }
        return false;
    }

    //判断是不是工作模式中的scene模式
    public boolean isSceneMode(Map<String, Object> dpsMap) {
        if (dpsMap == null) {
            return false;
        }
        if (GroupHelper.getInstance().getLampWorkMode(dpsMap).equals(ConstantValue.LAMP_WORK_MODE_SCENE)
                || GroupHelper.getInstance().getLampWorkMode(dpsMap).equals(ConstantValue.LAMP_WORK_MODE_SCENE_ONE)
                || GroupHelper.getInstance().getLampWorkMode(dpsMap).equals(ConstantValue.LAMP_WORK_MODE_SCENE_TWO)
                || GroupHelper.getInstance().getLampWorkMode(dpsMap).equals(ConstantValue.LAMP_WORK_MODE_SCENE_THREE)
                || GroupHelper.getInstance().getLampWorkMode(dpsMap).equals(ConstantValue.LAMP_WORK_MODE_SCENE_FOUR)) {
            return true;
        }
        return false;
    }

    //获取开关颜色的十六进制字符串
    public String getLampColorHexstr(Map<String, Object> dpsMap) {
        if (dpsMap == null)
            return "";

        if (dpsMap.containsKey(getColour_data_id())) {
            return (String) dpsMap.get(getColour_data_id());
        }

        return "";
    }

    //根据开关颜色的十六进制字符串获得相应的hsv
    public float[] getHsvByColorHexStr(String colorHexStr) {
        float[] hsv = new float[3];
        if (TextUtils.isEmpty(colorHexStr) || colorHexStr.length() < 12) {
            hsv[2] = 10 / 1000f;
            return hsv;
        }
        hsv[0] = Long.parseLong(colorHexStr.substring(0, 4), 16);
        hsv[1] = Long.parseLong(colorHexStr.substring(4, 8), 16) / 1000f;
        hsv[2] = Long.parseLong(colorHexStr.substring(8, 12), 16) / 1000f;
        return hsv;
    }

    //旧dp发送  根据开关颜色的十六进制字符串获得相应的hsv
    public float[] getOldHsvByColorHexStr(String colorHexStr) {
        float[] hsv = new float[3];
        if (TextUtils.isEmpty(colorHexStr) || colorHexStr.length() < 14) {
            hsv[2] = 25 / 255f;
            return hsv;
        }

        hsv[0] = Long.parseLong(colorHexStr.substring(6, 10), 16);
        hsv[1] = Long.parseLong(colorHexStr.substring(10, 12), 16) / 255f;
        hsv[2] = Long.parseLong(colorHexStr.substring(12, 14), 16) / 255f;
        if (hsv[2] < 25 / 255f) {
            hsv[2] = 25 / 255f;
        }
        return hsv;
    }

    //根据hsv得到progressBar的颜色
    public int getColorByHSV(float[] colorHsv) {
        float[] hsv = new float[3];
        hsv[0] = colorHsv[0];
        hsv[1] = colorHsv[1];
        hsv[2] = 1f;
        return Color.HSVToColor(hsv);
    }

    //老dp6  根据主题的颜色获取相应的Hex字符串
    public String getOldThemeHexByColor(String themeColor) {
        int color = Color.parseColor(themeColor);
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        int red = (color & 0xff0000) >> 16;
        int green = (color & 0x00ff00) >> 8;
        int blue = (color & 0x0000ff);
        String r = Integer.toHexString(red);
        String g = Integer.toHexString(green);
        String b = Integer.toHexString(blue);
        String h = Integer.toHexString((int) hsv[0]);
        String s = Integer.toHexString((int) (hsv[1] * 255));
        String v = Integer.toHexString((int) (hsv[2] * 255));
        return addZeroForNum(r, 2) + addZeroForNum(g, 2) + addZeroForNum(b, 2) + addZeroForNum(h, 4) + addZeroForNum(s, 2) + addZeroForNum(v, 2);
    }

    //灯逐渐变色 28
    public String getGraduallyColorHexByIntColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        String h = addZeroForNum(Integer.toHexString((int) hsv[0]), 4);
        String s = addZeroForNum(Integer.toHexString((int) (hsv[1] * 1000)), 4);
        String v = addZeroForNum(Integer.toHexString((int) (hsv[2] * 1000)), 4);
        return "1" + h + s + v + "00000000";
    }

    //灯逐渐变色 28 自定义颜色时使用
    public String getGraduallyColorHex(float hsv[]) {
        String h = addZeroForNum(Integer.toHexString((int) hsv[0]), 4);
        String s = addZeroForNum(Integer.toHexString((int) (hsv[1] * 1000)), 4);
        String v = addZeroForNum(Integer.toHexString((int) (hsv[2] * 1000)), 4);
        return "1" + h + s + v + "00000000";
    }

    //根据颜色的hsv获取彩色的指令 dpId：24
    public String getColorHexByHVS(float[] hvs) {
        String h = Integer.toHexString((int) hvs[0]);
        String s = Integer.toHexString((int) (hvs[1] * 1000));
        String v = Integer.toHexString((int) (hvs[2] * 1000));
        return addZeroForNum(h, 4) + addZeroForNum(s, 4) + addZeroForNum(v, 4);
    }

    //旧dp值发送 根据颜色的hsv获取彩色的指令 dpId：5
    public String getOldColorHexByHVS(int color, float[] hvs) {
        //int color--->change 10 RGB--->16
        int red = (color & 0xff0000) >> 16;
        int green = (color & 0x00ff00) >> 8;
        int blue = (color & 0x0000ff);
        String r = Integer.toHexString(red);
        String g = Integer.toHexString(green);
        String b = Integer.toHexString(blue);
        String h = Integer.toHexString((int) hvs[0]);
        String s = Integer.toHexString((int) (hvs[1] * 255));
        String v = Integer.toHexString((int) (hvs[2] * 255));
        return addZeroForNum(r, 2) + addZeroForNum(g, 2) + addZeroForNum(b, 2) + addZeroForNum(h, 4) + addZeroForNum(s, 2) + addZeroForNum(v, 2);
    }

    //获取智能灯群组模式 white colour scene music
    public String getLampWorkMode(Map<String, Object> dpsMap) {
        if (dpsMap == null) {
            return "";
        }
        if (dpsMap.containsKey(getWork_mode_id())) {
            return (String) dpsMap.get(getWork_mode_id());
        }
        return "";
    }

    //获取智能灯冷暖值
    public int getLampTempValue(Map<String, Object> dpsMap) {
        if (dpsMap == null) {
            return 0;
        }
        if (dpsMap.containsKey(getTemp_value_id())) {
            return (int) dpsMap.get(getTemp_value_id());
        }
        return 0;
    }

    //获取智能灯亮度
    public int getLampBrightValue(Map<String, Object> dpsMap) {
        if (dpsMap == null) {
            return 0;
        }
        if (dpsMap.containsKey(getBright_value_id())) {
            return (int) dpsMap.get(getBright_value_id());
        }
        return 0;
    }

    public String getLampSceneData(Map<String, Object> dpsMap) {

        if (dpsMap == null) {
            return "";
        }

        if (dpsMap.containsKey(getScene_data_id())) {
            return (String) dpsMap.get(getScene_data_id());
        }

        return "";
    }

    public String getLampFlashSceneOne(Map<String, Object> dpsMap) {

        if (dpsMap == null) {
            return "";
        }

        if (dpsMap.containsKey(getSoft_id())) {
            return (String) dpsMap.get(getSoft_id());
        }

        return "";
    }

    public String getLampFlashSceneTwo(Map<String, Object> dpsMap) {

        if (dpsMap == null) {
            return "";
        }

        if (dpsMap.containsKey(getRainbow_id())) {
            return (String) dpsMap.get(getRainbow_id());
        }

        return "";
    }

    public String getLampFlashSceneThree(Map<String, Object> dpsMap) {

        if (dpsMap == null) {
            return "";
        }

        if (dpsMap.containsKey(getShine_id())) {
            return (String) dpsMap.get(getShine_id());
        }

        return "";
    }

    public String getLampFlashSceneFour(Map<String, Object> dpsMap) {

        if (dpsMap == null) {
            return "";
        }

        if (dpsMap.containsKey(getGorgeous_id())) {
            return (String) dpsMap.get(getGorgeous_id());
        }

        return "";
    }

    //根据主题的颜色获取相应的Hex字符串
    public String getThemeHexByColor(String themeColor) {
        int color = Color.parseColor(themeColor);
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        String h = Integer.toHexString((int) hsv[0]);
        String s = Integer.toHexString((int) (hsv[1] * 1000));
        String v = Integer.toHexString((int) (hsv[2] * 1000));
        /**
         * 这里返回的是没有前边位置的自定义的值
         */
        return "0e0d00" + addZeroForNum(h, 4) + addZeroForNum(s, 4) + addZeroForNum(v, 4) + "00000000";
    }

    public boolean isHaveCycleTime(Map<String, Object> dpsMap) {
        if (dpsMap == null)
            return false;
        return (dpsMap.containsKey(getCycleTime_id()) && base64Decode(dpsMap.get(getCycleTime_id()).toString()).length() >= 20);
    }

    //判断是否有调节智能灯
    public boolean isHaveControlData(Map<String, Object> dpsMap) {
        if (dpsMap == null)
            return false;
        return (dpsMap.containsKey(getControl_data_id()));
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

    public String base64Encode(String hexStr) {
        if (TextUtils.isEmpty(hexStr))
            return "";
        return Base64Util.byteArrayToBase64(HexUtil.decode(hexStr));
    }

    public String base64Decode(String base64Str) {
        if (TextUtils.isEmpty(base64Str))
            return "";
        return HexUtil.encodeToString(Base64Util.base64ToByteArray(base64Str));
    }

    //左边补零
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

    public boolean isOpen(String isOpen) {
        if (isOpen.equals(ConstantValue.SCHEDULE_ON)) {
            return true;
        } else if (isOpen.equals(ConstantValue.SCHEDULE_OFF)) {
            return false;
        }
        return false;
    }


    //是否有交集
    public static boolean haveIntersection(int from, int to, int begin, int end) {
        return (begin > to || end < from) ? false : true;
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

    public String getSwitch_1_id() {
        return switch_1_id;
    }

    public void setSwitch_1_id(String switch_1_id) {
        this.switch_1_id = switch_1_id;
    }

    public String getSwitch_2_id() {
        return switch_2_id;
    }

    public void setSwitch_2_id(String switch_2_id) {
        this.switch_2_id = switch_2_id;
    }

    public String getSwitch_3_id() {
        return switch_3_id;
    }

    public void setSwitch_3_id(String switch_3_id) {
        this.switch_3_id = switch_3_id;
    }

    public String getSwitch_4_id() {
        return switch_4_id;
    }

    public void setSwitch_4_id(String switch_4_id) {
        this.switch_4_id = switch_4_id;
    }

    public String getSwitch_usb1_id() {
        return switch_usb1_id;
    }

    public void setSwitch_usb1_id(String switch_usb1_id) {
        this.switch_usb1_id = switch_usb1_id;
    }


    public String getSwitch_led_id() {
        return switch_led_id;
    }

    public void setSwitch_led_id(String switch_led_id) {
        this.switch_led_id = switch_led_id;
    }

    public String getLed_switch_id() {
        return led_switch_id;
    }

    public void setLed_switch_id(String led_switch_id) {
        this.led_switch_id = led_switch_id;
    }

    public String getWork_mode_id() {
        return work_mode_id;
    }

    public void setWork_mode_id(String work_mode_id) {
        this.work_mode_id = work_mode_id;
    }

    public String getBright_value_id() {
        return bright_value_id;
    }

    public void setBright_value_id(String bright_value_id) {
        this.bright_value_id = bright_value_id;
    }

    public String getTemp_value_id() {
        return temp_value_id;
    }

    public void setTemp_value_id(String temp_value_id) {
        this.temp_value_id = temp_value_id;
    }

    public String getColour_data_id() {
        return colour_data_id;
    }

    public void setColour_data_id(String colour_data_id) {
        this.colour_data_id = colour_data_id;
    }

    public String getScene_data_id() {
        return scene_data_id;
    }

    public void setScene_data_id(String scene_data_id) {
        this.scene_data_id = scene_data_id;
    }

    public String getColour_gradient_id() {
        return colour_gradient_id;
    }

    public void setColour_gradient_id(String colour_gradient_id) {
        this.colour_gradient_id = colour_gradient_id;
    }

    public String getSoft_id() {
        return soft_id;
    }

    public void setSoft_id(String soft_id) {
        this.soft_id = soft_id;
    }

    public String getRainbow_id() {
        return rainbow_id;
    }

    public void setRainbow_id(String rainbow_id) {
        this.rainbow_id = rainbow_id;
    }

    public String getShine_id() {
        return shine_id;
    }

    public void setShine_id(String shine_id) {
        this.shine_id = shine_id;
    }

    public String getGorgeous_id() {
        return gorgeous_id;
    }

    public void setGorgeous_id(String gorgeous_id) {
        this.gorgeous_id = gorgeous_id;
    }

    public String getControl_data_id() {
        return control_data_id;
    }

    public void setControl_data_id(String control_data_id) {
        this.control_data_id = control_data_id;
    }
}
