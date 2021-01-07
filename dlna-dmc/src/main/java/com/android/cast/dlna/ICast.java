package com.android.cast.dlna;

import androidx.annotation.NonNull;

public interface ICast {

    @NonNull
    String getId();

    @NonNull
    String getUri();

    String getName();

    interface ICastVideo extends ICast {

        /**
         * @return video duration, ms
         */
        long getDurationMillSeconds();

        long getSize();

        long getBitrate();
    }
}
