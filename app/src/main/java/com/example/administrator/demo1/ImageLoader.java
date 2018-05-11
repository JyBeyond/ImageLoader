package com.example.administrator.demo1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2018/5/8.
 */

public class ImageLoader {
    private static final String TAG = "ImageLoader";
    //图片缓存
    ImageCache mImageCache = new ImageCache();

    //sd卡缓存
    DiskCache mDiskCache = new DiskCache();
    //是否使用sd卡缓存
    boolean isUseDiskCache = false;
    //线程池 线程数量为CPu的数量
    ExecutorService mExecutorservice = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private ImageView imageView;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bitmap bitmap = (Bitmap) msg.obj;
            if (bitmap.equals(""))
                return;
            imageView.setImageBitmap(bitmap);
        }
    };

    public void displayImage(final String url, final ImageView imageView) {
        this.imageView = imageView;
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
                Bitmap bitmap = downLoadImageUrl(url, imageView);
                if (bitmap == null) {
                    throw new NullPointerException("图片为空");
                }
                if (imageView.getTag().equals(url)) {
                    Message message = new Message();
                    message.what = 1;
                    message.obj = bitmap;
                    handler.sendMessage(message);
                }
                mDiskCache.put(url, bitmap);
                mImageCache.put(url, bitmap);
            }
        });
    }

    private Bitmap downLoadImageUrl(String imageUrl, ImageView imageView) {
        Bitmap bitmap = null;
        InputStream is = null;
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(3000);
            urlConnection.connect();
            int code = urlConnection.getResponseCode();
//            Log.d("-->>54", code + "");
            is = new BufferedInputStream(urlConnection.getInputStream());
            Log.d("-->>90", is.available() + "");
            is.mark(is.);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);
            Log.d("-->>90", imageView.getWidth() + ":" + imageView.getHeight());
            options.inSampleSize = caculateSampleSize(options, imageView.getWidth(), imageView.getHeight());
            options.inJustDecodeBounds = false;
            is.reset();
            bitmap = BitmapFactory.decodeStream(is, null, options);
//            byte[] bytes = getByteInputStream(urlConnection.getInputStream());
//            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }

    public void setUseDiskCache(boolean useDiskCache) {
        isUseDiskCache = useDiskCache;
    }

    public byte[] getByteInputStream(InputStream in) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            byte[] buff = new byte[512];
            int len;
            while ((len = in.read(buff)) != -1) {
                byteArrayOutputStream.write(buff, 0, len);
            }
            in.close();
            byteArrayOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArrayOutputStream.toByteArray();
    }


    public int caculateSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int width = options.outWidth;
        int height = options.outHeight;
        int sampleSize = 1;
        if (width > reqWidth || height > reqHeight) {
            int widthRadio = Math.round(width * 1.0f / reqWidth);
            int heightRadio = Math.round(height * 1.0f / reqHeight);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            sampleSize = heightRadio < widthRadio ? heightRadio : widthRadio;
        }
        return sampleSize;
    }

}
