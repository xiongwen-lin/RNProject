package com.afar.osaio.smart.electrician.eventbus;

public class WeatherEvent {

    private String condition;
    private String temp;
    private String iconUrl;

    public WeatherEvent(String condition, String temp, String iconUrl) {
        this.condition = condition;
        this.temp = temp;
        this.iconUrl = iconUrl;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    @Override
    public String toString() {
        return "WeatherEvent{" +
                "condition='" + condition + '\'' +
                ", temp='" + temp + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                '}';
    }
}
