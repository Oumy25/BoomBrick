package com.example.helloworld;

import static android.view.WindowManager.*;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    public void startgame(View view ){
        GameView gameView = new GameView(this);
        setContentView(gameView);
    }
}