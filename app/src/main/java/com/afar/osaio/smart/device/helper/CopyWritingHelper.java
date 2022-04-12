package com.afar.osaio.smart.device.helper;

import android.content.Context;

import com.afar.osaio.R;
import com.afar.osaio.util.ConstantValue;

public class CopyWritingHelper {

    public static String getString(Context context, int resId) {
        if (context == null) {
            return "";
        }
        return context.getString(resId);
    }

    public static String convertFlashLightModeTitle(Context context, int mode) {
        if (mode == ConstantValue.FLASH_LIGHT_MODE_FULL_COLOR_NIGHT_VISION) {
            return getString(context, R.string.flash_light_full_color_night_vision_title);
        } else if (mode == ConstantValue.FLASH_LIGHT_MODE_FLASH_WARNING) {
            return getString(context, R.string.flash_light_flash_warning_title);
        } else if (mode == ConstantValue.FLASH_LIGHT_MODE_CLOSE) {
            return getString(context, R.string.flash_light_close_title);
        } else {
            return "";
        }
    }

    public static String convertFlashLightModeTag(Context context, int mode) {
        if (mode == ConstantValue.FLASH_LIGHT_MODE_FULL_COLOR_NIGHT_VISION) {
            return getString(context, R.string.flash_light_full_color_night_vision_tag);
        } else if (mode == ConstantValue.FLASH_LIGHT_MODE_FLASH_WARNING) {
            return getString(context, R.string.flash_light_flash_warning_tag);
        } else if (mode == ConstantValue.FLASH_LIGHT_MODE_CLOSE) {
            return getString(context, R.string.flash_light_close_tag);
        } else {
            return "";
        }
    }
}
