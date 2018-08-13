package com.example.process;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.process.guard.LocalService;
import com.example.process.guard.RemoteService;

import java.lang.annotation.Retention;

public class ServiceActivity extends AppCompatActivity implements View.OnClickListener {
    private Button mGuard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);
        mGuard = findViewById(R.id.startService);
        mGuard.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == mGuard) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                Intent intent = new Intent(this, LocalService.class);
                startService(intent);
                intent = new Intent(this, RemoteService.class);
                startService(intent);
            } else {
                Intent intent = new Intent(this, ProtectedService.class);
                startService(intent);

                intent = new Intent(this, RestartJobService.class);
                startService(intent);
            }
        }
    }
}
