package com.nguyendinhdoan.userapp.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nguyendinhdoan.userapp.R;
import com.nguyendinhdoan.userapp.common.Common;
import com.nguyendinhdoan.userapp.model.RateDriver;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RateActivity extends FragmentActivity implements RatingDialogListener {

    private static final String TAG = "RateActivity";

    private static final String RATE_DRIVER_TABLE = "rate_driver";
    private static final String DRIVER_TABLE = "drivers";

    private DatabaseReference driverTable;
    private DatabaseReference rateDriverTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);

        driverTable = FirebaseDatabase.getInstance().getReference(DRIVER_TABLE);
        rateDriverTable = FirebaseDatabase.getInstance().getReference(RATE_DRIVER_TABLE);

        showDialog();
    }

    private void showDialog() {
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNeutralButtonText("Later")
                .setNoteDescriptions(Arrays.asList("Very Bad", "Not good", "Quite ok", "Very Good", "Excellent !!!"))
                .setDefaultRating(2)
                .setTitle("Rate for driver")
                .setDescription("Please select some stars and give your feedback")
                .setCommentInputEnabled(true)
                .setDefaultComment("")
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
                .create(RateActivity.this)
                .show();
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
                                                sumStar += Double.parseDouble(rateDriver1.getRates());
                                                count++;
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
                                                                Toast.makeText(RateActivity.this, "Thank you your submit", Toast.LENGTH_SHORT).show();
                                                                finish();
                                                            } else {
                                                                Toast.makeText(RateActivity.this, "rate updated but can't write to driver table", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(RateActivity.this, "error occur", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
