package com.afar.osaio.widget.listener;

import java.util.Calendar;

/**
 * Created by victor on 2018/7/13
 * Email is victor.qiao.0604@gmail.com
 */
public interface OnRecyclerItemClickListener {
    void onClickItem(Calendar object);

    void onClickItem(Calendar object, boolean isLive, long currentSeekDay);
}
