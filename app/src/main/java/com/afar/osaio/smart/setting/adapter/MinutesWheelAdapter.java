package com.afar.osaio.smart.setting.adapter;

import com.contrarywind.adapter.WheelAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by victor on 2018/8/2
 * Email is victor.qiao.0604@gmail.com
 */
public class MinutesWheelAdapter implements WheelAdapter<String> {
    List<Integer> list;

    public MinutesWheelAdapter() {
        this.list = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            list.add(i);
        }
    }

    @Override
    public int getItemsCount() {
        return list.size();
    }

    @Override
    public String getItem(int index) {
        return String.format("%02d", list.get(index).intValue());
    }

    @Override
    public int indexOf(String o) {
        for (int i = 0; i < list.size(); i++) {
            if (o.equals(String.valueOf(list.get(i)))) {
                return i;
            }
        }
        return -1;
    }

    public int getValue(int index) {
        if (index > -1 && index < list.size()) {
            return list.get(index);
        } else {
            return 0;
        }
    }
}
