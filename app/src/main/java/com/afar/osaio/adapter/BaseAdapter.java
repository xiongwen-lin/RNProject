package com.afar.osaio.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afar.osaio.base.NooieApplication;
import com.nooie.common.utils.collection.CollectionUtil;

import java.util.ArrayList;
import java.util.List;

public class BaseAdapter<T, L, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    public List<T> mDatas = new ArrayList<>();
    public L mListener;

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
    }

    @Override
    public int getItemCount() {
        return CollectionUtil.size(mDatas);
    }

    public View createVHView(int layoutId, ViewGroup parent) {
        return LayoutInflater.from(NooieApplication.mCtx).inflate(layoutId, parent, false);
    }

    public void setData(List<T> datas) {
        if (mDatas == null) {
            mDatas = new ArrayList<>();
        }

        mDatas.clear();
        mDatas.addAll(CollectionUtil.safeFor(datas));
        notifyDataSetChanged();
    }

    public void appendData(List<T> datas) {
        if (mDatas == null) {
            mDatas = new ArrayList<>();
        }

        mDatas.addAll(CollectionUtil.safeFor(datas));
        notifyDataSetChanged();
    }

    public void clearData() {
        if (mDatas != null) {
            mDatas.clear();
        }
        notifyDataSetChanged();
    }

    public List<T> getData() {
        return mDatas;
    }

    public T getDataByPosition(int position) {
        if (CollectionUtil.isEmpty(mDatas) || !CollectionUtil.isIndexSafe(position, mDatas.size())) {
            return null;
        }
        return mDatas.get(position);
    }

    public void setListener(L listener) {
        mListener = listener;
    }

    public void release() {
        if (mDatas != null) {
            mDatas.clear();
            mDatas = null;
        }

        if (mListener != null) {
            mListener = null;
        }
    }
}
