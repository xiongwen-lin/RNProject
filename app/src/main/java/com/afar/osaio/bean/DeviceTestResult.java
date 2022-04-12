package com.afar.osaio.bean;

public class DeviceTestResult {

    private int upgradeCount;
    private int sendUpgradeCmdSuccessCount;
    private int sendUpgradeCmdFailCount;
    private int upgradeSuccessCount;
    private int upgradeFailCount;

    public int getUpgradeCount() {
        return upgradeCount;
    }

    public void setUpgradeCount(int upgradeCount) {
        this.upgradeCount = upgradeCount;
    }

    public void addUpgradeCount() {
        this.upgradeCount++;
    }

    public int getSendUpgradeCmdSuccessCount() {
        return sendUpgradeCmdSuccessCount;
    }

    public void setSendUpgradeCmdSuccessCount(int sendUpgradeCmdSuccessCount) {
        this.sendUpgradeCmdSuccessCount = sendUpgradeCmdSuccessCount;
    }

    public void addSendUpgradeCmdSuccessCount() {
        this.sendUpgradeCmdSuccessCount++;
    }

    public int getSendUpgradeCmdFailCount() {
        return sendUpgradeCmdFailCount;
    }

    public void setSendUpgradeCmdFailCount(int sendUpgradeCmdFailCount) {
        this.sendUpgradeCmdFailCount = sendUpgradeCmdFailCount;
    }

    public void addSendUpgradeCmdFailCount() {
        this.sendUpgradeCmdFailCount++;
    }

    public int getUpgradeSuccessCount() {
        return upgradeSuccessCount;
    }

    public void setUpgradeSuccessCount(int upgradeSuccessCount) {
        this.upgradeSuccessCount = upgradeSuccessCount;
    }

    public void addUpgradeSuccessCount() {
        this.upgradeSuccessCount++;
    }

    public int getUpgradeFailCount() {
        return upgradeFailCount;
    }

    public void setUpgradeFailCount(int upgradeFailCount) {
        this.upgradeFailCount = upgradeFailCount;
    }

    public void addUpgradeFailCount() {
        this.upgradeFailCount++;
    }

    public void reset() {
        setUpgradeCount(0);
        setSendUpgradeCmdSuccessCount(0);
        setSendUpgradeCmdFailCount(0);
        setUpgradeSuccessCount(0);
        setUpgradeFailCount(0);
    }

    public String log() {
        StringBuilder resultSb = new StringBuilder();
        resultSb.append("升级次数:");
        resultSb.append(getUpgradeCount());
        resultSb.append("\n");
        resultSb.append("发送升级命令成功次数:");
        resultSb.append(getSendUpgradeCmdSuccessCount());
        resultSb.append("\n");
        resultSb.append("发送升级命令失败或超时次数:");
        resultSb.append(getSendUpgradeCmdFailCount());
        resultSb.append("\n");
        /*
        resultSb.append("升级成功次数:");
        resultSb.append(getUpgradeSuccessCount());
        resultSb.append("\n");
        resultSb.append("升级失败次数:");
        resultSb.append(getUpgradeFailCount());
        resultSb.append("\n");
        */
        return resultSb.toString();
    }
}
