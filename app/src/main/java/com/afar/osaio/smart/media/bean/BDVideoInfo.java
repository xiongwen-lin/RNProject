package com.afar.osaio.smart.media.bean;

import com.boredream.bdvideoplayer.bean.IVideoInfo;

public class BDVideoInfo implements IVideoInfo {

    public String title;
    public String videoPath;

    @Override
    public String getVideoTitle() {
        return title;
    }

    @Override
    public String getVideoPath() {
        return videoPath;
    }
}
