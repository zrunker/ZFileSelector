package cc.ibooker.zphotochoose.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * 本地图片加载器 - 也可以使用Glide等框架
 *
 * @author 邹峰立
 */
public class PhotoLoader {

    // 内存缓存
    private LruCache<String, Bitmap> mLruCache;

    // 线程池
    private ExecutorService mThreadPool;

    // 队列的调度方法
    private Type mType = Type.LIFO;
    // 任务队列
    private LinkedList<Runnable> mTaskQueue;
    private Semaphore mTaskSemaphore;
    private Handler mPoolHandler;
    // 信号量
    private Semaphore mPoolHandlerSemaphore = new Semaphore(0);

    // 更新UI线程Handler
    private Handler mUIHandler;

    public enum Type {
        FIFO, LIFO
    }

    // 构造方法
    private PhotoLoader(int threadCount, Type type) {
        init(threadCount, type);
    }

    private static PhotoLoader photoLoader;

    public static PhotoLoader getInstance(int threadCount, Type type) {
        if (photoLoader == null) {
            synchronized (PhotoLoader.class) {
                if (photoLoader == null)
                    photoLoader = new PhotoLoader(threadCount, type);
            }
        }
        return photoLoader;
    }

    /**
     * 初始化
     *
     * @param threadCount 线程数量
     * @param type        调度策略
     */
    private void init(int threadCount, Type type) {
        // 初始化后台线程
        Thread mPoolThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                mPoolHandler = new MyPoolHandler(PhotoLoader.this);
                // 释放一个信号量
                mPoolHandlerSemaphore.release();
                Looper.loop();
            }
        });
        mPoolThread.start();

        // 初始化LruCache
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheMemory = maxMemory / 8;
        mLruCache = new LruCache<String, Bitmap>(cacheMemory) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };

        // 初始化线程池
        mThreadPool = Executors.newFixedThreadPool(threadCount);
        mTaskQueue = new LinkedList<>();

        mType = type;

        mTaskSemaphore = new Semaphore(threadCount);
    }

    /**
     * 加载图片
     *
     * @param path      图片路径
     * @param imageView 带显示的图片
     */
    public void loadImage(final String path, final ImageView imageView) {
        imageView.setTag(path);
        if (mUIHandler == null) {
            mUIHandler = new MyUiHandler();
        }

        // 根据path从缓存中获取Bitmap
        Bitmap bm = getBitmapFromLruCache(path);
        if (bm != null) {
            reflashBitmap(path, imageView, bm);
        } else {
            addTask(new Runnable() {

                @Override
                public void run() {
                    // 加载图片
                    // 1.获得图片需要显示的大小
                    ImageSize imageSize = getImageViewSize(imageView);
                    // 2.压缩图片
                    Bitmap bm = decodeSampledBitmapFromPath(path, imageSize.width, imageSize.height);
                    // 3.加入缓存
                    addBitmapLruCache(path, bm);
                    reflashBitmap(path, imageView, bm);

                    // 释放一个信号量
                    mTaskSemaphore.release();
                }
            });
        }
    }

    /**
     * 显示图片
     */
    private void reflashBitmap(String path, ImageView imageView, Bitmap bm) {
        Message message = Message.obtain();
        ImageBeanHolder imageBeanHolder = new ImageBeanHolder();
        imageBeanHolder.bitmap = bm;
        imageBeanHolder.imageView = imageView;
        imageBeanHolder.path = path;
        message.obj = imageBeanHolder;
        mUIHandler.sendMessage(message);
    }

    /**
     * 将图片加入缓存
     *
     * @param path 图片路径 - key
     * @param bm   图片内容
     */
    private void addBitmapLruCache(String path, Bitmap bm) {
        if (getBitmapFromLruCache(path) == null)
            if (bm != null)
                mLruCache.put(path, bm);
    }

    /**
     * 压缩图片
     *
     * @param path   图片路径
     * @param width  宽度
     * @param height 高度
     */
    private Bitmap decodeSampledBitmapFromPath(String path, int width, int height) {
        // 获取图片的宽和高，并不把图片加载到内存
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // 再次解析图片
        options.inSampleSize = caculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * 根据图片实际宽和高以及图片需求的宽和高计算SampleSize
     *
     * @param options   图片实际宽和高
     * @param reqWidth  要求宽度
     * @param reqHeight 要求高度
     */
    private int caculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int width = options.outWidth;
        int height = options.outHeight;

        int inSampleSize = 1;
        if (width > reqWidth || height > reqHeight) {
            int widthRadio = Math.round(width * 1.0f / reqWidth);
            int heightRadio = Math.round(height * 1.0f / reqHeight);

            inSampleSize = Math.max(widthRadio, heightRadio);
        }
        return inSampleSize;
    }

    /**
     * 获得图片需要显示的大小
     *
     * @param imageView 待测量的图片
     */
    private ImageSize getImageViewSize(ImageView imageView) {
        ViewGroup.LayoutParams lp = imageView.getLayoutParams();
        DisplayMetrics displayMetrics = imageView.getContext().getResources().getDisplayMetrics();

        int width = imageView.getWidth();// 获取ImageView的实际宽度
        if (width <= 0) {
            width = lp.width;
        }
        if (width <= 0) {
            width = getImageViewFieldValue(imageView, "mMaxWidth");// 检查最大值
        }
        if (width <= 0) {
            width = displayMetrics.widthPixels;// 获取屏幕宽度
        }


        int height = imageView.getHeight();// 获取ImageView的实际高度
        if (height <= 0) {
            height = lp.height;
        }
        if (height <= 0) {
            height = getImageViewFieldValue(imageView, "mMaxHeight");// 检查最大值
        }
        if (height <= 0) {
            height = displayMetrics.heightPixels;// 获取屏幕宽度
        }

        ImageSize imageSize = new ImageSize();
        imageSize.height = height;
        imageSize.width = width;
        return imageSize;
    }

    /**
     * 通过反射获取ImageView属性值
     */
    private static int getImageViewFieldValue(Object obj, String fieldName) {
        int value = 0;
        try {
            Field field = ImageView.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            int fieldValue = field.getInt(obj);
            if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE)
                value = fieldValue;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return value;
    }

    /**
     * 添加任务 下载图片
     */
    private synchronized void addTask(Runnable runnable) {
        mTaskQueue.add(runnable);
        try {
            if (mPoolHandler == null)
                mPoolHandlerSemaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mPoolHandler.sendEmptyMessage(0x111);
    }

    /**
     * 从任务队列取出Runnable
     */
    private Runnable getTask() {
        if (mType == Type.FIFO) {
            return mTaskQueue.removeFirst();
        } else if (mType == Type.LIFO) {
            return mTaskQueue.removeLast();
        }
        return null;
    }

    /**
     * 根据path从缓存中获取Bitmap
     */
    private Bitmap getBitmapFromLruCache(String path) {
        return mLruCache.get(path);
    }

    /**
     * 自定义后台线程Handler
     */
    private static class MyPoolHandler extends Handler {

        private WeakReference<PhotoLoader> mWeakRef;

        MyPoolHandler(PhotoLoader photoLoader) {
            mWeakRef = new WeakReference<>(photoLoader);
        }

        @Override
        public void handleMessage(Message msg) {
            // 从线程池中取出一个任务并执行
            Runnable runnable = mWeakRef.get().getTask();
            if (runnable != null) {
                mWeakRef.get().mThreadPool.execute(runnable);
                try {
                    mWeakRef.get().mTaskSemaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 自定义UI线程Handler
     */
    private static class MyUiHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            // 获取图片，并设置ImageView回调
            if (msg.obj != null) {
                ImageBeanHolder holder = (ImageBeanHolder) msg.obj;
                ImageView imageView = holder.imageView;
                Bitmap bitmap = holder.bitmap;
                String path = holder.path;
                if (path.equals(imageView.getTag().toString())) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    /**
     * 保存图片相关信息Holder
     */
    class ImageBeanHolder {
        ImageView imageView;
        Bitmap bitmap;
        String path;
    }

    /**
     * 保存图片相关信息
     */
    class ImageSize {
        int width;
        int height;
    }

}
