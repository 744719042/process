package com.example.process;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.example.process.singleton.PixelActivityManager;

public class PixelActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_pixel);
        WindowManager windowManager = getWindowManager();
        View view = new View(this);
        view.setBackgroundColor(Color.RED);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.width = 5;
        params.height = 5;
        params.gravity = Gravity.TOP | Gravity.RIGHT;
        windowManager.addView(view, params);

        PixelActivityManager.getInstance().init(this);
    }
}
