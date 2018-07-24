package com.neulion.android.demo.render;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.neulion.android.demo.render.utils.Loader;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;

import java.util.HashMap;

public class MediaRendererActivity extends Activity implements OnCompletionListener
{

    final private static String TAG = "DefMediaRenderer";
    private static VideoView videoView;
    private static ImageView imgView;
    private MediaController mMedia;
    private static String urlMedia;
    private static boolean sAct = false;
    private static Loader imageLoader;
    private static AudioManager am;
    private static int volume_level = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_renderer);

        sAct = true;
        videoView = (VideoView) findViewById(R.id.myvideoview);
        imgView = (ImageView) findViewById(R.id.imgback);

        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        volume_level = am.getStreamVolume(AudioManager.STREAM_MUSIC);

        urlMedia = RendererApplication.getUrl();

        imageLoader = new Loader(this);

        if (!MediaRendererService.isRunning())
        {
            sendBroadcast(new Intent(MediaRendererService.ACTION_START_RENDER));
        }

        videoView.setOnPreparedListener(new OnPreparedListener()
        {

            @Override
            public void onPrepared(MediaPlayer mediaPlayer)
            {
                if (!getMimeType(urlMedia).equals("video"))
                {
                    videoView.setBackgroundColor(Color.BLACK);
                    imgView.setImageBitmap(downloadBitmap(urlMedia));
                    mMedia.show(1500);
                }
                else
                {
                    imgView.setImageBitmap(null);
                    videoView.setBackgroundResource(0);
                    mMedia.show(1500);
                }
                if (RendererApplication.getPlayMode())
                {
                    play();
                }
            }
        });

        videoView.setOnCompletionListener(this);
        mMedia = new MediaController(this);
        mMedia.setMediaPlayer(videoView);
        mMedia.setAnchorView(videoView);
        videoView.setMediaController(mMedia);

        if (sAct)
        {
            if (urlMedia != "")
            {
                setUri(urlMedia);
            }
        }
    }

    public static boolean isPlaying()
    {
        return videoView.isPlaying();
    }

    public static void seekTo(int msec)
    {
        videoView.seekTo(msec);
    }

    public static void pause()
    {

        videoView.pause();

    }

    public static void play()
    {

        if (getMimeType(urlMedia).equals("image"))
        {
            Log.d(TAG, "true");
            playImage();
        }
        else
        {
            Log.d(TAG, "false");
            videoView.requestFocus();
            videoView.start();
        }

        RendererApplication.setPlayMode(false);
    }

    public static void stop()
    {

        videoView.stopPlayback();

    }

    public static int curentTime()
    {

        return videoView.getCurrentPosition();

    }

    public static long tackTime()
    {

        return videoView.getDuration();

    }

    public static void setUri(final String url)
    {
        try
        {
            if (!getMimeType(urlMedia).equals("image"))
            {
                Uri uriMedia = Uri.parse(url);
                videoView.setVideoURI(uriMedia);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static boolean getStatusAct()
    {
        return sAct;
    }

    public void endOfMedia()
    {
        this.finish();
    }

    public static void playImage()
    {
        videoView.setBackgroundColor(Color.BLACK);
        imageLoader.DisplayImage(urlMedia, imgView);
    }

    @Override
    public void onDestroy()
    {
        sAct = false;
        super.onDestroy();
    }

    @Override
    protected void onResume()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(MediaRendererService.ACTION_SETURI);
        registerReceiver(mFsActionsReceiver, filter);
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        Log.v(TAG, "onPause: Unregistering the Server actions");
        unregisterReceiver(mFsActionsReceiver);

    }

    @Override
    public void onCompletion(MediaPlayer mp)
    {
        MediaRendererService.getMediaPlayers().get(new UnsignedIntegerFourBytes(0)).complete();
    }

    private Bitmap downloadBitmap(String url)
    {
        final MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        metaRetriever.setDataSource(url, new HashMap<String, String>());
        try
        {
            final byte[] art = metaRetriever.getEmbeddedPicture();
            return BitmapFactory.decodeByteArray(art, 0, art.length);
        }
        catch (Exception e)
        {
            Log.d(TAG, "Couldn't create album art: " + e.getMessage());
            return BitmapFactory.decodeResource(getResources(), R.drawable.nocover_audio);
        }
    }

    private static String getMimeType(String url)
    {
        try
        {
            String extension = url.substring(url.lastIndexOf("."));
            String mimeTypeMap = MimeTypeMap.getFileExtensionFromUrl(extension);
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(mimeTypeMap);
            return mimeType.split("/")[0];
        }
        catch (Exception e)
        {
            Log.d(TAG, "Couldn't get MimeType: " + e.getMessage());
            return "video";
        }

    }

    public static int getVolume()
    {
        volume_level = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        Log.d(TAG, "getVolume: " + volume_level);
        return (int) ((volume_level * 100) / am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));

    }

    public static void setVolume(float leftVolume)
    {
        am.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (leftVolume * am.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 100), 0);
        Log.d(TAG, "setVolume: " + leftVolume);
    }

    BroadcastReceiver mFsActionsReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.v(TAG, "action received: " + intent.getAction());
            if (intent.getAction().equals(MediaRendererService.ACTION_SETURI))
            {
                urlMedia = RendererApplication.getUrl();
                setUri(urlMedia);
            }

        }
    };
}
