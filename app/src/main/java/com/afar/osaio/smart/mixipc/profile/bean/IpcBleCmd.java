package com.afar.osaio.smart.mixipc.profile.bean;

import android.text.TextUtils;

import com.nooie.common.utils.data.StringHelper;
import com.nooie.common.utils.log.NooieLog;

import java.util.ArrayList;
import java.util.List;

public class IpcBleCmd {

    //send cmd
    public static final String BLE_CMD_LOGIN = "AT+L,VICTURE\r";
    public static final String BLE_CMD_SET_PW = "AT+P,";
    public static final String BLE_CMD_QUERY_PW = "AT+DP\r";
    public static final String BLE_CMD_OPEN_HOT_SPOT = "AT+OA\r";
    public static final String BLE_CMD_CLOSE_HOT_SPOT = "AT+CA\r";
    public static final String BLE_CMD_DISTRIBUTE_NETWORK_SEND = "AT+S%1$s%2$s\r";
    public static final String BLE_CMD_DISTRIBUTE_NETWORK_SEND_END = "AT+SEND,%1$s\r";

    //cmd response
    public static final String BLE_CMD_LOGIN_RSP = "AR+L";
    public static final String BLE_CMD_SET_PW_RSP = "AR+P";
    public static final String BLE_CMD_QUERY_PW_RSP = "AR+DP";
    public static final String BLE_CMD_OPEN_HOT_SPOT_RSP = "AR+OA";
    public static final String BLE_CMD_CLOSE_HOT_SPOT_RSP = "AR+CA";
    public static final String BLE_CMD_DISTRIBUTE_NETWORK_SEND_RSP = "AR+S";
    public static final String BLE_CMD_FEATURE_RSP = "AR";

    public static final String BLE_CMD_RSP_SUCCESS = "OK";
    public static final String BLE_CMD_RSP_FAIL = "FAIL";
    public static final String BLE_CMD_RSP_UNBIND = "UNBIND";
    public static final String BLE_CMD_RSP_BOUND_BY_SELF = "BINDED1";
    public static final String BLE_CMD_RSP_BOUND_BY_OHTER = "BINDED2";

    public static final int BLE_CMD_SINGLE_PACKAGE_MAX_LEN = 20;
    public static final int BLE_CMD_VALUE_MAX_LEN = 14;

    public static final String BLE_CMD_QUERY_STATE_FACTORY = "0,0";

    /**
     * 以单个字节长度切割命令
     * @param cmdValue
     * @return
     */
    public static List<String> convertBleLongCmd(String cmdValue) {
        NooieLog.d("-->> debug IpcBleCmd cmdValue=" + cmdValue);
        if (TextUtils.isEmpty(cmdValue)) {
            return null;
        }
        try {
            List<String> splitCmdList = new ArrayList<>();
            int cmdLength = getTextByteSize(cmdValue);
            if (cmdLength <= BLE_CMD_VALUE_MAX_LEN) {
                NooieLog.d("-->> debug IpcBleCmd 1 subCmdValue=" + cmdValue);
                splitCmdList.add(cmdValue);
                return splitCmdList;
            }

            StringBuilder currentTextSb = new StringBuilder();
            StringBuilder resultSb = new StringBuilder();
            int i = 1;
            while (getTextByteSize(currentTextSb.toString()) < cmdLength) {
                int subLen = (BLE_CMD_VALUE_MAX_LEN - (numLength(i) - 1));
                if (subLen <= 0) {
                    break;
                }
                int subStart = currentTextSb.length();
                boolean isLast = resultSb.length() == 0 && (cmdLength - getTextByteSize(currentTextSb.toString()) <= subLen);
                if (isLast) {
                    if (cmdLength - getTextByteSize(currentTextSb.toString()) > 0) {
                        String subCmdValue = cmdValue.substring(subStart);
                        currentTextSb.append(subCmdValue);
                        NooieLog.d("-->> debug IpcBleCmd 2 subCmdValue=" + subCmdValue + " subLen=" + subLen);
                        splitCmdList.add(subCmdValue);
                    }
                    break;
                }

                String tmpValue = cmdValue.substring(subStart, (subStart + 1));
                //NooieLog.d("-->> debug IpcBleCmd tmpValue=" + tmpValue + " newresultsblen=" + (getTextByteSize(resultSb.toString()) + getTextByteSize(tmpValue)) + " subLen=" + subLen);
                if ((getTextByteSize(resultSb.toString()) + getTextByteSize(tmpValue)) <= subLen) {
                    resultSb.append(tmpValue);
                    currentTextSb.append(tmpValue);
                    if (getTextByteSize(resultSb.toString()) == subLen) {
                        NooieLog.d("-->> debug IpcBleCmd 3 subCmdValue=" + resultSb.toString() + " subLen=" + subLen);
                        splitCmdList.add(resultSb.toString());
                        resultSb.setLength(0);
                        i++;
                    }
                } else {
                    NooieLog.d("-->> debug IpcBleCmd 4 subCmdValue=" + resultSb.toString() + " subLen=" + subLen);
                    splitCmdList.add(resultSb.toString());
                    resultSb.setLength(0);
                    i++;
                }
            }
            NooieLog.d("-->> debug IpcBleCmd currentTextSb=" + currentTextSb);
            return splitCmdList;
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
        return null;
    }

    /**
     * 以当个字符长度切割命令
     * @param cmdValue
     * @return
     */
    public static List<String> convertBleLongCmd2(String cmdValue) {
        NooieLog.d("-->> debug IpcBleCmd cmdValue=" + cmdValue);
        if (cmdValue == null || cmdValue.length() <= BLE_CMD_VALUE_MAX_LEN) {
            return null;
        }
        try {
            List<String> splitCmdList = new ArrayList<>();
            int cmdLength = cmdValue.length();
            int i = 1;
            int currentLength = 0;
            while (currentLength < cmdLength) {
                NooieLog.d("-->> debug IpcBleCmd i= " + i + " cmdLength=" + cmdLength + " currentLength=" + currentLength);
                int subLen = (BLE_CMD_VALUE_MAX_LEN - (numLength(i) - 1));
                int subStart = currentLength;
                NooieLog.d("-->> debug IpcBleCmd subLen=" + subLen + " subStart=" + subStart + " isLast=" + (cmdLength - currentLength));
                i++;
                if (cmdLength - currentLength < subLen) {
                    String tmpValue = cmdLength - currentLength > 0 ? cmdValue.substring(subStart) : "";
                    NooieLog.d("-->> debug IpcBleCmd tmpValue=" + tmpValue);
                    if (tmpValue != null) {
                        splitCmdList.add(tmpValue);
                    }
                    break;
                } else {
                    int subEnd = currentLength + subLen;
                    String tmpValue = cmdValue.substring(subStart, subEnd);
                    currentLength = currentLength + tmpValue.length();
                    NooieLog.d("-->> debug IpcBleCmd tmpValue=" + tmpValue + " subEnd" + subEnd + " currentLength=" + currentLength);
                    if (tmpValue != null) {
                        splitCmdList.add(tmpValue);
                    }
                }
            }
            return splitCmdList;
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
        return null;
    }

    public static int getTextByteSize(String text) {
        return (int)StringHelper.getStringByteSize(text, StringHelper.CharSet_UTF_8);
    }

    public static int numLength(int num) {
        try {
            return String.valueOf(num).length();
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
        return 0;
    }

    public static boolean checkCmdRspIsUnbind(String rsp) {
        if (TextUtils.isEmpty(rsp)) {
            return false;
        }
        return rsp.contains(BLE_CMD_RSP_UNBIND);
    }

    public static boolean checkCmdRspIsBoundBySelf(String rsp) {
        if (TextUtils.isEmpty(rsp)) {
            return false;
        }
        return rsp.contains(BLE_CMD_RSP_BOUND_BY_SELF);
    }

    public static boolean checkCmdRspIsBoundByOther(String rsp) {
        if (TextUtils.isEmpty(rsp)) {
            return false;
        }
        return rsp.contains(BLE_CMD_RSP_BOUND_BY_OHTER);
    }
}
