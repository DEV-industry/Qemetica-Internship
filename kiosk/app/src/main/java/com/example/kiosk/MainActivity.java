package com.example.kiosk;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static MyAccessibilityService accessibilityService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnStart = findViewById(R.id.btnClick);
        btnStart.setOnClickListener(v -> {
            if (accessibilityService != null) {
                accessibilityService.startAutoClick();
                Toast.makeText(this, "Auto-klik co 1s uruchomiony", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Usługa dostępności nieaktywna!", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnStop = findViewById(R.id.btnStop);
        btnStop.setOnClickListener(v -> {
            if (accessibilityService != null) {
                accessibilityService.stopAutoClick();
                Toast.makeText(this, "Auto-klik zatrzymany", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
