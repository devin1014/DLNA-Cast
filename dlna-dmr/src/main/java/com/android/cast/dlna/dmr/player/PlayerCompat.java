package com.android.cast.dlna.dmr.player;

import android.content.Context;

/**
 *
 */
public class PlayerCompat {
    public static void startPlayer(Context context, String currentURI, String currentURIMetaData) {
        if (context != null) {
            CastMediaRequest castMediaRequest = new CastMediaRequest(currentURI, currentURIMetaData, null, null);
            DLNARendererActivity.startActivity(context, castMediaRequest);
            // if (false) //TODO: show player!!!
            // {
            //     DLNARendererActivity.startActivity(context, castMediaRequest);
            // } else {
            //     CastVideoPlayerLeanbackActivity.startVideoActivity(context, castMediaRequest);
            // }
        }
    }
}
