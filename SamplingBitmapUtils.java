package com.lda.imagesample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.InputStream;

/**
 * Created by answer on 2015/7/1 0001.
 */
public class SamplingBitmapUtils {

    public static Bitmap getSamplingBitmapFromStream(InputStream inputStream, int width, int height, int samplingRate){
        BitmapFactory.Options opts = new BitmapFactory.Options();
        int rate = samplingRate;
        if(samplingRate <= 0){
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream,null,opts);
            rate = getSampleSize(opts, width, height);
        }
        opts.inJustDecodeBounds = false;
        opts.inSampleSize = rate;
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream,null,opts);
        return bitmap;
    }

    public static Bitmap getSamplingBitmapFromFile(String filePath, int width, int height, int samplingRate){
        BitmapFactory.Options opts = new BitmapFactory.Options();
        int rate = samplingRate;
        if(samplingRate <= 0){
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath,opts);
            rate = getSampleSize(opts, width, height);
        }
        opts.inJustDecodeBounds = false;
        opts.inSampleSize = rate;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath,opts);
        return bitmap;
    }

    public static Bitmap getSamplingBitmapFromResource(Context context, int imageId, int width, int height, int samplingRate){
        BitmapFactory.Options opts = new BitmapFactory.Options();
        int rate = samplingRate;
        if(samplingRate <= 0){
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(context.getResources(), imageId, opts);
            rate = getSampleSize(opts, width, height);
        }
        opts.inJustDecodeBounds = false;
        opts.inSampleSize = rate;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), imageId, opts);
        return bitmap;
    }

    public static int getSampleSize(BitmapFactory.Options opts, int viewWidth, int viewHeight) {
        int widthSize = opts.outWidth / viewWidth;
        int heightSize = opts.outHeight / viewHeight;
        return widthSize > heightSize ? widthSize : heightSize;
    }
}
