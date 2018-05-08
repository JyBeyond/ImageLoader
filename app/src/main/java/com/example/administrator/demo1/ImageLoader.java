package com.example.administrator.demo1;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2018/5/8.
 */

public class ImageLoader {
    //图片缓存
    ImageCache mImageCache = new ImageCache();
    //sd卡缓存
    DiskCache mDiskCache = new DiskCache();
    //是否使用sd卡缓存
    boolean isUseDiskCache = false;
    //线程池 线程数量为CPu的数量
    ExecutorService mExecutorservice = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());


    public void displayImage(final String url, final ImageView imageView) {
        imageView.setTag(url);
        //判断使用哪种缓存
        Bitmap mBitmap = isUseDiskCache ? mDiskCache.get(url) : mImageCache.get(url);
        if (mBitmap != null && imageView.getTag().equals(url)) {
            imageView.setImageBitmap(mBitmap);
            return;
        }
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
                mDiskCache.put(url, bitmap);
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

    public void setUseDiskCache(boolean useDiskCache) {
        isUseDiskCache = useDiskCache;
    }
}
