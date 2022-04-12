package com.afar.osaio.bean;

import java.util.List;
import java.util.Map;

public class FAQMap {
    private Map<String, List<FAQBean>> data;

    public Map<String, List<FAQBean>> getData() {
        return data;
    }

    public void setData(Map<String, List<FAQBean>> data) {
        this.data = data;
    }
}
