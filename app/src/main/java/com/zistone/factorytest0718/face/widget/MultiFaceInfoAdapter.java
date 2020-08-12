package com.zistone.factorytest0718.face.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.zistone.factorytest0718.R;
import com.zistone.factorytest0718.face.model.ItemShowInfo;

import java.util.List;

/**
 * 人脸比对1:N界面中用到的adapter
 */
public class MultiFaceInfoAdapter extends RecyclerView.Adapter<MultiFaceInfoAdapter.ShowInfoHolder> {

    class ShowInfoHolder extends RecyclerView.ViewHolder {
        ImageView _image;
        TextView _txt;

        ShowInfoHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    private List<ItemShowInfo> _list;
    private LayoutInflater _layoutInflater;

    public MultiFaceInfoAdapter(List<ItemShowInfo> _list, Context context) {
        this._list = _list;
        this._layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ShowInfoHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = _layoutInflater.inflate(R.layout.activity_face_compare_item_multi_face_info, viewGroup, false);
        ImageView image = itemView.findViewById(R.id.img_face_compare_item_multi_face_info);
        TextView txt = itemView.findViewById(R.id.txt_face_compare_item_multi_face_info);
        ShowInfoHolder holder = new ShowInfoHolder(itemView);
        holder._image = image;
        holder._txt = txt;
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ShowInfoHolder showInfoHolder, int i) {
        showInfoHolder._txt.setText(_list.get(i).toString());
        Glide.with(showInfoHolder._image.getContext()).load(_list.get(i).getBitmap()).into(showInfoHolder._image);
    }

    @Override
    public int getItemCount() {
        return _list == null ? 0 : _list.size();
    }

}
