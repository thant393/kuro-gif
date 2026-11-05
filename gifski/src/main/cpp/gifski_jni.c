#include <jni.h>
#include <string.h>
#include <stdint.h>
#include <android/bitmap.h>

#include "gifski/gifski.h"

#define PTR(h) ((gifski *)(uintptr_t)(h))

JNIEXPORT jlong JNICALL
Java_app_nekogram_gifski_GifskiNative_nativeNew(JNIEnv *env, jclass cls, jint width, jint height,
                                          jint quality, jboolean fast, jshort repeat) {
    GifskiSettings s = {0};

    s.width = (uint32_t) width;
    s.height = (uint32_t) height;
    s.quality = (uint8_t) quality;
    s.fast = fast == JNI_TRUE;
    s.repeat = repeat;

    gifski *g = gifski_new(&s);
    return (jlong) (uintptr_t) g;
}

JNIEXPORT jint JNICALL
Java_app_nekogram_gifski_GifskiNative_nativeSetMotionQuality(JNIEnv *env, jclass cls, jlong handle,
                                                       jint quality) {
    if (!handle) return GIFSKI_NULL_ARG;

    return gifski_set_motion_quality(PTR(handle), (uint8_t) quality);
}

JNIEXPORT jint JNICALL
Java_app_nekogram_gifski_GifskiNative_nativeSetLossyQuality(JNIEnv *env, jclass cls, jlong handle,
                                                      jint quality) {
    if (!handle) return GIFSKI_NULL_ARG;

    return gifski_set_lossy_quality(PTR(handle), (uint8_t) quality);
}

JNIEXPORT jint JNICALL
Java_app_nekogram_gifski_GifskiNative_nativeSetExtraEffort(JNIEnv *env, jclass cls, jlong handle,
                                                     jboolean extra) {
    if (!handle) return GIFSKI_NULL_ARG;

    return gifski_set_extra_effort(PTR(handle), extra == JNI_TRUE);
}

JNIEXPORT jint JNICALL
Java_app_nekogram_gifski_GifskiNative_nativeAddFixedColor(JNIEnv *env, jclass cls, jlong handle, jint r,
                                                    jint g, jint b) {
    if (!handle) return GIFSKI_NULL_ARG;

    return gifski_add_fixed_color(
            PTR(handle),
            (uint8_t) r,
            (uint8_t) g,
            (uint8_t) b
    );
}

JNIEXPORT jint JNICALL
Java_app_nekogram_gifski_GifskiNative_nativeSetFileOutput(JNIEnv *env, jclass cls, jlong handle,
                                                    jstring path) {
    if (!handle || !path) return GIFSKI_NULL_ARG;

    const char *cpath = (*env)->GetStringUTFChars(env, path, NULL);
    GifskiError err = gifski_set_file_output(PTR(handle), cpath);
    (*env)->ReleaseStringUTFChars(env, path, cpath);

    return err;
}

JNIEXPORT jint JNICALL
Java_app_nekogram_gifski_GifskiNative_nativeAddFrameBitmap(
        JNIEnv *env,
        jclass cls,
        jlong handle,
        jint frameNumber,
        jobject bitmap,
        jdouble pts
) {
    if (!handle || !bitmap) {
        return GIFSKI_NULL_ARG;
    }

    AndroidBitmapInfo info;
    void *pixels = NULL;

    if (AndroidBitmap_getInfo(env, bitmap, &info) != ANDROID_BITMAP_RESULT_SUCCESS ||
        AndroidBitmap_lockPixels(env, bitmap, &pixels) != ANDROID_BITMAP_RESULT_SUCCESS) {
        return GIFSKI_INVALID_INPUT;
    }

    // Android: RGBA_8888 in memory == R G B A bytes
    // gifski expects ARGB for gifski_add_frame_argb
    GifskiError err = gifski_add_frame_rgba_stride(
            PTR(handle),
            (uint32_t) frameNumber,
            (uint32_t) info.width,
            (uint32_t) info.height,
            (uint32_t) info.stride, // bytes per row
            (const unsigned char *) pixels,
            pts
    );

    AndroidBitmap_unlockPixels(env, bitmap);
    return err;
}

JNIEXPORT jint JNICALL
Java_app_nekogram_gifski_GifskiNative_nativeFinish(JNIEnv *env, jclass cls, jlong handle) {
    if (!handle) return GIFSKI_NULL_ARG;
    return gifski_finish(PTR(handle));
}
