package com.afar.osaio.bean;

import java.util.Map;

/**
 * DeviceCompatibleVersion
 *
 * @author Administrator
 * @date 2020/10/14
 */
public class DeviceCompatibleVersion {

    private String model;
    private String type;
    private String version;
    private Map<String, String> compatibleMinVersion;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, String> getCompatibleMinVersion() {
        return compatibleMinVersion;
    }

    public void setCompatibleMinVersion(Map<String, String> compatibleMinVersion) {
        this.compatibleMinVersion = compatibleMinVersion;
    }
}