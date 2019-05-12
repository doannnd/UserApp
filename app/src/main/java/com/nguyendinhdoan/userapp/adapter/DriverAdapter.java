package com.nguyendinhdoan.userapp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.nguyendinhdoan.userapp.R;
import com.nguyendinhdoan.userapp.model.Driver;

import java.util.List;

public class DriverAdapter extends RecyclerView.Adapter<DriverAdapter.ViewHolder> {

    private Context context;
    private List<Driver> driverList;
    private String distance;

    public DriverAdapter(Context context, List<Driver> driverList, String distance) {
        this.context = context;
        this.driverList = driverList;
        this.distance = distance;
    }

    @NonNull
    @Override
    public DriverAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.recycler_view_item, viewGroup, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DriverAdapter.ViewHolder viewHolder, int position) {
        Driver driver = driverList.get(position);

        // load avatar
        Glide.with(context).load(driver.getAvatarUrl())
                .placeholder(R.drawable.ic_profile)
                .into(viewHolder.avatarImageView);
        // load data for text view
        viewHolder.nameTextView.setText(driver.getName());
        viewHolder.vehicleNameTextView.setText(driver.getVehicleName());
        viewHolder.starTextView.setText(driver.getRates());
        // load trip price
        calculateTripFee(viewHolder, driver);
    }

    private void calculateTripFee(ViewHolder viewHolder, Driver driver) {
        String[] distances = distance.split(" ");
        String valueDistance = distances[0];
        String unitDistance = distances[1];

        switch (unitDistance) {
            case "m": {
                viewHolder.priceTextView.setText(context.getString(R.string.price_text, driver.getZeroToTwo()));
                break;
            }
            case "km": {
                double valueDistanceFormat = Double.parseDouble(valueDistance);
                if (valueDistanceFormat <= 2) {
                    double price = valueDistanceFormat * Double.parseDouble(driver.getZeroToTwo());
                    viewHolder.priceTextView.setText(context.getString(R.string.price_text, String.valueOf(price)));
                } else if (valueDistanceFormat > 2 && valueDistanceFormat <= 10) {
                    double price = valueDistanceFormat * Double.parseDouble(driver.getThreeToTen());
                    viewHolder.priceTextView.setText(context.getString(R.string.price_text, String.valueOf(price)));
                } else if (valueDistanceFormat > 10 && valueDistanceFormat <= 20) {
                    double price = valueDistanceFormat * Double.parseDouble(driver.getElevenToTwenty());
                    viewHolder.priceTextView.setText(context.getString(R.string.price_text, String.valueOf(price)));
                } else if (valueDistanceFormat > 20) {
                    double price = valueDistanceFormat * Double.parseDouble(driver.getBiggerTwenty());
                    viewHolder.priceTextView.setText(context.getString(R.string.price_text, String.valueOf(price)));
                }
                break;
            }
        }

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView avatarImageView;
        private TextView nameTextView;
        private TextView vehicleNameTextView;
        private TextView priceTextView;
        private TextView starTextView;

        private OnItemClickListener onItemClickListener;

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            avatarImageView = itemView.findViewById(R.id.avatar_image_view);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            vehicleNameTextView = itemView.findViewById(R.id.vehicle_name_text_view);
            priceTextView = itemView.findViewById(R.id.price_text_view);
            starTextView = itemView.findViewById(R.id.star_text_view);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemClickListener.onItemClicked(getAdapterPosition());
        }
    }

    public interface OnItemClickListener {
        void onItemClicked(int position);
    }
}
