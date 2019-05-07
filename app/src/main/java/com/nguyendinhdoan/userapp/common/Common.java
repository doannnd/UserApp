package com.nguyendinhdoan.userapp.common;

import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;
import com.nguyendinhdoan.userapp.model.User;
import com.nguyendinhdoan.userapp.remote.FirebaseMessagingClient;
import com.nguyendinhdoan.userapp.remote.IFirebaseMessagingAPI;
import com.nguyendinhdoan.userapp.remote.IGoogleAPI;
import com.nguyendinhdoan.userapp.remote.RetrofitClient;

public class Common {

    public static User currentUser;

    private static final String API_KEY = "AIzaSyDXP3aehsojrBx1Nr0RPt85sLPpZLvmeAM";
    private static final String fcmFURL = "https://fcm.googleapis.com";
    private static final String baseURL = "https://maps.googleapis.com";

    private static final String URL_SCHEME = "https";
    private static final String URL_AUTHORITY = "maps.googleapis.com";
    private static final String URL_PATH_1 = "maps";
    private static final String URL_PATH_2 = "api";
    private static final String URL_PATH_3 = "directions";
    private static final String URL_PATH_4 = "json";
    private static final String URL_QUERY_PARAM_MODE_KEY = "mode";
    private static final String URL_QUERY_PARAM_MODE_VALUE = "drivings";
    private static final String URL_QUERY_PARAM_TRANSIT_KEY = "transit_routing_preference";
    private static final String URL_QUERY_PARAM_TRANSIT_VALUE = "less_driving";
    private static final String URL_QUERY_PARAM_ORIGIN_KEY = "origin";
    private static final String URL_QUERY_PARAM_DESTINATION_KEY = "destination";
    private static final String URL_QUERY_PARAM_API_KEY = "key";

    private static final double BASE_FARE = 2.5; // 2.55$
    private static final double COST_PER_MINUTES = 0.35; // 0.35$
    private static final double COST_PER_KM = 1.75; // 1.75$


    // ==> formula = BASE_FARE + (COST_PER_MINUTES * MINUTES) + (COST_PER_KM * KM)
    // with uber - USER_FEE + OTHER_FEE but basic application ==> remove this

    public static double getPrice(double km, int minutes) {
        return BASE_FARE + (COST_PER_MINUTES * minutes) + (COST_PER_KM * km);
    }

    public static IFirebaseMessagingAPI getFirebaseMessagingAPI() {
        return FirebaseMessagingClient.getClient(fcmFURL).create(IFirebaseMessagingAPI.class);
    }

    public static IGoogleAPI getGoogleAPI() {
        return RetrofitClient.getClient(baseURL).create(IGoogleAPI.class);
    }

    public static String directionURL(String currentPosition, String destinationPosition) {
        return new Uri.Builder().scheme(URL_SCHEME)
                .authority(URL_AUTHORITY)
                .appendPath(URL_PATH_1)
                .appendPath(URL_PATH_2)
                .appendPath(URL_PATH_3)
                .appendPath(URL_PATH_4)
                .appendQueryParameter(URL_QUERY_PARAM_MODE_KEY, URL_QUERY_PARAM_MODE_VALUE)
                .appendQueryParameter(URL_QUERY_PARAM_TRANSIT_KEY, URL_QUERY_PARAM_TRANSIT_VALUE)
                .appendQueryParameter(URL_QUERY_PARAM_ORIGIN_KEY, currentPosition)
                .appendQueryParameter(URL_QUERY_PARAM_DESTINATION_KEY, destinationPosition)
                .appendQueryParameter(URL_QUERY_PARAM_API_KEY, API_KEY)
                .build().toString();
    }
}
