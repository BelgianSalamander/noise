package me.salamander.ourea.glsl.transpile;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BuiltinConstantType implements ConstantType{
    private final String name;
    private final List<Field> fields;

    public BuiltinConstantType(String name, Class<?> javaType, String... fieldNames){
        this.name = name;
        this.fields = new ArrayList<>();
        for(String fieldName : fieldNames){
            try {
                Field field = javaType.getDeclaredField(fieldName);
                field.setAccessible(true);
                fields.add(field);
            }catch (NoSuchFieldException e){
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String declare() {
        return "";
    }

    @Override
    public String create(Object obj, Map<ConstantType, List<Object>> constantPool, Map<Class<?>, ConstantType> constantTypes) {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("(");
        for (int i = 0; i < fields.size(); i++) {
            if(i != 0) sb.append(", ");
            Field field = fields.get(i);
            try {
                Object value = field.get(obj);
                sb.append(value);
            }catch (IllegalAccessException e){
                throw new RuntimeException(e);
            }
        }

        return sb.append(")").toString();
    }

    @Override
    public String getName() {
        return name;
    }
}
