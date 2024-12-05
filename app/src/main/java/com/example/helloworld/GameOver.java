package com.example.helloworld;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
//juste pour commit
import com.example.helloworld.MainActivity;
import com.example.helloworld.R;

public class GameOver extends AppCompatActivity {
    TextView tvPoints;
    ImageView record;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        super .onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.game_over);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //record = findViewById(R.id.record);
        tvPoints=findViewById(R.id.tvpoints);

        int points = getIntent().getIntExtra("points",0);
        if (points == 240){
            record.setVisibility(TextView.VISIBLE);
        }
        tvPoints.setText(String.valueOf(points));
    }
    public void restart(View view){
        String level = getIntent().getStringExtra("level");

        // Retourner au niveau précédent

            Intent intent = new Intent(GameOver.this, MainActivity.class);
            intent.putExtra("restart", true);
            intent.putExtra("level", level);
            startActivity(intent);
            finish();


    }
    public void home (View view){
        Intent intent = new Intent(GameOver.this, MainActivity.class);
        intent.putExtra("home", true);
        startActivity(intent);
        finish();
    }
}
