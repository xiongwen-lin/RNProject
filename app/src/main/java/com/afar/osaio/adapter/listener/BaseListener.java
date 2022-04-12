package com.afar.osaio.adapter.listener;

public interface BaseListener<T> {

    void onItemClick(T data);

    void onItemLongClick(T data);

}
