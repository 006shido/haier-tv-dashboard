package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import java.io.*;

/** Imports a bounded local image so the wallpaper survives USB removal. */
final class WallpaperImage {
    static File file(Context c){return new File(c.getFilesDir(),"personal-wallpaper.jpg");}
    static void save(Context c,Uri uri) throws IOException {
        File source=File.createTempFile("wallpaper-source-",".tmp",c.getCacheDir());
        File output=File.createTempFile("wallpaper-ready-",".jpg",c.getFilesDir());
        Bitmap bitmap=null;
        try{
            try(InputStream input=c.getContentResolver().openInputStream(uri);OutputStream dest=new FileOutputStream(source)){
                if(input==null)throw new IOException("No image");
                byte[] buffer=new byte[16384];int n,total=0;
                while((n=input.read(buffer))!=-1){total+=n;if(total>32*1024*1024)throw new IOException("Image exceeds 32 MB");dest.write(buffer,0,n);}
            }
            BitmapFactory.Options options=new BitmapFactory.Options();options.inJustDecodeBounds=true;
            BitmapFactory.decodeFile(source.getPath(),options);
            if(options.outWidth<=0||options.outHeight<=0||options.outWidth>32768||options.outHeight>32768)throw new IOException("Unsupported image");
            options.inSampleSize=1;
            while(options.outWidth/options.inSampleSize>1920||options.outHeight/options.inSampleSize>1080)options.inSampleSize*=2;
            options.inJustDecodeBounds=false;
            bitmap=BitmapFactory.decodeFile(source.getPath(),options);
            if(bitmap==null)throw new IOException("Unable to decode image");
            try(OutputStream dest=new FileOutputStream(output)){
                if(!bitmap.compress(Bitmap.CompressFormat.JPEG,92,dest))throw new IOException("Unable to save image");
            }
            if(!output.renameTo(file(c)))throw new IOException("Unable to replace wallpaper");
        }finally{if(bitmap!=null)bitmap.recycle();source.delete();output.delete();}
    }
}
