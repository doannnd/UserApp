package com.nguyendinhdoan.userapp.widget;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nguyendinhdoan.userapp.R;

public class CancelDialogFragment extends DialogFragment {

    public static CancelDialogFragment newInstance() {
        CancelDialogFragment fragment = new CancelDialogFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_fragment_cancel, container);
    }

}
