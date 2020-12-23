package com.android.cast.dlna.demo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class CastFragment extends DialogFragment {

    public interface Callback {
        void onCastUrl(String url);
    }

    private Callback mCallback;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cast, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.cast_url_ok).setOnClickListener(v -> {
            RadioGroup group = view.findViewById(R.id.cast_url_group);
            if (group.getCheckedRadioButtonId() == R.id.cast_url_mp4) {
                mCallback.onCastUrl(Constants.CAST_URL_MP4_INNER);
            } else if (group.getCheckedRadioButtonId() == R.id.cast_url_hls_1) {
                mCallback.onCastUrl(Constants.CAST_URL_HLS_BT_INNER);
            } else if (group.getCheckedRadioButtonId() == R.id.cast_url_hls_2) {
                mCallback.onCastUrl(Constants.CAST_URL_HLS_CC_INNER);
            } else if (group.getCheckedRadioButtonId() == R.id.cast_url_hls_3) {
                mCallback.onCastUrl(Constants.CAST_URL_IPHONE_SAMPLE);
            }
            dismiss();
        });
    }

    public CastFragment setCallback(Callback callback) {
        mCallback = callback;
        return this;
    }
}
