package com.neulion.android.upnpcast.renderer.player;

import com.neulion.android.upnpcast.renderer.NLUpnpRendererService;
import com.neulion.android.upnpcast.renderer.localservice.RendererAVTransportService;
import com.neulion.android.upnpcast.renderer.localservice.RendererAudioControlService;
import com.neulion.android.upnpcast.renderer.utils.ILogger;
import com.neulion.android.upnpcast.renderer.utils.ILogger.DefaultLoggerImpl;

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
    public interface IActivityAliveCallback
    {
        boolean isActivityDestroyed();
    }

    private ILogger mLogger = new DefaultLoggerImpl(this);

    NLUpnpRendererService mRendererService;

    private IActivityAliveCallback mCallback;

    RendererThread(IActivityAliveCallback callback, NLUpnpRendererService service)
    {
        mCallback = callback;

        mRendererService = service;
    }

    @Override
    public void run()
    {
        mLogger.i(String.format("[%s] running!!!", getClass().getSimpleName()));

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

        mLogger.i(String.format("[%s] exit!!!", getClass().getSimpleName()));

        mCallback = null;

        mRendererService = null;
    }

    public abstract void running(LocalDevice localDevice);

    boolean isActivityAlive()
    {
        return mCallback != null && !mCallback.isActivityDestroyed();
    }


    // ---------------------------------------------------------------------------------------------------
    // - AvControl
    // ---------------------------------------------------------------------------------------------------
    static class AvControlThread extends RendererThread
    {
        AvControlThread(IActivityAliveCallback activity, NLUpnpRendererService service)
        {
            super(activity, service);
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

            while (mRendererService != null && isActivityAlive())
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
        AudioControlThread(IActivityAliveCallback activity, NLUpnpRendererService service)
        {
            super(activity, service);
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

            while (mRendererService != null && isActivityAlive())
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
