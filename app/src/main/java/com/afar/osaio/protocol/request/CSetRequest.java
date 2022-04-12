package com.afar.osaio.protocol.request;

import com.afar.osaio.protocol.bean.Constant;

/**
 * CSetRequest
 *
 * @author Administrator
 * @date 2019/2/26
 */
public class CSetRequest extends CRequestImple {

    public CSetRequest(int key) {
        super(Constant.CMD_SET, key);
        this.len = 2;
    }
}
