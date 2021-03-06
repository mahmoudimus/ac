package com.atlassian.plugin.connect.modules.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class ConnectReflectionHelper {
    public static boolean isParameterizedList(Type type) {
        return (type instanceof ParameterizedType && ((ParameterizedType) type).getRawType().equals(List.class));
    }

    public static void copyFieldsByNameAndType(Object source, Object dest) {
        Class sourceClass = source.getClass();
        List<Field> sourceFields = getAllFieldsInObjectChain(sourceClass);

        Class destClass = dest.getClass();
        List<Field> destFields = getAllFieldsInObjectChain(destClass);


        if (!sourceFields.isEmpty() && !destFields.isEmpty()) {
            List<String> alreadySet = new ArrayList<String>(sourceFields.size());

            for (Field sourceField : sourceFields) {
                Field destField = getFieldInObjectChain(destClass, sourceField.getName());
                if (destField != null && !alreadySet.contains(sourceField.getName())) {
                    destField.setAccessible(true);
                    sourceField.setAccessible(true);
                    try {
                        if (sourceField.getType().equals(destField.getType()) && sourceField.get(source) != null) {
                            destField.set(dest, sourceField.get(source));
                            alreadySet.add(sourceField.getName());
                        }
                    } catch (IllegalAccessException e) {
                        //just doesn't get set, but this should never happen
                    }
                }
            }
        }
    }

    public static List<Field> getAllFieldsInObjectChain(Class someClass) {
        List<Field> fieldList = new ArrayList<Field>();


        for (Class myClass = someClass; myClass != Object.class; myClass = myClass.getSuperclass()) {
            fieldList.addAll(Arrays.asList(myClass.getDeclaredFields()));
        }

        return fieldList;
    }

    public static List<Method> getAllGettersInObjectChain(Class someClass) {
        List<Method> methodList = new ArrayList<>();

        for (Class myClass = someClass; myClass != Object.class; myClass = myClass.getSuperclass()) {
            methodList.addAll(Arrays.asList(myClass.getDeclaredMethods()).stream().filter(method -> method.getName().startsWith("get")).collect(toList()));
        }

        return methodList;
    }

    private static Field getFieldInObjectChain(Class someClass, String fieldName) {
        Field field = null;

        for (Class myClass = someClass; myClass != Object.class; myClass = myClass.getSuperclass()) {
            try {
                field = myClass.getDeclaredField(fieldName);
                break;
            } catch (NoSuchFieldException e) {
                //ignore
            }
        }
        return field;
    }
}
