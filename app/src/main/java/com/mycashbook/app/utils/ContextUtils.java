package com.mycashbook.app.utils;

import android.content.Context;
import android.content.ContextWrapper;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;

/**
 * Utility class for handling Android Context operations safely
 */
public class ContextUtils {

    /**
     * Safely unwraps a context to find a FragmentActivity
     */
    public static FragmentActivity getFragmentActivity(Context context) {
        if (context == null)
            return null;
        if (context instanceof FragmentActivity) {
            return (FragmentActivity) context;
        } else if (context instanceof ContextWrapper) {
            return getFragmentActivity(((ContextWrapper) context).getBaseContext());
        }
        return null;
    }

    /**
     * Safely unwraps a context to find a LifecycleOwner
     */
    public static LifecycleOwner getLifecycleOwner(Context context) {
        if (context == null)
            return null;
        if (context instanceof LifecycleOwner) {
            return (LifecycleOwner) context;
        } else if (context instanceof ContextWrapper) {
            return getLifecycleOwner(((ContextWrapper) context).getBaseContext());
        }
        return null;
    }
}
