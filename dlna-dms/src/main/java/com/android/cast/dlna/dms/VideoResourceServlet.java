/*
 * Copyright (C) 2014 Kevin Shen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.cast.dlna.dms;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.util.resource.FileResource;
import org.eclipse.jetty.util.resource.Resource;

import java.io.File;

public class VideoResourceServlet extends DefaultServlet {

    private final Context mApplicationContext;

    public VideoResourceServlet(Context context) {
        mApplicationContext = context.getApplicationContext();
    }

    @Override
    public Resource getResource(String pathInContext) {
        Resource resource = null;
        Log.i(VideoResourceServlet.class.getSimpleName(), "Path:" + pathInContext);
        try {
            String id = Utils.parseResourceId(pathInContext);
            Log.i(VideoResourceServlet.class.getSimpleName(), "Id:" + id);

            Uri uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, Long.parseLong(id));
            Cursor cursor = mApplicationContext.getContentResolver().query(
                    uri, null, null, null, null);
            cursor.moveToFirst();
            String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
            File file = new File(path);
            if (file.exists()) {
                resource = FileResource.newResource(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resource;
    }
}
