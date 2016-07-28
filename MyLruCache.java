package com.lda.lrucache;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.Toast;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by answer on 2014/7/2 0002.
 */
public class MyLruCache {
    Context context;
    DiskLruCache diskLruCache;
    private static final String TAG = "MyLruCache";
    LruCache<String, Bitmap> lruCache;
    private OnBitmapReadyListener listener;
    private OutputStream os;

    public MyLruCache(Context context) {
        this.context = context;
    }

    public void setOnBitmapReadyListener(OnBitmapReadyListener listener) {
        this.listener = listener;
    }

    public interface OnBitmapReadyListener {
        void bitmapReady(Bitmap bitmap);
    }

    public Bitmap getLruCacheBitmap(String path) {
        if (lruCache == null) {
            initialLruCache();
        }
        if (diskLruCache == null) {
            initialDiskLruCache(10 * 1024 * 1024);
        }
        final String bitmapKey = MD5Utils.MD5Encryption(path, null);
        Bitmap bitmap = lruCache.get(bitmapKey);
        Log.d(TAG, "bitmapKey:" + bitmapKey);
        if (bitmap == null) {
            BitmapAsyncTask task = new BitmapAsyncTask(diskLruCache);
            task.setOnCompletedListener(new BitmapAsyncTask.OnCompletedListener() {
                @Override
                public void onCompleted(Bitmap bit) {
                    lruCache.put(bitmapKey, bit);
                    listener.bitmapReady(bit);
//                    saveBitmapOnDisk(bit,bitmapKey);
                    Log.d(TAG, "onCompleted: 缓存里面没有");
                }
            });
            task.execute(path);
        } else {
            listener.bitmapReady(bitmap);
            Log.d(TAG, "getLrucacheBitmap: 缓存中获取");
        }
        return bitmap;
    }

    private void saveBitmapOnDisk(Bitmap bit, String bitmapKey) {
        OutputStream os = null;
        try {
            DiskLruCache.Editor editor = diskLruCache.edit(bitmapKey);
            if (editor != null) {
                os =  editor.newOutputStream(0);
                bit.compress(Bitmap.CompressFormat.PNG,100,os);
                os.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show();
        }finally {
            if(os != null){
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private int getVersionCode() {
        int versionCode = 1;
        try {
            versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    private File getCacheFile() {
        File file;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())||
                !Environment.isExternalStorageRemovable()) {
            file = context.getExternalCacheDir();
        } else {
            file = context.getCacheDir();
        }
        return file;
    }

    private void initialDiskLruCache(int maxCacheSize) {
        try {
            diskLruCache = DiskLruCache.open(getCacheFile(),
                    getVersionCode(), 1, maxCacheSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initialLruCache() {
        int maxMemory = (int) Runtime.getRuntime().maxMemory() / 1024;
        int cacheSize = maxMemory / 10;
        Log.d(TAG, "cacheSize:" + cacheSize);
        lruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                int sizeOf = value.getByteCount() / 1024;
                Log.d(TAG, "sizeOf:" + sizeOf);
                return sizeOf;
            }
        };
    }
}
