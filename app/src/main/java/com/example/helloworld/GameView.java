package com.example.helloworld;

import static android.os.Build.VERSION_CODES.O;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Shader;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.os.Handler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.WindowManager;
import android.widget.Toast;

public class GameView extends View{

    String niveau;
    Context context;
    float balleX, balleY;  //coordonnées de la balle
    Vitesse vitesse = new Vitesse(25,32); // vitesse initiale de la balle
    Handler handler; // rafraichir l'écran

    final long UPDATE_MS = 16; // chaque 30 ms, l'écran est mis à jour
    Runnable runnable; // mettre à jour l'affichage
    Paint textPaint = new Paint (); // Objet paint pour le texte
    Paint viePaint = new Paint (); // objet paint pour afficher la barre des vies
    Paint briquePaint = new Paint (); // objet paint pour dessiner les briques
    float text_size = 120; // taille du texte
    float raquetteX, raquetteY;// Coordonnées de la raquette
    float oldX, oldRaquetteX; // anciennes coordonnées pour suivre le déplacement de la raquette
    int points = 0; // le score du jeu
    int vies = 3; // Nombre de vies du joueur
    Bitmap balle, raquette,malusRaquetteImage,malusVitesseImage; // Images de la balle et de la raquette
    int dl, dL;// dimensions de l'écran ( l = largeur; L=Longueur )
    int lballe, Lballe; // dimensions de la balle
    MediaPlayer mprater,mpfrapper,mpcasser; // sons pour quand on rate le coup; frappe la balle et casse une brique
    Random random; // générateur de nombres
    Brique[] briques = new Brique[1000]; // tableau de briques
    int numBriques = 0; // pour compter le nombre de briques
    int brokenBriques =0; // compter le nombre de briques cassées
    boolean gameOver = false;
    List<Malus> malusList = new ArrayList<>();
    boolean mvmBalle;
    // Variables semi hard
    private long Tfin = 0;
    private final long Int_Descente = 5000;
    private int Ddescente = 10;
    private boolean briquesdescente = false;
    private int[] resistances;
    // Variables hard
    private int VbriqueHoriz = 10;
    private boolean direction = true;
    private int CaptMvm;
    private final int Delay_mvm = 5;
    private int decalageHorizontal = 0;
    private  int [] Vhoriz;
    private int [] directionsRg;
    private long LastChangement = 0;
    private final long Int_vitesse = 5000; // Intervallle de changement vitesse
    private boolean Vaccel = false; // Accélaration de la balle
    private boolean ecranSombre = false;
    private long DbeffetSombre = 0;
    private final long Duree_effetSombre = 500;
    private final long INT_effet = 7000;
    private long Lasteffet = 0;
    private List<Ball> Addballs = new ArrayList<>();
    private long ballStime = 0;
    private  final long dureeBall = 5000;
    private long lastBallTime = 0;
    private final long INT_ball = 15000;
    private final int MAX_ball = 5;
    private boolean AddBallsGener = false;
    private final int SEUIL_SCORE = 50;
    private int[] delaiColonnes; // Délai de déplacement pour chaque colonne
    private final int DELAI_BASE = 500;
    //Gestion des capteurs :
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private SensorEventListener sensorEventListener;
    private float initialXTilt = 0; // Valeur de référence pour l'inclinaison
    private boolean tiltCalibrated = false; // État de calibration
    private static final float SENSITIVITY = 15.0f;
    private Bitmap homeButtonBitmap;
    private int headerHeight = 110; // Hauteur totale du rectangle supérieur (pixels)
    private boolean gagner = false;
    private Bitmap backgroundBitmap;
    boolean showMoveMessage = true; // Affiche le message au début


    public GameView(Context context, String niveau){
        super(context);
        this.context = context;
        // Stocke le niveau de jeu
        this.niveau = niveau;

        homeButtonBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.home_blanc); // Bouton Home pour revenir à la page d'accueil
        int homeButtonSize = 100; // Taille en pixels
        homeButtonBitmap = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(getResources(), R.drawable.home_blanc),
                homeButtonSize,
                homeButtonSize,
                false
        );

        // Choix des vitesses par niveau
        if (niveau.equals("medium")){
            vitesse = new Vitesse(25, 30);
        } else {
            vitesse = new Vitesse(25,32);
        }

        // Charger l'image de la balle, de la raquette, du malus et de la balle Malus
        balle = BitmapFactory.decodeResource(getResources(), R.drawable.ballerouge);
        raquette = BitmapFactory.decodeResource(getResources(), R.drawable.raquette);
        malusRaquetteImage = BitmapFactory.decodeResource(getResources(),R.drawable.malus_raquette);
        malusVitesseImage = BitmapFactory.decodeResource(getResources(),R.drawable.ballemalus);
        //réduction de l'image de la balle
        int newlballe = balle.getWidth()/60;
        int newLballe = balle.getHeight()/60;
        balle = Bitmap.createScaledBitmap(balle,newlballe,newLballe, false);
        // réduction de l'image de la raquette
        int newlraquette = raquette.getWidth()/16;
        int newLraquette = raquette.getHeight()/26;
        raquette = Bitmap.createScaledBitmap(raquette,newlraquette,newLraquette, false);
        // ajout du malus
        malusRaquetteImage = Bitmap.createScaledBitmap(malusRaquetteImage,100,100,false);
        malusVitesseImage = Bitmap.createScaledBitmap(malusVitesseImage,100,100,false);

        // Détecter l'orientation de l'écran
        int rotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getRotation();
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
        raquetteY = (dL *10)/11;  // position  de la raquette en hauteur
        raquetteX = dl/2 - raquette.getWidth()/2; // Position de la raquette au centre de l'écran
        balleX = raquetteX + raquette.getWidth() / 2 - balle.getWidth()/2;// position initiale de la balle
        balleY = raquetteY - balle.getHeight() - 10; // Position de la balle en hauteur

        lballe = balle.getWidth(); // largeur de la balle
        Lballe = balle.getHeight(); // hauteur de la balle
        resistances = new int [1000];

        // gestion de la raquette avec l'inclinaison du téléphone
        if (niveau.equals("hard")) {
            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            if (accelerometer != null) {
                sensorEventListener = new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        float xTilt;

                        if (rotation == Surface.ROTATION_270) {
                            // Utiliser Y pour le mouvement horizontal en mode paysage gauche
                            xTilt = event.values[1];
                        } else if (rotation == Surface.ROTATION_90) {
                            // Utiliser Y pour le mouvement horizontal en mode paysage droit
                            xTilt = -event.values[1];
                        } else {
                            // Utiliser X pour les autres orientations
                            xTilt = event.values[0];
                        }

                        if (!tiltCalibrated) {
                            initialXTilt = xTilt; // Calibrer la position neutre
                            tiltCalibrated = true;
                        }

                        // Ajuster la valeur d'inclinaison par rapport à la référence initiale
                        float adjustedXTilt = xTilt - initialXTilt;

                        if (!mvmBalle) {
                            // Démarrer le jeu seulement après une inclinaison significative
                            if (Math.abs(adjustedXTilt) > 5) {
                                mvmBalle = true;
                            }
                        }

                        // Déplacer la raquette
                        float newRaquetteX = raquetteX - adjustedXTilt * 10; // Sensibilité ajustée
                        if (newRaquetteX <= 0) {
                            raquetteX = 0;
                        } else if (newRaquetteX >= dl - raquette.getWidth()) {
                            raquetteX = dl - raquette.getWidth();
                        } else {
                            raquetteX = newRaquetteX;
                        }
                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {
                        // Non utilisé ici
                    }
                };

                sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_GAME);
                } else {
                    throw new RuntimeException("Accelerometer not available on this device.");
                }
            }
            creerBriques(); //
        }

    private void creerBriques (){ // méthode pour créer les briques
        int lbrique = dL/25; // largeur des briques
        int Lbrique = dl/20; // Longueur des briques
        int nbreRg = niveau.equals("easy") ? 10 : niveau.equals("medium") ? 10 : (niveau.equals("semi-hard") ? 5 : 7); // nb de lignes de briques
        int resistance = 1;
        boolean isHard = niveau.equals("hard");
        // HARD
        Vhoriz = new int [1000];
        directionsRg = new int [nbreRg];

        // dans le niveau challenge, chaque rangée de briques se déplace dans un sens
        if ( isHard){
            for (int rg = 0; rg<nbreRg; rg++) {
                directionsRg[rg] = ( rg % 2 == 0) ? 1 : -1;
            }
        }
        for (int rg = 0; rg<nbreRg; rg++){
            for (int colone = 0; colone <20; colone++){
                int offset = (isHard && rg%2 != 0) ? dl / 2 : 0;

                // Si le niveau est medium, certaines briques deviennent incassables
                boolean incassable = niveau.equals("medium") &&
                        ((rg == 2 && (colone == 1 || colone == 18) ) ||
                                (rg == 3 && (colone == 4 || colone == 15 )) ||
                                (rg == 4 && (colone == 7 || colone == 12 )) ||
                                (rg == 5 && (colone == 9 || colone == 10 )) ||
                                (rg == 6 && (colone == 6 || colone == 13 )) ||
                                (rg == 7 && (colone == 3 || colone == 16 )) ||
                                (rg == 8 && (colone == 0 || colone == 19)));
                boolean genereMalusRaquette = niveau.equals("medium")&&(rg == nbreRg -1 &&(colone %2 ==0 ))||
                        (rg == nbreRg -2 &&(colone ==3 || colone==8 ));
                boolean genereMalusVitesse = niveau.equals("medium")&& (rg == nbreRg-2 && (colone == 3 || colone ==7));
                int typeMalus = genereMalusRaquette ? 1 : (genereMalusVitesse ? 2 : 0);
                incassable = niveau.equals("hard") ? false : incassable;
                if (niveau.equals("semi")){
                    resistance = 1 + (rg % 3);
                } else if (isHard) {
                    resistance = 2;
                Vhoriz[numBriques] = directionsRg[rg];
                }else{
                    Vhoriz [numBriques]= - random.nextInt(10) + 5; // se déplace aléatoirement vers la droite ou la gauche
                }
                if (niveau.equals("hard") && ((rg % 2 == 0 && colone % 2 == 0) || (rg % 2 != 0 && colone % 2 != 1))) {
                    continue; // Ignore cette colonne
                }
                briques[numBriques] = new Brique(rg,colone,lbrique,Lbrique, incassable,(typeMalus !=0),typeMalus,resistance,isHard);
                briques[numBriques].positionX = offset + colone*lbrique;
                numBriques++;
            }
        }
    }
    // Ajout des rangées pour le niveau semi
    private void AjouterNewRg (){
        int lbrique = dL/25; // largeur des briques = largeur de l'écran / 8 pour avoir 8 briques par rangées
        int Lbrique = dl / 20; // Longueur des briques
        int NvRg = 0;

        for(int colone = 0; colone < 20; colone ++){
            int resistance = 1 +((numBriques /20) % 3);
            boolean incassable =false;
            briques[numBriques] = new Brique(NvRg,colone,lbrique,Lbrique,incassable, false,0,resistance,false);
            numBriques++;
        }
    }
    // Changer de manière innatudue la vitesse de la balle
    private void ChangerVitesseBalle (){
        Random random = new Random();
        int choix = random.nextInt(3);
        if(choix == 0){
            // ralentir la balle
            vitesse.setX((int) (vitesse.getX() * 0.7));
            vitesse.setY((int) (vitesse.getY() * 0.7));
            Vaccel = false;
        } else if (choix == 2){
            // ACCélérer la balle
            vitesse.setX((int) (vitesse.getX() * 1.1));
            vitesse.setY((int) (vitesse.getY() * 1.1));
            Vaccel = true;
        } else {
            //Vitesse normale
            vitesse.setX((int) (vitesse.getX() / (Vaccel ? 1.1 : 0.7)));
            vitesse.setY((int) (vitesse.getY() / (Vaccel ? 1.1 : 0.7)));
            Vaccel = false;
        }
    }

    // Activer écran sombre
    private void activerEffetSombre (){
        ecranSombre = true;
        DbeffetSombre = System.currentTimeMillis();
    }
    private void ajouterBallesTemporaires() {
        Random random = new Random();
        for (int i = 0; i < 2; i++) { // Ajouter deux balles
            float startX = random.nextInt(dl - lballe); // Position X aléatoire
            float startY = random.nextInt((int) (raquetteY / 2)); // Position Y aléatoire
            float vitesseX = random.nextFloat() * 10 - 5; // Vitesse X aléatoire
            float vitesseY = random.nextFloat() * 10 + 5; // Vitesse Y aléatoire
            Addballs.add(new Ball(startX, startY, vitesseX, vitesseY));
        }
        ballStime = System.currentTimeMillis(); // Enregistrer le temps de création
    }


    @Override
    protected void onDraw (Canvas canvas ){
        super.onDraw(canvas);

        backgroundBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.fond_arcade2); // fond de la page de la partie
        canvas.drawBitmap(backgroundBitmap,0,0,null);
        int blueColor = Color.parseColor("#012a61");
        int screenWidth = getWidth(); // largeur de l'écran

        // Bords
        Paint borderPaint_b = new Paint();
        borderPaint_b.setColor(Color.DKGRAY); // Couleur de la bordure
        borderPaint_b.setStyle(Paint.Style.STROKE);
        borderPaint_b.setStrokeWidth(10); // Épaisseur de la bordure

        // Créer un Paint pour les contours lumineux
        Paint bordLumieuxPaint = new Paint();
        bordLumieuxPaint.setStyle(Paint.Style.STROKE); // Style contour
        bordLumieuxPaint.setStrokeWidth(5); // Épaisseur du bord
        bordLumieuxPaint.setColor(Color.BLACK); // Couleur lumineuse pour le bord
        bordLumieuxPaint.setShadowLayer(10, 0, 0, Color.WHITE); // Ajout d'ombre pour effet lumineux

        Paint contourPaint = new Paint();
        contourPaint.setColor(Color.BLACK);
        contourPaint.setStyle(Paint.Style.STROKE);
        contourPaint.setStrokeWidth(3); // Épaisseur du contour

        canvas.drawRect(0, 0, screenWidth, headerHeight,new Paint(){{setColor(blueColor);}} ); // Zone bleue en haut de la page
        // Couleur blanche pour le bord
        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.WHITE);

        // Dessinez le bord en bas du rectangle (petit rectangle blanc)
        int borderHeight = 5; // Hauteur du bord blanc
        canvas.drawRect(0, headerHeight - borderHeight, screenWidth, headerHeight, borderPaint);

        if (niveau.equals("hard") && !tiltCalibrated) {
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(60); // Ajustez la taille pour qu'elle soit lisible
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Calibrating...", dl / 2f, dL / 2f, textPaint);
            handler.postDelayed(runnable, UPDATE_MS); // Continuer à rafraîchir l'écran
            return; // Ne dessinez rien d'autre tant que le calibrage n'est pas terminé
        }

        // Affiche le message uniquement au début si le niveau est "challenge"
        if (niveau.equals("hard") && showMoveMessage) {
            Paint messagePaint = new Paint();
            messagePaint.setColor(Color.WHITE); // Couleur du texte
            messagePaint.setTextSize(80); // Taille du texte
            messagePaint.setTextAlign(Paint.Align.CENTER); // Alignement du texte
            canvas.drawText("Move your phone <-->", dl / 2, dL / 2, messagePaint);

            // Désactiver l'affichage du message après un délai (1 seconde par exemple)
            postDelayed(() -> {
                showMoveMessage = false;
                invalidate(); // Rafraîchir l'écran pour effacer le message
            }, 1000);

            return; // Ne dessiner que le message au début
        }

        int [][] CouleursRg = {
                {Color.parseColor("#E48A48"), Color.parseColor("#EF7607")},
                {Color.parseColor("#85B4EC"), Color.parseColor("#004AAD")},
                {Color.parseColor("#32CD32"), Color.parseColor("#006F4A")},
                {Color.parseColor("#E8E789"), Color.parseColor("#FFD700")},
                {Color.parseColor("#EB6856"), Color.parseColor("#EB6856")},
                {Color.parseColor("#5CE1E6"), Color.parseColor("#0079A7")},
                {Color.parseColor("#B273D4"), Color.parseColor("#8050DF")},
                {Color.parseColor("#72CDA7"), Color.parseColor("#C1FF72")},
                {Color.parseColor("#FF83CF"), Color.parseColor("#E441C0")},
                {Color.parseColor("#CED8FF"), Color.parseColor("#CB9DD8")},
        };

        // Ajuster la position de début des briques pour éviter de chevaucher la barre supérieure
        if(mvmBalle){
            balleX += vitesse.getX();
            balleY += vitesse.getY();
        }
        // Dessiner le texte du score
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40);
        canvas.drawText("Score: " + points, 90, 60, textPaint);

        // Dessiner les vies (sous forme de rectangles ou icônes)
        for (int i = 0; i < vies; i++) {
            int rectX = screenWidth - 450 - i * 100;
            if (vies > 0) {
                // Assurez-vous que l'image de cœur est bien chargée depuis les ressources
                Bitmap heartBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.heart);
                // Dessiner l'image à la position spécifiée
                canvas.drawBitmap(heartBitmap, rectX, 15, null);
            }
        }
        canvas.drawBitmap(homeButtonBitmap, dl - 200, 5, null);

        // Dessiner le bouton "Home"
        if (homeButtonBitmap == null) {
            homeButtonBitmap = Bitmap.createScaledBitmap(
                    BitmapFactory.decodeResource(getResources(), R.drawable.home_blanc), 70, 70, false
            );
        }

        if ((balleX >= dl - balle.getWidth()) || balleX <= 0){ // Collision avec les bords
            vitesse.setX(vitesse.getX() * -1); // inverser la direction de la balle
        }
        if (balleY <= headerHeight + borderHeight){ // Collision avec le haut de l'écran
            vitesse.setY(vitesse.getY() * -1); // Inverse la direction de la balle
        }
        if (balleY > raquetteY + raquette.getHeight()) { // si la balle dépasse la raquette
            balleX = raquetteX + raquette.getWidth() / 2 - balle.getWidth()/2;// position initiale de la balle
            balleY = raquetteY - balle.getHeight() - 10; // Position de la balle en hauteur
            if (mprater != null) {
                mprater.start(); // mettre le son de rater
            }
            vitesse.setX(xVitesse()); // Nouvelle vitesse en X
            vitesse.setY(20);  // Réinitialise la vitesse Y // 32
            vies--; // diminue le nombre de vies
            if (vies == 0) { // q'il n'ya pas plus de vies, le jeu est terminé
                gameOver = true;
                LancerGameOver();
            }
        }
        // On gère ici la collision entre la balle et la raquette
        if((balleX + balle.getWidth() >= raquetteX )
                && (balleX <= raquetteX + raquette.getWidth())
                && (balleY + balle.getHeight() >= raquetteY)
                && (balleY <= raquetteY + raquette.getHeight())){
            if ( mpfrapper != null){
                mpfrapper.start();
            }
            vitesse.setY(-Math.abs(vitesse.getY() )); // Inverser la direction de la balle en Y
        }

        // Dessiner la balle et la raquette
        canvas.drawBitmap(balle,balleX,balleY,null);
        canvas.drawBitmap(raquette, raquetteX,raquetteY,null);

        // MALUS
        if (niveau.equals("medium")) {
            Iterator<Malus> iterator = malusList.iterator();
            while (iterator.hasNext()) {
                Malus malus = iterator.next();
                if (malus.visible) {
                    canvas.drawBitmap(malus.image, malus.x, malus.y, null);
                    malus.descendre();

                    if (malus.verifCollision(raquetteX, raquetteY, raquette.getWidth(), raquette.getHeight())) {
                        malus.visible = false;
                        if (malus.type == 1) {
                            raquette = Bitmap.createScaledBitmap(raquette, raquette.getWidth() - 20, raquette.getHeight(), false);
                        } else if (malus.type == 2) {
                            vitesse.setX((int) (vitesse.getX() * 1.2f));
                            vitesse.setY((int) (vitesse.getY() * 1.2f));
                        }
                    }
                    if (malus.y > dL) {
                        iterator.remove();
                    }
                }
            }
        }

        for (int i = 0; i < numBriques; i++) {
            if (briques[i].getVisibility()) {
                int rangée = briques[i].rg;

                // Appliquer le décalage pour les briques mobiles au niveau "hard"
                int positionX = niveau.equals("hard") ? (int) briques[i].positionX : briques[i].colone * briques[i].l;
                int positionY = headerHeight + borderHeight + briques[i].rg * briques[i].L;

                int left = positionX;
                int top = positionY;
                int right = positionX + briques[i].l;
                int bottom = positionY + briques[i].L;

                // Dessiner les briques selon leur type et niveau
                if (briques[i].isIncassable()) {
                    briquePaint.setColor(Color.DKGRAY);
                    canvas.drawRect(left+5, top+5, right-5, bottom-5, borderPaint_b); // Dessiner les bords de la brique
                    canvas.drawRect(left, top, right, bottom, contourPaint); // Contour de la brique
                    briquePaint.setShader(null);

                } else if (niveau.equals("semi")) {
                    int couleur = briques[i].getCouleur();
                    briquePaint.setColor(couleur);
                    briquePaint.setShader(null);
                    canvas.drawRect(left, top, right, bottom, contourPaint); // contour noir autour de la brique

                } else {
                    int Couleurdebut = CouleursRg[rangée % CouleursRg.length][0];
                    int Couleurfin = CouleursRg[rangée % CouleursRg.length][1];
                    LinearGradient gradient = new LinearGradient(
                            left,
                            top,
                            right,
                            bottom,
                            Couleurdebut,
                            Couleurfin,
                            Shader.TileMode.CLAMP
                    );
                    briquePaint.setShader(gradient);

                    // Dessiner les bords lumineux
                    bordLumieuxPaint.setStyle(Paint.Style.STROKE);
                    bordLumieuxPaint.setStrokeWidth(5); // Ajuster l'épaisseur du bord lumineux à 5
                    bordLumieuxPaint.setColor(Color.BLACK); // bord lumineux noir
                    canvas.drawRect(left, top, right, bottom, bordLumieuxPaint); // Bord lumineux autour de la brique
                }

                // Dessiner la brique elle-même
                canvas.drawRect(
                        left + 1,
                        top + 1,
                        right - 1,
                        bottom - 1,
                        briquePaint
                );
            }
        }

        // On gère ici la collision entre la balle et la raquette
        boolean Unecollision = false;
        for(int i=0; i<numBriques; i++){
            if(briques[i].getVisibility() && !Unecollision){
                /*boolean collision = balleX + lballe >= briques[i].colone * briques[i].l
                        && balleX <= briques[i].colone * briques[i].l + briques[i].l
                        && balleY <= headerHeight + borderHeight + briques[i].rg * briques[i].L + briques[i].L
                        && balleY + Lballe >= headerHeight + borderHeight + briques[i].rg * briques[i].L;
                */
                boolean collision;
                if (niveau.equals("hard")) {
                    collision = balleX + lballe >= briques[i].positionX
                            && balleX <= briques[i].positionX + briques[i].l
                            && balleY + Lballe >=  headerHeight + borderHeight+briques[i].positionY
                            && balleY <= headerHeight + borderHeight+briques[i].positionY + briques[i].L;
                }else {
                    collision=  balleX + lballe >= briques[i].colone * briques[i].l
                            && balleX <= briques[i].colone * briques[i].l + briques[i].l
                            && balleY <= headerHeight + borderHeight + briques[i].rg * briques[i].L + briques[i].L
                            && balleY + Lballe >= headerHeight + borderHeight + briques[i].rg * briques[i].L;}

                if(collision){
                    //medium
                    if (briques[i].isIncassable()){
                        // La brique est incassable, déterminer le côté de la collision et ajuster en conséquence
                        if (balleY + Lballe - vitesse.getY() <= headerHeight + borderHeight + briques[i].rg * briques[i].L) {
                            // Collision par le dessus
                            balleY = headerHeight + borderHeight + briques[i].rg * briques[i].L - Lballe;
                            vitesse.setY(-Math.abs(vitesse.getY())); // Inverser Y vers le haut
                        } else if (balleY - vitesse.getY() >= headerHeight + borderHeight + briques[i].rg * briques[i].L + briques[i].L) {
                            // Collision par le dessous
                            balleY = headerHeight + borderHeight + briques[i].rg * briques[i].L + briques[i].L;
                            vitesse.setY(Math.abs(vitesse.getY())); // Inverser Y vers le bas
                        } else if (balleX + lballe - vitesse.getX() <= briques[i].colone * briques[i].l) {
                            //Collision par la gauche
                            balleX = briques[i].colone * briques[i].l - lballe;
                            vitesse.setX(-Math.abs(vitesse.getX())); // Inverser X vers la gauche
                        } else if (balleX - vitesse.getX() >= briques[i].colone * briques[i].l + briques[i].l) {
                            balleX = briques[i].colone * briques[i].l + briques[i].l;
                            vitesse.setX(Math.abs(vitesse.getX())); // Inverser X vers le droite
                        }
                        // vérifie si la balle est entre deux briques incassables

                        if (mpfrapper != null) {
                            mpcasser.start();
                        }
                    } else {
                        if (niveau.equals("semi")) {
                            briques[i].diminuerResistance();
                            if (briques[i].getResistance() <= 0){
                                points += 10;
                                brokenBriques++;
                            }
                        } else {
                            briques[i].setInvisible(); // Cacher la brique
                            points += 10; // augmenter le score de 10 POINTS
                            brokenBriques++; // Incrémenter le compteur de briques cassées
                        }
                        if (briques[i].genereMalus){
                            Bitmap malusImage = briques[i].typeMalus == 1 ? malusRaquetteImage : malusVitesseImage;
                            Malus nouveauMalus = new Malus(
                                    briques[i].colone * briques [i].l / 2 - malusImage.getWidth()/2,
                                    briques[i].rg * briques[i].L,
                                    malusImage,
                                    briques[i].typeMalus
                            );
                            malusList.add(nouveauMalus);
                        }
                        Unecollision = true;
                    }
                    vitesse.setY(-vitesse.getY()); // Inverser et augmenter la vitesse en Y
                }
            }
        }
        // Niveau Semi hard
        if (niveau.equals("semi") && System.currentTimeMillis() - Tfin > Int_Descente) {
            Tfin = System.currentTimeMillis();
            for (int i = 0; i < numBriques; i++) {
                if (briques[i].getVisibility()) {
                    briques[i].rg++;
                    if (briques[i].rg * briques[i].L >= raquetteY) {
                        gameOver = true;
                        LancerGameOver();
                        return;
                    }
                }
            }
            // ajouter une nouvelle rangée
            AjouterNewRg();
        }
        /// NIVEAU HARD
        if(niveau.equals("hard")){
            for (int i = 0; i < numBriques; i++) {
                if (briques[i].getVisibility()) {

                    int direction = directionsRg[briques[i].rg];

                    if (briques[i].colone % 2 == 0) {
                        briques[i].positionX += direction * VbriqueHoriz;
                    } else {
                        briques[i].positionX += direction * (VbriqueHoriz / 2);
                    }
                    // Vérifier si la brique dépasse les bords et la réinsérer
                    if (briques[i].positionX <= 0) {
                        // briques[i].positionX = dl;
                        directionsRg[briques[i].rg] = 1;
                        briques[i].positionX = 0;

                    } else if (briques[i].positionX + briques[i].l > dl) {
                        directionsRg[briques[i].rg] = -1;
                        briques[i].positionX = dl - briques[i].l;
                    }
                }
                // Activer l'effet sombre à des intervalles de temps réguliers
                long tempsActuel = System.currentTimeMillis();

                if(tempsActuel - Lasteffet > INT_effet && !ecranSombre){
                    ecranSombre = true;
                    DbeffetSombre = tempsActuel; // Enregistrer le début de l'effet sombre
                    Lasteffet = tempsActuel;
                }
                // Appliquer l'effet sombre si actif
                if(ecranSombre){
                    // Calcul de la durée écoulée depuis le début de l'effet
                    long tempsEffet = tempsActuel-DbeffetSombre;
                    // Si l'effet est toujours en cours, dessiner l'écran sombre
                    if(tempsEffet < Duree_effetSombre){
                        Paint darkPaint = new Paint();
                        darkPaint.setColor(Color.BLACK);
                        darkPaint.setAlpha(150); // Opacité (0 = transparent, 255 = opaque)
                        canvas.drawRect(0, 0, dl, dL, darkPaint);
                    } else {
                        ecranSombre = false;
                    }
                }
                if(points == 100 && tempsActuel - LastChangement > Int_vitesse){
                    ChangerVitesseBalle();
                    LastChangement = tempsActuel;
                }
            }
        }

        if (gagner) {
            // Arrêter les mises à jour de l'écran
            handler.removeCallbacksAndMessages(null);

            // Rendre l'écran sombre
            Paint darkPaint = new Paint();
            darkPaint.setColor(Color.BLACK);
            darkPaint.setAlpha(150); // Opacité (0 = transparent, 255 = opaque)
            canvas.drawRect(0, 0, dl, dL, darkPaint);

            // Dessiner l'écran de victoire
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setTextSize(80);
            paint.setTextAlign(Paint.Align.CENTER);

            // Afficher la coupe au centre de l'écran
            Bitmap trophy = BitmapFactory.decodeResource(getResources(), R.drawable.record);
            int trophyWidth = 500; // Largeur de la coupe
            int trophyHeight = 500; // Hauteur de la coupe
            trophy = Bitmap.createScaledBitmap(trophy, trophyWidth, trophyHeight, false);
            canvas.drawBitmap(trophy, dl / 2f - trophyWidth / 2, dL / 2f - trophyHeight / 2 - 50, null);

            // Afficher le message de victoire
            canvas.drawText("Félicitations !", dl / 2f, dL / 2f + 100, paint);
            canvas.drawText("Vies restantes : " + vies, dl / 2f, dL / 2f + 200, paint);

            return; // Arrêter le reste du dessin
        }

        if(niveau.equals("medium")){
            if(brokenBriques == numBriques - 14){//8 oumy
                gagner = true;
            }
        } else if (niveau.equals("easy")) {
            if (brokenBriques == numBriques){
                gagner = true;
            }
        } else if (niveau.equals("semi")) {
            if (points == 5000){
                gagner = true;
            }
        } else if (niveau.equals("hard")) {
            if (brokenBriques == numBriques){
                gagner = true;
            }
        }
        if (!gameOver){
            handler.postDelayed(runnable, UPDATE_MS); // rafraichir l'écran si le jeu n'est pas terminé
        }
    }

    @Override
    // on gère les évènements tactiles pour déplacer la raquette
    public boolean onTouchEvent(MotionEvent event){
        float toucheX = event.getX(); // coordonnées de X au toucher
        float toucheY = event.getY(); //coordonnées de Y au toucher

        // Vérifiez si le clic est dans la zone du bouton "home"
        if (toucheX >= dl-200 && toucheX <= dl-200 + homeButtonBitmap.getWidth() &&
                toucheY >= 15 && toucheY <= 15 + homeButtonBitmap.getHeight()) {
            // Quittez la partie ou retournez à l'écran d'accueil
            quitterPartie();
            //return true;
        }
        if (niveau.equals("hard")) {
            if (!mvmBalle && tiltCalibrated && event.getAction() == MotionEvent.ACTION_DOWN) {
                mvmBalle = true; // Lancer la balle après que l'utilisateur touche l'écran
            }
            return true; // Désactiver le contrôle tactile dans le niveau "hard"
        }
        if(!mvmBalle && event.getAction()==MotionEvent.ACTION_DOWN){
            mvmBalle = true;
        }

        if ( toucheY >= raquetteY){ // Si on touche en dessous de la raquette
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN){ // Appui initial
                oldX = event.getX(); // Enregistrer la position X initiale au toucher
                oldRaquetteX = raquetteX; // Enregistrer la position initiale de la raquette
            }
            if (action == MotionEvent.ACTION_MOVE){ // Mouvement du doigt
                float shift =  oldX - toucheX; // Calculer le déplacement
                float NouvRaquetteX = oldRaquetteX - shift; // Calculer la nouvelle position de la raquette
                if (NouvRaquetteX <= 0) // Empêcher la raquette de sortir de l'écran à gauche
                    raquetteX = 0;
                else if (NouvRaquetteX >= dl - raquette.getWidth()) // Empêcher la raquette de sortir de l'écran à droite
                    raquetteX = dl - raquette.getWidth();
                else
                    raquetteX = NouvRaquetteX; // mettre à jour la position de la raquette
            }
        }

        return true;
    }

    private void LancerGameOver() {
        handler.removeCallbacksAndMessages(null);
        Intent intent = new Intent(context, GameOver.class);
        intent.putExtra("points", points);
        ((Activity) context).startActivity(intent);
        ((Activity) context).finish();
    }

    private void quitterPartie() {
        // Retourner à l'écran principal ou quitter l'activité
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
        ((Activity) context).finish();
    }

    private int xVitesse (){
        int [] valeurs = {-35, -30, -25, 30, 35}; // Valeurs possibles de vitesse
        int index = random.nextInt(5); //
        return valeurs [index];
    }
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (niveau.equals("hard") && sensorManager != null) {
            sensorManager.unregisterListener(sensorEventListener);
        }
    }
}
