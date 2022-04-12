package com.afar.osaio.smart.electrician.manager;

import android.graphics.Color;
import android.text.TextUtils;

import com.afar.osaio.smart.electrician.bean.PowerStripName;
import com.afar.osaio.smart.electrician.bean.PowerStripScheduleBean;
import com.afar.osaio.smart.electrician.bean.Schedule;
import com.afar.osaio.smart.electrician.bean.Schema;
import com.afar.osaio.smart.electrician.util.Base64Util;
import com.afar.osaio.smart.electrician.util.HexUtil;
import com.afar.osaio.util.ConstantValue;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.GroupBean;
import com.tuya.smart.sdk.bean.Timer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PowerStripHelper {

    /**
     * 单插schema
     * [{"mode":"rw","code":"switch_1","name":"开关1","property":{"type":"bool"},"iconname":"icon-dp_power2","id":1,"type":"obj","desc":""},{"mode":"rw","code":"countdown_1","name":"开关1倒计时","property":{"unit":"s","min":0,"max":86400,"scale":0,"step":1,"type":"value"},"iconname":"icon-dp_time2","id":9,"type":"obj","desc":""},{"mode":"rw","code":"relay_status","name":"设备重启状态设置","property":{"range":["0","1","2"],"type":"enum"},"iconname":"icon-dp_filter","id":39,"type":"obj","desc":"0：断电；\n1：通电；\n2：记忆继电器上次状态"},{"mode":"rw","code":"child_lock","name":"童锁","property":{"type":"bool"},"iconname":"icon-dp_lock","id":41,"type":"obj","desc":"童锁打开发true,关闭发false"},{"mode":"rw","code":"cycle_time","name":"循环定时","property":{"type":"string","maxlen":255},"id":42,"type":"obj","desc":"最大10个定时功能: 每10个字节代表 #1通道号（bit7--开关，bit6-bit0---通道号）/#2星期/#3#4起始时间(min)/#5#6结束时间(min)/#7#8开启时间(min)/#9#10关闭时间(min) \n星期：00(单次)01(周日)02(周一)04(周二)08(周三)10(周四)20(周五)40(周六)"},{"mode":"rw","code":"random_time","name":"随机定时","property":{"type":"string","maxlen":255},"id":43,"type":"obj","desc":"最大16个定时功能：每6个字节代表 #1通道号（bit7--开关，bit6-bit0---通道号----排插位数0-6）/#2星期/#3#4起始时间(min)/#5#6结束时间(min)，最大42组\n字符型：6*2字节"}]
     */

    private final int SWITCH_BEGIN_INDEX = 0;
    private final int SWITCH_END_INDEX = 2;

    private String SWITCH = "switch";
    private String SWITCH_1 = "switch_1";//排插开关1或单插开关
    private String SWITCH_2 = "switch_2";//开关2
    private String SWITCH_3 = "switch_3";//开关3
    private String SWITCH_4 = "switch_4";//开关4
    private String SWITCH_USB1 = "switch_usb1";//开关usb
    private String SWITCH_ALL = "switch_all";//全部开关
    private String COUNTDOWN_1 = "countdown_1";//开关1倒计时
    private String COUNTDOWN_2 = "countdown_2";//开关2倒计时
    private String COUNTDOWN_3 = "countdown_3";//开关3倒计时
    private String COUNTDOWN_4 = "countdown_4";//开关4倒计时
    private String COUNTDOWN1 = "countdown1";//开关1倒计时
    private String COUNTDOWN2 = "countdown2";//开关2倒计时
    private String COUNTDOWN3 = "countdown3";//开关3倒计时
    private String COUNTDOWN_USB1 = "countdown_usb1";//开关USB1倒计时
    private String RELAY_STATUS = "relay_status";//设备重启状态设置
    private String CHILD_LOCK = "child_lock";//童锁
    private String CYCLE_TIME = "cycle_time";//循环定时
    private String RANDOM_TIME = "random_time";//随机定时
    private String SWITCH_INCHING = "switch_inching";//点动开关
    private String SWITCH_OVERCHARGE = "switch_overcharge";//过充保护
    public static final String RAND_TIME_2 = "randomTime"; // teckin randomTime Identifier
    public static final String CYCLE_TIME_2 = "cycleTime"; // teckin cycleTime Identifier
    public static final String NORMAL_TIME = "normalTime"; // teckin normalTime Identifier

    private String switch_1_id = "1";
    private String switch_2_id = "2";
    private String switch_3_id = "3";
    private String switch_4_id = "4";
    private String switch_usb1_id = "5";
    private String switch_all_id = "38";
    private String countdown_1_id = "9";
    private String countdown_2_id = "10";
    private String countdown_3_id = "11";
    private String countdown_4_id = "12";
    private String countdown_usb1_id = "15";
    private String relay_status_id = "7";
    private String child_lock_id = "41";
    private String switch_inching_id = "44";
    private String switch_overcharge_id = "46";
    private String cycle_time_id = "102";
    private String random_time_id = "101";
    private String normal_time_id = "103";

    //---------  智能灯 -----

    /*private final String SWITCH_LED = "switch_led";//开关
    private final String WORK_MODE = "work_mode";//模式
    private final String BRIGHT_VALUE = "bright_value";//亮度值
    private final String TEMP_VALUE = "temp_value";//冷暖值
    private final String COLOUR_DATA = "colour_data";//彩光
    private final String SCENE_DATA = "scene_data";//场景
    private final String COUNTDOWN = "countdown";//倒计时剩余时间
    private final String CONTROL_DATA = "control_data";//调节
    private final String WHITE_COLOR = "white_color";//白光常用
    private final String FULL_COLOR = "full_color";//彩光常用
    private final String WAKEUP_MODE = "wakeup_mode";//唤醒模式/淡入
    private final String SLEEP_MODE = "sleep_mode";//入睡模式/淡出*/

    //---------  teckin智能灯 -----
    private final String SWITCH_LED = "switch_led";
    private final String COUNTDOWN = "countdown";
    private final String CONTROL_DATA = "control_data";
    private final String LED_SWITCH = "led_switch";//开关
    private final String WORK_MODE = "work_mode";//模式
    private final String BRIGHT_VALUE = "bright_value";//亮度值
    private final String TEMP_VALUE = "temp_value";//冷暖值
    private final String COLOUR_DATA = "colour_data";//彩光
    private final String SCENE_DATA = "scene_data";//场景
    private final String SOFT = "flash_scene_1";//柔光模式
    private final String RAINBOW = "flash_scene_2";//缤纷模式
    private final String SHINE = "flash_scene_3";//炫彩模式
    private final String GORGEOUS = "flash_scene_4";//斑斓模式

    /*private String work_mode_id = "21";
    private String bright_value_id = "22";
    private String temp_value_id = "23";
    private String colour_data_id = "24";
    private String scene_data_id = "25";
    private String white_color_id = "101";
    private String full_color_id = "102";
    private String wakeup_mode_id = "103";
    private String sleep_mode_id = "104";*/

    private String led_switch_id = "1";
    private String work_mode_id = "2";
    private String new_work_mode_id = "21";
    private String bright_value_id = "3";
    private String new_bright_value_id = "22";
    private String temp_value_id = "4";
    private String colour_data_id = "5";
    private String new_colour_data_id = "24";
    private String scene_data_id = "6";
    private String soft_id = "7";
    private String rainbow_id = "8";
    private String shine_id = "9";
    private String gorgeous_id = "10";
    private String switch_led_id = "20";
    private String countdown_id = "26";
    private String control_data_id = "28";
    private String colour_gradient_id = "28";//灯渐变色指令，用于滑动彩色盘时灯实时改变颜色

    private volatile static PowerStripHelper instance;

    private PowerStripHelper() {
    }

    public static PowerStripHelper getInstance() {
        if (instance == null) {
            synchronized (PowerStripHelper.class) {
                if (instance == null) {
                    instance = new PowerStripHelper();
                }
            }
        }
        return instance;
    }

    /**
     * 根据deviceBean获取 到对应的DP值
     */
    public void getDPs(DeviceBean deviceBean) {
        String test = deviceBean.getProductBean().getSchemaInfo().getSchema();
//        LogUtil.e("------------->>> schema "+test);
        Gson gson = new Gson();
        List<Schema> schemas = gson.fromJson(test, new TypeToken<List<Schema>>() {
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
            } else if (schema.getCode().contains(SWITCH_ALL)) {
                setSwitch_all_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(COUNTDOWN_1) || (schema.getCode().contains(COUNTDOWN1))) {
                setCountdown_1_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(COUNTDOWN_2) || (schema.getCode().contains(COUNTDOWN2))) {
                setCountdown_2_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(COUNTDOWN_3) || (schema.getCode().contains(COUNTDOWN3))) {
                setCountdown_3_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(COUNTDOWN_4)) {
                setCountdown_4_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(COUNTDOWN_USB1)) {
                setCountdown_usb1_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(RELAY_STATUS)) {
                setRelay_status_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(CHILD_LOCK)) {
                setChild_lock_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(CYCLE_TIME) || (schema.getCode().contains(CYCLE_TIME_2))) {
                setCycle_time_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(RANDOM_TIME) || (schema.getCode().contains(RAND_TIME_2))) {
                setRandom_time_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(NORMAL_TIME)) {
                setNormal_time_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(SWITCH_OVERCHARGE)) {
                setSwitch_overcharge_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(SWITCH_INCHING)) {
                setSwitch_inching_id(String.valueOf(schema.getId()));
            }
        }
    }

    public void getLampDPs(DeviceBean deviceBean) {
        String test = deviceBean.getProductBean().getSchemaInfo().getSchema();
        //LogUtil.e("------------->>> schema " + test);
        Gson gson = new Gson();
        List<Schema> schemas = gson.fromJson(test, new TypeToken<List<Schema>>() {
        }.getType());
        for (Schema schema : schemas) {
            if (schema.getCode().contains(SWITCH_LED)) {
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
            } else if (schema.getCode().contains(COUNTDOWN)) {
                setCountdown_id(String.valueOf(schema.getId()));
            } else if (schema.getCode().contains(CONTROL_DATA)) {
                setControl_data_id(String.valueOf(schema.getId()));
            }
        }
    }

    public Map<String, Object> getControlPoerStripMap(DeviceBean deviceBean, boolean isOpen) {
        Map<String, Object> dpsMap = new HashMap<>();
        String test = deviceBean.getProductBean().getSchemaInfo().getSchema();
        Gson gson = new Gson();
        List<Schema> schemas = gson.fromJson(test, new TypeToken<List<Schema>>() {
        }.getType());
        for (Schema schema : schemas) {
            if (schema.getCode().contains(SWITCH)) {
                dpsMap.put(String.valueOf(schema.getId()), isOpen);
            }
        }
        return dpsMap;
    }

    //判断排插开关状态
    public boolean isDeviceOpen(DeviceBean deviceBean) {
        if (deviceBean == null || deviceBean.getProductBean() == null || deviceBean.getProductBean().getSchemaInfo() == null) {
            return false;
        }
        String schemaStr = deviceBean.getProductBean().getSchemaInfo().getSchema();
        if (TextUtils.isEmpty(schemaStr)) {
            return false;
        }
        Gson gson = new Gson();
        List<Schema> schemas = gson.fromJson(schemaStr, new TypeToken<List<Schema>>() {
        }.getType());
        Map<String, Object> dpsMap = deviceBean.getDps();
        if (dpsMap == null) {
            return false;
        }
        for (Schema schema : schemas) {
            if (schema.getCode().contains(SWITCH) && !schema.getCode().equals(SWITCH_ALL) && !schema.getCode().equals(SWITCH_OVERCHARGE)) {
                if (dpsMap.containsKey(String.valueOf(schema.getId()))) {
                    boolean isOpen = false;
                    try {
                        Object value = dpsMap.get(String.valueOf(schema.getId()));
                        if (value != null && value instanceof Boolean) {
                            isOpen = (boolean) value;
                        }
                    } catch (ClassCastException e) {
                        e.printStackTrace();
                    }
                    /*if (!isOpen) {
                        return false;
                    }*/
                    if (isOpen) {
                        return true;
                    }
                }
            }
        }
        //return true;
        return false;
    }

    public List<PowerStripName> convertPowerStripName(String json) {
        if (!TextUtils.isEmpty(json)) {
            Gson gson = new Gson();
            List<PowerStripName> powerStripNames = gson.fromJson(json, new TypeToken<List<PowerStripName>>() {
            }.getType());
            return powerStripNames;
        }
        return null;
    }

    public String convertPowerStripTaskTimeName(String devId, String dpId) {
        return devId + "_" + "switch_" + dpId;
    }

    //判断是否使用涂鸦面板
    public boolean isUserTuyaPanel(DeviceBean deviceBean) {
        if (deviceBean == null) {
            return false;
        }
        String productId = deviceBean.getProductId();
        if (productId.equals(ConstantValue.SMART_PLUG_PRODUCTID)
                || productId.equals(ConstantValue.SMART_PLUG_EU_PRODUCTID_TWO)
                || productId.equals(ConstantValue.SMART_PLUG_UK_PRODUCTID)
                || productId.equals(ConstantValue.SMART_LAMP_PRODUCTID)
                || productId.equals(ConstantValue.SMART_LAMP_PRODUCTID_TWO)
                || productId.equals(ConstantValue.SMART_LIGHT_STRIP_PRODUCTID_TWO)
                || productId.equals(ConstantValue.SMART_LIGHT_STRIP_PRODUCTID_THREE)
                || productId.equals(ConstantValue.SMART_LIGHT_STRIP_PRODUCTID_FOUR)
                || productId.equals(ConstantValue.SMART_LAMP_PRODUCTID_FIVE)
                || productId.equals(ConstantValue.SMART_LAMP_PRODUCTID_SIX)
                || productId.equals(ConstantValue.SMART_FLOOR_LAMP_PRODUCTID_TWO)
                || productId.equals(ConstantValue.SMART_PLUG_PRODUCTID_NEW_TWO)
                || productId.equals(ConstantValue.SMART_PLUG_US_PRODUCTID_FOUR)
                || productId.equals(ConstantValue.SMART_PLUG_EU_PRODUCTID_FOUR)
                || productId.equals(ConstantValue.SMART_PLUG_EU_PRODUCTID_FIVE)
                || productId.equals(ConstantValue.SMART_PLUG_UK_PRODUCTID_FOUR)
                || productId.equals(ConstantValue.SMART_PLUG_EU_PRODUCTID_SIX)
                || productId.equals(ConstantValue.SMART_STRIP_PRODUCTID_FIVE)
                || productId.equals(ConstantValue.SMART_STRIP_PRODUCTID_SIX)
                || productId.equals(ConstantValue.SMART_PLUG_UK_PRODUCTID_FIVE)
                || productId.equals(ConstantValue.SMART_LAMP_PRODUCTID_SEVEN)
                || productId.equals(ConstantValue.SMART_LAMP_PRODUCTID_EIGHT)
                || productId.equals(ConstantValue.SMART_LAMP_PRODUCTID_NINE)
                || productId.equals(ConstantValue.SMART_LAMP_PRODUCTID_TEN)
                || productId.equals(ConstantValue.SMART_LAMP_PRODUCTID_ELEVEN)
                || productId.equals(ConstantValue.SMART_LAMP_PRODUCTID_TWELVE)
                || productId.equals(ConstantValue.SMART_STRIP_PRODUCTID_SEVEN)
                || productId.equals(ConstantValue.SMART_PLUG_EU_PRODUCTID_SEVEN)
                || productId.equals(ConstantValue.SMART_SWITCH_PRODUCTID_FIVE)
                || productId.equals(ConstantValue.SMART_SWITCH_PRODUCTID_SIX)
                || productId.equals(ConstantValue.SMART_BULB_PRODUCTID)
        ) {
            return true;
        }
        return false;
    }

    //判断是否是单插
    public boolean isPlug(DeviceBean deviceBean) {
        if (deviceBean == null) {
            return false;
        }
        String productId = deviceBean.getProductId();
        if (productId.equals(ConstantValue.SMART_PLUG_PRODUCTID)
                || productId.equals(ConstantValue.SMART_PLUG_PRODUCTID_NEW)
                || productId.equals(ConstantValue.SMART_PLUG_PRODUCTID_OLD)
                || productId.equals(ConstantValue.SMART_PLUG_US_PRODUCTID_TWO)
                || productId.equals(ConstantValue.SMART_PLUG_US_PRODUCTID_THREE)
                || productId.equals(ConstantValue.SMART_PLUG_EU_PRODUCTID)
                || productId.equals(ConstantValue.SMART_PLUG_UK_PRODUCTID)
                || productId.equals(ConstantValue.SMART_PLUG_UK_PRODUCTID_TWO)
                || productId.equals(ConstantValue.SMART_PLUG_UK_PRODUCTID_THREE)
                || productId.equals(ConstantValue.SMART_PLUG_EU_PRODUCTID_TWO)
                || productId.equals(ConstantValue.SMART_PLUG_EU_PRODUCTID_THREE)
                || productId.equals(ConstantValue.SMART_PLUG_JP_PRODUCTID_ONE)
                || productId.equals(ConstantValue.SMART_PLUG_PRODUCTID_NEW_TWO)
                || productId.equals(ConstantValue.SMART_PLUG_US_PRODUCTID_FOUR)
                || productId.equals(ConstantValue.SMART_PLUG_EU_PRODUCTID_FOUR)
                || productId.equals(ConstantValue.SMART_PLUG_EU_PRODUCTID_FIVE)
                || productId.equals(ConstantValue.SMART_PLUG_UK_PRODUCTID_FOUR)
                || productId.equals(ConstantValue.SMART_PLUG_EU_PRODUCTID_SIX)
                || productId.equals(ConstantValue.SMART_PLUG_UK_PRODUCTID_FIVE)
                || productId.equals(ConstantValue.SMART_PLUG_EU_PRODUCTID_SEVEN)
                || productId.equals(ConstantValue.SMART_PLUG_EU_PRODUCTID_EIGHT)
                || productId.equals(ConstantValue.SMART_PLUG_EU_PRODUCTID_NINE)
                || productId.equals(ConstantValue.SMART_PLUG_US_PRODUCTID_FIVE)
                || productId.equals(ConstantValue.SMART_PLUG_EU_PRODUCTID_TEN)
                || productId.equals(ConstantValue.SMART_PLUG_US_PRODUCTID_SIX)
                || productId.equals(ConstantValue.SMART_PLUG_UK_PRODUCTID_SIX)
                || productId.equals(ConstantValue.SMART_PLUG_UK_PRODUCTID_SEVEN)
        ) {
            return true;
        }
        return false;
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

    //判断设备是否是排插
    public boolean isPowerStrip(DeviceBean deviceBean) {
        if (deviceBean == null) {
            return false;
        }
        String productId = deviceBean.getProductId();
        if (productId.equals(ConstantValue.SMART_STRIP_PRODUCTID)
                || productId.equals(ConstantValue.SMART_STRIP_PRODUCTID_NEW)
                || productId.equals(ConstantValue.SMART_STRIP_PRODUCTID_TWO)
                || productId.equals(ConstantValue.SMART_STRIP_PRODUCTID_THREE)
                || productId.equals(ConstantValue.SMART_STRIP_PRODUCTID_FOUR)
                || productId.equals(ConstantValue.SMART_STRIP_PRODUCTID_SEVEN)
               /* || productId.equals(ConstantValue.SMART_STRIP_PRODUCTID_FIVE)
                || productId.equals(ConstantValue.SMART_STRIP_PRODUCTID_SIX)*/
        ) {
            return true;
        }
        return false;
    }

    //判断设备是否是3空排插
    public boolean isThreeHolesPowerStrip(DeviceBean deviceBean) {
        if (deviceBean == null) {
            return false;
        }
        String productId = deviceBean.getProductId();
        if (productId.equals(ConstantValue.SMART_STRIP_PRODUCTID_THREE)
                || productId.equals(ConstantValue.SMART_STRIP_PRODUCTID_SEVEN)) {
            return true;
        }
        return false;
    }

    //判断设备是否是5孔排插
    public boolean isFiveHolesPowerStrip(DeviceBean deviceBean) {
        if (deviceBean == null) {
            return false;
        }
        String productId = deviceBean.getProductId();
        if (productId.equals(ConstantValue.SMART_STRIP_PRODUCTID_FOUR)) {
            return true;
        }
        return false;
    }

    //判断设备是否是调光插头
    public boolean isDimmerPlug(DeviceBean deviceBean) {
        if (deviceBean == null) {
            return false;
        }
        String productId = deviceBean.getProductId();
        if (productId.equals(ConstantValue.SMART_STRIP_PRODUCTID_FIVE)
                || productId.equals(ConstantValue.SMART_STRIP_PRODUCTID_SIX)
        ) {
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
                || productId.equals(ConstantValue.SMART_SWITCH_PRODUCTID_TWO)
                || productId.equals(ConstantValue.SMART_SWITCH_PRODUCTID_FIVE)
                || productId.equals(ConstantValue.SMART_SWITCH_PRODUCTID_SIX)
        ) {
            return true;
        }
        return false;
    }

    //判断设备是否是墙壁开关3个
    public boolean isThreeHolesWallSwitch(DeviceBean deviceBean) {
        if (deviceBean == null) {
            return false;
        }
        String productId = deviceBean.getProductId();
        if (productId.equals(ConstantValue.SMART_SWITCH_PRODUCTID_THREE)) {
            return true;
        }
        return false;
    }

    //判断设备是否是墙壁多个开关
    public boolean isMultiWallSwitch(DeviceBean deviceBean) {
        if (deviceBean == null) {
            return false;
        }
        String productId = deviceBean.getProductId();
        if (productId.equals(ConstantValue.SMART_SWITCH_PRODUCTID_THREE)
                || productId.equals(ConstantValue.SMART_SWITCH_PRODUCTID_FOUR)) {
            return true;
        }
        return false;
    }

    //判断设备是否是灯带
    public boolean isLampStrip(DeviceBean deviceBean) {
        if (deviceBean == null) {
            return false;
        }
        String productId = deviceBean.getProductId();
        if (productId.equals(ConstantValue.SMART_LIGHT_STRIP_PRODUCTID)
                || productId.equals(ConstantValue.SMART_LIGHT_STRIP_PRODUCTID_TWO)
                || productId.equals(ConstantValue.SMART_LIGHT_STRIP_PRODUCTID_THREE)
                || productId.equals(ConstantValue.SMART_LIGHT_STRIP_PRODUCTID_FOUR)) {
            return true;
        }
        return false;
    }

    //判断设备是否是调光器
    public boolean isLightModulator(DeviceBean deviceBean) {
        if (deviceBean == null) {
            return false;
        }
        String productId = deviceBean.getProductId();
        if (productId.equals(ConstantValue.SMART_LIGHT_MODULATOR_PRODUCTID)) {
            return true;
        }
        return false;
    }

    //判断设备是否是宠物喂食器
    public boolean isPetFeeder(DeviceBean deviceBean) {
        if (deviceBean == null) {
            return false;
        }
        String productId = deviceBean.getProductId();
        if (productId.equals(ConstantValue.SMART_FEEDER_PRODUCTID)) {
            return true;
        }
        return false;
    }

    //判断设备是否是空气净化器
    public boolean isAirPurifier(DeviceBean deviceBean) {
        if (deviceBean == null) {
            return false;
        }
        String productId = deviceBean.getProductId();
        if (productId.equals(ConstantValue.SMART_AIR_PURIFIER_PRODUCTID)) {
            return true;
        }
        return false;
    }

    //判断设备是否是落地灯
    public boolean isFloorLamp(DeviceBean deviceBean) {
        if (deviceBean == null) {
            return false;
        }
        String productId = deviceBean.getProductId();
        if (productId.equals(ConstantValue.SMART_FLOOR_LAMP_PRODUCTID)
                || productId.equals(ConstantValue.SMART_FLOOR_LAMP_PRODUCTID_TWO)) {
            return true;
        }
        return false;
    }

    //判断设备是否是只有定点定时的单插
    public boolean isActionPlug(DeviceBean deviceBean) {
        if (deviceBean == null) {
            return false;
        }
        String productId = deviceBean.getProductId();
        if (productId.equals(ConstantValue.SMART_PLUG_US_PRODUCTID_TWO)
                || productId.equals(ConstantValue.SMART_PLUG_US_PRODUCTID_THREE)
                || productId.equals(ConstantValue.SMART_PLUG_EU_PRODUCTID)
                || productId.equals(ConstantValue.SMART_PLUG_UK_PRODUCTID)
                || productId.equals(ConstantValue.SMART_PLUG_UK_PRODUCTID_TWO)
                || productId.equals(ConstantValue.SMART_PLUG_UK_PRODUCTID_THREE)
                || productId.equals(ConstantValue.SMART_PLUG_EU_PRODUCTID_TWO)
                || productId.equals(ConstantValue.SMART_PLUG_EU_PRODUCTID_THREE)
                || productId.equals(ConstantValue.SMART_SWITCH_PRODUCTID)
                || productId.equals(ConstantValue.SMART_SWITCH_PRODUCTID_TWO)) {
            return true;
        }
        return false;
    }

    //判断是否是智能灯
    public boolean isLamp(DeviceBean deviceBean) {
        if (deviceBean == null) {
            return false;
        }
        String productId = deviceBean.getProductId();

        if (productId.equals(ConstantValue.SMART_LAMP_PRODUCTID)
                || productId.equals(ConstantValue.SMART_LAMP_PRODUCTID_TWO)
                || productId.equals(ConstantValue.SMART_LAMP_PRODUCTID_THREE)
                || productId.equals(ConstantValue.SMART_LAMP_PRODUCTID_FOUR)
                || productId.equals(ConstantValue.SMART_LAMP_PRODUCTID_FIVE)
                || productId.equals(ConstantValue.SMART_LAMP_PRODUCTID_SIX)
                || productId.equals(ConstantValue.SMART_FLOOR_LAMP_PRODUCTID)
                || productId.equals(ConstantValue.SMART_FLOOR_LAMP_PRODUCTID_TWO)
                || productId.equals(ConstantValue.SMART_LIGHT_STRIP_PRODUCTID)
                || productId.equals(ConstantValue.SMART_LIGHT_STRIP_PRODUCTID_TWO)
                || productId.equals(ConstantValue.SMART_LIGHT_STRIP_PRODUCTID_THREE)
                || productId.equals(ConstantValue.SMART_LIGHT_STRIP_PRODUCTID_FOUR)
                || productId.equals(ConstantValue.SMART_LIGHT_MODULATOR_PRODUCTID)
                || productId.equals(ConstantValue.SMART_LAMP_PRODUCTID_SEVEN)
                || productId.equals(ConstantValue.SMART_LAMP_PRODUCTID_EIGHT)
                || productId.equals(ConstantValue.SMART_LAMP_PRODUCTID_NINE)
                || productId.equals(ConstantValue.SMART_LAMP_PRODUCTID_TEN)
                || productId.equals(ConstantValue.SMART_LAMP_PRODUCTID_ELEVEN)
                || productId.equals(ConstantValue.SMART_LAMP_PRODUCTID_TWELVE)
                || productId.equals(ConstantValue.SMART_LAMP_PRODUCTID_THIRTEEN)
                || productId.equals(ConstantValue.SMART_BULB_PRODUCTID)
        ) {
            return true;
        }
        return false;
    }

    //判断是否发送老dp的灯
    public boolean isOldDPLamp(DeviceBean deviceBean) {
        if (deviceBean == null) {
            return false;
        }
        String productId = deviceBean.getProductId();

        if (productId.equals(ConstantValue.SMART_LAMP_PRODUCTID)
                || productId.equals(ConstantValue.SMART_LAMP_PRODUCTID_TWO)
                || productId.equals(ConstantValue.SMART_LAMP_PRODUCTID_THREE)
                || productId.equals(ConstantValue.SMART_FLOOR_LAMP_PRODUCTID)) {
            return true;
        }
        return false;
    }

    public String getWorkModeId(DeviceBean deviceBean) {
        if (deviceBean == null) {
            return "";
        }
        if (isOldDPLamp(deviceBean)) {
            return getWork_mode_id();
        } else {
            return new_work_mode_id;
        }
    }

    public String getBrightValueId(DeviceBean deviceBean) {
        if (deviceBean == null) {
            return "";
        }
        if (isOldDPLamp(deviceBean)) {
            return getBright_value_id();
        } else {
            return new_bright_value_id;
        }
    }

    public String getColourDataId(DeviceBean deviceBean) {
        if (deviceBean == null) {
            return "";
        }
        if (isOldDPLamp(deviceBean)) {
            return getColour_data_id();
        } else {
            return new_colour_data_id;
        }
    }

    //判断是否是智能灯的群组
    public boolean isLampGroup(GroupBean groupBean) {
        if (groupBean == null) {
            return false;
        }
        if (groupBean.getProductId().contains(ConstantValue.SMART_LAMP_PRODUCTID)
                || groupBean.getProductId().contains(ConstantValue.SMART_LAMP_PRODUCTID_TWO)
                || groupBean.getProductId().contains(ConstantValue.SMART_LAMP_PRODUCTID_THREE)
                || groupBean.getProductId().contains(ConstantValue.SMART_LAMP_PRODUCTID_FOUR)
                || groupBean.getProductId().contains(ConstantValue.SMART_LAMP_PRODUCTID_FIVE)
                || groupBean.getProductId().contains(ConstantValue.SMART_LAMP_PRODUCTID_SIX)
        ) {
            return true;
        }

        return false;
    }

    public boolean isPlugOpen(Map<String, Object> mDpsMap, String dpId) {
        if (mDpsMap == null) {
            return false;
        }
        if (mDpsMap.containsKey(dpId)) {
            return (boolean) mDpsMap.get(dpId);
        }
        return false;
    }

    //判断单插是否有倒计时
    public boolean isHavePlugCountDown(Map<String, Object> dpsMap) {
        if (dpsMap == null)
            return false;
        return dpsMap.containsKey(getCountdown_1_id());
    }

    //获取单插的倒计时
    public int getPlugCountDownTime(Map<String, Object> dpsMap) {
        if (isHavePlugCountDown(dpsMap)) {
            String countDown = String.valueOf(dpsMap.get(getCountdown_1_id()));
            long countDownTime = (long) Double.parseDouble(countDown);
            return (int) countDownTime;
        }
        return 0;
    }

    //判断是否有循环定时
    public boolean isHaveCycleTime(Map<String, Object> dpsMap) {
        if (dpsMap == null)
            return false;
        return (dpsMap.containsKey(getCycle_time_id()) && base64Decode(dpsMap.get(getCycle_time_id()).toString()).length() >= 20);
    }

    //获取循环定时的HexStr
    public String getCycleTime(Map<String, Object> dpsMap) {
        if (dpsMap == null)
            return "";
        return base64Decode(dpsMap.get(getCycle_time_id()).toString());
    }

    //判断是否有随机定时
    public boolean isHaveRandomTime(Map<String, Object> dpsMap) {
        if (dpsMap == null)
            return false;
        return (dpsMap.containsKey(getRandom_time_id()) && base64Decode(dpsMap.get(getRandom_time_id()).toString()).length() >= 12);
    }

    //获取随机定时的HexStr
    public String getRandomTime(Map<String, Object> dpsMap) {
        if (dpsMap == null)
            return "";
        return base64Decode(dpsMap.get(getRandom_time_id()).toString());
    }

    //判断是否有调节智能灯
    public boolean isHaveControlData(Map<String, Object> dpsMap) {
        if (dpsMap == null)
            return false;
        return (dpsMap.containsKey(getControl_data_id()));
    }

    //判断是否有倒计时智能灯
    public boolean isHaveCountDown(Map<String, Object> dpsMap) {
        if (dpsMap == null)
            return false;
        return (dpsMap.containsKey(getCountdown_id()));
    }

    //判断是否有插孔1倒计时
    public boolean isHaveCountDown1(Map<String, Object> dpsMap) {
        if (dpsMap == null)
            return false;
        return dpsMap.containsKey(getCountdown_1_id());
    }

    //获取插孔1的倒计时
    public int getCountDownTime1(Map<String, Object> dpsMap) {
        if (isHaveCountDown1(dpsMap)) {
            String countDown = String.valueOf(dpsMap.get(getCountdown_1_id()));
            long countDownTime = (long) Double.parseDouble(countDown);
            return (int) countDownTime;
        }
        return 0;
    }

    //判断是否有插孔2倒计时
    public boolean isHaveCountDown2(Map<String, Object> dpsMap) {
        if (dpsMap == null)
            return false;
        return dpsMap.containsKey(getCountdown_2_id());
    }

    //获取插孔2的倒计时
    public int getCountDownTime2(Map<String, Object> dpsMap) {
        if (isHaveCountDown2(dpsMap)) {
            String countDown = String.valueOf(dpsMap.get(getCountdown_2_id()));
            long countDownTime = (long) Double.parseDouble(countDown);
            return (int) countDownTime;
        }
        return 0;
    }

    //判断是否有插孔3倒计时
    public boolean isHaveCountDown3(Map<String, Object> dpsMap) {
        if (dpsMap == null)
            return false;
        return dpsMap.containsKey(getCountdown_3_id());
    }

    //获取插孔3的倒计时
    public int getCountDownTime3(Map<String, Object> dpsMap) {
        if (isHaveCountDown3(dpsMap)) {
            String countDown = String.valueOf(dpsMap.get(getCountdown_3_id()));
            long countDownTime = (long) Double.parseDouble(countDown);
            return (int) countDownTime;
        }
        return 0;
    }

    //判断是否有插孔4倒计时
    public boolean isHaveCountDown4(Map<String, Object> dpsMap) {
        if (dpsMap == null)
            return false;
        return dpsMap.containsKey(getCountdown_4_id());
    }

    //获取插孔4的倒计时
    public int getCountDownTime4(Map<String, Object> dpsMap) {
        if (isHaveCountDown4(dpsMap)) {
            String countDown = String.valueOf(dpsMap.get(getCountdown_4_id()));
            long countDownTime = (long) Double.parseDouble(countDown);
            return (int) countDownTime;
        }
        return 0;
    }

    //判断是否有插孔Usb1倒计时
    public boolean isHaveCountDownUsb1(Map<String, Object> dpsMap) {
        if (dpsMap == null)
            return false;
        return dpsMap.containsKey(getCountdown_usb1_id());
    }

    //获取插孔Usb1的倒计时
    public int getCountDownTimeUsb1(Map<String, Object> dpsMap) {
        if (isHaveCountDownUsb1(dpsMap)) {
            String countDown = String.valueOf(dpsMap.get(getCountdown_usb1_id()));
            long countDownTime = (long) Double.parseDouble(countDown);
            return (int) countDownTime;
        }
        return 0;
    }

    //判断设备重启状态设置
    public boolean isHaveRelayStatus(Map<String, Object> dpsMap) {
        if (dpsMap == null)
            return false;
        return (dpsMap.containsKey(getRelay_status_id()));
    }

    //获取设备重启状态
    public void setRelay_status_id(String relay_status_id) {
        this.relay_status_id = relay_status_id;
    }

    public String getRelayStatusValue(Map<String, Object> dpsMap) {
        if (isHaveRelayStatus(dpsMap)) {
            return (String) dpsMap.get(getRelay_status_id());
        }
        return "";
    }

    //判断设备是否支持童锁
    public boolean isHaveChildLock(Map<String, Object> dpsMap) {
        if (dpsMap == null)
            return false;
        return (dpsMap.containsKey(getChild_lock_id()));
    }

    //获取童锁的值
    public boolean getChildLockValue(Map<String, Object> dpsMap) {
        if (isHaveChildLock(dpsMap)) {
            return (boolean) dpsMap.get(getChild_lock_id());
        }
        return false;
    }

    //通过dp获取相应插孔的倒计时时间
    public int getCountDownTimeByDpId(String dpId, Map<String, Object> dpsMap) {
        if (dpsMap == null)
            return 0;
        if (dpId.equals(getSwitch_1_id()) && isHaveCountDown1(dpsMap)) {
            return getCountDownTime1(dpsMap);
        } else if (dpId.equals(getSwitch_2_id()) && isHaveCountDown2(dpsMap)) {
            return getCountDownTime2(dpsMap);
        } else if (dpId.equals(getSwitch_3_id()) && isHaveCountDown3(dpsMap)) {
            return getCountDownTime3(dpsMap);
        } else if (dpId.equals(getSwitch_4_id()) && isHaveCountDown4(dpsMap)) {
            return getCountDownTime4(dpsMap);
        } else if (dpId.equals(getSwitch_usb1_id()) && isHaveCountDownUsb1(dpsMap)) {
            return getCountDownTimeUsb1(dpsMap);
        }
        return 0;
    }

    //通过dpId获取相应的定时时间（循环定时或随机定时）
    public List<String> getFilterScheduleTime(String mDpId, List<String> scheduleList) {
        List<String> filterScheduleTime = new ArrayList<>();
        if (mDpId.equals(getSwitch_1_id())) {
            for (String cycleTime : scheduleList) {
                if (cycleTime.substring(SWITCH_BEGIN_INDEX, SWITCH_END_INDEX).equals(ConstantValue.SCHEDULE_SWITCH1_OFF) || cycleTime.substring(SWITCH_BEGIN_INDEX, SWITCH_END_INDEX).equals(ConstantValue.SCHEDULE_SWITCH1_ON)) {
                    filterScheduleTime.add(cycleTime);
                }
            }
        } else if (mDpId.equals(getSwitch_2_id())) {
            for (String cycleTime : scheduleList) {
                if (cycleTime.substring(SWITCH_BEGIN_INDEX, SWITCH_END_INDEX).equals(ConstantValue.SCHEDULE_SWITCH2_OFF) || cycleTime.substring(SWITCH_BEGIN_INDEX, SWITCH_END_INDEX).equals(ConstantValue.SCHEDULE_SWITCH2_ON)) {
                    filterScheduleTime.add(cycleTime);
                }
            }
        } else if (mDpId.equals(getSwitch_3_id())) {
            for (String cycleTime : scheduleList) {
                if (cycleTime.substring(SWITCH_BEGIN_INDEX, SWITCH_END_INDEX).equals(ConstantValue.SCHEDULE_SWITCH3_OFF) || cycleTime.substring(SWITCH_BEGIN_INDEX, SWITCH_END_INDEX).equals(ConstantValue.SCHEDULE_SWITCH3_ON)) {
                    filterScheduleTime.add(cycleTime);
                }
            }
        } else if (mDpId.equals(getSwitch_4_id())) {
            for (String cycleTime : scheduleList) {
                if (cycleTime.substring(SWITCH_BEGIN_INDEX, SWITCH_END_INDEX).equals(ConstantValue.SCHEDULE_SWITCH4_OFF) || cycleTime.substring(SWITCH_BEGIN_INDEX, SWITCH_END_INDEX).equals(ConstantValue.SCHEDULE_SWITCH4_ON)) {
                    filterScheduleTime.add(cycleTime);
                }
            }
        } else if (mDpId.equals(getSwitch_usb1_id())) {
            for (String cycleTime : scheduleList) {
                if (cycleTime.substring(SWITCH_BEGIN_INDEX, SWITCH_END_INDEX).equals(ConstantValue.SCHEDULE_SWITCH_USB1_OFF) || cycleTime.substring(SWITCH_BEGIN_INDEX, SWITCH_END_INDEX).equals(ConstantValue.SCHEDULE_SWITCH_USB1_ON)) {
                    filterScheduleTime.add(cycleTime);
                }
            }
        }
        return filterScheduleTime;
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

    //将dpStr转化为相应map
    public Map<String, Object> convertDps(String dps) {
        if (!TextUtils.isEmpty(dps)) {
            Gson gson = new Gson();
            Map<String, Object> dpsMap = gson.fromJson(dps, new TypeToken<Map<String, Object>>() {
            }.getType());
            return dpsMap;
        }
        return null;
    }

    //ScheduleActivity
    public static List<PowerStripScheduleBean> convertMixScheduleBean(List<Timer> timerList) {
        List<PowerStripScheduleBean> mixScheduleBeanList = new ArrayList<>();
        if (CollectionUtil.isEmpty(timerList)) {
            return mixScheduleBeanList;
        }
        for (Timer timerBeam : CollectionUtil.safeFor(timerList)) {
            PowerStripScheduleBean mixScheduleBean = new PowerStripScheduleBean(timerBeam);
            mixScheduleBeanList.add(mixScheduleBean);
        }
        return mixScheduleBeanList;
    }

    public List<PowerStripScheduleBean> convertMixScheduleBean(List<Schedule> scheduleList, List<Timer> timerList) {
        List<PowerStripScheduleBean> mixScheduleBeanList = new ArrayList<>();
        if (CollectionUtil.isEmpty(scheduleList) && CollectionUtil.isEmpty(timerList)) {
            return mixScheduleBeanList;
        }
        for (Timer timerBeam : CollectionUtil.safeFor(timerList)) {
            PowerStripScheduleBean mixScheduleBean = new PowerStripScheduleBean(timerBeam);
            mixScheduleBeanList.add(mixScheduleBean);
        }
        for (Schedule scheduleBean : CollectionUtil.safeFor(scheduleList)) {
            PowerStripScheduleBean mixScheduleBean = new PowerStripScheduleBean(scheduleBean);
            mixScheduleBeanList.add(mixScheduleBean);
        }
        return mixScheduleBeanList;
    }

    //判断是否是开
    public boolean isOpen(String isOpen) {
        if (isOpen.equals(ConstantValue.SCHEDULE_SWITCH1_ON) || isOpen.equals(ConstantValue.SCHEDULE_SWITCH2_ON) || isOpen.equals(ConstantValue.SCHEDULE_SWITCH3_ON)
                || isOpen.equals(ConstantValue.SCHEDULE_SWITCH4_ON) || isOpen.equals(ConstantValue.SCHEDULE_SWITCH_USB1_ON)) {
            return true;
        } else if (isOpen.equals(ConstantValue.SCHEDULE_SWITCH1_OFF) || isOpen.equals(ConstantValue.SCHEDULE_SWITCH2_OFF) ||
                isOpen.equals(ConstantValue.SCHEDULE_SWITCH3_OFF) || isOpen.equals(ConstantValue.SCHEDULE_SWITCH4_OFF) || isOpen.equals(ConstantValue.SCHEDULE_SWITCH_USB1_OFF)) {
            return false;
        }
        return false;
    }

    //根据dpId获取默认插孔的命名
    public String getDefalutNameByDpId(String dpId) {
        if (dpId.equals(getSwitch_1_id())) {//第一个插孔
            return ConstantValue.POWERSTRIP_PLUG_DEFAULT_NAME_1;
        } else if (dpId.equals(getSwitch_2_id())) {
            return ConstantValue.POWERSTRIP_PLUG_DEFAULT_NAME_2;
        } else if (dpId.equals(getSwitch_3_id())) {
            return ConstantValue.POWERSTRIP_PLUG_DEFAULT_NAME_3;
        } else if (dpId.equals(getSwitch_4_id())) {
            return ConstantValue.POWERSTRIP_PLUG_DEFAULT_NAME_4;
        } else if (dpId.equals(getSwitch_usb1_id())) {
            return ConstantValue.POWERSTRIP_PLUG_DEFAULT_NAME_USB1;
        }
        return "";
    }

    //判断灯是否开关
    public boolean isLampOpen(Map<String, Object> dpsMap) {
        if (dpsMap == null)
            return false;
        if (dpsMap.containsKey(getSwitch_led_id())) {
            return (boolean) dpsMap.get(getSwitch_led_id());
        }
        if (dpsMap.containsKey(getLed_switch_id())) {
            return (boolean) dpsMap.get(getLed_switch_id());
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

    //新dp发送  根据开关颜色的十六进制字符串获得相应的hsv
    public float[] getHsvByColorHexStr(String colorHexStr) {
        float[] hsv = new float[3];
        if (TextUtils.isEmpty(colorHexStr) || colorHexStr.length() < 12) {
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
            return hsv;
        }

        hsv[0] = Long.parseLong(colorHexStr.substring(6, 10), 16);
        hsv[1] = Long.parseLong(colorHexStr.substring(10, 12), 16) / 255f;
        hsv[2] = Long.parseLong(colorHexStr.substring(12, 14), 16) / 255f;
        return hsv;
    }

    public int getColorByColorHexStr(String colorHexStr) {
        float[] hsv = getHsvByColorHexStr(colorHexStr);
        return Color.HSVToColor(hsv);
    }

    public int getBrightColorByColorHexStr(String colorHexStr, DeviceBean deviceBean) {
        float[] hsv;
        if (isOldDPLamp(deviceBean)) {
            hsv = getOldHsvByColorHexStr(colorHexStr);
        } else {
            hsv = getHsvByColorHexStr(colorHexStr);
        }
        hsv[2] = 1f;
        return Color.HSVToColor(hsv);
    }

    //根据hsv得到progressBar的颜色
    public int getColorByHSV(float[] colorHsv) {
        float[] hsv = new float[3];
        hsv[0] = colorHsv[0];
        hsv[1] = colorHsv[1];
        hsv[2] = 1f;
        return Color.HSVToColor(hsv);
    }

    //获取智能灯模式 white colour scene music
    public String getLampWorkMode(Map<String, Object> dpsMap) {
        if (dpsMap == null) {
            return "";
        }
        if (dpsMap.containsKey(getWork_mode_id())) {
            return (String) dpsMap.get(getWork_mode_id());
        }
        return "";
    }

    //判断是不是工作模式中的scene模式
    public boolean isSceneMode(Map<String, Object> dpsMap) {
        if (dpsMap == null) {
            return false;
        }
        if (PowerStripHelper.getInstance().getLampWorkMode(dpsMap).equals(ConstantValue.LAMP_WORK_MODE_SCENE)
                || PowerStripHelper.getInstance().getLampWorkMode(dpsMap).equals(ConstantValue.LAMP_WORK_MODE_SCENE_ONE)
                || PowerStripHelper.getInstance().getLampWorkMode(dpsMap).equals(ConstantValue.LAMP_WORK_MODE_SCENE_TWO)
                || PowerStripHelper.getInstance().getLampWorkMode(dpsMap).equals(ConstantValue.LAMP_WORK_MODE_SCENE_THREE)
                || PowerStripHelper.getInstance().getLampWorkMode(dpsMap).equals(ConstantValue.LAMP_WORK_MODE_SCENE_FOUR)) {
            return true;
        }
        return false;
    }

    public String getLampWorkMode(DeviceBean deviceBean) {

        if (deviceBean == null) {
            return "";
        }

        Map<String, Object> dpsMap = deviceBean.getDps();

        if (dpsMap == null) {
            return "";
        }
        if (dpsMap.containsKey(getWork_mode_id())) {
            return (String) dpsMap.get(getWork_mode_id());
        }
        return "";
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

    public String getLampThemeValue(DeviceBean deviceBean) {
        if (deviceBean == null || deviceBean.getDps() == null)
            return "";
        if (deviceBean.getDps().containsKey(getScene_data_id())) {
            return (String) deviceBean.getDps().get(getScene_data_id());
        }

        return "";

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

    public String getThemeHexByHVS(float[] hsv) {
        String h = Integer.toHexString((int) hsv[0]);
        String s = Integer.toHexString((int) (hsv[1] * 1000));
        String v = Integer.toHexString((int) (hsv[2] * 1000));
        /**
         * 这里返回的是没有前边位置的自定义的值
         */
        return "0e0d00" + addZeroForNum(h, 4) + addZeroForNum(s, 4) + addZeroForNum(v, 4) + "00000000";
    }


    //新dp值发送 根据颜色的hsv获取彩色的指令 dpId：24
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

    //灯逐渐变色 28 彩色模式使用
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

    public String getSWITCH_1() {
        return SWITCH_1;
    }

    public String getSWITCH_2() {
        return SWITCH_2;
    }

    public String getSWITCH_3() {
        return SWITCH_3;
    }

    public String getSWITCH_4() {
        return SWITCH_4;
    }

    public String getSWITCH_USB1() {
        return SWITCH_USB1;
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

    public String getRelay_status_id() {
        return relay_status_id;
    }

    public String getSwitch_all_id() {
        return switch_all_id;
    }

    public void setSwitch_all_id(String switch_all_id) {
        this.switch_all_id = switch_all_id;
    }

    public String getCountdown_1_id() {
        return countdown_1_id;
    }

    public void setCountdown_1_id(String countdown_1_id) {
        this.countdown_1_id = countdown_1_id;
    }

    public String getCountdown_2_id() {
        return countdown_2_id;
    }

    public void setCountdown_2_id(String countdown_2_id) {
        this.countdown_2_id = countdown_2_id;
    }

    public String getCountdown_3_id() {
        return countdown_3_id;
    }

    public void setCountdown_3_id(String countdown_3_id) {
        this.countdown_3_id = countdown_3_id;
    }

    public String getCountdown_4_id() {
        return countdown_4_id;
    }

    public void setCountdown_4_id(String countdown_4_id) {
        this.countdown_4_id = countdown_4_id;
    }

    public String getCountdown_usb1_id() {
        return countdown_usb1_id;
    }

    public void setCountdown_usb1_id(String countdown_usb1_id) {
        this.countdown_usb1_id = countdown_usb1_id;
    }

    public String getChild_lock_id() {
        return child_lock_id;
    }

    public void setChild_lock_id(String child_lock_id) {
        this.child_lock_id = child_lock_id;
    }

    public String getCycle_time_id() {
        return cycle_time_id;
    }

    public void setCycle_time_id(String cycle_time_id) {
        this.cycle_time_id = cycle_time_id;
    }

    public String getRandom_time_id() {
        return random_time_id;
    }

    public void setRandom_time_id(String random_time_id) {
        this.random_time_id = random_time_id;
    }

    public String getNormal_time_id() {
        return normal_time_id;
    }

    public void setNormal_time_id(String normal_time_id) {
        this.normal_time_id = normal_time_id;
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


    public String getCountdown_id() {
        return countdown_id;
    }

    public void setCountdown_id(String countdown_id) {
        this.countdown_id = countdown_id;
    }

    public String getControl_data_id() {
        return control_data_id;
    }

    public void setControl_data_id(String control_data_id) {
        this.control_data_id = control_data_id;
    }

    public String getSwitch_inching_id() {
        return switch_inching_id;
    }

    public void setSwitch_inching_id(String switch_inching_id) {
        this.switch_inching_id = switch_inching_id;
    }

    /* public String getWhite_color_id() {
        return white_color_id;
    }

    public void setWhite_color_id(String white_color_id) {
        this.white_color_id = white_color_id;
    }

    public String getFull_color_id() {
        return full_color_id;
    }

    public void setFull_color_id(String full_color_id) {
        this.full_color_id = full_color_id;
    }

    public String getWakeup_mode_id() {
        return wakeup_mode_id;
    }

    public void setWakeup_mode_id(String wakeup_mode_id) {
        this.wakeup_mode_id = wakeup_mode_id;
    }

    public String getSleep_mode_id() {
        return sleep_mode_id;
    }

    public void setSleep_mode_id(String sleep_mode_id) {
        this.sleep_mode_id = sleep_mode_id;
    }*/

    public String getColour_gradient_id() {
        return colour_gradient_id;
    }

    public void setColour_gradient_id(String colour_gradient_id) {
        this.colour_gradient_id = colour_gradient_id;
    }

    public String getSwitch_overcharge_id() {
        return switch_overcharge_id;
    }

    public void setSwitch_overcharge_id(String switch_overcharge_id) {
        this.switch_overcharge_id = switch_overcharge_id;
    }

    public boolean checkTuyaProductIdValid(String productId) {
        return CollectionUtil.isNotEmpty(tuyaList) && tuyaList.contains(productId);
    }

    public boolean checkThreeHolesDeviceValid(String productId) {
        return CollectionUtil.isNotEmpty(threeHolesPowerStripWallSwitchList) && threeHolesPowerStripWallSwitchList.contains(productId);
    }

    public boolean checkFiveHolesDeviceValid(String productId) {
        return productId.equals(ConstantValue.SMART_STRIP_PRODUCTID_FOUR);
    }

    public boolean checkDimmerDeviceValid(String productId) {
        return CollectionUtil.isNotEmpty(dimmerPlugList) && dimmerPlugList.contains(productId);
    }

    public boolean checkNormalPowerStripValid(String productId) {
        return CollectionUtil.isNotEmpty(powerStripMultiWallList) && powerStripMultiWallList.contains(productId);
    }

    public boolean checkOldLampValid(String productId) {
        return CollectionUtil.isNotEmpty(oldLampList) && oldLampList.contains(productId);
    }

    public boolean checkNewLampValid(String productId) {
        return CollectionUtil.isNotEmpty(lampList) && lampList.contains(productId);
    }

    public boolean checkNormalSwitchValid(String productId) {
        return CollectionUtil.isNotEmpty(normalSwitchList) && normalSwitchList.contains(productId);
    }

    public boolean checkPetFeederValid(String productId) {
        return productId.equals(ConstantValue.SMART_FEEDER_PRODUCTID);
    }

    public boolean checkAirPurifierValid(String productId) {
        return productId.equals(ConstantValue.SMART_AIR_PURIFIER_PRODUCTID);
    }

    List<String> tuyaList = Arrays.asList(
            "octeoqhuayzof69q",
            "iqgfsxokdkzzehmj",
            "4bVOiYN0zdh6vTYq",
            "dok3rzi3pnnqu6ju",
            "viv1giuyu2tk4kt4",
            "cya3zxfd38g4qp8d",
            "5bvnmoqjth5nd4de",
            "sOhGq6u1M2JwB5d8",
            "twezq8g8ykoaggey",
            "isehgkqn5uqlrorl",
            "fnxgcsysunpyxkou",
            "ttrn0dlxota0rrav",
            "hdnoe1sqimwad9f4",
            "gswrpjab2vfawful",
            "5abhrka6ejfr0hvx",
            "fbvia0apnlnattcy",
            "vnya2spfopsh9lro",
            "01wjigkru2tgixxp",
            "omwxkdvwpxtyjans",
            "EQD8hAQw543vzh6O",
            "iGuAESc6917owGUr",
            "j7ewsefbjxaprlqy",
            "pJnpT0XcM5FTRjOd",
            "bYdRrWx5iLCyAfPs",
            "nscnnpeguv620u4f",
            "1ogqu4bxwzrjxu8v",
            "gmjdt6mvy1mntvqn",
            "g56afofns8lpko6v",
            "behqxmx1m8e4sr08",
            "b6vjkghax6amtwdf",
            "A6bBfm2fmKKRfIxU",
            "8jkyyvxsep3yr5ql",
            "ycccdik7krsxuybg",
            "J4b9HONUUjrBxJXK",
            "5at5rg1h1viwp6ol",
            "z6ai9dh9a0aujfdy",
            "sfurldd0ddoc2mat",
            "f0o1eyjw1pfojq87",
            "99oomugbqvd1axj0",
            "ga8gcmwpkl3jc4ek",
            "ep0rgcsdmq4sp6cd",
            "rk9wwke99mdncz2n",
            "qtbtyifjcm3ou6f1",
            "zae4ua68xt9wxfap",
            "pahtehvb1nisjsa4",
            "ohmqju5mfrmjktsu",
            "c2uurdygilpx7nko",
            "s3a7fbytlm1gprhz",
            "ee2nlhpqplatlcoh",
            "8vbbgyn8uosnntvf",
            "rcaduvje2rrpoe9d",
            "ugdkg6rn63peraai",
            "nkd4cs6u1vfu9ksi",
            "rofiypesat1paym2",
            "xyfjwup7nwnsqak7",
            "trtvoqie5as0bfpo",
            "xmpy4utews9k3waf",
            "xajto1x7xm4w3x0s",
            "fsxtzzhujkrak2oy",
            "infi2mnk4bjxhjtc",
            "evmunsts6htgolmm",
            "q6rtbvpccl5jvkjf",
            "6aheu8de32k7ruvq",
            "0f76bavw2lp9yu5e",
            "7n504uls2twfik2b"
    );

    List<String> normalSwitchList = Arrays.asList(
            "octeoqhuayzof69q",
            "iqgfsxokdkzzehmj",
            "4bVOiYN0zdh6vTYq",
            "dok3rzi3pnnqu6ju",
            "viv1giuyu2tk4kt4",
            "cya3zxfd38g4qp8d",
            "5bvnmoqjth5nd4de",
            "sOhGq6u1M2JwB5d8",
            "twezq8g8ykoaggey",
            "fbvia0apnlnattcy",
            "vnya2spfopsh9lro",
            "pJnpT0XcM5FTRjOd",
            "bYdRrWx5iLCyAfPs",
            "A6bBfm2fmKKRfIxU",
            "8jkyyvxsep3yr5ql",
            "z6ai9dh9a0aujfdy",
            "sfurldd0ddoc2mat",
            "f0o1eyjw1pfojq87",
            "99oomugbqvd1axj0",
            "ga8gcmwpkl3jc4ek",
            "ep0rgcsdmq4sp6cd",
            "zae4ua68xt9wxfap",
            "nkd4cs6u1vfu9ksi",
            "rofiypesat1paym2",
            "xyfjwup7nwnsqak7",
            "xmpy4utews9k3waf",
            "xajto1x7xm4w3x0s",
            "infi2mnk4bjxhjtc",
            "evmunsts6htgolmm",
            "q6rtbvpccl5jvkjf",
            "6aheu8de32k7ruvq",
            "0f76bavw2lp9yu5e",
            "7n504uls2twfik2b"
    );

    List<String> powerStripMultiWallList = Arrays.asList(
            "01wjigkru2tgixxp",
            "omwxkdvwpxtyjans",
            "EQD8hAQw543vzh6O",
            "J4b9HONUUjrBxJXK"
    );

    List<String> threeHolesPowerStripWallSwitchList = Arrays.asList(
            "iGuAESc6917owGUr",
            "ugdkg6rn63peraai",
            "ycccdik7krsxuybg"
    );

    List<String> fiveHolesPowerStripWallSwitchList = Arrays.asList(
            "j7ewsefbjxaprlqy"
    );

    List<String> dimmerPlugList = Arrays.asList(
            "rk9wwke99mdncz2n",
            "qtbtyifjcm3ou6f1"
    );

    List<String> lampList = Arrays.asList(
            "hdnoe1sqimwad9f4",
            "gswrpjab2vfawful",
            "5abhrka6ejfr0hvx",
            "5at5rg1h1viwp6ol",
            "gmjdt6mvy1mntvqn",
            "g56afofns8lpko6v",
            "behqxmx1m8e4sr08",
            "b6vjkghax6amtwdf",
            "1ogqu4bxwzrjxu8v",
            "c2uurdygilpx7nko",
            "s3a7fbytlm1gprhz",
            "ee2nlhpqplatlcoh",
            "8vbbgyn8uosnntvf",
            "rcaduvje2rrpoe9d",
            "pahtehvb1nisjsa4",
            "ohmqju5mfrmjktsu",
            "trtvoqie5as0bfpo"
    );

    List<String> oldLampList = Arrays.asList(
            "isehgkqn5uqlrorl",
            "fnxgcsysunpyxkou",
            "ttrn0dlxota0rrav",
            "nscnnpeguv620u4f"
    );

}
