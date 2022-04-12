package com.afar.osaio.smart.event;

import android.os.Bundle;

/**
 * AccountStateEvent
 *
 * @author Administrator
 * @date 2019/11/4
 */
public class AccountStateEvent {

    /**
     * ACCOUNT_STATE_REFRESH_AFTER_LOGIN 登录后不跳转首页时，全局刷新登录状态
     */
    public static final int ACCOUNT_STATE_REFRESH_AFTER_LOGIN = 1;
    public int state;
    public Bundle data;

    public AccountStateEvent(int state) {
        this.state = state;
    }

    public AccountStateEvent(int state, Bundle data) {
        this.state = state;
        this.data = data;
    }
}
