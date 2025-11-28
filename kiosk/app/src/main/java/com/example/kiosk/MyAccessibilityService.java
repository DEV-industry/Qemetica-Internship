package com.example.kiosk;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.annotation.SuppressLint;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.List;


public class MyAccessibilityService extends AccessibilityService {

    private Handler handler;
    private Runnable autoClickRunnable;
    private boolean isAutoClicking = false;

    // 🔹 do obsługi 20 klików w róg
    private View exitTouchView;
    private int cornerClickCount = 0;
    private long lastClickTime = 0;


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {}

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        MainActivity.accessibilityService = this;

        addExitOverlay();   // 🔹 dodajemy ukryty róg
        startAutoClick();   // 🔹 start autoklika
    }

    // 🔹 Funkcja klikająca w punkt
    public void clickAt(int x, int y) {
        Path path = new Path();
        path.moveTo(x, y);

        GestureDescription.StrokeDescription stroke =
                new GestureDescription.StrokeDescription(path, 0, 100);

        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(stroke);

        dispatchGesture(builder.build(), null, null);
    }

    private int[] getScreenSize() {
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(metrics);
        return new int[]{metrics.widthPixels, metrics.heightPixels};
    }

    private String getForegroundApp() {
        long end = System.currentTimeMillis();
        long begin = end - 10000;

        UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        List<UsageStats> stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, begin, end);

        if (stats != null) {
            UsageStats recent = null;
            for (UsageStats u : stats) {
                if (recent == null || u.getLastTimeUsed() > recent.getLastTimeUsed()) {
                    recent = u;
                }
            }
            if (recent != null) {
                return recent.getPackageName();
            }
        }
        return null;
    }

    private boolean isAllowedScreen() {
        String pkg = getForegroundApp();
        if (pkg == null) return false;

        // 🔹 Zakaz tylko dla Chrome
        return !pkg.equals("com.android.chrome");
    }


    // 🔹 Start auto-klikania co 1 sekundę
    public void startAutoClick() {
        if (isAutoClicking) return;

        handler = new Handler(Looper.getMainLooper());
        autoClickRunnable = new Runnable() {
            @Override
            public void run() {
                if (isAllowedScreen()) {
                    int[] size = getScreenSize();
                    int centerX = size[0] / 2;
                    int bottomY = size[1] - 10;
                    clickAt(centerX, bottomY);
                    clickAt(centerX, bottomY);

                    handler.postDelayed(() -> clickAt(200, 300), 500);
                }

                handler.postDelayed(this, 1_000);
            }
        };

        handler.post(autoClickRunnable);
        isAutoClicking = true;
    }

    // 🔹 Stop auto-klikania
    public void stopAutoClick() {
        if (handler != null && autoClickRunnable != null) {
            handler.removeCallbacks(autoClickRunnable);
        }
        isAutoClicking = false;
    }

    // 🔹 Dodanie niewidzialnego pola w rogu
    @SuppressLint("ClickableViewAccessibility")
    private void addExitOverlay() {
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                200, 200, // rozmiar rogu w px
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.BOTTOM | Gravity.LEFT; // lewy dolny róg

        exitTouchView = new View(this);
        exitTouchView.setBackgroundColor(Color.TRANSPARENT); // niewidzialny

        exitTouchView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                long now = System.currentTimeMillis();

                // reset licznika po 3s bez klikania
                if (now - lastClickTime > 3000) {
                    cornerClickCount = 0;
                }
                lastClickTime = now;

                cornerClickCount++;
                if (cornerClickCount >= 20) {
                    stopAutoClick();
                    Toast.makeText(this, "⛔ Auto-click WYŁĄCZONY (20 klików w rogu)", Toast.LENGTH_LONG).show();
                    cornerClickCount = 0;
                }
            }
            return true;
        });

        wm.addView(exitTouchView, params);
    }
}
