package me.salamander.ourea.glsl.transpile;

import java.util.List;
import java.util.Map;

public interface ConstantType {
    String declare();
    String create(Object obj, Map<ConstantType, List<Object>> constantPool, Map<Class<?>, ConstantType> constantTypes);
    String getName();
}
