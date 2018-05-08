package com.example.administrator.demo1;

import android.graphics.Bitmap;
import android.util.LruCache;

import java.util.BitSet;

/**
 * @description: ImageCache
 * @author: lijingya
 * @email: ljy@91118.com
 * @createDate: 2018/5/8 19:45
 * @company: 杭州天音
 */
public class ImageCache {
    //图片缓存
    LruCache<String, Bitmap> mImageCache;

    public ImageCache() {
        initImageCache();
    }

    private void initImageCache() {
        //计算可使用的最大内存
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 4;
        mImageCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight() / 1024;
            }
        };
    }

    public void put(String url, Bitmap bitmap) {
        mImageCache.put(url, bitmap);
    }

    public void get(String url) {
        mImageCache.get(url);
    }
}