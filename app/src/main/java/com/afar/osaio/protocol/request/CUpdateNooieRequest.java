package com.afar.osaio.protocol.request;

import android.text.TextUtils;

import com.afar.osaio.protocol.bean.Constant;

/**
 * CUpdateNooieRequest
 *
 * @author Administrator
 * @date 2019/5/7
 */
public class CUpdateNooieRequest extends CRequestImple {

    private String uid;
    private String timezone;
    private String area;

    public CUpdateNooieRequest(String uid, String timezone, String area) {
        super(Constant.CMD_SET, Constant.KEY_CAMERA_UPDATE_NOOIE);
        this.uid = uid;
        this.timezone = timezone;
        this.area = area;
        this.len = 0x1a;
    }

    @Override
    public boolean buildCmd() {
        boolean result = super.buildCmd();
        if (result && isValueValid(uid, 16) && isValueValid(timezone, 6) && isValueValid(area, 2)) {
            byte[] uidBytes = uid.getBytes();
            for (int i = 0; i < 16; i++) {
                data[3 + i] = uidBytes[i];
            }

            byte[] timezoneBytes = timezone.getBytes();
            for (int i = 0; i < 6; i++) {
                data[19 + i] = timezoneBytes[i];
            }

            byte[] areaBytes = area.getBytes();
            for (int i = 0; i < 2; i++) {
                data[25 + i] = areaBytes[i];
            }
        } else {
            result = false;
        }

        return result;
    }

    public boolean isValueValid(String value, int len) {
        return !TextUtils.isEmpty(value) && value.length() == len;
    }
}
