package com.afar.osaio.smart.setting.adapter.listener;

import androidx.recyclerview.widget.RecyclerView;

import com.nooie.sdk.api.network.base.bean.entity.PresetPointConfigure;

public interface PresetPointListener {

    void onStartDragItem(RecyclerView.ViewHolder holder);

    void onItemClick(int position, PresetPointConfigure presetPointConfigure);

    void onItemDeleteClick(int position, PresetPointConfigure presetPointConfigure);

    void onItemEditClick(int position, PresetPointConfigure presetPointConfigure);

    void onItemAddClick(int presetPointPosition);
}
