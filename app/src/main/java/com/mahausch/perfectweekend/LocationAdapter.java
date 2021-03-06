package com.mahausch.perfectweekend;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mahausch.perfectweekend.data.LocationContract.LocationEntry;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    private Context mContext;
    private Cursor mCursor;
    private final LocationItemClickListener locationItemClickListener;


    public LocationAdapter(Context context, Cursor cursor,
                           LocationItemClickListener locationItemClickListener) {
        this.mContext = context;
        this.mCursor = cursor;
        this.locationItemClickListener = locationItemClickListener;
    }

    //Called when RecyclerView needs a new ViewHolder of the given type to represent an item
    @Override
    public LocationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Get the RecyclerView item layout
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.overview_list_item, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final LocationViewHolder holder, final int position) {

        mCursor.moveToPosition(position);
        int idIndex = mCursor.getColumnIndex(LocationEntry._ID);
        int nameIndex = mCursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_NAME);
        int imageIndex = mCursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_IMAGE);

        long locationId = mCursor.getLong(idIndex);
        String locationName = mCursor.getString(nameIndex);
        String locationImage = mCursor.getString(imageIndex);

        holder.overviewName.setText(String.valueOf(locationName));
        Picasso.get().load(Uri.parse(locationImage)).fit().centerCrop().into(holder.overviewImage);
        holder.overviewImage.setTag(locationId);
        ViewCompat.setTransitionName(holder.overviewImage, locationName);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationItemClickListener.onLocationItemClick(position, holder.overviewImage);
            }
        });
    }

    public interface LocationItemClickListener {
        void onLocationItemClick(int position, ImageView imageView);
    }

    public void swapCursor(Cursor newCursor) {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = newCursor;
        if (mCursor != null) {
            // Force the RecyclerView to refresh
            this.notifyDataSetChanged();
        }
    }

    //Returns the number of items in the cursor
    @Override
    public int getItemCount() {
        if (mCursor == null) return 0;
        return mCursor.getCount();
    }

    //LocationViewHolder class for the recycler view item
    class LocationViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.overview_image)
        ImageView overviewImage;

        @BindView(R.id.overview_name)
        TextView overviewName;

        public LocationViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }
}

