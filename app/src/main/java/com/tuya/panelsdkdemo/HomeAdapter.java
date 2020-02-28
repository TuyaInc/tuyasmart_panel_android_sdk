package com.tuya.panelsdkdemo;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.tuya.panelsdkdemo.bean.ItemBean;

import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter {
    private List<ItemBean> mData;

    private OnItemClickListener mOnItemClickListener;

    public HomeAdapter(List<ItemBean> data) {
        this.mData = data;
    }

    public void setData(List<ItemBean> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_rv_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        final ItemBean bean = mData.get(position);
        if (bean == null) {
            return;
        }
        ViewHolder viewHolder = (ViewHolder) holder;
        if (!TextUtils.isEmpty(bean.getIconUrl())) {
            Uri uri = Uri.parse(bean.getIconUrl());
            viewHolder.icon.setImageURI(uri);

        }
        viewHolder.title.setText(bean.getTitle());
        viewHolder.itemLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(bean, holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        this.mOnItemClickListener = l;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout itemLay;
        SimpleDraweeView icon;
        TextView title;

        public ViewHolder(View itemView) {
            super(itemView);
            itemLay = itemView.findViewById(R.id.ll_item);
            icon = itemView.findViewById(R.id.sd_icon);
            title = itemView.findViewById(R.id.tv_title);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(ItemBean bean, int position);
    }
}
