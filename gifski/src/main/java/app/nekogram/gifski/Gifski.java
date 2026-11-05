package app.nekogram.gifski;

import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

/**
 * Java binding for the gifski encoder.
 *
 * <p>Typical usage:</p>
 * <pre>
 * Gifski g = new Gifski(settings);
 * g.setFileOutput(path);
 * g.addFrameBitmap(0, bitmap0, 0.0);
 * g.addFrameBitmap(1, bitmap1, 0.05);
 * g.finish();
 * </pre>
 *
 * <p>Notes:</p>
 * <ul>
 *   <li>All set* configuration methods must be called before adding frames.</li>
 *   <li>{@link #finish()} must always be called to avoid native memory leaks.</li>
 *   <li>This class is not thread-safe.</li>
 * </ul>
 */
public final class Gifski {

    private long handle; // native gifski*

    /**
     * <p>Settings for creating a new encoder instance.</p>
     *
     * <p>If width is non-zero and height is zero, height is computed automatically.
     * If both are zero, the original frame size is used.</p>
     */
    public static final class Settings {
        private int width;
        private int height;
        private int quality = 90;
        private boolean fast;
        private short repeat = -1;

        public Settings() {
        }

        /**
         * Settings for creating a new encoder instance.
         *
         * @param width   Resize to max this width if non-0.
         * @param height  Resize to max this height if width is non-0. Note that aspect ratio is not preserved.
         * @param quality 1-100, but useful range is 50-100. Recommended to set to 90.
         * @param fast    Lower quality, but faster encode.
         * @param repeat  If negative, looping is disabled. The number of times the sequence is repeated. 0 to loop forever.
         */
        public Settings(int width, int height, @IntRange(from = 1, to = 100) int quality, boolean fast, short repeat) {
            this.width = width;
            this.height = height;
            this.quality = quality;
            this.fast = fast;
            this.repeat = repeat;
        }

        public int getWidth() {
            return width;
        }

        /**
         * Resize to max this width if non-0.
         */
        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        /**
         * Resize to max this height if width is non-0. Note that aspect ratio is not preserved.
         */
        public void setHeight(int height) {
            this.height = height;
        }

        @IntRange(from = 1, to = 100)
        public int getQuality() {
            return quality;
        }

        /**
         * 1-100, but useful range is 50-100. Recommended to set to 90.
         */
        public void setQuality(@IntRange(from = 1, to = 100) int quality) {
            this.quality = quality;
        }

        public boolean isFast() {
            return fast;
        }

        /**
         * Lower quality, but faster encode.
         */
        public void setFast(boolean fast) {
            this.fast = fast;
        }

        public short getRepeat() {
            return repeat;
        }

        /**
         * If negative, looping is disabled. The number of times the sequence is repeated. 0 to loop forever.
         */
        public void setRepeat(short repeat) {
            this.repeat = repeat;
        }
    }

    /**
     * Creates a new gifski encoder instance.
     *
     * @param settings encoder settings
     * @throws IllegalStateException on failed to create encoder
     */
    public Gifski(Settings settings) throws IllegalStateException {
        handle = GifskiNative.nativeNew(settings.width, settings.height, settings.quality, settings.fast, settings.repeat);
        if (handle == 0) {
            throw new IllegalStateException("Failed to create gifski encoder");
        }
    }

    /**
     * <p>Quality 1-100 of temporal denoising. Lower values reduce motion. Defaults to {@link Settings#getQuality()}.</p>
     *
     * <p>Only valid immediately after calling {@code new}, before any frames are added.</p>
     *
     * @param quality quality of temporal denoising.
     * @throws IllegalStateException if the encoder has already been finished
     * @throws GifskiException       if an error occurs in the native encoder
     */
    public void setMotionQuality(@IntRange(from = 1, to = 100) int quality) throws GifskiException {
        if (handle == 0) {
            throw new IllegalStateException("Gifski has already been finished");
        }
        GifskiException.throwIfError(GifskiNative.nativeSetMotionQuality(handle, quality));
    }

    /**
     * <p>Quality 1-100 of gifsicle compression. Lower values add noise. Defaults to {@link Settings#getQuality()}.</p>
     *
     * <p>Only valid immediately after calling {@code new}, before any frames are added.</p>
     *
     * @param quality quality of gifsicle compression.
     * @throws IllegalStateException if the encoder has already been finished
     * @throws GifskiException       if an error occurs in the native encoder
     */
    public void setLossyQuality(@IntRange(from = 1, to = 100) int quality) throws GifskiException {
        if (handle == 0) {
            throw new IllegalStateException("Gifski has already been finished");
        }
        GifskiException.throwIfError(GifskiNative.nativeSetLossyQuality(handle, quality));
    }

    /**
     * <p>If {@code true}, encoding will be significantly slower, but may look a bit better.</p>
     *
     * <p>Only valid immediately after calling {@code new}, before any frames are added.</p>
     *
     * @param extra use extra effort
     * @throws IllegalStateException if the encoder has already been finished
     * @throws GifskiException       if an error occurs in the native encoder
     */
    public void setExtraEffort(boolean extra) throws GifskiException {
        if (handle == 0) {
            throw new IllegalStateException("Gifski has already been finished");
        }
        GifskiException.throwIfError(GifskiNative.nativeSetExtraEffort(handle, extra));
    }

    /**
     * <p>Adds a fixed color that will be kept in the palette at all times.</p>
     *
     * <p>Only valid immediately after calling {@code new}, before any frames are added.</p>
     *
     * @param color A fixed color that will be kept in the palette at all times
     * @throws IllegalStateException if the encoder has already been finished
     * @throws GifskiException       if an error occurs in the native encoder
     */
    public void addFixedColor(@ColorInt int color) throws GifskiException {
        if (handle == 0) {
            throw new IllegalStateException("Gifski has already been finished");
        }
        GifskiException.throwIfError(GifskiNative.nativeAddFixedColor(handle, Color.red(color), Color.green(color), Color.blue(color)));
    }

    /**
     * <p>Insert a new frame at the position of {@code frameNumber}.</p>
     *
     * <p>Frame numbers start at 0, and must be consecutive (without gaps).
     * You can add frames in any order, and they will be sorted by their {@code frameNumber}.</p>
     *
     * <p>The bitmap must be {@link Bitmap.Config#ARGB_8888}.</p>
     * <p>The bitmap contents are copied synchronously during this call.
     * The bitmap may be reused or recycled after the method returns.</p>
     *
     * <p>Presentation timestamp (PTS) is time in seconds, since start of the file, when this frame is to be displayed.
     * For a 20fps video it could be {@code frameNumber/20.0}.
     * Frames with duplicate or out-of-order PTS will be skipped.
     * The first frame should have PTS=0. If the first frame has PTS > 0, it'll be used as a delay after the last frame.</p>
     *
     * <p>This function may block and wait until encoding can start.</p>
     *
     * @param frameNumber position of frame, start and 0, and must be consecutive
     * @param bitmap      frame in bitmap, must be ARGB_8888
     * @param pts         time in seconds, since start of the file, when this frame is to be displayed
     * @throws IllegalStateException if the encoder has already been finished
     * @throws GifskiException       if an error occurs in the native encoder
     */
    public void addFrameBitmap(int frameNumber, @NonNull Bitmap bitmap, double pts) throws GifskiException {
        if (handle == 0) {
            throw new IllegalStateException("Gifski has already been finished");
        }
        if (bitmap.getConfig() != Bitmap.Config.ARGB_8888) {
            throw new IllegalArgumentException("Bitmap must be ARGB_8888");
        }
        GifskiException.throwIfError(GifskiNative.nativeAddFrameBitmap(handle, frameNumber, bitmap, pts));
    }

    /**
     * <p>Start writing to the file at {@code destinationPath} (overwrites if needed).</p>
     *
     * <p>This function has to be called before any frames are added.
     * This call will not block.</p>
     *
     * @param destinationPath destination path
     * @throws IllegalStateException if the encoder has already been finished
     * @throws GifskiException       if an error occurs in the native encoder
     */
    public void setFileOutput(@NonNull String destinationPath) throws GifskiException {
        if (handle == 0) {
            throw new IllegalStateException("Gifski has already been finished");
        }
        GifskiException.throwIfError(GifskiNative.nativeSetFileOutput(handle, destinationPath));
    }

    /**
     * <p>The last step:</p>
     * <ul>
     *   <li>Stops accepting any more frames (addFrame* calls are blocked)</li>
     *   <li>Blocks and waits until all already-added frames have finished writing</li>
     * </ul>
     *
     * <p>Must always be called, otherwise it will leak memory.
     * After this call, the handle is freed and can't be used any more.</p>
     *
     * @throws GifskiException if an error occurs in the native encoder
     */
    public void finish() throws GifskiException {
        if (handle != 0) {
            GifskiException.throwIfError(GifskiNative.nativeFinish(handle));
            handle = 0;
        }
    }

    /**
     * Error codes returned by the native gifski encoder.
     *
     * <p>These correspond closely to the underlying gifski / C API errors.
     * Any value other than {@link #OK} indicates a failure.</p>
     */
    public enum GifskiError {
        OK(0),

        /**
         * one of input arguments was NULL
         */
        NULL_ARG(1),
        /**
         * a one-time function was called twice, or functions were called in wrong order
         */
        INVALID_STATE(2),
        /**
         * internal error related to palette quantization
         */
        QUANT(3),
        /**
         * internal error related to gif composing
         */
        GIF(4),
        /**
         * internal error - unexpectedly aborted
         */
        THREAD_LOST(5),

        /**
         * I/O error: file or directory not found
         */
        NOT_FOUND(6),
        /**
         * I/O error: permission denied
         */
        PERMISSION_DENIED(7),
        /**
         * I/O error: file already exists
         */
        ALREADY_EXISTS(8),

        /**
         * invalid arguments passed to function
         */
        INVALID_INPUT(9),

        /**
         * misc I/O error
         */
        TIMED_OUT(10),
        /**
         * misc I/O error
         */
        WRITE_ZERO(11),
        /**
         * misc I/O error
         */
        INTERRUPTED(12),
        /**
         * misc I/O error
         */
        UNEXPECTED_EOF(13),

        /**
         * progress callback returned 0, writing aborted
         */
        ABORTED(14),
        /**
         * should not happen, file a bug
         */
        OTHER(15);

        private final int code;

        GifskiError(int code) {
            this.code = code;
        }

        /**
         * Maps a native integer error code to a {@link GifskiError}.
         *
         * @param code native error code
         * @return matching {@link GifskiError}, or {@link #OTHER} if unknown
         */
        static GifskiError fromCode(int code) {
            for (GifskiError e : values()) {
                if (e.code == code) {
                    return e;
                }
            }
            return OTHER;
        }

        /**
         * @return {@code true} if this error represents success
         */
        public boolean isOk() {
            return this == OK;
        }
    }

    /**
     * Runtime exception thrown when a gifski operation fails.
     *
     * <p>This exception wraps a {@link GifskiError} returned by the native encoder.
     * All public {@link Gifski} methods either succeed silently or throw this exception.</p>
     *
     */
    public static final class GifskiException extends RuntimeException {
        private final GifskiError error;

        /**
         * Creates a new exception for the given gifski error.
         *
         * @param error the underlying {@link GifskiError}
         */
        GifskiException(GifskiError error) {
            super("Gifski error: " + error + " (code=" + error.code + ")");
            this.error = error;
        }

        /**
         * Returns the underlying {@link GifskiError} that caused this exception.
         *
         * @return the gifski error code
         */
        public GifskiError getError() {
            return error;
        }

        /**
         * Throws a {@link GifskiException} if the given native error code indicates a failure.
         *
         * <p>This is a convenience helper for JNI wrappers. It converts a native integer
         * error code into a {@link GifskiError} using {@link GifskiError#fromCode(int)}
         * and throws a {@link GifskiException} for any value other than {@link GifskiError#OK}.</p>
         *
         * <p>If the error code is unknown, {@link GifskiError#OTHER} is used.</p>
         *
         * @param code native error code returned by the gifski encoder
         * @throws GifskiException if {@code code} does not represent success
         */
        static void throwIfError(int code) {
            GifskiError err = GifskiError.fromCode(code);
            if (!err.isOk()) {
                throw new GifskiException(err);
            }
        }
    }
}
