package com.afar.osaio.smart.media.adapter;

import android.view.View;

import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.bigkoo.convenientbanner.holder.Holder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;
import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.media.bean.CameraPhotoMediaBean;
import com.github.chrisbanes.photoview.PhotoView;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class PreviewCameraPhotoAdapter implements CBViewHolderCreator {

    @Override
    public Holder createHolder(View itemView) {
        return new PreviewImageHolderView(itemView);
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_media_preview_image;
    }

    private static class PreviewImageHolderView extends Holder<CameraPhotoMediaBean> {
        private PhotoView pvPreviewImage;

        public PreviewImageHolderView(View view) {
            super(view);
        }

        @Override
        protected void initView(View itemView) {
            pvPreviewImage = (PhotoView) itemView.findViewById(R.id.pvPreviewImage);
        }

        @Override
        public void updateUI(CameraPhotoMediaBean data) {
            if (data != null && pvPreviewImage != null) {
                Glide.with(NooieApplication.mCtx)
                        .load(data.getPath())
                        .apply(new RequestOptions()
                                //.dontTransform().transform(new MultiTransformation<Bitmap>(new CenterCrop(), new RoundedCorners(DisplayUtil.dpToPx(NooieApplication.mCtx, 5))))
                                .format(DecodeFormat.PREFER_RGB_565)
                                .error(R.drawable.default_preview)
                        )
                        .transition(withCrossFade())
                        .into(pvPreviewImage);
            }
        }
    }

}
