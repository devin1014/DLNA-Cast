package com.android.cast.dlna.dmr.player;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.android.cast.dlna.dmr.R;
import com.android.cast.dlna.dmr.player.RendererThread.IActivityAliveCallback;

public class NLCastVideoPlayerLeanbackActivity extends FragmentActivity implements IActivityAliveCallback {
    public static final String EXTRA_KEY_VIDEO = "com.neulion.intent.extra.EXTRA_KEY_VIDEO";
    private NLCastVideoPlayerLeanbackFragment mFragment;
    private boolean mActivityDestroyed = false;

    public static void startVideoActivity(Context context, CastMediaRequest castMediaRequest) {
        Intent intent = new Intent(context, NLCastVideoPlayerLeanbackActivity.class);

        intent.putExtra(EXTRA_KEY_VIDEO, castMediaRequest);

        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivityDestroyed = false;

        setContentView(R.layout.activity_cast_videoplayer_leanback);

        CastMediaRequest castMediaRequest = getIntent().getParcelableExtra(NLCastVideoPlayerLeanbackActivity.EXTRA_KEY_VIDEO);

        mFragment = NLCastVideoPlayerLeanbackFragment.newInstance(castMediaRequest);

        getSupportFragmentManager().beginTransaction().replace(R.id.videoFragment, mFragment).commit();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null) {
            CastMediaRequest castMediaRequest = getIntent().getParcelableExtra(NLCastVideoPlayerLeanbackActivity.EXTRA_KEY_VIDEO);

            if (mFragment != null) {
                mFragment.playVideo(castMediaRequest);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mActivityDestroyed = true;
    }

    @Override
    public boolean isActivityDestroyed() {
        return mActivityDestroyed;
    }
}
