package com.android.cast.dlna.dmr.player;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.cast.dlna.dmr.DLNARendererService;
import com.android.cast.dlna.dmr.ILogger;

public class CastVideoPlayerLeanbackFragment extends Fragment {
    private ILogger mLogger = new ILogger.DefaultLoggerImpl(this);
    private CastMediaController mCastControlImp;
    private DLNARendererService mRendererService;
    private ServiceConnection mRendererServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mLogger.i(String.format("onServiceConnected: [%s]", name.getShortClassName()));

            mRendererService = ((DLNARendererService.RendererServiceBinder) service).getRendererService();

            mRendererService.registerControlBridge(mCastControlImp = new CastMediaController(CastVideoPlayerLeanbackFragment.this, mRendererService));

            //TODO: service maybe bind more than once!
            new RendererThread.AvControlThread((RendererThread.IActivityAliveCallback) getActivity(), mRendererService).start();

            new RendererThread.AudioControlThread((RendererThread.IActivityAliveCallback) getActivity(), mRendererService).start();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mLogger.w(String.format("onServiceDisconnected: [%s]", name.getShortClassName()));

            mRendererService = null;
        }
    };

    public static CastVideoPlayerLeanbackFragment newInstance(CastMediaRequest castMediaRequest) {
        CastVideoPlayerLeanbackFragment fragment = new CastVideoPlayerLeanbackFragment();

        Bundle arguments = new Bundle();

        arguments.putParcelable(CastVideoPlayerLeanbackActivity.EXTRA_KEY_VIDEO, castMediaRequest);

        fragment.setArguments(arguments);

        return fragment;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() != null) {
            getActivity().bindService(new Intent(getActivity(), DLNARendererService.class), mRendererServiceConnection, Service.BIND_AUTO_CREATE);
        }

        if (getArguments() != null) {
            playVideo((CastMediaRequest) getArguments().getParcelable(CastVideoPlayerLeanbackActivity.EXTRA_KEY_VIDEO));
        }
    }

    public void playVideo(CastMediaRequest castMediaRequest) {
        //openMedia(new MediaRequest(castMediaRequest.videoURL), castMediaRequest.title, castMediaRequest.subTitle);
    }

    @Override
    public void onDestroyView() {
        if (mRendererService != null) {
            mRendererService.unregisterControlBridge(mCastControlImp);
        }

        if (getActivity() != null) {
            getActivity().unbindService(mRendererServiceConnection);
        }

        super.onDestroyView();
    }
}
