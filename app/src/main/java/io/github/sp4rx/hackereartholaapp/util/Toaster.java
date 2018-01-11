package io.github.sp4rx.hackereartholaapp.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.StringRes;
import android.widget.Toast;

/**
 * Created by suvajit.<br>
 * This class is to maintain cancelable previous toasts.
 */

@SuppressLint("ShowToast")
public class Toaster {
    @IntDef({LENGTH_SHORT, LENGTH_LONG})
    @interface ToastMode {
    }

    /**
     * Short duration of toast
     */
    public static final int LENGTH_SHORT = 0;
    /**
     * Long duration of toast
     */
    public static final int LENGTH_LONG = 1;
    private static Toast toast;

    /**
     * Makes a Toast message
     *
     * @param context Context
     * @param message Toast message
     * @param length  Length of 0 or 1
     * @return {@link Toaster}
     */
    public static Toast makeText(Context context, String message, @ToastMode int length) {
        if (toast != null)
            toast.cancel();
        toast = Toast.makeText(context, message, length);
        return toast;
    }

    /**
     * Makes a Toast message
     *
     * @param context    Context
     * @param messageRes Toast message string resource
     * @param length     Length of 0 or 1
     * @return {@link Toaster}
     */
    public static Toast makeText(Context context, @StringRes int messageRes, @ToastMode int length) {
        if (toast != null)
            toast.cancel();
        toast = Toast.makeText(context, messageRes, length);
        return toast;
    }
}
