package com.nguyendinhdoan.userapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.nguyendinhdoan.userapp.R;
import com.nguyendinhdoan.userapp.activity.CallActivity;
import com.nguyendinhdoan.userapp.model.Driver;

import java.util.List;

public class DriverAdapter extends RecyclerView.Adapter<DriverAdapter.ViewHolder> {

    public static final String CALL_DRIVER_KEY = "CALL_DRIVER_KEY";
    public static final String LOCATION_ADDRESS_KEY = "LOCATION_ADDRESS_KEY";
    public static final String DESTINATION_ADDRESS_KEY = "DESTINATION_ADDRESS_KEY";
    public static final String PRICE_KEY = "PRICE_KEY";
    public static final String DESTINATION_LOCATION_KEY = "DESTINATION_LOCATION_KEY";
    public static final String DESTINATION_LOCATION_BUNDLE = "DESTINATION_LOCATION_BUNDLE";

    private Context context;
    private List<Driver> driverList;
    private String distance;
    private String locationAddress;
    private String destinationAddress;
    private int priceFormat;
    private LatLng destinationLocation;

    public DriverAdapter(Context context, List<Driver> driverList, String distance,
                         String locationAddress, String destinationAddress, LatLng destinationLocation) {
        this.context = context;
        this.driverList = driverList;
        this.distance = distance;
        this.locationAddress = locationAddress;
        this.destinationAddress = destinationAddress;
        this.destinationLocation = destinationLocation;
    }

    @NonNull
    @Override
    public DriverAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.recycler_view_item, viewGroup, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DriverAdapter.ViewHolder viewHolder, int position) {
        final Driver driver = driverList.get(position);

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

        viewHolder.rootItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentCall = new Intent(context, CallActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(DESTINATION_LOCATION_KEY, destinationLocation);

                intentCall.putExtra(CALL_DRIVER_KEY, driver);
                intentCall.putExtra(LOCATION_ADDRESS_KEY, locationAddress);
                intentCall.putExtra(DESTINATION_ADDRESS_KEY, destinationAddress);
                intentCall.putExtra(PRICE_KEY, priceFormat);
                intentCall.putExtra(DESTINATION_LOCATION_BUNDLE, bundle);
                context.startActivity(intentCall);
            }
        });

    }

    private void calculateTripFee(ViewHolder viewHolder, Driver driver) {
        String[] distances = distance.split(" ");
        String valueDistance = distances[0];
        String unitDistance = distances[1];

        switch (unitDistance) {
            case "m": {
                double price = Double.parseDouble(driver.getZeroToTwo());
                 priceFormat = (int) (price / 1000);
                viewHolder.priceTextView.setText(context.getString(R.string.price_text, String.valueOf(priceFormat)));

                break;
            }
            case "km": {
                double valueDistanceFormat = Double.parseDouble(valueDistance);
                if (valueDistanceFormat <= 2) {
                    double price = valueDistanceFormat * Double.parseDouble(driver.getZeroToTwo());
                    priceFormat = (int) (price / 1000);
                    viewHolder.priceTextView.setText(context.getString(R.string.price_text, String.valueOf(priceFormat)));
                } else if (valueDistanceFormat > 2 && valueDistanceFormat <= 10) {
                    double price = valueDistanceFormat * Double.parseDouble(driver.getThreeToTen());
                    priceFormat = (int) (price / 1000);
                    viewHolder.priceTextView.setText(context.getString(R.string.price_text, String.valueOf(priceFormat)));
                } else if (valueDistanceFormat > 10 && valueDistanceFormat <= 20) {
                    double price = valueDistanceFormat * Double.parseDouble(driver.getElevenToTwenty());
                    priceFormat = (int) (price / 1000);
                    viewHolder.priceTextView.setText(context.getString(R.string.price_text, String.valueOf(priceFormat)));
                } else if (valueDistanceFormat > 20) {
                    double price = valueDistanceFormat * Double.parseDouble(driver.getBiggerTwenty());
                    priceFormat = (int) (price / 1000);
                    viewHolder.priceTextView.setText(context.getString(R.string.price_text, String.valueOf(priceFormat)));
                }
                break;
            }
        }

    }

    @Override
    public int getItemCount() {
        return driverList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView avatarImageView;
        private TextView nameTextView;
        private TextView vehicleNameTextView;
        private TextView priceTextView;
        private TextView starTextView;
        private ConstraintLayout rootItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            avatarImageView = itemView.findViewById(R.id.avatar_image_view);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            vehicleNameTextView = itemView.findViewById(R.id.vehicle_name_text_view);
            priceTextView = itemView.findViewById(R.id.price_text_view);
            starTextView = itemView.findViewById(R.id.star_text_view);
            rootItem = itemView.findViewById(R.id.root_item);
        }

    }

}
