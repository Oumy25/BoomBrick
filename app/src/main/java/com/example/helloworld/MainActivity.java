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
    // démarre le jeu en mode "easy"
    public void startGameEasy(View view ){
        // Modif ici pour niveau medium j'"ai rajouté easy, yavait pas avant
        GameView gameView = new GameView(this, "easy");
        setContentView(gameView);
    }
    public void startGameMedium(View view){
        GameView gameView = new GameView(this, "medium");
        setContentView(gameView);
    }
    public void startGameSemiHard(View view){
        GameView gameView = new GameView(this, "semi");
        setContentView(gameView);
    }
}