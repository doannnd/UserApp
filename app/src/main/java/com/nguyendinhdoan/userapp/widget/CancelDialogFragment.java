package com.nguyendinhdoan.userapp.widget;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.nguyendinhdoan.userapp.R;

import java.util.Objects;

public class CancelDialogFragment extends DialogFragment {

    private static final String MESSAGE_KEY = "MESSAGE_KEY";

    public static CancelDialogFragment newInstance(String message) {
        CancelDialogFragment fragment = new CancelDialogFragment();
        Bundle args = new Bundle();
        args.putString(MESSAGE_KEY, message);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_fragment_cancel, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView messageCancelTextView = view.findViewById(R.id.message_cancel_text_view);
        Objects.requireNonNull(getDialog().getWindow()).requestFeature(Window.FEATURE_NO_TITLE);
        if (getArguments() != null) {
            String message = getArguments().getString(MESSAGE_KEY);
            messageCancelTextView.setText(message);
        }
    }

}
