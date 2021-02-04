package com.android.cast.dlna.dms.player;

import android.content.Context;

/**
 *
 */
public class NLPlayerCompat {
    public static void startPlayer(Context context, String currentURI, String currentURIMetaData) {
        if (context != null) {
            CastMediaRequest castMediaRequest = new CastMediaRequest(currentURI, currentURIMetaData, null, null);
            if (false) //TODO: show player!!!
            {
                NLCastVideoPlayerActivity.startActivity(context, castMediaRequest);
            } else {
                NLCastVideoPlayerLeanbackActivity.startVideoActivity(context, castMediaRequest);
            }
        }
    }
}
