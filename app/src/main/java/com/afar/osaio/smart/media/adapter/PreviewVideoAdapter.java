package com.afar.osaio.smart.media.adapter;

import android.view.View;

import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.bigkoo.convenientbanner.holder.Holder;
import com.boredream.bdvideoplayer.CustomVideoView;
import com.afar.osaio.R;
import com.afar.osaio.smart.media.bean.ImageMediaBean;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class PreviewVideoAdapter implements CBViewHolderCreator {

    @Override
    public Holder createHolder(View itemView) {
        return new PreviewVideoHolderView(itemView);
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_media_preview_video;
    }

    private static class PreviewVideoHolderView extends Holder<ImageMediaBean> {
        private CustomVideoView cvvPreviewVideo;

        public PreviewVideoHolderView(View view) {
            super(view);
        }

        @Override
        protected void initView(View itemView) {
            cvvPreviewVideo = (CustomVideoView) itemView.findViewById(R.id.cvvPreviewVideo);
        }

        @Override
        public void updateUI(ImageMediaBean data) {
            if (data != null && cvvPreviewVideo != null) {
            }
        }
    }

}
