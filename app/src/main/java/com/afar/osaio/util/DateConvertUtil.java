package com.afar.osaio.util;

import android.os.Bundle;
import android.text.TextUtils;

import com.nooie.common.utils.log.NooieLog;

import java.util.Map;

public class DateConvertUtil {

    public static Bundle bundleFromMap(Map<String, Object> dataMap) {
        if (dataMap == null || dataMap.isEmpty()) {
            return null;
        }
        Bundle result = new Bundle();
        try {
            for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                if (entry == null || TextUtils.isEmpty(entry.getKey()) || entry.getValue() == null) {
                    continue;
                }
                if (entry.getValue() instanceof Boolean) {
                    result.putBoolean(entry.getKey(), (Boolean)entry.getValue());
                } else if (entry.getValue() instanceof Byte) {
                    result.putInt(entry.getKey(), (Byte)entry.getValue());
                } else if (entry.getValue() instanceof Character) {
                    result.putChar(entry.getKey(), (Character)entry.getValue());
                } else if (entry.getValue() instanceof Short) {
                    result.putShort(entry.getKey(), (Short)entry.getValue());
                } else if (entry.getValue() instanceof Integer) {
                    result.putInt(entry.getKey(), (Integer)entry.getValue());
                } else if (entry.getValue() instanceof Long) {
                    result.putLong(entry.getKey(), (Long)entry.getValue());
                } else if (entry.getValue() instanceof Float) {
                    result.putFloat(entry.getKey(), (Float)entry.getValue());
                } else if (entry.getValue() instanceof Double) {
                    result.putDouble(entry.getKey(), (Double)entry.getValue());
                } else if (entry.getValue() instanceof String) {
                    result.putString(entry.getKey(), (String)entry.getValue());
                }
            }
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
        return result;
    }
}
