package com.afar.osaio.smart.push.bean;

/**
 * JPushFeedbackMessageExtras
 *
 * @author Administrator
 * @date 2019/4/25
 */
public class PushActiveMessageExtras extends PushMessageBaseExtras {

    private String url;
    private String msg;
    private String title;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
