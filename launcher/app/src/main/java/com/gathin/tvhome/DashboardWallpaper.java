package com.gathin.tvhome;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Decode only the selected background, off the UI thread, with a bounded bitmap size. */
final class DashboardWallpaper {
    private static final ExecutorService DECODER = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "DashboardWallpaper");
        thread.setPriority(Thread.MIN_PRIORITY);
        return thread;
    });
    private Future<?> pending;
    private boolean closed;

    void load(Activity activity, ImageView image, boolean custom) {
        WeakReference<Activity> owner = new WeakReference<>(activity);
        WeakReference<ImageView> target = new WeakReference<>(image);
        android.content.Context context = activity.getApplicationContext();
        pending = DECODER.submit(() -> {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            String path = WallpaperImage.file(context).getPath();
            if (custom) BitmapFactory.decodeFile(path, options);
            else BitmapFactory.decodeResource(context.getResources(), R.drawable.dashboard_wallpaper, options);
            options.inSampleSize = 1;
            while (options.outWidth / options.inSampleSize > 1920
                    || options.outHeight / options.inSampleSize > 1080) options.inSampleSize *= 2;
            options.inJustDecodeBounds = false;
            Bitmap bitmap = custom ? BitmapFactory.decodeFile(path, options)
                    : BitmapFactory.decodeResource(context.getResources(), R.drawable.dashboard_wallpaper, options);
            if (Thread.currentThread().isInterrupted()) {
                if (bitmap != null) bitmap.recycle();
                return;
            }
            Activity host = owner.get();
            if (host == null) { if (bitmap != null) bitmap.recycle(); return; }
            host.runOnUiThread(() -> {
                Activity current = owner.get();
                ImageView view = target.get();
                if (!closed && current != null && !current.isDestroyed() && view != null) view.setImageBitmap(bitmap);
                else if (bitmap != null) bitmap.recycle();
            });
        });
    }

    void close() { closed = true; if (pending != null) pending.cancel(true); }
}
