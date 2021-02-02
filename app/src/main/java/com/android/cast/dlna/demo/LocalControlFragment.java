package com.android.cast.dlna.demo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.cast.dlna.DLNACastManager;
import com.android.cast.dlna.dms.MediaServer;
import com.android.cast.dlna.dms.Utils;
import com.orhanobut.logger.Logger;

import org.fourthline.cling.model.meta.Device;

public class LocalControlFragment extends Fragment implements IDisplayDevice {

    private static final int REQUEST_CODE_SELECT = 222;
    private TextView mPickupContent;
    private MediaServer mMediaServer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_local_control, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMediaServer = new MediaServer(view.getContext());
        mMediaServer.start();
        DLNACastManager.getInstance().addMediaServer(mMediaServer);
        //ContentFactory.getInstance().getContent(MediaItem.VIDEO_ID);

        initComponent(view);
    }

    private void initComponent(View view) {
        String selectPath = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("selectPath", "");
        mPickupContent = view.findViewById(R.id.local_ctrl_pick_content_text);
        mPickupContent.setText(selectPath);
        mCastPathUrl = selectPath;
        view.findViewById(R.id.local_ctrl_pick_content).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            // intent.setType("text/plain");
            intent.setType("video/*;audio/*;image/*");
            startActivityForResult(intent, REQUEST_CODE_SELECT);
        });
        view.findViewById(R.id.local_ctrl_cast).setOnClickListener(v -> {
                    if (mDevice != null) {
                        DLNACastManager.getInstance().cast(mDevice, CastObject.newInstance(mCastPathUrl, Constants.CAST_ID, Constants.CAST_NAME));
                    }
                }
        );
    }

    private String mCastPathUrl = "";

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_SELECT && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            String path = Utils.parseUri2Path(getActivity(), uri);
            Logger.i("onActivityResult: " + uri.toString());
            Logger.i("onActivityResult: " + path);
            mCastPathUrl = mMediaServer.getBaseUrl() + path;
            mPickupContent.setText(mCastPathUrl);
            PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .edit().putString("selectPath", mCastPathUrl)
                    .apply();
        }
    }

    private Device<?, ?, ?> mDevice;

    @Override
    public void setCastDevice(Device<?, ?, ?> device) {
        mDevice = device;
    }

    @Override
    public void onDestroyView() {
        mMediaServer.stop();
        super.onDestroyView();
    }
}
