package com.afar.osaio.smart.electrician.eventbus;

public class StyleEvent {
    private String styleColor;

    public StyleEvent(String color) {
        this.styleColor = color;
    }

    public String getStyleColor() {
        return styleColor;
    }

    public void setStyleColor(String styleColor) {
        this.styleColor = styleColor;
    }
}
