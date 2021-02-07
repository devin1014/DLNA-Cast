package com.android.cast.dlna.dmr.service;


import android.content.Context;
import android.text.TextUtils;

import com.android.cast.dlna.core.Utils;
import com.android.cast.dlna.dmr.CastUtils;
import com.android.cast.dlna.dmr.ILogger;
import com.android.cast.dlna.dmr.player.ICastMediaControl;
import com.android.cast.dlna.dmr.player.PlayerCompat;

import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.AVTransportErrorCode;
import org.fourthline.cling.support.avtransport.AVTransportException;
import org.fourthline.cling.support.model.DeviceCapabilities;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PlayMode;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.SeekMode;
import org.fourthline.cling.support.model.StorageMedium;
import org.fourthline.cling.support.model.TransportAction;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.TransportSettings;

import java.net.URI;

public class AVTransportController implements IRendererInterface.IAVTransport {
    private final TransportAction[] TRANSPORT_ACTION_STOPPED = new TransportAction[]{TransportAction.Play};
    private final TransportAction[] TRANSPORT_ACTION_PLAYING = new TransportAction[]{TransportAction.Stop, TransportAction.Pause, TransportAction.Seek};
    private final TransportAction[] TRANSPORT_ACTION_PAUSE_PLAYBACK = new TransportAction[]{TransportAction.Stop, TransportAction.Pause, TransportAction.Seek, TransportAction.Play};
    private final UnsignedIntegerFourBytes mInstanceId;
    private final ILogger mLogger = new ILogger.DefaultLoggerImpl(this);
    private PositionInfo mPositionInfo = new PositionInfo();
    private MediaInfo mMediaInfo = new MediaInfo();
    private volatile TransportInfo mTransportInfo = new TransportInfo();
    private final TransportSettings mTransportSettings = new TransportSettings();
    private final Context mApplicationContext;
    private final ICastMediaControl mControlListener;
    private int mCountIndex = 0;

    public AVTransportController(Context context, UnsignedIntegerFourBytes instanceId, ICastMediaControl listener) {
        mApplicationContext = context.getApplicationContext();
        mInstanceId = instanceId;
        mControlListener = listener;
    }

    public UnsignedIntegerFourBytes getInstanceId() {
        return mInstanceId;
    }

    public synchronized TransportAction[] getCurrentTransportActions() {
        if (mTransportInfo != null) {
            switch (mTransportInfo.getCurrentTransportState()) {
                case STOPPED:
                    return TRANSPORT_ACTION_STOPPED;
                case PLAYING:
                    return TRANSPORT_ACTION_PLAYING;
                case PAUSED_PLAYBACK:
                    return TRANSPORT_ACTION_PAUSE_PLAYBACK;
            }
        }

        return null;
    }

    @Override
    public DeviceCapabilities getDeviceCapabilities() {
        return new DeviceCapabilities(new StorageMedium[]{StorageMedium.NETWORK});
    }

    @Override
    public MediaInfo getMediaInfo() {
        mLogger.d(String.format("getMediaInfo: currentURI=[%s]", mMediaInfo.getCurrentURI()));
        mLogger.d(String.format("getMediaInfo: currentURIMetaData=[%s]", mMediaInfo.getCurrentURIMetaData()));
        mLogger.d(String.format("getMediaInfo: mediaDuration=[%s]", mMediaInfo.getMediaDuration()));
        mLogger.d(String.format("getMediaInfo: nextURI=[%s]", mMediaInfo.getNextURI()));
        mLogger.d(String.format("getMediaInfo: nextURIMetaData=[%s]", mMediaInfo.getNextURIMetaData()));

        return mMediaInfo;
    }

    @Override
    public PositionInfo getPositionInfo() {
        if (mCountIndex % 10 == 0) {
            mCountIndex = 0;
            mLogger.d(String.format("getPositionInfo: %s", mPositionInfo));
        }
        mCountIndex++;
        return mPositionInfo;
    }

    @Override
    public TransportInfo getTransportInfo() {
        mLogger.d(String.format("getTransportInfo: [%s][%s]", mTransportInfo.getCurrentTransportStatus(), mTransportInfo.getCurrentTransportState()));
        return mTransportInfo;
    }

    @Override
    public TransportSettings getTransportSettings() {
        mLogger.d(String.format("getTransportSettings: [%s][%s]", mTransportSettings.getPlayMode(), mTransportSettings.getRecQualityMode()));
        return mTransportSettings;
    }

    @Override
    public void setAVTransportURI(String currentURI, String currentURIMetaData) throws AVTransportException {
        mLogger.d(String.format("setAVTransportURI:[%s]", currentURI));
        mLogger.d(String.format("setAVTransportURI:[%s]", currentURIMetaData));
        // check currentURI
        try {
            new URI(currentURI);
        } catch (Exception ex) {
            throw new AVTransportException(ErrorCode.INVALID_ARGS, "CurrentURI can not be null or malformed");
        }
        //        if (currentURI.startsWith("http:"))
        //        {
        //            try
        //            {
        //                HttpFetch.validate(URIUtil.toURL(uri));
        //            }
        //            catch (Exception ex)
        //            {
        //                throw new AVTransportException(AVTransportErrorCode.RESOURCE_NOT_FOUND, ex.getMessage());
        //            }
        //        }
        //        else if (!currentURI.startsWith("file:"))
        //        {
        //            throw new AVTransportException(ErrorCode.INVALID_ARGS, "Only HTTP and file: resource identifiers are supported");
        //        }
        mMediaInfo = new MediaInfo(currentURI, currentURIMetaData, getInstanceId(), "", StorageMedium.NETWORK);
        mPositionInfo = new PositionInfo(1, currentURIMetaData, currentURI);
        PlayerCompat.startPlayer(mApplicationContext, currentURI, currentURIMetaData);
    }

    @Override
    public void setNextAVTransportURI(String nextURI, String nextURIMetaData) {
        mLogger.d(String.format("setNextAVTransportURI:[%s]", nextURI));
        mLogger.d(String.format("setNextAVTransportURI:[%s]", nextURIMetaData));
    }

    @Override
    public void play(String speed) {
        mLogger.d("play: " + speed);
        mControlListener.play();
    }

    public void pause() {
        mLogger.d("pause");
        mControlListener.pause();
    }

    @Override
    public void seek(String unit, String target) throws AVTransportException {
        mLogger.d(String.format("seek [%s][%s]", unit, target));
        SeekMode seekMode = SeekMode.valueOrExceptionOf(unit);
        if (!seekMode.equals(SeekMode.REL_TIME)) {
            throw new AVTransportException(AVTransportErrorCode.SEEKMODE_NOT_SUPPORTED, "Unsupported seek mode: " + unit);
        }
        long position = Utils.getIntTime(target);
        mControlListener.seek(position);
    }

    synchronized public void stop() {
        mLogger.d("stop");
        mControlListener.stop();
    }

    @Override
    public void previous() {
        mLogger.d("previous");
    }

    @Override
    public void next() {
        mLogger.d("next");
    }

    @Override
    public void record() {
        mLogger.d("record");
    }

    @Override
    public void setPlayMode(String newPlayMode) {
        mLogger.d(String.format("setPlayMode:[%s]", newPlayMode));
        if (!newPlayMode.equalsIgnoreCase(PlayMode.NORMAL.name())) {
            throw new IllegalArgumentException("Only accept 'NORMAL' playMode!!!");
        }
    }

    @Override
    public void setRecordQualityMode(String newRecordQualityMode) {
        mLogger.d(String.format("setRecordQualityMode:[%s]", newRecordQualityMode));
    }

    // ----------------------------------------------------------------------------------------------------------------
    // - Update
    // ----------------------------------------------------------------------------------------------------------------
    @Override
    public void updateMediaCurrentPosition(long position) {
        mPositionInfo.setRelTime(Utils.getStringTime(position));
    }

    @Override
    public void updateMediaDuration(long duration) {
        if (TextUtils.isEmpty(mMediaInfo.getMediaDuration())) {
            mMediaInfo = new MediaInfo(
                    mMediaInfo.getCurrentURI(),
                    mMediaInfo.getCurrentURIMetaData(),
                    getInstanceId(),
                    Utils.getStringTime(duration),
                    StorageMedium.NETWORK);
        }

        mPositionInfo.setTrackDuration(Utils.getStringTime(duration));
    }

    @Override
    public void updateMediaState(int state) {
        final TransportInfo oldInfo = mTransportInfo;

        mTransportInfo = CastUtils.getTransportInfo(state);

        mLogger.d(String.format("transportStateChanged:[%s]->[%s]", oldInfo.getCurrentTransportState(), mTransportInfo.getCurrentTransportState()));

    }
}
