package com.example.helloworld;

public class Vitesse {
    private int x,y;
    public Vitesse (int x, int y){
        this.x = x;
        this.y = y;
    }
    // récupérer la valeur de X
    public int getX(){
        return x;
    }
    // initialiser la valeur de X
    public void setX( int x){
        this.x = x;
    }

    // récupérer la valeur de Y
    public int getY(){
        return y;
    }
    // initialiser la valeur de Y
    public void setY(int y){
        this.y = y;
    }

}
