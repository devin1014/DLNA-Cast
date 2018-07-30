//package com.neulion.android.upnpcast.renderer.player;
//
//import android.content.ComponentName;
//import android.content.ServiceConnection;
//import android.os.Bundle;
//import android.os.IBinder;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.v4.app.Fragment;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import com.neulion.android.upnpcast.renderer.Constants.Key;
//import com.neulion.android.upnpcast.renderer.NLUpnpRendererService;
//import com.neulion.android.upnpcast.renderer.NLUpnpRendererService.RendererServiceBinder;
//import com.neulion.android.upnpcast.renderer.R;
//import com.neulion.android.upnpcast.renderer.player.RendererThread.AudioControlThread;
//import com.neulion.android.upnpcast.renderer.player.RendererThread.AvControlThread;
//import com.neulion.android.upnpcast.renderer.utils.CastUtils;
//import com.neulion.android.upnpcast.renderer.utils.ILogger;
//import com.neulion.android.upnpcast.renderer.utils.ILogger.DefaultLoggerImpl;
//import com.neulion.media.control.VideoController.ControlBar;
//import com.neulion.media.control.impl.CommonVideoController;
//
//import java.net.URI;
//
///**
// * User: liuwei(wei.liu@neulion.com.com)
// * Date: 2018-07-30
// * Time: 14:38
// */
//public class NLCastVideoPlayerFragment extends Fragment implements ServiceConnection
//{
//    public static NLCastVideoPlayerFragment newInstance(Bundle argument)
//    {
//        NLCastVideoPlayerFragment fragment = new NLCastVideoPlayerFragment();
//
//        fragment.setArguments(argument);
//
//        return fragment;
//    }
//
//    private ILogger mLogger = new DefaultLoggerImpl(this);
//
//    private CommonVideoController mVideoController;
//
//    private NLCastVideoPlayerController mCastControlImp;
//
//    private NLCastVideoPlayerActivity mActivity;
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
//    {
//        return inflater.inflate(R.layout.fragmnet_cast_videoplayer, container, false);
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
//    {
//        super.onViewCreated(view, savedInstanceState);
//
//        mLogger.d("onCreate: " + getArguments());
//
//        mVideoController = view.findViewById(R.id.d_video_controller);
//
//        ControlBar topControlBar = mVideoController.findViewById(R.id.m_top_bar_panel);
//
//        topControlBar.setSupported(false);
//
//        mVideoController.setSupportFullScreenControls(false);
//
//        mVideoController.setFullScreen(true);
//
//        mActivity = (NLCastVideoPlayerActivity) getActivity();
//
//        openMedia();
//    }
//
//    //private URI mURI;
//
//    @SuppressWarnings("ConstantConditions")
//    private void openMedia()
//    {
//        String url = getArguments().getString(Key.EXTRA_URL);
//
//        URI mURI = CastUtils.parseURI(url);
//
//        if (mURI != null)
//        {
//            mLogger.i("open Media: " + mURI.toString());
//
//            mVideoController.openMedia(mURI.toString());
//        }
//        else
//        {
//            mLogger.i("URI is NULL!");
//
//            mActivity.finish();
//        }
//    }
//
//    @Override
//    public void onDestroyView()
//    {
//        mLogger.w("onDestroyView");
//
//        if (mVideoController != null)
//        {
//            mVideoController.releaseMedia();
//        }
//
//        onServiceDisconnected(null);
//
//        super.onDestroyView();
//    }
//
//    private NLUpnpRendererService mRendererService;
//
//    private Boolean[] mServiceConnected = new Boolean[]{false};
//
//    @Override
//    public void onServiceConnected(ComponentName name, IBinder service)
//    {
//        mLogger.i(String.format("onServiceConnected: [%s]", name.getShortClassName()));
//
//        mRendererService = ((RendererServiceBinder) service).getRendererService();
//
//        //noinspection ConstantConditions
//        mRendererService.registerControlBridge(mCastControlImp = new NLCastVideoPlayerController(getActivity(), mRendererService, mVideoController));
//
//        mServiceConnected[0] = true;
//
//        //TODO: service maybe bind more than once!
//        new AvControlThread(this, mRendererService, mServiceConnected, mVideoController).start();
//
//        new AudioControlThread(this, mRendererService, mServiceConnected, mVideoController).start();
//    }
//
//    @Override
//    public void onServiceDisconnected(ComponentName name)
//    {
//        if (mRendererService != null)
//        {
//            mRendererService.unregisterControlBridge(mCastControlImp);
//        }
//
//        mRendererService = null;
//
//        mServiceConnected[0] = false;
//    }
//}
