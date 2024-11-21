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
import android.media.MediaPlayer;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.os.Handler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

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
    private final long Int_Descente = 3000;
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

    public GameView(Context context, String niveau){
        super(context);
        this.context = context;
        // Stocke le niveau de jeu
        this.niveau = niveau;

        if (niveau.equals("medium")){
            vitesse = new Vitesse(25, 30);
        } else {
            vitesse = new Vitesse(25,32);
        }

        // Charger l'image de la balle et de la raquette
        balle = BitmapFactory.decodeResource(getResources(), R.drawable.ballerouge);
        raquette = BitmapFactory.decodeResource(getResources(), R.drawable.raquette);
        malusRaquetteImage = BitmapFactory.decodeResource(getResources(),R.drawable.malus_raquette);
        malusVitesseImage = BitmapFactory.decodeResource(getResources(),R.drawable.ballemalus);
        //réduction de l'image de la balle
        int newlballe = balle.getWidth()/40;
        int newLballe = balle.getHeight()/40;
        balle = Bitmap.createScaledBitmap(balle,newlballe,newLballe, false);
        // réduction de l'image de la raquette
        int newlraquette = raquette.getWidth()/16;
        int newLraquette = raquette.getHeight()/26;
        raquette = Bitmap.createScaledBitmap(raquette,newlraquette,newLraquette, false);
        // ajout du malus

        malusRaquetteImage = Bitmap.createScaledBitmap(malusRaquetteImage,150,150,false);
        malusVitesseImage = Bitmap.createScaledBitmap(malusVitesseImage,150,150,false);

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
        raquetteY = (dL *4)/5;  // position  de la raquette en hauteur
        raquetteX = dl/2 - raquette.getWidth()/2; // Position de la raquette au centre de l'écran
        balleX = raquetteX + raquette.getWidth() / 2 - balle.getWidth()/2;// position initiale de la balle
        balleY = raquetteY - balle.getHeight() - 10; // Position de la balle en hauteur

        lballe = balle.getWidth(); // largeur de la balle
        Lballe = balle.getHeight(); // hauteur de la balle
        resistances = new int [1000];

        creerBriques(); //
    }

    private void creerBriques (){ // méthode pour créer les briques
        int lbrique = dL/25; // largeur des briques = largeur de l'écran / 8 pour avoir 8 briques par rangées
        int Lbrique = dl / 20; // Longueur des briques
        int nbreRg = niveau.equals("medium") ? 10 : (niveau.equals("semi-hard") ? 5 : 7);
        int resistance = 1;
        boolean isHard = niveau.equals("hard");
        // HARD
        Vhoriz = new int [1000];

        for (int colone = 0; colone <20; colone++){
            for (int rg = 0; rg<nbreRg; rg++){

                // Si le niveau est medium, certaines briques de la 2e rangée deviennent incassables
                boolean incassable = niveau.equals("medium") && ((rg == 1 || rg ==4 || rg ==7)  && ((colone >=0 && colone <= 8)|| colone >=12 && colone <= 16));
                boolean genereMalusRaquette = niveau.equals("medium")&&(rg == nbreRg -1 &&(colone %2 ==0 ))||
                        (rg == nbreRg -2 &&(colone ==3 || colone==8 ));
                boolean genereMalusVitesse = niveau.equals("medium")&& (rg == nbreRg-2 && (colone == 3 || colone ==7));
                int typeMalus = genereMalusRaquette ? 1 : (genereMalusVitesse ? 2 : 0);
                incassable = niveau.equals("hard") ? false : incassable;
            if (niveau.equals("semi")){
                    resistance = 1 + (rg % 3);
                } else if (isHard) {
                    resistance = 2;
                Vhoriz[numBriques] = random.nextInt(10) + 5; // Vitesse entre 5 et 10
                if (random.nextBoolean()) {
                    Vhoriz [numBriques]= - Vhoriz [numBriques]; // se déplace aléatoirement vers la droite ou la gauche
                }
            }
                //resistances[numBriques]=resistance;

                briques[numBriques] = new Brique(rg,colone,lbrique,Lbrique, incassable,(typeMalus !=0),typeMalus,resistance,isHard);

                //briques[numBriques].positionX = colone*lbrique;
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
            vitesse.setX((int) (vitesse.getX() * 1.5));
            vitesse.setY((int) (vitesse.getY() * 1.5));
            Vaccel = true;
        } else {
            //Vitesse normale
            vitesse.setX((int) (vitesse.getX() / (Vaccel ? 1.5 : 0.7)));
            vitesse.setY((int) (vitesse.getY() / (Vaccel ? 1.5 : 0.7)));
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
    private void gererBallesTemperatures(Canvas canvas){
        long tempsActuel = System.currentTimeMillis();

        // Vérifier les déclencheurs pour ajouter de nouvelles balles
        if (points % SEUIL_SCORE == 0 && points > 0 && Addballs.isEmpty()) {
            ajouterBallesTemporaires();
        } else if (tempsActuel - lastBallTime > INT_ball && Addballs.isEmpty()) {
            ajouterBallesTemporaires();
            lastBallTime = tempsActuel;
        }

        // Mise à jour et dessin des balles temporaires
        Iterator<Ball> iterator = Addballs.iterator();
        while (iterator.hasNext()) {
            Ball ball = iterator.next();

            // Mise à jour des positions
            ball.x += ball.vitesseX;
            ball.y += ball.vitesseY;

            // Collision avec les bords
            if (ball.x <= 0 || ball.x + lballe >= dl) {
                ball.vitesseX *= -1; // Rebondir sur les bords
            }
            if (ball.y <= 0) {
                ball.vitesseY *= -1; // Rebondir sur le haut
            }

            // Collision avec la raquette
            if (ball.x + lballe >= raquetteX && ball.x <= raquetteX + raquette.getWidth() &&
                    ball.y + Lballe >= raquetteY && ball.y <= raquetteY + raquette.getHeight()) {
                iterator.remove(); // Supprimer la balle si elle est touchée
                continue;
            }

            // Si la balle tombe hors de l'écran
            if (ball.y > dL) {
                iterator.remove(); // Supprimer la balle
                vies--; // Perte d'une vie
                if (vies == 0) {
                    gameOver = true;
                    LancerGameOver();
                    return;
                }
            }

            // Dessiner la balle
            canvas.drawBitmap(balle, ball.x, ball.y, null);
        }

        // Supprimer les balles temporaires après leur durée de vie
        if (tempsActuel - ballStime > dureeBall) {
            Addballs.clear();
        }
    }

    @Override
    protected void onDraw (Canvas canvas ){
        super.onDraw(canvas);
        canvas.drawColor(Color.BLACK); // Fond
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
        if(mvmBalle){
            balleX += vitesse.getX();
            balleY += vitesse.getY();
        }



        if ((balleX >= dl - balle.getWidth()) || balleX <= 0){ // Collision avec les bords
            vitesse.setX(vitesse.getX() * -1); // inverser la direction de la balle
        }
        if (balleY <= 0){ // Collision avec le haut de l'écran
            vitesse.setY(vitesse.getY() * -1); // Inverse la direction de la balle
        }
        if (balleY > raquetteY + raquette.getHeight()) { // si la balle dépasse la raquette
            balleX = raquetteX + raquette.getWidth() / 2 - balle.getWidth()/2;// position initiale de la balle
            balleY = raquetteY - balle.getHeight() - 10; // Position de la balle en hauteur
            if (mprater != null) {
                mprater.start(); // mettre le son de rater
            }
            vitesse.setX(xVitesse()); // Nouvelle vitesse en X
            vitesse.setY(32);  // Réinitialise la vitesse Y
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

        //Dessiner les briques
            /*for(int i=0; i<numBriques;i++){
                if(briques[i].getVisibility()){
                    if(briques[i].isIncassable()){
                        briquePaint.setColor(Color.GRAY);
                    } else {
                        briquePaint.setColor(Color.CYAN);
                    }*/
        for(int i=0; i<numBriques;i++){
            if(briques[i].getVisibility()){
                int rangée = briques[i].rg;
                // appliquer le décalage pour niveau hard
                int positionX = niveau.equals("hard") ? (int) briques[i].positionX : briques[i].colone * briques[i].l;
                int positionY = briques[i].rg * briques[i].L;
                /*if (niveau.equals("hard")){
                    positionX += decalageHorizontal;
                }*/

                if(briques[i].isIncassable()){
                    briquePaint.setColor(Color.GRAY);
                    briquePaint.setShader(null);
                } else if (niveau.equals("semi")){
                    int couleur  = briques[i].getCouleur();
                    briquePaint.setColor(couleur);
                    briquePaint.setShader(null);
                }
                else {
                    int Couleurdebut = CouleursRg[rangée %CouleursRg.length][0];
                    int Couleurfin = CouleursRg[rangée %CouleursRg.length][1];

                    LinearGradient gradient = new LinearGradient(
                            /*briques[i].colone * briques[i].l,
                            briques[i].rg * briques[i].L,
                            briques[i].colone * briques[i].l + briques[i].l,
                            briques[i].rg * briques[i].L + briques[i].L,*/
                            positionX,
                            positionY,
                            positionX + briques[i].l,
                            positionY + briques[i].L,
                            Couleurdebut,
                            Couleurfin,
                            Shader.TileMode.CLAMP
                    );
                    briquePaint.setShader(gradient);
                }
                canvas.drawRect(
                        /*briques[i].colone * briques[i].l +1,
                        briques[i].rg * briques[i].L + 1,
                        briques [i].colone * briques[i].l + briques[i].l -1,
                        briques[i].rg * briques[i].L + briques[i].L -1,*/
                        positionX +1,
                        positionY,
                        positionX + briques[i].l -1,
                        positionY + briques[i].L -1,
                        briquePaint
                );

            }
        }
        // Afficher le score et la barre des vies
        canvas.drawText(""+ points, 20, text_size,textPaint);
        if(vies == 2){ // S'il reste 2 vies on change la couleur de la barre des vies en jaune
            viePaint.setColor(Color.YELLOW);
        } else if (vies ==1) { // S'il reste 1 vie, la couleur de la barre de vie devient rouge
            viePaint.setColor(Color.RED);
        }
        canvas.drawRect(dl-200,30,dl-200 + 60 * vies, 80,viePaint);

        // On gère ici la collision entre la balle et la raquette
        boolean Unecollision = false;
        for(int i=0; i<numBriques; i++){
            if(briques[i].getVisibility() && !Unecollision){
                boolean collision = balleX + lballe >= briques[i].colone * briques[i].l
                        && balleX <= briques[i].colone * briques[i].l + briques[i].l
                        && balleY <= briques[i].rg * briques[i].L + briques[i].L
                        && balleY + Lballe >= briques[i].rg * briques[i].L;
                if(collision){
                    //medium
                    if (briques[i].isIncassable()){
                        // La brique est incassable, déterminer le côté de la collision et ajuster en conséquence
                        if (balleY + Lballe - vitesse.getY() <= briques[i].rg * briques[i].L) {
                            // Collision par le dessus
                            balleY = briques[i].rg * briques[i].L - Lballe;
                            vitesse.setY(-Math.abs(vitesse.getY())); // Inverser Y vers le haut
                        } else if (balleY - vitesse.getY() >= briques[i].rg * briques[i].L + briques[i].L) {
                            // Collision par le dessous
                            balleY = briques[i].rg * briques[i].L + briques[i].L;
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
                    // Mettre à jour la position horizontale réelle
                    briques[i].positionX += Vhoriz[i];

                    // Vérifier si la brique dépasse les bords et la réinsérer
                    if (briques[i].positionX + briques[i].l < 0) {
                        briques[i].positionX = dl;
                    } else if (briques[i].positionX > dl) {
                        briques[i].positionX = -briques[i].l;
                    }

                    // Mettre à jour la colonne en fonction de la nouvelle position
                    briques[i].colone = (int) (briques[i].positionX / briques[i].l);
                }

                // Activer l'effet sombre à des intervalles de temps réguliers
                long tempsActuel = System.currentTimeMillis();
                gererBallesTemperatures(canvas);

                if(tempsActuel - Lasteffet > INT_effet && !ecranSombre){
                    activerEffetSombre();
                    Lasteffet = tempsActuel;
                }
                // Vérifier si l'effet sombre est actif
                if(ecranSombre){
                    // Calcul de la durée écoulée depuis le début de l'effet
                    long tempsEffet = tempsActuel-DbeffetSombre;
                    if(tempsEffet < Duree_effetSombre){
                        Paint paintSombre = new Paint();
                        paintSombre.setColor(Color.BLACK);
                        paintSombre.setAlpha(100);
                        canvas.drawRect(0,0,dl,dL,paintSombre); // Couvrir tout l'écran

                        // Réduire la taille de l'écran
                        float scaleFactor = 0.9f; // 90% de la taille originale
                        canvas.save(); // Sauvegarder l'état initial du canvas
                        canvas.scale(scaleFactor, scaleFactor, dl / 2f, dL / 2f); // Ré


                    } else {
                        ecranSombre = false;
                    }
                }
                if(ecranSombre && (System.currentTimeMillis() - DbeffetSombre)< Duree_effetSombre){
                    canvas.restore();
                }

                if(tempsActuel - LastChangement > Int_vitesse){
                    ChangerVitesseBalle();
                    LastChangement = tempsActuel;
                }
            }
            }

        if(niveau.equals("medium")){
            if(brokenBriques == numBriques - 8){
                gameOver = true;
                LancerGameOver();
            }
        } else if (niveau.equals("easy")) {
            if (brokenBriques == numBriques){
                gameOver = true;
                LancerGameOver();
            }
        }
        if (!gameOver){
            handler.postDelayed(runnable, UPDATE_MS); // rafraichir l'écran si le jeu n'est pas terminé
        }
    }

    @Override
    // on gère les évènements tactiles pour déplacer la raquette
    public boolean onTouchEvent(MotionEvent event){
        if(!mvmBalle && event.getAction()==MotionEvent.ACTION_DOWN){
            mvmBalle = true;
        }
        float toucheX = event.getX(); // coordonnées de X au toucher
        float toucheY = event.getY(); //coordonnées de Y au toucher
        if ( toucheY >= raquetteY){ // Si on touche en dessous de la raquette
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN){ // Appui initial
                oldX = event.getX(); // Enregistrer la position X initiale au toucher
                oldRaquetteX = raquetteX; // Enregistrer la position initiale de la raquette
            }
            if (action == MotionEvent.ACTION_MOVE){ // Mouvement du doigt
                float shift =  oldX - toucheX; // Calcuyler le déplacement
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

    private int xVitesse (){
        int [] valeurs = {-35, -30, -25, 30, 35}; // Valeurs possibles de vitesse
        int index = random.nextInt(5); //
        return valeurs [index];

    }

}
// commentaire juste pour commit
