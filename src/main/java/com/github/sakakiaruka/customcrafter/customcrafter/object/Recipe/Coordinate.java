package com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe;

public class Coordinate {

    public static final Coordinate NULL_ANCHOR = new Coordinate(Integer.MIN_VALUE, Integer.MIN_VALUE);
    public static final Coordinate NON_REQUIRED_ANCHOR = new Coordinate(Integer.MAX_VALUE, Integer.MAX_VALUE);

    private int x;
    private int y;
    public Coordinate(int x,int y){
        this.x = x;
        this.y = y;
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

    public boolean isSame(Coordinate coordinate){
        if(x != coordinate.getX())return false;
        if(y != coordinate.getY())return false;
        return true;
    }

    @Override
    public String toString() {
        return "[x=" + x + ",y=" + y +"]";
    }
}
