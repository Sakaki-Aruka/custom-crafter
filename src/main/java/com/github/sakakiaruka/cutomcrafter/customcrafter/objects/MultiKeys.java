package com.github.sakakiaruka.cutomcrafter.customcrafter.objects;

public class MultiKeys {

    private int key1;
    private int key2;
    public MultiKeys(int key1,int key2){
        this.key1 = key1;
        this.key2 = key2;
    }

    public String getKeys(){
        return key1+","+key2;
    }

    public int getKey1(){
        return key1;
    }

    public int getKey2(){
        return key2;
    }

    public boolean same(MultiKeys target){
        if(this.key1!=target.getKey1())return false;
        if(this.key2!=target.getKey2())return false;
        return true;
    }
}
