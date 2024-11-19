package com.example.helloworld;

import android.graphics.Bitmap;

public class Malus {
    float x, y;
    int L, l;
    boolean visible;
    int type;
    Bitmap image;
    public Malus (float x, float y, Bitmap image,int type){
        this.x = x;
        this.y = y;
        this.image = image;
        this.l = image.getWidth();
        this.L = image.getHeight();
        this.visible = true;
        this.type = type;
    }
    public void descendre(){
        y +=10; // vitesse de descente
    }
    public boolean verifCollision (float raquetteX, float raquetteY, float lraquette, float Lraquette){
        return visible &&
                x + image.getWidth()>= raquetteX &&
                x <= raquetteX + lraquette &&
                y+ image.getHeight() >= raquetteY &&
                y <= raquetteY + Lraquette;
    }
}
