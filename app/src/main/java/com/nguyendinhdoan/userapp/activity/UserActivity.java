package com.nguyendinhdoan.userapp.activity;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.nguyendinhdoan.userapp.R;
import com.nguyendinhdoan.userapp.adapter.DriverAdapter;
import com.nguyendinhdoan.userapp.common.Common;
import com.nguyendinhdoan.userapp.model.Driver;
import com.nguyendinhdoan.userapp.model.Token;
import com.nguyendinhdoan.userapp.model.User;
import com.nguyendinhdoan.userapp.remote.IFirebaseMessagingAPI;
import com.nguyendinhdoan.userapp.remote.IGoogleAPI;
import com.nguyendinhdoan.userapp.services.MyFirebaseIdServices;
import com.nguyendinhdoan.userapp.services.MyFirebaseMessaging;
import com.nguyendinhdoan.userapp.utils.CommonUtils;
import com.nguyendinhdoan.userapp.widget.CancelDialogFragment;
import com.stepstone.apprating.AppRatingDialog;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
        View.OnTouchListener, View.OnClickListener {

    private static final String PICKUP_REQUEST_TABLE_NAME = "pickup_request";
    private static final String TAG = "USER_ACTIVITY";
    private static final String DRIVER_LOCATION_TABLE_NAME = "driver_location";
    private static final String DRIVER_TABLE_NAME = "drivers";

    private static final int DESTINATION_AUTOCOMPLETE_REQUEST_CODE = 9001;
    private static final float USER_MAP_ZOOM = 15.0F;
    private static final long LOCATION_REQUEST_INTERVAL = 5000L;
    private static final long LOCATION_REQUEST_FASTEST_INTERVAL = 3000L;
    private static final float LOCATION_REQUEST_DISPLACEMENT = 10.0F;
    private static final int RADIUS_LOAD_DRIVER_LIMIT = 3; // limit 3km
    private static final int GOOGLE_MAP_PADDING = 150;
    private static final String VN_CODE = "VN";
    private static final double DISTANCE_RESTRICT = 100000;
    private static final double HEADING_NORTH = 0;
    private static final double HEADING_SOUTH = 180;
    private static final int UPLOAD_REQUEST_CODE = 10;

    private static final String NAME_KEY = "name";
    private static final String EMAIL_KEY = "email";
    private static final String PHONE_KEY = "phone";
    private static final String AVATAR_URL_KEY = "avatarUrl";
    private static final String RATE_DRIVER_TABLE = "rate_driver";
    private static final String DRIVER_TABLE = "drivers";
    public static final String USER_ID_KEY = "USER_ID_KEY";

    public static final String DIRECTION_ROUTES_KEY = "routes";
    public static final String DIRECTION_POLYLINE_KEY = "overview_polyline";
    public static final String DIRECTION_POINT_KEY = "points";
    public static final int DIRECTION_PADDING = 150;
    private static final float POLYLINE_WIDTH = 5F;
    private static final long DIRECTION_ANIMATE_DURATION = 3000L;

    private static final String LOCATION_ADDRESS = "LOCATION_ADDRESS_KEY";
    private static final String DESTINATION_ADDRESS = "DESTINATION_ADDRESS_KEY";
    private static final String DIRECTION_LEGS_KEY = "legs";
    private static final String DIRECTION_DURATION_KEY = "duration";
    private static final String DIRECTION_DISTANCE_KEY = "distance";
    private static final String DIRECTION_ADDRESS_KEY = "end_address";
    private static final String DIRECTION_TEXT_KEY = "text";
    private static final String START_ADDRESS_KEY = "start_address";

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private EditText destinationEditText;
    private ProgressBar userProgressBar;
    private FloatingActionButton pickupRequestButton;

    private GoogleMap userGoogleMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private FirebaseAuth userAuth;
    private GeoFire pickupRequestGeoFire;
    private GeoFire driverLocationGeoFire;

    private int radiusLoadAllDriver = 1; // 1km
    private int radiusFindDriver = 0; // 1km

    private IFirebaseMessagingAPI mServices;
    private LatLng destinationLocation;
    private String destination;

    private ImageView uploadImageView;
    private TextInputEditText emailEditText;
    private TextInputEditText nameEditText;
    private TextInputEditText phoneEditText;

    private DatabaseReference userTable;
    private StorageReference storageReference;
    private AlertDialog loading;

    private BottomSheetBehavior driverBottomSheetBehavior;
    private TextView distanceTextView;
    private TextView locationTextView;
    private TextView destinationTextView;
    private RecyclerView driverRecyclerView;

    private DatabaseReference driverTable;
    private DatabaseReference rateDriverTable;
    private GeoQuery findGeoQuery;

    private List<LatLng> directionPolylineList;
    private Polyline grayPolyline;
    private Polyline blackPolyline;
    private ValueAnimator polyLineAnimator;
    private IGoogleAPI mServicesGoogle;
    private List<Driver> driverList;
    private  DriverAdapter adapter;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(MyFirebaseMessaging.MESSAGE_KEY);
            switch (message) {
                case "cancel": {
                    Common.driverId = "";
                    Common.isDriverFound = false;
                    break;
                }
                case "accept":
                    break;
                case "DropOff":
                    showDialog();
                    break;
                case "cancelTrip": {
                    Common.driverId = "";
                    Common.isDriverFound = false;
                    showAlertDialog();
                    break;
                }
            }
        }
    };

    private void showAlertDialog() {
        FragmentManager fm = getSupportFragmentManager();
        CancelDialogFragment alertDialog = CancelDialogFragment.newInstance("Cancel Trip", "The driver has canceled the trip for some reason, please find another driver");
        alertDialog.show(fm, "fragment_alert");
    }

    private void showDialog() {
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNeutralButtonText("Later")
                .setNoteDescriptions(Arrays.asList("Very Bad", "Not good", "Quite ok", "Very Good", "Excellent !!!"))
                .setDefaultRating(3)
                .setTitle("Rate for driver")
                .setDescription("Please select some stars and give your feedback")
                .setCommentInputEnabled(true)
                .setDefaultComment("Comment here")
                .setStarColor(R.color.starColor)
                .setNoteDescriptionTextColor(R.color.noteDescriptionTextColor)
                .setTitleTextColor(R.color.titleTextColor)
                .setDescriptionTextColor(R.color.contentTextColor)
                .setHint("Please write your comment here ...")
                .setHintTextColor(R.color.hintTextColor)
                .setCommentTextColor(R.color.commentTextColor)
                .setCommentBackgroundColor(R.color.colorCommentBackground)
                .setWindowAnimation(R.style.MyDialogFadeAnimation)
                .setCancelable(false)
                .setCanceledOnTouchOutside(false)
                .create(UserActivity.this)
                .show();
    }

    public static Intent start(Context context) {
        return new Intent(context, UserActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: started.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter(MyFirebaseMessaging.MESSAGE_DRIVER_KEY));

        initViews();
        setupUI();
        addEvents();

    }

    private void setupLoading() {
        loading = new SpotsDialog.Builder()
                .setContext(this)
                .build();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addEvents() {
        Log.d(TAG, "addEvents: started.");
        navigationView.setNavigationItemSelectedListener(this);
        destinationEditText.setOnTouchListener(this);
        pickupRequestButton.setOnClickListener(this);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        destinationEditText = findViewById(R.id.destination_edit_text);
        userProgressBar = findViewById(R.id.user_progress_bar);
        pickupRequestButton = findViewById(R.id.pickup_request_button);
        distanceTextView = findViewById(R.id.distance_text_view);
        locationTextView = findViewById(R.id.location_address_text_view);
        destinationTextView = findViewById(R.id.destination_address_text_view);
        driverRecyclerView = findViewById(R.id.driver_recycler_view);
    }

    private void setupUI() {
        Log.d(TAG, "setupUI: started.");
        setupLoading();
        setupToolbar();
        setupNavigationView();
        setupGoogleMap();
        setupPlacesAPI();
        setupFirebase();
        setupLocation();
        initServices();
        updateTokenToDatabase();
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

    private void initServices() {
        mServices = Common.getFirebaseMessagingAPI();
        mServicesGoogle = Common.getGoogleAPI();
    }

    private void setupFirebase() {
        DatabaseReference pickupRequest = FirebaseDatabase.getInstance().getReference(PICKUP_REQUEST_TABLE_NAME);
        pickupRequestGeoFire = new GeoFire(pickupRequest);
        userAuth = FirebaseAuth.getInstance();

        // driver location
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference(DRIVER_LOCATION_TABLE_NAME);
        driverLocationGeoFire = new GeoFire(driverLocation);

        // storage
        storageReference = FirebaseStorage.getInstance().getReference();

        driverTable = FirebaseDatabase.getInstance().getReference(DRIVER_TABLE);
        rateDriverTable = FirebaseDatabase.getInstance().getReference(RATE_DRIVER_TABLE);
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

            // add my button location in bottom right
            View locationButton = ((View) Objects.requireNonNull(supportMapFragment.getView()).findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            rlp.setMargins(0, 0, 30, 150);
        }
    }

    private void setupNavigationView() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout,
                toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        updateInforUser();
    }

    private void updateInforUser() {
        View headerView = navigationView.getHeaderView(0);
        final TextView nameTextView = headerView.findViewById(R.id.name_text_view);
        final TextView emailTextView = headerView.findViewById(R.id.email_text_view);
        final TextView starTextView = headerView.findViewById(R.id.star_text_view);
        final CircleImageView avatarImageView = headerView.findViewById(R.id.avatar_image_view);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // find driver with driver id
            String userId = user.getUid();
            userTable = FirebaseDatabase.getInstance()
                    .getReference(VerifyPhoneActivity.USER_TABLE_NAME).child(userId);

            userTable.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Common.currentUser = dataSnapshot.getValue(User.class);

                    // display information of driver on navigation header.
                    if (Common.currentUser != null) {
                        nameTextView.setText(Common.currentUser.getName());
                        emailTextView.setText(Common.currentUser.getEmail());
                        starTextView.setText(Common.currentUser.getRates());
                        Glide.with(UserActivity.this).load(Common.currentUser.getAvatarUrl())
                                .placeholder(R.drawable.ic_profile)
                                .into(avatarImageView);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled: error load profile driver" + databaseError);
                }
            });
        }
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.nav_trip_history: {
                launchHistoryActivity();
                break;
            }
            case R.id.nav_edit_profile: {
                showDialogUpdateProfile();
                break;
            }
            case R.id.nav_sign_out: {
                signOut();
                break;
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void launchHistoryActivity() {
        FirebaseUser driver = FirebaseAuth.getInstance().getCurrentUser();
        if (driver != null) {
            String userId = driver.getUid();
            Intent intentHistory = HistoryActivity.start(this);
            intentHistory.putExtra(USER_ID_KEY, userId);

            intentHistory.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentHistory);
            //finish();
        }
    }

    private void showDialogUpdateProfile() {
        AlertDialog.Builder editProfileDialog = new AlertDialog.Builder(this);
        editProfileDialog.setTitle(getString(R.string.edit_profile));

        View view = LayoutInflater.from(this).inflate(R.layout.edit_user_profile, null);

        emailEditText = view.findViewById(R.id.email_edit_text);
        nameEditText = view.findViewById(R.id.name_edit_text);
        phoneEditText = view.findViewById(R.id.phone_edit_text);
        uploadImageView = view.findViewById(R.id.upload_image_view);

       /* layoutEmail = view.findViewById(R.id.layout_email_profile);
        layoutName = view.findViewById(R.id.layout_name_profile);
        layoutPhone = view.findViewById(R.id.layout_phone_profile);*/

        // display information of driver ==> ui
        emailEditText.setText(Common.currentUser.getEmail());
        nameEditText.setText(Common.currentUser.getName());
        phoneEditText.setText(Common.currentUser.getPhone());
        Glide.with(UserActivity.this).load(Common.currentUser.getAvatarUrl())
                .placeholder(R.drawable.ic_profile)
                .into(uploadImageView);

        // upload image from your phone
        uploadImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadAvatarImage();
            }
        });

        editProfileDialog.setView(view);
        handelEditProfileUser(editProfileDialog);
    }

    private void handelEditProfileUser(AlertDialog.Builder editProfileDialog) {
        editProfileDialog.setPositiveButton(getString(R.string.edit_button_dialog), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                loading.show();

                String name = Objects.requireNonNull(nameEditText.getText()).toString();
                String email = Objects.requireNonNull(emailEditText.getText()).toString();
                String phone = Objects.requireNonNull(phoneEditText.getText()).toString();

                Map<String, Object> userInfor = new HashMap<>();

                if (CommonUtils.validateName(name)) {
                    userInfor.put(NAME_KEY, name);
                }

                if (CommonUtils.validateEmail(email)) {
                    userInfor.put(EMAIL_KEY, email);
                }

                if (CommonUtils.validatePhone(phone)) {
                    userInfor.put(PHONE_KEY, phone);
                }

                userTable.updateChildren(userInfor).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        loading.dismiss();

                        if (task.isSuccessful()) {
                            showSnackBar(getString(R.string.update_infor_success));
                            // update information in navigation drawer.
                            updateInforUser();
                        } else {
                            showSnackBar(getString(R.string.update_infor_failed));
                        }
                    }
                });

            }
        }).setNegativeButton(getString(R.string.cancel_button_dialog), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // close dialog and cancel update profile of driver.
                dialog.dismiss();
            }
        });

        // show edit profile dialog on ui
        editProfileDialog.show();
    }

    private void uploadAvatarImage() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        Intent uploadIntent = new Intent();
                        uploadIntent.setAction(Intent.ACTION_GET_CONTENT);
                        uploadIntent.setType("image/*");
                        startActivityForResult(uploadIntent, UPLOAD_REQUEST_CODE);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            showSnackBar(getString(R.string.permission_denied));
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void signOut() {
        userAuth.signOut();
        // jump to login activity
        Intent intentLogin = LoginActivity.start(this);
        intentLogin.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentLogin);
        finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        userGoogleMap = googleMap;
        setupMap();
    }

    private void setupMap() {
        userGoogleMap.getUiSettings().setZoomControlsEnabled(true);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // user touch destination edit text
        if (v.getId() == R.id.destination_edit_text) {
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
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME,
                Place.Field.ADDRESS, Place.Field.LAT_LNG);

        // restrict places only in city
        LatLng pinLocation = new LatLng(Common.lastLocation.getLatitude(),
                Common.lastLocation.getLongitude());
        /*
         * distance: meter unit: 100000 = 100 km
         * heading: 0 - north, 180-south
         * */
        LatLng northSide = SphericalUtil.computeOffset(pinLocation, DISTANCE_RESTRICT, HEADING_NORTH);
        LatLng southSide = SphericalUtil.computeOffset(pinLocation, DISTANCE_RESTRICT, HEADING_SOUTH);

        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields)
                .setTypeFilter(TypeFilter.ADDRESS)
                .setCountry(VN_CODE)
                .setLocationBias(RectangularBounds.newInstance(southSide, northSide))
                .build(this);
        startActivityForResult(intent, DESTINATION_AUTOCOMPLETE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DESTINATION_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                Place place = Autocomplete.getPlaceFromIntent(data);

                destination = place.getName();
                destinationLocation = place.getLatLng();
                Log.d(TAG, "origin place address: " + place.getAddress());
                Log.d(TAG, "origin place name: " + place.getName());

                // set address for destination edit text
                destinationEditText.setText(destination);
                handleDriverDirection(destinationLocation);

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(Objects.requireNonNull(data));
                Log.d(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
                showSnackBar(getString(R.string.user_cancel_operation));
            }
        } else if (requestCode == UPLOAD_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {

                Uri imageUri = data.getData();

                CropImage.activity(imageUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(this);

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
                showSnackBar(getString(R.string.error_upload_image));
            }
        }

        // load avatar image wit crop image
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK && result != null) {
                Uri resultUri = result.getUri();

                updateUIAndServer(resultUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE && result != null) {
                Exception error = result.getError();
                Log.e(TAG, "onActivityResult: error upload image with crop" + error);
            }
        }
    }

    private void handleDriverDirection(LatLng destinationLocation) {

        if (directionPolylineList != null) {
            userGoogleMap.clear();
        }

        if (polyLineAnimator != null) {
            polyLineAnimator.cancel();
        }

        try {
            //building direction url for driver
            String directionURL = Common.directionURL(String.format(Locale.getDefault(), "%f,%f", Common.lastLocation.getLatitude(), Common.lastLocation.getLongitude()),
                    String.format(Locale.getDefault(), "%f,%f", destinationLocation.latitude, destinationLocation.longitude));
            Log.d(TAG, "direction url: " + directionURL);

            // show direction
            mServicesGoogle.getDirectionPath(directionURL)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            handleDirectionJSON(response.body());
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            Log.e(TAG, "error in show direction of driver: " + t.getMessage());
                            showSnackBar(t.getMessage());
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void handleDirectionJSON(String directionJSON) {
        try {
            JSONObject root = new JSONObject(directionJSON);
            JSONArray routes = root.getJSONArray(DIRECTION_ROUTES_KEY);

            // handle and decode direction json ==> string
            for (int i = 0; i < routes.length(); i++) {
                JSONObject route = routes.getJSONObject(i);
                JSONObject overviewPolyline = route.getJSONObject(DIRECTION_POLYLINE_KEY);
                String points = overviewPolyline.getString(DIRECTION_POINT_KEY);
                directionPolylineList = PolyUtil.decode(points);
            }
            Log.d(TAG, "direction polyline list size: " + directionPolylineList.size());

            // show direction polyline on google map
            showDirectionOnMap(directionPolylineList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showDirectionOnMap(List<LatLng> directionPolylineList) {
        // adjusting bound
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : directionPolylineList) {
            builder.include(latLng);
        }

        // handle display camera
        LatLngBounds bounds = builder.build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, DIRECTION_PADDING);
        userGoogleMap.moveCamera(cameraUpdate);

        // handle information display of direction gray polyline
        PolylineOptions grayPolylineOptions = new PolylineOptions();
        grayPolylineOptions.color(Color.GRAY);
        grayPolylineOptions.width(POLYLINE_WIDTH);
        grayPolylineOptions.startCap(new SquareCap());
        grayPolylineOptions.endCap(new SquareCap());
        grayPolylineOptions.jointType(JointType.ROUND);
        grayPolylineOptions.addAll(directionPolylineList);

        // display black polyline overlay gray polyline on google map
        grayPolyline = userGoogleMap.addPolyline(grayPolylineOptions);

        PolylineOptions blackPolylineOptions = new PolylineOptions();
        blackPolylineOptions.color(Color.BLACK);
        blackPolylineOptions.width(POLYLINE_WIDTH);
        blackPolylineOptions.startCap(new SquareCap());
        blackPolylineOptions.endCap(new SquareCap());
        blackPolylineOptions.jointType(JointType.ROUND);

        // display black polyline on map
        blackPolyline = userGoogleMap.addPolyline(blackPolylineOptions);

        // display default marker at destination position
        int destinationPosition = directionPolylineList.size() - 1;
        Marker destinationMarker = userGoogleMap.addMarker(new MarkerOptions()
                .position(directionPolylineList.get(destinationPosition))
                .title(destinationEditText.getText().toString())
        );
        // show destination marker title
        destinationMarker.showInfoWindow();

        animateDirectionPolyline();
    }

    private void animateDirectionPolyline() {
        // animation polyline
        polyLineAnimator = ValueAnimator.ofInt(0, 100);
        polyLineAnimator.setDuration(DIRECTION_ANIMATE_DURATION);
        polyLineAnimator.setInterpolator(new LinearInterpolator());
        polyLineAnimator.setRepeatCount(ValueAnimator.INFINITE);
        polyLineAnimator.setRepeatMode(ValueAnimator.RESTART);
        polyLineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                List<LatLng> points = grayPolyline.getPoints();
                int percentValue = (int) valueAnimator.getAnimatedValue();
                int size = points.size();
                int newPoints = (int) (size * (percentValue / 100.0f));
                List<LatLng> p = points.subList(0, newPoints);
                blackPolyline.setPoints(p);
            }
        });
        polyLineAnimator.start();
    }

    private void updateUIAndServer(final Uri resultUri) {
        loading.show();

        // random name image uploaded --> image code
        String imageName = UUID.randomUUID().toString();
        final StorageReference imageFolder = storageReference.child("images" + imageName);

        imageFolder.putFile(resultUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(final Uri uri) {
                                // update uri to driver table
                                Map<String, Object> avatarUrl = new HashMap<>();
                                avatarUrl.put(AVATAR_URL_KEY, uri.toString());

                                // update avatar url to driver table
                                userTable.updateChildren(avatarUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        loading.dismiss();
                                        if (task.isSuccessful()) {
                                            // display avatar image
                                            uploadImageView.setImageURI(resultUri);

                                            showSnackBar(getString(R.string.upload_avatar_success));
                                            // update avatar navigation drawer
                                            updateInforUser();
                                        } else {
                                            showSnackBar(getString(R.string.upload_avatar_failed));
                                        }
                                    }
                                });

                            }
                        });
                    }
                });
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
                Common.lastLocation = locationResult.getLastLocation();
                Log.d(TAG, "current location latitude: " + Common.lastLocation.getLatitude());
                Log.d(TAG, "current location longitude: " + Common.lastLocation.getLongitude());

                // display current location on the google map
                displayCurrentLocation();
            }
        };

    }

    private void displayCurrentLocation() {

        DatabaseReference driverLocationTable = FirebaseDatabase.getInstance().getReference(DRIVER_LOCATION_TABLE_NAME);
        driverLocationTable.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // reload if change driver location : offline or online of driver
                loadAllAvailableDriver();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: error in reload all available driver " + databaseError);
            }
        });

        double userLatitude = Common.lastLocation.getLatitude();
        double userLongitude = Common.lastLocation.getLongitude();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        userGoogleMap.setMyLocationEnabled(true);
        userGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);

        userGoogleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(new LatLng(userLatitude, userLongitude), USER_MAP_ZOOM)
        );

        userProgressBar.setVisibility(View.INVISIBLE);

        if (directionPolylineList != null) {
            userGoogleMap.clear();
            handleDriverDirection(destinationLocation);
        }

        if (polyLineAnimator != null) {
            polyLineAnimator.cancel();
        }

        loadAllAvailableDriver();
    }

    private void loadAllAvailableDriver() {

        GeoQuery loadAllGeoQuery = driverLocationGeoFire.queryAtLocation(new GeoLocation(
                Common.lastLocation.getLatitude(), Common.lastLocation.getLongitude()), radiusLoadAllDriver);

        loadAllGeoQuery.removeAllListeners();
        loadAllGeoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {
                FirebaseDatabase.getInstance().getReference(DRIVER_TABLE_NAME)
                        .child(key)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Driver driver = dataSnapshot.getValue(Driver.class);
                                if (driver != null) {
                                    // show driver with icon car on google map
                                    userGoogleMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(location.latitude, location.longitude))
                                            .flat(true)
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car))
                                            .title(driver.getName())
                                            .snippet(driver.getPhone())
                                    );
                                    // add driver to list
                                    driverList = new ArrayList<>();
                                    driverList.add(driver);
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.e(TAG, "load all driver error: " + databaseError.getMessage());
                                showSnackBar(databaseError.getMessage());
                            }
                        });
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (radiusLoadAllDriver <= RADIUS_LOAD_DRIVER_LIMIT) { // distance just find for 3km
                    radiusLoadAllDriver++;
                    loadAllAvailableDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(LOCATION_REQUEST_INTERVAL);
        locationRequest.setFastestInterval(LOCATION_REQUEST_FASTEST_INTERVAL);
        locationRequest.setSmallestDisplacement(LOCATION_REQUEST_DISPLACEMENT);
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void showSnackBar(String message) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    @Override
    protected void onDestroy() {

        stopLocationUpdates();
        userGoogleMap.clear();
        if (polyLineAnimator != null) {
            polyLineAnimator.cancel();
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.pickup_request_button) {
            saveCurrentLocationOfUser();
        }
    }

    private void saveCurrentLocationOfUser() {
        // save current location of user into pickup request
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        pickupRequestGeoFire.setLocation(
                userId,
                new GeoLocation(
                        Common.lastLocation.getLatitude(),
                        Common.lastLocation.getLongitude()),
                new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        if (error == null) {
                            showDestinationDetail();
                        }
                    }
                }
        );


    }

    private void showDestinationDetail() {
        if (destinationLocation != null && destination != null) {
            View view = findViewById(R.id.driver_bottom_sheet);
            driverBottomSheetBehavior = BottomSheetBehavior.from(view);
            driverBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

            // display information detail of trip
            displayPlaceDetail(
                    String.format(Locale.getDefault(), "%f,%f", Common.lastLocation.getLatitude(), Common.lastLocation.getLongitude()),
                    String.format(Locale.getDefault(), "%f,%f", destinationLocation.latitude, destinationLocation.longitude)
            );
        } else {
            showSnackBar(getString(R.string.please_enter_destination_address));
        }
    }

    private void displayPlaceDetail(String mLocationAddress, String mDestinationAddress) {
        try {
            String userCallURL = Common.directionURL(mLocationAddress, mDestinationAddress);

            mServicesGoogle.getDirectionPath(userCallURL)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            try {
                                JSONObject root = new JSONObject(response.body());
                                JSONArray routes = root.getJSONArray(DIRECTION_ROUTES_KEY);
                                JSONObject routeObject = routes.getJSONObject(0);
                                JSONArray legs = routeObject.getJSONArray(DIRECTION_LEGS_KEY);
                                JSONObject legObject = legs.getJSONObject(0);

                               /* // get time and display on time text view
                                JSONObject time = legObject.getJSONObject(DIRECTION_DURATION_KEY);
                                String minutes = time.getString(DIRECTION_TEXT_KEY);
                                Log.d(TAG, "minutes: " + time.getString(DIRECTION_TEXT_KEY));

                                int timeFormatted = Integer.parseInt(minutes.replaceAll("\\D+", ""));*/

                                // get distance and display on distance text view
                                JSONObject distance = legObject.getJSONObject(DIRECTION_DISTANCE_KEY);
                                String km = distance.getString(DIRECTION_TEXT_KEY);
                                Log.d(TAG, "km: " + distance.getString(DIRECTION_TEXT_KEY));

                                //double distanceFormatted = Double.parseDouble(km.replaceAll("[^0-9\\\\.]", ""));

                                // get end address and display on address text view
                                String destinationAddress = legObject.getString(DIRECTION_ADDRESS_KEY);
                                String locationAddress = legObject.getString(START_ADDRESS_KEY);
                                Log.d(TAG, "destination address: " + destinationAddress);
                                Log.d(TAG, "location address: " + locationAddress);

                                // set value for text view
                                locationTextView.setText(locationAddress);
                                destinationTextView.setText(destinationAddress);
                                distanceTextView.setText(km);

                                if (driverList != null) {
                                    loadAllDriverToRecyclerView(driverList, km, locationAddress, destinationAddress, destinationLocation);
                                }

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

    private void loadAllDriverToRecyclerView(List<Driver> driverList, String km,
                                             String locationAddress, String destinationAddress, LatLng destinationLocation) {
        driverRecyclerView.setHasFixedSize(true);
        driverRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new DriverAdapter(this, driverList, km, locationAddress, destinationAddress, destinationLocation);
        driverRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

   /* private void findDriver() {
        findGeoQuery = driverLocationGeoFire.queryAtLocation(
                new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()), radiusFindDriver
        );

        findGeoQuery.removeAllListeners();
        findGeoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(final String key, GeoLocation location) {
                if (!Common.isDriverFound) {
                    Common.isFind = true;
                    DatabaseReference driverTable = FirebaseDatabase.getInstance().getReference(DRIVER_TABLE_NAME)
                            .child(key);
                    driverTable.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Driver driverPickupRequest = dataSnapshot.getValue(Driver.class);
                            Log.d(TAG, "sate: " + driverPickupRequest.getState());
                            if (driverPickupRequest.getState().equals("not_working") && driverPickupRequest.getCancel().equals("0")) {
                                Common.isDriverFound = true;
                                Common.driverId = key;
                                Toast.makeText(UserActivity.this, "" + key, Toast.LENGTH_SHORT).show();

                                driverNameTextView.setText(driverPickupRequest.getName());
                                if (driverPickupRequest.getRates() == null) {
                                    driverStarTextView.setText("No");
                                } else {
                                    driverStarTextView.setText(driverPickupRequest.getRates());
                                }
                                phoneNumberDriver = driverPickupRequest.getPhone();
                                driverPhoneTextView.setText(phoneNumberDriver);
                                // show call detail
                                callDriverBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                            } else {
                                Common.isFind = false;
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!Common.isFind && !Common.isDriverFound && radiusFindDriver < RADIUS_LOAD_DRIVER_LIMIT) {
                    radiusFindDriver++;
                    findDriver();
                } else {
                    if (!Common.isDriverFound && !Common.isFind) {
                        showSnackBar(getString(R.string.no_driver));
                        findGeoQuery.removeAllListeners();
                    }
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }*/

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
    }
*/
}
