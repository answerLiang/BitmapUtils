package com.lda.lrucache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by answer on 2014/7/2 0002.
 */
public class BitmapAsyncTask extends AsyncTask<String, Void, Bitmap> {

    private static final String TAG = "BitmapAsyncTask";
    private final DiskLruCache diskLruCache;
    private OnCompletedListener listener;

    public BitmapAsyncTask(DiskLruCache diskLruCache) {
        this.diskLruCache = diskLruCache;
    }

    public interface OnCompletedListener {
        void onCompleted(Bitmap bitmap);
    }

    public void setOnCompletedListener(OnCompletedListener listener) {
        this.listener = listener;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        String key = MD5Utils.MD5Encryption(params[0], null);
        Bitmap bitmap = getBitmapFromDisk(key);
        HttpURLConnection conn = null;
        if (bitmap == null) {
            Log.d(TAG, "到网上下载");
            try {
                conn = (HttpURLConnection) new URL(params[0]).openConnection();
                int code = conn.getResponseCode();
                if (code == HttpURLConnection.HTTP_OK) {
                    saveBitmapOnDisk(conn.getInputStream(), key);
                }
                bitmap = getBitmapFromDisk(key);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (null != conn) {
                    conn.disconnect();
                }
            }
        }
        return bitmap;
    }

    public void saveBitmapOnDisk(InputStream is, String key) {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            bis = new BufferedInputStream(is);
//          bitmap = BitmapFactory.decodeStream(bis);不能这样写，这样的话bis的数据会被bitmap拿走了
            DiskLruCache.Editor editor = diskLruCache.edit(key);
            if (editor != null) {
                bos = new BufferedOutputStream(editor.newOutputStream(0));
                byte[] b = new byte[1024];
                int len;
                while ((len = bis.read(b)) != -1) {
                    bos.write(b, 0, len);
                    bos.flush();
                }
                editor.commit();
                Log.d(TAG, "写入磁盘");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Bitmap getBitmapFromDisk(String key) {
        Bitmap bm = null;
        try {
            DiskLruCache.Snapshot snapshot = diskLruCache.get(key);
            if (snapshot != null) {
                InputStream inputStream = snapshot.getInputStream(0);
                bm = BitmapFactory.decodeStream(inputStream);
                Log.d(TAG, "在磁盘中获取图片");
                Log.d(TAG, "getBitmapFromDisk: " + bm);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bm;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (listener != null) {
            listener.onCompleted(bitmap);
        }
    }
}
