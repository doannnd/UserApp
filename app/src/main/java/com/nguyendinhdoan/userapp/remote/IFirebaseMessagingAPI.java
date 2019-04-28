package com.nguyendinhdoan.userapp.remote;

import com.nguyendinhdoan.userapp.model.Result;
import com.nguyendinhdoan.userapp.model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFirebaseMessagingAPI {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAEQ5MJ2U:APA91bEEotxX4qX22nIhApDs_WVzK0WW33-jDXfpsManz_C3qlUKBlCAj4yb6AIvuwujKnWU8esqJYYxfVlE2aOI7IV0_WfwgwWJFoRI4jyUn821rC39Q1F97qVxAZkxYWvIlzElYxUN"
    })
    @POST("fcm/send")
    Call<Result> sendMessage(@Body Sender body);
}
