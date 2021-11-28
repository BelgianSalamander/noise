package me.salamander.ourea.render;

public class FirstClicked {
    private final Window window;
    private final int key;
    private boolean clicked = false;

    public FirstClicked(Window window, int key){
        this.window = window;
        this.key = key;
    }

    public boolean wasClicked(){
        if(window.isKeyPressed(key)){
            if(!clicked){
                clicked = true;
                return true;
            }
        }else{
            clicked = false;
        }
        return false;
    }
}
