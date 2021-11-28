package me.salamander.ourea.render;

import org.joml.Vector3f;

public class LinAlg {
    public static Vector3f perpendicularProjection(Vector3f from, Vector3f onto){
        return from.sub(parallelProjection(from, onto), new Vector3f());
    }

    public static Vector3f parallelProjection(Vector3f from, Vector3f onto){
        return onto.mul(from.dot(onto) / onto.dot(onto), new Vector3f());
    }
}
