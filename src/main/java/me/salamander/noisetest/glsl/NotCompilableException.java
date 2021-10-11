package me.salamander.noisetest.glsl;

public class NotCompilableException extends RuntimeException{
    public NotCompilableException(){
        super();
    }

    public NotCompilableException(String message){
        super(message);
    }

    public NotCompilableException(String message, Exception other){
        super(message, other);
    }
}
