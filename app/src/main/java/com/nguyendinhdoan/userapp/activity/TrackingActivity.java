package com.nguyendinhdoan.userapp.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.nguyendinhdoan.userapp.R;
import com.nguyendinhdoan.userapp.common.Common;
import com.nguyendinhdoan.userapp.model.Driver;
import com.nguyendinhdoan.userapp.model.Notification;
import com.nguyendinhdoan.userapp.model.Result;
import com.nguyendinhdoan.userapp.model.Sender;
import com.nguyendinhdoan.userapp.model.Token;
import com.nguyendinhdoan.userapp.remote.IFirebaseMessagingAPI;
import com.nguyendinhdoan.userapp.remote.IGoogleAPI;
import com.nguyendinhdoan.userapp.services.MyFirebaseIdServices;
import com.nguyendinhdoan.userapp.services.MyFirebaseMessaging;
import com.nguyendinhdoan.userapp.widget.AcceptDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrackingActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    private static final String TAG = "TrackingActivity";
    private static final float TRACKING_MAP_ZOOM = 15.0F;
    public static final long LOCATION_REQUEST_INTERVAL = 5000L;
    public static final long LOCATION_REQUEST_FASTEST_INTERVAL = 3000L;
    public static final float LOCATION_REQUEST_DISPLACEMENT = 10.0F;
    public static final int BOTTOM_MAP = 30;
    public static final int RIGHT_MAP = 30;
    public static final int TOP_MAP = 0;
    public static final int LEFT_MAP = 0;
    public static final int SUBJECT_KEY = 0;
    public static final String DRIVER_LOCATION_TABLE_NAME = "driver_location";
    public static final String DIRECTION_ROUTES_KEY = "routes";
    private static final String DIRECTION_LEGS_KEY = "legs";
    private static final String DIRECTION_DURATION_KEY = "duration";
    private static final String DIRECTION_TEXT_KEY = "text";
    public static final int ROUTES_INDEX = 0;
    public static final int LEGS_INDEX = 0;
    private static final String NOTIFICATION_KEY = "cancelTrip";
    private static final String DRIVER_TABLE_NAME = "drivers";
    private static final String STATE_KEY = "state";

    private TextView timeTextView;
    private TextView introTextView;
    private TextView licensePlatesTextView;
    private TextView vehicleNameTextView;
    private ImageView avatarImageView;
    private TextView nameTextView;
    private TextView starTextView;
    private ImageView phoneImageView;
    private Button cancelButton;
    private ProgressBar loadingMap;

    private GoogleMap trackingMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    private Driver driver;

    public static Intent start(Context context) {
        return new Intent(context, TrackingActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        setupBroadcastReceiver();
        initViews();
        setupUI();
        addEvents();
    }

    private void setupBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter(MyFirebaseMessaging.MESSAGE_DRIVER_TRACKING_KEY));
    }

    private void addEvents() {
        phoneImageView.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
    }

    private void setupUI() {
        getDataDriver();
        initGoogleMap();
        initLocation();
        setupDisplayInfoDriver();
    }

    private void initLocation() {
        fusedLocationProviderClient = new FusedLocationProviderClient(this);

        // begin update location;
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            // show loading
                            loadingMap.setVisibility(View.VISIBLE);

                            buildLocationRequest();
                            buildLocationCallback();
                            // update location
                            if (ActivityCompat.checkSelfPermission(TrackingActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                    && ActivityCompat.checkSelfPermission(TrackingActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }

                            fusedLocationProviderClient.requestLocationUpdates(
                                    locationRequest, locationCallback, Looper.myLooper());
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions,
                                                                   PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .onSameThread()
                .check();
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(LOCATION_REQUEST_INTERVAL);
        locationRequest.setFastestInterval(LOCATION_REQUEST_FASTEST_INTERVAL);
        locationRequest.setSmallestDisplacement(LOCATION_REQUEST_DISPLACEMENT);
    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Common.lastLocation = locationResult.getLastLocation();
                Log.d(TAG, "current location latitude: " + Common.lastLocation.getLatitude());
                Log.d(TAG, "current location longitude: " + Common.lastLocation.getLongitude());

                // display current location on the google map
                displayCurrentLocation();
            }
        };

    }

    private void displayCurrentLocation() {
        if (Common.lastLocation != null) {

            DatabaseReference driverLocationTable = FirebaseDatabase.getInstance().getReference(DRIVER_LOCATION_TABLE_NAME);
            driverLocationTable.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // reload if change driver location : offline or online of driver
                    displayDriverLocation();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "onCancelled: error in reload all available driver " + databaseError);
                }
            });

            final double userLatitude = Common.lastLocation.getLatitude();
            final double userLongitude = Common.lastLocation.getLongitude();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            trackingMap.setMyLocationEnabled(true);
            trackingMap.getUiSettings().setMyLocationButtonEnabled(true);

            trackingMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(new LatLng(userLatitude, userLongitude), TRACKING_MAP_ZOOM)
            );

            // hide progress bar
            loadingMap.setVisibility(View.INVISIBLE);

            // load location of driver
            displayDriverLocation();
        }

    }

    private void displayDriverLocation() {

        trackingMap.clear();

        if (driver != null) {
            DatabaseReference driverLocationTable = FirebaseDatabase.getInstance().getReference(DRIVER_LOCATION_TABLE_NAME);
            GeoFire driverLocationGeoFire = new GeoFire(driverLocationTable);
            driverLocationGeoFire.getLocation(driver.getId(), new com.firebase.geofire.LocationCallback() {
                @Override
                public void onLocationResult(String key, GeoLocation location) {
                    if (location != null) {
                        // show driver with icon car on google map
                        trackingMap.addMarker(new MarkerOptions()
                                .position(new LatLng(location.latitude, location.longitude))
                                .flat(true)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car))
                        );

                        loadTimeDriverToUser(location);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "error load location of driver: " + databaseError);
                }
            });
        }
    }

    private void loadTimeDriverToUser(GeoLocation location) {
        String currentLocation = String.format(Locale.getDefault(), "%f,%f",
                Common.lastLocation.getLatitude(), Common.lastLocation.getLongitude());
        String driverLocation = String.format(Locale.getDefault(), "%f,%f",
                location.latitude, location.longitude);

        try {
            String trackingURL = Common.directionURL(currentLocation, driverLocation);
            Log.d(TAG, "user call url: " + trackingURL);

            IGoogleAPI mService = Common.getGoogleAPI();
            mService.getDirectionPath(trackingURL)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            try {
                                JSONObject root = new JSONObject(response.body());
                                JSONArray routes = root.getJSONArray(DIRECTION_ROUTES_KEY);
                                JSONObject routeObject = routes.getJSONObject(ROUTES_INDEX);
                                JSONArray legs = routeObject.getJSONArray(DIRECTION_LEGS_KEY);
                                JSONObject legObject = legs.getJSONObject(LEGS_INDEX);

                                // get time and display on time text view
                                JSONObject time = legObject.getJSONObject(DIRECTION_DURATION_KEY);
                                String minutes = time.getString(DIRECTION_TEXT_KEY);
                                Log.d(TAG, "minutes: " + time.getString(DIRECTION_TEXT_KEY));

                                // set time text view
                                timeTextView.setText(minutes);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            Log.e(TAG, "error load information time");
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupDisplayInfoDriver() {
        if (driver != null) {
            // load avatar
            Glide.with(this).load(driver.getAvatarUrl())
                    .placeholder(R.drawable.ic_profile)
                    .into(avatarImageView);
            // load other
            licensePlatesTextView.setText(driver.getLicensePlates());
            vehicleNameTextView.setText(driver.getVehicleName());
            nameTextView.setText(driver.getName());
            starTextView.setText(driver.getRates());
        }
    }

    private void getDataDriver() {
        if (getIntent() != null) {
            driver = getIntent().getParcelableExtra(CallActivity.MESSAGE_ACCEPT_KEY);
            if (driver != null) {
                showAcceptDialog();
            }
        }
    }

    private void showAcceptDialog() {
        FragmentManager fm = getSupportFragmentManager();
        AcceptDialogFragment acceptDialog = AcceptDialogFragment.newInstance();
        acceptDialog.show(fm, acceptDialog.getTag());
    }

    private void initViews() {
        timeTextView = findViewById(R.id.time_text_view);
        introTextView = findViewById(R.id.intro_text_view);
        licensePlatesTextView = findViewById(R.id.license_plates_text_view);
        vehicleNameTextView = findViewById(R.id.vehicle_name_text_view);
        nameTextView = findViewById(R.id.name_text_view);
        starTextView = findViewById(R.id.star_text_view);
        avatarImageView = findViewById(R.id.avatar_image_view);
        phoneImageView = findViewById(R.id.phone_image_view);
        cancelButton = findViewById(R.id.cancel_button);
        loadingMap = findViewById(R.id.loading_map);
    }

    private void initGoogleMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // add my button location in bottom right
        View locationButton = ((View) Objects.requireNonNull(Objects.requireNonNull(mapFragment).getView())
                .findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, SUBJECT_KEY);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.setMargins(LEFT_MAP, TOP_MAP, RIGHT_MAP, BOTTOM_MAP);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        trackingMap = googleMap;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel_button: {
                handleUserCancelTrip();
                break;
            }
            case R.id.phone_image_view: {
                if (driver != null) {
                    callPhoneToDriver();
                }
                break;
            }
        }
    }

    private void handleUserCancelTrip() {
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
        IFirebaseMessagingAPI mService = Common.getFirebaseMessagingAPI();
        mService.sendMessage(sender)
                .enqueue(new Callback<Result>() {
                    @Override
                    public void onResponse(@NonNull Call<Result> call, @NonNull Response<Result> response) {
                        if (response.isSuccessful()) {
                            updateStateDriver();
                            launchUserActivity();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Result> call, @NonNull Throwable t) {
                        Log.e(TAG, "onFailure: error" + t.getMessage());
                    }
                });
    }

    private void launchUserActivity() {
        Intent intentUser = UserActivity.start(this);
        intentUser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentUser);
        finish();
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

    private void callPhoneToDriver() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CALL_PHONE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        if (ActivityCompat.checkSelfPermission(
                                TrackingActivity.this, Manifest.permission.CALL_PHONE)
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
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(MyFirebaseMessaging.MESSAGE_KEY);
            switch (message) {
                case MyFirebaseMessaging.ARRIVED_TITLE: {
                    introTextView.setText(getString(R.string.driver_has_arrived));
                    break;
                }
                case MyFirebaseMessaging.CANCEL_TRIP_TITLE: {

                    break;
                }
                case MyFirebaseMessaging.DROP_OFF_TITLE: {

                    break;
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager
                .getInstance(this)
                .unregisterReceiver(mMessageReceiver);
        stopLocationUpdates();
        trackingMap.clear();
    }
}
