package com.afar.osaio.smart.routerlocal;

public class RouterInfo {

    private String routerName;
    private String routerMac;
    private String isbind;
    private String isOnline;

    public RouterInfo(String routerName, String routerMac, String isbind, String isOnline) {
        this.routerName = routerName;
        this.routerMac = routerMac;
        this.isbind = isbind;
        this.isOnline = isOnline;
    }

    public RouterInfo(String routerName, String routerMac, String isbind) {
        this.routerName = routerName;
        this.routerMac = routerMac;
        this.isbind = isbind;
    }

    public String getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(String isOnline) {
        this.isOnline = isOnline;
    }

    public String getRouterName() {
        return routerName;
    }

    public void setRouterName(String routerName) {
        this.routerName = routerName;
    }

    public String getRouterMac() {
        return routerMac;
    }

    public void setRouterMac(String routerMac) {
        this.routerMac = routerMac;
    }

    public String getIsbind() {
        return isbind;
    }

    public void setIsbind(String isbind) {
        this.isbind = isbind;
    }
}
