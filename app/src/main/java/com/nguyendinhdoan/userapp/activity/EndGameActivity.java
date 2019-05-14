package com.nguyendinhdoan.userapp.activity;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nguyendinhdoan.userapp.R;
import com.nguyendinhdoan.userapp.common.Common;
import com.nguyendinhdoan.userapp.model.Driver;
import com.nguyendinhdoan.userapp.model.RateDriver;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EndGameActivity extends AppCompatActivity implements RatingBar.OnRatingBarChangeListener, View.OnClickListener {

    private static final String TAG = "EndGameActivity";
    public static final String RATE_DRIVER_TABLE_NAME = "rate_driver";
    public static final String DRIVERS_TABLE_NAME = "drivers";
    public static final String RATES_KEY = "rates";

    private Toolbar endGameToolbar;
    private ImageView closeImageView;
    private TextView dateTimeTextView;
    private TextView tripPriceTextView;
    private CircleImageView avatarImageView;
    private RatingBar driverRatingBar;

    private Driver driver;
    private DatabaseReference rateDriverTable;
    private DatabaseReference driverTable;
    private String tripPrice;
    private double rates;

    public static Intent start(Context context) {
        return new Intent(context, EndGameActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_game);

        initViews();
        setupUI();
        addEvents();
    }

    private void addEvents() {
        driverRatingBar.setOnRatingBarChangeListener(this);
        closeImageView.setOnClickListener(this);
    }

    private void setupUI() {
        setupToolbar();
        getDataFromTrackingActivity();
        initTableDatabase();
    }

    private void setupToolbar() {
        setSupportActionBar(endGameToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void initTableDatabase() {
        rateDriverTable = FirebaseDatabase.getInstance()
                .getReference(RATE_DRIVER_TABLE_NAME);
        driverTable = FirebaseDatabase.getInstance().getReference(DRIVERS_TABLE_NAME);
    }

    private void getDataFromTrackingActivity() {
        if (getIntent() != null) {
            driver = getIntent().getParcelableExtra(TrackingActivity.DRIVER_DROP_OFF_TRIP_KEY);
            tripPrice = getIntent().getStringExtra(TrackingActivity.DRIVER_DROP_OFF_PRICE_TRIP_KEY);
        }
        updateUI();
    }

    private void updateUI() {
        // load date time
        dateTimeTextView.setText(Common.getCurrentDate());

        if (driver != null) {
            Glide.with(this)
                    .load(driver.getAvatarUrl())
                    .placeholder(R.drawable.ic_profile)
                    .into(avatarImageView);
        }

        if (tripPrice != null) {
            tripPriceTextView.setText(getString(R.string.trip_price_text, tripPrice));
        }
    }

    private void initViews() {
        endGameToolbar = findViewById(R.id.end_game_toolbar);
        closeImageView = findViewById(R.id.close_image_view);
        dateTimeTextView = findViewById(R.id.date_time_text_view);
        tripPriceTextView = findViewById(R.id.trip_price_text_view);
        avatarImageView = findViewById(R.id.avatar_image_view);
        driverRatingBar = findViewById(R.id.driver_rating_bar);
    }

    @Override
    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        rates = ratingBar.getRating();
    }

    private void saveRateToDatabase() {
        if (driver.getId() != null) {
            RateDriver rateDriver = new RateDriver(String.valueOf(rates));
            rateDriverTable.child(driver.getId())
                    .push()
                    .setValue(rateDriver)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                calculateAverageRates();
                            } else {
                                Log.e(TAG, "save rates to rate_driver table error");
                            }
                        }
                    });
        }
    }

    private void calculateAverageRates() {
        rateDriverTable.child(driver.getId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        double ratesSum = 0.0;
                        int ratesCount = 0;
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            RateDriver rateDriver = data.getValue(RateDriver.class);
                            if (rateDriver != null) {
                                ratesSum = Double.parseDouble(rateDriver.getRates());
                                ratesCount++;
                            }
                        }

                        double averageRates = ratesSum / ratesCount;
                        DecimalFormat df = new DecimalFormat("#.#");
                        String valueRateOfDriver = df.format(averageRates);

                        updateRateToDriverTable(valueRateOfDriver);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG,"calculate average rates error");
                    }
                });
    }

    private void updateRateToDriverTable(String valueRateOfDriver) {
        // create object update
        Map<String, Object> driverUpdateRate = new HashMap<>();
        driverUpdateRate.put(RATES_KEY, valueRateOfDriver);

        driverTable.child(driver.getId())
                .updateChildren(driverUpdateRate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            showSnackBar(getString(R.string.thank_you));
                        } else {
                            Log.e(TAG,"save rate to driver table error");
                        }
                    }
                });
    }

    private void showSnackBar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message , Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.close_image_view) {
            if (rates == 0.0) {
                showSnackBar(getString(R.string.review_driver));
            } else {
                saveRateToDatabase();
                Intent intentUser = UserActivity.start(this);
                intentUser.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentUser);
                finish();
            }
        }
    }
}
