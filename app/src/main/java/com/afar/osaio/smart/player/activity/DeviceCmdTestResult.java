package com.afar.osaio.smart.player.activity;

import com.nooie.sdk.bean.SDKConstant;

/**
 * DeviceCmdApiTestResult
 *
 * @author Administrator
 * @date 2020/8/21
 */
public class DeviceCmdTestResult<T> {

    private String key;
    private int code = SDKConstant.CODE_INIT;
    private int state;
    private T result;
    private T cacheResult;

    public DeviceCmdTestResult() {
        this.code = SDKConstant.CODE_INIT;
    }

    public DeviceCmdTestResult(String key) {
        this.key = key;
        this.code = SDKConstant.CODE_INIT;
    }

    public DeviceCmdTestResult(String key, int code, int state, T result, T cacheResult) {
        this.key = key;
        this.code = code;
        this.state = state;
        this.result = result;
        this.cacheResult = result;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public T getCacheResult() {
        return cacheResult;
    }

    public void setCacheResult(T cacheResult) {
        this.cacheResult = cacheResult;
    }
}