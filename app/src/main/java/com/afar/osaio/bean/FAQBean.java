package com.afar.osaio.bean;

import java.util.List;

public class FAQBean {

    private String title;
    private String content;
    private List<FAQBean> children;
    private boolean isExpand;

    public FAQBean() {
    }

    public FAQBean(String title, String content, List<FAQBean> children, boolean isExpand) {
        this.title = title;
        this.content = content;
        this.children = children;
        this.isExpand = isExpand;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<FAQBean> getChildren() {
        return children;
    }

    public void setChildren(List<FAQBean> children) {
        this.children = children;
    }

    public boolean isExpand() {
        return isExpand;
    }

    public void setExpand(boolean expand) {
        isExpand = expand;
    }

    public void toggleExpand() {
        isExpand = !isExpand;
    }
}
