package com.example.communicare_mobile;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.example.communicare_mobile.model.TileModel;

import java.util.List;

public class TileModelDiffCallback extends DiffUtil.Callback {

    private final List<TileModel> mOldTileModelList;
    private final List<TileModel> mNewTileModelList;

    public TileModelDiffCallback(List<TileModel> oldTileModelList, List<TileModel> newTileModelList){
        this.mOldTileModelList = oldTileModelList;
        this.mNewTileModelList = newTileModelList;
    }

    @Override
    public int getOldListSize() {
        return mOldTileModelList.size();
    }

    @Override
    public int getNewListSize() {
        return mNewTileModelList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {

        return mOldTileModelList.get(oldItemPosition).getId() == mNewTileModelList.get(newItemPosition).getId();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        final TileModel oldTileModel = mOldTileModelList.get(oldItemPosition);
        final TileModel newTileModel = mOldTileModelList.get(newItemPosition);
        return oldTileModel.getLabel().equals(newTileModel.getLabel());
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {

        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
