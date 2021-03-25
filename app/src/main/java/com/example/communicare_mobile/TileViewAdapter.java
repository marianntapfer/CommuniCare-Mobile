package com.example.communicare_mobile;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.communicare_mobile.model.TileModel;

import java.util.List;

public class TileViewAdapter extends RecyclerView.Adapter<TileViewAdapter.ViewHolder> {
    private static final String TAG = "TileViewAdapter";

    private static List<TileModel> localDataSet;
    private OnAdapterItemClickListener adapterItemClickListener;
    public String labelLang = "english";


    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final Button tileView;

        public ViewHolder(View v) {
            super(v);
            // Define click listener for the ViewHolder's View.
            tileView = v.findViewById(R.id.tileView);
            tileView.setOnClickListener(this);
        }

        public Button getTileView() {
            return tileView;
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG, "Element " + getAdapterPosition() + " clicked.");
            TileModel tile = localDataSet.get(getAdapterPosition());
            adapterItemClickListener.onAdapterItemClickListener(tile);
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     *                by RecyclerView.
     */
    public TileViewAdapter(List<TileModel> dataSet, OnAdapterItemClickListener listener) {
        localDataSet = dataSet;
        this.adapterItemClickListener = listener;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a new view, which defines the UI of the list item
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tile_view_item, parent, false);
        int h = parent.getHeight() == 0 ? 600 : parent.getHeight();
        // margin - activity_vertical_margin
        // rows - number of rows in diffrent display modes
        // h = (h - Math.round(margin * 2)) / rows;
        // TODO: Implement dynamic tile generation based on item count
        int rows = getItemCount() / 2;
        h = (h - Math.round(40 * 2)) / rows;

        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemView.getLayoutParams();
        params.height = h;
        itemView.setLayoutParams(params);

        return new ViewHolder(itemView);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Log.d(TAG, "Element " + position + " set.");

        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        // TODO: implement translation query based on the label

        viewHolder.getTileView().setText(localDataSet.get(position).getLabel());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

    public void updateTileModelListItems(List<TileModel> tiles) {
        final TileModelDiffCallback diffCallback = new TileModelDiffCallback(this.localDataSet, tiles);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.localDataSet.clear();
        this.localDataSet.addAll(tiles);
        diffResult.dispatchUpdatesTo(this);

    }


}
