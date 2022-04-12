package com.afar.osaio.protocol.bean;

/**
 * Created by victor on 2018/7/31
 * Email is victor.qiao.0604@gmail.com
 */
public class Constant {
    // action
    public static final int CMD_GET = 0x01;
    public static final int CMD_SET = 0x02;

    // key
    public static final int KEY_LED = 0x01;                         // 0x01(ON) | 0x00(OFF)
    public static final int KEY_RECORD_WITH_AUDIO = 0x02;           // 0x01(ON) | 0x00(OFF)
    public static final int KEY_NOTIFICATION_WEEK_PLAN = 0x03;      // time11 + time12 + ... + time71+time72
    public static final int KEY_NOTIFICATION_MON_PLAN = 0x04;       // time1 + time2
    public static final int KEY_NOTIFICATION_TUES_PLAN = 0x05;      // time1 + time2
    public static final int KEY_NOTIFICATION_WED_PLAN = 0x06;       // time1 + time2
    public static final int KEY_NOTIFICATION_THUR_PLAN = 0x07;      // time1 + time2
    public static final int KEY_NOTIFICATION_FRI_PLAN = 0x08;       // time1 + time2
    public static final int KEY_NOTIFICATION_SAT_PLAN = 0x09;       // time1 + time2
    public static final int KEY_NOTIFICATION_SUN_PLAN = 0x0A;       // time1 + time2
    public static final int KEY_NOTIFICATION_MON_SWITCH = 0x0B;     // 0x01(ON) | 0x00(OFF)
    public static final int KEY_NOTIFICATION_TUES_SWITCH = 0x0C;    // 0x01(ON) | 0x00(OFF)
    public static final int KEY_NOTIFICATION_WED_SWITCH = 0x0D;     // 0x01(ON) | 0x00(OFF)
    public static final int KEY_NOTIFICATION_THUR_SWITCH = 0x0E;    // 0x01(ON) | 0x00(OFF)
    public static final int KEY_NOTIFICATION_FRI_SWITCH = 0x0F;     // 0x01(ON) | 0x00(OFF)
    public static final int KEY_NOTIFICATION_SAT_SWITCH = 0x10;     // 0x01(ON) | 0x00(OFF)
    public static final int KEY_NOTIFICATION_SUN_SWITCH = 0x11;     // 0x01(ON) | 0x00(OFF)
    public static final int KEY_LOOP_RECORD = 0x12;                 // 0x01(ON)| 0x00(OFF)
    public static final int KEY_MODEL = 0x13;                       // IPC007-720P
    public static final int KEY_RECENT_SDCARD_RECORDS = 0x14;       // 0 -> no records; !0 -> have records
    public static final int KEY_SLEEP_STATE = 0x15;                 // 0x01(ON) | 0x00(OFF)
    public static final int KEY_MOTION_TRACKING = 0x16;             // 0x01(ON) | 0x00(OFF)

    //sound key
    public static final int KEY_SUD_DETECT_WEEK_PLAN = 0x17;       //time11 + time12 + ... + time71 + time72
    public static final int KEY_SUD_DETECT_PLAN_MON = 0x18;        //time1 + time2
    public static final int KEY_SUD_DETECT_PLAN_TUES = 0x19;       //time1 + time2
    public static final int KEY_SUD_DETECT_PLAN_WED = 0x1A;        //time1 + time2
    public static final int KEY_SUD_DETECT_PLAN_THUR = 0x1B;       //time1 + time2
    public static final int KEY_SUD_DETECT_PLAN_FRI = 0x1C;        //time1 + time2
    public static final int KEY_SUD_DETECT_PLAN_SAT = 0x1D;        //time1 + time2
    public static final int KEY_SUD_DETECT_PLAN_SUN = 0x1E;        //time1 + time2

    public static final int KEY_SUD_DETECT_PLAN_MON_SWITCH = 0x1F;     //0x00(OFF) | 0x01(ON)
    public static final int KEY_SUD_DETECT_PLAN_TUES_SWITCH = 0x20;    //0x00(OFF) | 0x01(ON)
    public static final int KEY_SUD_DETECT_PLAN_WED_SWITCH = 0x21;     //0x00(OFF) | 0x01(ON)
    public static final int KEY_SUD_DETECT_PLAN_THUR_SWITCH = 0x22;    //0x00(OFF) | 0x01(ON)
    public static final int KEY_SUD_DETECT_PLAN_FRI_SWITCH = 0x23;     //0x00(OFF) | 0x01(ON)
    public static final int KEY_SUD_DETECT_PLAN_SAT_SWITCH = 0x24;     //0x00(OFF) | 0x01(ON)
    public static final int KEY_SUD_DETECT_PLAN_SUN_SWITCH = 0x25;     //0x00(OFF) | 0x01(ON)

    //reset
    public static final int KEY_CAMERA_FACTORY_RESET = 0x26;
    //nooie update
    public static final int KEY_CAMERA_UPDATE_NOOIE = 0x27;    //char[16] uid + char[6] timezone ([+|-]xx.yy)
    //device alarm
    public static final int KEY_CAMERA_ALRAM_AUDIO = 0x28;     // 0x01(ON) | 0x00(OFF) + (int)id + (int)time(s) + (int)num(s)

    // code
    public static final int CODE_OK = 0x00;
    public static final int CODE_ERROR = 0x01;

    // value
    public static final byte VALUE_ON = 0x01;
    public static final byte VALUE_OFF = 0x00;

    // common
    public static final int LEN_BYTES_CODE = 1;
    public static final int LEN_BYTES_LEN = 1;
    public static final int LEN_BYTES_ACTION = 1;
    public static final int LEN_BYTES_KEY = 1;
    public static final int LEN_BYTES_TIME = 2;
}
