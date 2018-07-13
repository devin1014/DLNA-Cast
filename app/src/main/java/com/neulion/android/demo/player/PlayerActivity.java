package com.neulion.android.demo.player;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.neulion.android.demo.upnpcast.MainActivity;
import com.neulion.android.demo.upnpcast.R;
import com.neulion.media.control.MediaRequest;
import com.neulion.media.control.impl.CommonVideoController;

public class PlayerActivity extends AppCompatActivity
{
    private CommonVideoController mController;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video_player);

        initComponent();

        openMedia(MainActivity.CAST_URL);
    }

    private void initComponent()
    {
        mController = findViewById(R.id.d_video_controller);

        mController.setFullScreen(false);

        mController.setSupportFullScreenControls(true);

        mController.removeSupportedGestures(CommonVideoController.GESTURE_SCROLL_VERTICAL_VOLUME);

        mController.setMediaConnection(new NLCastMediaConnection(this));

        //((CommonControlBar) findViewById(R.id.m_top_bar_panel)).setSupported(false);

        //NLCast.getManager().addMediaRouterButton(this, (ViewGroup) mController.findViewById(R.id.m_controller_fit_system_windows_panel));
    }

    private void openMedia(String url)
    {
        if (mController != null)
        {
            final MediaRequest request = new MediaRequest(url);

            //        if (castProvider != null)
            //        {
            //            request.putParam(NLCastProvider.KEY, castProvider);
            //        }

            // open media.
            mController.openMedia(request);
        }
    }

    @Override
    public void onDestroy()
    {
        mController.releaseMedia();

        mController.setMediaConnection(null);

        super.onDestroy();
    }

}
