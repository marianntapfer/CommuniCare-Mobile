package com.example.communicare_mobile;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.communicare_mobile.model.TileModel;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class TileViewAdapter extends RecyclerView.Adapter<TileViewAdapter.ViewHolder> {
    private static final String TAG = "TileViewAdapter";

    private static List<TileModel> localDataSet;
    private OnAdapterItemClickListener adapterItemClickListener;
    RecyclerViewFragment context;


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
     * @param context
     */
    public TileViewAdapter(List<TileModel> dataSet, OnAdapterItemClickListener listener, RecyclerViewFragment context) {
        localDataSet = dataSet;
        this.adapterItemClickListener = listener;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a new view, which defines the UI of the list item
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tile_view_item, parent, false);
        DisplayMetrics displayMetrics = itemView.getContext().getResources().getDisplayMetrics();
        int dpHeight = (int) (displayMetrics.heightPixels * 0.72);

        int h = parent.getHeight() == 0 ? dpHeight : parent.getHeight();
        // margin - activity_vertical_margin
        // rows - number of rows in different display modes
        // h = (h - Math.round(margin * 2)) / rows;
        int rows = getItemCount() > 5 ? getItemCount() / 2 : getItemCount();
        h = (h - Math.round(25 * 2)) / rows;

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
        Button tile = viewHolder.getTileView();
        tile.setText(localDataSet.get(position).getLabel());

        String drawable = localDataSet.get(position).getDrawable();
//        this works
//        Drawable drawableIcon = context.getResources().getDrawable(R.drawable.home);
//        drawableIcon.setBounds(0,0,0,0);

//        this does not work

        AssetManager manager = context.getActivity().getAssets();

        InputStream open = null;

        try {
            open = manager.open(drawable);
            Bitmap bitmap = BitmapFactory.decodeStream(open);
            int height = (int) (tile.getMinHeight() * 1.25);
            int width = tile.getMinWidth();
            Drawable icon = new BitmapDrawable(context.getResources(), scaleBitmapAndKeepRation(bitmap, height, width));

            viewHolder.getTileView().setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (open != null) {
                try {
                    open.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

    public static Bitmap scaleBitmapAndKeepRation(Bitmap targetBmp, int reqHeightInPixels, int reqWidthInPixels) {
        Matrix matrix = new Matrix();
        matrix.setRectToRect(new RectF(0, 0, targetBmp.getWidth(), targetBmp.getHeight()), new RectF(0, 0, reqWidthInPixels, reqHeightInPixels), Matrix.ScaleToFit.CENTER);
        return Bitmap.createBitmap(targetBmp, 0, 0, targetBmp.getWidth(), targetBmp.getHeight(), matrix, true);
    }
}
