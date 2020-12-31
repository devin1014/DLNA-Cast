package com.android.cast.dlna.control;

import androidx.annotation.NonNull;

interface ICastInfoListener<T> {
    void onChanged(@NonNull T t);
}
