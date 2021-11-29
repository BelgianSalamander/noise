package me.salamander.ourea.render;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class Camera {
    private final Vector3f position;
    private final Vector3f forward = new Vector3f(0.0f, 0.0f, 1.0f);

    private float yaw = (float) (Math.PI * 0.5f);
    private float pitch = 0;

    private static final Vector3f UP = new Vector3f(0.0f, 1.0f, 0.0f);

    private double[] previousPos;

    private static final float ROTATION_SPEED = 0.005f;
    private float MOVEMENT_SPEED = 500.0f;
    private static final float MAX_PITCH = (float) (Math.PI * 0.495f);
    private static final float MIN_PITCH = (float) (Math.PI * -0.495f);

    private Vector3f lockedPosition;
    private Vector3f lockedDirection;
    private boolean locked;

    public Camera(Window window){
        position = new Vector3f(0, 0, 0);

        initialisePreviousPosition(window);
    }

    public Camera(Window window, float x, float y, float z){
        position = new Vector3f(x, y, z);

        initialisePreviousPosition(window);

        glfwSetScrollCallback(window.getWindowHandle(), (w, xOffset, yOffset) -> {
            MOVEMENT_SPEED *= Math.pow(1.1, yOffset);
        });
    }


    private void initialisePreviousPosition(Window window){
        previousPos = window.getMousePosition();
    }

    public void handleInput(Window window, float dt){
        if(glfwGetKey(window.getWindowHandle(), GLFW_KEY_Z) == GLFW_PRESS){
            if(!locked){
                lockedDirection = new Vector3f(forward);
                lockedPosition = new Vector3f(position);

                locked = true;
            }
        }else{
            if(locked){
                locked =false;
            }
        }

        double[] currentPos = window.getMousePosition();
        double x = currentPos[0];
        double y = currentPos[1];

        if(x != previousPos[0] || y != previousPos[1]){
            double dx = x - previousPos[0];
            double dy = y - previousPos[1];

            yaw += dx * ROTATION_SPEED;
            pitch += dy * ROTATION_SPEED;

            if(pitch > MAX_PITCH){
                pitch = MAX_PITCH;
            }else if(pitch < MIN_PITCH){
                pitch = MIN_PITCH;
            }

            previousPos = currentPos;

            updateDirection();
        }

        float offsetX = 0, offsetY = 0, offsetZ = 0;

        if(window.isKeyPressed(GLFW_KEY_W)){
            offsetZ -= 1;
        }

        if(window.isKeyPressed(GLFW_KEY_S)){
            offsetZ += 1;
        }

        if(window.isKeyPressed(GLFW_KEY_A)){
            offsetX -= 1;
        }

        if(window.isKeyPressed(GLFW_KEY_D)){
            offsetX += 1;
        }

        if(window.isKeyPressed(GLFW_KEY_SPACE)){
            offsetY += 1;
        }

        if(window.isKeyPressed(GLFW_KEY_LEFT_SHIFT)){
            offsetY -= 1;
        }

        movePosition(offsetX, offsetY, offsetZ, dt * MOVEMENT_SPEED);
    }

    private void updateDirection() {
        float XZLength = (float) Math.cos(pitch);
        forward.x = (float) (XZLength * Math.cos(yaw));
        forward.y = (float) Math.sin(pitch);
        forward.z = (float) (XZLength * ( Math.sin(yaw)));
    }

    public Vector3f getPosition() {
        if(locked) return lockedPosition;
        return position;
    }

    public void setPosition(float x, float y, float z) {
        position.x = x;
        position.y = y;
        position.z = z;
    }

    private void movePosition(float offsetX, float offsetY, float offsetZ, float multiplier) {
        if(offsetX == 0 && offsetY == 0 && offsetZ == 0) return;
        if(offsetZ != 0){
            Vector3f movement = LinAlg.perpendicularProjection(forward, UP).normalize().mul(multiplier * offsetZ);
            position.add(movement);
        }

        if(offsetY != 0){
            position.y += offsetY * multiplier;
        }

        if(offsetX != 0){
            position.add(UP.cross(forward, new Vector3f()).normalize().mul(multiplier * offsetX));
        }

        //System.out.println(getViewMatrix());
    }

    private void dumpState(){
        System.out.println("###Camera State Dump###");
        System.out.println("Camera position: " + position);
        System.out.println("Yaw: " + yaw);
        System.out.println("Pitch: " + pitch);
        System.out.println("Forward: " + forward);
        System.out.println("UP: " + UP);
    }

    public Matrix4f getViewMatrix(){
        forward.normalize();

        Vector3f cameraRight = new Vector3f(UP);
        cameraRight.cross(forward).normalize();
        Vector3f cameraUp = new Vector3f(forward);
        cameraUp.cross(cameraRight).normalize();

        Matrix4f view = new Matrix4f(
                cameraRight.x, cameraUp.x, forward.x, 0.0f,
                cameraRight.y, cameraUp.y, forward.y, 0.0f,
                cameraRight.z, cameraUp.z, forward.z, 0.0f,
                -cameraRight.dot(position), -cameraUp.dot(position), -forward.dot(position), 1.0f);

        return view;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public Vector3f getForward() {
        if(locked) return lockedDirection;
        return forward;
    }
}
