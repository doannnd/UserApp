package com.nguyendinhdoan.userapp.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.nguyendinhdoan.userapp.R;
import com.wang.avi.AVLoadingIndicatorView;

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

    public static Intent start(Context context) {
        return new Intent(context, CallActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        initViews();
        setupUI();
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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel_button: {
                break;
            }
            case R.id.phone_text_view: {
                break;
            }
        }
    }
}
