package com.afar.osaio.smart.scan.adapter;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.adapter.BaseAdapter;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.bean.SelectDeviceBean;
import com.afar.osaio.bean.SelectProduct;
import com.afar.osaio.smart.scan.adapter.listener.ModelSelectListener;
import com.afar.osaio.smart.scan.adapter.listener.ProductSelectListListener;
import com.afar.osaio.widget.helper.ResHelper;
import com.nooie.common.utils.collection.CollectionUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProductSelectListAdapter extends BaseAdapter<SelectProduct, ProductSelectListListener, ProductSelectListAdapter.ProductSelectVH> {

    @Override
    public ProductSelectVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_product_select_list, parent, false);
        return new ProductSelectVH(view);
    }

    @Override
    public void onBindViewHolder(ProductSelectVH holder, int position) {
        SelectProduct selectProduct = mDatas.get(position);
        holder.tvModelLabel.setText(NooieApplication.mCtx.getString(ResHelper.getInstance().getProductCategoryStringByType(selectProduct.getType())));
        bindModelListView(holder.rvModelSelect, selectProduct.getChildren());
    }

    private void bindModelListView(RecyclerView rvModelSelect, List<SelectDeviceBean> selectDevices) {
        if (rvModelSelect == null) {
            return;
        }
        ModelSelectAdapter modelSelectAdapter = new ModelSelectAdapter();
        modelSelectAdapter.setListener(new ModelSelectListener() {
            @Override
            public void onItemClick(SelectDeviceBean data) {
                if (mListener != null) {
                    mListener.onModelItemClick(data);
                }
            }

            @Override
            public void onItemLongClick(SelectDeviceBean data) {
                if (mListener != null) {
                    mListener.onModelItemClick(data);
                }
            }
        });
        GridLayoutManager modelLayoutManager = new GridLayoutManager(NooieApplication.mCtx, 3);
        rvModelSelect.setLayoutManager(modelLayoutManager);
        rvModelSelect.setAdapter(modelSelectAdapter);
        modelSelectAdapter.setData(CollectionUtil.safeFor(selectDevices));
    }

    public SelectProduct getItemByPosition(int position) {
        if (CollectionUtil.isEmpty(mDatas) || position >= mDatas.size()) {
            return null;
        }
        return mDatas.get(position);
    }

    public static class ProductSelectVH extends RecyclerView.ViewHolder {

        @BindView(R.id.vProductSelectListContainer)
        View vProductSelectListContainer;
        @BindView(R.id.tvModelLabel)
        TextView tvModelLabel;
        @BindView(R.id.rvModelSelect)
        RecyclerView rvModelSelect;

        public ProductSelectVH(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
