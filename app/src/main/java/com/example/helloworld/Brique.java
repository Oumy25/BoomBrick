package com.example.helloworld;

public class Brique {
    // variables pour définir les briques
    private boolean isVisible;
    public int rg, colone,l,L;

    public Brique (int rg, int colone, int L, int l ){
        isVisible = true;
        this.rg = rg;
        this.l = l;
        this.L = L;
        this.colone = colone;
    }
    public void setInvisible(){
        isVisible = false;
    }
    public boolean getVisibility(){
       return isVisible;
    }
}
