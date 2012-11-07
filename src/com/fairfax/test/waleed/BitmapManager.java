package com.fairfax.test.waleed;

import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

/**
 * Class responsible for managing bitmaps. It maintains an in-memory cache of loaded images
 * as well as a 'default' image (placeholder).
 * 
 * A maximum of five images can be downloaded at a time.
 */
public class BitmapManager {
	private static final int MAX_CONCURRENT_IMAGE_DL = 5;
	private final Map<String, Bitmap> cache;
	private final ExecutorService pool;
	private Map<ImageView, String> imageViews = 
			Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
	private int maxWidth = 128;
	private int maxHeight = 128;
	private static BitmapManager INSTANCE = null;

	public static synchronized BitmapManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new BitmapManager();
		}
		return INSTANCE;
	}
	
	public static synchronized void setInstance(BitmapManager instance) {
		INSTANCE = instance;
	}
	
	private BitmapManager() {
		// create an LRU cache by setting the last argument in constructor to true
		cache = new LinkedHashMap<String,Bitmap>(10, 0.75f, true);
		pool = Executors.newFixedThreadPool(MAX_CONCURRENT_IMAGE_DL);
	}

	public Bitmap getBitmapFromCache(String url) {
		if (cache.containsKey(url)) {
			return cache.get(url);
		}

		return null;
	}

	public void queueJob(final String url, final ImageView imageView,
			final int width, final int height) {
		/* Create handler in UI thread. */
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				String tag = imageViews.get(imageView);
				if (tag != null && tag.equals(url)) {
					if (msg.obj != null) {
						imageView.setImageBitmap((Bitmap) msg.obj);
						Log.i(null, "Image downloaded: " + url);
					} else {
						imageView.setImageDrawable(null);
						Log.i(null, "Using placeholder because failed to download image: " + url);
					}
				}
			}
		};

		pool.submit(new Runnable() {
			@Override
			public void run() {
				final Bitmap bmp = downloadBitmap(url, width, height);
				Message message = Message.obtain();
				message.obj = bmp;
				handler.sendMessage(message);
			}
		});
	}

	public void loadBitmap(final String url, final ImageView imageView,
			final int width, final int height) {
		// if no url given then don't attempt to load the image, the default placeholder will be used
		if (url == null) {
			imageView.setImageDrawable(null);
			return;
		}
		imageViews.put(imageView, url);
		Bitmap bitmap = getBitmapFromCache(url);

		if (bitmap != null) {
			Log.d(null, "Item loaded from cache: " + url);
			imageView.setImageBitmap(bitmap);
		} else {
			imageView.setImageDrawable(null);
			queueJob(url, imageView, width, height);
		}
	}

	private Bitmap downloadBitmap(String url, int width, int height) {
		try {
			Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(
					url).getContent());
			// the bitmap size must be limited in order to ensure consistency in the display
			bitmap = resizeIfNecessary(bitmap);
			cache.put(url, bitmap);
			return bitmap;
		} catch (Exception e) {
			Log.d(getClass().getSimpleName(), "failed to download url: " + url, e);
		}
		return null;
	}
	
	private Bitmap resizeIfNecessary(Bitmap bitmap) {
		int height = (bitmap.getHeight() < maxHeight) ? bitmap.getHeight() : maxHeight;
		int width = (bitmap.getWidth() < maxWidth) ? bitmap.getWidth() : maxWidth;
		
		return Bitmap.createScaledBitmap(bitmap, width, height, true);
	}
}
