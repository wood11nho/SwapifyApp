package com.elias.swapify.keyboard;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;

public class KeyboardUtils {
    public static void addKeyboardVisibilityListener(Activity activity, KeyboardVisibilityListener listener) {
        final View activityRootView = activity.findViewById(android.R.id.content);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private boolean wasKeyboardVisible = false;

            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                activityRootView.getWindowVisibleDisplayFrame(r);
                int heightDiff = activityRootView.getRootView().getHeight() - r.height();
                boolean isKeyboardVisible = heightDiff > activityRootView.getRootView().getHeight() * 0.15;

                if (isKeyboardVisible != wasKeyboardVisible) {
                    wasKeyboardVisible = isKeyboardVisible;
                    listener.onVisibilityChanged(isKeyboardVisible);
                }
            }
        });
    }

    public interface KeyboardVisibilityListener {
        void onVisibilityChanged(boolean isVisible);
    }
}
