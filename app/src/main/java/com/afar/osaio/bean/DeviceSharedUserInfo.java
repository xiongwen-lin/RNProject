package com.afar.osaio.bean;

import java.io.Serializable;
import java.util.List;

public class DeviceSharedUserInfo implements Serializable {

    private String userAccount;
    private String userAlias;
    private String headIconUrl;
    private String userId;
    private List<String> deviceIdList;

    public DeviceSharedUserInfo() {
    }

    public String getUserAccount() {
        return this.userAccount;
    }

    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
    }

    public String getUserAlias() {
        return this.userAlias;
    }

    public void setUserAlias(String userAlias) {
        this.userAlias = userAlias;
    }

    public String getHeadIconUrl() {
        return this.headIconUrl;
    }

    public void setHeadIconUrl(String headIconUrl) {
        this.headIconUrl = headIconUrl;
    }

    public List<String> getDeviceIdList() {
        return this.deviceIdList;
    }

    public void setDeviceIdList(List<String> deviceIdList) {
        this.deviceIdList = deviceIdList;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return this.userId;
    }

}
