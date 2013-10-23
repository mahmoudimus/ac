package com.atlassian.plugin.connect.plugin.capabilities.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @since version
 */
public class CapabilityBeanUtils
{
    public static void copyFieldsByNameAndType(Object source, Object dest)
    {
        Class sourceClass = source.getClass();
        List<Field> sourceFields = getAllFieldsInObjectChain(sourceClass);

        Class destClass = dest.getClass();
        List<Field> destFields = getAllFieldsInObjectChain(destClass);


        if (!sourceFields.isEmpty() && !destFields.isEmpty())
        {
            List<String> alreadySet = new ArrayList<String>(sourceFields.size());

            for (Field sourceField : sourceFields)
            {
                Field destField = getFieldInObjectChain(destClass,sourceField.getName());
                if (destField != null && !alreadySet.contains(sourceField.getName()))
                {
                    boolean destAccess = destField.isAccessible();
                    boolean sourceAccess = sourceField.isAccessible();
                    destField.setAccessible(true);
                    sourceField.setAccessible(true);
                    try
                    {
                        if (sourceField.getType().equals(destField.getType()) && sourceField.get(source) != null)
                        {
                            Object something = sourceField.get(source);
                            destField.set(dest, sourceField.get(source));
                            alreadySet.add(sourceField.getName());
                        }
                    }
                    catch (IllegalAccessException e)
                    {
                        //just doesn't get set, but this should never happen
                    }
                    finally
                    {
                        destField.setAccessible(destAccess);
                        sourceField.setAccessible(sourceAccess);
                    }
                }
            }
        }
    }

    private static List<Field> getAllFieldsInObjectChain(Class someClass)
    {
        List<Field> fieldList = new ArrayList<Field>();


        for (Class myClass = someClass; myClass != Object.class; myClass = myClass.getSuperclass())
        {
            fieldList.addAll(Arrays.asList(myClass.getDeclaredFields()));
        }

        return fieldList;
    }

    private static Field getFieldInObjectChain(Class someClass, String fieldName)
    {
        Field field = null;

        for (Class myClass = someClass; myClass != Object.class; myClass = myClass.getSuperclass())
        {
            try {
                field = myClass.getDeclaredField(fieldName);
                break;
            }
            catch (NoSuchFieldException e)
            {
                //ignore
            }
        }
        return field;
    }
}
