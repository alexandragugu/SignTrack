package com.example.demo.utils;

public class SignaturePosition {
    int originX;
    int originY;
    float baseX;
    float baseY;

    public int getOriginX() {
        return originX;
    }

    public void setOriginX(int originX) {
        this.originX = originX;
    }

    public int getOriginY() {
        return originY;
    }

    public void setOriginY(int originY) {
        this.originY = originY;
    }

    public void increaseX() {
        this.originX += 1;
    }

    public void increaseY() {
        this.originY += 1;
    }

    public float getBaseX() {
        return baseX;
    }

    public void setBaseX(float baseX) {
        this.baseX = baseX;
    }

    public float getBaseY() {
        return baseY;
    }

    public void setBaseY(float baseY) {
        this.baseY = baseY;
    }
}
