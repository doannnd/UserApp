package com.nguyendinhdoan.userapp.common;

import com.nguyendinhdoan.userapp.remote.FirebaseMessagingClient;
import com.nguyendinhdoan.userapp.remote.IFirebaseMessagingAPI;

public class Common {

    private static final String fcmFURL = "https://fcm.googleapis.com";

    public static IFirebaseMessagingAPI getFirebaseMessagingAPI() {
        return FirebaseMessagingClient.getClient(fcmFURL).create(IFirebaseMessagingAPI.class);
    }
}
