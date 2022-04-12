package com.afar.osaio.bean;

public enum ProductType {

    PRODUCT_IPC("IPC"),
    PRODUCT_BLE_AP_IPC("BLE_AP_IPC"),
    PRODUCT_LOCK("LOCK"),
    PRODUCT_UNKNOWN("UNKNOWN");

    private static final String PRODUCT_TYPE_IPC = "IPC";
    private static final String PRODUCT_TYPE_BLE_AP_IPC = "BLE_AP_IPC";
    private static final String PRODUCT_TYPE_LOCK = "LOCK";

    private String type;

    private ProductType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public static ProductType getType(String type) {
        if (PRODUCT_TYPE_IPC.equalsIgnoreCase(type)) {
            return PRODUCT_IPC;
        } else if (PRODUCT_TYPE_BLE_AP_IPC.equalsIgnoreCase(type)) {
            return PRODUCT_BLE_AP_IPC;
        } else if (PRODUCT_TYPE_LOCK.equalsIgnoreCase(type)) {
            return PRODUCT_LOCK;
        } else {
            return PRODUCT_UNKNOWN;
        }
    }
}
