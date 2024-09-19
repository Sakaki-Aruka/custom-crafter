package com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Coordinate)) return false;
        Coordinate that = (Coordinate) o;
        return x == that.x && y == that.y;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = result * 31 + Integer.hashCode(x);
        result = result * 31 + Integer.hashCode(y);
        return result;
    }
}
