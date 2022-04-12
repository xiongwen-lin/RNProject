package com.afar.osaio.smart.push.bean;

/**
 * JPushFeedbackMessageExtras
 *
 * @author Administrator
 * @date 2019/4/25
 */
public class PushFeedbackMessageExtras extends PushMessageBaseExtras {

    private String content;
    private String feed_type_name;
    private int feedback_status;
    private String pro_model;
    private String msg;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFeed_type_name() {
        return feed_type_name;
    }

    public void setFeed_type_name(String feed_type_name) {
        this.feed_type_name = feed_type_name;
    }

    public int getFeedback_status() {
        return feedback_status;
    }

    public void setFeedback_status(int feedback_status) {
        this.feedback_status = feedback_status;
    }

    public String getPro_model() {
        return pro_model;
    }

    public void setPro_model(String pro_model) {
        this.pro_model = pro_model;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
