package com.neulion.android.upnpcast.renderer.player;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.neulion.android.upnpcast.renderer.NLUpnpRendererService;
import com.neulion.android.upnpcast.renderer.NLUpnpRendererService.RendererServiceBinder;
import com.neulion.android.upnpcast.renderer.player.RendererThread.AudioControlThread;
import com.neulion.android.upnpcast.renderer.player.RendererThread.AvControlThread;
import com.neulion.android.upnpcast.renderer.player.RendererThread.IActivityAliveCallback;
import com.neulion.android.upnpcast.renderer.utils.ILogger;
import com.neulion.android.upnpcast.renderer.utils.ILogger.DefaultLoggerImpl;
import com.neulion.media.control.MediaRequest;
import com.neulion.media.tv.fragment.VideoControllerFragment;

public class NLCastVideoPlayerLeanbackFragment extends VideoControllerFragment
{
    public static NLCastVideoPlayerLeanbackFragment newInstance(CastMediaRequest castMediaRequest)
    {
        NLCastVideoPlayerLeanbackFragment fragment = new NLCastVideoPlayerLeanbackFragment();

        Bundle arguments = new Bundle();

        arguments.putParcelable(NLCastVideoPlayerLeanbackActivity.EXTRA_KEY_VIDEO, castMediaRequest);

        fragment.setArguments(arguments);

        return fragment;
    }

    private ILogger mLogger = new DefaultLoggerImpl(this);

    private NLCastMediaController mCastControlImp;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() != null)
        {
            getActivity().bindService(new Intent(getActivity(), NLUpnpRendererService.class), mRendererServiceConnection, Service.BIND_AUTO_CREATE);
        }

        if (getArguments() != null)
        {
            playVideo((CastMediaRequest) getArguments().getParcelable(NLCastVideoPlayerLeanbackActivity.EXTRA_KEY_VIDEO));
        }
    }

    public void playVideo(CastMediaRequest castMediaRequest)
    {
        openMedia(new MediaRequest(castMediaRequest.videoURL), castMediaRequest.title, castMediaRequest.subTitle);
    }

    @Override
    public void onDestroyView()
    {
        if (mRendererService != null)
        {
            mRendererService.unregisterControlBridge(mCastControlImp);
        }

        if (getActivity() != null)
        {
            getActivity().unbindService(mRendererServiceConnection);
        }

        super.onDestroyView();
    }

    private NLUpnpRendererService mRendererService;

    private ServiceConnection mRendererServiceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            mLogger.i(String.format("onServiceConnected: [%s]", name.getShortClassName()));

            mRendererService = ((RendererServiceBinder) service).getRendererService();

            mRendererService.registerControlBridge(mCastControlImp = new NLCastMediaController(NLCastVideoPlayerLeanbackFragment.this, mRendererService, getNLCommonVideoView()));

            //TODO: service maybe bind more than once!
            new AvControlThread((IActivityAliveCallback) getActivity(), mRendererService).start();

            new AudioControlThread((IActivityAliveCallback) getActivity(), mRendererService).start();
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            mLogger.w(String.format("onServiceDisconnected: [%s]", name.getShortClassName()));

            mRendererService = null;
        }
    };
}
