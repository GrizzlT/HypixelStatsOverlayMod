package com.github.grizzlt.hypixelstatsoverlay.util;

import java.lang.reflect.Method;

public class ReflectionHelpingClass
{
    public static Method findMethodWithArgs(Class<?> clazz, Class<?>... signature) throws NoSuchMethodException
    {
        Method[] methods = clazz.getDeclaredMethods();

        for ( Method m : methods ) {
            Class<?>[] params = m.getParameterTypes();
            if ( params.length == signature.length ) {
                int i;
                for ( i = 0; i < signature.length && params[i].isAssignableFrom( signature[i] ); i++ ) {

                }
                if ( i == signature.length ) {
                    System.out.println("Found matching method: " + m.getName());
                    return m;
                }
            }
        }

        throw new NoSuchMethodException("Couldn't find the matching function in " + clazz.getName());
    }
}
