package com.example.helloworld;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.provider.MediaStore;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.os.Handler;

import java.util.Random;

public class GameView extends View{

    Context context;
    float balleX, balleY;  //coordonnées de la balle
    Vitesse vitesse = new Vitesse(25,32); // vitesse initiale de la balle
    Handler handler; // rafraichir l'écran

    final long UPDATE_MS = 30; // chaque 30 ms, l'écran est mis à jour
    Runnable runnable; // mettre à jour l'affichage
    Paint textPaint = new Paint (); // Objet paint pour le texte
    Paint viePaint = new Paint (); // objet paint pour afficher la barre des vies
    Paint briquePaint = new Paint (); // objet paint pour dessiner les briques
    float text_size = 120; // taille du texte
    float raquetteX, raquetteY;// Coordonnées de la raquette
    float oldX, oldRaquetteX; // anciennes coordonnées pour suivre le déplacement de la raquette
    int points = 0; // le score du jeu
    int vies = 3; // Nombre de vies du joueur
    Bitmap balle, raquette; // Images de la balle et de la raquette
    int dl, dL;// dimensions de l'écran ( l = largeur; L=Longueur )
    int lballe, Lballe; // dimensions de la balle
    MediaPlayer mprater,mpfrapper,mpcasser; // sons pour quand on rate le coup; frappe la balle et casse une brique
    Random random; // générateur de nombres
    Brique[] briques = new Brique[30]; // tableau de briques
    int numBriques = 0; // pour compter le nombre de briques
    int brokenBriques =0; // compter le nombre de briques cassées
    boolean gameOver = false;

    public GameView(Context context){
        super(context);
        this.context = context;
        // Charger l'image de la balle et de la raquette
        balle = BitmapFactory.decodeResource(getResources(), R.drawable.ballerouge);
        raquette = BitmapFactory.decodeResource(getResources(), R.drawable.raquette);
        //réduction de l'image de la balle
        int newlballe = balle.getWidth()/40;
        int newLballe = balle.getHeight()/40;
        balle = Bitmap.createScaledBitmap(balle,newlballe,newLballe, false);
        // réduction de l'image de la raquette
        int newlraquette = raquette.getWidth()/16;
        int newLraquette = raquette.getHeight()/26;
        raquette = Bitmap.createScaledBitmap(raquette,newlraquette,newLraquette, false);

        handler = new Handler();
        runnable = new Runnable(){
            @Override
            public void run (){
                invalidate();
            }
        };
        // Chargement des sons
        mpfrapper = MediaPlayer.create(context, R.raw.mpfrapper);
        mprater = MediaPlayer.create(context, R.raw.mprater);
        mpcasser = MediaPlayer.create(context, R.raw.mpcasser);
        textPaint.setColor(Color.RED); // couleur du texte
        textPaint.setTextSize(text_size); // taille du texte
        textPaint.setTextAlign(Paint.Align.LEFT); // aligement du texte
        viePaint.setColor(Color.GREEN); // couleur de la barre de vies
        briquePaint.setColor(Color.CYAN); // couleur des briques
        Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();// récupère la taille de l'écran
        Point size = new Point();
        display.getSize(size);
        dl = size.x; // largeur de l'écran
        dL = size.y; // Longueur de l'écran
        random = new Random();
        balleX = random.nextInt(dl-50); // position initiale de la balle
        balleY = dL/3; // Position de la balle en hauteur
        raquetteY = (dL *4)/5;  // position  de la raquette en hauteur
        raquetteX = dl/2 - raquette.getWidth()/2; // Position de la raquette au centre de l'écran
        lballe = balle.getWidth(); // largeur de la balle
        Lballe = balle.getHeight(); // hauteur de la balle
        creerBriques(); //
    }
    private void creerBriques (){ // méthode pour créer les briques
        int lbrique = dl/8; // largeur des briques
        int Lbrique = dL / 16; // Longueur des briques
        for (int colone = 0; colone <8; colone++){
            for (int rg = 0; rg<3; rg++){
                briques[numBriques] = new Brique(rg, colone, lbrique,Lbrique);
                numBriques++;
            }
        }
    }
    @Override
    protected void onDraw (Canvas canvas ){
        super.onDraw(canvas);
        canvas.drawColor(Color.BLACK);
        balleX += vitesse.getX();
        balleY += vitesse.getY();
        if ((balleX >= dl - balle.getWidth()) || balleX <= 0){
            vitesse.setX(vitesse.getX() * -1);
        }
        if (balleY <= 0){
            vitesse.setY(vitesse.getY() * -1);
        }
        if (balleY > raquetteY + raquette.getHeight()) {
            balleX = 1 + random.nextInt(dl - balle.getWidth() - 1);
            balleY = dL / 3;
            if (mprater != null) {
                mprater.start();
            }
            vitesse.setX(xVitesse());
            vitesse.setY(32);
            vies--;
            if (vies == 0) {
                gameOver = true;
                ///LancerGameOver();
            }
        }
            if((balleX + balle.getWidth() >= raquetteX )
            && (balleX <= raquetteX + raquette.getWidth())
            && (balleY + balle.getHeight() >= raquetteY)
            && (balleY <= raquetteY + raquette.getHeight())){
                if ( mpfrapper != null){
                    mpfrapper.start();
                }

                vitesse.setY(-Math.abs(vitesse.getY() ));
            }
            canvas.drawBitmap(balle,balleX,balleY,null);
            canvas.drawBitmap(raquette, raquetteX,raquetteY,null);
            for(int i=0; i<numBriques;i++){
                if(briques[i].getVisibility()){
                    canvas.drawRect(
                            briques[i].colone * briques[i].l +1,
                            briques[i].rg * briques[i].L + 1,
                            briques [i].colone * briques[i].l + briques[i].l -1,
                            briques[i].rg * briques[i].L + briques[i].L -1,
                            briquePaint
                    );

                }
            }
            canvas.drawText(""+ points, 20, text_size,textPaint);
            if(vies == 2){
                viePaint.setColor(Color.YELLOW);
            } else if (vies ==1) {
                viePaint.setColor(Color.RED);
            }
            canvas.drawRect(dl-200,30,dl-200 + 60 * vies, 80,viePaint);
            for(int i=0; i<numBriques; i++){
                if(briques[i].getVisibility()){
                    if(balleX + lballe >= briques[i].colone * briques[i].l
                    && balleX <= briques[i].colone * briques[i].l + briques[i].l
                    && balleY <= briques[i].rg * briques[i].L + briques[i].L
                    && balleY >= briques[i].rg * briques[i].L){
                        if (mpcasser != null){
                            mpcasser.start();
                        }
                        vitesse.setY((vitesse.getY() + 1) * -1);
                        briques[i].setInvisible();
                        points += 10;
                        brokenBriques++;
                        if (brokenBriques == 24){
                            //LancerGameOver();
                        }
                    }
                }
            }
            if ( brokenBriques == numBriques){
                gameOver = true;
            }
            if (!gameOver){
                handler.postDelayed(runnable, UPDATE_MS);
            }
        }
    
    @Override
    public boolean onTouchEvent(MotionEvent event){
        float toucheX = event.getX();
        float toucheY = event.getY();
        if ( toucheY >= raquetteY){
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN){
                oldX = event.getX();
                oldRaquetteX = raquetteX;
            }
            if (action == MotionEvent.ACTION_MOVE){
                float shift =  oldX - toucheX;
                float NouvRaquetteX = oldRaquetteX - shift;
                if (NouvRaquetteX <= 0)
                    raquetteX = 0;
                else if (NouvRaquetteX >= dl - raquette.getWidth())
                    raquetteX = dl - raquette.getWidth();
                else
                    raquetteX = NouvRaquetteX;
            }
        }
        return true;
    }

    private void LancerGameOver() {
        /*handler.removeCallbacksAndMessages(null);
        Intent intent = new Intent(context, GameOver.class);
        intent.putExtra("points", points);
        context.startActivity(intent);*/
        ((Activity) context).finish();


    }

    private int xVitesse (){
        int [] valeurs = {-35, -30, -25, 30, 35};
        int index = random.nextInt(6);
        return valeurs [index];

    }

}

