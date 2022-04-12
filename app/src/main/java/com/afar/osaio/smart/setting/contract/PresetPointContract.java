package com.afar.osaio.smart.setting.contract;

import androidx.annotation.NonNull;

import com.nooie.sdk.api.network.base.bean.entity.PresetPointConfigure;

import java.util.List;

public interface PresetPointContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void onCompressScreenShot(String account, String deviceId, String thumbnailPath);

        void onGetPresetPoints(int result, List<PresetPointConfigure> presetPointConfigures);

        void onCheckAddPresetPointPosition(int result, String name, int position, List<PresetPointConfigure> presetPointConfigures);

        void onAddPresetPoint(int result, List<PresetPointConfigure> presetPointConfigures);

        void onSortPresetPointConfigureList(int result, String deviceId);

        void onEditPresetPointConfigure(int result, String deviceId, PresetPointConfigure presetPointConfigure);

        void onDeletePresetPointConfigure(int result, String deviceId, PresetPointConfigure presetPointConfigure);

        void onTurnPresetPoint(int result, String deviceId);
    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void startCompressScreenShot(String account, String deviceId, String thumbnailPath);

        void getPresetPoints(String deviceId, boolean isSyncToDevice);

        void checkAddPresetPointPosition(String deviceId, String name, int position);

        void addPresetPoint(String account, String deviceId, boolean isSetOnPower, String name, int position, String tempPresetPointPath, String presetPointPath);

        void sortPresetPointConfigureList(String deviceId, List<PresetPointConfigure> presetPointConfigures);

        void editPresetPointConfigure(String deviceId, PresetPointConfigure presetPointConfigure);

        void deletePresetPointConfigure(String account, String deviceId, PresetPointConfigure presetPointConfigure);

        void turnPresetPoint(String deviceId, PresetPointConfigure presetPointConfigure);
    }
}
