package com.afar.osaio.smart.electrician.adapter;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.bigkoo.convenientbanner.holder.Holder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.nooie.common.utils.graphics.DisplayUtil;

public class BannerHolderView extends Holder<String> {

    private ImageView mImageView;

    public BannerHolderView(View itemView) {
        super(itemView);
    }

    @Override
    protected void initView(View itemView) {
        mImageView = itemView.findViewById(R.id.item_image);
    }

    @Override
    public void updateUI(String data) {
        Glide.with(NooieApplication.mCtx).load(data)
                .transform(new MultiTransformation<Bitmap>(new CenterCrop(), new RoundedCorners(DisplayUtil.dpToPx(NooieApplication.mCtx, 15))))
                .placeholder(R.drawable.placeholder_map)
                .into(mImageView);
    }

}