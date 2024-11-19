package com.example.helloworld;

public class Brique {
    // variables pour définir les briques
    private boolean isVisible;
    private boolean incassable;
    public int rg, colone,l,L;
    public boolean genereMalus;
    int typeMalus;

    public Brique (int rg, int colone, int L, int l, boolean b,boolean genereMalus,int typeMalus){
        isVisible = true;
        this.incassable = b;
        this.rg = rg;
        this.l = l;
        this.L = L;
        this.colone = colone;
        this.genereMalus = genereMalus;
        this.typeMalus = typeMalus;

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
}
