package com.afar.osaio.smart.push.firebase.analytics;

public class FirebaseConstant {

    public static final long DEFAULT_SESSION_TIME_OUT_DURATION = 3 * 60 * 1000L;

    public static final String USER_PROPERTY_NUMBER_OF_IPC = "IPC绑定数";
    public static final String USER_PROPERTY_NUMBER_OF_GUEST_IPC = "Guest IPC数";

    public static final String EVENT_LOGIN_RESULT = "EVENT_LOGIN_RESULT";
    public static final String EVENT_DEVICE_BINDING = "IPC_连接中页_连接结果";
    public static final String EVENT_CLOUD_ORIGIN_FROM_LIVE = "IPC_直播页_查看云付费套餐";
    public static final String EVENT_CLOUD_ORIGIN_FROM_DEVICE_MESSAGE = "IPC_设备消息页_查看云付费套餐";
    public static final String EVENT_CLOUD_ORIGIN_FROM_DEVICE_MESSAGE_ITEM = "IPC_设备消息页_查看消息提示";
    public static final String EVENT_CLOUD_ORIGIN_FROM_STORAGE = "IPC_设备设置_存储选项页_查看云付费套餐";
    public static final String EVENT_CLOUD_PACK_PAGE_START_LOADING = "IPC_云付费套餐页_开始加载";
    public static final String EVENT_CLOUD_PACK_PAGE_FINISH_LOADING = "IPC_云付费套餐页_加载完成";

    public static final String KEY_DEVICE_UUID = "设备uuid";
    public static final String KEY_DEVICE_NAME = "设备名字";
    public static final String KEY_DEVICE_MODEL = "设备型号";
    public static final String KEY_ORIGIN = "来路";
    public static final String KEY_ENTER_MARK = "标记";
    public static final String KEY_LANGUAGE = "语言";
    public static final String KEY_RESULT = "结果";

    public static final String KEY_MESSAGE_TYPE = "message";
    public static final String KEY_NETWORK_TYPE = "network_type";

    public static final String RESULT_SUCCESS = "成功";
    public static final String RESULT_FAIL = "失败";

    public static final String EVENT_URL_CLOUD_PACK_HOME = "pack/list";
}
