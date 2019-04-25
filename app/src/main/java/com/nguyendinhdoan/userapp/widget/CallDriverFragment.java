package com.nguyendinhdoan.userapp.widget;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nguyendinhdoan.userapp.R;

public class CallDriverFragment extends BottomSheetDialogFragment {

    private static final String CALL_DRIVER_TITLE_KEY = "CALL_DRIVER_TITLE_KEY";
    private String title;

    public static CallDriverFragment newInstance(String title) {
        CallDriverFragment callDriverFragment = new CallDriverFragment();
        Bundle args = new Bundle();
        args.putString(CALL_DRIVER_TITLE_KEY, title);
        callDriverFragment.setArguments(args);
        return callDriverFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            title = getArguments().getString(CALL_DRIVER_TITLE_KEY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_call_driver, container, false);
    }
}
