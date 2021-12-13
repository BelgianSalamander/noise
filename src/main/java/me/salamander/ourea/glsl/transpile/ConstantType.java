package me.salamander.ourea.glsl.transpile;

import org.objectweb.asm.Type;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class ConstantType {
    private final String name;
    private final List<Field> fields;
    private final boolean nullable;
    private final List<String> referenceFields = new ArrayList<>();

    public ConstantType(Class<?> clazz, boolean nullable) {
        this.nullable = nullable;

        if (clazz.isPrimitive()) {
            throw new RuntimeException("Cannot create constant of primitive type");
        }

        Set<Field> allFields = new HashSet<>();

        Class<?> curr = clazz;
        while (curr != null) {
            allFields.addAll(Arrays.stream(curr.getDeclaredFields()).toList());
            curr = curr.getSuperclass();
        }

        for (Field field : allFields) {
            if ((field.getModifiers() & Modifier.FINAL) == 0) {
                System.err.println("WARNING: Field '" + field.getName() + " of type " + clazz.getName() + "' is not final");
            }

            if(!field.getType().isPrimitive()){
                referenceFields.add(field.getName());
            }

            field.setAccessible(true);
        }

        this.name = clazz.getName().replace('.', '_').replace('$', '_');
        this.fields = new ArrayList<>(allFields);
    }

    public String declare(){
        StringBuilder sb = new StringBuilder();
        sb.append("struct ").append(name).append(" {\n");
        if(nullable){
            sb.append("  bool isNull;\n");
        }
        for (Field field : fields) {
            if(field.getType().isPrimitive()){
                sb.append("  ").append(field.getType()).append(" ").append(field.getName()).append(";\n");
            }else{
                sb.append("  ").append("uint ").append(field.getName()).append(";\n");
            }
        }
        sb.append("};\n");
        return sb.toString();
    }

    public String create(Object obj, Map<ConstantType, List<Object>> constantPool, Map<Class<?>, ConstantType> constantTypes) {
        if(obj == null){
            if(nullable){
                return createNull();
            }else{
                throw new RuntimeException("Cannot create null constant");
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append("(");
        if(nullable){
            sb.append("false, ");
        }
        for (int i = 0; i < fields.size(); i++) {
            if(i != 0){
                sb.append(", ");
            }
            Field field = fields.get(i);
            if(field.getType().isPrimitive()){
                try {
                    sb.append(field.get(obj));
                }catch (IllegalAccessException e){
                    throw new RuntimeException(e);
                }
            }else{
                List<Object> objects = constantPool.get(constantTypes.get(field.getType()));
                if(objects == null){
                    throw new RuntimeException("Constant pool not initialized");
                }
                try {
                    sb.append(objects.indexOf(field.get(obj)));
                }catch (IllegalAccessException e){
                    throw new RuntimeException(e);
                }
                sb.append("u");
            }
        }
        sb.append(")");

        return sb.toString();
    }

    public String createNull(){
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append("(");
        sb.append("true");
        for (int i = 0; i < fields.size(); i++) {
            sb.append(", ");
            sb.append("0");
        }
        return sb.append(")").toString();
    }

    public boolean isNullable(){
        return nullable;
    }

    public String getName() {
        return name;
    }

    public List<String> referenceFields() {
        return referenceFields;
    }
}
