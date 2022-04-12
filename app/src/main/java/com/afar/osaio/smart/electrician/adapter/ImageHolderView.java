package com.afar.osaio.smart.electrician.adapter;

import android.view.View;
import android.widget.ImageView;

import com.afar.osaio.R;
import com.bigkoo.convenientbanner.holder.Holder;

public class ImageHolderView extends Holder<Integer> {

    private ImageView mImageView;

    public ImageHolderView(View itemView) {
        super(itemView);
    }

    @Override
    protected void initView(View itemView) {
        mImageView = itemView.findViewById(R.id.item_image);
    }

    @Override
    public void updateUI(Integer data) {
        mImageView.setImageResource(data);
    }

}