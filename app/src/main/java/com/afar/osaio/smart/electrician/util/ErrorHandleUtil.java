package com.afar.osaio.smart.electrician.util;

import android.app.Activity;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.util.ToastUtil;

/**
 * ErrorHandleUtil
 *
 * @author Administrator
 * @date 2019/3/11
 */
public class ErrorHandleUtil {

    public static void toastTuyaError(Activity activity, String msg) {
        if (activity == null) {
            return;
        }
        try {
            if (msg.equals(String.valueOf(103)) || msg.equals(String.valueOf(10203)) || msg.contains("Network error")) {
                ToastUtil.showToast(activity, NooieApplication.mCtx.getString(R.string.network_error0));
            } else {
                ToastUtil.showToast(activity, msg);
            }
        } catch (Exception e) {
            ToastUtil.showToast(activity, msg);
        }
    }
}
