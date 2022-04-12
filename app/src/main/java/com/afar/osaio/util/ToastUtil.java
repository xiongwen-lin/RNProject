package com.afar.osaio.util;

import android.app.Activity;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.TextView;

import com.afar.osaio.R;
import com.nooie.common.utils.log.NooieLog;
import com.tapadoo.alerter.Alerter;

/**
 * Created by victor on 2018/6/27
 * Email is victor.qiao.0604@gmail.com
 */
public class ToastUtil {

    private static final int SHORT_TOAST_DURATION = 1000;
    private static final int LONG_TOAST_DURATION = 2000;

    public static void showToast(Activity activity, String msg) {
        if (Alerter.isShowing()) {
            Alerter.hide();
        }
        showCustomToast(activity, msg, SHORT_TOAST_DURATION);
    }

    public static void showToast(Activity activity, int rStringId) {
        if (Alerter.isShowing()) {
            Alerter.hide();
        }
        showCustomToast(activity, rStringId, SHORT_TOAST_DURATION);
    }


    public static void showToast(Activity activity, int rStringId, int duration) {
        if (Alerter.isShowing()) {
            Alerter.hide();
        }
        showCustomToast(activity, rStringId, duration);
    }

    public static void showLongToast(Activity activity, String msg) {
        if (Alerter.isShowing()) {
            Alerter.hide();
        }
        showCustomToast(activity, msg, LONG_TOAST_DURATION);
    }

    public static void showLongToast(Activity activity, int rStringId) {
        if (Alerter.isShowing()) {
            Alerter.hide();
        }
        showCustomToast(activity, rStringId, LONG_TOAST_DURATION);
    }

    public static void showLongToast(Activity activity, int rStringId, long duration) {
        if (Alerter.isShowing()) {
            Alerter.hide();
        }
        duration = duration < 1 ? LONG_TOAST_DURATION : duration;
        showCustomToast(activity, rStringId, duration);
    }

    public static void showCustomToast(Activity activity, String msg, long duration) {
        if (activity == null || TextUtils.isEmpty(msg)) {
            return;
        }
        if (duration < 1) {
            duration = SHORT_TOAST_DURATION;
        }
        try {
            Alerter alerter = Alerter.create(activity, R.layout.layout_alert_text)
                    .setBackgroundColorRes(R.color.transparent)
                    .enableVibration(false)
                    .setDuration(duration);
            TextView tvAlertContent = alerter.getLayoutContainer().findViewById(R.id.tvAlertContent);
            tvAlertContent.setText(msg);
            alerter.show();
            return;
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }

        Alerter.create(activity)
                .setBackgroundColorRes(R.color.toast_bg)
                .hideIcon()
                .setText(msg)
                .setTextAppearance(R.style.toast_text)
                .setContentGravity(Gravity.CENTER_HORIZONTAL)
                .enableVibration(false)
                .setDuration(duration)
                .show();
    }

    public static void showCustomToast(Activity activity, int textResId, long duration) {
        if (activity == null) {
            return;
        }
        String msg = new String();
        try {
            msg = activity.getString(textResId);
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
        if (TextUtils.isEmpty(msg)) {
            return;
        }
        showCustomToast(activity, msg, duration);
    }
}
