package com.example.helloworld;

import static android.view.WindowManager.*;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
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
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Vérifier si l'on revient d'un GameOver
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("restart", false)) {
            String level = intent.getStringExtra("level");
            if (level != null) {
                // Recharger le niveau spécifié
                startGame(null, level);
            }
        }
    }
    public void startGame(View view, String level) {
        GameView gameView = new GameView(this, level);
        setContentView(gameView);
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
    public void startGameHard(View view){
        GameView gameView = new GameView(this, "hard");
        setContentView(gameView);
    }
    public void openInstructions(View view){
        OpenInstructions instructionsDialog = new OpenInstructions(this);
        instructionsDialog.show();
    }
}