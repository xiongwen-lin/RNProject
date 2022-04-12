package com.afar.osaio.protocol.response;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class CRecentSDRecordsResponse extends CResponseImpl {
    private List<Boolean> recentDayRecords = new ArrayList<>();

    public List<Boolean> getRecentDayRecords() {
        return recentDayRecords;
    }

    public void setRecentDayRecords(List<Boolean> recentDayRecords) {
        this.recentDayRecords = recentDayRecords;
    }

    @Override
    public Object getValue() {
        return recentDayRecords;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 0E 01 14 00 00 00 00 00 00 00 00 00 00 f8 0f                                                                   //
    //      f8          0f                                                                                            //
    //      1111 1000  0000 1111                                                                                      //
    // parse:                                                                                                         //
    //     0001 1111  1111 0000                                                                                       //
    // note: 从低位开始读,在byte中，低位 -> 高位正好对应日期的 远 -> 近， 即，如果低位表示10月10日，则高一位则表示10月11日        //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean parse(@NonNull byte[] data) {
        boolean result = super.parse(data);
        if (result) {
            byte temp;
            for (int i = 0; i < dataLen; i++) {
                temp = data[3 + i];

                for (int j = 0; j < 8; j++) {
                    recentDayRecords.add(((temp >> j) & 0x01) == 1);
                }
            }
        } else {
            return result;
        }
        return true;
    }
}
