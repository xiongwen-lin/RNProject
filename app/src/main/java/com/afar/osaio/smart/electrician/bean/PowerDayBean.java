package com.afar.osaio.smart.electrician.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PowerDayBean implements Serializable {
    private List<String> days;
    private String total;
    private List<String> values;

    public PowerDayBean(){
        days = new ArrayList<>();
        values = new ArrayList<>();
    }

    public List<String> getDays() {
        return days;
    }

    public void setDays(List<String> days) {
        this.days = days;
    }
    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

}
