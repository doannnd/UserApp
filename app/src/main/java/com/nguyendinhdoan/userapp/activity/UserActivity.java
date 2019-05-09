package com.nguyendinhdoan.userapp.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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
import com.google.gson.Gson;
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
import com.nguyendinhdoan.userapp.common.Common;
import com.nguyendinhdoan.userapp.model.Body;
import com.nguyendinhdoan.userapp.model.Notification;
import com.nguyendinhdoan.userapp.model.RateDriver;
import com.nguyendinhdoan.userapp.model.Result;
import com.nguyendinhdoan.userapp.model.Sender;
import com.nguyendinhdoan.userapp.model.Token;
import com.nguyendinhdoan.userapp.model.User;
import com.nguyendinhdoan.userapp.remote.IFirebaseMessagingAPI;
import com.nguyendinhdoan.userapp.services.MyFirebaseIdServices;
import com.nguyendinhdoan.userapp.services.MyFirebaseMessaging;
import com.nguyendinhdoan.userapp.utils.CommonUtils;
import com.nguyendinhdoan.userapp.widget.PlaceDetailFragment;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.DecimalFormat;
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
        View.OnTouchListener, View.OnClickListener, RatingDialogListener {

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


    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private EditText destinationEditText;
    private ProgressBar userProgressBar;
    private ImageView upImageView;
    private Button pickupRequestButton;

    private GoogleMap userGoogleMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private FirebaseAuth userAuth;
    private Location lastLocation;
    private Marker userMarker;
    private Marker driverMarker;
    private GeoFire pickupRequestGeoFire;
    private GeoFire driverLocationGeoFire;

    private int radiusLoadAllDriver = 1; // 1km
    private int radiusFindDriver = 1; // 1km

    private IFirebaseMessagingAPI mServices;
    private LatLng destinationLocation;
    private String destination;
    private Marker destinationMarker;

    private ImageView uploadImageView;
    private TextInputEditText emailEditText;
    private TextInputEditText nameEditText;
    private TextInputEditText phoneEditText;

    private DatabaseReference userTable;
    private StorageReference storageReference;
    private AlertDialog loading;

    private BottomSheetBehavior callDriverBehavior;
    private TextView driverNameTextView;
    private TextView driverStarTextView;
    private TextView driverPhoneTextView;
    private Button driverCallButton;
    private TextView resultCallDriverTextView;

    private String phoneNumberDriver;

    private DatabaseReference driverTable;
    private DatabaseReference rateDriverTable;
    private  GeoQuery findGeoQuery;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(MyFirebaseMessaging.MESSAGE_KEY);
            if (message.equals("cancel")) {
                Common.driverId = "";
                Common.isDriverFound = false;
                pickupRequestButton.setText(getString(R.string.pickup_request_button_text));
                resultCallDriverTextView.setText(getString(R.string.driver_decline_request));
                resultCallDriverTextView.setTextColor(Color.RED);
            } else if (message.equals("accept")) {
                pickupRequestButton.setText(getString(R.string.pickup_request_button_text));
                resultCallDriverTextView.setText(getString(R.string.driver_accept_request));
                resultCallDriverTextView.setTextColor(Color.BLUE);
                driverCallButton.setEnabled(false);
            } else if (message.equals("DropOff")) {
                showDialog();
                resultCallDriverTextView.setText("");
            }
        }
    };

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
        upImageView.setOnClickListener(this);
        pickupRequestButton.setOnClickListener(this);
        driverPhoneTextView.setOnClickListener(this);
        driverCallButton.setOnClickListener(this);
    }

    private void initViews() {
        Log.d(TAG, "initViews: started.");
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        destinationEditText = findViewById(R.id.destination_edit_text);
        userProgressBar = findViewById(R.id.user_progress_bar);
        upImageView = findViewById(R.id.up_image_view);
        pickupRequestButton = findViewById(R.id.pickup_request_button);

        View view = findViewById(R.id.call_driver_bottom_sheet);
        callDriverBehavior = BottomSheetBehavior.from(view);
        driverNameTextView = view.findViewById(R.id.driver_name_text_view);
        driverStarTextView = view.findViewById(R.id.driver_star_text_view);
        driverPhoneTextView = view.findViewById(R.id.driver_phone_text_view);
        driverCallButton = view.findViewById(R.id.driver_call_button);
        resultCallDriverTextView = view.findViewById(R.id.result_call_driver_text_view);
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
        }

        // add my button location in bottom right
        View locationButton = ((View) supportMapFragment.getView().findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.setMargins(0, 0, 30, 30);
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
        userGoogleMap.setIndoorEnabled(false);
        userGoogleMap.setBuildingsEnabled(false);
        userGoogleMap.setTrafficEnabled(false);
        userGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
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
        LatLng pinLocation = new LatLng(lastLocation.getLatitude(),
                lastLocation.getLongitude());
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

                // add marker destination on google map
                //userGoogleMap.clear();

                // display default marker at destination location
                displayDestinationMarker(destination, destinationLocation);
                // display place detail
                displayPlaceDetail();

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

    private void displayDestinationMarker(String destination, LatLng destinationLocation) {

        if (destinationMarker != null) {
            destinationMarker.remove();
        }

        destinationMarker = userGoogleMap.addMarker(
                new MarkerOptions().position(destinationLocation)
                        .title(destination)
                        .icon(BitmapDescriptorFactory.defaultMarker())
        );

        destinationMarker.showInfoWindow();

        // padding 2 marker: current location and destination location
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
        builder.include(destinationLocation);

        // handle display camera
        LatLngBounds bounds = builder.build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, GOOGLE_MAP_PADDING);
        userGoogleMap.moveCamera(cameraUpdate);
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
                lastLocation = locationResult.getLastLocation();
                Log.d(TAG, "current location latitude: " + lastLocation.getLatitude());
                Log.d(TAG, "current location longitude: " + lastLocation.getLongitude());

                // display current location on the google map
                displayCurrentLocation();
            }
        };

    }

    private void displayCurrentLocation() {

        DatabaseReference driverLocationTable = FirebaseDatabase.getInstance().getReference(DRIVER_LOCATION_TABLE_NAME);
        // if any change from driver location table ==> reload all driver
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

        /*if (userMarker != null) {
            userMarker.remove(); // if marker existed --> delete
        }*/

        double userLatitude = lastLocation.getLatitude();
        double userLongitude = lastLocation.getLongitude();

        // draw marker on google map
        /*userMarker = userGoogleMap.addMarker(new MarkerOptions()
                .position(new LatLng(userLatitude, userLongitude))
                .title(getString(R.string.title_of_you))
        );*/

        // display icon default
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // icon current location of google
        userGoogleMap.setMyLocationEnabled(true);
        // icon ..
        userGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);

        // move camera
        userGoogleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(new LatLng(userLatitude, userLongitude), USER_MAP_ZOOM)
        );

        // hide progress bar complete display current location
        userProgressBar.setVisibility(View.INVISIBLE);

        // load all available driver
        loadAllAvailableDriver();
    }

    private void loadAllAvailableDriver() {

        // first remove add marker on map
        userGoogleMap.clear();
        // after add marker of current location
       /* userGoogleMap.addMarker(
                new MarkerOptions().position(
                        new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude())
                ).title("You")
        );*/

       /*if (driverMarker != null) {
           driverMarker.remove();
       }*/
        if (destination != null) {
            displayDestinationMarker(destination, destinationLocation);
        }

        GeoQuery loadAllGeoQuery = driverLocationGeoFire.queryAtLocation(new GeoLocation(
                lastLocation.getLatitude(), lastLocation.getLongitude()), radiusLoadAllDriver);

        loadAllGeoQuery.removeAllListeners();
        loadAllGeoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {
                // use key get phone from table drivers;
                // table driver is table when driver register account ad update information
                // just open your driver to check this table name

                FirebaseDatabase.getInstance().getReference(DRIVER_TABLE_NAME)
                        .child(key)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                // Because driver and user is sample properties
                                // so we can use User model to get Driver class

                                User driver = dataSnapshot.getValue(User.class);
                                if (driver != null) {
                                    // show driver with icon car on google map
                                    userGoogleMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(location.latitude, location.longitude))
                                            .flat(true)
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car))
                                            .title(driver.getName())
                                            .snippet(getString(R.string.driver_phone, driver.getPhone()))
                                    );
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
       /* View view = snackbar.getView();
        view.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        TextView textSnack = view.findViewById(android.support.design.R.id.snackbar_text);
        textSnack.setTextColor(getResources().getColor(R.color.colorBlack));*/
        snackbar.show();
    }

    @Override
    protected void onDestroy() {

        stopLocationUpdates();
        userGoogleMap.clear();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pickup_request_button: {
                if (!Common.isDriverFound) {
                    requestPickupHere();
                    resultCallDriverTextView.setText("");
                } /*else {
                // user call driver request a car, user app send current location of user --> driver app
                sendRequestToDiver(Common.driverId);
            }*/
                break;
            }
            case R.id.up_image_view: {
                if (destination != null) {
                    displayPlaceDetail();
                }
                break;
            }
            case R.id.driver_phone_text_view: {
                callPhoneToDriver(phoneNumberDriver);
                break;
            }
            case R.id.driver_call_button: {
                // user call driver request a car, user app send current location of user --> driver app
                if (Common.driverId != null) {
                    sendRequestToDiver(Common.driverId);
                }
                break;
            }
        }

    }

    private void callPhoneToDriver(final String phoneNumberDriver) {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CALL_PHONE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        if (ActivityCompat.checkSelfPermission(UserActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
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
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void sendRequestToDiver(String driverId) {
        DatabaseReference tokenTable = FirebaseDatabase.getInstance().getReference(MyFirebaseIdServices.TOKEN_TABLE_NAME);

        // check token id equal driverId
        tokenTable.orderByKey().equalTo(driverId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot tokenSnapshot : dataSnapshot.getChildren()) {
                            // get token object from database with key is driverId
                            Token token = tokenSnapshot.getValue(Token.class);
                            String jsonLocation;
                            // if user pickup request ==> send destination to driver app
                            if (destinationLocation != null && destination != null) {
                                Body body = new Body(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()),
                                        destinationLocation, destination);
                                jsonLocation = new Gson().toJson(body);
                            } else {
                                // convert LatLng to json , next send json to driver app
                                // if no send current location of user
                                jsonLocation = new Gson().toJson(
                                        new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude())
                                );
                            }

                            // send notification for driver app [jsonLocation - body] and title is user id token
                            FirebaseUser user = userAuth.getCurrentUser();
                            Notification notification = null;
                            if (user != null) {
                                String userId = user.getUid();
                                notification = new Notification(userId, jsonLocation);
                            }

                            // content send have driverId, --> driver is people user want to call
                            if (token != null) {
                                String driverIdToken = token.getToken();
                                Sender sender = new Sender(notification, driverIdToken);

                                // handle send to driver app firebase cloud messaging
                                mServices.sendMessage(sender)
                                        .enqueue(new Callback<Result>() {
                                            @Override
                                            public void onResponse(@NonNull Call<Result> call,
                                                                   @NonNull Response<Result> response) {
                                                if (response.isSuccessful()) {
                                                    showSnackBar(getString(R.string.send_message_to_driver_success));
                                                    callDriverBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                                                } else {
                                                    showSnackBar(getString(R.string.send_message_to_driver_error));
                                                }
                                            }

                                            @Override
                                            public void onFailure(@NonNull Call<Result> call,
                                                                  @NonNull Throwable t) {
                                                showSnackBar(t.getMessage());
                                                Log.e(TAG, "send message to driver app error" + t.getMessage());
                                            }
                                        });
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void requestPickupHere() {
        FirebaseUser user = userAuth.getCurrentUser();
        if (user != null && lastLocation != null) {
            String userId = user.getUid();

            pickupRequestGeoFire.setLocation(userId, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()),
                    new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if (error == null) {
                                //pickupRequestButton.setText(getString(R.string.call_driver_button_text));

                               /* if (userMarker != null) {
                                    userMarker.remove();
                                }

                                // add new marker
                                userMarker = userGoogleMap.addMarker(new MarkerOptions()
                                        .title("SET PICKUP LOCATION")
                                        .position(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()))
                                );
                                // always show ...
                                userMarker.showInfoWindow();*/

                                // find Driver when pickup request
                                findDriver();

                            } else {
                                showSnackBar(getString(R.string.error_message));
                            }
                        }
                    });
        }
    }

    private void findDriver() {
        findGeoQuery = driverLocationGeoFire.queryAtLocation(
                new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()), radiusFindDriver
        );

        findGeoQuery.removeAllListeners();
        findGeoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!Common.isDriverFound) {
                    Common.isDriverFound = true;
                    Common.driverId = key;
                    Toast.makeText(UserActivity.this, "" + key, Toast.LENGTH_SHORT).show();

                    DatabaseReference driverTable = FirebaseDatabase.getInstance().getReference(DRIVER_TABLE_NAME)
                            .child(key);
                    driverTable.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User driverPickupRequest = dataSnapshot.getValue(User.class);
                            if (driverPickupRequest != null) {
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
                if (!Common.isDriverFound && radiusFindDriver < RADIUS_LOAD_DRIVER_LIMIT) {
                    radiusFindDriver++;
                    findDriver();
                } else {
                    if (!Common.isDriverFound) {
                        showSnackBar(getString(R.string.no_driver));
                        //findGeoQuery.removeAllListeners();
                    }
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    private void displayPlaceDetail() {
        // convert location, latng --> string
        PlaceDetailFragment placeDetailFragment =
                PlaceDetailFragment.newInstance(
                        String.format(Locale.getDefault(), "%f,%f", lastLocation.getLatitude(), lastLocation.getLongitude()),
                        String.format(Locale.getDefault(), "%f,%f", destinationLocation.latitude, destinationLocation.longitude)
                );

        placeDetailFragment.show(getSupportFragmentManager(), placeDetailFragment.getTag());
    }

    @Override
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
                                                                driverCallButton.setEnabled(true);
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
}
