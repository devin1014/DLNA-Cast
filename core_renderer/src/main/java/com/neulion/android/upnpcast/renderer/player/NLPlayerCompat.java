package com.neulion.android.upnpcast.renderer.player;

import android.content.Context;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-08-01
 * Time: 10:45
 */
public class NLPlayerCompat
{
    public static void startPlayer(Context context, String currentURI, String currentURIMetaData)
    {
        if (context != null)
        {
            CastMediaRequest castMediaRequest = new CastMediaRequest(currentURI, currentURIMetaData, null, null);

            if (false) //TODO: show player!!!
            {
                NLCastVideoPlayerActivity.startActivity(context, castMediaRequest);
            }
            else
            {
                NLCastVideoPlayerLeanbackActivity.startVideoActivity(context, castMediaRequest);
            }
        }
    }
}
