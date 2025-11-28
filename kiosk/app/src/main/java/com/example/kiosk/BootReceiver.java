package com.example.kiosk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("BootReceiver", "Telefon uruchomiony – AccessibilityService zostanie włączona przez system");

            // UWAGA: nie można ręcznie startować AccessibilityService
            // System uruchamia ją automatycznie, jeśli użytkownik ją włączył w ustawieniach.
            // Jedyne co możemy zrobić to np. uruchomić MainActivity:
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}
