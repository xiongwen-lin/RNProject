package com.afar.osaio.smart.electrician.bean;

import java.io.Serializable;
import java.util.Map;

public class PowerBean implements Serializable {
    private String sum;
    private String thisDay;
    private Map<String,Map<String,String>> years;

    public String getSum() {
        return sum;
    }

    public void setSum(String sum) {
        this.sum = sum;
    }

    public String getThisDay() {
        return thisDay;
    }

    public void setThisDay(String thisDay) {
        this.thisDay = thisDay;
    }

    public Map<String, Map<String, String>> getYears() {
        return years;
    }

    public void setYears(Map<String, Map<String, String>> years) {
        this.years = years;
    }

}
