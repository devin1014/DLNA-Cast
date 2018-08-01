package com.neulion.android.upnpcast.renderer.player;

import android.os.Parcel;

public class CastMediaRequest implements android.os.Parcelable
{
    public final String videoURL;
    public final String videoMetadata;
    public final String title;
    public final String subTitle;

    public CastMediaRequest(String videoURL, String videoMetadata, String title, String subTitle)
    {
        this.videoURL = videoURL;
        this.videoMetadata = videoMetadata;
        this.title = title;
        this.subTitle = subTitle;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(videoURL);
        dest.writeString(videoMetadata);
        dest.writeString(title);
        dest.writeString(subTitle);
    }

    @SuppressWarnings("WeakerAccess")
    protected CastMediaRequest(Parcel in)
    {
        videoURL = in.readString();
        videoMetadata = in.readString();
        title = in.readString();
        subTitle = in.readString();
    }

    public static final Creator<CastMediaRequest> CREATOR = new Creator<CastMediaRequest>()
    {
        @Override
        public CastMediaRequest createFromParcel(Parcel source)
        {
            return new CastMediaRequest(source);
        }

        @Override
        public CastMediaRequest[] newArray(int size)
        {
            return new CastMediaRequest[size];
        }
    };
}
