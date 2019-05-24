package com.nguyendinhdoan.userapp.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
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
import com.nguyendinhdoan.userapp.utils.CommonUtils;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CallActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "CallActivity";
    private static final String NOTIFICATION_KEY = "cancel";
    public static final String MESSAGE_CANCEL_KEY = "MESSAGE_CANCEL_KEY";
    public static final String MESSAGE_ACCEPT_KEY = "MESSAGE_ACCEPT_KEY";
    public static final String DESTINATION_LOCATION_CALL_KEY = "DESTINATION_LOCATION_CALL_KEY";

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

        if (CommonUtils.isNetworkConnected(this)) {
            updateTokenToDatabase();
            setupBroadcastReceiver();
            initViews();
            setupUI();
            addEvents();
        } else {
            showSnackBar(getString(R.string.network_not_connect));
        }
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
            destinationLocation = Objects.requireNonNull(getIntent().getExtras()).getParcelable(DriverAdapter.DESTINATION_LOCATION_KEY);

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
        if (v.getId() == R.id.cancel_button) {
            handleCancelBooking();
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
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Result> call, @NonNull Throwable t) {
                        Log.e(TAG, "onFailure: error" + t.getMessage());
                    }
                });
    }

    private void sendRequestToDiver() {
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
            }
        }
    };

    private void handleDriverAcceptBooking() {
        Intent intentTracking = TrackingActivity.start(this);
        intentTracking.putExtra(MESSAGE_ACCEPT_KEY, driver);
        intentTracking.putExtra(DESTINATION_LOCATION_CALL_KEY, destinationLocation);
        intentTracking.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentTracking);
        finish();
    }

    private void handleDriverCancelBooking() {
        Intent intentUser = UserActivity.start(this);
        intentUser.putExtra(MESSAGE_CANCEL_KEY, getString(R.string.sorry_the_driver_declined_your_request));
        intentUser.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentUser);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager
                .getInstance(this)
                .unregisterReceiver(mMessageReceiver);
    }

}
