package com.android.cast.dlna.dms;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import org.fourthline.cling.support.model.PersonWithRole;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.item.ImageItem;
import org.fourthline.cling.support.model.item.Item;
import org.fourthline.cling.support.model.item.Movie;
import org.fourthline.cling.support.model.item.MusicTrack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

interface IMediaContentDao {
    @NonNull
    List<Item> getImageItems(@NonNull Context context);

    @NonNull
    List<Item> getAudioItems(@NonNull Context context);

    @NonNull
    List<Item> getVideoItems(@NonNull Context context);

    // -------------------------------------------------------
    // ---- Implement
    // -------------------------------------------------------
    final class MediaContentDao implements IMediaContentDao {

        private static final String[] CONTENT_IMAGE_COLUMNS = {MediaStore.MediaColumns._ID, MediaStore.MediaColumns.TITLE, MediaStore.MediaColumns.ARTIST,
                MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.MIME_TYPE, MediaStore.MediaColumns.SIZE};
        private static final String[] CONTENT_AUDIO_COLUMNS = {MediaStore.MediaColumns._ID, MediaStore.MediaColumns.TITLE, MediaStore.MediaColumns.ARTIST,
                MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.MIME_TYPE, MediaStore.MediaColumns.SIZE, MediaStore.MediaColumns.ALBUM};
        private static final String[] CONTENT_VIDEO_COLUMNS = {MediaStore.MediaColumns._ID, MediaStore.MediaColumns.TITLE, MediaStore.MediaColumns.ARTIST,
                MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.MIME_TYPE, MediaStore.MediaColumns.SIZE, MediaStore.MediaColumns.RESOLUTION};

        private final String mBaseUrl;

        public MediaContentDao(String baseUrl) {
            mBaseUrl = baseUrl;
        }

        @Override
        @NonNull
        public List<Item> getImageItems(@NonNull Context context) {
            List<Item> items = new ArrayList<>();
            try (Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, CONTENT_IMAGE_COLUMNS, null, null, null)) {
                if (cursor == null) return items;
                cursor.moveToFirst();
                while (cursor.moveToNext()) {
                    String id = String.valueOf(cursor.getInt(cursor.getColumnIndex(CONTENT_IMAGE_COLUMNS[0])));
                    String title = cursor.getString(cursor.getColumnIndex(CONTENT_IMAGE_COLUMNS[1]));
                    String creator = cursor.getString(cursor.getColumnIndex(CONTENT_IMAGE_COLUMNS[2]));
                    String data = cursor.getString(cursor.getColumnIndex(CONTENT_IMAGE_COLUMNS[3]));
                    String mimeType = cursor.getString(cursor.getColumnIndex(CONTENT_IMAGE_COLUMNS[4]));
                    long size = cursor.getLong(cursor.getColumnIndex(CONTENT_IMAGE_COLUMNS[5]));
                    String url = mBaseUrl + File.separator + data;
                    Res res = new Res(mimeType, size, "", null, url);
                    ImageItem imageItem = new ImageItem(id, MediaItem.IMAGE_ID, title, creator, res);
                    items.add(imageItem);
                }
            }
            return items;
        }

        @NonNull
        public List<Item> getAudioItems(@NonNull Context context) {
            List<Item> items = new ArrayList<>();
            try (Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, CONTENT_AUDIO_COLUMNS, null, null, null)) {
                if (cursor == null) return items;
                cursor.moveToFirst();
                while (cursor.moveToNext()) {
                    String id = String.valueOf(cursor.getInt(cursor.getColumnIndex(CONTENT_AUDIO_COLUMNS[0])));
                    String title = cursor.getString(cursor.getColumnIndex(CONTENT_AUDIO_COLUMNS[1]));
                    String creator = cursor.getString(cursor.getColumnIndex(CONTENT_AUDIO_COLUMNS[2]));
                    String data = cursor.getString(cursor.getColumnIndex(CONTENT_AUDIO_COLUMNS[3]));
                    String mimeType = cursor.getString(cursor.getColumnIndex(CONTENT_AUDIO_COLUMNS[4]));
                    long size = cursor.getLong(cursor.getColumnIndex(CONTENT_AUDIO_COLUMNS[5]));
                    // long duration = cursor.getLong(cursor.getColumnIndex(CONTENT_AUDIO_COLUMNS[6]));
                    // String durationStr = ModelUtil.toTimeString(duration / 1000);
                    String album = cursor.getString(cursor.getColumnIndex(CONTENT_AUDIO_COLUMNS[7]));
                    String url = mBaseUrl + File.separator + data;
                    Res res = new Res(mimeType, size, "", null, url);
                    MusicTrack musicTrack = new MusicTrack(id, MediaItem.AUDIO_ID, title, creator, album, new PersonWithRole(creator), res);
                    items.add(musicTrack);
                }
            }

            return items;
        }

        @NonNull
        public List<Item> getVideoItems(@NonNull Context context) {
            List<Item> items = new ArrayList<>();
            try (Cursor cur = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, CONTENT_VIDEO_COLUMNS, null, null, null)) {
                if (cur == null) return items;
                cur.moveToFirst();
                while (cur.moveToNext()) {
                    String id = String.valueOf(cur.getInt(cur.getColumnIndex(CONTENT_VIDEO_COLUMNS[0])));
                    String title = cur.getString(cur.getColumnIndex(CONTENT_VIDEO_COLUMNS[1]));
                    String creator = cur.getString(cur.getColumnIndex(CONTENT_VIDEO_COLUMNS[2]));
                    String data = cur.getString(cur.getColumnIndexOrThrow(CONTENT_VIDEO_COLUMNS[3]));
                    String mimeType = cur.getString(cur.getColumnIndex(CONTENT_VIDEO_COLUMNS[4]));
                    long size = cur.getLong(cur.getColumnIndex(CONTENT_VIDEO_COLUMNS[5]));
                    // long duration = cur.getLong(cur.getColumnIndex(CONTENT_VIDEO_COLUMNS[6]));
                    // String durationStr = ModelUtil.toTimeString(duration / 1000);
                    String resolution = cur.getString(cur.getColumnIndex(CONTENT_VIDEO_COLUMNS[7]));
                    String url = mBaseUrl + File.separator + data;
                    Res res = new Res(mimeType, size, "", null, url);
                    res.setResolution(resolution);
                    Movie movie = new Movie(id, MediaItem.VIDEO_ID, title, creator, res);
                    items.add(movie);
                }
            }
            return items;
        }
    }
}
