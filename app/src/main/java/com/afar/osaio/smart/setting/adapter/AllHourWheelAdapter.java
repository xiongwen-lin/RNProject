package com.afar.osaio.smart.setting.adapter;

import com.contrarywind.adapter.WheelAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * HourWheelAdapter
 *
 * @author Administrator
 * @date 2019/3/11
 */
public class AllHourWheelAdapter implements WheelAdapter<String> {

    private static int maxTime = 24;
    List<Integer> list;

    public AllHourWheelAdapter() {
        this.list = new ArrayList<>();
        for (int i = 0; i <= maxTime; i++) {
            list.add(i);
        }
    }

    @Override
    public int getItemsCount() {
        return list.size();
    }

    @Override
    public String getItem(int index) {
        return String.valueOf(list.get(index));
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
