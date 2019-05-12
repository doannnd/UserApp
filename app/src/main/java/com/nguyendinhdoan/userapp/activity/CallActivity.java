package com.nguyendinhdoan.userapp.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.nguyendinhdoan.userapp.R;
import com.nguyendinhdoan.userapp.common.Common;
import com.nguyendinhdoan.userapp.model.Body;
import com.nguyendinhdoan.userapp.model.Notification;
import com.nguyendinhdoan.userapp.model.Result;
import com.nguyendinhdoan.userapp.model.Sender;
import com.nguyendinhdoan.userapp.model.Token;
import com.nguyendinhdoan.userapp.remote.IFirebaseMessagingAPI;
import com.nguyendinhdoan.userapp.services.MyFirebaseIdServices;
import com.wang.avi.AVLoadingIndicatorView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CallActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "CallActivity";

    private Toolbar callToolbar;
    private AVLoadingIndicatorView loadingIndicator;
    private TextView pickUpAddressTextView;
    private TextView droffOfAddressTextView;
    private TextView feeTextView;
    private TextView phoneTextView;
    private TextView licensePlatesTextView;
    private Button cancelButton;
    private LinearLayout driverDetail;

    private String driverId;
    private String destinationAdress;
    private LatLng destinationLocation;

    private String phoneNumberDriver;
    private String licensePlatesDriver;

    public static Intent start(Context context) {
        return new Intent(context, CallActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        initViews();
        setupUI();
        sendRequestToDiver();
        addEvents();
    }

    private void addEvents() {
        phoneTextView.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
    }

    private void setupUI() {
        setupToolbar();
    }

    private void setupToolbar() {
        setSupportActionBar(callToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void initViews() {
        callToolbar = findViewById(R.id.call_toolbar);
        loadingIndicator = findViewById(R.id.loading_indicator);
        pickUpAddressTextView = findViewById(R.id.tv_pick_up_address);
        droffOfAddressTextView = findViewById(R.id.tv_drop_off_address);
        feeTextView = findViewById(R.id.fee_text_view);
        phoneTextView = findViewById(R.id.phone_text_view);
        licensePlatesTextView = findViewById(R.id.license_plates_text_view);
        cancelButton = findViewById(R.id.cancel_button);
        driverDetail = findViewById(R.id.driver_detail);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel_button: {
                break;
            }
            case R.id.phone_text_view: {
                if (phoneNumberDriver != null) {
                    callPhoneToDriver(phoneNumberDriver);
                }
                break;
            }
        }
    }

    private void sendRequestToDiver() {
        if (driverId != null && destinationAdress != null & destinationLocation != null) {
            DatabaseReference tokenTable = FirebaseDatabase
                    .getInstance()
                    .getReference(MyFirebaseIdServices.TOKEN_TABLE_NAME);
            tokenTable.orderByKey().equalTo(driverId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot tokenSnapshot : dataSnapshot.getChildren()) {
                                Token token = tokenSnapshot.getValue(Token.class);
                                if (token != null) {
                                    // if user pickup request ==> send destination to driver app
                                    LatLng currentLocationUser = new LatLng(
                                            Common.lastLocation.getLatitude(),
                                            Common.lastLocation.getLongitude()
                                    );

                                    Body body = new Body(
                                            currentLocationUser,
                                            destinationLocation,
                                            destinationAdress
                                    );

                                    // convert body to json
                                    String jsonLocation = new Gson().toJson(body);
                                    // send notification for driver app [jsonLocation - body] and title is user id token
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    if (user != null) {
                                        String userId = user.getUid();
                                        Notification notification = new Notification(userId, jsonLocation);
                                        String driverIdToken = token.getToken();

                                        Sender sender = new Sender(notification, driverIdToken);
                                        callDriver(sender);
                                    }

                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e(TAG, "onCancelled: " + databaseError);
                        }
                    });
        }
    }

    private void callDriver(Sender sender) {
        IFirebaseMessagingAPI mServices = Common.getFirebaseMessagingAPI();
        mServices.sendMessage(sender)
                .enqueue(new Callback<Result>() {
                    @Override
                    public void onResponse(@NonNull Call<Result> call,
                                           @NonNull Response<Result> response) {
                        if (response.isSuccessful()) {
                            showSnackBar(getString(R.string.send_message_to_driver_success));

                            // add driver phone and driver license plates here
                            displayPhoneAndLicenses();

                        } else {
                            showSnackBar(getString(R.string.send_message_to_driver_error));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Result> call,
                                          @NonNull Throwable t) {
                        showSnackBar(t.getMessage());
                    }
                });
    }

    private void displayPhoneAndLicenses() {
        // display phone and licensePlates on screen
        driverDetail.setVisibility(View.VISIBLE);

        phoneTextView.setText(phoneNumberDriver);
        licensePlatesTextView.setText(licensePlatesDriver);
    }

    private void callPhoneToDriver(final String phoneNumberDriver) {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CALL_PHONE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        if (ActivityCompat.checkSelfPermission(
                                CallActivity.this, Manifest.permission.CALL_PHONE)
                                != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        Intent intentCall = new Intent(Intent.ACTION_CALL);
                        intentCall.setData(Uri.parse("tel:" + phoneNumberDriver));
                        startActivity(intentCall);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        showSnackBar(getString(R.string.permission_denied));
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(
                            PermissionRequest permission,
                            PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void showSnackBar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }
}
