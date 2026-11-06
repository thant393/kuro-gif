package tw.kurocore.kurogram.gifski;

import android.graphics.Bitmap;

class GifskiNative {

    static {
        System.loadLibrary("gifski_jni");
    }

    public static native long nativeNew(int width, int height, int quality, boolean fast, short repeat);

    public static native int nativeSetMotionQuality(long handle, int quality);

    public static native int nativeSetLossyQuality(long handle, int quality);

    public static native int nativeSetExtraEffort(long handle, boolean extra);

    public static native int nativeAddFixedColor(long handle, int r, int g, int b);

    public static native int nativeSetFileOutput(long handle, String path);

    public static native int nativeAddFrameBitmap(long handle, int frameNumber, Bitmap bitmap, double pts);

    public static native int nativeFinish(long handle);
}
