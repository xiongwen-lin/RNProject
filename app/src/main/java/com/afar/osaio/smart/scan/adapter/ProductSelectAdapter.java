package com.afar.osaio.smart.scan.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.adapter.BaseAdapter;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.bean.SelectProduct;
import com.afar.osaio.smart.scan.adapter.listener.ProductSelectListener;
import com.nooie.common.utils.collection.CollectionUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProductSelectAdapter extends BaseAdapter<SelectProduct, ProductSelectListener, ProductSelectAdapter.ProductSelectVH> {

    @Override
    public ProductSelectVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_product_select, parent, false);
        return new ProductSelectVH(view);
    }

    @Override
    public void onBindViewHolder(ProductSelectVH holder, int position) {
        SelectProduct selectProduct = mDatas.get(position);
        holder.btnProduct.setText(selectProduct.getName());
        holder.btnProduct.setBackgroundResource(selectProduct.isSelected() ? R.drawable.button_blue_with_round_6 : R.drawable.button_white_with_gray_round_6);
        holder.vProductSelectContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = holder.getAdapterPosition();
                updateSelectedItem(id);
                if (mListener != null) {
                    mListener.onItemClick(getItemByPosition(id));
                }
            }
        });
    }

    public void updateSelectedItem(int position) {
        if (position < 0) {
            return;
        }
        if (mDatas != null && position < mDatas.size()) {
            for (int i = 0; i < mDatas.size(); i++) {
                if (i == position && mDatas.get(i) != null) {
                    mDatas.get(i).setSelected(true);
                } else if (mDatas.get(i) != null) {
                    mDatas.get(i).setSelected(false);
                }
            }
            notifyDataSetChanged();
        }
    }

    public SelectProduct getItemByPosition(int position) {
        if (CollectionUtil.isEmpty(mDatas) || position >= mDatas.size()) {
            return null;
        }
        return mDatas.get(position);
    }

    public static class ProductSelectVH extends RecyclerView.ViewHolder {

        @BindView(R.id.vProductSelectContainer)
        View vProductSelectContainer;
        @BindView(R.id.btnProduct)
        TextView btnProduct;

        public ProductSelectVH(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
