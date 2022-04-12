package com.afar.osaio.util;

import android.Manifest;

/**
 * Created by victor on 2018/6/26
 * Email is victor.qiao.0604@gmail.com
 */
public class ConstantValue {
    public static final String SUCCESS = "SUCCESS";
    public static final String ERROR = "ERROR";
    public static final int STATE_SUCCESS = 1;
    public static final int STATE_ERROR = 0;
    //涂鸦配网模式
    public static final int AP_MODE = 0;
    public static final int EC_MODE = 1;
    public static final int BLUE_MODE = 2;

    public static final int MEMBER_OWNER = 0;
    public static final int MEMBER_GUEST = 1;
    public static final int GROUP_CREATE = 0;
    public static final int GROUP_RENAME = 1;

    public static final String APP_TAG_PREFIX = "Osaio";

    //app account brand
    public static final String APP_ACCOUNT_BRAND_CURRENT = "osaio";
    public static final String APP_ACCOUNT_BRAND_VICTURE = "victure";
    public static final String APP_ACCOUNT_BRAND_TECKIN = "teckin";
    public static final String APP_NAME_OF_BRAND_VICTURE = "Victure Home";
    public static final String APP_NAME_OF_BRAND_TECKIN = "Teckin Home";

    //Configure
    public static final String UMENG_APP_KEY = "61237f7fe0080c5063767777";
    public static final String UMENG_APP_SECRET = "2a39e8b12b59ef13e5920a91a51e44ba";

    public static final String PUSH_FCM_TUYA_PUSHPROVIDER = "fcm";
    public static final String PUSH_UMENG_TUYA_PUSHPROVIDER = "umeng";

    // Url
    public static final String URL_USER_MANUAL = "https://www.nooie.com/app-user-manual-%s";
    public static final String URL_CONDITION_OF_USE = "https://www.govicture.com/privacy-policy-%s";
    public static final String URL_PRIVACY_NOTICE = "https://www.govicture.com/blank-1";
    public static final String URL_FAQ = "https://www.nooie.com/app-faq-%s";
    public static final String URL_USER_MANUAL_VICTURE = "http://www.nooie.com/app-user-manual-720";
    public static final String AMAZON_VICTURE_IPC = "https://www.amazon.com/-/zh/dp/B07MG46PTD/ref=sr_1_2?__mk_zh_CN=%E4%BA%9A%E9%A9%AC%E9%80%8A%E7%BD%91%E7%AB%99&dchild=1&keywords=victure+baby+monitor&qid=1585105258&sr=8-2";
    public static final String AMAZON_VICTURE_GATEWAY = "https://www.amazon.com/dp/B085TLC7CS?ref=myi_title_dp";
    public static final String URL_PRIVACY_POLICY = "https://osaio.net/privacy-policy-%s";
    public static final String URL_TERMS = "https://osaio.net/terms-%s";
    public static final String URL_PRIVACY_POLICY_TEMPLATE = "https://osaio.net/privacy-policy-%s-%s";

    // email
    public static final String EMAIL_TO = "noo@apemans.com";
    //public static final String EMAIL_TO = "qiaofei@apemans.com";
    public static final String EMAIL_HOST = "smtp.sina.com";
    public static final String EMAIL_ACCOUNT = "apemans@sina.com";
    public static final String EMAIL_PASSWORD = "apemans";
    public static final String EMAIL_PORT = "465";
    public static final String EMAIL_SENDER_NAME = "android";
    public static final String EMAIL_FEEDBACK = "feedback";

    // Broadcast
    public static final String BROADCAST_KEY_REMOVE_CAMERA = "BROADCAST.KEY.REMOVE.CAMERA";
    public static final String BROADCAST_KEY_UPDATE_CAMERA = "BROADCAST.KEY.UPDATE.CAMERA";
    public static final String BROADCAST_KEY_RECEIVE_PUSH = "BROADCAST_KEY_RECEIVE_PUSH";
    public static final String BROADCAST_KEY_RECEIVE_JG_PUSH = "BROADCAST_KEY_RECEIVE_JG_PUSH";
    public static final String BROADCAST_KEY_RECEIVE_FCM_PUSH = "BROADCAST_KEY_RECEIVE_FCM_PUSH";
    public static final String BROADCAST_KEY_RECEIVE_SHARE_AGREE = "BROADCAST_KEY_RECEIVE_SHARE_AGREE";
    public static final String BROADCAST_KEY_RECEIVE_UPDATE_SHARE_DATA = "BROADCAST_KEY_RECEIVE_UPDATE_SHARE_DATA";
    public static final String BROADCAST_KEY_NOOIE_SERVICE_STATE = "BROADCAST_KEY_NOOIE_SERVICE_STATE";

    // Intent key
    public static final String INTENT_KEY_RECEIVE_IS_SYSTEM_PUSH = "INTENT_KEY_RECEIVE_IS_SYSTEM_PUSH";
    public static final String INTENT_KEY_RECEIVE_PUSH_MSG = "INTENT_KEY_RECEIVE_PUSH_MSG";
    public static final String INTENT_KEY_COUNTRY_CODE = "INTENT_KEY_COUNTRY_CODE";
    public static final String INTENT_KEY_VERIFY_CODE = "INTENT_KEY_VERIFY_CODE";
    public static final String INTENT_KEY_NOTIFICATIONS_LEVEL = "INTENT_KEY_NOTIFICATIONS_LEVEL";
    public static final String INTENT_KEY_DEVICE_ID = "INTENT_KEY_DEVICE_ID";
    public static final String INTENT_KEY_DEVICE_PRODUCTID = "INTENT_KEY_DEVICE_PRODUCTID";
    public static final String INTENT_KEY_SHARE_DEVICE_INFO = "INTENT_KEY_SHARE_DEVICE_INFO";
    public static final String INTENT_KEY_NICK_NAME = "INTENT_KEY_NICK_NAME";
    public static final String INTENT_KEY_AVATAR = "INTENT_KEY_AVATAR";
    public static final String INTENT_KEY_DATA_TYPE = "INTENT_KEY_DATA_TYPE";
    public static final String INTENT_NOLINE_DEVICE_LIST = "INTENT_NOLINE_DEVICE_LIST";
    public static final String INTENT_KEY_PUSH_MSG = "INTENT_KEY_PUSH_MSG";
    public static final String INTENT_KEY_TIME_STAMP = "INTENT_KEY_TIME_STAMP";
    public static final String INTENT_KEY_SHARED_USER_LIST = "INTENT_KEY_SHARED_USER_LIST";
    public static final String INTENT_KEY_DEVICE_NAME = "INTENT_KEY_DEVICE_NAME";
    public static final String INTENT_KEY_URL = "INTENT_KEY_URL";
    public static final String INTENT_KEY_TITLE = "INTENT_KEY_TITLE";
    public static final String INTENT_KEY_ACCOUNT = "INTENT_KEY_ACCOUNT";
    public static final String INTENT_KEY_WEEK = "INTENT_KEY_WEEK";
    public static final String INTENT_KEY_START = "INTENT_KEY_START";
    public static final String INTENT_KEY_END = "INTENT_KEY_END";
    public static final String INTENT_KEY_VERIFY_TYPE = "INTENT_KEY_VERIFY_TYPE";
    public static final String INTENT_KEY_SSID = "INTENT_KEY_SSID";
    public static final String INTENT_KEY_PSD = "INTENT_KEY_PSD";
    public static final String INTENT_KEY_SSID_5 = "INTENT_KEY_SSID_5";
    public static final String INTENT_KEY_PSD_5 = "INTENT_KEY_PSD_5";
    public static final String INTENT_KEY_WIFI_INFO = "INTENT_KEY_WIFI_INFO";
    public static final String INTENT_KEY_SKIP_QR_CODE_CONFIG_NETWORK = "INTENT_KEY_SKIP_QR_CODE_CONFIG_NETWORK";
    public static final String INTENT_KEY_NEED_FOUND_SDCARD_FIRST_TIME_RECORD = "INTENT_KEY_NEED_FOUND_SDCARD_FIRST_TIME_RECORD";
    public static final String INTENT_KEY_NEED_FOUND_CLOUD_FIRST_TIME_RECORD = "INTENT_KEY_NEED_FOUND_CLOUD_FIRST_TIME_RECORD";
    public static final String INTENT_KEY_IPC_MODEL = "IPC_MODEL";
    public static final String INTENT_KEY_DEVICE_PLATFORM = "INTENT_KEY_DEVICE_PLATFORM";
    public static final String INTENT_KEY_RECEIVE_JG_PUSH = "INTENT_KEY_RECEIVE_JG_PUSH";
    public static final String INTENT_KEY_RECEIVE_FCM_PUSH = "INTENT_KEY_RECEIVE_FCM_PUSH";
    public static final String INTENT_KEY_DEVICE_IP = "INTENT_KEY_DEVICE_IP";
    public static final String INTENT_KEY_DEVICE_PORT = "INTENT_KEY_DEVICE_PORT";
    public static final String INTENT_KEY_PHONE_CODE = "INTENT_KEY_PHONE_CODE";
    public static final String INTENT_KEY_COUNTRY_NAME = "INTENT_KEY_COUNTRY_NAME";
    public static final String INTENT_KEY_COUNTRY_KEY = "INTENT_KEY_COUNTRY_KEY";
    public static final String INTENT_KEY_DATA_ID = "INTENT_KEY_DATA_ID";
    public static final String INTENT_KEY_DATA_PARAM = "INTENT_KEY_DATA_PARAM";
    public static final String INTENT_KEY_DATA_PARAM_1 = "INTENT_KEY_DATA_PARAM_1";
    public static final String INTENT_KEY_DATA_PARAM_2 = "INTENT_KEY_DATA_PARAM_2";
    public static final String INTENT_KEY_DATA_PARAM_3 = "INTENT_KEY_DATA_PARAM_3";
    public static final String INTENT_KEY_DATA_PARAM_4 = "INTENT_KEY_DATA_PARAM_4";
    public static final String INTENT_KEY_ROUTE_SOURCE = "INTENT_KEY_ROUTE_SOURCE";
    public static final String INTENT_KEY_IS_ADMIN = "INTENT_KEY_IS_ADMIN";
    public static final String INTENT_KEY_BLE_DEVICE = "INTENT_KEY_BLE_DEVICE";
    public static final String INTENT_KEY_BLE_SEC = "INTENT_KEY_BLE_SEC";
    public static final String INTENT_KEY_CONNECTION_MODE = "INTENT_KEY_CONNECTION_MODE";
    public static final String INTENT_KEY_EVENT_ID = "INTENT_KEY_EVENT_ID";
    public static final String INTENT_KEY_START_PARAM = "INTENT_KEY_START_PARAM";
    public static final String INTENT_KEY_HOME_PAGE_ACTION = "INTENT_KEY_HOME_PAGE_ACTION";
    public static final String INTENT_KEY_IS_FROM_LOGIN = "INTENT_KEY_IS_FROM_LOGIN";
    public static final String INTENT_KEY_DEVICE_ADD_TYPE = "INTENT_KEY_DEVICE_ADD_TYPE";
    public static final String INTENT_KEY_CONFIG_MODE = "INTENT_KEY_CONFIG_MODE";
    public static final String INTENT_KEY_TOKEN = "INTENT_KEY_TOKEN";
    public static final String INTENT_KEY_PRODUCT_ID = "INTENT_KEY_PRODUCT_ID";
    public static final String INTENT_KEY_GROUP_NAME_TYPE = "INTENT_KEY_GROUP_NAME_TYPE";
    public static final String INTENT_KEY_GROUP_NAME = "INTENT_KEY_GROUP_NAME";
    public static final String INTENT_KEY_ADD_HOME_ID = "INTENT_KEY_ADD_HOME_ID";
    public static final String INTENT_KEY_HOME_ID = "INTENT_KEY_HOME_ID";
    public static final String INTENT_KEY_HOME_TYPE = "INTENT_KEY_HOME_TYPE";
    public static final String INTENT_KEY_HOME_NAME = "INTENT_KEY_HOME_NAME";
    public static final String INTENT_KEY_ADD_MEMBER_CODE = "INTENT_KEY_ADD_MEMBER_CODE";
    public static final String INTENT_KEY_DEVICESIDS = "INTENT_KEY_DEVICESIDS";
    public static final String INTENT_KEY_UID = "INTENT_KEY_UID";
    public static final String INTENT_KEY_IS_ADD_GUEST = "INTENT_KEY_IS_ADD_GUEST";
    public static final String INTENT_KEY_MEMBER_ID = "INTENT_KEY_MEMBER_ID";
    public static final String INTENT_KEY_ADD_MEMBER_ID = "INTENT_KEY_ADD_MEMBER_ID";
    public static final String INTENT_KEY_MEMBER_ROLE = "INTENT_KEY_MEMBER_ROLE";
    public static final String INTENT_KEY_MINE_ROLE = "INTENT_KEY_MINE_ROLE";
    public static final String INTENT_KEY_IS_HOME_ADMIN = "INTENT_KEY_IS_HOME_ADMIN";
    public static final String INTENT_KEY_MEMBER_OPERATE = "INTENT_KEY_MEMBER_OPERATE";
    public static final String INTENT_KEY_NICK_URL = "INTENT_KEY_NICK_URL";
    public static final String REMOVE_SINGLE_DEVICE_SHARE = "single_device_share_remove";
    public static final String REMOVE_HOME_GUEST = "home_guest_remove";
    public static final String INTENT_KEY_MEMBER_EMAIL = "INTENT_KEY_MEMBER_EMAIL";
    public static final String INTENT_KEY_NAME = "INTENT_KEY_NAME";
    public static final String INTENT_KEY_NAME_TYPE = "INTENT_KEY_NAME_TYPE";
    public static final String INTENT_KEY_DEVICE_IS_SHARE = "INTENT_KEY_DEVICE_IS_SHARE";
    public static final String INTENT_KEY_DEVICE_RENAME_TYPE = "INTENT_KEY_DEVICE_RENAME_TYPE";
    public static final String INTENT_KEY_DEVICE_DP_ID = "INTENT_KEY_DEVICE_DP_ID";
    public static final String INTENT_KEY_POWER_SZTRIP_NAME_TYPE = "INTENT_KEY_POWER_SZTRIP_NAME_TYPE";
    public static final String INTENT_KEY_TASKLISTBEAN = "INTENT_KEY_TASKLISTBEAN";
    public static final String INTENT_KEY_SCENETASK = "INTENT_KEY_SCENETASK";
    public static final String INTENT_KEY_SCENECONDITION = "INTENT_KEY_SCENECONDITION";
    public static final String INTENT_KEY_CONDITIONLISTBEAN = "INTENT_KEY_CONDITIONLISTBEAN";
    public static final String INTENT_KEY_REPEAT = "INTENT_KEY_REPEAT";
    public static final String INTENT_KEY_REPEAT_VALUE = "INTENT_KEY_REPEAT_VALUE";
    public static final String INTENT_KEY_IS_TASK = "INTENT_KEY_IS_TASK";
    public static final String INTENT_KEY_IS_CONDITION = "INTENT_KEY_IS_CONDITION";
    public static final String INTENT_KEY_PLACEFACADEBEAN = "INTENT_KEY_PLACEFACADEBEAN";
    public static final String INTENT_KEY_SCENEBEAN = "INTENT_KEY_SCENEBEAN";
    public static final String INTENT_KEY_IS_EFFECT = "INTENT_KEY_IS_EFFECT";
    public static final String INTENT_KEY_EFFECT = "INTENT_KEY_EFFECT";
    public static final String INTENT_KEY_PRECONDITION = "INTENT_KEY_PRECONDITION";
    public static final String INTENT_KEY_SCENEE_ADD = "INTENT_KEY_SCENEE_ADD";
    public static final String INTENT_KEY_SCENEE_MODIFY = "INTENT_KEY_SCENEE_MODIFY";
    public static final String INTENT_KEY_SCENEE_POSITION = "INTENT_KEY_SCENEE_POSITION";
    public static final String INTENT_KEY_SCENE_EFFECT = "INTENT_KEY_SCENE_EFFECT";
    public static final String INTENT_KEY_SCENE_LOOP = "INTENT_KEY_SCENE_LOOP";
    public static final String INTENT_KEY_SCENE_PRECONDITIONEXPR = "INTENT_KEY_SCENE_PRECONDITIONEXPR";
    public static final String INTENT_KEY_BLUE_DEVICE_TYPE = "INTENT_KEY_BLUE_DEVICE_TYPE";
    public static final String INTENT_KEY_BLUE_DEVICE_UUID = "INTENT_KEY_BLUE_DEVICE_UUID";
    public static final String INTENT_KEY_BLUE_DEVICE_ADDRESS = "INTENT_KEY_BLUE_DEVICE_ADDRESS";
    public static final String INTENT_KEY_BLUE_DEVICE_MAC = "INTENT_KEY_BLUE_DEVICE_MAC";


    //nooie home 重命名设备类型
    public final static String RENAME_PLUG = "RENAME_PLUG";//重命名单插
    public final static String RENAME_LAMP = "RENAME_LAMP";//重命名智能灯
    public final static String RENAME_SWITCH = "RENAME_SWITCH";//重命名开关
    public final static String RENAME_LIGHT_STRIP = "RENAME_LIGHT_STRIP";//重命名灯带
    public final static String RENAME_MODULATOR = "RENAME_MODULATOR";//重命名调节器
    public final static String RENAME_PETFEEDER = "RENAME_PETFEEDER";//重命名宠物喂食器
    public final static String RENAME_AIRPURIFIER = "RENAME_AIRPURIFIER";//重命名空气净化器

    //排插命名
    public final static int POWER_STRIP_RENAME = 0;
    public final static int POWER_STRIP_PLUG_RENAME = 1;

    //ota
    public static final int UPGRADE_WIFI_TYPE = 0;

    // router key
    public static final String INTENT_KEY_ONLINE_MSG = "INTENT_KEY_ONLINE_MSG";
    public static final String INTENT_KEY_ONLINE_DEVIVE = "INTENT_KEY_ONLINE_DEVIVE";
    public static final String INTENT_KEY_PARETAL_CONTROL_RULE_MSG = "INTENT_KEY_PARETAL_CONTROL_RULE_MSG";
    public static final String INTENT_KEY_DEVICE_MAC = "INTENT_KEY_DEVICE_MAC";
    public static final String INTENT_KEY_DEVICE_WIFI_TYPE = "INTENT_KEY_DEVICE_WIFI_TYPE";
    public static final String INTENT_KEY_DEVICE_IS_WHITE = "INTENT_KEY_DEVICE_IS_WHITE";
    public static final String INTENT_KEY_DEVICE_ISBIND = "INTENT_KEY_DEVICE_ISBIND";
    public static final String INTENT_KEY_ROUTER_SSID = "INTENT_KEY_ROUTER_SSID";
    public static final String INTENT_KEY_ROUTER_SSID_5G = "INTENT_KEY_ROUTER_SSID_5G";
    public static final String INTENT_KEY_DEVICE_SETTING = "INTENT_KEY_DEVICE_SETTING";
    public static final String INTENT_KEY_DEVICE_RETURN_INFO = "INTENT_KEY_DEVICE_RETURN_INFO";
    public static final String INTENT_KEY_SSID_SWITCH = "INTENT_KEY_SSID_SWITCH";
    public static final String INTENT_KEY_SSID_SWITCH_2G = "INTENT_KEY_SSID_SWITCH_2G";
    public static final String INTENT_KEY_SSID_SWITCH_5G = "INTENT_KEY_SSID_SWITCH_5G";
    public static final String INTENT_KEY_DEVICE_UPGRADE_VERSION = "INTENT_KEY_DEVICE_UPGRADE_VERSION";

    //intent filter
    public static final String INTENT_FILTER_NOOIE_SYS = "osaio.message.sys";
    public static final String INTENT_FILTER_NOOIE_DEVICE = "osaio.message.device";
    public static final String INTENT_FILTER_NOOIE_HOME = "osaio.message.home";

    //common use
    public static final String MIN_SOUND_DETECT_VERSION = "2.6.30";
    public static final String MIN_SLEEP_SUPPORT_VERSION = "2.6.26";
    public static final String MIN_NOOIE_NIGHT_VISION_720 = "2.6.63";
    public static final String MIN_NOOIE_NIGHT_VISION_1080 = "2.1.65";
    public static final String MIN_NOOIE_NIGHT_VISION_100 = "1.3.57";
    public static final String MIN_NOOIE_NIGHT_VISION_200 = "2.1.50";
    public static final String MIN_NOOIE_ALARM_AUDIO_200 = "2.1.50";
    public static final String MIN_NOOIE_SYNC_TIME_720 = "2.6.75";
    public static final String MIN_NOOIE_SYNC_TIME_1080 = "2.1.86";
    public static final String MIN_NOOIE_SYNC_TIME_100 = "1.3.79";
    public static final String MIN_NOOIE_SYNC_TIME_200 = "2.1.58";
    public static final String MIN_DEVICE_REMOVE_SELF_420 = "1.1.25";
    public static final String MIN_DEVICE_REMOVE_SELF_530 = "1.1.4";
    public static final String MIN_DEVICE_REMOVE_SELF_810 = "1.0.62";
    public static final String MIN_DEVICE_REMOVE_SELF_810_HUB = "3.1.59";
    public static final String MIN_DEVICE_DETECTION_ZONE_420 = "1.1.39";
    public static final String MIN_DEVICE_DETECTION_ZONE_530 = "1.3.8";
    public static final String MIN_DEVICE_DETECTION_ZONE_530A = "5.0.0";
    public static final String MIN_DEVICE_DETECTION_ZONE_540 = "1.3.8";
    public static final String MIN_DEVICE_DETECTION_ZONE_650 = "5.0.0";
    public static final String MIN_DEVICE_DETECTION_ZONE_730 = "1.1.41";
    public static final String MIN_DEVICE_DETECTION_ZONE_730_F23 = "1.0.5";
    public static final String MIN_DEVICE_DETECTION_ZONE_120 = "5.0.0";
    public static final String MIN_DEVICE_DETECTION_ZONE_210 = "1.1.0";
    public static final String MIN_DEVICE_DETECTION_ZONE_220 = "1.0.9";
    public static final int AUDIO_STATE_ON = 1;
    public static final int AUDIO_STATE_OFF = 0;
    public static final int ALARM_AUDIO_STATE_ON = 1;
    public static final int ALARM_AUDIO_STATE_OFF = 0;

    public static final int HOME_NAME_MAX_LENGTH = 25;
    public static final int MAX_NAME_LENGTH = 50;
    public static final int MIN_CODE_LENGTH = 6;
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_PASSWORD_LENGTH = 20;

    public static final int NOOIE_MSG_TYPE_SYS = 1;
    public static final int NOOIE_MSG_TYPE_DEVICE = 2;

    public static final String NOOIE_INTENT_KEY_DETECT_TYPE = "NOOIE_INTENT_KEY_DETECT_TYPE";
    public static final int NOOIE_DETECT_TYPE_MOTION = 0;
    public static final int NOOIE_DETECT_TYPE_SOUND = 1;
    public static final int DETECT_TYPE_PIR = 2;
    public static final int CAM_SETTING_TYPE_NORMAL = 0;
    public static final int CAM_SETTING_TYPE_DEVICE_OFFLINE = 1;
    public static final int CAM_INFO_TYPE_NORMAL = 0;
    public static final int CAM_INFO_TYPE_DIRECT = 1;

    public static final int NOOIE_SD_STATUS_NORMAL = 0;
    public static final int NOOIE_SD_STATUS_FORMATING = 1;
    public static final int NOOIE_SD_STATUS_NO_SD = 2;
    public static final int NOOIE_SD_STATUS_DAMAGE = 3;
    public static final int NOOIE_SD_STATUS_MOUNTING = 4;

    public static final int HUB_SD_STATUS_NORMAL = 0;
    public static final int HUB_SD_STATUS_FORMATING = 1;
    public static final int HUB_SD_STATUS_NO_SD = 2;
    public static final int HUB_SD_STATUS_DAMAGE = 3;

    public static final int NOOIE_PLAYBACK_TYPE_LIVE = 0;
    public static final int NOOIE_PLAYBACK_TYPE_SD = 1;
    public static final int NOOIE_PLAYBACK_TYPE_CLOUD = 2;
    public static final int NOOIE_PLAYBACK_SOURCE_TYPE_NORMAL = 0;
    public static final int NOOIE_PLAYBACK_SOURCE_TYPE_DIRECT = 1;

    public static final String NOOIE_AREA_US = "us";
    public static final String NOOIE_AREA_EU = "eu";
    public static final String NOOIE_AREA_CN = "cn";

    public static final int NOOIE_CUSTOM_NAME_TYPE_USER = 0;
    public static final int NOOIE_CUSTOM_NAME_TYPE_DEVICE = 1;

    public static final boolean FORCE_USE_JPUSH = false;

    public static final int NOOIE_AUTO_LOGIN_REPORT_YES = 1;
    public static final int NOOIE_AUTO_LOGIN_REPORT_NO = 0;

    public static final int APP_NOTIFICATION_REQUEST_YES = 1;
    public static final int APP_NOTIFICATION_REQUEST_NO = 0;

    public static final int DEVICE_GUIDE_USED = 1;
    public static final int DEVICE_GUIDE_NOT_USED = 0;

    public static final int STATE_ON = 1;
    public static final int STATE_OFF = 0;

    public static final int CLOUD_PACK_DEFAULT_DAY_NUM = 7;

    public static final int NOOIE_DEVELOP_MODE_DEFAULT = 0;

    public static final int USER_REGISTER_VERIFY = 1;
    public static int USER_FIND_PWD_VERIFY = 2;

    public static final String NORMAL_DEVICE_PUUID = "1";
    public static final int DEVICE_POWER_MODE_NORMAL = 1;
    public static final int DEVICE_POWER_MODE_LP_ACTIVE = 2;
    public static final int DEVICE_POWER_MODE_LP_SLEEP = 3;

    public static final int LP_CAMERA_PLAY_LIMIT_TIME = 240;

    public static final int REMOVE_DEVICE_TYPE_IPC = 1;
    public static final int REMOVE_DEVICE_TYPE_GATEWAY = 2;

    public static final int CONNECTION_MODE_NONE = 0;
    public static final int CONNECTION_MODE_AP = 1;
    public static final int CONNECTION_MODE_QC = 2;
    public static final int CONNECTION_MODE_AP_DIRECT = 3;
    public static final int CONNECTION_MODE_LAN = 4;

    public static final String AP_FUTURE_CODE_PREFIX_VICTURE = "victure_";
    public static final String AP_FUTURE_CODE_PREFIX_VICTURE_REPLACE_TAG = "victure-";
    public static final int AP_FUTURE_CODE_VICTURE_SSID_LEN = 20;
    public static final String AP_FUTURE_PREFIX = "victure";
    public static final int AP_FUTURE_ID_LEN = 14;
    public static final String AP_FUTURE_CODE_PREFIX_GNCC = "securitycam_";
    public static final String AP_FUTURE_CODE_PREFIX_GNCC_FILE_TAG = "osaio_";
    public static final String AP_FUTURE_CODE_PREFIX_GNCC_REPLACE_TAG = "osaio-";
    public static final String AP_FUTURE_PREFIX_GNCC = "securitycam";

    public static final String AP_FUTURE_CODE_PREFIX_TECKIN = "teckin_";
    public static final String AP_FUTURE_PREFIX_TECKIN = "teckin";

    public static final String WIFI_FUTURE_CODE_5 = "_5";
    public static final String WIFI_FUTURE_CODE_5G = "_5g";

    public static final String HB_SERVER_EMPTY = "0.0.0.0";

    public static final String VICTURE_AP_DIRECT_DEVICE_ID = "victure_000011112222";

    public static final int EDIT_MODE_NORMAL = 1;
    public static final int EDIT_MODE_EDITABLE = 2;
    public static final int EDIT_MODE_EDITING = 3;

    public static final int CMD_STATE_ENABLE = 1;
    public static final int CMD_STATE_DISABLE = 0;

    public static final int STATUS_BAR_DARK_MODE = 1;
    public static final int STATUS_BAR_LIGHT_MODE = 2;
    public static final int STATUS_BAR_LIGHT_BLUE_MODE = 3;
    public static final int STATUS_BAR_SPLASH_MODE = 4;
    public static final int STATUS_BAR_DARK_MODE_2 = 5;

    public static final int PRODUCT_TYPE_ROUTER = 0;
    public static final int PRODUCT_TYPE_CARD = 1;
    public static final int PRODUCT_TYPE_GUN = 2;
    public static final int PRODUCT_TYPE_HEAD = 3;
    public static final int PRODUCT_TYPE_LOW_POWER = 4;
    public static final int PRODUCT_TYPE_MINI = 5;
    public static final int PRODUCT_TYPE_LOCK = 205;

    public static final int ROUTER_TYPE_NET_GEAR = 1;
    public static final int ROUTER_TYPE_ASUS = 2;
    public static final int ROUTER_TYPE_D_LINK = 3;
    public static final int ROUTER_TYPE_TP_LINK = 4;

    public static final int CLOUD_RECORD_REQUEST_NORMAL = 1;
    public static final int CLOUD_RECORD_REQUEST_MORE = 2;

    public static final int DEVICE_ID_MAX_LEN = 32;
    public static final int DEVICE_NAME_MAX_LEN = 50;
    public static final int DEVICE_WIFI_MAX_LEN = 32;
    public static final int DEVICE_WIFI_MIN_LEN = 4;

    public final static String EventTypeLive = "EventTypeLive";
    public final static String EventTypeSDPlayBack = "EventTypeSDPlayBack";
    public final static String EventTypeCloudPlayBack = "EventTypeCloudPlayBack";

    public final static int BTN_CLICK_GAP_TIME = 500;

    public static final int PLAY_DISPLAY_TYPE_NORMAL = 1;
    public static final int PLAY_DISPLAY_TYPE_DETAIL = 2;

    public static final int ROUTE_SOURCE_NORMAL = 0;
    public static final int ROUTE_SOURCE_ADD_DEVICE = 1;

    public static final String MODEL_VALUE_OF_530_540 = "PC530/PC540";

    public static final String THIRD_PARTY_CONTROL_PARENT_URL = "file:///android_asset/html/thirdpartycontrol/";
    public static final String THIRD_PARTY_CONTROL_ALEXA_PATH = "alexa.html";
    public static final String THIRD_PARTY_CONTROL_GOOGLE_ASSISTANT_PATH = "google.html";

    public static final int GESTURE_MOVE_LEFT = 1;
    public static final int GESTURE_MOVE_TOP = 2;
    public static final int GESTURE_MOVE_RIGHT = 3;
    public static final int GESTURE_MOVE_BOTTOM = 4;
    public static final int GESTURE_TOUCH_DOWN = 5;
    public static final int GESTURE_TOUCH_UP = 6;

    public static final String KEY_TWO_AUTH_DEVICE_NAME = "KEY_TWO_AUTH_DEVICE_NAME";
    public static final String KEY_TWO_AUTH_DEVICE_PHONE_ID = "KEY_TWO_AUTH_DEVICE_PHONE_ID";
    public static final String KEY_TWO_AUTH_DEVICE_MODEL = "KEY_TWO_AUTH_DEVICE_MODEL";
    public static final String KEY_TWO_AUTH_DEVICE_LAST_TIME = "KEY_TWO_AUTH_DEVICE_LAST_TIME";

    public static final int LP_SUIT_ADD_DEVICE_TYPE_HUM_AND_CAM = 1;
    public static final int LP_SUIT_ADD_DEVICE_TYPE_CAM_WITH_ROUTER = 2;

    public static final int TYPE_FILE_SETTING_CONFIGURE_NONE = 0;
    public static final int TYPE_FILE_SETTING_CONFIGURE_MODE = 1;
    public static final int TYPE_FILE_SETTING_CONFIGURE_SNAP_NUMBER = 2;
    public static final int TYPE_FILE_SETTING_CONFIGURE_RECORDING_TIME = 3;

    public static int DEFAULT_CAMERA_TYPE = 0;
    public static int ADD_CAMERA_TYPE = 1;
    public static int DRAG_CAMERA_TYPE = 2;
    public static int AP_DIRECT_CAMERA_TYPE = 3;

    public static final int DEVICE_ACCOUNT_MAX_LENGTH = 320;

    public static final int INCORRECT_LIGHT_PAGE_ERROR_TYPE_SUB_DEVICE = 1;
    public static final int INCORRECT_LIGHT_PAGE_ERROR_TYPE_ROUTER_DEVICE = 2;

    public static final int FLASH_LIGHT_MODE_FULL_COLOR_NIGHT_VISION = 1;
    public static final int FLASH_LIGHT_MODE_FLASH_WARNING = 2;
    public static final int FLASH_LIGHT_MODE_CLOSE = 0;

    public static int BLUETOOTH_SCAN_TYPE_NEW = 1;
    public static int BLUETOOTH_SCAN_TYPE_EXIST = 2;

    public static int DEVICE_MEDIA_MODE_VIDEO = 0;
    public static int DEVICE_MEDIA_MODE_IMAGE = 1;
    public static int DEVICE_MEDIA_MODE_VIDEO_IMAGE = 2;

    public static int LP_DEVICE_COUNTDOWN_TYPE_PLAYBACK = 1;
    public static int LP_DEVICE_COUNTDOWN_TYPE_SHORT_LINK = 2;

    public static final int AP_DEVICE_TYPE_NORMAL = 0;
    public static final int AP_DEVICE_TYPE_BLE_LP = 1;
    public static final int AP_DEVICE_TYPE_IPC = 2;

    public static final String DEFAULT_PASSWORD_AP_P2P = "12345678";
    public static final String DEFAULT_UUID_AP_P2P = "victure_ap";
    public static final String DEFAULT_SERVER_AP_P2P = "192.168.43.1";
    public static final int DEFAULT_PORT_AP_P2P = 23000;

    public static final int CHANGE_BLE_AP_DEVICE_PASSWORD_RESULT_SUCCESS = 1;
    public static final int CHANGE_BLE_AP_DEVICE_PASSWORD_RESULT_OLD_PW_ERROR = 2;
    public static final int CHANGE_BLE_AP_DEVICE_PASSWORD_RESULT_FAIL = 3;

    public static final int HOME_PAGE_ACTION_NORMAL = 0;
    public static final int HOME_PAGE_ACTION_SWITCH_CONNECTION_MODE = 1;

    public static final String CLOUD_PACK_PARAM_KEY_UUID = "uuid";
    public static final String CLOUD_PACK_PARAM_KEY_MODEL = "model";
    public static final String CLOUD_PACK_PARAM_KEY_ENTER_MARK = "enter_mark";
    public static final String CLOUD_PACK_PARAM_KEY_ORIGIN = "origin";

    public static final int DEVICE_LIGHT_MODE_AUTO = 0;
    public static final int DEVICE_LIGHT_MODE_OFF = 1;
    public static final int DEVICE_LIGHT_MODE_COLOR = 3;
    public static final int DEVICE_LIGHT_MODE_IR = 2;

    public static final String TAB_DEVICE_CATEGORY_ALL = "ALL";
    public static final String TAB_DEVICE_CATEGORY_CAMERA = "CAMERA";
    public static final String TAB_DEVICE_CATEGORY_TUYA = "TUYA";
    public static final String TAB_DEVICE_CATEGORY_APPLIANCES = "APPLIANCES";
    public static final String TAB_DEVICE_CATEGORY_ELECTRICIAN = "ELECTRICIAN";
    public static final String TAB_DEVICE_CATEGORY_LIGHT = "LIGHT";
    public static final String TAB_DEVICE_CATEGORY_ROUTER = "ROUTER";

    public static final String TUYA_CATEGORY_CODE_APPLIANCES = "cwwsq";
    public static final String TUYA_CATEGORY_CODE_ELECTRICIAN = "cz";
    public static final String TUYA_CATEGORY_CODE_LIGHT = "dj";

    public static final int BLE_USER_TYPE_NORMAL = 1;
    public static final int BLE_USER_TYPE_ADMIN = 2;

    public static final int LOCK_RECORD_NAME_FOR_USE_OPEN = 0;
    public static final int LOCK_RECORD_NAME_FOR_FINGER_OPEN = 0x01;
    public static final int LOCK_RECORD_NAME_FOR_PSW_OPEN = 0x02;
    public static final int LOCK_RECORD_NAME_FOR_APP_OPEN = 0x03;
    public static final int LOCK_RECORD_NAME_FOR_REMOTE_OPEN = 0x04;

    //request code
    public static final int REQUEST_CODE_UPDATE_TO_NOOIE = 0x01;
    public static final int REQUEST_CODE_SELECT_COUNTRY = 0x02;
    public static final int REQUEST_CODE_SELECT_SCHEDULE = 0x03;
    public static final int REQUEST_CODE_SET_DETECTION_ZONE = 0x04;
    public static final int REQUEST_CODE_CUSTOM_NAME = 0x05;
    public static final int REQUEST_CODE_DELETE_FILE = 0x06;
    public static final int REQUEST_CODE_SIGN_IN = 0x07;
    public static final int REQUEST_CODE_INPUT_DEVICE_ID = 0x08;
    public static final int REQUEST_CODE_FOR_CAMERA = 0x09;
    public static final int REQUEST_CODE_FOR_TWO_AUTH_LOGIN = 0x10;

    public static final int REQUEST_CODE_FOR_ENABLE_BLUE = 0x401;
    public static final int REQUEST_CODE_FOR_LOCATION_PERM = 0x402;
    public static final int REQUEST_CODE_FOR_OPENING_LOCATION_PERM_SETTING = 0x403;
    public static final int REQUEST_CODE_FOR_LOCATION_WEATHER_PERM = 0x404;

    public static final int REQUEST_CODE_WIFI_SETTING = 0X35;
    public static final int REQUEST_CODE_ADD_MEMBER_SINGLE = 0x11;
    public static final int REQUEST_CODE_ADD_HOME = 0x12;
    public static final int REQUEST_CODE_DEVICE_ADD = 0x20;
    public static final int REQUEST_CODE_DEVICE_REMOVE = 0x14;
    public static final int REQUEST_CODE_GROUP_SETTING = 0x15;
    public static final int REQUEST_CODE_GROUP_RENAME = 0x16;
    public static final int REQUEST_CODE_MEMBER_INFO = 0x18;
    public static final int REQUEST_CODE_HOME_RENAME = 0x22;
    public static final int REQUEST_CODE_ADD_MEMBER_HOME = 0x23;
    public static final int REQUEST_CODE_MEMBER_RENAME = 0x24;
    public static final int REQUEST_CODE_HOME_MANAGE = 0x25;
    public static final int REQUEST_CODE_MEMBER_INFO_REMOVE = 0x32;
    public static final int REQUEST_CODE_REPEAT = 0X36;
    public static final int REQUEST_CODE_EFFECT = 0X37;

    //permission group
    public static final String[] PERM_GROUP_LOCATION = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    public static final String[] PERM_GROUP_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static final String[] PERM_GROUP_STORAGE_API_30 = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public static final String[] PERM_GROUP_CAMERA = {
            Manifest.permission.CAMERA
    };

    public static final String[] PERM_GROUP_PHONE = {
            Manifest.permission.READ_PHONE_STATE
    };

    public interface Suffix {
        String MP4 = ".mp4";
        String DNAV = ".dnav";
        String JPEG = ".jpg";
        String PNG = ".png";
    }

    public static final String PARAM_KEY_DEVICE_ID = "PARAM_KEY_DEVICE_ID";
    public static final String PARAM_KEY_PDEVICE_ID = "PARAM_KEY_PDEVICE_ID";
    public static final String PARAM_KEY_MODEL = "PARAM_KEY_MODEL";
    public static final String PARAM_KEY_CONNECTION_MODE = "PARAM_KEY_CONNECTION_MODE";
    public static final String PARAM_KEY_DEVICE_SSID = "PARAM_KEY_DEVICE_SSID   ";


    /*Router*/
    public static final String ROUTER_CONNECT_STATE = "ROUTER_CONNECT_STATE";

    public static final String IPC_MODEL_A1 = "C2";
    public static final String IPC_MODEL_C1 = "C1";
    public static final String IPC_MODEL_Q1 = "Q1";
    public static final String IPC_MODEL_T1 = "T1";
    public static final String IPC_MODEL_P1 = "P1";
    public static final String IPC_MODEL_P2 = "P2";
    public static final String IPC_MODEL_P3 = "P3";
    public static final String IPC_MODEL_P4 = "P4";
    public static final String IPC_MODEL_K1 = "K1";
    public static final String IPC_MODEL_K2 = "K2";
    public static final String IPC_MODEL_P3PRO = "P3 Pro";
    public static final String IPC_MODEL_M1 = "M1";
    public static final String IPC_MODEL_W0_CAM = "W0-CAM";
    public static final String IPC_MODEL_W0_HUB = "W0-HUB";
    public static final String IPC_MODEL_W1 = "W1";
    public static final String IPC_MODEL_W2 = "W2";

    public static final int SHARE_DEVICE_MAX_COUNT = 3;

    public static final String BANNER_PARAM_CODE = "742896f316ecec63145655034c04574a";

    //tuya
    public static final String INTENT_KEY_REFRESH_TYPE = "INTENT_KEY_REFRESH_TYPE";
    public static final String INTENT_KEY_GROUP_ID = "INTENT_KEY_GROUP_ID";

    //groupType
    public final static String GROUP_TYPE_PLUG = "GROUP_TYPE_PLUG";//插座群组
    public final static String GROUP_TYPE_LAMP = "GROUP_TYPE_LAMP";//灯的群组
    public static final String INTENT_KEY_SCHEDULE_GROUP_OPERATE = "INTENT_KEY_SCHEDULE_GROUP_OPERATE";//用于群组定点定时界面
    public static final String INTENT_KEY_GROUP_TYPE = "INTENT_KEY_GROUP_TYPE";

    public static final int MAX_DEVICE_NAME_LENGTH = 50;//设备名称最大长度
    public static final int MAX_FAMILY_NAME_LENGTH = 25;//家庭名称最大长度
    public static final int MAX_GROUP_NAME_LENGTH = 64;//群组名称最大长度

    //device model
    public final static String SMART_PLUG_PRODUCTID = "octeoqhuayzof69q";//teckin美规单插  SP10
    public final static String SMART_PLUG_PRODUCTID_NEW = "iqgfsxokdkzzehmj";//teckin美规上线单插  SP10
    public final static String SMART_PLUG_PRODUCTID_OLD = "4bVOiYN0zdh6vTYq";//teckin美规单插  SP10
    public final static String SMART_PLUG_US_PRODUCTID_TWO = "dok3rzi3pnnqu6ju";//teckin美规规插头 SP20
    public final static String SMART_PLUG_US_PRODUCTID_THREE = "viv1giuyu2tk4kt4";//teckin美规规插头 SP20
    public final static String SMART_PLUG_EU_PRODUCTID = "cya3zxfd38g4qp8d"; //teckin欧规插头 SP21
    public final static String SMART_PLUG_UK_PRODUCTID = "5bvnmoqjth5nd4de"; //teckin英规插头 SP23/SP27
    public final static String SMART_PLUG_UK_PRODUCTID_TWO = "sOhGq6u1M2JwB5d8"; //teckin英规插头 SP23/SP27
    public final static String SMART_PLUG_UK_PRODUCTID_THREE = "twezq8g8ykoaggey"; //teckin英规插头 SP23/SP27
    public final static String SMART_LAMP_PRODUCTID = "isehgkqn5uqlrorl";//teckin美规灯 SB53
    public final static String SMART_LAMP_PRODUCTID_TWO = "fnxgcsysunpyxkou";//teckin美规灯 SB50L
    public final static String SMART_LAMP_PRODUCTID_THREE = "ttrn0dlxota0rrav";//teckin美规灯 SB50H
    public final static String SMART_LAMP_PRODUCTID_FOUR = "hdnoe1sqimwad9f4";//teckin美规灯 SB60
    public final static String SMART_LAMP_PRODUCTID_FIVE = "gswrpjab2vfawful";//teckin新dp灯 SB50
    public final static String SMART_LAMP_PRODUCTID_SIX = "5abhrka6ejfr0hvx";//teckin新dp灯 SB50
    public final static String SMART_PLUG_EU_PRODUCTID_TWO = "fbvia0apnlnattcy";//teckin欧规插头 SP22
    public final static String SMART_PLUG_EU_PRODUCTID_THREE = "vnya2spfopsh9lro";//teckin欧规插头 SP22
    public final static String SMART_STRIP_PRODUCTID = "01wjigkru2tgixxp";//teckin排插 SS36
    public final static String SMART_STRIP_PRODUCTID_NEW = "omwxkdvwpxtyjans";//teckin排插 SS36
    public final static String SMART_STRIP_PRODUCTID_TWO = "EQD8hAQw543vzh6O";//teckin排插 SS31
    public final static String SMART_STRIP_PRODUCTID_THREE = "iGuAESc6917owGUr";//teckin排插 SS33
    public final static String SMART_STRIP_PRODUCTID_FOUR = "j7ewsefbjxaprlqy";//teckin排插 SS30N
    public final static String SMART_SWITCH_PRODUCTID = "pJnpT0XcM5FTRjOd";//SR41
    public final static String SMART_SWITCH_PRODUCTID_TWO = "bYdRrWx5iLCyAfPs";//SR42
    public final static String SMART_FLOOR_LAMP_PRODUCTID = "nscnnpeguv620u4f";//落地灯 FL41
    public final static String SMART_LIGHT_MODULATOR_PRODUCTID = "1ogqu4bxwzrjxu8v";//调光器 SR46
    public final static String SMART_LIGHT_STRIP_PRODUCTID = "gmjdt6mvy1mntvqn";//灯带 SL02
    public final static String SMART_LIGHT_STRIP_PRODUCTID_TWO = "g56afofns8lpko6v";//灯带 SL02/SL07
    public final static String SMART_LIGHT_STRIP_PRODUCTID_THREE = "behqxmx1m8e4sr08";//灯带 SL12
    public final static String SMART_LIGHT_STRIP_PRODUCTID_FOUR = "b6vjkghax6amtwdf";//灯带 SL08
    public final static String SMART_PLUG_JP_PRODUCTID = "A6bBfm2fmKKRfIxU";//teckin日规单插
    public final static String SMART_PLUG_JP_PRODUCTID_ONE = "8jkyyvxsep3yr5ql";//teckin日规单插SP11
    public final static String SMART_SWITCH_PRODUCTID_THREE = "ycccdik7krsxuybg";//teckin墙壁开关SR40
    public final static String SMART_SWITCH_PRODUCTID_FOUR = "J4b9HONUUjrBxJXK";//teckin墙壁开关带USB SR43
    /*使用wifi+蓝牙模组的设备*/
    public final static String SMART_FLOOR_LAMP_PRODUCTID_TWO = "5at5rg1h1viwp6ol";//落地灯 FL41
    public final static String SMART_PLUG_PRODUCTID_NEW_TWO = "z6ai9dh9a0aujfdy";//SP10
    public final static String SMART_PLUG_US_PRODUCTID_FOUR = "sfurldd0ddoc2mat";//SP20
    public final static String SMART_PLUG_EU_PRODUCTID_FOUR = "f0o1eyjw1pfojq87";//SP21
    public final static String SMART_PLUG_EU_PRODUCTID_FIVE = "99oomugbqvd1axj0";//SP22
    public final static String SMART_PLUG_UK_PRODUCTID_FOUR = "ga8gcmwpkl3jc4ek";//SP27
    public final static String SMART_PLUG_EU_PRODUCTID_SIX = "ep0rgcsdmq4sp6cd";//SP22
    public final static String SMART_STRIP_PRODUCTID_FIVE = "rk9wwke99mdncz2n";//SS32
    public final static String SMART_STRIP_PRODUCTID_SIX = "qtbtyifjcm3ou6f1";//SS34
    public final static String SMART_PLUG_UK_PRODUCTID_FIVE = "zae4ua68xt9wxfap";//SP23
    public final static String SMART_LAMP_PRODUCTID_SEVEN = "pahtehvb1nisjsa4";//SB30 欧标
    public final static String SMART_LAMP_PRODUCTID_EIGHT = "ohmqju5mfrmjktsu";//SB30 美标
    public final static String SMART_LAMP_PRODUCTID_NINE = "c2uurdygilpx7nko";//SB50 欧标
    public final static String SMART_LAMP_PRODUCTID_TEN = "s3a7fbytlm1gprhz";//SB50 美标
    public final static String SMART_LAMP_PRODUCTID_ELEVEN = "ee2nlhpqplatlcoh";//SB50 北美矮款
    public final static String SMART_LAMP_PRODUCTID_TWELVE = "8vbbgyn8uosnntvf";//SB30 北美矮款
    public final static String SMART_LAMP_PRODUCTID_THIRTEEN = "rcaduvje2rrpoe9d";//SB50佳比泰  220V欧规

    public final static String SMART_STRIP_PRODUCTID_SEVEN = "ugdkg6rn63peraai";//ss60
    public final static String SMART_PLUG_EU_PRODUCTID_SEVEN = "nkd4cs6u1vfu9ksi";//sp31
    public final static String SMART_SWITCH_PRODUCTID_FIVE = "rofiypesat1paym2";//SR41
    public final static String SMART_SWITCH_PRODUCTID_SIX = "xyfjwup7nwnsqak7";//SR42
    public final static String SMART_BULB_PRODUCTID = "trtvoqie5as0bfpo";//DL46
    public final static String SMART_FEEDER_PRODUCTID = "xmpy4utews9k3waf";
    public final static String SMART_PLUG_EU_PRODUCTID_EIGHT = "xajto1x7xm4w3x0s";
    public final static String SMART_AIR_PURIFIER_PRODUCTID = "fsxtzzhujkrak2oy";
    public final static String SMART_PLUG_EU_PRODUCTID_NINE = "infi2mnk4bjxhjtc";
    public final static String SMART_PLUG_US_PRODUCTID_FIVE = "evmunsts6htgolmm";
    public final static String SMART_PLUG_EU_PRODUCTID_TEN = "q6rtbvpccl5jvkjf";
    public final static String SMART_PLUG_US_PRODUCTID_SIX = "6aheu8de32k7ruvq";
    public final static String SMART_PLUG_UK_PRODUCTID_SIX = "0f76bavw2lp9yu5e";
    public final static String SMART_PLUG_UK_PRODUCTID_SEVEN = "7n504uls2twfik2b";

    //device model value
    public static final String SMART_PLUG_US_NAME = "SP10";//teckin美规单插
    public static final String SMART_PLUG_US_NAME_TWO = "SP20";//teckin美规单插
    public static final String SMART_PLUG_EU_NAME = "SP21";//teckin欧规单插
    public static final String SMART_PLUG_UK_NAME = "SP23/SP27";//teckin英规单插
    public static final String SMART_PLUG_UK_NAME_TWO = "SP23";//teckin英规单插
    public static final String SMART_PLUG_UK_NAME_THREE = "SP27";//teckin英规单插
    public static final String SMART_LAMP_NAME = "SB53";//teckin灯
    public static final String SMART_LAMP_NAME_TWO = "SB50";//teckin灯
    public static final String SMART_LAMP_NAME_THREE = "SB60";//teckin灯
    public static final String SMART_LAMP_NAME_FOUR = "SB30";//teckin灯
    public static final String SMART_PLUG_NAME_JP = "SP11";//teckin日规单插
    public static final String SMART_PLUG_EU_NAME_TWO = "SP22";//teckin欧规带电量插头
    public static final String SMART_STRIP = "SS36";//teckin防水排插
    public static final String SMART_STRIP_TWO = "SS42 Pro";//teckin防水排插
    public static final String SMART_STRIP_THREE = "SS33 Pro";//teckin防水排插
    public static final String SMART_STRIP_FIVE = "SS32";//teckin防水排插
    public static final String SMART_STRIP_SIX = "SS34";//teckin防水排插
    public static final String SMART_STRIP_FOUR = "SS30N Pro";//teckin排插
    public static final String SMART_SWITCH = "SR41";//teckin墙壁开关
    public static final String SMART_SWITCH_TWO = "SR42";//teckin墙壁开关
    public static final String SMART_FLOOR_LAMP = "FL41";//落地灯
    public static final String SMART_LIGHT_MODULATOR = "SR46";//调光器
    public static final String SMART_LIGHT_STRIP = "SL02";//灯带
    public static final String SMART_LIGHT_STRIP_TWO = "SL02/SL07";//灯带
    public static final String SMART_LIGHT_STRIP_THREE = "SL12";//灯带
    public static final String SMART_LIGHT_STRIP_FOUR = "SL08";//灯带
    public static final String SMART_SWITCH_THREE = "SR40";//teckin墙壁开关
    public static final String SMART_SWITCH_FOUR = "SR43";//teckin墙壁开关
    public static final String SMART_PLUG_EU_NAME_THREE = "SP31";//欧规单插
    public static final String SMART_STRIP_SEVEN = "SS60";//欧规排插
    public static final String SMART_BULB = "DL46";//球泡
    public static final String SMART_PORIK_PLUG = "SP01";//Porik品牌设备名字
    public static final String SMART_PORIK_PLUG_TWO = "SP12";
    public static final String SMART_PORIK_LAMP = "NB11";
    public static final String SMART_PORIK_PLUG_UK_NAME = "SP21/SP22";
    public static final String SMART_FEEDER = "KPF01";
    public static final String SMART_AIR_PURIFIER = "CA01";
    public static final String SMART_PLUG_EU_NAME_FOUR = "SP11 Pro";
    public static final String SMART_PLUG_US_NAME_THREE = "SP01 Pro";
    public static final String SMART_PLUG_UK_NAME_FOUR = "SP22 Pro";
    public static final String SMART_PLUG_EU_NAME_FIVE = "SP11 Plus";
    public static final String SMART_PLUG_US_NAME_FOUR = "SP01 Plus";
    public static final String SMART_PLUG_UK_NAME_FIVE = "SP22 Plus";
    public final static String DEVICE_DEFAULT_NAME = " unknown";

    //Lamp work mode value
    public final static String LAMP_WORK_MODE_WHITE = "white";
    public final static String LAMP_WORK_MODE_COLOUR = "colour";
    public final static String LAMP_WORK_MODE_SCENE = "scene";
    public final static String LAMP_WORK_MODE_SCENE_ONE = "scene_1";
    public final static String LAMP_WORK_MODE_SCENE_TWO = "scene_2";
    public final static String LAMP_WORK_MODE_SCENE_THREE = "scene_3";
    public final static String LAMP_WORK_MODE_SCENE_FOUR = "scene_4";
    public final static String LAMP_WORK_MODE_MUSIC = "music";

    //set scene color
    public static final String SMART_SCENE_COLOR_ONE = "#64A994";
    public static final String SMART_SCENE_COLOR_TWO = "#7899C6";
    public static final String SMART_SCENE_COLOR_THREE = "#7D88CA";
    public static final String SMART_SCENE_COLOR_FOUR = "#8E959F";
    public static final String SMART_SCENE_COLOR_FIVE = "#9E908F";
    public static final String SMART_SCENE_COLOR_SIX = "#929992";
    public static final String SMART_SCENE_COLOR_SEVEN = "#E77E68";
    public static final String SMART_SCENE_COLOR_EIGHT = "#E67577";
    public static final String SMART_SCENE_COLOR_NINE = "#DE7997";
    public static final String SMART_SCENE_COLOR_TEN = "#DF8A53";
    public static final String SMART_SCENE_COLOR_ELEVEN = "#D4972C";
    public static final String SMART_SCENE_COLOR_TWELVE = "#71A656";


    //nooie home 添加设备类型
    public final static String ADD_DEFAULT = "add_default";//无选中设备，跳转到其他页面
    public final static String ADD_DEVICE = "add_device";//添加单插
    public final static String ADD_POWERSTRIP = "add_powerstrip";//添加排插
    public final static String ADD_LAMP = "add_lamp";//添加智能灯
    public final static String ADD_SWITCH = "add_switch";//添加开关
    public final static String ADD_LIGHT_STRIP = "add_light_strip";//添加灯带
    public final static String ADD_LIGHT_MODULATOR = "add_light_modulator";//添加调光器
    public final static String ADD_PET_FEEDER = "add_pet_feeder";//添加宠物喂食器
    public final static String ADD_AIR_PURIFIER = "add_air_purifier";//添加空气净化器


    //保存到本地当前选中的themebean
    public final static String LAMP_ISSELECT_THEMEBEAN = "themeBean";
    public final static String LAMP_ISSELECT__GROUP_THEMEBEAN = "groupThemeBean";

    //Schema
    public static final String TIME_FORMAT_LONG = "%04x";
    public static final String TIME_FORMAT_SHORT = "%02d";
    public static final int HOUR_MINUTE = 60;
    public static final String SCHEDULE_ON = "01";
    public static final String SCHEDULE_OFF = "00";
    public static final String SCHEDULE_ONCE = "00";
    public static final String SCHEDULE_MONDAY = "02";
    public static final String SCHEDULE_TUESDAY = "04";
    public static final String SCHEDULE_WEDNESDAY = "08";
    public static final String SCHEDULE_THURSDAY = "10";
    public static final String SCHEDULE_FRIDAY = "20";
    public static final String SCHEDULE_SATERDAY = "40";
    public static final String SCHEDULE_SUNDAY = "01";
    public static final String SCHEDULE_ONCE_DES = "Once";
    public static final String SCHEDULE_MONDAY_DES = "Mon";
    public static final String SCHEDULE_TUESDAY_DES = "Tues";
    public static final String SCHEDULE_WEDNESDAY_DES = "Wed";
    public static final String SCHEDULE_THURSDAY_DES = "Thur";
    public static final String SCHEDULE_FRIDAY_DES = "Fri";
    public static final String SCHEDULE_SATERDAY_DES = "Sat";
    public static final String SCHEDULE_SUNDAY_DES = "Sun";
    public static final String SCHEDULE_EVERY_DAY_DES = "Every Day";

    public final static String SCHEDULE_SWITCH1_OFF = "00";
    public final static String SCHEDULE_SWITCH1_ON = "01";
    public final static String SCHEDULE_SWITCH2_OFF = "02";
    public final static String SCHEDULE_SWITCH2_ON = "03";
    public final static String SCHEDULE_SWITCH3_OFF = "04";
    public final static String SCHEDULE_SWITCH3_ON = "05";
    public final static String SCHEDULE_SWITCH4_OFF = "06";
    public final static String SCHEDULE_SWITCH4_ON = "07";
    public final static String SCHEDULE_SWITCH_USB1_OFF = "08";
    public final static String SCHEDULE_SWITCH_USB1_ON = "09";

    //PowerStrip detfalut name
    public final static String POWERSTRIP_PLUG = "switch";
    public final static String POWERSTRIP_PLUG_1 = "switch_1";
    public final static String POWERSTRIP_PLUG_2 = "switch_2";
    public final static String POWERSTRIP_PLUG_3 = "switch_3";
    public final static String POWERSTRIP_PLUG_4 = "switch_4";
    public final static String POWERSTRIP_PLUG_ALL = "switch_all";
    public final static String POWERSTRIP_PLUG_USB1 = "switch_usb1";
    public final static String POWERSTRIP_PLUG_DEFAULT_NAME_1 = "plug1";
    public final static String POWERSTRIP_PLUG_DEFAULT_NAME_2 = "plug2";
    public final static String POWERSTRIP_PLUG_DEFAULT_NAME_3 = "plug3";
    public final static String POWERSTRIP_PLUG_DEFAULT_NAME_4 = "plug4";
    public final static String POWERSTRIP_PLUG_DEFAULT_NAME_USB1 = "USB";

    //Home event operate
    public final static String EVENTTYPE = "eventType";
    public final static String HOME_ONREFRESH = "onRefresh";//刷新数据
    public final static String HOME_STOPREFRESH = "stopRefresh";//停止刷新数据
    public final static String HOME_SHOW_LOADING = "showLoadingDialog";//显示加载Loading
    public final static String HOME_HIDE_LOADING = "hideLoadingDialog";//隐藏加载Loading

    //glide
    public static final int DURATION_MILLIS = 300;

    //tuya dp_id
    public static final String DP_ID_SWITCH_ON = "1";
    public static final String LAMP_DP_ID_SWITCH_ON = "20";
    public static final String DP_ID_COUNT_DOWN = "9";
    public static final String DP_ID_POWER_STATE = "9";
    public static final String DP_ID_LIGHT_STATE = "9";

    //group schedule type 用于ScheduleActionActivity定点定时
    public final static String GROUP_FOR_PLUG = "group_shedule_for_plug";//智能插座群组定点定时
    public final static String GROUP_FOR_LAMP = "group_shedule_for_lamp";//智能灯群组定点定时
    public final static String SHEDULE_FOR_SINGLE_DEVICE = "shedule_for_single_device";//单个设备定点定时


    public static final String HOME_DEFALUT_NAME = "Home";
}
