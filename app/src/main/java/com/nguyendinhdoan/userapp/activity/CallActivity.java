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
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.nguyendinhdoan.userapp.R;
import com.nguyendinhdoan.userapp.adapter.DriverAdapter;
import com.nguyendinhdoan.userapp.common.Common;
import com.nguyendinhdoan.userapp.model.Body;
import com.nguyendinhdoan.userapp.model.Driver;
import com.nguyendinhdoan.userapp.model.Notification;
import com.nguyendinhdoan.userapp.model.RateDriver;
import com.nguyendinhdoan.userapp.model.Result;
import com.nguyendinhdoan.userapp.model.Sender;
import com.nguyendinhdoan.userapp.model.Token;
import com.nguyendinhdoan.userapp.remote.IFirebaseMessagingAPI;
import com.nguyendinhdoan.userapp.services.MyFirebaseIdServices;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CallActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "CallActivity";

    private AVLoadingIndicatorView loadingIndicator;
    private TextView pickUpAddressTextView;
    private TextView dropOfAddressTextView;
    private TextView feeTextView;
    private TextView phoneTextView;
    private TextView licensePlatesTextView;
    private Button cancelButton;
    private LinearLayout driverDetail;

    private Driver driver;
    private String destinationAddress;
    private LatLng destinationLocation;

    public static Intent start(Context context) {
        return new Intent(context, CallActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        updateTokenToDatabase();
        initViews();
        setupUI();
        addEvents();
    }

    private void updateTokenToDatabase() {
        Log.d(TAG, "updateTokenToDatabase: started");
        final DatabaseReference tokenTable = FirebaseDatabase.getInstance().getReference(MyFirebaseIdServices.TOKEN_TABLE_NAME);

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String newToken = instanceIdResult.getToken();
                Token token = new Token(newToken);

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    String userId = user.getUid();
                    tokenTable.child(userId).setValue(token)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "update token at [UserActivity] success ");
                                    } else {
                                        Log.e(TAG, "update new token at [UserActivity] failed ");
                                    }
                                }
                            });
                }
            }
        });
    }

    private void addEvents() {
        phoneTextView.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
    }

    private void setupUI() {
        displayTripDetail();
    }

    private void displayTripDetail() {
        if (getIntent() != null) {
            driver = getIntent().getParcelableExtra(DriverAdapter.CALL_DRIVER_KEY);
            String locationAddress = getIntent().getStringExtra(DriverAdapter.LOCATION_ADDRESS_KEY);
            destinationAddress = getIntent().getStringExtra(DriverAdapter.DESTINATION_ADDRESS_KEY);
            int tripPrice = getIntent().getIntExtra(DriverAdapter.PRICE_KEY, 0);

            Bundle bundle = getIntent().getParcelableExtra(DriverAdapter.DESTINATION_LOCATION_BUNDLE);
            destinationLocation = bundle.getParcelable(DriverAdapter.DESTINATION_LOCATION_KEY);

            // update ui
            pickUpAddressTextView.setText(locationAddress);
            dropOfAddressTextView.setText(destinationAddress);
            feeTextView.setText(getString(R.string.price_text, String.valueOf(tripPrice)));

            // send request to driver
            sendRequestToDiver();
        }
    }


    private void initViews() {
        loadingIndicator = findViewById(R.id.loading_indicator);
        pickUpAddressTextView = findViewById(R.id.tv_pick_up_address);
        dropOfAddressTextView = findViewById(R.id.tv_drop_off_address);
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
                if (driver.getPhone() != null) {
                    callPhoneToDriver();
                }
                break;
            }
        }
    }

    private void sendRequestToDiver() {
        Log.d(TAG, "driver_id: " + driver.getId());
        Log.d(TAG, "destination address: " + destinationAddress);
        Log.d(TAG, "destination location" + destinationLocation);
        if (driver.getId() != null && destinationAddress != null & destinationLocation != null) {
            DatabaseReference tokenTable = FirebaseDatabase
                    .getInstance()
                    .getReference(MyFirebaseIdServices.TOKEN_TABLE_NAME);
            tokenTable.orderByKey().equalTo(driver.getId())
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
                                            destinationAddress
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

                            loadingIndicator.setVisibility(View.INVISIBLE);
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

        phoneTextView.setText(driver.getPhone());
        licensePlatesTextView.setText(driver.getLicensePlates());
    }

    private void callPhoneToDriver() {
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
                        intentCall.setData(Uri.parse("tel:" + driver.getPhone()));
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

   /* @Override
    public void onNegativeButtonClicked() {
        Toast.makeText(this, "cancel", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNeutralButtonClicked() {
        Toast.makeText(this, "later", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPositiveButtonClicked(int rates, @NonNull String comments) {
        RateDriver rateDriver = new RateDriver(String.valueOf(rates), comments);

        rateDriverTable.child(Common.driverId)
                .push()
                .setValue(rateDriver)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            // if success , calculate average of rate and update to Driver information
                            rateDriverTable.child(Common.driverId)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            double sumStar = 0.0;
                                            int count = 0;
                                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                                RateDriver rateDriver1 = postSnapshot.getValue(RateDriver.class);
                                                if (rateDriver1 != null) {
                                                    sumStar += Double.parseDouble(rateDriver1.getRates());
                                                    count++;
                                                }
                                            }
                                            double averageStar = sumStar / count;
                                            DecimalFormat df = new DecimalFormat("#.#");
                                            String valueUpdate = df.format(averageStar);


                                            // create object update
                                            Map<String, Object> driverUpdateRate = new HashMap<>();
                                            driverUpdateRate.put("rates", valueUpdate);

                                            driverTable.child(Common.driverId).updateChildren(driverUpdateRate)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                showSnackBar("Thank you your submit");
                                                                // reset pickup request
                                                                findGeoQuery.removeAllListeners();
                                                                //driverCallButton.setEnabled(true);
                                                                Common.driverId = "";
                                                                Common.isDriverFound = false;
                                                            } else {
                                                                showSnackBar("rate updated but can't write to driver table");
                                                            }
                                                        }
                                                    });
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Log.e(TAG, "onCancelled: error" + databaseError);
                                        }
                                    });

                        } else {
                            Toast.makeText(UserActivity.this, "error occur", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }*/
}
