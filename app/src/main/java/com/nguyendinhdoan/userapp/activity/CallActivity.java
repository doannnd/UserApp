package com.nguyendinhdoan.userapp.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
import com.nguyendinhdoan.userapp.model.Result;
import com.nguyendinhdoan.userapp.model.Sender;
import com.nguyendinhdoan.userapp.model.Token;
import com.nguyendinhdoan.userapp.remote.IFirebaseMessagingAPI;
import com.nguyendinhdoan.userapp.services.MyFirebaseIdServices;
import com.nguyendinhdoan.userapp.services.MyFirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CallActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "CallActivity";
    private static final String DRIVER_TABLE_NAME = "drivers";
    private static final String STATE_KEY = "state";
    private static final String NOTIFICATION_KEY = "cancel";
    public static final String MESSAGE_CANCEL_KEY = "MESSAGE_CANCEL_KEY";
    public static final String MESSAGE_ACCEPT_KEY = "MESSAGE_CANCEL_KEY";

    private TextView pickUpAddressTextView;
    private TextView dropOfAddressTextView;
    private TextView feeTextView;
    private Button cancelButton;

    private Driver driver;
    private String destinationAddress;
    private LatLng destinationLocation;

    private IFirebaseMessagingAPI mService;

    public static Intent start(Context context) {
        return new Intent(context, CallActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        updateTokenToDatabase();
        setupBroadcastReceiver();
        initViews();
        setupUI();
        addEvents();
    }

    private void setupBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter(MyFirebaseMessaging.MESSAGE_DRIVER_KEY));
    }

    private void updateTokenToDatabase() {
        final DatabaseReference tokenTable = FirebaseDatabase
                .getInstance().getReference(MyFirebaseIdServices.TOKEN_TABLE_NAME);

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
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
        cancelButton.setOnClickListener(this);
    }

    private void setupUI() {
        displayTripDetail();
        setupService();
    }

    private void setupService() {
        mService = Common.getFirebaseMessagingAPI();
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
        pickUpAddressTextView = findViewById(R.id.tv_pick_up_address);
        dropOfAddressTextView = findViewById(R.id.tv_drop_off_address);
        feeTextView = findViewById(R.id.fee_text_view);
        cancelButton = findViewById(R.id.cancel_button);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel_button: {
                handleCancelBooking();
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

    private void handleCancelBooking() {
        sendMessageToDriver();
    }

    private void sendMessageToDriver() {
        if (driver != null) {
            DatabaseReference tokenTable = FirebaseDatabase
                    .getInstance().getReference(MyFirebaseIdServices.TOKEN_TABLE_NAME);

            tokenTable.orderByKey().equalTo(driver.getId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                Token token = postSnapshot.getValue(Token.class);
                                if (token != null) {
                                    String bodyMessage = getString(R.string.message_user_cancel_booking);
                                    Notification notification = new Notification(NOTIFICATION_KEY, bodyMessage);

                                    Sender sender = new Sender(notification, token.getToken());
                                    handleSendMessage(sender);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e(TAG, "onCancelled: error" + databaseError);
                        }
                    });
        }
    }

    private void handleSendMessage(Sender sender) {
        mService.sendMessage(sender)
                .enqueue(new Callback<Result>() {
                    @Override
                    public void onResponse(@NonNull Call<Result> call, @NonNull Response<Result> response) {
                        if (response.isSuccessful()) {
                            updateStateDriver();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Result> call, @NonNull Throwable t) {
                        Log.e(TAG, "onFailure: error" + t.getMessage());
                    }
                });
    }

    private void updateStateDriver() {
        Map<String, Object> driverUpdateState = new HashMap<>();
        String stateValue = getString(R.string.state_not_working);
        driverUpdateState.put(STATE_KEY, stateValue);

        DatabaseReference driverTable = FirebaseDatabase.getInstance().getReference(DRIVER_TABLE_NAME);
        driverTable.child(driver.getId())
                .updateChildren(driverUpdateState)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "update state driver success");
                        } else {
                            Log.e(TAG, "update state driver failed" + task.getException());
                        }
                    }
                });
    }

    private void sendRequestToDiver() {
        Log.d(TAG, "driver_id: " + driver.getId());
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
                            Log.d(TAG, "CALL DRIVER SUCCESS");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Result> call,
                                          @NonNull Throwable t) {
                        showSnackBar(t.getMessage());
                    }
                });
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

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(MyFirebaseMessaging.MESSAGE_KEY);
            switch (message) {
                case MyFirebaseMessaging.CANCEL_TITLE: {
                    handleDriverCancelBooking();
                    break;
                }
                case MyFirebaseMessaging.ACCEPT_TITLE:
                    handleDriverAcceptBooking();
                    break;
                case MyFirebaseMessaging.DROP_OFF_TITLE:
                    break;
                case MyFirebaseMessaging.CANCEL_TRIP_TITLE: {
                    break;
                }
            }
        }
    };

    private void handleDriverAcceptBooking() {
        Intent intentTracking = TrackingActivity.start(CallActivity.this);
        intentTracking.putExtra(MESSAGE_ACCEPT_KEY, driver);
        intentTracking.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentTracking);
        finish();
    }

    private void handleDriverCancelBooking() {
        Intent intentUser = UserActivity.start(CallActivity.this);
        intentUser.putExtra(MESSAGE_CANCEL_KEY, "cancel");
        intentUser.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentUser);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
