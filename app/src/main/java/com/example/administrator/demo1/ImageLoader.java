package com.example.administrator.demo1;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import android.widget.ImageView;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2018/5/8.
 */

public class ImageLoader {
    //图片缓存
    ImageCache mImageCache = new ImageCache();
    //线程池 线程数量为CPu的数量
    ExecutorService mExecutorservice = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());


    public void displayImage(final String url, final ImageView imageView) {
        imageView.setTag(url);
        mExecutorservice.submit(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = downLoadImageUrl(url);
                if (bitmap == null) {
                    throw new NullPointerException("图片为空");
                }
                if (imageView.getTag().equals(url)) {
                    imageView.setImageBitmap(bitmap);
                }

                mImageCache.put(url, bitmap);
            }
        });
    }

    private Bitmap downLoadImageUrl(String imageUrl) {
        Bitmap bitmap = null;
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            bitmap = BitmapFactory.decodeStream(urlConnection.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
