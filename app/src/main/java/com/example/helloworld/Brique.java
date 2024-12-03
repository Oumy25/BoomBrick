package com.example.helloworld;

import android.graphics.Color;

public class Brique {
    // variables pour définir les briques
    private boolean isVisible;
    private boolean incassable;
    public int rg, colone,l,L;
    public boolean genereMalus;
    int typeMalus;
    int resistance;
    public float positionX;
    public float positionY;


    public Brique (int rg, int colone, int L, int l, boolean b,boolean genereMalus,int typeMalus,int resistance,boolean isHard){
        isVisible = true;
        this.incassable = b;
        this.rg = rg;
        this.l = l;
        this.L = L;
        this.colone = colone;
        this.genereMalus = genereMalus;
        this.typeMalus = typeMalus;
        this.resistance = resistance;
        if (isHard){
            this.positionX = colone * l;
            this.positionY = rg*L;
        }
    }
    public void setInvisible(){
        if (!incassable){ // rendre invisible que si la brique est cassable
            isVisible = false;
        }
    }
    public boolean getVisibility(){
       return isVisible;
    }
    public boolean isIncassable (){
        return incassable;
    }
    public int getTypeMalus (){
        return typeMalus;
    }
    public void diminuerResistance (){
        if ( !incassable && resistance > 0){
            resistance--;
        }
        if (resistance == 0){
            setInvisible();
        }

    }
    public int getResistance (){
        return resistance ;

    }
    public int getCouleur (){
        switch (resistance){
            case 3:
                return Color.parseColor("#EB5E0C");
            case 2:
                return Color.parseColor("#ED8F57");
            case 1:
                return Color.parseColor("#EDC1A8");
            default:
                return Color.GRAY;
        }

    }

}
