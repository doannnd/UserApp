package com.nguyendinhdoan.userapp.widget;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nguyendinhdoan.userapp.R;
import com.nguyendinhdoan.userapp.common.Common;
import com.nguyendinhdoan.userapp.remote.IGoogleAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlaceDetailFragment extends BottomSheetDialogFragment {

    private static final String TAG = "PlaceDetailFragment";
    private static final String LOCATION_ADDRESS = "LOCATION_ADDRESS_KEY";
    private static final String DESTINATION_ADDRESS = "DESTINATION_ADDRESS_KEY";
    public static final String DIRECTION_ROUTES_KEY = "routes";
    private static final String DIRECTION_LEGS_KEY = "legs";
    private static final String DIRECTION_DURATION_KEY = "duration";
    private static final String DIRECTION_DISTANCE_KEY = "distance";
    private static final String DIRECTION_ADDRESS_KEY = "end_address";
    private static final String DIRECTION_TEXT_KEY = "text";
    private static final String START_ADDRESS_KEY = "start_address";

    private TextView locationAddressTextView;
    private TextView destinationAddressTextView;
    private TextView calculateMoneyTextView;

    private String mLocationAddress;
    private String mDestinationAddress;
    
    private IGoogleAPI mServices;

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        locationAddressTextView = view.findViewById(R.id.location_address);
        destinationAddressTextView = view.findViewById(R.id.destination_address);
        calculateMoneyTextView  = view.findViewById(R.id.calculate_money);

        mServices = Common.getGoogleAPI();
        displayPlaceDetail(mLocationAddress, mDestinationAddress);
    }

    private void displayPlaceDetail(String mLocationAddress, String mDestinationAddress) {
        try {
            String userCallURL = Common.directionURL(mLocationAddress, mDestinationAddress);

            mServices.getDirectionPath(userCallURL)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            try {
                                JSONObject root = new JSONObject(response.body());
                                JSONArray routes = root.getJSONArray(DIRECTION_ROUTES_KEY);
                                JSONObject routeObject = routes.getJSONObject(0);
                                JSONArray legs = routeObject.getJSONArray(DIRECTION_LEGS_KEY);
                                JSONObject legObject = legs.getJSONObject(0);

                                // get time and display on time text view
                                JSONObject time = legObject.getJSONObject(DIRECTION_DURATION_KEY);
                                String minutes = time.getString(DIRECTION_TEXT_KEY);
                                Log.d(TAG, "minutes: " + time.getString(DIRECTION_TEXT_KEY));

                                int timeFormatted = Integer.parseInt(minutes.replaceAll("\\D+", ""));

                                // get distance and display on distance text view
                                JSONObject distance = legObject.getJSONObject(DIRECTION_DISTANCE_KEY);
                                String km = distance.getString(DIRECTION_TEXT_KEY);
                                Log.d(TAG, "km: " + distance.getString(DIRECTION_TEXT_KEY));

                                double distanceFormatted = Double.parseDouble(km.replaceAll("[^0-9\\\\.]", ""));

                                String finalPrice = String.format(Locale.getDefault(), "%s km + %s minute = $%.2f", distanceFormatted, timeFormatted,
                                        Common.getPrice(distanceFormatted, timeFormatted));

                                // get end address and display on address text view
                                String destinationAddress = legObject.getString(DIRECTION_ADDRESS_KEY);
                                String locationAddress = legObject.getString(START_ADDRESS_KEY);
                                Log.d(TAG, "destination address: " + destinationAddress);
                                Log.d(TAG, "location address: " + locationAddress);

                                // set value for text view
                                locationAddressTextView.setText(locationAddress);
                                destinationAddressTextView.setText(destinationAddress);
                                calculateMoneyTextView.setText(finalPrice);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            Log.e(TAG, "error load information user : time, distance, address");
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
