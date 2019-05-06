package com.nguyendinhdoan.userapp.widget;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nguyendinhdoan.userapp.R;

public class PlaceDetailFragment extends BottomSheetDialogFragment {

    private static final String LOCATION_ADDRESS = "LOCATION_ADDRESS_KEY";
    private static final String DESTINATION_ADDRESS = "DESTINATION_ADDRESS_KEY";

    private String mLocationAddress;
    private String mDestinationAddress;

    public static PlaceDetailFragment newInstance(String locationAddress, String destinationAddress) {
        PlaceDetailFragment placeDetailFragment = new PlaceDetailFragment();

        Bundle args = new Bundle();
        args.putString(LOCATION_ADDRESS, locationAddress);
        args.putString(DESTINATION_ADDRESS, destinationAddress);

        placeDetailFragment.setArguments(args);
        return placeDetailFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLocationAddress = getArguments().getString(LOCATION_ADDRESS);
            mDestinationAddress = getArguments().getString(DESTINATION_ADDRESS);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.place_detail, container, false);
    }
}
