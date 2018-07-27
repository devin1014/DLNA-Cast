package com.neulion.android.demo.render.player;

import com.neulion.android.demo.render.NLUpnpRendererService;
import com.neulion.android.demo.render.upnp.RendererAVTransportService;
import com.neulion.android.demo.render.upnp.RendererAudioControlService;
import com.neulion.android.demo.render.utils.ILogger;
import com.neulion.android.demo.render.utils.ILogger.DefaultLoggerImpl;
import com.neulion.media.control.MediaControl;

import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.support.lastchange.LastChangeAwareServiceManager;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-07-26
 * Time: 15:41
 */
abstract class RendererThread extends Thread
{
    protected ILogger mLogger = new DefaultLoggerImpl(this);

    protected NLUpnpRendererService mRendererService;

    protected NLCastVideoPlayerActivity mActivity;

    protected MediaControl mMediaControl;

    public RendererThread(NLCastVideoPlayerActivity activity, NLUpnpRendererService service, MediaControl mediaControl)
    {
        mActivity = activity;

        mRendererService = service;

        mMediaControl = mediaControl;
    }

    @Override
    public void run()
    {
        mLogger.i("RenderThread running!");

        if (mRendererService != null)
        {
            LocalDevice localDevice = mRendererService.getLocalDevice();

            if (localDevice != null)
            {
                running(localDevice);
            }
            else
            {
                mLogger.w("LocalDevice is NULL!");
            }
        }
        else
        {
            mLogger.w("NLUpnpRendererService is NULL!");
        }

        mLogger.w("RenderThread exit!");

        mActivity = null;

        mRendererService = null;

        mMediaControl = null;
    }

    public abstract void running(LocalDevice localDevice);

    // ---------------------------------------------------------------------------------------------------
    // - AvControl
    // ---------------------------------------------------------------------------------------------------
    static class AvControlThread extends RendererThread
    {
        AvControlThread(NLCastVideoPlayerActivity activity, NLUpnpRendererService service, MediaControl mediaControl)
        {
            super(activity, service, mediaControl);
        }

        @Override
        public void running(LocalDevice localDevice)
        {
            LastChangeAwareServiceManager lastChangeAwareServiceManager = null;

            for (LocalService service : localDevice.getServices())
            {
                if (service != null && service.getManager().getImplementation() instanceof RendererAVTransportService)
                {
                    lastChangeAwareServiceManager = (LastChangeAwareServiceManager) service.getManager();

                    break;
                }
            }

            while (mRendererService != null && mActivity != null && !mActivity.isDestroy())
            {
                try
                {
                    Thread.sleep(500);

                    if (lastChangeAwareServiceManager != null)
                    {
                        lastChangeAwareServiceManager.fireLastChange();
                    }
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    static class AudioControlThread extends RendererThread
    {
        AudioControlThread(NLCastVideoPlayerActivity activity, NLUpnpRendererService service, MediaControl mediaControl)
        {
            super(activity, service, mediaControl);
        }

        @Override
        public void running(LocalDevice localDevice)
        {
            LastChangeAwareServiceManager lastChangeAwareServiceManager = null;

            for (LocalService service : localDevice.getServices())
            {
                if (service != null && service.getManager().getImplementation() instanceof RendererAudioControlService)
                {
                    lastChangeAwareServiceManager = (LastChangeAwareServiceManager) service.getManager();

                    break;
                }
            }

            while (mRendererService != null && mActivity != null && !mActivity.isDestroy())
            {
                try
                {
                    Thread.sleep(500);

                    if (lastChangeAwareServiceManager != null)
                    {
                        lastChangeAwareServiceManager.fireLastChange();
                    }
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
