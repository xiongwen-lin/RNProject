package com.afar.osaio.smart.push.bean;

/**
 * JPushFeedbackMessageExtras
 *
 * @author Administrator
 * @date 2019/4/25
 */
public class PushNormalMessageExtras extends PushMessageBaseExtras {

    private String title;
    private String msg;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
