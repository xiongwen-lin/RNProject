package com.nooie.sdk.encrypt;

import com.nooie.sdk.jni.NooieEncryptNative;

/**
 * Created by Victor on 2020/2/22
 * Email: victor.qiao.0604@gmail.com
 * Copyright © 2020年 Victor. All rights reserved.
 */
public class NooieEncryptService {
    private static NooieEncryptService manage = new NooieEncryptService();

    private NooieEncryptService() {
    }

    public static NooieEncryptService getInstance() {
        return manage;
    }


    public String getTuyaPsd(String uid) {
        return NooieEncryptNative.getInstance().nativeEncryptUID(uid);
    }
}
