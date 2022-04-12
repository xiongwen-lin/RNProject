package com.afar.osaio.smart.smartlook.bean;

public class BaseBleCmd {

    public BaseBleCmd(short[] cmdBytes) {
        setCmdBytes(cmdBytes);
    }

    public short[] cmdBytes;

    public void setCmdBytes(short[] cmdBytes) {
        this.cmdBytes = cmdBytes;
    }

    public short[] getCmdBytes() {
        return this.cmdBytes;
    }

}
