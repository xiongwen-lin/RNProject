package com.afar.osaio.smart.push.bean;

import android.text.TextUtils;

import com.google.gson.Gson;

/**
 * JPushCustomMessage
 *
 * @author Administrator
 * @date 2019/4/25
 */
public class PushCustomMessage<T> {

    private String message;
    private T extras;

    public PushCustomMessage() {
    }

    public PushCustomMessage(String msg, String extras, Class<T> clazz) {
        setMessage(msg);
        setExtras(convertExtras(extras, clazz));
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getExtras() {
        return extras;
    }

    public void setExtras(T extras) {
        this.extras = extras;
    }

    public T convertExtras(String extras, Class<T> clazz) {
        T result = null;
        Gson gson = new Gson();
        if (!TextUtils.isEmpty(extras)) {
            try {
                result = gson.fromJson(extras, clazz);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }
}
