package com.neulion.android.demo.render.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;


import com.neulion.android.demo.render.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Loader
{
    private static final String TAG = "Loader";

    FileCache fileCache;
    ExecutorService executorService;
    final int stub_id = R.drawable.nocover_audio;

    public Loader(Context context)
    {
        fileCache = new FileCache(context);
        executorService = Executors.newFixedThreadPool(5);
    }

    public void DisplayImage(String url, ImageView imageView)
    {
        queuePhoto(url, imageView);
        // imageView.setImageResource(stub_id);

    }

    private void queuePhoto(String url, ImageView imageView)
    {
        PhotoToLoad p = new PhotoToLoad(url, imageView);
        executorService.submit(new PhotosLoader(p));
    }

    private Bitmap getBitmap(String url)
    {
        File f = fileCache.getFile(url);

        // from SD cache
        Bitmap b = decodeFile(f);
        if (b != null)
        {
            return b;
        }

        // from web
        try
        {
            Bitmap bitmap = null;
            URL imageUrl = new URL(url);

            HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
            conn.setInstanceFollowRedirects(false);
            InputStream is = conn.getInputStream();
            OutputStream os = new FileOutputStream(f);
            Utils.CopyStream(is, os);
            os.close();
            bitmap = decodeFile(f);
            return bitmap;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
    }

    // decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File f)
    {
        try
        {
            // decode image
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inSampleSize = 2;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o);

        }
        catch (FileNotFoundException e)
        {
        }
        return null;
    }

    // Task for the queue
    private class PhotoToLoad
    {
        public String url;
        public ImageView imageView;

        public PhotoToLoad(String u, ImageView i)
        {
            url = u;
            imageView = i;

        }
    }

    class PhotosLoader implements Runnable
    {
        PhotoToLoad photoToLoad;

        PhotosLoader(PhotoToLoad photoToLoad)
        {
            this.photoToLoad = photoToLoad;
        }

        @Override
        public void run()
        {
            Bitmap bmp = getBitmap(photoToLoad.url);
            BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
            Activity a = (Activity) photoToLoad.imageView.getContext();
            a.runOnUiThread(bd);
        }
    }

    // Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable
    {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;

        public BitmapDisplayer(Bitmap b, PhotoToLoad p)
        {
            bitmap = b;
            photoToLoad = p;
        }

        public void run()
        {

            if (bitmap != null)
            {
                photoToLoad.imageView.setImageBitmap(bitmap);
            }
            else
            {
                photoToLoad.imageView.setImageResource(stub_id);
            }

        }
    }

    public void clearCache()
    {
        // memoryCache.clear();
        // fileCache.clear();
    }

}
