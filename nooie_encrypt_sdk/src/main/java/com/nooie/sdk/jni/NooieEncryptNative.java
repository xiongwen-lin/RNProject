package com.nooie.sdk.jni;

public class NooieEncryptNative {

    static {
        System.loadLibrary("nooie-encrypt");
    }

    private static NooieEncryptNative nooieEncryptNative = new NooieEncryptNative();

    private NooieEncryptNative() {
        nativeInit();
    }

    public static NooieEncryptNative getInstance() {
        return nooieEncryptNative;
    }

    public void destroy() {
        nooieEncryptNative.nativeUninit();
    }

    private native int nativeInit();

    public native String nativeVersion();

    public native String nativeEncryptUID(String uid);

    public native int nativeUninit();
}
