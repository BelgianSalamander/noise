package me.salamander.ourea.glsl.transpile;

public class TestMethods {
    public void testOne(){
        for(int i = 0; i < 10; i++){
            System.out.println("testOne");
        }
    }

    public void testTwo(int a, int b){
        if(a == b || a > b){
            System.out.println("testTwo");
        }
    }

    public void testThree(long a, long b){
        if(a == b || a > b){
            System.out.println("testThree");
        }
    }
}
