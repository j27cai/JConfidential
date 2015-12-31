package com.example.jazzconfidential.jazzconfidential.Game;

/**
 * Created by simonvilleneuve on 15-06-07.
 */
public class Coordinate implements java.io.Serializable {
    int x;
    int y;

    public Coordinate(int X, int Y) {
        this.x = X;
        this.y = Y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
