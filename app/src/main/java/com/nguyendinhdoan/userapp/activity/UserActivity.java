package com.nguyendinhdoan.userapp.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.api.Status;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.nguyendinhdoan.userapp.R;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class UserActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
        View.OnTouchListener {

    private static final String USER_LOCATION_TABLE_NAME = "user_location";
    private static final String TAG = "USER_ACTIVITY";

    private static final int ORIGIN_AUTOCOMPLETE_REQUEST_CODE = 9000;
    private static final int DESTINATION_AUTOCOMPLETE_REQUEST_CODE = 9001;
    private static final float USER_MAP_ZOOM = 15.0F;
    public static final long LOCATION_REQUEST_INTERVAL = 5000L;
    public static final long LOCATION_REQUEST_FASTEST_INTERVAL = 3000L;
    public static final float LOCATION_REQUEST_DISPLACEMENT = 10.0F;

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private EditText originEditText;
    private EditText destinationEditText;
    private ProgressBar userProgressBar;

    private GoogleMap userGoogleMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private FirebaseAuth userAuth;
    private Location lastLocation;
    private Marker userMarker;
    private GeoFire userGeoFire;

    public static Intent start(Context context) {
        return new Intent(context, UserActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        initViews();
        setupUI();
        addEvents();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addEvents() {
        navigationView.setNavigationItemSelectedListener(this);
        originEditText.setOnTouchListener(this);
        destinationEditText.setOnTouchListener(this);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        originEditText = findViewById(R.id.origin_edit_text);
        destinationEditText = findViewById(R.id.destination_edit_text);
        userProgressBar = findViewById(R.id.user_progress_bar);
    }

    private void setupUI() {
        setupToolbar();
        setupNavigationView();
        setupGoogleMap();
        setupPlacesAPI();
        setupFirebase();
        setupLocation();
    }

    private void setupFirebase() {
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference(USER_LOCATION_TABLE_NAME);
        userGeoFire = new GeoFire(driverLocation);
        userAuth = FirebaseAuth.getInstance();
    }

    private void setupLocation() {
        fusedLocationProviderClient = new FusedLocationProviderClient(this);

        // begin update location;
        startLocationUpdates();
    }

    private void setupPlacesAPI() {
        Places.initialize(this, getString(R.string.google_api_key));
    }

    private void setupGoogleMap() {
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (supportMapFragment != null) {
            supportMapFragment.getMapAsync(this);
        }
    }

    private void setupNavigationView() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout,
                toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_tools) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

       userGoogleMap = googleMap;

        setupMap();
    }

    private void setupMap() {
        userGoogleMap.setIndoorEnabled(false);
        userGoogleMap.setBuildingsEnabled(false);
        userGoogleMap.setTrafficEnabled(false);
        userGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // user touch origin edit text
        if (v.getId() == R.id.origin_edit_text) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    initAutoCompleteSearchOrigin();
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    v.performClick();
                    break;
                }
                default:
                    break;
            }
        } else if (v.getId() == R.id.destination_edit_text) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    initAutoCompleteSearchDestination();
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    v.performClick();
                    break;
                }
                default:
                    break;
            }
        }

        return true;
    }

    private void initAutoCompleteSearchDestination() {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS);

        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this);
        startActivityForResult(intent, DESTINATION_AUTOCOMPLETE_REQUEST_CODE);
    }

    private void initAutoCompleteSearchOrigin() {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS);

        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this);
        startActivityForResult(intent, ORIGIN_AUTOCOMPLETE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ORIGIN_AUTOCOMPLETE_REQUEST_CODE) {

            if (resultCode == RESULT_OK && data != null) {

                Place place = Autocomplete.getPlaceFromIntent(data);
                String origin = place.getName();
                Log.d(TAG, "origin place address: " + place.getAddress());
                Log.d(TAG, "origin place name: " + place.getName());
                // set address for origin edit text
                originEditText.setText(origin);
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(Objects.requireNonNull(data));
                Log.d(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
                showSnackBar(getString(R.string.user_cancel_operation));
            }

        } else if (requestCode == DESTINATION_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {

                Place place = Autocomplete.getPlaceFromIntent(data);
                String destination = place.getName();
                Log.d(TAG, "origin place address: " + place.getAddress());
                Log.d(TAG, "origin place name: " + place.getName());
                // set address for destination edit text
                destinationEditText.setText(destination);
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(Objects.requireNonNull(data));
                Log.d(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
                showSnackBar(getString(R.string.user_cancel_operation));
            }
        }
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
                            userProgressBar.setVisibility(View.VISIBLE);

                            buildLocationRequest();
                            buildLocationCallback();
                            // update location
                            if (ActivityCompat.checkSelfPermission(UserActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                    && ActivityCompat.checkSelfPermission(UserActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            fusedLocationProviderClient.requestLocationUpdates(
                                    locationRequest, locationCallback, Looper.myLooper());
                        }

                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // TODO: ....
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

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                lastLocation = locationResult.getLastLocation();
                Log.d(TAG, "current location latitude: " + lastLocation.getLatitude());
                Log.d(TAG, "current location longitude: " + lastLocation.getLongitude());

                // display current location on the google map
                displayCurrentLocation();
            }
        };

    }

    private void displayCurrentLocation() {
            // get information save in driver_location table on firebase
            FirebaseUser user = userAuth.getCurrentUser();
            if (user != null) {
                String userId = user.getUid();
                final double driverLatitude = lastLocation.getLatitude();
                final double driverLongitude = lastLocation.getLongitude();

                // save location of driver in realtime database and update location on google map
                userGeoFire.setLocation(userId, new GeoLocation(driverLatitude, driverLongitude),
                        new GeoFire.CompletionListener() {
                            @Override
                            public void onComplete(String key, DatabaseError error) {
                                if (error == null) {
                                    Log.d(TAG, "save current location of user success");
                                    updateUI(driverLatitude, driverLongitude);
                                } else {
                                    Log.e(TAG, "have error in display current location: " + error);
                                }
                            }
                        });
            }
    }

    private void updateUI(double driverLatitude, double driverLongitude) {

        if (userMarker != null) {
            userMarker.remove(); // if marker existed --> delete
        }

        // draw marker on google map
        userMarker = userGoogleMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_current_location))
                .position(new LatLng(driverLatitude, driverLongitude))
                .title(getString(R.string.title_of_you))
        );

        // move camera
        userGoogleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(new LatLng(driverLatitude, driverLongitude), USER_MAP_ZOOM)
        );

        // hide progress bar complete display current location
        userProgressBar.setVisibility(View.INVISIBLE);
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(LOCATION_REQUEST_INTERVAL);
        locationRequest.setFastestInterval(LOCATION_REQUEST_FASTEST_INTERVAL);
        locationRequest.setSmallestDisplacement(LOCATION_REQUEST_DISPLACEMENT);
    }

    // TODO: ......
    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void showSnackBar(String message) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        TextView textSnack = view.findViewById(android.support.design.R.id.snackbar_text);
        textSnack.setTextColor(getResources().getColor(R.color.colorBlack));
        snackbar.show();
    }
}
